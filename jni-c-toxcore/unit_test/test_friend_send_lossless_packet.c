#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1lossless_1packet

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jbyteArray data,
    jint data_length
);

static void t_normal_packet_ok(void) {
    TEST_BEGIN("t_normal_packet_ok");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t bytes[16] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        42,
        (jbyteArray)&arr,
        16
    );

    NOTE("JNI lossless packet call survived");

    TEST_EQUAL_LONG(1, ret);
    TEST_ASSERT(tox_mock_lossless_called);
    TEST_EQUAL_LONG(42, tox_mock_last_lossless_friend_number);
    TEST_EQUAL_SIZE(16, tox_mock_last_lossless_length);
    TEST_ASSERT(tox_mock_last_lossless_data == bytes);

    TEST_END();
}

static void t_tox_error_returns_minus_99(void) {
    TEST_BEGIN("t_tox_error_returns_minus_99");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_lossless_return = 0;
    tox_mock_lossless_error = TOX_ERR_FRIEND_CUSTOM_PACKET_TOO_LONG;

    uint8_t bytes[4] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        (jbyteArray)&arr,
        4
    );

    NOTE("JNI call survived toxcore error");

    TEST_EQUAL_LONG(-99, ret);
    TEST_ASSERT(tox_mock_lossless_called);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    uint8_t bytes[4] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        (jbyteArray)&arr,
        4
    );

    NOTE("JNI call survived NULL tox_global");

    TEST_EQUAL_LONG(-9991, ret);
    TEST_ASSERT_FALSE(tox_mock_lossless_called);

    TEST_END();
}

/*
    SECURITY TEST

    This test FAILS on the current vulnerable code.

    The Java array has only 10 bytes, but Java passes data_length = 1000.

    Secure invariant:

        The JNI layer must never ask toxcore to process more bytes
        than are present in the Java byte array.

    Possible correct fixes:

      - reject data_length > real array length
      - clamp data_length to real array length
      - return an error before calling toxcore

    This test accepts any fix that preserves the invariant.
*/
static void t_data_length_must_be_validated(void) {
    TEST_BEGIN("t_data_length_must_be_validated");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t bytes[10] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        2,
        (jbyteArray)&arr,
        1000
    );

    NOTE("attempted overlength data_length: 1000 for a 10-byte Java array");

    (void)ret;

    /*
        Secure expectation.

        If toxcore was called, the length passed to toxcore must not be
        larger than the actual Java array length.

        If the JNI function rejected the request and did not call toxcore,
        tox_mock_last_lossless_length remains 0, which is also okay.
    */
    TEST_ASSERT(tox_mock_last_lossless_length <= (size_t)arr.length);

    TEST_END();
}

void run_friend_send_lossless_packet_tests(void) {
    SUITE_BEGIN("lossless packet: basic");
    t_normal_packet_ok();
    SUITE_END();

    SUITE_BEGIN("lossless packet: errors");
    t_tox_error_returns_minus_99();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("lossless packet: security");
    t_data_length_must_be_validated();
    SUITE_END();
}
