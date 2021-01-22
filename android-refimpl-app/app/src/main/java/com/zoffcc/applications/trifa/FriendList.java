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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import androidx.annotation.Nullable;

@Table
public class FriendList
{
    // pubkey is always saved as UPPER CASE hex string!! -----------------
    @PrimaryKey
    String tox_public_key_string = "";
    // pubkey is always saved as UPPER CASE hex string!! -----------------

    @Column
    @Nullable
    String name;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String alias_name;

    @Column
    @Nullable
    String status_message;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_real; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_on_off_real; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_USER_STATUS; // 0 --> NONE, 1 --> online AWAY, 2 --> online BUSY

    @Column
    @Nullable
    String avatar_pathname = null;

    @Column
    @Nullable
    String avatar_filename = null;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean avatar_update = false; // has avatar changed for this friend?

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long avatar_update_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this friend?

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int sort = 0;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp_real = -1L;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long added_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean is_relay = false;

    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.name = in.name;
        out.status_message = in.status_message;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_real = in.TOX_CONNECTION_real;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.TOX_CONNECTION_on_off_real = in.TOX_CONNECTION_on_off_real;
        out.TOX_USER_STATUS = in.TOX_USER_STATUS;
        out.avatar_filename = in.avatar_filename;
        out.avatar_pathname = in.avatar_pathname;
        out.avatar_update = in.avatar_update;
        out.notification_silent = in.notification_silent;
        out.sort = in.sort;
        out.last_online_timestamp = in.last_online_timestamp;
        out.last_online_timestamp_real = in.last_online_timestamp_real;
        out.alias_name = in.alias_name;
        out.is_relay = in.is_relay;
        out.avatar_update_timestamp = in.avatar_update_timestamp;
        out.added_timestamp = in.added_timestamp;

        return out;
    }

    @Override
    public String toString()
    {
        try
        {
            return "tox_public_key_string=" + tox_public_key_string.substring(0, 4) + ", is_relay=" + is_relay +
                   ", name=" + name + ", status_message=" + status_message + ", TOX_CONNECTION=" + TOX_CONNECTION +
                   ", TOX_CONNECTION_on_off=" + TOX_CONNECTION_on_off + ", TOX_CONNECTION_real=" + TOX_CONNECTION_real +
                   ", TOX_USER_STATUS=" + TOX_USER_STATUS + ", avatar_pathname=" + avatar_pathname +
                   ", avatar_filename=" + avatar_filename + ", notification_silent=" + notification_silent + ", sort=" +
                   sort + ", last_online_timestamp=" + last_online_timestamp + ", alias_name=" + alias_name +
                   ", avatar_update=" + avatar_update + ", added_timestamp=" + added_timestamp;
        }
        catch (Exception e)
        {
            return "*Exception*";
        }
    }
}
