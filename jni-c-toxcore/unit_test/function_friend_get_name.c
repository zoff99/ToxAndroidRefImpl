/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1name */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1name(JNIEnv *env, jobject thiz, jlong friend_number)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return NULL;
    }

    Tox_Err_Friend_Query error;
    size_t length = tox_friend_get_name_size(tox_global, (uint32_t)friend_number, &error);
    if (error != 0)
    {
        return NULL;
    }

    // HIGH SECURITY FIX: Reject the operation if the name size exceeds TOX_MAX_NAME_LENGTH.
    // tox_friend_get_name_size() returns the size of the friend's name stored in toxcore.
    // Under normal conditions this is at most TOX_MAX_NAME_LENGTH (128 bytes), but if the
    // toxcore state is corrupted or a future bug causes it to return an excessively large
    // value, the VLA "char name[length + 1]" would allocate a massive buffer on the stack,
    // causing stack exhaustion and a crash. If length is SIZE_MAX, then "length + 1" wraps
    // to 0, creating a zero-length VLA which is undefined behavior in C.
    // We must reject the operation entirely rather than trying to truncate, because
    // tox_friend_get_name() will write the full name into the buffer, and truncating the
    // length would cause a buffer overflow.
    if(length > TOX_MAX_NAME_LENGTH)
    {
        return NULL;
    }

    char name[length + 1];
    CLEAR(name);

    tox_friend_get_name(tox_global, friend_number, (uint8_t *)name, &error);
    if (error != 0)
    {
        return NULL;
    }

    jstring js1 = c_safe_string_from_java((char *)name, length);
    return js1;
}
