/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message_1size */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message_1size(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    long long l = (long long)tox_self_get_status_message_size(tox_global);
    return (jlong)(unsigned long long)l;
}
