#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1get_1all_1tcp_1relays

JNIEXPORT jstring JNICALL SUT(
    JNIEnv* env,
    jobject thiz
);

static void t_normal(void) {
    TEST_BEGIN("t_normal");

    jni_mock_reset();
    tox_mock_reset();

    strcpy(tox_mock_get_all_tcp_relays_data, "192.168.1.1:33445 10.0.0.1:33446");

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("normal TCP relays returned");

    TEST_ASSERT(ret != NULL);
    TEST_ASSERT(tox_mock_get_all_tcp_relays_called);
    TEST_ASSERT(tox_mock_safe_string_called);

    TEST_END();
}

static void t_null_tox_global(void) {
    TEST_BEGIN("t_null_tox_global");

    jni_mock_reset();
    tox_mock_reset();

    tox_global = NULL;

    jstring ret = SUT(
        jni_mock_env(),
        NULL
    );

    NOTE("tox_global is NULL");

    TEST_ASSERT(ret == NULL);
    TEST_ASSERT_FALSE(tox_mock_get_all_tcp_relays_called);

    TEST_END();
}

void run_get_all_tcp_relays_tests(void) {
    SUITE_BEGIN("get all tcp relays: basic");
    t_normal();
    t_null_tox_global();
    SUITE_END();
}
