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
    char name[length + 1];
    CLEAR(name);
    // dbg(9, "name len=%d", (int)length);
    tox_self_get_name(tox_global, (uint8_t *)name);
    // dbg(9, "name=%s", (char *)name);
    // return (*env)->NewStringUTF(env, (uint8_t *)name);
    jstring js1 = c_safe_string_from_java((char *)name, length);
    return js1;
}
