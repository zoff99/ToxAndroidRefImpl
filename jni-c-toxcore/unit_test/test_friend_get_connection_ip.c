#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1ip

JNIEXPORT jstring JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number
);

static void t_normal_ip_returned(void) {
    TEST_BEGIN("t_normal_ip_returned");

    jni_mock_reset();
    tox_mock_reset();

    strcpy(tox_mock_ip_to_write, "192.168.1.20");

    jstring ret = SUT(
        jni_mock_env(),
        NULL,
        3
    );

    NOTE("connection IP returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_friend_get_connection_ip_called);
    TEST_EQUAL_LONG(3, tox_mock_last_friend_get_connection_ip_friend_number);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_ASSERT(tox_mock_safe_string_last_length > 0);

    TEST_END();
}

static void t_null_tox_global_returns_null(void) {
    TEST_BEGIN("t_null_tox_global_returns_null");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jstring ret = SUT(
        jni_mock_env(),
        NULL,
        3
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(tox_mock_friend_get_connection_ip_called);
    TEST_ASSERT_FALSE(tox_mock_safe_string_called);

    TEST_END();
}

void run_friend_get_connection_ip_tests(void) {
    SUITE_BEGIN("friend connection ip: basic");
    t_normal_ip_returned();
    t_null_tox_global_returns_null();
    SUITE_END();
}
