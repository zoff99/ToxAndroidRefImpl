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
public class RelayListDB
{
    @PrimaryKey
    String tox_public_key_string = "";

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION; // 0 --> NONE (offline), 1 --> TCP (online), 2 --> UDP (online)

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int TOX_CONNECTION_on_off; // 0 --> offline, 1 --> online

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean own_relay = false; // false --> friends relay, true --> my relay

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_online_timestamp = -1L;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String tox_public_key_string_of_owner = "";

    // ______@@SORMA_END@@______

    @Override
    public String toString()
    {
        try
        {
            return "tox_public_key_string=" + tox_public_key_string.substring(0, 4) +
                   ", owner_pubkey=" + tox_public_key_string_of_owner.substring(0, 4) +
                   ", own_relay=" + own_relay +
                   ", TOX_CONNECTION=" + TOX_CONNECTION +
                   ", TOX_CONNECTION_on_off=" + TOX_CONNECTION_on_off
                   + ", last_online_timestamp=" + last_online_timestamp;
        }
        catch (Exception e)
        {
            return "*Exception*";
        }
    }
}
