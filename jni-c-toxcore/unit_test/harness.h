#ifndef UNIT_TEST_HARNESS_H
#define UNIT_TEST_HARNESS_H

#include <stdio.h>
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

/*
    Minimal JNI replacement.

    This is not a full JNI implementation. It is just enough to compile
    and test selected JNI functions from jni-c-toxcore.c.
*/

#define JNIEXPORT
#define JNICALL

#define JNI_ABORT 2
#define JNI_FALSE 0
#define JNI_TRUE 1

typedef int8_t jbyte;
typedef int32_t jint;
typedef int64_t jlong;
typedef uint8_t jboolean;
typedef int32_t jsize;

typedef void* jobject;
typedef void* jclass;
typedef void* jmethodID;
typedef void* jstring;
typedef void* jbyteArray;

struct JNINativeInterface_;
typedef struct JNINativeInterface_* JNIEnv;

struct JNINativeInterface_ {
    const char* (*GetStringUTFChars)(JNIEnv* env, jstring str, jboolean* isCopy);
    void (*ReleaseStringUTFChars)(JNIEnv* env, jstring str, const char* chars);

    jbyte* (*GetByteArrayElements)(JNIEnv* env, jbyteArray array, jboolean* isCopy);
    void (*ReleaseByteArrayElements)(JNIEnv* env, jbyteArray array, jbyte* elems, jint mode);
    jsize (*GetArrayLength)(JNIEnv* env, jbyteArray array);

    void* (*GetDirectBufferAddress)(JNIEnv* env, jobject buf);
    jlong (*GetDirectBufferCapacity)(JNIEnv* env, jobject buf);
};

/*
    Minimal Tox-related constants and types used by selected extracted
    functions. Adjust these if you extract more functions.
*/

#define TOX_MSGV3_MSGID_LENGTH 8

typedef enum {
    TOX_ERR_FRIEND_CUSTOM_PACKET_OK = 0,
    TOX_ERR_FRIEND_CUSTOM_PACKET_NULL = 1,
    TOX_ERR_FRIEND_CUSTOM_PACKET_FRIEND_NOT_FOUND = 2,
    TOX_ERR_FRIEND_CUSTOM_PACKET_FRIEND_NOT_CONNECTED = 3,
    TOX_ERR_FRIEND_CUSTOM_PACKET_SENDQ = 4,
    TOX_ERR_FRIEND_CUSTOM_PACKET_TOO_LONG = 5
} TOX_ERR_FRIEND_CUSTOM_PACKET;

/*
    Helper objects for mocked JNI calls.
*/

typedef struct {
    uint8_t* data;
    jsize length;
} MockByteArray;

typedef struct {
    void* address;
    jlong capacity;
} MockDirectBuffer;

/*
    Tiny test framework.
*/

void harness_suite_begin(const char* name);
void harness_suite_end(void);

void harness_test_begin(const char* name);
void harness_test_end(void);

void harness_note(const char* msg);

void harness_assert(int ok, const char* expr, const char* file, int line);
void harness_assert_eq_long(long expected, long actual, const char* expr, const char* file, int line);
void harness_assert_eq_size(size_t expected, size_t actual, const char* expr, const char* file, int line);

void harness_print_report(const char* title);
int harness_result(void);

#define SUITE_BEGIN(name) harness_suite_begin(name)
#define SUITE_END() harness_suite_end()

#define TEST_BEGIN(name) harness_test_begin(name)
#define TEST_END() harness_test_end()

#define NOTE(msg) harness_note(msg)

#define TEST_ASSERT(expr) \
    harness_assert((expr) ? 1 : 0, #expr, __FILE__, __LINE__)

#define TEST_ASSERT_FALSE(expr) \
    harness_assert((expr) ? 0 : 1, "!(" #expr ")", __FILE__, __LINE__)

#define TEST_EQUAL_LONG(expected, actual) \
    harness_assert_eq_long((long)(expected), (long)(actual), #actual " == " #expected, __FILE__, __LINE__)

#define TEST_EQUAL_SIZE(expected, actual) \
    harness_assert_eq_size((size_t)(expected), (size_t)(actual), #actual " == " #expected, __FILE__, __LINE__)

/*
    JNI mock.
*/

JNIEnv* jni_mock_env(void);
void jni_mock_reset(void);

void jni_mock_set_string(const char* s);
void jni_mock_set_string_null(void);

/*
    Tox mock.
*/

extern void* tox_global;

void tox_mock_reset(void);

extern bool tox_mock_lossless_called;
extern uint32_t tox_mock_last_lossless_friend_number;
extern const uint8_t* tox_mock_last_lossless_data;
extern size_t tox_mock_last_lossless_length;
extern uint32_t tox_mock_lossless_return;
extern TOX_ERR_FRIEND_CUSTOM_PACKET tox_mock_lossless_error;

extern bool tox_mock_messagev3_id_called;
extern bool tox_mock_messagev3_id_return;
extern uint8_t* tox_mock_messagev3_id_last_buffer;

uint32_t tox_friend_send_lossless_packet(
    void* tox,
    uint32_t friend_number,
    const uint8_t* data,
    size_t length,
    TOX_ERR_FRIEND_CUSTOM_PACKET* error
);

bool tox_messagev3_get_new_message_id(uint8_t* buffer);

/*
    Macros used by the real jni-c-toxcore.c code.
*/

#define TRACE_LOGGER()
#define dbg(level, ...) do { } while (0)
#define CLEAR(x) memset(&(x), 0, sizeof(x))

#endif /* UNIT_TEST_HARNESS_H */
