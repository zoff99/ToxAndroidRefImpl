/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1get_1file_1id */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1get_1file_1id(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jobject file_id_buffer)
{
    TRACE_LOGGER();
    uint8_t *file_id_buffer_c = NULL;
    long capacity = 0;

    if(file_id_buffer == NULL)
    {
        return -3;
    }

    file_id_buffer_c = (uint8_t *)(*env)->GetDirectBufferAddress(env, file_id_buffer);
    capacity = (*env)->GetDirectBufferCapacity(env, file_id_buffer);

    if(capacity < TOX_FILE_ID_LENGTH)
    {
        return -2;
    }

    TOX_ERR_FILE_GET error;
    bool res = tox_file_get_file_id(tox_global, (uint32_t)friend_number, (uint32_t)file_number, file_id_buffer_c, &error);

    if(res != true)
    {
        return -1;
    }
    else
    {
        return 0;
    }
}
