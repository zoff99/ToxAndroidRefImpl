/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2024 Zoff <zoff@zoff.cc>
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

import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import static com.zoffcc.applications.trifa.MainActivity.Notification_new_message_ID;
import static com.zoffcc.applications.trifa.MainActivity.Notification_watchdog_trifa_stopped_ID;
import static com.zoffcc.applications.trifa.MainActivity.WATCHDOG_NOTIFICATION_ID;
import static com.zoffcc.applications.trifa.MainActivity.channelId_newmessage_sound_and_vibrate;
import static com.zoffcc.applications.trifa.MainActivity.channelId_toxservice;
import static com.zoffcc.applications.trifa.MainActivity.context_s;

public class WatchdogService extends Service
{
    static final String TAG = "trifa.WatchdogService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "onBind intent=" + intent);
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "onUnbind" + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void unbindService(ServiceConnection conn)
    {
        Log.i(TAG, "unbindService conn=" + conn);
        super.unbindService(conn);
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand intent=" + intent + " flags=" + flags + " startId=" + startId);
        try {
            Notification notification = new NotificationCompat.Builder(this, channelId_toxservice).build();
            int type = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
            }
            ServiceCompat.startForeground(this,
                                          WATCHDOG_NOTIFICATION_ID,
                                          notification,
                                          type);

        }
        catch (Exception e)
        {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (e instanceof ForegroundServiceStartNotAllowedException))
            {
                // App not in a valid state to start foreground service
                // (e.g started from bg)
                Log.i(TAG, "onStartCommand: App not in a valid state to start foreground service");
            }
            else
            {
                Log.i(TAG, "onStartCommand: Some Error occured");
            }
        }

        if (intent == null)
        {
            /*
            try
            {
                Intent notificationIntent = new Intent(this, StartMainActivityWrapper.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.setAction("com.zoffcc.applications.trifa." + (long) (Math.random() * 100000));
                notificationIntent.putExtra("CLEAR_NEW_MESSAGE_NOTIFICATION", "1");
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                                                                        notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                Notification notification = new NotificationCompat.
                        Builder(this, channelId_newmessage_sound_and_vibrate).
                        setContentTitle("TRIfA has stopped working!").
                        setContentText("Start the app again by clicking here").
                        setSmallIcon(R.mipmap.ic_launcher).
                        setAutoCancel(true).
                        setContentIntent(pendingIntent).
                        setVisibility(NotificationCompat.VISIBILITY_PUBLIC).
                        build();
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(Notification_watchdog_trifa_stopped_ID, notification);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            */
        }

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }
}
