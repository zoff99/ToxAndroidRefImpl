/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1message */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1message(JNIEnv *env, jobject thiz,
        jlong friend_number, jint type, jobject message)
{
    TRACE_LOGGER();
    TOX_ERR_FRIEND_SEND_MESSAGE error;
    uint32_t res = 0;

    if(tox_global == NULL)
    {
        return (jlong)-99;
    }


#ifdef JAVA_LINUX

    const jclass stringClass = (*env)->GetObjectClass(env, (jstring)message);
    const jmethodID getBytes = (*env)->GetMethodID(env, stringClass, "getBytes", "(Ljava/lang/String;)[B");

    const jstring charsetName = (*env)->NewStringUTF(env, "UTF-8");
    // MEDIUM SECURITY FIX: NewStringUTF can return NULL on out-of-memory.
    if(charsetName == NULL)
    {
        return (jlong)-98;
    }

    const jbyteArray stringJbytes = (jbyteArray) (*env)->CallObjectMethod(env, (jstring)message, getBytes, charsetName);
    (*env)->DeleteLocalRef(env, charsetName);

    const jsize plength = (*env)->GetArrayLength(env, stringJbytes);
    jbyte* pBytes = (*env)->GetByteArrayElements(env, stringJbytes, NULL);

    // MEDIUM SECURITY FIX: Validate message length before passing to toxcore.
    // The Tox protocol limits messages to TOX_MAX_MESSAGE_LENGTH.
    // Without this check, a malicious caller could pass an arbitrarily long
    // message string, which would be forwarded to toxcore.
    if(plength > TOX_MAX_MESSAGE_LENGTH)
    {
        (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
        (*env)->DeleteLocalRef(env, stringJbytes);
        return (jlong)-97;
    }

    res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)pBytes,
                                  (size_t)plength, &error);
    (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
    (*env)->DeleteLocalRef(env, stringJbytes);

#else

    const char *message_str = NULL;
    // TODO: UTF-8
    message_str = (*env)->GetStringUTFChars(env, message, NULL);

    // MEDIUM SECURITY FIX: GetStringUTFChars can return NULL on out-of-memory.
    if(message_str == NULL)
    {
        return (jlong)-98;
    }

    // MEDIUM SECURITY FIX: Validate message length before passing to toxcore.
    // The Tox protocol limits messages to TOX_MAX_MESSAGE_LENGTH.
    if(strlen(message_str) > TOX_MAX_MESSAGE_LENGTH)
    {
        (*env)->ReleaseStringUTFChars(env, message, message_str);
        return (jlong)-97;
    }

    res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)message_str,
                                  (size_t)strlen(message_str), &error);
    (*env)->ReleaseStringUTFChars(env, message, message_str);

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
