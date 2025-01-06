#! /bin/bash
base_url='https://github.com/zoff99/sqlite-jdbc/releases/download/nightly/'

file_arm64='linux-android-arm64__libsqlitejdbc.so'
file_arm='linux-android-arm__libsqlitejdbc.so'
file_x86='linux-android-x86__libsqlitejdbc.so'
file_x86_64='linux-android-x64__libsqlitejdbc.so'

file_jnilib='libsqlitejdbc.so'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../android-refimpl-app/app/nativelibs/"

cd "$basedir"
wget "$base_url""$file_arm64" -O ./arm64-v8a/"$file_jnilib"
wget "$base_url""$file_arm" -O ./armeabi-v7a/"$file_jnilib"
wget "$base_url""$file_x86" -O ./x86/"$file_jnilib"
wget "$base_url""$file_x86_64" -O ./x86_64/"$file_jnilib"

