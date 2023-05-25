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
                python \
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
