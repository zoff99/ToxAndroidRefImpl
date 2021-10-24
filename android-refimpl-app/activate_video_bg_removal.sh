#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_


grep -rl '//\*@@VIDEOBGREMOVE@@\*// ' | \
    grep -v activate_video_bg_removal.sh | \
    xargs -L1 sed -i -e 's#//\*@@VIDEOBGREMOVE@@\*// ##g'
sed -i -e 's#VideoFrameAnalyser.java#XXXXXXXYYYYYYY.java#g' ./app/build.gradle
sed -i -e 's#boolean IS_GPLAY_VERSION = false#boolean IS_GPLAY_VERSION = true#' \
    ./app/src/main/java/com/zoffcc/applications/trifa/MainActivity.java
./gradlew assembleRelease

ls -al app/build/outputs/apk/release/*apk
