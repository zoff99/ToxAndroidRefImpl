#!/bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

# start in the script directory
cd $_HOME_/
pwd 
# enter gwitness directory
cd gwitness/
pwd

set -e

../gradlew build || echo "*ERROR*"

