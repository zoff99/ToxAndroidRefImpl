#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1status

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number
);

static void t_valid_friend(void) {
    TEST_BEGIN("t_valid_friend");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_get_connection_status_return = (int)TOX_CONNECTION_UDP;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        5
    );

    NOTE("valid friend number");

    TEST_ASSERT(tox_mock_friend_get_connection_status_called);
    TEST_EQUAL_LONG(5, tox_mock_last_friend_get_connection_status_friend_number);
    TEST_EQUAL_LONG((long)TOX_CONNECTION_UDP, (long)ret);

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
        5
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT_FALSE(tox_mock_friend_get_connection_status_called);
    TEST_EQUAL_LONG((long)TOX_CONNECTION_NONE, (long)ret);

    TEST_END();
}

void run_friend_get_connection_status_tests(void) {
    SUITE_BEGIN("friend get connection status: basic");
    t_valid_friend();
    t_null_tox_global();
    SUITE_END();
}
