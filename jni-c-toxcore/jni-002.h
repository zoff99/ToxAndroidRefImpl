
/** -----XX-----SPLIT-02-----XX----- */


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
 * Return the number of offline peers in the conference. Return value is unspecified on failure.
 */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1offline_1peer_1count(JNIEnv *env, jobject thiz,
        jlong conference_number)
{
    if(tox_global == NULL)
    {
        return (jlong)-99;
    }

    TOX_ERR_CONFERENCE_PEER_QUERY error;
    uint32_t res = tox_conference_offline_peer_count(tox_global, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_PEER_QUERY_OK)
    {
        if(error == TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND)
        {
            dbg(0, "tox_conference_offline_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND)
        {
            dbg(0, "tox_conference_offline_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION)
        {
            dbg(0, "tox_conference_offline_peer_count:TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION");
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



/**
 * Creates a new conference.
 *
 * This function creates a new text conference.
 *
 * @return conference number on success, or an unspecified value on failure.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1new(JNIEnv *env, jobject thiz)
{
    if(tox_global == NULL)
    {
        return (jint)-99;
    }

    TOX_ERR_CONFERENCE_NEW error;
    uint32_t res = tox_conference_new(tox_global, &error);

    if(error != TOX_ERR_CONFERENCE_NEW_OK)
    {
        if(error == TOX_ERR_CONFERENCE_NEW_INIT)
        {
            dbg(0, "tox_conference_new:TOX_ERR_CONFERENCE_NEW_INIT");
            return (jint)-1;
        }
        else
        {
            return (jint)-99;
        }
    }

    return (jint)res;
}


/**
 * Invites a friend to a conference.
 *
 * We must be connected to the conference, meaning that the conference has not
 * been deleted, and either we created the conference with the tox_conference_new function,
 * or a `conference_connected` event has occurred for the conference.
 *
 * @param friend_number The friend number of the friend we want to invite.
 * @param conference_number The conference number of the conference we want to invite the friend to.
 *
 * @return true on success.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1conference_1invite(JNIEnv *env, jobject thiz,
        jlong friend_number, jlong conference_number)
{
    TOX_ERR_CONFERENCE_INVITE error;
    bool res = tox_conference_invite(tox_global, (uint32_t)friend_number, (uint32_t)conference_number, &error);

    if(error != TOX_ERR_CONFERENCE_INVITE_OK)
    {
        if(error == TOX_ERR_CONFERENCE_INVITE_CONFERENCE_NOT_FOUND)
        {
            dbg(0, "tox_conference_invite:TOX_ERR_CONFERENCE_INVITE_CONFERENCE_NOT_FOUND");
            return (jlong)-1;
        }
        else if(error == TOX_ERR_CONFERENCE_INVITE_FAIL_SEND)
        {
            dbg(0, "tox_conference_invite:TOX_ERR_CONFERENCE_INVITE_FAIL_SEND");
            return (jlong)-2;
        }
        else if(error == TOX_ERR_CONFERENCE_INVITE_NO_CONNECTION)
        {
            dbg(0, "tox_conference_invite:TOX_ERR_CONFERENCE_INVITE_NO_CONNECTION");
            return (jlong)-3;
        }
        else
        {
            return (jint)-99;
        }
    }

    return (jint)res;
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
    dbg(9, "JNI:toxav_call_control:ENTER");
    TOXAV_ERR_CALL_CONTROL error;
    bool res = toxav_call_control(tox_av_global, (uint32_t)friend_number, (TOXAV_CALL_CONTROL)control, &error);
    dbg(9, "JNI:toxav_call_control:FINISHED");
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


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1video_1send_1frame_1h264(JNIEnv *env, jobject thiz,
        jlong friend_number, jint frame_width_px, jint frame_height_px, jlong data_len)
{
    TOXAV_ERR_SEND_FRAME error;
    bool res = toxav_video_send_frame_h264(tox_av_global, (uint32_t)friend_number, (uint16_t)frame_width_px,
                                           (uint16_t)frame_height_px,
                                           (uint8_t *)video_buffer_2,
                                           (uint32_t)data_len, &error);
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
    TOXAV_ERR_SEND_FRAME error = 0;

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

        if(sample_count > 0)
        {
            if((filteraudio) && (pcm) && (filteraudio_active == 1) && (filteraudio_incompatible_1 == 0)
                    && (filteraudio_incompatible_2 == 0))
            {
                filter_audio(filteraudio, pcm, (unsigned int)sample_count);
            }
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
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wnull-dereference"

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

#pragma GCC diagnostic pop
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC(JNIEnv *env, jobject thiz)
{
    COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(env, thiz));
}
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------





