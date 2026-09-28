/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message(JNIEnv *env, jobject thiz, jobject status_message)
{
    TRACE_LOGGER();
    if (tox_global == NULL) return (jint)-1;

    size_t len = 0;
    jint err = 0;
    
    // 1. Get the safely converted Standard UTF-8 string
    uint8_t* data = jni_get_utf8_safe(env, (jstring)status_message, TOX_MAX_STATUS_MESSAGE_LENGTH, &len, &err);
    
    if (data == NULL) {
        return -1;
    }

    // 2. Pass to Toxcore
    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_status_message(tox_global, data, len, &error);

    // 3. Clean up and return
    free(data);
    return (jint)res;
}
