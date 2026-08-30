/* Generated from test_groups.txt. Do not edit. */
#include "harness.h"

void run_friend_send_lossless_packet_tests(void);
void run_messagev3_get_new_message_id_tests(void);
void run_friend_add_tests(void);
void run_friend_add_norequest_tests(void);
void run_self_set_name_tests(void);
void run_self_set_status_message_tests(void);
void run_friend_send_message_tests(void);
void run_file_control_tests(void);
void run_friend_delete_tests(void);
void run_friend_get_connection_ip_tests(void);
void run_self_get_name_tests(void);
void run_self_get_status_message_tests(void);
void run_messagev3_friend_send_message_tests(void);
void run_self_set_status_tests(void);
void run_friend_get_connection_status_tests(void);
void run_self_get_name_size_tests(void);
void run_self_get_status_message_size_tests(void);
void run_get_all_tcp_relays_tests(void);
void run_self_set_typing_tests(void);
void run_friend_get_name_tests(void);
void run_file_send_tests(void);
void run_file_send_chunk_tests(void);
void run_file_seek_tests(void);
void run_file_get_file_id_tests(void);

int main(void) {
    printf("\n");
    printf("  ================================================================\n");
    printf("   JNI-C-TOXCORE UNIT TESTS\n");
    printf("   Source: ../jni-c-toxcore.c (extracted functions)\n");
    printf("   Verifies: JNI boundary behavior and security invariants\n");
    printf("  ================================================================\n");

    run_friend_send_lossless_packet_tests();
    run_messagev3_get_new_message_id_tests();
    run_friend_add_tests();
    run_friend_add_norequest_tests();
    run_self_set_name_tests();
    run_self_set_status_message_tests();
    run_friend_send_message_tests();
    run_file_control_tests();
    run_friend_delete_tests();
    run_friend_get_connection_ip_tests();
    run_self_get_name_tests();
    run_self_get_status_message_tests();
    run_messagev3_friend_send_message_tests();
    run_self_set_status_tests();
    run_friend_get_connection_status_tests();
    run_self_get_name_size_tests();
    run_self_get_status_message_size_tests();
    run_get_all_tcp_relays_tests();
    run_self_set_typing_tests();
    run_friend_get_name_tests();
    run_file_send_tests();
    run_file_send_chunk_tests();
    run_file_seek_tests();
    run_file_get_file_id_tests();

    harness_print_report("jni-c-toxcore unit tests");

    return harness_result();
}
