#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../android-refimpl-app/"
cd "$basedir"

r1='https://github.com/zoff99/ToxAndroidRefImpl'
f1='app/build.gradle'

tagspec=''
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" 2>/dev/null \
    | cut --delimiter='/' --fields=3 2>/dev/null \
    | grep '^trifajni-' \
    | tail -1 2>/dev/null \
    | sed -e 's#^trifajni-##')

echo "__VERSIONUPDATE__:""$ver"

sed -i -e 's#implementation '"'"'com.github.zoff99:pkgs_ToxAndroidRefImpl:.*#implementation '"'"'com.github.zoff99:pkgs_ToxAndroidRefImpl:'"$ver"''"'"'#' "$f1"
./gradlew -q calculateChecksums > app/witness.gradle

