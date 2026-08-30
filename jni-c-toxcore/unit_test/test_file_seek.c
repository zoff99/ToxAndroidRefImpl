#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1seek

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jlong file_number,
    jlong position
);

static void t_valid_seek(void) {
    TEST_BEGIN("t_valid_seek");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_file_seek_return = true;
    tox_mock_file_seek_error = TOX_ERR_FILE_SEEK_OK;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        512
    );

    NOTE("valid file seek");

    TEST_ASSERT(tox_mock_file_seek_called);
    TEST_EQUAL_LONG(1, tox_mock_last_file_seek_friend_number);
    TEST_EQUAL_LONG(2, tox_mock_last_file_seek_file_number);
    TEST_EQUAL_LONG(512, tox_mock_last_file_seek_position);
    TEST_EQUAL_LONG(1, ret);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        512
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_file_seek_called);
    TEST_ASSERT(ret <= 0);

    TEST_END();
}

void run_file_seek_tests(void) {
    SUITE_BEGIN("file seek: basic");
    t_valid_seek();
    t_null_tox_global();
    SUITE_END();
}
