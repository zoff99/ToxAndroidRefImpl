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

import com.zoffcc.applications.sorm.ConferenceDB;
import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.GroupDB;

public class CombinedFriendsAndConferences
{
    long id = -1; // primary key for lookup // TODO: unsed now!!!!!
    int is_friend = COMBINED_IS_FRIEND; // 0 -> Friend, 1 -> Conference, 2 -> group

    FriendList friend_item = null;
    ConferenceDB conference_item = null;
    GroupDB group_item = null;

    final static int COMBINED_IS_FRIEND = 0;
    final static int COMBINED_IS_CONFERENCE = 1;
    final static int COMBINED_IS_GROUP = 2;

    @Override
    public String toString()
    {
        return "id=" + id + ", is_friend=" + is_friend + ", friend_item=" + friend_item + ", conference_item" +
               conference_item + ", group_item" + group_item;
    }
}
