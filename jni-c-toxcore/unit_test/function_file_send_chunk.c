/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send_1chunk */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send_1chunk(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jlong position, jobject data_buffer, jlong data_length)
{
    TRACE_LOGGER();
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
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_NULL");
            return (jint)-1;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND");
            return (jint)-2;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED");
            return (jint)-3;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND");
            return (jint)-4;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING");
            return (jint)-5;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH");
            return (jint)-6;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_SENDQ)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_SENDQ");
            return (jint)-7;
        }
        else if(error == TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION)
        {
            dbg(0, "tox_file_send_chunk:TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION");
            return (jint)-8;
        }
        else
        {
            return (jint)-99;
        }
    }

    return (jint)0;
}
