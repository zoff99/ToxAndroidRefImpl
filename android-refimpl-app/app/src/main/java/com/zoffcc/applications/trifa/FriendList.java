package com.zoffcc.applications.trifa;

public class FriendList
{
    String tox_public_key_string = "";
    long tox_friendnum = 0L;
    String name;
    String status_message;
    int TOXCONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)
    int TOX_USER_STATUS; // 0 --> NONE, 1 --> online AWAY, 2 --> online BUSY


    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.tox_friendnum = in.tox_friendnum;
        out.name = in.name;
        out.status_message = in.status_message;
        out.TOXCONNECTION = in.TOXCONNECTION;
        out.TOX_USER_STATUS = in.TOX_USER_STATUS;

        return out;
    }
}
