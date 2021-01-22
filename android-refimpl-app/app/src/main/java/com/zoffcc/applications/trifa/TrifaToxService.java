/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2019 Zoff <zoff@zoff.cc>
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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import info.guardianproject.iocipher.VirtualFileSystem;

import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.get_tcprelay_nodelist_from_db;
import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.get_udp_nodelist_from_db;
import static com.zoffcc.applications.trifa.HelperConference.new_or_updated_conference;
import static com.zoffcc.applications.trifa.HelperConference.set_all_conferences_inactive;
import static com.zoffcc.applications.trifa.HelperFriend.add_friend_real;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online;
import static com.zoffcc.applications.trifa.HelperFriend.set_all_friends_offline;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.battery_saving_can_sleep;
import static com.zoffcc.applications.trifa.HelperGeneric.bootstrap_single_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.get_combined_connection_status;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.get_toxconnection_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.hex_to_bytes;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format_or_empty;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.vfs__detach;
import static com.zoffcc.applications.trifa.HelperGeneric.vfs__unmount;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_cancel;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_change;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_change_wrapper;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_setup;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_timeout;
import static com.zoffcc.applications.trifa.MainActivity.PREF__force_udp_only;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.cache_confid_confnum;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.conference_audio_activity;
import static com.zoffcc.applications.trifa.MainActivity.conference_message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.get_my_toxid;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.receiver1;
import static com.zoffcc.applications.trifa.MainActivity.receiver2;
import static com.zoffcc.applications.trifa.MainActivity.receiver3;
import static com.zoffcc.applications.trifa.MainActivity.receiver4;
import static com.zoffcc.applications.trifa.MainActivity.set_filteraudio_active;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_type;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_iteration_interval;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_service_fg;
import static com.zoffcc.applications.trifa.MainActivity.tox_util_friend_resend_message_v2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ADD_BOTS_ON_STARTUP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP1;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP3;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.DEBUG_BATTERY_OPTIMIZATION_LOGGING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HAVE_INTERNET_CONNECTIVITY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_NODES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrap_node_list;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_entered_battery_saving_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_online_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_anygroupview;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_messageview;
import static com.zoffcc.applications.trifa.TRIFAGlobals.tcprelay_node_list;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;

public class TrifaToxService extends Service
{
    static final String TAG = "trifa.ToxService";
    Notification notification2 = null;
    NotificationManager nmn2 = null;
    static Thread ToxServiceThread = null;
    // static EchoCanceller canceller = null;
    static boolean stop_me = false;
    static OrmaDatabase orma = null;
    static VirtualFileSystem vfs = null;
    static boolean is_tox_started = false;
    static boolean global_toxid_text_set = false;
    static boolean TOX_SERVICE_STARTED = false;
    static Thread trifa_service_thread = null;
    static long last_resend_pending_messages_ms = -1;
    static long last_resend_pending_messages2_ms = -1;
    static boolean need_wakeup_now = false;
    static int tox_thread_starting_up = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        // this gets called all the time!
        tox_service_fg = this;
        return START_NOT_STICKY; // START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        // serivce is created ---
        super.onCreate();

        TOX_SERVICE_STARTED = true;
        start_me();
    }

    /*
     *
     * ------ this really stops the whole thing ------
     *
     */
    void stop_me(boolean exit_app)
    {
        Log.i(TAG, "stop_me:001:tox_thread_starting_up=" + tox_thread_starting_up);
        stopForeground(true);

        Log.i(TAG, "stop_me:002:tox_thread_starting_up=" + tox_thread_starting_up);
        tox_notification_cancel(this);
        Log.i(TAG, "stop_me:003");

        final Context static_context = this;

        if (exit_app)
        {
            try
            {
                Log.i(TAG, "stop_me:004:tox_thread_starting_up=" + tox_thread_starting_up);
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Log.i(TAG, "stop_me:005:tox_thread_starting_up=" + tox_thread_starting_up);
                        set_filteraudio_active(0);
                        long i = 0;
                        while (is_tox_started)
                        {
                            i++;
                            if (i > 40)
                            {
                                break;
                            }

                            Log.i(TAG, "stop_me:006:tox_thread_starting_up=" + tox_thread_starting_up);

                            try
                            {
                                Thread.sleep(150);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "stop_me:006a:tox_thread_starting_up=" + tox_thread_starting_up);

                        if (VFS_ENCRYPT)
                        {
                            Log.i(TAG, "stop_me:006b");
                            try
                            {
                                Log.i(TAG, "stop_me:006c");
                                if (vfs.isMounted())
                                {
                                    Log.i(TAG, "stop_me:006d");
                                    Log.i(TAG, "VFS:detach:start:vfs.isMounted()=" + vfs.isMounted());
                                    vfs__detach();
                                    Log.i(TAG, "stop_me:006e");
                                    Thread.sleep(1);
                                    Log.i(TAG, "VFS:unmount:start:vfs.isMounted()=" + vfs.isMounted());
                                    vfs__unmount();
                                    Log.i(TAG, "stop_me:006f");
                                }
                                else
                                {
                                    Log.i(TAG, "stop_me:006g");
                                    Log.i(TAG, "VFS:unmount:NOT MOUNTED");
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "VFS:unmount:EE:" + e.getMessage());
                                Log.i(TAG, "stop_me:006h");
                            }
                        }

                        Log.i(TAG, "stop_me:007");

                        try
                        {
                            unregisterReceiver(receiver1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            unregisterReceiver(receiver2);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            unregisterReceiver(receiver3);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            unregisterReceiver(receiver4);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "stop_me:008");
                        tox_notification_cancel(static_context);
                        Log.i(TAG, "stop_me:009");

                        Log.i(TAG, "stop_me:010");
                        stopSelf();
                        Log.i(TAG, "stop_me:011");

                        try
                        {
                            Log.i(TAG, "stop_me:012");
                            Thread.sleep(300);
                            Log.i(TAG, "stop_me:013");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "stop_me:014");
                        tox_notification_cancel(static_context);
                        Log.i(TAG, "stop_me:015");

                        MainActivity.exit();

                        Log.i(TAG, "stop_me:089");
                    }
                };
                t.start();
                Log.i(TAG, "stop_me:099");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    stopSelf();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }
        }
    }

    static boolean stop_tox_fg_done = false;

    void stop_tox_fg(final boolean want_exit)
    {
        stop_tox_fg_done = false;
        Log.i(TAG, "stop_tox_fg:001");
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "stop_tox_fg:002:a");
                HelperGeneric.update_savedata_file_wrapper(); // save on tox shutdown
                Log.i(TAG, "stop_tox_fg:002:b");
                stop_me = true;

                try
                {
                    ToxServiceThread.interrupt();
                }
                catch (Exception e)
                {

                }

                Log.i(TAG, "stop_tox_fg:003");
                try
                {
                    Log.i(TAG, "stop_tox_fg:004");
                    ToxServiceThread.join();
                    Log.i(TAG, "stop_tox_fg:005");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "stop_tox_fg:006:EE:" + e.getMessage());
                    e.printStackTrace();
                }

                stop_me = false; // reset flag again!
                Log.i(TAG, "stop_tox_fg:007");
                tox_notification_change_wrapper(0, ""); // set to offline
                Log.i(TAG, "stop_tox_fg:008");
                set_all_friends_offline();
                Log.i(TAG, "set_all_conferences_inactive:003");
                set_all_conferences_inactive();

                // so that the app knows we went offline
                global_self_connection_status = TOX_CONNECTION_NONE.value;

                Log.i(TAG, "stop_tox_fg:009");

                if (want_exit)
                {
                    tox_notification_cancel(context_s);
                    Log.i(TAG, "stop_tox_fg:clear_tox_notification");
                }

                try
                {
                    Log.i(TAG, "stop_tox_fg:010a");
                    Thread.sleep(500);
                    Log.i(TAG, "stop_tox_fg:010b");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                stop_tox_fg_done = true;
                is_tox_started = false;

                Log.i(TAG, "stop_tox_fg:thread:done");
            }
        };

        Log.i(TAG, "stop_tox_fg:HH:001");
        t.start();
        Log.i(TAG, "stop_tox_fg:HH:004");
        Log.i(TAG, "stop_tox_fg:099");
    }

    void load_and_add_all_friends()
    {
        // --- load and update all friends ---
        MainActivity.friends = MainActivity.tox_self_get_friend_list();
        Log.i(TAG, "loading_friend:number_of_friends=" + MainActivity.friends.length);

        int fc = 0;
        boolean exists_in_db = false;
        //                try
        //                {
        //                    MainActivity.friend_list_fragment.clear_friends();
        //                }
        //                catch (Exception e)
        //                {
        //                }

        for (fc = 0; fc < MainActivity.friends.length; fc++)
        {
            // Log.i(TAG, "loading_friend:" + fc + " friendnum=" + MainActivity.friends[fc]);
            // Log.i(TAG, "loading_friend:" + fc + " pubkey=" + tox_friend_get_public_key__wrapper(MainActivity.friends[fc]));

            FriendList f;
            List<FriendList> fl = orma.selectFromFriendList().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();

            // Log.i(TAG, "loading_friend:" + fc + " db entry size=" + fl);

            if (fl.size() > 0)
            {
                f = fl.get(0);
                // Log.i(TAG, "loading_friend:" + fc + " db entry=" + f);
            }
            else
            {
                f = null;
            }

            if (f == null)
            {
                Log.i(TAG, "loading_friend:c is null");

                f = new FriendList();
                f.tox_public_key_string = "" + (long) ((Math.random() * 10000000d));
                try
                {
                    f.tox_public_key_string = tox_friend_get_public_key__wrapper(MainActivity.friends[fc]);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                f.name = "friend #" + fc;
                exists_in_db = false;
                // Log.i(TAG, "loading_friend:c is null fnew=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:found friend in DB " + f.tox_public_key_string + " f=" + f);
                exists_in_db = true;
            }

            try
            {
                // get the real "live" connection status of this friend
                // the value in the database may be old (and wrong)
                int status_new = tox_friend_get_connection_status(MainActivity.friends[fc]);
                int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string, status_new);
                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                f.added_timestamp = System.currentTimeMillis();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // ----- would be double in list -----
            // ----- would be double in list -----
            // ----- would be double in list -----
            //                    if (MainActivity.friend_list_fragment != null)
            //                    {
            //                        try
            //                        {
            //                            MainActivity.friend_list_fragment.add_friends(f);
            //                        }
            //                        catch (Exception e)
            //                        {
            //                        }
            //                    }
            // ----- would be double in list -----
            // ----- would be double in list -----
            // ----- would be double in list -----

            if (exists_in_db == false)
            {
                // Log.i(TAG, "loading_friend:1:insertIntoFriendList:" + " f=" + f);
                orma.insertIntoFriendList(f);
                // Log.i(TAG, "loading_friend:2:insertIntoFriendList:" + " f=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
                orma.updateFriendList().tox_public_key_stringEq(
                        tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).name(f.name).status_message(
                        f.status_message).TOX_CONNECTION(f.TOX_CONNECTION).TOX_CONNECTION_on_off(
                        get_toxconnection_wrapper(f.TOX_CONNECTION)).TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
            }

            FriendList f_check;
            List<FriendList> fl_check = orma.selectFromFriendList().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();
            // Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check);
            try
            {
                // Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check.get(0));

                try
                {
                    if (MainActivity.friend_list_fragment != null)
                    {
                        // reload friend in friendlist
                        CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                        cc.is_friend = true;
                        cc.friend_item = fl_check.get(0);
                        MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "loading_friend:check:EE:" + e.getMessage());
            }
        }
        // --- load and update all friends ---
    }

    void load_and_add_all_conferences()
    {
        long num_conferences = tox_conference_get_chatlist_size();
        Log.i(TAG, "load conferences at startup: num=" + num_conferences);

        long[] conference_numbers = tox_conference_get_chatlist();
        ByteBuffer cookie_buf3 = ByteBuffer.allocateDirect(CONFERENCE_ID_LENGTH * 2);

        int conf_ = 0;
        for (conf_ = 0; conf_ < num_conferences; conf_++)

        {
            cookie_buf3.clear();
            if (tox_conference_get_id(conference_numbers[conf_], cookie_buf3) == 0)
            {
                byte[] cookie_buffer = new byte[CONFERENCE_ID_LENGTH];
                cookie_buf3.get(cookie_buffer, 0, CONFERENCE_ID_LENGTH);
                String conference_identifier = bytes_to_hex(cookie_buffer);
                // Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " cookie=" + conference_identifier +
                //           " offset=" + cookie_buf3.arrayOffset());

                final ConferenceDB conf2 = orma.selectFromConferenceDB().toList().get(0);
                //Log.i(TAG,
                //      "conference 0 in db:" + conf2.conference_identifier + " " + conf2.tox_conference_number + " " +
                //      conf2.name);

                new_or_updated_conference(conference_numbers[conf_], tox_friend_get_public_key__wrapper(0),
                                          conference_identifier, tox_conference_get_type(
                                conference_numbers[conf_])); // rejoin a saved conference

                //if (tox_conference_get_type(conference_numbers[conf_]) == TOX_CONFERENCE_TYPE_AV.value)
                //{
                //    // TODO: this returns error. check it
                //    long result = toxav_groupchat_disable_av(conference_numbers[conf_]);
                //    Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " toxav_groupchat_disable_av res=" +
                //               result);
                //}

                try
                {
                    if (conference_message_list_activity != null)
                    {
                        if (conference_message_list_activity.get_current_conf_id().equals(conference_identifier))
                        {
                            conference_message_list_activity.set_conference_connection_status_icon();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (conference_audio_activity != null)
                    {
                        if (conference_audio_activity.get_current_conf_id().equals(conference_identifier))
                        {
                            conference_audio_activity.set_conference_connection_status_icon();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    static void write_debug_file(String filename)
    {
        if (DEBUG_BATTERY_OPTIMIZATION_LOGGING)
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/trifa/debug/");
            dir.mkdirs();
            String filename2 = long_date_time_format(System.currentTimeMillis()) + "_" + filename;
            File file = new File(dir, filename2);

            try
            {
                FileOutputStream f = new FileOutputStream(file);
                f.write(1);
                f.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    void tox_thread_start_fg()
    {
        Log.i(TAG, "tox_thread_start_fg");

        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {

                tox_thread_starting_up = 0;

                try
                {
                    // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
                    this.setName("tox_iterate()");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "thread set name:" + e.getMessage());
                }

                // ------ correct startup order ------
                boolean old_is_tox_started = is_tox_started;
                Log.i(TAG, "is_tox_started:==============================");
                Log.i(TAG, "is_tox_started=" + is_tox_started);
                Log.i(TAG, "is_tox_started:==============================");

                is_tox_started = true;

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!global_toxid_text_set)
                        {
                            global_toxid_text_set = true;
                            // MainActivity.mt.setText(MainActivity.mt.getText() + "\n" + "my_ToxId=" + get_my_toxid());
                        }
                    }
                };

                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }

                if (!old_is_tox_started)
                {
                    Log.i(TAG, "set_all_conferences_inactive:004");
                    set_all_friends_offline();
                    set_all_conferences_inactive();
                    MainActivity.init_tox_callbacks();
                    HelperGeneric.update_savedata_file_wrapper();
                }
                // ------ correct startup order ------

                cache_pubkey_fnum.clear();
                cache_fnum_pubkey.clear();
                cache_confid_confnum.clear();

                // ----- convert old conference messages which did not contain a sent timestamp -----
                try
                {
                    boolean need_migrate_old_conf_msg_date = true;

                    if (get_g_opts("MIGRATE_OLD_CONF_MSG_DATE_done") != null)
                    {
                        if (get_g_opts("MIGRATE_OLD_CONF_MSG_DATE_done").equals("true"))
                        {
                            need_migrate_old_conf_msg_date = false;
                        }
                    }

                    if (need_migrate_old_conf_msg_date == true)
                    {
                        try
                        {
                            orma.getConnection().execSQL(
                                    "update ConferenceMessage set sent_timestamp=rcvd_timestamp" + " where " +
                                    " sent_timestamp='0'");
                            Log.i(TAG, "onCreate:migrate_old_conf_msg_date");
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "onCreate:migrate_old_conf_msg_date:EE01");
                        }
                        // now remember that we did that, and don't do it again
                        set_g_opts("MIGRATE_OLD_CONF_MSG_DATE_done", "true");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "onCreate:migrate_old_conf_msg_date:EE:" + e.getMessage());
                }
                // ----- convert old conference messages which did not contain a sent timestamp -----

                // ----- convert old NULL's into false -----
                try
                {
                    orma.getConnection().execSQL(
                            "update ConferenceMessage set was_synced=false" + " where " + " was_synced is NULL");
                    Log.i(TAG, "onCreate:migrate_was_synced");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:migrate_was_synced:EE01");
                }
                // ----- convert old NULL's into false -----


                // TODO --------
                String my_tox_id_local = get_my_toxid();
                global_my_toxid = my_tox_id_local;
                if (tox_self_get_name_size() > 0)
                {
                    String tmp_name = tox_self_get_name();
                    if (tmp_name != null)
                    {
                        if (tmp_name.length() > 0)
                        {
                            global_my_name = tmp_name;
                            // Log.i(TAG, "AAA:003:" + global_my_name + " size=" + tox_self_get_name_size());
                        }
                    }
                }
                else
                {
                    tox_self_set_name("TRIfA " + my_tox_id_local.substring(0, 4));
                    global_my_name = ("TRIfA " + my_tox_id_local.substring(0, 4));
                    Log.i(TAG, "AAA:005");
                }

                if (tox_self_get_status_message_size() > 0)
                {
                    String tmp_status = tox_self_get_status_message();
                    if (tmp_status != null)
                    {
                        if (tmp_status.length() > 0)
                        {
                            global_my_status_message = tmp_status;
                            // Log.i(TAG, "AAA:008:" + global_my_status_message + " size=" + tox_self_get_status_message_size());
                        }
                    }
                }
                else
                {
                    tox_self_set_status_message("this is TRIfA");
                    global_my_status_message = "this is TRIfA";
                    Log.i(TAG, "AAA:010");
                }
                Log.i(TAG, "AAA:011");

                HelperGeneric.update_savedata_file_wrapper();

                load_and_add_all_friends();

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                if (!old_is_tox_started)
                {
                    bootstrapping = true;
                    global_self_last_went_offline_timestamp = System.currentTimeMillis();
                    Log.i(TAG, "global_self_last_went_offline_timestamp[1]=" + global_self_last_went_offline_timestamp +
                               " HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY);
                    Log.i(TAG, "bootrapping:set to true[1]");
                    try
                    {
                        tox_notification_change(context_s, nmn2, 0, ""); // set notification to "bootstrapping"
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        bootstrap_me();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "bootstrap_me:001:EE:" + e.getMessage());
                    }
                }

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------

                long tox_iteration_interval_ms = tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                boolean tox_iterate_thread_high_prio = false;

                MainActivity.tox_iterate();

                if (ADD_BOTS_ON_STARTUP)
                {
                    boolean need_add_bots = true;

                    try
                    {
                        if (get_g_opts("ADD_BOTS_ON_STARTUP_done") != null)
                        {
                            if (get_g_opts("ADD_BOTS_ON_STARTUP_done").equals("true"))
                            {
                                need_add_bots = false;
                                Log.i(TAG, "need_add_bots=false");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (need_add_bots)
                    {
                        Log.i(TAG, "need_add_bots:start");
                        add_friend_real(ECHOBOT_TOXID);
                        set_g_opts("ADD_BOTS_ON_STARTUP_done", "true");
                        Log.i(TAG, "need_add_bots=true (INSERT)");
                    }
                }

                try
                {
                    load_and_add_all_conferences();
                }
                catch (Exception e)
                {
                }

                global_self_last_went_offline_timestamp = System.currentTimeMillis();
                Log.i(TAG, "global_self_last_went_offline_timestamp[2]=" + global_self_last_went_offline_timestamp +
                           " HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY);


                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                tox_thread_starting_up = 1;
                while (!stop_me)
                {
                    try
                    {
                        if (tox_iteration_interval_ms < 1)
                        {
                            //Log.i(TAG, "tox_iterate:(tox_iteration_interval_ms < 2ms!!):" + tox_iteration_interval_ms +
                            //           "ms");
                            Thread.sleep(1);
                        }
                        else
                        {
                            if ((PREF__X_battery_saving_mode) && (battery_saving_can_sleep()))
                            {
                                need_wakeup_now = false;

                                // set the used value to the new value
                                BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS = PREF__X_battery_saving_timeout * 1000 * 60;
                                Log.i(TAG, "set BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS:" +
                                           BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS + " PREF__X_battery_saving_timeout:" +
                                           PREF__X_battery_saving_timeout);


                                Log.i(TAG, "entering BATTERY SAVINGS MODE ...");
                                TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__enter");

                                long current_timestamp_ = System.currentTimeMillis();
                                global_self_last_entered_battery_saving_timestamp = current_timestamp_;

                                trifa_service_thread = Thread.currentThread();

                                // ---------------------------------------------------------
                                Intent intent_wakeup = new Intent(getApplicationContext(), WakeupAlarmReceiver.class);
                                // intentWakeFullBroacastReceiver.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1001,
                                                                                       intent_wakeup,
                                                                                       PendingIntent.FLAG_CANCEL_CURRENT);
                                getApplicationContext();
                                AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(
                                        ALARM_SERVICE);


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                {
                                    //alarmManager.setExactAndAllowWhileIdle(
                                    //        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    //        SystemClock.elapsedRealtime() +
                                    //        BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS +
                                    //        (int) (Math.random() * 15000d) + 5000, alarmIntent);

                                    Log.i(TAG, "get BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS:" +
                                               BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS);


                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                                                           System.currentTimeMillis() +
                                                                           BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS +
                                                                           (int) (Math.random() * 15000d) + 5000,
                                                                           alarmIntent);
                                }
                                else
                                {
                                    //alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    //                      SystemClock.elapsedRealtime() +
                                    //                      BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS +
                                    //                      (int) (Math.random() * 15000d) + 5000,
                                    //                      alarmIntent);

                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                                                                                   BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS +
                                                                                   (int) (Math.random() * 15000d) +
                                                                                   5000, alarmIntent);
                                }


                                //MARSHMALLOW OR ABOVE
                                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                {
                                    //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                    //                                       System.currentTimeMillis() +
                                    //                                       BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS +
                                    //                                       (int) (Math.random() * 15000d) +
                                    //                                       5000, alarmIntent);

                                    //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    //                                 SystemClock.elapsedRealtime() +
                                    //                                 AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                                    //                                 AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                                    //                                 alarmIntent);

                                }

                                // ---------------------------------------------------------


                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                tox_notification_change_wrapper(0, "sleep: " +
                                                                   (int) ((BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS /
                                                                           1000) / 60) + "min (" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP1 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP2 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP3 + ") " +
                                                                   long_date_time_format_or_empty(
                                                                           global_self_last_entered_battery_saving_timestamp)); // set to offline

                                if (!need_wakeup_now)
                                {
                                    if ((!global_showing_messageview) && (!global_showing_anygroupview))
                                    {
                                        set_all_friends_offline();
                                        set_all_conferences_inactive();
                                    }
                                }
                                // so that the app knows we went offline
                                global_self_last_went_offline_timestamp = System.currentTimeMillis();
                                global_self_connection_status = TOX_CONNECTION_NONE.value;
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------

                                if (!need_wakeup_now)
                                {
                                    if ((!global_showing_messageview) && (!global_showing_anygroupview))
                                    {
                                        try
                                        {
                                            Thread.sleep(30 * 1000);
                                        }
                                        catch (Exception es)
                                        {
                                        }
                                    }
                                }
                                MainActivity.tox_iterate();

                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                tox_notification_change_wrapper(0, "sleep: " +
                                                                   (int) ((BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS /
                                                                           1000) / 60) + "min (" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP1 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP2 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP3 + ") " +
                                                                   long_date_time_format_or_empty(
                                                                           global_self_last_entered_battery_saving_timestamp)); // set to offline

                                if (!need_wakeup_now)
                                {
                                    if ((!global_showing_messageview) && (!global_showing_anygroupview))
                                    {
                                        set_all_friends_offline();
                                        set_all_conferences_inactive();
                                    }
                                }
                                // so that the app knows we went offline
                                global_self_connection_status = TOX_CONNECTION_NONE.value;
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------

                                Log.i(TAG, "entering BATTERY SAVINGS MODE ... 30s");

                                if (!need_wakeup_now)
                                {
                                    if ((!global_showing_messageview) && (!global_showing_anygroupview))
                                    {
                                        try
                                        {
                                            Thread.sleep(30 * 1000);
                                        }
                                        catch (Exception es)
                                        {
                                        }
                                    }
                                }
                                MainActivity.tox_iterate();

                                Log.i(TAG, "entering BATTERY SAVINGS MODE ... 60s");

                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                tox_notification_change_wrapper(0, "sleep: " +
                                                                   (int) ((BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS /
                                                                           1000) / 60) + "min (" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP1 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP2 + "/" +
                                                                   BATTERY_OPTIMIZATION_LAST_SLEEP3 + ") " +
                                                                   long_date_time_format_or_empty(
                                                                           global_self_last_entered_battery_saving_timestamp)); // set to offline

                                if (!need_wakeup_now)
                                {
                                    if ((!global_showing_messageview) && (!global_showing_anygroupview))
                                    {
                                        set_all_friends_offline();
                                        set_all_conferences_inactive();
                                    }
                                }
                                // so that the app knows we went offline
                                global_self_last_went_offline_timestamp = System.currentTimeMillis();
                                global_self_connection_status = TOX_CONNECTION_NONE.value;
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------
                                // --------------- set everything to offline ---------------

                                long sleep_in_sec = BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS;
                                // add some random value, so that the sleep is not always exactly the same
                                sleep_in_sec = sleep_in_sec + (int) (Math.random() * 15000d) + 5000;
                                sleep_in_sec = sleep_in_sec / 1000;
                                sleep_in_sec = sleep_in_sec / 10; // now in 10s of seconds!!

                                Log.i(TAG, "entering BATTERY SAVINGS MODE ... sleep for " + (10 * sleep_in_sec) + "s");

                                for (int ii = 0; ii < sleep_in_sec; ii++)
                                {
                                    if ((global_showing_messageview) || (global_showing_anygroupview))
                                    {
                                        // if the user opens the message view, or any group view -> go online, to be able to send messages
                                        Log.i(TAG, "finish BATTERY SAVINGS MODE (Message view opened)");
                                        TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__msgview");
                                        break;
                                    }

                                    if (need_wakeup_now)
                                    {
                                        break;
                                    }

                                    try
                                    {
                                        Thread.sleep(10 * 1000); // sleep very long!!
                                        //Log.i(TAG,
                                        //      "BATTERY SAVINGS MODE (sleep " + ii + "/" + sleep_in_sec +
                                        //      ")");
                                    }
                                    catch (Exception es)
                                    {
                                        TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__interrupted");
                                        break;
                                    }
                                }

                                Log.i(TAG, "finish BATTERY SAVINGS MODE, connecting again");
                                TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__connecting");

                                // update all friends again
                                try
                                {
                                    load_and_add_all_friends();
                                }
                                catch (Exception e)
                                {
                                }
                                // load conferences again
                                try
                                {
                                    load_and_add_all_conferences();
                                }
                                catch (Exception e)
                                {
                                }

                                Log.i(TAG, "BATTERY SAVINGS MODE, load_and_add_all_conferences");

                                // iterate a few times ---------------------
                                MainActivity.tox_iterate();
                                try
                                {
                                    Thread.sleep(10);
                                }
                                catch (Exception es)
                                {
                                }
                                MainActivity.tox_iterate();
                                try
                                {
                                    Thread.sleep(10);
                                }
                                catch (Exception es)
                                {
                                }
                                MainActivity.tox_iterate();
                                try
                                {
                                    Thread.sleep(10);
                                }
                                catch (Exception es)
                                {
                                }
                                // iterate a few times ---------------------

                                need_wakeup_now = false;
                                trifa_service_thread = null;

                                // bootstrap_single_wrapper("127.3.2.1",9988, "AAA236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9FFF");

                                int TOX_CONNECTION_a = tox_self_get_connection_status();
                                if (TOX_CONNECTION_a == TOX_CONNECTION_NONE.value)
                                {
                                    bootstrapping = true;
                                    global_self_last_went_offline_timestamp = System.currentTimeMillis();
                                    Log.i(TAG, "BATTERY SAVINGS MODE, bootstrapping");
                                    tox_notification_change_wrapper(TOX_CONNECTION_a,
                                                                    ""); // set to real connection status
                                    bootstrap_me();
                                    TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__bootstrapping");
                                }
                                else
                                {
                                    bootstrapping = false;
                                    global_self_last_went_online_timestamp = System.currentTimeMillis();
                                    global_self_last_went_offline_timestamp = -1;
                                    tox_notification_change_wrapper(TOX_CONNECTION_a,
                                                                    ""); // set to real connection status
                                    Log.i(TAG, "BATTERY SAVINGS MODE, already_online");

                                    TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__already_online");
                                }


                                BATTERY_OPTIMIZATION_LAST_SLEEP3 = BATTERY_OPTIMIZATION_LAST_SLEEP2;
                                BATTERY_OPTIMIZATION_LAST_SLEEP2 = BATTERY_OPTIMIZATION_LAST_SLEEP1;
                                BATTERY_OPTIMIZATION_LAST_SLEEP1 = (int) (
                                        (System.currentTimeMillis() - current_timestamp_) / 1000 / 60);
                                if ((BATTERY_OPTIMIZATION_LAST_SLEEP1 < 0) ||
                                    (BATTERY_OPTIMIZATION_LAST_SLEEP1 > (3 * 3600 * 1000)))
                                {
                                    BATTERY_OPTIMIZATION_LAST_SLEEP1 = -1;
                                }


                                // set the used value to the new value
                                BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS = PREF__X_battery_saving_timeout * 1000 * 60;
                                Log.i(TAG, "set BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS:" +
                                           BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS + " PREF__X_battery_saving_timeout:" +
                                           PREF__X_battery_saving_timeout);

                                // global_self_connection_status = tox_self_get_connection_status();
                            }
                            else
                            {
                                // Log.i(TAG, "(tox_iteration_interval_ms):" + tox_iteration_interval_ms + "ms");
                                Thread.sleep(tox_iteration_interval_ms);
                            }
                        }

                        // ----------
                        if (global_self_connection_status == TOX_CONNECTION_NONE.value)
                        {
                            if (HAVE_INTERNET_CONNECTIVITY)
                            {
                                if (global_self_last_went_offline_timestamp != -1)
                                {
                                    if (global_self_last_went_offline_timestamp +
                                        TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS < System.currentTimeMillis())
                                    {
                                        Log.i(TAG, "offline and we have internet connectivity --> bootstrap again ...");
                                        global_self_last_went_offline_timestamp = System.currentTimeMillis();

                                        bootstrapping = true;
                                        Log.i(TAG, "bootrapping:set to true[2]");
                                        try
                                        {
                                            tox_notification_change(context_s, nmn2, 0, "sleep: " +
                                                                                        (int) ((BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS /
                                                                                                1000) / 60) + "min (" +
                                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP1 +
                                                                                        "/" +
                                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP2 +
                                                                                        "/" +
                                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP3 +
                                                                                        ") " +
                                                                                        long_date_time_format_or_empty(
                                                                                                global_self_last_entered_battery_saving_timestamp)); // set notification to "bootstrapping"
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }

                                        try
                                        {
                                            bootstrap_me();
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            Log.i(TAG, "bootstrap_me:001:EE:" + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    MainActivity.tox_iterate();

                    if ((Callstate.state != 0) || (Callstate.audio_group_active))
                    {
                        if (Callstate.audio_group_active)
                        {
                            tox_iteration_interval_ms = 5; // if we are in a group audio call iterate more often

                            /*
                            if (!tox_iterate_thread_high_prio)
                            {
                                try
                                {
                                    tox_iterate_thread_high_prio = true;
                                    this.setName("tox_iterate()+");
                                    // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
                                    // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                                    android.os.Process.setThreadPriority(
                                            android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            */
                        }
                        else
                        {
                            tox_iteration_interval_ms = 10; // if we are in a video/audio call iterate more often

                            if (!tox_iterate_thread_high_prio)
                            {
                                try
                                {
                                    tox_iterate_thread_high_prio = true;
                                    this.setName("tox_iterate()+");
                                    // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
                                    // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                                    // android.os.Process.setThreadPriority(
                                    //        android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            /*
                            long tox_iteration_interval_should_be = tox_iteration_interval();
                            if (tox_iteration_interval_should_be < tox_iteration_interval_ms)
                            {
                                tox_iteration_interval_ms = tox_iteration_interval_should_be;
                            }
                            */
                        }
                    }
                    else
                    {
                        // tox_iteration_interval_ms = Math.max(100, MainActivity.tox_iteration_interval());
                        tox_iteration_interval_ms = tox_iteration_interval();
                        // Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                        if (tox_iterate_thread_high_prio)
                        {
                            try
                            {
                                tox_iterate_thread_high_prio = false;
                                this.setName("tox_iterate()");
                                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }

                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {

                        if ((last_resend_pending_messages_ms + (20 * 1000)) < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "send_pending_1-on-1_messages ============================================");
                            last_resend_pending_messages_ms = System.currentTimeMillis();

                            // loop through all pending outgoing 1-on-1 text messages --------------
                            try
                            {
                                final int max_resend_count_per_iteration = 10;
                                int cur_resend_count_per_iteration = 0;

                                List<Message> m_v1 = orma.selectFromMessage().
                                        directionEq(1).
                                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                                        resend_countEq(0).
                                        readEq(false).
                                        orderBySent_timestampAsc().
                                        toList();

                                if (m_v1.size() > 0)
                                {
                                    Iterator<Message> ii = m_v1.iterator();
                                    while (ii.hasNext())
                                    {
                                        Message m_resend_v1 = ii.next();

                                        if (is_friend_online(
                                                tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey)) == 0)
                                        {
                                            //Log.i(TAG, "send_pending_1-on-1_messages:v1:fname=" +
                                            //           get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey) +
                                            //           " NOT online m=" + m_resend_v1.text);

                                            continue;
                                        }

                                        Log.i(TAG, "send_pending_1-on-1_messages:v1:fname=" +
                                                   get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey) + " m=" +
                                                   m_resend_v1.text);

                                        MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                                                tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey), 0,
                                                m_resend_v1.text);
                                        long res = result.msg_num;

                                        Log.i(TAG,
                                              "send_pending_1-on-1_messages:v1:res=" + res + " m=" + m_resend_v1.text);

                                        if (res > -1) // sending was OK
                                        {
                                            m_resend_v1.message_id = res;
                                            update_message_in_db_messageid(m_resend_v1);

                                            if (!result.raw_message_buf_hex.equalsIgnoreCase(""))
                                            {
                                                // save raw message bytes of this v2 msg into the database
                                                // we need it if we want to resend it later
                                                m_resend_v1.raw_msgv2_bytes = result.raw_message_buf_hex;
                                            }

                                            if (!result.msg_hash_hex.equalsIgnoreCase(""))
                                            {
                                                // msgV2 message -----------
                                                m_resend_v1.msg_id_hash = result.msg_hash_hex;
                                                m_resend_v1.msg_version = 1;
                                                // msgV2 message -----------
                                            }

                                            m_resend_v1.resend_count = 1; // we sent the message successfully
                                            update_message_in_db_no_read_recvedts(m_resend_v1);
                                            update_message_in_db_resend_count(m_resend_v1);
                                            update_single_message(m_resend_v1, true);

                                            cur_resend_count_per_iteration++;

                                            if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                                            {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "send_pending_1-on-1_messages:v1:EE:" + e.getMessage());
                            }
                            // loop through all pending outgoing 1-on-1 text messages --------------

                        }

                        if ((last_resend_pending_messages2_ms + (120 * 1000)) < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "send_pending_1-on-1_messages 2 ============================================");
                            last_resend_pending_messages2_ms = System.currentTimeMillis();


                            // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------
                            try
                            {
                                final int max_resend_count_per_iteration = 10;
                                int cur_resend_count_per_iteration = 0;

                                List<Message> m_v1 = orma.selectFromMessage().
                                        directionEq(1).
                                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                                        resend_countEq(1).
                                        msg_versionEq(1).
                                        readEq(false).
                                        orderBySent_timestampAsc().
                                        toList();

                                if (m_v1.size() > 0)
                                {
                                    Iterator<Message> ii = m_v1.iterator();
                                    while (ii.hasNext())
                                    {
                                        Message m_resend_v2 = ii.next();

                                        if (is_friend_online(
                                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey)) == 0)
                                        {
                                            continue;
                                        }

                                        Log.i(TAG, "send_pending_1-on-1_messages:v2:fname=" +
                                                   get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " m=" +
                                                   m_resend_v2.text);

                                        // m_resend_v2.raw_msgv2_bytes

                                        final int raw_data_length = (m_resend_v2.raw_msgv2_bytes.length() / 2);
                                        byte[] raw_msg_resend_data = hex_to_bytes(m_resend_v2.raw_msgv2_bytes);

                                        ByteBuffer msg_text_buffer_resend_v2 = ByteBuffer.allocateDirect(
                                                raw_data_length);
                                        msg_text_buffer_resend_v2.put(raw_msg_resend_data, 0, raw_data_length);

                                        int res = tox_util_friend_resend_message_v2(
                                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey),
                                                msg_text_buffer_resend_v2, raw_data_length);

                                        Log.i(TAG, "send_pending_1-on-1_messages:v2:res=" + res);

                                        cur_resend_count_per_iteration++;

                                        if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                                        {
                                            break;
                                        }

                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "send_pending_1-on-1_messages:v1:EE:" + e.getMessage());
                            }
                            // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------


                        }
                    }
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------


                    //**// if (s_time + 4000 < System.currentTimeMillis())
                    //**// {
                    //**//     tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                    //**//     Log.i(TAG, "tox_iterate:--END--:took" +
                    //**//                (long) (((float) (s_time - System.currentTimeMillis()) / 1000f)) +
                    //**//                "s, new interval=" + tox_iteration_interval_ms + "ms");
                    //**// }
                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------

                tox_thread_starting_up = 2;

                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    NativeAudio.shutdownEngine();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    // this stops Tox
                    MainActivity.tox_kill();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //Log.i(TAG, "VFS:detachThread:(TrifaToxService):" + Thread.currentThread().getId() + ":" +
                //           Thread.currentThread().getName());
                //vfs.detachThread();
                //Log.i(TAG, "VFS:detachThread:(TrifaToxService):OK");
            }
        };

        ToxServiceThread.start();
    }

    void start_me()
    {
        Log.i(TAG, "start_me");
        notification2 = tox_notification_setup(this, nmn2);
        startForeground(HelperToxNotification.ONGOING_NOTIFICATION_ID, notification2);
    }

    static void bootstrap_me()
    {
        Log.i(TAG, "bootstrap_me");

        // ----- UDP ------
        get_udp_nodelist_from_db();
        Log.i(TAG, "bootstrap_node_list[sort]=" + bootstrap_node_list.toString());
        try
        {
            Collections.shuffle(bootstrap_node_list);
            Collections.shuffle(bootstrap_node_list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i(TAG, "bootstrap_node_list[rand]=" + bootstrap_node_list.toString());

        try
        {
            Iterator i2 = bootstrap_node_list.iterator();
            BootstrapNodeEntryDB ee;
            int used = 0;
            while (i2.hasNext())
            {
                ee = (BootstrapNodeEntryDB) i2.next();
                int bootstrap_result = bootstrap_single_wrapper(ee.ip, ee.port, ee.key_hex);
                Log.i(TAG, "bootstrap_single:res=" + bootstrap_result);

                if (bootstrap_result == 0)
                {
                    used++;
                }

                if (used > USE_MAX_NUMBER_OF_BOOTSTRAP_NODES)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ----- UDP ------
        //
        // ----- TCP ------
        get_tcprelay_nodelist_from_db();
        Log.i(TAG, "tcprelay_node_list[sort]=" + tcprelay_node_list.toString());
        try
        {
            Collections.shuffle(tcprelay_node_list);
            Collections.shuffle(tcprelay_node_list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i(TAG, "tcprelay_node_list[rand]=" + tcprelay_node_list.toString());

        try
        {
            if (USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS > 0)
            {
                if (!PREF__force_udp_only)
                {
                    Iterator i2 = tcprelay_node_list.iterator();
                    BootstrapNodeEntryDB ee;
                    int used = 0;
                    while (i2.hasNext())
                    {
                        ee = (BootstrapNodeEntryDB) i2.next();
                        int bootstrap_result = HelperGeneric.add_tcp_relay_single_wrapper(ee.ip, ee.port, ee.key_hex);
                        Log.i(TAG, "add_tcp_relay_single:res=" + bootstrap_result);

                        if (bootstrap_result == 0)
                        {
                            used++;
                        }

                        if (used > USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS)
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ----- TCP ------

        // ----- TCP mobile ------
        // Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("127.0.0.1", 33447, "252E6D7F8168682363BC473C3951357FB2E28BC9A7B7E1F4CB3B302DC331BDAA".substring(0, (TOX_PUBLIC_KEY_SIZE * 2) - 0)));
        // ----- TCP mobile ------
    }

    static void wakeup_tox_thread()
    {
        // This will wakeup the tox_iterate() thread and go online as quick as possible
        // only useful if in Batterysavings-Mode
        try
        {
            if (trifa_service_thread != null)
            {
                Log.i(TAG, "wakeup_tox_thread");
                TrifaToxService.need_wakeup_now = true;
                trifa_service_thread.interrupt();
                Log.i(TAG, "wakeup_tox_thread:DONE");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void unbindService(ServiceConnection conn)
    {
        Log.i(TAG, "unbindService");
        super.unbindService(conn);
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "onBind");
        return null;
    }

    // ------------------------------


    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    static void logger(int level, String text)
    {
        Log.i(TAG, text);
    }

    static String safe_string(byte[] in)
    {
        // Log.i(TAG, "safe_string:in=" + in);
        String out = "";

        try
        {
            out = new String(in, StandardCharsets.UTF_8);  // Best way to decode using "UTF-8"
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "safe_string:EE:" + e.getMessage());
            try
            {
                out = new String(in);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "safe_string:EE2:" + e2.getMessage());
            }
        }

        // Log.i(TAG, "safe_string:out=" + out);
        return out;
    }
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------
}
