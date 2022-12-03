cmake_minimum_required(VERSION 3.4.1)
project(loggingstdout-c)

add_library(loggingstdout SHARED
        loggingstdout.c)

target_link_libraries(loggingstdout
        android
        log)
