/* Extracted from ../jni-c-toxcore.c: c_safe_string_from_java */
jstring c_safe_string_from_java(const char *instr, size_t len)
{
    JNIEnv *jnienv2;
    jnienv2 = jni_getenv();
    jbyteArray data = (*jnienv2)->NewByteArray(jnienv2, (int)len);
    (*jnienv2)->SetByteArrayRegion(jnienv2, data, 0, (int)len, (const jbyte *)instr);
    jstring js1 = (jstring)(*jnienv2)->CallStaticObjectMethod(jnienv2, TrifaToxService_class, safe_string_method, data);
    (*jnienv2)->DeleteLocalRef(jnienv2, data);
    return js1;
}
