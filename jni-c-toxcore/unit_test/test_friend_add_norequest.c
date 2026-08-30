#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add_1norequest

#ifndef TOX_PUBLIC_KEY_SIZE
#define TOX_PUBLIC_KEY_SIZE 32
#endif

JNIEXPORT jlong JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jobject public_key_str
);

static void fill_hex(char* buf, size_t hex_chars) {
    for (size_t i = 0; i < hex_chars; i++) {
        buf[i] = 'A';
    }
    buf[hex_chars] = '\0';
}

static void t_valid_key_ok(void) {
    TEST_BEGIN("t_valid_key_ok");

    jni_mock_reset();
    tox_mock_reset();

    char key[TOX_PUBLIC_KEY_SIZE * 2 + 1];
    fill_hex(key, TOX_PUBLIC_KEY_SIZE * 2);

    tox_mock_friend_add_norequest_return = 3;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)key
    );

    NOTE("valid public key hex accepted");

    TEST_EQUAL_LONG(3, ret);
    TEST_ASSERT(tox_mock_pk_hex_to_bin_called);
    TEST_ASSERT(tox_mock_friend_add_norequest_called);

    TEST_END();
}

static void t_invalid_key_too_long_must_not_add(void) {
    TEST_BEGIN("t_invalid_key_too_long_must_not_add");

    jni_mock_reset();
    tox_mock_reset();

    char key[TOX_PUBLIC_KEY_SIZE * 2 + 3];
    fill_hex(key, TOX_PUBLIC_KEY_SIZE * 2 + 2);

    tox_mock_friend_add_norequest_return = 3;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)key
    );

    NOTE("public key hex too long");

    TEST_ASSERT_FALSE(tox_mock_friend_add_norequest_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

static void t_invalid_key_too_short_must_not_add(void) {
    TEST_BEGIN("t_invalid_key_too_short_must_not_add");

    jni_mock_reset();
    tox_mock_reset();

    char key[TOX_PUBLIC_KEY_SIZE * 2 + 1];
    fill_hex(key, TOX_PUBLIC_KEY_SIZE * 2 - 2);

    tox_mock_friend_add_norequest_return = 3;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)key
    );

    NOTE("public key hex too short");

    TEST_ASSERT_FALSE(tox_mock_friend_add_norequest_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

static void t_null_tox_global_must_not_add(void) {
    TEST_BEGIN("t_null_tox_global_must_not_add");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    char key[TOX_PUBLIC_KEY_SIZE * 2 + 1];
    fill_hex(key, TOX_PUBLIC_KEY_SIZE * 2);

    tox_mock_friend_add_norequest_return = 3;

    jlong ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)key
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_friend_add_norequest_called);
    TEST_ASSERT(ret < 0);

    TEST_END();
}

void run_friend_add_norequest_tests(void) {
    SUITE_BEGIN("friend add norequest: basic");
    t_valid_key_ok();
    SUITE_END();

    SUITE_BEGIN("friend add norequest: security");
    t_invalid_key_too_long_must_not_add();
    t_invalid_key_too_short_must_not_add();
    t_null_tox_global_must_not_add();
    SUITE_END();
}
