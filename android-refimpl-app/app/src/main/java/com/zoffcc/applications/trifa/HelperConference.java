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

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_AV;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperConference
{
    private static final String TAG = "trifa.Hlp.Conf";

    static void copy_selected_conference_messages(Context c)
    {
        try
        {
            if (!MainActivity.selected_conference_messages.isEmpty())
            {
                // sort ascending (lowest ID on top)
                Collections.sort(MainActivity.selected_conference_messages, new Comparator<Long>()
                {
                    public int compare(Long o1, Long o2)
                    {
                        return o1.compareTo(o2);
                    }
                });
                StringBuilder copy_text = new StringBuilder();
                boolean first = true;
                Iterator i = MainActivity.selected_conference_messages.iterator();

                while (i.hasNext())
                {
                    try
                    {
                        if (first)
                        {
                            first = false;
                            copy_text = new StringBuilder(
                                "" + orma.selectFromConferenceMessage().idEq((Long) i.next()).get(0).text);
                        }
                        else
                        {
                            copy_text.append("\n").append(
                                orma.selectFromConferenceMessage().idEq((Long) i.next()).get(0).text);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                MainActivity.clipboard.setPrimaryClip(ClipData.newPlainText("", copy_text.toString()));
                Toast.makeText(c, "copied to Clipboard", Toast.LENGTH_SHORT).show();
                MainActivity.selected_conference_messages.clear();

                try
                {
                    // need to redraw all items again here, to remove the selections
                    MainActivity.conference_message_list_fragment.adapter.redraw_all_items();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    static void delete_selected_conference_messages(Context c, final boolean update_conf_message_list, final String dialog_text)
    {
        // TODO: write me!
        ProgressDialog progressDialog2 = null;

        try
        {
            new MainActivity.delete_selected_conference_messages_asynchtask(c, progressDialog2,
                                                                            update_conf_message_list,
                                                                            dialog_text).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_selected_conference_messages:EE2:" + e.getMessage());
        }
    }

    static void add_conference_wrapper(final long friend_number, long conference_num, String conference_identifier_in, final int a_TOX_CONFERENCE_TYPE, boolean has_conference_identifier)
    {
        if (conference_num < 0)
        {
            Log.d(TAG, "add_conference_wrapper:ERR:conference number less than zero:" + conference_num);
            return;
        }

        //Log.d(TAG, "add_conference_wrapper:confnum=" + conference_num + " conference_identifier_in=" +
        //           conference_identifier_in);
        String conference_identifier = conference_identifier_in;

        if (has_conference_identifier != true)
        {
            //Log.d(TAG, "add_conference_wrapper:need to get conference_identifier");
            // we need to get the conference identifier
            ByteBuffer cookie_buf3 = ByteBuffer.allocateDirect((int) CONFERENCE_ID_LENGTH * 2);
            cookie_buf3.clear();
            if (MainActivity.tox_conference_get_id(conference_num, cookie_buf3) == 0)
            {
                byte[] cookie_buffer = new byte[CONFERENCE_ID_LENGTH];
                cookie_buf3.get(cookie_buffer, 0, CONFERENCE_ID_LENGTH);
                conference_identifier = MainActivity.bytes_to_hex(cookie_buffer);
            }
            else
            {
                Log.d(TAG, "add_conference_wrapper:ERR:error getting conference identifier");
                return;
            }
        }

        //Log.d(TAG, "add_conference_wrapper:conference_identifier=" + conference_identifier);

        if (conference_num >= 0)
        {
            new_or_updated_conference(conference_num, HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                      conference_identifier, a_TOX_CONFERENCE_TYPE); // joining new conference
        }
        else
        {
            //Log.i(TAG, "add_conference_wrapper:error=" + conference_num + " joining conference");
        }

        try
        {
            if (MainActivity.conference_message_list_activity != null)
            {
                if (MainActivity.conference_message_list_activity.get_current_conf_id().equals(conference_identifier))
                {
                    MainActivity.conference_message_list_activity.set_conference_connection_status_icon();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // save tox savedate file
        MainActivity.update_savedata_file_wrapper();
    }

    static boolean is_conference_active(String conference_identifier)
    {
        try
        {
            return (orma.selectFromConferenceDB().
                conference_identifierEq(conference_identifier).
                toList().get(0).conference_active);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static String tox_conference_peer_get_public_key__wrapper(long conference_number, long peer_number)
    {
        if (MainActivity.cache_peernum_pubkey.containsKey("" + conference_number + ":" + peer_number))
        {
            // Log.i(TAG, "cache hit:2");
            return MainActivity.cache_peernum_pubkey.get("" + conference_number + ":" + peer_number);
        }
        else
        {
            if (MainActivity.cache_peernum_pubkey.size() >= 100)
            {
                // TODO: bad!
                MainActivity.cache_peernum_pubkey.clear();
            }

            String result = MainActivity.tox_conference_peer_get_public_key(conference_number, peer_number);
            if ((conference_number != -1) && (peer_number != -1))
            {
                MainActivity.cache_peernum_pubkey.put("" + conference_number + ":" + peer_number, result);
            }
            return result;
        }
    }

    public static String tox_conference_peer_get_name__wrapper(String conference_identifier, String peer_pubkey)
    {
        if (MainActivity.cache_peername_pubkey2.containsKey("" + conference_identifier + ":" + peer_pubkey))
        {
            // Log.i(TAG, "cache hit:2b");
            return MainActivity.cache_peername_pubkey2.get("" + conference_identifier + ":" + peer_pubkey);
        }
        else
        {
            if (MainActivity.cache_peername_pubkey2.size() >= 100)
            {
                // TODO: bad!
                MainActivity.cache_peername_pubkey2.clear();
            }

            long conf_num = tox_conference_by_confid__wrapper(conference_identifier);
            long peer_num = get_peernum_from_peer_pubkey(conference_identifier, peer_pubkey);

            if ((conf_num > -1) && (peer_num > -1))
            {
                String result = MainActivity.tox_conference_peer_get_name(conf_num, peer_num);

                if (result.equals("-1"))
                {
                    return null;
                }
                else
                {
                    MainActivity.cache_peername_pubkey2.put("" + conference_identifier + ":" + peer_pubkey, result);
                    return result;
                }
            }
            else
            {
                return null;
            }
        }
    }

    static ConferenceMessage get_last_conference_message_in_this_conference_within_n_seconds(String conference_identifier, int n)
    {
        try
        {
            ConferenceMessage cm = orma.selectFromConferenceMessage().
                conference_identifierEq(conference_identifier.toLowerCase()).
                and().
                rcvd_timestampGt(System.currentTimeMillis() - (n * 1000)).
                orderByRcvd_timestampDesc().
                limit(1).
                toList().
                get(0);
            return cm;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            return null;
        }
    }

    static long insert_into_conference_message_db_system_message(final ConferenceMessage m, final boolean update_conference_view_flag)
    {
        long row_id = orma.insertIntoConferenceMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery(
                "SELECT id FROM ConferenceMessage where rowid='" + row_id + "'");
            cursor.moveToFirst();
            //Log.i(TAG, "insert_into_conference_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_conference_view_flag)
            {
                if (MainActivity.PREF__conference_show_system_messages)
                {
                    HelperMessage.add_single_conference_message_from_messge_id(msg_id, true);
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

    static long insert_into_conference_message_db(final ConferenceMessage m, final boolean update_conference_view_flag)
    {
        long row_id = orma.insertIntoConferenceMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery(
                "SELECT id FROM ConferenceMessage where rowid='" + row_id + "'");
            cursor.moveToFirst();
            //Log.i(TAG, "insert_into_conference_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_conference_view_flag)
            {
                HelperMessage.add_single_conference_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static void delete_conference(final String conference_id)
    {
        try
        {
            Log.i(TAG, "delete_conference:del");
            orma.deleteFromConferenceDB().conference_identifierEq(conference_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_conference:EE:" + e.getMessage());
        }
    }

    static void delete_conference_all_messages(final String conference_id)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "delete_conference_all_messages:del");
                    orma.deleteFromConferenceMessage().conference_identifierEq(conference_id).execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "delete_conference_all_messages:EE:" + e.getMessage());
                }
            }
        };
        t.start();
    }

    static long get_peernum_from_peer_pubkey(String conference_id, String peer_pubkey)
    {
        try
        {
            long conf_num = tox_conference_by_confid__wrapper(conference_id);
            long num_peers = MainActivity.tox_conference_peer_count(conf_num);

            if (num_peers > 0)
            {
                int i = 0;

                for (i = 0; i < num_peers; i++)
                {
                    String pubkey_try = MainActivity.tox_conference_peer_get_public_key(conf_num, i);

                    if (pubkey_try != null)
                    {
                        if (pubkey_try.equals(peer_pubkey))
                        {
                            // we found the peer number
                            return i;
                        }
                    }
                }
            }

            return -1;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    public static long tox_conference_by_confid__wrapper(@NonNull String conference_id_string)
    {
        if (MainActivity.cache_confid_confnum.containsKey(conference_id_string))
        {
            // Log.i(TAG, "cache_hit:1");
            return MainActivity.cache_confid_confnum.get(conference_id_string);
        }
        else
        {
            if (MainActivity.cache_confid_confnum.size() >= 60)
            {
                // TODO: bad!
                MainActivity.cache_confid_confnum.clear();
            }

            long result = get_conference_num_from_confid(conference_id_string);
            if (result != -1)
            {
                MainActivity.cache_confid_confnum.put(conference_id_string, result);
            }
            return result;
        }
    }

    static long get_conference_num_from_confid(String conference_id)
    {
        // HINT: use tox_conference_by_confid__wrapper for a cached method of this
        try
        {
            return orma.selectFromConferenceDB().
                conference_activeEq(true).and().
                conference_identifierEq(conference_id.toLowerCase()).get(0).tox_conference_number;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    static String get_conference_title_from_confid(String conference_id)
    {
        try
        {
            // try in the database
            String name = orma.selectFromConferenceDB().
                conference_identifierEq(conference_id).get(0).name;

            if ((name == null) || (name.equals("-1")))
            {
                Log.i(TAG, "get_conference_title_from_confid:EE:1");
            }
            else
            {
                return name;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_conference_title_from_confid:EE:2:" + e.getMessage());
        }

        try
        {
            String name = MainActivity.tox_conference_get_title(orma.selectFromConferenceDB().
                conference_activeEq(true).and().
                conference_identifierEq(conference_id).get(0).tox_conference_number);

            if ((name == null) || (name.equals("-1")))
            {
                Log.i(TAG, "get_conference_title_from_confid:Unknown Conference:1");
                name = "Unknown Conference";
            }

            try
            {
                // remember it in the Database
                orma.updateConferenceDB().
                    conference_identifierEq(conference_id).
                    name(name).execute();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "get_conference_title_from_confid:EE:3:" + e2.getMessage());
            }

            return name;
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "get_conference_title_from_confid:EE:4:" + e2.getMessage());
        }

        Log.i(TAG, "get_conference_title_from_confid:Unknown Conference:2");
        return "Unknown Conference";
    }

    static int get_conference_kind_from_confid(String conference_id)
    {
        try
        {
            // try in the database
            int kind = orma.selectFromConferenceDB().
                conference_identifierEq(conference_id).get(0).kind;

            if ((kind < TOX_CONFERENCE_TYPE_TEXT.value) || (kind > TOX_CONFERENCE_TYPE_AV.value))
            {
                Log.i(TAG, "get_conference_kind_from_confid:EE:1");
                return TOX_CONFERENCE_TYPE_TEXT.value;
            }
            else
            {
                return kind;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_conference_kind_from_confid:EE:2:" + e.getMessage());
        }

        return TOX_CONFERENCE_TYPE_TEXT.value;
    }

    static String conference_identifier_short(String conference_identifier, boolean uppercase_result)
    {
        try
        {
            //            return conference_identifier.substring(0, 2) +
            //                    //
            //                    ".." +
            //                    //
            //                    conference_identifier.substring(conference_identifier.length() - 5, conference_identifier.length());
            if (uppercase_result)
            {
                return (conference_identifier.substring(conference_identifier.length() - 6,
                                                        conference_identifier.length())).toUpperCase(Locale.ENGLISH);
            }
            else
            {
                return conference_identifier.substring(conference_identifier.length() - 6,
                                                       conference_identifier.length());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return conference_identifier;
        }
    }

    static void set_all_conferences_inactive()
    {
        try
        {
            orma.updateConferenceDB().
                conference_active(false).
                tox_conference_number(-1).
                execute();
            Log.i(TAG, "set_all_conferences_inactive");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_all_conferences_inactive:EE:" + e.getMessage());
        }
    }

    static void set_conference_inactive(String conference_identifier)
    {
        try
        {
            orma.updateConferenceDB().
                conference_identifierEq(conference_identifier).
                conference_active(false).
                tox_conference_number(-1).
                execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_conference_inactive:EE:" + e.getMessage());
        }
    }

    static void new_or_updated_conference(long conference_number, String who_invited_public_key, String conference_identifier, int conference_type)
    {
        try
        {
            // Log.i(TAG, "new_or_updated_conference:" + "conference_number=" + conference_identifier);
            final ConferenceDB conf2 = orma.selectFromConferenceDB().
                conference_identifierEq(conference_identifier).toList().get(0);
            // conference already exists -> update and connect
            orma.updateConferenceDB().
                conference_identifierEq(conference_identifier).
                conference_active(true).
                kind(conference_type).
                tox_conference_number(conference_number).execute();

            try
            {
                Log.i(TAG, "new_or_updated_conference:*update*");
                final ConferenceDB conf3 = orma.selectFromConferenceDB().
                    conference_identifierEq(conference_identifier).toList().get(0);
                // update or add to "friendlist"
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = false;
                cc.conference_item = ConferenceDB.deep_copy(conf3);
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_conference:EE3:" + e3.getMessage());
            }

            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_or_updated_conference:EE1:" + e.getMessage());

            // conference is new -> add
            try
            {
                ConferenceDB conf_new = new ConferenceDB();
                conf_new.conference_identifier = conference_identifier;
                conf_new.who_invited__tox_public_key_string = who_invited_public_key;
                conf_new.peer_count = -1;
                conf_new.own_peer_number = -1;
                conf_new.kind = conference_type;
                conf_new.tox_conference_number = conference_number;
                conf_new.conference_active = true;
                //
                orma.insertIntoConferenceDB(conf_new);
                Log.i(TAG, "new_or_updated_conference:+ADD+");

                try
                {
                    // update or add to "friendlist"
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = ConferenceDB.deep_copy(conf_new);
                    MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
                catch (Exception e4)
                {
                    Log.i(TAG, "new_or_updated_conference:EE4:" + e4.getMessage());
                }

                return;
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Log.i(TAG, "new_or_updated_conference:EE2:" + e1.getMessage());
            }
        }
    }

    static void update_single_conference_in_friendlist_view(final ConferenceDB conf)
    {
        if (MainActivity.friend_list_fragment != null)
        {
            if (conf != null)
            {
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = false;
                cc.conference_item = conf;
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
        }
    }
}
