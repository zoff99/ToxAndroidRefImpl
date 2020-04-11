
/** -----XX-----SPLIT-01-----XX----- */

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

        // ------------ change PCM volume here ------------
        if((sample_count > 0) && (channels > 0))
        {
            if(audio_play_volume_percent_c < 100)
            {
                if(audio_play_volume_percent_c == 0)
                {
                    change_audio_volume_pcm_null((int16_t *)audio_buffer_pcm_2, (size_t)(sample_count * channels * 2));
                }
                else
                {
                    change_audio_volume_pcm((int16_t *)audio_buffer_pcm_2, (size_t)(sample_count * channels));
                }
            }
        }

        // ------------ change PCM volume here ------------
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

    if(sample_count > 0)
    {
        if((filteraudio) && (pcm) && (filteraudio_active == 1) && (filteraudio_incompatible_1 == 0)
                && (filteraudio_incompatible_2 == 0))
        {
            pass_audio_output(filteraudio, pcm, (unsigned int)sample_count);
        }
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


void android_toxav_callback_video_receive_frame_h264_cb(uint32_t friend_number, uint32_t buf_size)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_toxav_callback_video_receive_frame_h264_cb_method, (jlong)(unsigned long long)friend_number,
                                     (jlong)(unsigned long long)buf_size
                                    );
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1av_1call_1status(JNIEnv *env, jobject thiz, jint status)
{
    global_av_call_active = (uint8_t)status;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1audio_1play_1volume_1percent(JNIEnv *env, jclass clazz,
        jint volume_percent)
{
    if((volume_percent >= 0) && (volume_percent <= 100))
    {
        audio_play_volume_percent_c = volume_percent;
    }

    //volume in dB 0db = unity gain, no attenuation, full amplitude signal
    //           -20db = 10x attenuation, significantly more quiet
    // ** // float volumeLevelDb = -((float)((100 - volume_percent) / 5)) + 0.0001f;
    // ** // const float VOLUME_REFERENCE = 1.0f;
    // ** // volumeMultiplier = (VOLUME_REFERENCE * pow(10, (volumeLevelDb / 20.f)));
    float volumeLevelDb = ((float)volume_percent / 100.0f) - 1.0f;
    volumeMultiplier = powf(20, volumeLevelDb);
    // ** // volumeMultiplier = ((float)audio_play_volume_percent_c / 100.0f);
    // dbg(9, "set_audio_play_volume_percent:vol=%d mul=%f", volume_percent, volumeMultiplier);
}

void change_audio_volume_pcm_null(int16_t *buf, size_t buf_size_bytes)
{
    memset(buf, 0, buf_size_bytes);
}

void change_audio_volume_pcm(int16_t *buf, size_t num_samples)
{
    for(size_t i = 0; i < num_samples; i++)
    {
        buf[i] = buf[i] * volumeMultiplier;
    }
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

void toxav_video_receive_frame_h264_cb_(ToxAV *av, uint32_t friend_number, const uint8_t *buf,
                                        const uint32_t buf_size, void *user_data)
{
    if(video_buffer_1 != NULL)
    {
        if((buf) && (buf_size > 0))
        {
            // memset(video_buffer_1, 0, video_buffer_1_size);
            memcpy(video_buffer_1, buf, (size_t)(buf_size));

            if(buf_size > 8)
            {
                dbg(9, "v_receive_frame_h264_cb:size=%d", buf_size);
                dbg(9, "v_receive_frame_h264_cb:%d %d %d %d %d %d h %d %d",
                    video_buffer_1[0],
                    video_buffer_1[1],
                    video_buffer_1[2],
                    video_buffer_1[3],
                    video_buffer_1[4],
                    video_buffer_1[5],
                    video_buffer_1[buf_size-2],
                    video_buffer_1[buf_size-1]);
            }
        }
    }

    android_toxav_callback_video_receive_frame_h264_cb(friend_number, buf_size);
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
    dbg(2, "AV video Thread #%d: starting", (int) id);
    long av_iterate_interval = 1;

    while(toxav_video_thread_stop != 1)
    {
        toxav_iterate(av);
        // dbg(9, "AV video Thread #%d running ...", (int) id);
        av_iterate_interval = toxav_iteration_interval(av);

        //usleep((av_iterate_interval / 2) * 1000);
        if(global_av_call_active == 1)
        {
            usleep(5 * 1000);
        }
        else
        {
            usleep(800 * 1000);
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
    // --------------------
    // --------------------
    android_toxav_callback_video_receive_frame_h264_cb_method = (*env)->GetStaticMethodID(env, MainActivity,
            "android_toxav_callback_video_receive_frame_h264_cb_method", "(JJ)V");
    // toxav_callback_video_receive_frame_h264(tox_av_global, toxav_video_receive_frame_h264_cb_, &mytox_CC);
    // --------------------
    // --------------------
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
    (*env)->ReleaseStringUTFChars(env, passphrase_j, s);
    size_t passphrase_len = (size_t)strlen(passphrase);
    dbg(9, "update_savedata_file");
    update_savedata_file(tox_global, (const uint8_t *)passphrase, (size_t)passphrase_len);

    if(passphrase)
    {
        free(passphrase);
    }
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file(JNIEnv *env, jobject thiz, jstring passphrase_j)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file__real(env, thiz,
                   passphrase_j));
}

void Java_com_zoffcc_applications_trifa_MainActivity_export_1savedata_1file_1unsecure(JNIEnv *env, jobject thiz,
        jstring passphrase_j, jstring export_full_path_of_file_j)
{
    if(tox_global == NULL)
    {
        return;
    }

    const char *export_full_path_of_file = (*env)->GetStringUTFChars(env, export_full_path_of_file_j, NULL);
    const char *s = (*env)->GetStringUTFChars(env, passphrase_j, NULL);
    char *passphrase = strdup(s);
    char *filename_with_path = strdup(export_full_path_of_file);
    (*env)->ReleaseStringUTFChars(env, passphrase_j, s);
    (*env)->ReleaseStringUTFChars(env, export_full_path_of_file_j, export_full_path_of_file);
    size_t passphrase_len = (size_t)strlen(passphrase);
    dbg(9, "export_savedata_file_unsecure");
    export_savedata_file_unsecure(tox_global, (const uint8_t *)passphrase, (size_t)passphrase_len, filename_with_path);

    if(passphrase)
    {
        free(passphrase);
    }

    if(filename_with_path)
    {
        free(filename_with_path);
    }
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


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1connection_1status(JNIEnv *env, jobject thiz)
{
    if(tox_global == NULL)
    {
        dbg(9, "tox_self_get_connection_status:NULL:1");
        return (jint)TOX_CONNECTION_NONE;
    }

    return (jint)(tox_self_get_connection_status(tox_global));
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

void Java_com_zoffcc_applications_trifa_MainActivity_exit__real(JNIEnv *env, jobject thiz) __attribute__((noreturn));
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

    if(capacity < raw_msg_len)
    {
        return (jint)-4;
    }

    TOX_ERR_FRIEND_SEND_MESSAGE error;
    bool res = tox_util_friend_resend_message_v2(tox_global, (uint32_t) friend_number,
               (const uint8_t *)raw_message_buffer_c,
               (const uint32_t)raw_msg_len,
               &error);

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


/** -----XX-----SPLIT-02-----XX----- */

