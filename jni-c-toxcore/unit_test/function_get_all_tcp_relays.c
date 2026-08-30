/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1get_1all_1tcp_1relays */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1get_1all_1tcp_1relays(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    size_t length = 60301; // minimum according to tox.h
    char result_c[length];
    CLEAR(result_c);

    if(tox_global == NULL)
    {
        return (jstring)NULL;
    }

    tox_get_all_tcp_relays(tox_global, (char *)result_c);
    jstring result = c_safe_string_from_java((char *)result_c, length);
    return result;
}
