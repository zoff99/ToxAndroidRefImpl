#include "harness.h"

/* =========================================================
 * Output helpers
 * ========================================================= */

#define HARNESS_BOX_WIDTH 60
#define HARNESS_TEST_NAME_WIDTH 48

static void print_chars(char c, int n) {
    for (int i = 0; i < n; i++) {
        fputc(c, stdout);
    }
}

static void print_rule(char c, int width) {
    printf("  ");
    print_chars(c, width);
    printf("\n");
}

static void print_box_top(void) {
    printf("  +");
    print_chars('=', HARNESS_BOX_WIDTH);
    printf("+\n");
}

static void print_box_title(const char* title) {
    char buf[512];
    snprintf(buf, sizeof(buf), "  SUITE: %s", title);

    printf("  |");
    printf("%-60.60s", buf);
    printf("|\n");
}

static void print_box_footer(const char* name, double elapsed) {
    char buf[512];
    snprintf(buf, sizeof(buf), "---- %s done (%.3fs) ----", name, elapsed);

    printf("  +%s+\n", buf);
}

static double elapsed_since(clock_t start) {
    return (double)(clock() - start) / (double)CLOCKS_PER_SEC;
}

/* =========================================================
 * Test state
 * ========================================================= */

static int total_tests = 0;
static int passed_tests = 0;
static int failed_tests = 0;

static int suites_run = 0;
static int suites_failed = 0;

static int suite_failed_tests = 0;

static int current_test_failed = 0;

static clock_t suite_start_time;
static clock_t test_start_time;

static char current_suite_name[256] = "";
static char current_test_name[256] = "";

/* =========================================================
 * Suite API
 * ========================================================= */

void harness_suite_begin(const char* name) {
    snprintf(current_suite_name, sizeof(current_suite_name), "%s", name);

    suite_failed_tests = 0;
    suites_run++;

    suite_start_time = clock();

    printf("\n");
    print_box_top();
    print_box_title(name);
    print_box_top();
}

void harness_suite_end(void) {
    double elapsed = elapsed_since(suite_start_time);

    if (suite_failed_tests > 0) {
        suites_failed++;
    }

    print_box_footer(current_suite_name, elapsed);
}

/* =========================================================
 * Test API
 * ========================================================= */

void harness_test_begin(const char* name) {
    snprintf(current_test_name, sizeof(current_test_name), "%s", name);
    current_test_failed = 0;
    test_start_time = clock();
}

void harness_test_end(void) {
    double elapsed = elapsed_since(test_start_time);

    total_tests++;

    if (current_test_failed) {
        failed_tests++;
        suite_failed_tests++;
        printf("  \033[31m[FAIL] %-48.48s (%.3fs)\033[0m\n",
               current_test_name, elapsed);
    } else {
        passed_tests++;
        printf("  \033[32m[PASS] %-48.48s (%.3fs)\033[0m\n",
               current_test_name, elapsed);
    }
}

void harness_note(const char* msg) {
    printf("         %s\n", msg);
}

/* =========================================================
 * Assertions
 * ========================================================= */

void harness_assert(int ok, const char* expr, const char* file, int line) {
    if (!ok) {
        current_test_failed = 1;
        printf("         FAIL %s:%d: %s\n", file, line, expr);
    }
}

void harness_assert_eq_long(long expected, long actual, const char* expr, const char* file, int line) {
    if (expected != actual) {
        current_test_failed = 1;
        printf("         FAIL %s:%d: %s (expected %ld, got %ld)\n",
               file, line, expr, expected, actual);
    }
}

void harness_assert_eq_size(size_t expected, size_t actual, const char* expr, const char* file, int line) {
    if (expected != actual) {
        current_test_failed = 1;
        printf("         FAIL %s:%d: %s (expected %zu, got %zu)\n",
               file, line, expr, expected, actual);
    }
}

/* =========================================================
 * Final report
 * ========================================================= */

void harness_print_report(const char* title) {
    printf("\n");

    print_rule('=', 62);
    printf("    RESULTS: %s\n", title);
    print_rule('=', 62);

    printf("    Total:   %d\n", total_tests);
    printf("    Passed:  %d\n", passed_tests);
    printf("    Failed:  %d\n", failed_tests);

    print_rule('=', 62);

    if (failed_tests == 0) {
        printf("    >>> ALL PASSED <<<\n");
    } else {
        printf("    >>> %d TEST(S) FAILED <<<\n", failed_tests);
    }

    print_rule('=', 62);

    printf("\n");

    print_rule('=', 60);
    printf("  GRAND TOTAL (all individual tests across all suites)\n");
    print_rule('=', 60);

    printf("    Total:   %d\n", total_tests);
    printf("    Passed:  %d\n", passed_tests);
    printf("    Failed:  %d\n", failed_tests);
    printf("    Suites:  %d run, %d with failures\n", suites_run, suites_failed);

    print_rule('=', 60);

    if (failed_tests == 0) {
        printf("    >>> ALL %d TESTS PASSED <<<\n", total_tests);
    } else {
        printf("    >>> %d TEST(S) FAILED <<<\n", failed_tests);
    }

    print_rule('=', 60);
}

int harness_result(void) {
    return failed_tests ? 1 : 0;
}

/* =========================================================
 * JNI mock
 * ========================================================= */

static const char* jni_mock_string = "mock";
static bool jni_mock_string_null = false;

static const char* mock_GetStringUTFChars(JNIEnv* env, jstring str, jboolean* isCopy) {
    (void)env;

    if (isCopy) {
        *isCopy = JNI_FALSE;
    }

    if (jni_mock_string_null) {
        return NULL;
    }

    if (str) {
        return (const char*)str;
    }

    return jni_mock_string;
}

static void mock_ReleaseStringUTFChars(JNIEnv* env, jstring str, const char* chars) {
    (void)env;
    (void)str;
    (void)chars;
}

static jbyte* mock_GetByteArrayElements(JNIEnv* env, jbyteArray array, jboolean* isCopy) {
    (void)env;

    if (isCopy) {
        *isCopy = JNI_FALSE;
    }

    if (!array) {
        return NULL;
    }

    return (jbyte*)((MockByteArray*)array)->data;
}

static void mock_ReleaseByteArrayElements(JNIEnv* env, jbyteArray array, jbyte* elems, jint mode) {
    (void)env;
    (void)array;
    (void)elems;
    (void)mode;
}

static jsize mock_GetArrayLength(JNIEnv* env, jbyteArray array) {
    (void)env;

    if (!array) {
        return 0;
    }

    return ((MockByteArray*)array)->length;
}

static void* mock_GetDirectBufferAddress(JNIEnv* env, jobject buf) {
    (void)env;

    if (!buf) {
        return NULL;
    }

    return ((MockDirectBuffer*)buf)->address;
}

static jlong mock_GetDirectBufferCapacity(JNIEnv* env, jobject buf) {
    (void)env;

    if (!buf) {
        return 0;
    }

    return ((MockDirectBuffer*)buf)->capacity;
}

/* Additional JNI mock functions for c_safe_string_from_java */
bool mock_NewStringUTF_called = false;
jstring mock_NewStringUTF_return = NULL;

bool mock_NewByteArray_called = false;
jbyteArray mock_NewByteArray_return = NULL;

bool mock_SetByteArrayRegion_called = false;

bool mock_CallStaticObjectMethod_called = false;
jobject mock_CallStaticObjectMethod_return = NULL;

bool mock_ExceptionCheck_called = false;
jboolean mock_ExceptionCheck_return = JNI_FALSE;

bool mock_ExceptionClear_called = false;

bool mock_DeleteLocalRef_called = false;

bool mock_CallStaticVoidMethod_called = false;

static jstring mock_NewStringUTF(JNIEnv* env, const char* str) {
    (void)env;
    (void)str;
    mock_NewStringUTF_called = true;
    return mock_NewStringUTF_return;
}

static jbyteArray mock_NewByteArray(JNIEnv* env, jsize length) {
    (void)env;
    (void)length;
    mock_NewByteArray_called = true;
    return mock_NewByteArray_return;
}

static void mock_SetByteArrayRegion(JNIEnv* env, jbyteArray array, jsize start, jsize len, const jbyte* buf) {
    (void)env;
    (void)array;
    (void)start;
    (void)len;
    (void)buf;
    mock_SetByteArrayRegion_called = true;
}

static jobject mock_CallStaticObjectMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    (void)env;
    (void)clazz;
    (void)methodID;
    mock_CallStaticObjectMethod_called = true;
    return mock_CallStaticObjectMethod_return;
}

static jboolean mock_ExceptionCheck(JNIEnv* env) {
    (void)env;
    mock_ExceptionCheck_called = true;
    return mock_ExceptionCheck_return;
}

static void mock_ExceptionClear(JNIEnv* env) {
    (void)env;
    mock_ExceptionClear_called = true;
}

static void mock_DeleteLocalRef(JNIEnv* env, jobject localRef) {
    (void)env;
    (void)localRef;
    mock_DeleteLocalRef_called = true;
}

void mock_CallStaticVoidMethod_fn(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    (void)env;
    (void)clazz;
    mock_CallStaticVoidMethod_called = true;

    if (methodID == android_tox_callback_friend_message_cb_method) {
        va_list args;
        va_start(args, methodID);
        jlong fn = va_arg(args, jlong);
        jint type = va_arg(args, jint);
        jstring js1 = va_arg(args, jstring);
        jlong new_length = va_arg(args, jlong);
        jbyteArray hash = va_arg(args, jbyteArray);
        jlong timestamp = va_arg(args, jlong);
        va_end(args);

        tox_mock_callback_friend_message_called = true;
        tox_mock_callback_friend_message_friend_number = (uint32_t)fn;
        tox_mock_callback_friend_message_type = (Tox_Message_Type)type;
        tox_mock_callback_friend_message_length = (size_t)new_length;
        tox_mock_callback_friend_message_js1 = js1;
        tox_mock_callback_friend_message_hash_jbuffer = hash;
        tox_mock_callback_friend_message_timestamp = (uint32_t)timestamp;
    }
}

/* Use designated initializers to avoid ordering issues */
static struct JNINativeInterface_ jni_table = {
    .GetStringUTFChars = mock_GetStringUTFChars,
    .ReleaseStringUTFChars = mock_ReleaseStringUTFChars,
    .GetByteArrayElements = mock_GetByteArrayElements,
    .ReleaseByteArrayElements = mock_ReleaseByteArrayElements,
    .GetArrayLength = mock_GetArrayLength,
    .GetDirectBufferAddress = mock_GetDirectBufferAddress,
    .GetDirectBufferCapacity = mock_GetDirectBufferCapacity,
    .CallStaticVoidMethod = mock_CallStaticVoidMethod_fn,
    .NewStringUTF = mock_NewStringUTF,
    .NewByteArray = mock_NewByteArray,
    .SetByteArrayRegion = mock_SetByteArrayRegion,
    .CallStaticObjectMethod = mock_CallStaticObjectMethod,
    .ExceptionCheck = mock_ExceptionCheck,
    .ExceptionClear = mock_ExceptionClear,
    .DeleteLocalRef = mock_DeleteLocalRef,
};

static JNIEnv jni_env_value = &jni_table;

JNIEnv* jni_mock_env(void) {
    return &jni_env_value;
}

void jni_mock_reset(void) {
    jni_mock_string = "mock";
    jni_mock_string_null = false;
}

void jni_mock_set_string(const char* s) {
    jni_mock_string = s;
    jni_mock_string_null = false;
}

void jni_mock_set_string_null(void) {
    jni_mock_string = NULL;
    jni_mock_string_null = true;
}

/* =========================================================
 * Tox mock state
 * ========================================================= */

static char tox_mock_dummy_tox;

Tox* tox_global = NULL;

/* friend_send_lossless_packet */
bool tox_mock_lossless_called = false;
uint32_t tox_mock_last_lossless_friend_number = 0;
const uint8_t* tox_mock_last_lossless_data = NULL;
size_t tox_mock_last_lossless_length = 0;
bool tox_mock_lossless_return = true;
Tox_Err_Friend_Custom_Packet tox_mock_lossless_error = TOX_ERR_FRIEND_CUSTOM_PACKET_OK;

/* messagev3_get_new_message_id */
bool tox_mock_messagev3_id_called = false;
bool tox_mock_messagev3_id_return = true;
uint8_t* tox_mock_messagev3_id_last_buffer = NULL;

/* friend_add / toxid_hex_to_bin */
bool tox_mock_hex_to_bin_called = false;
char tox_mock_last_hex[512] = "";
size_t tox_mock_last_hex_length = 0;

bool tox_mock_friend_add_called = false;
uint8_t tox_mock_last_friend_add_address[TOX_ADDRESS_SIZE];
size_t tox_mock_last_friend_add_message_length = 0;
uint32_t tox_mock_friend_add_return = 0;
Tox_Err_Friend_Add tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_OK;

/* friend_add_norequest / toxpk_hex_to_bin */
bool tox_mock_pk_hex_to_bin_called = false;
char tox_mock_last_pk_hex[512] = "";
size_t tox_mock_last_pk_hex_length = 0;

bool tox_mock_friend_add_norequest_called = false;
uint8_t tox_mock_last_friend_add_norequest_key[TOX_PUBLIC_KEY_SIZE];
uint32_t tox_mock_friend_add_norequest_return = 0;

/* friend_send_message / messagev3_friend_send_message */
bool tox_mock_friend_send_message_called = false;
uint32_t tox_mock_last_friend_send_message_friend_number = 0;
Tox_Message_Type tox_mock_last_friend_send_message_type = TOX_MESSAGE_TYPE_NORMAL;
size_t tox_mock_last_friend_send_message_length = 0;
uint32_t tox_mock_friend_send_message_return = 0;
Tox_Err_Friend_Send_Message tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

/* xnet_pack_u32 */
bool tox_mock_xnet_pack_u32_called = false;
uint32_t tox_mock_last_xnet_pack_u32_value = 0;

/* self_set_name */
bool tox_mock_self_set_name_called = false;
size_t tox_mock_self_set_name_length = 0;
bool tox_mock_self_set_name_return = true;
Tox_Err_Set_Info tox_mock_self_set_name_error = TOX_ERR_SET_INFO_OK;

/* self_set_status_message */
bool tox_mock_self_set_status_message_called = false;
size_t tox_mock_self_set_status_message_length = 0;
bool tox_mock_self_set_status_message_return = true;
Tox_Err_Set_Info tox_mock_self_set_status_message_error = TOX_ERR_SET_INFO_OK;

/* file_control */
bool tox_mock_file_control_called = false;
uint32_t tox_mock_last_file_control_friend_number = 0;
uint32_t tox_mock_last_file_control_file_number = 0;
Tox_File_Control tox_mock_last_file_control_control = TOX_FILE_CONTROL_RESUME;
bool tox_mock_file_control_return = true;
Tox_Err_File_Control tox_mock_file_control_error = TOX_ERR_FILE_CONTROL_OK;

/* friend_delete */
bool tox_mock_friend_delete_called = false;
uint32_t tox_mock_last_friend_delete_friend_number = 0;
bool tox_mock_friend_delete_return = true;
Tox_Err_Friend_Delete tox_mock_friend_delete_error = TOX_ERR_FRIEND_DELETE_OK;

/* friend_get_connection_ip */
bool tox_mock_friend_get_connection_ip_called = false;
uint32_t tox_mock_last_friend_get_connection_ip_friend_number = 0;
char tox_mock_ip_to_write[TOX_MOCK_IP_BUFFER_SIZE] = "";

/* c_safe_string_from_java */
bool tox_mock_safe_string_called = false;
size_t tox_mock_safe_string_last_length = 0;
char tox_mock_safe_string_last[1024] = "";

/* self_get_name */
size_t tox_mock_self_get_name_size_return = 0;
bool tox_mock_self_get_name_called = false;
char tox_mock_self_get_name_data[1024] = "";

/* self_get_status_message */
size_t tox_mock_self_get_status_message_size_return = 0;
bool tox_mock_self_get_status_message_called = false;
char tox_mock_self_get_status_message_data[1024] = "";

/* self_set_status */
bool tox_mock_self_set_status_called = false;
int tox_mock_last_self_set_status = 0;

/* friend_get_connection_status */
bool tox_mock_friend_get_connection_status_called = false;
int tox_mock_friend_get_connection_status_return = 0;
uint32_t tox_mock_last_friend_get_connection_status_friend_number = 0;

/* get_all_tcp_relays */
bool tox_mock_get_all_tcp_relays_called = false;
char tox_mock_get_all_tcp_relays_data[1024] = "";

/* self_set_typing */
bool tox_mock_self_set_typing_called = false;
bool tox_mock_self_set_typing_return = true;
uint32_t tox_mock_last_self_set_typing_friend_number = 0;

/* friend_get_name */
bool tox_mock_friend_get_name_called = false;
size_t tox_mock_friend_get_name_size_return = 0;
char tox_mock_friend_get_name_data[1024] = "";
bool tox_mock_friend_get_name_return = true;
uint32_t tox_mock_last_friend_get_name_friend_number = 0;

/* self_get_address / self_get_public_key / self_get_secret_key */
bool tox_mock_self_get_address_called = false;
bool tox_mock_self_get_public_key_called = false;
bool tox_mock_self_get_secret_key_called = false;

/* file_send */
bool tox_mock_file_send_called = false;
uint32_t tox_mock_file_send_return = 0;
TOX_ERR_FILE_SEND tox_mock_file_send_error = TOX_ERR_FILE_SEND_OK;
uint32_t tox_mock_last_file_send_friend_number = 0;
uint64_t tox_mock_last_file_send_file_size = 0;
size_t tox_mock_last_file_send_filename_length = 0;

/* file_send_chunk */
bool tox_mock_file_send_chunk_called = false;
bool tox_mock_file_send_chunk_return = true;
TOX_ERR_FILE_SEND_CHUNK tox_mock_file_send_chunk_error = TOX_ERR_FILE_SEND_CHUNK_OK;
uint32_t tox_mock_last_file_send_chunk_friend_number = 0;
uint32_t tox_mock_last_file_send_chunk_file_number = 0;
uint64_t tox_mock_last_file_send_chunk_position = 0;
size_t tox_mock_last_file_send_chunk_length = 0;

/* file_seek */
bool tox_mock_file_seek_called = false;
bool tox_mock_file_seek_return = true;
TOX_ERR_FILE_SEEK tox_mock_file_seek_error = TOX_ERR_FILE_SEEK_OK;
uint32_t tox_mock_last_file_seek_friend_number = 0;
uint32_t tox_mock_last_file_seek_file_number = 0;
uint64_t tox_mock_last_file_seek_position = 0;

/* file_get_file_id */
bool tox_mock_file_get_file_id_called = false;
bool tox_mock_file_get_file_id_return = true;
TOX_ERR_FILE_GET tox_mock_file_get_file_id_error = TOX_ERR_FILE_GET_OK;
uint32_t tox_mock_last_file_get_file_id_friend_number = 0;
uint32_t tox_mock_last_file_get_file_id_file_number = 0;

/* android_tox_callback_friend_message mock state */
bool tox_mock_callback_friend_message_called = false;
uint32_t tox_mock_callback_friend_message_friend_number = 0;
Tox_Message_Type tox_mock_callback_friend_message_type = TOX_MESSAGE_TYPE_NORMAL;
size_t tox_mock_callback_friend_message_length = 0;
jstring tox_mock_callback_friend_message_js1 = NULL;
jbyteArray tox_mock_callback_friend_message_hash_jbuffer = NULL;
uint32_t tox_mock_callback_friend_message_timestamp = 0;

/* Global variables used by android_tox_callback_friend_message_cb */
jclass MainActivity = NULL;
jmethodID android_tox_callback_friend_message_cb_method = NULL;

size_t xnet_unpack_u32(const uint8_t* src, uint32_t* value) {
    if (src && value) {
        *value = (uint32_t)src[0] | ((uint32_t)src[1] << 8) | ((uint32_t)src[2] << 16) | ((uint32_t)src[3] << 24);
    }
    return 4;
}

/* c_safe_string_from_java mock state */
JNIEnv* mock_jni_env_ptr = NULL;
jclass TrifaToxService_class = NULL;
jmethodID safe_string_method = NULL;

/* Mock jni_getenv to return our controlled environment */
JNIEnv* jni_getenv(void) {
    return mock_jni_env_ptr;
}

/* =========================================================
 * tox_mock_reset()
 * ========================================================= */

void tox_mock_reset(void) {
    tox_global = (Tox*)&tox_mock_dummy_tox;

    /* lossless */
    tox_mock_lossless_called = false;
    tox_mock_last_lossless_friend_number = 0;
    tox_mock_last_lossless_data = NULL;
    tox_mock_last_lossless_length = 0;
    tox_mock_lossless_return = true;
    tox_mock_lossless_error = TOX_ERR_FRIEND_CUSTOM_PACKET_OK;

    /* messagev3 id */
    tox_mock_messagev3_id_called = false;
    tox_mock_messagev3_id_return = true;
    tox_mock_messagev3_id_last_buffer = NULL;

    /* friend_add / toxid_hex_to_bin */
    tox_mock_hex_to_bin_called = false;
    tox_mock_last_hex[0] = '\0';
    tox_mock_last_hex_length = 0;

    tox_mock_friend_add_called = false;
    memset(tox_mock_last_friend_add_address, 0, sizeof(tox_mock_last_friend_add_address));
    tox_mock_last_friend_add_message_length = 0;
    tox_mock_friend_add_return = 0;
    tox_mock_friend_add_error = TOX_ERR_FRIEND_ADD_OK;

    /* friend_add_norequest / toxpk_hex_to_bin */
    tox_mock_pk_hex_to_bin_called = false;
    tox_mock_last_pk_hex[0] = '\0';
    tox_mock_last_pk_hex_length = 0;

    tox_mock_friend_add_norequest_called = false;
    memset(tox_mock_last_friend_add_norequest_key, 0, sizeof(tox_mock_last_friend_add_norequest_key));
    tox_mock_friend_add_norequest_return = 0;

    /* friend_send_message / messagev3_friend_send_message */
    tox_mock_friend_send_message_called = false;
    tox_mock_last_friend_send_message_friend_number = 0;
    tox_mock_last_friend_send_message_type = TOX_MESSAGE_TYPE_NORMAL;
    tox_mock_last_friend_send_message_length = 0;
    tox_mock_friend_send_message_return = 0;
    tox_mock_friend_send_message_error = TOX_ERR_FRIEND_SEND_MESSAGE_OK;

    /* xnet_pack_u32 */
    tox_mock_xnet_pack_u32_called = false;
    tox_mock_last_xnet_pack_u32_value = 0;

    /* self_set_name */
    tox_mock_self_set_name_called = false;
    tox_mock_self_set_name_length = 0;
    tox_mock_self_set_name_return = true;
    tox_mock_self_set_name_error = TOX_ERR_SET_INFO_OK;

    /* self_set_status_message */
    tox_mock_self_set_status_message_called = false;
    tox_mock_self_set_status_message_length = 0;
    tox_mock_self_set_status_message_return = true;
    tox_mock_self_set_status_message_error = TOX_ERR_SET_INFO_OK;

    /* file_control */
    tox_mock_file_control_called = false;
    tox_mock_last_file_control_friend_number = 0;
    tox_mock_last_file_control_file_number = 0;
    tox_mock_last_file_control_control = TOX_FILE_CONTROL_RESUME;
    tox_mock_file_control_return = true;
    tox_mock_file_control_error = TOX_ERR_FILE_CONTROL_OK;

    /* friend_delete */
    tox_mock_friend_delete_called = false;
    tox_mock_last_friend_delete_friend_number = 0;
    tox_mock_friend_delete_return = true;
    tox_mock_friend_delete_error = TOX_ERR_FRIEND_DELETE_OK;

    /* friend_get_connection_ip */
    tox_mock_friend_get_connection_ip_called = false;
    tox_mock_last_friend_get_connection_ip_friend_number = 0;
    tox_mock_ip_to_write[0] = '\0';

    /* c_safe_string_from_java */
    tox_mock_safe_string_called = false;
    tox_mock_safe_string_last_length = 0;
    tox_mock_safe_string_last[0] = '\0';

    /* self_get_name */
    tox_mock_self_get_name_size_return = 0;
    tox_mock_self_get_name_called = false;
    tox_mock_self_get_name_data[0] = '\0';

    /* self_get_status_message */
    tox_mock_self_get_status_message_size_return = 0;
    tox_mock_self_get_status_message_called = false;
    tox_mock_self_get_status_message_data[0] = '\0';

    /* self_set_status */
    tox_mock_self_set_status_called = false;
    tox_mock_last_self_set_status = 0;

    /* friend_get_connection_status */
    tox_mock_friend_get_connection_status_called = false;
    tox_mock_friend_get_connection_status_return = 0;
    tox_mock_last_friend_get_connection_status_friend_number = 0;

    /* get_all_tcp_relays */
    tox_mock_get_all_tcp_relays_called = false;
    tox_mock_get_all_tcp_relays_data[0] = '\0';

    /* self_set_typing */
    tox_mock_self_set_typing_called = false;
    tox_mock_self_set_typing_return = true;
    tox_mock_last_self_set_typing_friend_number = 0;

    /* friend_get_name */
    tox_mock_friend_get_name_called = false;
    tox_mock_friend_get_name_size_return = 0;
    tox_mock_friend_get_name_data[0] = '\0';
    tox_mock_friend_get_name_return = true;
    tox_mock_last_friend_get_name_friend_number = 0;

    /* self_get_address / self_get_public_key / self_get_secret_key */
    tox_mock_self_get_address_called = false;
    tox_mock_self_get_public_key_called = false;
    tox_mock_self_get_secret_key_called = false;

    /* file_send */
    tox_mock_file_send_called = false;
    tox_mock_file_send_return = 0;
    tox_mock_file_send_error = TOX_ERR_FILE_SEND_OK;
    tox_mock_last_file_send_friend_number = 0;
    tox_mock_last_file_send_file_size = 0;
    tox_mock_last_file_send_filename_length = 0;

    /* file_send_chunk */
    tox_mock_file_send_chunk_called = false;
    tox_mock_file_send_chunk_return = true;
    tox_mock_file_send_chunk_error = TOX_ERR_FILE_SEND_CHUNK_OK;
    tox_mock_last_file_send_chunk_friend_number = 0;
    tox_mock_last_file_send_chunk_file_number = 0;
    tox_mock_last_file_send_chunk_position = 0;
    tox_mock_last_file_send_chunk_length = 0;

    /* file_seek */
    tox_mock_file_seek_called = false;
    tox_mock_file_seek_return = true;
    tox_mock_file_seek_error = TOX_ERR_FILE_SEEK_OK;
    tox_mock_last_file_seek_friend_number = 0;
    tox_mock_last_file_seek_file_number = 0;
    tox_mock_last_file_seek_position = 0;

    /* file_get_file_id */
    tox_mock_file_get_file_id_called = false;
    tox_mock_file_get_file_id_return = true;
    tox_mock_file_get_file_id_error = TOX_ERR_FILE_GET_OK;
    tox_mock_last_file_get_file_id_friend_number = 0;
    tox_mock_last_file_get_file_id_file_number = 0;

    /* android_tox_callback_friend_message */
    tox_mock_callback_friend_message_called = false;
    tox_mock_callback_friend_message_friend_number = 0;
    tox_mock_callback_friend_message_type = TOX_MESSAGE_TYPE_NORMAL;
    tox_mock_callback_friend_message_length = 0;
    tox_mock_callback_friend_message_js1 = NULL;
    tox_mock_callback_friend_message_hash_jbuffer = NULL;
    tox_mock_callback_friend_message_timestamp = 0;
    MainActivity = NULL;
    android_tox_callback_friend_message_cb_method = NULL;
    mock_CallStaticVoidMethod_called = false;

    /* c_safe_string_from_java mocks */
    mock_jni_env_ptr = jni_mock_env();
    TrifaToxService_class = NULL;
    safe_string_method = NULL;
    mock_NewStringUTF_called = false;
    mock_NewStringUTF_return = NULL;
    mock_NewByteArray_called = false;
    mock_NewByteArray_return = NULL;
    mock_SetByteArrayRegion_called = false;
    mock_CallStaticObjectMethod_called = false;
    mock_CallStaticObjectMethod_return = NULL;
    mock_ExceptionCheck_called = false;
    mock_ExceptionCheck_return = JNI_FALSE;
    mock_ExceptionClear_called = false;
    mock_DeleteLocalRef_called = false;
}

/* =========================================================
 * Mocked Tox / helper functions
 * ========================================================= */

bool tox_friend_send_lossless_packet(
    Tox* tox,
    uint32_t friend_number,
    const uint8_t* data,
    size_t length,
    Tox_Err_Friend_Custom_Packet* error
) {
    (void)tox;

    tox_mock_lossless_called = true;
    tox_mock_last_lossless_friend_number = friend_number;
    tox_mock_last_lossless_data = data;
    tox_mock_last_lossless_length = length;

    if (error) {
        *error = tox_mock_lossless_error;
    }

    return tox_mock_lossless_return;
}

bool tox_messagev3_get_new_message_id(uint8_t* buffer) {
    tox_mock_messagev3_id_called = true;
    tox_mock_messagev3_id_last_buffer = buffer;

    if (tox_mock_messagev3_id_return && buffer) {
        memset(buffer, 0xAB, TOX_MSGV3_MSGID_LENGTH);
    }

    return tox_mock_messagev3_id_return;
}

int toxid_hex_to_bin(uint8_t* bin, const char* hex) {
    tox_mock_hex_to_bin_called = true;

    if (!hex) {
        tox_mock_last_hex[0] = '\0';
        tox_mock_last_hex_length = 0;
        return -1;
    }

    size_t len = strlen(hex);

    tox_mock_last_hex_length = len;
    snprintf(tox_mock_last_hex, sizeof(tox_mock_last_hex), "%s", hex);

    if (len != (size_t)(TOX_ADDRESS_SIZE * 2)) {
        return -1;
    }

    if (bin) {
        memset(bin, 0xAB, TOX_ADDRESS_SIZE);
    }

    return 0;
}

uint32_t tox_friend_add(
    Tox* tox,
    const uint8_t* address,
    const uint8_t* message,
    size_t length,
    Tox_Err_Friend_Add* error
) {
    (void)tox;
    (void)message;

    tox_mock_friend_add_called = true;

    if (address) {
        memcpy(tox_mock_last_friend_add_address, address, TOX_ADDRESS_SIZE);
    } else {
        memset(tox_mock_last_friend_add_address, 0, TOX_ADDRESS_SIZE);
    }

    tox_mock_last_friend_add_message_length = length;

    if (error) {
        *error = tox_mock_friend_add_error;
    }

    return tox_mock_friend_add_return;
}

int toxpk_hex_to_bin(uint8_t* bin, const char* hex) {
    tox_mock_pk_hex_to_bin_called = true;

    if (!hex) {
        tox_mock_last_pk_hex[0] = '\0';
        tox_mock_last_pk_hex_length = 0;
        return -1;
    }

    size_t len = strlen(hex);

    tox_mock_last_pk_hex_length = len;
    snprintf(tox_mock_last_pk_hex, sizeof(tox_mock_last_pk_hex), "%s", hex);

    if (len != (size_t)(TOX_PUBLIC_KEY_SIZE * 2)) {
        return -1;
    }

    if (bin) {
        memset(bin, 0xCD, TOX_PUBLIC_KEY_SIZE);
    }

    return 0;
}

uint32_t tox_friend_add_norequest(
    Tox* tox,
    const uint8_t* public_key,
    Tox_Err_Friend_Add* error
) {
    (void)tox;

    tox_mock_friend_add_norequest_called = true;

    if (public_key) {
        memcpy(tox_mock_last_friend_add_norequest_key, public_key, TOX_PUBLIC_KEY_SIZE);
    } else {
        memset(tox_mock_last_friend_add_norequest_key, 0, TOX_PUBLIC_KEY_SIZE);
    }

    if (error) {
        *error = TOX_ERR_FRIEND_ADD_OK;
    }

    return tox_mock_friend_add_norequest_return;
}

uint32_t tox_friend_send_message(
    Tox* tox,
    uint32_t friend_number,
    Tox_Message_Type type,
    const uint8_t* message,
    size_t length,
    Tox_Err_Friend_Send_Message* error
) {
    (void)tox;
    (void)message;

    tox_mock_friend_send_message_called = true;
    tox_mock_last_friend_send_message_friend_number = friend_number;
    tox_mock_last_friend_send_message_type = type;
    tox_mock_last_friend_send_message_length = length;

    if (error) {
        *error = tox_mock_friend_send_message_error;
    }

    return tox_mock_friend_send_message_return;
}

void xnet_pack_u32(uint8_t* dst, uint32_t value) {
    tox_mock_xnet_pack_u32_called = true;
    tox_mock_last_xnet_pack_u32_value = value;

    if (dst) {
        dst[0] = (uint8_t)(value & 0xFF);
        dst[1] = (uint8_t)((value >> 8) & 0xFF);
        dst[2] = (uint8_t)((value >> 16) & 0xFF);
        dst[3] = (uint8_t)((value >> 24) & 0xFF);
    }
}

bool tox_self_set_name(
    Tox* tox,
    const uint8_t* name,
    size_t length,
    Tox_Err_Set_Info* error
) {
    (void)tox;
    (void)name;

    tox_mock_self_set_name_called = true;
    tox_mock_self_set_name_length = length;

    if (error) {
        *error = tox_mock_self_set_name_error;
    }

    return tox_mock_self_set_name_return;
}

bool tox_self_set_status_message(
    Tox* tox,
    const uint8_t* status_message,
    size_t length,
    Tox_Err_Set_Info* error
) {
    (void)tox;
    (void)status_message;

    tox_mock_self_set_status_message_called = true;
    tox_mock_self_set_status_message_length = length;

    if (error) {
        *error = tox_mock_self_set_status_message_error;
    }

    return tox_mock_self_set_status_message_return;
}

bool tox_file_control(
    Tox* tox,
    uint32_t friend_number,
    uint32_t file_number,
    Tox_File_Control control,
    Tox_Err_File_Control* error
) {
    (void)tox;

    tox_mock_file_control_called = true;
    tox_mock_last_file_control_friend_number = friend_number;
    tox_mock_last_file_control_file_number = file_number;
    tox_mock_last_file_control_control = control;

    if (error) {
        *error = tox_mock_file_control_error;
    }

    return tox_mock_file_control_return;
}

bool tox_friend_delete(
    Tox* tox,
    uint32_t friend_number,
    Tox_Err_Friend_Delete* error
) {
    (void)tox;

    tox_mock_friend_delete_called = true;
    tox_mock_last_friend_delete_friend_number = friend_number;

    if (error) {
        *error = tox_mock_friend_delete_error;
    }

    return tox_mock_friend_delete_return;
}

bool tox_utils_friend_delete(Tox *tox, uint32_t friend_number, Tox_Err_Friend_Delete *error) {
    return tox_friend_delete(tox, friend_number, error);
}

void tox_friend_get_connection_ip(
    const Tox* tox,
    uint32_t friend_number,
    uint8_t* ip_str
) {
    (void)tox;

    tox_mock_friend_get_connection_ip_called = true;
    tox_mock_last_friend_get_connection_ip_friend_number = friend_number;

    if (ip_str) {
        snprintf(
            (char*)ip_str,
            TOX_MOCK_IP_BUFFER_SIZE,
            "%.*s",
            TOX_MOCK_IP_BUFFER_SIZE - 1,
            tox_mock_ip_to_write
        );
    }
}

size_t tox_self_get_name_size(const Tox* tox) {
    (void)tox;

    return tox_mock_self_get_name_size_return;
}

void tox_self_get_name(
    const Tox* tox,
    uint8_t* name
) {
    (void)tox;

    tox_mock_self_get_name_called = true;

    if (!name) {
        return;
    }

    size_t len = tox_mock_self_get_name_size_return;
    size_t data_len = strlen(tox_mock_self_get_name_data);

    for (size_t i = 0; i < len; i++) {
        if (i < data_len) {
            name[i] = (uint8_t)tox_mock_self_get_name_data[i];
        } else {
            name[i] = (uint8_t)'A';
        }
    }
}

size_t tox_self_get_status_message_size(const Tox* tox) {
    (void)tox;

    return tox_mock_self_get_status_message_size_return;
}

void tox_self_get_status_message(
    const Tox* tox,
    uint8_t* message
) {
    (void)tox;

    tox_mock_self_get_status_message_called = true;

    if (!message) {
        return;
    }

    size_t len = tox_mock_self_get_status_message_size_return;
    size_t data_len = strlen(tox_mock_self_get_status_message_data);

    for (size_t i = 0; i < len; i++) {
        if (i < data_len) {
            message[i] = (uint8_t)tox_mock_self_get_status_message_data[i];
        } else {
            message[i] = (uint8_t)'B';
        }
    }
}

/* self_set_status */
void tox_self_set_status(Tox* tox, Tox_User_Status status) {
    (void)tox;
    tox_mock_self_set_status_called = true;
    tox_mock_last_self_set_status = (int)status;
}

/* friend_get_connection_status */
Tox_Connection tox_friend_get_connection_status(const Tox* tox, uint32_t friend_number, Tox_Err_Friend_Query* error) {
    (void)tox;
    tox_mock_friend_get_connection_status_called = true;
    tox_mock_last_friend_get_connection_status_friend_number = friend_number;
    if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
    return (Tox_Connection)tox_mock_friend_get_connection_status_return;
}

/* self_set_typing */
bool tox_self_set_typing(Tox* tox, uint32_t friend_number, bool typing, Tox_Err_Set_Typing* error) {
    (void)tox;
    (void)typing;
    tox_mock_self_set_typing_called = true;
    tox_mock_last_self_set_typing_friend_number = friend_number;
    if (error) *error = TOX_ERR_SET_TYPING_OK;
    return tox_mock_self_set_typing_return;
}

/* self_get_address */
void tox_self_get_address(const Tox* tox, uint8_t* address) {
    (void)tox;
    tox_mock_self_get_address_called = true;
    if (address) {
        memset(address, 0xAA, TOX_ADDRESS_SIZE);
    }
}

/* self_get_public_key */
void tox_self_get_public_key(const Tox* tox, uint8_t* public_key) {
    (void)tox;
    tox_mock_self_get_public_key_called = true;
    if (public_key) {
        memset(public_key, 0xBB, TOX_PUBLIC_KEY_SIZE);
    }
}

/* self_get_secret_key */
void tox_self_get_secret_key(const Tox* tox, uint8_t* secret_key) {
    (void)tox;
    tox_mock_self_get_secret_key_called = true;
    if (secret_key) {
        memset(secret_key, 0xCC, TOX_SECRET_KEY_SIZE);
    }
}

/* friend_get_name_size */
size_t tox_friend_get_name_size(const Tox* tox, uint32_t friend_number, Tox_Err_Friend_Query* error) {
    (void)tox;
    (void)friend_number;
    if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
    return tox_mock_friend_get_name_size_return;
}

/* friend_get_name */
bool tox_friend_get_name(const Tox* tox, uint32_t friend_number, uint8_t* name, Tox_Err_Friend_Query* error) {
    (void)tox;
    tox_mock_friend_get_name_called = true;
    tox_mock_last_friend_get_name_friend_number = friend_number;
    if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
    if (name) {
        size_t len = tox_mock_friend_get_name_size_return;
        size_t data_len = strlen(tox_mock_friend_get_name_data);
        for (size_t i = 0; i < len; i++) {
            if (i < data_len) {
                name[i] = (uint8_t)tox_mock_friend_get_name_data[i];
            } else {
                name[i] = (uint8_t)'A';
            }
        }
    }
    return tox_mock_friend_get_name_return;
}

/* get_all_tcp_relays - adjust function name to match toxutil.h */
char* tox_utils_get_all_tcp_relays(const Tox* tox) {
    (void)tox;
    tox_mock_get_all_tcp_relays_called = true;
    return tox_mock_get_all_tcp_relays_data;
}

void tox_get_all_tcp_relays(const Tox* tox, char* report) {
    (void)tox;
    tox_mock_get_all_tcp_relays_called = true;

    if (report) {
        snprintf(report, 4096, "%s", tox_mock_get_all_tcp_relays_data);
    }
}

/* file_send */
uint32_t tox_file_send(
    Tox* tox,
    uint32_t friend_number,
    uint32_t kind,
    uint64_t file_size,
    const uint8_t* file_id,
    const uint8_t* filename,
    size_t filename_length,
    TOX_ERR_FILE_SEND* error
) {
    (void)tox;
    (void)kind;
    (void)file_id;
    (void)filename;

    tox_mock_file_send_called = true;
    tox_mock_last_file_send_friend_number = friend_number;
    tox_mock_last_file_send_file_size = file_size;
    tox_mock_last_file_send_filename_length = filename_length;

    if (error) {
        *error = tox_mock_file_send_error;
    }

    return tox_mock_file_send_return;
}

/* file_send_chunk */
bool tox_file_send_chunk(
    Tox* tox,
    uint32_t friend_number,
    uint32_t file_number,
    uint64_t position,
    const uint8_t* data,
    size_t length,
    TOX_ERR_FILE_SEND_CHUNK* error
) {
    (void)tox;
    (void)data;

    tox_mock_file_send_chunk_called = true;
    tox_mock_last_file_send_chunk_friend_number = friend_number;
    tox_mock_last_file_send_chunk_file_number = file_number;
    tox_mock_last_file_send_chunk_position = position;
    tox_mock_last_file_send_chunk_length = length;

    if (error) {
        *error = tox_mock_file_send_chunk_error;
    }

    return tox_mock_file_send_chunk_return;
}

/* file_seek */
bool tox_file_seek(
    Tox* tox,
    uint32_t friend_number,
    uint32_t file_number,
    uint64_t position,
    TOX_ERR_FILE_SEEK* error
) {
    (void)tox;

    tox_mock_file_seek_called = true;
    tox_mock_last_file_seek_friend_number = friend_number;
    tox_mock_last_file_seek_file_number = file_number;
    tox_mock_last_file_seek_position = position;

    if (error) {
        *error = tox_mock_file_seek_error;
    }

    return tox_mock_file_seek_return;
}

/* file_get_file_id */
bool tox_file_get_file_id(
    const Tox* tox,
    uint32_t friend_number,
    uint32_t file_number,
    uint8_t* file_id,
    TOX_ERR_FILE_GET* error
) {
    (void)tox;

    tox_mock_file_get_file_id_called = true;
    tox_mock_last_file_get_file_id_friend_number = friend_number;
    tox_mock_last_file_get_file_id_file_number = file_number;

    if (file_id) {
        memset(file_id, 0xDD, TOX_FILE_ID_LENGTH);
    }

    if (error) {
        *error = tox_mock_file_get_file_id_error;
    }

    return tox_mock_file_get_file_id_return;
}
