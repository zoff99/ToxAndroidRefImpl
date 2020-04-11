/**
 * [TRIfA], JNI part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2020 Zoff <zoff@zoff.cc>
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
#include <math.h>

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

#define AV_MEDIACODEC 1

#ifdef AV_MEDIACODEC
#include <libavcodec/jni.h>
#endif

// HINT: it may not be working properly
// #define USE_ECHO_CANCELLATION 1

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
#define VERSION_PATCH 37
static const char global_version_string[] = "0.99.37";
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

int audio_play_volume_percent_c = 10;
float volumeMultiplier = -20.0f;

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
jmethodID android_toxav_callback_video_receive_frame_h264_cb_method = NULL;
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

void change_audio_volume_pcm_null(int16_t *buf, size_t buf_size_bytes);
void change_audio_volume_pcm(int16_t *buf, size_t num_samples);


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
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wformat-nonliteral"
        vsnprintf(log_line_str, (size_t)MAX_LOG_LINE_LENGTH, level_and_format, ap);
#pragma GCC diagnostic pop
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


void export_savedata_file_unsecure(const Tox *tox, const uint8_t *passphrase, size_t passphrase_len,
                                   const char *export_full_path_of_file)
{
    size_t size = tox_get_savedata_size(tox);
    dbg(9, "export_savedata_file_unsecure:tox_get_savedata_size=%d", (int)size);
    char *savedata = malloc(size);
    dbg(9, "export_savedata_file_unsecure:savedata=%p", savedata);
    tox_get_savedata(tox, (uint8_t *)savedata);
    FILE *f = fopen(export_full_path_of_file, "wb");
    fwrite(savedata, size, 1, f);
    fclose(f);

    if(savedata)
    {
        free(savedata);
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
    // ----------- custom packets -----------
    // tox_utils_callback_friend_lossless_packet(tox_global, friend_lossless_packet_cb);
    // tox_callback_friend_lossless_packet(tox_global, tox_utils_friend_lossless_packet_cb);
    tox_utils_callback_friend_lossless_packet(tox_global, friend_lossless_packet_cb);
    tox_callback_friend_lossless_packet_per_pktid(tox_global, tox_utils_friend_lossless_packet_cb, 170);
    tox_callback_friend_lossless_packet_per_pktid(tox_global, friend_lossless_packet_cb, 176);
    // ----------- custom packets -----------
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


/** -----XX-----SPLIT-01-----XX----- */

#include "jni-001.h"
#include "jni-002.h"

