#! /bin/bash



echo "starting ..."

START_TIME=$SECONDS

## ----------------------
numcpus_=$(nproc)
quiet_=1
download_full="1"
## ----------------------



_HOME_="/root/work/"
export _HOME_
echo "_HOME_=$_HOME_"

export WRKSPACEDIR="$_HOME_""/workspace/"
export CIRCLE_ARTIFACTS="$_HOME_""/artefacts/"
mkdir -p $WRKSPACEDIR
mkdir -p $CIRCLE_ARTIFACTS


export qqq=""

if [ "$quiet_""x" == "1x" ]; then
	export qqq=" -qq "
fi


redirect_cmd() {
    if [ "$quiet_""x" == "1x" ]; then
        "$@" > /dev/null 2>&1
    else
        "$@"
    fi
}



echo "installing system packages ..."

redirect_cmd apt-get update $qqq

redirect_cmd apt-get install $qqq -y --force-yes lsb-release
system__=$(lsb_release -i|cut -d ':' -f2|sed -e 's#\s##g')
version__=$(lsb_release -r|cut -d ':' -f2|sed -e 's#\s##g')
echo "compiling on: $system__ $version__"

echo "installing more system packages ..."

redirect_cmd apt-get install $qqq -y --force-yes qrencode
redirect_cmd apt-get install $qqq -y --force-yes p7zip-full
redirect_cmd apt-get install $qqq -y --force-yes pax-utils

echo $_HOME_

export _SRC_=$_HOME_/trifa_build/
export _INST_=$_HOME_/trifa_inst/

echo $_SRC_
echo $_INST_

rm -Rf $_SRC_
rm -Rf $_INST_

mkdir -p $_SRC_
mkdir -p $_INST_




# --------- check code style -----------
cd
mkdir -p ./astyle_compile/
cd ./astyle_compile/
wget -O astyle.tgz https://downloads.sourceforge.net/project/astyle/astyle/astyle%203.1/astyle_3.1_linux.tar.gz?r=https%3A%2F%2Fsourceforge.net%2Fprojects%2Fastyle%2Ffiles%2Fastyle%2Fastyle%25203.1%2Fastyle_3.1_linux.tar.gz%2Fdownload
tar -xzvf astyle.tgz > /dev/null 2>&1
mkdir -p build_astyle > /dev/null 2>&1
cd build_astyle/ > /dev/null 2>&1
cmake ../astyle/
make -j$(nproc)

export astyle_bin="$(pwd)/astyle"
"$astyle_bin" --version || exit 1

cd
mkdir -p ./astyle_check/
cd ./astyle_check/

cp -av /root/work/android-refimpl-app ./astyle_check/ || exit 1
cp -av /root/work/jni-c-toxcore ./astyle_check/ || exit 1
cp -a /root/work/.git ./astyle_check/ || exit 1

ls -al /root/work/astyle/astylerc  || exit 1

cd ./astyle_check/
ls -al

SOURCES=`find android-refimpl-app/app/src/main/java/com/zoffcc/applications/trifa -name '*.java'|sort`;
"$astyle_bin" -n --options=/root/work/astyle/astylerc jni-c-toxcore/jni-c-toxcore.c $SOURCES
git --no-pager diff
git diff | cat > $CIRCLE_ARTIFACTS/astyle_check.patch 2>&1
# --------- check code style -----------






export ORIG_PATH_=$PATH


export _SDK_="$_INST_/sdk"
export _NDK_="$_INST_/ndk/"
export _BLD_="$_SRC_/build/"
export _CPUS_=$numcpus_

export _toolchain_="$_INST_/toolchains/"
export _s_="$_SRC_/"

export AND_TOOLCHAIN_ARCH="arm"
export AND_TOOLCHAIN_ARCH2="arm-linux-androideabi"
export AND_PATH="$_toolchain_/arm-linux-androideabi/bin:$ORIG_PATH_"
export AND_PKG_CONFIG_PATH="$_toolchain_/arm-linux-androideabi/sysroot/usr/lib/pkgconfig"
export AND_CC="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang"
export AND_GCC="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-gcc"
export AND_CXX="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang++"
export AND_READELF="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-readelf"

export PATH="$_SDK_"/tools/bin:$ORIG_PATH_

export ANDROID_NDK_HOME="$_NDK_"
export ANDROID_HOME="$_SDK_"

export CLASS_P="com.zoffcc.applications.trifa"
export START_INTENT_P="com.zoffcc.applications.trifa.StartMainActivityWrapper"


mkdir -p $_toolchain_
mkdir -p $AND_PKG_CONFIG_PATH
mkdir -p $WRKSPACEDIR


if [ "$download_full""x" == "1x" ]; then
    cd $WRKSPACEDIR
    redirect_cmd curl https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip -o sdk.zip

    cd $WRKSPACEDIR
    redirect_cmd curl http://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip -o android-ndk-r13b-linux-x86_64.zip
fi

cd $WRKSPACEDIR
# --- verfiy SDK package ---
echo '92ffee5a1d98d856634e8b71132e8a95d96c83a63fde1099be3d86df3106def9  sdk.zip' \
    > sdk.zip.sha256
sha256sum -c sdk.zip.sha256 || exit 1
# --- verfiy SDK package ---
redirect_cmd unzip sdk.zip

# -- clean SDK dir --
rm -Rf "$_SDK_"
# -- clean SDK dir --

mkdir -p "$_SDK_"
mv -v tools "$_SDK_"/
yes | "$_SDK_"/tools/bin/sdkmanager --licenses > /dev/null 2>&1

# Install Android Build Tool and Libraries ------------------------------
# Install Android Build Tool and Libraries ------------------------------
# Install Android Build Tool and Libraries ------------------------------
$ANDROID_HOME/tools/bin/sdkmanager --update
ANDROID_VERSION=26
ANDROID_BUILD_TOOLS_VERSION=26.0.2
redirect_cmd $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"
ANDROID_VERSION=25
redirect_cmd $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-${ANDROID_VERSION}"
ANDROID_BUILD_TOOLS_VERSION=23.0.3
redirect_cmd $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
ANDROID_BUILD_TOOLS_VERSION=25.0.0
redirect_cmd $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"

echo y | $ANDROID_HOME/tools/bin/sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout;1.0.2"
echo y | $ANDROID_HOME/tools/bin/sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout-solver;1.0.2"
echo y | $ANDROID_HOME/tools/bin/sdkmanager "build-tools;27.0.3"
echo y | $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-27"
# -- why is this not just called "cmake" ? --
# cmake_pkg_name=$($ANDROID_HOME/tools/bin/sdkmanager --list --verbose|grep -i cmake| tail -n 1 | cut -d \| -f 1 |tr -d " ");
echo y | $ANDROID_HOME/tools/bin/sdkmanager "cmake;3.6.4111459"
# -- why is this not just called "cmake" ? --
# Install Android Build Tool and Libraries ------------------------------
# Install Android Build Tool and Libraries ------------------------------
# Install Android Build Tool and Libraries



cd $WRKSPACEDIR
# --- verfiy NDK package ---
echo '3524d7f8fca6dc0d8e7073a7ab7f76888780a22841a6641927123146c3ffd29c  android-ndk-r13b-linux-x86_64.zip' \
    > android-ndk-r13b-linux-x86_64.zip.sha256
sha256sum -c android-ndk-r13b-linux-x86_64.zip.sha256 || exit 1
# --- verfiy NDK package ---
redirect_cmd unzip android-ndk-r13b-linux-x86_64.zip
rm -Rf "$_NDK_"
mv -v android-ndk-r13b "$_NDK_"



echo 'export ARTEFACT_DIR="$AND_ARTEFACT_DIR";export PATH="$AND_PATH";export PKG_CONFIG_PATH="$AND_PKG_CONFIG_PATH";export READELF="$AND_READELF";export GCC="$AND_GCC";export CC="$AND_CC";export CXX="$AND_CXX";export CPPFLAGS="";export LDFLAGS="";export TOOLCHAIN_ARCH="$AND_TOOLCHAIN_ARCH";export TOOLCHAIN_ARCH2="$AND_TOOLCHAIN_ARCH2"' > $_HOME_/pp
chmod u+x $_HOME_/pp
rm -Rf "$_s_"
mkdir -p "$_s_"


## ------- init vars ------- ##
## ------- init vars ------- ##
## ------- init vars ------- ##
. $_HOME_/pp
## ------- init vars ------- ##
## ------- init vars ------- ##
## ------- init vars ------- ##




# ----- get the source -----
rm -Rf $_s_/jni-c-toxcore
rm -Rf $_s_/trifa_src
mkdir -p $_s_/jni-c-toxcore
mkdir -p $_s_/trifa_src

cd /root/work/ ; current_git_tag=$(git describe --tags --exact-match 2> /dev/null)
echo "##########################################"
echo "##########################################"
echo "##########################################"
echo "current_git_tag: $current_git_tag"
echo "##########################################"
echo "##########################################"
echo "##########################################"

# copy the source ----------
cp -av /root/work/android-refimpl-app $_s_/trifa_src/
cp -av /root/work/jni-c-toxcore $_s_/trifa_src/
# copy JNI libs ------------
cp -av /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so $_s_/trifa_src/android-refimpl-app/app/nativelibs/armeabi-v7a/
cp -av /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so $_s_/trifa_src/android-refimpl-app/app/nativelibs/x86/

ls -hal /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so || exit 1
ls -hal /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so || exit 1

echo "###### ---------- ARM --------------------"
scanelf -qT $_s_/trifa_src/android-refimpl-app/app/nativelibs/armeabi-v7a//libjni-c-toxcore.so
echo "###### ---------- X86 --------------------"
scanelf -qT $_s_/trifa_src/android-refimpl-app/app/nativelibs/x86//libjni-c-toxcore.so
echo "###### ------------------------------"

# ----- get the source -----






# --------- GRADLE - build app -------------
cd $_s_/trifa_src/android-refimpl-app/
pwd
ls -al
chmod a+rx ./gradlew
if [ "$CIRCLE_BRANCH""x" == "zoff99/maven_artefactx" ]; then
    echo "not building TRIfA app! -> maven branch"
    :
else
    echo "Building TRIfA app"
    ./gradlew :app:dependencies
    ./gradlew :app:build --max-workers=1 --stacktrace --no-daemon || ./gradlew :app:build --stacktrace --no-daemon # first build may FAIL
fi
# --------- GRADLE - build app -------------






# --------- bintray artefact -------------
if [ "$CIRCLE_BRANCH""x" == "zoff99/maven_artefactx" ]; then
    cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/armeabi-v7a/
    cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/armeabi-v7a/
    cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/x86/
    cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/x86/

    cd $_s_/trifa_src/android-refimpl-app/ ; ./gradlew :jnilib:dependencies
    cd $_s_/trifa_src/android-refimpl-app/ ; ./gradlew :jnilib:build --max-workers=1 --stacktrace --no-daemon || ./gradlew :jnilib:build --stacktrace --no-daemon # first build may FAIL
    cd $_s_/trifa_src/android-refimpl-app/ ; ./gradlew :jnilib:install --info
    cd $_s_/trifa_src/android-refimpl-app/
    ls -al ./gradlew

    if [[ "$current_git_tag""x"  =~ ^trifajni-.* ]] ; then
        echo "############### ------------ ###################"
        echo "############### ------------ ###################"
        echo "trying to upload artefact to bintray ..."
        echo "############### ------------ ###################"
        echo "############### ------------ ###################"
        ./gradlew :jnilib:bintrayUpload --info || exit 1
        echo "############### ------------ ###################"
        echo "############### ------------ ###################"
        echo "trying to upload artefact to bintray ... DONE"
        echo "############### ------------ ###################"
        echo "############### ------------ ###################"
    else
        echo "not uploading artefact to bintray!! -> not maven branch"
    fi

    find ~/.m2/repository -type f -exec ls -al {} \;
# --------- bintray artefact -------------
# --------- show generated aar file -----------
    cd $_s_/trifa_src/android-refimpl-app/ ; ls -al jnilib/build/outputs/aar/
    cd ~ ; find ./ -name '*.aar' | grep 'trifa-jni'

    unzip -t ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.*/trifa-jni-lib-1.*.aar
    sha256sum ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.*/trifa-jni-lib-1.*.aar
    cat ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.*/trifa-jni-lib-1.*.pom
    cat ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/maven-metadata-local.xml


    cd $_s_/trifa_src/android-refimpl-app/ ; unzip -t ./jnilib/build/outputs/aar/trifa-jni-lib-release.aar
    cd $_s_/trifa_src/android-refimpl-app/ ; sha256sum ./jnilib/build/outputs/aar/trifa-jni-lib-release.aar
    cd $_s_/trifa_src/android-refimpl-app/ ; cp -av ./jnilib/build/outputs/aar/trifa-jni-lib-release.aar $CIRCLE_ARTIFACTS/
    cd $_s_/trifa_src/android-refimpl-app/ ; ls -hal ./jnilib/build/outputs/aar/trifa-jni-lib-release.aar || exit 1

    # cp -av ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.*/trifa-jni-lib-1.*.aar $CIRCLE_ARTIFACTS/
    # ls -hal ~/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.*/trifa-jni-lib-1.*.aar || exit 1
fi
# --------- show generated aar file -----------



if [ "$CIRCLE_BRANCH""x" != "zoff99/maven_artefactx" ]; then

    # ----------- show generated apk file -----------
    cd $_s_/trifa_src/android-refimpl-app/
    ls -al app/build/outputs/apk/
    find ./ -name '*.apk'
    mkdir -p app/build/outputs/apk/
    cp -av ./app/build/outputs/apk/release/app-release-unsigned.apk ./app/build/outputs/apk/


    ls -hal $_s_/trifa_src/android-refimpl-app/app/build/outputs/apk/app-release-unsigned.apk
    # ----------- show generated apk file -----------



    zip -d $_s_/trifa_src/android-refimpl-app/app/build/outputs/apk/app-release-unsigned.apk META-INF/\*     # remove signature !!
    cp -av $_s_/trifa_src/android-refimpl-app/app/build/outputs/apk/app-release-unsigned.apk ~/app.apk
    cd ~/
    # remove the "xxxxxx" in fron the of the "rm -f" to create a new debug signing key!! --------------
    echo xxxxxxrm -f ~/.android/debug.keystore
    # remove the "xxxxxx" in fron the of the "rm -f" to create a new debug signing key!! --------------
    ls -al ~/.android/debug.keystore
    if [ ! -f ~/.android/debug.keystore ]; then echo "*** generating new signer key ***"
        echo "*** generating new signer key ***"
        echo "*** generating new signer key ***"
        keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -keyalg RSA -keysize 2048 -validity 10000 -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"
    fi

    ls -al ~/
    jarsigner -verbose -keystore ~/.android/debug.keystore -storepass android -keypass android -sigalg SHA1withRSA -digestalg SHA1 -sigfile CERT -signedjar app-signed.apk app.apk androiddebugkey
    type -a apksigner
    type -a jarsigner
    # apksigner sign --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey --out app-signed.apk app.apk


    ls -al ~/
    find . -name zipalign
    ls -al ./work/trifa_inst/sdk/build-tools/23.0.3/zipalign
    ls -al $_SDK_/build-tools/23.0.3/zipalign
    file $_SDK_/build-tools/23.0.3/zipalign
    ls -al $_SDK_/build-tools/27.0.3/zipalign
    file $_SDK_/build-tools/27.0.3/zipalign
    cd ~/
    # HINT: zipalign is a 32bit binary?
    $_SDK_/build-tools/27.0.3/zipalign -v 4 ~/app-signed.apk ~/app-signed-aligned.apk

    ls -al ~/
    pwd

    ls -al
    cp -av app-signed-aligned.apk $CIRCLE_ARTIFACTS/${CIRCLE_PROJECT_REPONAME}.apk

    ls -hal $CIRCLE_ARTIFACTS/${CIRCLE_PROJECT_REPONAME}.apk || exit 1


    ##   also make apk files with different names for each build (for individual downloads)
    cp -av $CIRCLE_ARTIFACTS/${CIRCLE_PROJECT_REPONAME}.apk $CIRCLE_ARTIFACTS/${CIRCLE_PROJECT_REPONAME}_circleci_$CIRCLE_SHA1.apk
    ##   qr code to scan with your phone to directly download the apk file (for convenience)
    qrencode -o $CIRCLE_ARTIFACTS/QR_apk.png 'https://circle-artifacts.com/gh/'${CIRCLE_PROJECT_USERNAME}'/'${CIRCLE_PROJECT_REPONAME}'/'${CIRCLE_BUILD_NUM}'/artifacts/'${CIRCLE_NODE_INDEX}'/tmp/'`basename $CIRCLE_ARTIFACTS`'/'"${CIRCLE_PROJECT_REPONAME}_circleci_$CIRCLE_SHA1.apk" ; exit 0
    ##   qr code to go directly to the aritfacts (to scan with phone)
    qrencode -o $CIRCLE_ARTIFACTS/QR_artifacts.png 'https://circleci.com/gh/'${CIRCLE_PROJECT_USERNAME}'/'${CIRCLE_PROJECT_REPONAME}'/'${CIRCLE_BUILD_NUM}'#artifacts' ; exit 0

fi


pwd

ELAPSED_TIME=$(($SECONDS - $START_TIME))

echo "compile time: $(($ELAPSED_TIME/60)) min $(($ELAPSED_TIME%60)) sec"

