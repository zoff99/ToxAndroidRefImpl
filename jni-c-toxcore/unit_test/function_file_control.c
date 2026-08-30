/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1control */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1control(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jint control)
{
    TRACE_LOGGER();
    TOX_ERR_FILE_CONTROL error;
    bool res = tox_file_control(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (TOX_FILE_CONTROL)control,
                                &error);

    if(res == true)
    {
        return (jint)0;
    }
    else
    {
        return (jint)-1;
    }
}
