#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1name

#ifndef TOX_MAX_NAME_LENGTH
#define TOX_MAX_NAME_LENGTH 128
#endif

JNIEXPORT jstring JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jlong friend_number
);

static void t_valid_friend(void) {
    TEST_BEGIN("t_valid_friend");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_get_name_size_return = 5;
    strcpy(tox_mock_friend_get_name_data, "Alice");
    tox_mock_friend_get_name_return = true;

    jstring ret = SUT(
        jni_mock_env(),
        NULL,
        3
    );

    NOTE("valid friend name");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_friend_get_name_called);
    TEST_EQUAL_LONG(3, tox_mock_last_friend_get_name_friend_number);
    TEST_ASSERT(tox_mock_safe_string_called);
    TEST_EQUAL_SIZE(5, tox_mock_safe_string_last_length);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

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
    TEST_ASSERT_FALSE(tox_mock_friend_get_name_called);

    TEST_END();
}

static void t_excessive_name_size(void) {
    TEST_BEGIN("t_excessive_name_size");

    jni_mock_reset();
    tox_mock_reset();

    tox_mock_friend_get_name_size_return = 1000;
    tox_mock_friend_get_name_data[0] = '\0';
    tox_mock_friend_get_name_return = true;

    jstring ret = SUT(
        jni_mock_env(),
        NULL,
        3
    );

    (void)ret;

    NOTE("mocked friend name size is 1000");

    TEST_ASSERT(tox_mock_safe_string_last_length <= TOX_MAX_NAME_LENGTH);

    TEST_END();
}

void run_friend_get_name_tests(void) {
    SUITE_BEGIN("friend get name: basic");
    t_valid_friend();
    t_null_tox_global();
    SUITE_END();

    SUITE_BEGIN("friend get name: security");
    t_excessive_name_size();
    SUITE_END();
}
