#! /bin/bash

ver="n7.0.1"

cd ../circle_scripts/

grep -ril '_FFMPEG_VERSION_='|xargs -L1 sed -i -e 's#_FFMPEG_VERSION_=".*"#_FFMPEG_VERSION_="'"$ver"'"#'

