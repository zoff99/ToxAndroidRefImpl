#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_


grep -rl '//\*@@VIDEOBGREMOVE@@\*// ' | xargs -L1 sed -i -e 's#//\*@@VIDEOBGREMOVE@@\*// ##g'
./gradlew assembleRelease
