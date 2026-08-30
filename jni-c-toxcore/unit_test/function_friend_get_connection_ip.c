/* Extracted from ../jni-c-toxcore.c: Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1ip */
JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1ip(JNIEnv *env, jobject thiz,
        jlong friend_number)
{
    TRACE_LOGGER();
    if(tox_global == NULL)
    {
        return NULL;
    }

/*
    const int ipv6_strlen_max = 39;
    const int port_strlen_max = 5;
    const int space_char_strlen_max = 1;
    const int max_tcp_relays_per_friend = 6;
    const int max_length = ((ipv6_strlen_max + space_char_strlen_max + port_strlen_max + 2) * max_tcp_relays_per_friend) + 10;
*/
// compiler hates humanity, so its a define now. silly.
#define IP_STR_MAX_STR_LEN (((39 + 1 + 5 + 2) * 6) + 10)
    char ip_str[IP_STR_MAX_STR_LEN + 1];
    CLEAR(ip_str);
    tox_friend_get_connection_ip(tox_global, (uint32_t)friend_number, (uint8_t *)ip_str);
    jstring js1 = c_safe_string_from_java((char *)ip_str, IP_STR_MAX_STR_LEN);
    return js1;
}
