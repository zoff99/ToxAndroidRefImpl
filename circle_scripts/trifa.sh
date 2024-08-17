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

echo $_HOME_

export _SRC_=$_HOME_/trifa_build/
export _INST_=$_HOME_/trifa_inst/

echo $_SRC_
echo $_INST_

rm -Rf $_SRC_
rm -Rf $_INST_

mkdir -p $_SRC_
mkdir -p $_INST_


export ORIG_PATH_=$PATH


export _BLD_="$_SRC_/build/"
export _CPUS_=$numcpus_

export _s_="$_SRC_/"
mkdir -p $WRKSPACEDIR

rm -Rf "$_s_"
mkdir -p "$_s_"

# get current artefact version number
cur_version=$(cat /root/work/android-refimpl-app/jnilib/build.gradle|grep 'def maven_artefact_version'|cut -d "'" -f 2)

if [ "$cur_version""x" == "x" ]; then
    echo "ERROR: can not determine current verion"
    exit 1
fi

ls -hal /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so || exit 1
ls -hal /root/work//artefacts//android/libs/arm64-v8a/libjni-c-toxcore.so || exit 1
ls -hal /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so || exit 1
ls -hal /root/work//artefacts//android/libs/x86_64/libjni-c-toxcore.so || exit 1

mkdir -p $_s_/trifa_src/jni-c-toxcore/

# --------- JNI libs -------------
#cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/armeabi-v7a/
#cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/armeabi-v7a/
#cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/arm64-v8a/
#cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/arm64-v8a/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/arm64-v8a/
#cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/x86/
#cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/x86/
#cd $_s_/trifa_src/jni-c-toxcore/; mkdir -p ../android-refimpl-app/jnilib/src/main/jniLibs/x86_64/
#cd $_s_/trifa_src/jni-c-toxcore/; cp -av /root/work//artefacts//android/libs/x86_64/libjni-c-toxcore.so ../android-refimpl-app/jnilib/src/main/jniLibs/x86_64/

echo "----- stubs -----"
ls -al /root/work/stubaar/jni/*/libjni-c-toxcore.so
echo "----- stubs -----"
rm -fv /root/work/stubaar/jni/*/libjni-c-toxcore.so
cp -av /root/work//artefacts//android/libs/armeabi/libjni-c-toxcore.so /root/work/stubaar/jni/armeabi-v7a/
cp -av /root/work//artefacts//android/libs/arm64-v8a/libjni-c-toxcore.so /root/work/stubaar/jni/arm64-v8a/
cp -av /root/work//artefacts//android/libs/x86/libjni-c-toxcore.so /root/work/stubaar/jni//x86/
cp -av /root/work//artefacts//android/libs/x86_64/libjni-c-toxcore.so /root/work/stubaar/jni/x86_64/
echo "----- real -----"
ls -al /root/work/stubaar/jni/*/libjni-c-toxcore.so
echo "----- real -----"
# --------- JNI libs -------------


# --------- generate maven repo file -----------
pwd


cd /root/work/stubaar/
zip -r /root/work/stub/root/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/1.0.142/trifa-jni-lib-"$cur_version".aar .
cd /root/work/stub/root/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib
sed -i -e 's#1.0.142#'"$cur_version"'#' maven-metadata-local.xml
mv -v 1.0.142 "$cur_version"
cd /root/work/stub/root/.m2/repository/com/zoffcc/applications/trifajni/trifa-jni-lib/"$cur_version"/
mv -v trifa-jni-lib-1.0.142.pom trifa-jni-lib-"$cur_version".pom
sed -i -e 's#1.0.142#'"$cur_version"'#' trifa-jni-lib-"$cur_version".pom

ls -al

cd /root/work/stub/
zip -r $CIRCLE_ARTIFACTS/local_maven.zip ./root
zip -r $CIRCLE_ARTIFACTS/local_maven_trifa_jni_"$cur_version".zip ./root
# --------- generate maven repo file -----------

# cp $CIRCLE_ARTIFACTS/local_maven_trifa_jni_"$cur_version".zip /artefacts/
# chmod a+rw /artefacts/*

ls -al $CIRCLE_ARTIFACTS/

pwd

ELAPSED_TIME=$(($SECONDS - $START_TIME))

echo "compile time: $(($ELAPSED_TIME/60)) min $(($ELAPSED_TIME%60)) sec"

