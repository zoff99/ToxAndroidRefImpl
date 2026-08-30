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

EXTRA_CFLAGS ?=

CFLAGS += -I. -include harness.h -Wno-unused-parameter $(EXTRA_CFLAGS)

REAL_SRC := ../jni-c-toxcore.c
GROUPS := test_groups.txt

# Read test groups from test_groups.txt.
TESTS := $(shell awk '$$1 !~ /^#/ && NF >= 2 { print $$1 }' $(GROUPS) 2>/dev/null)

FUNC_SRCS := $(addprefix function_,$(addsuffix .c,$(TESTS)))
TEST_SRCS := $(addprefix test_,$(addsuffix .c,$(TESTS)))

RUNNER := runner_generated.c
UNIT_BIN := unit_tests

ASAN_FLAGS := -fsanitize=address -fno-omit-frame-pointer -O1 -g

# symbols.mk is generated from test_groups.txt.
# It creates lines like:
#   SYMBOL_friend_add := Java_com_...
-include symbols.mk

.PHONY: all test extract clean asan asan-build new-test

all: $(UNIT_BIN)

# -----------------------------------------------------------
# Generate SYMBOL_<name> variables from test_groups.txt.
# -----------------------------------------------------------
symbols.mk: $(GROUPS)
> awk '$$1 !~ /^#/ && NF >= 2 { printf "SYMBOL_%s := %s\n", $$1, $$2 }' $(GROUPS) > $@

extract: $(FUNC_SRCS)

# -----------------------------------------------------------
# Generate function_<name>.c from the real source file.
# -----------------------------------------------------------
define extract_rule
function_$(1).c: $(REAL_SRC) extract_function.py symbols.mk
> $(PYTHON) extract_function.py $(REAL_SRC) '$(SYMBOL_$(1))' > $$@.tmp
> mv $$@.tmp $$@
endef

$(foreach t,$(TESTS),$(eval $(call extract_rule,$(t))))

# -----------------------------------------------------------
# Generate runner_generated.c from test_groups.txt.
# -----------------------------------------------------------
$(RUNNER): generate_runner.sh $(GROUPS)
> sh ./generate_runner.sh $(GROUPS) > $@

# -----------------------------------------------------------
# Build one unified unit test runner.
# -----------------------------------------------------------
$(UNIT_BIN): $(RUNNER) harness.c $(TEST_SRCS) $(FUNC_SRCS)
> $(CC) $(CFLAGS) -o $@ $^

test: $(UNIT_BIN)
> ./$(UNIT_BIN)

# Show only failed tests with suite name
failures: $(UNIT_BIN)
> ./$(UNIT_BIN) 2>&1 | grep -e 'SUITE:' -e '\[FAIL\]' 2>&1| sed -e 's# |  SUITE:#SUITE:#' | grep --no-group-separator -B1 '\[FAIL\]' || true


# -----------------------------------------------------------
# ASAN targets.
# -----------------------------------------------------------
asan:
> $(MAKE) clean
> $(MAKE) EXTRA_CFLAGS="$(ASAN_FLAGS)" test

asan-build:
> $(MAKE) clean
> $(MAKE) EXTRA_CFLAGS="$(ASAN_FLAGS)" all

# -----------------------------------------------------------
# Create a new test group skeleton.
#
# Example:
#   make new-test NAME=friend_add SYMBOL=Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add
# -----------------------------------------------------------
new-test:
> @sh ./new_test.sh "$(NAME)" "$(SYMBOL)"

clean:
> rm -f $(UNIT_BIN) $(FUNC_SRCS) runner_generated.c symbols.mk *.tmp
