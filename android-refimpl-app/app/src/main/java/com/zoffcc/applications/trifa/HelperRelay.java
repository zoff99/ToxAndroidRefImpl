/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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

import java.util.List;

import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.del_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_invite;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_FCM_PUSH_URL_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_TOKEN_DB_KEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_UP_PUSH_URL_PREFIX;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperRelay
{
    private static final String TAG = "trifa.Hlp.Relay";

    static void add_or_update_friend_relay(String relay_public_key_string, String friend_pubkey)
    {
        if (relay_public_key_string == null)
        {
            // Log.d(TAG, "add_or_update_friend_relay:ret01");
            return;
        }

        if (friend_pubkey == null)
        {
            // Log.d(TAG, "add_or_update_friend_relay:ret02");
            return;
        }

        try
        {
            if (!is_any_relay(friend_pubkey))
            {
                String friend_old_relay_pubkey = get_relay_for_friend(friend_pubkey);

                if (friend_old_relay_pubkey != null)
                {
                    // delete old relay
                    delete_friend_current_relay(friend_pubkey);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!is_any_relay(friend_pubkey))
            {
                FriendList fl = HelperFriend.main_get_friend(tox_friend_by_public_key__wrapper(friend_pubkey));

                if (fl != null)
                {
                    // add relay to DB table
                    RelayListDB new_relay = new RelayListDB();
                    new_relay.own_relay = false;
                    new_relay.TOX_CONNECTION = fl.TOX_CONNECTION;
                    new_relay.TOX_CONNECTION_on_off = fl.TOX_CONNECTION_on_off;
                    new_relay.last_online_timestamp = fl.last_online_timestamp;
                    new_relay.tox_public_key_string = relay_public_key_string.toUpperCase();
                    new_relay.tox_public_key_string_of_owner = friend_pubkey;

                    //
                    try
                    {
                        orma.insertIntoRelayListDB(new_relay);
                        // Log.i(TAG, "add_or_update_friend_relay:+ADD friend relay+ owner pubkey=" + friend_pubkey);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    // friend exists -> update
                    try
                    {
                        orma.updateFriendList().
                                tox_public_key_stringEq(relay_public_key_string).
                                is_relay(true).
                                execute();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_current_relay(String friend_pubkey)
    {
        try
        {
            String friend_current_relay_pubkey = get_relay_for_friend(friend_pubkey);

            if (friend_current_relay_pubkey != null)
            {
                try
                {
                    orma.updateFriendList().
                            tox_public_key_stringEq(friend_current_relay_pubkey).
                            is_relay(false).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }

                try
                {
                    orma.deleteFromRelayListDB().tox_public_key_string_of_ownerEq(friend_pubkey).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void send_relay_pubkey_to_all_friends(String relay_public_key_string)
    {
        List<FriendList> fl = orma.selectFromFriendList().
                is_relayNotEq(true).
                toList();

        if (fl != null)
        {
            if (fl.size() > 0)
            {
                long friend_num = -1;
                int i = 0;

                for (i = 0; i < fl.size(); i++)
                {
                    FriendList n = fl.get(i);
                    friend_num = tox_friend_by_public_key__wrapper(n.tox_public_key_string);
                    byte[] data = HelperGeneric.hex_to_bytes("FF" + relay_public_key_string);
                    data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value;
                    MainActivity.tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
                }
            }
        }
    }

    static void send_all_friend_pubkeys_to_relay(String relay_public_key_string)
    {
        List<FriendList> fl = orma.selectFromFriendList().
                is_relayNotEq(true).
                toList();

        if (fl != null)
        {
            if (fl.size() > 0)
            {
                int i = 0;
                long friend_num = tox_friend_by_public_key__wrapper(relay_public_key_string);

                for (i = 0; i < fl.size(); i++)
                {
                    FriendList n = fl.get(i);
                    byte[] data = HelperGeneric.hex_to_bytes("FF" + n.tox_public_key_string);
                    data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY.value;
                    MainActivity.tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
                }
            }
        }
    }

    static void invite_to_all_conferences_own_relay(String relay_public_key_string)
    {
        try
        {
            List<ConferenceDB> c = orma.selectFromConferenceDB().
                    conference_activeEq(true).and().
                    tox_conference_numberNotEq(-1).
                    toList();

            if (c != null)
            {
                if (c.size() > 0)
                {
                    for (int i = 0; i < c.size(); i++)
                    {
                        ConferenceDB conf = c.get(i);
                        int res = tox_conference_invite(tox_friend_by_public_key__wrapper(relay_public_key_string),
                                                        conf.tox_conference_number);

                        // Log.i(TAG,
                        //       "invite_to_all_conferences_own_relay:confnum=" + conf.tox_conference_number + " res=" +
                        //       res);

                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static int invite_to_conference_own_relay(long conference_num)
    {
        return tox_conference_invite(tox_friend_by_public_key__wrapper(HelperRelay.get_own_relay_pubkey()),
                                     conference_num);
    }

    static boolean is_any_relay(String friend_pubkey)
    {
        boolean ret = false;
        int num = orma.selectFromFriendList().
                tox_public_key_stringEq(friend_pubkey).
                is_relayEq(true).
                count();

        if (num > 0)
        {
            ret = true;
        }

        return ret;
    }

    static void send_friend_pubkey_to_relay(String relay_public_key_string, String friend_pubkey)
    {
        int i = 0;
        long friend_num = tox_friend_by_public_key__wrapper(relay_public_key_string);
        byte[] data = HelperGeneric.hex_to_bytes("FF" + friend_pubkey);
        data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY.value;
        // Log.d(TAG, "send_friend_pubkey_to_relay:data=" + data);
        int result = MainActivity.tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
        // Log.d(TAG, "send_friend_pubkey_to_relay:res=" + result);
    }

    static void send_relay_pubkey_to_friend(String relay_public_key_string, String friend_pubkey)
    {
        int i = 0;
        long friend_num = tox_friend_by_public_key__wrapper(friend_pubkey);
        byte[] data = HelperGeneric.hex_to_bytes("FF" + relay_public_key_string);
        data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value;
        // Log.d(TAG, "send_relay_pubkey_to_friend:data=" + data);
        int result = MainActivity.tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
        // Log.d(TAG, "send_relay_pubkey_to_friend:res=" + result);
    }

    static boolean have_own_relay()
    {
        boolean ret = false;
        int num = orma.selectFromRelayListDB().own_relayEq(true).count();

        if (num == 1)
        {
            ret = true;
        }

        return ret;
    }

    static void own_push_token_load()
    {
        if (TRIFAGlobals.global_notification_token == null)
        {
            if (get_g_opts(NOTIFICATION_TOKEN_DB_KEY) != null)
            {
                TRIFAGlobals.global_notification_token = get_g_opts(NOTIFICATION_TOKEN_DB_KEY);
            }
        }
    }

    static String push_token_to_push_url(final String push_token)
    {
        if (push_token != null)
        {
            // I dont have a relay, but i have a PUSH token
            String notification_push_url = push_token;
            if (push_token.startsWith("https://"))
            {
                // this must be a gotify/unifiedpush token
            }
            else
            {
                // this must be a google FCM token, add the porper HTTPS url here
                notification_push_url = NOTIFICATION_FCM_PUSH_URL_PREFIX + push_token;
            }

            if (notification_push_url.length() < 1000)
            {
                return notification_push_url;
            }
        }

        return null;
    }

    static boolean have_own_pushurl()
    {
        try
        {
            if (get_g_opts(NOTIFICATION_TOKEN_DB_KEY) != null)
            {
                final String tmp = get_g_opts(NOTIFICATION_TOKEN_DB_KEY);
                if (tmp.length() > 5)
                {
                    return true;
                }
            }
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    static FriendList get_friend_for_relay(String relay_pubkey)
    {
        FriendList ret = null;

        try
        {
            String f_pubkey = orma.selectFromRelayListDB().own_relayEq(false).
                    tox_public_key_stringEq(relay_pubkey).get(0).tox_public_key_string_of_owner;
            ret = orma.selectFromFriendList().tox_public_key_stringEq(f_pubkey).get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    static boolean is_own_relay(String friend_pubkey)
    {
        boolean ret = false;

        try
        {
            String own_relay_pubkey = get_own_relay_pubkey();

            if (own_relay_pubkey != null)
            {
                if (friend_pubkey.equals(own_relay_pubkey) == true)
                {
                    ret = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static String get_own_relay_pubkey()
    {
        String ret = null;

        try
        {
            ret = orma.selectFromRelayListDB().own_relayEq(true).get(0).tox_public_key_string;
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static int get_own_relay_connection_status_real()
    {
        int ret = 0;

        try
        {
            return is_friend_online_real(tox_friend_by_public_key__wrapper(get_own_relay_pubkey()));
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static String get_relay_for_friend(String friend_pubkey)
    {
        String ret = null;

        try
        {
            ret = orma.selectFromRelayListDB().own_relayEq(false).
                    tox_public_key_string_of_ownerEq(friend_pubkey).get(0).tox_public_key_string;
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static String get_pushurl_for_friend(String friend_pubkey)
    {
        String ret = null;

        try
        {
            ret = orma.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).get(0).push_url;
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static boolean is_valid_pushurl_for_friend_with_whitelist(String push_url)
    {
        // whitelist google FCM gateway
        if (push_url.length() > NOTIFICATION_FCM_PUSH_URL_PREFIX.length())
        {
            if (push_url.startsWith(NOTIFICATION_FCM_PUSH_URL_PREFIX))
            {
                return true;
            }
        }

        // whitelist OLD google FCM gateway
        if (push_url.length() > NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD.length())
        {
            if (push_url.startsWith(NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD))
            {
                return true;
            }
        }

        // whitelist unified push demo server
        if (push_url.length() > NOTIFICATION_UP_PUSH_URL_PREFIX.length())
        {
            if (push_url.startsWith(NOTIFICATION_UP_PUSH_URL_PREFIX))
            {
                return true;
            }
        }

        // anything else is not allowed at this time!
        return false;
    }

    static boolean set_friend_as_own_relay_in_db(String friend_public_key)
    {
        boolean ret = false;

        try
        {
            final List<FriendList> fl = orma.selectFromFriendList().
                    tox_public_key_stringEq(friend_public_key).toList();

            if (fl.size() == 1)
            {
                // add relay to DB table
                RelayListDB new_relay = new RelayListDB();
                new_relay.own_relay = true;
                new_relay.TOX_CONNECTION = fl.get(0).TOX_CONNECTION;
                new_relay.TOX_CONNECTION_on_off = fl.get(0).TOX_CONNECTION_on_off;
                new_relay.last_online_timestamp = fl.get(0).last_online_timestamp;
                new_relay.tox_public_key_string = friend_public_key;
                new_relay.tox_public_key_string_of_owner = "-- OWN RELAY --";
                //
                orma.insertIntoRelayListDB(new_relay);
                // Log.i(TAG, "friend_as_relay_own_in_db:+ADD own relay+");
                // friend exists -> update
                orma.updateFriendList().
                        tox_public_key_stringEq(friend_public_key).
                        is_relay(true).
                        execute();
                // Log.i(TAG, "friend_as_relay_own_in_db:+UPDATE friend+");
                ret = true;
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "friend_as_relay_own_in_db:EE3:" + e1.getMessage());
        }

        return ret;
    }

    static boolean remove_friend_relay_in_db(String friend_pubkey)
    {
        boolean ret = false;

        try
        {
            String friend_relay_pubkey = get_relay_for_friend(friend_pubkey);

            if (friend_relay_pubkey != null)
            {
                try
                {
                    orma.updateFriendList().tox_public_key_stringEq(friend_relay_pubkey).
                            is_relay(false).execute();
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                }

                try
                {
                    orma.deleteFromRelayListDB().tox_public_key_string_of_ownerEq(friend_pubkey).
                            execute();
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                }
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "remove_friend_relay_in_db:EE3:" + e1.getMessage());
        }

        return ret;
    }

    static void remove_friend_pushurl_in_db(String friend_pubkey)
    {
        try
        {
            orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                    push_url(null).execute();
        }
        catch (Exception e1)
        {
            Log.i(TAG, "remove_friend_pushurl_in_db:EE3:" + e1.getMessage());
        }
    }

    static boolean remove_own_relay_in_db()
    {
        boolean ret = false;

        try
        {
            final List<RelayListDB> rl = orma.selectFromRelayListDB().
                    own_relayEq(true).toList();

            if (rl.size() == 1)
            {
                orma.deleteFromRelayListDB().
                        tox_public_key_stringEq(rl.get(0).tox_public_key_string).execute();
                // friend exists -> update
                orma.updateFriendList().
                        tox_public_key_stringEq(rl.get(0).tox_public_key_string).
                        is_relay(false).
                        execute();
                // Log.i(TAG, "remove_own_relay_in_db:+UPDATE friend+");
                ret = true;
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "remove_own_relay_in_db:EE3:" + e1.getMessage());
        }

        return ret;
    }

    static void remove_own_pushurl_in_db()
    {
        del_g_opts(NOTIFICATION_TOKEN_DB_KEY);
    }
}
