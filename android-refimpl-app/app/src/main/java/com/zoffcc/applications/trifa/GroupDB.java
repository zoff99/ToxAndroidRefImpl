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

import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_PRIVACY_STATE;

@Table
public class GroupDB
{
    // group id is always saved as lower case hex string!! -----------------
    @PrimaryKey
    String group_identifier = "";
    // group id is always saved as lower case hex string!! -----------------

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String who_invited__tox_public_key_string = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String name = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long peer_count = -1;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long own_peer_number = -1;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int privacy_state = TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long tox_group_number = -1; // this changes often!!

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this conference?

    static GroupDB deep_copy(GroupDB in)
    {
        GroupDB out = new GroupDB();
        out.group_identifier = in.group_identifier;
        out.name = in.name;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.privacy_state = in.privacy_state;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
        out.tox_group_number = in.tox_group_number;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "tox_group_number=" + tox_group_number + ", group_identifier=" + group_identifier +
               ", who_invited__tox_public_key_string=" + who_invited__tox_public_key_string + ", name=" + name +
               ", privacy_state=" + privacy_state + ", peer_count=" + peer_count + ", own_peer_number=" +
               own_peer_number + ", notification_silent=" + notification_silent;
    }
}
