/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1seek */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1seek(JNIEnv *env, jobject thiz, jlong friend_number,
        jlong file_number, jlong position)
{
    TRACE_LOGGER();
    TOX_ERR_FILE_SEEK error;
    bool res = tox_file_seek(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (uint64_t)position, &error);

    if(res != true)
    {
        if(error == TOX_ERR_FILE_SEEK_FRIEND_NOT_FOUND)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_FRIEND_NOT_FOUND");
            return (jint)-1;
        }
        else if(error == TOX_ERR_FILE_SEEK_FRIEND_NOT_CONNECTED)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_FRIEND_NOT_CONNECTED");
            return (jint)-2;
        }
        else if(error == TOX_ERR_FILE_SEEK_NOT_FOUND)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_NOT_FOUND");
            return (jint)-3;
        }
        else if(error == TOX_ERR_FILE_SEEK_DENIED)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_DENIED");
            return (jint)-4;
        }
        else if(error == TOX_ERR_FILE_SEEK_INVALID_POSITION)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_INVALID_POSITION");
            return (jint)-5;
        }
        else if(error == TOX_ERR_FILE_SEEK_SENDQ)
        {
            dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_SEEK_SENDQ");
            return (jint)-6;
        }
        else
        {
            dbg(9, "tox_file_seek:ERROR:%d", (int)error);
            return (jint)-99;
        }
    }
    else
    {
        // dbg(9, "tox_file_seek");
        return (jint)res;
    }
}
