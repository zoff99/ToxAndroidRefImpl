package com.zoffcc.applications.trifa;

import android.support.annotation.Nullable;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.Table;

// for CREATE INDEX:
@Table(indexes = @Index(value = {"from_tox_friendnum", "to_tox_friendnum"} // ,unique = true
))
public class Message
{
    @Column(indexed = true)
    long from_tox_friendnum = 0L;

    @Column(indexed = true)
    long to_tox_friendnum = 0L;

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column
    @Nullable
    long sent_timestamp = 0L;

    @Column
    @Nullable
    long rcvd_timestamp = 0L;

    @Column
    boolean read = false;

    @Column
    @Nullable
    String text = null;

    @Column
    @Nullable
    String filename_fullpath = null;


    static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.from_tox_friendnum = in.from_tox_friendnum;
        out.to_tox_friendnum = in.to_tox_friendnum;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;

        return out;
    }

    @Override
    public String toString()
    {
        return from_tox_friendnum + ":" + to_tox_friendnum + ":" + TOX_MESSAGE_TYPE + ":" + sent_timestamp + ":" + rcvd_timestamp + ":" + read + ":" + text + ":" + filename_fullpath;
    }
}
