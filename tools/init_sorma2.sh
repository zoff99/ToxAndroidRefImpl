#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"
cd "$basedir"/sorma2/gen/

#for i in $(grep -l 'Column.Helpers' /home/zoff/StudioProjects/ToxAndroidRefImpl/android-refimpl-app/app/src/main/java/com/zoffcc/applications/trifa/*.java); do
#    ln -sf "$i" "_sorm_"$(basename "$i")
#done

cd ../

./do_compile.sh || exit 1

java \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen" || exit 1

# cp -v \
# ../sorma2/gen/com/zoffcc/applications/sorm/OrmaDatabase.java \
# ../android-refimpl-app/app/src/main/java/com/zoffcc/applications/sorm/OrmaDatabase.java
