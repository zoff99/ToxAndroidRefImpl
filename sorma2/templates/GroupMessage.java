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

package com.zoffcc.applications.sorm;

import com.zoffcc.applications.sorm.Column;
import com.zoffcc.applications.sorm.PrimaryKey;
import com.zoffcc.applications.sorm.Table;

import androidx.annotation.Nullable;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_USER;

@Table
public class GroupMessage
{
    @PrimaryKey(autoincrement = true, auto = true)
    public long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    public String message_id_tox = ""; // Tox Group Message_ID (4 bytes as hex string lowercase)

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public String group_identifier = "-1"; // f_key -> GroupDB.group_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    public int tox_group_peer_role = -1;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public int private_message = 0; // 0 -> message to group, 1 -> msg privately to/from peer

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peername = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    public int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    public int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    public long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    public boolean read = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String text = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public boolean was_synced = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public int TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    public int sync_confirmations = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peer_pubkey_syncer_01;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peer_pubkey_syncer_02;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String tox_group_peer_pubkey_syncer_03;

    @Column(indexed = true)
    @Nullable
    public long tox_group_peer_pubkey_syncer_01_sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    public long tox_group_peer_pubkey_syncer_02_sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    public long tox_group_peer_pubkey_syncer_03_sent_timestamp = 0L;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String msg_id_hash = null; // 32 byte hash

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String sent_privately_to_tox_group_peer_pubkey = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    public String file_name = "";

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    public String filename_fullpath = null;

    @Column(defaultExpr = "-1", indexed = true, helpers = Column.Helpers.ALL)
    public long filesize = -1;

    @Column(indexed = true, defaultExpr = "false")
    public boolean storage_frame_work = false;

    // ______@@SORMA_END@@______

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", tox_group_peername=" + tox_group_peername +
               ", tox_peerpubkey=" + "*tox_peerpubkey*" + ", private_message=" + private_message + ", direction=" +
               direction + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
               ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
               ", text=" + "xxxxxx" + ", is_new=" + is_new + ", was_synced=" + was_synced + " TRIFA_SYNC_TYPE=" + TRIFA_SYNC_TYPE +
               ", tox_group_peer_role=" + tox_group_peer_role;
    }
}
