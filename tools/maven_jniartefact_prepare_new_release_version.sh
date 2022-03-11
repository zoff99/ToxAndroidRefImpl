#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

f1="android-refimpl-app/jnilib/build.gradle"

cd "$basedir"

if [[ $(git status --porcelain --untracked-files=no) ]]; then
	echo "ERROR: git repo has changes."
	echo "please commit or cleanup the git repo."
	exit 1
else
	echo "git repo clean."
fi

cur_p_version=$(cat "$f1" | grep 'maven_artefact_int_version", ' | head -1 | \
	sed -e 's#^.*maven_artefact_int_version", ##' | \
	sed -e 's#.$##')
cur_m_version=$(cat "$f1" | grep 'maven_artefact_version = ' | head -1 | \
	sed -e 's#^.*maven_artefact_version = .##' | \
    sed -e 's#.$##')

next_p_version=$[ $cur_p_version + 1 ]
# thanks to: https://stackoverflow.com/a/8653732
next_m_version=$(echo "$cur_m_version"|awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')

echo $cur_p_version
echo $next_p_version

echo $cur_m_version
echo $next_m_version

# project.ext.set("maven_artefact_int_version", 10074)
# def maven_artefact_version = '1.0.74'

sed -i -e 's#maven_artefact_int_version".*#maven_artefact_int_version", '"$next_p_version"')#' "$f1"
sed -i -e 's#maven_artefact_version = .*#maven_artefact_version = '"'""$next_m_version""'"'#' "$f1"

commit_message="new version ""$next_m_version"
tag_name="$next_m_version"

# git commit -m "$commit_message" "$f1"
# echo git tag -a "$next_m_version" -m "$next_m_version"

