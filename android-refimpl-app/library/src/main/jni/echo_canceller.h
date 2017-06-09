#ifndef _Included_speex_EchoCanceller
#define _Included_speex_EchoCanceller

#include <jni.h>
#include "speex/speex_echo.h"
#include "speex/speex_preprocess.h"

extern "C" {
    JNIEXPORT void JNICALL Java_speex_EchoCanceller_open (JNIEnv *env, jobject jObj, jint jSampleRate, jint jBufSize, jint jTotalSize);
    JNIEXPORT jshortArray JNICALL Java_speex_EchoCanceller_process  (JNIEnv * env, jobject jObj, jshortArray input_frame, jshortArray echo_frame);
    JNIEXPORT void JNICALL Java_speex_EchoCanceller_close(JNIEnv *env, jobject jObj);
    JNIEXPORT jshortArray JNICALL Java_speex_EchoCanceller_capture(JNIEnv *env, jobject jObj, jshortArray input_frame);
    JNIEXPORT void JNICALL Java_speex_EchoCanceller_playback(JNIEnv *env, jobject jObj, jshortArray echo_frame);
    JNIEXPORT void JNICALL Java_speex_EchoCanceller_reset(JNIEnv *env, jobject jObj);
    JNIEXPORT bool JNICALL Java_speex_EchoCanceller_isOpen(JNIEnv *env, jobject jObj);
}
#endif