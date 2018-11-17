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
#include <assert.h>
#include <jni.h>
#include <string.h>
#include <pthread.h>


// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

const char *LOGTAG = "trifa.nativeaudio";


// -----------------------------
JavaVM *cachedJVM = NULL;
uint8_t *audio_play_buffer[20];
long audio_play_buffer_size[20];
int audio_play_buffers_in_queue = 0;
#define _STOPPED 0
#define _PLAYING 1
#define _SHUTDOWN 2
int playing_state = _STOPPED;
int player_state_current = _STOPPED;
#define PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS 2
pthread_mutex_t play_buffer_queued_count_mutex;
int play_buffer_queued_count_mutex_valid = 0;

uint8_t *audio_rec_buffer[20];
long audio_rec_buffer_size[20];
int rec_buf_pointer_start = 0;
int rec_buf_pointer_next = 0;
int num_rec_bufs = 3;
#define _RECORDING 3
int rec_state = _STOPPED;
#define RECORD_BUFFERS_BETWEEN_REC_AND_PROCESS 2

jclass NativeAudio_class = NULL;
jmethodID rec_buffer_ready_method = NULL;
// -----------------------------


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
static short *resampleBuf = NULL;

// URI player interfaces
static SLObjectItf uriPlayerObject = NULL;
static SLPlayItf uriPlayerPlay;
static SLSeekItf uriPlayerSeek;
static SLMuteSoloItf uriPlayerMuteSolo;
static SLVolumeItf uriPlayerVolume;

// file descriptor player interfaces
static SLObjectItf fdPlayerObject = NULL;
static SLPlayItf fdPlayerPlay;
static SLSeekItf fdPlayerSeek;
static SLMuteSoloItf fdPlayerMuteSolo;
static SLVolumeItf fdPlayerVolume;

// recorder interfaces
static SLObjectItf recorderObject = NULL;
static SLRecordItf recorderRecord;
static SLAndroidSimpleBufferQueueItf recorderBufferQueue;


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
    int nextSize = 0;
    int8_t *nextBuffer = NULL;

    nextBuffer = (int8_t *) audio_rec_buffer[rec_buf_pointer_next];
    nextSize = audio_rec_buffer_size[rec_buf_pointer_next];

    if ((nextSize > 0) && (nextBuffer))
    {
        if (bq == NULL)
        {
            return;
        }

        // enque the next buffer
        SLresult result = (*bq)->Enqueue(bq, nextBuffer, (SLuint32) nextSize);
        (void) result;

        rec_buf_pointer_next++;
        if (rec_buf_pointer_next >= num_rec_bufs)
        {
            rec_buf_pointer_next = 0;
        }

        // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A003:ENQU-CB:max_num_bufs=%d,bs=%d,bn=%d",
        //                    (int)num_rec_bufs, (int)rec_buf_pointer_start, (int)rec_buf_pointer_next);

        // signal Java code that a new record data is available in buffer #cur_rec_buf
        if ((NativeAudio_class) && (rec_buffer_ready_method))
        {
            // TODO: rewerite this, so that it does not need to call "AttachCurrentThread" and "DetachCurrentThread"
            //       every time!
            JNIEnv *jnienv2;
            jnienv2 = jni_getenv();
            if (jnienv2 == NULL)
            {
                JavaVMAttachArgs args;
                args.version = JNI_VERSION_1_6; // choose your JNI version
                args.name = NULL; // you might want to give the java thread a name
                args.group = NULL; // you might want to assign the java thread to a ThreadGroup
                (*cachedJVM)->AttachCurrentThread(cachedJVM, (JNIEnv **) &jnienv2, &args);
            }

            (*jnienv2)->CallStaticVoidMethod(jnienv2, NativeAudio_class, rec_buffer_ready_method,
                                             rec_buf_pointer_start);
            (*cachedJVM)->DetachCurrentThread(cachedJVM);

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
    if (audio_play_buffers_in_queue < 0)
    {
        audio_play_buffers_in_queue = 0;
    }
    pthread_mutex_unlock(&play_buffer_queued_count_mutex);

    if (audio_play_buffers_in_queue < PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS)
    {
        if (player_state_current != _STOPPED)
        {
            // set the player's state
            SLresult result;
            result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
            player_state_current = _STOPPED;
            __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                "player_state:res_011=%d SL_RESULT_SUCCESS=%d PAUSED",
                                (int) result, (int) SL_RESULT_SUCCESS);
            (void) result;
        }
    }
}

// create the engine and output mix objects
void Java_com_zoffcc_applications_nativeaudio_NativeAudio_createEngine(JNIEnv *env, jclass clazz, jint num_bufs)
{
    SLresult result;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine");

    pthread_mutex_init(&play_buffer_queued_count_mutex, NULL);
    play_buffer_queued_count_mutex_valid = 1;

    // find java methods ------------
    NativeAudio_class = NULL;
    android_find_class_global("com/zoffcc/applications/nativeaudio/NativeAudio", &NativeAudio_class);
    rec_buffer_ready_method = (*env)->GetStaticMethodID(env, NativeAudio_class, "rec_buffer_ready", "(I)V");

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine:class=%p", NativeAudio_class);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createEngine:method=%p", rec_buffer_ready_method);
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
                                                                                       jclass clazz, jint sampleRate,
                                                                                       jint channels,
                                                                                       jint num_bufs)
{
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:start:engineEngine=%p", engineEngine);

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
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, (SLuint32) num_bufs};

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

    // ----------------------------------------------------------
    // Code for working with ear speaker by setting stream type to STREAM_VOICE ??
    SLAndroidConfigurationItf playerConfig;
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_ANDROIDCONFIGURATION, &playerConfig);

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_001=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);

    if (SL_RESULT_SUCCESS == result)
    {
        // SLint32 streamType = SL_ANDROID_STREAM_MEDIA;
        SLint32 streamType = SL_ANDROID_STREAM_VOICE;
        result = (*playerConfig)->SetConfiguration(playerConfig, SL_ANDROID_KEY_STREAM_TYPE, &streamType,
                                                   sizeof(SLint32));
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_002=%d", (int) result);
    }
    // ----------------------------------------------------------

    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_Realize=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // get the play interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_003=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;


    // get the buffer queue interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                             &bqPlayerBufferQueue);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_005=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // register callback on the buffer queue
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_005a=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // get the volume interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_006=%d SL_RESULT_SUCCESS=%d",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    // set the player's state
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createBufferQueueAudioPlayer:res_007=%d SL_RESULT_SUCCESS=%d PAUSED",
                        (int) result, (int) SL_RESULT_SUCCESS);
    (void) result;

    player_state_current = _STOPPED;
    playing_state = _STOPPED;
}


// create audio recorder: recorder is not in fast path
void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_createAudioRecorder(JNIEnv *env, jclass clazz, jint sampleRate,
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
    else if ((int) sampleRate == 16000)
    {
        rec_samplerate = SL_SAMPLINGRATE_16;
    }

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, (SLuint32) num_rec_bufs};

    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, (SLuint32) channels, (SLuint32) rec_samplerate,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[2] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_ANDROIDCONFIGURATION};
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &audioSrc,
                                                  &audioSnk, 2, id, req);

    if (SL_RESULT_SUCCESS != result)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:ERR:01");
        return;
    }


    // Configure the voice recognition preset which has no
    // signal processing for lower latency.
    SLAndroidConfigurationItf inputConfig;
    result = (*recorderObject)->GetInterface(recorderObject,
                                             SL_IID_ANDROIDCONFIGURATION,
                                             &inputConfig);

    if (SL_RESULT_SUCCESS == result)
    {
        SLuint32 presetValue = SL_ANDROID_RECORDING_PRESET_VOICE_COMMUNICATION;
        (*inputConfig)->SetConfiguration(inputConfig,
                                         SL_ANDROID_KEY_RECORDING_PRESET,
                                         &presetValue,
                                         sizeof(SLuint32));
    }


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

    // get the buffer queue interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);

    // register callback on the buffer queue
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback, NULL);
    assert(SL_RESULT_SUCCESS == result);

    rec_buf_pointer_start = 0;
    rec_buf_pointer_next = 0;
    rec_state = _STOPPED;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "createAudioRecorder:end");
}


void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_set_1JNI_1audio_1buffer(JNIEnv *env, jobject clazz, jobject buffer,
                                                                             jlong buffer_size_in_bytes, jint num)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();

    audio_play_buffer[num] = (uint8_t *) (*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = buffer_size_in_bytes; // (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
    audio_play_buffer_size[num] = (long) capacity;
}

void
Java_com_zoffcc_applications_nativeaudio_NativeAudio_set_1JNI_1audio_1rec_1buffer(JNIEnv *env, jobject clazz,
                                                                                  jobject buffer,
                                                                                  jlong buffer_size_in_bytes, jint num)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "set_JNI_audio_rec_buffer:num=%d, len=%d", (int) num,
                        (int) buffer_size_in_bytes);

    audio_rec_buffer[num] = (uint8_t *) (*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = buffer_size_in_bytes; // (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
    audio_rec_buffer_size[num] = (long) capacity;
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
    rec_state = _STOPPED;

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StopREC");

    if (recorderRecord != NULL)
    {
        SLuint32 curState;
        result = (*recorderRecord)->GetRecordState(recorderRecord, &curState);
        if (curState == SL_RECORDSTATE_STOPPED)
        {
            return JNI_TRUE;
        }
    }

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
                        (int) num_rec_bufs, (int) rec_buf_pointer_start, (int) rec_buf_pointer_next);

    if (nextSize > 0)
    {
        if (recorderBufferQueue == NULL)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:01");
            return -2;
        }

        // in case already recording, stop recording and clear buffer queue
        SLresult result;
        result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
        result = (*recorderBufferQueue)->Clear(recorderBufferQueue);

        // enque the buffer
        // __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:1:Enqueue -> %d", rec_buf_pointer_next);
        result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, nextBuffer, nextSize);
        if (SL_RESULT_SUCCESS != result)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:02");
            return -2;
        }

        rec_buf_pointer_next++;

        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:A003:ENQU-01:max_num_bufs=%d,bs=%d,bn=%d",
                            (int) num_rec_bufs, (int) rec_buf_pointer_start, (int) rec_buf_pointer_next);

        if (num_rec_bufs > 1)
        {
            int jj = 0;
            for (jj = 0; jj < (num_rec_bufs - (RECORD_BUFFERS_BETWEEN_REC_AND_PROCESS + 1)); jj++)
            {

                nextBuffer = (short *) audio_rec_buffer[rec_buf_pointer_next];
                nextSize = audio_rec_buffer_size[rec_buf_pointer_next];

                if (nextSize > 0)
                {
                    // enque the buffer
                    result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, nextBuffer, nextSize);
                    if (SL_RESULT_SUCCESS != result)
                    {
                        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:07");
                    }

                    rec_buf_pointer_next++;

                    __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                        "StartREC:A003:ENQU-02:max_num_bufs=%d,bs=%d,bn=%d,jj=%d",
                                        (int) num_rec_bufs, (int) rec_buf_pointer_start, (int) rec_buf_pointer_next,
                                        (int) jj);

                }
            }
        }

        // start recording
        result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);

        if (SL_RESULT_SUCCESS != result)
        {
            __android_log_print(ANDROID_LOG_INFO, LOGTAG, "StartREC:ERR:03");
            return -2;
        }

        rec_state = _RECORDING;
    }

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

    // set the player's state
    SLresult result;
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "player_state:res_007=%d SL_RESULT_SUCCESS=%d STOPPED",
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

jint Java_com_zoffcc_applications_nativeaudio_NativeAudio_PlayPCM16(JNIEnv *env, jclass clazz, jint bufnum)
{
    if (playing_state == _SHUTDOWN)
    {
        return -1;
    }

    int nextSize = 0;
    int8_t *nextBuffer = NULL;

    nextBuffer = (int8_t *) audio_play_buffer[bufnum];
    nextSize = audio_play_buffer_size[bufnum];

    if (nextSize > 0)
    {
        if (bqPlayerBufferQueue == NULL)
        {
            return -2;
        }
        // enque the buffer
        SLresult result;
        playing_state = _PLAYING;
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer, (SLuint32) nextSize);
        if (SL_RESULT_SUCCESS != result)
        {
            return -2;
        }
        else
        {
            pthread_mutex_lock(&play_buffer_queued_count_mutex);
            audio_play_buffers_in_queue++;
            pthread_mutex_unlock(&play_buffer_queued_count_mutex);
        }

        if (audio_play_buffers_in_queue > PLAY_BUFFERS_BETWEEN_PLAY_AND_PROCESS)
        {
            if (player_state_current != _PLAYING)
            {
                // set the player's state
                SLresult result;
                result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
                __android_log_print(ANDROID_LOG_INFO, LOGTAG,
                                    "player_state:res_010=%d SL_RESULT_SUCCESS=%d PLAYING",
                                    (int) result, (int) SL_RESULT_SUCCESS);
                (void) result;

                player_state_current = _PLAYING;
            }
        }
    }

    return 0;
}


// shut down the native audio system
void Java_com_zoffcc_applications_nativeaudio_NativeAudio_shutdownEngine(JNIEnv *env, jclass clazz)
{
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "shutdownEngine");

    playing_state = _SHUTDOWN;
    rec_state = _SHUTDOWN;

    // set the player's state
    if (bqPlayerPlay != NULL)
    {
        SLresult result;
        result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
        (void) result;
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "player_state:res_009=%d SL_RESULT_SUCCESS=%d STOPPED",
                            (int) result, (int) SL_RESULT_SUCCESS);
    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, LOGTAG, "player_state:bqPlayerPlay==NULL");
    }

    player_state_current = _STOPPED;

    if (bqPlayerBufferQueue != NULL)
    {
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

    // destroy audio recorder object, and invalidate all associated interfaces
    if (recorderObject != NULL)
    {
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = NULL;
        recorderRecord = NULL;
        recorderBufferQueue = NULL;
    }


    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL)
    {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        // outputMixEnvironmentalReverb = NULL;
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
