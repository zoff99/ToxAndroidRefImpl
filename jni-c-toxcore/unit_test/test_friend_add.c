#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jobject toxid_str,
    jobject message
);

static void fill_hex(char* buf, size_t hex_chars) {
    for (size_t i = 0; i < hex_chars; i++) {
        buf[i] = 'A';
    }
    buf[hex_chars] = '\0';
}

static void t_valid_add_ok(void) {
    TEST_BEGIN("t_valid_add_ok");

    jni_mock_reset();
    tox_mock_reset();

    char toxid[TOX_ADDRESS_SIZE * 2 + 1];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2);

    tox_mock_friend_add_return = 7;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("valid toxid hex and normal friend request message");

    TEST_EQUAL_LONG(7, ret);
    TEST_ASSERT(tox_mock_hex_to_bin_called);
    TEST_ASSERT(tox_mock_friend_add_called);
    TEST_EQUAL_SIZE(5, tox_mock_last_friend_add_message_length);

    TEST_END();
}

static void t_friend_add_already_sent(void) {
    TEST_BEGIN("t_friend_add_already_sent");

    jni_mock_reset();
    tox_mock_reset();

    char toxid[TOX_ADDRESS_SIZE * 2 + 1];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2);

    tox_mock_friend_add_return = 0xFFFFFFFFu;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_ALREADY_SENT;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("toxcore reported TOX_ERR_FRIEND_ADD_ALREADY_SENT");

    TEST_EQUAL_LONG(-1, ret);
    TEST_ASSERT(tox_mock_friend_add_called);

    TEST_END();
}

static void t_friend_add_other_error(void) {
    TEST_BEGIN("t_friend_add_other_error");

    jni_mock_reset();
    tox_mock_reset();

    char toxid[TOX_ADDRESS_SIZE * 2 + 1];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2);

    tox_mock_friend_add_return = 0xFFFFFFFFu;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_MALLOC;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("toxcore reported another friend-add error");

    TEST_EQUAL_LONG(-2, ret);
    TEST_ASSERT(tox_mock_friend_add_called);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    char toxid[TOX_ADDRESS_SIZE * 2 + 1];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2);

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("tox_global is NULL");

    TEST_EQUAL_LONG(-3, ret);
    TEST_ASSERT_FALSE(tox_mock_friend_add_called);

    TEST_END();
}

/*
    SECURITY TEST

    This test FAILS on the current vulnerable code.

    The JNI wrapper currently passes the hex string to toxid_hex_to_bin()
    and then calls tox_friend_add() without ensuring that the hex string
    has the required length:

        TOX_ADDRESS_SIZE * 2 == 76 characters

    Secure expectation:

        An invalid toxid must not result in tox_friend_add() being called.
*/
static void t_invalid_hex_too_long_must_not_add(void) {
    TEST_BEGIN("t_invalid_hex_too_long_must_not_add");

    jni_mock_reset();
    tox_mock_reset();

    char toxid[TOX_ADDRESS_SIZE * 2 + 3];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2 + 2); /* 78 chars instead of 76 */

    tox_mock_friend_add_return = 7;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("attempted toxid hex with 78 chars instead of 76");

    /*
        Secure expectation:

        - invalid toxid must not add a friend
        - JNI function should return an error
    */
    TEST_ASSERT_FALSE(tox_mock_friend_add_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

/*
    SECURITY TEST

    This test FAILS on the current vulnerable code.

    A too-short toxid hex string must not be accepted.
*/
static void t_invalid_hex_too_short_must_not_add(void) {
    TEST_BEGIN("t_invalid_hex_too_short_must_not_add");

    jni_mock_reset();
    tox_mock_reset();

    char toxid[TOX_ADDRESS_SIZE * 2 + 1];
    fill_hex(toxid, TOX_ADDRESS_SIZE * 2 - 2); /* 74 chars instead of 76 */

    tox_mock_friend_add_return = 7;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_OK;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)toxid,
        (jobject)"hello"
    );

    NOTE("attempted toxid hex with 74 chars instead of 76");

    /*
        Secure expectation:

        - invalid toxid must not add a friend
        - JNI function should return an error
    */
    TEST_ASSERT_FALSE(tox_mock_friend_add_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

void run_friend_add_tests(void) {
    SUITE_BEGIN("friend add: basic");
    t_valid_add_ok();
    t_friend_add_already_sent();
    t_friend_add_other_error();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("friend add: security");
    t_invalid_hex_too_long_must_not_add();
    t_invalid_hex_too_short_must_not_add();
    SUITE_END();
}

