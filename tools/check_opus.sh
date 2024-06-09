#! /bin/bash

r1='https://github.com/xiph/opus'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1)

echo "$ver"

d='./circle_scripts/'
f='deps.sh'

cd "$d"
grep -ril '_OPUS_VERSION_='|xargs -L1 sed -i -e 's#_OPUS_VERSION_=".*"#_OPUS_VERSION_="'"$ver"'"#'
