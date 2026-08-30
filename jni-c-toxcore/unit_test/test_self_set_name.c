#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1name

#ifndef TOX_MAX_NAME_LENGTH
#define TOX_MAX_NAME_LENGTH 128
#endif

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jobject name
);

static void fill_chars(char* buf, size_t len, char c) {
    for (size_t i = 0; i < len; i++) {
        buf[i] = c;
    }
    buf[len] = '\0';
}

static void t_normal_name_ok(void) {
    TEST_BEGIN("t_normal_name_ok");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_set_name_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)"Alice"
    );

    NOTE("normal name accepted");

    TEST_EQUAL_LONG(1, ret);
    TEST_ASSERT(tox_mock_self_set_name_called);
    TEST_EQUAL_SIZE(5, tox_mock_self_set_name_length);

    TEST_END();
}

static void tox_returns_false(void) {
    TEST_BEGIN("tox_returns_false");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_set_name_return = false;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)"Alice"
    );

    NOTE("tox_self_set_name() returned false");

    TEST_EQUAL_LONG(0, ret);
    TEST_ASSERT(tox_mock_self_set_name_called);

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
        (jobject)"Alice"
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(-1, ret);
    TEST_ASSERT_FALSE(tox_mock_self_set_name_called);

    TEST_END();
}

static void t_long_name_must_be_bounded(void) {
    TEST_BEGIN("t_long_name_must_be_bounded");

    jni_mock_reset();
    tox_mock_reset();

    char long_name[201];
    fill_chars(long_name, sizeof(long_name) - 1, 'A');

    tox_mock_self_set_name_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)long_name
    );

    (void)ret;

    NOTE("attempted 200-char name");

    TEST_ASSERT(tox_mock_self_set_name_length <= TOX_MAX_NAME_LENGTH);

    TEST_END();
}

void run_self_set_name_tests(void) {
    SUITE_BEGIN("self set name: basic");
    t_normal_name_ok();
    tox_returns_false();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("self set name: security");
    t_long_name_must_be_bounded();
    SUITE_END();
}
