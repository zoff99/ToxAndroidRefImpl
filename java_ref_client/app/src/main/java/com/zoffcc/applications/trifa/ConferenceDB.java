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

import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;

@Table
public class ConferenceDB
{
    @PrimaryKey
    String conference_identifier = ""; // for now (bytes->HexString) of the cookie used to join the conference!!

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
    int kind = TOX_CONFERENCE_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long tox_conference_number = -1; // this changes often!!

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean conference_active = false; // is this conference active now? are we invited?

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this conference?

    static ConferenceDB deep_copy(ConferenceDB in)
    {
        ConferenceDB out = new ConferenceDB();
        out.conference_identifier = in.conference_identifier;
        out.name = in.name;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.kind = in.kind;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
        out.tox_conference_number = in.tox_conference_number;
        out.conference_active = in.conference_active;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "tox_conference_number=" + tox_conference_number + ", conference_active=" + conference_active + ", conference_identifier=" + conference_identifier + ", who_invited__tox_public_key_string=" + who_invited__tox_public_key_string + ", name=" + name + ", kind=" + kind + ", peer_count=" + peer_count + ", own_peer_number=" + own_peer_number + ", notification_silent=" + notification_silent;
    }
}
