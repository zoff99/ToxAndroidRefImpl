/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status */
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status(JNIEnv *env, jobject thiz, jint status)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return;
    }

    tox_self_set_status(tox_global, (TOX_USER_STATUS)status);
}
