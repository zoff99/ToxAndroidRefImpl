/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1lossless_1packet */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1lossless_1packet(JNIEnv *env, jobject thiz,
        jlong friend_number, jbyteArray data, jint data_length)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jlong)-9991;
    }

    jbyte *data2 = (*env)->GetByteArrayElements(env, data, 0);
    TOX_ERR_FRIEND_CUSTOM_PACKET error;
    uint32_t res = tox_friend_send_lossless_packet(tox_global, (uint32_t)friend_number, (const uint8_t *)data2,
                   (size_t)data_length, &error);
    (*env)->ReleaseByteArrayElements(env, data, data2, JNI_ABORT); /* abort to not copy back contents */

    if(error != 0)
    {
        // dbg(9, "tox_friend_send_lossless_packet:ERROR:%d", (int)error);
        return (jlong)-99;
    }
    else
    {
        // dbg(9, "tox_friend_send_lossless_packet");
        return (jlong)(unsigned long long)res;
    }
}
