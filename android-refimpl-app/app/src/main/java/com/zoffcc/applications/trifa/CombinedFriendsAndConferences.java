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

public class CombinedFriendsAndConferences
{
    long id = -1; // primary key for lookup // TODO: unsed now!!!!!
    boolean is_friend = true; // false -> Conference, true -> Friend

    FriendList friend_item = null;
    ConferenceDB conference_item = null;

    @Override
    public String toString()
    {
        return "id=" + id + ", is_friend=" + is_friend + ", friend_item=" + friend_item + ", conference_item" + conference_item;
    }
}
