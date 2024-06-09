#! /bin/bash

r3='https://github.com/xiph/opus'
git ls-remote --refs --sort='v:refname' --tags "$r3" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1
