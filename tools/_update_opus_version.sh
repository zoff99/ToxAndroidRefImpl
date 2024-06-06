#! /bin/bash

ver="v1.5.2"

cd ../circle_scripts/
grep -ril '_OPUS_VERSION_='|xargs -L1 sed -i -e 's#_OPUS_VERSION_=".*"#_OPUS_VERSION_="'"$ver"'"#'

