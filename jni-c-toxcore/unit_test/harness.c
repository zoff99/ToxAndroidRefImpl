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

static struct JNINativeInterface_ jni_table = {
    mock_GetStringUTFChars,
    mock_ReleaseStringUTFChars,
    mock_GetByteArrayElements,
    mock_ReleaseByteArrayElements,
    mock_GetArrayLength,
    mock_GetDirectBufferAddress,
    mock_GetDirectBufferCapacity
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
 * Tox mock
 * ========================================================= */

static int tox_mock_dummy_tox;

void* tox_global = NULL;

bool tox_mock_lossless_called = false;
uint32_t tox_mock_last_lossless_friend_number = 0;
const uint8_t* tox_mock_last_lossless_data = NULL;
size_t tox_mock_last_lossless_length = 0;
uint32_t tox_mock_lossless_return = 1;
TOX_ERR_FRIEND_CUSTOM_PACKET tox_mock_lossless_error = TOX_ERR_FRIEND_CUSTOM_PACKET_OK;

bool tox_mock_messagev3_id_called = false;
bool tox_mock_messagev3_id_return = true;
uint8_t* tox_mock_messagev3_id_last_buffer = NULL;

void tox_mock_reset(void) {
    tox_global = &tox_mock_dummy_tox;

    tox_mock_lossless_called = false;
    tox_mock_last_lossless_friend_number = 0;
    tox_mock_last_lossless_data = NULL;
    tox_mock_last_lossless_length = 0;
    tox_mock_lossless_return = 1;
    tox_mock_lossless_error = TOX_ERR_FRIEND_CUSTOM_PACKET_OK;

    tox_mock_messagev3_id_called = false;
    tox_mock_messagev3_id_return = true;
    tox_mock_messagev3_id_last_buffer = NULL;
}

uint32_t tox_friend_send_lossless_packet(
    void* tox,
    uint32_t friend_number,
    const uint8_t* data,
    size_t length,
    TOX_ERR_FRIEND_CUSTOM_PACKET* error
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
