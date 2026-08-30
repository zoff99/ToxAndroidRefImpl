#include "harness.h"

#define SUT Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1get_1new_1message_1id

JNIEXPORT jint JNICALL SUT(
    JNIEnv* env,
    jobject thiz,
    jobject hash_buffer
);

static void t_capacity_too_small(void) {
    TEST_BEGIN("t_capacity_too_small");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t small[TOX_MSGV3_MSGID_LENGTH - 1];
    memset(small, 0, sizeof(small));

    MockDirectBuffer buf = {
        .address = small,
        .capacity = (jlong)sizeof(small)
    };

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)&buf
    );

    NOTE("JNI call survived small direct buffer capacity");

    TEST_EQUAL_LONG(-2, ret);
    TEST_ASSERT_FALSE(tox_mock_messagev3_id_called);

    TEST_END();
}

static void t_tox_false_returns_minus_1(void) {
    TEST_BEGIN("t_tox_false_returns_minus_1");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t id[TOX_MSGV3_MSGID_LENGTH];
    memset(id, 0, sizeof(id));

    MockDirectBuffer buf = {
        .address = id,
        .capacity = (jlong)sizeof(id)
    };

    tox_mock_messagev3_id_return = false;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)&buf
    );

    NOTE("JNI call survived tox_messagev3_get_new_message_id() == false");

    TEST_EQUAL_LONG(-1, ret);
    TEST_ASSERT(tox_mock_messagev3_id_called);
    TEST_ASSERT(tox_mock_messagev3_id_last_buffer == id);

    TEST_END();
}

static void t_success_fills_buffer(void) {
    TEST_BEGIN("t_success_fills_buffer");

    jni_mock_reset();
    tox_mock_reset();

    uint8_t id[TOX_MSGV3_MSGID_LENGTH];
    memset(id, 0, sizeof(id));

    MockDirectBuffer buf = {
        .address = id,
        .capacity = (jlong)sizeof(id)
    };

    tox_mock_messagev3_id_return = true;

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)&buf
    );

    NOTE("JNI call succeeded and mock filled message id buffer");

    TEST_EQUAL_LONG(0, ret);
    TEST_ASSERT(tox_mock_messagev3_id_called);
    TEST_ASSERT(tox_mock_messagev3_id_last_buffer == id);

    int filled_ok = 1;
    for (size_t i = 0; i < TOX_MSGV3_MSGID_LENGTH; i++) {
        if (id[i] != 0xAB) {
            filled_ok = 0;
            break;
        }
    }

    TEST_ASSERT(filled_ok);

    TEST_END();
}

/*
    SECURITY TEST

    This test FAILS on the current vulnerable code.

    The direct buffer reports a valid capacity, but its address is NULL.

    Secure invariant:

        The JNI layer must not pass a NULL buffer to
        tox_messagev3_get_new_message_id().

    A correct fix would probably return -2 or another error before
    calling tox_messagev3_get_new_message_id().
*/
static void t_null_buffer_address_must_be_rejected(void) {
    TEST_BEGIN("t_null_buffer_address_must_be_rejected");

    jni_mock_reset();
    tox_mock_reset();

    MockDirectBuffer buf = {
        .address = NULL,
        .capacity = (jlong)TOX_MSGV3_MSGID_LENGTH
    };

    jint ret = SUT(
        jni_mock_env(),
        NULL,
        (jobject)&buf
    );

    NOTE("attempted direct buffer with valid capacity but NULL address");

    /*
        Secure expectation:

        - NULL address must not be handed to toxcore.
        - The function should not return success.
    */
    TEST_ASSERT(ret != 0);
    TEST_ASSERT_FALSE(tox_mock_messagev3_id_called);
    TEST_ASSERT(tox_mock_messagev3_id_last_buffer == NULL);

    TEST_END();
}

void run_messagev3_get_new_message_id_tests(void) {
    SUITE_BEGIN("msgv3 id: capacity");
    t_capacity_too_small();
    SUITE_END();

    SUITE_BEGIN("msgv3 id: tox result");
    t_tox_false_returns_minus_1();
    t_success_fills_buffer();
    SUITE_END();

    SUITE_BEGIN("msgv3 id: security");
    t_null_buffer_address_must_be_rejected();
    SUITE_END();
}
