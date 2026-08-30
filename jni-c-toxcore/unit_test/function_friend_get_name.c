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
