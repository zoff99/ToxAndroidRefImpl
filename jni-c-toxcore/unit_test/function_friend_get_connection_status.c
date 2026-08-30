/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1status */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1status(JNIEnv *env, jobject thiz,
        jlong friend_number)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jint)TOX_CONNECTION_NONE;
    }

    TOX_ERR_FRIEND_QUERY error;
    TOX_CONNECTION res = tox_friend_get_connection_status(tox_global, (uint32_t)friend_number, &error);
    return (jint)res;
}
