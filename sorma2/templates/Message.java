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

package com.zoffcc.applications.sorm;


import com.zoffcc.applications.sorm.Column;
import com.zoffcc.applications.sorm.PrimaryKey;
import com.zoffcc.applications.sorm.Table;

import androidx.annotation.Nullable;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2;

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
    long sent_timestamp = 0L; // the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    long sent_timestamp_ms = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp = 0L; // the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp_ms = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int send_retries = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean is_new = true;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String msg_id_hash = null; // 32byte hash, used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String raw_msgv2_bytes = null; // used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, defaultExpr = "0")
    int msg_version; // 0 -> old Message, 1 -> for MessageV2 Message

    @Column(indexed = true, defaultExpr = "" + TRIFAGlobals.MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION)
    int resend_count; // how many times we have tried to resend old text messages

    @Column(indexed = true, defaultExpr = "false")
    boolean storage_frame_work = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_outgoing_queued = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean msg_at_relay = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String msg_idv3_hash = null; // 32byte hash, used for MessageV3 Messages! and otherwise NULL

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    int sent_push = 0;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    int filetransfer_kind = TOX_FILE_KIND_DATA.value;

    // ______@@SORMA_END@@______

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id=" + message_id + ", filetransfer_id=" + filetransfer_id + ", filedb_id=" +
               filedb_id + ", tox_friendpubkey=" + "*pubkey*" + ", direction=" + direction + ", state=" + state +
               ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
               ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
               ", send_retries=" + send_retries + ", text=" + "xxxxxx" + ", filename_fullpath=" + filename_fullpath +
               ", is_new=" + is_new + ", msg_id_hash=" + msg_id_hash + ", msg_version=" + msg_version +
               ", resend_count=" + resend_count + ", raw_msgv2_bytes=" + "xxxxxx" + ", storage_frame_work=" +
               storage_frame_work + ", ft_outgoing_queued=" + ft_outgoing_queued + ", msg_at_relay=" + msg_at_relay +
               ", sent_push=" + sent_push + ", filetransfer_kind=" + filetransfer_kind;
    }
}
