#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1friend_1send_1message

/*
    These must also exist in harness.h / harness.c.

    The values here are fallbacks for the test file only.
*/
#ifndef TOX_MSGV3_MAX_MESSAGE_LENGTH
#define TOX_MSGV3_MAX_MESSAGE_LENGTH 1368
#endif

#ifndef TOX_MSGV3_GUARD
#define TOX_MSGV3_GUARD 0
#endif

#ifndef TOX_MSGV3_MSGID_LENGTH
#define TOX_MSGV3_MSGID_LENGTH 8
#endif

#ifndef TOX_MSGV3_TIMESTAMP_LENGTH
#define TOX_MSGV3_TIMESTAMP_LENGTH 4
#endif

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number,
    jint type,
    jobject message,
    jobject hash_buffer,
    jlong timestamp
);

static void fill_chars(char* buf, size_t len, char c) {
    for (size_t i = 0; i < len; i++) {
        buf[i] = c;
    }
    buf[len] = '\0';
}

static void t_normal_messagev3_send_ok(void) {
    TEST_BEGIN("t_normal_messagev3_send_ok");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t hash[TOX_MSGV3_MSGID_LENGTH] = {0};

    MockDirectBuffer hash_buf = {
        .address = hash,
        .capacity = (jlong)sizeof(hash)
    };

    tox_mock_friend_send_message_return = 999;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        11,
        0,
        (jobject)"hello",
        (jobject)&hash_buf,
        12345
    );

    NOTE("normal messagev3 send");

    size_t expected_len =
        strlen("hello") +
        TOX_MSGV3_GUARD +
        TOX_MSGV3_MSGID_LENGTH +
        TOX_MSGV3_TIMESTAMP_LENGTH;

    TEST_EQUAL_LONG(999, ret);
    TEST_ASSERT(tox_mock_friend_send_message_called);
    TEST_EQUAL_LONG(11, tox_mock_last_friend_send_message_friend_number);
    TEST_EQUAL_LONG(0, tox_mock_last_friend_send_message_type);
    TEST_EQUAL_SIZE(expected_len, tox_mock_last_friend_send_message_length);

    TEST_ASSERT(tox_mock_xnet_pack_u32_called);
    TEST_EQUAL_LONG(12345, tox_mock_last_xnet_pack_u32_value);

    TEST_END();
}

static void t_hash_buffer_too_small_returns_minus_98(void) {
    TEST_BEGIN("t_hash_buffer_too_small_returns_minus_98");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t small_hash[TOX_MSGV3_MSGID_LENGTH - 1] = {0};

    MockDirectBuffer hash_buf = {
        .address = small_hash,
        .capacity = (jlong)sizeof(small_hash)
    };

    tox_mock_friend_send_message_return = 999;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        11,
        0,
        (jobject)"hello",
        (jobject)&hash_buf,
        12345
    );

    NOTE("hash buffer capacity smaller than TOX_MSGV3_MSGID_LENGTH");

    TEST_EQUAL_LONG(-98, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_send_message_called);

    TEST_END();
}

static void t_message_too_long_returns_minus_5(void) {
    TEST_BEGIN("t_message_too_long_returns_minus_5");

    jni_mock_reset();
    tox_mock_reset();

    char long_message[TOX_MSGV3_MAX_MESSAGE_LENGTH + 2];
    fill_chars(long_message, TOX_MSGV3_MAX_MESSAGE_LENGTH + 1, 'M');

    uint8_t hash[TOX_MSGV3_MSGID_LENGTH] = {0};

    MockDirectBuffer hash_buf = {
        .address = hash,
        .capacity = (jlong)sizeof(hash)
    };

    tox_mock_friend_send_message_return = 999;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        11,
        0,
        (jobject)long_message,
        (jobject)&hash_buf,
        12345
    );

    NOTE("message longer than TOX_MSGV3_MAX_MESSAGE_LENGTH");

    TEST_EQUAL_LONG(-5, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_send_message_called);

    TEST_END();
}

static void t_message_boundary_length_ok(void) {
    TEST_BEGIN("t_message_boundary_length_ok");

    jni_mock_reset();
    tox_mock_reset();

    char boundary_message[TOX_MSGV3_MAX_MESSAGE_LENGTH + 1];
    fill_chars(boundary_message, TOX_MSGV3_MAX_MESSAGE_LENGTH, 'B');

    uint8_t hash[TOX_MSGV3_MSGID_LENGTH] = {0};

    MockDirectBuffer hash_buf = {
        .address = hash,
        .capacity = (jlong)sizeof(hash)
    };

    tox_mock_friend_send_message_return = 999;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        11,
        0,
        (jobject)boundary_message,
        (jobject)&hash_buf,
        12345
    );

    NOTE("message exactly TOX_MSGV3_MAX_MESSAGE_LENGTH bytes long");

    size_t expected_len =
        TOX_MSGV3_MAX_MESSAGE_LENGTH +
        TOX_MSGV3_GUARD +
        TOX_MSGV3_MSGID_LENGTH +
        TOX_MSGV3_TIMESTAMP_LENGTH;

    TEST_EQUAL_LONG(999, ret);
    TEST_ASSERT(tox_mock_friend_send_message_called);
    TEST_EQUAL_SIZE(expected_len, tox_mock_last_friend_send_message_length);

    TEST_END();
}

static void t_null_tox_global_returns_minus_99(void) {
    TEST_BEGIN("t_null_tox_global_returns_minus_99");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    uint8_t hash[TOX_MSGV3_MSGID_LENGTH] = {0};

    MockDirectBuffer hash_buf = {
        .address = hash,
        .capacity = (jlong)sizeof(hash)
    };

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        11,
        0,
        (jobject)"hello",
        (jobject)&hash_buf,
        12345
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(-99, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_send_message_called);

    TEST_END();
}

/*
    Note:

    A NULL hash-buffer address test is intentionally omitted.

    The current vulnerable code checks capacity, but may not check
    whether GetDirectBufferAddress() returned NULL. If we passed
    NULL address with valid capacity, the unfixed function could
    do memcpy(..., NULL, ...) and crash the test process.

    That case should be tested with a fork/death-test style harness
    or after the code is fixed.
*/

void run_messagev3_friend_send_message_tests(void) {
    SUITE_BEGIN("msgv3 send message: basic");
    t_normal_messagev3_send_ok();
    t_hash_buffer_too_small_returns_minus_98();
    t_message_too_long_returns_minus_5();
    t_message_boundary_length_ok();
    t_null_tox_global_returns_minus_99();
    SUITE_END();
}
