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

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String avatar_ftid_hex = null;

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

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String push_url;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String ip_addr_str;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    long capabilities = 0;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    long msgv3_capability = 0;

    // ______@@SORMA_END@@______

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
                   ", avatar_update=" + avatar_update + ", added_timestamp=" + added_timestamp + ", push_url=" +
                   "*****" + ", capabilities=" + capabilities + ", avatar_ftid_hex=" + avatar_ftid_hex;
        }
        catch (Exception e)
        {
            return "*Exception*";
        }
    }
}
