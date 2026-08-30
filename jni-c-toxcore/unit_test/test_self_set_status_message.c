#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message

#ifndef TOX_MAX_STATUS_MESSAGE_LENGTH
#define TOX_MAX_STATUS_MESSAGE_LENGTH 1007
#endif

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jobject status_message
);

static void fill_chars(char* buf, size_t len, char c) {
    for (size_t i = 0; i < len; i++) {
        buf[i] = c;
    }
    buf[len] = '\0';
}

static void t_normal_status_ok(void) {
    TEST_BEGIN("t_normal_status_ok");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_set_status_message_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)"Available"
    );

    NOTE("normal status message accepted");

    TEST_EQUAL_LONG(1, ret);
    TEST_ASSERT(tox_mock_self_set_status_message_called);
    TEST_EQUAL_SIZE(9, tox_mock_self_set_status_message_length);

    TEST_END();
}

static void tox_returns_false(void) {
    TEST_BEGIN("tox_returns_false");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_set_status_message_return = false;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)"Available"
    );

    NOTE("tox_self_set_status_message() returned false");

    TEST_EQUAL_LONG(0, ret);
    TEST_ASSERT(tox_mock_self_set_status_message_called);

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
        (jobject)"Available"
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(-1, ret);
    TEST_ASSERT_FALSE(tox_mock_self_set_status_message_called);

    TEST_END();
}

static void t_long_status_must_be_bounded(void) {
    TEST_BEGIN("t_long_status_must_be_bounded");

    jni_mock_reset();
    tox_mock_reset();

    char long_status[1201];
    fill_chars(long_status, sizeof(long_status) - 1, 'B');

    tox_mock_self_set_status_message_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)long_status
    );

    (void)ret;

    NOTE("attempted 1200-char status message");

    TEST_ASSERT(tox_mock_self_set_status_message_length <= TOX_MAX_STATUS_MESSAGE_LENGTH);

    TEST_END();
}

void run_self_set_status_message_tests(void) {
    SUITE_BEGIN("self set status: basic");
    t_normal_status_ok();
    tox_returns_false();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("self set status: security");
    t_long_status_must_be_bounded();
    SUITE_END();
}
