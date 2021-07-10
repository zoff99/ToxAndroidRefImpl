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
export DEBIAN_FRONTEND=noninteractive

redirect_cmd apt-get update $qqq

redirect_cmd apt-get install $qqq -y --force-yes --no-install-recommends lsb-release
system__=$(lsb_release -i|cut -d ':' -f2|sed -e "s#\s##g")
version__=$(lsb_release -r|cut -d ':' -f2|sed -e "s#\s##g")
echo "compiling on: $system__ $version__"

echo "installing more system packages ..."

pkgs="
    clang
    cmake
    libconfig-dev
    libgtest-dev
    ninja-build
    pkg-config
    zip grep file ca-certificates autotools-dev autoconf automake
    git bc wget rsync cmake make pkg-config libtool
    ssh gzip tar
    coreutils
    python
    libncurses5
"

for i in $pkgs ; do
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

cd /trifa/

pwd
id -a

mkdir -p /root/work/
mkdir -p /root/work/deploy/
mkdir -p /root/.android/

echo "make a local copy ..."
redirect_cmd rsync -avz --exclude=".localrun" --exclude="local.properties" ./ /root/work/

cd /root/work/
mkdir -p build_dir

cd /root/work/build_dir/
bash ../circle_scripts/deps.sh || exit 1

cd /root/work/build_dir/
bash ../circle_scripts/trifa.sh || exit 1

cp -av /root/work//artefacts//_circleci_.apk /artefacts/trifa_localrun.apk

chmod a+rwx -R /workspace/
chmod a+rwx -R /artefacts/

' > $_HOME_/script/do_it___external.sh

chmod a+rx $_HOME_/script/do_it___external.sh


system_to_build_for="ubuntu:20.04"

cd $_HOME_/
docker run -ti --rm \
  -v $_HOME_/artefacts:/artefacts \
  -v $_HOME_/script:/script \
  -v $_HOME_/../:/trifa \
  -v $_HOME_/workspace:/workspace \
  -e DISPLAY=$DISPLAY \
  "$system_to_build_for" \
  /bin/bash /script/do_it___external.sh

