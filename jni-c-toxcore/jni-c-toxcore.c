/**
 * [TRIfA], JNI part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */


#include <ctype.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>
#include <dirent.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <unistd.h>

#include <fcntl.h>
#include <errno.h>

#include <sodium/utils.h>
#include <tox/tox.h>
#include <tox/toxav.h>

#include <linux/videodev2.h>
#include <vpx/vpx_image.h>
#include <sys/mman.h>

// ------- Android/JNI stuff -------
// #include <android/log.h>
#include <jni.h>
// ------- Android/JNI stuff -------

#define CLEAR(x) memset(&(x), 0, sizeof(x))
#define c_sleep(x) usleep(1000*x)

#define CURRENT_LOG_LEVEL 9 // 0 -> error, 1 -> warn, 2 -> info, 9 -> debug
#define MAX_LOG_LINE_LENGTH 1000
#define MAX_FULL_PATH_LENGTH 1000

const char *savedata_filename = "savedata.tox";
const char *savedata_tmp_filename = "savedata.tox.tmp";
int tox_loop_running = 1;
TOX_CONNECTION my_connection_status = TOX_CONNECTION_NONE;



typedef struct DHT_node {
    const char *ip;
    uint16_t port;
    const char key_hex[TOX_PUBLIC_KEY_SIZE*2 + 1];
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
} DHT_node;







void dbg(int level, const char *fmt, ...)
{
	char *level_and_format = NULL;
	char *fmt_copy = NULL;
	char *log_line_str = NULL;

	if (fmt == NULL)
	{
		return;
	}

	if (strlen(fmt) < 1)
	{
		return;
	}

	if ((level < 0) || (level > 9))
	{
		level = 0;
	}

	level_and_format = malloc(strlen(fmt) + 3);

	if (!level_and_format)
	{
		return;
	}

	fmt_copy = level_and_format + 2;
	strcpy(fmt_copy, fmt);
	level_and_format[1] = ':';
	if (level == 0)
	{
		level_and_format[0] = 'E';
	}
	else if (level == 1)
	{
		level_and_format[0] = 'W';
	}
	else if (level == 2)
	{
		level_and_format[0] = 'I';
	}
	else
	{
		level_and_format[0] = 'D';
	}

	if (level <= CURRENT_LOG_LEVEL)
	{
		log_line_str = malloc((size_t)MAX_LOG_LINE_LENGTH);
		va_list ap;
		va_start(ap, fmt);
		vsnprintf(log_line_str, (size_t)MAX_LOG_LINE_LENGTH, level_and_format, ap);
		// send "log_line_str" to android
		android_logger(log_line_str);
		va_end(ap);
		free(log_line_str);
	}

	if (level_and_format)
	{
		free(level_and_format);
	}
}


Tox *create_tox()
{
	Tox *tox;
	struct Tox_Options options;

	tox_options_default(&options);

	uint16_t tcp_port = 33776; // act as TCP relay

	options.ipv6_enabled = true;
	options.udp_enabled = true;
	options.local_discovery_enabled = true;
	options.hole_punching_enabled = true;
	options.tcp_port = tcp_port;

	char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
	snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);


    FILE *f = fopen(full_path_filename, "rb");
    if (f)
	{
        fseek(f, 0, SEEK_END);
        long fsize = ftell(f);
        fseek(f, 0, SEEK_SET);

        uint8_t *savedata = malloc(fsize);

        size_t dummy = fread(savedata, fsize, 1, f);
		if (dummy < 1)
		{
			dbg(0, "reading savedata failed\n");
		}
        fclose(f);

        options.savedata_type = TOX_SAVEDATA_TYPE_TOX_SAVE;
        options.savedata_data = savedata;
        options.savedata_length = fsize;

        tox = tox_new(&options, NULL);

        free((void *)savedata);
    }
	else
	{
        tox = tox_new(&options, NULL);
    }

	bool local_discovery_enabled = tox_options_get_local_discovery_enabled(&options);
	dbg(9, "local discovery enabled = %d\n", (int)local_discovery_enabled);

	free(full_path_filename);

    return tox;
}


void update_savedata_file(const Tox *tox)
{
    size_t size = tox_get_savedata_size(tox);
    char *savedata = malloc(size);
    tox_get_savedata(tox, (uint8_t *)savedata);

	char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
	snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);

	char *full_path_filename_tmp = malloc(MAX_FULL_PATH_LENGTH);
	snprintf(full_path_filename_tmp, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_tmp_filename);

    FILE *f = fopen(full_path_filename_tmp, "wb");
    fwrite(savedata, size, 1, f);
    fclose(f);

    rename(full_path_filename_tmp, full_path_filename);

	free(full_path_filename);
	free(full_path_filename_tmp);
    free(savedata);
}


int bin_id_to_string(const char *bin_id, size_t bin_id_size, char *output, size_t output_size)
{
    if (bin_id_size != TOX_ADDRESS_SIZE || output_size < (TOX_ADDRESS_SIZE * 2 + 1))
    {
        return -1;
    }

    size_t i;
    for (i = 0; i < TOX_ADDRESS_SIZE; ++i)
    {
        snprintf(&output[i * 2], output_size - (i * 2), "%02X", bin_id[i] & 0xff);
    }

	return 0;
}

void bootstrap(Tox *tox)
{
    DHT_node nodes[] =
    {
        {"178.62.250.138",             33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B", {0}},
        {"2a03:b0c0:2:d0::16:1",       33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B", {0}},
        {"tox.zodiaclabs.org",         33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074", {0}},
        {"163.172.136.118",            33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
        {"2001:bc8:4400:2100::1c:50f", 33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
        {"128.199.199.197",            33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09", {0}},
        {"2400:6180:0:d0::17a:a001",   33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09", {0}},
        {"biribiri.org",               33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67", {0}}
    };

    for (size_t i = 0; i < sizeof(nodes)/sizeof(DHT_node); i ++) {
        sodium_hex2bin(nodes[i].key_bin, sizeof(nodes[i].key_bin),
                       nodes[i].key_hex, sizeof(nodes[i].key_hex)-1, NULL, NULL, NULL);
        tox_bootstrap(tox, nodes[i].ip, nodes[i].port, nodes[i].key_bin, NULL);
    }
}

// fill string with toxid in upper case hex.
// size of toxid_str needs to be: [TOX_ADDRESS_SIZE*2 + 1] !!
void get_my_toxid(Tox *tox, char *toxid_str)
{
    uint8_t tox_id_bin[TOX_ADDRESS_SIZE];
    tox_self_get_address(tox, tox_id_bin);

	char tox_id_hex_local[TOX_ADDRESS_SIZE*2 + 1];
    sodium_bin2hex(tox_id_hex_local, sizeof(tox_id_hex_local), tox_id_bin, sizeof(tox_id_bin));

    for (size_t i = 0; i < sizeof(tox_id_hex_local)-1; i ++)
	{
        tox_id_hex_local[i] = toupper(tox_id_hex_local[i]);
    }

	snprintf(toxid_str, (size_t)(TOX_ADDRESS_SIZE*2 + 1), "%s", (const char*)tox_id_hex_local);
}

void print_tox_id(Tox *tox)
{
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
	get_my_toxid(tox, tox_id_hex);

	dbg(2, "MyToxID:%s\n", tox_id_hex);
}

void self_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data)
{
    switch (connection_status)
	{
        case TOX_CONNECTION_NONE:
            dbg(2, "Offline\n");
			my_connection_status = TOX_CONNECTION_NONE;
            break;
        case TOX_CONNECTION_TCP:
            dbg(2, "Online, using TCP\n");
			my_connection_status = TOX_CONNECTION_TCP;
            break;
        case TOX_CONNECTION_UDP:
            dbg(2, "Online, using UDP\n");
			my_connection_status = TOX_CONNECTION_UDP;
            break;
    }
}

void _main_()
{
	Tox *tox = create_tox();

    const char *name = "[TRIfA]";
    tox_self_set_name(tox, (uint8_t *)name, strlen(name), NULL);

    const char *status_message = "This is [TRIfA]";
	tox_self_set_status_message(tox, (uint8_t *)status_message, strlen(status_message), NULL);

	bootstrap(tox);
	print_tox_id(tox);

	tox_callback_self_connection_status(tox, self_connection_status_cb);
	update_savedata_file(tox);

	long long unsigned int cur_time = time(NULL);
	uint8_t off = 1;
	while (1)
	{
        tox_iterate(tox, NULL);
        usleep(tox_iteration_interval(tox) * 1000);
        if (tox_self_get_connection_status(tox) && off)
		{
            dbg(2, "Tox online, took %llu seconds\n", time(NULL) - cur_time);
            off = 0;
			break;
        }
        c_sleep(20);
	}

	tox_loop_running = 1;

    while (tox_loop_running)
    {
        tox_iterate(tox, NULL);
        usleep(tox_iteration_interval(tox) * 1000);
	}

	// does not reach here now!
	tox_kill(tox);
}


// ------------- JNI -------------
// ------------- JNI -------------
// ------------- JNI -------------


JNIEnv *jnienv;
JavaVM *cachedJVM = NULL;
jobject *android_activity;

char *app_data_dir = NULL;
jclass MainActivity = NULL;
jmethodID logger_method = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	JNIEnv *env_this;
	cachedJVM = jvm;
	if ((*jvm)->GetEnv(jvm, (void**) &env_this, JNI_VERSION_1_6))
	{
		// dbg(0,"Could not get JVM\n");
		return JNI_ERR;
	}

	// dbg(0,"++ Found JVM ++\n");
	return JNI_VERSION_1_6;
}

JNIEnv* jni_getenv()
{
	JNIEnv* env_this;
	(*cachedJVM)->GetEnv(cachedJVM, (void**) &env_this, JNI_VERSION_1_6);
	return env_this;
}


JNIEnv *AttachJava()
{
	JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
	JNIEnv *java;
	(*cachedJVM)->AttachCurrentThread(cachedJVM, &java, &args);
	return java;
}

int android_find_class_global(char *name, jclass *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->FindClass(jnienv2, name);
	if (!*ret)
	{
		return 0;
	}

	*ret = (*jnienv2)->NewGlobalRef(jnienv2, *ret);
	return 1;
}

int android_find_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->GetMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		return 0;
	}

	return 1;
}


int android_find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->GetStaticMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		return 0;
	}

	return 1;
}


void android_logger(const char* logtext)
{
	if ((MainActivity) && (logger_method) && (logtext))
	{
		if (strlen(logtext) > 0)
		{
			JNIEnv *jnienv2;
			jnienv2 = jni_getenv();
			jstring js2 = NULL;

			js2 = (*jnienv2)->NewStringUTF(jnienv2, logtext);
			(*jnienv2)->CallVoidMethod(jnienv2, MainActivity, logger_method, js2);
			(*jnienv2)->DeleteLocalRef(jnienv2, js2);
		}
	}
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_init(JNIEnv* env, jobject thiz, jobject datadir)
{
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// jnienv = env;
	// dbg(0,"jnienv=%p\n", env);
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!

	if (MainActivity == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/trifa/MainActivity", &MainActivity))
		{
			MainActivity = NULL;
		}
	}

	if (logger_method == NULL)
	{
		android_find_method(MainActivity, "logger(String text)", "(Ljava/lang/String;)V", &logger_method);
	}

	dbg(9, "Logging test ---***---");

	int thread_id = gettid();
	dbg(9, "THREAD ID=%d\n", thread_id);

	s =  (*env)->GetStringUTFChars(env, datadir, NULL);
	app_data_dir = strdup(s);
	(*env)->ReleaseStringUTFChars(env, datadir, s);

}


// ------------------------------------------------------------------------------------------------
// taken from:
// https://github.com/googlesamples/android-ndk/blob/master/hello-jni/app/src/main/cpp/hello-jni.c
// ------------------------------------------------------------------------------------------------

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_getNativeLibAPI(JNIEnv* env, jobject thiz)
{
#if defined(__arm__)
    #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif

    return (*env)->NewStringUTF(env, "Native Code Compiled with ABI:" ABI "");
}


// ------------- JNI -------------
// ------------- JNI -------------
// ------------- JNI -------------


