/**
 * [TRIfA], JNI part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2019 Zoff <zoff@zoff.cc>
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

#include <tox/tox.h>

#ifdef TOX_HAVE_TOXUTIL
#include <tox/toxutil.h>
#endif

#include <tox/toxav.h>
#include <tox/toxencryptsave.h>

#include <sodium/utils.h>

#include <pthread.h>

#include <linux/videodev2.h>
#include <vpx/vpx_image.h>
#include <sys/mman.h>

// #define AV_MEDIACODEC 1

#ifdef AV_MEDIACODEC
#include <libavcodec/jni.h>
#endif

// HINT: it may not be working properly
#define USE_ECHO_CANCELLATION 1

// ------- Android/JNI stuff -------
// #include <android/log.h>
#include <jni.h>
#include "coffeecatch.h"
#include "coffeejni.h"
#ifdef USE_ECHO_CANCELLATION
#include "filter_audio/filter_audio.h"
#endif
// ------- Android/JNI stuff -------


// ----------- version -----------
// ----------- version -----------
#define VERSION_MAJOR 0
#define VERSION_MINOR 99
#define VERSION_PATCH 34
static const char global_version_string[] = "0.99.34";
// ----------- version -----------
// ----------- version -----------





/*
 * ------------------------------------------------------------
 * TOXCORE compatibility layer --------------------------------
 * ------------------------------------------------------------
 */
#ifndef TOXCOMPAT_H_
#define TOXCOMPAT_H_

#if TOX_VERSION_IS_API_COMPATIBLE(0, 2, 0)
#else
// no need to fake the function
#endif

#endif
/*
 * ------------------------------------------------------------
 * TOXCORE compatibility layer --------------------------------
 * ------------------------------------------------------------
 */














#define CLEAR(x) memset(&(x), 0, sizeof(x))
#define c_sleep(x) usleep(1000*x)

#define max(a,b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a > _b ? _a : _b; })

#define min(a,b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a < _b ? _a : _b; })


#define CURRENT_LOG_LEVEL 9 // 0 -> error, 1 -> warn, 2 -> info, 9 -> debug
#define MAX_LOG_LINE_LENGTH 1000
#define MAX_FULL_PATH_LENGTH 1000

#define DEFAULT_FPS_SLEEP_MS 160 // default video fps (sleep in msecs. !!)

typedef struct
{
    bool incoming;
    uint32_t state;
    uint32_t audio_bit_rate;
    uint32_t video_bit_rate;
    // pthread_mutex_t arb_mutex[1];
} CallControl;


const char *savedata_filename = "savedata.tox";
const char *savedata_tmp_filename = "savedata.tox.tmp";
int tox_loop_running = 1;
int toxav_video_thread_stop = 0;
int toxav_audio_thread_stop = 0;
int toxav_iterate_thread_stop = 0;

TOX_CONNECTION my_connection_status = TOX_CONNECTION_NONE;
Tox *tox_global = NULL;
ToxAV *tox_av_global = NULL;
bool global_toxav_valid = false;
CallControl mytox_CC;
pthread_t tid[3]; // 0 -> toxav_iterate thread, 1 -> video iterate thread, 2 -> audio iterate thread

#ifdef USE_ECHO_CANCELLATION
Filter_Audio *filteraudio = NULL;
#endif

uint8_t filteraudio_active = 1;
uint8_t filteraudio_incompatible_1 = 1;
uint8_t filteraudio_incompatible_2 = 1;


// ----- JNI stuff -----
JNIEnv *jnienv;
JavaVM *cachedJVM = NULL;
jobject *android_activity;

char *app_data_dir = NULL;
jclass MainActivity = NULL;
jclass TrifaToxService_class = NULL;
jmethodID logger_method = NULL;
jmethodID safe_string_method = NULL;

uint8_t *video_buffer_1 = NULL;
uint8_t *video_buffer_1_u = NULL;
uint8_t *video_buffer_1_v = NULL;
long video_buffer_1_size = 0;
int video_buffer_1_width = 0;
int video_buffer_1_height = 0;
int video_buffer_1_y_size = 0;
int video_buffer_1_u_size = 0;
int video_buffer_1_v_size = 0;

uint8_t *video_buffer_2 = NULL;
uint8_t *video_buffer_2_u = NULL;
uint8_t *video_buffer_2_v = NULL;
long video_buffer_2_size = 0;
int video_buffer_2_y_size = 0;
int video_buffer_2_u_size = 0;
int video_buffer_2_v_size = 0;

uint8_t *audio_buffer_pcm_1 = NULL;
long audio_buffer_pcm_1_size = 0;

uint8_t *audio_buffer_pcm_2 = NULL;
long audio_buffer_pcm_2_size = 0;

uint32_t recording_samling_rate = 48000;
int16_t global_audio_frame_duration_ms = 60;
uint8_t global_av_call_active = 0;


// -------- _callbacks_ --------
jmethodID android_tox_callback_self_connection_status_cb_method = NULL;
jmethodID android_tox_callback_friend_name_cb_method = NULL;
jmethodID android_tox_callback_friend_status_message_cb_method = NULL;
jmethodID android_tox_callback_friend_lossless_packet_cb_method = NULL;
jmethodID android_tox_callback_friend_status_cb_method = NULL;
jmethodID android_tox_callback_friend_connection_status_cb_method = NULL;
jmethodID android_tox_callback_friend_typing_cb_method = NULL;
jmethodID android_tox_callback_friend_read_receipt_cb_method = NULL;
jmethodID android_tox_callback_friend_request_cb_method = NULL;
jmethodID android_tox_callback_friend_message_cb_method = NULL;
jmethodID android_tox_callback_friend_message_v2_cb_method = NULL;
jmethodID android_tox_callback_friend_sync_message_v2_cb_method = NULL;
jmethodID android_tox_callback_friend_read_receipt_message_v2_cb_method = NULL;
jmethodID android_tox_callback_file_recv_control_cb_method = NULL;
jmethodID android_tox_callback_file_chunk_request_cb_method = NULL;
jmethodID android_tox_callback_file_recv_cb_method = NULL;
jmethodID android_tox_callback_file_recv_chunk_cb_method = NULL;
jmethodID android_tox_callback_conference_invite_cb_method = NULL;
jmethodID android_tox_callback_conference_connected_cb_method = NULL;
jmethodID android_tox_callback_conference_message_cb_method = NULL;
jmethodID android_tox_callback_conference_title_cb_method = NULL;
jmethodID android_tox_callback_conference_peer_name_cb_method = NULL;
jmethodID android_tox_callback_conference_peer_list_changed_cb_method = NULL;
jmethodID android_tox_callback_conference_namelist_change_cb_method = NULL;
jmethodID android_tox_log_cb_method = NULL;
// -------- _AV-callbacks_ -----
jmethodID android_toxav_callback_call_cb_method = NULL;
jmethodID android_toxav_callback_video_receive_frame_cb_method = NULL;
jmethodID android_toxav_callback_call_state_cb_method = NULL;
jmethodID android_toxav_callback_bit_rate_status_cb_method = NULL;
jmethodID android_toxav_callback_audio_receive_frame_cb_method = NULL;
jmethodID android_toxav_callback_call_comm_cb_method = NULL;
// -------- _AV-callbacks_ -----
// -------- _callbacks_ --------

// ----- JNI stuff -----



typedef struct DHT_node
{
    const char *ip;
    uint16_t port;
    const char key_hex[TOX_PUBLIC_KEY_SIZE*2 + 1];
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
} DHT_node;




// functions -----------
// functions -----------
// functions -----------
void self_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data);

void friend_name_cb(Tox *tox, uint32_t friend_number, const uint8_t *name, size_t length, void *user_data);
void friend_status_message_cb(Tox *tox, uint32_t friend_number, const uint8_t *message, size_t length, void *user_data);
void friend_lossless_packet_cb(Tox *tox, uint32_t friend_number, const uint8_t *data, size_t length, void *user_data);
void friend_status_cb(Tox *tox, uint32_t friend_number, TOX_USER_STATUS status, void *user_data);
void friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data);
void friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data);
void friend_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data);
void friend_request_cb(Tox *tox, const uint8_t *public_key, const uint8_t *message, size_t length, void *user_data);
void friend_message_cb(Tox *tox, uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message, size_t length,
                       void *user_data);
void friend_message_v2_cb(Tox *tox, uint32_t friend_number, const uint8_t *raw_message, size_t raw_message_len);
void friend_sync_message_v2_cb(Tox *tox, uint32_t friend_number, const uint8_t *raw_message, size_t raw_message_len);
void friend_read_receipt_message_v2_cb(Tox *tox, uint32_t friend_number, uint32_t ts_sec, const uint8_t *msgid);

void file_recv_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control,
                          void *user_data);
void file_chunk_request_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length,
                           void *user_data);
void file_recv_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size,
                  const uint8_t *filename, size_t filename_length, void *user_data);
void file_recv_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, const uint8_t *data,
                        size_t length, void *user_data);

void conference_invite_cb(Tox *tox, uint32_t friend_number, TOX_CONFERENCE_TYPE type, const uint8_t *cookie,
                          size_t length, void *user_data);
void conference_connected_cb(Tox *tox, uint32_t conference_number, void *user_data);
void conference_message_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number, TOX_MESSAGE_TYPE type,
                           const uint8_t *message, size_t length, void *user_data);
void conference_title_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number, const uint8_t *title,
                         size_t length, void *user_data);

void conference_peer_name_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number,
                             const uint8_t *name, size_t length, void *user_data);

#if TOX_VERSION_IS_API_COMPATIBLE(0, 2, 0)
void conference_peer_list_changed_cb(Tox *tox, uint32_t conference_number, void *user_data);
#else
void conference_namelist_change_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number,
                                   TOX_CONFERENCE_STATE_CHANGE change, void *user_data);
#endif

void tox_log_cb__custom(Tox *tox, TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func,
                        const char *message, void *user_data);

void android_logger(int level, const char *logtext);
jstring c_safe_string_from_java(const char *instr, size_t len);
// functions -----------
// functions -----------
// functions -----------





void dbg(int level, const char *fmt, ...)
{
    char *level_and_format = NULL;
    char *fmt_copy = NULL;
    char *log_line_str = NULL;

    if(fmt == NULL)
    {
        return;
    }

    if(strlen(fmt) < 1)
    {
        return;
    }

    if((level < 0) || (level > 9))
    {
        level = 0;
    }

    level_and_format = malloc(strlen(fmt) + 3);

    if(!level_and_format)
    {
        return;
    }

    fmt_copy = level_and_format + 2;
    strcpy(fmt_copy, fmt);
    level_and_format[1] = ':';

    if(level == 0)
    {
        level_and_format[0] = 'E';
    }
    else if(level == 1)
    {
        level_and_format[0] = 'W';
    }
    else if(level == 2)
    {
        level_and_format[0] = 'I';
    }
    else
    {
        level_and_format[0] = 'D';
    }

    if(level <= CURRENT_LOG_LEVEL)
    {
        log_line_str = malloc((size_t)MAX_LOG_LINE_LENGTH);
        // memset(log_line_str, 0, (size_t)MAX_LOG_LINE_LENGTH);
        va_list ap;
        va_start(ap, fmt);
        vsnprintf(log_line_str, (size_t)MAX_LOG_LINE_LENGTH, level_and_format, ap);
        // send "log_line_str" to android
        android_logger(level, log_line_str);
        va_end(ap);
        free(log_line_str);
    }

    if(level_and_format)
    {
        free(level_and_format);
    }
}


Tox *create_tox(int udp_enabled, int orbot_enabled, const char *proxy_host, uint16_t proxy_port,
                int local_discovery_enabled_, const uint8_t *passphrase, size_t passphrase_len)
{
    Tox *tox = NULL;
    TOX_ERR_NEW error;
    struct Tox_Options options;
    CLEAR(options);
    dbg(9, "1006");
    tox_options_default(&options);
    uint16_t tcp_port = 33776;
    options.ipv6_enabled = true;

    if(orbot_enabled == 1)
    {
        options.proxy_type = TOX_PROXY_TYPE_SOCKS5;
        options.proxy_host = proxy_host;
        options.proxy_port = proxy_port;
    }
    else
    {
        options.proxy_type = TOX_PROXY_TYPE_NONE;
    }

    if(udp_enabled == 1)
    {
        options.udp_enabled = true;
    }
    else
    {
        options.udp_enabled = false; // set TCP as default mode for android !!
    }

    if(local_discovery_enabled_ == 1)
    {
        options.local_discovery_enabled = true;
    }
    else
    {
        options.local_discovery_enabled = false;
    }

    options.hole_punching_enabled = true;
    // options.tcp_port = tcp_port;
    options.tcp_port = 0; // TCP relay is disabled !!
    // ------------------------------------------------------------
    // set our own handler for c-toxcore logging messages!!
    options.log_callback = tox_log_cb__custom;
    // ------------------------------------------------------------
    dbg(9, "1007");
    char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
    dbg(9, "1008");
    snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);
    dbg(9, "1009");
    FILE *f = fopen(full_path_filename, "rb");

    if(f)
    {
        fseek(f, 0, SEEK_END);
        long fsize = ftell(f);
        fseek(f, 0, SEEK_SET);
        uint8_t *savedata_enc = malloc(fsize);
        size_t dummy = fread(savedata_enc, fsize, 1, f);

        if(dummy < 1)
        {
            dbg(0, "reading savedata_enc failed");
        }

        fclose(f);
        uint8_t *savedata = NULL;
        bool res1 = false;

        if(fsize < TOX_PASS_ENCRYPTION_EXTRA_LENGTH)
        {
        }
        else
        {
            res1 = tox_is_data_encrypted(savedata_enc);
        }

        dbg(9, "create_tox:tox_is_data_encrypted=%d", (int)res1);

        if(res1 == true)
        {
            size_t savedata_len = (size_t)(fsize - TOX_PASS_ENCRYPTION_EXTRA_LENGTH);
            savedata = malloc(savedata_len);
            TOX_ERR_DECRYPTION error2;
            bool res2 = tox_pass_decrypt(savedata_enc, (size_t)fsize, passphrase, passphrase_len, savedata, &error2);

            if(savedata_enc)
            {
                free(savedata_enc);
            }
        }
        else
        {
            // save data is not encrypted (yet) !
            savedata = savedata_enc;
        }

        options.savedata_type = TOX_SAVEDATA_TYPE_TOX_SAVE;
        options.savedata_data = savedata;
        options.savedata_length = fsize;
        dbg(9, "1008");
#ifdef TOX_HAVE_TOXUTIL
        tox = tox_utils_new(&options, &error);
#else
        tox = tox_new(&options, &error);
#endif
        dbg(9, "1009 tox=%p error=%d", tox, error);
        int j = 0;

        while(error != 0)
        {
            j++;

            if(j > 100)
            {
                break;
            }

            // could not allocate network port, sleep and try again ...
            c_sleep(150);
#ifdef TOX_HAVE_TOXUTIL
            tox = tox_utils_new(&options, &error);
#else
            tox = tox_new(&options, &error);
#endif
            dbg(9, "1009 tox=%p error=%d", tox, error);
        }

        free((void *)savedata);
    }
    else
    {
        dbg(9, "1010");
#ifdef TOX_HAVE_TOXUTIL
        tox = tox_utils_new(&options, NULL);
#else
        tox = tox_new(&options, NULL);
#endif
        dbg(9, "1011 tox=%p", tox);
    }

    bool local_discovery_enabled = tox_options_get_local_discovery_enabled(&options);
    dbg(9, "local discovery enabled = %d", (int)local_discovery_enabled);
    free(full_path_filename);
    return tox;
}


void stop_filter_audio()
{
#ifdef USE_ECHO_CANCELLATION

    /* Prepare filter_audio */
    if(filteraudio != NULL)
    {
        dbg(9, "filter_audio: shutdown");
        kill_filter_audio(filteraudio);
        filteraudio = NULL;
    }

#endif
}

void start_filter_audio(uint32_t in_samplerate)
{
#ifdef USE_ECHO_CANCELLATION

    if(filteraudio)
    {
        stop_filter_audio();
    }

    /* Prepare filter_audio */
    filteraudio = new_filter_audio(in_samplerate);
    dbg(9, "filter_audio: prepare. samplerate=%d", (int)in_samplerate);

    if(filteraudio != NULL)
    {
        /* Enable/disable filters. 1 to enable, 0 to disable. */
        int echo_ = 1;
        int noise_ = 0;
        int gain_ = 0;
        int vad_ = 0;
        enable_disable_filters(filteraudio, echo_, noise_, gain_, vad_);
    }

#endif
}

/*
 * input_latency_ms mostly set to "0" (zero)
 * use frame_duration_ms for cumulative value
 */
void set_delay_ms_filter_audio(int16_t input_latency_ms, int16_t frame_duration_ms)
{
#ifdef USE_ECHO_CANCELLATION
    /* It's essential that echo delay is set correctly; it's the most important part of the
     * echo cancellation process. If the delay is not set to the acceptable values the AEC
     * will not be able to recover. Given that it's not that easy to figure out the exact
     * time it takes for a signal to get from Output to the Input, setting it to suggested
     * input device latency + frame duration works really good and gives the filter ability
     * to adjust it internally after some time (usually up to 6-7 seconds in my tests when
     * the error is about 20%).
     */
    dbg(9, "filter_audio: set delay in ms=%d", (int)(input_latency_ms + frame_duration_ms));

    if(filteraudio)
    {
        set_echo_delay_ms(filteraudio, (input_latency_ms + frame_duration_ms));
    }

    /*
     */
#endif
}

void restart_filter_audio(uint32_t in_samplerate)
{
#ifdef USE_ECHO_CANCELLATION
    dbg(9, "filter_audio: restart. samplerate=%d", (int)in_samplerate);

    if(filteraudio)
    {
        stop_filter_audio();
    }

    start_filter_audio(in_samplerate);
#endif
}


void update_savedata_file(const Tox *tox, const uint8_t *passphrase, size_t passphrase_len)
{
    size_t size = tox_get_savedata_size(tox);
    dbg(9, "update_savedata_file:tox_get_savedata_size=%d", (int)size);
    char *savedata = malloc(size);
    dbg(9, "update_savedata_file:savedata=%p", savedata);
    tox_get_savedata(tox, (uint8_t *)savedata);
    char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
    snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);
    char *full_path_filename_tmp = malloc(MAX_FULL_PATH_LENGTH);
    snprintf(full_path_filename_tmp, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_tmp_filename);
    size_t size_enc = size + TOX_PASS_ENCRYPTION_EXTRA_LENGTH;
    dbg(9, "update_savedata_file:size_enc=%d", (int)size_enc);
    char *savedata_enc = malloc(size_enc);
    dbg(9, "update_savedata_file:savedata_enc=%p", savedata_enc);
    TOX_ERR_ENCRYPTION error;
    tox_pass_encrypt(savedata, size, passphrase, passphrase_len, savedata_enc, &error);
    dbg(9, "update_savedata_file:tox_pass_encrypt:%d", (int)error);
    bool res = false;

    if(size_enc < TOX_PASS_ENCRYPTION_EXTRA_LENGTH)
    {
    }
    else
    {
        res = tox_is_data_encrypted(savedata_enc);
    }

    dbg(9, "update_savedata_file:tox_is_data_encrypted=%d", (int)res);
    FILE *f = fopen(full_path_filename_tmp, "wb");
    fwrite(savedata_enc, size_enc, 1, f);
    fclose(f);
    rename(full_path_filename_tmp, full_path_filename);
    free(full_path_filename);
    free(full_path_filename_tmp);

    if(savedata)
    {
        free(savedata);
    }

    if(savedata_enc)
    {
        free(savedata_enc);
    }
}


int bin_id_to_string(const char *bin_id, size_t bin_id_size, char *output, size_t output_size)
{
    if(bin_id_size != TOX_ADDRESS_SIZE || output_size < (TOX_ADDRESS_SIZE * 2 + 1))
    {
        return -1;
    }

    size_t i;

    for(i = 0; i < TOX_ADDRESS_SIZE; ++i)
    {
        snprintf(&output[i * 2], output_size - (i * 2), "%02X", bin_id[i] & 0xff);
    }

    return 0;
}

void bootstrap_real(Tox *tox)
{
#if 0
    // OLD
    DHT_node nodes[] =
    {
        {"178.62.250.138",             33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B", {0}},
        {"nodes.tox.chat",             33445, "6FC41E2BD381D37E9748FC0E0328CE086AF9598BECC8FEB7DDF2E440475F300E", {0}},
        {"130.133.110.14",             33445, "461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F", {0}},
        {"tox.zodiaclabs.org",         33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074", {0}},
        {"163.172.136.118",            33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
        {"217.182.143.254",             443, "7AED21F94D82B05774F697B209628CD5A9AD17E0C073D9329076A4C28ED28147", {0}},
        {"185.14.30.213",               443,  "2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
        {"136.243.141.187",             443,  "6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39", {0}},
        {"128.199.199.197",            33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09", {0}},
        // {"192.168.0.20",   33447, "578E5F044C98290D0368F425E0E957056B30FB995F53DEB21C3E23D7A3B4E679", {0}} ,
        // {"192.168.0.22",   33447, "578E5F044C98290D0368F425E0E957056B30FB995F53DEB21C3E23D7A3B4E679", {0}} ,
        {"biribiri.org",               33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67", {0}}
    };
#else
    // current bootstrap nodes
    DHT_node nodes[] =
    {
        {"85.172.30.117",33445,"8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832", {0}},
        {"tox.verdict.gg",33445,"1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976", {0}},
        {"163.172.136.118",33445,"2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
        {"78.46.73.141",33445,"02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46", {0}},
        {"tox.initramfs.io",33445,"3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25", {0}},
        {"46.229.52.198",33445,"813C8F4187833EF0655B10F7752141A352248462A567529A38B6BBF73E979307", {0}},
        {"tox.neuland.technology",33445,"15E9C309CFCB79FDDF0EBA057DABB49FE15F3803B1BFF06536AE2E5BA5E4690E", {0}},
        {"144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C", {0}},
        {"tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E", {0}},
        {"37.48.122.22",33445,"1B5A8AB25FFFB66620A531C4646B47F0F32B74C547B30AF8BD8266CA50A3AB59", {0}},
        {"tox.novg.net",33445,"D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463", {0}},
        {"95.31.18.227",33445,"257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E", {0}},
        {"185.14.30.213",443,"2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
        {"185.14.30.213",3389,"2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
        {"198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F", {0}},
        {"52.53.185.100",33445,"A04F5FE1D006871588C8EC163676458C1EC75B20B4A147433D271E1E85DAF839", {0}},
        {"tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23", {0}},
        {"116.196.77.132",443,"040326E850DDCB49B1B2D9E3E2789D425774E4C5D783A55C09A024D05D2A8A66", {0}},
        {"116.196.77.132",33445,"040326E850DDCB49B1B2D9E3E2789D425774E4C5D783A55C09A024D05D2A8A66", {0}},
        {"116.196.77.132",3389,"040326E850DDCB49B1B2D9E3E2789D425774E4C5D783A55C09A024D05D2A8A66", {0}},
        {"87.118.126.207",33445,"0D303B1778CA102035DA01334E7B1855A45C3EFBC9A83B9D916FFDEBC6DD3B2E", {0}},
        {"81.169.136.229",33445,"D031DAC44F00464D3C9636F9850BF0064BC37FEB55789A13B6F59052CAE8A958", {0}}
    };
#endif

    for(size_t i = 0; i < sizeof(nodes)/sizeof(DHT_node); i ++)
    {
        sodium_hex2bin(nodes[i].key_bin, sizeof(nodes[i].key_bin),
                       nodes[i].key_hex, sizeof(nodes[i].key_hex)-1, NULL, NULL, NULL);
        tox_bootstrap(tox, nodes[i].ip, nodes[i].port, nodes[i].key_bin, NULL);
        tox_add_tcp_relay(tox, nodes[i].ip, nodes[i].port, nodes[i].key_bin, NULL); // also try as TCP relay
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

    for(size_t i = 0; i < sizeof(tox_id_hex_local)-1; i ++)
    {
        tox_id_hex_local[i] = toupper(tox_id_hex_local[i]);
    }

    snprintf(toxid_str, (size_t)(TOX_ADDRESS_SIZE*2 + 1), "%s", (const char *)tox_id_hex_local);
}

void toxid_hex_to_bin(unsigned char *public_key, const char *toxid_str)
{
    sodium_hex2bin(public_key, TOX_ADDRESS_SIZE, toxid_str, (TOX_ADDRESS_SIZE*2), NULL, NULL, NULL);
}

void toxid_bin_to_hex(const uint8_t *public_key, char *toxid_str)
{
    char tox_id_hex_local[TOX_ADDRESS_SIZE*2 + 1];
    sodium_bin2hex(tox_id_hex_local, sizeof(tox_id_hex_local), public_key, TOX_ADDRESS_SIZE);

    for(size_t i = 0; i < sizeof(tox_id_hex_local)-1; i ++)
    {
        tox_id_hex_local[i] = toupper(tox_id_hex_local[i]);
    }

    snprintf(toxid_str, (size_t)(TOX_ADDRESS_SIZE*2 + 1), "%s", (const char *)tox_id_hex_local);
}

void print_tox_id(Tox *tox)
{
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
    get_my_toxid(tox, tox_id_hex);
    // dbg(2, "MyToxID:%s", tox_id_hex);
}

void bootstrap()
{
    bootstrap_real(tox_global);
}

void init_tox_callbacks()
{
#ifdef TOX_HAVE_TOXUTIL
    // -------- _callbacks_ --------
    tox_callback_friend_name(tox_global, friend_name_cb);
    tox_callback_friend_status_message(tox_global, friend_status_message_cb);
    tox_callback_friend_status(tox_global, friend_status_cb);
    tox_callback_friend_typing(tox_global, friend_typing_cb);
    tox_callback_friend_read_receipt(tox_global, friend_read_receipt_cb);
    tox_callback_friend_request(tox_global, friend_request_cb);
    tox_callback_friend_message(tox_global, friend_message_cb);
    tox_callback_conference_invite(tox_global, conference_invite_cb);
    tox_callback_conference_connected(tox_global, conference_connected_cb);
    tox_callback_conference_message(tox_global, conference_message_cb);
    tox_callback_conference_title(tox_global, conference_title_cb);
    tox_callback_conference_peer_name(tox_global, conference_peer_name_cb);
#if TOX_VERSION_IS_API_COMPATIBLE(0, 2, 0)
    tox_callback_conference_peer_list_changed(tox_global, conference_peer_list_changed_cb);
#else
    tox_callback_conference_namelist_change(tox_global, conference_namelist_change_cb);
#endif
    // --------------------
    tox_utils_callback_self_connection_status(tox_global, self_connection_status_cb);
    tox_callback_self_connection_status(tox_global, tox_utils_self_connection_status_cb);
    tox_utils_callback_friend_connection_status(tox_global, friend_connection_status_cb);
    tox_callback_friend_connection_status(tox_global, tox_utils_friend_connection_status_cb);
    tox_utils_callback_friend_lossless_packet(tox_global, friend_lossless_packet_cb);
    tox_callback_friend_lossless_packet(tox_global, tox_utils_friend_lossless_packet_cb);
    tox_utils_callback_file_recv_control(tox_global, file_recv_control_cb);
    tox_callback_file_recv_control(tox_global, tox_utils_file_recv_control_cb);
    tox_utils_callback_file_chunk_request(tox_global, file_chunk_request_cb);
    tox_callback_file_chunk_request(tox_global, tox_utils_file_chunk_request_cb);
    tox_utils_callback_file_recv(tox_global, file_recv_cb);
    tox_callback_file_recv(tox_global, tox_utils_file_recv_cb);
    tox_utils_callback_file_recv_chunk(tox_global, file_recv_chunk_cb);
    tox_callback_file_recv_chunk(tox_global, tox_utils_file_recv_chunk_cb);
    tox_utils_callback_friend_message_v2(tox_global, friend_message_v2_cb);
    tox_utils_callback_friend_sync_message_v2(tox_global, friend_sync_message_v2_cb);
    tox_utils_callback_friend_read_receipt_message_v2(tox_global, friend_read_receipt_message_v2_cb);
    // -------- _callbacks_ --------
#else
    // -------- _callbacks_ --------
    tox_callback_self_connection_status(tox_global, self_connection_status_cb);
    tox_callback_friend_name(tox_global, friend_name_cb);
    tox_callback_friend_status_message(tox_global, friend_status_message_cb);
    tox_callback_friend_status(tox_global, friend_status_cb);
    tox_callback_friend_connection_status(tox_global, friend_connection_status_cb);
    tox_callback_friend_typing(tox_global, friend_typing_cb);
    tox_callback_friend_read_receipt(tox_global, friend_read_receipt_cb);
    tox_callback_friend_request(tox_global, friend_request_cb);
    tox_callback_friend_message(tox_global, friend_message_cb);
    tox_callback_file_recv_control(tox_global, file_recv_control_cb);
    tox_callback_file_chunk_request(tox_global, file_chunk_request_cb);
    tox_callback_file_recv(tox_global, file_recv_cb);
    tox_callback_file_recv_chunk(tox_global, file_recv_chunk_cb);
    tox_callback_conference_invite(tox_global, conference_invite_cb);
    tox_callback_conference_connected(tox_global, conference_connected_cb);
    tox_callback_conference_message(tox_global, conference_message_cb);
    tox_callback_conference_title(tox_global, conference_title_cb);
    tox_callback_conference_peer_name(tox_global, conference_peer_name_cb);
#if TOX_VERSION_IS_API_COMPATIBLE(0, 2, 0)
    tox_callback_conference_peer_list_changed(tox_global, conference_peer_list_changed_cb);
#else
    tox_callback_conference_namelist_change(tox_global, conference_namelist_change_cb);
#endif
    // tox_callback_friend_lossy_packet(tox_global, friend_lossy_packet_cb);
    // -------- _callbacks_ --------
#endif
}




// ------------- JNI -------------
// ------------- JNI -------------
// ------------- JNI -------------

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv *env_this;
    cachedJVM = jvm;

    if((*jvm)->GetEnv(jvm, (void **) &env_this, JNI_VERSION_1_6))
    {
        // dbg(0,"Could not get JVM");
        return JNI_ERR;
    }

#ifdef AV_MEDIACODEC

    // Required for MediaCodec HW decoder
    if(av_jni_set_java_vm(jvm, NULL) != 0)
    {
    }

#endif
    // dbg(0,"++ Found JVM ++");
    return JNI_VERSION_1_6;
}

JNIEnv *jni_getenv()
{
    JNIEnv *env_this;
    (*cachedJVM)->GetEnv(cachedJVM, (void **) &env_this, JNI_VERSION_1_6);
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

    if(!*ret)
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

    if(*ret == NULL)
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

    if(*ret == NULL)
    {
        return 0;
    }

    return 1;
}

// -------- _callbacks_ --------
void android_tox_callback_self_connection_status_cb(int a_TOX_CONNECTION)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_self_connection_status_cb_method, (jint)a_TOX_CONNECTION);
}

void self_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data)
{
    switch(connection_status)
    {
        case TOX_CONNECTION_NONE:
            dbg(2, "Offline");
            my_connection_status = TOX_CONNECTION_NONE;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;

        case TOX_CONNECTION_TCP:
            dbg(2, "Online, using TCP");
            my_connection_status = TOX_CONNECTION_TCP;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;

        case TOX_CONNECTION_UDP:
            dbg(2, "Online, using UDP");
            my_connection_status = TOX_CONNECTION_UDP;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;
    }
}

void android_tox_callback_friend_name_cb(uint32_t friend_number, const uint8_t *name, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)name, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_name_cb_method, (jlong)(unsigned long long)friend_number, js1,
                                     (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_name_cb(Tox *tox, uint32_t friend_number, const uint8_t *name, size_t length, void *user_data)
{
    android_tox_callback_friend_name_cb(friend_number, name, length);
}

void android_tox_callback_friend_status_message_cb(uint32_t friend_number, const uint8_t *message, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)message, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_status_message_cb_method, (jlong)(unsigned long long)friend_number, js1,
                                     (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_status_message_cb(Tox *tox, uint32_t friend_number, const uint8_t *message, size_t length, void *user_data)
{
    android_tox_callback_friend_status_message_cb(friend_number, message, length);
}

void android_tox_callback_friend_lossless_packet_cb(uint32_t friend_number, const uint8_t *data, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)length);

    if(data2 == NULL)
    {
        // TODO: catch this OOM error!!
        // return; // out of memory error thrown
    }

    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)length, (const jbyte *)data);
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_lossless_packet_cb_method,
                                     (jlong)(unsigned long long)friend_number,
                                     data2,
                                     (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, data2);
}

void friend_lossless_packet_cb(Tox *tox, uint32_t friend_number, const uint8_t *data, size_t length,
                               void *user_data)
{
    android_tox_callback_friend_lossless_packet_cb(friend_number, data, length);
}

void android_tox_callback_friend_status_cb(uint32_t friend_number, TOX_USER_STATUS status)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_status_cb_method, (jlong)(unsigned long long)friend_number, (jint)status);
}

void friend_status_cb(Tox *tox, uint32_t friend_number, TOX_USER_STATUS status, void *user_data)
{
    android_tox_callback_friend_status_cb(friend_number, status);
}

void android_tox_callback_friend_connection_status_cb(uint32_t friend_number, TOX_CONNECTION connection_status)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();

    switch(connection_status)
    {
        case TOX_CONNECTION_NONE:
            dbg(2, "friend# %d Offline", (int)friend_number);
            (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                             android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number,
                                             (jint)connection_status);
            break;

        case TOX_CONNECTION_TCP:
            dbg(2, "friend# %d Online, using TCP", (int)friend_number);
            (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                             android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number,
                                             (jint)connection_status);
            break;

        case TOX_CONNECTION_UDP:
            dbg(2, "friend# %d Online, using UDP", (int)friend_number);
            (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                             android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number,
                                             (jint)connection_status);
            break;
    }
}

void friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data)
{
    android_tox_callback_friend_connection_status_cb(friend_number, connection_status);
}

void android_tox_callback_friend_typing_cb(uint32_t friend_number, bool is_typing)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_typing_cb_method, (jlong)(unsigned long long)friend_number, (jlong)is_typing);
}

void friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data)
{
    android_tox_callback_friend_typing_cb(friend_number, is_typing);
}

void android_tox_callback_friend_read_receipt_cb(uint32_t friend_number, uint32_t message_id)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_read_receipt_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)message_id);
}

void friend_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data)
{
    android_tox_callback_friend_read_receipt_cb(friend_number, message_id);
}

void android_tox_callback_friend_request_cb(const uint8_t *public_key, const uint8_t *message, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
    CLEAR(tox_id_hex);
    toxid_bin_to_hex(public_key, tox_id_hex);
    tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key
    dbg(9, "pubkey string=%s", tox_id_hex);
    jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, tox_id_hex);
    jstring js2 = c_safe_string_from_java((char *)message, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_request_cb_method, js1, js2, (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
    (*jnienv2)->DeleteLocalRef(jnienv2, js2);
}


void friend_request_cb(Tox *tox, const uint8_t *public_key, const uint8_t *message, size_t length, void *user_data)
{
    android_tox_callback_friend_request_cb(public_key, message, length);
}




void android_tox_callback_friend_read_receipt_message_v2_cb(uint32_t friend_number,
        uint32_t ts_sec, const uint8_t *msgid)
{
#ifdef TOX_MESSAGE_V2_ACTIVE

    if(msgid)
    {
        JNIEnv *jnienv2;
        jnienv2 = jni_getenv();
        jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)TOX_PUBLIC_KEY_SIZE);

        if(data2 == NULL)
        {
            // TODO: catch this OOM error!!
            // return; // out of memory error thrown
        }

        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)TOX_PUBLIC_KEY_SIZE, (const jbyte *)msgid);
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                         android_tox_callback_friend_read_receipt_message_v2_cb_method,
                                         (jlong)(unsigned long long)friend_number,
                                         (jlong)ts_sec,
                                         data2
                                        );
        (*jnienv2)->DeleteLocalRef(jnienv2, data2);
    }

#endif
}

void friend_read_receipt_message_v2_cb(Tox *tox, uint32_t friend_number,
                                       uint32_t ts_sec, const uint8_t *msgid)
{
    android_tox_callback_friend_read_receipt_message_v2_cb(friend_number, ts_sec, msgid);
}


void android_tox_callback_friend_message_v2_cb(uint32_t friend_number, const uint8_t *raw_message,
        size_t raw_message_len)
{
#ifdef TOX_MESSAGE_V2_ACTIVE
    uint8_t *message_text = calloc(1, raw_message_len);

    if(message_text)
    {
        JNIEnv *jnienv2;
        jnienv2 = jni_getenv();
        jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)raw_message_len);

        if(data2 == NULL)
        {
            // TODO: catch this OOM error!!
            // return; // out of memory error thrown
        }

        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)raw_message_len, (const jbyte *)raw_message);
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        uint32_t ts_sec = tox_messagev2_get_ts_sec(raw_message);
        uint16_t ts_ms = tox_messagev2_get_ts_ms(raw_message);
        uint32_t text_length = 0;
        bool res = tox_messagev2_get_message_text(raw_message,
                   (uint32_t)raw_message_len,
                   (bool)false, (uint32_t)0,
                   message_text, &text_length);

        if(text_length > 0)
        {
            JNIEnv *jnienv2;
            jnienv2 = jni_getenv();
            jstring js1 = c_safe_string_from_java((char *)message_text, text_length);
            // TODO: give back also the raw message bytes!
            (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                             android_tox_callback_friend_message_v2_cb_method,
                                             (jlong)(unsigned long long)friend_number,
                                             js1,
                                             (jlong)(unsigned long long)text_length,
                                             (jlong)ts_sec,
                                             (jlong)ts_ms,
                                             data2,
                                             (jlong)(unsigned long long)raw_message_len
                                            );
            (*jnienv2)->DeleteLocalRef(jnienv2, js1);
        }

        (*jnienv2)->DeleteLocalRef(jnienv2, data2);
        free(message_text);
    }

#endif
}


void friend_message_v2_cb(Tox *tox, uint32_t friend_number, const uint8_t *raw_message, size_t raw_message_len)
{
    android_tox_callback_friend_message_v2_cb(friend_number, raw_message, raw_message_len);
}

void android_tox_callback_friend_sync_message_v2_cb(uint32_t friend_number, const uint8_t *raw_message,
        size_t raw_message_len)
{
    dbg(9, "friend_sync_message_v2_cb:fn=%d", (int)friend_number);
#ifdef TOX_MESSAGE_V2_ACTIVE
    uint8_t *message_data = calloc(1, raw_message_len);

    if(message_data)
    {
        JNIEnv *jnienv2;
        jnienv2 = jni_getenv();
        jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)raw_message_len);
        jbyteArray data3 = (*jnienv2)->NewByteArray(jnienv2, (int)raw_message_len);

        if(data2 == NULL)
        {
            // TODO: catch this OOM error!!
            // return; // out of memory error thrown
        }

        if(data3 == NULL)
        {
            // TODO: catch this OOM error!!
            // return; // out of memory error thrown
        }

        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)raw_message_len, (const jbyte *)raw_message);
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
        uint32_t ts_sec = tox_messagev2_get_ts_sec(raw_message);
        uint16_t ts_ms = tox_messagev2_get_ts_ms(raw_message);
        uint32_t data_length = 0;
        bool res = tox_messagev2_get_sync_message_data(raw_message,
                   (uint32_t)raw_message_len, message_data, &data_length);
        (*jnienv2)->SetByteArrayRegion(jnienv2, data3, 0, (int)data_length, (const jbyte *)message_data);

        if(raw_message_len > 0)
        {
            JNIEnv *jnienv2;
            jnienv2 = jni_getenv();
            // TODO: give back also the raw message bytes!
            (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                             android_tox_callback_friend_sync_message_v2_cb_method,
                                             (jlong)(unsigned long long)friend_number,
                                             (jlong)ts_sec,
                                             (jlong)ts_ms,
                                             data2,
                                             (jlong)(unsigned long long)raw_message_len,
                                             data3,
                                             (jlong)(unsigned long long)data_length
                                            );
        }

        (*jnienv2)->DeleteLocalRef(jnienv2, data2);
        (*jnienv2)->DeleteLocalRef(jnienv2, data3);
        free(message_data);
    }

#endif
}


void friend_sync_message_v2_cb(Tox *tox, uint32_t friend_number, const uint8_t *raw_message, size_t raw_message_len)
{
    android_tox_callback_friend_sync_message_v2_cb(friend_number, raw_message, raw_message_len);
}

void android_tox_callback_friend_message_cb(uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message,
        size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)message, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_message_cb_method, (jlong)(unsigned long long)friend_number, (jint) type, js1,
                                     (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_message_cb(Tox *tox, uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message, size_t length,
                       void *user_data)
{
    android_tox_callback_friend_message_cb(friend_number, type, message, length);
}

void android_tox_callback_file_recv_control_cb(uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_file_recv_control_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)file_number, (jint)control);
}

void file_recv_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control,
                          void *user_data)
{
    android_tox_callback_file_recv_control_cb(friend_number, file_number, control);
}

void android_tox_callback_file_chunk_request_cb(uint32_t friend_number, uint32_t file_number, uint64_t position,
        size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_file_chunk_request_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)file_number, (jlong)(unsigned long long)position, (jlong)(unsigned long long)length);
}

void file_chunk_request_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length,
                           void *user_data)
{
    android_tox_callback_file_chunk_request_cb(friend_number, file_number, position, length);
}

void android_tox_callback_file_recv_cb(uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size,
                                       const uint8_t *filename, size_t filename_length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    char filename_unknown[] = "unknown.png";
    size_t len = filename_length;
    char *mystr = NULL;
    dbg(9, "file_recv_cb:001:file_number=%d", (int)file_number);
    dbg(9, "file_recv_cb:filename=%p filename_length=%d", filename, (int)filename_length);
    dbg(9, "file_recv_cb:002");

    if((! filename)||(filename_length == 0))
    {
        dbg(9, "file_recv_cb:003");
        dbg(9, "file_recv_cb:004");
        len = strlen(filename_unknown);
        mystr = filename_unknown;
        dbg(9, "file_recv_cb:005");
    }
    else
    {
        dbg(9, "file_recv_cb:006");
        mystr = (char *)filename;
        dbg(9, "file_recv_cb:007");
    }

    jstring js1 = c_safe_string_from_java(mystr, len);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_file_recv_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)file_number, (jint)kind, (jlong)(unsigned long long)file_size,
                                     js1, (jlong)(unsigned long long)len);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
    dbg(9, "file_recv_cb:009");
}

jstring c_safe_string_from_java(const char *instr, size_t len)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jbyteArray data = (*jnienv2)->NewByteArray(jnienv2, (int)len);
    (*jnienv2)->SetByteArrayRegion(jnienv2, data, 0, (int)len, (const jbyte *)instr);
    jstring js1 = (jstring)(*jnienv2)->CallStaticObjectMethod(jnienv2, TrifaToxService_class, safe_string_method, data);
    (*jnienv2)->DeleteLocalRef(jnienv2, data);
    return js1;
}

void file_recv_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size,
                  const uint8_t *filename, size_t filename_length, void *user_data)
{
    android_tox_callback_file_recv_cb(friend_number, file_number, kind, file_size, filename, filename_length);
}








// ------------ Conference [2] ------------
// ------------ Conference [2] ------------
// ------------ Conference [2] ------------

#if TOX_VERSION_IS_API_COMPATIBLE(0, 2, 0)

void android_tox_callback_conference_peer_list_changed_cb(uint32_t conference_number)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_peer_list_changed_cb_method,
                                     (jlong)(unsigned long long)conference_number);
}

void conference_peer_list_changed_cb(Tox *tox, uint32_t conference_number, void *user_data)
{
    android_tox_callback_conference_peer_list_changed_cb(conference_number);
}

#else

void android_tox_callback_conference_namelist_change_cb(uint32_t conference_number, uint32_t peer_number,
        TOX_CONFERENCE_STATE_CHANGE change)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_namelist_change_cb_method, (jlong)(unsigned long long)conference_number,
                                     (jlong)(unsigned long long)peer_number,
                                     (jint)change);
}

void conference_namelist_change_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number,
                                   TOX_CONFERENCE_STATE_CHANGE change, void *user_data)
{
    android_tox_callback_conference_namelist_change_cb(conference_number, peer_number, change);
}

#endif

void android_tox_callback_conference_title_cb(uint32_t conference_number, uint32_t peer_number, const uint8_t *title,
        size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)title, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_title_cb_method, (jlong)(unsigned long long)conference_number,
                                     (jlong)(unsigned long long)peer_number,
                                     js1, (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void conference_title_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number, const uint8_t *title,
                         size_t length, void *user_data)
{
    android_tox_callback_conference_title_cb(conference_number, peer_number, title, length);
}

void android_tox_callback_conference_peer_name_cb(uint32_t conference_number, uint32_t peer_number,
        const uint8_t *name, size_t length)
{
    // TODO: write me
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)name, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_peer_name_cb_method,
                                     (jlong)(unsigned long long)conference_number,
                                     (jlong)(unsigned long long)peer_number,
                                     js1, (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void conference_peer_name_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number,
                             const uint8_t *name, size_t length, void *user_data)
{
    android_tox_callback_conference_peer_name_cb(conference_number, peer_number,
            name, length);
}

void android_tox_callback_conference_message_cb(uint32_t conference_number, uint32_t peer_number, TOX_MESSAGE_TYPE type,
        const uint8_t *message, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jstring js1 = c_safe_string_from_java((char *)message, length);
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_message_cb_method, (jlong)(unsigned long long)conference_number,
                                     (jlong)(unsigned long long)peer_number,
                                     (jint) type, js1, (jlong)(unsigned long long)length);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void conference_message_cb(Tox *tox, uint32_t conference_number, uint32_t peer_number, TOX_MESSAGE_TYPE type,
                           const uint8_t *message, size_t length, void *user_data)
{
    android_tox_callback_conference_message_cb(conference_number, peer_number, type, message, length);
}

void android_tox_callback_conference_invite_cb(uint32_t friend_number, TOX_CONFERENCE_TYPE type, const uint8_t *cookie,
        size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)length);
    dbg(9, "android_tox_callback_conference_invite_cb:cookie length=%d", (int)length);
    dbg(9, "android_tox_callback_conference_invite_cb:byte 0=%d", (int)cookie[0]);
    dbg(9, "android_tox_callback_conference_invite_cb:byte end=%d", (int)cookie[length - 1]);

    if(data2 == NULL)
    {
        // return NULL; // out of memory error thrown
    }

    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)length, (const jbyte *)cookie);
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_invite_cb_method, (jlong)(unsigned long long)friend_number, (jint)type,
                                     data2, (jlong)(unsigned long long)length);
    // delete jobject --------
    (*jnienv2)->DeleteLocalRef(jnienv2, data2);
}

void conference_invite_cb(Tox *tox, uint32_t friend_number, TOX_CONFERENCE_TYPE type, const uint8_t *cookie,
                          size_t length, void *user_data)
{
    android_tox_callback_conference_invite_cb(friend_number, type, cookie, length);
}


void android_tox_callback_conference_connected_cb(uint32_t conference_number)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_conference_connected_cb_method,
                                     (jlong)(unsigned long long)conference_number);
}

void conference_connected_cb(Tox *tox, uint32_t conference_number, void *user_data)
{
    android_tox_callback_conference_connected_cb(conference_number);
}


// ------------ Conference [2] ------------
// ------------ Conference [2] ------------
// ------------ Conference [2] ------------






void android_tox_callback_file_recv_chunk_cb(uint32_t friend_number, uint32_t file_number, uint64_t position,
        const uint8_t *data, size_t length)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)length);

    if(data2 == NULL)
    {
        // return NULL; // out of memory error thrown
    }

    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)length, (const jbyte *)data);
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    // TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_file_recv_chunk_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)file_number,
                                     (jlong)(unsigned long long)position, data2, (jlong)(unsigned long long)length);
    // delete jobject --------
    (*jnienv2)->DeleteLocalRef(jnienv2, data2);
}

void file_recv_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, const uint8_t *data,
                        size_t length, void *user_data)
{
    android_tox_callback_file_recv_chunk_cb(friend_number, file_number, position, data, length);
}

void android_tox_log_cb(TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func, const char *message)
{
    if(message == NULL)
    {
        return;
    }

    if(file == NULL)
    {
        return;
    }

    if(func == NULL)
    {
        return;
    }

    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    // jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, file);
    jstring js1 = c_safe_string_from_java((const char *)file, strlen(file));
    // jstring js2 = (*jnienv2)->NewStringUTF(jnienv2, func);
    jstring js2 = c_safe_string_from_java((const char *)func, strlen(func));
    // jstring js3 = (*jnienv2)->NewStringUTF(jnienv2, message);
    jstring js3 = c_safe_string_from_java((const char *)message, strlen(message));
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity, android_tox_log_cb_method, (int)level, js1,
                                     (jlong)(unsigned long long)line, js2, js3);
    (*jnienv2)->DeleteLocalRef(jnienv2, js1);
    (*jnienv2)->DeleteLocalRef(jnienv2, js2);
    (*jnienv2)->DeleteLocalRef(jnienv2, js3);
}

void tox_log_cb__custom(Tox *tox, TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func,
                        const char *message, void *user_data)
{
    android_tox_log_cb(level, file, line, func, message);
}


// ------------- AV ------------
// ------------- AV ------------
void android_toxav_callback_call_state_cb(uint32_t friend_number, uint32_t state)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_call_state_cb_method, (jlong)(unsigned long long)friend_number, (jint)state);
}
void toxav_call_state_cb_(ToxAV *av, uint32_t friend_number, uint32_t state, void *user_data)
{
    android_toxav_callback_call_state_cb(friend_number, state);
}

void android_toxav_callback_bit_rate_status_cb(uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_bit_rate_status_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)audio_bit_rate, (jlong)(unsigned long long)video_bit_rate);
}

void toxav_bit_rate_status_cb_(ToxAV *av, uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate,
                               void *user_data)
{
    android_toxav_callback_bit_rate_status_cb(friend_number, audio_bit_rate, video_bit_rate);
}

#ifdef TOX_HAVE_TOXAV_CALLBACKS_002
void android_toxav_callback_call_comm_cb(uint32_t friend_number, TOXAV_CALL_COMM_INFO comm_value,
        int64_t comm_number)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_call_comm_cb_method, (jlong)friend_number,
                                     (jlong)comm_value, (jlong)comm_number);
}

void toxav_call_comm_cb_(ToxAV *av, uint32_t friend_number, TOXAV_CALL_COMM_INFO comm_value,
                         int64_t comm_number, void *user_data)
{
    android_toxav_callback_call_comm_cb(friend_number, comm_value, comm_number);
}
#endif

void android_toxav_callback_audio_receive_frame_cb(uint32_t friend_number, size_t sample_count, uint8_t channels,
        uint32_t sampling_rate)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_audio_receive_frame_cb_method,
                                     (jlong)(unsigned long long)friend_number,
                                     (jlong)sample_count, (jint)channels,
                                     (jlong)sampling_rate
                                    );
}

/**
 * The function type for the audio_receive_frame callback. The callback can be
 * called multiple times per single iteration depending on the amount of queued
 * frames in the buffer. The received format is the same as in send function.
 *
 * @param friend_number The friend number of the friend who sent an audio frame.
 * @param pcm An array of audio samples (sample_count * channels elements).
 * @param sample_count The number of audio samples per channel in the PCM array.
 * @param channels Number of audio channels.
 * @param sampling_rate Sampling rate used in this frame.
 *
 */
void toxav_audio_receive_frame_cb_(ToxAV *av, uint32_t friend_number, const int16_t *pcm, size_t sample_count,
                                   uint8_t channels, uint32_t sampling_rate, void *user_data)
{
    if((audio_buffer_pcm_2 != NULL) && (pcm != NULL))
    {
        memcpy((void *)audio_buffer_pcm_2, (void *)pcm, (size_t)(sample_count * channels * 2));
    }

#ifdef USE_ECHO_CANCELLATION

    if(((int)channels == 1) && ((int)sampling_rate == 48000))
    {
        filteraudio_incompatible_2 = 0;
    }
    else
    {
        filteraudio_incompatible_2 = 1;
    }

    if((filteraudio) && (pcm) && (filteraudio_active == 1) && (filteraudio_incompatible_1 == 0)
            && (filteraudio_incompatible_2 == 0))
    {
        pass_audio_output(filteraudio, pcm, (unsigned int)sample_count);
    }

#endif
#if 0
    const int8_t *pcm2 = (int8_t *)pcm;
    dbg(9, "toxav_audio_receive_frame_cb_: ch:%d r:%d - %d %d %d %d %d %d %d",
        (int)channels,
        (int)sampling_rate,
        (int8_t)pcm[0], (int8_t)pcm[1], (int8_t)pcm[2],
        (int8_t)pcm[3], (int8_t)pcm[4], (int8_t)pcm[5],
        (int8_t)pcm[6]);
#endif
    android_toxav_callback_audio_receive_frame_cb(friend_number, sample_count, channels, sampling_rate);
}

void android_toxav_callback_video_receive_frame_cb(uint32_t friend_number, uint16_t width, uint16_t height,
        int32_t ystride, int32_t ustride, int32_t vstride)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_video_receive_frame_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)width, (jlong)(unsigned long long)height,
                                     (jlong)(unsigned long long)ystride,
                                     (jlong)(unsigned long long)ustride, (jlong)(unsigned long long)vstride
                                    );
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1av_1call_1status(JNIEnv *env, jobject thiz, jint status)
{
    global_av_call_active = (uint8_t)status;
}

// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1video_1buffer(JNIEnv *env, jobject thiz, jobject buffer,
        jint frame_width_px, jint frame_height_px)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    // jclass cls = (*jnienv2)->GetObjectClass(jnienv2, buffer);
    // jmethodID mid = (*jnienv2)->GetMethodID(jnienv2, cls, "limit", "(I)Ljava/nio/Buffer;");
    video_buffer_1 = (uint8_t *)(*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
    video_buffer_1_size = (long)capacity;
    video_buffer_1_width = (int)frame_width_px;
    video_buffer_1_height = (int)frame_height_px;
    video_buffer_1_y_size = (int)(frame_width_px * frame_height_px);
    video_buffer_1_u_size = (int)(video_buffer_1_y_size / 4);
    video_buffer_1_v_size = (int)(video_buffer_1_y_size / 4);
    video_buffer_1_u = (uint8_t *)(video_buffer_1 + video_buffer_1_y_size);
    video_buffer_1_v = (uint8_t *)(video_buffer_1 + video_buffer_1_y_size + video_buffer_1_u_size);
    int written = 0;
    // (*jnienv2)->CallObjectMethod(jnienv2, buffer, mid, written);
    return written;
}
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----





// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1video_1buffer2(JNIEnv *env, jobject thiz, jobject buffer2,
        jint frame_width_px, jint frame_height_px)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    video_buffer_2 = (uint8_t *)(*jnienv2)->GetDirectBufferAddress(jnienv2, buffer2);
    dbg(9, "video_buffer_2=(call.a)%p buffer2=%p", video_buffer_2, buffer2);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer2);
    dbg(9, "video_buffer_2=(call.b)capacity");
    video_buffer_2_size = (long)capacity;
    dbg(9, "video_buffer_2=(call.b)capacity=%d", (int)video_buffer_2_size);
}
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----



// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1audio_1buffer(JNIEnv *env, jobject thiz, jobject audio_buffer)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    audio_buffer_pcm_1 = (uint8_t *)(*jnienv2)->GetDirectBufferAddress(jnienv2, audio_buffer);
    dbg(9, "audio_buffer_1=(call)%p audio_buffer=%p", audio_buffer_pcm_1, audio_buffer);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, audio_buffer);
    audio_buffer_pcm_1_size = (long)capacity;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1audio_1buffer2(JNIEnv *env, jobject thiz,
        jobject audio_buffer2)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    audio_buffer_pcm_2 = (uint8_t *)(*jnienv2)->GetDirectBufferAddress(jnienv2, audio_buffer2);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, audio_buffer2);
    audio_buffer_pcm_2_size = (long)capacity;
    dbg(9, "audio_buffer_2_=================================");
    dbg(9, "audio_buffer_2_=================================");
    dbg(9, "audio_buffer_2_=(call)%p audio_buffer2=%p size in bytes=%d", audio_buffer_pcm_2, audio_buffer2,
        (int)audio_buffer_pcm_2_size);
    dbg(9, "audio_buffer_2_=================================");
    dbg(9, "audio_buffer_2_=================================");
}

// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_restart_1filteraudio(JNIEnv *env, jobject thiz,
        jlong in_samplerate)
{
    restart_filter_audio((uint32_t)in_samplerate);
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1audio_1frame_1duration_1ms(JNIEnv *env, jobject thiz,
        jint audio_frame_duration_ms)
{
    global_audio_frame_duration_ms = (int16_t)audio_frame_duration_ms;
#ifdef USE_ECHO_CANCELLATION

    if(filteraudio)
    {
        set_delay_ms_filter_audio(0, global_audio_frame_duration_ms);
    }

#endif
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1filteraudio_1active(JNIEnv *env, jobject thiz,
        jint filteraudio_active_new)
{
#ifdef USE_ECHO_CANCELLATION

    if(((uint8_t)filteraudio_active_new == 0) || ((uint8_t)filteraudio_active_new == 1))
    {
        filteraudio_active = (uint8_t)filteraudio_active_new;
        dbg(2, "setting filteraudio_active=%d", (int)filteraudio_active);
    }

#endif
}



/*
 * @param y Luminosity plane. Size = MAX(width, abs(ystride)) * height.
 * @param u U chroma plane. Size = MAX(width/2, abs(ustride)) * (height/2).
 * @param v V chroma plane. Size = MAX(width/2, abs(vstride)) * (height/2).
 */
void toxav_video_receive_frame_cb_(ToxAV *av, uint32_t friend_number, uint16_t width, uint16_t height,
                                   const uint8_t *y, const uint8_t *u, const uint8_t *v, int32_t ystride, int32_t ustride, int32_t vstride,
                                   void *user_data)
{
    if(video_buffer_1 != NULL)
    {
        if((y) && (u) && (v))
        {
            // dbg(9, "[V0]ys=%d us=%d vs=%d",
            //    (int)video_buffer_1_y_size,
            //    (int)video_buffer_1_u_size,
            //    (int)video_buffer_1_v_size);
            int actual_y_size = max(width, abs(ystride)) * height;
            int actual_u_size = max(width/2, abs(ustride)) * (height/2);
            int actual_v_size = max(width/2, abs(vstride)) * (height/2);
            video_buffer_1_u = (uint8_t *)(video_buffer_1 + actual_y_size);
            video_buffer_1_v = (uint8_t *)(video_buffer_1 + actual_y_size + actual_u_size);

            if((actual_y_size + actual_u_size + actual_v_size) > video_buffer_1_size)
            {
                dbg(9, "Video buffer too small for incoming frame frame=%d buffer=%d",
                    (int)(actual_y_size + actual_u_size + actual_v_size),
                    (int)video_buffer_1_size);
                // clear out any data in the video buffer
                // TODO: with all "0" the video frame is all green!
                memset(video_buffer_1, 0, video_buffer_1_size);
            }
            else
            {
                // copy the Y layer into the buffer
                //dbg(9, "[V1]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
                memcpy(video_buffer_1, y, (size_t)(actual_y_size));
                // copy the U layer into the buffer
                //dbg(9, "[V2]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
                memcpy(video_buffer_1_u, u, (size_t)(actual_u_size));
                // copy the V layer into the buffer
                //dbg(9, "[V3]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
                memcpy(video_buffer_1_v, v, (size_t)(actual_v_size));
                //dbg(9, "[V4]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
            }
        }
    }

    android_toxav_callback_video_receive_frame_cb(friend_number, width, height, ystride, ustride, vstride);
}

void android_toxav_callback_call_cb(uint32_t friend_number, bool audio_enabled, bool video_enabled)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_call_cb_method, (jlong)(unsigned long long)friend_number, (jint)audio_enabled,
                                     (jint)video_enabled);
}

void toxav_call_cb_(ToxAV *av, uint32_t friend_number, bool audio_enabled, bool video_enabled, void *user_data)
{
    android_toxav_callback_call_cb(friend_number, audio_enabled, video_enabled);
}
// ------------- AV ------------
// ------------- AV ------------

// -------- _callbacks_ --------






void android_logger(int level, const char *logtext)
{
    if((TrifaToxService_class) && (logger_method) && (logtext))
    {
        if(strlen(logtext) > 0)
        {
            JNIEnv *jnienv2;
            jnienv2 = jni_getenv();
            // jstring js2 = (*jnienv2)->NewStringUTF(jnienv2, logtext);
            jstring js2 = c_safe_string_from_java((const char *)logtext, strlen(logtext));
            (*jnienv2)->CallStaticVoidMethod(jnienv2, TrifaToxService_class, logger_method, level, js2);
            (*jnienv2)->DeleteLocalRef(jnienv2, js2);
        }
    }
}

void yieldcpu(uint32_t ms)
{
    usleep(1000 * ms);
}

void *thread_av(void *data)
{
    JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
    JNIEnv *env;
    (*cachedJVM)->AttachCurrentThread(cachedJVM, &env, &args);
    dbg(9, "2001");
    ToxAV *av = (ToxAV *) data;
    dbg(9, "2002");
    pthread_t id = pthread_self();
    dbg(9, "2003");
    pthread_mutex_t av_thread_lock;
    dbg(9, "2004");

    if(pthread_mutex_init(&av_thread_lock, NULL) != 0)
    {
        dbg(0, "Error creating av_thread_lock");
    }
    else
    {
        dbg(2, "av_thread_lock created successfully");
    }

    dbg(2, "AV Thread #%d: starting", (int) id);

    while(toxav_iterate_thread_stop != 1)
    {
        // usleep(toxav_iteration_interval(av) * 1000);
        yieldcpu(100);
    }

    dbg(2, "ToxVideo:Clean thread exit!\n");
    (*cachedJVM)->DetachCurrentThread(cachedJVM);
    env = NULL;
    return (void *)NULL;
}


void *thread_video_av(void *data)
{
    JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
    JNIEnv *env;
    (*cachedJVM)->AttachCurrentThread(cachedJVM, &env, &args);
    dbg(9, "2001");
    ToxAV *av = (ToxAV *) data;
    dbg(9, "2002");
    pthread_t id = pthread_self();
    dbg(9, "2003");
    // pthread_mutex_t av_thread_lock;
    dbg(9, "2004");
#if 0

    if(pthread_mutex_init(&av_thread_lock, NULL) != 0)
    {
        dbg(0, "Error creating video av_thread_lock");
    }
    else
    {
        dbg(2, "av_thread_lock video created successfully");
    }

#endif
    dbg(2, "AV video Thread #%d: starting", (int) id);
    long av_iterate_interval = 1;

    while(toxav_video_thread_stop != 1)
    {
        // pthread_mutex_lock(&av_thread_lock);
        toxav_iterate(av);
        // dbg(9, "AV video Thread #%d running ...", (int) id);
        // pthread_mutex_unlock(&av_thread_lock);
        av_iterate_interval = toxav_iteration_interval(av);

        if((av_iterate_interval / 2) < 1)
        {
            usleep(1 * 1000);
        }
        else
        {
            //usleep((av_iterate_interval / 2) * 1000);
            if(global_av_call_active == 1)
            {
                usleep(8 * 1000);
            }
            else
            {
                usleep(800 * 1000);
            }
        }
    }

    dbg(2, "ToxVideo:Clean video thread exit!\n");
    (*cachedJVM)->DetachCurrentThread(cachedJVM);
    env = NULL;
    return (void *)NULL;
}

void *thread_audio_av(void *data)
{
    JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
    JNIEnv *env;
    (*cachedJVM)->AttachCurrentThread(cachedJVM, &env, &args);
    ToxAV *av = (ToxAV *) data;
    pthread_t id = pthread_self();
    dbg(2, "AV audio Thread #%d: starting", (int) id);
    long av_iterate_interval = 1;

    while(toxav_audio_thread_stop != 1)
    {
        toxav_audio_iterate(av);
        // dbg(9, "AV audio Thread #%d running ...", (int) id);
        av_iterate_interval = toxav_iteration_interval(av);

        if((av_iterate_interval / 2) < 1)
        {
            usleep(1 * 1000);
        }
        else
        {
            //usleep((av_iterate_interval / 2) * 1000);
            if(global_av_call_active == 1)
            {
                usleep(8 * 1000);
            }
            else
            {
                usleep(800 * 1000);
            }
        }
    }

    dbg(2, "ToxVideo:Clean audio thread exit!\n");
    (*cachedJVM)->DetachCurrentThread(cachedJVM);
    env = NULL;
    return (void *)NULL;
}


void Java_com_zoffcc_applications_trifa_MainActivity_init__real(JNIEnv *env, jobject thiz, jobject datadir,
        jint udp_enabled, jint local_discovery_enabled, jint orbot_enabled, jstring proxy_host, jlong proxy_port,
        jstring passphrase_j)
{
    const char *s = NULL;
    // SET GLOBAL JNIENV here, this is bad!!
    // SET GLOBAL JNIENV here, this is bad!!
    // SET GLOBAL JNIENV here, this is bad!!
    // jnienv = env;
    // dbg(0,"jnienv=%p", env);
    // SET GLOBAL JNIENV here, this is bad!!
    // SET GLOBAL JNIENV here, this is bad!!
    // SET GLOBAL JNIENV here, this is bad!!
    // ------------------- *********** -------------------
    // ------------------- *********** -------------------
    // ------------------- *********** -------------------
    TrifaToxService_class = NULL;
    android_find_class_global("com/zoffcc/applications/trifa/TrifaToxService", &TrifaToxService_class);
    logger_method = (*env)->GetStaticMethodID(env, TrifaToxService_class, "logger", "(ILjava/lang/String;)V");
    safe_string_method = (*env)->GetStaticMethodID(env, TrifaToxService_class, "safe_string", "([B)Ljava/lang/String;");
    dbg(9, "TrifaToxService=%p", TrifaToxService_class);
    dbg(9, "safe_string_method=%p", safe_string_method);
    dbg(9, "logger_method=%p", logger_method);
    // ------------------- *********** -------------------
    // ------------------- *********** -------------------
    // ------------------- *********** -------------------
    jclass cls_local = (*env)->GetObjectClass(env, thiz);
    MainActivity = (*env)->NewGlobalRef(env, cls_local);
    // logger_method = (*env)->GetStaticMethodID(env, MainActivity, "logger", "(ILjava/lang/String;)V");
    dbg(9, "cls_local=%p", cls_local);
    dbg(9, "MainActivity=%p", MainActivity);
    dbg(9, "Logging test ---***---");
    int thread_id = gettid();
    dbg(9, "THREAD ID=%d", thread_id);
    s = (*env)->GetStringUTFChars(env, datadir, NULL);
    app_data_dir = strdup(s);
    dbg(9, "app_data_dir=%s", app_data_dir);
    (*env)->ReleaseStringUTFChars(env, datadir, s);
    s = (*env)->GetStringUTFChars(env, passphrase_j, NULL);
    char *passphrase = strdup(s);
    // WARNING // dbg(9, "passphrase=%s", passphraseXX);
    (*env)->ReleaseStringUTFChars(env, passphrase_j, s);
    size_t passphrase_len = (size_t)strlen(passphrase);
    // jclass class2 = NULL;
    // android_find_class_global("com/zoffcc/applications/trifa/MainActivity", &class2);
    // dbg(9, "class2=%p", class2);
    // safe_string_method = (*env)->GetStaticMethodID(env, MainActivity, "safe_string", "([B)Ljava/lang/String;");
    // jmethodID test_method = NULL;
    // android_find_method(class2, "test", "(I)V", &test_method);
    // dbg(9, "test_method=%p", test_method);
    // (*env)->CallVoidMethod(env, thiz, test_method, 79);
    // -------- _callbacks_ --------
    dbg(9, "linking callbacks ... START");
    android_tox_callback_self_connection_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_self_connection_status_cb_method", "(I)V");
    android_tox_callback_friend_name_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_name_cb_method", "(JLjava/lang/String;J)V");
    android_tox_callback_friend_status_message_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_status_message_cb_method", "(JLjava/lang/String;J)V");
    android_tox_callback_friend_lossless_packet_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_lossless_packet_cb_method", "(J[BJ)V");
    android_tox_callback_friend_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_status_cb_method", "(JI)V");
    android_tox_callback_friend_connection_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_connection_status_cb_method", "(JI)V");
    android_tox_callback_friend_typing_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_typing_cb_method", "(JI)V");
    android_tox_callback_friend_read_receipt_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_read_receipt_cb_method", "(JJ)V");
    android_tox_callback_friend_request_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_request_cb_method", "(Ljava/lang/String;Ljava/lang/String;J)V");
    android_tox_callback_friend_message_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_message_cb_method", "(JILjava/lang/String;J)V");
    android_tox_callback_friend_message_v2_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_message_v2_cb_method", "(JLjava/lang/String;JJJ[BJ)V");
    android_tox_callback_friend_sync_message_v2_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_sync_message_v2_cb_method", "(JJJ[BJ[BJ)V");
    android_tox_callback_friend_read_receipt_message_v2_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_friend_read_receipt_message_v2_cb_method", "(JJ[B)V");
    android_tox_callback_file_recv_control_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_file_recv_control_cb_method", "(JJI)V");
    android_tox_callback_file_chunk_request_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_file_chunk_request_cb_method", "(JJJJ)V");
    android_tox_callback_file_recv_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_file_recv_cb_method", "(JJIJLjava/lang/String;J)V");
    android_tox_callback_file_recv_chunk_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_file_recv_chunk_cb_method", "(JJJ[BJ)V");
    android_tox_callback_conference_invite_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_invite_cb_method", "(JI[BJ)V");
    android_tox_callback_conference_connected_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_connected_cb_method", "(J)V");
    android_tox_callback_conference_message_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_message_cb_method", "(JJILjava/lang/String;J)V");
    android_tox_callback_conference_title_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_title_cb_method", "(JJLjava/lang/String;J)V");
    android_tox_callback_conference_peer_name_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_peer_name_cb_method", "(JJLjava/lang/String;J)V");
    android_tox_callback_conference_peer_list_changed_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_peer_list_changed_cb_method", "(J)V");
    android_tox_callback_conference_namelist_change_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_tox_callback_conference_namelist_change_cb_method", "(JJI)V");
    android_tox_log_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_log_cb_method",
                                "(ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;)V");
    dbg(9, "linking callbacks ... READY");
    // -------- _callbacks_ --------
    start_filter_audio(recording_samling_rate);
    set_delay_ms_filter_audio(0, global_audio_frame_duration_ms);
    // -------- resumable FTs: not working fully yet, so turn it off --------
    tox_set_filetransfer_resumable(true);
    // tox_set_filetransfer_resumable(false);
    // -------- resumable FTs: not working fully yet, so turn it off --------
    // ----------- create Tox instance -----------
    const char *proxy_host_str = (*env)->GetStringUTFChars(env, proxy_host, NULL);
    tox_global = create_tox((int)udp_enabled, (int)orbot_enabled, (const char *)proxy_host_str, (uint16_t)proxy_port,
                            (int)local_discovery_enabled, (const uint8_t *)passphrase, (size_t)passphrase_len);
    (*env)->ReleaseStringUTFChars(env, proxy_host, proxy_host_str);
    dbg(9, "tox_global=%p", tox_global);
    // ----------- create Tox instance -----------
    // dbg(9, "1001");
    // const char *name = "TRIfA";
    // dbg(9, "1002");
    // tox_self_set_name(tox_global, (uint8_t *)name, strlen(name), NULL);
    // dbg(9, "1003");
    // const char *status_message = "This is TRIfA";
    // dbg(9, "1004");
    // tox_self_set_status_message(tox_global, (uint8_t *)status_message, strlen(status_message), NULL);
    // dbg(9, "1005");
    // ----------- create Tox AV instance --------
    TOXAV_ERR_NEW rc;
    dbg(2, "new Tox AV");
    tox_av_global = toxav_new(tox_global, &rc);

    if(rc != TOXAV_ERR_NEW_OK)
    {
        dbg(0, "Error at toxav_new: %d", rc);
    }

    global_toxav_valid = true;
    memset(&mytox_CC, 0, sizeof(CallControl));
    // ----------- create Tox AV instance --------
    toxav_audio_iterate_seperation(tox_av_global, true);
    // init AV callbacks -------------------------------
    dbg(9, "linking AV callbacks ... START");
    android_toxav_callback_call_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
                                            "android_toxav_callback_call_cb_method", "(JII)V");
    toxav_callback_call(tox_av_global, toxav_call_cb_, &mytox_CC);
    android_toxav_callback_video_receive_frame_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_video_receive_frame_cb_method", "(JJJJJJ)V");
    toxav_callback_video_receive_frame(tox_av_global, toxav_video_receive_frame_cb_, &mytox_CC);
    android_toxav_callback_call_state_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_call_state_cb_method", "(JI)V");
    toxav_callback_call_state(tox_av_global, toxav_call_state_cb_, &mytox_CC);
    android_toxav_callback_bit_rate_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_bit_rate_status_cb_method", "(JJJ)V");
    toxav_callback_bit_rate_status(tox_av_global, toxav_bit_rate_status_cb_, &mytox_CC);
    android_toxav_callback_audio_receive_frame_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_audio_receive_frame_cb_method", "(JJIJ)V");
    toxav_callback_audio_receive_frame(tox_av_global, toxav_audio_receive_frame_cb_, &mytox_CC);
#ifdef TOX_HAVE_TOXAV_CALLBACKS_002
    android_toxav_callback_call_comm_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_call_comm_cb_method", "(JJJ)V");
    toxav_callback_call_comm(tox_av_global, toxav_call_comm_cb_, &mytox_CC);
#endif
    dbg(9, "linking AV callbacks ... READY");
    // init AV callbacks -------------------------------
    // start toxav thread ------------------------------
    toxav_iterate_thread_stop = 0;

    if(pthread_create(&(tid[0]), NULL, thread_av, (void *)tox_av_global) != 0)
    {
        dbg(0, "AV iterate Thread create failed");
    }
    else
    {
        dbg(2, "AV iterate Thread successfully created");
    }

    toxav_video_thread_stop = 0;

    if(pthread_create(&(tid[1]), NULL, thread_video_av, (void *)tox_av_global) != 0)
    {
        dbg(0, "AV video Thread create failed");
    }
    else
    {
        dbg(2, "AV video Thread successfully created");
    }

    toxav_audio_thread_stop = 0;

    if(pthread_create(&(tid[2]), NULL, thread_audio_av, (void *)tox_av_global) != 0)
    {
        dbg(0, "AV audio Thread create failed");
    }
    else
    {
        dbg(2, "AV audio Thread successfully created");
    }

    // start toxav thread ------------------------------

    if(passphrase)
    {
        free(passphrase);
    }

    // if (s)
    //{
    //  free((void*)s);
    //}
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_init(JNIEnv *env, jobject thiz, jobject datadir, jint udp_enabled,
        jint local_discovery_enabled, jint orbot_enabled, jstring proxy_host, jlong proxy_port, jstring passphrase_j)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_init__real(env, thiz, datadir, udp_enabled,
                   local_discovery_enabled, orbot_enabled, proxy_host, proxy_port, passphrase_j));
}


// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
void Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file__real(JNIEnv *env, jobject thiz,
        jstring passphrase_j)
{
    if(tox_global == NULL)
    {
        return;
    }

    const char *s = (*env)->GetStringUTFChars(env, passphrase_j, NULL);
    char *passphrase = strdup(s);
    // WARNING // dbg(9, "passphrase=%s", passphraseXX);
    (*env)->ReleaseStringUTFChars(env, passphrase_j, s);
    size_t passphrase_len = (size_t)strlen(passphrase);
    dbg(9, "update_savedata_file");
    update_savedata_file(tox_global, (const uint8_t *)passphrase, (size_t)passphrase_len);

    if(passphrase)
    {
        free(passphrase);
    }

    //if (s)
    //{
    //  free((void*)s);
    //}
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file(JNIEnv *env, jobject thiz, jstring passphrase_j)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file__real(env, thiz,
                   passphrase_j));
}

// -----------------
// -----------------
// -----------------
int add_tcp_relay_single(Tox *tox, const char *ip, uint16_t port, const char *key_hex)
{
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
    toxid_hex_to_bin(key_bin, key_hex);
    int res1 = sodium_hex2bin(key_bin, sizeof(key_bin), key_hex, sizeof(key_hex)-1, NULL, NULL, NULL);
    dbg(9, "sodium_hex2bin:res=%d", res1);
    TOX_ERR_BOOTSTRAP error;
    bool res = tox_add_tcp_relay(tox, ip, port, key_bin, &error); // also try as TCP relay

    if(res != true)
    {
        if(error == TOX_ERR_BOOTSTRAP_OK)
        {
            return 0;
        }
        else if(error == TOX_ERR_BOOTSTRAP_NULL)
        {
            return 1;
        }
        else if(error == TOX_ERR_BOOTSTRAP_BAD_HOST)
        {
            return 2;
        }
        else if(error == TOX_ERR_BOOTSTRAP_BAD_PORT)
        {
            return 3;
        }
        else
        {
            return 99;
        }
    }
    else
    {
        return 0;
    }
}

int Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single__real(JNIEnv *env, jobject thiz, jstring ip,
        jstring key_hex, long port)
{
    dbg(9, "add_tcp_relay_single1");
    const char *key_hex_str = NULL;
    const char *ip_str = NULL;
    key_hex_str = (*env)->GetStringUTFChars(env, key_hex, NULL);
    char *key_hex_str2 = strdup(key_hex_str);
    ip_str = (*env)->GetStringUTFChars(env, ip, NULL);
    char *ip_str2 = strdup(ip_str);
    int res = add_tcp_relay_single(tox_global, ip_str2, (uint16_t)port, key_hex_str2);
    (*env)->ReleaseStringUTFChars(env, ip, ip_str);
    (*env)->ReleaseStringUTFChars(env, key_hex, key_hex_str);

    if(ip_str2)
    {
        free(ip_str2);
    }

    if(key_hex_str2)
    {
        free(key_hex_str2);
    }

    return res;
}

JNIEXPORT int JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single(JNIEnv *env, jobject thiz, jstring ip,
        jstring key_hex, long port)
{
    jint retcode = 0;
    COFFEE_TRY_JNI(env, retcode = Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single__real(env, thiz,
                                  ip, key_hex, port));
    return retcode;
}

int bootstrap_single(Tox *tox, const char *ip, uint16_t port, const char *key_hex)
{
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
    toxid_hex_to_bin(key_bin, key_hex);
    int res1 = sodium_hex2bin(key_bin, sizeof(key_bin), key_hex, sizeof(key_hex)-1, NULL, NULL, NULL);
    dbg(9, "sodium_hex2bin:res=%d", res1);
    TOX_ERR_BOOTSTRAP error;
    bool res = tox_bootstrap(tox, ip, port, key_bin, &error);

    if(res != true)
    {
        if(error == TOX_ERR_BOOTSTRAP_OK)
        {
            return 0;
        }
        else if(error == TOX_ERR_BOOTSTRAP_NULL)
        {
            return 1;
        }
        else if(error == TOX_ERR_BOOTSTRAP_BAD_HOST)
        {
            return 2;
        }
        else if(error == TOX_ERR_BOOTSTRAP_BAD_PORT)
        {
            return 3;
        }
        else
        {
            return 99;
        }
    }
    else
    {
        return 0;
    }
}

int Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single__real(JNIEnv *env, jobject thiz, jobject ip,
        jobject key_hex, long port)
{
    dbg(9, "bootstrap_single");
    const char *ip_str = (*env)->GetStringUTFChars(env, ip, NULL);
    const char *key_hex_str = (*env)->GetStringUTFChars(env, key_hex, NULL);
    int res = bootstrap_single(tox_global, ip_str, (uint16_t)port, key_hex_str);
    (*env)->ReleaseStringUTFChars(env, key_hex, key_hex_str);
    (*env)->ReleaseStringUTFChars(env, ip, ip_str);
    return res;
}

JNIEXPORT int JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single(JNIEnv *env, jobject thiz, jobject ip,
        jobject key_hex, long port)
{
    jint retcode = 0;
    COFFEE_TRY_JNI(env, retcode = Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single__real(env, thiz, ip,
                                  key_hex, port));
    return retcode;
}
// -----------------
// -----------------
// -----------------






JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_get_1my_1toxid(JNIEnv *env, jobject thiz)
{
    jstring result;
    dbg(9, "get_my_toxid");
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];

    if(tox_global == NULL)
    {
        dbg(9, "get_my_toxid:NULL:1");
        return (jstring)NULL;
    }

    get_my_toxid(tox_global, tox_id_hex);
    // dbg(2, "MyToxID:%s", tox_id_hex);
    result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
    return result;
}


void Java_com_zoffcc_applications_trifa_MainActivity_bootstrap__real(JNIEnv *env, jobject thiz)
{
    dbg(9, "bootstrap");
    bootstrap();
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_bootstrap(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_bootstrap__real(env, thiz));
}


void Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks__real(JNIEnv *env, jobject thiz)
{
    dbg(9, "init_tox_callbacks");
    init_tox_callbacks();
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks__real(env, thiz));
}



void Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate__real(JNIEnv *env, jobject thiz)
{
    // dbg(9, "tox_iterate ... START");
    tox_iterate(tox_global, NULL);
    // dbg(9, "tox_iterate ... READY");
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate__real(env, thiz));
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1friend_1list_1size(JNIEnv *env, jobject thiz)
{
    size_t numfriends = tox_self_get_friend_list_size(tox_global);
    return (jlong)(unsigned long long)numfriends;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1public_1key(JNIEnv *env, jobject thiz,
        jlong friend_number)
{
    jstring result;

    if(tox_global == NULL)
    {
        return (jstring)NULL;
    }

    uint8_t public_key[TOX_PUBLIC_KEY_SIZE];
    TOX_ERR_FRIEND_GET_PUBLIC_KEY error;
    bool res = tox_friend_get_public_key(tox_global, (uint32_t)friend_number, public_key, &error);

    if(res == false)
    {
        result = (*env)->NewStringUTF(env, "-1"); // C style string to Java String
    }
    else
    {
        char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1]; // need this wrong size for next call
        CLEAR(tox_id_hex);
        toxid_bin_to_hex(public_key, tox_id_hex);
        tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key
        result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
    }

    return result;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1by_1public_1key(JNIEnv *env, jobject thiz,
        jobject public_key_str)
{
    if(tox_global == NULL)
    {
        return (jlong)-1;
    }

    unsigned char public_key_bin[TOX_PUBLIC_KEY_SIZE];
    char *public_key_str2 = NULL;
    const char *s = NULL;

    if(public_key_str == NULL)
    {
        return (jlong)-1;
    }

    s = (*env)->GetStringUTFChars(env, public_key_str, NULL);

    if(s == NULL)
    {
        (*env)->ReleaseStringUTFChars(env, public_key_str, s);
        return (jlong)-1;
    }

    public_key_str2 = strdup(s);
    (*env)->ReleaseStringUTFChars(env, public_key_str, s);
    toxid_hex_to_bin(public_key_bin, public_key_str2);
    TOX_ERR_FRIEND_BY_PUBLIC_KEY error;
    uint32_t friendnum = tox_friend_by_public_key(tox_global, (uint8_t *)public_key_bin, &error);

    if(public_key_str2)
    {
        free(public_key_str2);
    }

    if(error != TOX_ERR_FRIEND_BY_PUBLIC_KEY_OK)
    {
        return (jlong)-1;
    }
    else
    {
        return (jlong)friendnum;
    }
}

JNIEXPORT jlongArray JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1friend_1list(JNIEnv *env, jobject thiz)
{
    size_t numfriends = tox_self_get_friend_list_size(tox_global);
    size_t memsize = (numfriends * sizeof(uint32_t));
    uint32_t *friend_list = malloc(memsize);
    uint32_t *friend_list_iter = friend_list;
    jlongArray result;
    tox_self_get_friend_list(tox_global, friend_list);
    result = (*env)->NewLongArray(env, numfriends);

    if(result == NULL)
    {
        // TODO this would be bad!!
    }

    jlong buffer[numfriends];
    size_t i = 0;

    for(i=0; i<numfriends; i++)
    {
        buffer[i] = (long)friend_list_iter[i];
    }

    (*env)->SetLongArrayRegion(env, result, 0, numfriends, buffer);

    if(friend_list)
    {
        free(friend_list);
    }

    return result;
}


void Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill__real(JNIEnv *env, jobject thiz)
{
    global_toxav_valid = false;
    dbg(9, "tox_kill ... START");
    stop_filter_audio();
    toxav_iterate_thread_stop = 1;
    pthread_join(tid[0], NULL); // wait for toxav iterate thread to end
    toxav_video_thread_stop = 1;
    pthread_join(tid[1], NULL); // wait for toxav video thread to end
    toxav_audio_thread_stop = 1;
    pthread_join(tid[2], NULL); // wait for toxav audio thread to end
    toxav_kill(tox_av_global);
    tox_av_global = NULL;
#ifdef TOX_HAVE_TOXUTIL
    tox_utils_kill(tox_global);
#else
    tox_kill(tox_global);
#endif
    tox_global = NULL;
    dbg(9, "tox_kill ... READY");
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill__real(env, thiz));
}


void Java_com_zoffcc_applications_trifa_MainActivity_exit__real(JNIEnv *env, jobject thiz)
{
    dbg(9, "Exit Program");
    exit(0);
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_exit(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_exit__real(env, thiz));
}




JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1iteration_1interval(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_iteration_interval(tox_global);
    // dbg(9, "tox_iteration_interval=%lld", (long long)l);
    return (jlong)(unsigned long long)l;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1max_1message_1length(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_max_message_length();
    return (jlong)(unsigned long long)l;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1id_1length(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_file_id_length();
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1max_1filename_1length(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_max_filename_length();
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1major(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_version_major();
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1minor(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_version_minor();
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1patch(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_version_patch();
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_jnictoxcore_1version(JNIEnv *env, jobject thiz)
{
    return (*env)->NewStringUTF(env, global_version_string);
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1util_1friend_1send_1msg_1receipt_1v2(JNIEnv *env,
        jobject thiz, jlong friend_number, jlong ts_sec,
        jobject msgid_buffer)
{
#ifdef TOX_HAVE_TOXUTIL

    if(msgid_buffer == NULL)
    {
        return (jint)-3;
    }

    uint8_t *msgid_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, msgid_buffer);
    long msgid_buffer_capacity = (*env)->GetDirectBufferCapacity(env, msgid_buffer);
    bool res = tox_util_friend_send_msg_receipt_v2(tox_global,
               (uint32_t)friend_number, msgid_buffer_c, (uint32_t)ts_sec);

    if(res == true)
    {
        return (jint)0;
    }
    else
    {
        return (jint)1;
    }

#else
    return (jint)-99;
#endif
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1util_1friend_1resend_1message_1v2(JNIEnv *env,
        jobject thiz, jlong friend_number,
        jobject raw_message_buffer,
        jlong raw_msg_len)
{
#ifdef TOX_HAVE_TOXUTIL

    if(raw_message_buffer == NULL)
    {
        return (jint)-2;
    }

    if(raw_msg_len < 1)
    {
        return (jint)-3;
    }

    long capacity = 0;
    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    TOX_ERR_FRIEND_SEND_MESSAGE error;
    bool res = tox_util_friend_resend_message_v2(tox_global, (uint32_t) friend_number,
               (const uint8_t *)raw_message_buffer_c,
               (const uint32_t)raw_msg_len,
               &error);
    (*env)->ReleaseStringUTFChars(env, raw_message_buffer, raw_message_buffer_c);

    if(res == false)
    {
        return (jint)-1;
    }
    else
    {
        return (jint)0;
    }

#else
    return (jint)-99;
#endif
}



JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1util_1friend_1send_1message_1v2(JNIEnv *env,
        jobject thiz, jlong friend_number, jint type, jlong ts_sec,
        jobject message, jlong length,
        jobject raw_message_back_buffer,
        jobject raw_msg_len_back,
        jobject msgid_back_buffer)
{
#ifdef TOX_HAVE_TOXUTIL
    long capacity = 0;

    if(raw_message_back_buffer == NULL)
    {
        return (jlong)-9991;
    }

    if(msgid_back_buffer == NULL)
    {
        return (jlong)-9991;
    }

    if(raw_msg_len_back == NULL)
    {
        return (jlong)-9991;
    }

    uint8_t *raw_message_back_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_back_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, raw_message_back_buffer);
    uint8_t *msgid_back_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, msgid_back_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, msgid_back_buffer);
    uint8_t *raw_msg_len_back_c_2 = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_msg_len_back);
    capacity = (*env)->GetDirectBufferCapacity(env, raw_msg_len_back);
    uint32_t raw_msg_len_back_c;
    const char *message_str = NULL;
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_FRIEND_SEND_MESSAGE error;
    int64_t res = tox_util_friend_send_message_v2(tox_global, (uint32_t) friend_number,
                  (int)type, (uint32_t) ts_sec,
                  (const uint8_t *)message_str, (size_t)strlen(message_str),
                  (uint8_t *)raw_message_back_buffer_c, &raw_msg_len_back_c, (uint8_t *)msgid_back_buffer_c,
                  &error);
    (*env)->ReleaseStringUTFChars(env, message, message_str);
    // HINT: give number back as 2 bytes in ByteBuffer
    //       a bit hacky, but it works
    raw_msg_len_back_c_2[0] = (uint8_t)(raw_msg_len_back_c % 256); // low byte
    raw_msg_len_back_c_2[1] = (uint8_t)(raw_msg_len_back_c / 256); // high byte

    if(res == -1)
    {
        // MSG V2 was used to send message
        if(error == 0)
        {
            // return OK
            return (jlong)-9999;
        }

        // otherwise give some error
        return (jlong)-9991;
    }

    if(error != 0)
    {
        if(error == TOX_ERR_FRIEND_SEND_MESSAGE_NULL)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_NULL");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED");
            return (jlong)-3;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ");
            return (jlong)-4;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG");
            return (jlong)-5;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY)
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY");
            return (jlong)-6;
        }
        else
        {
            dbg(9, "tox_util_friend_send_message_v2:ERROR:%d", (int)error);
            return (jlong)-99;
        }
    }
    else
    {
        dbg(9, "tox_util_friend_send_message_v2");
        return (jlong)res;
    }

#else
    return (jlong)-99;
#endif
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1message(JNIEnv *env, jobject thiz,
        jlong friend_number, jint type, jobject message)
{
    const char *message_str = NULL;
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_FRIEND_SEND_MESSAGE error;
    uint32_t res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)message_str,
                                           (size_t)strlen(message_str), &error);
    (*env)->ReleaseStringUTFChars(env, message, message_str);

    if(error != 0)
    {
        if(error == TOX_ERR_FRIEND_SEND_MESSAGE_NULL)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_NULL");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED");
            return (jlong)-3;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ");
            return (jlong)-4;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG");
            return (jlong)-5;
        }
        else if(error == TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY)
        {
            dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY");
            return (jlong)-6;
        }
        else
        {
            dbg(9, "tox_friend_send_message:ERROR:%d", (int)error);
            return (jlong)-99;
        }
    }
    else
    {
        dbg(9, "tox_friend_send_message");
        return (jlong)(unsigned long long)res;
    }
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1lossless_1packet(JNIEnv *env, jobject thiz,
        jlong friend_number, jbyteArray data, jint data_length)
{
    jbyte *data2 = (*env)->GetByteArrayElements(env, data, 0);
    TOX_ERR_FRIEND_CUSTOM_PACKET error;
    uint32_t res = tox_friend_send_lossless_packet(tox_global, (uint32_t)friend_number, (const uint8_t *)data2,
                   (size_t)data_length, &error);
    (*env)->ReleaseByteArrayElements(env, data, data2, JNI_ABORT); /* abort to not copy back contents */

    if(error != 0)
    {
        dbg(9, "tox_friend_send_lossless_packet:ERROR:%d", (int)error);
        return (jlong)-99;
    }
    else
    {
        dbg(9, "tox_friend_send_lossless_packet");
        return (jlong)(unsigned long long)res;
    }
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add(JNIEnv *env, jobject thiz, jobject toxid_str,
        jobject message)
{
    unsigned char public_key_bin[TOX_ADDRESS_SIZE];
    char *public_key_str2 = NULL;
    const char *s = NULL;
    const char *message_str = NULL;
    s = (*env)->GetStringUTFChars(env, toxid_str, NULL);
    dbg(9, "add friend:s=%p", s);
    public_key_str2 = strdup(s);
    dbg(9, "add friend:public_key_str2=%p", public_key_str2);
    dbg(9, "add friend:TOX_PUBLIC_KEY_SIZE len=%d", (int)TOX_ADDRESS_SIZE);
    dbg(9, "add friend:public_key_str2 len=%d", strlen(public_key_str2));
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_FRIEND_ADD error;
    toxid_hex_to_bin(public_key_bin, public_key_str2);
    dbg(9, "add friend:public_key_bin=%p", public_key_bin);
    dbg(9, "add friend:public_key_bin len=%d", strlen(public_key_bin));
    dbg(9, "add friend:message_str=%p", message_str);
    uint32_t friendnum = tox_friend_add(tox_global, (uint8_t *)public_key_bin, (uint8_t *)message_str,
                                        (size_t)strlen(message_str), &error);

    if(public_key_str2)
    {
        free(public_key_str2);
    }

    (*env)->ReleaseStringUTFChars(env, message, message_str);
    (*env)->ReleaseStringUTFChars(env, toxid_str, s);

    if(error != 0)
    {
        if(error == TOX_ERR_FRIEND_ADD_ALREADY_SENT)
        {
            dbg(9, "add friend:ERROR:TOX_ERR_FRIEND_ADD_ALREADY_SENT");
            return (jlong)-1;
        }
        else
        {
            dbg(9, "add friend:ERROR:%d", (int)error);
            return (jlong)-2;
        }
    }
    else
    {
        dbg(9, "add friend");
        return (jlong)(unsigned long long)friendnum;
    }
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add_1norequest(JNIEnv *env, jobject thiz,
        jobject public_key_str)
{
    unsigned char public_key_bin[TOX_PUBLIC_KEY_SIZE];
    char *public_key_str2 = NULL;
    const char *s = NULL;
    s = (*env)->GetStringUTFChars(env, public_key_str, NULL);
    public_key_str2 = strdup(s);
    (*env)->ReleaseStringUTFChars(env, public_key_str, s);
    toxid_hex_to_bin(public_key_bin, public_key_str2);
    uint32_t friendnum = tox_friend_add_norequest(tox_global, (uint8_t *)public_key_bin, NULL);

    if(public_key_str2)
    {
        free(public_key_str2);
    }

    dbg(9, "add friend norequest");
    return (jlong)(unsigned long long)friendnum;
}



JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1name(JNIEnv *env, jobject thiz, jobject name)
{
    if(tox_global == NULL)
    {
        return (jint)-1;
    }

    const char *s = NULL;
    s = (*env)->GetStringUTFChars(env, name, NULL);
    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_name(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
    (*env)->ReleaseStringUTFChars(env, name, s);
    return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message(JNIEnv *env, jobject thiz,
        jobject status_message)
{
    if(tox_global == NULL)
    {
        return (jint)-1;
    }

    const char *s = NULL;
    s = (*env)->GetStringUTFChars(env, status_message, NULL);
    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_status_message(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
    (*env)->ReleaseStringUTFChars(env, status_message, s);
    return (jint)res;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status(JNIEnv *env, jobject thiz, jint status)
{
    if(tox_global == NULL)
    {
        return;
    }

    tox_self_set_status(tox_global, (TOX_USER_STATUS)status);
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1typing(JNIEnv *env, jobject thiz, jlong friend_number,
        jint typing)
{
    TOX_ERR_SET_TYPING error;
    bool res = tox_self_set_typing(tox_global, (uint32_t)friend_number, (bool)typing, &error);
    return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1status(JNIEnv *env, jobject thiz,
        jlong friend_number)
{
    TOX_ERR_FRIEND_QUERY error;
    TOX_CONNECTION res = tox_friend_get_connection_status(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1delete(JNIEnv *env, jobject thiz, jlong friend_number)
{
    TOX_ERR_FRIEND_DELETE error;
#ifdef TOX_HAVE_TOXUTIL
    bool res = tox_utils_friend_delete(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
#else
    bool res = tox_friend_delete(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
#endif
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name(JNIEnv *env, jobject thiz)
{
    size_t length = tox_self_get_name_size(tox_global);
    char name[length + 1];
    CLEAR(name);
    // dbg(9, "name len=%d", (int)length);
    tox_self_get_name(tox_global, name);
    // dbg(9, "name=%s", (char *)name);
    // return (*env)->NewStringUTF(env, (uint8_t *)name);
    jstring js1 = c_safe_string_from_java((char *)name, length);
    return js1;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name_1size(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_self_get_name_size(tox_global);
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message_1size(JNIEnv *env, jobject thiz)
{
    long long l = (long long)tox_self_get_status_message_size(tox_global);
    return (jlong)(unsigned long long)l;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message(JNIEnv *env, jobject thiz)
{
    size_t length = tox_self_get_status_message_size(tox_global);
    char message[length + 1];
    CLEAR(message);
    tox_self_get_status_message(tox_global, message);
    jstring js1 = c_safe_string_from_java((char *)message, length);
    return js1;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1control(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jint control)
{
    TOX_ERR_FILE_CONTROL error;
    bool res = tox_file_control(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (TOX_FILE_CONTROL)control,
                                &error);

    if(res == true)
    {
        return (jint)0;
    }
    else
    {
        return (jint)-1;
    }
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1hash(JNIEnv *env, jobject thiz, jobject hash_buffer,
        jobject data_buffer, jlong data_length)
{
    uint8_t *hash_buffer_c = NULL;
    long capacity_hash = 0;
    uint8_t *data_buffer_c = NULL;
    long capacity_data = 0;
    hash_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, hash_buffer);
    capacity_hash = (*env)->GetDirectBufferCapacity(env, hash_buffer);

    if(capacity_hash < TOX_HASH_LENGTH)
    {
        return -2;
    }

    if(data_buffer != NULL)
    {
        data_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, data_buffer);
        capacity_data = (*env)->GetDirectBufferCapacity(env, data_buffer);
    }

    if(capacity_data < data_length)
    {
        return -3;
    }

    bool res = tox_hash(hash_buffer_c, data_buffer_c, (size_t)data_length);

    if(res != true)
    {
        return -1;
    }
    else
    {
        return 0;
    }
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1seek(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jlong position)
{
    TOX_ERR_FILE_SEEK error;
    bool res = tox_file_seek(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (uint64_t)position, &error);

    if(res != true)
    {
        if(error == TOX_ERR_FILE_SEEK_FRIEND_NOT_FOUND)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_FRIEND_NOT_FOUND");
            return (jint)-1;
        }
        else if(error == TOX_ERR_FILE_SEEK_FRIEND_NOT_CONNECTED)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_FRIEND_NOT_CONNECTED");
            return (jint)-2;
        }
        else if(error == TOX_ERR_FILE_SEEK_NOT_FOUND)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_NOT_FOUND");
            return (jint)-3;
        }
        else if(error == TOX_ERR_FILE_SEEK_DENIED)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_DENIED");
            return (jint)-4;
        }
        else if(error == TOX_ERR_FILE_SEEK_INVALID_POSITION)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_INVALID_POSITION");
            return (jint)-5;
        }
        else if(error == TOX_ERR_FILE_SEEK_SENDQ)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_SENDQ");
            return (jint)-6;
        }
        else
        {
            dbg(9, "tox_file_seek:ERROR:%d", (int)error);
            return (jint)-99;
        }
    }
    else
    {
        dbg(9, "tox_file_seek");
        return (jint)res;
    }
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1get_1file_1id(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jobject file_id_buffer)
{
    uint8_t *file_id_buffer_c = NULL;
    long capacity = 0;

    if(file_id_buffer == NULL)
    {
        return -3;
    }

    file_id_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, file_id_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, file_id_buffer);

    if(capacity < TOX_FILE_ID_LENGTH)
    {
        return -2;
    }

    TOX_ERR_FILE_GET error;
    bool res = tox_file_get_file_id(tox_global, (uint32_t)friend_number, (uint32_t)file_number, file_id_buffer_c, &error);

    if(res != true)
    {
        return -1;
    }
    else
    {
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong kind, jlong file_size, jobject file_id_buffer, jstring file_name, jlong filename_length)
{
    uint8_t *file_id_buffer_c = NULL;
    long capacity = 0;

    // TODO: this can be NULL !! -- fix me --
    if(file_id_buffer == NULL)
    {
        return -21;
    }

    // TODO: this can be NULL !! -- fix me --
    file_id_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, file_id_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, file_id_buffer);

    if(capacity < TOX_FILE_ID_LENGTH)
    {
        return -22;
    }

    const char *filename_str = NULL;
    filename_str = (*env)->GetStringUTFChars(env, file_name, NULL);
    TOX_ERR_FILE_SEND error;
    uint32_t res = tox_file_send(tox_global, (uint32_t)friend_number, (uint32_t)kind, (uint64_t)file_size, file_id_buffer_c,
                                 (uint8_t *)filename_str, (size_t)filename_length, &error);
    (*env)->ReleaseStringUTFChars(env, file_name, filename_str);

    if(error == TOX_ERR_FILE_SEND_NULL)
    {
        dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_NULL");
        return (jlong)-1;
    }
    else if(error == TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND)
    {
        dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND");
        return (jlong)-2;
    }
    else if(error == TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED)
    {
        dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED");
        return (jlong)-3;
    }
    else if(error == TOX_ERR_FILE_SEND_NAME_TOO_LONG)
    {
        dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_NAME_TOO_LONG");
        return (jlong)-4;
    }
    else if(error == TOX_ERR_FILE_SEND_TOO_MANY)
    {
        dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_TOO_MANY");
        return (jlong)-5;
    }
    else
    {
        return (jlong)res;
    }
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send_1chunk(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jlong position, jobject data_buffer, jlong data_length)
{
    uint8_t *data_buffer_c = NULL;
    long capacity = 0;

    if(data_buffer == NULL)
    {
        return -21;
    }

    data_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, data_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, data_buffer);
    TOX_ERR_FILE_SEND_CHUNK error;
    bool res = tox_file_send_chunk(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (uint64_t)position,
                                   data_buffer_c,
                                   (size_t)data_length, &error);

    if(res != true)
    {
        if(error == TOX_ERR_FILE_SEND_CHUNK_NULL)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NULL");
            return (jint)-1;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND");
            return (jint)-2;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED");
            return (jint)-3;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND");
            return (jint)-4;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING");
            return (jint)-5;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH");
            return (jint)-6;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_SENDQ)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_SENDQ");
            return (jint)-7;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION)
        {
            dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION");
            return (jint)-8;
        }
        else
        {
            return (jint)-99;
        }
    }

    return (jint)0;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1nospam(JNIEnv *env, jobject thiz, jlong nospam)
{
    tox_self_set_nospam(tox_global, (uint32_t)nospam);
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1nospam(JNIEnv *env, jobject thiz)
{
    uint32_t nospam = tox_self_get_nospam(tox_global);
    return (jlong)nospam;
}

// -----------------------
// TODO
// -----------------------
/*
void tox_self_get_public_key(const Tox *tox, uint8_t *public_key);
void tox_self_get_secret_key(const Tox *tox, uint8_t *secret_key);
bool tox_friend_exists(const Tox *tox, uint32_t friend_number);
uint64_t tox_friend_get_last_online(const Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_GET_LAST_ONLINE *error);
TOX_USER_STATUS tox_friend_get_status(const Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);
*/
// -----------------------
// TODO
// -----------------------








/*
 * ------------------------------------------------------------
 * ----------------- MessageV2 --------------------------------
 * ------------------------------------------------------------
 */
#ifdef TOX_MESSAGE_V2_ACTIVE

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1size(JNIEnv *env, jobject thiz, jlong text_length,
        jlong type, jlong alter_type)
{
    uint32_t tox_msg_size = tox_messagev2_size((uint32_t)text_length, (uint32_t)type, (uint32_t)alter_type);
    return (jlong)tox_msg_size;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1wrap(JNIEnv *env, jobject thiz,
        jlong text_length, jlong type,
        jlong alter_type,
        jobject message_text_buffer, jlong ts_sec,
        jlong ts_ms, jobject raw_message_buffer,
        jobject msgid_buffer)
{
    if(message_text_buffer == NULL)
    {
        return -1;
    }

    if(raw_message_buffer == NULL)
    {
        return -2;
    }

    if(msgid_buffer == NULL)
    {
        return -3;
    }

    dbg(0, "tox_messagev2_wrap:001");
    uint8_t *message_text_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, message_text_buffer);
    dbg(0, "tox_messagev2_wrap:002");
    long message_text_buffer_capacity = (*env)->GetDirectBufferCapacity(env, message_text_buffer);
    dbg(0, "tox_messagev2_wrap:00");
    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    dbg(0, "tox_messagev2_wrap:003");
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    dbg(0, "tox_messagev2_wrap:004");
    uint8_t *msgid_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, msgid_buffer);
    dbg(0, "tox_messagev2_wrap:005");
    long msgid_buffer_capacity = (*env)->GetDirectBufferCapacity(env, msgid_buffer);
    dbg(0, "tox_messagev2_wrap:006");
    dbg(0, "tox_messagev2_wrap:007");
    bool res = tox_messagev2_wrap((uint32_t)text_length, (uint32_t)type,
                                  (uint32_t)alter_type, message_text_buffer_c, (uint32_t)ts_sec,
                                  (uint16_t)ts_ms, raw_message_buffer_c, msgid_buffer_c);
    dbg(0, "tox_messagev2_wrap:008");

    if(res == true)
    {
        return 0;
    }
    else
    {
        return 1;
    }
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1sync_1message_1pubkey(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer)
{
    if(raw_message_buffer == NULL)
    {
        return (jstring)NULL;
    }

    jstring result = NULL;
    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);

    if(tox_global == NULL)
    {
        return (jstring)NULL;
    }

    uint8_t public_key[TOX_PUBLIC_KEY_SIZE];
    bool res = tox_messagev2_get_sync_message_pubkey(raw_message_buffer_c, public_key);

    if(res == false)
    {
        result = (*env)->NewStringUTF(env, "-1"); // C style string to Java String
    }
    else
    {
        char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1]; // need this wrong size for next call
        CLEAR(tox_id_hex);
        toxid_bin_to_hex(public_key, tox_id_hex);
        tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key
        result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
    }

    return result;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1sync_1message_1type(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer)
{
    if(raw_message_buffer == NULL)
    {
        return (jlong)-1;
    }

    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);

    if(tox_global == NULL)
    {
        return (jlong)-2;
    }

    uint32_t result = tox_messagev2_get_sync_message_type(raw_message_buffer_c);

    if(result == UINT32_MAX)
    {
        return (jlong)-3;
    }
    else
    {
        return (jlong)result;
    }
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1message_1id(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer, jobject msgid_buffer)
{
    if(raw_message_buffer == NULL)
    {
        return -1;
    }

    if(msgid_buffer == NULL)
    {
        return -2;
    }

    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    uint8_t *msgid_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, msgid_buffer);
    long msgid_buffer_capacity = (*env)->GetDirectBufferCapacity(env, msgid_buffer);
    bool res = tox_messagev2_get_message_id(raw_message_buffer_c, msgid_buffer_c);

    if(res == true)
    {
        return 0;
    }
    else
    {
        return 1;
    }
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1ts_1sec(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer)
{
    if(raw_message_buffer == NULL)
    {
        return -1;
    }

    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    uint32_t res = tox_messagev2_get_ts_sec(raw_message_buffer_c);
    return (jlong)res;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1ts_1ms(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer)
{
    if(raw_message_buffer == NULL)
    {
        return -1;
    }

    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    uint16_t res = tox_messagev2_get_ts_ms(raw_message_buffer_c);
    return (jlong)res;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev2_1get_1message_1text(JNIEnv *env, jobject thiz,
        jobject raw_message_buffer, jlong raw_message_len,
        jint is_alter_msg,
        jlong alter_type,
        jobject message_text_buffer)
{
    if(message_text_buffer == NULL)
    {
        return -1;
    }

    if(raw_message_buffer == NULL)
    {
        return -2;
    }

    uint32_t text_length = 0;
    uint8_t *message_text_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, message_text_buffer);
    long message_text_buffer_capacity = (*env)->GetDirectBufferCapacity(env, message_text_buffer);
    uint8_t *raw_message_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, raw_message_buffer);
    long raw_message_buffer_capacity = (*env)->GetDirectBufferCapacity(env, raw_message_buffer);
    bool res = tox_messagev2_get_message_text(raw_message_buffer_c, (uint32_t)raw_message_len,
               (bool)is_alter_msg,
               (uint32_t)alter_type, message_text_buffer_c,
               &text_length);

    if(res == true)
    {
        return (long)text_length;
    }
    else
    {
        return -3;
    }
}


// bool tox_messagev2_get_message_alter_id(uint8_t *raw_message, uint8_t *alter_id);
// uint8_t tox_messagev2_get_alter_type(uint8_t *raw_message);


#endif
/*
 * ------------------------------------------------------------
 * ----------------- MessageV2 --------------------------------
 * ------------------------------------------------------------
 */







// ------------------- Conference -------------------
// ------------------- Conference -------------------
// ------------------- Conference -------------------


/**
 * This function deletes a conference.
 *
 * @param conference_number The conference number of the conference to be deleted.
 *
 * @return true on success.
 */
// !! this actually means -> leave conference !!
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1delete(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    TOX_ERR_CONFERENCE_DELETE error;
    bool res = tox_conference_delete(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_DELETE_OK)
    {
        dbg(0, "tox_conference_delete:ERROR=%d", (int)error);
        return (jint)-1;
    }

    return (jint)res;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1join(JNIEnv *env, jobject thiz, jlong friend_number,
        jobject cookie_buffer, jlong cookie_length)
{
    uint8_t *cookie_buffer_c = NULL;
    long capacity = 0;

    if(cookie_buffer == NULL)
    {
        return -21;
    }

    cookie_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, cookie_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, cookie_buffer);
    // dbg(0, "tox_conference_join:cookie length=%d", (int)capacity);
    // dbg(0, "tox_conference_join:cookie start byte=%d", (int)cookie_buffer_c[0]);
    // dbg(0, "tox_conference_join:cookie end byte=%d", (int)cookie_buffer_c[cookie_length - 1]);
    TOX_ERR_CONFERENCE_JOIN error;
    uint32_t res = tox_conference_join(tox_global, (uint32_t)friend_number, cookie_buffer_c, (size_t)cookie_length, &error);

    if(error != TOX_ERR_CONFERENCE_JOIN_OK)
    {
        if(error == TOX_ERR_CONFERENCE_JOIN_INVALID_LENGTH)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_INVALID_LENGTH");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_JOIN_WRONG_TYPE)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_WRONG_TYPE");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_JOIN_FRIEND_NOT_FOUND)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_FRIEND_NOT_FOUND");
            return (jlong)-3;
        }
        else if(error == TOX_ERR_CONFERENCE_JOIN_DUPLICATE)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_DUPLICATE");
            return (jlong)-4;
        }
        else if(error == TOX_ERR_CONFERENCE_JOIN_INIT_FAIL)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_INIT_FAIL");
            return (jlong)-5;
        }
        else if(error == TOX_ERR_CONFERENCE_JOIN_FAIL_SEND)
        {
            dbg(0, "tox_conference_join:TOX_ERR_CONFERENCE_JOIN_FAIL_SEND");
            return (jlong)-6;
        }
        else
        {
            dbg(0, "tox_conference_join:*OTHER ERROR*");
            return (jlong)-99;
        }
    }

    return (jlong)res;
}





/**
 * Send a text chat message to the conference.
 *
 * This function creates a conference message packet and pushes it into the send
 * queue.
 *
 * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
 * must be split by the client and sent as separate messages. Other clients can
 * then reassemble the fragments.
 *
 * @param conference_number The conference number of the conference the message is intended for.
 * @param type Message type (normal, action, ...).
 * @param message A non-NULL pointer to the first element of a byte array
 *   containing the message text.
 * @param length Length of the message to be sent.
 *
 * @return true on success.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1send_1message(JNIEnv *env, jobject thiz,
        jlong conference_number, jint type, jobject message)
{
    const char *message_str = NULL;
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_CONFERENCE_SEND_MESSAGE error;
    bool res = tox_conference_send_message(tox_global, (uint32_t)conference_number, (int)type, (uint8_t *)message_str,
                                           (size_t)strlen(message_str), &error);
    (*env)->ReleaseStringUTFChars(env, message, message_str);

    if(res == false)
    {
        if(error == TOX_ERR_CONFERENCE_SEND_MESSAGE_CONFERENCE_NOT_FOUND)
        {
            dbg(9, "tox_conference_send_message:ERROR:TOX_ERR_CONFERENCE_SEND_MESSAGE_CONFERENCE_NOT_FOUND");
            return (jint)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_SEND_MESSAGE_TOO_LONG)
        {
            dbg(9, "tox_conference_send_message:ERROR:TOX_ERR_CONFERENCE_SEND_MESSAGE_TOO_LONG");
            return (jint)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_SEND_MESSAGE_NO_CONNECTION)
        {
            dbg(9, "tox_conference_send_message:ERROR:TOX_ERR_CONFERENCE_SEND_MESSAGE_NO_CONNECTION");
            return (jint)-3;
        }
        else if(error == TOX_ERR_CONFERENCE_SEND_MESSAGE_FAIL_SEND)
        {
            dbg(9, "tox_conference_send_message:ERROR:TOX_ERR_CONFERENCE_SEND_MESSAGE_FAIL_SEND");
            return (jint)-4;
        }
        else
        {
            dbg(9, "tox_conference_send_message:ERROR:%d", (int)error);
            return (jint)-99;
        }
    }
    else
    {
        return (jint)res;
    }
}


/**
 * Returns the type of conference (TOX_CONFERENCE_TYPE) that conference_number is. Return value is
 * unspecified on failure.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1type(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    if(tox_global == NULL)
    {
        return (jint)-2;
    }

    TOX_ERR_CONFERENCE_GET_TYPE error;
    TOX_CONFERENCE_TYPE type = tox_conference_get_type(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_GET_TYPE_OK)
    {
        dbg(0, "tox_conference_get_type:ERROR=%d", (int)error);
        return (jint)-1;
    }

    return (jint)type;
}



JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1peer_1get_1public_1key(JNIEnv *env, jobject thiz,
        jlong conference_number, jlong peer_number)
{
    if(tox_global == NULL)
    {
        return (jstring)NULL;
    }

    jstring result;
    uint8_t public_key[TOX_PUBLIC_KEY_SIZE];
    TOX_ERR_CONFERENCE_PEER_QUERY error;
    bool res = tox_conference_peer_get_public_key(tox_global, (uint32_t)conference_number, (uint32_t)peer_number,
               public_key, &error);

    if(res == false)
    {
        result = (*env)->NewStringUTF(env, "-1"); // C style string to Java String
    }
    else
    {
        char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1]; // need this wrong size for next call
        CLEAR(tox_id_hex);
        toxid_bin_to_hex(public_key, tox_id_hex);
        tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key
        result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
    }

    return result;
}

/**
 * Return the number of peers in the conference. Return value is unspecified on failure.
 */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1peer_1count(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    if(tox_global == NULL)
    {
        return (jlong)-99;
    }

    TOX_ERR_CONFERENCE_PEER_QUERY error;
    uint32_t res = tox_conference_peer_count(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_PEER_QUERY_OK)
    {
        if(error == TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND)
        {
            dbg(0, "tox_conference_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND)
        {
            dbg(0, "tox_conference_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION)
        {
            dbg(0, "tox_conference_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION");
            return (jlong)-3;
        }
        else
        {
            return (jlong)-99;
        }
    }

    return (jlong)res;
}

/**
 * Return the length of the peer's name. Return value is unspecified on failure.
 */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1peer_1get_1name_1size(JNIEnv *env, jobject thiz,
        jlong conference_number, jlong peer_number)
{
    if(tox_global == NULL)
    {
        return (jlong)-99;
    }

    TOX_ERR_CONFERENCE_PEER_QUERY error;
    size_t res = tox_conference_peer_get_name_size(tox_global, (uint32_t)conference_number, (uint32_t)peer_number, &error);

    if(error != TOX_ERR_CONFERENCE_PEER_QUERY_OK)
    {
        if(error == TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND)
        {
            dbg(0, "tox_conference_peer_get_name_size:TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND)
        {
            dbg(0, "tox_conference_peer_get_name_size:TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION)
        {
            dbg(0, "tox_conference_peer_get_name_size:TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION");
            return (jlong)-3;
        }
        else
        {
            return (jlong)-99;
        }
    }

    return (jlong)(unsigned long long)res;
}


/**
 * Copy the name of peer_number who is in conference_number to name.
 * name must be at least TOX_MAX_NAME_LENGTH long.
 *
 * @return true on success.
 */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1peer_1get_1name(JNIEnv *env, jobject thiz,
        jlong conference_number, jlong peer_number)
{
    TOX_ERR_CONFERENCE_PEER_QUERY error;
    size_t length = tox_conference_peer_get_name_size(tox_global, (uint32_t)conference_number, (uint32_t)peer_number,
                    &error);

    if(error != TOX_ERR_CONFERENCE_PEER_QUERY_OK)
    {
        return NULL;
    }
    else
    {
        char name[length + 1];
        CLEAR(name);
        bool res = tox_conference_peer_get_name(tox_global, (uint32_t)conference_number, (uint32_t)peer_number, name, &error);

        if(res == false)
        {
            return (*env)->NewStringUTF(env, "-1"); // C style string to Java String
        }
        else
        {
            jstring js1 = c_safe_string_from_java((char *)name, length);
            return js1;
        }
    }
}

/**
 * Return true if passed peer_number corresponds to our own.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1peer_1number_1is_1ours(JNIEnv *env, jobject thiz,
        jlong conference_number, jlong peer_number)
{
    TOX_ERR_CONFERENCE_PEER_QUERY error;
    bool res = tox_conference_peer_number_is_ours(tox_global, (uint32_t)conference_number, (uint32_t)peer_number, &error);

    if(error != TOX_ERR_CONFERENCE_PEER_QUERY_OK)
    {
        dbg(0, "tox_conference_peer_number_is_ours:ERROR=%d", (int)error);
        return (jint)-1;
    }

    return (jint)res;
}


/**
 * Return the length of the conference title. Return value is unspecified on failure.
 *
 * The return value is equal to the `length` argument received by the last
 * `conference_title` callback.
 */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1title_1size(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    if(tox_global == NULL)
    {
        return (jlong)-99;
    }

    TOX_ERR_CONFERENCE_TITLE error;
    size_t res = tox_conference_get_title_size(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_TITLE_OK)
    {
        if(error == TOX_ERR_CONFERENCE_TITLE_CONFERENCE_NOT_FOUND)
        {
            dbg(0, "tox_conference_get_title_size:TOX_ERR_CONFERENCE_TITLE_CONFERENCE_NOT_FOUND");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_TITLE_INVALID_LENGTH)
        {
            dbg(0, "tox_conference_get_title_size:TOX_ERR_CONFERENCE_TITLE_INVALID_LENGTH");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_TITLE_FAIL_SEND)
        {
            dbg(0, "tox_conference_get_title_size:TOX_ERR_CONFERENCE_TITLE_FAIL_SEND");
            return (jlong)-3;
        }
        else
        {
            return (jlong)-99;
        }
    }

    return (jlong)(unsigned long long)res;
}


/**
 * Write the title designated by the given conference number to a byte array.
 *
 * Call tox_conference_get_title_size to determine the allocation size for the `title` parameter.
 *
 * The data written to `title` is equal to the data received by the last
 * `conference_title` callback.
 *
 * @param title A valid memory region large enough to store the title.
 *   If this parameter is NULL, this function has no effect.
 *
 * @return true on success.
 */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1title(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    if(tox_global == NULL)
    {
        return (jstring)NULL;
    }

    TOX_ERR_CONFERENCE_TITLE error;
    size_t length = tox_conference_get_title_size(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_TITLE_OK)
    {
        return NULL;
    }
    else
    {
        char title[length + 1];
        CLEAR(title);
        bool res = tox_conference_get_title(tox_global, (uint32_t)conference_number, title, &error);

        if(res == false)
        {
            return (*env)->NewStringUTF(env, "-1"); // C style string to Java String
        }
        else
        {
            jstring js1 = c_safe_string_from_java((char *)title, length);
            return js1;
        }
    }
}

/**
 * Return the number of conferences in the Tox instance.
 * This should be used to determine how much memory to allocate for `tox_conference_get_chatlist`.
 */

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1chatlist_1size(JNIEnv *env, jobject thiz)
{
    size_t res = tox_conference_get_chatlist_size(tox_global);
    dbg(9, "tox_conference_get_chatlist_size=%d", (int)res);
    return (jlong)(unsigned long long)res;
}

/**
 * Copy a list of valid conference numbers into the array chatlist. Determine how much space
 * to allocate for the array with the `tox_conference_get_chatlist_size` function.
 */

JNIEXPORT jlongArray JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1chatlist(JNIEnv *env, jobject thiz)
{
    size_t numconferences = tox_conference_get_chatlist_size(tox_global);
    size_t memsize = (numconferences * sizeof(uint32_t));
    uint32_t *conferences_list = malloc(memsize);
    uint32_t *conferences_list_iter = conferences_list;
    jlongArray result;
    tox_conference_get_chatlist(tox_global, conferences_list);
    result = (*env)->NewLongArray(env, numconferences);

    if(result == NULL)
    {
        // TODO this would be bad!!
    }

    jlong buffer[numconferences];
    size_t i = 0;

    for(i=0; i<numconferences; i++)
    {
        buffer[i] = (long)conferences_list_iter[i];
    }

    (*env)->SetLongArrayRegion(env, result, 0, numconferences, buffer);

    if(conferences_list)
    {
        free(conferences_list);
    }

    return result;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1get_1id(JNIEnv *env, jobject thiz,
        jlong conference_number, jobject cookie_buffer)
{
    if(tox_global == NULL)
    {
        return (jint)-99;
    }

    uint8_t *cookie_buffer_c = NULL;
    long capacity = 0;

    if(cookie_buffer == NULL)
    {
        return -21;
    }

    cookie_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, cookie_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, cookie_buffer);
    bool res = tox_conference_get_id(tox_global, (uint32_t)conference_number, (uint8_t *)cookie_buffer_c);

    if(res == true)
    {
        return (jint)0;
    }
    else
    {
        return (jint)-1;
    }
}



// ------------------- Conference -------------------
// ------------------- Conference -------------------
// ------------------- Conference -------------------





// ------------------- AV -------------------
// ------------------- AV -------------------
// ------------------- AV -------------------
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1answer(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong audio_bit_rate, jlong video_bit_rate)
{
    TOXAV_ERR_ANSWER error;
    bool res = toxav_answer(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate, (uint32_t)video_bit_rate,
                            &error);
    return (jint)res;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1iteration_1interval(JNIEnv *env, jobject thiz)
{
    long long l = (long long)toxav_iteration_interval(tox_av_global);
    dbg(9, "toxav_iteration_interval=%lld", (long long)l);
    return (jlong)(unsigned long long)l;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1call(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong audio_bit_rate, jlong video_bit_rate)
{
    TOXAV_ERR_CALL error;
    bool res = toxav_call(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate, (uint32_t)video_bit_rate,
                          &error);
    return (jint)res;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1bit_1rate_1set(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong audio_bit_rate, jlong video_bit_rate)
{
    TOXAV_ERR_BIT_RATE_SET error;
    bool res = toxav_bit_rate_set(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate,
                                  (uint32_t)video_bit_rate, &error);
    return (jint)res;
}


#ifdef HAVE_TOXAV_OPTION_SET
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1option_1set(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong option, jlong value)
{
    TOXAV_ERR_OPTION_SET error;
    int res = toxav_option_set(tox_av_global, (uint32_t)friend_number, (TOXAV_OPTIONS_OPTION)option, (int32_t)value,
                               &error);
    return (jint)res;
}
#endif

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1call_1control(JNIEnv *env, jobject thiz, jlong friend_number,
        jint control)
{
    TOXAV_ERR_CALL_CONTROL error;
    bool res = toxav_call_control(tox_av_global, (uint32_t)friend_number, (TOXAV_CALL_CONTROL)control, &error);
    return (jint)res;
}



// reverse the order of the U and V planes ---------------
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1video_1send_1frame_1uv_1reversed(JNIEnv *env, jobject thiz,
        jlong friend_number, jint frame_width_px, jint frame_height_px)
{
    TOXAV_ERR_SEND_FRAME error;
    video_buffer_2_y_size = (int)(frame_width_px * frame_height_px);
    video_buffer_2_u_size = (int)(video_buffer_2_y_size / 4);
    video_buffer_2_v_size = (int)(video_buffer_2_y_size / 4);
    // reversed -----------
    // reversed -----------
    video_buffer_2_v = (uint8_t *)(video_buffer_2 + video_buffer_2_y_size);
    video_buffer_2_u = (uint8_t *)(video_buffer_2 + video_buffer_2_y_size + video_buffer_2_u_size);
    // reversed -----------
    // reversed -----------
    bool res = toxav_video_send_frame(tox_av_global, (uint32_t)friend_number, (uint16_t)frame_width_px,
                                      (uint16_t)frame_height_px,
                                      (uint8_t *)video_buffer_2, video_buffer_2_u, video_buffer_2_v, &error);
    return (jint)error;
}
// reverse the order of the U and V planes ---------------


/**
 * Send a video frame to a friend.
 *
 * Y - plane should be of size: height * width
 * U - plane should be of size: (height/2) * (width/2)
 * V - plane should be of size: (height/2) * (width/2)
 *
 * @param friend_number The friend number of the friend to which to send a video
 * frame.
 * @param width Width of the frame in pixels.
 * @param height Height of the frame in pixels.
 * @param y Y (Luminance) plane data.
 * @param u U (Chroma) plane data.
 * @param v V (Chroma) plane data.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1video_1send_1frame(JNIEnv *env, jobject thiz,
        jlong friend_number, jint frame_width_px, jint frame_height_px)
{
    TOXAV_ERR_SEND_FRAME error;
    video_buffer_2_y_size = (int)(frame_width_px * frame_height_px);
    video_buffer_2_u_size = (int)(video_buffer_2_y_size / 4);
    video_buffer_2_v_size = (int)(video_buffer_2_y_size / 4);
    video_buffer_2_u = (uint8_t *)(video_buffer_2 + video_buffer_2_y_size);
    video_buffer_2_v = (uint8_t *)(video_buffer_2 + video_buffer_2_y_size + video_buffer_2_u_size);
    // dbg(9, "toxav_video_send_frame:fn=%d,video_buffer_2=%p,w=%d,h=%d", (int)friend_number, video_buffer_2, (int)frame_width_px, (int)frame_height_px);
    bool res = toxav_video_send_frame(tox_av_global, (uint32_t)friend_number, (uint16_t)frame_width_px,
                                      (uint16_t)frame_height_px,
                                      (uint8_t *)video_buffer_2, video_buffer_2_u, video_buffer_2_v, &error);
    // dbg(9, "toxav_video_send_frame:res=%d,error=%d", (int)res, (int)error);
    return (jint)error;
}


/**
 * Send an audio frame to a friend.
 *
 * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
 * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
 * For mono audio, this has no meaning, every sample is subsequent. For stereo,
 * this means the expected format is LRLRLR... with samples for left and right
 * alternating.
 *
 * @param friend_number The friend number of the friend to which to send an
 * audio frame.
 * @param pcm An array of audio samples. The size of this array must be
 * sample_count * channels.
 * @param sample_count Number of samples in this frame. Valid numbers here are
 * ((sample rate) * (audio length) / 1000), where audio length can be
 * 2.5, 5, 10, 20, 40 or 60 millseconds.
 * @param channels Number of audio channels. Supported values are 1 and 2.
 * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
 * rates are 8000, 12000, 16000, 24000, or 48000.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1audio_1send_1frame(JNIEnv *env, jobject thiz,
        jlong friend_number, jlong sample_count, jint channels, jlong sampling_rate)
{
    TOXAV_ERR_SEND_FRAME error;

    if(global_toxav_valid != true)
    {
        return (jint)TOXAV_ERR_SEND_FRAME_FRIEND_NOT_IN_CALL;
    }

    if(audio_buffer_pcm_1)
    {
        int16_t *pcm = (int16_t *)audio_buffer_pcm_1;
#if 0
        const int8_t *pcm2 = (int8_t *)pcm;
        dbg(9, "toxav_audio_send_frame: ch:%d r:%d c:%d - %d %d %d %d %d %d %d",
            (int)channels,
            (int)sampling_rate,
            (int)sample_count,
            (int8_t)pcm[0], (int8_t)pcm[1], (int8_t)pcm[2],
            (int8_t)pcm[3], (int8_t)pcm[4], (int8_t)pcm[5],
            (int8_t)pcm[6]);
#endif
#ifdef USE_ECHO_CANCELLATION

        if(((int)channels == 1) && ((int)sampling_rate == 48000))
        {
            filteraudio_incompatible_1 = 0;
        }
        else
        {
            filteraudio_incompatible_1 = 1;
        }

        // TODO: need some locking here!
        if(recording_samling_rate != (uint32_t)sampling_rate)
        {
            recording_samling_rate = (uint32_t)sampling_rate;
            restart_filter_audio((uint32_t)sampling_rate);
        }

        // TODO: need some locking here!

        if((filteraudio) && (pcm) && (filteraudio_active == 1) && (filteraudio_incompatible_1 == 0)
                && (filteraudio_incompatible_2 == 0))
        {
            filter_audio(filteraudio, pcm, (unsigned int)sample_count);
        }

#endif
        bool res = toxav_audio_send_frame(tox_av_global, (uint32_t)friend_number, pcm, (size_t)sample_count,
                                          (uint8_t)channels, (uint32_t)sampling_rate, &error);
    }

    return (jint)error;
}
// ------------------- AV -------------------
// ------------------- AV -------------------
// ------------------- AV -------------------



// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------




// JNIEXPORT void JNICALL
// Java_com_zoffcc_applications_trifa_MainActivity_toxloop(JNIEnv* env, jobject thiz)
// {
//  _main_();
// }

// ------------------------------------------------------------------------------------------------
// taken from:
// https://github.com/googlesamples/android-ndk/blob/master/hello-jni/app/src/main/cpp/hello-jni.c
// ------------------------------------------------------------------------------------------------

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_getNativeLibAPI(JNIEnv *env, jobject thiz)
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


// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv* env, jobject thiz) __attribute__((optimize("-O0")));
// void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv* env, jobject thiz) __attribute__((optimize("O0")));
void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv *env, jobject thiz)
{
    // int i = 3;
    // i = (1 / 0);
    char *name = NULL;
    name = (char *)0;
    name = "ekrpowekrp";
    int *pi;
    int c;
    pi = NULL;
    c = *pi;
    int *x = NULL;
    int y = *x;
    y = y + 1;
    *(long *)0 = 0xDEADBEEF;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(env, thiz));
}
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------





