#! /bin/bash



echo "starting ..."

START_TIME=$SECONDS

## ----------------------
numcpus_=$(nproc)
quiet_=1
full="1"
download_full="1"
build_yasm="1"
## ----------------------


## set this to make c-toxcore log more verbose -------------
export DEBUG_TOXCORE_LOGGING=" -DMIN_LOGGER_LEVEL=1 "
## set this to make c-toxcore log more verbose -------------


#_HOME2_=$(dirname $0)
#export _HOME2_
#_HOME_=$(cd $_HOME2_;pwd)
#export _HOME_

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

redirect_cmd apt-get install $qqq -y --force-yes wget
redirect_cmd apt-get install $qqq -y --force-yes git
redirect_cmd apt-get install $qqq -y --force-yes curl

redirect_cmd apt-get install $qqq -y --force-yes python-software-properties
redirect_cmd apt-get install $qqq -y --force-yes software-properties-common


pkgs="
    unzip
    zip
    automake
    autotools-dev
    build-essential
    check
    checkinstall
    libtool
    libfreetype6-dev
    libopus-dev
    fontconfig-config
    libfontconfig1-dev
    pkg-config
    openjdk-8-jdk
"

for i in $pkgs ; do
    redirect_cmd apt-get install $qqq -y --force-yes $i
done






#### ARM build ###############################################


echo $_HOME_

export _SRC_=$_HOME_/build/
export _INST_=$_HOME_/inst/

echo $_SRC_
echo $_INST_

rm -Rf $_SRC_
rm -Rf $_INST_

mkdir -p $_SRC_
mkdir -p $_INST_


export ORIG_PATH_=$PATH


export _SDK_="$_INST_/sdk"
export _NDK_="$_INST_/ndk/"
export _BLD_="$_SRC_/build/"
export _CPUS_=$numcpus_

export _toolchain_="$_INST_/toolchains/"
export _s_="$_SRC_/"
export CF2=" -ftree-vectorize "
export CF3=" -funsafe-math-optimizations -ffast-math "
# ---- arm -----
export AND_TOOLCHAIN_ARCH="arm"
export AND_TOOLCHAIN_ARCH2="arm-linux-androideabi"
export AND_PATH="$_toolchain_/arm-linux-androideabi/bin:$ORIG_PATH_"
export AND_PKG_CONFIG_PATH="$_toolchain_/arm-linux-androideabi/sysroot/usr/lib/pkgconfig"
export AND_CC="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang"
export AND_GCC="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-gcc"
export AND_CXX="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang++"
export AND_READELF="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-readelf"
export AND_ARTEFACT_DIR="arm"



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


    mkdir -p "$PKG_CONFIG_PATH"
    redirect_cmd $_NDK_/build/tools/make_standalone_toolchain.py --arch "$TOOLCHAIN_ARCH" \
        --install-dir "$_toolchain_"/arm-linux-androideabi --api 12 --force   


    if [ "$build_yasm""x" == "1x" ]; then
    # --- YASM ---
    cd $_s_
    rm -Rf yasm
    git clone --depth=1 --branch=v1.3.0 https://github.com/yasm/yasm.git
    cd $_s_/yasm/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";$_s_/yasm/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-shared --disable-soname-versions --host=arm-linux-androideabi \
        --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot
    cd "$_BLD_"
    make -j1
    ret_=$?
    if [ $ret -ne 0 ]; then
        sleep 10
        make clean
        make -j1 || exit 1
    fi
    cd "$_BLD_";make install
    # --- YASM ---
    fi



    # --- LIBAV ---
    cd $_s_;git clone https://github.com/FFmpeg/FFmpeg libav
    cd $_s_/libav/; git checkout n4.1.4
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";

    ECFLAGS="-Os -fpic -march=armv7-a -mfloat-abi=softfp -mfpu=neon -marm -mthumb -D__thumb__"
    ELDFLAGS=""
    ARCH_SPECIFIC="--arch=arm --cpu=armv7-a --cross-prefix=arm-linux-androideabi- --enable-cross-compile"

    $_s_/libav/configure \
        --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        ${ARCH_SPECIFIC} \
        --target-os=android \
        --sysroot="$_toolchain_"/arm-linux-androideabi/sysroot \
        --extra-cflags="$ECFLAGS" \
        --extra-ldflags="$ELDFLAGS" \
        --disable-shared --enable-static \
        --enable-pthreads \
        --disable-symver \
        --disable-devices --disable-programs \
        --disable-doc --disable-avdevice \
        --disable-swscale \
        --disable-network --disable-everything \
        --disable-bzlib \
        --disable-libxcb-shm \
        --disable-libxcb-xfixes \
        --enable-ffmpeg --enable-ffprobe \
        --enable-parser=h264 \
        --enable-runtime-cpudetect \
        --enable-jni \
        --enable-neon \
        --enable-mediacodec \
        --enable-decoder=h264_mediacodec \
        --enable-gpl --enable-decoder=h264 || exit 1
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBAV ---



    # --- X264 ---
    # export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    cd $_s_;git clone git://git.videolan.org/x264.git
    cd $_s_/x264/; git checkout 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";
    $_s_/x264/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-opencl --enable-static \
        --disable-avs --disable-cli --enable-pic \
        --host=arm-linux-androideabi \
        --sysroot="$_toolchain_"/arm-linux-androideabi/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- X264 ---



    # --- LIBVPX ---
    cd $_s_;git clone --depth=1 --branch=v1.8.0 https://github.com/webmproject/libvpx.git
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 ";export CFLAGS=" -g -O3 $CF2 $CF3 "
        $_s_/libvpx/configure \
          --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
          --sdk-path="$_NDK_" \
          --disable-examples \
          --disable-unit-tests \
          --target=armv7-android-gcc \
          --size-limit=16384x16384 \
          --enable-onthefly-bitpacking \
          --enable-runtime-cpu-detect \
          --enable-realtime-only \
          --enable-multi-res-encoding \
          --enable-temporal-denoising

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch=v1.3.1 https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-shared --disable-soname-versions --host=arm-linux-androideabi \
        --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---
       


    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch=1.0.13 https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 ";export CFLAGS=" -g -O3 "
    $_s_/libsodium/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-shared --disable-soname-versions --host=arm-linux-androideabi \
        --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"


cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wl,--no-merge-exidx-entries -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
    --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
    --disable-soname-versions --host=arm-linux-androideabi \
    --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot \
    --disable-testing --disable-rt


cd "$_BLD_";export V=1 VERBOSE=1;make -j $_CPUS_ || exit 1
cd "$_BLD_";make install



# ----- get the source -----
rm -Rf $_s_/jni-c-toxcore
rm -Rf $_s_/trifa_src
mkdir -p $_s_/jni-c-toxcore
mkdir -p $_s_/trifa_src
cp -av /root/work/jni-c-toxcore $_s_/
# ----- get the source -----






# --- filter_audio ---
cd $_s_/jni-c-toxcore/filter_audio; make clean; make
cd $_s_/jni-c-toxcore/filter_audio; ls -hal
cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/
cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/arm-linux-androideabi/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."

# make certain warnings into errors!
WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '


cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared -Wall -Wextra \
    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
    -Wno-pointer-sign -Wno-unused-but-set-variable \
    $WARNS \
    -funwind-tables -Wl,--no-merge-exidx-entries -Wl,-soname,libjni-c-toxcore.so \
    jni-c-toxcore.c -o libjni-c-toxcore.so \
    -std=gnu99 -I"$_toolchain_"/arm-linux-androideabi/sysroot/usr/include \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libtoxcore.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libtoxencryptsave.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libtoxav.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libvpx.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libopus.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libsodium.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libx264.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libavcodec.a \
    "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/libavutil.a \
    ./filter_audio/libfilteraudio.a \
    coffeecatch.c coffeejni.c \
    -lm "$_NDK_"/sources/android/cpufeatures/cpu-features.c || exit 1

res=$?

echo "... done"


if [ $res -ne 0 ]; then
    echo "ERROR"
    exit 1
fi

echo ""
echo ""
ls -hal $_s_/jni-c-toxcore/libjni-c-toxcore.so


mkdir -p $CIRCLE_ARTIFACTS/android/libs/armeabi/
cp -av $_s_/jni-c-toxcore/libjni-c-toxcore.so $CIRCLE_ARTIFACTS/android/libs/armeabi/

$READELF -d $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -a $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -A $_s_/jni-c-toxcore/libjni-c-toxcore.so

# --> /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so

#### ARM build ###############################################





#### x86 build ###############################################



echo $_HOME_

export _SRC_=$_HOME_/x86_build/
export _INST_=$_HOME_/x86_inst/

echo $_SRC_
echo $_INST_

rm -Rf $_SRC_
rm -Rf $_INST_

mkdir -p $_SRC_
mkdir -p $_INST_




export _SDK_="$_INST_/sdk"
export _NDK_="$_INST_/ndk/"
export _BLD_="$_SRC_/build/"
export _CPUS_=$numcpus_

export _toolchain_="$_INST_/toolchains/"
export _s_="$_SRC_/"
export CF2=" -ftree-vectorize "
export CF3=" -funsafe-math-optimizations -ffast-math "
# ---- arm -----
export AND_TOOLCHAIN_ARCH="x86"
export AND_TOOLCHAIN_ARCH2="x86"
export AND_PATH="$_toolchain_/x86/bin:$ORIG_PATH_"
export AND_PKG_CONFIG_PATH="$_toolchain_/x86/sysroot/usr/lib/pkgconfig"
export AND_CC="$_toolchain_/x86/bin/i686-linux-android-clang"
export AND_GCC="$_toolchain_/x86/bin/i686-linux-android-gcc"
export AND_CXX="$_toolchain_/x86/bin/i686-linux-android-clang++"
export AND_READELF="$_toolchain_/x86/bin/i686-linux-android-readelf"
export AND_ARTEFACT_DIR="x86"

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
    redirect_cmd $ANDROID_HOME/tools/bin/sdkmanager --update
    ANDROID_VERSION=26
    ANDROID_BUILD_TOOLS_VERSION=26.0.2
    $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
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


    mkdir -p "$PKG_CONFIG_PATH"
    redirect_cmd $_NDK_/build/tools/make_standalone_toolchain.py --arch "$TOOLCHAIN_ARCH" \
        --install-dir "$_toolchain_"/x86 --api 12 --force   


    if [ "$build_yasm""x" == "1x" ]; then
        # --- YASM ---
        cd $_s_
        rm -Rf yasm
#        cd $_s_;git clone --depth=1 --branch=v1.3.0 https://github.com/yasm/yasm.git
#        cd $_s_/yasm/;autoreconf -fi
#        rm -Rf "$_BLD_"
#        mkdir -p "$_BLD_"
#        cd "$_BLD_";$_s_/yasm/configure --prefix="$_toolchain_"/x86/sysroot/usr \
#            --disable-shared --disable-soname-versions --host=x86 \
#            --with-sysroot="$_toolchain_"/x86/sysroot
#        cd "$_BLD_";make -j $_CPUS_ || exit 1
#        cd "$_BLD_";make install
#        echo $PATH
#        type -a yasm
        # --- YASM ---
    fi


    # --- NASM ---
#    cd $_s_
#    rm -Rf nasm
#    git clone http://repo.or.cz/nasm.git
#    cd $_s_/nasm
#    git checkout nasm-2.13.03
#    ./autogen.sh
#    ./configure --prefix=/ \
#    --host=i686-linux-android
#    make -j $_CPUS_
#    # seems man pages are not always built. but who needs those
#    touch nasm.1
#    touch ndisasm.1
#    make install
#    type -a nasm
#    nasm --version
    # --- NASM ---




    # --- LIBAV ---
    cd $_s_;git clone https://github.com/FFmpeg/FFmpeg libav
    cd $_s_/libav/; git checkout n4.1.4
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";

    ECFLAGS="-Os -fpic "
    ELDFLAGS=""
    ARCH_SPECIFIC="--arch=x86 --cross-prefix=i686-linux-android- --enable-cross-compile"

    $_s_/libav/configure \
        --prefix="$_toolchain_"/x86/sysroot/usr \
        ${ARCH_SPECIFIC} \
        --target-os=android \
        --sysroot="$_toolchain_"/x86/sysroot \
        --extra-cflags="$ECFLAGS" \
        --extra-ldflags="$ELDFLAGS" \
        --disable-asm \
        --disable-shared --enable-static \
        --enable-pthreads \
        --disable-symver \
        --disable-devices --disable-programs \
        --disable-doc --disable-avdevice \
        --disable-swscale \
        --disable-network --disable-everything \
        --disable-bzlib \
        --disable-libxcb-shm \
        --disable-libxcb-xfixes \
        --enable-ffmpeg --enable-ffprobe \
        --enable-parser=h264 \
        --enable-runtime-cpudetect \
        --enable-jni \
        --enable-neon \
        --enable-mediacodec \
        --enable-decoder=h264_mediacodec \
        --enable-gpl --enable-decoder=h264 || exit 1
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBAV ---



    # --- X264 ---
    # export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    cd $_s_;git clone git://git.videolan.org/x264.git
    cd $_s_/x264/; git checkout 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";
    $_s_/x264/configure --prefix="$_toolchain_"/x86/sysroot/usr \
        --disable-opencl --enable-static \
        --disable-avs --disable-cli --enable-pic \
        --host=i686-linux-android \
        --disable-asm \
        --sysroot="$_toolchain_"/x86/sysroot

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- X264 ---



    # --- LIBVPX ---
    cd $_s_;git clone --depth=1 --branch=v1.8.0 https://github.com/webmproject/libvpx.git
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 ";export CFLAGS=" -g -O3 $CF2 $CF3 "
        $_s_/libvpx/configure \
          --prefix="$_toolchain_"/x86/sysroot/usr \
          --sdk-path="$_NDK_" \
          --disable-examples \
          --disable-unit-tests \
          --target=x86-android-gcc \
          --disable-mmx --disable-sse \
          --disable-sse2 --disable-sse3 --disable-ssse3 --disable-sse4_1 \
          --disable-runtime_cpu_detect \
          --size-limit=16384x16384 \
          --disable-onthefly-bitpacking \
          --enable-realtime-only \
          --enable-multi-res-encoding \
          --enable-temporal-denoising

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch=v1.3.1 https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/x86/sysroot/usr \
        --disable-shared --disable-soname-versions --host=x86 \
        --with-sysroot="$_toolchain_"/x86/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---
       


    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch=1.0.13 https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 ";export CFLAGS=" -g -O3 "
    $_s_/libsodium/configure --prefix="$_toolchain_"/x86/sysroot/usr \
        --disable-shared --disable-soname-versions --host=x86 \
        --with-sysroot="$_toolchain_"/x86/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"

cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wl,--no-merge-exidx-entries -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
    --prefix="$_toolchain_"/x86/sysroot/usr \
    --disable-soname-versions --host=x86 \
    --with-sysroot="$_toolchain_"/x86/sysroot \
    --disable-testing --disable-rt


cd "$_BLD_";export V=1 VERBOSE=1;make -j $_CPUS_ || exit 1
cd "$_BLD_";make install



# ----- get the source -----
rm -Rf $_s_/jni-c-toxcore
rm -Rf $_s_/trifa_src
mkdir -p $_s_/jni-c-toxcore
mkdir -p $_s_/trifa_src
cp -av /root/work/jni-c-toxcore $_s_/
# ----- get the source -----






# --- filter_audio ---
cd $_s_/jni-c-toxcore/filter_audio; make clean; make
cd $_s_/jni-c-toxcore/filter_audio; ls -hal
cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/x86/sysroot/usr/lib/
cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/x86/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."

# make certain warnings into errors!
WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '


cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared -Wall -Wextra \
    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
    -Wno-pointer-sign -Wno-unused-but-set-variable \
    $WARNS \
    -funwind-tables -Wl,--no-merge-exidx-entries -Wl,-soname,libjni-c-toxcore.so \
    jni-c-toxcore.c -o libjni-c-toxcore.so \
    -std=gnu99 -I"$_toolchain_"/x86/sysroot/usr/include \
    "$_toolchain_"/x86/sysroot/usr/lib/libtoxcore.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libtoxencryptsave.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libtoxav.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libvpx.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libopus.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libsodium.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libx264.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libavcodec.a \
    "$_toolchain_"/x86/sysroot/usr/lib/libavutil.a \
    ./filter_audio/libfilteraudio.a \
    coffeecatch.c coffeejni.c \
    -lm "$_NDK_"/sources/android/cpufeatures/cpu-features.c || exit 1

res=$?

echo "... done"


if [ $res -ne 0 ]; then
    echo "ERROR"
    exit 1
fi

echo ""
echo ""
ls -hal $_s_/jni-c-toxcore/libjni-c-toxcore.so


mkdir -p $CIRCLE_ARTIFACTS/android/libs/x86/
cp -av $_s_/jni-c-toxcore/libjni-c-toxcore.so $CIRCLE_ARTIFACTS/android/libs/x86/

$READELF -d $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -a $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -A $_s_/jni-c-toxcore/libjni-c-toxcore.so

# --> /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so

#### x86 build ###############################################



ls -hal $CIRCLE_ARTIFACTS/android/libs/armeabi/libjni-c-toxcore.so || exit 1
ls -hal $CIRCLE_ARTIFACTS/android/libs/x86/libjni-c-toxcore.so || exit 1


pwd

ELAPSED_TIME=$(($SECONDS - $START_TIME))

echo "compile time: $(($ELAPSED_TIME/60)) min $(($ELAPSED_TIME%60)) sec"

