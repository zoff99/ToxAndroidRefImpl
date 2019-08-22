#!/bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

# start in the script directory
cd $_HOME_/

set -e

PROJECTS=(
    'app'
)

# clear witness files to prevent errors when upgrading dependencies
for project in ${PROJECTS[@]}
do
    echo "" > ${project}/witness.gradle
done

# calculating new checksums
for project in ${PROJECTS[@]}
do
    echo "Calculating new checksums for ${project} ..."
    ./gradlew -q --configure-on-demand ${project}:calculateChecksums | \
    grep -v 'and:sdk:platforms:android.jar' | \
    grep -v 'com.android.tools.' | \
    grep -v 'org.ow2.asm.' | \
    grep -v '^\(Skipping\|Verifying\|Welcome to Gradle\)' \
    > ${project}/witness.gradle
done
