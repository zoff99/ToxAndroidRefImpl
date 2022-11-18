SET(WRAPPER_SRC
  "interface/aligned_array.h"
  "interface/aligned_malloc.h"
  "interface/atomic32.h"
  "interface/clock.h"
  "interface/condition_variable_wrapper.h"
  "interface/cpu_features_wrapper.h"
  "interface/cpu_info.h"
  "interface/critical_section_wrapper.h"
  "interface/data_log.h"
  "interface/data_log_c.h"
  "interface/data_log_impl.h"
  "interface/event_tracer.h"
  "interface/event_wrapper.h"
  "interface/field_trial.h"
  "interface/file_wrapper.h"
  "interface/fix_interlocked_exchange_pointer_win.h"
  "interface/logging.h"
  "interface/metrics.h"
  "interface/ref_count.h"
  "interface/rtp_to_ntp.h"
  "interface/rw_lock_wrapper.h"
  "interface/scoped_vector.h"
  "interface/sleep.h"
  "interface/sort.h"
  "interface/static_instance.h"
  "interface/stl_util.h"
  "interface/stringize_macros.h"
  "interface/thread_wrapper.h"
  "interface/tick_util.h"
  "interface/timestamp_extrapolator.h"
  "interface/trace.h"
  "interface/trace_event.h"
  "interface/utf_util_win.h"
  "interface/asm_defines.h"
  "source/aligned_malloc.cc"

  # "source/atomic32_win.cc"
  "source/clock.cc"
  "source/condition_variable.cc"
  # "source/condition_variable_event_win.cc"
  # "source/condition_variable_event_win.h"
  # "source/condition_variable_native_win.cc"
  # "source/condition_variable_native_win.h"
  "source/condition_variable_posix.cc"
  "source/condition_variable_posix.h"
  "source/cpu_features.cc"
  "source/cpu_info.cc"
  "source/critical_section.cc"
  "source/critical_section_posix.cc"
  "source/critical_section_posix.h"
  # "source/critical_section_win.cc"
  # "source/critical_section_win.h"
  "source/data_log_c.cc"
  "source/event.cc"
  "source/event_timer_posix.cc"
  "source/event_timer_posix.h"
  # "source/event_timer_win.cc"
  # "source/event_timer_win.h"
  "source/event_tracer.cc"
  "source/file_impl.cc"
  "source/file_impl.h"
  "source/logging.cc"
  "source/rtp_to_ntp.cc"
  "source/rw_lock.cc"
  "source/rw_lock_generic.cc"
  "source/rw_lock_generic.h"
  "source/rw_lock_posix.cc"
  "source/rw_lock_posix.h"
  # "source/rw_lock_win.cc"
  # "source/rw_lock_win.h"
  "source/sleep.cc"
  "source/sort.cc"
  "source/thread.cc"
  "source/thread_posix.cc"
  "source/thread_posix.h"
  # "source/thread_win.cc"
  # "source/thread_win.h"
  "source/tick_util.cc"
  "source/timestamp_extrapolator.cc"
  "source/trace_impl.cc"
  "source/trace_impl.h"
  "source/trace_posix.cc"
  "source/trace_posix.h"
  # "source/trace_win.cc"
  # "source/trace_win.h"
  "source/data_log_no_op.cc"
  "source/metrics_default.cc"

  )
SET(LIBS  "")
if(APPLE)
  if(IOS)
    ADD_DEFINITIONS(-DWEBRTC_THREAD_RR
      -DWEBRTC_CLOCK_TYPE_REALTIME
      
      -DWEBRTC_POSIX
      -DWEBRTC_MAC
      -DWEBRTC_IOS
      )
    SET(WRAPPER_SRC
      ${WRAPPER_SRC}
      "source/cpu_features_ios.c"
      "source/atomic32_mac.cc"
      )
  else()
    ADD_DEFINITIONS(-DWEBRTC_THREAD_RR
      -DWEBRTC_CLOCK_TYPE_REALTIME
      -DWEBRTC_POSIX
      -DWEBRTC_MAC)
    SET(WRAPPER_SRC
      ${WRAPPER_SRC}
      "source/atomic32_mac.cc")
  endif()
elseif(ANDROID)
  message("system wrapper: build for android")
  ADD_DEFINITIONS(-DWEBRTC_THREAD_RR
    -DWEBRTC_CLOCK_TYPE_REALTIME
    -DWEBRTC_POSIX
    -DWEBRTC_LINUX
    -DWEBRTC_ANDROID)
  #SET(LIBS ${LIBS} "cpufeatures")
  SET(WRAPPER_SRC
    ${WRAPPER_SRC}
    "interface/logcat_trace_context.h"
    "source/logcat_trace_context.cc"
    "source/atomic32_posix.cc"
    "source/cpu_features_android.c"
    )
endif()

foreach(var ${LIBS})
  message(item:${var})
  FIND_LIBRARY(find${var} ${var})
  message(full path:${find${var}})
  if(${lib2} MATCHES "NOTFOUND")
    message(SEND_ERROR "library not not found:${var}")
  else()
    set(LIBRARYS ${LIBRARYS} ${find${var}})
  endif()
endforeach()


add_library(SystemWrapper STATIC ${WRAPPER_SRC})
TARGET_LINK_LIBRARIES(SystemWrapper BASE COMMON ${LIBRARYS})
