/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2019 Zoff <zoff@zoff.cc>
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

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.trifa.TrifaToxService.trifa_service_thread;

public class MyExternReceiver extends BroadcastReceiver
{
    private static final String TAG = "trifa.MyExternRcvr";
    private static PowerManager.WakeLock extern_wakeup_lock = null;

    static int ICOMING_MSG_NOTIFICATION_ID = 886676;

    @Override
    public void onReceive(Context context, Intent intent2)
    {
        try
        {
            if (extern_wakeup_lock == null)
            {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                extern_wakeup_lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "trifa:trifa_extern_wakeup_lock");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!extern_wakeup_lock.isHeld())
                    {
                        extern_wakeup_lock.acquire();
                        Log.i(TAG, "acquiring wakelock");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (isMyServiceRunning(TrifaToxService.class.getName(), context))
                    {
                        Log.i(TAG, "TrifaToxService running");
                    }
                    else
                    {
                        Log.i(TAG, "TrifaToxService NOT running");

                        if (Build.VERSION.SDK_INT < 29)
                        {
                            // TODO: this is not working anymore starting with Android 10
                            // https://developer.android.com/guide/components/activities/background-starts
                            // thanks Google

                            Intent open_trifa_intent = new Intent(context, StartMainActivityWrapper.class);
                            open_trifa_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(open_trifa_intent);
                            Log.i(TAG, "activity started");
                        }
                        else
                        {
                            Log.i(TAG, "API:" + Build.VERSION.SDK_INT);
                            try
                            {
                                NotificationManager nm3 = (NotificationManager) context.getSystemService(
                                        Context.NOTIFICATION_SERVICE);

                                Intent fullScreenIntent = new Intent(context, StartMainActivityWrapper.class);
                                PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                                                                                                  fullScreenIntent,
                                                                                                  PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                {
                                    NotificationChannel channel = new NotificationChannel(
                                            "trifa_extern_msg_receiver_id", "new Message",
                                            NotificationManager.IMPORTANCE_HIGH);
                                    nm3.createNotificationChannel(channel);
                                }

                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.
                                        Builder(context, "trifa_extern_msg_receiver_id").
                                        setSmallIcon(R.mipmap.ic_launcher).
                                        setContentTitle("TRIfA").
                                        setContentText("Incoming Message").
                                        setPriority(NotificationCompat.PRIORITY_HIGH).
                                        setCategory(NotificationCompat.CATEGORY_CALL).
                                        setAutoCancel(true).
                                        setFullScreenIntent(fullScreenPendingIntent, true);

                                Notification incomingMsgNotification = notificationBuilder.build();
                                nm3.notify(ICOMING_MSG_NOTIFICATION_ID, incomingMsgNotification);
                                Log.i(TAG, "notify");
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                                Log.i(TAG, "show_noti:EE02:" + e2.getMessage());
                            }
                        }

                        try
                        {
                            Thread.sleep(20 * 1000); // wait for 20 seconds
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                }


                try
                {
                    Log.i(TAG, "MyExternReceiver:" + "onReceive");

                    if (trifa_service_thread != null)
                    {
                        TrifaToxService.need_wakeup_now = true;
                        trifa_service_thread.interrupt();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(10 * 1000); // keep wakelock for 10 seconds
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (extern_wakeup_lock.isHeld())
                    {
                        Log.i(TAG, "releasing wakelock");
                        extern_wakeup_lock.release();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                extern_wakeup_lock = null;
            }
        };
        t.start();
    }

    private boolean isMyServiceRunning(String serviceClassName, Context c)
    {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClassName.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
