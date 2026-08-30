/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name_1size */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name_1size(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jlong)0;
    }

    long long l = (long long)tox_self_get_name_size(tox_global);
    return (jlong)(unsigned long long)l;
}
