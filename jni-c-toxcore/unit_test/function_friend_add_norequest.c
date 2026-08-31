/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add_1norequest */
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add_1norequest(JNIEnv *env, jobject thiz,
        jobject public_key_str)
{
    TRACE_LOGGER();

    // MEDIUM SECURITY FIX: Check tox_global before using it.
    // Without this check, calling tox_friend_add_norequest() with a NULL tox_global
    // pointer would cause a NULL pointer dereference crash inside toxcore.
    if(tox_global == NULL)
    {
        return (jlong)-3;
    }

    unsigned char public_key_bin[TOX_PUBLIC_KEY_SIZE];
    char *public_key_str2 = NULL;
    const char *s = NULL;
    s = (*env)->GetStringUTFChars(env, public_key_str, NULL);

    // HIGH SECURITY FIX: GetStringUTFChars can return NULL on out-of-memory
    // or if the string cannot be converted. Without this check, passing NULL
    // to strdup() is undefined behavior (crashes on glibc with SIGSEGV),
    // and later strlen(public_key_str2) would also crash.
    if(s == NULL)
    {
        return (jlong)-2;
    }

    public_key_str2 = strdup(s);
    (*env)->ReleaseStringUTFChars(env, public_key_str, s);

    // HIGH SECURITY FIX: Validate hex string length before conversion.
    // A Tox public key must be exactly TOX_PUBLIC_KEY_SIZE * 2 hex characters (64 chars).
    // Without this check, a hex string that is too long will cause toxpk_hex_to_bin()
    // to write beyond the public_key_bin buffer (buffer overflow vulnerability).
    // A string that is too short may result in incomplete or incorrect conversion.
    // Both cases can lead to memory corruption or adding the wrong friend.
    // Also check strdup() result - it can return NULL on OOM.
    if(public_key_str2 == NULL || strlen(public_key_str2) != TOX_PUBLIC_KEY_SIZE * 2)
    {
        if(public_key_str2)
        {
            free(public_key_str2);
        }
        return (jlong)-1;
    }

    toxpk_hex_to_bin(public_key_bin, public_key_str2);
    uint32_t friendnum = tox_friend_add_norequest(tox_global, (uint8_t *)public_key_bin, NULL);

    if(public_key_str2)
    {
        free(public_key_str2);
    }

    dbg(9, "add friend norequest");
    return (jlong)(unsigned long long)friendnum;
}
