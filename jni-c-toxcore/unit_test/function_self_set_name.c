/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1name */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1name(JNIEnv *env, jobject thiz, jobject name)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return (jint)-1;
    }

#ifdef JAVA_LINUX

    const jclass stringClass = (*env)->GetObjectClass(env, (jstring)name);
    const jmethodID getBytes = (*env)->GetMethodID(env, stringClass, "getBytes", "(Ljava/lang/String;)[B");

    const jstring charsetName = (*env)->NewStringUTF(env, "UTF-8");

    // MEDIUM SECURITY FIX: NewStringUTF can return NULL on out-of-memory.
    // Without this check, passing NULL to CallObjectMethod would cause
    // undefined behavior or a crash.
    if(charsetName == NULL)
    {
        return (jint)-3;
    }

    const jbyteArray stringJbytes = (jbyteArray) (*env)->CallObjectMethod(env, (jstring)name, getBytes, charsetName);
    (*env)->DeleteLocalRef(env, charsetName);

    const jsize plength = (*env)->GetArrayLength(env, stringJbytes);
    jbyte* pBytes = (*env)->GetByteArrayElements(env, stringJbytes, NULL);

    // MEDIUM SECURITY FIX: Validate name length before passing to toxcore.
    // The Tox protocol limits names to TOX_MAX_NAME_LENGTH (128 bytes).
    // Without this check, a malicious caller could pass an arbitrarily long
    // name string, which would be forwarded to toxcore. While toxcore may
    // reject it internally, passing oversized data wastes resources and
    // could expose the system to edge cases or future vulnerabilities in
    // the toxcore name handling path.
    if(plength > TOX_MAX_NAME_LENGTH)
    {
        (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
        (*env)->DeleteLocalRef(env, stringJbytes);
        return (jint)-2;
    }

    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_name(tox_global, (uint8_t *)pBytes, (size_t)plength, &error);

    (*env)->ReleaseByteArrayElements(env, stringJbytes, pBytes, JNI_ABORT);
    (*env)->DeleteLocalRef(env, stringJbytes);

    return (jint)res;

#else

    const char *s = NULL;
    // TODO: UTF-8
    s = (*env)->GetStringUTFChars(env, name, NULL);

    // MEDIUM SECURITY FIX: GetStringUTFChars can return NULL on out-of-memory.
    // Without this check, passing NULL to strlen() would cause undefined
    // behavior (crash), and ReleaseStringUTFChars with NULL is also invalid.
    if(s == NULL)
    {
        return (jint)-4;
    }

    // MEDIUM SECURITY FIX: Validate name length before passing to toxcore.
    // The Tox protocol limits names to TOX_MAX_NAME_LENGTH (128 bytes).
    // Without this check, a malicious caller could pass an arbitrarily long
    // name string, which would be forwarded to toxcore.
    if(strlen(s) > TOX_MAX_NAME_LENGTH)
    {
        (*env)->ReleaseStringUTFChars(env, name, s);
        return (jint)-2;
    }

    TOX_ERR_SET_INFO error;
    bool res = tox_self_set_name(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
    (*env)->ReleaseStringUTFChars(env, name, s);
    return (jint)res;

#endif
}
