cmake_minimum_required(VERSION 3.4.1)
project(rnnoise_module)

SET(CMAKE_SKIP_BUILD_RPATH TRUE)
if (ANDROID)
    ADD_DEFINITIONS(-DWEBRTC_POSIX)
    SET(CMAKE_CXX_FLAGS " -std=c++11 -v ${CMAKE_CXX_FLAGS} ")
    SET(CMAKE_C_FLAGS " ${CMAKE_C_FLAGS} -g")
else ()
    ADD_DEFINITIONS(-DWEBRTC_POSIX)
    SET(CMAKE_CXX_FLAGS " -std=c++11 -msse2  ${CMAKE_CXX_FLAGS} ")
    SET(CMAKE_C_FLAGS " -msse2 ${CMAKE_C_FLAGS} -g")
endif ()
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR})

include_directories(./)
include_directories(./include)
include_directories(./src)

set(SOURCE
        src/denoise.c
        src/rnn.c
        src/pitch.c
        src/kiss_fft.c
        src/celt_lpc.c
        src/nnet.c
        src/nnet_default.c
        src/parse_lpcnet_weights.c
        src/rnnoise_data.c
        src/rnnoise_tables.c
   )

add_library(
        rnnoiselib1
        STATIC
        ${SOURCE}
)
