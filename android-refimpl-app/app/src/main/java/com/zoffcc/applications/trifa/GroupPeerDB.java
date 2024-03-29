/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2023 Zoff <zoff@zoff.cc>
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
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import androidx.annotation.Nullable;

@Table(indexes = @Index(value = {"group_identifier", "tox_group_peer_pubkey"}, unique = true))
public class GroupPeerDB
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String group_identifier = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_group_peer_pubkey = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String peer_name = "";

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_update_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long first_join_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "2", helpers = Column.Helpers.ALL)
    int Tox_Group_Role = 2;

    static GroupPeerDB deep_copy(GroupPeerDB in)
    {
        GroupPeerDB out = new GroupPeerDB();
        out.group_identifier = in.group_identifier;
        out.tox_group_peer_pubkey = in.tox_group_peer_pubkey;
        out.peer_name = in.peer_name;
        out.last_update_timestamp = in.last_update_timestamp;
        out.first_join_timestamp = in.first_join_timestamp;
        out.Tox_Group_Role = in.Tox_Group_Role;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", group_identifier=" + group_identifier + ", tox_group_peer_pubkey=" +
               tox_group_peer_pubkey.substring(0, 4) + ", peer_name=" + peer_name +
               ", last_update_timestamp=" + last_update_timestamp +
               ", first_join_timestamp=" + first_join_timestamp + ", Tox_Group_Role=" + Tox_Group_Role;
    }
}
