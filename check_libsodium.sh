#! /bin/bash

r4='https://github.com/jedisct1/libsodium'
git ls-remote --refs --sort='v:refname' --tags "$r4" \
    | cut --delimiter='/' --fields=3 | grep '\-RELEASE' | tail --lines=1
