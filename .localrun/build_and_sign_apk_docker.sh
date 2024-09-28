#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

# docker info

mkdir -p $_HOME_/artefacts
mkdir -p $_HOME_/script

echo '#! /bin/bash
# --------------------------------
echo "installing system packages ..."
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --force-yes --no-install-recommends ca-certificates git wget rsync sdkmanager android-sdk openjdk-17-jdk-headless zipalign apksigner
apt purge -y --force-yes android-sdk-build-tools android-sdk-build-tools-common android-sdk-common android-sdk-platform-tools android-sdk-platform-tools-common
# --------------------------------
echo "checkout latest source code ..."
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd ./ToxAndroidRefImpl/android-refimpl-app/
# --------------------------------
echo "checking sdkmanager licenses ..."
export ANDROID_HOME="/usr/lib/android-sdk/"
echo y|sdkmanager --licenses
# --------------------------------
echo "compile application ..."
./gradlew assembleRelease || (sleep 10; ./gradlew assembleRelease)

echo "1--------"
find . -name "*.apk"
echo "2--------"

ls -al ./app/build/outputs/apk/release/app-release-unsigned.apk


echo "generate debug key ..."
# keytool -genkey -v -keystore /script/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname CN=appauth

echo "align and sign apk ..."
zipalign -p 4 ./app/build/outputs/apk/release/app-release-unsigned.apk ./app/build/outputs/apk/release/app-release-unsigned-aligned.apk
apksigner sign --ks /script/debug.keystore --ks-pass "pass:android" --ks-key-alias androiddebugkey --out ./app/build/outputs/apk/release/app-release-signed-aligned.apk --verbose ./app/build/outputs/apk/release/app-release-unsigned-aligned.apk

echo "copy to artefacts directory ..."
cp -v ./app/build/outputs/apk/release/app-release-signed-aligned.apk /artefacts/
chmod a+rwx /artefacts/*

# for error handling do "ls" again
ls -hal ./app/build/outputs/apk/release/app-release-signed-aligned.apk

' > $_HOME_/script/do_it___external.sh

chmod a+rx $_HOME_/script/do_it___external.sh


system_to_build_for="ubuntu:22.04"

cd $_HOME_/
docker run -ti --rm \
  -v $_HOME_/artefacts:/artefacts \
  -v $_HOME_/script:/script \
  -e DISPLAY=$DISPLAY \
  "$system_to_build_for" \
  /bin/bash /script/do_it___external.sh

