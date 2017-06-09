#include "echo_canceller.h"
SpeexEchoState *st;
SpeexPreprocessState *den;

JNIEXPORT void JNICALL Java_speex_EchoCanceller_open
  (JNIEnv *env, jobject jObj, jint jSampleRate, jint jBufSize, jint jTotalSize)
{
     int sampleRate=jSampleRate;
     st = speex_echo_state_init(jBufSize, jTotalSize);
     den = speex_preprocess_state_init(jBufSize, sampleRate);
     speex_echo_ctl(st, SPEEX_ECHO_SET_SAMPLING_RATE, &sampleRate);
     speex_preprocess_ctl(den, SPEEX_PREPROCESS_SET_ECHO_STATE, st);
     //speex_preprocess_ctl(den, SPEEX_PREPROCESS_SET_DENOISE, st);
     //speex_preprocess_ctl(den, SPEEX_PREPROCESS_SET_DEREVERB, st);
}

JNIEXPORT jshortArray JNICALL Java_speex_EchoCanceller_process
  (JNIEnv * env, jobject jObj, jshortArray input_frame, jshortArray echo_frame)
{
  //create native shorts from java shorts
  jshort *native_input_frame = env->GetShortArrayElements(input_frame, 0);
  jshort *native_echo_frame = env->GetShortArrayElements(echo_frame, 0);

  //allocate memory for output data
  jint length = env->GetArrayLength(input_frame);
  jshortArray temp = env->NewShortArray(length);
  jshort *native_output_frame = env->GetShortArrayElements(temp, 0);

  //call echo cancellation
  speex_echo_cancellation(st, native_input_frame, native_echo_frame, native_output_frame);
  //preprocess output frame
  speex_preprocess_run(den, native_output_frame);

  //convert native output to java layer output
  jshortArray output_shorts = env->NewShortArray(length);
  env->SetShortArrayRegion(output_shorts, 0, length, native_output_frame);

  //cleanup and return
  env->ReleaseShortArrayElements(input_frame, native_input_frame, 0);
  env->ReleaseShortArrayElements(echo_frame, native_echo_frame, 0);
  env->ReleaseShortArrayElements(temp, native_output_frame, 0);

  return output_shorts;   
}


JNIEXPORT void JNICALL Java_speex_EchoCanceller_playback
  (JNIEnv *env, jobject jObj, jshortArray echo_frame)
{
    jshort *native_echo_frame = env->GetShortArrayElements(echo_frame, 0);
    speex_echo_playback(st, native_echo_frame);
    env->ReleaseShortArrayElements(echo_frame, native_echo_frame, 0);
}

JNIEXPORT jshortArray JNICALL Java_speex_EchoCanceller_capture
  (JNIEnv *env, jobject jObj, jshortArray input_frame)
{
    env->MonitorEnter(jObj);
    jshort *native_input_frame = env->GetShortArrayElements(input_frame, 0);
    
    jint length = env->GetArrayLength(input_frame);
    jshortArray temp = env->NewShortArray(length);
    jshort *native_output_frame = env->GetShortArrayElements(temp, 0);
    
    speex_echo_capture(st, native_input_frame, native_output_frame);
    speex_preprocess_run(den, native_output_frame);    
    
    jshortArray output_shorts = env->NewShortArray(length);
    env->SetShortArrayRegion(output_shorts, 0, length, native_output_frame);
    
    env->ReleaseShortArrayElements(input_frame, native_input_frame, 0);
    env->ReleaseShortArrayElements(temp, native_output_frame, 0);
    env->MonitorExit(jObj);
    return output_shorts;
}

JNIEXPORT void JNICALL Java_speex_EchoCanceller_reset(JNIEnv *env, jobject jObj) {
    speex_echo_state_reset(st);    
}

JNIEXPORT void JNICALL Java_speex_EchoCanceller_close
  (JNIEnv *env, jobject jObj)
{
     speex_echo_state_destroy(st);
     speex_preprocess_state_destroy(den);
     st = 0;
     den = 0;
}