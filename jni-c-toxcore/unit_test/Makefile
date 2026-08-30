# jni-c-toxcore/unit_test/Makefile
#
# Simple no-framework unit test harness.
#
# Requires GNU Make because of .RECIPEPREFIX.
# If your Make does not support .RECIPEPREFIX, remove that line
# and replace leading '>' characters in recipes with real tabs.

.RECIPEPREFIX = >

PYTHON ?= python3
CC ?= gcc

CFLAGS := -std=c11 -Wall -Wextra -g

# Extra flags can be injected from the command line.
# Used by the ASAN target.
EXTRA_CFLAGS ?=

CFLAGS += -I. -include harness.h -Wno-unused-parameter $(EXTRA_CFLAGS)

REAL_SRC := ../jni-c-toxcore.c

# -----------------------------------------------------------
# Test groups.
#
# For each name xyz:
#   - function_xyz.c is extracted from ../jni-c-toxcore.c
#   - test_xyz.c must exist and define run_xyz_tests()
# -----------------------------------------------------------
TESTS := \
    friend_send_lossless_packet \
    messagev3_get_new_message_id

# Full C symbol names to extract from ../jni-c-toxcore.c
SYMBOL_friend_send_lossless_packet := \
    Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1lossless_1packet

SYMBOL_messagev3_get_new_message_id := \
    Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1get_1new_1message_1id

FUNC_SRCS := $(addprefix function_,$(addsuffix .c,$(TESTS)))
TEST_SRCS := $(addprefix test_,$(addsuffix .c,$(TESTS)))

UNIT_BIN := unit_tests

ASAN_FLAGS := -fsanitize=address -fno-omit-frame-pointer -O1 -g

.PHONY: all test extract clean asan asan-build

all: $(UNIT_BIN)

extract: $(FUNC_SRCS)

# -----------------------------------------------------------
# Generate function_<name>.c from the real source file.
# -----------------------------------------------------------
define extract_rule
function_$(1).c: $(REAL_SRC) extract_function.py
> $(PYTHON) extract_function.py $(REAL_SRC) '$(SYMBOL_$(1))' > $$@.tmp
> mv $$@.tmp $$@
endef

$(foreach t,$(TESTS),$(eval $(call extract_rule,$(t))))

# -----------------------------------------------------------
# Build one unified unit test runner.
# -----------------------------------------------------------
$(UNIT_BIN): runner.c harness.c $(TEST_SRCS) $(FUNC_SRCS)
> $(CC) $(CFLAGS) -o $@ $^

test: $(UNIT_BIN)
> ./$(UNIT_BIN)

# -----------------------------------------------------------
# ASAN targets.
#
#   make asan       clean + build with ASAN + run tests
#   make asan-build clean + build with ASAN only
# -----------------------------------------------------------
asan:
> $(MAKE) clean
> $(MAKE) EXTRA_CFLAGS="$(ASAN_FLAGS)" test

asan-build:
> $(MAKE) clean
> $(MAKE) EXTRA_CFLAGS="$(ASAN_FLAGS)" all

clean:
> rm -f $(UNIT_BIN) $(FUNC_SRCS) *.tmp
