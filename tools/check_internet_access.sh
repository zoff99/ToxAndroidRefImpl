#!/usr/bin/env bash
#
# check_internet_access.sh
#
# List every place in OUR Java source that could access the internet, so the
# access points can be inspected and a malicious commit that adds one stands out.
#
# Scope:
#   android-refimpl-app/app/src/main/java  -- the app module's Java source,
#   INCLUDING the in-tree third-party code (netcipher, privacyfriendlynetmonitor,
#   ...). NOT the source of gradle dependencies, and NOT native JNI code.
#
# Output (one line per finding, sorted):
#   <file>:<line>: [CATEGORY] <source snippet>
#
# Categories:
#   HTTP-OKHTTP    okhttp3 client / request execution
#   HTTP-JAVANET   java.net URL / HttpURLConnection / openConnection / openStream
#   HTTP-APACHE    org.apache.http / commons-httpclient
#   SOCKET         java.net Socket / ServerSocket / DatagramSocket / Proxy / InetSocketAddress
#   DNS            InetAddress name / address resolution
#   TLS-TRUST      TrustManager / HostnameVerifier / SSLContext  (incl. trust-all = insecure)
#   TOR-PROXY      Orbot / netcipher / Tor socket plumbing
#   WEBVIEW        WebView.loadUrl / loadDataWithBaseURL / postUrl
#   DOWNLOAD       DownloadManager
#   CONNECTIVITY   ConnectivityManager / NetworkInfo / NetworkRequest  (network STATE, not traffic)
#
# Excluded as non-access (false positives):
#   android.net.Uri (URI parsing), URLConnection.guessContentType* / getFileNameMap
#   (MIME lookup), UnknownHostException, URLEncoder / URLDecoder, and comment lines.
#
# Usage:
#   tools/check_internet_access.sh                  # full report to stdout
#   tools/check_internet_access.sh --usage-only     # only real call sites (no imports)
#   tools/check_internet_access.sh -o report.txt    # write report to a file
#   tools/check_internet_access.sh --baseline b.txt # fail (exit 1) on findings NOT in baseline
#
# Baseline workflow (catch a malicious commit):
#   tools/check_internet_access.sh -o tools/internet_access_baseline.txt   # snapshot now
#   ...later / in CI...
#   tools/check_internet_access.sh --baseline tools/internet_access_baseline.txt
#   -> any NEW access point (file + category + snippet) fails the check.
#
# Exit status: 0 report generated (or no new findings vs baseline),
#              1 new findings vs baseline, 2 setup error.

set -u

_HOME2_=$(dirname "$0")
_HOME_=$(cd "$_HOME2_" && pwd)
basedir="$_HOME_/.."
cd "$basedir" || exit 2

SCAN_ROOT="android-refimpl-app/app/src/main/java"

# --- detection patterns: "CATEGORY|extended-regex" (first match wins, in order) ---
PATTERNS=(
    'HTTP-OKHTTP|okhttp3|OkHttpClient|\.newCall\('
    'HTTP-JAVANET|HttpURLConnection|HttpsURLConnection|\.openConnection\(|\.openStream\(|new[[:space:]]+URL[[:space:]]*\(|import[[:space:]]+java\.net\.URL;'
    'HTTP-APACHE|org\.apache\.http|DefaultHttpClient|HttpClientBuilder|AndroidHttpClient|org\.apache\.commons\.httpclient'
    'SOCKET|java\.net\.Socket|java\.net\.ServerSocket|java\.net\.DatagramSocket|java\.net\.MulticastSocket|new[[:space:]]+Socket[[:space:]]*\(|new[[:space:]]+ServerSocket[[:space:]]*\(|new[[:space:]]+DatagramSocket[[:space:]]*\(|java\.net\.Proxy|new[[:space:]]+Proxy[[:space:]]*\(|java\.net\.InetSocketAddress|new[[:space:]]+InetSocketAddress[[:space:]]*\('
    'DNS|InetAddress|getAllByName|getByName|getByAddress'
    'TLS-TRUST|TrustManager|X509TrustManager|HostnameVerifier|AllowAllHostnameVerifier|sslSocketFactory|SSLContext|trustAllCerts|checkServerTrusted|checkClientTrusted|setHostnameVerifier'
    'TOR-PROXY|OrbotHelper|StrongBuilder|StrongOkHttpClientBuilder|TorSocket|info\.guardianproject'
    'WEBVIEW|WebView|loadUrl|loadDataWithBaseURL|postUrl'
    'DOWNLOAD|DownloadManager'
    'CONNECTIVITY|ConnectivityManager|NetworkInfo|NetworkRequest|NetworkCapabilities|registerNetworkCallback|getActiveNetworkInfo|getAllNetworks'
)

# --- benign uses to exclude (URI parsing / MIME lookup / encoding / exception) ---
BENIGN='android\.net\.Uri|guessContentTypeFrom|getFileNameMap|UnknownHostException|URLEncoder|URLDecoder'

# --- "real usage" markers (for --usage-only; otherwise imports are listed too) ---
USAGE='new[[:space:]]|\.openConnection\(|\.openStream\(|\.newCall\(|\.execute\(|\.enqueue\(|\.connect\(|getByName|getAllByName|getByAddress|getActiveNetworkInfo|getAllNetworks|registerNetworkCallback|sslSocketFactory|setHostnameVerifier|\.loadUrl\(|loadDataWithBaseURL|postUrl|getInstance\('

usage() { sed -n '2,46p' "$0"; }

usage_only=0
baseline=''
outfile=''
while [ $# -gt 0 ]; do
    case "$1" in
        --usage-only) usage_only=1 ;;
        --baseline)   shift; baseline=${1:-} ;;
        -o|--output)  shift; outfile=${1:-} ;;
        -h|--help)    usage; exit 0 ;;
        *) printf 'Error: unknown argument %s\n' "'$1'" >&2; usage >&2; exit 2 ;;
    esac
    shift
done

[ -d "$SCAN_ROOT" ] || { echo "Error: source root not found: $SCAN_ROOT" >&2; exit 2; }

tmpd=$(mktemp -d) || exit 2
trap 'rm -rf "$tmpd"' EXIT

# Join patterns with TAB for awk (regexes may contain '|', so split on first '|').
joined=$(printf '%s\t' "${PATTERNS[@]}")
export CHK_PATTERNS="$joined" CHK_BENIGN="$BENIGN" CHK_USAGE="$USAGE" CHK_USAGE_ONLY="$usage_only"

# grep all candidate lines, then classify / filter in awk.
grep_args=(-rnE --include='*.java')
for entry in "${PATTERNS[@]}"; do grep_args+=(-e "${entry#*|}"); done
grep_args+=("$SCAN_ROOT")

grep "${grep_args[@]}" 2>/dev/null | awk '
BEGIN {
    n = split(ENVIRON["CHK_PATTERNS"], arr, "\t");
    for (i = 1; i <= n; i++) {
        if (arr[i] == "") continue;
        p = index(arr[i], "|");
        cat[i] = substr(arr[i], 1, p - 1);
        re[i]  = substr(arr[i], p + 1);
    }
    benign = ENVIRON["CHK_BENIGN"];
    usg    = ENVIRON["CHK_USAGE"];
    uo     = ENVIRON["CHK_USAGE_ONLY"];
}
{
    i1 = index($0, ":"); file = substr($0, 1, i1 - 1); rest = substr($0, i1 + 1);
    i2 = index(rest, ":"); line = substr(rest, 1, i2 - 1); content = substr(rest, i2 + 1);
    t = content; sub(/^[ \t]+/, "", t);
    if (t ~ /^\/\// || t ~ /^\*/ || t ~ /^\/\*/) next;   # comment lines
    if (t ~ benign) next;                                # benign URI / MIME / encoding
    if (uo == "1" && t !~ usg) next;                     # usage-only filter
    c = "";
    for (i = 1; i <= n; i++) { if (re[i] != "" && t ~ re[i]) { c = cat[i]; break; } }
    if (c == "") next;
    printf "%s:%s: [%s] %s\n", file, line, c, t;
}
' | sort > "$tmpd/findings"

total=$(wc -l < "$tmpd/findings" | tr -d ' ')

# --- baseline comparison (if requested) ---
new_count=0
if [ -n "$baseline" ]; then
    if [ ! -f "$baseline" ]; then
        echo "Error: baseline file not found: $baseline" >&2
        exit 2
    fi
    normalize() { grep -E ':[0-9]+: \[' "$1" 2>/dev/null | sed -E 's/:[0-9]+: \[/: [/' | sort -u; }
    normalize "$tmpd/findings" > "$tmpd/cur.norm"
    normalize "$baseline"      > "$tmpd/base.norm"
    comm -23 "$tmpd/cur.norm" "$tmpd/base.norm" > "$tmpd/new.norm"
    new_count=$(wc -l < "$tmpd/new.norm" | tr -d ' ')
fi

# --- build the report ---
report="$tmpd/report"
{
    echo "# Internet-access audit of app Java source (incl. in-tree third-party; excl. gradle deps & native JNI)"
    echo "# root: $SCAN_ROOT"
    echo "# mode: $( [ "$usage_only" = 1 ] && echo usage-only || echo full )"
    echo "# total findings: $total"
    echo "#"
    cat "$tmpd/findings"
    echo "#"
    echo "# summary by category:"
    sed -E 's/.*: \[([A-Z-]+)\].*/\1/' "$tmpd/findings" | sort | uniq -c | sort -rn | sed 's/^/#   /'
    if [ -n "$baseline" ]; then
        echo "#"
        echo "# baseline: $baseline"
        echo "# NEW findings not in baseline: $new_count"
        if (( new_count > 0 )); then
            sed 's/^/#   NEW: /' "$tmpd/new.norm"
        fi
    fi
} > "$report"

if [ -n "$outfile" ]; then
    cp "$report" "$outfile" || { echo "Error: cannot write $outfile" >&2; exit 2; }
    echo "report written to $outfile ($total findings)"
else
    cat "$report"
fi

if [ -n "$baseline" ]; then
    if (( new_count > 0 )); then
        echo "FAIL: $new_count new internet-access point(s) not in baseline." >&2
        exit 1
    fi
    echo "OK: no new internet-access points vs baseline." >&2
fi
exit 0
