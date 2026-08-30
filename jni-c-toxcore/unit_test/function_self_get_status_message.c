/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message(JNIEnv *env, jobject thiz)
{
    TRACE_LOGGER();
    size_t length = tox_self_get_status_message_size(tox_global);
    char message[length + 1];
    CLEAR(message);
    tox_self_get_status_message(tox_global, (uint8_t *)message);
    jstring js1 = c_safe_string_from_java((char *)message, length);
    return js1;
}
