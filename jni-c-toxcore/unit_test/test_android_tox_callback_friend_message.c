#include "harness.h"
#include <stdarg.h>

// Declare the function to test
void android_tox_callback_friend_message_cb(uint32_t friend_number, Tox_Message_Type type, const uint8_t *message, size_t length);

// Declare mock xnet_unpack_u32
size_t xnet_unpack_u32(const uint8_t* src, uint32_t* value);

static void t_normal_message(void) {
    TEST_BEGIN("t_normal_message");
    jni_mock_reset();
    tox_mock_reset();

    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;

    uint8_t msg[] = "hello";
    android_tox_callback_friend_message_cb(1, TOX_MESSAGE_TYPE_NORMAL, msg, 5);

    TEST_ASSERT(tox_mock_callback_friend_message_called);
    TEST_ASSERT(tox_mock_callback_friend_message_friend_number == 1);
    TEST_ASSERT(tox_mock_callback_friend_message_type == TOX_MESSAGE_TYPE_NORMAL);
    TEST_ASSERT(tox_mock_callback_friend_message_length == 5);
    TEST_ASSERT(tox_mock_callback_friend_message_hash_jbuffer == NULL);
    TEST_ASSERT(tox_mock_callback_friend_message_timestamp == 0);
    TEST_END();
}

static void t_msgv3_message(void) {
    TEST_BEGIN("t_msgv3_message");
    jni_mock_reset();
    tox_mock_reset();

    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;
    mock_NewByteArray_return = (jbyteArray)0x3333;

    // Construct a msgv3 message
    // length = 46
    // pos = 46 - 12 = 34
    // message[34] = 0, message[35] = 0 (guard)
    // message[36..67] = hash (32 bytes)
    // message[68..71] = timestamp (4 bytes)
    uint8_t msg[72];
    memset(msg, 'A', 34); // payload
    msg[34] = 0; msg[35] = 0; // guard
    memset(msg + 36, 'H', 32); // hash
    msg[68] = 0x12; msg[69] = 0x34; msg[70] = 0x56; msg[71] = 0x78; // timestamp 0x78563412

    android_tox_callback_friend_message_cb(2, TOX_MESSAGE_TYPE_ACTION, msg, 72);

    TEST_ASSERT(tox_mock_callback_friend_message_called);
    TEST_ASSERT(tox_mock_callback_friend_message_friend_number == 2);
    TEST_ASSERT(tox_mock_callback_friend_message_type == TOX_MESSAGE_TYPE_ACTION);
    TEST_ASSERT(tox_mock_callback_friend_message_length == 34); // new_length
    TEST_ASSERT(tox_mock_callback_friend_message_hash_jbuffer == (jbyteArray)0x3333);
    TEST_ASSERT(tox_mock_callback_friend_message_timestamp == 0x78563412);
    TEST_ASSERT(mock_NewByteArray_called);
    TEST_ASSERT(mock_SetByteArrayRegion_called);
    TEST_ASSERT(mock_DeleteLocalRef_called); // for js1 and msgV3_hash_jbuffer
    TEST_END();
}

static void t_msgv3_message_invalid_guard(void) {
    TEST_BEGIN("t_msgv3_message_invalid_guard");
    jni_mock_reset();
    tox_mock_reset();

    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;

    uint8_t msg[20];
    memset(msg, 'A', 20);
    msg[8] = 1; msg[9] = 0; // invalid guard

    android_tox_callback_friend_message_cb(3, TOX_MESSAGE_TYPE_NORMAL, msg, 20);

    TEST_ASSERT(tox_mock_callback_friend_message_called);
    TEST_ASSERT(tox_mock_callback_friend_message_length == 20); // should not be truncated
    TEST_ASSERT(tox_mock_callback_friend_message_hash_jbuffer == NULL);
    TEST_END();
}

static void t_null_jni_env(void) {
    TEST_BEGIN("t_null_jni_env");
    jni_mock_reset();
    tox_mock_reset();

    mock_jni_env_ptr = NULL;
    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;

    uint8_t msg[20];
    memset(msg, 'A', 8);
    msg[8] = 0; msg[9] = 0; // valid guard to trigger jnienv2 usage

    // After fix: should return early without crashing
    android_tox_callback_friend_message_cb(4, TOX_MESSAGE_TYPE_NORMAL, msg, 20);

    TEST_ASSERT_FALSE(tox_mock_callback_friend_message_called);
    TEST_END();
}

static void t_null_message(void) {
    TEST_BEGIN("t_null_message");
    jni_mock_reset();
    tox_mock_reset();

    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;

    android_tox_callback_friend_message_cb(5, TOX_MESSAGE_TYPE_NORMAL, NULL, 0);

    TEST_ASSERT(tox_mock_callback_friend_message_called);
    TEST_ASSERT(tox_mock_callback_friend_message_length == 0);
    TEST_END();
}

static void t_msgv3_newbyte_array_oom(void) {
    TEST_BEGIN("t_msgv3_newbyte_array_oom");
    jni_mock_reset();
    tox_mock_reset();

    MainActivity = (jclass)0x1111;
    android_tox_callback_friend_message_cb_method = (jmethodID)0x2222;
    mock_NewByteArray_return = NULL; // Simulate OOM

    uint8_t msg[72];
    memset(msg, 'A', 34);
    msg[34] = 0; msg[35] = 0;
    memset(msg + 36, 'H', 32);
    msg[68] = 0x12; msg[69] = 0x34; msg[70] = 0x56; msg[71] = 0x78;

    android_tox_callback_friend_message_cb(6, TOX_MESSAGE_TYPE_NORMAL, msg, 72);

    TEST_ASSERT(tox_mock_callback_friend_message_called);
    // Should still call the callback, but with NULL hash buffer
    TEST_ASSERT(tox_mock_callback_friend_message_hash_jbuffer == NULL);
    TEST_END();
}

void run_android_tox_callback_friend_message_tests(void) {
    SUITE_BEGIN("android_tox_callback_friend_message: basic");
    t_normal_message();
    t_msgv3_message();
    t_msgv3_message_invalid_guard();
    t_null_message();
    SUITE_END();

    SUITE_BEGIN("android_tox_callback_friend_message: security");
    t_null_jni_env();
    t_msgv3_newbyte_array_oom();
    SUITE_END();
}
