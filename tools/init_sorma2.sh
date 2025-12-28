#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"
cd "$basedir"/sorma2/gen/

cd ../

./do_compile.sh || exit 1

java \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen" || exit 1

# cp -v \
# ../sorma2/gen/com/zoffcc/applications/sorm/OrmaDatabase.java \
# ../android-refimpl-app/app/src/main/java/com/zoffcc/applications/sorm/OrmaDatabase.java
