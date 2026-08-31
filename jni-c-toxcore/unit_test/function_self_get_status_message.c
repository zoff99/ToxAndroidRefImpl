/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    size_t length = tox_self_get_status_message_size(tox_global);

    // HIGH SECURITY FIX: Reject the operation if the status message size exceeds TOX_MAX_STATUS_MESSAGE_LENGTH.
    // tox_self_get_status_message_size() returns the size of the status message stored in toxcore.
    // Under normal conditions this is at most TOX_MAX_STATUS_MESSAGE_LENGTH (1007 bytes), but if the
    // toxcore state is corrupted or a future bug causes it to return an excessively large value,
    // the VLA "char message[length + 1]" would allocate a massive buffer on the stack, causing
    // stack exhaustion and a crash. If length is SIZE_MAX, then "length + 1" wraps to 0, creating
    // a zero-length VLA which is undefined behavior in C.
    // We must reject the operation entirely rather than trying to truncate, because
    // tox_self_get_status_message() will write the full message into the buffer, and truncating
    // the length would cause a buffer overflow.
    if(length > TOX_MAX_STATUS_MESSAGE_LENGTH)
    {
        return NULL;
    }

    char message[length + 1];
    CLEAR(message);
    tox_self_get_status_message(tox_global, (uint8_t *)message);
    jstring js1 = c_safe_string_from_java((char *)message, length);
    return js1;
}
