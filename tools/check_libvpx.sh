#! /bin/bash

r1='https://github.com/webmproject/libvpx'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1)

echo "$ver"

d='./circle_scripts/'
f='deps.sh'

cd "$d"
grep -ril '_LIBSODIUM_VERSION_='|xargs -L1 sed -i -e 's#_LIBSODIUM_VERSION_=".*"#_LIBSODIUM_VERSION_="'"$ver"'"#'
