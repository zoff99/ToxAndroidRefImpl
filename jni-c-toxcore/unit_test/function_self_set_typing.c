/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1typing */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1typing(JNIEnv *env, jobject thiz, jlong friend_number,
        jint typing)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jint)-1;
    }

    TOX_ERR_SET_TYPING error;
    bool res = tox_self_set_typing(tox_global, (uint32_t)friend_number, (bool)typing, &error);
    return (jint)res;
}
