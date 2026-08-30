#!/bin/sh

set -e

GROUPS="${1:-test_groups.txt}"

cat <<HDR
/* Generated from $GROUPS. Do not edit. */
#include "harness.h"

HDR

awk '
    $1 !~ /^#/ && NF >= 2 {
        printf "void run_%s_tests(void);\n", $1
    }
' "$GROUPS"

cat <<MAIN

int main(void) {
    printf("\n");
    printf("  ================================================================\n");
    printf("   JNI-C-TOXCORE UNIT TESTS\n");
    printf("   Source: ../jni-c-toxcore.c (extracted functions)\n");
    printf("   Verifies: JNI boundary behavior and security invariants\n");
    printf("  ================================================================\n");

MAIN

awk '
    $1 !~ /^#/ && NF >= 2 {
        printf "    run_%s_tests();\n", $1
    }
' "$GROUPS"

cat <<END

    harness_print_report("jni-c-toxcore unit tests");

    return harness_result();
}
END
