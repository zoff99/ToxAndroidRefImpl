#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

jq --version || exit 1

furl_text=$(wget -q -O - 'https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts?branch=zoff99%2Fdev003&filter=successful')
libs_urls=$(echo $furl_text |jq ' .[].url' |grep 'libjni-c-toxcore.so')

url_arm_lib=$(echo "$libs_urls" |grep /armeabi/|tr -d '"')
url_arm64_lib=$(echo "$libs_urls" |grep /arm64-v8a/|tr -d '"')
url_x86_lib=$(echo "$libs_urls" |grep /x86/|tr -d '"')
url_x86_64_lib=$(echo "$libs_urls" |grep /x86_64/|tr -d '"')

rm -f "$_HOME_""/app/nativelibs/armeabi-v7a/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/arm64-v8a/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/x86/libjni-c-toxcore.so"
rm -f "$_HOME_""/app/nativelibs/x86_64/libjni-c-toxcore.so"

wget "$url_arm_lib" -O "$_HOME_""/app/nativelibs/armeabi-v7a/libjni-c-toxcore.so"
wget "$url_arm64_lib" -O "$_HOME_""/app/nativelibs/arm64-v8a/libjni-c-toxcore.so"
wget "$url_x86_lib" -O "$_HOME_""/app/nativelibs/x86/libjni-c-toxcore.so"
wget "$url_x86_64_lib" -O "$_HOME_""/app/nativelibs/x86_64/libjni-c-toxcore.so"
