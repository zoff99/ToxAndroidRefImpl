#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../android-refimpl-app/"
cd "$basedir"

r1='https://github.com/zoff99/ToxAndroidRefImpl'
u1='https://jitpack.io/com/github/zoff99/pkgs_ToxAndroidRefImpl/'
u2='/pkgs_ToxAndroidRefImpl-'
u3='.aar'
f1='app/build.gradle'

tagspec=''
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" 2>/dev/null \
    | cut --delimiter='/' --fields=3 2>/dev/null \
    | grep '^trifajni-' \
    | tail -1 2>/dev/null \
    | sed -e 's#^trifajni-##')


url="$u1""$ver""$u2""$ver""$u3"

tf='./tmpfile.aar'

wget "$url" -O "$tf" >/dev/null 2>/dev/null
if [ ! -s "$tf" ]; then
  echo "new version trifajni-""$ver"" not yet uploaded to jitpack"
  rm -f "$tf"
  exit 0
fi
rm -f "$tf"

echo "__VERSIONUPDATE__:""$ver"

sed -i -e 's#implementation '"'"'com.github.zoff99:pkgs_ToxAndroidRefImpl:.*#implementation '"'"'com.github.zoff99:pkgs_ToxAndroidRefImpl:'"$ver"''"'"'#' "$f1"

./gradlew -q calculateChecksums >/dev/null 2>/dev/null # first run add some checking for license text. silly crap!

./gradlew -q calculateChecksums | \
grep -v 'and:sdk:platforms:android.jar' | \
grep -v 'android:sdk:platforms:android.jar' | \
grep -v 'android:sdk:platforms:core-for-system-modules.jar' | \
grep -v '^\(Skipping\|Verifying\|Welcome to Gradle\)' \
> ./app/witness.gradle
