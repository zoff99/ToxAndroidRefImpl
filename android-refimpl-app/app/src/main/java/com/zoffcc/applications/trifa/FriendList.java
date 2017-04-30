package com.zoffcc.applications.trifa;

public class FriendList
{
    String tox_public_key_string = "";
    long tox_friendnum = 0L;
    String name;
    String status_message;
    int TOXCONNECTION; // 0 --> offline, 1 --> online TCP, 2 --> online UDP


    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.tox_friendnum = in.tox_friendnum;
        out.name = in.name;
        out.status_message = in.status_message;
        out.TOXCONNECTION = in.TOXCONNECTION;

        return out;
    }
}
