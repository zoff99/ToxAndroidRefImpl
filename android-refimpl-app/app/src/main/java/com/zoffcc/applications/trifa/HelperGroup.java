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

import android.database.Cursor;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Locale;

import androidx.annotation.NonNull;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_by_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_chat_id;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA;
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

        try
        {
            if (MainActivity.group_message_list_activity != null)
            {
                if (MainActivity.group_message_list_activity.get_current_group_id().equals(group_identifier))
                {
                    MainActivity.group_message_list_activity.set_group_connection_status_icon();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                // update or add to "friendlist"
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = COMBINED_IS_CONFERENCE;
                cc.group_item = GroupDB.deep_copy(conf3);
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_group:EE3:" + e3.getMessage());
                e3.printStackTrace();
            }

            return;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
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
                conf_new.group_active = false;
                conf_new.tox_group_number = group_num;
                //
                orma.insertIntoGroupDB(conf_new);
                Log.i(TAG, "new_or_updated_group:+ADD+");

                try
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = COMBINED_IS_CONFERENCE;
                    cc.group_item = GroupDB.deep_copy(conf_new);
                    MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
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

    public static long tox_group_by_confid__wrapper(@NonNull String group_id_string)
    {
        ByteBuffer group_id_buffer = ByteBuffer.allocateDirect(GROUP_ID_LENGTH);
        byte[] data = HelperGeneric.hex_to_bytes(group_id_string.toUpperCase());
        group_id_buffer.put(data);
        group_id_buffer.rewind();

        long res = tox_group_by_chat_id(group_id_buffer);
        if (res == UINT32_MAX_JAVA)
        {
            return -1;
        }
        else if (res < 0)
        {
            return -1;
        }
        else
        {
            return res;
        }
    }

    public static String tox_group_by_groupnum__wrapper(long groupnum)
    {
        try
        {
            ByteBuffer groupid_buf = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);
            if (tox_group_get_chat_id(groupnum, groupid_buf) == 0)
            {
                byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                groupid_buf.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                return bytes_to_hex(groupid_buffer);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static long insert_into_group_message_db(final GroupMessage m, final boolean update_group_view_flag)
    {
        long row_id = orma.insertIntoGroupMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM GroupMessage where rowid='" + row_id + "'");
            cursor.moveToFirst();
            //Log.i(TAG, "insert_into_conference_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_group_view_flag)
            {
                add_single_group_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static void add_single_group_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (MainActivity.group_message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                GroupMessage m = orma.selectFromGroupMessage().idEq(message_id).orderByIdDesc().get(0);

                                if (m.id != -1)
                                {
                                    if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                    MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                    System.currentTimeMillis()))
                                    {
                                        MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.group_message_list_fragment.add_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static String tox_group_peer_get_name__wrapper(String group_identifier, String group_peer_pubkey)
    {
        // TODO: write me
        return "some peer";
    }

    public static String tox_group_peer_get_public_key__wrapper(long group_num, long peer_number)
    {
        String result = MainActivity.tox_group_peer_get_public_key(group_num, peer_number);
        return result;
    }

    static boolean is_group_active(String group_identifier)
    {
        try
        {
            return (orma.selectFromGroupDB().
                    group_identifierEq(group_identifier).
                    toList().get(0).group_active);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    static void set_group_active(String group_identifier)
    {
        try
        {
            orma.updateGroupDB().
                    group_identifierEq(group_identifier).
                    group_active(true).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_group_active:EE:" + e.getMessage());
        }
    }

    static void set_group_inactive(String group_identifier)
    {
        try
        {
            orma.updateGroupDB().
                    group_identifierEq(group_identifier).
                    group_active(false).
                    execute();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            // Log.i(TAG, "set_group_inactive:EE:" + e.getMessage());
        }
    }

    static String group_identifier_short(String group_identifier, boolean uppercase_result)
    {
        try
        {
            if (uppercase_result)
            {
                return (group_identifier.substring(group_identifier.length() - 6,
                                                   group_identifier.length())).toUpperCase(Locale.ENGLISH);
            }
            else
            {
                return group_identifier.substring(group_identifier.length() - 6, group_identifier.length());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return group_identifier;
        }
    }

    static void update_group_in_db_topic(final String group_identifier, final String name)
    {
        orma.updateGroupDB().
                group_identifierEq(group_identifier).
                name(name).
                execute();
    }
}