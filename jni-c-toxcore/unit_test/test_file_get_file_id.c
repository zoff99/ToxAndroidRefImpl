#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1get_1file_1id

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jlong file_number,
    jbyteArray file_id
);

static void t_valid_get_file_id(void) {
    TEST_BEGIN("t_valid_get_file_id");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t file_id[TOX_FILE_ID_LENGTH] = {0};
    MockByteArray arr = {
        .data = file_id,
        .length = (jsize)sizeof(file_id)
    };

    tox_mock_file_get_file_id_return = true;
    tox_mock_file_get_file_id_error = TOX_ERR_FILE_GET_OK;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        (jbyteArray)&arr
    );

    NOTE("valid get file id");

    TEST_ASSERT(tox_mock_file_get_file_id_called);
    TEST_EQUAL_LONG(1, tox_mock_last_file_get_file_id_friend_number);
    TEST_EQUAL_LONG(2, tox_mock_last_file_get_file_id_file_number);
    TEST_EQUAL_LONG(1, ret);

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

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        (jbyteArray)&arr
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_file_get_file_id_called);
    TEST_ASSERT(ret <= 0);

    TEST_END();
}

void run_file_get_file_id_tests(void) {
    SUITE_BEGIN("file get file id: basic");
    t_valid_get_file_id();
    t_null_tox_global();
    SUITE_END();
}
