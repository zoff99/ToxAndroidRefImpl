#!/usr/bin/env bash
#
# check_apk_contents.sh
#
# Build the debug APK and verify that the native JNI libraries (*.so) and the
# bundled asset files (fonts / ML model) are EXACTLY the allowlisted sets --
# nothing extra, and nothing missing.
#
# Why this exists:
#   A malicious (or accidental) commit could pull a new native library or asset
#   into the APK via a gradle dependency, or drop one we rely on. The APK is the
#   ground truth of what actually ships, so we build it and inspect its contents
#   with `unzip -l`.
#
# What it does:
#   1. Runs ./gradlew assembleDebug (unless --skip-build).
#   2. Finds the newest debug APK and rejects it if it is stale.
#   3. Lists the APK contents with `unzip -l`.
#   4. Native libs (lib/<abi>/*.so): FAIL on any .so not allowlisted (unexpected)
#      and on any allowlisted .so absent from the APK (missing).
#   5. Assets (assets/**): FAIL on any asset not allowlisted and on any
#      allowlisted asset absent. (assets/dexopt/* build profiles are ignored.)
#
# Configure the allowlists in the blocks below. Each is BOTH the permitted set
# and the required set: to add a new lib/asset intentionally, add it there; to
# drop one, remove it.
#
# Usage:
#   tools/check_apk_contents.sh                 # build assembleDebug, then check
#   tools/check_apk_contents.sh --skip-build    # check the existing APK only
#   tools/check_apk_contents.sh --max-age 1800  # stale threshold (seconds)
#   tools/check_apk_contents.sh --apk PATH      # check a specific APK (no build)
#   tools/check_apk_contents.sh --list          # just print libs + assets
#
# Requires: bash, unzip, and a working Android/gradle build environment.
#
# Exit status: 0 exactly the allowlisted contents, 1 mismatch, 2 build/setup error.

set -u

_HOME2_=$(dirname "$0")
_HOME_=$(cd "$_HOME2_" && pwd)
basedir="$_HOME_/.."
cd "$basedir" || exit 2

GRADLE_DIR="android-refimpl-app"

# ===========================================================================
# ALLOWED JNI LIBS  --  EDIT THIS BLOCK to configure the expected native libs.
# ===========================================================================
# Permitted AND required. File names only (the lib/<abi>/ prefix is stripped).
# Ground truth from a real assembleDebug APK (4 ABIs: arm64-v8a, armeabi-v7a,
# x86, x86_64 -- each carries the same set):
ALLOWED_JNI_LIBS=(
    libiocipher2.so
    libjni-c-toxcore.so
    libloggingstdout.so
    libnative-audio-jni.so
    libsqlitejdbc.so
    libyuv_converter.so
)
# ===========================================================================

# ===========================================================================
# ALLOWED ASSETS  --  EDIT THIS BLOCK to configure the expected asset files.
# ===========================================================================
# Permitted AND required. Full paths inside the APK (assets/...). These are the
# bundled fonts and the ML model. assets/dexopt/* (build-generated baseline
# profiles) are ignored automatically and need not be listed.
ALLOWED_ASSETS=(
    assets/fonts/fontawesome-font-v5.3.1.1.ttf
    assets/fonts/google-material-font-v3.0.1.0.original.ttf
    assets/fonts/material-design-iconic-font-v2.2.0.ttf
    assets/fonts/materialdrawerfont-font-v5.0.0.ttf
    assets/selfiesegmentation_mlkit-256x256-2021_01_19-v1215.f16.tflite
)
# ===========================================================================

usage() { sed -n '2,34p' "$0"; }

skip_build=0
list_only=0
max_age=3600
apk_override=''

while [ $# -gt 0 ]; do
    case "$1" in
        --skip-build) skip_build=1 ;;
        --list)       list_only=1; skip_build=1 ;;
        --max-age)    shift; max_age=${1:-} ;;
        --apk)        shift; apk_override=${1:-}; skip_build=1 ;;
        -h|--help)    usage; exit 0 ;;
        *) printf 'Error: unknown argument %s\n' "'$1'" >&2; usage >&2; exit 2 ;;
    esac
    shift
done

if [[ ! $max_age =~ ^[0-9]+$ ]]; then
    echo 'Error: --max-age must be a non-negative integer (seconds).' >&2
    exit 2
fi
command -v unzip >/dev/null 2>&1 || { echo 'Error: unzip is required.' >&2; exit 2; }

# ---------------------------------------------------------------------------
# 1. Build (unless skipped).
# ---------------------------------------------------------------------------
build_start=$(date +%s)
if (( ! skip_build )); then
    [ -x "$GRADLE_DIR/gradlew" ] || { echo "Error: $GRADLE_DIR/gradlew not found/executable." >&2; exit 2; }
    echo "==> building: (cd $GRADLE_DIR && ./gradlew assembleDebug)"
    if ! (cd "$GRADLE_DIR" && ./gradlew assembleDebug); then
        echo 'Error: gradlew assembleDebug failed.' >&2
        exit 2
    fi
fi

# ---------------------------------------------------------------------------
# 2. Locate the newest debug APK.
# ---------------------------------------------------------------------------
if [ -n "$apk_override" ]; then
    apk=$apk_override
else
    apk=$(find "$GRADLE_DIR/app/build/outputs/apk/debug" -type f -name '*.apk' 2>/dev/null \
          | xargs -r ls -t 2>/dev/null | head -n1)
    if [ -z "$apk" ]; then
        apk=$(find "$GRADLE_DIR/app/build/outputs/apk" -type f -name '*.apk' 2>/dev/null \
              | xargs -r ls -t 2>/dev/null | head -n1)
    fi
fi
if [ -z "$apk" ] || [ ! -f "$apk" ]; then
    echo 'Error: no APK found. Build first (run without --skip-build).' >&2
    exit 2
fi

# ---------------------------------------------------------------------------
# 3. Freshness check: reject a stale APK.
# ---------------------------------------------------------------------------
now=$(date +%s)
apk_mtime=$(stat -c %Y "$apk" 2>/dev/null || stat -f %m "$apk" 2>/dev/null || echo 0)
apk_age=$(( now - apk_mtime ))
if (( skip_build )); then
    if (( apk_age > max_age )); then
        printf 'Error: APK is too old (%d s > --max-age %d s): %s\n' "$apk_age" "$max_age" "$apk" >&2
        echo '       Rebuild, or raise --max-age, or pass --apk.' >&2
        exit 2
    fi
else
    if (( apk_mtime < build_start )); then
        printf 'Error: APK is stale (mtime %d < build start %d): %s\n' "$apk_mtime" "$build_start" "$apk" >&2
        echo '       The build did not produce a fresh APK (up-to-date?). Try a clean build.' >&2
        exit 2
    fi
fi
printf '==> APK: %s (age %d s)\n' "$apk" "$apk_age"

# ---------------------------------------------------------------------------
# 4. List contents with `unzip -l`, extract lib/*.so and assets/* entries.
# ---------------------------------------------------------------------------
listing=$(unzip -l "$apk" 2>/dev/null)
if [ -z "$listing" ]; then
    echo "Error: 'unzip -l' produced no output for $apk" >&2
    exit 2
fi

# Native libs: paths like lib/<abi>/<name>.so  -> distinct file names + per-ABI.
lib_paths=$(printf '%s\n' "$listing" | awk '{print $NF}' | grep -E '^lib/[^/]+/[^/]+\.so$' | sort -u)
# Assets: paths under assets/, ignoring build-generated dexopt profiles.
asset_paths=$(printf '%s\n' "$listing" | awk '{print $NF}' | grep -E '^assets/' | grep -vE '^assets/dexopt/' | sort -u)

# ---------------------------------------------------------------------------
# 5. Compare against the allowlists (comm on sorted lists).
# ---------------------------------------------------------------------------
tmpd=$(mktemp -d) || exit 2
trap 'rm -rf "$tmpd"' EXIT

printf '%s\n' "${ALLOWED_JNI_LIBS[@]}" | sort -u > "$tmpd/libs_allowed"
printf '%s\n' "$lib_paths" | sed -E 's#^lib/[^/]+/##' | sort -u > "$tmpd/libs_found"
printf '%s\n' "${ALLOWED_ASSETS[@]}" | sort -u > "$tmpd/assets_allowed"
printf '%s\n' "$asset_paths" | sort -u > "$tmpd/assets_found"

echo '==> native libs in APK (per ABI):'
if [ -z "$lib_paths" ]; then
    echo '    (none)'
else
    printf '%s\n' "$lib_paths" | awk -F/ '{c[$2]++} END {for (a in c) printf "    %s: %d lib(s)\n", a, c[a]}' | sort
    printf '%s\n' "$lib_paths" | sed -E 's#^lib/[^/]+/##' | sort -u | sed 's/^/        /'
fi
echo '==> assets in APK (excluding assets/dexopt):'
if [ -z "$asset_paths" ]; then
    echo '    (none)'
else
    printf '%s\n' "$asset_paths" | sed 's/^/        /'
fi

if (( list_only )); then
    exit 0
fi

libs_unknown=$(comm -23 "$tmpd/libs_found" "$tmpd/libs_allowed")
libs_missing=$(comm -13 "$tmpd/libs_found" "$tmpd/libs_allowed")
assets_unknown=$(comm -23 "$tmpd/assets_found" "$tmpd/assets_allowed")
assets_missing=$(comm -13 "$tmpd/assets_found" "$tmpd/assets_allowed")

rc=0
if [ -n "$libs_unknown" ]; then
    rc=1
    echo 'FAIL: unexpected native lib(s) NOT in ALLOWED_JNI_LIBS:'
    printf '%s\n' "$libs_unknown" | sed 's/^/    /'
    echo '    If intentional, add them to ALLOWED_JNI_LIBS in tools/check_apk_contents.sh.'
fi
if [ -n "$libs_missing" ]; then
    rc=1
    echo 'FAIL: expected native lib(s) MISSING from the APK:'
    printf '%s\n' "$libs_missing" | sed 's/^/    /'
fi
if [ -n "$assets_unknown" ]; then
    rc=1
    echo 'FAIL: unexpected asset(s) NOT in ALLOWED_ASSETS:'
    printf '%s\n' "$assets_unknown" | sed 's/^/    /'
    echo '    If intentional, add them to ALLOWED_ASSETS in tools/check_apk_contents.sh.'
fi
if [ -n "$assets_missing" ]; then
    rc=1
    echo 'FAIL: expected asset(s) MISSING from the APK:'
    printf '%s\n' "$assets_missing" | sed 's/^/    /'
fi

if (( rc == 0 )); then
    echo 'OK: APK contains exactly the allowlisted JNI libs and assets (none extra, none missing).'
fi
exit $rc
