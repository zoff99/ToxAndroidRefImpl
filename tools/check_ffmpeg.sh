#! /bin/bash

r1='https://github.com/FFmpeg/FFmpeg'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '^v' | grep -v '\-dev'|tail --lines=1)

echo "__VERSIONUPDATE__:""$ver"

d='./circle_scripts/'
f='deps.sh'

cd "$d"
grep -ril '_FFMPEG_VERSION_='|xargs -L1 sed -i -e 's#_FFMPEG_VERSION_=".*"#_FFMPEG_VERSION_="'"$ver"'"#'
