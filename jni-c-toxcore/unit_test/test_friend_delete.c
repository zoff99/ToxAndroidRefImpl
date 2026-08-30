#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1delete

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number
);

static void t_delete_ok(void) {
    TEST_BEGIN("t_delete_ok");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_delete_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        7
    );

    NOTE("friend delete succeeded");

    TEST_EQUAL_LONG(1, ret);
    TEST_ASSERT(tox_mock_friend_delete_called);
    TEST_EQUAL_LONG(7, tox_mock_last_friend_delete_friend_number);

    TEST_END();
}

static void t_delete_fails(void) {
    TEST_BEGIN("t_delete_fails");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_delete_return = false;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        7
    );

    NOTE("tox_friend_delete() returned false");

    TEST_EQUAL_LONG(0, ret);
    TEST_ASSERT(tox_mock_friend_delete_called);

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
        7
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(0, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_delete_called);

    TEST_END();
}

void run_friend_delete_tests(void) {
    SUITE_BEGIN("friend delete: basic");
    t_delete_ok();
    t_delete_fails();
    t_null_tox_global();
    SUITE_END();
}
