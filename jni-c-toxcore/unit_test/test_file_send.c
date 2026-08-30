#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jint file_type,
    jlong file_size,
    jbyteArray file_id,
    jobject filename
);

static void t_valid_file_send(void) {
    TEST_BEGIN("t_valid_file_send");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t file_id[TOX_FILE_ID_LENGTH] = {0};
    MockByteArray arr = {
        .data = file_id,
        .length = (jsize)sizeof(file_id)
    };

    tox_mock_file_send_return = 42;
    tox_mock_file_send_error = TOX_ERR_FILE_SEND_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        0,
        1024,
        (jbyteArray)&arr,
        (jobject)"test.txt"
    );

    NOTE("valid file send");

    TEST_ASSERT(tox_mock_file_send_called);
    TEST_EQUAL_LONG(1, tox_mock_last_file_send_friend_number);
    TEST_EQUAL_LONG(42, ret);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    uint8_t file_id[TOX_FILE_ID_LENGTH] = {0};
    MockByteArray arr = {
        .data = file_id,
        .length = (jsize)sizeof(file_id)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        0,
        1024,
        (jbyteArray)&arr,
        (jobject)"test.txt"
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_file_send_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

void run_file_send_tests(void) {
    SUITE_BEGIN("file send: basic");
    t_valid_file_send();
    t_null_tox_global();
    SUITE_END();
}
