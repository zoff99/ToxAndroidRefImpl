cmake_minimum_required(VERSION 3.4.1)
project(loggingstdout-c)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC -g -fstack-protector-all -Wl,-z,max-page-size=16384")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -g -fstack-protector-all -Wl,-z,max-page-size=16384")

add_library(loggingstdout SHARED
        loggingstdout.c)

target_link_libraries(loggingstdout
        android
        log)
