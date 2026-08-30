#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send_1chunk

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jlong file_number,
    jlong position,
    jbyteArray data,
    jint data_length
);

static void t_valid_chunk_send(void) {
    TEST_BEGIN("t_valid_chunk_send");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t bytes[64] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    tox_mock_file_send_chunk_return = true;
    tox_mock_file_send_chunk_error = TOX_ERR_FILE_SEND_CHUNK_OK;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        0,
        (jbyteArray)&arr,
        64
    );

    NOTE("valid chunk send");

    TEST_ASSERT(tox_mock_file_send_chunk_called);
    TEST_EQUAL_LONG(1, tox_mock_last_file_send_chunk_friend_number);
    TEST_EQUAL_LONG(2, tox_mock_last_file_send_chunk_file_number);
    TEST_EQUAL_SIZE(64, tox_mock_last_file_send_chunk_length);
    TEST_EQUAL_LONG(1, ret);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    uint8_t bytes[64] = {0};
    MockByteArray arr = {
        .data = bytes,
        .length = (jsize)sizeof(bytes)
    };

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        0,
        (jbyteArray)&arr,
        64
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_file_send_chunk_called);
    TEST_ASSERT(ret <= 0);

    TEST_END();
}

/*
    SECURITY TEST

    This test FAILS on the current vulnerable code.

    The Java array has only 10 bytes, but Java passes data_length = 1000.

    Secure invariant:

        The JNI layer must never ask toxcore to send more bytes
        than are present in the Java byte array.
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

    tox_mock_file_send_chunk_return = true;
    tox_mock_file_send_chunk_error = TOX_ERR_FILE_SEND_CHUNK_OK;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        0,
        (jbyteArray)&arr,
        1000
    );

    (void)ret;

    NOTE("attempted overlength data_length: 1000 for a 10-byte Java array");

    TEST_ASSERT(tox_mock_last_file_send_chunk_length <= (size_t)arr.length);

    TEST_END();
}

void run_file_send_chunk_tests(void) {
    SUITE_BEGIN("file send chunk: basic");
    t_valid_chunk_send();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("file send chunk: security");
    t_data_length_must_be_validated();
    SUITE_END();
}
