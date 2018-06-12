BASE_NAME = libfilteraudio
VERSION = 0.0.0
PREFIX ?= /usr/local
LIBDIR ?= lib
INCLUDEDIR ?= include

STATIC_LIB = $(BASE_NAME).a
PC_FILE = filteraudio.pc

SRC = $(wildcard aec/*.c) $(wildcard agc/*.c) $(wildcard ns/*.c) $(wildcard other/*.c) $(wildcard zam/*.c) $(wildcard vad/*.c) filter_audio.c
OBJ = $(SRC:.c=.o)
HEADER = filter_audio.h
TARGET_ONLY = NO

# Check on which system we are running
UNAME_S = $(shell uname -s)
ifeq ($(UNAME_S), Darwin)
    SHARED_EXT = dylib
    TARGET = $(BASE_NAME).$(VERSION).$(SHARED_EXT)
    SHARED_LIB = $(BASE_NAME).$(shell echo $(VERSION) | rev | cut -d "." -f 1 | rev).$(SHARED_EXT)
	LDFLAGS += -lm -lpthread
    LDFLAGS += -Wl,-install_name,$(SHARED_LIB)
else ifneq (, $(shell echo $(UNAME_S) | grep -E 'MSYS|MINGW|CYGWIN'))
    SHARED_EXT = dll
    TARGET = $(BASE_NAME).$(SHARED_EXT)
    TARGET_ONLY = YES
    NO_STATIC = 1
	LDFLAGS += -lm
    LDFLAGS += -Wl,--out-implib,$(TARGET).a
else
    SHARED_EXT = so
    TARGET = $(BASE_NAME).$(SHARED_EXT).$(VERSION)
    SHARED_LIB = $(BASE_NAME).$(SHARED_EXT).$(shell echo $(VERSION) | rev | cut -d "." -f 1 | rev)
    CFLAGS += -O3 -g
	LDFLAGS += -lm
    LDFLAGS += -Wl,-soname=$(SHARED_LIB)
endif


all: $(TARGET)

$(TARGET): $(OBJ)
	@echo "  LD    $@"
	$(CC) $(LDFLAGS) -shared -o $@ $^
	@if [ "$(NO_STATIC)" != "1" ]; then \
		echo "  AR    $(STATIC_LIB)" ;\
		ar rcs $(STATIC_LIB) $(OBJ) ;\
	fi

%.o: %.c
	@echo "  CC    $@"
	@$(CC) $(CFLAGS) -fPIC -c -o $@ $<

clean:
	rm -f $(TARGET) $(STATIC_LIB) $(OBJ)

.PHONY: all clean
