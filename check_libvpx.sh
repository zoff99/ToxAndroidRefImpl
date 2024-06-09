#! /bin/bash

r2='https://github.com/webmproject/libvpx'
git ls-remote --refs --sort='v:refname' --tags "$r2" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1
