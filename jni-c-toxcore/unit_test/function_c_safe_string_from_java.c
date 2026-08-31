/* Extracted from ../jni-c-toxcore.c: c_safe_string_from_java */
/**
 * Safely converts a C string (which may contain invalid UTF-8 or binary data)
 * into a Java String using a dedicated Java-side sanitization method.
 *
 * WHY THIS IS USED:
 * Standard JNI functions like NewStringUTF() will crash the JVM (SIGABRT) if
 * the input C string contains invalid UTF-8 sequences. Tox core data (names,
 * status messages, etc.) is not guaranteed to be valid UTF-8. By passing the
 * raw bytes as a jbyteArray to a Java method (safe_string_method), we delegate
 * the UTF-8 sanitization and fallback handling to Java, which is much more
 * robust and prevents JVM crashes.
 *
 * SECURITY & ROBUSTNESS GUARANTEES:
 * 1. NULL pointer checks for instr, jnienv, class, and method.
 * 2. Length overflow protection: rejects lengths that exceed INT_MAX to prevent
 *    integer truncation when casting to (int) for JNI calls.
 * 3. Graceful degradation: returns NULL on any JNI failure (OOM, missing class)
 *    instead of crashing the native process.
 * 4. Proper cleanup: ensures the temporary jbyteArray is deleted even if the
 *    Java method call fails or throws an exception.
 *
 * @param instr Pointer to the raw C string/byte array. Can be NULL if len == 0.
 * @param len   Length of the string in bytes.
 * @return      A valid jstring on success, or NULL on any failure.
 */
jstring c_safe_string_from_java(const char *instr, size_t len)
{
    // 1. Handle zero-length strings explicitly to avoid NULL pointer issues in JNI
    if (len == 0) {
        JNIEnv *jnienv2 = jni_getenv();
        if (jnienv2 == NULL) return NULL;
        return (jstring)(*jnienv2)->NewStringUTF(jnienv2, "");
    }

    // 2. Reject NULL input for non-zero length to prevent segfaults
    if (instr == NULL) {
        return NULL;
    }

    // 3. Prevent integer overflow when casting size_t to jint (int)
    if (len > (size_t)INT_MAX) {
        return NULL;
    }

    JNIEnv *jnienv2 = jni_getenv();
    if (jnienv2 == NULL) {
        return NULL;
    }

    if (TrifaToxService_class == NULL || safe_string_method == NULL) {
        return NULL;
    }

    // 5. Allocate byte array. May return NULL on OutOfMemoryError.
    jbyteArray data = (*jnienv2)->NewByteArray(jnienv2, (jsize)len);
    if (data == NULL) {
        (*jnienv2)->ExceptionClear(jnienv2);
        return NULL;
    }

    // 6. Copy data. Safe because len <= INT_MAX and instr != NULL
    (*jnienv2)->SetByteArrayRegion(jnienv2, data, 0, (jsize)len, (const jbyte *)instr);

    // 7. Call Java sanitization method
    jstring js1 = (jstring)(*jnienv2)->CallStaticObjectMethod(jnienv2, TrifaToxService_class, safe_string_method, data);

    // Check for Java exceptions (e.g., if the Java method threw something)
    if ((*jnienv2)->ExceptionCheck(jnienv2)) {
        (*jnienv2)->ExceptionClear(jnienv2);
        (*jnienv2)->DeleteLocalRef(jnienv2, data);
        return NULL;
    }

    // 8. Clean up local reference
    (*jnienv2)->DeleteLocalRef(jnienv2, data);

    return js1;
}
