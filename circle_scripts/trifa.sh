#! /bin/bash



echo "starting ..."

START_TIME=$SECONDS

## ----------------------
numcpus_=$(nproc)
quiet_=1
full="1"
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




echo $_HOME_

export _SRC_=$_HOME_/trifa/build/
export _INST_=$_HOME_/trifa/inst/

echo $_SRC_
echo $_INST_

rm -Rf $_SRC_
rm -Rf $_INST_

mkdir -p $_SRC_
mkdir -p $_INST_


export ORIG_PATH_=$PATH


export _SDK_="$_INST_/sdk"
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


mkdir -p $_toolchain_
mkdir -p $AND_PKG_CONFIG_PATH
mkdir -p $WRKSPACEDIR

if [ "$full""x" == "1x" ]; then

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

# copy the source ----------
cp -av /root/work/android-refimpl-app $_s_/trifa_src/
# copy JNI libs ------------
cp -av /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so $_s_/trifa_src/android-refimpl-app/app/nativelibs/armeabi-v7a/
cp -av /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so $_s_/trifa_src/android-refimpl-app/app/nativelibs/x86/


# ----- get the source -----






# --------- GRADLE -------------
  cd $_s_/trifa_src/android-refimpl-app/
  pwd
  ls -al
  chmod a+rx ./gradlew
  ./gradlew :app:dependencies
  ./gradlew :app:build --max-workers=1 --stacktrace --no-daemon || ./gradlew :app:build --stacktrace --no-daemon # first build may FAIL
# --------- GRADLE -------------


# --- save lint output ---
  cd $_s_/trifa_src/android-refimpl-app/ ; find . -name '*lint-report*' -exec ls -al {} \;
  cd $_s_/trifa_src/android-refimpl-app/ ; cp -av ./app/lint-report.html $CIRCLE_ARTIFACTS/
  cd $_s_/trifa_src/android-refimpl-app/ ; cp -av ./app/lint-report.xml $CIRCLE_ARTIFACTS/
  cd $_s_/trifa_src/android-refimpl-app/ ; cp -av ./lint-report.txt $CIRCLE_ARTIFACTS/
# --- save lint output ---

# ----------- show generated apk file -----------
  cd $_s_/trifa_src/android-refimpl-app/
  ls -al app/build/outputs/apk/
  find ./ -name '*.apk'
  mkdir -p app/build/outputs/apk/
  cp -av ./app/build/outputs/apk/app-release-unsigned.apk ../app/build/outputs/apk/
# ----------- show generated apk file -----------



pwd

ELAPSED_TIME=$(($SECONDS - $START_TIME))

echo "compile time: $(($ELAPSED_TIME/60)) min $(($ELAPSED_TIME%60)) sec"

