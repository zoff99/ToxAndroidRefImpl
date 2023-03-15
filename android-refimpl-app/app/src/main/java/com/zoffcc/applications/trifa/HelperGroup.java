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

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import androidx.annotation.NonNull;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_GROUP;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.display_toast;
import static com.zoffcc.applications.trifa.HelperGeneric.fourbytes_of_long_to_hex;
import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__conference_show_system_messages;
import static com.zoffcc.applications.trifa.MainActivity.group_message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_by_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_peer_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_custom_packet;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_ADD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_CHAT_ID_SIZE;
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
            if (group_message_list_activity != null)
            {
                if (group_message_list_activity.get_current_group_id().equals(group_identifier))
                {
                    group_message_list_activity.set_group_connection_status_icon();
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
                    group_identifierEq(group_identifier.toLowerCase()).toList().get(0);
            // group already exists -> update and connect
            orma.updateGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).
                    privacy_state(privacy_state).
                    tox_group_number(group_num).execute();

            try
            {
                Log.i(TAG, "new_or_updated_group:*update*");
                final GroupDB conf3 = orma.selectFromGroupDB().
                        group_identifierEq(group_identifier.toLowerCase()).toList().get(0);
                // update or add to "friendlist"
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = COMBINED_IS_GROUP;
                cc.group_item = GroupDB.deep_copy(conf3);
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_group:EE3:" + e3.getMessage());
                // e3.printStackTrace();
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
                String group_topic = "";
                try
                {
                    group_topic = tox_group_get_name(group_num);
                    Log.i(TAG, "new_or_updated_group:group_topic=" + group_topic);
                    if (group_topic == null)
                    {
                        group_topic = "";
                    }
                }
                catch (Exception e6)
                {
                    e6.printStackTrace();
                    Log.i(TAG, "new_or_updated_group:EE6:" + e6.getMessage());
                }

                GroupDB conf_new = new GroupDB();
                conf_new.group_identifier = group_identifier;
                conf_new.who_invited__tox_public_key_string = who_invited_public_key;
                conf_new.peer_count = -1;
                conf_new.own_peer_number = -1;
                conf_new.privacy_state = privacy_state;
                conf_new.group_active = false;
                conf_new.tox_group_number = group_num;
                conf_new.name = group_topic;
                //
                orma.insertIntoGroupDB(conf_new);
                Log.i(TAG, "new_or_updated_group:+ADD+");

                try
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = COMBINED_IS_GROUP;
                    cc.group_item = GroupDB.deep_copy(conf_new);
                    Log.i(TAG, "new_or_updated_group:EE4__:" + MainActivity.friend_list_fragment + " " + cc);
                    MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                    //!! if we are coming from another activity the friend_list_fragment might not be initialized yet!!
                }
                catch (Exception e4)
                {
                    e4.printStackTrace();
                    // Log.i(TAG, "new_or_updated_group:EE4:" + e4.getMessage());
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

    static void update_group_in_friendlist(long group_num)
    {
        try
        {
            final String group_identifier = tox_group_by_groupnum__wrapper(group_num);
            Log.i(TAG, "new_or_updated_group:*update*");
            final GroupDB conf3 = orma.selectFromGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).toList().get(0);
            // update in "friendlist"
            CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
            cc.is_friend = COMBINED_IS_GROUP;
            cc.group_item = GroupDB.deep_copy(conf3);
            MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
        }
        catch (Exception e3)
        {
            Log.i(TAG, "update_group_in_friendlist:EE3:" + e3.getMessage());
        }
    }

    public static long tox_group_by_groupid__wrapper(@NonNull String group_id_string)
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
                if ((PREF__conference_show_system_messages == false) &&
                    (m.tox_group_peer_pubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)))
                {
                    // HINT: dont show system message because of user PREF
                }
                else
                {
                    add_single_group_message_from_messge_id(msg_id, true);
                }
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
        try
        {
            return tox_group_peer_get_name(tox_group_by_groupid__wrapper(group_identifier),
                                           get_group_peernum_from_peer_pubkey(group_identifier, group_peer_pubkey));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /*
   this is a bit costly, asking for pubkeys of all group peers
   */
    static long get_group_peernum_from_peer_pubkey(final String group_identifier, final String peer_pubkey)
    {
        try
        {
            long group_num = tox_group_by_groupid__wrapper(group_identifier);
            long num_peers = MainActivity.tox_group_peer_count(group_num);

            if (num_peers > 0)
            {
                long[] peers = tox_group_get_peerlist(group_num);
                if (peers != null)
                {
                    long i = 0;
                    for (i = 0; i < num_peers; i++)
                    {
                        try
                        {
                            String pubkey_try = tox_group_peer_get_public_key(group_num, peers[(int) i]);
                            if (pubkey_try != null)
                            {
                                if (pubkey_try.equals(peer_pubkey))
                                {
                                    // we found the peer number
                                    return peers[(int) i];
                                }
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
            return -2;
        }
        catch (Exception e)
        {
            return -2;
        }
    }


    public static String tox_group_peer_get_public_key__wrapper(long group_num, long peer_number)
    {
        String result = null;
        try
        {
            result = MainActivity.tox_group_peer_get_public_key(group_num, peer_number);
        }
        catch (Exception ignored)
        {
        }
        return result;
    }

    static boolean is_group_active(String group_identifier)
    {
        try
        {
            return (orma.selectFromGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).
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
                    group_identifierEq(group_identifier.toLowerCase()).
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
                    group_identifierEq(group_identifier.toLowerCase()).
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

    static void update_group_in_db_name(final String group_identifier, final String name)
    {
        try
        {
            orma.updateGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).
                    name(name).
                    execute();
        }
        catch (Exception ignored)
        {
        }
    }

    static void update_group_in_db_topic(final String group_identifier, final String topic)
    {
        try
        {
            orma.updateGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).
                    topic(topic).
                    execute();
        }
        catch (Exception ignored)
        {
        }
    }

    static void update_group_in_db_privacy_state(final String group_identifier, final int a_TOX_GROUP_PRIVACY_STATE)
    {
        try
        {
            orma.updateGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).
                    privacy_state(a_TOX_GROUP_PRIVACY_STATE).
                    execute();
        }
        catch (Exception ignored)
        {
        }
    }

    static void delete_group_all_messages(final String group_identifier)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "group_conference_all_messages:del");
                    orma.deleteFromGroupMessage().group_identifierEq(group_identifier.toLowerCase()).execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "group_conference_all_messages:EE:" + e.getMessage());
                }
            }
        };
        t.start();
    }

    static void delete_group(final String group_identifier)
    {
        try
        {
            Log.i(TAG, "delete_group:del");
            orma.deleteFromGroupDB().group_identifierEq(group_identifier.toLowerCase()).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_group:EE:" + e.getMessage());
        }
    }

    static void update_group_in_friendlist(final String group_identifier)
    {
        try
        {
            final GroupDB conf3 = orma.selectFromGroupDB().
                    group_identifierEq(group_identifier.toLowerCase()).toList().get(0);

            CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
            cc.is_friend = COMBINED_IS_GROUP;
            cc.group_item = GroupDB.deep_copy(conf3);
            // TODO: sometimes friend_list_fragment == NULL here!
            //       because its not yet resumed yet
            MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
        }
        catch (Exception e1)
        {
            // Log.i(TAG, "update_group_in_friendlist:EE1:" + e1.getMessage());
            // e1.printStackTrace();
        }
    }

    static void update_group_in_groupmessagelist(final String group_identifier)
    {
        try
        {
            if (group_message_list_activity != null)
            {
                if (group_identifier != null)
                {
                    if (group_message_list_activity.get_current_group_id().toLowerCase().equals(
                            group_identifier.toLowerCase()))
                    {
                        group_message_list_activity.update_group_all_users();
                    }
                }
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "update_group_in_groupmessagelist:EE1:" + e1.getMessage());
            e1.printStackTrace();
        }
    }

    static void add_system_message_to_group_chat(final String group_identifier, final String system_message)
    {
        GroupMessage m = new GroupMessage();
        m.is_new = false;
        m.tox_group_peer_pubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_group_peername = "System";
        m.private_message = 0;
        m.group_identifier = group_identifier.toLowerCase();
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = system_message;
        m.message_id_tox = "";
        m.was_synced = false;

        if (group_message_list_activity != null)
        {
            if (group_message_list_activity.get_current_group_id().equals(group_identifier.toLowerCase()))
            {
                HelperGroup.insert_into_group_message_db(m, true);
            }
            else
            {
                HelperGroup.insert_into_group_message_db(m, false);
            }
        }
        else
        {
            long new_msg_id = HelperGroup.insert_into_group_message_db(m, false);
        }
    }

    static void android_tox_callback_group_message_cb_method_wrapper(long group_number, long peer_id, int a_TOX_MESSAGE_TYPE, String message_orig, long length, long message_id, boolean is_private_message)
    {
        // Log.i(TAG, "android_tox_callback_group_message_cb_method_wrapper:gn=" + group_number + " peerid=" + peer_id +
        //           " message=" + message_orig + " is_private_message=" + is_private_message);

        long res = tox_group_self_get_peer_id(group_number);
        if (res == peer_id)
        {
            // HINT: do not add our own messages, they are already in the DB!
            Log.i(TAG, "group_message_cb:gn=" + group_number + " peerid=" + peer_id + " ignoring own message");
            return;
        }

        // TODO: add message ID later --------
        String message_ = "";
        String message_id_ = "";
        message_ = message_orig;
        message_id_ = "";
        // TODO: add message ID later --------

        if (!is_private_message)
        {
            message_id_ = fourbytes_of_long_to_hex(message_id);
        }

        boolean do_notification = true;
        boolean do_badge_update = true;
        String group_id = "-1";
        GroupDB group_temp = null;

        try
        {
            group_id = tox_group_by_groupnum__wrapper(group_number);
            group_temp = orma.selectFromGroupDB().
                    group_identifierEq(group_id.toLowerCase()).
                    toList().get(0);
        }
        catch (Exception e)
        {
        }

        if (group_id.compareTo("-1") == 0)
        {
            display_toast("ERROR 001 with incoming Group Message!", true, 0);
            return;
        }

        if (group_temp.group_identifier.toLowerCase().compareTo(group_id.toLowerCase()) != 0)
        {
            display_toast("ERROR 002 with incoming Group Message!", true, 0);
            return;
        }

        try
        {
            if (group_temp.notification_silent)
            {
                do_notification = false;
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            do_notification = false;
        }


        if (group_message_list_activity != null)
        {
            Log.i(TAG,
                  "noti_and_badge:002group:" + group_message_list_activity.get_current_group_id() + ":" + group_id);
            if (group_message_list_activity.get_current_group_id().equals(group_id))
            {
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }
        }

        GroupMessage m = new GroupMessage();
        m.is_new = do_badge_update;
        // m.tox_friendnum = friend_number;
        m.tox_group_peer_pubkey = HelperGroup.tox_group_peer_get_public_key__wrapper(group_number, peer_id);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_group_peername = null;
        if (is_private_message)
        {
            m.private_message = 1;
        }
        else
        {
            m.private_message = 0;
        }
        m.group_identifier = group_id.toLowerCase();
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = message_;
        m.message_id_tox = message_id_;
        m.was_synced = false;
        // Log.i(TAG, "message_id_tox=" + message_id_ + " message_id=" + message_id);

        try
        {
            m.tox_group_peername = HelperGroup.tox_group_peer_get_name__wrapper(m.group_identifier,
                                                                                m.tox_group_peer_pubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (group_message_list_activity != null)
        {
            if (group_message_list_activity.get_current_group_id().equals(group_id.toLowerCase()))
            {
                HelperGroup.insert_into_group_message_db(m, true);
            }
            else
            {
                HelperGroup.insert_into_group_message_db(m, false);
            }
        }
        else
        {
            long new_msg_id = HelperGroup.insert_into_group_message_db(m, false);
            Log.i(TAG, "group_message_cb:new_msg_id=" + new_msg_id);
        }

        HelperFriend.add_all_friends_clear_wrapper(0);

        if (do_notification)
        {
            change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.group_identifier);
        }
    }

    static GroupMessage get_last_group_message_in_this_group_within_n_seconds_from_sender_pubkey(String group_identifier, String sender_pubkey, long sent_timestamp, String message_id_tox, int n, boolean was_synced, final String message_text)
    {
        try
        {
            if ((message_id_tox == null) || (message_id_tox.length() < 8))
            {
                return null;
            }

            final int SECONDS_FOR_DOUBLE_MESSAGES_INTERVAL = 30; // 30 sec

            GroupMessage gm = orma.selectFromGroupMessage().
                    group_identifierEq(group_identifier.toLowerCase()).
                    tox_group_peer_pubkeyEq(sender_pubkey.toUpperCase()).
                    message_id_toxEq(message_id_tox.toLowerCase()).
                    sent_timestampGt(sent_timestamp - (SECONDS_FOR_DOUBLE_MESSAGES_INTERVAL * 1000)).
                    sent_timestampLt(sent_timestamp + (SECONDS_FOR_DOUBLE_MESSAGES_INTERVAL * 1000)).
                    textEq(message_text).
                    limit(1).
                    toList().
                    get(0);

            return gm;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static void group_message_add_from_sync(final String group_identifier, long peer_number2, String peer_pubkey, int a_TOX_MESSAGE_TYPE, String message, long length, long sent_timestamp_in_ms, String message_id)
    {
        // Log.i(TAG,
        //       "group_message_add_from_sync:cf_num=" + group_identifier + " pnum=" + peer_number2 + " msg=" + message);

        int res = -1;
        if (peer_number2 == -1)
        {
            res = -1;
        }
        else
        {
            long group_num_ = tox_group_by_groupid__wrapper(group_identifier);
            final long my_peer_num = tox_group_self_get_peer_id(group_num_);
            if (my_peer_num == peer_number2)
            {
                res = 1;
            }
            else
            {
                res = 0;
            }
        }

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            // Log.i(TAG, "conference_message_add_from_sync:own peer");
            return;
        }

        boolean do_notification = true;
        boolean do_badge_update = true;
        GroupDB group_temp = null;

        try
        {
            // TODO: cache me!!
            group_temp = orma.selectFromGroupDB().
                    group_identifierEq(group_identifier).get(0);
        }
        catch (Exception e)
        {
        }

        if (group_temp == null)
        {
            Log.i(TAG, "group_message_add_from_sync:cf_num=" + group_identifier + " pnum=" + peer_number2 + " msg=" +
                       message + " we dont have the group anymore????");
            return;
        }

        try
        {
            if (group_temp.notification_silent)
            {
                do_notification = false;
            }
        }
        catch (Exception e)
        {
        }

        if (group_message_list_activity != null)
        {
            // Log.i(TAG, "conference_message_add_from_sync:noti_and_badge:002conf:" +
            //            conference_message_list_activity.get_current_conf_id() + ":" + conf_id);

            if (group_message_list_activity.get_current_group_id().equals(group_identifier))
            {
                // Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }
        }

        GroupMessage m = new GroupMessage();
        m.is_new = do_badge_update;
        m.tox_group_peer_pubkey = peer_pubkey;
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_group_peername = null;
        m.group_identifier = group_identifier;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.sent_timestamp = sent_timestamp_in_ms;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = message;
        m.message_id_tox = message_id;
        m.was_synced = true;

        try
        {
            m.tox_group_peername = tox_group_peer_get_name__wrapper(m.group_identifier, m.tox_group_peer_pubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (group_message_list_activity != null)
        {
            if (group_message_list_activity.get_current_group_id().equals(group_identifier))
            {
                insert_into_group_message_db(m, true);
            }
            else
            {
                insert_into_group_message_db(m, false);
            }
        }
        else
        {
            long new_msg_id = insert_into_group_message_db(m, false);
            // Log.i(TAG, "conference_message_add_from_sync:new_msg_id=" + new_msg_id);
        }

        HelperFriend.add_all_friends_clear_wrapper(0);

        if (do_notification)
        {
            change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.group_identifier);
        }
    }

    static void send_group_image(final GroupMessage g)
    {
        // @formatter:off
        /*
           40000 max bytes length for custom lossless NGC packets.
           37000 max bytes length for file and header, to leave some space for offline message syncing.

        | what      | Length in bytes| Contents                                           |
        |------     |--------        |------------------                                  |
        | magic     |       6        |  0x667788113435                                    |
        | version   |       1        |  0x01                                              |
        | pkt id    |       1        |  0x11                                              |
        | msg id    |      32        | *uint8_t  to uniquely identify the message         |
        | create ts |       4        |  uint32_t unixtimestamp in UTC of local wall clock |
        | filename  |     255        |  len TOX_MAX_FILENAME_LENGTH                       |
        |           |                |      data first, then pad with NULL bytes          |
        | data      |[1, 1334]       |  bytes of file data, zero length files not allowed!|


        header size: 299 bytes
        data   size: 1 - 36701 bytes
         */
        // @formatter:on

        final long header = 6 + 1 + 1 + 32 + 4 + 255;
        long data_length = header + g.filesize;

        if ((data_length > 37000L) || (data_length < (header + 1)))
        {
            Log.i(TAG, "send_group_image: data length has wrong size: " + data_length);
            return;
        }

        ByteBuffer data_buf = ByteBuffer.allocateDirect((int)data_length);

        data_buf.rewind();
        //
        data_buf.put((byte)0x66);
        data_buf.put((byte)0x77);
        data_buf.put((byte)0x88);
        data_buf.put((byte)0x11);
        data_buf.put((byte)0x34);
        data_buf.put((byte)0x35);
        //
        data_buf.put((byte)0x01);
        //
        data_buf.put((byte)0x11);
        //
        data_buf.put(HelperGeneric.hex_to_bytes(g.msg_id_hash));
        //
        // TODO: write actual timestamp into buffer
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        //
        byte[] fn = "image.jpg".getBytes(StandardCharsets.UTF_8);
        data_buf.put(fn);
        for (int k=0;k<(255 - fn.length);k++)
        {
            // fill with null bytes up to 255 for the filename
            data_buf.put((byte) 0x0);
        }
        // -- now fill the data from file --
        java.io.File img_file = new java.io.File(g.filename_fullpath);




        long length_sum = 0;
        java.io.FileInputStream is = null;
        try
        {
            is = new java.io.FileInputStream(img_file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0)
            {
                data_buf.put(buffer, 0, length);
                length_sum = length_sum + length;
                Log.i(TAG,"put " + length + " bytes into buffer");
            }
        }
        catch(Exception e)
        {
        }
        finally
        {
            try
            {
                is.close();
            }
            catch(Exception e2)
            {
            }
        }
        Log.i(TAG,"put " + length_sum + " bytes TOTAL into buffer, and should match " + g.filesize);
        // -- now fill the data from file --

        byte[] data = new byte[(int)data_length];
        data_buf.rewind();
        data_buf.get(data);
        tox_group_send_custom_packet(tox_group_by_groupid__wrapper(g.group_identifier),
                                     1,
                                     data,
                                     (int)data_length);
    }

    static void do_join_public_group(Intent data)
    {
        try
        {
            String group_id = data.getStringExtra("group_id");
            Log.i(TAG, "join_group:group_id:>" + group_id + "<");

            ByteBuffer join_chat_id_buffer = ByteBuffer.allocateDirect(TOX_GROUP_CHAT_ID_SIZE);
            byte[] data_join = HelperGeneric.hex_to_bytes(group_id.toUpperCase());
            join_chat_id_buffer.put(data_join);
            join_chat_id_buffer.rewind();

            long new_group_num = MainActivity.tox_group_join(join_chat_id_buffer, TOX_GROUP_CHAT_ID_SIZE,
                                                             "peer " + MainActivity.getRandomString(4), null);

            Log.i(TAG, "join_group:new groupnum:=" + new_group_num);

            if ((new_group_num >= 0) && (new_group_num < UINT32_MAX_JAVA))
            {
                ByteBuffer groupid_buf = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);
                if (tox_group_get_chat_id(new_group_num, groupid_buf) == 0)
                {
                    byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                    groupid_buf.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                    String group_identifier = bytes_to_hex(groupid_buffer);

                    int privacy_state = MainActivity.tox_group_get_privacy_state(new_group_num);

                    Log.i(TAG, "join_group:group num=" + new_group_num + " privacy_state=" + privacy_state +
                                            " group_id=" + group_identifier + " offset=" + groupid_buf.arrayOffset());

                    add_group_wrapper(0, new_group_num, group_identifier, privacy_state);

                    display_toast(MainActivity.context_s.getString(R.string.join_public_group_joined), false, 300);
                    set_group_active(group_identifier);
                    try
                    {
                        final GroupDB conf3 = orma.selectFromGroupDB().group_identifierEq(
                                group_identifier.toLowerCase()).toList().get(0);
                        CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                        cc.is_friend = COMBINED_IS_CONFERENCE;
                        cc.group_item = GroupDB.deep_copy(conf3);
                        MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                    }
                    catch (Exception e3)
                    {
                        // e3.printStackTrace();
                    }
                }
            }
            else
            {
                display_toast(MainActivity.context_s.getString(R.string.join_public_group_failed), false, 300);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "join_group:EE01:" + e.getMessage());
        }
    }
}