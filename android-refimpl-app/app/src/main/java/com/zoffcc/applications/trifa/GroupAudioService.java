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
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.nativeaudio.AudioProcessing;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import static com.zoffcc.applications.nativeaudio.AudioProcessing.init_buffers;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.SAMPLE_RATE_FIXED;
import static com.zoffcc.applications.trifa.MainActivity.toxav_groupchat_enable_av;

public class GroupAudioService extends Service
{
    static final String TAG = "trifa.GAService";
    static String conf_id = "-1";
    static int ONGOING_GROUP_AUDIO_NOTIFICATION_ID = 886613;
    public static final String ACTION_PLAY = "com.zoffcc.applications.trifa.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.zoffcc.applications.trifa.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.zoffcc.applications.trifa.ACTION_STOP";
    static boolean running = false;
    static Thread GAThread = null;
    static NotificationManager nm3 = null;
    static GroupAudioService ga_service = null;
    static int activity_state = 0;
    static notification_and_builder noti_and_builder = null;
    static RemoteViews views = null;

    final int GAS_PAUSED = 0;
    final int GAS_PLAYING = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        try
        {
            conf_id = intent.getStringExtra("conf_id");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i(TAG, "onStartCommand:conf_id=" + conf_id);

        try
        {
            final String f_name = HelperConference.get_conference_title_from_confid(conf_id);
            final long conference_num = tox_conference_by_confid__wrapper(conf_id);

            // update the notification text
            noti_and_builder.b.setContentTitle("#" + conference_num + ": " + f_name);
            noti_and_builder.n = noti_and_builder.b.build();
            nm3.notify(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        // serivce is created ---
        super.onCreate();

        ga_service = this;

        nm3 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti_and_builder = buildNotification(GAS_PLAYING);
        startForeground(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, noti_and_builder.n);

        Log.i(TAG, "onCreate:thread:1");

        running = true;
        GAThread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "GAThread:starting");

                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                Callstate.audio_group_active = true;
                try
                {
                    CallingActivity.ap = new AudioProcessing();
                    init_buffers(10, 1, SAMPLE_RATE_FIXED, 1, SAMPLE_RATE_FIXED);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (!AudioRecording.stopped)
                    {
                        AudioRecording.close();
                        audio_thread.join();
                        audio_thread = null;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (!AudioReceiver.stopped)
                    {
                        AudioReceiver.close();
                        audio_receiver_thread.join();
                        audio_receiver_thread = null;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (AudioReceiver.stopped)
                    {
                        audio_receiver_thread = new AudioReceiver();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (AudioRecording.stopped)
                    {
                        audio_thread = new AudioRecording();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                ConferenceAudioActivity.conf_id = GroupAudioService.conf_id;
                toxav_groupchat_enable_av(tox_conference_by_confid__wrapper(conf_id));
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------

                activity_state = 1;

                try
                {
                    this.setName("t_g_S_play");
                    android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
                    // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                DetectHeadset dha = null;
                try
                {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if (dha._Detect())
                    {
                        // headset plugged in
                        Log.i(TAG, "onReceive:headset:plugged in"); //$NON-NLS-1$
                        manager.setSpeakerphoneOn(false);
                        manager.setWiredHeadsetOn(true);
                        Callstate.audio_device = 1;
                        Callstate.audio_speaker = false;
                        manager.setBluetoothScoOn(false);
                    }
                    else
                    {
                        Log.i(TAG, "onReceive:headset:setImageDrawable:null1"); //$NON-NLS-1$
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                    Log.i(TAG, "onReceive:headset:setImageDrawable:null2"); //$NON-NLS-1$
                }

                // HINT: stop audio recoring, we do not need it in this mode -------------
                try
                {
                    if (!AudioRecording.stopped)
                    {
                        AudioRecording.close();
                        audio_thread.join();
                        audio_thread = null;
                        NativeAudio.StopREC();
                        Log.i(TAG, "stop_audio_recording:DONE");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "stop_audio_recording:EE01" + e.getMessage());
                }
                // HINT: stop audio recoring, we do not need it in this mode -------------


                try
                {
                    Log.i(TAG, "GAThread:starting ...");
                    int delta = 0;
                    final int sleep_millis = 60; // 60ms is the maximum that JNI can buffer!
                    int sleep_millis_current = sleep_millis;
                    running = true;
                    while (running)
                    {
                        delta = MainActivity.jni_iterate_group_audio(0, sleep_millis);
                        sleep_millis_current = sleep_millis - delta;
                        if (sleep_millis_current < 1)
                        {
                            sleep_millis_current = 1;
                        }
                        else if (sleep_millis_current > sleep_millis)
                        {
                            sleep_millis_current = sleep_millis;
                        }
                        //Log.i(TAG, "GAThread:delta=" + delta + " sleep_millis_current=" + sleep_millis_current + " " +
                        //           ConferenceAudioActivity.conf_id);
                        Thread.sleep(sleep_millis_current - 1, (1000000 - 100000)); // sleep
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "GAThread:finished");
                activity_state = 0;
            }
        };

        Log.i(TAG, "onCreate:thread:2");
        GAThread.start();
        Log.i(TAG, "onCreate:thread:3");
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "onUnbind");
        noti_and_builder.n.flags = Notification.FLAG_ONGOING_EVENT;

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

    static class notification_and_builder
    {
        Notification n;
        NotificationCompat.Builder b;
    }

    private notification_and_builder buildNotification(int playbackStatus)
    {
        notification_and_builder nb = new notification_and_builder();

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.gas_status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(), R.layout.gas_status_bar_expanded);

        views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime(), null, true);
        bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime(), null, true);

        Drawable d_play = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_play_arrow).backgroundColor(
            Color.TRANSPARENT).sizeDp(50);

        Drawable d_pause = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_pause).backgroundColor(
            Color.TRANSPARENT).sizeDp(50);

        Drawable d_stop = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_stop).backgroundColor(
            Color.TRANSPARENT).sizeDp(50);

        views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));
        bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));

        views.setImageViewBitmap(R.id.status_bar_stop, drawableToBitmap(d_stop));
        bigViews.setImageViewBitmap(R.id.status_bar_stop, drawableToBitmap(d_stop));

        views.setTextViewText(R.id.status_bar_track_name, "Title"); // bold
        bigViews.setTextViewText(R.id.status_bar_track_name, "Title"); // bold

        views.setTextViewText(R.id.status_bar_artist_name, "Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Name");

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == GAS_PLAYING)
        {
            notificationAction = android.R.drawable.ic_media_pause;
            // play_pauseAction = playbackAction(1);
        }
        else // if (playbackStatus == GAS_PAUSED)
        {
            notificationAction = android.R.drawable.ic_media_play;
            // play_pauseAction = playbackAction(0);
        }

        NotificationCompat.Builder b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b = new NotificationCompat.Builder(this, GroupAudioPlayer.channelId);
        }
        else
        {
            b = new NotificationCompat.Builder(this);
        }

        b.setContentTitle("...");
        b.setShowWhen(false);
        b.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0));
        b.setColor(getResources().getColor(R.color.colorPrimary));
        b.setSmallIcon(R.mipmap.ic_launcher);
        b.setLargeIcon(null);
        b.setContentText("Tox:" + "GroupAudio playing ...");
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setAutoCancel(false);
        b.setOngoing(true);
        b.setLocalOnly(true);
        b.setWhen(System.currentTimeMillis());
        b.setCustomContentView(views);
        b.setCustomBigContentView(bigViews);
        b.setUsesChronometer(true);

        if (playbackStatus == GAS_PLAYING)
        {
            b.addAction(notificationAction, "pause.0", play_pauseAction);
        }
        else
        {
            b.addAction(notificationAction, "play.1", play_pauseAction);
        }

        Notification n = b.build();
        nb.b = b;
        nb.n = n;
        return nb;
    }

    private static void removeNotification()
    {
        try
        {
            nm3.cancel(ONGOING_GROUP_AUDIO_NOTIFICATION_ID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void stop_me()
    {
        running = false;
        try
        {
            if (GAThread != null)
            {
                GAThread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Callstate.audio_group_active = false;
        ConferenceAudioActivity.conf_id = "-1";
        GroupAudioService.conf_id = "-1";

        try
        {
            ga_service.stopForeground(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            ga_service.stopSelf();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ga_service = null;
        removeNotification();
    }

    public static Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
