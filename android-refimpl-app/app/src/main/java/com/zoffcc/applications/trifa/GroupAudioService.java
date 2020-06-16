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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.nativeaudio.AudioProcessing;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.nativeaudio.AudioProcessing.init_buffers;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.HeadsetStateReceiver.isBluetoothConnected;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.drawableToBitmap;
import static com.zoffcc.applications.trifa.MainActivity.SAMPLE_RATE_FIXED;
import static com.zoffcc.applications.trifa.MainActivity.toxav_groupchat_disable_av;
import static com.zoffcc.applications.trifa.MainActivity.toxav_groupchat_enable_av;
import static com.zoffcc.applications.trifa.TrifaToxService.wakeup_tox_thread;

public class GroupAudioService extends Service
{
    static final String TAG = "trifa.GAService";
    static String conf_id = "-1";
    static int ONGOING_GROUP_AUDIO_NOTIFICATION_ID = 886613;
    public static final String ACTION_PLAY = "com.zoffcc.applications.trifa.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.zoffcc.applications.trifa.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.zoffcc.applications.trifa.ACTION_STOP";
    public static final int ACTION_PLAY_ID = 112124;
    public static final int ACTION_PAUSE_ID = 112125;
    public static final int ACTION_STOP_ID = 112126;
    static boolean running = false;
    static Thread GAThread = null;
    static NotificationManager nm3 = null;
    static GroupAudioService ga_service = null;
    static int activity_state = 0;
    static notification_and_builder noti_and_builder = null;
    static RemoteViews views = null;
    static RemoteViews bigViews = null;
    static Context context_gas_static = null;
    static long chronometer_base = 0;
    static long chronometer_base2 = 0;

    static final int GAS_PAUSED = 0;
    static final int GAS_PLAYING = 1;
    static int global_gas_status = 0;

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
        // Log.i(TAG, "onStartCommand:conf_id=" + conf_id);

        try
        {
            final String f_name = HelperConference.get_conference_title_from_confid(conf_id);
            final long conference_num = tox_conference_by_confid__wrapper(conf_id);

            // update the notification text
            views.setTextViewText(R.id.status_bar_track_name, "#" + conference_num + ": " + f_name); // bold
            bigViews.setTextViewText(R.id.status_bar_track_name, "#" + conference_num + ": " + f_name); // bold
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

        global_gas_status = GAS_PLAYING;

        context_gas_static = this;

        nm3 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti_and_builder = buildNotification(global_gas_status);
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

                wakeup_tox_thread();

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
                        if (isBluetoothConnected())
                        {
                            Log.i(TAG, "startBluetoothSco");
                            manager.startBluetoothSco();
                        }
                        else
                        {
                            // headset plugged in
                            Log.i(TAG, "onReceive:headset:plugged in"); //$NON-NLS-1$
                            manager.setSpeakerphoneOn(false);
                            manager.setWiredHeadsetOn(true);
                            Callstate.audio_device = 1;
                            Callstate.audio_speaker = false;
                            manager.setBluetoothScoOn(false);
                        }
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

                // HINT: stop audio recording, we do not need it in this mode -------------
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
                // HINT: stop audio recording, we do not need it in this mode -------------
                try
                {
                    Log.i(TAG, "GAThread:starting ...");
                    int delta = 0;
                    final int sleep_millis = NativeAudio.n_buf_iterate_ms; // "x" ms is what native audio wants
                    int sleep_millis_current = sleep_millis;
                    running = true;
                    long d1 = 0;
                    long d2 = 0;
                    while (running)
                    {
                        d1 = SystemClock.uptimeMillis();
                        // Log.i(TAG, "deltaZZ=" + (SystemClock.uptimeMillis() - d2));
                        // d2 = SystemClock.uptimeMillis();
                        MainActivity.jni_iterate_group_audio(0, sleep_millis);
                        delta = (int) (SystemClock.uptimeMillis() - d1);

                        sleep_millis_current = sleep_millis - delta;
                        if (sleep_millis_current < 1)
                        {
                            sleep_millis_current = 1;
                        }
                        else if (sleep_millis_current > sleep_millis + 5)
                        {
                            sleep_millis_current = sleep_millis + 5;
                        }

                        //if (delta > 5)
                        //{
                        //    Log.i(TAG, "delta=" + delta + " sleep_millis_current=" + sleep_millis_current);
                        //}
                        Thread.sleep(sleep_millis_current - 1, (1000000 - 330000)); // sleep
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "GAThread:finished");
                activity_state = 0;

                // Log.i(TAG, "toxav_groupchat_disable_av:E:gnum=" + tox_conference_by_confid__wrapper(conf_id));
                // Log.i(TAG, "toxav_groupchat_disable_av:E:gid=" + conf_id);
                toxav_groupchat_disable_av(tox_conference_by_confid__wrapper(conf_id));
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

        views = new RemoteViews(getPackageName(), R.layout.gas_status_bar);
        bigViews = new RemoteViews(getPackageName(), R.layout.gas_status_bar_expanded);

        views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime(), null, true);
        bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime(), null, true);

        chronometer_base = 0;
        chronometer_base2 = SystemClock.elapsedRealtime();

        Drawable d_pause = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_pause).backgroundColor(
            Color.TRANSPARENT).sizeDp(50);

        Drawable d_stop = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_stop).backgroundColor(
            Color.TRANSPARENT).sizeDp(50);

        try
        {
            views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));
            bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            views.setImageViewBitmap(R.id.status_bar_stop, drawableToBitmap(d_stop));
            bigViews.setImageViewBitmap(R.id.status_bar_stop, drawableToBitmap(d_stop));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // *************
        // *************
        // *************
        Intent stopIntent = new Intent(this, ButtonReceiver.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, ACTION_STOP_ID, stopIntent,
                                                                     PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.status_bar_stop, playPendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_stop, playPendingIntent);
        // *************
        // *************
        // *************
        Intent pauseIntent = new Intent(this, ButtonReceiver.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, ACTION_PAUSE_ID, pauseIntent,
                                                                      PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
        // *************
        // *************
        // *************

        views.setTextViewText(R.id.status_bar_track_name, "..."); // bold
        bigViews.setTextViewText(R.id.status_bar_track_name, "..."); // bold

        views.setTextViewText(R.id.status_bar_artist_name, "Tox:" + "GroupAudio");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Tox:" + "GroupAudio");

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

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
        b.setStyle(new androidx.media.app.NotificationCompat.MediaStyle());
        b.setColor(getResources().getColor(R.color.colorPrimary));
        b.setSmallIcon(R.mipmap.ic_launcher);
        b.setLargeIcon(null);
        b.setContentText("Tox:" + "GroupAudio playing ...");
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setAutoCancel(false);
        b.setOngoing(true);
        b.setLocalOnly(true);
        b.setCustomContentView(views);
        b.setCustomBigContentView(bigViews);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        b.setContentIntent(pendingIntent);

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
            // e.printStackTrace();
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
            // e.printStackTrace();
        }

        try
        {
            ga_service.stopSelf();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        ga_service = null;

        removeNotification();
    }

    public static class ButtonReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                System.out.println("ButtonReceiver:" + intent.getAction());

                if (intent.getAction() == ACTION_STOP)
                {
                    global_gas_status = GAS_PAUSED;
                    stop_me();
                }
                else if (intent.getAction() == ACTION_PLAY)
                {
                    if (global_gas_status == GAS_PAUSED)
                    {
                        do_play();
                    }
                }
                else if (intent.getAction() == ACTION_PAUSE)
                {
                    if (global_gas_status == GAS_PLAYING)
                    {
                        do_pause();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static void do_pause()
    {
        try
        {
            // update the notification
            noti_and_builder.n = noti_and_builder.b.build();

            chronometer_base = chronometer_base + (SystemClock.elapsedRealtime() - chronometer_base2);

            views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime() - chronometer_base, null,
                                 false);
            bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime() - chronometer_base, null,
                                    false);

            Drawable d_play = new IconicsDrawable(context_gas_static).icon(
                GoogleMaterial.Icon.gmd_play_arrow).backgroundColor(Color.TRANSPARENT).sizeDp(50);

            // Log.i(TAG, "toxav_groupchat_disable_av:D:gnum=" + tox_conference_by_confid__wrapper(conf_id));
            // Log.i(TAG, "toxav_groupchat_disable_av:D:gid=" + conf_id);
            toxav_groupchat_disable_av(tox_conference_by_confid__wrapper(conf_id));

            try
            {
                views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_play));
                bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_play));

                Intent playIntent = new Intent(context_gas_static, ButtonReceiver.class);
                playIntent.setAction(ACTION_PLAY);
                PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context_gas_static, ACTION_PLAY_ID,
                                                                              playIntent,
                                                                              PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
                bigViews.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            nm3.notify(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        global_gas_status = GAS_PAUSED;
    }

    static void do_play()
    {
        try
        {
            // update the notification
            noti_and_builder.n = noti_and_builder.b.build();

            views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime() - chronometer_base, null, true);
            bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime() - chronometer_base, null,
                                    true);

            chronometer_base2 = SystemClock.elapsedRealtime();

            Drawable d_pause = new IconicsDrawable(context_gas_static).icon(
                GoogleMaterial.Icon.gmd_pause).backgroundColor(Color.TRANSPARENT).sizeDp(50);

            toxav_groupchat_enable_av(tox_conference_by_confid__wrapper(conf_id));

            try
            {
                views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));
                bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_pause));

                Intent pauseIntent = new Intent(context_gas_static, ButtonReceiver.class);
                pauseIntent.setAction(ACTION_PAUSE);
                PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context_gas_static, ACTION_PAUSE_ID,
                                                                              pauseIntent,
                                                                              PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
                bigViews.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            nm3.notify(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        global_gas_status = GAS_PLAYING;
    }

    static void do_update_group_title()
    {
        try
        {
            // update the notification
            noti_and_builder.n = noti_and_builder.b.build();

            try
            {
                final String f_name = HelperConference.get_conference_title_from_confid(conf_id);
                final long conference_num = tox_conference_by_confid__wrapper(conf_id);

                // update the notification text
                views.setTextViewText(R.id.status_bar_track_name, "#" + conference_num + ": " + f_name); // bold
                bigViews.setTextViewText(R.id.status_bar_track_name, "#" + conference_num + ": " + f_name); // bold
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            nm3.notify(ONGOING_GROUP_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        global_gas_status = GAS_PLAYING;
    }

}
