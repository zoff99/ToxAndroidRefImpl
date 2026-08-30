#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status

JNIEXPORT void JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jint status
);

static void t_valid_status(void) {
    TEST_BEGIN("t_valid_status");

    jni_mock_reset();
    tox_mock_reset();

    SUT(
        jni_mock_env(),
        NULL,
        (jint)TOX_USER_STATUS_AWAY
    );

    NOTE("set status to AWAY");

    TEST_ASSERT(tox_mock_self_set_status_called);
    TEST_EQUAL_LONG((long)TOX_USER_STATUS_AWAY, (long)tox_mock_last_self_set_status);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    SUT(
        jni_mock_env(),
        NULL,
        (jint)TOX_USER_STATUS_AWAY
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_self_set_status_called);

    TEST_END();
}

void run_self_set_status_tests(void) {
    SUITE_BEGIN("self set status: basic");
    t_valid_status();
    t_null_tox_global();
    SUITE_END();
}
