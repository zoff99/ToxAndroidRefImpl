#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_


android_debug_keystore_danger_danger=~/.android/debug.keystore
temp_apk_file="working.apk"

basedir="$_HOME_""/../"
cd "$basedir"/
cd android-refimpl-app/

rm -fv working.apk.idsig
find . -name '*.apk' -exec 'rm' '-fv' {} \;

./gradlew assembleRelease || exit 1

find . -name '*.apk' 2>/dev/null | grep 'app-release-unsigned.apk' || exit 1
release_apk_file=$(find . -name '*.apk' 2>/dev/null)

zipalign -f 4 "$release_apk_file" "$temp_apk_file" || exit 1

apksigner sign --ks "$android_debug_keystore_danger_danger" --ks-key-alias androiddebugkey \
 --v1-signing-enabled true --key-pass pass:android --ks-pass pass:android \
 --v2-signing-enabled true "$temp_apk_file" || exit 1

rm -fv working.apk.idsig

mv -v "$temp_apk_file" "$release_apk_file"

cd "$basedir"/
echo "singed apk file is:"
release_apk_file2=$(find . -name '*.apk' 2>/dev/null)
echo "$release_apk_file2"
