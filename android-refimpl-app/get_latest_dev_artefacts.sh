
#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

url_arm_lib='https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts/0/artefacts/android/libs/armeabi/libjni-c-toxcore.so?filter=successful&branch=zoff99%2Fdev003'
url_arm64_lib='https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts/0/artefacts/android/libs/arm64-v8a/libjni-c-toxcore.so?filter=successful&branch=zoff99%2Fdev003'
url_x86_lib='https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts/0/artefacts/android/libs/x86/libjni-c-toxcore.so?filter=successful&branch=zoff99%2Fdev003'
url_x86_64_lib='https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts/0/artefacts/android/libs/x86_64/libjni-c-toxcore.so?filter=successful&branch=zoff99%2Fdev003'

rm -f "$_HOME_""/app/nativelibs/armeabi-v7a/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/arm64-v8a/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/x86/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/x86_64/libjni-c-toxcore.so"

wget "$url_arm_lib" -O "$_HOME_""/app/nativelibs/armeabi-v7a/libjni-c-toxcore.so"
wget "$url_arm64_lib" -O "$_HOME_""/app/nativelibs/arm64-v8a/libjni-c-toxcore.so"
wget "$url_x86_lib" -O "$_HOME_""/app/nativelibs/x86/libjni-c-toxcore.so"
wget "$url_x86_64_lib" -O "$_HOME_""/app/nativelibs/x86_64/libjni-c-toxcore.so"
