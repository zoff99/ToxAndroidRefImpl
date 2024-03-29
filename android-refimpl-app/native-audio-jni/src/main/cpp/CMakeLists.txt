cmake_minimum_required(VERSION 3.4.1)
project("native-audio-jni")

add_subdirectory(webrtc6)
include_directories(./webrtc6)

set(CMAKE_C_FLAGS1 "${CMAKE_C_FLAGS} -Wall \
-Wextra \
-Weverything \
-Wno-cast-align \
-Wno-conversion \
-Wno-covered-switch-default \
-Wno-disabled-macro-expansion \
-Wno-documentation-deprecated-sync \
-Wno-format-nonliteral \
-Wno-missing-field-initializers \
-Wno-padded \
-Wno-reserved-id-macro \
-Wno-sign-compare \
-Wno-thread-safety-analysis \
-Wno-type-limits \
-Wno-unused-parameter \
-Wno-used-but-marked-unused \
-Wno-vla \
-Wno-documentation-unknown-command \
\
-Wno-missing-variable-declarations \
-Wno-missing-prototypes \
-Wno-error=deprecated-declarations \
-Wno-error=unused-macros \
-Wno-error=float-equal \
-Wno-error=cast-qual \
-Wno-error=strict-prototypes \
-Wno-error=gnu-statement-expression \
-Wno-documentation \
-Werror \
")

set(CMAKE_C_FLAGS2 "${CMAKE_C_FLAGS} -Wall -Iwebrtc6/webrtc")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS1} -std=c99 -fPIC -g -fstack-protector-all")

add_library(native-audio-jni SHARED
        native-audio-jni.c
        )

# Include libraries needed for native-audio-jni lib
target_link_libraries(native-audio-jni
        android
        aec2
        log
        OpenSLES)
