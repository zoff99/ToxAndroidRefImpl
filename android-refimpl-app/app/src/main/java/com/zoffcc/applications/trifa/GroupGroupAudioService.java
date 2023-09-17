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
import com.zoffcc.applications.nativeaudio.NativeAudio;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.nativeaudio.NativeAudio.n_audio_in_buffer_max_count;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.ConferenceAudioActivity.push_to_talk_active;
import static com.zoffcc.applications.trifa.GroupMessageListActivity.init_native_audio_stuff;
import static com.zoffcc.applications.trifa.GroupMessageListActivity.ngc_audio_in_queue;
import static com.zoffcc.applications.trifa.HeadsetStateReceiver.isBluetoothConnected;
import static com.zoffcc.applications.trifa.HelperGeneric.drawableToBitmap;
import static com.zoffcc.applications.trifa.HelperGeneric.reset_audio_mode;
import static com.zoffcc.applications.trifa.HelperGeneric.set_audio_to_headset;
import static com.zoffcc.applications.trifa.HelperGeneric.stop_ngc_audio_system;

public class GroupGroupAudioService extends Service
{
    static final String TAG = "trifa.GRService";
    static String group_id = "-1";
    static int ONGOING_NGC_AUDIO_NOTIFICATION_ID = 886627;
    static boolean ngc_running = false;
    static Thread NGCGAThread = null;
    static NotificationManager ngc_nm3 = null;
    static GroupGroupAudioService ngc_ga_service = null;
    static int ngc_activity_state = 0;
    static ngc_notification_and_builder ngc_noti_and_builder = null;
    static RemoteViews views = null;
    static RemoteViews bigViews = null;
    static Context context_ngc_gas_static = null;
    static int global_ngc_gas_status = 0;
    static long ngc_chronometer_base = 0;
    static long ngc_chronometer_base2 = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        try
        {
            group_id = intent.getStringExtra("group_id");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // Log.i(TAG, "onStartCommand:conf_id=" + conf_id);

        try
        {
            // update the notification text
            views.setTextViewText(R.id.status_bar_track_name, "#G1"); // bold
            bigViews.setTextViewText(R.id.status_bar_track_name, "#G2"); // bold
            ngc_noti_and_builder.n = ngc_noti_and_builder.b.build();
            ngc_nm3.notify(ONGOING_NGC_AUDIO_NOTIFICATION_ID, ngc_noti_and_builder.n);
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

        ngc_ga_service = this;
        context_ngc_gas_static = this;

        ngc_nm3 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ngc_noti_and_builder = buildNotification(global_ngc_gas_status);
        startForeground(ONGOING_NGC_AUDIO_NOTIFICATION_ID, ngc_noti_and_builder.n);

        Log.i(TAG, "onCreate:thread:1");

        ngc_running = true;
        NGCGAThread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "GAThread:starting");

                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                Callstate.audio_ngc_group_active = true;

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

                /* --------- do not start audorecording yet
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
                */
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------
                // ------------- START Audio playing -------------

                ngc_activity_state = 1;

                try
                {
                    this.setName("t_ngc_play");
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
                            Log.i(TAG, "AUDIOROUTE:startBluetoothSco");
                            manager.startBluetoothSco();
                            Callstate.audio_device = 2;
                            // manager.setBluetoothScoOn(true);
                        }
                        else
                        {
                            // headset plugged in
                            Log.i(TAG, "AUDIOROUTE:onReceive:headset:plugged in");
                            Callstate.audio_device = 1;
                            set_audio_to_headset(manager);
                        }
                    }
                    else
                    {
                        Log.i(TAG, "onReceive:headset:setImageDrawable:null1");
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                    Log.i(TAG, "onReceive:headset:setImageDrawable:null2");
                }

                try
                {
                    Log.i(TAG, "NGCGAThread:starting ...");
                    int delta = 0;
                    final int sleep_millis = NativeAudio.n_buf_iterate_ms; // "x" ms is what native audio wants
                    int sleep_millis_current = sleep_millis;
                    ngc_running = true;
                    long d1 = 0;
                    long d2 = 0;

                    init_native_audio_stuff();

                    while (ngc_running)
                    {
                        d1 = SystemClock.uptimeMillis();

                        // -- play incoming bytes --
                        try
                        {
                            final byte[] buf = ngc_audio_in_queue.poll();
                            if (buf != null)
                            {
                                NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] = buf.length;
                                NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].rewind();
                                NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].put(buf);
                                int res = NativeAudio.PlayPCM16(NativeAudio.n_cur_buf);
                                // Log.i(TAG, "NGCGAThread:playPCM16:res=" + res);

                                if (NativeAudio.n_cur_buf + 1 >= n_audio_in_buffer_max_count)
                                {
                                    NativeAudio.n_cur_buf = 0;
                                }
                                else
                                {
                                    NativeAudio.n_cur_buf++;
                                }
                            }
                        }
                        catch(Exception e)
                        {
                        }
                        // -- play incoming bytes --

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

                        Thread.sleep(sleep_millis_current - 1, (1000000 - 5000)); // sleep
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "GAThread:finished");
                ngc_activity_state = 0;
                push_to_talk_active = false;

                // ------ shutdown audio device ------
                // ------ shutdown audio device ------
                // ------ shutdown audio device ------
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                try
                {
                    if (dha._Detect())
                    {
                        if (isBluetoothConnected())
                        {
                            Log.i(TAG, "AUDIOROUTE:stopBluetoothSco:1");
                            // manager.setBluetoothScoOn(false);
                            manager.stopBluetoothSco();
                        }
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
                // ------ shutdown audio device ------
                // ------ shutdown audio device ------
                // ------ shutdown audio device ------
            }
        };

        Log.i(TAG, "onCreate:thread:2");
        NGCGAThread.start();
        Log.i(TAG, "onCreate:thread:3");
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "onUnbind");
        ngc_noti_and_builder.n.flags = Notification.FLAG_ONGOING_EVENT;

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

    static class ngc_notification_and_builder
    {
        Notification n;
        NotificationCompat.Builder b;
    }

    private ngc_notification_and_builder buildNotification(int playbackStatus)
    {
        ngc_notification_and_builder nb = new ngc_notification_and_builder();

        views = new RemoteViews(getPackageName(), R.layout.gas_status_bar);
        bigViews = new RemoteViews(getPackageName(), R.layout.gas_status_bar_expanded);

        views.setChronometer(R.id.status_bar_chrono1, SystemClock.elapsedRealtime(), null, true);
        bigViews.setChronometer(R.id.status_bar_chrono2, SystemClock.elapsedRealtime(), null, true);

        ngc_chronometer_base = 0;
        ngc_chronometer_base2 = SystemClock.elapsedRealtime();

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

        views.setTextViewText(R.id.status_bar_track_name, "..."); // bold
        bigViews.setTextViewText(R.id.status_bar_track_name, "..."); // bold

        views.setTextViewText(R.id.status_bar_artist_name, "Tox:" + "GroupAudio");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Tox:" + "GroupAudio");

        NotificationCompat.Builder b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b = new NotificationCompat.Builder(this, GroupMessageListActivity.ngc_channelId);
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

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
            ngc_nm3.cancel(ONGOING_NGC_AUDIO_NOTIFICATION_ID);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void stop_me(boolean stop_full)
    {
        ngc_running = false;
        try
        {
            if (NGCGAThread != null)
            {
                NGCGAThread.join();
                NGCGAThread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Callstate.audio_ngc_group_active = false;
        if (stop_full)
        {
            Callstate.reset_values();
        }
        GroupGroupAudioService.group_id = "-1";
        AudioRecording.global_audio_group_send_res = -999;

        try
        {
            ngc_ga_service.stopForeground(true);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            ngc_ga_service.stopSelf();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        ngc_ga_service = null;

        if (stop_full)
        {
            reset_audio_mode();
            stop_ngc_audio_system();
        }

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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
