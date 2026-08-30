#ifndef UNIT_TEST_HARNESS_H
#define UNIT_TEST_HARNESS_H

/*
    Needed for strdup() when compiling with -std=c11.
*/
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <stdio.h>
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

/*
    Include the real tox.h and toxutil.h for enums, constants, and types.
*/
#include "tox.h"
#include "toxutil.h"

/* =========================================================
 * Minimal JNI replacement
 * ========================================================= */

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

/* =========================================================
 * Tox constants not in tox.h but used by JNI code
 * ========================================================= */

#ifndef TOX_MSGV3_MSGID_LENGTH
#define TOX_MSGV3_MSGID_LENGTH 8
#endif

#ifndef TOX_MSGV3_MAX_MESSAGE_LENGTH
#define TOX_MSGV3_MAX_MESSAGE_LENGTH 1368
#endif

#ifndef TOX_MSGV3_GUARD
#define TOX_MSGV3_GUARD 0
#endif

#ifndef TOX_MSGV3_TIMESTAMP_LENGTH
#define TOX_MSGV3_TIMESTAMP_LENGTH 4
#endif

/* =========================================================
 * Helper objects for mocked JNI calls
 * ========================================================= */

typedef struct {
    uint8_t* data;
    jsize length;
} MockByteArray;

typedef struct {
    void* address;
    jlong capacity;
} MockDirectBuffer;

/* =========================================================
 * Tiny test framework
 * ========================================================= */

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

/* =========================================================
 * JNI mock
 * ========================================================= */

JNIEnv* jni_mock_env(void);
void jni_mock_reset(void);

void jni_mock_set_string(const char* s);
void jni_mock_set_string_null(void);

/* =========================================================
 * Tox mock state
 * ========================================================= */

extern Tox* tox_global;

void tox_mock_reset(void);

/* ---------------------------------------------------------
 * friend_send_lossless_packet
 * --------------------------------------------------------- */

extern bool tox_mock_lossless_called;
extern uint32_t tox_mock_last_lossless_friend_number;
extern const uint8_t* tox_mock_last_lossless_data;
extern size_t tox_mock_last_lossless_length;
extern bool tox_mock_lossless_return;
extern Tox_Err_Friend_Custom_Packet tox_mock_lossless_error;

/* ---------------------------------------------------------
 * messagev3_get_new_message_id
 * --------------------------------------------------------- */

extern bool tox_mock_messagev3_id_called;
extern bool tox_mock_messagev3_id_return;
extern uint8_t* tox_mock_messagev3_id_last_buffer;

/* ---------------------------------------------------------
 * friend_add / toxid_hex_to_bin
 * --------------------------------------------------------- */

extern bool tox_mock_hex_to_bin_called;
extern char tox_mock_last_hex[512];
extern size_t tox_mock_last_hex_length;

extern bool tox_mock_friend_add_called;
extern uint8_t tox_mock_last_friend_add_address[TOX_ADDRESS_SIZE];
extern size_t tox_mock_last_friend_add_message_length;
extern uint32_t tox_mock_friend_add_return;
extern Tox_Err_Friend_Add tox_mock_friend_add_error;

/* ---------------------------------------------------------
 * friend_add_norequest / toxpk_hex_to_bin
 * --------------------------------------------------------- */

extern bool tox_mock_pk_hex_to_bin_called;
extern char tox_mock_last_pk_hex[512];
extern size_t tox_mock_last_pk_hex_length;

extern bool tox_mock_friend_add_norequest_called;
extern uint8_t tox_mock_last_friend_add_norequest_key[TOX_PUBLIC_KEY_SIZE];
extern uint32_t tox_mock_friend_add_norequest_return;

/* ---------------------------------------------------------
 * friend_send_message / messagev3_friend_send_message
 * --------------------------------------------------------- */

extern bool tox_mock_friend_send_message_called;
extern uint32_t tox_mock_last_friend_send_message_friend_number;
extern Tox_Message_Type tox_mock_last_friend_send_message_type;
extern size_t tox_mock_last_friend_send_message_length;
extern uint32_t tox_mock_friend_send_message_return;
extern Tox_Err_Friend_Send_Message tox_mock_friend_send_message_error;

/* ---------------------------------------------------------
 * xnet_pack_u32
 * --------------------------------------------------------- */

extern bool tox_mock_xnet_pack_u32_called;
extern uint32_t tox_mock_last_xnet_pack_u32_value;

/* ---------------------------------------------------------
 * self_set_name
 * --------------------------------------------------------- */

extern bool tox_mock_self_set_name_called;
extern size_t tox_mock_self_set_name_length;
extern bool tox_mock_self_set_name_return;
extern Tox_Err_Set_Info tox_mock_self_set_name_error;

/* ---------------------------------------------------------
 * self_set_status_message
 * --------------------------------------------------------- */

extern bool tox_mock_self_set_status_message_called;
extern size_t tox_mock_self_set_status_message_length;
extern bool tox_mock_self_set_status_message_return;
extern Tox_Err_Set_Info tox_mock_self_set_status_message_error;

/* ---------------------------------------------------------
 * file_control
 * --------------------------------------------------------- */

extern bool tox_mock_file_control_called;
extern uint32_t tox_mock_last_file_control_friend_number;
extern uint32_t tox_mock_last_file_control_file_number;
extern Tox_File_Control tox_mock_last_file_control_control;
extern bool tox_mock_file_control_return;
extern Tox_Err_File_Control tox_mock_file_control_error;

/* ---------------------------------------------------------
 * friend_delete
 * --------------------------------------------------------- */

extern bool tox_mock_friend_delete_called;
extern uint32_t tox_mock_last_friend_delete_friend_number;
extern bool tox_mock_friend_delete_return;
extern Tox_Err_Friend_Delete tox_mock_friend_delete_error;

/* ---------------------------------------------------------
 * friend_get_connection_ip
 * --------------------------------------------------------- */

extern bool tox_mock_friend_get_connection_ip_called;
extern uint32_t tox_mock_last_friend_get_connection_ip_friend_number;

#define TOX_MOCK_IP_BUFFER_SIZE 293
extern char tox_mock_ip_to_write[TOX_MOCK_IP_BUFFER_SIZE];

/* ---------------------------------------------------------
 * c_safe_string_from_java
 * --------------------------------------------------------- */

extern bool tox_mock_safe_string_called;
extern size_t tox_mock_safe_string_last_length;
extern char tox_mock_safe_string_last[1024];

/* ---------------------------------------------------------
 * self_get_name
 * --------------------------------------------------------- */

extern size_t tox_mock_self_get_name_size_return;
extern bool tox_mock_self_get_name_called;
extern char tox_mock_self_get_name_data[1024];

/* ---------------------------------------------------------
 * self_get_status_message
 * --------------------------------------------------------- */

extern size_t tox_mock_self_get_status_message_size_return;
extern bool tox_mock_self_get_status_message_called;
extern char tox_mock_self_get_status_message_data[1024];

/* ---------------------------------------------------------
 * self_set_status
 * --------------------------------------------------------- */

extern bool tox_mock_self_set_status_called;
extern int tox_mock_last_self_set_status;

/* ---------------------------------------------------------
 * friend_get_connection_status
 * --------------------------------------------------------- */

extern bool tox_mock_friend_get_connection_status_called;
extern int tox_mock_friend_get_connection_status_return;
extern uint32_t tox_mock_last_friend_get_connection_status_friend_number;

/* ---------------------------------------------------------
 * get_all_tcp_relays
 * --------------------------------------------------------- */

extern bool tox_mock_get_all_tcp_relays_called;
extern char tox_mock_get_all_tcp_relays_data[1024];

/* ---------------------------------------------------------
 * self_set_typing
 * --------------------------------------------------------- */

extern bool tox_mock_self_set_typing_called;
extern bool tox_mock_self_set_typing_return;
extern uint32_t tox_mock_last_self_set_typing_friend_number;

/* ---------------------------------------------------------
 * self_get_address / self_get_public_key / self_get_secret_key
 * --------------------------------------------------------- */

extern bool tox_mock_self_get_address_called;
extern bool tox_mock_self_get_public_key_called;
extern bool tox_mock_self_get_secret_key_called;

/* ---------------------------------------------------------
 * friend_get_name
 * --------------------------------------------------------- */

extern bool tox_mock_friend_get_name_called;
extern size_t tox_mock_friend_get_name_size_return;
extern char tox_mock_friend_get_name_data[1024];
extern bool tox_mock_friend_get_name_return;
extern uint32_t tox_mock_last_friend_get_name_friend_number;

/* ---------------------------------------------------------
 * file_send
 * --------------------------------------------------------- */

extern bool tox_mock_file_send_called;
extern uint32_t tox_mock_file_send_return;
extern TOX_ERR_FILE_SEND tox_mock_file_send_error;
extern uint32_t tox_mock_last_file_send_friend_number;
extern uint64_t tox_mock_last_file_send_file_size;
extern size_t tox_mock_last_file_send_filename_length;

/* ---------------------------------------------------------
 * file_send_chunk
 * --------------------------------------------------------- */

extern bool tox_mock_file_send_chunk_called;
extern bool tox_mock_file_send_chunk_return;
extern TOX_ERR_FILE_SEND_CHUNK tox_mock_file_send_chunk_error;
extern uint32_t tox_mock_last_file_send_chunk_friend_number;
extern uint32_t tox_mock_last_file_send_chunk_file_number;
extern uint64_t tox_mock_last_file_send_chunk_position;
extern size_t tox_mock_last_file_send_chunk_length;

/* ---------------------------------------------------------
 * file_seek
 * --------------------------------------------------------- */

extern bool tox_mock_file_seek_called;
extern bool tox_mock_file_seek_return;
extern TOX_ERR_FILE_SEEK tox_mock_file_seek_error;
extern uint32_t tox_mock_last_file_seek_friend_number;
extern uint32_t tox_mock_last_file_seek_file_number;
extern uint64_t tox_mock_last_file_seek_position;

/* ---------------------------------------------------------
 * file_get_file_id
 * --------------------------------------------------------- */

extern bool tox_mock_file_get_file_id_called;
extern bool tox_mock_file_get_file_id_return;
extern TOX_ERR_FILE_GET tox_mock_file_get_file_id_error;
extern uint32_t tox_mock_last_file_get_file_id_friend_number;
extern uint32_t tox_mock_last_file_get_file_id_file_number;

/* =========================================================
 * Mocked helper functions
 *
 * All the standard tox_* functions are already declared in tox.h,
 * so we just need to provide mock implementations in harness.c.
 * These are non-standard helpers that also need mocking.
 * ========================================================= */

int toxid_hex_to_bin(uint8_t* bin, const char* hex);
int toxpk_hex_to_bin(uint8_t* bin, const char* hex);
void xnet_pack_u32(uint8_t* dst, uint32_t value);
jstring c_safe_string_from_java(char* str, size_t length);
bool tox_messagev3_get_new_message_id(uint8_t* buffer);

/* =========================================================
 * Macros used by the real jni-c-toxcore.c code
 * ========================================================= */

#define TRACE_LOGGER()
#define dbg(level, ...) do { } while (0)
#define CLEAR(x) memset(&(x), 0, sizeof(x))

#endif /* UNIT_TEST_HARNESS_H */
