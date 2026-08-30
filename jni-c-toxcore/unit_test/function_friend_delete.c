/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1delete */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1delete(JNIEnv *env, jobject thiz, jlong friend_number)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jint)false;
    }

    TOX_ERR_FRIEND_DELETE error;
#ifdef TOX_HAVE_TOXUTIL
    bool res = tox_utils_friend_delete(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
#else
    bool res = tox_friend_delete(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
#endif
}
