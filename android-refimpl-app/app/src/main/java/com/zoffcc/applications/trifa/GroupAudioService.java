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
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import static com.zoffcc.applications.trifa.MainActivity.notification_view;

public class GroupAudioService extends Service
{
    static final String TAG = "trifa.GAService";
    static int ONGOING_GROUP_AUDIO_NOTIFICATION_ID = 886613;
    Notification notification2 = null;
    NotificationManager nmn2 = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        // serivce is created ---
        super.onCreate();

        nmn2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification_view = new RemoteViews(getPackageName(), R.layout.custom_group_audio_play_notification);
        notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
        notification_view.setTextViewText(R.id.title, "Tox: " + "GroupAudio");
        notification_view.setTextViewText(R.id.text, "playing ...");

        NotificationCompat.Builder b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b = new NotificationCompat.Builder(this, GroupAudioPlayer.channelId);
        }
        else
        {
            b = new NotificationCompat.Builder(this);
        }
        b.setContent(notification_view);
        b.setOnlyAlertOnce(false);
        // b.setContentIntent(pendingIntent);
        b.setSmallIcon(R.drawable.circle_green_notification);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            b.setColor(Color.parseColor("#04b431"));
        }

        notification2 = b.build();

        startForeground(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, notification2);
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
}
