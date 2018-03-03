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

@Table
public class FriendList
{
    @PrimaryKey
    String tox_public_key_string = "";

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
    int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

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
    boolean notification_silent = false; // show notifications for this friend?

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int sort = 0;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp = -1L;

    static FriendList deep_copy(FriendList in)
    {
        FriendList out = new FriendList();
        out.tox_public_key_string = in.tox_public_key_string;
        out.name = in.name;
        out.status_message = in.status_message;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.TOX_USER_STATUS = in.TOX_USER_STATUS;
        out.avatar_filename = in.avatar_filename;
        out.avatar_pathname = in.avatar_pathname;
        out.notification_silent = in.notification_silent;
        out.sort = in.sort;
        out.last_online_timestamp = in.last_online_timestamp;
        out.alias_name = in.alias_name;

        return out;
    }

    @Override
    public String toString()
    {
        try
        {
        return "tox_public_key_string=" + tox_public_key_string.substring(0, 4) + ", name=" + name + ", status_message=" +
               status_message + ", TOX_CONNECTION=" + TOX_CONNECTION + ", TOX_CONNECTION_on_off=" +
               TOX_CONNECTION_on_off + ", TOX_USER_STATUS=" + TOX_USER_STATUS + ", avatar_pathname=" + avatar_pathname +
               ", avatar_filename=" + avatar_filename + ", notification_silent=" + notification_silent + ", sort=" +
               sort + ", last_online_timestamp=" + last_online_timestamp + ", alias_name=" + alias_name;
        }
        catch(Exception e)
        {
            return "*Exception*";
        }
    }
}
