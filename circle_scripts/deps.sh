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

## ----------------------
_FFMPEG_VERSION_="n4.3.2"
_OPUS_VERSION_="v1.3.1"
_VPX_VERSION_="v1.8.0"
_LIBSODIUM_VERSION_="1.0.18"
_X264_VERSION_="5db6aa6cab1b146e07b60cc1736a01f21da01154"
_ANDROID_SDK_FILE_="sdk-tools-linux-4333796.zip"
_ANDROID_DSK_HASH_="92ffee5a1d98d856634e8b71132e8a95d96c83a63fde1099be3d86df3106def9"

_ANDROID_NDK_FILE_="android-ndk-r13b-linux-x86_64.zip"
_ANDROID_NDK_HASH_="3524d7f8fca6dc0d8e7073a7ab7f76888780a22841a6641927123146c3ffd29c"
_ANDROID_NDK_UNPACKDIR_="android-ndk-r13b"

#_ANDROID_NDK_FILE_="android-ndk-r20b-linux-x86_64.zip"
#_ANDROID_NDK_HASH_="8381c440fe61fcbb01e209211ac01b519cd6adf51ab1c2281d5daad6ca4c8c8c"
#_ANDROID_NDK_UNPACKDIR_="android-ndk-r20b"

#_ANDROID_NDK_FILE_="android-ndk-r21e-linux-x86_64.zip"
#_ANDROID_NDK_HASH_="ad7ce5467e18d40050dc51b8e7affc3e635c85bd8c59be62de32352328ed467e"
#_ANDROID_NDK_UNPACKDIR_="android-ndk-r21e"

_ANDOIRD_CMAKE_VER_="3.10.2.4988404"
## ----------------------

# export ASAN_CLANG_FLAGS=" -fsanitize=address -fno-omit-frame-pointer -fno-optimize-sibling-calls "
export ASAN_CLANG_FLAGS=" "

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

mkdir -p .android && touch ~/.android/repositories.cfg

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
export DEBIAN_FRONTEND=noninteractive

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


wget "https://raw.githubusercontent.com/libav/gas-preprocessor/master/gas-preprocessor.pl" -O /usr/bin/gas-preprocessor.pl
chmod a+rx /usr/bin/gas-preprocessor.pl


export ORIG_PATH_=$PATH



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
export AND_AS="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-as"
export AND_GCC="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang"
export AND_CXX="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-clang++"
export AND_READELF="$_toolchain_/arm-linux-androideabi/bin/arm-linux-androideabi-readelf"
export AND_ARTEFACT_DIR="arm"

echo "-------------------------------------------------------"
echo $_toolchain_
echo "-------------------------------------------------------"
echo $AND_PATH
echo "-------------------------------------------------------"
echo $AND_CC
echo "-------------------------------------------------------"
echo $AND_AS
echo "-------------------------------------------------------"
echo $AND_GCC
echo "-------------------------------------------------------"
echo $AND_CXX
echo "-------------------------------------------------------"

export PATH="$_SDK_"/tools/bin:$ORIG_PATH_

export ANDROID_NDK_HOME="$_NDK_"
export ANDROID_HOME="$_SDK_"


mkdir -p $_toolchain_
mkdir -p $AND_PKG_CONFIG_PATH
mkdir -p $WRKSPACEDIR

if [ "$full""x" == "1x" ]; then

    if [ "$download_full""x" == "1x" ]; then
        cd $WRKSPACEDIR
        redirect_cmd curl https://dl.google.com/android/repository/"$_ANDROID_SDK_FILE_" -o sdk.zip

        cd $WRKSPACEDIR
        # redirect_cmd curl http://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip -o android-ndk-r13b-linux-x86_64.zip
        redirect_cmd curl https://dl.google.com/android/repository/"$_ANDROID_NDK_FILE_" -o ndk.zip
    fi

    cd $WRKSPACEDIR
    # --- verfiy SDK package ---
    echo "$_ANDROID_DSK_HASH_"'  sdk.zip' \
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
    echo y | $ANDROID_HOME/tools/bin/sdkmanager "cmake;$_ANDOIRD_CMAKE_VER_"
    # -- why is this not just called "cmake" ? --
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries



    cd $WRKSPACEDIR
    # --- verfiy NDK package ---
    echo "$_ANDROID_NDK_HASH_"'  ndk.zip' \
        > ndk.zip.sha256
    sha256sum ndk.zip
    sha256sum -c ndk.zip.sha256 || exit 1
    # --- verfiy NDK package ---
    redirect_cmd unzip ndk.zip
    set -x
    rm -Rf "$_NDK_"
    mv -v "$_ANDROID_NDK_UNPACKDIR_" "$_NDK_"
    set +x


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

    echo "CC=""$CC"
    echo "GCC=""$GCC"
    echo "PATH=""$PATH"

    mkdir -p "$PKG_CONFIG_PATH"
    redirect_cmd $_NDK_/build/tools/make_standalone_toolchain.py --arch "$TOOLCHAIN_ARCH" \
        --install-dir "$_toolchain_"/arm-linux-androideabi --api 16 --force


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
    make -j $_CPUS_
    ret_=$?
    if [ $ret_ -ne 0 ]; then
        sleep 10
        make clean
        make -j $_CPUS_ || exit 1
    fi
    cd "$_BLD_";make install
    # --- YASM ---
    fi



    # --- LIBAV ---
    cd $_s_;git clone https://github.com/FFmpeg/FFmpeg libav
    cd $_s_/libav/; git checkout "$_FFMPEG_VERSION_"
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
    cd $_s_;git clone https://code.videolan.org/videolan/x264.git
    cd $_s_/x264/; git checkout "$_X264_VERSION_" # 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
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
    cd $_s_;git clone --depth=1 --branch="$_VPX_VERSION_" https://github.com/webmproject/libvpx.git
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
                export CFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
        $_s_/libvpx/configure \
          --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
          --sdk-path="$_NDK_" \
          --disable-examples \
          --disable-unit-tests \
          --target=armv7-android-gcc \
          --size-limit=16384x16384 \
          --enable-onthefly-bitpacking \
          --disable-runtime-cpu-detect \
          --enable-realtime-only \
          --enable-multi-res-encoding \
          --enable-temporal-denoising || exit 1

    cd "$_BLD_";make V=1 -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch="$_OPUS_VERSION_" https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-shared --disable-soname-versions --host=arm-linux-androideabi \
        --enable-float-approx \
        --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---
       


    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch="$_LIBSODIUM_VERSION_" https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 ";export CFLAGS=" -g -Os -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -marm -march=armv7-a "
    $_s_/libsodium/configure --prefix="$_toolchain_"/arm-linux-androideabi/sysroot/usr \
        --disable-shared --disable-soname-versions --host=arm-linux-androideabi \
        --with-sysroot="$_toolchain_"/arm-linux-androideabi/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    export CFLAGS=" -g -O3 "
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd $_s_/c-toxcore/toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------

cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -DTOX_CAPABILITIES_ACTIVE -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wl,--no-merge-exidx-entries -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
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
#cd $_s_/jni-c-toxcore/filter_audio; make clean; make
#cd $_s_/jni-c-toxcore/filter_audio; ls -hal
#cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/arm-linux-androideabi/sysroot/usr/lib/
#cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/arm-linux-androideabi/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."


## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------


# make certain warnings into errors!
# WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '

WARNS=''
add_flag()     { WARNS="$WARNS $@";               }

# Add all warning flags we can.
add_flag -Wall
add_flag -Wextra
add_flag -Weverything

# Disable specific warning flags for both C and C++.

# TODO(iphydf): Clean these up. Probably all of these are actual bugs.
add_flag -Wno-cast-align
# Very verbose, not very useful. This warns about things like int -> uint
# conversions that change sign without a cast and narrowing conversions.
add_flag -Wno-conversion
# TODO(iphydf): Check enum values when received from the user, then assume
# correctness and remove this suppression.
add_flag -Wno-covered-switch-default
# Due to clang's tolower() macro being recursive
# https://github.com/TokTok/c-toxcore/pull/481
add_flag -Wno-disabled-macro-expansion
# We don't put __attribute__ on the public API.
add_flag -Wno-documentation-deprecated-sync
# Bootstrap daemon does this.
add_flag -Wno-format-nonliteral
# struct Foo foo = {0}; is a common idiom.
add_flag -Wno-missing-field-initializers
# Useful sometimes, but we accept padding in structs for clarity.
# Reordering fields to avoid padding will reduce readability.
add_flag -Wno-padded
# This warns on things like _XOPEN_SOURCE, which we currently need (we
# probably won't need these in the future).
add_flag -Wno-reserved-id-macro
# TODO(iphydf): Clean these up. They are likely not bugs, but still
# potential issues and probably confusing.
add_flag -Wno-sign-compare
# Our use of mutexes results in a false positive, see 1bbe446.
add_flag -Wno-thread-safety-analysis
# File transfer code has this.
add_flag -Wno-type-limits
# Callbacks often don't use all their parameters.
add_flag -Wno-unused-parameter
# libvpx uses __attribute__((unused)) for "potentially unused" static
# functions to avoid unused static function warnings.
add_flag -Wno-used-but-marked-unused
# We use variable length arrays a lot.
add_flag -Wno-vla

# Disable specific warning flags for C++.

# Comma at end of enum is supported everywhere we run.
#add_cxx_flag -Wno-c++98-compat-pedantic
# TODO(iphydf): Stop using flexible array members.
#add_cxx_flag -Wno-c99-extensions
# We're C-compatible, so use C style casts.
#add_cxx_flag -Wno-old-style-cast

# Downgrade to warning so we still see it.
# add_flag -Wno-error=documentation-unknown-command
add_flag -Wno-documentation-unknown-command

add_flag -Wno-error=unreachable-code
add_flag -Wno-error=unused-variable


# added by Zoff
# add_flag -Wno-error=double-promotion
add_flag -Wno-double-promotion

# add_flag -Wno-error=missing-variable-declarations
add_flag -Wno-missing-variable-declarations

# add_flag -Wno-error=missing-prototypes
add_flag -Wno-missing-prototypes

add_flag -Wno-error=incompatible-pointer-types-discards-qualifiers
add_flag -Wno-error=deprecated-declarations

# add_flag -Wno-error=unused-macros
add_flag -Wno-unused-macros

#add_flag -Wno-error=bad-function-cast
add_flag -Wno-bad-function-cast

#add_flag -Wno-error=float-equal
add_flag -Wno-float-equal

#add_flag -Wno-error=cast-qual
add_flag -Wno-cast-qual

#add_flag -Wno-error=strict-prototypes
add_flag -Wno-strict-prototypes

#add_flag -Wno-error=gnu-statement-expression
add_flag -Wno-gnu-statement-expression

#add_flag -Wno-error=documentation
add_flag -Wno-documentation

# reactivate this later! ------------
# add_flag -Wno-error=pointer-sign
add_flag -Wno-pointer-sign
# reactivate this later! ------------


add_flag -Werror
add_flag -fdiagnostics-color=always


echo '#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wlanguage-extension-token"
#pragma GCC diagnostic ignored "-Wpedantic"
' > "$_NDK_"/sources/android/cpufeatures/cpu-features2.c
cat "$_NDK_"/sources/android/cpufeatures/cpu-features.c >> "$_NDK_"/sources/android/cpufeatures/cpu-features2.c
echo '#pragma GCC diagnostic pop
' >> "$_NDK_"/sources/android/cpufeatures/cpu-features2.c

## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------










#    -Wall -Wextra
#    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
#    -Wno-pointer-sign -Wno-unused-but-set-variable \

echo ""
echo ""
echo "-------- compiler version --------"
echo "-------- compiler version --------"
$GCC --version
echo "-------- compiler version --------"
echo "-------- compiler version --------"
echo ""
echo ""

git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"

cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared \
    $WARNS \
    -DGIT_HASH=\"$git_hash_for_jni\" \
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
    coffeecatch.c coffeejni.c \
    -lm "$_NDK_"/sources/android/cpufeatures/cpu-features2.c || exit 1

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











#### ARM64 build ###############################################


echo $_HOME_

export _SRC_=$_HOME_/arm64_build/
export _INST_=$_HOME_/arm64_inst/

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
export AND_TOOLCHAIN_ARCH="arm64"
export AND_TOOLCHAIN_ARCH2="aarch64"
export AND_TOOLCHAIN_ARCH3="aarch64-linux-android"
export AND_PATH="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin:$ORIG_PATH_"
export AND_PKG_CONFIG_PATH="$_toolchain_/$AND_TOOLCHAIN_ARCH/sysroot/usr/lib/pkgconfig"
export AND_CC="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/aarch64-linux-android-clang"
export AND_AS="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/aarch64-linux-android-as"
export AND_GCC="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/aarch64-linux-android-clang"
export AND_CXX="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/aarch64-linux-android-clang++"
export AND_READELF="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/aarch64-linux-android-readelf"
export AND_ARTEFACT_DIR="arm64"

export PATH="$_SDK_"/tools/bin:$ORIG_PATH_

export ANDROID_NDK_HOME="$_NDK_"
export ANDROID_HOME="$_SDK_"


mkdir -p $_toolchain_
mkdir -p $AND_PKG_CONFIG_PATH
mkdir -p $WRKSPACEDIR


if [ "$full""x" == "1x" ]; then

    if [ "$download_full""x" == "1x" ]; then
        cd $WRKSPACEDIR
        redirect_cmd curl https://dl.google.com/android/repository/"$_ANDROID_SDK_FILE_" -o sdk.zip

        cd $WRKSPACEDIR
        redirect_cmd curl http://dl.google.com/android/repository/"$_ANDROID_NDK_FILE_" -o ndk.zip
    fi

    cd $WRKSPACEDIR
    # --- verfiy SDK package ---
    echo "$_ANDROID_DSK_HASH_"'  sdk.zip' \
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
    echo y | $ANDROID_HOME/tools/bin/sdkmanager "cmake;$_ANDOIRD_CMAKE_VER_"
    # -- why is this not just called "cmake" ? --
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries



    cd $WRKSPACEDIR
    # --- verfiy NDK package ---
    echo "$_ANDROID_NDK_HASH_"'  ndk.zip' \
        > ndk.zip.sha256
    sha256sum -c ndk.zip.sha256 || exit 1
    # --- verfiy NDK package ---
    redirect_cmd unzip ndk.zip
    rm -Rf "$_NDK_"
    mv -v "$_ANDROID_NDK_UNPACKDIR_" "$_NDK_"



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
        --install-dir "$_toolchain_"/arm64 --api 21 --force


    if [ "$build_yasm""x" == "1x" ]; then
    # --- YASM ---
    cd $_s_
    rm -Rf yasm
    git clone --depth=1 --branch=v1.3.0 https://github.com/yasm/yasm.git
    cd $_s_/yasm/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";$_s_/yasm/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-shared --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
        --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot || exit 1
    cd "$_BLD_"
    make -j $_CPUS_
    ret_=$?
    if [ $ret -ne 0 ]; then
        sleep 10
        make clean
        make -j $_CPUS_ || exit 1
    fi
    cd "$_BLD_";make install
    # --- YASM ---
    fi



    # --- LIBAV ---
    cd $_s_;git clone https://github.com/FFmpeg/FFmpeg libav
    cd $_s_/libav/; git checkout "$_FFMPEG_VERSION_"
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";

    ECFLAGS="-Os -fpic"
    ELDFLAGS=""
    ARCH_SPECIFIC="--arch=arm64 --cross-prefix=$AND_TOOLCHAIN_ARCH3- --enable-cross-compile"

    $_s_/libav/configure \
        --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        ${ARCH_SPECIFIC} \
        --target-os=android \
        --sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot \
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
    cd $_s_;git clone https://code.videolan.org/videolan/x264.git
    cd $_s_/x264/; git checkout "$_X264_VERSION_" # 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";
    $_s_/x264/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-opencl --enable-static \
        --disable-avs --disable-cli --enable-pic \
        --host="$AND_TOOLCHAIN_ARCH3" \
        --sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- X264 ---


# find "$_toolchain_"/"$AND_TOOLCHAIN_ARCH" -name 'arm_neon.h'
# ls -al /root/work//arm64_inst//toolchains//arm64/lib64/clang/3.8.256229/include/arm_neon.h
# ls -al /root/work//arm64_inst//toolchains//arm64/lib64/clang/3.8/include/arm_neon.h
# ls -al /root/work//arm64_inst//toolchains//arm64/lib/gcc/aarch64-linux-android/4.9.x/include/arm_neon.h

    # --- LIBVPX ---
    cd $_s_;git clone --depth=1 --branch="$_VPX_VERSION_" https://github.com/webmproject/libvpx.git
    cd $_s_;wget 'https://raw.githubusercontent.com/cmeng-git/vpx-android/de613e367ea86190955a836c3c0f2bc0f260562f/patches/10.libvpx_configure.sh.patch' -O aa.patch
    cd $_s_; patch -p1 < aa.patch
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
                export CFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
        $_s_/libvpx/configure \
          --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
          --sdk-path="$_NDK_" \
          --disable-examples \
          --disable-unit-tests \
          --target=arm64-android-gcc \
          --size-limit=16384x16384 \
          --enable-onthefly-bitpacking \
          --disable-runtime-cpu-detect \
          --enable-realtime-only \
          --enable-multi-res-encoding \
          --enable-temporal-denoising || exit 1

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    unset CFLAGS
    unset CXXFLAGS
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch="$_OPUS_VERSION_" https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-shared --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
        --enable-float-approx \
        --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---
       


    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch="$_LIBSODIUM_VERSION_" https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -Os -march=armv8-a ";export CFLAGS=" -g -Os -march=armv8-a "
    $_s_/libsodium/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-shared --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
        --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    export CFLAGS=" -g -O3 "
    export CXXFLAGS=" -g -O3 "
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd $_s_/c-toxcore/toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------

cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -DTOX_CAPABILITIES_ACTIVE -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
    --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
    --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
    --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot \
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
#cd $_s_/jni-c-toxcore/filter_audio; make clean; make
#cd $_s_/jni-c-toxcore/filter_audio; ls -hal
#cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/
#cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."


## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------


# make certain warnings into errors!
# WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '

WARNS=''
add_flag()     { WARNS="$WARNS $@";               }

# Add all warning flags we can.
add_flag -Wall
add_flag -Wextra
add_flag -Weverything

# Disable specific warning flags for both C and C++.

# TODO(iphydf): Clean these up. Probably all of these are actual bugs.
add_flag -Wno-cast-align
# Very verbose, not very useful. This warns about things like int -> uint
# conversions that change sign without a cast and narrowing conversions.
add_flag -Wno-conversion
# TODO(iphydf): Check enum values when received from the user, then assume
# correctness and remove this suppression.
add_flag -Wno-covered-switch-default
# Due to clang's tolower() macro being recursive
# https://github.com/TokTok/c-toxcore/pull/481
add_flag -Wno-disabled-macro-expansion
# We don't put __attribute__ on the public API.
add_flag -Wno-documentation-deprecated-sync
# Bootstrap daemon does this.
add_flag -Wno-format-nonliteral
# struct Foo foo = {0}; is a common idiom.
add_flag -Wno-missing-field-initializers
# Useful sometimes, but we accept padding in structs for clarity.
# Reordering fields to avoid padding will reduce readability.
add_flag -Wno-padded
# This warns on things like _XOPEN_SOURCE, which we currently need (we
# probably won't need these in the future).
add_flag -Wno-reserved-id-macro
# TODO(iphydf): Clean these up. They are likely not bugs, but still
# potential issues and probably confusing.
add_flag -Wno-sign-compare
# Our use of mutexes results in a false positive, see 1bbe446.
add_flag -Wno-thread-safety-analysis
# File transfer code has this.
add_flag -Wno-type-limits
# Callbacks often don't use all their parameters.
add_flag -Wno-unused-parameter
# libvpx uses __attribute__((unused)) for "potentially unused" static
# functions to avoid unused static function warnings.
add_flag -Wno-used-but-marked-unused
# We use variable length arrays a lot.
add_flag -Wno-vla

# Disable specific warning flags for C++.

# Comma at end of enum is supported everywhere we run.
#add_cxx_flag -Wno-c++98-compat-pedantic
# TODO(iphydf): Stop using flexible array members.
#add_cxx_flag -Wno-c99-extensions
# We're C-compatible, so use C style casts.
#add_cxx_flag -Wno-old-style-cast

# Downgrade to warning so we still see it.
# add_flag -Wno-error=documentation-unknown-command
add_flag -Wno-documentation-unknown-command

add_flag -Wno-error=unreachable-code
add_flag -Wno-error=unused-variable


# added by Zoff
# add_flag -Wno-error=double-promotion
add_flag -Wno-double-promotion

# add_flag -Wno-error=missing-variable-declarations
add_flag -Wno-missing-variable-declarations

# add_flag -Wno-error=missing-prototypes
add_flag -Wno-missing-prototypes

add_flag -Wno-error=incompatible-pointer-types-discards-qualifiers
add_flag -Wno-error=deprecated-declarations

# add_flag -Wno-error=unused-macros
add_flag -Wno-unused-macros

#add_flag -Wno-error=bad-function-cast
add_flag -Wno-bad-function-cast

#add_flag -Wno-error=float-equal
add_flag -Wno-float-equal

#add_flag -Wno-error=cast-qual
add_flag -Wno-cast-qual

#add_flag -Wno-error=strict-prototypes
add_flag -Wno-strict-prototypes

#add_flag -Wno-error=gnu-statement-expression
add_flag -Wno-gnu-statement-expression

#add_flag -Wno-error=documentation
add_flag -Wno-documentation

# reactivate this later! ------------
# add_flag -Wno-error=pointer-sign
add_flag -Wno-pointer-sign
# reactivate this later! ------------


add_flag -Werror
add_flag -fdiagnostics-color=always


echo '#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wlanguage-extension-token"
#pragma GCC diagnostic ignored "-Wpedantic"
' > "$_NDK_"/sources/android/cpufeatures/cpu-features2.c
cat "$_NDK_"/sources/android/cpufeatures/cpu-features.c >> "$_NDK_"/sources/android/cpufeatures/cpu-features2.c
echo '#pragma GCC diagnostic pop
' >> "$_NDK_"/sources/android/cpufeatures/cpu-features2.c

## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------
## ---------------- clang flags ----------------










#    -Wall -Wextra
#    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
#    -Wno-pointer-sign -Wno-unused-but-set-variable \

echo ""
echo ""
echo "-------- compiler version --------"
echo "-------- compiler version --------"
$GCC --version
echo "-------- compiler version --------"
echo "-------- compiler version --------"
echo ""
echo ""

git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"

set -x

cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared \
    $WARNS \
    $ASAN_CLANG_FLAGS \
    -DGIT_HASH=\"$git_hash_for_jni\" \
    -funwind-tables -Wl,-soname,libjni-c-toxcore.so \
    jni-c-toxcore.c -o libjni-c-toxcore.so \
    -std=gnu99 -I"$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/include \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxcore.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxencryptsave.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxav.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libvpx.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libopus.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libsodium.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libx264.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libavcodec.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libavutil.a \
    coffeecatch.c coffeejni.c \
    -lm "$_NDK_"/sources/android/cpufeatures/cpu-features2.c || exit 1

res=$?

echo "... done"


set +x

if [ $res -ne 0 ]; then
    echo "ERROR"
    exit 1
fi

echo ""
echo ""
ls -hal $_s_/jni-c-toxcore/libjni-c-toxcore.so


mkdir -p $CIRCLE_ARTIFACTS/android/libs/arm64-v8a/
cp -av $_s_/jni-c-toxcore/libjni-c-toxcore.so $CIRCLE_ARTIFACTS/android/libs/arm64-v8a/

$READELF -d $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -a $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -A $_s_/jni-c-toxcore/libjni-c-toxcore.so

# --> /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so

#### ARM64 build ###############################################





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
export AND_AS="$_toolchain_/x86/bin/i686-linux-android-as"
export AND_GCC="$_toolchain_/x86/bin/i686-linux-android-clang"
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
        redirect_cmd curl https://dl.google.com/android/repository/"$_ANDROID_SDK_FILE_" -o sdk.zip

        cd $WRKSPACEDIR
        redirect_cmd curl http://dl.google.com/android/repository/"$_ANDROID_NDK_FILE_" -o ndk.zip
    fi

    cd $WRKSPACEDIR
    # --- verfiy SDK package ---
    echo "$_ANDROID_DSK_HASH_"'  sdk.zip' \
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
    echo y | $ANDROID_HOME/tools/bin/sdkmanager "cmake;$_ANDOIRD_CMAKE_VER_"
    # -- why is this not just called "cmake" ? --
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries



    cd $WRKSPACEDIR
    # --- verfiy NDK package ---
    echo "$_ANDROID_NDK_HASH_"'  ndk.zip' \
        > ndk.zip.sha256
    sha256sum -c ndk.zip.sha256 || exit 1
    # --- verfiy NDK package ---
    redirect_cmd unzip ndk.zip
    rm -Rf "$_NDK_"
    mv -v "$_ANDROID_NDK_UNPACKDIR_" "$_NDK_"



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
        --install-dir "$_toolchain_"/x86 --api 16 --force


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
    cd $_s_/libav/; git checkout "$_FFMPEG_VERSION_"
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
    cd $_s_;git clone https://code.videolan.org/videolan/x264.git
    cd $_s_/x264/; git checkout "$_X264_VERSION_" # 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
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
    cd $_s_;git clone --depth=1 --branch="$_VPX_VERSION_" https://github.com/webmproject/libvpx.git
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
                export CFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
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
          --enable-temporal-denoising || exit 1

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch="$_OPUS_VERSION_" https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/x86/sysroot/usr \
        --disable-shared --disable-soname-versions --host=x86 \
        --enable-float-approx \
        --with-sysroot="$_toolchain_"/x86/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---
       


    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch="$_LIBSODIUM_VERSION_" https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 ";export CFLAGS=" -g -Os -march=i686 "
    $_s_/libsodium/configure --prefix="$_toolchain_"/x86/sysroot/usr \
        --disable-shared --disable-soname-versions --host=x86 \
        --with-sysroot="$_toolchain_"/x86/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    export CFLAGS=" -g -O3 "
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd $_s_/c-toxcore/toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------

cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -DTOX_CAPABILITIES_ACTIVE -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wl,--no-merge-exidx-entries -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
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
#cd $_s_/jni-c-toxcore/filter_audio; make clean; make
#cd $_s_/jni-c-toxcore/filter_audio; ls -hal
#cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/x86/sysroot/usr/lib/
#cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/x86/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."

# make certain warnings into errors!
WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '

git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"

cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared -Wall -Wextra \
    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
    -Wno-pointer-sign -Wno-unused-but-set-variable \
    $WARNS \
    -DGIT_HASH=\"$git_hash_for_jni\" \
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





#### x86_64 build ###############################################



echo $_HOME_

export _SRC_=$_HOME_/x86_64_build/
export _INST_=$_HOME_/x86_64_inst/

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
export AND_TOOLCHAIN_ARCH="x86_64"
export AND_TOOLCHAIN_ARCH2="x86_64"
export AND_TOOLCHAIN_ARCH3="x86_64-linux-android"
export AND_PATH="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin:$ORIG_PATH_"
export AND_PKG_CONFIG_PATH="$_toolchain_/$AND_TOOLCHAIN_ARCH/sysroot/usr/lib/pkgconfig"
export AND_CC="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/x86_64-linux-android-clang"
export AND_CS="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/x86_64-linux-android-clang-as"
export AND_GCC="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/x86_64-linux-android-clang"
export AND_CXX="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/x86_64-linux-android-clang++"
export AND_READELF="$_toolchain_/$AND_TOOLCHAIN_ARCH/bin/x86_64-linux-android-readelf"
export AND_ARTEFACT_DIR="x86_64"

export PATH="$_SDK_"/tools/bin:$ORIG_PATH_

export ANDROID_NDK_HOME="$_NDK_"
export ANDROID_HOME="$_SDK_"


mkdir -p $_toolchain_
mkdir -p $AND_PKG_CONFIG_PATH
mkdir -p $WRKSPACEDIR

if [ "$full""x" == "1x" ]; then

    if [ "$download_full""x" == "1x" ]; then
        cd $WRKSPACEDIR
        redirect_cmd curl https://dl.google.com/android/repository/"$_ANDROID_SDK_FILE_" -o sdk.zip

        cd $WRKSPACEDIR
        redirect_cmd curl http://dl.google.com/android/repository/"$_ANDROID_NDK_FILE_" -o ndk.zip
    fi

    cd $WRKSPACEDIR
    # --- verfiy SDK package ---
    echo "$_ANDROID_DSK_HASH_"'  sdk.zip' \
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
    echo y | $ANDROID_HOME/tools/bin/sdkmanager "cmake;$_ANDOIRD_CMAKE_VER_"
    # -- why is this not just called "cmake" ? --
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries ------------------------------
    # Install Android Build Tool and Libraries



    cd $WRKSPACEDIR
    # --- verfiy NDK package ---
    echo "$_ANDROID_NDK_HASH_"'  ndk.zip' \
        > ndk.zip.sha256
    sha256sum -c ndk.zip.sha256 || exit 1
    # --- verfiy NDK package ---
    redirect_cmd unzip ndk.zip
    rm -Rf "$_NDK_"
    mv -v "$_ANDROID_NDK_UNPACKDIR_" "$_NDK_"



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
        --install-dir "$_toolchain_"/x86_64 --api 21 --force


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
    cd $_s_/libav/; git checkout "$_FFMPEG_VERSION_"
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";

    ECFLAGS="-Os -fpic "
    ELDFLAGS=""
    ARCH_SPECIFIC="--arch=x86_64 --cross-prefix=x86_64-linux-android- --enable-cross-compile"


    $_s_/libav/configure \
        --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        ${ARCH_SPECIFIC} \
        --target-os=android \
        --sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot \
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
    cd $_s_;git clone https://code.videolan.org/videolan/x264.git
    cd $_s_/x264/; git checkout "$_X264_VERSION_" # 0a84d986e7020f8344f00752e3600b9769cc1e85 # stable
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";
    $_s_/x264/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-opencl --enable-static \
        --disable-avs --disable-cli --enable-pic \
        --host="$AND_TOOLCHAIN_ARCH3" \
        --disable-asm \
        --sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot

    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- X264 ---



    # --- LIBVPX ---
    cd $_s_;git clone --depth=1 --branch="$_VPX_VERSION_" https://github.com/webmproject/libvpx.git
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
                export CFLAGS=" -g -O3 $CF2 $CF3 -I${_NDK_}/sources/android/cpufeatures "; \
        $_s_/libvpx/configure \
          --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
          --sdk-path="$_NDK_" \
          --disable-examples \
          --disable-unit-tests \
          --target=x86_64-android-gcc \
          --enable-runtime_cpu_detect \
          --size-limit=16384x16384 \
          --disable-onthefly-bitpacking \
          --enable-realtime-only \
          --enable-multi-res-encoding \
          --enable-temporal-denoising || exit 1

#          --disable-mmx --disable-sse \
#          --disable-sse2 --disable-sse3 --disable-ssse3 --disable-sse4_1 \


    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- LIBVPX ---




    # --- OPUS ---
    cd $_s_;git clone --depth=1 --branch="$_OPUS_VERSION_" https://github.com/xiph/opus.git
    cd $_s_/opus/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 $CF2 ";export CFLAGS=" -g -O3 $CF2 "
    $_s_/opus/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-shared --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
        --enable-float-approx \
        --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    # --- OPUS ---



    # --- LIBSODIUM ---
    cd $_s_;git clone --depth=1 --branch="$_LIBSODIUM_VERSION_" https://github.com/jedisct1/libsodium.git
    cd $_s_/libsodium/;autoreconf -fi
    rm -Rf "$_BLD_"
    mkdir -p "$_BLD_"
    cd "$_BLD_";export CXXFLAGS=" -g -O3 ";export CFLAGS=" -g -Os -march=westmere "
    $_s_/libsodium/configure --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
        --disable-shared --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
        --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot --disable-pie
    cd "$_BLD_";make -j $_CPUS_ || exit 1
    cd "$_BLD_";make install
    export CFLAGS=" -g -O3 "
    # --- LIBSODIUM ---



fi


cd $_s_;rm -Rf c-toxcore

cd $_s_;git clone https://github.com/zoff99/c-toxcore c-toxcore
cd $_s_;cd c-toxcore;git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd $_s_/c-toxcore/toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cat $_s_/c-toxcore/toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------

cd $_s_/c-toxcore/;autoreconf -fi
rm -Rf "$_BLD_"
mkdir -p "$_BLD_"
cd "$_BLD_";$_s_/c-toxcore/configure \
    CFLAGS=" $DEBUG_TOXCORE_LOGGING -DTOX_CAPABILITIES_ACTIVE -D HW_CODEC_CONFIG_TRIFA -O3 -g -Wall -Wextra -funwind-tables -Wno-deprecated-declarations -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function" \
    --prefix="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr \
    --disable-soname-versions --host="$AND_TOOLCHAIN_ARCH3" \
    --with-sysroot="$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot \
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
#cd $_s_/jni-c-toxcore/filter_audio; make clean; make
#cd $_s_/jni-c-toxcore/filter_audio; ls -hal
#cp -av $_s_/jni-c-toxcore/filter_audio/libfilteraudio.a "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/
#cp -av $_s_/jni-c-toxcore/filter_audio/filter_audio.h "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/include/
# --- filter_audio ---







echo ""
echo ""
echo "compiling jni-c-toxcore ..."

# make certain warnings into errors!
WARNS=' -Werror=div-by-zero -Werror=sign-compare -Werror=format=2 -Werror=implicit-function-declaration '

git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"

cd $_s_/jni-c-toxcore/; export V=1;$GCC -O3 -g -shared -Wall -Wextra \
    -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function \
    -Wno-pointer-sign -Wno-unused-but-set-variable \
    $WARNS \
    -DGIT_HASH=\"$git_hash_for_jni\" \
    -funwind-tables -Wl,--no-merge-exidx-entries -Wl,-soname,libjni-c-toxcore.so \
    jni-c-toxcore.c -o libjni-c-toxcore.so \
    -std=gnu99 -I"$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/include \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxcore.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxencryptsave.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libtoxav.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libvpx.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libopus.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libsodium.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libx264.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libavcodec.a \
    "$_toolchain_"/"$AND_TOOLCHAIN_ARCH"/sysroot/usr/lib/libavutil.a \
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


mkdir -p $CIRCLE_ARTIFACTS/android/libs/x86_64/
cp -av $_s_/jni-c-toxcore/libjni-c-toxcore.so $CIRCLE_ARTIFACTS/android/libs/x86_64/

$READELF -d $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -a $_s_/jni-c-toxcore/libjni-c-toxcore.so
$READELF -A $_s_/jni-c-toxcore/libjni-c-toxcore.so

# --> /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so

#### x86_64 build ###############################################



ls -hal $CIRCLE_ARTIFACTS/android/libs/armeabi/libjni-c-toxcore.so || exit 1
ls -hal $CIRCLE_ARTIFACTS/android/libs/arm64-v8a/libjni-c-toxcore.so || exit 1
ls -hal $CIRCLE_ARTIFACTS/android/libs/x86/libjni-c-toxcore.so || exit 1
ls -hal $CIRCLE_ARTIFACTS/android/libs/x86_64/libjni-c-toxcore.so || exit 1


pwd

ELAPSED_TIME=$(($SECONDS - $START_TIME))

echo "compile time: $(($ELAPSED_TIME/60)) min $(($ELAPSED_TIME%60)) sec"

