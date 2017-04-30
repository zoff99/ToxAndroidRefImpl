package com.zoffcc.applications.trifa;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class FriendList
{
    @PrimaryKey
    String tox_public_key_string = "";

    @Column(indexed = true, unique = true)
    long tox_friendnum = 0L;

    @Column
    @Nullable
    String name;

    @Column
    @Nullable
    String status_message;

    @Column
    int TOXCONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column
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

    @Override
    public String toString()
    {
        return tox_friendnum + ":" + tox_public_key_string + ":" + name + ":" + status_message + ":" + TOXCONNECTION + ":" + TOX_USER_STATUS;
    }
}
