/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1friend_1send_1message */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1friend_1send_1message(JNIEnv *env, jobject thiz,
        jlong friend_number, jint type, jobject message, jobject hash_buffer, jlong timestamp)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jlong)-99;
    }

    uint8_t *hash_buffer_c = NULL;
    long capacity_hash = 0;
    hash_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, hash_buffer);
    capacity_hash = (*env)->GetDirectBufferCapacity(env, hash_buffer);

    if(capacity_hash < TOX_MSGV3_MSGID_LENGTH)
    {
        return -98;
    }

    uint32_t timestamp_unix = (uint32_t)timestamp;
    uint32_t timestamp_unix_buf = 0;
    xnet_pack_u32((uint8_t *)&timestamp_unix_buf, timestamp_unix);

#ifdef JAVA_LINUX

    const jclass stringClass = (*env)->GetObjectClass(env, (jstring)message);
    const jmethodID getBytes = (*env)->GetMethodID(env, stringClass, "getBytes", "(Ljava/lang/String;)[B");

    const jstring charsetName = (*env)->NewStringUTF(env, "UTF-8");
    const jbyteArray stringJbytes = (jbyteArray) (*env)->CallObjectMethod(env, (jstring)message, getBytes, charsetName);
    (*env)->DeleteLocalRef(env, charsetName);

    const jsize plength = (*env)->GetArrayLength(env, stringJbytes);
    jbyte* pBytes = (*env)->GetByteArrayElements(env, stringJbytes, NULL);

    if (plength > TOX_MSGV3_MAX_MESSAGE_LENGTH)
    {
        (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
        (*env)->DeleteLocalRef(env, stringJbytes);
        dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG:TOX_MSGV3_MAX_MESSAGE_LENGTH");
        return (jlong)-5;
    }

    uint8_t *message_str_v3 = (uint8_t *)calloc(1, (size_t)(plength + TOX_MSGV3_GUARD + TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH));
    if (!message_str_v3)
    {
        (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
        (*env)->DeleteLocalRef(env, stringJbytes);
        dbg(9, "tox_friend_send_message:ERROR:TOX_MSGV3:can not allocate buffer");
        return (jlong)-5;
    }

    uint8_t* position = message_str_v3;
    memcpy(position, pBytes, (size_t)(plength));
    position = position + plength;
    position = position + TOX_MSGV3_GUARD;
    memcpy(position, hash_buffer_c, (size_t)(TOX_MSGV3_MSGID_LENGTH));
    position = position + TOX_MSGV3_MSGID_LENGTH;
    memcpy(position, &timestamp_unix_buf, (size_t)(TOX_MSGV3_TIMESTAMP_LENGTH));

    size_t new_len = plength + TOX_MSGV3_GUARD + TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH;

    TOX_ERR_FRIEND_SEND_MESSAGE error;
    uint32_t res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)message_str_v3,
                                           (size_t)new_len, &error);

    free(message_str_v3);

    (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
    (*env)->DeleteLocalRef(env, stringJbytes);

#else

    const char *message_str = NULL;
    // TODO: UTF-8
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_FRIEND_SEND_MESSAGE error;

    if (strlen(message_str) > TOX_MSGV3_MAX_MESSAGE_LENGTH)
    {
        (*env)->ReleaseStringUTFChars(env, message, message_str);
        dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG:TOX_MSGV3_MAX_MESSAGE_LENGTH");
        return (jlong)-5;
    }

    uint8_t *message_str_v3 = (uint8_t *)calloc(1, (size_t)(strlen(message_str) + TOX_MSGV3_GUARD + TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH));
    if (!message_str_v3)
    {
        (*env)->ReleaseStringUTFChars(env, message, message_str);
        dbg(9, "tox_friend_send_message:ERROR:TOX_MSGV3:can not allocate buffer");
        return (jlong)-5;
    }

    uint8_t* position = message_str_v3;
    memcpy(position, message_str, (size_t)(strlen(message_str)));
    position = position + strlen(message_str);
    position = position + TOX_MSGV3_GUARD;
    memcpy(position, hash_buffer_c, (size_t)(TOX_MSGV3_MSGID_LENGTH));
    position = position + TOX_MSGV3_MSGID_LENGTH;
    memcpy(position, &timestamp_unix_buf, (size_t)(TOX_MSGV3_TIMESTAMP_LENGTH));

    size_t new_len = strlen(message_str) + TOX_MSGV3_GUARD + TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH;

    uint32_t res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)message_str_v3,
                                           new_len, &error);

    free(message_str_v3);

#endif

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
        // dbg(9, "tox_friend_send_message");
        return (jlong)(unsigned long long)res;
    }
}
