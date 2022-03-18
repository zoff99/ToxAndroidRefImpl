#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

f1="android-refimpl-app/app/build.gradle"

cd "$basedir"

if [[ $(git status --porcelain --untracked-files=no) ]]; then
	echo "ERROR: git repo has changes."
	echo "please commit or cleanup the git repo."
	if [ "$1""x" == "-fx" ]; then
		echo "** force mode **"
	else
		exit 1
	fi
else
	echo "git repo clean."
fi

cur_p_version=$(cat "$f1" | grep 'versionCode ' | head -1 | \
	sed -e 's#^.*versionCode ##' )
cur_m_version=$(cat "$f1" | grep 'versionName "' | head -1 | \
	sed -e 's#^.*versionName "##' | \
	sed -e 's#".*$##')

next_p_version=$[ $cur_p_version + 1 ]
# thanks to: https://stackoverflow.com/a/8653732
next_m_version=$(echo "$cur_m_version"|awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')

echo $cur_p_version
echo $next_p_version

echo $cur_m_version
echo $next_m_version

sed -i -e 's#versionCode .*#versionCode '"$next_p_version"'#g' "$f1"
sed -i -e 's#versionName ".*#versionName "'"$next_m_version"'"#g' "$f1"

commit_message="new version ""$next_m_version"
tag_name="$next_m_version"

git commit -m "$commit_message" "$f1"
git tag -a "$next_m_version" -m "$next_m_version"

