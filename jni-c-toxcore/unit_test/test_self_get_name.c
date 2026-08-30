#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name

#ifndef TOX_MAX_NAME_LENGTH
#define TOX_MAX_NAME_LENGTH 128
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
    TEST_ASSERT_FALSE(tox_mock_self_get_name_called);
    TEST_ASSERT_FALSE(tox_mock_safe_string_called);

    TEST_END();
}

static void t_normal_name_returned(void) {
    TEST_BEGIN("t_normal_name_returned");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_name_size_return = 5;
    strcpy(tox_mock_self_get_name_data, "Alice");

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("normal name returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_self_get_name_called);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_EQUAL_SIZE(5, tox_mock_safe_string_last_length);

    TEST_END();
}

static void t_empty_name_returned(void) {
    TEST_BEGIN("t_empty_name_returned");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_name_size_return = 0;
    tox_mock_self_get_name_data[0] = '\0';

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("empty name returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_self_get_name_called);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_EQUAL_SIZE(0, tox_mock_safe_string_last_length);

    TEST_END();
}

static void t_excessive_name_size_must_be_bounded(void) {
    TEST_BEGIN("t_excessive_name_size_must_be_bounded");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_self_get_name_size_return = 1000;
    tox_mock_self_get_name_data[0] = '\0';

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    (void)ret;

    NOTE("mocked name size is 1000");

    TEST_ASSERT(tox_mock_safe_string_last_length <= TOX_MAX_NAME_LENGTH);

    TEST_END();
}

void run_self_get_name_tests(void) {
    SUITE_BEGIN("self get name: basic");
    t_null_tox_global_returns_null();
    t_normal_name_returned();
    t_empty_name_returned();
    SUITE_END();

    SUITE_BEGIN("self get name: security");
    t_excessive_name_size_must_be_bounded();
    SUITE_END();
}
