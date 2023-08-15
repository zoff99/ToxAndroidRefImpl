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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
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

import static com.zoffcc.applications.nativeaudio.NativeAudio.set_aec_active;
import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.get_tcprelay_nodelist_from_db;
import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.get_udp_nodelist_from_db;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_FRIEND;
import static com.zoffcc.applications.trifa.HelperConference.new_or_updated_conference;
import static com.zoffcc.applications.trifa.HelperConference.set_all_conferences_inactive;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_all_filetransfers_inactive;
import static com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft;
import static com.zoffcc.applications.trifa.HelperFriend.add_friend_real;
import static com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_msgv3_capability;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.set_all_friends_offline;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.update_friend_in_db_connection_status;
import static com.zoffcc.applications.trifa.HelperGeneric.IPisValid;
import static com.zoffcc.applications.trifa.HelperGeneric.battery_saving_can_sleep;
import static com.zoffcc.applications.trifa.HelperGeneric.bootstrap_single_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.get_combined_connection_status;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.get_toxconnection_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.hex_to_bytes;
import static com.zoffcc.applications.trifa.HelperGeneric.isIPPortValid;
import static com.zoffcc.applications.trifa.HelperGeneric.is_valid_tox_public_key;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format_or_empty;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_resend_msgv3_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.vfs__unmount;
import static com.zoffcc.applications.trifa.HelperGroup.new_or_updated_group;
import static com.zoffcc.applications.trifa.HelperGroup.update_group_in_db_name;
import static com.zoffcc.applications.trifa.HelperGroup.update_group_in_db_privacy_state;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.is_any_relay;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_cancel;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_change;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_change_wrapper;
import static com.zoffcc.applications.trifa.HelperToxNotification.tox_notification_setup;
import static com.zoffcc.applications.trifa.MainActivity.DEBUG_BATTERY_OPTIMIZATION_LOGGING;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_timeout;
import static com.zoffcc.applications.trifa.MainActivity.PREF__force_udp_only;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_push_service;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_DEBUG_DIR;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.cache_confid_confnum;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.conference_audio_activity;
import static com.zoffcc.applications.trifa.MainActivity.conference_message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.get_my_toxid;
import static com.zoffcc.applications.trifa.MainActivity.group_message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.receiver1;
import static com.zoffcc.applications.trifa.MainActivity.receiver2;
import static com.zoffcc.applications.trifa.MainActivity.receiver3;
import static com.zoffcc.applications.trifa.MainActivity.receiver4;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_type;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_grouplist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_number_groups;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_privacy_state;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.MainActivity.tox_iteration_interval;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_capabilites;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_capabilities;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_service_fg;
import static com.zoffcc.applications.trifa.MainActivity.tox_util_friend_resend_message_v2;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_video_decode;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_video_encode;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ADD_BOTS_ON_STARTUP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP1;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_LAST_SLEEP3;
import static com.zoffcc.applications.trifa.TRIFAGlobals.BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_INIT_NAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_INIT_STATUSMSG;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HAVE_INTERNET_CONNECTIVITY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_NODES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrap_node_list;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_incoming_ft_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts;
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
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;

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
    static long last_resend_pending_messages0_ms = -1;
    static long last_resend_pending_messages1_ms = -1;
    static long last_resend_pending_messages2_ms = -1;
    static long last_resend_pending_messages3_ms = -1;
    static long last_resend_pending_messages4_ms = -1;
    static long last_start_queued_fts_ms = -1;
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
                        set_aec_active(0);
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
                                    Log.i(TAG, "VFS:detach:start:vfs.isMounted()=" + vfs.isMounted() + " " +
                                               Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
                                    // vfs__detach();
                                    Log.i(TAG, "stop_me:006e");
                                    Thread.sleep(1);
                                    Log.i(TAG, "VFS:unmount:start:vfs.isMounted()=" + vfs.isMounted() + " " +
                                               Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
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

                        // MainActivity.exit();
                        try
                        {
                            System.exit(0);
                        }
                        catch (Exception ignored)
                        {
                        }

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

                try
                {
                    System.exit(0);
                }
                catch (Exception ignored)
                {
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
        long[] friends = MainActivity.tox_self_get_friend_list();
        Log.i(TAG, "loading_friend:number_of_friends=" + friends.length);

        int fc = 0;
        boolean exists_in_db = false;

        for (fc = 0; fc < friends.length; fc++)
        {
            // Log.i(TAG, "loading_friend:" + fc + " friendnum=" + MainActivity.friends[fc]);
            // Log.i(TAG, "loading_friend:" + fc + " pubkey=" + tox_friend_get_public_key__wrapper(MainActivity.friends[fc]));

            FriendList f;
            List<FriendList> fl = orma.selectFromFriendList().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friends[fc])).toList();

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
                    f.tox_public_key_string = tox_friend_get_public_key__wrapper(friends[fc]);
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
                int status_new = tox_friend_get_connection_status(friends[fc]);
                int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string, status_new);

                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                f.TOX_CONNECTION_real = status_new;
                f.TOX_CONNECTION_on_off_real = get_toxconnection_wrapper(f.TOX_CONNECTION_real);

                f.added_timestamp = System.currentTimeMillis();

                if ((status_new != 0) && (combined_connection_status_ != 0))
                {
                    // Log.i(TAG, "non_relay_status:ALL:" + friends[fc] + " pk=" +
                    //           get_friend_name_from_pubkey(f.tox_public_key_string) + " status=" + status_new +
                    //           " combined_connection_status_=" + combined_connection_status_);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (exists_in_db == false)
            {
                // Log.i(TAG, "loading_friend:1:insertIntoFriendList:" + " f=" + f);
                orma.insertIntoFriendList(f);
                // Log.i(TAG, "loading_friend:2:insertIntoFriendList:" + " f=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);

                // @formatter:off
                orma.updateFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friends[fc])).
                        name(f.name).
                        status_message(f.status_message).
                        TOX_CONNECTION(f.TOX_CONNECTION).
                        TOX_CONNECTION_on_off(get_toxconnection_wrapper(f.TOX_CONNECTION)).
                        TOX_CONNECTION_real(f.TOX_CONNECTION_real).
                        TOX_CONNECTION_on_off_real(get_toxconnection_wrapper(f.TOX_CONNECTION_real)).
                        TOX_USER_STATUS(f.TOX_USER_STATUS).
                        execute();
                // @formatter:on
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
            }

            try_update_friend_in_friendlist(friends[fc]);
        }
        // --- load and update all friends ---

        // now run thru the list again to account for relays ----------------

        for (fc = 0; fc < friends.length; fc++)
        {
            try
            {
                List<FriendList> fl = orma.selectFromFriendList().tox_public_key_stringEq(
                        tox_friend_get_public_key__wrapper(friends[fc])).toList();

                if (fl.size() > 0)
                {
                    final FriendList f = fl.get(0);
                    if (!is_any_relay(f.tox_public_key_string))
                    {
                        final int status_new = tox_friend_get_connection_status(friends[fc]);
                        final String friends_relay = HelperRelay.get_relay_for_friend(f.tox_public_key_string);
                        if (friends_relay != null)
                        {
                            int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string,
                                                                                             status_new);

                            // Log.i(TAG, "non_relay_status:FWR:" + friends[fc] + " pk=" +
                            //           get_friend_name_from_pubkey(f.tox_public_key_string) + " status=" + status_new +
                            //           " combined_connection_status_=" + combined_connection_status_);

                            f.TOX_CONNECTION = combined_connection_status_;
                            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                            update_friend_in_db_connection_status(f);
                            try_update_friend_in_friendlist(friends[fc]);
                        }
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
        // now run thru the list again to account for relays ----------------


    }

    static void try_update_friend_in_friendlist(long friendnum)
    {
        FriendList f_check;
        List<FriendList> fl_check = orma.selectFromFriendList().tox_public_key_stringEq(
                tox_friend_get_public_key__wrapper(friendnum)).toList();
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
                    cc.is_friend = COMBINED_IS_FRIEND;
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

                // final ConferenceDB conf2 = orma.selectFromConferenceDB().toList().get(0);
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

    void load_and_add_all_groups()
    {
        long num_groups = tox_group_get_number_groups();
        Log.i(TAG, "load groups at startup: num=" + num_groups);

        long[] group_numbers = tox_group_get_grouplist();
        ByteBuffer groupid_buf3 = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);

        int conf_ = 0;
        for (conf_ = 0; conf_ < num_groups; conf_++)
        {
            groupid_buf3.clear();

            if (tox_group_get_chat_id(group_numbers[conf_], groupid_buf3) == 0)
            {
                byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                groupid_buf3.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                String group_identifier = bytes_to_hex(groupid_buffer);
                int is_connected = tox_group_is_connected(conf_);
                Log.i(TAG, "load group num=" + group_numbers[conf_] + " connected=" + is_connected);

                new_or_updated_group(group_numbers[conf_], tox_friend_get_public_key__wrapper(0), group_identifier,
                                     tox_group_get_privacy_state(group_numbers[conf_]));

                String group_name = tox_group_get_name(group_numbers[conf_]);
                if (group_name == null)
                {
                    group_name = "";
                }
                update_group_in_db_name(group_identifier, group_name);

                final int new_privacy_state = tox_group_get_privacy_state(group_numbers[conf_]);
                update_group_in_db_privacy_state(group_identifier, new_privacy_state);

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
            }
        }
    }

    static void write_debug_file(String filename)
    {
        if (DEBUG_BATTERY_OPTIMIZATION_LOGGING)
        {
            try
            {
                Log.d("BATTOPTDEBUG", "" + filename);

                File dir = new File(SD_CARD_FILES_DEBUG_DIR);
                dir.mkdirs();
                String filename2 = long_date_time_format(System.currentTimeMillis()) + "_" + filename;
                File file = new File(dir, filename2);

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
                    this.setName("tox_iterate()");
                }
                catch (Exception e)
                {
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
                    set_all_filetransfers_inactive();
                    MainActivity.init_tox_callbacks();
                    HelperGeneric.update_savedata_file_wrapper();
                }
                // ------ correct startup order ------

                cache_pubkey_fnum.clear();
                cache_fnum_pubkey.clear();
                cache_confid_confnum.clear();

                tox_self_capabilites = tox_self_get_capabilities();
                //Log.i(TAG, "tox_self_capabilites:" + tox_self_capabilites + " decoded:" +
                //           TOX_CAPABILITY_DECODE_TO_STRING(TOX_CAPABILITY_DECODE(tox_self_capabilites)) + " " +
                //           (1L << 63L));

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


                // ----- convert old NULL's into 0 -----
                try
                {
                    orma.getConnection().execSQL("update Message set sent_push='0' where sent_push is NULL");
                    Log.i(TAG, "onCreate:sent_push");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:sent_push:EE01");
                }
                // ----- convert old NULL's into 0 -----

                // ----- convert old NULL's into 0 -----
                try
                {
                    orma.getConnection().execSQL(
                            "update FriendList set msgv3_capability='0' where msgv3_capability is NULL");
                    Log.i(TAG, "onCreate:msgv3_capability");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:msgv3_capability:EE01");
                }
                // ----- convert old NULL's into 0 -----

                // ----- convert old NULL's into 0 -----
                try
                {
                    orma.getConnection().execSQL(
                            "update Message set filetransfer_kind='0' where filetransfer_kind is NULL");
                    Log.i(TAG, "onCreate:filetransfer_kind");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:filetransfer_kind:EE01");
                }
                // ----- convert old NULL's into 0 -----

                // ----- convert old NULL's into 0 -----
                try
                {
                    orma.getConnection().execSQL(
                            "update GroupMessage set TRIFA_SYNC_TYPE='0' where TRIFA_SYNC_TYPE is NULL");
                    Log.i(TAG, "onCreate:TRIFA_SYNC_TYPE");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:TRIFA_SYNC_TYPE:EE01");
                }
                // ----- convert old NULL's into 0 -----

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
                        TrifaToxService.write_debug_file("STARTUP__start__bootstrapping");
                        bootstrap_me();
                        TrifaToxService.write_debug_file("STARTUP__finish__bootstrapping");
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

                        FriendList f_echobot = main_get_friend(ECHOBOT_TOXID.substring(0, 32 * 2).toUpperCase());
                        if (f_echobot != null)
                        {
                            f_echobot.status_message = ECHOBOT_INIT_STATUSMSG;
                            f_echobot.name = ECHOBOT_INIT_NAME;
                            HelperFriend.update_friend_in_db_name(f_echobot);
                            HelperFriend.update_friend_in_db_status_message(f_echobot);
                            HelperFriend.update_single_friend_in_friendlist_view(f_echobot);
                        }

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

                try
                {
                    load_and_add_all_groups();
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
                                TrifaToxService.write_debug_file(
                                        "BATTERY_SAVINGS_MODE__enter:" + tox_self_get_connection_status());

                                // try to fix endless bootstraping (on yellow) bug ----------------
                                if (tox_self_get_connection_status() == 0)
                                {
                                    final int millis_sleep = 100;
                                    final int seconds_for_bootstrapping = 10;
                                    TrifaToxService.write_debug_file(
                                            "BATTERY_SAVINGS_MODE__start_wait_for_bootstrapping:" +
                                            tox_self_get_connection_status());
                                    for (int ii = 0; ii < ((seconds_for_bootstrapping * 1000) / millis_sleep); ii++)
                                    {
                                        MainActivity.tox_iterate();
                                        try
                                        {
                                            Thread.sleep(millis_sleep);
                                        }
                                        catch (Exception e)
                                        {
                                        }
                                    }
                                    TrifaToxService.write_debug_file(
                                            "BATTERY_SAVINGS_MODE__done_wait_for_bootstrapping:" +
                                            tox_self_get_connection_status());
                                }
                                // try to fix endless bootstraping (on yellow) bug ----------------

                                long current_timestamp_ = System.currentTimeMillis();
                                global_self_last_entered_battery_saving_timestamp = current_timestamp_;

                                trifa_service_thread = Thread.currentThread();

                                // ---------------------------------------------------------
                                Intent intent_wakeup = new Intent(getApplicationContext(), WakeupAlarmReceiver.class);
                                // intentWakeFullBroacastReceiver.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1001,
                                                                                       intent_wakeup,
                                                                                       PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_IMMUTABLE);
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
                                // load groups again
                                try
                                {
                                    load_and_add_all_groups();
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
                                global_self_connection_status = TOX_CONNECTION_a;
                                if (TOX_CONNECTION_a == TOX_CONNECTION_NONE.value)
                                {
                                    bootstrapping = true;
                                    global_self_last_went_offline_timestamp = System.currentTimeMillis();
                                    Log.i(TAG, "BATTERY SAVINGS MODE, bootstrapping");
                                    tox_notification_change_wrapper(TOX_CONNECTION_a,
                                                                    ""); // set to real connection status
                                    TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__start__bootstrapping");
                                    bootstrap_me();
                                    TrifaToxService.write_debug_file("BATTERY_SAVINGS_MODE__finish__bootstrapping:" +
                                                                     tox_self_get_connection_status());
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
                        check_if_need_bootstrap_again();
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

                    /*
                    int w = 240;
                    int h = 320;
                    int y_bytes = w * h;
                    int u_bytes = (w * h) / 4;
                    int v_bytes = (w * h) / 4;
                    byte[] y_buf = new byte[y_bytes];
                    byte[] u_buf = new byte[u_bytes];
                    byte[] v_buf = new byte[v_bytes];
                    byte[] encoded_vframe = new byte[40000];
                    int encoded_bytes = toxav_ngc_video_encode(300, 240,320,
                                                               y_buf, y_bytes,
                                                               u_buf, u_bytes,
                                                               v_buf, v_bytes,
                                                               encoded_vframe);
                    Log.i(TAG, "toxav_ngc_video_encode:bytes=" + encoded_bytes);
                    int w2 = 240 + 16; // encoder stride added
                    int h2 = 320;
                    int y_bytes2 = w2 * h2;
                    int u_bytes2 = (w2 * h2) / 4;
                    int v_bytes2 = (w2 * h2) / 4;
                    byte[] y_buf2 = new byte[y_bytes2];
                    byte[] u_buf2 = new byte[u_bytes2];
                    byte[] v_buf2 = new byte[v_bytes2];
                    int ystride = toxav_ngc_video_decode(encoded_vframe,
                                           encoded_bytes,
                                           w2, h2,
                                           y_buf2,
                                           u_buf2,
                                           v_buf2);
                    Log.i(TAG, "toxav_ngc_video_decode:ystride=" + ystride);
                    */

                    if ((Callstate.state != 0) || (Callstate.audio_group_active))
                    {
                        if (Callstate.audio_group_active)
                        {
                            tox_iteration_interval_ms = 5; // if we are in a group audio call iterate more often
                            // Log.i(TAG, "(tox_iteration_interval_ms):001");
                        }
                        else
                        {
                            tox_iteration_interval_ms = 10; // if we are in a video/audio call iterate more often
                            // Log.i(TAG, "(tox_iteration_interval_ms):002");
                        }
                    }
                    else
                    {
                        if (global_last_activity_outgoung_ft_ts > -1)
                        {
                            if ((global_last_activity_outgoung_ft_ts + 200) > System.currentTimeMillis())
                            {
                                // iterate faster if outgoing filetransfers are active
                                tox_iteration_interval_ms = 5;
                                // Log.i(TAG, "(tox_iteration_interval_ms):004");
                            }
                            else
                            {
                                tox_iteration_interval_ms = tox_iteration_interval();
                            }
                        }
                        else if (global_last_activity_incoming_ft_ts > -1)
                        {
                            if ((global_last_activity_incoming_ft_ts + 200) > System.currentTimeMillis())
                            {
                                // iterate faster if incoming filetransfers are active
                                tox_iteration_interval_ms = 5;
                                // Log.i(TAG, "(tox_iteration_interval_ms):005");
                            }
                            else
                            {
                                tox_iteration_interval_ms = tox_iteration_interval();
                            }
                        }
                        else
                        {
                            // tox_iteration_interval_ms = Math.max(100, MainActivity.tox_iteration_interval());
                            tox_iteration_interval_ms = tox_iteration_interval();
                            // Log.i(TAG, "tox_iteration_interval_ms:006=" + tox_iteration_interval_ms);
                        }

                        if (tox_iteration_interval_ms == 50)
                        {
                            // HINT: when nothing special is happening, iterate less often to save battery
                            tox_iteration_interval_ms = 100;
                            // Log.i(TAG, "tox_iteration_interval_ms:007=" + tox_iteration_interval_ms);
                        }
                    }

                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {
                        send_or_resend_pending_messages();
                    }

                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {
                        start_queued_filetransfers();
                    }
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

    private void send_or_resend_pending_messages()
    {
        if ((last_resend_pending_messages4_ms + (5 * 1000)) < System.currentTimeMillis())
        {
            last_resend_pending_messages4_ms = System.currentTimeMillis();
            resend_push_for_v3_messages();
        }

        if ((last_resend_pending_messages0_ms + (30 * 1000)) < System.currentTimeMillis())
        {
            last_resend_pending_messages0_ms = System.currentTimeMillis();
            resend_old_messages(null);
        }

        if ((last_resend_pending_messages1_ms + (30 * 1000)) < System.currentTimeMillis())
        {
            last_resend_pending_messages1_ms = System.currentTimeMillis();
            resend_v3_messages(null);
        }

        if ((last_resend_pending_messages2_ms + (30 * 1000)) < System.currentTimeMillis())
        {
            last_resend_pending_messages2_ms = System.currentTimeMillis();
            resend_v2_messages(false);
        }

        if ((last_resend_pending_messages3_ms + (120 * 1000)) < System.currentTimeMillis())
        {
            last_resend_pending_messages3_ms = System.currentTimeMillis();
            resend_v2_messages(true);
        }
    }

    private void start_queued_filetransfers()
    {
        if ((last_start_queued_fts_ms + (4 * 1000)) < System.currentTimeMillis())
        {
            last_start_queued_fts_ms = System.currentTimeMillis();
            // Log.i(TAG, "start_queued_outgoing_FTs ============================================");

            try
            {
                List<Message> m_v1 = orma.selectFromMessage().
                        directionEq(1).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                        ft_outgoing_queuedEq(true).
                        stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                        orderBySent_timestampAsc().
                        toList();

                // Log.i(TAG, "start_queued_outgoing_FTs:000:" + m_v1);

                if ((m_v1 != null) && (m_v1.size() > 0))
                {
                    // Log.i(TAG, "start_queued_outgoing_FTs:001:" + m_v1.size());

                    Iterator<Message> ii = m_v1.iterator();
                    while (ii.hasNext())
                    {
                        Message m_resend_ft = ii.next();

                        if (is_friend_online_real(tox_friend_by_public_key__wrapper(m_resend_ft.tox_friendpubkey)) != 0)
                        {
                            start_outgoing_ft(m_resend_ft);
                        }
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }
    }

    private void check_if_need_bootstrap_again()
    {
        if (global_self_connection_status == TOX_CONNECTION_NONE.value)
        {
            if (HAVE_INTERNET_CONNECTIVITY)
            {
                if (global_self_last_went_offline_timestamp != -1)
                {
                    if (global_self_last_went_offline_timestamp + TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS <
                        System.currentTimeMillis())
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
                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP1 + "/" +
                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP2 + "/" +
                                                                        BATTERY_OPTIMIZATION_LAST_SLEEP3 + ") " +
                                                                        long_date_time_format_or_empty(
                                                                                global_self_last_entered_battery_saving_timestamp)); // set notification to "bootstrapping"
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            TrifaToxService.write_debug_file("RUN__start__bootstrapping");
                            bootstrap_me();
                            TrifaToxService.write_debug_file(
                                    "RUN__finish__bootstrapping:" + tox_self_get_connection_status());
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

    void start_me()
    {
        Log.i(TAG, "start_me");
        notification2 = tox_notification_setup(this, nmn2);
        startForeground(HelperToxNotification.ONGOING_NOTIFICATION_ID, notification2);
    }

    static void bootstap_from_custom_nodes()
    {
        try
        {
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context_s);
            final String bs_udp_ip = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP, "");
            final String bs_udp_port = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT, "");
            final String bs_udp_keyhex = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX, "");
            final String bs_tcp_ip = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP, "");
            final String bs_tcp_port = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT, "");
            final String bs_tcp_keyhex = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX, "");

            if ((bs_udp_ip.length() > 0) && (bs_udp_port.length() > 0) && (IPisValid(bs_udp_ip)) &&
                (isIPPortValid(bs_udp_port)) && (is_valid_tox_public_key(bs_udp_keyhex)))
            {
                Log.i(TAG, "bootstap_from_custom_nodes:bootstrap_single:ip=" + bs_udp_ip + " port=" +
                           Integer.parseInt(bs_udp_port) + " key=" + bs_udp_keyhex.toUpperCase());
                int bootstrap_result = bootstrap_single_wrapper(bs_udp_ip, Integer.parseInt(bs_udp_port),
                                                                bs_udp_keyhex.toUpperCase());
                Log.i(TAG, "bootstap_from_custom_nodes:bootstrap_single:res=" + bootstrap_result);
            }

            if ((bs_tcp_ip.length() > 0) && (bs_tcp_port.length() > 0) && (IPisValid(bs_tcp_ip)) &&
                (isIPPortValid(bs_tcp_port)) && (is_valid_tox_public_key(bs_tcp_keyhex)))
            {
                Log.i(TAG, "bootstap_from_custom_nodes:add_tcp_relay_single:ip=" + bs_tcp_ip + " port=" +
                           Integer.parseInt(bs_tcp_port) + " key=" + bs_tcp_keyhex.toUpperCase());
                int bootstrap_result = HelperGeneric.add_tcp_relay_single_wrapper(bs_tcp_ip,
                                                                                  Integer.parseInt(bs_tcp_port),
                                                                                  bs_tcp_keyhex.toUpperCase());
                Log.i(TAG, "bootstap_from_custom_nodes:add_tcp_relay_single:res=" + bootstrap_result);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "bootstap_from_custom_nodes:EE01:" + e.getMessage());
        }
    }

    static void bootstrap_me()
    {
        Log.i(TAG, "bootstrap_me");

        bootstap_from_custom_nodes();

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
                    // Log.i(TAG, "bootstrap_single:++:used=" + used);
                }

                if (used >= USE_MAX_NUMBER_OF_BOOTSTRAP_NODES)
                {
                    Log.i(TAG, "bootstrap_single:break:used=" + used);
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
                            // Log.i(TAG, "add_tcp_relay_single:++:used=" + used);
                        }

                        if (used >= USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS)
                        {
                            Log.i(TAG, "add_tcp_relay_single:break:used=" + used);
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

    static void resend_push_for_v3_messages()
    {
        try
        {
            if (!PREF__use_push_service)
            {
                return;
            }

            // HINT: if we have not received a "read receipt" for msgV3 within 10 seconds, then we trigger a push again
            final long cutoff_sent_time = System.currentTimeMillis() - (10 * 1000);

            List<Message> m_push = orma.selectFromMessage().
                    directionEq(1).
                    msg_versionEq(0).
                    TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                    sent_pushEq(0).
                    readEq(false).
                    orderBySent_timestampAsc().
                    sent_timestampLt(cutoff_sent_time).
                    toList();

            if ((m_push != null) && (m_push.size() > 0))
            {
                Iterator<Message> ii = m_push.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_push = ii.next();
                    if ((m_resend_push.msg_idv3_hash != null) && (m_resend_push.msg_idv3_hash.length() > 3))
                    {
                        friend_call_push_url(m_resend_push.tox_friendpubkey, m_resend_push.sent_timestamp);
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "resend_push_for_v3_messages:EE:" + e.getMessage());
        }
    }

    static void resend_v3_messages(final String friend_pubkey)
    {
        // loop through "old msg version" msgV3 1-on-1 text messages that have "resend_count < MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION" --------------
        try
        {
            int max_resend_count_per_iteration = 20;

            if (friend_pubkey != null)
            {
                max_resend_count_per_iteration = 20;
            }

            int cur_resend_count_per_iteration = 0;

            List<Message> m_v1 = null;
            if (friend_pubkey != null)
            {
                m_v1 = orma.selectFromMessage().
                        directionEq(1).
                        msg_versionEq(0).
                        tox_friendpubkeyEq(friend_pubkey).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).
                        readEq(false).
                        orderBySent_timestampAsc().
                        toList();
            }
            else
            {
                m_v1 = orma.selectFromMessage().
                        directionEq(1).
                        msg_versionEq(0).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).
                        readEq(false).
                        orderBySent_timestampAsc().
                        toList();
            }

            if ((m_v1 != null) && (m_v1.size() > 0))
            {
                Iterator<Message> ii = m_v1.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v1 = ii.next();
                    if (friend_pubkey == null)
                    {
                        if (is_friend_online_real(tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey)) == 0)
                        {
                            continue;
                        }
                    }

                    if (get_friend_msgv3_capability(m_resend_v1.tox_friendpubkey) != 1)
                    {
                        continue;
                    }

                    tox_friend_resend_msgv3_wrapper(m_resend_v1);
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
            Log.i(TAG, "resend_v3_messages:EE:" + e.getMessage());
        }
        // loop through all pending outgoing 1-on-1 text messages --------------
    }

    static void resend_old_messages(final String friend_pubkey)
    {
        try
        {
            int max_resend_count_per_iteration = 10;

            if (friend_pubkey != null)
            {
                max_resend_count_per_iteration = 20;
            }

            int cur_resend_count_per_iteration = 0;

            // HINT: cutoff time "now" minus 25 seconds
            final long cutoff_sent_time = System.currentTimeMillis() - (25 * 1000);
            List<Message> m_v0 = null;

            if (friend_pubkey != null)
            {
                // HINT: this is the generic resend for all friends, that happens in regular intervals
                //       only resend if the original sent timestamp is at least 25 seconds in the past
                //       to try to avoid resending when the read receipt is very late.
                m_v0 = orma.selectFromMessage().
                        directionEq(1).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        msg_versionEq(0).
                        tox_friendpubkeyEq(friend_pubkey).
                        readEq(false).
                        resend_countLt(2).
                        orderBySent_timestampAsc().
                        sent_timestampLt(cutoff_sent_time).
                        toList();
            }
            else
            {
                // HINT: this is the specific resend for 1 friend only, when that friend comes online
                m_v0 = orma.selectFromMessage().
                        directionEq(1).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        msg_versionEq(0).
                        readEq(false).
                        resend_countLt(2).
                        orderBySent_timestampAsc().
                        toList();
            }

            if ((m_v0 != null) && (m_v0.size() > 0))
            {
                Iterator<Message> ii = m_v0.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v0 = ii.next();

                    if (friend_pubkey == null)
                    {
                        if (is_friend_online_real(tox_friend_by_public_key__wrapper(m_resend_v0.tox_friendpubkey)) == 0)
                        {
                            // Log.i(TAG, "resend_old_messages:RET:01:" +
                            //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                            continue;
                        }
                    }

                    if (get_friend_msgv3_capability(m_resend_v0.tox_friendpubkey) == 1)
                    {
                        // Log.i(TAG, "resend_old_messages:RET:02:" +
                        //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                        continue;
                    }

                    // Log.i(TAG, "resend_old_messages:tox_friend_resend_msgv3_wrapper:" + m_resend_v0.text + " : m=" +
                    //            m_resend_v0 + " : " + get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                    tox_friend_resend_msgv3_wrapper(m_resend_v0);

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
        }
    }

    static void resend_v2_messages(boolean at_relay)
    {
        // loop through all pending outgoing 1-on-1 text messages V2 (resend) --------------
        try
        {
            final int max_resend_count_per_iteration = 10;
            int cur_resend_count_per_iteration = 0;

            List<Message> m_v1 = orma.selectFromMessage().
                    directionEq(1).
                    TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                    msg_versionEq(1).
                    readEq(false).
                    msg_at_relayEq(at_relay).
                    orderBySent_timestampAsc().
                    toList();


            if ((m_v1 != null) && (m_v1.size() > 0))
            {
                Iterator<Message> ii = m_v1.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v2 = ii.next();

                    if (is_friend_online(tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey)) == 0)
                    {
                        continue;
                    }

                    if ((m_resend_v2.msg_id_hash == null) ||
                        (m_resend_v2.msg_id_hash.equalsIgnoreCase(""))) // resend msgV2 WITHOUT hash
                    {
                        // Log.i(TAG, "resend_msgV2_WITHOUT_hash:f=" +
                        //           get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " m=" + m_resend_v2);
                        MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                                m_resend_v2.tox_friendpubkey, 0, m_resend_v2.text, (m_resend_v2.sent_timestamp / 1000));

                        if (result != null)
                        {
                            long res = result.msg_num;

                            if (res > -1)
                            {
                                m_resend_v2.resend_count = 1; // we sent the message successfully
                                m_resend_v2.message_id = res;
                            }
                            else
                            {
                                m_resend_v2.resend_count = 0; // sending was NOT successfull
                                m_resend_v2.message_id = -1;
                            }

                            if (result.msg_v2)
                            {
                                m_resend_v2.msg_version = 1;
                            }
                            else
                            {
                                m_resend_v2.msg_version = 0;
                            }

                            if ((result.msg_hash_hex != null) && (!result.msg_hash_hex.equalsIgnoreCase("")))
                            {
                                // msgV2 message -----------
                                m_resend_v2.msg_id_hash = result.msg_hash_hex;
                                // msgV2 message -----------
                            }

                            if ((result.msg_hash_v3_hex != null) && (!result.msg_hash_v3_hex.equalsIgnoreCase("")))
                            {
                                // msgV3 message -----------
                                m_resend_v2.msg_idv3_hash = result.msg_hash_v3_hex;
                                // msgV3 message -----------
                            }

                            if ((result.raw_message_buf_hex != null) &&
                                (!result.raw_message_buf_hex.equalsIgnoreCase("")))
                            {
                                // save raw message bytes of this v2 msg into the database
                                // we need it if we want to resend it later
                                m_resend_v2.raw_msgv2_bytes = result.raw_message_buf_hex;
                            }

                            update_message_in_db_messageid(m_resend_v2);
                            update_message_in_db_resend_count(m_resend_v2);
                            update_message_in_db_no_read_recvedts(m_resend_v2);
                        }
                    }
                    else // resend msgV2 with hash
                    {
                        final int raw_data_length = (m_resend_v2.raw_msgv2_bytes.length() / 2);
                        byte[] raw_msg_resend_data = hex_to_bytes(m_resend_v2.raw_msgv2_bytes);

                        ByteBuffer msg_text_buffer_resend_v2 = ByteBuffer.allocateDirect(raw_data_length);
                        msg_text_buffer_resend_v2.put(raw_msg_resend_data, 0, raw_data_length);

                        int res = tox_util_friend_resend_message_v2(
                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey),
                                msg_text_buffer_resend_v2, raw_data_length);


                        String relay = get_relay_for_friend(m_resend_v2.tox_friendpubkey);
                        if (relay != null)
                        {
                            int res_relay = tox_util_friend_resend_message_v2(tox_friend_by_public_key__wrapper(relay),
                                                                              msg_text_buffer_resend_v2,
                                                                              raw_data_length);

                        }
                    }

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
        }
        // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------
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

    /*
     * This is called by native methods to check/fix broken UTF-8 Strings
     */
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
