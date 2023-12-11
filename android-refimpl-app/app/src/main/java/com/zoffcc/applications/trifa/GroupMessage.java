/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2022 Zoff <zoff@zoff.cc>
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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import androidx.annotation.Nullable;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;

@Table
public class GroupMessage
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    String message_id_tox = ""; // Tox Group Message_ID (4 bytes as hex string lowercase)

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    String group_identifier = "-1"; // f_key -> GroupDB.group_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_group_peer_pubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    int private_message = 0; // 0 -> message to group, 1 -> msg privately to/from peer

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String tox_group_peername = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    boolean was_synced = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    int TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int sync_confirmations = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String tox_group_peer_pubkey_syncer_01;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String tox_group_peer_pubkey_syncer_02;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String tox_group_peer_pubkey_syncer_03;

    @Column(indexed = true)
    @Nullable
    long tox_group_peer_pubkey_syncer_01_sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    long tox_group_peer_pubkey_syncer_02_sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    long tox_group_peer_pubkey_syncer_03_sent_timestamp = 0L;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String msg_id_hash = null; // 32 byte hash

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String sent_privately_to_tox_group_peer_pubkey = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String file_name = "";

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String filename_fullpath = null;

    @Column(defaultExpr = "-1", indexed = true, helpers = Column.Helpers.ALL)
    long filesize = -1;

    @Column(indexed = true, defaultExpr = "false")
    boolean storage_frame_work = false;

    static GroupMessage deep_copy(GroupMessage in)
    {
        GroupMessage out = new GroupMessage();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id_tox = in.message_id_tox;
        out.group_identifier = in.group_identifier;
        out.tox_group_peer_pubkey = in.tox_group_peer_pubkey;
        out.private_message = in.private_message;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
        out.tox_group_peername = in.tox_group_peername;
        out.was_synced = in.was_synced;
        out.msg_id_hash = in.msg_id_hash;
        out.sent_privately_to_tox_group_peer_pubkey = in.sent_privately_to_tox_group_peer_pubkey;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.filesize = in.filesize;
        out.filename_fullpath = in.filename_fullpath;
        out.storage_frame_work = in.storage_frame_work;
        out.TRIFA_SYNC_TYPE = in.TRIFA_SYNC_TYPE;
        out.sync_confirmations = in.sync_confirmations;
        out.tox_group_peer_pubkey_syncer_01 = in.tox_group_peer_pubkey_syncer_01;
        out.tox_group_peer_pubkey_syncer_02 = in.tox_group_peer_pubkey_syncer_02;
        out.tox_group_peer_pubkey_syncer_03 = in.tox_group_peer_pubkey_syncer_03;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", tox_group_peername=" + tox_group_peername +
               ", tox_peerpubkey=" + "*tox_peerpubkey*" + ", private_message=" + private_message + ", direction=" +
               direction + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
               ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
               ", text=" + "xxxxxx" + ", is_new=" + is_new + ", was_synced=" + was_synced + " TRIFA_SYNC_TYPE=" + TRIFA_SYNC_TYPE;
    }
}
