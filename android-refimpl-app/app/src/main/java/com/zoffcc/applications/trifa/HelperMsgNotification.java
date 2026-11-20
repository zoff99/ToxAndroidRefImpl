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

import android.os.SystemClock;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    /** Tracks whether we currently have a message notification posted. */
    private static volatile boolean msg_notification_is_posted = false;

    private static final HashMap<String, Integer> unread_count_map = new HashMap<>();

    static synchronized void set_unread_count_for_key(final String key, final int count)
    {
        if (key == null || key.length() < 1)
        {
            return;
        }

        if (count > 0)
        {
            unread_count_map.put(key, count);
        }
        else
        {
            unread_count_map.remove(key);
        }

        // Log.i(TAG, "set_unread_count_for_key: key=" + key + " count=" + count + " TOTAL=" + get_total_unread_count());

        update_msg_notification_unread_count();
    }

    static synchronized void subtract_unread_count_for_key(final String key, final int count_read)
    {
        if (key == null || key.length() < 1 || count_read <= 0)
        {
            return;
        }

        Integer current = unread_count_map.get(key);
        if (current == null)
        {
            return;
        }

        int newCount = current - count_read;
        if (newCount <= 0)
        {
            unread_count_map.remove(key);
        }
        else
        {
            unread_count_map.put(key, newCount);
        }

        Log.i(TAG, "subtract_unread_count_for_key: key=" + key + " was=" + current + " read=" + count_read + " now=" + newCount + " TOTAL=" + get_total_unread_count());

        update_msg_notification_unread_count();
    }

    static synchronized int get_unread_count_for_key(final String key)
    {
        if (key == null)
        {
            return 0;
        }
        Integer val = unread_count_map.get(key);
        return (val == null) ? 0 : val;
    }

    /*
     * action: NOTIFICATION_EDIT_ACTION
     * key: either a friend pubkey or a conference id or a group id, both as hex string representation
     */
    static synchronized void change_msg_notification(final int action, final String key,
                                                     final String notification_title, final String notification_text)
    {
        if (action == NOTIFICATION_EDIT_ACTION_CLEAR.value)
        {
            Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_CLEAR");
            global_active_notifications.clear();
            unread_count_map.clear();
            msg_notification_is_posted = false;
            remove_msg_notification();
        }
        else if (action == NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST.value)
        {
            Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST");
            // only call this when clicking on the notification to remove it
            // so the notification is now already removed!
            global_active_notifications.clear();
            unread_count_map.clear();
            msg_notification_is_posted = false;
        }
        else if (action == NOTIFICATION_EDIT_ACTION_ADD.value)
        {
            if (key != null)
            {
                if (key.length() > 1)
                {
                    Log.i(TAG, "change_msg_notification:NOTIFICATION_EDIT_ACTION_ADD");
                    global_active_notifications.add(key);

                    try
                    {
                        Integer cur = unread_count_map.get(key);
                        unread_count_map.put(key, (cur == null ? 0 : cur) + 1);
                    }
                    catch(Exception e)
                    {
                    }
                }
            }

            if (msg_notification_is_posted)
            {
                update_msg_notification_unread_count();
            }
            else
            {
                show_msg_notification(notification_title, notification_text);
            }
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
                msg_notification_is_posted = false;
                remove_msg_notification();
            }
            else
            {
                // Still have active notifications — refresh the number silently
                update_msg_notification_unread_count();
            }
        }
    }

    /**
     * Recomputes total unread count from DB and updates the currently-posted
     * message notification's number badge. Silent (no sound/vibration/alert).
     * If no notification is posted, does nothing.
     * Call this whenever messages are marked read (is_new=false) so the
     * notification number stays in sync with the red circles.
     */
    static synchronized void update_msg_notification_unread_count()
    {
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;
        // Log.i(TAG, "update_msg_notification_unread_count: posted=" + msg_notification_is_posted);

        if (!msg_notification_is_posted)
        {
            HelperGeneric.perf_log(TAG, "notif:unread-update:skip(no-notification-posted)");
            return;
        }

        final int total = get_total_unread_count();
        HelperGeneric.perf_log(TAG, "notif:unread-update total=" + total + " +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        // Log.i(TAG, "update_msg_notification_unread_count: updating notification number to " + total);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (total <= 0)
                    {
                        // All unread cleared while notification was still up — cancel it
                        msg_notification_is_posted = false;
                        NotificationManager nm = (NotificationManager) context_s.getSystemService(
                                Context.NOTIFICATION_SERVICE);
                        nm.cancel(Notification_new_message_ID);
                        return;
                    }

                    if (!PREF__notification)
                    {
                        return;
                    }

                    Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    notificationIntent.putExtra("CLEAR_NEW_MESSAGE_NOTIFICATION", "1");

                    int requestCode = (int) (System.currentTimeMillis() % 100000);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            context_s,
                            requestCode,
                            notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    // Use silent channel — this is a number-only refresh, not a new alert
                    String channelId = MainActivity.channelId_newmessage_silent;

                    NotificationCompat.Builder b = new NotificationCompat.Builder(context_s, channelId);

                    // Pre-O: explicitly suppress sound/vibrate/lights for silent refresh
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)
                    {
                        b.setSound(null);
                        b.setVibrate(null);
                        b.setLights(0, 0, 0);
                    }

                    String title = "TRIfA";
                    String text = context_s.getString(R.string.MainActivity_notification_new_message2);

                    if (PREF__notification_show_content)
                    {
                        // Keep existing title/text from last show; fall back to defaults
                        // (we don't have them here, so defaults are fine for a number refresh)
                    }

                    b.setSmallIcon(R.drawable.circle_orange)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setNumber(total)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

                    MainActivity.nmn3.notify(Notification_new_message_ID, b.build());
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

    /**
     * Sums unread message counts across friends, conferences, and groups.
     * Mirrors exactly what the red-circle Holders query at bind time.
     */
    static synchronized int get_total_unread_count()
    {
        int total = 0;
        for (Map.Entry<String, Integer> entry : unread_count_map.entrySet())
        {
            total += entry.getValue();
        }
        // Log.i(TAG, "get_total_unread_count: TOTAL=" + total);
        return total;
    }

    static void remove_msg_notification()
    {
        // Log.i(TAG, "noti_and_badge:remove_notification:");
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

    static void show_msg_notification(final String nf_title, final String nf_text)
    {
        // Log.i(TAG, "noti_and_badge:show_notification:");
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
                            msg_notification_is_posted = true;

                            Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            notificationIntent.putExtra("CLEAR_NEW_MESSAGE_NOTIFICATION", "1");

                            // FIX: Replaced the Math.random() action hack with a proper unique request code.
                            // This is the official Android way to force a PendingIntent to update without memory leaks.
                            int requestCode = (int) (System.currentTimeMillis() % 100000);
                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    context_s,
                                    requestCode,
                                    notificationIntent,
                                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            // Determine the correct channel for Android O+
                            String channelId = MainActivity.channelId_newmessage_silent; // Safe fallback for pre-O
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                            {
                                if (PREF__notification_sound && PREF__notification_vibrate) {
                                    channelId = MainActivity.channelId_newmessage_sound_and_vibrate;
                                } else if (PREF__notification_sound) {
                                    channelId = MainActivity.channelId_newmessage_sound;
                                } else if (PREF__notification_vibrate) {
                                    channelId = MainActivity.channelId_newmessage_vibrate;
                                }
                            }

                            NotificationCompat.Builder b = new NotificationCompat.Builder(context_s, channelId);

                            // Pre-O manual sound/vibrate/lights
                            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)
                            {
                                b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                if (PREF__notification_sound) {
                                    b.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                }
                                if (PREF__notification_vibrate) {
                                    b.setVibrate(new long[]{100, 300});
                                }
                            }

                            // Determine Title and Text based on privacy preferences
                            String title = "TRIfA";
                            String text = context_s.getString(R.string.MainActivity_notification_new_message2);

                            if (PREF__notification_show_content)
                            {
                                if (nf_title != null && !nf_title.isEmpty()) title = nf_title;
                                if (nf_text != null && !nf_text.isEmpty()) text = nf_text;
                            }

                            int totalUnread = get_total_unread_count();

                            b.setSmallIcon(R.drawable.circle_orange) // Kept your exact icon!
                                    .setContentTitle(title)
                                    .setContentText(text)
                                    .setNumber(totalUnread)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Tells Android this is a chat message
                                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures heads-up popup on older devices
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text)); // FIX: Expands beautifully for long messages!

                            MainActivity.nmn3.notify(Notification_new_message_ID, b.build());
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
