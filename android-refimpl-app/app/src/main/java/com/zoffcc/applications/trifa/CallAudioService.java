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
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.trifa.CallingActivity.callactivity_handler_s;
import static com.zoffcc.applications.trifa.CallingActivity.mute_button;
import static com.zoffcc.applications.trifa.CallingActivity.stop_active_call;
import static com.zoffcc.applications.trifa.CallingActivity.trifa_is_MicrophoneMute;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.drawableToBitmap;
import static com.zoffcc.applications.trifa.HelperGeneric.get_vfs_image_filename_friend_avatar;
import static com.zoffcc.applications.trifa.HelperGeneric.put_vfs_image_on_imageview_real_remoteviews;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;
import static com.zoffcc.applications.trifa.TrifaToxService.wakeup_tox_thread;

public class CallAudioService extends Service
{
    static final String TAG = "trifa.GAService";
    static int ONGOING_CALL_AUDIO_NOTIFICATION_ID = 886614;
    public static final String ACTION_MUTE = "com.zoffcc.applications.trifa.ACTION_MUTE";
    public static final String ACTION_STOP = "com.zoffcc.applications.trifa.ACTION_STOP";
    public static final int ACTION_MUTE_ID = 112128;
    public static final int ACTION_STOP_ID = 112129;
    static boolean running = false;
    static Thread GAThread = null;
    static NotificationManager nm3 = null;
    static CallAudioService ga_service = null;
    static int activity_state = 0;
    static notification_and_builder noti_and_builder = null;
    static RemoteViews views = null;
    static RemoteViews bigViews = null;
    static RemoteViews status_bar_album_art = null;
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
            // update the notification text
            views.setTextViewText(R.id.status_bar_artist_name, "" + Callstate.friend_alias_name); // bold
            bigViews.setTextViewText(R.id.status_bar_artist_name, "" + Callstate.friend_alias_name); // bold
            noti_and_builder.n = noti_and_builder.b.build();
            nm3.notify(ONGOING_CALL_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
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
        startForeground(ONGOING_CALL_AUDIO_NOTIFICATION_ID, noti_and_builder.n);

        Log.i(TAG, "onCreate:thread:1");

        running = true;
        GAThread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "GAThread:starting");
                activity_state = 1;
                wakeup_tox_thread();

                try
                {
                    this.setName("t_va_call_play");
                    android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
                    // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Log.i(TAG, "Videocall_audio_play_thread_service:starting ...");
                    int delta = 0;
                    final int sleep_millis = NativeAudio.n_buf_iterate_ms; // "x" ms is what native audio wants
                    int sleep_millis_current = sleep_millis;
                    running = true;
                    long d1 = 0;
                    int res = 0;

                    while (running)
                    {
                        d1 = SystemClock.uptimeMillis();

                        res = MainActivity.jni_iterate_videocall_audio(0, sleep_millis, NativeAudio.channel_count,
                                                                 NativeAudio.sampling_rate, 0);
                        if (res == -1)
                        {
                            Thread.sleep(1);
                            res = MainActivity.jni_iterate_videocall_audio(0, sleep_millis, NativeAudio.channel_count,
                                                                           NativeAudio.sampling_rate, 1);
                            // Log.i(TAG, "jni_iterate_videocall_audio:res=" + res);
                        }

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

                        Thread.sleep(sleep_millis_current - 1, (1000000 - 1000)); // sleep
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

        views = new RemoteViews(getPackageName(), R.layout.gas_status_bar);
        bigViews = new RemoteViews(getPackageName(), R.layout.gas_status_bar_expanded);

        views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime(), null, true);
        bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime(), null, true);

        chronometer_base = 0;
        chronometer_base2 = SystemClock.elapsedRealtime();

        Drawable d_mute = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(
                Color.TRANSPARENT).sizeDp(50);

        Drawable d_stop = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_stop).backgroundColor(
                Color.TRANSPARENT).sizeDp(50);

        try
        {
            views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_mute));
            bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_mute));
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
        pauseIntent.setAction(ACTION_MUTE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, ACTION_MUTE_ID, pauseIntent,
                                                                      PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pausePendingIntent);
        // *************
        // *************
        // *************

        views.setTextViewText(R.id.status_bar_artist_name, "...");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "...");

        views.setTextViewText(R.id.status_bar_track_name, "Tox:" + "Call"); // bold
        bigViews.setTextViewText(R.id.status_bar_track_name, "Tox:" + "Call"); // bold

        NotificationCompat.Builder b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b = new NotificationCompat.Builder(this, CallingActivity.channelId);
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
        b.setContentText("Tox:" + "Call ...");
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setAutoCancel(false);
        b.setOngoing(true);
        b.setLocalOnly(true);
        b.setCustomContentView(views);
        b.setCustomBigContentView(bigViews);

        Intent notificationIntent = new Intent(this, CallingActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        b.setContentIntent(pendingIntent);

        Notification n = b.build();
        nb.b = b;
        nb.n = n;

        try
        {
            final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);

            views.setImageViewBitmap(R.id.status_bar_album_art, drawableToBitmap(d1));
            bigViews.setImageViewBitmap(R.id.status_bar_album_art, drawableToBitmap(d1));

            String fname = get_vfs_image_filename_friend_avatar(
                    tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));

            if (fname != null)
            {
                put_vfs_image_on_imageview_real_remoteviews(this, n, ONGOING_CALL_AUDIO_NOTIFICATION_ID,
                                                            R.id.status_bar_album_art, views, d1, fname, false, true,
                                                            main_get_friend(tox_friend_by_public_key__wrapper(
                                                                    Callstate.friend_pubkey)));
                put_vfs_image_on_imageview_real_remoteviews(this, n, ONGOING_CALL_AUDIO_NOTIFICATION_ID,
                                                            R.id.status_bar_album_art, bigViews, d1, fname, false, true,
                                                            main_get_friend(tox_friend_by_public_key__wrapper(
                                                                    Callstate.friend_pubkey)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "EEE:" + e.getMessage());
        }

        return nb;
    }

    private static void removeNotification()
    {
        try
        {
            nm3.cancel(ONGOING_CALL_AUDIO_NOTIFICATION_ID);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void stop_me(boolean cancel_toxav_call)
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

        if (cancel_toxav_call)
        {
            try
            {
                toxav_call_control(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                   ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }

        try
        {
            stop_active_call();
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    static void do_mute()
    {
        try
        {
            // update the notification
            noti_and_builder.n = noti_and_builder.b.build();

            Drawable d_mute = new IconicsDrawable(context_gas_static).icon(
                    GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).sizeDp(50);

            try
            {
                if (trifa_is_MicrophoneMute)
                {
                    audio_manager_s.setMicrophoneMute(false);
                    trifa_is_MicrophoneMute = false;
                    d_mute = new IconicsDrawable(context_gas_static).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(
                            Color.TRANSPARENT).sizeDp(50);

                }
                else
                {
                    audio_manager_s.setMicrophoneMute(true);
                    trifa_is_MicrophoneMute = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "setMicrophoneMute:002:EE:" + e.getMessage());
            }

            try
            {
                if (trifa_is_MicrophoneMute)
                {
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Drawable d2a = new IconicsDrawable(context_gas_static).icon(
                                        GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                        context_gas_static.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(
                                        50);
                                mute_button.setImageDrawable(d2a);
                            }
                            catch (Exception ignored)
                            {
                            }
                        }
                    };
                    callactivity_handler_s.post(myRunnable);
                }
                else
                {
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Drawable d2a = new IconicsDrawable(context_gas_static).icon(
                                        GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                        context_gas_static.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(
                                        50);
                                mute_button.setImageDrawable(d2a);
                            }
                            catch (Exception ignored)
                            {
                            }
                        }
                    };
                    callactivity_handler_s.post(myRunnable);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "setMicrophoneMute:003:EE:" + e.getMessage());
            }

            try
            {
                views.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_mute));
                bigViews.setImageViewBitmap(R.id.status_bar_play, drawableToBitmap(d_mute));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            nm3.notify(ONGOING_CALL_AUDIO_NOTIFICATION_ID, noti_and_builder.n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class ButtonReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                System.out.println("ButtonReceiver:" + intent.getAction());

                if (intent.getAction().equals(ACTION_STOP))
                {
                    stop_me(true);
                }
                else if (intent.getAction().equals(ACTION_MUTE))
                {
                    do_mute();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
