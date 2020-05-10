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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class GroupAudioPlayer extends AppCompatActivity
{
    static final String TAG = "trifa.GAActy";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREV = "action_previous";

    static GroupAudioService group_audio_service = null;

    static final int GROUP_AUDIO_STATE_PLAYING = 1;
    static int PlaybackState = GROUP_AUDIO_STATE_PLAYING;
    static boolean mStarted = false;
    static long PlaybackState_Position = 0;

    public static String channelId = "";
    static NotificationChannel notification_channel_group_audio_play_service = null;
    static NotificationManager nmn3 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        nmn3 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            String channelName = "Tox Group Audio Play";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channelId = "trifa_group_audio_play";
            notification_channel_group_audio_play_service = new NotificationChannel(channelId, channelName, importance);
            notification_channel_group_audio_play_service.setDescription(channelId);
            notification_channel_group_audio_play_service.setSound(null, null);
            notification_channel_group_audio_play_service.enableVibration(false);
            nmn3.createNotificationChannel(notification_channel_group_audio_play_service);
        }

        Log.i(TAG, "group_audio_service:start");
        try
        {
            Intent i = new Intent(this, GroupAudioService.class);
            startService(i);
        }
        catch (Exception e)
        {
            Log.i(TAG, "group_audio_service:EE01:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    private Notification createNotification()
    {
        Log.i(TAG, "group_audio_service=" + group_audio_service);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setVisibility(
            Notification.VISIBILITY_PUBLIC).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(
            "My notification").setContentText("Hello World!");

        int playPauseButtonPosition = 0;

        // addPlayPauseAction(builder);

        return (builder.build());
    }

    private void addPlayPauseAction(Notification.Builder builder)
    {
        String label;
        int icon;
        PendingIntent intent = null;

        if (PlaybackState == GROUP_AUDIO_STATE_PLAYING)
        {
            label = "pause";
            icon = R.drawable.circle_orange;
            // intent = mPauseIntent;
        }
        else
        {
            label = "play";
            icon = R.drawable.circle_green;
            // intent = mPlayIntent;
        }
        builder.addAction(new Notification.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(Notification.Builder builder)
    {
        if (!mStarted)
        {
            group_audio_service.stopForeground(true);
            return;
        }

        if (PlaybackState == GROUP_AUDIO_STATE_PLAYING && PlaybackState_Position >= 0)
        {
            builder.setWhen(System.currentTimeMillis() - PlaybackState_Position).setShowWhen(true).setUsesChronometer(
                true);
        }
        else
        {
            builder.setWhen(0).setShowWhen(false).setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(PlaybackState == GROUP_AUDIO_STATE_PLAYING);
    }
}
