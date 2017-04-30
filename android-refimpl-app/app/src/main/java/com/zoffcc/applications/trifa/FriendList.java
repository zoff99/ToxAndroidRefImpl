package com.zoffcc.applications.trifa;

public class FriendList
{
    String tox_public_key_string = "";
    long tox_friendnum = 0L;
    String name;
    String status_message;
    int TOXCONNECTION; // 0 --> offline, 1 --> online TCP, 2 --> online UDP
}
