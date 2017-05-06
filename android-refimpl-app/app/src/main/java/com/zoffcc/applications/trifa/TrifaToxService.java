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

import static com.zoffcc.applications.trifa.MainActivity.change_notification;
import static com.zoffcc.applications.trifa.MainActivity.notification_view;
import static com.zoffcc.applications.trifa.MainActivity.set_all_friends_offline;

public class TrifaToxService extends Service
{
    static int ONGOING_NOTIFICATION_ID = 1030;
    static final String TAG = "trifa.ToxService";
    Notification notification2 = null;
    NotificationManager nmn2 = null;
    static Thread ToxServiceThread = null;
    static boolean stop_me = false;
    static OrmaDatabase orma = null;
    static boolean is_tox_started = false;
    static boolean global_toxid_text_set=false;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        // this gets called all the time!
        MainActivity.tox_service_fg = this;
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
        notification_view.setTextViewText(R.id.text, "");

        b.setContentIntent(pendingIntent);
        b.setContent(notification_view);
        notification2 = b.build();
        nmn2.notify(ONGOING_NOTIFICATION_ID, notification2);
    }


    void stop_me()
    {
        Log.i(TAG, "stop_me");
        stopSelf();
    }

    void stop_tox_fg()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                stop_me = true;
                ToxServiceThread.interrupt();
                try
                {
                    ToxServiceThread.join();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                stop_me = false; // reset flag again!
                change_notification(0); // set to offline
                set_all_friends_offline();
                is_tox_started = false;

                // nmn2.cancel(ONGOING_NOTIFICATION_ID); // -> cant remove notification because of foreground service
                // ** // MainActivity.exit();
            }
        };
        MainActivity.main_handler_s.post(myRunnable);
    }

    void tox_thread_start_fg()
    {
        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {
                // ------ correct startup order ------
                boolean old_is_tox_started = is_tox_started;
                Log.i(TAG,"is_tox_started:==============================");
                Log.i(TAG,"is_tox_started="+is_tox_started);
                Log.i(TAG,"is_tox_started:==============================");

                is_tox_started = true;
                if (!old_is_tox_started)
                {
                    MainActivity.bootstrap();
                }

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!global_toxid_text_set)
                        {
                            global_toxid_text_set = true;
                            MainActivity.mt.setText(MainActivity.mt.getText() + "\n" + "my_ToxId=" + my_ToxId);
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

                MainActivity.friends = MainActivity.tox_self_get_friend_list();
                Log.i(TAG, "number of friends=" + MainActivity.friends.length);

                int fc = 0;
                boolean exists_in_db = false;
                MainActivity.friend_list_fragment.clear_friends();
                for (fc = 0; fc < MainActivity.friends.length; fc++)
                {
                    Log.i(TAG, "loading friend  #:" + fc);

                    FriendList f;
                    List<FriendList> fl = orma.selectFromFriendList().tox_friendnumEq(fc).toList();
                    if (fl.size() > 0)
                    {
                        f = fl.get(0);
                    }
                    else
                    {
                        f = null;
                    }

                    if (f == null)
                    {
                        Log.i(TAG, "fc is null");

                        f = new FriendList();
                        f.tox_public_key_string = "" + (Math.random() * 100000);
                        f.tox_friendnum = fc;
                        f.name = "friend #" + fc;
                        exists_in_db = false;
                    }
                    else
                    {
                        Log.i(TAG, "found friend in DB " + f.tox_friendnum + " f=" + f);
                        exists_in_db = true;
                    }

                    if (MainActivity.friend_list_fragment != null)
                    {
                         MainActivity.friend_list_fragment.add_friends(f);
                    }

                    if (exists_in_db == false)
                    {
                        orma.insertIntoFriendList(f);
                    }
                    else
                    {
                        orma.updateFriendList().tox_friendnumEq(f.tox_friendnum).tox_friendnum(f.tox_friendnum).tox_public_key_string(f.tox_public_key_string).name(f.name).status_message(f.status_message).TOX_CONNECTION(f.TOX_CONNECTION).TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                    }
                }

                long tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                MainActivity.tox_iterate();

                while (!stop_me)
                {
                    try
                    {
                        sleep(tox_iteration_interval_ms);
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

                try
                {
                    MainActivity.tox_kill();
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
        MainActivity.tox_service_fg = null;
        return super.onUnbind(intent);
    }

    @Override
    public void unbindService(ServiceConnection conn)
    {
        Log.i(TAG, "unbindService");
        MainActivity.tox_service_fg = null;
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
        MainActivity.tox_service_fg = this;
        return null;
    }

    // ------------------------------

}