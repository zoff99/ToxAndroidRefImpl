#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../android-refimpl-app/"
cd "$basedir"

f1='app/witness.gradle'

pkg='pkgs_zoffccAndroidJDBC'

INPUT=$(cat "$f1" 2>/dev/null |grep 'com.github.zoff99:'"$pkg"':')

VERSION=$(echo "$INPUT" | cut -d':' -f3)
echo "Detected Version: $VERSION"

FILENAME=$(echo "$INPUT" | cut -d':' -f4)
EMBEDDED_HASH=$(echo "$INPUT" | cut -d':' -f5 | tr -d "',")

echo "Target File: $FILENAME"
echo "Embedded Hash: $EMBEDDED_HASH"

# Download the official .sha256 file
SHA_URL="https://raw.githubusercontent.com/zoff99/${pkg}/refs/heads/master/${pkg}-${VERSION}.aar.sha256"

echo "Checksum URL: $SHA_URL"
REMOTE_SHA_FILE="remote.sha256"

echo "Downloading remote hash..."
curl -sL "$SHA_URL" -o "$REMOTE_SHA_FILE"

# Extract the hash from the downloaded file
# (Removing extra spaces/filenames that might be in the .sha256 file)
REMOTE_HASH=$(awk '{print $1}' "$REMOTE_SHA_FILE")

echo "Remote Hash:   $REMOTE_HASH"

# Compare the sums
if [ "$EMBEDDED_HASH" == "$REMOTE_HASH" ]; then
    echo "SUCCESS: The SHA256 sums match!"
    rm "$REMOTE_SHA_FILE"
    exit 0
else
    echo "FAILURE: The SHA256 sums do NOT match."
    rm "$REMOTE_SHA_FILE"
    exit 1
fi



