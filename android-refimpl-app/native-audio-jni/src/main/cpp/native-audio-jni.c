/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is a JNI example where we use native methods to play sounds
 * using OpenSL ES. See the corresponding Java source file located at:
 *
 *   src/com/example/nativeaudio/NativeAudio/NativeAudio.java
 */

#include <stdlib.h>
#include <stdint.h>
#include <assert.h>
#include <jni.h>
#include <string.h>
#include <pthread.h>
#include <math.h>
#include <sys/types.h>
#include <stdbool.h>

// ------------------
// com.zoffcc.applications.trifa W/libOpenSLES: Leaving BufferQueue::Enqueue (SL_RESULT_BUFFER_INSUFFICIENT)
// ------------------

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>


// filter_audio lib for software AEC
#include "filter_audio/filter_audio.h"

#ifdef WEBRTC_AEC
// webrtc lib for software AEC
#include "webrtc/echo_control_mobile.h"
#endif

/*---------------------------------------------------------------------------*/
/* Android AudioPlayer and AudioRecorder configuration                       */
/*---------------------------------------------------------------------------*/

/** Audio Performance mode.
 * Performance mode tells the framework how to configure the audio path
 * for a player or recorder according to application performance and
 * functional requirements.
 * It affects the output or input latency based on acceptable tradeoffs on
 * battery drain and use of pre or post processing effects.
 * Performance mode should be set before realizing the object and should be
 * read after realizing the object to check if the requested mode could be
 * granted or not.
 */
/** Audio Performance mode key */
#ifdef SL_ANDROID_KEY_PERFORMANCE_MODE
#undef SL_ANDROID_KEY_PERFORMANCE_MODE
#endif
#define SL_ANDROID_KEY_PERFORMANCE_MODE ((const SLchar*) "androidPerformanceMode")

/** Audio performance values */
/*      No specific performance requirement. Allows HW and SW pre/post processing. */
#ifdef SL_ANDROID_PERFORMANCE_NONE
#undef SL_ANDROID_PERFORMANCE_NONE
#endif
#define SL_ANDROID_PERFORMANCE_NONE ((SLuint32) 0x00000000)
/*      Priority given to latency. No HW or software pre/post processing.
 *      This is the default if no performance mode is specified. */
#ifdef SL_ANDROID_PERFORMANCE_LATENCY
#undef SL_ANDROID_PERFORMANCE_LATENCY
#endif
#define SL_ANDROID_PERFORMANCE_LATENCY ((SLuint32) 0x00000001)
/*      Priority given to latency while still allowing HW pre and post processing. */
#ifdef SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS
#undef SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS
#endif
#define SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS ((SLuint32) 0x00000002)
/*      Priority given to power saving if latency is not a concern.
 *      Allows HW and SW pre/post processing. */
#ifdef SL_ANDROID_PERFORMANCE_POWER_SAVING
#undef SL_ANDROID_PERFORMANCE_POWER_SAVING
#endif
#define SL_ANDROID_PERFORMANCE_POWER_SAVING ((SLuint32) 0x00000003)

const char *LOGTAG = "trifa.nativeaudio";
// #define DEBUG_NATIVE_AUDIO_DEEP 1 // define to activate full debug logging

// -----------------------------
JavaVM *cachedJVM = NULL;
uint8_t *audio_play_buffer[20];
static const uint8_t empty_buffer[20000];
long audio_play_buffer_size[20];
int audio_play_buffers_in_queue = 0;
#define _STOPPED 0
#define _PLAYING 1
#define _SHUTDOWN 2
int playing_state = _STOPPED;
int player_state_current = _STOPPED;
#define PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS1 2
pthread_mutex_t play_buffer_queued_count_mutex;
int play_buffer_queued_count_mutex_valid = 0;

// --------- AEC ---------
Filter_Audio *filteraudio = NULL;
bool filteraudio_used = false;
// --------- AEC ---------
#ifdef WEBRTC_AEC
void *webrtc_aecmInst = NULL;
#endif
// --------- AEC ---------

uint8_t *audio_rec_buffer[20];
long audio_rec_buffer_size[20];
int rec_buf_pointer_start = 0;
int rec_buf_pointer_next = 0;
int num_rec_bufs = 3; // BAD: !!! always keep in sync with NavitAudio.java `public static final int n_rec_audio_in_buffer_max_count = 2;` !!!
#define _RECORDING 3
int rec_state = _STOPPED;
#define RECORD_BUFFERS_BETWEEN_REC_AND_PROCESS 2
float wanted_mic_gain = 1.0f;

jclass NativeAudio_class = NULL;
jmethodID rec_buffer_ready_method = NULL;
// -----------------------------

static float audio_in_vu_value = 0.0f;
static float audio_out_vu_value = 0.0f;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

// buffer queue player interfaces
static SLObjectItf bqPlayerObject = NULL;
static SLPlayItf bqPlayerPlay;
static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
static SLEffectSendItf bqPlayerEffectSend;
static SLMuteSoloItf bqPlayerMuteSolo;
static SLVolumeItf bqPlayerVolume;
static SLmilliHertz bqPlayerSampleRate = 0;

// recorder interfaces
static SLObjectItf recorderObject = NULL;
static SLRecordItf recorderRecord;
static SLAndroidSimpleBufferQueueItf recorderBufferQueue;


// ----- function defs ------
void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_set_1JNI_1audio_1buffer(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jobject buffer,
                                                                             jlong buffer_size_in_bytes,
                                                                             jint num);

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_PlayPCM16(JNIEnv *env, jclass clazz,
                                                                    jint bufnum);

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_isPlaying(JNIEnv *env, jclass clazz);

jboolean Java_com_zoffcc_applications_nativeaudio_NativeAudio_StopPCM16(JNIEnv *env, jclass clazz);

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_isRecording(JNIEnv *env, jclass clazz);

jboolean Java_com_zoffcc_applications_nativeaudio_NativeAudio_StopREC(JNIEnv *env, jclass clazz);

float audio_vu(const int16_t *pcm_data, uint32_t sample_count);

void start_filter_audio(uint32_t in_samplerate);

void stop_filter_audio();

void set_delay_ms_filter_audio(int16_t input_latency_ms, int16_t frame_duration_ms);
// ----- function defs ------




JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv *env_this;
    cachedJVM = jvm;

    if ((*jvm)->GetEnv(jvm, (void **) &env_this, JNI_VERSION_1_6))
    {
        // dbg(0,"Could not get JVM");
        return JNI_ERR;
    }

    // dbg(0,"++ Found JVM ++");
    return JNI_VERSION_1_6;
}


JNIEnv *jni_getenv()
{
    JNIEnv *env_this;
    (*cachedJVM)->GetEnv(cachedJVM, (void **) &env_this, JNI_VERSION_1_6);
    return env_this;
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

// --------------------------

// this callback handler is called every time a buffer finishes recording
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
#ifdef DEBUG_NATIVE_AUDIO_DEEP
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "bqRecorderCallback:A003:STATE = %d", (int)rec_state);
#endif
    if (rec_state != _RECORDING)
    {
#ifdef DEBUG_NATIVE_AUDIO_DEEP
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "bqRecorderCallback:A003:RETURN");
#endif
        return;
    }

    int nextSize = 0;
    int8_t *nextBuffer = NULL;

    nextBuffer = (int8_t *) audio_rec_buffer[rec_buf_pointer_next];
    nextSize = audio_rec_buffer_size[rec_buf_pointer_next];

    if ((nextSize > 0) && (nextBuffer))
    {
        if (bq == NULL)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "bqRecorderCallback:A003:ERR:bq == NULL");
            return;
        }

        // enque the next buffer
#ifdef DEBUG_NATIVE_AUDIO_DEEP
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "bqRecorderCallback:A005:Enqueue -> %d, nextSize=%d", rec_buf_pointer_next, (int)nextSize);
#endif

        SLAndroidSimpleBufferQueueState state;
        (*bq)->GetState(bq, &state);

#if 1
        if (state.count < 1)
        {
            SLresult result;
            result = (*bq)->Enqueue(bq, nextBuffer,
                                    (SLuint32) nextSize);
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "bqRecorderCallback:Enqueue:more_buffers");

            (*bq)->GetState(bq, &state);

            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "bqRecorderCallback:EB:real_buffer_count=%d", state.count);

            rec_buf_pointer_next++;
            if (rec_buf_pointer_next >= num_rec_bufs)
            {
                rec_buf_pointer_next = 0;
            }

            rec_buf_pointer_start++;
            if (rec_buf_pointer_start >= num_rec_bufs)
            {
                rec_buf_pointer_start = 0;
            }

            nextBuffer = (int8_t *) audio_rec_buffer[rec_buf_pointer_next];
            nextSize = audio_rec_buffer_size[rec_buf_pointer_next];

        }
#endif


        SLresult result = (*bq)->Enqueue(bq, nextBuffer, (SLuint32) nextSize);
        if (result != SL_RESULT_SUCCESS)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "bqRecorderCallback:A006:Enqueue:ERR:result=%d", (int) result);
        }
        (void) result;

        rec_buf_pointer_next++;
        if (rec_buf_pointer_next >= num_rec_bufs)
        {
            rec_buf_pointer_next = 0;
        }

        // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A003:ENQU-CB:max_num_bufs=%d,bs=%d,bn=%d",
        //                    (int)num_rec_bufs, (int)rec_buf_pointer_start, (int)rec_buf_pointer_next);

        // signal Java code that a new record data is available in buffer #cur_rec_buf
        if ((NativeAudio_class) && (rec_buffer_ready_method) && (rec_state == _RECORDING))
        {
            if (filteraudio_used)
            {
                filter_audio(filteraudio,
                             (int16_t *) audio_rec_buffer[rec_buf_pointer_start],
                             (unsigned int) (
                                     audio_rec_buffer_size[rec_buf_pointer_start] /
                                     2));

                // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "filter_audio:AEC:res=%d", res_filter);
            }

            // TODO: make this better? faster?
            // --------------------------------------------------
            // increase GAIN manually and rather slow:
            int16_t *this_buffer_pcm16 = (int16_t *) audio_rec_buffer[rec_buf_pointer_start];
            int this_buffer_size_pcm16 = audio_rec_buffer_size[rec_buf_pointer_start] / 2;
            int loop = 0;
            int32_t temp = 0;
            for (loop = 0; loop < this_buffer_size_pcm16; loop++)
            {
                temp = (int16_t) (*this_buffer_pcm16) * wanted_mic_gain;
                if (temp > INT16_MAX)
                {
                    temp = INT16_MAX;
                }
                else if (temp < INT16_MIN)
                {
                    temp = INT16_MIN;
                }

                // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "gain:old=%d new=%d",
                //                     (*this_buffer_pcm16), (int16_t) temp);
                *this_buffer_pcm16 = (int16_t) temp;
                this_buffer_pcm16++;
            }
            // --------------------------------------------------

            this_buffer_pcm16 = (int16_t *) audio_rec_buffer[rec_buf_pointer_start];
            audio_in_vu_value = audio_vu(this_buffer_pcm16,
                                         (uint32_t) (this_buffer_size_pcm16 / 2));

            // TODO: rewerite this, so that it does not need to call "AttachCurrentThread" and "DetachCurrentThread"
            //       every time!
            if (rec_state == _RECORDING)
            {
                JNIEnv *jnienv2;
                jnienv2 = jni_getenv();
                if (jnienv2 == NULL)
                {
                    JavaVMAttachArgs args;
                    args.version = JNI_VERSION_1_6; // choose your JNI version
                    args.name = NULL; // you might want to give the java thread a name
                    args.group = NULL; // you might want to assign the java thread to a ThreadGroup
                    if (cachedJVM)
                    {
                        (*cachedJVM)->AttachCurrentThread(cachedJVM, (JNIEnv **) &jnienv2, &args);
                    }
                }

                if (jnienv2 != NULL)
                {
                    (*jnienv2)->CallStaticVoidMethod(jnienv2, NativeAudio_class,
                                                     rec_buffer_ready_method,
                                                     rec_buf_pointer_start);
                }

                if (cachedJVM)
                {
                    (*cachedJVM)->DetachCurrentThread(cachedJVM);
                }
            }
        }

        rec_buf_pointer_start++;
        if (rec_buf_pointer_start >= num_rec_bufs)
        {
            rec_buf_pointer_start = 0;
        }

        // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A003:ENQU-CB_PR:max_num_bufs=%d,bs=%d,bn=%d",
        //                    (int)num_rec_bufs, (int)rec_buf_pointer_start, (int)rec_buf_pointer_next);

    }
}

// this callback handler is called every time a buffer finishes playing
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    pthread_mutex_lock(&play_buffer_queued_count_mutex);
    audio_play_buffers_in_queue--;

#ifdef DEBUG_NATIVE_AUDIO_DEEP
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "bqPlayerCallback:1:audio_play_buffers_in_queue=%d",(int)audio_play_buffers_in_queue);
#endif

    if (audio_play_buffers_in_queue < 0)
    {
        audio_play_buffers_in_queue = 0;
    }

#ifdef DEBUG_NATIVE_AUDIO_DEEP
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "bqPlayerCallback:2:audio_play_buffers_in_queue=%d",(int)audio_play_buffers_in_queue);
#endif

    pthread_mutex_unlock(&play_buffer_queued_count_mutex);

    SLAndroidSimpleBufferQueueState state;
    (*bq)->GetState(bq, &state);

#if 0
    if (state.count < PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS1)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "bqPlayerCallback:real_buffer_count=%d", state.count);

#ifdef DEBUG_NATIVE_AUDIO_DEEP
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "bqPlayerCallback:%d < %d", (int) audio_play_buffers_in_queue,
                            (int) PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS1);
#endif

        if (player_state_current != _STOPPED)
        {
            // set the player's state
            SLresult result;
            result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
            player_state_current = _STOPPED;
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "player_state:res_011=%d SL_RESULT_SUCCESS=%d PAUSED",
                                (int) result, (int) SL_RESULT_SUCCESS);


            audio_out_vu_value = 0.0f;
            (void) result;
        }
    }
#endif
}

// create the engine and output mix objects
void Java_com_zoffcc_applications_nativeaudio_NativeAudio_createEngine(JNIEnv *env, jclass clazz,
                                                                       jint num_bufs)
{
    audio_in_vu_value = 0.0f;
    audio_out_vu_value = 0.0f;

    SLresult result;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine");

    pthread_mutex_init(&play_buffer_queued_count_mutex, NULL);
    play_buffer_queued_count_mutex_valid = 1;

    // find java methods ------------
    NativeAudio_class = NULL;
    android_find_class_global("com/zoffcc/applications/nativeaudio/NativeAudio",
                              &NativeAudio_class);
    rec_buffer_ready_method = (*env)->GetStaticMethodID(env, NativeAudio_class, "rec_buffer_ready",
                                                        "(I)V");

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine:class=%p", NativeAudio_class);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine:method=%p",
                        (void *) rec_buffer_ready_method);
    // find java methods ------------


    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // create output mix
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, 0, 0);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

}


// create buffer queue audio player
void Java_com_zoffcc_applications_nativeaudio_NativeAudio_createBufferQueueAudioPlayer(JNIEnv *env,
                                                                                       jclass clazz,
                                                                                       jint sampleRate,
                                                                                       jint channels,
                                                                                       jint num_bufs,
                                                                                       jint eac_delay_ms)
{
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:start:engineEngine=%p",
                        (const void *) engineEngine);

    SLresult result;
    if (sampleRate >= 0)
    {
        bqPlayerSampleRate = sampleRate * 1000;
    }

    pthread_mutex_lock(&play_buffer_queued_count_mutex);
    audio_play_buffers_in_queue = 0;
    pthread_mutex_unlock(&play_buffer_queued_count_mutex);

    SLuint32 _speakers = SL_SPEAKER_FRONT_CENTER;

    if (channels == 2)
    {
        _speakers = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    }

    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                       (SLuint32) num_bufs};

    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, (SLuint32) channels, SL_SAMPLINGRATE_44_1,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   _speakers, SL_BYTEORDER_LITTLEENDIAN};

    /*
     * Enable Fast Audio when possible:  once we set the same rate to be the native, fast audio path
     * will be triggered
     */
    if (bqPlayerSampleRate)
    {
        format_pcm.samplesPerSec = bqPlayerSampleRate;       //sample rate in milli seconds
    }


    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:start:samplerate=%d",
                        (int) format_pcm.samplesPerSec);

    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};


    /*
     * create audio player:
     *     fast audio does not support when SL_IID_EFFECTSEND is required, skip it
     *     for fast audio case
     */
#define  num_params  3
    const SLInterfaceID ids[num_params] = {SL_IID_BUFFERQUEUE,
                                           SL_IID_VOLUME,
                                           SL_IID_ANDROIDCONFIGURATION,
            /*SL_IID_EFFECTSEND,*/
            /*SL_IID_MUTESOLO,*/};
    const SLboolean req[num_params] = {SL_BOOLEAN_TRUE,
                                       SL_BOOLEAN_TRUE,
                                       SL_BOOLEAN_TRUE,
            /*SL_BOOLEAN_TRUE,*/
            /*SL_BOOLEAN_TRUE,*/};

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk,
                                                num_params, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

#if 1
    // ----------------------------------------------------------
    // Code for working with ear speaker by setting stream type to STREAM_VOICE ??
    SLAndroidConfigurationItf playerConfig;
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_ANDROIDCONFIGURATION,
                                             &playerConfig);

    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_001=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);

    if (SL_RESULT_SUCCESS == result)
    {
        // SLint32 streamType = SL_ANDROID_STREAM_MEDIA;
        SLint32 streamType = SL_ANDROID_STREAM_VOICE;
        result = (*playerConfig)->SetConfiguration(playerConfig, SL_ANDROID_KEY_STREAM_TYPE,
                                                   &streamType,
                                                   sizeof(SLint32));
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_002=%d",
                            (int) result);


        SLuint32 presetValue2 = SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS;
        (*playerConfig)->SetConfiguration(playerConfig,
                                          SL_ANDROID_KEY_PERFORMANCE_MODE,
                                          &presetValue2,
                                          sizeof(SLuint32));

        // -- read what values where actually set --
        SLuint32 presetRetrieved = SL_ANDROID_RECORDING_PRESET_NONE;
        SLuint32 presetSize = 2 * sizeof(SLuint32); // intentionally too big
        (*playerConfig)->GetConfiguration(playerConfig,
                                          SL_ANDROID_KEY_PERFORMANCE_MODE,
                                          &presetSize, (void *) &presetRetrieved);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createBufferQueueAudioPlayer:performance_mode=%u",
                            presetRetrieved);

        // -- read what values where actually set --

    }
    // ----------------------------------------------------------

#endif

    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_Realize=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // get the play interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_003=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;


    // get the buffer queue interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                             &bqPlayerBufferQueue);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_005=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // register callback on the buffer queue
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_005a=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

#if 1
    // get the volume interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_006=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    SLmillibel curVolume;
    (*bqPlayerVolume)->GetVolumeLevel(bqPlayerVolume, &curVolume);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:GetVolumeLevel=%d",
                        (int) curVolume);

    SLmillibel maxVolume;
    (*bqPlayerVolume)->GetMaxVolumeLevel(bqPlayerVolume, &maxVolume);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:GetMaxVolumeLevel=%d",
                        (int) maxVolume);

    // set max volume
    (*bqPlayerVolume)->SetVolumeLevel(bqPlayerVolume, maxVolume);

#endif

#if 0
    int8_t *nextBuffer = (int8_t *) audio_play_buffer[0];
    int nextSize = audio_play_buffer_size[0];
    if (bqPlayerBufferQueue != NULL)
    {
        SLAndroidSimpleBufferQueueState state;
        // -- enque buffer --
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer,
                                        (SLuint32) nextSize);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createBufferQueueAudioPlayer:Enqueue=%d",
                            (int) 0);
        pthread_mutex_lock(&play_buffer_queued_count_mutex);

        (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createBufferQueueAudioPlayer:Enqueue:real_buffer_count:2=%d",
                            state.count);

        audio_play_buffers_in_queue++;
        pthread_mutex_unlock(&play_buffer_queued_count_mutex);
        // -- enque buffer --

        // -- enque buffer --
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer,
                                        (SLuint32) nextSize);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createBufferQueueAudioPlayer:Enqueue=%d",
                            (int) 0);
        pthread_mutex_lock(&play_buffer_queued_count_mutex);

        (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createBufferQueueAudioPlayer:Enqueue:real_buffer_count:2=%d",
                            state.count);

        audio_play_buffers_in_queue++;
        pthread_mutex_unlock(&play_buffer_queued_count_mutex);
        // -- enque buffer --
    }
#endif

#if 0
    // Enqueue a few buffers to get the ball rolling
    int nextSize = 0;
    int8_t *nextBuffer = NULL;
    int feed_buffer_count = PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS2;
    if (feed_buffer_count > num_bufs)
    {
        feed_buffer_count = num_bufs;
    }
    int jk;
    for (jk = 0; jk < feed_buffer_count; jk++)
    {
        if (bqPlayerBufferQueue != NULL)
        {
            nextBuffer = (int8_t *) audio_play_buffer[jk];
            nextSize = audio_play_buffer_size[jk];
            if (nextSize > 0)
            {
                (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer,
                                                (SLuint32) nextSize);
                __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                    "createBufferQueueAudioPlayer:Enqueue=%d",
                                    (int) jk);
                pthread_mutex_lock(&play_buffer_queued_count_mutex);
                audio_play_buffers_in_queue++;
                pthread_mutex_unlock(&play_buffer_queued_count_mutex);
            }
        }
    }
#endif

    // set the player's state
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createBufferQueueAudioPlayer:res_007=%d SL_RESULT_SUCCESS=%d PAUSED",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    if ((channels == 1) && (sampleRate == 48000))
    {
        filteraudio_used = true;
        // filteraudio
        start_filter_audio(sampleRate);
        set_delay_ms_filter_audio(eac_delay_ms, 40);
#ifdef WEBRTC_AEC
        // webrtc
        WebRtcAecm_Create(&webrtc_aecmInst);
        WebRtcAecm_Init(webrtc_aecmInst, sampleRate);
        AecmConfig config;
        config.echoMode = AecmTrue;
        config.cngMode = 3;
        WebRtcAecm_set_config(webrtc_aecmInst, config);
#endif
    }
    else
    {
        filteraudio_used = false;
    }

    player_state_current = _STOPPED;
    playing_state = _STOPPED;
}


// create audio recorder: recorder is not in fast path
void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_createAudioRecorder(JNIEnv *env, jclass clazz,
                                                                         jint sampleRate,
                                                                         jint num_bufs)
{
    SLresult result;

    SLuint32 channels = 1; // always record mono
    num_rec_bufs = num_bufs;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:start");

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};

    SLDataSource audioSrc = {&loc_dev, NULL};

    SLuint32 rec_samplerate = SL_SAMPLINGRATE_16;
    if ((int) sampleRate == 48000)
    {
        rec_samplerate = SL_SAMPLINGRATE_48;
    }
    else if ((int) sampleRate == 8000)
    {
        rec_samplerate = SL_SAMPLINGRATE_8;
    }
    else if ((int) sampleRate == 32000)
    {
        rec_samplerate = SL_SAMPLINGRATE_32;
    }
    else if ((int) sampleRate == 16000)
    {
        rec_samplerate = SL_SAMPLINGRATE_16;
    }

    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "createAudioRecorder:start:samplerate=%d", (int) rec_samplerate);


    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                     (SLuint32) num_rec_bufs};

    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, (SLuint32) channels,
                                   (SLuint32) rec_samplerate,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[2] = {
            SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            SL_IID_ANDROIDCONFIGURATION
    };
    const SLboolean req[2] = {
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE
    };
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &audioSrc,
                                                  &audioSnk, (2), id, req);

    if (SL_RESULT_SUCCESS != result)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:ERR:01");
        return;
    }

#if 1
    // Configure the voice recognition preset which has no
    // signal processing for lower latency.
    SLAndroidConfigurationItf inputConfig;
    result = (*recorderObject)->GetInterface(recorderObject,
                                             SL_IID_ANDROIDCONFIGURATION,
                                             &inputConfig);

    if (SL_RESULT_SUCCESS == result)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_IID_ANDROIDCONFIGURATION...");

        SLuint32 presetValue = SL_ANDROID_RECORDING_PRESET_VOICE_COMMUNICATION;
        // SL_ANDROID_RECORDING_PRESET_UNPROCESSED <--- ??
        // SL_ANDROID_RECORDING_PRESET_VOICE_RECOGNITION
        // SL_ANDROID_RECORDING_PRESET_VOICE_COMMUNICATION
        (*inputConfig)->SetConfiguration(inputConfig,
                                         SL_ANDROID_KEY_RECORDING_PRESET,
                                         &presetValue,
                                         sizeof(SLuint32));

        SLuint32 presetValue2 = SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS;
        // SL_ANDROID_PERFORMANCE_NONE
        // SL_ANDROID_PERFORMANCE_LATENCY <--- ??
        // SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS
        (*inputConfig)->SetConfiguration(inputConfig,
                                         SL_ANDROID_KEY_PERFORMANCE_MODE,
                                         &presetValue2,
                                         sizeof(SLuint32));

        // -- read what values where actually set --
        SLuint32 presetRetrieved = SL_ANDROID_RECORDING_PRESET_NONE;
        SLuint32 presetSize = 2 * sizeof(SLuint32); // intentionally too big
        (*inputConfig)->GetConfiguration(inputConfig,
                                         SL_ANDROID_KEY_RECORDING_PRESET,
                                         &presetSize, (void *) &presetRetrieved);

        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_RECORDING_PRESET_VOICE_RECOGNITION=3");
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_RECORDING_PRESET_VOICE_COMMUNICATION=4");
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_RECORDING_PRESET_UNPROCESSED=5");

        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:record_preset=%u",
                            presetRetrieved);

        presetRetrieved = SL_ANDROID_RECORDING_PRESET_NONE;
        presetSize = 2 * sizeof(SLuint32); // intentionally too big
        (*inputConfig)->GetConfiguration(inputConfig,
                                         SL_ANDROID_KEY_PERFORMANCE_MODE,
                                         &presetSize, (void *) &presetRetrieved);

        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_PERFORMANCE_NONE=0");
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_PERFORMANCE_LATENCY=1");
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "createAudioRecorder:SL_ANDROID_PERFORMANCE_LATENCY_EFFECTS=2");

        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:performance_mode=%u",
                            presetRetrieved);

        // -- read what values where actually set --

    }
#endif

    // realize the audio recorder
    result = (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:ERR:02");
        return;
    }

    // get the record interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    assert(SL_RESULT_SUCCESS == result);

#if 0
    /* Enable AEC if requested */
    if (aec)
    {
        SLAndroidAcousticEchoCancellationItf aecItf;
        result = (*recorderObject)->GetInterface(
            recorderObject, SL_IID_ANDROIDACOUSTICECHOCANCELLATION, (void *) &aecItf);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:AEC is %savailable",
                            SL_RESULT_SUCCESS == result ? "" : "not ");
        if (SL_RESULT_SUCCESS == result)
        {
            result = (*aecItf)->SetEnabled(aecItf, true);
            SLboolean enabled;
            result = (*aecItf)->IsEnabled(aecItf, &enabled);
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:AEC is %s",
                                enabled ? "enabled" : "not enabled");
        }
    }
    /* Enable AGC if requested */
    if (agc)
    {
        SLAndroidAutomaticGainControlItf agcItf;
        result = (*recorderObject)->GetInterface(
            recorderObject, SL_IID_ANDROIDAUTOMATICGAINCONTROL, (void *) &agcItf);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:AGC is %savailable",
                            SL_RESULT_SUCCESS == result ? "" : "not ");
        if (SL_RESULT_SUCCESS == result)
        {
            result = (*agcItf)->SetEnabled(agcItf, true);
            SLboolean enabled;
            result = (*agcItf)->IsEnabled(agcItf, &enabled);
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:AGC is %s",
                                enabled ? "enabled" : "not enabled");
        }
    }
    /* Enable NS if requested */
    if (ns)
    {
        SLAndroidNoiseSuppressionItf nsItf;
        result = (*recorderObject)->GetInterface(
            recorderObject, SL_IID_ANDROIDNOISESUPPRESSION, (void *) &nsItf);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:NS is %savailable",
                            SL_RESULT_SUCCESS == result ? "" : "not ");
        if (SL_RESULT_SUCCESS == result)
        {
            result = (*nsItf)->SetEnabled(nsItf, true);
            SLboolean enabled;
            result = (*nsItf)->IsEnabled(nsItf, &enabled);
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:NS is %s",
                                enabled ? "enabled" : "not enabled");
        }
    }
#endif

    // get the buffer queue interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);

    // register callback on the buffer queue
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback,
                                                      NULL);
    assert(SL_RESULT_SUCCESS == result);

    rec_buf_pointer_start = 0;
    rec_buf_pointer_next = 0;
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:SET:001:_STOPPED");
    rec_state = _STOPPED;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:end");
}


void Java_com_zoffcc_applications_nativeaudio_NativeAudio_set_1JNI_1audio_1buffer(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jobject buffer,
                                                                                  jlong buffer_size_in_bytes,
                                                                                  jint num)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();

    audio_play_buffer[num] = (uint8_t *) (*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = buffer_size_in_bytes; // (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
    audio_play_buffer_size[num] = (long) capacity;
}

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_PlayPCM16(JNIEnv *env, jclass clazz,
                                                                    jint bufnum)
{
    if (playing_state == _SHUTDOWN)
    {
        audio_out_vu_value = 0.0f;
        return -1;
    }

    int nextSize = 0;
    int8_t *nextBuffer = NULL;

    nextBuffer = (int8_t *) audio_play_buffer[bufnum];
    nextSize = audio_play_buffer_size[bufnum];

    SLAndroidSimpleBufferQueueState state;
    (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);

    if (state.count < 1)
    {
        SLresult result;
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, empty_buffer,
                                                 (SLuint32) nextSize);
        //__android_log_print(ANDROID_LOG_INFO, LOGTAG,
        //                    "PlayPCM16:1:Enqueue:empty_buffer");
    }

    if (nextSize > 0)
    {
        if (bqPlayerBufferQueue == NULL)
        {
            audio_out_vu_value = 0.0f;
            return -2;
        }

        if (player_state_current == _PLAYING)
        {
            audio_out_vu_value = audio_vu((int16_t *) nextBuffer, (uint32_t) (nextSize / 2));
        }

        // enque the buffer
        SLresult result;
        playing_state = _PLAYING;
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer,
                                                 (SLuint32) nextSize);

        if (SL_RESULT_SUCCESS != result)
        {
            SLAndroidSimpleBufferQueueState state2;
            (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state2);

            audio_out_vu_value = 0.0f;
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "PlayPCM16:1:Enqueue:ERR:result=%d real_buffer_count:%d",
                                result,
                                state2.count);
            return -2;
        }
        else
        {
            if (filteraudio_used)
            {
                pass_audio_output(filteraudio, (const int16_t *) nextBuffer,
                                  (unsigned int) (nextSize / 2));
            }

            pthread_mutex_lock(&play_buffer_queued_count_mutex);
            audio_play_buffers_in_queue++;
            pthread_mutex_unlock(&play_buffer_queued_count_mutex);
        }

#ifdef DEBUG_NATIVE_AUDIO_DEEP
        SLAndroidSimpleBufferQueueState state;
        (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "bqPlayerCallback:real_buffer_count:2=%d guess=%d", state.count,
                            audio_play_buffers_in_queue);
#endif

        (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);
        //__android_log_print(ANDROID_LOG_INFO, LOGTAG,
        //                    "PlayPCM16:1:Enqueue:22:real_buffer_count:%d",
        //                    state.count);

        if (player_state_current != _PLAYING)
        {
            // set the player's state
            SLresult result2;
            result2 = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "player_state:res_010=%d SL_RESULT_SUCCESS=%d PLAYING inqueue=%d",
                                (int) result2, (int) SL_RESULT_SUCCESS,
                                (int) state.count);
            (void) result2;
            player_state_current = _PLAYING;
        }
        else
        {
            SLuint32 pState;
            SLresult result2;
            result2 = (*bqPlayerPlay)->GetPlayState(bqPlayerPlay, &pState);
            (void) result2;

            if (pState != SL_PLAYSTATE_PLAYING)
            {
                result2 = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
                (void) result2;
                player_state_current = _PLAYING;
                //__android_log_print(ANDROID_LOG_INFO, LOGTAG, "player_state:res_01011=%d",
                //                    (int) pState);
            }
        }
    }

    return 0;
}

void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_set_1JNI_1audio_1rec_1buffer(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jobject buffer,
                                                                                  jlong buffer_size_in_bytes,
                                                                                  jint num)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "set_JNI_audio_rec_buffer:num=%d, len=%d",
                        (int) num,
                        (int) buffer_size_in_bytes);

    audio_rec_buffer[num] = (uint8_t *) (*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = buffer_size_in_bytes; // (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
    audio_rec_buffer_size[num] = (long) capacity;
}


void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_setMicGainFactor(JNIEnv *env, jclass clazz,
                                                                      jfloat gain_factor)
{
    if (((float) gain_factor >= 1.0f) && ((float) gain_factor <= 15.0f))
    {
        wanted_mic_gain = (float) gain_factor;
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "setMicGainFactor:%f",
                            (double) wanted_mic_gain);
    }
}

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_isRecording(JNIEnv *env, jclass clazz)
{
    if (rec_state == _RECORDING)
    {
        return (jint) 1;
    }
    else
    {
        return (jint) 0;
    }
}


jboolean Java_com_zoffcc_applications_nativeaudio_NativeAudio_StopREC(JNIEnv *env, jclass clazz)
{
    SLresult result;
    rec_buf_pointer_start = 0;
    rec_buf_pointer_next = 0;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:SET:002:_STOPPED");
    rec_state = _STOPPED;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StopREC, state=%d", (int) rec_state);

#if 0
    if (recorderRecord != NULL)
    {
        SLuint32 curState;
        result = (*recorderRecord)->GetRecordState(recorderRecord, &curState);
        if (curState == SL_RECORDSTATE_STOPPED)
        {
            return JNI_TRUE;
        }
    }
#endif

    // stop recording and clear buffer queue
    if (recorderRecord != NULL)
    {
        result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    }

    if (recorderBufferQueue != NULL)
    {
        result = (*recorderBufferQueue)->Clear(recorderBufferQueue);
    }

    // also reset buffer pointers
    rec_buf_pointer_start = 0;
    rec_buf_pointer_next = 0;

    return JNI_TRUE;
}


jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_StartREC(JNIEnv *env, jclass clazz)
{
    if (rec_state == _SHUTDOWN)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:_SHUTDOWN --> RET");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC");

    rec_buf_pointer_start = 0;
    rec_buf_pointer_next = 0;
    int nextSize = 0;
    short *nextBuffer = NULL;
    nextBuffer = (short *) audio_rec_buffer[rec_buf_pointer_next];
    nextSize = audio_rec_buffer_size[rec_buf_pointer_next];

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A001:max_num_bufs=%d,bs=%d,bn=%d",
                        (int) num_rec_bufs, (int) rec_buf_pointer_start,
                        (int) rec_buf_pointer_next);

    if (nextSize > 0)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A002x");

        if (recorderBufferQueue == NULL)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:01");
            return -2;
        }

        // in case already recording, stop recording and clear buffer queue
        SLresult result;
        result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
        result = (*recorderBufferQueue)->Clear(recorderBufferQueue);

        // enqueue buffers ----------------
        SLAndroidSimpleBufferQueueState state;
        result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, nextBuffer,
                                                 (SLuint32) nextSize);

        (*recorderBufferQueue)->GetState(recorderBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "bqRecorderCallback:Enqueue:more_buffers:%d", state.count);

        rec_buf_pointer_next++;
        if (rec_buf_pointer_next >= num_rec_bufs)
        {
            rec_buf_pointer_next = 0;
        }

        rec_buf_pointer_start++;
        if (rec_buf_pointer_start >= num_rec_bufs)
        {
            rec_buf_pointer_start = 0;
        }
        // enqueue buffers ----------------

        // enqueue buffers ----------------
        result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, nextBuffer,
                                                 (SLuint32) nextSize);
        (*recorderBufferQueue)->GetState(recorderBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "bqRecorderCallback:Enqueue:more_buffers:%d", state.count);

        rec_buf_pointer_next++;
        if (rec_buf_pointer_next >= num_rec_bufs)
        {
            rec_buf_pointer_next = 0;
        }

        rec_buf_pointer_start++;
        if (rec_buf_pointer_start >= num_rec_bufs)
        {
            rec_buf_pointer_start = 0;
        }
        // enqueue buffers ----------------

        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "StartREC:A003:ENQU-01:max_num_bufs=%d,bs=%d,bn=%d",
                            (int) num_rec_bufs, (int) rec_buf_pointer_start,
                            (int) rec_buf_pointer_next);


        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:SET:002:_RECORDING");
        rec_state = _RECORDING;

        // start recording
        result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);

        if (SL_RESULT_SUCCESS != result)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:03");
            return -2;
        }

        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:088");
    }

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:099");

    return 0;
}


jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_isPlaying(JNIEnv *env, jclass clazz)
{
    if (playing_state == _PLAYING)
    {
        return (jint) 1;
    }
    else
    {
        return (jint) 0;
    }
}

jboolean Java_com_zoffcc_applications_nativeaudio_NativeAudio_StopPCM16(JNIEnv *env, jclass clazz)
{
    playing_state = _STOPPED;

    audio_out_vu_value = 0.0f;

    // set the player's state
    SLresult result;
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "player_state:res_007=%d SL_RESULT_SUCCESS=%d STOPPED",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    player_state_current = _STOPPED;

    if (bqPlayerBufferQueue != NULL)
    {
        (*bqPlayerBufferQueue)->Clear(bqPlayerBufferQueue);
    }

    pthread_mutex_lock(&play_buffer_queued_count_mutex);
    audio_play_buffers_in_queue = 0;
    pthread_mutex_unlock(&play_buffer_queued_count_mutex);

    return JNI_TRUE;
}


// shut down the native audio system
void Java_com_zoffcc_applications_nativeaudio_NativeAudio_shutdownEngine(JNIEnv *env, jclass clazz)
{
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "shutdownEngine");

    if (filteraudio_used)
    {
        // filteraudio
        stop_filter_audio();
        // webrtc
#ifdef WEBRTC_AEC
        WebRtcAecm_Free(webrtc_aecmInst);
        webrtc_aecmInst = NULL;
#endif
        filteraudio_used = false;
    }

    playing_state = _SHUTDOWN;
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "rec_state:SET:002:_SHUTDOWN");
    rec_state = _SHUTDOWN;

    // set the player's state
    if (bqPlayerPlay != NULL)
    {
        SLresult result;
        result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
        (void) result;
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "player_state:res_009=%d SL_RESULT_SUCCESS=%d STOPPED",
                            (int) result, (int) SL_RESULT_SUCCESS);
    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "player_state:bqPlayerPlay==NULL");
    }

    player_state_current = _STOPPED;

    if (bqPlayerBufferQueue != NULL)
    {
        SLAndroidSimpleBufferQueueState state;
        (*bqPlayerBufferQueue)->GetState(bqPlayerBufferQueue, &state);
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "player_state:real_buffer_count(before clear)=%d",
                            state.count);

        (*bqPlayerBufferQueue)->Clear(bqPlayerBufferQueue);
    }

    if (play_buffer_queued_count_mutex_valid == 1)
    {
        pthread_mutex_lock(&play_buffer_queued_count_mutex);
    }
    audio_play_buffers_in_queue = 0;
    if (play_buffer_queued_count_mutex_valid == 1)
    {
        pthread_mutex_unlock(&play_buffer_queued_count_mutex);
    }

    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL)
    {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
        bqPlayerEffectSend = NULL;
        bqPlayerMuteSolo = NULL;
        bqPlayerVolume = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL)
    {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        // outputMixEnvironmentalReverb = NULL;
    }

    // destroy audio recorder object, and invalidate all associated interfaces
    if (recorderObject != NULL)
    {
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = NULL;
        recorderRecord = NULL;
        recorderBufferQueue = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL)
    {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    pthread_mutex_destroy(&play_buffer_queued_count_mutex);
    play_buffer_queued_count_mutex_valid = 0;
}

float audio_vu(const int16_t *pcm_data, uint32_t sample_count)
{
    float sum = 0.0;

    for (uint32_t i = 0; i < sample_count; i++)
    {
        sum += abs(pcm_data[i]) / 32767.0;
    }

    float vu = 20.0f * logf(sum);
    return vu;
}

jfloat Java_com_zoffcc_applications_nativeaudio_NativeAudio_get_1vu_1in(JNIEnv *env, jclass clazz)
{
    return (jfloat) audio_in_vu_value;
}

jfloat Java_com_zoffcc_applications_nativeaudio_NativeAudio_get_1vu_1out(JNIEnv *env, jclass clazz)
{
    return (jfloat) audio_out_vu_value;
}

void start_filter_audio(uint32_t in_samplerate)
{
    /* Prepare filter_audio */
    filteraudio = new_filter_audio(in_samplerate);

    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "filter_audio: prepare. samplerate=%d",
                        (int) in_samplerate);

    if (filteraudio != NULL)
    {
        /* Enable/disable filters. 1 to enable, 0 to disable. */
        int echo_ = 1;
        int noise_ = 0;
        int gain_ = 0;
        int vad_ = 0;
        enable_disable_filters(filteraudio, echo_, noise_, gain_, vad_);
    }
}

void stop_filter_audio()
{
    /* Shutdown filter_audio */
    if (filteraudio != NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                            "filter_audio: shutdown");
        kill_filter_audio(filteraudio);
        filteraudio = NULL;
    }
}

void set_delay_ms_filter_audio(int16_t input_latency_ms, int16_t frame_duration_ms)
{
    /* It's essential that echo delay is set correctly; it's the most important part of the
     * echo cancellation process. If the delay is not set to the acceptable values the AEC
     * will not be able to recover. Given that it's not that easy to figure out the exact
     * time it takes for a signal to get from Output to the Input, setting it to suggested
     * input device latency + frame duration works really good and gives the filter ability
     * to adjust it internally after some time (usually up to 6-7 seconds in my tests when
     * the error is about 20%).
     */
    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                        "filter_audio: set delay in ms=%d",
                        (int) (input_latency_ms + frame_duration_ms));

    if (filteraudio)
    {
        set_echo_delay_ms(filteraudio, (input_latency_ms + frame_duration_ms));
    }

}
