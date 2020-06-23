cmake_minimum_required(VERSION 3.4.1)


set(CMAKE_C_FLAGS2 "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS2} -std=c99 -fPIC")

set(SOURCES_FILTER_AUDIO
        filter_audio.c
        vad/webrtc_vad.c
        vad/vad_core.c
        vad/vad_gmm.c
        vad/vad_filterbank.c
        vad/vad_sp.c
        zam/filters.c
        other/energy.c
        other/float_util.c
        other/ring_buffer.c
        other/downsample_fast.c
        other/speex_resampler.c
        other/high_pass_filter.c
        other/resample_by_2_internal.c
        other/randomization_functions.c
        other/cross_correlation.c
        other/copy_set_operations.c
        other/complex_bit_reverse.c
        other/real_fft.c
        other/resample_by_2.c
        other/spl_init.c
        other/resample_fractional.c
        other/vector_scaling_operations.c
        other/get_scaling_square.c
        other/delay_estimator.c
        other/delay_estimator_wrapper.c
        other/splitting_filter.c
        other/spl_sqrt_floor.c
        other/resample_48khz.c
        other/spl_sqrt.c
        other/fft4g.c
        other/division_operations.c
        other/dot_product_with_scale.c
        other/complex_fft.c
        other/min_max_operations.c
        aec/aec_rdft.c
        aec/echo_cancellation.c
        aec/aec_rdft_sse2.c
        aec/aec_core.c
        aec/aec_core_sse2.c
        aec/aec_resampler.c
        ns/noise_suppression.c
        ns/nsx_core_c.c
        ns/noise_suppression_x.c
        ns/nsx_core.c
        ns/ns_core.c
        agc/digital_agc.c
        agc/analog_agc.c
        )

add_library(filter_audio STATIC
        ${SOURCES_FILTER_AUDIO}
        )

