#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1message

#ifndef TOX_MAX_FRIEND_MESSAGE_LENGTH
#define TOX_MAX_FRIEND_MESSAGE_LENGTH 1368
#endif

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jint type,
    jobject message
);

static void fill_chars(char* buf, size_t len, char c) {
    for (size_t i = 0; i < len; i++) {
        buf[i] = c;
    }
    buf[len] = '\0';
}

static void t_normal_message_ok(void) {
    TEST_BEGIN("t_normal_message_ok");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_send_message_return = 777;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        5,
        0,
        (jobject)"hello"
    );

    NOTE("normal message accepted");

    TEST_EQUAL_LONG(777, ret);
    TEST_ASSERT(tox_mock_friend_send_message_called);
    TEST_EQUAL_LONG(5, tox_mock_last_friend_send_message_friend_number);
    TEST_EQUAL_LONG(0, tox_mock_last_friend_send_message_type);
    TEST_EQUAL_SIZE(5, tox_mock_last_friend_send_message_length);

    TEST_END();
}

static void t_too_long_error_maps_to_minus_5(void) {
    TEST_BEGIN("t_too_long_error_maps_to_minus_5");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_send_message_return = 0;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        5,
        0,
        (jobject)"hello"
    );

    NOTE("toxcore reported TOO_LONG");

    TEST_EQUAL_LONG(-5, ret);
    TEST_ASSERT(tox_mock_friend_send_message_called);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        5,
        0,
        (jobject)"hello"
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(-99, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_send_message_called);

    TEST_END();
}

static void t_long_message_must_be_bounded(void) {
    TEST_BEGIN("t_long_message_must_be_bounded");

    jni_mock_reset();
    tox_mock_reset();

    char long_message[1501];
    fill_chars(long_message, sizeof(long_message) - 1, 'M');

    tox_mock_friend_send_message_return = 777;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        5,
        0,
        (jobject)long_message
    );

    (void)ret;

    NOTE("attempted 1500-char message");

    TEST_ASSERT(tox_mock_last_friend_send_message_length <= TOX_MAX_FRIEND_MESSAGE_LENGTH);

    TEST_END();
}

void run_friend_send_message_tests(void) {
    SUITE_BEGIN("friend send message: basic");
    t_normal_message_ok();
    t_too_long_error_maps_to_minus_5();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("friend send message: security");
    t_long_message_must_be_bounded();
    SUITE_END();
}
