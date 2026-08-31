#include "harness.h"
#include <limits.h>
#include <stdint.h>
#include <string.h>

// Match the signature from harness.h
jstring c_safe_string_from_java(const char* str, size_t length);

// Declare the globals that the function uses
extern jclass TrifaToxService_class;
extern jmethodID safe_string_method;

/* =========================================================
 * Helper: Set up standard mock state for a successful call
 * ========================================================= */
static void setup_successful_call(void) {
    TrifaToxService_class = (jclass)0x1234;
    safe_string_method = (jmethodID)0x5678;
    mock_NewByteArray_return = (jbyteArray)0x2222;
    mock_CallStaticObjectMethod_return = (jobject)0x9ABC;
    mock_ExceptionCheck_return = JNI_FALSE;
}

/* =========================================================
 * BASIC & BOUNDARY TESTS
 * ========================================================= */

static void t_valid_utf8_string(void) {
    TEST_BEGIN("t_valid_utf8_string");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    char test_str[] = "hello";
    jstring ret = c_safe_string_from_java(test_str, 5);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_NewByteArray_called);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_ASSERT(mock_CallStaticObjectMethod_called);
    TEST_ASSERT(mock_DeleteLocalRef_called);
    TEST_END();
}

static void t_length_zero_with_valid_ptr(void) {
    TEST_BEGIN("t_length_zero_with_valid_ptr");
    jni_mock_reset();
    tox_mock_reset();
    mock_NewStringUTF_return = (jstring)0x1111;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 0);

    TEST_ASSERT(ret == (jstring)0x1111);
    TEST_ASSERT(mock_NewStringUTF_called);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_zero_with_null_ptr(void) {
    TEST_BEGIN("t_length_zero_with_null_ptr");
    jni_mock_reset();
    tox_mock_reset();
    mock_NewStringUTF_return = (jstring)0x1111;

    jstring ret = c_safe_string_from_java(NULL, 0);

    TEST_ASSERT(ret == (jstring)0x1111);
    TEST_ASSERT(mock_NewStringUTF_called);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_one(void) {
    TEST_BEGIN("t_length_one");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    char test_str[] = "A";
    jstring ret = c_safe_string_from_java(test_str, 1);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_two(void) {
    TEST_BEGIN("t_length_two");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    char test_str[] = "AB";
    jstring ret = c_safe_string_from_java(test_str, 2);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_int_max(void) {
    TEST_BEGIN("t_length_int_max");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    char test_str[] = "A";
    (void)c_safe_string_from_java(test_str, (size_t)INT_MAX);

    TEST_ASSERT(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_int_max_plus_one(void) {
    TEST_BEGIN("t_length_int_max_plus_one");
    jni_mock_reset();
    tox_mock_reset();

    char test_str[] = "A";
    jstring ret = c_safe_string_from_java(test_str, (size_t)INT_MAX + 1);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

static void t_length_size_max(void) {
    TEST_BEGIN("t_length_size_max");
    jni_mock_reset();
    tox_mock_reset();

    char test_str[] = "A";
    jstring ret = c_safe_string_from_java(test_str, SIZE_MAX);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

/* =========================================================
 * NULL & STATE FAILURE TESTS
 * ========================================================= */

static void t_null_instr_nonzero_len(void) {
    TEST_BEGIN("t_null_instr_nonzero_len");
    jni_mock_reset();
    tox_mock_reset();

    jstring ret = c_safe_string_from_java(NULL, 10);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

static void t_null_instr_len_one(void) {
    TEST_BEGIN("t_null_instr_len_one");
    jni_mock_reset();
    tox_mock_reset();

    jstring ret = c_safe_string_from_java(NULL, 1);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(mock_NewByteArray_called);
    TEST_END();
}

static void t_null_jni_env(void) {
    TEST_BEGIN("t_null_jni_env");
    jni_mock_reset();
    tox_mock_reset();
    mock_jni_env_ptr = NULL;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);

    TEST_ASSERT(ret == NULL);
    TEST_END();
}

static void t_null_jni_env_zero_length(void) {
    TEST_BEGIN("t_null_jni_env_zero_length");
    jni_mock_reset();
    tox_mock_reset();
    mock_jni_env_ptr = NULL;

    // Even zero-length path should handle NULL env gracefully
    jstring ret = c_safe_string_from_java(NULL, 0);

    TEST_ASSERT(ret == NULL);
    TEST_END();
}

static void t_null_class_only(void) {
    TEST_BEGIN("t_null_class_only");
    jni_mock_reset();
    tox_mock_reset();

    TrifaToxService_class = NULL;
    safe_string_method = (jmethodID)0x5678;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);
    TEST_ASSERT(ret == NULL);
    TEST_END();
}

static void t_null_method_only(void) {
    TEST_BEGIN("t_null_method_only");
    jni_mock_reset();
    tox_mock_reset();

    TrifaToxService_class = (jclass)0x1234;
    safe_string_method = NULL;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);
    TEST_ASSERT(ret == NULL);
    TEST_END();
}

static void t_both_class_and_method_null(void) {
    TEST_BEGIN("t_both_class_and_method_null");
    jni_mock_reset();
    tox_mock_reset();

    TrifaToxService_class = NULL;
    safe_string_method = NULL;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);
    TEST_ASSERT(ret == NULL);
    TEST_END();
}

static void t_new_byte_array_oom(void) {
    TEST_BEGIN("t_new_byte_array_oom");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();
    mock_NewByteArray_return = NULL;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT(mock_ExceptionClear_called);
    TEST_END();
}

static void t_java_method_throws_exception(void) {
    TEST_BEGIN("t_java_method_throws_exception");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();
    mock_CallStaticObjectMethod_return = NULL;
    mock_ExceptionCheck_return = JNI_TRUE;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT(mock_ExceptionClear_called);
    TEST_ASSERT(mock_DeleteLocalRef_called);
    TEST_END();
}

static void t_java_method_returns_null_without_exception(void) {
    TEST_BEGIN("t_java_method_returns_null_without_exception");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();
    mock_CallStaticObjectMethod_return = NULL;
    mock_ExceptionCheck_return = JNI_FALSE;
    char test_str[] = "hello";

    jstring ret = c_safe_string_from_java(test_str, 5);

    // Function should return NULL (the Java method's return value)
    TEST_ASSERT(ret == NULL);
    TEST_ASSERT(mock_DeleteLocalRef_called);
    TEST_ASSERT_FALSE(mock_ExceptionClear_called);
    TEST_END();
}

/* =========================================================
 * UTF-8 EDGE CASES - Every type of malformed UTF-8
 * ========================================================= */

static void t_truncated_2byte_sequence(void) {
    TEST_BEGIN("t_truncated_2byte_sequence");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xC2 starts a 2-byte sequence but has no continuation
    uint8_t data[] = { 0xC2 };
    jstring ret = c_safe_string_from_java((const char *)data, 1);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_truncated_3byte_sequence(void) {
    TEST_BEGIN("t_truncated_3byte_sequence");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xE0 starts a 3-byte sequence but only has one continuation
    uint8_t data[] = { 0xE0, 0xA0 };
    jstring ret = c_safe_string_from_java((const char *)data, 2);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_truncated_4byte_sequence(void) {
    TEST_BEGIN("t_truncated_4byte_sequence");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xF0 starts a 4-byte sequence but only has two continuations
    uint8_t data[] = { 0xF0, 0x90, 0x80 };
    jstring ret = c_safe_string_from_java((const char *)data, 3);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_overlong_encoding(void) {
    TEST_BEGIN("t_overlong_encoding");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // Overlong encoding of NUL (0xC0 0x80) - invalid UTF-8
    uint8_t data[] = { 0xC0, 0x80 };
    jstring ret = c_safe_string_from_java((const char *)data, 2);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_invalid_continuation_byte(void) {
    TEST_BEGIN("t_invalid_continuation_byte");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xC2 expects a continuation byte (0x80-0xBF) but gets 0x41 ('A')
    uint8_t data[] = { 0xC2, 0x41 };
    jstring ret = c_safe_string_from_java((const char *)data, 2);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_lone_continuation_byte(void) {
    TEST_BEGIN("t_lone_continuation_byte");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0x80 is a continuation byte without a starter
    uint8_t data[] = { 0x80 };
    jstring ret = c_safe_string_from_java((const char *)data, 1);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_invalid_starter_c0_c1(void) {
    TEST_BEGIN("t_invalid_starter_c0_c1");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xC0 and 0xC1 are invalid starters (would produce overlong encodings)
    uint8_t data[] = { 0xC0, 0x80, 0xC1, 0xBF };
    jstring ret = c_safe_string_from_java((const char *)data, 4);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_invalid_starter_f5_to_ff(void) {
    TEST_BEGIN("t_invalid_starter_f5_to_ff");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 0xF5-0xFF are invalid UTF-8 starters (beyond Unicode range)
    uint8_t data[] = { 0xF5, 0x80, 0x80, 0x80, 0xFF, 0xBF, 0xBF, 0xBF };
    jstring ret = c_safe_string_from_java((const char *)data, 8);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_unicode_surrogate_halves(void) {
    TEST_BEGIN("t_unicode_surrogate_halves");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // UTF-8 encodings of surrogate halves (U+D800 to U+DFFF) - invalid
    // U+D800 = 0xED 0xA0 0x80
    // U+DFFF = 0xED 0xBF 0xBF
    uint8_t data[] = { 0xED, 0xA0, 0x80, 0xED, 0xBF, 0xBF };
    jstring ret = c_safe_string_from_java((const char *)data, 6);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_beyond_unicode_max(void) {
    TEST_BEGIN("t_beyond_unicode_max");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // U+110000 (first code point beyond Unicode) = 0xF4 0x90 0x80 0x80
    uint8_t data[] = { 0xF4, 0x90, 0x80, 0x80 };
    jstring ret = c_safe_string_from_java((const char *)data, 4);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_mixed_valid_and_invalid_utf8(void) {
    TEST_BEGIN("t_mixed_valid_and_invalid_utf8");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // Mix of valid ASCII, valid multi-byte, and invalid sequences
    uint8_t data[] = {
        'H', 'e', 'l', 'l', 'o',           // Valid ASCII
        0xC3, 0xA9,                         // Valid: é
        0xFF,                               // Invalid
        0xE2, 0x82, 0xAC,                   // Valid: €
        0x80,                               // Invalid lone continuation
        'W', 'o', 'r', 'l', 'd'             // Valid ASCII
    };
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_all_valid_multibyte_sequences(void) {
    TEST_BEGIN("t_all_valid_multibyte_sequences");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // Every type of valid multi-byte UTF-8 sequence
    uint8_t data[] = {
        // 2-byte: U+00E9 (é)
        0xC3, 0xA9,
        // 3-byte: U+20AC (€)
        0xE2, 0x82, 0xAC,
        // 4-byte: U+1F600 (😀)
        0xF0, 0x9F, 0x98, 0x80,
        // 3-byte: U+4E2D (中)
        0xE4, 0xB8, 0xAD,
        // 2-byte: U+00A0 (non-breaking space)
        0xC2, 0xA0
    };
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

/* =========================================================
 * PATTERN-BASED DATA TESTS
 * ========================================================= */

static void t_all_zero_bytes(void) {
    TEST_BEGIN("t_all_zero_bytes");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[256];
    memset(data, 0x00, sizeof(data));
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_all_0xff_bytes(void) {
    TEST_BEGIN("t_all_0xff_bytes");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[256];
    memset(data, 0xFF, sizeof(data));
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_alternating_00_ff(void) {
    TEST_BEGIN("t_alternating_00_ff");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[256];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (i % 2 == 0) ? 0x00 : 0xFF;
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_incrementing_bytes(void) {
    TEST_BEGIN("t_incrementing_bytes");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[256];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)i;
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_decrementing_bytes(void) {
    TEST_BEGIN("t_decrementing_bytes");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[256];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(255 - i);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_all_ascii_printable(void) {
    TEST_BEGIN("t_all_ascii_printable");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[95];  // ASCII 32-126 (printable)
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(32 + i);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_all_control_characters(void) {
    TEST_BEGIN("t_all_control_characters");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[32];  // ASCII 0-31 (control characters)
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)i;
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_all_high_bytes(void) {
    TEST_BEGIN("t_all_high_bytes");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[128];  // 0x80-0xFF
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(0x80 + i);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

/* =========================================================
 * LARGE PAYLOAD TESTS
 * ========================================================= */

static void t_large_payload_1kb(void) {
    TEST_BEGIN("t_large_payload_1kb");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[1024];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(i % 256);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_large_payload_4kb(void) {
    TEST_BEGIN("t_large_payload_4kb");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[4096];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(i % 256);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_large_payload_64kb(void) {
    TEST_BEGIN("t_large_payload_64kb");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    uint8_t data[65536];
    for (size_t i = 0; i < sizeof(data); i++) {
        data[i] = (uint8_t)(i % 256);
    }
    jstring ret = c_safe_string_from_java((const char *)data, sizeof(data));

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_END();
}

static void t_large_payload_1mb(void) {
    TEST_BEGIN("t_large_payload_1mb");
    jni_mock_reset();
    tox_mock_reset();
    setup_successful_call();

    // 1MB payload - just under INT_MAX
    size_t size = 1024 * 1024;
    uint8_t *data = malloc(size);
    if (!data) {
        TEST_ASSERT(0);  // malloc failed
        TEST_END();
        return;
    }
    for (size_t i = 0; i < size; i++) {
        data[i] = (uint8_t)(i % 256);
    }
    jstring ret = c_safe_string_from_java((const char *)data, size);

    TEST_ASSERT(ret == (jstring)0x9ABC);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    free(data);
    TEST_END();
}

/* =========================================================
 * STRESS & FUZZ TESTS
 * ========================================================= */

static void t_fuzz_like_stress_test(void) {
    TEST_BEGIN("t_fuzz_like_stress_test");

    uint8_t fuzz_buffer[1024];
    
    // Run 10,000 iterations with varying lengths and pseudo-random data
    for (int i = 0; i < 10000; i++) {
        jni_mock_reset();
        tox_mock_reset();
        setup_successful_call();
        
        // Pseudo-random length between 1 and 1024
        size_t len = (size_t)((i * 17 + 31) % 1024) + 1;
        
        // Fill with pseudo-random pattern
        for (size_t j = 0; j < len; j++) {
            fuzz_buffer[j] = (uint8_t)((i * j + 7) % 256);
        }

        jstring ret = c_safe_string_from_java((const char *)fuzz_buffer, len);

        TEST_ASSERT(ret == (jstring)0x9ABC);
        TEST_ASSERT(mock_NewByteArray_called);
        TEST_ASSERT(mock_SetByteArrayRegion_called);
    }
    
    TEST_END();
}

static void t_fuzz_utf8_edge_cases(void) {
    TEST_BEGIN("t_fuzz_utf8_edge_cases");

    uint8_t fuzz_buffer[256];
    
    // Run 5,000 iterations focusing on UTF-8 boundary bytes
    for (int i = 0; i < 5000; i++) {
        jni_mock_reset();
        tox_mock_reset();
        setup_successful_call();
        
        size_t len = (size_t)((i * 13 + 41) % 200) + 1;
        
        // Fill with UTF-8 boundary bytes (0x80-0xFF) mixed with ASCII
        for (size_t j = 0; j < len; j++) {
            uint8_t byte_val;
            switch ((i + j) % 5) {
                case 0: byte_val = (uint8_t)(0x80 + ((i * j) % 64)); break;  // Continuation bytes
                case 1: byte_val = (uint8_t)(0xC0 + ((i * j) % 32)); break;  // 2-byte starters
                case 2: byte_val = (uint8_t)(0xE0 + ((i * j) % 16)); break;  // 3-byte starters
                case 3: byte_val = (uint8_t)(0xF0 + ((i * j) % 8)); break;   // 4-byte starters
                default: byte_val = (uint8_t)((i + j) % 128); break;         // ASCII
            }
            fuzz_buffer[j] = byte_val;
        }

        jstring ret = c_safe_string_from_java((const char *)fuzz_buffer, len);

        TEST_ASSERT(ret == (jstring)0x9ABC);
        TEST_ASSERT(mock_NewByteArray_called);
    }
    
    TEST_END();
}

static void t_concurrent_call_simulation(void) {
    TEST_BEGIN("t_concurrent_call_simulation");

    char test_str[] = "thread_safe_test";
    
    // Simulate rapid successive calls (like from multiple threads)
    for (int i = 0; i < 1000; i++) {
        jni_mock_reset();
        tox_mock_reset();
        setup_successful_call();
        
        jstring ret = c_safe_string_from_java(test_str, 16);
        TEST_ASSERT(ret == (jstring)0x9ABC);
        TEST_ASSERT(mock_DeleteLocalRef_called);
    }
    
    TEST_END();
}

static void t_rapid_exception_simulation(void) {
    TEST_BEGIN("t_rapid_exception_simulation");

    char test_str[] = "exception_test";
    
    // Simulate rapid calls where Java throws exceptions
    for (int i = 0; i < 1000; i++) {
        jni_mock_reset();
        tox_mock_reset();
        setup_successful_call();
        mock_CallStaticObjectMethod_return = NULL;
        mock_ExceptionCheck_return = JNI_TRUE;
        
        jstring ret = c_safe_string_from_java(test_str, 14);
        TEST_ASSERT(ret == NULL);
        TEST_ASSERT(mock_ExceptionClear_called);
        TEST_ASSERT(mock_DeleteLocalRef_called);
    }
    
    TEST_END();
}

static void t_alternating_success_and_failure(void) {
    TEST_BEGIN("t_alternating_success_and_failure");

    char test_str[] = "alternating_test";
    
    // Simulate alternating success and failure scenarios
    for (int i = 0; i < 1000; i++) {
        jni_mock_reset();
        tox_mock_reset();
        setup_successful_call();
        
        jstring ret;
        if (i % 2 == 0) {
            // Success case
            ret = c_safe_string_from_java(test_str, 16);
            TEST_ASSERT(ret == (jstring)0x9ABC);
        } else {
            // Failure case (OOM)
            mock_NewByteArray_return = NULL;
            ret = c_safe_string_from_java(test_str, 16);
            TEST_ASSERT(ret == NULL);
            TEST_ASSERT(mock_ExceptionClear_called);
        }
    }
    
    TEST_END();
}

/* =========================================================
 * TEST RUNNER
 * ========================================================= */

void run_c_safe_string_from_java_tests(void) {
    SUITE_BEGIN("c_safe_string_from_java: basic");
    t_valid_utf8_string();
    t_length_zero_with_valid_ptr();
    t_length_zero_with_null_ptr();
    t_length_one();
    t_length_two();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: boundaries");
    t_length_int_max();
    t_length_int_max_plus_one();
    t_length_size_max();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: null_and_state");
    t_null_instr_nonzero_len();
    t_null_instr_len_one();
    t_null_jni_env();
    t_null_jni_env_zero_length();
    t_null_class_only();
    t_null_method_only();
    t_both_class_and_method_null();
    t_new_byte_array_oom();
    t_java_method_throws_exception();
    t_java_method_returns_null_without_exception();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: utf8_edge_cases");
    t_truncated_2byte_sequence();
    t_truncated_3byte_sequence();
    t_truncated_4byte_sequence();
    t_overlong_encoding();
    t_invalid_continuation_byte();
    t_lone_continuation_byte();
    t_invalid_starter_c0_c1();
    t_invalid_starter_f5_to_ff();
    t_unicode_surrogate_halves();
    t_beyond_unicode_max();
    t_mixed_valid_and_invalid_utf8();
    t_all_valid_multibyte_sequences();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: patterns");
    t_all_zero_bytes();
    t_all_0xff_bytes();
    t_alternating_00_ff();
    t_incrementing_bytes();
    t_decrementing_bytes();
    t_all_ascii_printable();
    t_all_control_characters();
    t_all_high_bytes();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: large_payloads");
    t_large_payload_1kb();
    t_large_payload_4kb();
    t_large_payload_64kb();
    t_large_payload_1mb();
    SUITE_END();

    SUITE_BEGIN("c_safe_string_from_java: stress");
    t_fuzz_like_stress_test();
    t_fuzz_utf8_edge_cases();
    t_concurrent_call_simulation();
    t_rapid_exception_simulation();
    t_alternating_success_and_failure();
    SUITE_END();
}
