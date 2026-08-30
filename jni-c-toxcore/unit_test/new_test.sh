#!/bin/sh

set -e

if [ $# -ne 2 ]; then
    echo "usage: $0 short_name full_c_symbol"
    echo
    echo "example:"
    echo "  $0 friend_add Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add"
    exit 1
fi

NAME="$1"
SYMBOL="$2"

GROUPS="test_groups.txt"
TESTFILE="test_${NAME}.c"

if ! printf '%s' "$NAME" | grep -Eq '^[A-Za-z_][A-Za-z0-9_]*$'; then
    echo "error: short_name must be a valid C identifier fragment: $NAME"
    exit 1
fi

if [ -z "$SYMBOL" ]; then
    echo "error: symbol must not be empty"
    exit 1
fi

if grep -Eq "^[[:space:]]*${NAME}([[:space:]]|$)" "$GROUPS" 2>/dev/null; then
    echo "error: $NAME already exists in $GROUPS"
    exit 1
fi

if [ -e "$TESTFILE" ]; then
    echo "error: $TESTFILE already exists"
    exit 1
fi

echo "$NAME $SYMBOL" >> "$GROUPS"

cat > "$TESTFILE" <<EOF
#include "harness.h"

#define SUT $SYMBOL

/*
    TODO:

    1. Build/generate the extracted function:

         make function_${NAME}.c

    2. Open:

         function_${NAME}.c

    3. Copy the exact function prototype into this file.

       Example:

         JNIEXPORT jlong JNICALL SUT(JNIEnv* env, jobject thiz, ...);

    4. Call the function from your test and add assertions.
*/

static void t_example(void) {
    TEST_BEGIN("t_example");

    jni_mock_reset();
    tox_mock_reset();

    NOTE("TODO: call the extracted function and add assertions");

    TEST_ASSERT(1 == 1);

    TEST_END();
}

void run_${NAME}_tests(void) {
    SUITE_BEGIN("${NAME}: basic");
    t_example();
    SUITE_END();
}
EOF

echo "Created: $TESTFILE"
echo "Registered: $NAME in $GROUPS"
echo
echo "Next steps:"
echo "  1. Edit $TESTFILE"
echo "  2. Add the correct function prototype"
echo "  3. Call the function and add assertions"
echo "  4. Run: make test"
