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
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table(indexes = @Index(value = {"conference_identifier", "peer_pubkey"}, unique = true))
public class ConferencePeerCacheDB
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    // TODO: this column maybe NOT needed. peer name for this pubkey must be the same in all conferences? i am not sure ...
    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String conference_identifier = ""; // for now (bytes->HexString) of the cookie used to join the conference!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String peer_pubkey = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    String peer_name = "";

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long last_update_timestamp = -1L;


    static ConferencePeerCacheDB deep_copy(ConferencePeerCacheDB in)
    {
        ConferencePeerCacheDB out = new ConferencePeerCacheDB();
        out.conference_identifier = in.conference_identifier;
        out.peer_pubkey = in.peer_pubkey;
        out.peer_name = in.peer_name;
        out.last_update_timestamp = in.last_update_timestamp;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", conference_identifier=" + conference_identifier + ", peer_pubkey=" + peer_pubkey + ", peer_name=" + peer_name + ", last_update_timestamp=" + last_update_timestamp;
    }
}
