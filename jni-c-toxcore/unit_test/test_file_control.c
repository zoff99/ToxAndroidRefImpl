#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1control

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jlong file_number,
    jint control
);

static void t_normal_control_call(void) {
    TEST_BEGIN("t_normal_control_call");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_file_control_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        0
    );

    (void)ret;

    NOTE("file control call survived");

    TEST_ASSERT(tox_mock_file_control_called);
    TEST_EQUAL_LONG(1, tox_mock_last_file_control_friend_number);
    TEST_EQUAL_LONG(2, tox_mock_last_file_control_file_number);
    TEST_EQUAL_LONG(0, tox_mock_last_file_control_control);

    TEST_END();
}

static void t_null_tox_global_must_not_call_toxcore(void) {
    TEST_BEGIN("t_null_tox_global_must_not_call_toxcore");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        1,
        2,
        0
    );

    (void)ret;

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_file_control_called);

    TEST_END();
}

void run_file_control_tests(void) {
    SUITE_BEGIN("file control: basic");
    t_normal_control_call();
    SUITE_END();

    SUITE_BEGIN("file control: security");
    t_null_tox_global_must_not_call_toxcore();
    SUITE_END();
}
