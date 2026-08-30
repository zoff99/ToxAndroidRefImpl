#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1typing

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jint typing
);

static void t_valid(void) {
    TEST_BEGIN("t_valid");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_set_typing_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        7,
        1
    );

    NOTE("set typing to true for friend 7");

    TEST_ASSERT(tox_mock_self_set_typing_called);
    TEST_EQUAL_LONG(7, tox_mock_last_self_set_typing_friend_number);
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
        7,
        1
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_self_set_typing_called);
    TEST_EQUAL_LONG(-1, ret);

    TEST_END();
}

void run_self_set_typing_tests(void) {
    SUITE_BEGIN("self set typing: basic");
    t_valid();
    t_null_tox_global();
    SUITE_END();
}
