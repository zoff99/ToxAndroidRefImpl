/* Extracted from ../jni-c-toxcore.c: android_tox_callback_friend_message_cb */
void android_tox_callback_friend_message_cb(uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message,
        size_t length)
{
    JNIEnv *jnienv2 = jni_getenv();

    // CRITICAL SECURITY FIX: Check jnienv2 before dereferencing.
    // jni_getenv() can return NULL if the thread is not attached to the JVM.
    // Without this check, any subsequent JNI call will cause a NULL pointer
    // dereference crash (SIGSEGV).
    if (jnienv2 == NULL) {
        return;
    }

    uint8_t *message_copy = (uint8_t *)message;
    size_t new_length = length;
    int need_free = 0;
    jbyteArray msgV3_hash_jbuffer = NULL;
    uint32_t msgV3_timestamp = 0;

    //dbg(9, "friend_message_cb:------------- len=%d len2=%d msg=%p", length,
    //        (TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH + TOX_MSGV3_GUARD),
    //        message);

    if ((message != NULL) && (length > (TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH + TOX_MSGV3_GUARD)))
    {
        int pos = length - (TOX_MSGV3_MSGID_LENGTH + TOX_MSGV3_TIMESTAMP_LENGTH + TOX_MSGV3_GUARD);

        // check for guard
        uint8_t g1 = *(message + pos);
        uint8_t g2 = *(message + pos + 1);

        // dbg(9, "friend_message_cb:------------- g1=%d g2=%d", g1, g2);

        // check for the msgv3 guard
        if ((g1 == 0) && (g2 == 0))
        {
            // uint8_t m1 = *(message + 0);
            // uint8_t m2 = *(message + 1);
            // uint8_t m3 = *(message + 2);
            // uint8_t m4 = *(message + 3);
            // uint8_t m5 = *(message + 4);
            // dbg(9, "friend_message_cb:g %d %d : %d %d %d %d %d full len=%d", g1, g2, m1, m2, m3, m4, m5, length);

            message_copy = calloc(1, length);
            if (!message_copy)
            {
                dbg(9, "friend_message_cb:could not allocate buffer: incoming message discarded");
                return;
            }

            msgV3_hash_jbuffer = (*jnienv2)->NewByteArray(jnienv2, (int)TOX_MSGV3_MSGID_LENGTH);
            // dbg(9, "friend_message_cb:msgV3_hash_jbuffer:%p", msgV3_hash_jbuffer);
            if(msgV3_hash_jbuffer != NULL)
            {
                uint8_t *msgV3_hash_buffer_bin = (uint8_t *)(message + pos + 2);
                (*jnienv2)->SetByteArrayRegion(jnienv2, msgV3_hash_jbuffer, 0, (int)TOX_MSGV3_MSGID_LENGTH, (const jbyte *)msgV3_hash_buffer_bin);
                // dbg(9, "friend_message_cb:msgV3_hash_buffer_bin:%p", msgV3_hash_buffer_bin);
            }

            const uint8_t *p = (uint8_t *)(message + pos + 2);
            p = p + 32;
            p += xnet_unpack_u32(p, &msgV3_timestamp);

            new_length = pos;
            memcpy(message_copy, message, new_length);
            // NULL out the extra data
            memset(message_copy + pos, 0, (length - new_length));
            // indicate that we need to free the allocated buffer later
            need_free = 1;
        }
    }

    jstring js1 = c_safe_string_from_java((char *)message_copy, new_length);

    // Guard against NULL global references before calling Java
    if (MainActivity != NULL && android_tox_callback_friend_message_cb_method != NULL) {
          (*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
                                     android_tox_callback_friend_message_cb_method,
                                     (jlong)(unsigned long long)friend_number,
                                     (jint) type,
                                     js1,
                                     (jlong)(unsigned long long)new_length,
                                     msgV3_hash_jbuffer,
                                     (jlong)msgV3_timestamp);
    }

    if (js1 != NULL) {
        (*jnienv2)->DeleteLocalRef(jnienv2, js1);
    }

    if(msgV3_hash_jbuffer != NULL)
    {
        (*jnienv2)->DeleteLocalRef(jnienv2, msgV3_hash_jbuffer);
    }

    if (need_free == 1 && message_copy != NULL)
    {
        free(message_copy);
    }
}
