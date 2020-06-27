cmake_minimum_required(VERSION 3.4.1)

include_directories(include)
add_definitions(-DWEBRTC_POSIX)

add_library(
        webrtc
        STATIC
        randomization_functions.c
        spl_sqrt_floor.c
        division_operations.c
        vector_scaling_operations.c
        downsample_fast.c
        cross_correlation.c
        spl_init.c
        min_max_operations.c
        complex_fft.c
        complex_bit_reverse.c
        real_fft.c
        delay_estimator.c
        delay_estimator_wrapper.c
        ring_buffer.c
        aecm_core.c
        echo_control_mobile.c)


