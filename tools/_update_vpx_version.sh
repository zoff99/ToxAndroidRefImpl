#! /bin/bash

ver="v1.14.1"

cd ../circle_scripts/
grep -ril '_VPX_VERSION_='|xargs -L1 sed -i -e 's#_VPX_VERSION_=".*"#_VPX_VERSION_="'"$ver"'"#'

