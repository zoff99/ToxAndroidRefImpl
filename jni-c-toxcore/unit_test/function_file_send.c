/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong kind, jlong file_size, jobject file_id_buffer, jstring file_name, jlong filename_length)
{
    TRACE_LOGGER();
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
