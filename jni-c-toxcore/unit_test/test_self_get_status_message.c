#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message

#ifndef TOX_MAX_STATUS_MESSAGE_LENGTH
#define TOX_MAX_STATUS_MESSAGE_LENGTH 1007
#endif

JNIEXPORT jstring JNICALL SUT(
    JNIEnv* env,
    jobject thiz
);

static void t_null_tox_global_returns_null(void) {
    TEST_BEGIN("t_null_tox_global_returns_null");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(tox_mock_self_get_status_message_called);
    TEST_ASSERT_FALSE(tox_mock_safe_string_called);

    TEST_END();
}

static void t_normal_status_returned(void) {
    TEST_BEGIN("t_normal_status_returned");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_status_message_size_return = 9;
    strcpy(tox_mock_self_get_status_message_data, "Available");

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("normal status message returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_self_get_status_message_called);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_EQUAL_SIZE(9, tox_mock_safe_string_last_length);

    TEST_END();
}

static void t_empty_status_returned(void) {
    TEST_BEGIN("t_empty_status_returned");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_status_message_size_return = 0;
    tox_mock_self_get_status_message_data[0] = '\0';

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("empty status message returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_self_get_status_message_called);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_EQUAL_SIZE(0, tox_mock_safe_string_last_length);

    TEST_END();
}

static void t_excessive_status_size_must_be_bounded(void) {
    TEST_BEGIN("t_excessive_status_size_must_be_bounded");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_status_message_size_return = 2000;
    tox_mock_self_get_status_message_data[0] = '\0';

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    (void)ret;

    NOTE("mocked status message size is 2000");

    TEST_ASSERT(tox_mock_safe_string_last_length <= TOX_MAX_STATUS_MESSAGE_LENGTH);

    TEST_END();
}

void run_self_get_status_message_tests(void) {
    SUITE_BEGIN("self get status: basic");
    t_null_tox_global_returns_null();
    t_normal_status_returned();
    t_empty_status_returned();
    SUITE_END();

    SUITE_BEGIN("self get status: security");
    t_excessive_status_size_must_be_bounded();
    SUITE_END();
}
