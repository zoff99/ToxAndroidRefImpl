/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add(JNIEnv *env, jobject thiz, jobject toxid_str,
        jobject message)
{
    TRACE_LOGGER();

    if(tox_global == NULL)
    {
        return (jlong)-3;
    }

    unsigned char public_key_bin[TOX_ADDRESS_SIZE];
    char *public_key_str2 = NULL;
    const char *s = NULL;
    const char *message_str = NULL;
    s = (*env)->GetStringUTFChars(env, toxid_str, NULL);
    // dbg(9, "add friend:s=%p", s);
    public_key_str2 = strdup(s);
    // dbg(9, "add friend:public_key_str2=%p", public_key_str2);
    // dbg(9, "add friend:TOX_PUBLIC_KEY_SIZE len=%d", (int)TOX_ADDRESS_SIZE);
    // dbg(9, "add friend:public_key_str2 len=%d", strlen(public_key_str2));
    message_str = (*env)->GetStringUTFChars(env, message, NULL);
    TOX_ERR_FRIEND_ADD error;

    // HIGH SECURITY FIX: Validate hex string length before conversion.
    // A Tox address must be exactly TOX_ADDRESS_SIZE * 2 hex characters (76 chars).
    // Without this check, a hex string that is too long will cause toxid_hex_to_bin()
    // to write beyond the public_key_bin buffer (buffer overflow vulnerability).
    // A string that is too short may result in incomplete or incorrect conversion.
    // Both cases can lead to memory corruption or adding the wrong friend.
    if(public_key_str2 == NULL || strlen(public_key_str2) != TOX_ADDRESS_SIZE * 2)
    {
        if(public_key_str2)
        {
            free(public_key_str2);
        }
        (*env)->ReleaseStringUTFChars(env, message, message_str);
        (*env)->ReleaseStringUTFChars(env, toxid_str, s);
        return (jlong)-4;  // Return error code for invalid Tox ID length
    }

    toxid_hex_to_bin(public_key_bin, public_key_str2);
    // dbg(9, "add friend:public_key_bin=%p", public_key_bin);
    // dbg(9, "add friend:public_key_bin len=%d", strlen(public_key_bin));
    // dbg(9, "add friend:message_str=%p", message_str);
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
