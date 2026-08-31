/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1get_1new_1message_1id */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1messagev3_1get_1new_1message_1id(JNIEnv *env, jobject thiz, jobject hash_buffer)
{
    TRACE_LOGGER();

    uint8_t *hash_buffer_c = NULL;
    long capacity_hash = 0;
    hash_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, hash_buffer);
    capacity_hash = (*env)->GetDirectBufferCapacity(env, hash_buffer);

    if(capacity_hash < TOX_MSGV3_MSGID_LENGTH)
    {
        return -2;
    }

    // CRITICAL SECURITY FIX: Validate that the direct buffer address is not NULL.
    // GetDirectBufferAddress() can return NULL if the buffer is invalid or has been freed.
    // Without this check, passing NULL to tox_messagev3_get_new_message_id() would cause
    // a NULL pointer dereference, leading to a crash (Denial of Service vulnerability).
    if (hash_buffer_c == NULL) {
        return -2;  // Return error code for invalid buffer
    }

    bool res = tox_messagev3_get_new_message_id(hash_buffer_c);

    if(res != true)
    {
        return -1;
    }
    else
    {
        return 0;
    }
}
