#include "harness.h"

void run_friend_send_lossless_packet_tests(void);
void run_messagev3_get_new_message_id_tests(void);

int main(void) {
    printf("\n");
    printf("  ================================================================\n");
    printf("   JNI-C-TOXCORE UNIT TESTS\n");
    printf("   Source: ../jni-c-toxcore.c (extracted functions)\n");
    printf("   Verifies: JNI boundary behavior and vulnerability docs\n");
    printf("  ================================================================\n");

    run_friend_send_lossless_packet_tests();
    run_messagev3_get_new_message_id_tests();

    harness_print_report("jni-c-toxcore unit tests");

    return harness_result();
}
