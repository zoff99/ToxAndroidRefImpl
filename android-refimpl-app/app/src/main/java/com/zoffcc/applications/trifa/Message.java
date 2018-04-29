/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.support.annotation.Nullable;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;

@Table
public class Message
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long message_id = -1; // ID given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_friendpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "1", helpers = Column.Helpers.ALL)
    int state = TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_outgoing_started = false;

    @Column(indexed = true, defaultExpr = "-1")
    long filedb_id; // f_key -> FileDB.id

    @Column(indexed = true, defaultExpr = "-1")
    long filetransfer_id; // f_key -> Filetransfer.id

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    long sent_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    long sent_timestamp_ms = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp_ms = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int send_retries = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String msg_id_hash = null; // 32bit hash, used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String raw_msgv2_bytes = null; // used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, defaultExpr = "0")
    int msg_version; // 0 -> old Message, 1 -> for MessageV2 Message

    static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id = in.message_id;
        out.tox_friendpubkey = in.tox_friendpubkey;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.filedb_id = in.filedb_id;
        out.filetransfer_id = in.filetransfer_id;
        out.sent_timestamp = in.sent_timestamp;
        out.sent_timestamp_ms = in.sent_timestamp_ms;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.rcvd_timestamp_ms = in.rcvd_timestamp_ms;
        out.read = in.read;
        out.send_retries = in.send_retries;
        out.is_new = in.is_new;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;
        out.msg_id_hash = in.msg_id_hash;
        out.msg_version = in.msg_version;
        out.raw_msgv2_bytes = in.raw_msgv2_bytes;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id=" + message_id + ", filetransfer_id=" + filetransfer_id + ", filedb_id=" +
               filedb_id + ", tox_friendpubkey=" + "*pubkey*" + ", direction=" + direction + ", state=" + state +
               ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
               ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
               ", send_retries=" + send_retries + ", text=" + "xxxxxx" + ", filename_fullpath=" + filename_fullpath +
               ", is_new=" + is_new + ", msg_id_hash=" + msg_id_hash + ", msg_version=" + msg_version +
               ", raw_msgv2_bytes=" + "xxxxxx";
    }
}
