cmake_minimum_required(VERSION 3.4.1)
project("trifa_yuv")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -fPIC -g -fstack-protector-all -Wl,-z,max-page-size=16384")
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-z,max-page-size=16384")


add_library(
        yuv_converter
        SHARED
        yuv_converter.cpp
)

# Enable optimizations
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -ffast-math -Wl,-z,max-page-size=16384")

# Architecture-specific flags
if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(yuv_converter PRIVATE -mfpu=neon)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    # ARM64 always has NEON
elseif(${ANDROID_ABI} STREQUAL "x86" OR ${ANDROID_ABI} STREQUAL "x86_64")
    # Enable SSE2 and SSSE3 for fast byte-shuffling
    target_compile_options(yuv_converter PRIVATE -msse2 -mssse3)
endif()

find_library(
        log-lib
        log
)

target_link_libraries(
        yuv_converter
        ${log-lib}
)
