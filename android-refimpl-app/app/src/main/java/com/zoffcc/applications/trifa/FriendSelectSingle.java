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

public class FriendSelectSingle
{
    String name;
    String pubkey;
    int type;

    public FriendSelectSingle(String name, String pubkey, int type)
    {
        this.name = name;
        this.pubkey = pubkey;
        this.type = type; // 0 -> friend, 2 -> ngc group
    }

    public FriendSelectSingle(String name, String pubkey)
    {
        this.name = name;
        this.pubkey = pubkey;
        this.type = 0; // 0 -> friend, 2 -> ngc group
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
}
