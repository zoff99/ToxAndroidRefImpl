# syntax=docker/dockerfile:1
FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
        apt-get -y --force-yes install \
            lsb-release \
                clang \
                cmake \
                libconfig-dev \
                libgtest-dev \
                ninja-build \
                pkg-config \
                zip grep file ca-certificates autotools-dev autoconf automake \
                git bc wget rsync cmake make pkg-config libtool \
                ssh gzip tar \
                coreutils \
                curl \
                libncurses5

ENV _ANDROID_SDK_FILE_XX="sdk-tools-linux-4333796.zip"
ENV _ANDROID_NDK_FILE_XX="android-ndk-r21e-linux-x86_64.zip"

RUN curl https://dl.google.com/android/repository/"$_ANDROID_SDK_FILE_XX" -o /sdk.zip
RUN curl https://dl.google.com/android/repository/"$_ANDROID_NDK_FILE_XX" -o /ndk.zip

RUN apt-get update && \
        apt-get -y --force-yes install \
            wget \
            git \
            curl \
            software-properties-common \
            unzip \
            zip \
            automake \
            autotools-dev \
            build-essential \
            check \
            checkinstall \
            libtool \
            libfreetype6-dev \
            fontconfig-config \
            libfontconfig1-dev \
            pkg-config \
            openjdk-8-jdk

RUN touch /installing_more_system_packages_done

RUN apt-get update && \
        apt-get -y --force-yes install \
            python
RUN mkdir -p /root/work/ \
    mkdir -p /root/work/deploy/ \
    mkdir -p /root/.android/

RUN mkdir -p /root/work/build_dir/
RUN mkdir -p /root/work/circle_scripts/

COPY __temp__deps.sh /root/work/circle_scripts/deps.sh
RUN chmod a+rx /root/work/circle_scripts/deps.sh

COPY ./__temp__src/ /root/work/

RUN id -a; pwd; ls -al /root/work/; cd /root/work/build_dir/ && bash ../circle_scripts/deps.sh || exit 1


