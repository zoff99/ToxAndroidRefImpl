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
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.util.HashSet;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.trifa.MainActivity.Notification_new_message_ID;
import static com.zoffcc.applications.trifa.MainActivity.Notification_new_message_every_millis;
import static com.zoffcc.applications.trifa.MainActivity.Notification_new_message_last_shown_timestamp;
import static com.zoffcc.applications.trifa.MainActivity.PREF__notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__notification_show_content;
import static com.zoffcc.applications.trifa.MainActivity.PREF__notification_sound;
import static com.zoffcc.applications.trifa.MainActivity.PREF__notification_vibrate;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_ADD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_CLEAR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_REMOVE;

public class HelperMsgNotification
{
    private static final String TAG = "trifa.Hlp.Noti";

    static HashSet<String> global_active_notifications = new HashSet<String>();

    /*
     * action: NOTIFICATION_EDIT_ACTION
     * key: either a friend pubkey or a conference id or a group id, both as hex string representation
     */
    static synchronized void change_msg_notification(final int action, final String key, final String notification_text)
    {
        if (action == NOTIFICATION_EDIT_ACTION_CLEAR.value)
        {
            Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_CLEAR");
            global_active_notifications.clear();
            remove_msg_notification();
        }
        else if (action == NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST.value)
        {
            Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST");
            // only call this when clicking on the notification to remove it
            // so the notification is now already removed!
            global_active_notifications.clear();
        }
        else if (action == NOTIFICATION_EDIT_ACTION_ADD.value)
        {
            if (key != null)
            {
                if (key.length() > 1)
                {
                    Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_ADD");
                    global_active_notifications.add(key);
                }
            }
            show_msg_notification(notification_text);
        }
        else if (action == NOTIFICATION_EDIT_ACTION_REMOVE.value)
        {
            Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_REMOVE");
            if (key != null)
            {
                if (key.length() > 1)
                {
                    global_active_notifications.remove(key);
                }
            }

            if (global_active_notifications.isEmpty())
            {
                remove_msg_notification();
            }
        }
    }

    static void remove_msg_notification()
    {
        Log.i(TAG, "noti_and_badge:remove_notification:");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    NotificationManager notificationManager = (NotificationManager) context_s.getSystemService(
                            Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(Notification_new_message_ID);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        try
        {
            if (main_handler_s != null)
            {
                main_handler_s.post(myRunnable);
            }
        }
        catch (Exception e)
        {
        }
    }

    static void show_msg_notification(final String nf_text)
    {
        Log.i(TAG, "noti_and_badge:show_notification:");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // allow notification every n seconds
                    if ((Notification_new_message_last_shown_timestamp + Notification_new_message_every_millis) <
                        System.currentTimeMillis())
                    {
                        if (PREF__notification)
                        {
                            Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                            Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            notificationIntent.setAction(
                                    "com.zoffcc.applications.trifa." + (long) (Math.random() * 100000));
                            notificationIntent.putExtra("CLEAR_NEW_MESSAGE_NOTIFICATION", "1");
                            PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0, notificationIntent,
                                                                                    PendingIntent.FLAG_IMMUTABLE);
                            // -- notification ------------------
                            // -- notification -----------------
                            NotificationCompat.Builder b;

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                            {
                                if ((PREF__notification_sound) && (PREF__notification_vibrate))
                                {
                                    b = new NotificationCompat.Builder(context_s,
                                                                       MainActivity.channelId_newmessage_sound_and_vibrate);
                                }
                                else if ((PREF__notification_sound) && (!PREF__notification_vibrate))
                                {
                                    b = new NotificationCompat.Builder(context_s,
                                                                       MainActivity.channelId_newmessage_sound);
                                }
                                else if ((!PREF__notification_sound) && (PREF__notification_vibrate))
                                {
                                    b = new NotificationCompat.Builder(context_s,
                                                                       MainActivity.channelId_newmessage_vibrate);
                                }
                                else
                                {
                                    b = new NotificationCompat.Builder(context_s,
                                                                       MainActivity.channelId_newmessage_silent);
                                }
                            }
                            else
                            {
                                b = new NotificationCompat.Builder(context_s);
                            }

                            b.setContentIntent(pendingIntent);
                            b.setSmallIcon(R.drawable.circle_orange);
                            b.setLights(Color.parseColor("#ffce00"), 500, 500);
                            Uri default_notification_sound = RingtoneManager.getDefaultUri(
                                    RingtoneManager.TYPE_NOTIFICATION);

                            if (PREF__notification_sound)
                            {
                                b.setSound(default_notification_sound);
                            }

                            if (PREF__notification_vibrate)
                            {
                                long[] vibrate_pattern = {100, 300};
                                b.setVibrate(vibrate_pattern);
                            }

                            b.setContentTitle("TRIfA");
                            b.setAutoCancel(true);
                            if (PREF__notification_show_content)
                            {
                                if ((nf_text != null) && (!nf_text.isEmpty()))
                                {
                                    b.setContentText(nf_text);
                                }
                                else
                                {
                                    b.setContentText(context_s.getString(R.string.MainActivity_notification_new_message2));
                                }
                            }
                            else
                            {
                                b.setContentText(context_s.getString(R.string.MainActivity_notification_new_message2));
                            }
                            Notification notification3 = b.build();
                            MainActivity.nmn3.notify(Notification_new_message_ID, notification3);
                            // -- notification ------------------
                            // -- notification ------------------
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        try
        {
            if (main_handler_s != null)
            {
                main_handler_s.post(myRunnable);
            }
        }
        catch (Exception e)
        {
        }
    }
}
