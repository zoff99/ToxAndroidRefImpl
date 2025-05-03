#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"
cd "$basedir"

pwd

r1='https://github.com/xerial/sqlite-jdbc'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '^sqlite\-jdbc\-3' | tail --lines=1)

echo "$ver"

v1=$(echo "$ver"|sed -e 's#version-##'|awk -F'.' '{print $1}')
v2=$(echo "$ver"|sed -e 's#version-##'|awk -F'.' '{print $2}')
v3=$(echo "$ver"|sed -e 's#version-##'|awk -F'.' '{print $3}')
v4=$(echo "$ver"|sed -e 's#version-##'|awk -F'.' '{print $4}')

# echo "$v1"  "$v2"  "$v3"  "$v4"

url="https://github.com/xerial/sqlite-jdbc/releases/download/""$ver""/sqlite-jdbc-""$ver"".jar"
file="sqlite-jdbc-""$ver"".jar"

echo "URL: ""$url"
echo "File: ""$file"

f1="do_compile.sh"
f2="do_run.sh"
f3="README.md"

# download new jar
wget "$url" -O x.jar

# remove old jar
git rm ./test/sqlite-jdbc-*.jar
mv -v x.jar ./test/"$file"

sed -i -e 's#sqlite-jdbc-[^:]*\.jar#'"$file"'#' "$f1"
sed -i -e 's#sqlite-jdbc-[^:]*\.jar#'"$file"'#' "$f2"
sed -i -e 's#sqlite-jdbc-[^:]*\.jar#'"$file"'#' "$f3"

if [ "$1""x" == "nocommitx" ]; then
    echo "no commit"
    exit 0
fi

git add ./test/"$file"
git add "$f1" "$f2" "$f3"

git commit -m 'update to '"$file"



