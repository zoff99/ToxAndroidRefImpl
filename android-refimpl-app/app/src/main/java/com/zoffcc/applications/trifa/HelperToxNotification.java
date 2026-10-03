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
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.zoffcc.applications.trifa.MainActivity.PREF__orbot_enabled;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.nmn3;
import static com.zoffcc.applications.trifa.MainActivity.notification_view;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONNECTION_STATUS_MANUAL_LOGOUT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TrifaToxService.manually_logged_out;

public class HelperToxNotification
{
    private static final String TAG = "trifa.Hlp.ToxNoti";
    static int ONGOING_NOTIFICATION_ID = 1030;

    // Helper method to get the correct text color for Dark/Light mode
    private static int getSystemTextColor(Context c) {
        int nightModeFlags = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            return Color.WHITE; // Standard light text for Dark Mode
        } else {
            return Color.parseColor("#DE000000"); // Standard Material dark text (87% opacity) for Light Mode
        }
    }

    static Notification tox_notification_setup(Context c, NotificationManager nmn2)
    {
        Log.i(TAG, "tox_notification_setup:start");

        Intent notificationIntent = new Intent(c, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        notification_view = new RemoteViews(c.getPackageName(), R.layout.custom_notification);
        Log.i(TAG, "contentView=" + notification_view);
        notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);

        // Apply dynamic system text color for Dark/Light mode compatibility
        try {
            notification_view.setTextColor(R.id.title, getSystemTextColor(c));
        } catch (Exception e_text_color) {
            Log.i(TAG, "e_text_color:EE01:" + e_text_color.getMessage());
        }

        if (PREF__orbot_enabled) {
            notification_view.setTextViewText(R.id.title, "Tox Service: OFFLINE  [Tor Proxy]");
        } else {
            notification_view.setTextViewText(R.id.title, "Tox Service: OFFLINE");
        }
        notification_view.setTextViewText(R.id.text, "");

        // Modern NotificationCompat Builder (Automatically handles API 26+ Channels)
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, MainActivity.channelId_toxservice)
                .setSmallIcon(R.drawable.circle_red_notification)
                .setCustomContentView(notification_view)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle()) // Wraps your custom view in the modern system header
                .setOngoing(true) // Permanent foreground service notification
                .setOnlyAlertOnce(true)
                .setSilent(true) // Prevents sound/vibration spam on modern Android
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent);

        Notification notification2 = b.build();

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

        Intent notificationIntent = new Intent(c, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Re-apply text color in case the system theme changed while running
        try {
            notification_view.setTextColor(R.id.title, getSystemTextColor(c));
        } catch (Exception ignored) {}

        if ((manually_logged_out) || (a_TOXCONNECTION == CONNECTION_STATUS_MANUAL_LOGOUT))
        {
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
            notification_view.setTextViewText(R.id.title, "Tox Service: OFFLINE manually");
        }
        else if (bootstrapping)
        {
            Log.i(TrifaToxService.TAG, "change_notification_fg:bootstrapping=true");
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_orange);
            if (PREF__orbot_enabled) {
                notification_view.setTextViewText(R.id.title, "Tox Service: Bootstrapping [Tor Proxy] " + message);
            } else {
                notification_view.setTextViewText(R.id.title, "Tox Service: Bootstrapping " + message);
            }
        }
        else if (a_TOXCONNECTION == 0)
        {
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
            if (PREF__orbot_enabled) {
                notification_view.setTextViewText(R.id.title, "Tox Service: OFFLINE [Tor Proxy] " + message);
            } else {
                notification_view.setTextViewText(R.id.title, "Tox Service: OFFLINE " + message);
            }
        }
        else if (PREF__orbot_enabled)
        {
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_torproxy);
            notification_view.setTextViewText(R.id.title, "Tox Service: ONLINE [Tor Proxy] " + message);
        }
        else if (a_TOXCONNECTION == 1)
        {
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
            notification_view.setTextViewText(R.id.title, "Tox Service: ONLINE [TCP] " + message);
        }
        else
        {
            notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
            notification_view.setTextViewText(R.id.title, "Tox Service: ONLINE [UDP] " + message);
        }

        notification_view.setTextViewText(R.id.text, "");

        // Build using modern NotificationCompat (No more messy SDK version branching!)
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, MainActivity.channelId_toxservice)
                .setSmallIcon(getSmallIconForState(a_TOXCONNECTION))
                .setCustomContentView(notification_view)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent);

        try {
            nmn2.notify(ONGOING_NOTIFICATION_ID, b.build());
        } catch(Exception ignored) {
        }
        Log.i(TAG, "tox_notification_change:end");
    }

    // Helper to keep the switch statement clean
    private static int getSmallIconForState(int a_TOXCONNECTION) {
        if (manually_logged_out || a_TOXCONNECTION == CONNECTION_STATUS_MANUAL_LOGOUT) return R.drawable.circle_manuallyoffline_notification;
        if (bootstrapping) return R.drawable.circle_orange_notification;
        if (a_TOXCONNECTION == 0) return R.drawable.circle_red_notification;
        if (PREF__orbot_enabled) return R.drawable.circle_torproxy_notification;
        return R.drawable.circle_green_notification; // TCP or UDP
    }

    static void tox_notification_change_wrapper(int a_TOXCONNECTION, final String message)
    {
        Log.i(TAG, "tox_notification_change_wrapper:start");
        final int a_TOXCONNECTION_f = a_TOXCONNECTION;
        final Context static_context = context_s;

        try {
            Thread t = new Thread() {
                @Override
                public void run() {
                    long counter = 0;
                    while (MainActivity.tox_service_fg == null) {
                        counter++;
                        if (counter > 10) break;
                        try { Thread.sleep(100); } catch (Exception ignored) {}
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
