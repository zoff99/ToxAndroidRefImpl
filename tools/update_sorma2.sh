#! /bin/sh
url='https://github.com/zoff99/sorma2/archive/refs/heads/master.tar.gz'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd sorma2/
rm -Rfv *.txt *.jar *.md *.java LICENSE com/ test/ *.sh
cd "$basedir"
mkdir -p temp_sorma2/
cd temp_sorma2/
wget "$url" -O master.tar.gz
tar -xzvf master.tar.gz --strip-components=1 -C ./
rm -f master.tar.gz
cp  -av *.txt *.jar *.md *.java LICENSE com/ test/ *.sh "$basedir"/sorma2/
cd "$basedir"
rm -Rf temp_sorma2/
