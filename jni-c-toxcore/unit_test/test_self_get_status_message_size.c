#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message_1size

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz
);

static void t_normal(void) {
    TEST_BEGIN("t_normal");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_status_message_size_return = 99;

    jlong ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("normal status message size");

    TEST_EQUAL_LONG(99, ret);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jlong ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(0, ret);

    TEST_END();
}

void run_self_get_status_message_size_tests(void) {
    SUITE_BEGIN("self get status message size: basic");
    t_normal();
    t_null_tox_global();
    SUITE_END();
}
