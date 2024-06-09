#! /bin/bash

r1='https://github.com/FFmpeg/FFmpeg'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '^v' | grep -v '\-dev'|tail --lines=1)

echo "$ver"

f='./circle_scripts/deps.sh'

grep -ril '_FFMPEG_VERSION_='|xargs -L1 sed -i -e 's#_FFMPEG_VERSION_="n7.0.1"#'
