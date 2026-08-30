#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name_1size

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz
);

static void t_normal(void) {
    TEST_BEGIN("t_normal");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_name_size_return = 42;

    jlong ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("normal name size");

    TEST_EQUAL_LONG(42, ret);

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

void run_self_get_name_size_tests(void) {
    SUITE_BEGIN("self get name size: basic");
    t_normal();
    t_null_tox_global();
    SUITE_END();
}
