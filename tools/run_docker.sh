#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

# docker info

mkdir -p $_HOME_/artefacts
mkdir -p $_HOME_/script
mkdir -p $_HOME_/workspace

echo '#! /bin/bash

## ----------------------
numcpus_=$(nproc)
quiet_=1
## ----------------------

echo "hello"

export qqq=""

if [ "$quiet_""x" == "1x" ]; then
	export qqq=" -qq "
fi


redirect_cmd() {
    if [ "$quiet_""x" == "1x" ]; then
        "$@" > /dev/null 2>&1
    else
        "$@"
    fi
}


echo "installing system packages ..."

redirect_cmd apt-get update $qqq

redirect_cmd apt-get install $qqq -y --force-yes --no-install-recommends lsb-release
system__=$(lsb_release -i|cut -d ':' -f2|sed -e "s#\s##g")
version__=$(lsb_release -r|cut -d ':' -f2|sed -e "s#\s##g")
echo "compiling on: $system__ $version__"

echo "installing more system packages ..."

pkgs="
    rsync
    clang
    cmake
    libconfig-dev
    libgtest-dev
    libopus-dev
    libsodium-dev
    libvpx-dev
    ninja-build
    pkg-config
"

for i in $pkgs ; do
    redirect_cmd apt-get install $qqq -y --force-yes --no-install-recommends $i
done

pkgs_z="
    binutils
    llvm-dev
    libavutil-dev
    libavcodec-dev
    libavformat-dev
    libavfilter-dev
    libx264-dev
"

for i in $pkgs_z ; do
    redirect_cmd apt-get install $qqq -y --force-yes --no-install-recommends $i
done


echo ""
echo ""
echo "--------------------------------"
echo "clang version:"
c++ --version
echo "--------------------------------"
echo ""
echo ""

echo "make a local copy ..."
redirect_cmd rsync -avz /src/circle_scripts /workspace/
redirect_cmd rsync -avz /src/jni-c-toxcore /workspace/

cd /workspace/
ls -al

/src/circle_scripts/deps.sh

mkdir -p /artefacts/asan/
chmod a+rwx -R /workspace/
chmod a+rwx -R /artefacts/

' > $_HOME_/script/do_it___external.sh

chmod a+rx $_HOME_/script/do_it___external.sh


system_to_build_for="ubuntu:18.04"

cd $_HOME_/
time docker run -ti --rm \
  -v $_HOME_/artefacts:/artefacts \
  -v $_HOME_/script:/script \
  -v $_HOME_/../:/src \
  -v $_HOME_/workspace:/workspace \
  "$system_to_build_for" \
  /bin/bash # /script/do_it___external.sh

