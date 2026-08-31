/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return NULL;
    }
    size_t length = tox_self_get_name_size(tox_global);

    // HIGH SECURITY FIX: Reject the operation if the name size exceeds TOX_MAX_NAME_LENGTH.
    // tox_self_get_name_size() returns the size of the name stored in toxcore.
    // Under normal conditions this is at most TOX_MAX_NAME_LENGTH (128 bytes), but if the
    // toxcore state is corrupted or a future bug causes it to return an excessively large
    // value, the VLA "char name[length + 1]" would allocate a massive buffer on the stack,
    // causing stack exhaustion and a crash. If length is SIZE_MAX, then "length + 1" wraps
    // to 0, creating a zero-length VLA which is undefined behavior in C.
    // We must reject the operation entirely rather than trying to truncate, because
    // tox_self_get_name() will write the full name into the buffer, and truncating the
    // length would cause a buffer overflow.
    if(length > TOX_MAX_NAME_LENGTH)
    {
        return NULL;
    }

    char name[length + 1];
    CLEAR(name);
    // dbg(9, "name len=%d", (int)length);
    tox_self_get_name(tox_global, (uint8_t *)name);
    // dbg(9, "name=%s", (char *)name);
    // return (*env)->NewStringUTF(env, (uint8_t *)name);
    jstring js1 = c_safe_string_from_java((char *)name, length);
    return js1;
}
