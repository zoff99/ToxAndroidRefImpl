/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2022 Zoff <zoff@zoff.cc>
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

import android.util.Log;

import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperGroup
{
    private static final String TAG = "trifa.Hlp.Group";

    static void add_group_wrapper(final long friend_number, long group_num, String group_identifier_in, final int a_TOX_GROUP_PRIVACY_STATE)
    {
        if (group_num < 0)
        {
            Log.d(TAG, "add_group_wrapper:ERR:group number less than zero:" + group_num);
            return;
        }

        String group_identifier = group_identifier_in;


        if (group_num >= 0)
        {
            new_or_updated_group(group_num, HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                 group_identifier_in, a_TOX_GROUP_PRIVACY_STATE);
        }
        else
        {
            //Log.i(TAG, "add_conference_wrapper:error=" + conference_num + " joining conference");
        }

        // save tox savedate file
        HelperGeneric.update_savedata_file_wrapper();
    }

    static void new_or_updated_group(long group_num, String who_invited_public_key, String group_identifier, int privacy_state)
    {
        try
        {
            // Log.i(TAG, "new_or_updated_group:" + "group_num=" + group_identifier);
            final GroupDB conf2 = orma.selectFromGroupDB().
                    group_identifierEq(group_identifier).toList().get(0);
            // group already exists -> update and connect
            orma.updateGroupDB().
                    group_identifierEq(group_identifier).
                    privacy_state(privacy_state).
                    tox_group_number(group_num).execute();

            try
            {
                Log.i(TAG, "new_or_updated_group:*update*");
                final GroupDB conf3 = orma.selectFromGroupDB().
                        group_identifierEq(group_identifier).toList().get(0);
                // TODO: add to main friendlist
                // update or add to "friendlist"
                /*
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = false;
                cc.conference_item = ConferenceDB.deep_copy(conf3);
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                */
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_group:EE3:" + e3.getMessage());
            }

            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_or_updated_group:EE1:" + e.getMessage());

            // conference is new -> add
            try
            {
                GroupDB conf_new = new GroupDB();
                conf_new.group_identifier = group_identifier;
                conf_new.who_invited__tox_public_key_string = who_invited_public_key;
                conf_new.peer_count = -1;
                conf_new.own_peer_number = -1;
                conf_new.privacy_state = privacy_state;
                conf_new.tox_group_number = group_num;
                //
                orma.insertIntoGroupDB(conf_new);
                Log.i(TAG, "new_or_updated_group:+ADD+");

                try
                {
                    // TODO: add to main friendlist
                    // update or add to "friendlist"
                    /*
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = ConferenceDB.deep_copy(conf_new);
                    MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                    */
                }
                catch (Exception e4)
                {
                    Log.i(TAG, "new_or_updated_group:EE4:" + e4.getMessage());
                }

                return;
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Log.i(TAG, "new_or_updated_group:EE2:" + e1.getMessage());
            }
        }
    }
}
