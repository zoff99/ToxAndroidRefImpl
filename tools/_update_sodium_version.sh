#! /bin/bash

ver="v1.0.20"

cd ../circle_scripts/
grep -ril '_LIBSODIUM_VERSION_='|xargs -L1 sed -i -e 's#_LIBSODIUM_VERSION_=".*"#_LIBSODIUM_VERSION_="'"$ver"'"#'

