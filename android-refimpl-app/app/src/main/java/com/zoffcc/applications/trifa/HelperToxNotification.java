/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.nmn3;
import static com.zoffcc.applications.trifa.MainActivity.notification_view;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;

public class HelperToxNotification
{
    private static final String TAG = "trifa.Hlp.ToxNoti";
    static int ONGOING_NOTIFICATION_ID = 1030;

    static Notification tox_notification_setup(Context c, NotificationManager nmn2)
    {
        Log.i(TAG, "tox_notification_setup:start");
        Notification notification2 = null;

        Intent notificationIntent = new Intent(c, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

        // -- notification ------------------
        // -- notification ------------------
        nmn2 = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);

        notification_view = new RemoteViews(c.getPackageName(), R.layout.custom_notification);
        Log.i(TAG, "contentView=" + notification_view);
        notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);

        // TypedValue typedValue = new TypedValue();
        // c.getTheme().resolveAttribute(R.attr.color, typedValue, true);
        // notification_view.setTextColor(R.id.title, typedValue.data);


        notification_view.setTextColor(R.id.title, ResourcesCompat.getColor(c.getResources(), R.color.textColorSecondary, null));

        notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE");
        notification_view.setTextViewText(R.id.text, "");

        NotificationCompat.Builder b = null;
        Notification.Builder b_new = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b_new = new Notification.Builder(c, MainActivity.channelId_toxservice);
            b = null;
        }
        else
        {
            b_new = null;
            b = new NotificationCompat.Builder(c);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            b_new.setContent(notification_view);
            b_new.setOnlyAlertOnce(false);
            try
            {
                b_new.setSound(null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            b_new.setContentIntent(pendingIntent);
            b_new.setSmallIcon(R.drawable.circle_red_notification);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                b_new.setColor(Color.parseColor("#ff0000"));
            }
            notification2 = b_new.build();
        }
        else
        {
            b.setContent(notification_view);
            b.setOnlyAlertOnce(false);
            b.setContentIntent(pendingIntent);
            b.setSmallIcon(R.drawable.circle_red_notification);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                b.setColor(Color.parseColor("#ff0000"));
            }
            notification2 = b.build();
        }
        // -- notification ------------------
        // -- notification ------------------

        Log.i(TAG, "tox_notification_setup:end");
        return notification2;
    }

    static void tox_notification_cancel(Context c)
    {
        Log.i(TAG, "tox_notification_cancel:start");

        try
        {
            // remove the notification
            NotificationManager nmn2 = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
            nmn2.cancel(ONGOING_NOTIFICATION_ID);
            Log.i(TAG, "tox_notification_cancel:OK");
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        Log.i(TAG, "tox_notification_cancel:end");
    }

    static void tox_notification_change(Context c, NotificationManager nmn2, int a_TOXCONNECTION, String message)
    {
        Log.i(TAG, "tox_notification_change:start");

        Notification notification2 = null;
        NotificationCompat.Builder b = null;
        Notification.Builder b_new = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.i(TrifaToxService.TAG,
                  "change_notification_fg:SDK:" + Build.VERSION.SDK_INT + " -> O:" + Build.VERSION_CODES.O);
            b_new = new Notification.Builder(c, MainActivity.channelId_toxservice);
            b = null;
        }
        else
        {
            b_new = null;
            b = new NotificationCompat.Builder(c);
        }
        Intent notificationIntent = new Intent(c, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            b_new.setOnlyAlertOnce(false);
            try
            {
                b_new.setSound(null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            b.setOnlyAlertOnce(false);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

        if (bootstrapping)
        {
            Log.i(TrifaToxService.TAG, "change_notification_fg:bootstrapping=true");
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_orange);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                b_new.setSmallIcon(R.drawable.circle_orange_notification);
            }
            else
            {
                b.setSmallIcon(R.drawable.circle_orange_notification);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    b_new.setColor(Color.parseColor("#ffce00"));
                }
                else
                {
                    b.setColor(Color.parseColor("#ffce00"));
                }
            }
            notification_view.setTextViewText(R.id.title, "Tox Service: " + "Bootstrapping" + " " + message);
        }
        else
        {
            Log.i(TrifaToxService.TAG, "change_notification_fg:bootstrapping=FALSE");

            if (a_TOXCONNECTION == 0)
            {
                notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    b_new.setSmallIcon(R.drawable.circle_red_notification);
                }
                else
                {
                    b.setSmallIcon(R.drawable.circle_red_notification);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        b_new.setColor(Color.parseColor("#ff0000"));
                    }
                    else
                    {
                        b.setColor(Color.parseColor("#ff0000"));
                    }
                }
                notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE" + " " + message);
            }
            else
            {
                if (a_TOXCONNECTION == 1)
                {
                    notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        b_new.setSmallIcon(R.drawable.circle_green_notification);
                    }
                    else
                    {
                        b.setSmallIcon(R.drawable.circle_green_notification);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            b_new.setColor(Color.parseColor("#04b431"));
                        }
                        else
                        {
                            b.setColor(Color.parseColor("#04b431"));
                        }
                    }
                    notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [TCP]" + " " + message);
                    // get_network_connections();
                }
                else // if (a_TOXCONNECTION__f == 2)
                {
                    notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        b_new.setSmallIcon(R.drawable.circle_green_notification);
                    }
                    else
                    {
                        b.setSmallIcon(R.drawable.circle_green_notification);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            b_new.setColor(Color.parseColor("#04b431"));
                        }
                        else
                        {
                            b.setColor(Color.parseColor("#04b431"));
                        }
                    }
                    notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [UDP]" + " " + message);
                    // get_network_connections();
                }
            }
        }
        notification_view.setTextViewText(R.id.text, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            b_new.setContentIntent(pendingIntent);
            b_new.setContent(notification_view);
            notification2 = b_new.build();
        }
        else
        {
            b.setContentIntent(pendingIntent);
            b.setContent(notification_view);
            notification2 = b.build();
        }
        nmn2.notify(ONGOING_NOTIFICATION_ID, notification2);
        Log.i(TAG, "tox_notification_change:end");
    }

    static void tox_notification_change_wrapper(int a_TOXCONNECTION, final String message)
    {
        Log.i(TAG, "tox_notification_change_wrapper:start");
        final int a_TOXCONNECTION_f = a_TOXCONNECTION;
        final Context static_context = context_s;

        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    long counter = 0;

                    while (MainActivity.tox_service_fg == null)
                    {
                        counter++;

                        if (counter > 10)
                        {
                            break;
                        }

                        // Log.i(TAG, "change_notification:sleep");

                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                        }
                    }

                    try
                    {
                        tox_notification_change(static_context, nmn3, a_TOXCONNECTION_f, message);
                        Log.i(TAG, "tox_notification_change_wrapper:DONE");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "tox_notification_change_wrapper:EE01:" + e.getMessage());
        }

        Log.i(TAG, "tox_notification_change_wrapper:end");
    }
}
