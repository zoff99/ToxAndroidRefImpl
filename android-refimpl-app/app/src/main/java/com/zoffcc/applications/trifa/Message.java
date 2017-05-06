package com.zoffcc.applications.trifa;

import android.support.annotation.Nullable;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

// for CREATE INDEX:
@Table
public class Message
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long message_id = -1;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long tox_friendnum;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String filename_fullpath = null;

    static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.tox_friendnum = in.tox_friendnum;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;
        out.message_id = in.message_id;

        return out;
    }

    @Override
    public String toString()
    {
        return tox_friendnum + ":" + direction + ":" + TOX_MESSAGE_TYPE + ":" + sent_timestamp + ":" + rcvd_timestamp + ":" + read + ":" + text + ":" + filename_fullpath + ":" + message_id;
    }
}
