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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;

import info.guardianproject.iocipher.VirtualFileSystem;

import static com.zoffcc.applications.trifa.MainActivity.add_friend_real;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.change_notification;
import static com.zoffcc.applications.trifa.MainActivity.get_g_opts;
import static com.zoffcc.applications.trifa.MainActivity.get_my_toxid;
import static com.zoffcc.applications.trifa.MainActivity.notification_view;
import static com.zoffcc.applications.trifa.MainActivity.set_all_friends_offline;
import static com.zoffcc.applications.trifa.MainActivity.set_g_opts;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_service_fg;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ADD_BOTS_ON_STARTUP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUPBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;

public class TrifaToxService extends Service
{
    static int ONGOING_NOTIFICATION_ID = 1030;
    static final String TAG = "trifa.ToxService";
    Notification notification2 = null;
    NotificationManager nmn2 = null;
    static Thread ToxServiceThread = null;
    static boolean stop_me = false;
    static OrmaDatabase orma = null;
    static VirtualFileSystem vfs = null;
    static boolean is_tox_started = false;
    static boolean global_toxid_text_set = false;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        // this gets called all the time!
        tox_service_fg = this;
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        // serivce is created ---
        start_me();
        super.onCreate();
    }

    void change_notification_fg(int a_TOXCONNECTION)
    {
        Log.i(TAG, "change_notification_fg");

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (bootstrapping)
        {
            Log.i(TAG, "change_notification_fg:bootstrapping=true");
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_orange);
            b.setSmallIcon(R.drawable.circle_orange);
            notification_view.setTextViewText(R.id.title, "Tox Service: " + "Bootstrapping");
        }
        else
        {
            Log.i(TAG, "change_notification_fg:bootstrapping=FALSE");

            if (a_TOXCONNECTION == 0)
            {
                notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
                b.setSmallIcon(R.drawable.circle_red);
                notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE");
            }
            else
            {
                if (a_TOXCONNECTION == 1)
                {
                    notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                    b.setSmallIcon(R.drawable.circle_green);
                    notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [TCP]");
                }
                else // if (a_TOXCONNECTION__f == 2)
                {
                    notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                    b.setSmallIcon(R.drawable.circle_green);
                    notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [UDP]");
                }
            }
        }
        notification_view.setTextViewText(R.id.text, "");

        b.setContentIntent(pendingIntent);
        b.setContent(notification_view);
        notification2 = b.build();
        nmn2.notify(ONGOING_NOTIFICATION_ID, notification2);
    }


    void stop_me(boolean exit_app)
    {
        Log.i(TAG, "stop_me:001");

        stopForeground(true);
        try
        {
            Log.i(TAG, "stop_me:002");
            nmn2.cancel(ONGOING_NOTIFICATION_ID);
            Log.i(TAG, "stop_me:003");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "stop_me:EEn1" + e.getMessage());
        }

        if (exit_app)
        {
            try
            {
                Log.i(TAG, "stop_me:004");
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Log.i(TAG, "stop_me:005");
                        long i = 0;
                        while (is_tox_started)
                        {
                            i++;
                            if (i > 40)
                            {
                                break;
                            }

                            Log.i(TAG, "stop_me:006");

                            try
                            {
                                Thread.sleep(200);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "stop_me:006a");

                        try
                        {
                            if (vfs.isMounted())
                            {
                                Log.i(TAG, "VFS:unmount:start");
                                try
                                {
                                    Log.i(TAG, "stop_me:vfs:sleep:001");
                                    Thread.sleep(2500);
                                    Log.i(TAG, "stop_me:vfs:sleep:002");
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                                vfs.unmount();
                                Log.i(TAG, "VFS:unmount:OK");
                            }
                            else
                            {
                                Log.i(TAG, "VFS:unmount:NOT MOUNTED");
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "VFS:unmount:EE:" + e.getMessage());
                        }

                        Log.i(TAG, "stop_me:007");

                        try
                        {
                            Log.i(TAG, "stop_me:008");
                            nmn2.cancel(ONGOING_NOTIFICATION_ID);
                            Log.i(TAG, "stop_me:009");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "stop_me:EEn2" + e.getMessage());
                        }

                        Log.i(TAG, "stop_me:010");
                        stopSelf();
                        Log.i(TAG, "stop_me:011");

                        try
                        {
                            Log.i(TAG, "stop_me:012");
                            Thread.sleep(1500);
                            Log.i(TAG, "stop_me:013");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

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

    void stop_tox_fg()
    {
        Log.i(TAG, "stop_tox_fg:001");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "stop_tox_fg:002");
                stop_me = true;
                ToxServiceThread.interrupt();
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
                change_notification(0); // set to offline
                Log.i(TAG, "stop_tox_fg:008");
                set_all_friends_offline();
                is_tox_started = false;

                Log.i(TAG, "stop_tox_fg:009");

                // nmn2.cancel(ONGOING_NOTIFICATION_ID); // -> cant remove notification because of foreground service
                // ** // MainActivity.exit();
            }
        };
        MainActivity.main_handler_s.post(myRunnable);

        Log.i(TAG, "stop_tox_fg:099");
    }

    void tox_thread_start_fg()
    {
        Log.i(TAG, "tox_thread_start_fg");

        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {
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
                            MainActivity.mt.setText(MainActivity.mt.getText() + "\n" + "my_ToxId=" + get_my_toxid());
                        }
                    }
                };
                MainActivity.main_handler_s.post(myRunnable);

                if (!old_is_tox_started)
                {
                    MainActivity.init_tox_callbacks();
                    MainActivity.update_savedata_file();
                }
                // ------ correct startup order ------

                cache_pubkey_fnum.clear();
                cache_fnum_pubkey.clear();

                // TODO --------
                String my_tox_id_local = get_my_toxid();
                global_my_toxid = my_tox_id_local;
                if (tox_self_get_name_size() > 0)
                {
                    global_my_name = tox_self_get_name().substring(0, (int) tox_self_get_name_size());
                    Log.i(TAG, "AAA:003:" + global_my_name + " size=" + tox_self_get_name_size());
                }
                else
                {
                    tox_self_set_name("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5, my_tox_id_local.length()));
                    global_my_name = ("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5, my_tox_id_local.length()));
                    Log.i(TAG, "AAA:005");
                }

                if (tox_self_get_status_message_size() > 0)
                {
                    global_my_status_message = tox_self_get_status_message().substring(0, (int) tox_self_get_status_message_size());
                    Log.i(TAG, "AAA:008:" + global_my_status_message + " size=" + tox_self_get_status_message_size());
                }
                else
                {
                    tox_self_set_status_message("this is TRIfA");
                    global_my_status_message = "this is TRIfA";
                    Log.i(TAG, "AAA:010");
                }
                Log.i(TAG, "AAA:011");

                MainActivity.update_savedata_file();

                // TODO --------

                MainActivity.friends = MainActivity.tox_self_get_friend_list();
                Log.i(TAG, "loading_friend:number_of_friends=" + MainActivity.friends.length);

                int fc = 0;
                boolean exists_in_db = false;
                MainActivity.friend_list_fragment.clear_friends();
                for (fc = 0; fc < MainActivity.friends.length; fc++)
                {
                    Log.i(TAG, "loading_friend:" + fc + " friendnum=" + MainActivity.friends[fc]);
                    Log.i(TAG, "loading_friend:" + fc + " pubkey=" + tox_friend_get_public_key__wrapper(MainActivity.friends[fc]));

                    FriendList f;
                    List<FriendList> fl = orma.selectFromFriendList().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();

                    Log.i(TAG, "loading_friend:" + fc + " db entry size=" + fl);

                    if (fl.size() > 0)
                    {
                        f = fl.get(0);
                        Log.i(TAG, "loading_friend:" + fc + " db entry=" + f);
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
                        Log.i(TAG, "loading_friend:c is null fnew=" + f);
                    }
                    else
                    {
                        Log.i(TAG, "loading_friend:found friend in DB " + f.tox_public_key_string + " f=" + f);
                        exists_in_db = true;
                    }

                    try
                    {
                        // get the real "live" connection status of this friend
                        // the value in the database may be old (and wrong)
                        f.TOX_CONNECTION = tox_friend_get_connection_status(MainActivity.friends[fc]);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (MainActivity.friend_list_fragment != null)
                    {
                        MainActivity.friend_list_fragment.add_friends(f);
                    }

                    if (exists_in_db == false)
                    {
                        Log.i(TAG, "loading_friend:1:insertIntoFriendList:" + " f=" + f);
                        orma.insertIntoFriendList(f);
                        Log.i(TAG, "loading_friend:2:insertIntoFriendList:" + " f=" + f);
                    }
                    else
                    {
                        Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
                        orma.updateFriendList().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).name(f.name).status_message(f.status_message).TOX_CONNECTION(f.TOX_CONNECTION).TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                        Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
                    }

                    FriendList f_check;
                    List<FriendList> fl_check = orma.selectFromFriendList().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();
                    Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check);
                    try
                    {
                        Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check.get(0));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "loading_friend:check:EE:" + e.getMessage());
                    }

                }

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                if (!old_is_tox_started)
                {
                    bootstrapping = true;
                    Log.i(TAG, "bootrapping:set to true");
                    try
                    {
                        tox_service_fg.change_notification_fg(0); // set notification to "bootstrapping"
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // ----- UDP ------
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("178.62.250.138", 33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("nodes.tox.chat", 33445, "6FC41E2BD381D37E9748FC0E0328CE086AF9598BECC8FEB7DDF2E440475F300E"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("130.133.110.14", 33445, "461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("tox.zodiaclabs.org", 33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("163.172.136.118", 33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("217.182.143.254", 443, "7AED21F94D82B05774F697B209628CD5A9AD17E0C073D9329076A4C28ED28147"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("185.14.30.213", 443, "2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("136.243.141.187", 443, "6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("128.199.199.197", 33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("biribiri.org", 33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67"));
                    // ----- UDP ------
                    //
                    // ----- TCP ------
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("178.62.250.138", 33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("nodes.tox.chat", 33445, "6FC41E2BD381D37E9748FC0E0328CE086AF9598BECC8FEB7DDF2E440475F300E"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("130.133.110.14", 33445, "461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("tox.zodiaclabs.org", 33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("163.172.136.118", 33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("217.182.143.254", 443, "7AED21F94D82B05774F697B209628CD5A9AD17E0C073D9329076A4C28ED28147"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("185.14.30.213", 443, "2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("136.243.141.187", 443, "6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("128.199.199.197", 33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09"));
                    Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("biribiri.org", 33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67"));
                    // ----- TCP ------
                }

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------

                long tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                MainActivity.tox_iterate();

                if (ADD_BOTS_ON_STARTUP)
                {
                    boolean need_add_bots = true;

                    try
                    {
                        if (get_g_opts("ADD_BOTS_ON_STARTUP_done").equals("true"))
                        {
                            need_add_bots = false;
                            Log.i(TAG, "need_add_bots=false");
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
                        add_friend_real(GROUPBOT_TOXID);
                        set_g_opts("ADD_BOTS_ON_STARTUP_done", "true");
                        Log.i(TAG, "need_add_bots=true (INSERT)");
                    }
                }


                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                while (!stop_me)
                {
                    try
                    {
                        if (tox_iteration_interval_ms < 10)
                        {
                            Log.i(TAG, "(tox_iteration_interval_ms < 10ms!!):" + tox_iteration_interval_ms + "ms");
                            Thread.sleep(10);
                        }
                        else
                        {
                            // Log.i(TAG, "(tox_iteration_interval_ms):" + tox_iteration_interval_ms + "ms");
                            Thread.sleep(tox_iteration_interval_ms);
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

                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------


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

            }
        };

        ToxServiceThread.start();
    }

    void start_me()
    {
        Log.i(TAG, "start_me");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // -- notification ------------------
        // -- notification ------------------
        nmn2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification_view = new RemoteViews(getPackageName(), R.layout.custom_notification);
        Log.i(TAG, "contentView=" + notification_view);
        notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
        notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE");
        notification_view.setTextViewText(R.id.text, "");

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setContent(notification_view);
        b.setContentIntent(pendingIntent);
        b.setSmallIcon(R.drawable.circle_red);
        notification2 = b.build();
        // -- notification ------------------
        // -- notification ------------------

        startForeground(ONGOING_NOTIFICATION_ID, notification2);
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

}
