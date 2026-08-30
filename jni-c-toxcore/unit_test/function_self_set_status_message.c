/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message(JNIEnv *env, jobject thiz,
        jobject status_message)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jint)-1;
    }

#ifdef JAVA_LINUX

    const jclass stringClass = (*env)->GetObjectClass(env, (jstring)status_message);
    const jmethodID getBytes = (*env)->GetMethodID(env, stringClass, "getBytes", "(Ljava/lang/String;)[B");

    const jstring charsetName = (*env)->NewStringUTF(env, "UTF-8");
    const jbyteArray stringJbytes = (jbyteArray) (*env)->CallObjectMethod(env, (jstring)status_message, getBytes, charsetName);
    (*env)->DeleteLocalRef(env, charsetName);

    const jsize plength = (*env)->GetArrayLength(env, stringJbytes);
    jbyte* pBytes = (*env)->GetByteArrayElements(env, stringJbytes, NULL);

    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_status_message(tox_global, (uint8_t *)pBytes, (size_t)plength, &error);

    (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
    (*env)->DeleteLocalRef(env, stringJbytes);

    return (jint)res;

#else

    const char *s = NULL;
    // TODO: UTF-8
    s = (*env)->GetStringUTFChars(env, status_message, NULL);
    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_status_message(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
    (*env)->ReleaseStringUTFChars(env, status_message, s);
    return (jint)res;

#endif

}
