/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
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

import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.GroupDB;

public class FriendSelectSingle
{
    public static final int TYPE_FRIEND = 0;
    public static final int TYPE_NGC_GROUP = 2;
    /** section header only, not selectable */
    public static final int TYPE_SEPARATOR = 3;
    /** "nothing to select" info row, not selectable */
    public static final int TYPE_EMPTY_INFO = 4;

    String name;
    String pubkey;
    int type;

    boolean pinned = false;
    /** friend: TOX_CONNECTION_real != NONE, group: connected */
    boolean online = false;

    /** text shown in the fastscroller bubble */
    String section_text = " ";

    /** DB rows, so the adapter never has to query per getView() */
    FriendList friend_item = null;
    GroupDB group_item = null;

    public FriendSelectSingle(String name, String pubkey, int type)
    {
        this.name = name;
        this.pubkey = pubkey;
        this.type = type;
    }

    public FriendSelectSingle(String name, String pubkey)
    {
        this(name, pubkey, TYPE_FRIEND);
    }

    public static FriendSelectSingle separator(String label)
    {
        final FriendSelectSingle s = new FriendSelectSingle(label, null, TYPE_SEPARATOR);
        s.section_text = label;
        return s;
    }

    public static FriendSelectSingle empty_info(String message)
    {
        final FriendSelectSingle s = new FriendSelectSingle(message, null, TYPE_EMPTY_INFO);
        s.section_text = " ";
        return s;
    }

    public String getName()
    {
        return name;
    }

    public int getType()
    {
        return type;
    }

    public String getPK()
    {
        return pubkey;
    }

    public boolean isPinned()
    {
        return pinned;
    }

    public boolean isOnline()
    {
        return online;
    }
}
