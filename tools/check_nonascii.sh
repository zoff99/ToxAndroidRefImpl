#!/usr/bin/env bash
#
# check_nonascii.sh
#
# Non-ASCII check for this repository's Java source files (*.java).
#
# Why this exists:
#   A review diff shows glyphs, not code points. Right-to-left overrides can
#   make a line read as something completely different from what it compiles
#   to ("Trojan Source", CVE-2021-42574), zero-width characters can split an
#   identifier into two, and a private-use or tag character is invisible in
#   most editors. A look-alike (homoglyph) letter can quietly change a string
#   or a name. And "useless" typographic UTF-8 -- curly quotes, em/en dashes,
#   ellipsis, non-breaking spaces -- has no place in compiled source either.
#
# The rule:
#   * Java source files are pure ASCII. EVERY non-ASCII character is reported
#     with file:line:position so it can be reviewed. Use \uXXXX escapes in
#     string literals and plain ASCII in code and comments.
#   * Hidden / bidi / zero-width / private-use / tag characters are reported as
#     DANGEROUS. The GLOBAL exception list can never permit them; only an
#     explicit PER-FILE exception (scoped to one known file) may allow one.
#
# Only *.java files are checked for now (translations, store metadata, docs
# and native code are intentionally out of scope).
#
# Configure what is allowed in the EXCEPTION LIST block just below.
#
# Usage:
#   tools/check_nonascii.sh                 # all tracked *.java files
#   tools/check_nonascii.sh -v              # also list allowed non-ASCII chars
#   tools/check_nonascii.sh --staged        # only staged *.java (staged blobs)
#   tools/check_nonascii.sh -a 00E9,2014    # extra global code points (this run)
#   tools/check_nonascii.sh path [path...]  # only these files/dirs
#   tools/check_nonascii.sh -b              # raw byte mode (allowed = 00-FF)
#
# As a pre-commit hook (checks what is about to be committed):
#   printf '#!/bin/sh\nexec tools/check_nonascii.sh --staged\n' \
#       > .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
#
# Requires Bash 4+ (associative arrays).
#
# Exit status: 0 clean, 1 disallowed characters found, 2 setup/usage error.

set -u

_HOME2_=$(dirname "$0")
_HOME_=$(cd "$_HOME2_" && pwd)
basedir="$_HOME_/.."
cd "$basedir" || exit 2

# ===========================================================================
# EXCEPTION LIST  --  EDIT THIS BLOCK to configure what is allowed.
# ===========================================================================
# Code points are hexadecimal: 00E4, U+00E4 and 0x00E4 are all accepted.
# Separators inside the strings: space, comma, colon or semicolon.
# Keep comments OUTSIDE the quoted strings (a '#' inside quotes is literal).
#
# (1) GLOBAL exceptions -- allowed in EVERY Java file.
#     Dangerous hidden/bidi characters placed here are REFUSED (warned and
#     ignored). To allow a dangerous character you must use a per-file entry.
#
# German umlauts (lower + upper case) and sharp s:
#   00E4 ae  00C4 AE  00F6 oe  00D6 OE  00FC ue  00DC UE  00DF sharp-s
ALLOW_GLOBAL="00E4 00C4 00F6 00D6 00FC 00DC 00DF"
#
# Other approved characters currently present in the source:
#   221E infinity   00E6 ae-ligature   00C6 AE-ligature   00B0 degree sign
#   2014 em dash (TODO: replace with '-' where it is only a comment)
#   00A7 section sign   2192 rightwards arrow   2190 leftwards arrow
#   1F354 hamburger   1F44D thumbs up   1F61C winking-tongue emoji
ALLOW_GLOBAL="$ALLOW_GLOBAL 221E 00E6 00C6 00B0 2014 00A7 2192 2190 1F354 1F44D 1F61C"
#
# (2) PER-FILE exceptions -- allowed ONLY in the named file (path relative to
#     the repo root). These MAY include otherwise-dangerous characters (e.g.
#     FE0F variation selector) because they are scoped to one known file.
#     Use sparingly and keep them exact. Format:  "path/File.java|HEX HEX ..."
#
ALLOW_PERFILE=(
  # NetProfiler.java -- exactly the emoji present today (incl. FE0F modifier):
  #   23F0 alarm clock  26A0 warning  26A1 high voltage  270B raised hand
  #   2753 question mark  FE0F variation selector  1F465 busts  1F4A4 sleeping
  #   1F4AC speech balloon  1F4C1 file folder  1F4DE telephone  1F4F1 mobile phone
  #   1F50A speaker  1F514 bell  1F7E2 green circle
  "android-refimpl-app/app/src/main/java/com/zoffcc/applications/trifa/NetProfiler.java|23F0 26A0 26A1 270B 2753 FE0F 1F465 1F4A4 1F4AC 1F4C1 1F4DE 1F4F1 1F50A 1F514 1F7E2"
  # StartMainActivityWrapperTest.java -- MOCK_TEST_MSG_02 umbrella (2602 + FE0F):
  "android-refimpl-app/app/src/androidTest/java/com/zoffcc/applications/trifa/StartMainActivityWrapperTest.java|2602 FE0F"
  # EmojiGridViewCustom.java -- author-name letters (0110 D-stroke, 0107 c-acute):
  "android-refimpl-app/app/src/main/java/com/vanniktech/emoji/EmojiGridViewCustom.java|0110 0107"
  # NativeAudio.java -- FE55 small colon (looks like ':' but a distinct code point):
  "android-refimpl-app/app/src/main/java/com/zoffcc/applications/nativeaudio/NativeAudio.java|FE55"
  # CameraWrapper.java -- 00D7 multiplication sign (resolution strings):
  "android-refimpl-app/app/src/main/java/com/zoffcc/applications/trifa/CameraWrapper.java|00D7"
  # FriendSelectSingleAdapter.java -- 00B7 middle dot (separator in labels):
  "android-refimpl-app/app/src/main/java/com/zoffcc/applications/trifa/FriendSelectSingleAdapter.java|00B7"
)
# ===========================================================================
# END EXCEPTION LIST
# ===========================================================================

# ---------------------------------------------------------------------------
# Options
# ---------------------------------------------------------------------------
usage() {
    cat <<'EOF'
Usage: check_nonascii.sh [-a allowed_hex] [-b] [-v] [--staged] [-h] [path ...]

Checks this repository's Java source files (*.java) for non-ASCII characters.
Java source must be pure ASCII; every non-ASCII character is reported, and
hidden / bidi / zero-width / private-use characters are flagged DANGEROUS.

What is allowed is configured in the EXCEPTION LIST block at the top of this
script: a GLOBAL list (any file) and a PER-FILE list (one named file). Only a
per-file entry may permit an otherwise-dangerous character.

Options:
  -a allowed_hex   Extra GLOBAL code points for this run (hex). Separators may
                   be spaces, commas, colons or semicolons. Accepts 00E9, 0xE9,
                   U+00E9. Dangerous characters are never permitted globally.
  -b               Byte mode: treat files as raw bytes, allowed values 00-FF.
  -v, -l           Also list non-ASCII characters that ARE allowed (marked
                   "allowed"); does not change the exit status.
  --staged         Check only staged *.java files, using the staged blobs
                   (what is about to be committed), not the working tree.
  -h, --help       Show this help.

With no path arguments, all tracked *.java files are checked (or the staged
ones with --staged). With path arguments, only those files/dirs are checked.

Output:
  file:line:position: char='X' hex=YYYY
  file:line:position: char='X' hex=YYYY DANGEROUS: <what it is>

Exit status: 0 clean, 1 disallowed characters found, 2 setup/usage error.
EOF
}

allow_arg=''
byte_mode=0
list_allowed=0
staged=0

OPTIND=1
while getopts ':a:blvh-:' opt; do
    case $opt in
        a) allow_arg=$OPTARG ;;
        b) byte_mode=1 ;;
        l|v) list_allowed=1 ;;
        h) usage; exit 0 ;;
        -)
            case $OPTARG in
                staged)  staged=1 ;;
                verbose) list_allowed=1 ;;
                help)    usage; exit 0 ;;
                *) printf 'Error: unknown option --%s.\n' "$OPTARG" >&2; usage >&2; exit 2 ;;
            esac
            ;;
        :) printf 'Error: option -%s requires an argument.\n' "$OPTARG" >&2; exit 2 ;;
        \?) printf 'Error: unknown option -%s.\n' "$OPTARG" >&2; usage >&2; exit 2 ;;
    esac
done
shift $((OPTIND - 1))
paths=("$@")

if (( BASH_VERSINFO[0] < 4 )); then
    echo 'Error: bash 4 or newer is required.' >&2
    exit 2
fi

# In character mode we need a UTF-8 locale so Bash sees multibyte characters
# correctly. If none is found, fall back to byte mode.
if (( byte_mode == 0 )); then
    utf8_locale=$(locale -a 2>/dev/null | grep -iE '^(C|en_US)\.(UTF-?8)$' | head -n1)
    if [[ -n $utf8_locale ]]; then
        export LC_ALL=$utf8_locale
    else
        echo 'Warning: no UTF-8 locale found; falling back to byte mode.' >&2
        byte_mode=1
    fi
fi

# ---------------------------------------------------------------------------
# Characters that are never acceptable in source code (full set).
# ---------------------------------------------------------------------------
declare -A DANGER=()
danger_hex='
00AD 034F 061C 115F 1160 180E
200B 200C 200D 200E 200F
202A 202B 202C 202D 202E
2060 2061 2062 2063 2064
2066 2067 2068 2069
206A 206B 206C 206D 206E 206F
3164 FFA0 FFF9 FFFA FFFB FEFF
1D159 1D173 1D174 1D175 1D176 1D177 1D178 1D179 1D17A
'
for t in $danger_hex; do DANGER[$((16#$t))]=1; done

danger_name() {
    case $1 in
        0x202A|0x202B|0x202D|0x202E) echo "bidi override (Trojan Source)" ;;
        0x202C)                      echo "bidi pop (Trojan Source)" ;;
        0x2066|0x2067|0x2068|0x2069) echo "bidi isolate (Trojan Source)" ;;
        0x200E|0x200F|0x061C)        echo "invisible direction mark" ;;
        0x200B|0x200C|0x200D|0x2060|0xFEFF) echo "zero-width / no-break space character" ;;
        0x00AD)                      echo "soft hyphen (invisible)" ;;
        0x034F)                      echo "combining grapheme joiner (invisible)" ;;
        0x115F|0x1160|0x3164|0xFFA0) echo "Hangul filler (looks like a space)" ;;
        0xFFF9|0xFFFA|0xFFFB)        echo "interlinear annotation control" ;;
        0x2061|0x2062|0x2063|0x2064) echo "invisible mathematical operator" ;;
        0x206A|0x206B|0x206C|0x206D|0x206E|0x206F) echo "deprecated format control" ;;
        0xFE0[0-9A-Fa-f])            echo "variation selector (invisible emoji modifier)" ;;
        *)                           echo "invisible or private-use character" ;;
    esac
}

is_dangerous() {
    local c=$1
    (( c >= 0x80    && c <= 0x9F    )) && return 0   # C1 controls
    (( c >= 0xE000  && c <= 0xF8FF  )) && return 0   # private use area
    (( c >= 0xFE00  && c <= 0xFE0F  )) && return 0   # variation selectors
    (( c >= 0xE0000 && c <= 0xE01EF )) && return 0   # tags + variation supplement
    (( c >= 0xF0000 && c <= 0x10FFFD )) && return 0  # supplementary private use
    [[ -n ${DANGER[$c]:-} ]]
}

# The dangerous characters as raw UTF-8 byte sequences. This search runs in
# both modes and also catches them in a file that is not valid UTF-8 at all.
# The variation-selector alternative is optional so a per-file exception can
# drop it (the character report still catches any non-excepted selector).
build_danger_bytes() {
    local include_vs=$1
    local p=$'\xe2\x80[\x8b-\x8f\xaa-\xae]|\xe2\x81[\xa0-\xaf]|\xef\xbb\xbf|\xc2\xad|\xd8\x9c|\xe1\xa0\x8e|\xcd\x8f|\xe1\x85[\x9f\xa0]|\xe3\x85\xa4|\xef\xbe\xa0|\xef\xbf[\xb9-\xbb]'
    if (( include_vs )); then
        p=$p$'|\xef\xb8[\x80-\x8f]'
    fi
    p=$p$'|\xf3\xa0[\x80-\x81]'
    printf '%s' "$p"
}
danger_bytes=$(build_danger_bytes 1)

# ---------------------------------------------------------------------------
# Build the allowlists from the EXCEPTION LIST block (+ -a command line).
# ---------------------------------------------------------------------------
declare -A allowed=()     # global, non-dangerous only
declare -A perfile=()     # relpath -> "hex hex ..." (may include dangerous)

# Global: ALLOW_GLOBAL plus any -a argument. Dangerous values are refused.
IFS=$' \t\n,;:' read -r -a _gtokens <<< "$ALLOW_GLOBAL $allow_arg"
for token in "${_gtokens[@]}"; do
    [[ -z $token ]] && continue
    t=$token
    t=${t#0x}; t=${t#0X}; t=${t#U+}; t=${t#u+}
    if [[ ! $t =~ ^[0-9A-Fa-f]+$ ]]; then
        printf 'Error: allowed value %s is not hexadecimal.\n' "'$token'" >&2
        exit 2
    fi
    val=$((16#$t))
    if (( byte_mode && val > 255 )); then
        printf 'Warning: allowed value 0x%X is > 0xFF; ignored in byte mode.\n' "$val" >&2
        continue
    fi
    if (( byte_mode == 0 )) && is_dangerous "$val"; then
        printf 'Warning: U+%04X is never allowed globally (%s); ignored. Use a per-file exception if intentional.\n' \
            "$val" "$(danger_name "$(printf '0x%04X' "$val")")" >&2
        continue
    fi
    allowed[$val]=1
done

# Per-file entries: "path|hex hex".
for entry in ${ALLOW_PERFILE[@]+"${ALLOW_PERFILE[@]}"}; do
    [[ -z $entry ]] && continue
    pf_path=${entry%%|*}
    pf_hex=${entry#*|}
    if [[ $pf_path == "$entry" || -z $pf_path ]]; then
        printf 'Error: per-file exception needs "path|hex": %s\n' "'$entry'" >&2
        exit 2
    fi
    perfile[$pf_path]=$pf_hex
done

# ---------------------------------------------------------------------------
# Collect the Java file list.
# ---------------------------------------------------------------------------
list_files() {
    if (( ${#paths[@]} )); then
        printf '%s\n' "${paths[@]}"
        return
    fi
    if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        if (( staged )); then
            git diff --cached --name-only --diff-filter=ACMR -- '*.java'
        else
            git ls-files -- '*.java'
        fi
        return
    fi
    find . \( -name .git -o -name build -o -name .gradle -o -name .idea \) -prune -o \
         -type f -name '*.java' -print | sed 's|^\./||'
}

files=$(list_files) || exit 2
[ -n "$files" ] || { echo "no Java files to check"; exit 0; }

# In --staged mode the index is what matters, not the working tree: a file can
# be staged with one content and edited afterwards. Check the staged blobs.
TMPD=''
if (( staged )); then
    TMPD=$(mktemp -d) || exit 2
    trap 'rm -rf "$TMPD"' EXIT
fi

# ---------------------------------------------------------------------------
# Per-file scan (engine).  $1 = path to read, $2 = repo-relative path (used to
# look up per-file exceptions).
# ---------------------------------------------------------------------------
scan_file() {
    local file=$1 rel=$2
    local rc=0

    # Effective allowlist for this file = global + its per-file exceptions.
    local -A eff=()
    local k
    for k in ${allowed[@]+"${!allowed[@]}"}; do eff[$k]=1; done
    # Dangerous code points explicitly excepted for THIS file (e.g. FE0F).
    local -A override=()
    local pfhex=${perfile[$rel]:-}
    if [[ -n $pfhex ]]; then
        local t val
        IFS=$' \t\n,;:' read -r -a _ptokens <<< "$pfhex"
        for t in "${_ptokens[@]}"; do
            [[ -z $t ]] && continue
            t=${t#0x}; t=${t#0X}; t=${t#U+}; t=${t#u+}
            if [[ ! $t =~ ^[0-9A-Fa-f]+$ ]]; then
                printf 'Error: per-file hex %s is not hexadecimal.\n' "'$t'" >&2
                return 2
            fi
            val=$((16#$t))
            eff[$val]=1
            if (( byte_mode == 0 )) && is_dangerous "$val"; then
                override[$val]=1
            fi
        done
    fi

    # Danger byte pattern, dropping the variation-selector alternative when
    # this file has a selector exception (the char report still catches any
    # selector that is not excepted).
    local db=$danger_bytes
    if (( byte_mode == 0 )) && (( ${#override[@]} )); then
        local o has_vs=0
        for o in "${!override[@]}"; do
            if (( o >= 0xFE00 && o <= 0xFE0F )); then has_vs=1; fi
        done
        (( has_vs )) && db=$(build_danger_bytes 0)
    fi

    # 1. Raw byte search for characters that must never appear. Independent of
    #    locale and of whether the file decodes as UTF-8.
    local hit
    while IFS= read -r hit; do
        [[ -z $hit ]] && continue
        printf '%s:%s: DANGEROUS: hidden/bidi byte sequence (see the character report below)\n' \
            "$file" "${hit%%:*}"
        (( rc == 0 )) && rc=1
    done < <(LC_ALL=C grep -anE "$db" -- "$file" 2>/dev/null)

    # 2. Character (or byte) report with positions. Only lines that contain a
    #    high byte are examined, which keeps this fast on big files.
    local hits grep_rc
    hits=$(LC_ALL=C grep -anE $'[\x80-\xff]' -- "$file" 2>/dev/null)
    grep_rc=$?
    if (( grep_rc > 1 )); then
        printf 'Error: cannot scan file %s.\n' "'$file'" >&2
        return 2
    fi
    [[ -z $hits ]] && return $rc

    local lineno line len idx c code hex
    while IFS= read -r hit; do
        [[ -z $hit ]] && continue
        lineno=${hit%%:*}
        line=${hit#*:}
        len=${#line}

        for ((idx = 0; idx < len; idx++)); do
            c=${line:idx:1}
            [[ -n $c ]] || continue

            code=0
            printf -v code '%d' "'$c" 2>/dev/null || code=0
            if (( code == 0 )); then
                code=$(LC_ALL=C printf '%d' "'$c" 2>/dev/null) || code=0
                [[ $code =~ ^-?[0-9]+$ ]] || code=0
            fi
            (( code < 0 )) && code=$(( code & 0xFF ))
            (( code > 127 )) || continue

            if (( byte_mode )); then
                hex=$(printf '%02X' "$code")
            else
                hex=$(printf '%04X' "$code")
            fi

            # Dangerous unless explicitly excepted for this exact file.
            if (( byte_mode == 0 )) && is_dangerous "$code" && [[ -z ${override[$code]:-} ]]; then
                printf "%s:%d:%d: char='%s' hex=%s DANGEROUS: %s\n" \
                    "$file" "$lineno" "$((idx + 1))" "$c" "$hex" \
                    "$(danger_name "$(printf '0x%s' "$hex")")"
                (( rc == 0 )) && rc=1
                continue
            fi

            if [[ -z ${eff[$code]:-} ]]; then
                printf "%s:%d:%d: char='%s' hex=%s\n" \
                    "$file" "$lineno" "$((idx + 1))" "$c" "$hex"
                (( rc == 0 )) && rc=1
            elif (( list_allowed )); then
                printf "%s:%d:%d: char='%s' hex=%s allowed\n" \
                    "$file" "$lineno" "$((idx + 1))" "$c" "$hex"
            fi
        done
    done <<< "$hits"

    return $rc
}

n_files=0
n_skip=0
bad_files=0
violations=0

while IFS= read -r f; do
    [ -n "$f" ] || continue
    [ -f "$f" ] || continue

    # A file with a NUL byte is not text, whatever it is called.
    if ! LC_ALL=C tr -d '\000' < "$f" | cmp -s - "$f"; then
        n_skip=$((n_skip + 1))
        continue
    fi

    target=$f
    if [ -n "$TMPD" ]; then
        target="$TMPD/$f"
        mkdir -p "$(dirname "$target")"
        git show ":$f" > "$target" 2>/dev/null || continue
    fi

    n_files=$((n_files + 1))
    out=$(scan_file "$target" "$f" 2>&1)
    rc=$?
    [ -n "$TMPD" ] && out=${out//"$TMPD/"/}

    if [ -n "$out" ]; then
        printf '%s\n' "$out"
        if (( rc == 1 )); then
            bad_files=$((bad_files + 1))
            violations=$((violations + $(printf '%s\n' "$out" | grep 'hex=' | grep -vc ' allowed$')))
        fi
    fi
    (( rc == 2 )) && { echo "error while checking $f" >&2; exit 2; }
done <<< "$files"

echo "----------------------------------------------------------------"
printf 'checked %d Java file(s), %d binary/NUL skipped\n' "$n_files" "$n_skip"

if (( bad_files )); then
    printf 'FAIL: %d disallowed character(s) in %d file(s)\n' "$violations" "$bad_files"
    echo
    echo "Java source must be pure ASCII: use \\uXXXX escapes in string literals"
    echo "and plain ASCII in code and comments. Hidden or bidi characters are"
    echo "rejected unless explicitly excepted per-file in the EXCEPTION LIST."
    exit 1
fi

echo "OK: no disallowed non-ASCII characters in Java source"
exit 0
