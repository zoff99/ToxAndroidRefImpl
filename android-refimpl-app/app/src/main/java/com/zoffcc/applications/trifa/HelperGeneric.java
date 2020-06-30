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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Detector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static android.webkit.MimeTypeMap.getFileExtensionFromUrl;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.native_aec_lib_ready;
import static com.zoffcc.applications.trifa.CallingActivity.feed_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.fetch_from_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.global_sps_pps_nal_unit_bytes;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames_current;
import static com.zoffcc.applications.trifa.CallingActivity.set_vdelay_every_x_frames;
import static com.zoffcc.applications.trifa.CallingActivity.set_vdelay_every_x_frames_current;
import static com.zoffcc.applications.trifa.Callstate.java_video_encoder_first_frame_in;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.ProfileActivity.update_toxid_display_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_H264;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_CLIENT_VIDEO_CAPTURE_DELAY_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILETRANSFER_SIZE_MSGV2;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperGeneric
{
    /*
     all stuff here should be moved somewhere else at some point
     */

    private static final String TAG = "trifa.Hlp.Generic";

    static long video_frame_age_mean = 0;
    static int video_frame_age_values_cur_index = 0;
    final static int video_frame_age_values_cur_index_count = 10;
    static long[] video_frame_age_values = new long[video_frame_age_values_cur_index_count];
    static byte[] buf_video_send_frame = null;

    public static void clearCache_s()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    clearCache(MainActivity.context_s);
                }
                catch (Exception e)
                {
                }
            }
        };

        try
        {
            if (MainActivity.main_handler_s != null)
            {
                MainActivity.main_handler_s.post(myRunnable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void clearCache(final Context c)
    {
        Log.i(TAG, "clearCache");

        try
        {
            Glide.get(c).clearMemory();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "clearCache:EE2:" + e.getMessage());
        }

        // ------clear Glide image cache------
        final Thread t_glide_clean_cache = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "clearCache:bg:start");
                    File cacheDir = Glide.getPhotoCacheDir(c);

                    if (cacheDir.isDirectory())
                    {
                        for (File child : cacheDir.listFiles())
                        {
                            if (!child.delete())
                            {
                            }
                            else
                            {
                                // Log.i(TAG, "clearCache:" + child.getAbsolutePath());
                            }
                        }
                    }

                    Glide.get(c).clearDiskCache();
                    Log.i(TAG, "clearCache:bg:end");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "clearCache:EE1:" + e.getMessage());
                }
            }
        };
        t_glide_clean_cache.start();
        // ------clear Glide image cache------
    }

    public static void cleanup_temp_dirs()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "cleanup_temp_dirs:---START---");

                try
                {
                    Thread.sleep(400);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (MainActivity.VFS_ENCRYPT)
                    {
                        Log.i(TAG, "cleanup_temp_dirs:001");
                        vfs_deleteFilesAndFilesSubDirectories_vfs(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                        Log.i(TAG, "cleanup_temp_dirs:002");
                    }
                    else
                    {
                        Log.i(TAG, "cleanup_temp_dirs:003");
                        vfs_deleteFilesAndFilesSubDirectories_real(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                        Log.i(TAG, "cleanup_temp_dirs:004");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    vfs_deleteFilesAndFilesSubDirectories_real(MainActivity.SD_CARD_TMP_DIR + "/");
                }
                catch (Exception e)
                {
                    e.getMessage();
                }

                Log.i(TAG, "cleanup_temp_dirs:---READY---");
            }
        };
        t.start();
    }

    public static void vfs_deleteFilesAndFilesSubDirectories_real(String directoryName)
    {
        File directory1 = new File(directoryName);
        File[] fList1 = directory1.listFiles();

        for (File file : fList1)
        {
            if (file.isFile())
            {
                // Log.i(TAG, "VFS:REAL:rm:" + file);
                file.delete();
            }
            else if (file.isDirectory())
            {
                // Log.i(TAG, "VFS:REAL:rm:D:" + file);
                vfs_deleteFilesAndFilesSubDirectories_real(file.getAbsolutePath());
                file.delete();
            }
        }
    }

    public static void vfs_deleteFilesAndFilesSubDirectories_vfs(String directoryName)
    {
        if (MainActivity.VFS_ENCRYPT)
        {
            Log.i(TAG, "cleanup_temp_dirs:00a");
            info.guardianproject.iocipher.File directory1 = new info.guardianproject.iocipher.File(directoryName);
            info.guardianproject.iocipher.File[] fList1 = directory1.listFiles();

            for (info.guardianproject.iocipher.File file : fList1)
            {
                if (file.isFile())
                {
                    // Log.i(TAG, "VFS:VFS:rm:" + file);
                    file.delete();
                }
                else if (file.isDirectory())
                {
                    // Log.i(TAG, "VFS:VFS:rm:D:" + file);
                    vfs_deleteFilesAndFilesSubDirectories_vfs(file.getAbsolutePath());
                    file.delete();
                }
            }

            Log.i(TAG, "cleanup_temp_dirs:00b");
        }
    }

    static void conference_message_add_from_sync(long conference_number, long peer_number2, String peer_pubkey, int a_TOX_MESSAGE_TYPE, String message, long length, long sent_timestamp_in_ms)
    {
        // Log.i(TAG, "conference_message_add_from_sync:cf_num=" + conference_number + " pnum=" + peer_number2 + " msg=" +
        //            message);

        int res = -1;
        if (peer_number2 == -1)
        {
            res = -1;
        }
        else
        {
            res = MainActivity.tox_conference_peer_number_is_ours(conference_number, peer_number2);
        }

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            // Log.i(TAG, "conference_message_add_from_sync:own peer");
            return;
        }

        boolean do_notification = true;
        boolean do_badge_update = true;
        String conf_id = "-1";
        ConferenceDB conf_temp = null;

        try
        {
            // TODO: cache me!!
            conf_temp = orma.selectFromConferenceDB().
                    tox_conference_numberEq(conference_number).
                    and().
                    conference_activeEq(true).toList().get(0);
            conf_id = conf_temp.conference_identifier;
            // Log.i(TAG, "conference_message_add_from_sync:conf_id=" + conf_id);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            if (conf_temp.notification_silent)
            {
                do_notification = false;
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        if (MainActivity.conference_message_list_activity != null)
        {
            // Log.i(TAG, "conference_message_add_from_sync:noti_and_badge:002conf:" +
            //            conference_message_list_activity.get_current_conf_id() + ":" + conf_id);

            if (MainActivity.conference_message_list_activity.get_current_conf_id().equals(conf_id))
            {
                // Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }
        }

        ConferenceMessage m = new ConferenceMessage();
        m.is_new = do_badge_update;
        // m.tox_friendnum = friend_number;
        m.tox_peerpubkey = peer_pubkey;
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_peername = null;
        m.conference_identifier = conf_id;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.sent_timestamp = sent_timestamp_in_ms;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = message;

        try
        {
            m.tox_peername = HelperConference.tox_conference_peer_get_name__wrapper(m.conference_identifier,
                                                                                    m.tox_peerpubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (MainActivity.conference_message_list_activity != null)
        {
            if (MainActivity.conference_message_list_activity.get_current_conf_id().equals(conf_id))
            {
                HelperConference.insert_into_conference_message_db(m, true);
            }
            else
            {
                HelperConference.insert_into_conference_message_db(m, false);
            }
        }
        else
        {
            long new_msg_id = HelperConference.insert_into_conference_message_db(m, false);
            // Log.i(TAG, "conference_message_add_from_sync:new_msg_id=" + new_msg_id);
        }

        HelperConference.update_single_conference_in_friendlist_view(conf_temp);

        if (do_notification)
        {
            // Log.i(TAG, "noti_and_badge:005conf:");
            // start "new" notification
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // allow notification every n seconds
                        if ((MainActivity.Notification_new_message_last_shown_timestamp +
                             MainActivity.Notification_new_message_every_millis) < System.currentTimeMillis())
                        {
                            if (MainActivity.PREF__notification)
                            {
                                MainActivity.Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                Intent notificationIntent = new Intent(MainActivity.context_s,
                                                                       StartMainActivityWrapper.class);
                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.context_s, 0,
                                                                                        notificationIntent, 0);
                                // -- notification ------------------
                                // -- notification -----------------
                                NotificationCompat.Builder b;

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                {
                                    if ((MainActivity.PREF__notification_sound) &&
                                        (MainActivity.PREF__notification_vibrate))
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                           MainActivity.channelId_newmessage_sound_and_vibrate);
                                    }
                                    else if ((MainActivity.PREF__notification_sound) &&
                                             (!MainActivity.PREF__notification_vibrate))
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                           MainActivity.channelId_newmessage_sound);
                                    }
                                    else if ((!MainActivity.PREF__notification_sound) &&
                                             (MainActivity.PREF__notification_vibrate))
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                           MainActivity.channelId_newmessage_vibrate);
                                    }
                                    else
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                           MainActivity.channelId_newmessage_silent);
                                    }
                                }
                                else
                                {
                                    b = new NotificationCompat.Builder(MainActivity.context_s);
                                }

                                b.setContentIntent(pendingIntent);
                                b.setSmallIcon(R.drawable.circle_orange);
                                b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                Uri default_notification_sound = RingtoneManager.getDefaultUri(
                                        RingtoneManager.TYPE_NOTIFICATION);

                                if (MainActivity.PREF__notification_sound)
                                {
                                    b.setSound(default_notification_sound);
                                }

                                if (MainActivity.PREF__notification_vibrate)
                                {
                                    long[] vibrate_pattern = {100, 300};
                                    b.setVibrate(vibrate_pattern);
                                }

                                b.setContentTitle(MainActivity.context_s.getString(
                                        R.string.MainActivity_notification_new_message_title));
                                b.setAutoCancel(true);
                                b.setContentText(MainActivity.context_s.getString(
                                        R.string.MainActivity_notification_new_message));
                                Notification notification3 = b.build();
                                MainActivity.nmn3.notify(MainActivity.Notification_new_message_ID, notification3);
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
                if (MainActivity.main_handler_s != null)
                {
                    MainActivity.main_handler_s.post(myRunnable);
                }
            }
            catch (Exception e)
            {
            }
        }

    }

    public static void update_friend_connection_status_helper(int a_TOX_CONNECTION, FriendList f, boolean from_relay)
    {
        // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:ENTER");

        final long friend_number_ = tox_friend_by_public_key__wrapper(f.tox_public_key_string);
        boolean went_online = false;

        if (f.TOX_CONNECTION != a_TOX_CONNECTION)
        {
            if ((!from_relay) && (!HelperRelay.is_any_relay(f.tox_public_key_string)))
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    send_avatar_to_friend(tox_friend_by_public_key__wrapper(f.tox_public_key_string));
                }
            }

            if (a_TOX_CONNECTION == TOX_CONNECTION_NONE.value)
            {
                // ******** friend going offline ********
                // Log.i(TAG, "friend_connection_status:friend going offline:" + System.currentTimeMillis());
            }
            else
            {
                went_online = true;
                // ******** friend coming online ********
                // Log.i(TAG, "friend_connection_status:friend coming online:" + LAST_ONLINE_TIMSTAMP_ONLINE_NOW);
            }
        }

        if (went_online)
        {
            // Log.i(TAG, "friend_connection_status:friend status seems: ONLINE");
            f.last_online_timestamp = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
            HelperFriend.update_friend_in_db_last_online_timestamp(f);
            f.TOX_CONNECTION = a_TOX_CONNECTION;
            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
            HelperFriend.update_friend_in_db_connection_status(f);

            try
            {
                if (MainActivity.message_list_activity != null)
                {
                    if (MainActivity.message_list_activity.get_current_friendnum() == friend_number_)
                    {
                        MainActivity.message_list_activity.set_friend_connection_status_icon();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            HelperFriend.add_all_friends_clear_wrapper(0);
        }
        else // went offline -------------------
        {
            // check for combined online status of (friend + possible relay)
            int status_new = a_TOX_CONNECTION;
            int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string, status_new);
            // Log.i(TAG, "friend_connection_status:friend status combined con status:" + combined_connection_status_);

            if (get_toxconnection_wrapper(combined_connection_status_) == TOX_CONNECTION_NONE.value)
            {
                // Log.i(TAG, "friend_connection_status:friend status combined: OFFLINE");
                f.last_online_timestamp = System.currentTimeMillis();
                HelperFriend.update_friend_in_db_last_online_timestamp(f);
                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status(f);

                try
                {
                    if (MainActivity.message_list_activity != null)
                    {
                        if (MainActivity.message_list_activity.get_current_friendnum() == friend_number_)
                        {
                            MainActivity.message_list_activity.set_friend_connection_status_icon();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                HelperFriend.add_all_friends_clear_wrapper(0);
            }
            else
            {
                // Log.i(TAG, "friend or relay offline, combined still ONLINE");
                HelperFriend.update_single_friend_in_friendlist_view(f);
            }
        }
    }

    public static void send_avatar_to_friend(final long friend_number_)
    {
        final Thread new_thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (MainActivity.VFS_ENCRYPT)
                    {
                        String fname = get_vfs_image_filename_own_avatar();

                        // Log.i(TAG, "send_avatar_to_friend:own_avatar_filename:" + fname);

                        if (fname != null)
                        {
                            ByteBuffer avatar_bytes = file_to_bytebuffer(fname, true);

                            // Log.i(TAG, "send_avatar_to_friend:avatar_bytes:" + avatar_bytes);

                            if (avatar_bytes != null)
                            {
                                // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:avatar_bytes=" + bytes_to_hex(avatar_bytes));
                                ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
                                int res = MainActivity.tox_hash(hash_bytes, avatar_bytes, avatar_bytes.capacity());

                                // Log.i(TAG, "send_avatar_to_friend:tox_hash:res=" + res);


                                if (res == 0)
                                {
                                    // Log.i(TAG,
                                    //       "android_tox_callback_friend_connection_status_cb_method:hash(1)=" +
                                    //       bytes_to_hex(hash_bytes));
                                    // send avatar to friend -------


                                    String avatar_filename_for_remote =
                                            "avatar" + get_g_opts("VFS_OWN_AVATAR_FILE_EXTENSION");

                                    long filenum = MainActivity.tox_file_send(friend_number_,
                                                                              TOX_FILE_KIND_AVATAR.value,
                                                                              avatar_bytes.capacity(), hash_bytes,
                                                                              avatar_filename_for_remote,
                                                                              avatar_filename_for_remote.length());
                                    //Log.i(TAG, "send_avatar_to_friend:filenum=" + filenum + " fname=" +
                                    //           avatar_filename_for_remote);
                                    // save FT to db ---------------
                                    Filetransfer ft_avatar_outgoing = new Filetransfer();
                                    ft_avatar_outgoing.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(
                                            friend_number_);
                                    ft_avatar_outgoing.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
                                    ft_avatar_outgoing.file_number = filenum;
                                    ft_avatar_outgoing.kind = TOX_FILE_KIND_AVATAR.value;
                                    ft_avatar_outgoing.filesize = avatar_bytes.capacity();
                                    long rowid = HelperFiletransfer.insert_into_filetransfer_db(ft_avatar_outgoing);
                                    ft_avatar_outgoing.id = rowid;
                                }
                                else
                                {
                                    // Log.i(TAG, "send_avatar_to_friend:tox_hash res=" + res);
                                }
                            }
                        }
                    }
                    else
                    {
                        // TODO: write code
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "send_avatar_to_friend:EE01:" + e.getMessage());
                }
            }
        };
        new_thread.start();
    }

    public static void set_message_accepted_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_vfs_file(String vfs_path_name, String vfs_file_name)
    {
        info.guardianproject.iocipher.File f1 = null;

        try
        {
            f1 = new info.guardianproject.iocipher.File(vfs_path_name + "/" + vfs_file_name);

            if (f1.length() > 0)
            {
                f1.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_display_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        // TODO: update entry in main friendlist (if visible)
        //       or in chat view (if visible)
        HelperFriend.update_single_friend_in_friendlist_view(
                main_get_friend(tox_friend_by_public_key__wrapper(friend_pubkey)));
    }

    static void move_tmp_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        // Log.i(TAG, "move_tmp_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" +
        //           dst_file_name);
        try
        {
            if (MainActivity.VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(
                        src_path_name + "/" + src_file_name);
                info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(
                        dst_path_name + "/" + dst_file_name);
                info.guardianproject.iocipher.File dst_dir = new info.guardianproject.iocipher.File(
                        dst_path_name + "/");
                dst_dir.mkdirs();
                f1.renameTo(f2);
            }
            else
            {
                File f1 = new File(src_path_name + "/" + src_file_name);
                File f2 = new File(dst_path_name + "/" + dst_file_name);
                File dst_dir = new File(dst_path_name + "/");
                dst_dir.mkdirs();
                f1.renameTo(f2);
            }

            Log.i(TAG, "move_tmp_file_to_real_file:OK");
        }
        catch (Exception e)
        {
            Log.i(TAG, "move_tmp_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static String get_uniq_tmp_filename(String filename_with_path, long filesize)
    {
        String ret = null;

        try
        {
            java.security.MessageDigest md5_ = java.security.MessageDigest.getInstance("MD5");
            byte[] md5_digest = md5_.digest((filesize + ":" + filename_with_path).getBytes());
            BigInteger bigInt = new BigInteger(1, md5_digest);
            StringBuilder hashtext = new StringBuilder(bigInt.toString(16));

            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32)
            {
                hashtext.insert(0, "0");
            }

            ret = hashtext.toString();
            // Log.i(TAG, "get_uniq_tmp_filename:ret=" + ret);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_uniq_tmp_filename:EE:" + e.getMessage());
            ret = "temp__" + System.currentTimeMillis() + (int) (Math.random() * 10000d);
        }

        return ret;
    }

    static void copy_real_file_to_vfs_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        Log.i(TAG, "copy_real_file_to_vfs_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" +
                   dst_file_name);

        try
        {
            if (MainActivity.VFS_ENCRYPT)
            {
                File f_real = new File(src_path_name + "/" + src_file_name);
                String uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length());
                Log.i(TAG, "copy_real_file_to_vfs_file:uniq_temp_filename=" + uniq_temp_filename);
                info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(
                        VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + uniq_temp_filename);
                info.guardianproject.iocipher.File dst_dir = new info.guardianproject.iocipher.File(
                        VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                dst_dir.mkdirs();
                java.io.FileInputStream is = null;
                info.guardianproject.iocipher.FileOutputStream os = null;

                try
                {
                    is = new java.io.FileInputStream(f_real);
                    os = new info.guardianproject.iocipher.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }

                move_tmp_file_to_real_file(VFS_PREFIX + VFS_TMP_FILE_DIR, uniq_temp_filename, dst_path_name,
                                           dst_file_name);
            }
            else
            {
                File f_real = new File(src_path_name + "/" + src_file_name);
                String uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length());
                Log.i(TAG, "copy_real_file_to_vfs_file:uniq_temp_filename=" + uniq_temp_filename);
                File f2 = new File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + uniq_temp_filename);
                File dst_dir = new File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                dst_dir.mkdirs();
                java.io.FileInputStream is = null;
                java.io.FileOutputStream os = null;

                try
                {
                    is = new java.io.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }

                move_tmp_file_to_real_file(VFS_PREFIX + VFS_TMP_FILE_DIR, uniq_temp_filename, dst_path_name,
                                           dst_file_name);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "copy_real_file_to_vfs_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void change_notification(int a_TOXCONNECTION, final String message)
    {
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash_app_java(1);
        // crash_app_C();
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        Log.i(TAG, "change_notification");
        final int a_TOXCONNECTION_f = a_TOXCONNECTION;

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

                    Log.i(TAG, "change_notification:change");

                    try
                    {
                        MainActivity.tox_service_fg.change_notification_fg(a_TOXCONNECTION_f, message);
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
        }
    }

    static String make_some_static_dummy_file(Context context)
    {
        String ret = null;

        try
        {
            File dst_dir = new File(MainActivity.SD_CARD_STATIC_DIR + "/");
            dst_dir.mkdirs();
            File fout = new File(MainActivity.SD_CARD_STATIC_DIR + "/" + "__dummy__dummy_.jpg");
            java.io.FileOutputStream os = new java.io.FileOutputStream(fout);
            //            int len = 2 + 2 + 2 + 5 + 2 + 1 + 2 + 2;
            //            byte[] buffer = new byte[len];
            //
            //            int a = 0;
            //            buffer[a] = (byte) 0xff;
            //            a++;
            //            buffer[a] = (byte) 0xd8;
            //            a++;
            //
            //            buffer[a] = (byte) 0xff;
            //            a++;
            //            buffer[a] = (byte) 0xe0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x10;
            //            a++;
            //
            //            buffer[a] = (byte) 0x4a;
            //            a++;
            //            buffer[a] = (byte) 0x46;
            //            a++;
            //            buffer[a] = (byte) 0x49;
            //            a++;
            //            a++;
            //            buffer[a] = (byte) 0x46;
            //            a++;
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x01;
            //            a++;
            //            buffer[a] = (byte) 0x02;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x0a;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x0a;
            //            a++;
            //
            //            os.write(buffer, 0, len);
            //            os.close();
            java.io.InputStream ins = context.getResources().
                    openRawResource(context.getResources().
                            getIdentifier("ic_plus_sign", "drawable", context.getPackageName()));
            byte[] buffer = new byte[1024];
            int length;

            while ((length = ins.read(buffer)) > 0)
            {
                os.write(buffer, 0, length);
            }

            ins.close();
            os.close();
            ret = fout.getAbsolutePath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    static String copy_vfs_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String appl)
    {
        String uniq_temp_filename = null;

        try
        {
            if (MainActivity.VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f_real = new info.guardianproject.iocipher.File(
                        src_path_name + "/" + src_file_name);
                uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length()) + appl;
                //Log.i(TAG,
                //      "copy_vfs_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name +
                //      "/" + uniq_temp_filename);
                File f2 = new File(dst_path_name + "/" + uniq_temp_filename);
                File dst_dir = new File(dst_path_name + "/");
                dst_dir.mkdirs();
                info.guardianproject.iocipher.FileInputStream is = null;
                java.io.FileOutputStream os = null;

                if (!f_real.exists())
                {
                    // Log.i(TAG,
                    //      "copy_vfs_file_to_real_file:" + src_path_name + "/" + src_file_name + " : does not exist");
                    return null;
                }

                try
                {
                    is = new info.guardianproject.iocipher.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "copy_vfs_file_to_real_file:EE:"); // + e.getMessage());
            // e.printStackTrace();
        }

        return uniq_temp_filename;
    }

    static void export_vfs_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        try
        {
            if (MainActivity.VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f_real = new info.guardianproject.iocipher.File(
                        src_path_name + "/" + src_file_name);
                File f2 = new File(dst_path_name + "/" + dst_file_name);
                File dst_dir = new File(dst_path_name + "/");
                dst_dir.mkdirs();
                info.guardianproject.iocipher.FileInputStream is = null;
                java.io.FileOutputStream os = null;

                try
                {
                    is = new info.guardianproject.iocipher.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "export_vfs_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static String get_vfs_image_filename_own_avatar()
    {
        return get_g_opts("VFS_OWN_AVATAR_FNAME");
    }

    static String get_vfs_image_filename_friend_avatar(String friend_pubkey)
    {
        try
        {
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).toList().get(0);
            return f.avatar_pathname + "/" + f.avatar_filename;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static String get_vfs_image_filename_friend_avatar(long friendnum)
    {
        try
        {
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friendnum)).toList().get(0);

            if (f.avatar_pathname == null)
            {
                return null;
            }

            if (f.avatar_filename == null)
            {
                return null;
            }

            return f.avatar_pathname + "/" + f.avatar_filename;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static Drawable get_drawable_from_vfs_image(String vfs_image_filename)
    {
        try
        {
            if (MainActivity.VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
                info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(
                        f1);
                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());
                return new BitmapDrawable(BitmapFactory.decodeByteArray(byteArray, 0, (int) f1.length()));
            }
            else
            {
                File f1 = new File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);
                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());
                return new BitmapDrawable(BitmapFactory.decodeByteArray(byteArray, 0, (int) f1.length()));
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static void put_vfs_image_on_imageview_real(Context c, ImageView v, Drawable placholder, String vfs_image_filename, boolean force_update, boolean is_friend_avatar, FriendList fl)
    {
        try
        {
            // Log.i(TAG, "put_vfs_image_on_imageview:" + vfs_image_filename);
            if (MainActivity.VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
                // info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                //byte[] byteArray = new byte[(int) f1.length()];
                // fis.read(byteArray, 0, (int) f1.length());

                if (placholder == null)
                {
                    if (is_friend_avatar)
                    {
                        GlideApp.
                                with(c).
                                load(f1).
                                placeholder(R.drawable.round_loading_animation).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                signature(new com.bumptech.glide.signature.StringSignatureZ(
                                        "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                        fl.avatar_update_timestamp)).
                                skipMemoryCache(false).
                                into(v);
                    }
                    else
                    {
                        GlideApp.
                                with(c).
                                load(f1).
                                placeholder(R.drawable.round_loading_animation).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(force_update).
                                into(v);
                    }
                }
                else
                {
                    if (is_friend_avatar)
                    {
                        GlideApp.
                                with(c).
                                load(f1).
                                placeholder(placholder).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                signature(new com.bumptech.glide.signature.StringSignatureZ(
                                        "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                        fl.avatar_update_timestamp)).
                                skipMemoryCache(false).
                                into(v);
                    }
                    else
                    {
                        GlideApp.
                                with(c).
                                load(f1).
                                placeholder(placholder).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(force_update).
                                into(v);
                    }
                }
            }
            else
            {
                File f1 = new File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);
                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());
                GlideApp.
                        with(c).
                        load(byteArray).
                        placeholder(placholder).
                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        skipMemoryCache(force_update).
                        into(v);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "put_vfs_image_on_imageview:EE1:" + e.getMessage());
        }
    }

    static String get_g_opts(String key)
    {
        try
        {
            if (orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobalsNew g_opts = orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).get(0);
                return g_opts.value;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_g_opts:EE1:" + e.getMessage());
            return null;
        }
    }

    static void set_g_opts(String key, String value)
    {
        try
        {
            TRIFADatabaseGlobalsNew g_opts = new TRIFADatabaseGlobalsNew();
            g_opts.key = key;
            g_opts.value = value;

            try
            {
                orma.insertIntoTRIFADatabaseGlobalsNew(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + value);
            }
            catch (android.database.sqlite.SQLiteConstraintException | net.sqlcipher.database.SQLiteConstraintException e)
            {
                e.printStackTrace();

                try
                {
                    orma.updateTRIFADatabaseGlobalsNew().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + value);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e.getMessage());
        }
    }

    static int add_tcp_relay_single_wrapper(String ip, long port, String key_hex)
    {
        return MainActivity.add_tcp_relay_single(ip, key_hex, port);
    }

    static int bootstrap_single_wrapper(String ip, long port, String key_hex)
    {
        return MainActivity.bootstrap_single(ip, key_hex, port);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dp2px(float dp)
    {
        try
        {
            float px = dp * ((float) MainActivity.metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            return px;
        }
        catch (Exception e)
        {
            // if there is an error, just return the input value!!
            e.printStackTrace();
            return dp;
        }
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float px2dp(float px)
    {
        float dp = px / ((float) MainActivity.metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void update_fps()
    {
        if ((MainActivity.last_updated_fps + MainActivity.update_fps_every_ms) < System.currentTimeMillis())
        {
            MainActivity.last_updated_fps = System.currentTimeMillis();

            // these were updated: VIDEO_FRAME_RATE_INCOMING, VIDEO_FRAME_RATE_OUTGOING
            try
            {
                if (CallingActivity.ca != null)
                {
                    if (CallingActivity.ca.callactivity_handler != null)
                    {
                        final Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    CallingActivity.ca.right_top_text_3.setText(
                                            "IN   " + VIDEO_FRAME_RATE_INCOMING + " fps");
                                    CallingActivity.ca.right_top_text_4.setText(
                                            "Out " + VIDEO_FRAME_RATE_OUTGOING + " fps");
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                        if (CallingActivity.ca.callactivity_handler != null)
                        {
                            CallingActivity.ca.callactivity_handler.post(myRunnable);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void update_bitrates()
    {
        // these were updated: Callstate.audio_bitrate, Callstate.video_bitrate
        try
        {
            if (CallingActivity.ca != null)
            {
                if (CallingActivity.ca.callactivity_handler != null)
                {
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                CallingActivity.ca.right_top_text_1.setText(
                                        "O:" + Callstate.codec_to_str(Callstate.video_out_codec) + ":" +
                                        Callstate.video_bitrate);
                                CallingActivity.ca.right_top_text_1b.setText(
                                        "I:" + Callstate.codec_to_str(Callstate.video_in_codec) + ":" +
                                        Callstate.video_in_bitrate);
                                if (native_aec_lib_ready)
                                {
                                    CallingActivity.ca.right_top_text_2.setText(
                                            "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay + "e");
                                }
                                else
                                {
                                    CallingActivity.ca.right_top_text_2.setText(
                                            "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay);
                                }
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }

                            try
                            {
                                CallingActivity.top_text_line.setText(
                                        Callstate.friend_alias_name + " " + Callstate.round_trip_time + "/" +
                                        Callstate.play_delay + "/" + Callstate.play_buffer_entries);
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }

                        }
                    };

                    if (CallingActivity.ca.callactivity_handler != null)
                    {
                        CallingActivity.ca.callactivity_handler.post(myRunnable);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String format_timeduration_from_seconds(long seconds)
    {
        String positive = "";
        final long absSeconds = Math.abs(seconds);
        // Log.i(TAG,"format_timeduration_from_seconds:seconds="+seconds+" absSeconds="+absSeconds);
        int hours = (int) (absSeconds / 3600);

        if (hours < 1)
        {
            positive = String.format("%02d:%02d", (absSeconds % 3600) / 60, absSeconds % 60);
        }
        else
        {
            positive = String.format("%d:%02d:%02d", hours, (absSeconds % 3600) / 60, absSeconds % 60);
        }

        return seconds < 0 ? "-" + positive : positive;
    }

    public static ByteBuffer string_to_bytebuffer(String input_chars, int output_number_of_bytes)
    {
        try
        {
            ByteBuffer ret = ByteBuffer.allocateDirect(output_number_of_bytes);
            ret.rewind();
            ret.put(input_chars.getBytes());
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static ByteBuffer file_to_bytebuffer(String filename_with_fullpath, boolean is_vfs)
    {
        if (is_vfs)
        {
            // Log.i(TAG, "file_to_bytebuffer:001");

            info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(filename_with_fullpath);
            int size = (int) file.length();
            // Log.i(TAG, "file_to_bytebuffer:002:size=" + size);
            ByteBuffer ret = ByteBuffer.allocateDirect(size);
            byte[] bytes = new byte[size];

            try
            {
                BufferedInputStream buf = new BufferedInputStream(
                        new info.guardianproject.iocipher.FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
                ret = ret.put(bytes);
                return ret;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "file_to_bytebuffer:EE01:" + e.getMessage());
            }
        }

        Log.i(TAG, "file_to_bytebuffer:EE99:NULL");
        return null;
    }

    public static String bytesToHex(byte[] bytes, int start, int len)
    {
        char[] hexChars = new char[(len) * 2];
        // System.out.println("blen=" + (len));

        for (int j = start; j < (start + len); j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = MainActivity.hexArray[v >>> 4];
            hexChars[(j - start) * 2 + 1] = MainActivity.hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static String bytes_to_hex(ByteBuffer in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();

            for (byte b : in.array())
            {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    public static String bytes_to_hex(byte[] in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();

            for (byte b : in)
            {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    public static byte[] hex_to_bytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static com.bumptech.glide.load.Key StringSignature2(final String in)
    {
        com.bumptech.glide.load.Key ret = new StringObjectKey(in);
        return ret;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static PackageInfo get_my_pkg_info()
    {
        return MainActivity.packageInfo_s;
    }

    static String get_network_connections()
    {
        try
        {
            Detector.updateReportMap();
            return Collector.updateReports();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "ERROR_getting_network_connections";
        }
    }

    static String long_date_time_format(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_time_long.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "_Datetime_ERROR_";
        }
    }

    static String long_date_time_format_or_empty(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_time_long.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    static String only_date_time_format(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_only.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "_Datetime_ERROR_";
        }
    }

    static void waiting_for_orbot_info(final boolean enable)
    {
        if (enable)
        {
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Log.i(TAG, "waiting_for_orbot_info:" + enable);
                        MainActivity.waiting_view.setVisibility(View.VISIBLE);
                        MainActivity.waiting_image.setVisibility(View.VISIBLE);
                        MainActivity.normal_container.setVisibility(View.INVISIBLE);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "waiting_for_orbot_info:EE:" + e.getMessage());
                    }
                }
            };

            if (MainActivity.main_handler_s != null)
            {
                MainActivity.main_handler_s.post(myRunnable);
            }
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
                        Log.i(TAG, "waiting_for_orbot_info:" + enable);
                        MainActivity.waiting_view.setVisibility(View.GONE);
                        MainActivity.waiting_image.setVisibility(View.GONE);
                        MainActivity.normal_container.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "waiting_for_orbot_info:EE:" + e.getMessage());
                    }
                }
            };

            if (MainActivity.main_handler_s != null)
            {
                MainActivity.main_handler_s.post(myRunnable);
            }
        }
    }

    static byte[] read_chunk_from_SD_file(String file_name_with_path, long position, long file_chunk_length)
    {
        byte[] out = new byte[(int) file_chunk_length];

        try
        {
            RandomAccessFile raf = new RandomAccessFile(file_name_with_path, "r");
            FileChannel inChannel = raf.getChannel();
            MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, position, file_chunk_length);

            // Log.i(TAG, "read_chunk_from_SD_file:" + buffer.limit() + " <-> " + file_chunk_length);

            for (int i = 0; i < buffer.limit(); i++)
            {
                out[i] = buffer.get();
            }

            try
            {
                inChannel.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                raf.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return out;
    }

    static void write_chunk_to_VFS_file(String file_name_with_path, long position, long file_chunk_length, ByteBuffer data)
    {
        try
        {
            info.guardianproject.iocipher.RandomAccessFile raf = new info.guardianproject.iocipher.RandomAccessFile(
                    file_name_with_path, "rw");
            info.guardianproject.iocipher.IOCipherFileChannel inChannel = raf.getChannel();
            // inChannel.lseek(position, OsConstants.SEEK_SET);
            inChannel.write(data, position);

            try
            {
                inChannel.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                raf.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static String fileExt(String url)
    {
        return getFileExtensionFromUrl(url);
        //        if (url.indexOf("?") > -1)
        //        {
        //            url = url.substring(0, url.indexOf("?"));
        //        }
        //
        //        if (url.lastIndexOf(".") == -1)
        //        {
        //            return null;
        //        }
        //        else
        //        {
        //            String ext = url.substring(url.lastIndexOf(".") + 1);
        //
        //            if (ext.indexOf("%") > -1)
        //            {
        //                ext = ext.substring(0, ext.indexOf("%"));
        //            }
        //
        //            if (ext.indexOf("/") > -1)
        //            {
        //                ext = ext.substring(0, ext.indexOf("/"));
        //            }
        //
        //            return ext.toLowerCase();
        //        }
    }

    static int hash_to_bucket(String hash_value, int number_of_buckets)
    {
        try
        {
            int ret = 0;
            int value = (Integer.parseInt(hash_value.substring(hash_value.length() - 1, hash_value.length() - 0), 16) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 2, hash_value.length() - 1), 16) *
                          16) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 3, hash_value.length() - 2), 16) *
                          (16 * 2)) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 4, hash_value.length() - 3), 16) *
                          (16 * 3)));

            // Log.i(TAG, "hash_to_bucket:value=" + value);

            ret = (value % number_of_buckets);

            // BigInteger bigInt = new BigInteger(1, hash_value.getBytes());
            // int ret = (int) (bigInt.longValue() % (long) number_of_buckets);
            // // Log.i(TAG, "hash_to_bucket:" + "ret=" + ret + " hash_as_int=" + bigInt + " hash=" + hash_value);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "hash_to_bucket:EE:" + e.getMessage());
            return 0;
        }
    }

    public static boolean isColorLight(int color)
    {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        // System.out.println("HSV="+hsv[0]+" "+hsv[1]+" "+hsv[2]);
        return !(hsv[2] < 0.5);
    }

    public static int lightenColor(int inColor, float inAmount)
    {
        return Color.argb(Color.alpha(inColor), (int) Math.min(255, Color.red(inColor) + 255 * inAmount),
                          (int) Math.min(255, Color.green(inColor) + 255 * inAmount),
                          (int) Math.min(255, Color.blue(inColor) + 255 * inAmount));
    }

    public static int darkenColor(int inColor, float inAmount)
    {
        return Color.argb(Color.alpha(inColor), (int) Math.max(0, Color.red(inColor) - 255 * inAmount),
                          (int) Math.max(0, Color.green(inColor) - 255 * inAmount),
                          (int) Math.max(0, Color.blue(inColor) - 255 * inAmount));
    }

    static int get_toxconnection_wrapper(int TOX_CONNECTION_)
    {
        if (TOX_CONNECTION_ == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    static int get_combined_connection_status(String friend_pubkey, int a_TOX_CONNECTION)
    {
        int ret = TOX_CONNECTION_NONE.value;

        if (HelperRelay.is_any_relay(friend_pubkey))
        {
            ret = a_TOX_CONNECTION;
        }
        else
        {
            String relay_ = HelperRelay.get_relay_for_friend(friend_pubkey);

            if (relay_ == null)
            {
                // friend has no relay
                ret = a_TOX_CONNECTION;
            }
            else
            {
                // friend with relay
                if (a_TOX_CONNECTION != TOX_CONNECTION_NONE.value)
                {
                    ret = a_TOX_CONNECTION;
                }
                else
                {
                    int friend_con_status = orma.selectFromFriendList().
                            tox_public_key_stringEq(friend_pubkey).
                            get(0).TOX_CONNECTION_real;
                    int relay_con_status = orma.selectFromFriendList().
                            tox_public_key_stringEq(relay_).
                            get(0).TOX_CONNECTION_real;

                    if ((friend_con_status != TOX_CONNECTION_NONE.value) ||
                        (relay_con_status != TOX_CONNECTION_NONE.value))
                    {
                        // if one of them is online, return combined "online" as status
                        ret = TOX_CONNECTION_TCP.value;
                    }
                }
            }
        }

        return ret;
    }

    /*************************************************************************/
    public static MainActivity.send_message_result tox_friend_send_message_wrapper(long friendnum, int a_TOX_MESSAGE_TYPE, @NonNull String message)
    {
        Log.d(TAG, "tox_friend_send_message_wrapper:" + friendnum);
        long friendnum_to_use = friendnum;
        FriendList f = main_get_friend(friendnum);
        Log.d(TAG, "tox_friend_send_message_wrapper:f=" + f);

        if (f != null)
        {
            Log.d(TAG, "tox_friend_send_message_wrapper:f conn" + f.TOX_CONNECTION_real);

            if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
            {
                String relay_pubkey = HelperRelay.get_relay_for_friend(f.tox_public_key_string);

                if (relay_pubkey != null)
                {
                    // friend has a relay
                    friendnum_to_use = tox_friend_by_public_key__wrapper(relay_pubkey);
                    Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
                }
            }
        }

        MainActivity.send_message_result result = new MainActivity.send_message_result();
        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
        ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        // use msg V2 API Call
        long t_sec = (System.currentTimeMillis() / 1000);
        long res = MainActivity.tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE, t_sec, message,
                                                                message.length(), raw_message_buf,
                                                                raw_message_length_buf, msg_id_buffer);
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        Log.d(TAG, "tox_friend_send_message_wrapper:res=" + res);
        int raw_message_length_int = raw_message_length_buf.
                array()[raw_message_length_buf.arrayOffset()] & 0xFF + (raw_message_length_buf.
                array()[raw_message_length_buf.arrayOffset() + 1] & 0xFF) * 256;
        // Log.i(TAG,
        //      "tox_friend_send_message_wrapper:message=" + message + " res=" + res + " len=" + raw_message_length_int);
        result.error_num = res;

        if (res == -9999)
        {
            // msg V2 OK
            result.msg_num = (Long.MAX_VALUE - 1);
            result.msg_v2 = true;
            result.msg_hash_hex = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(), msg_id_buffer.limit());
            result.raw_message_buf_hex = bytesToHex(raw_message_buf.array(), raw_message_buf.arrayOffset(),
                                                    raw_message_length_int);
            Log.i(TAG, "tox_friend_send_message_wrapper:hash_hex=" + result.msg_hash_hex + " raw_msg_hex" +
                       result.raw_message_buf_hex);
            return result;
        }
        else if (res == -9991)
        {
            // msg V2 error
            result.msg_num = -1;
            result.msg_v2 = true;
            result.msg_hash_hex = "";
            result.raw_message_buf_hex = "";
            return result;
        }
        else
        {
            // old message
            result.msg_num = res;
            result.msg_v2 = false;
            result.msg_hash_hex = "";
            result.raw_message_buf_hex = "";
            return result;
        }
    }

    static void set_new_random_nospam_value()
    {
        // Log.i(TAG, "old ToxID=" + MainActivity.get_my_toxid());
        // Log.i(TAG, "old NOSPAM=" + MainActivity.tox_self_get_nospam());
        Random random = new Random();
        long new_nospam = (long) random.nextInt() + (1L << 31);
        // Log.i(TAG, "generated NOSPAM=" + new_nospam);
        MainActivity.tox_self_set_nospam(new_nospam);
        update_savedata_file_wrapper(); // set new random nospam

        try
        {
            update_toxid_display_s();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        // Log.i(TAG, "new ToxID=" + MainActivity.get_my_toxid());
        // Log.i(TAG, "new NOSPAM=" + MainActivity.tox_self_get_nospam());
    }

    static void update_savedata_file_wrapper()
    {
        if (is_tox_started == true)
        {
            try
            {
                MainActivity.semaphore_tox_savedata.acquire();
                long start_timestamp = System.currentTimeMillis();
                MainActivity.update_savedata_file(TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(
                        TrifaSetPatternActivity.StringToBytes2(MainActivity.PREF__DB_secrect_key))));
                long end_timestamp = System.currentTimeMillis();
                MainActivity.semaphore_tox_savedata.release();
                Log.i(TAG,
                      "update_savedata_file() took:" + (((float) (end_timestamp - start_timestamp)) / 1000f) + "s");
            }
            catch (InterruptedException e)
            {
                MainActivity.semaphore_tox_savedata.release();
                e.printStackTrace();
            }
        }
        else
        {
            Log.i(TAG, "update_savedata_file(): ERROR:Tox not ready:001");
        }
    }

    static void receive_incoming_message(int msg_type, long friend_number, String friend_message_text_utf8, byte[] raw_message, long raw_message_length, String original_sender_pubkey)
    {
        // incoming msg can be:
        // (msg_type == 0) msgV1 text only message -> msg_type, friend_number, friend_message_text_utf8
        // (msg_type == 1) msgV2 direct message    -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length
        // (msg_type == 2) msgV2 relay message     -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length, original_sender_pubkey
        if (msg_type == 0)
        {
            // msgV1 text only message
            // Log.i(TAG, "friend_message:friend:" + friend_number + " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            if (MainActivity.message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number)
                {
                    // Log.i(TAG, "noti_and_badge:003:");
                    // no notifcation and no badge update
                    do_notification = false;
                    do_badge_update = false;
                }
            }

            Message m = new Message();

            if (!do_badge_update)
            {
                Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            // m.tox_friendnum = friend_number;
            m.tox_friendpubkey = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.read = false;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.rcvd_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp_ms = 0;
            m.sent_timestamp = System.currentTimeMillis();
            m.sent_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 0;

            if (MainActivity.message_list_activity != null)
            {
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number)
                {
                    HelperMessage.insert_into_message_db(m, true);
                }
                else
                {
                    HelperMessage.insert_into_message_db(m, false);
                }
            }
            else
            {
                HelperMessage.insert_into_message_db(m, false);
            }

            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                Log.i(TAG, "noti_and_badge:005:");
                // start "new" notification
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // allow notification every n seconds
                            if ((MainActivity.Notification_new_message_last_shown_timestamp +
                                 MainActivity.Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (MainActivity.PREF__notification)
                                {
                                    MainActivity.Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(MainActivity.context_s,
                                                                           StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
                                    NotificationCompat.Builder b;

                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                    {
                                        if ((MainActivity.PREF__notification_sound) &&
                                            (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound_and_vibrate);
                                        }
                                        else if ((MainActivity.PREF__notification_sound) &&
                                                 (!MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound);
                                        }
                                        else if ((!MainActivity.PREF__notification_sound) &&
                                                 (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_vibrate);
                                        }
                                        else
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_silent);
                                        }
                                    }
                                    else
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s);
                                    }

                                    b.setContentIntent(pendingIntent);
                                    b.setSmallIcon(R.drawable.circle_orange);
                                    b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                    Uri default_notification_sound = RingtoneManager.getDefaultUri(
                                            RingtoneManager.TYPE_NOTIFICATION);

                                    if (MainActivity.PREF__notification_sound)
                                    {
                                        b.setSound(default_notification_sound);
                                    }

                                    if (MainActivity.PREF__notification_vibrate)
                                    {
                                        long[] vibrate_pattern = {100, 300};
                                        b.setVibrate(vibrate_pattern);
                                    }

                                    b.setContentTitle(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message_title));
                                    b.setAutoCancel(true);
                                    b.setContentText(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message3));
                                    Notification notification3 = b.build();
                                    MainActivity.nmn3.notify(MainActivity.Notification_new_message_ID, notification3);
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
                    if (MainActivity.main_handler_s != null)
                    {
                        MainActivity.main_handler_s.post(myRunnable);
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        else if (msg_type == 1)
        {
            // msgV2 direct message
            // Log.i(TAG,
            //      "friend_message_v2:friend:" + friend_number + " ts:" + ts_sec + " systime" + System.currentTimeMillis() +
            //      " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            if (MainActivity.message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number)
                {
                    // Log.i(TAG, "noti_and_badge:003:");
                    // no notifcation and no badge update
                    do_notification = false;
                    do_badge_update = false;
                }
            }

            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
            raw_message_buf.put(raw_message, 0, (int) raw_message_length);
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = MainActivity.tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = MainActivity.tox_messagev2_get_ts_ms(raw_message_buf);
            String msg_id_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                     msg_id_buffer.limit());
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);
            int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).and().msg_id_hashEq(
                    msg_id_as_hex_string).count();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // TODO: use received timstamp, not "now" here!
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer);
                return;
            }

            // add FT message to UI
            Message m = new Message();

            if (!do_badge_update)
            {
                Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            m.tox_friendpubkey = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.filetransfer_id = -1;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_RESUME.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

            if (MainActivity.message_list_activity != null)
            {
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number)
                {
                    HelperMessage.insert_into_message_db(m, true);
                }
                else
                {
                    HelperMessage.insert_into_message_db(m, false);
                }
            }
            else
            {
                HelperMessage.insert_into_message_db(m, false);
            }

            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                Log.i(TAG, "noti_and_badge:005:");
                // start "new" notification
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // allow notification every n seconds
                            if ((MainActivity.Notification_new_message_last_shown_timestamp +
                                 MainActivity.Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (MainActivity.PREF__notification)
                                {
                                    MainActivity.Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(MainActivity.context_s,
                                                                           StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
                                    NotificationCompat.Builder b;

                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                    {
                                        if ((MainActivity.PREF__notification_sound) &&
                                            (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound_and_vibrate);
                                        }
                                        else if ((MainActivity.PREF__notification_sound) &&
                                                 (!MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound);
                                        }
                                        else if ((!MainActivity.PREF__notification_sound) &&
                                                 (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_vibrate);
                                        }
                                        else
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_silent);
                                        }
                                    }
                                    else
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s);
                                    }

                                    b.setContentIntent(pendingIntent);
                                    b.setSmallIcon(R.drawable.circle_orange);
                                    b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                    Uri default_notification_sound = RingtoneManager.getDefaultUri(
                                            RingtoneManager.TYPE_NOTIFICATION);

                                    if (MainActivity.PREF__notification_sound)
                                    {
                                        b.setSound(default_notification_sound);
                                    }

                                    if (MainActivity.PREF__notification_vibrate)
                                    {
                                        long[] vibrate_pattern = {100, 300};
                                        b.setVibrate(vibrate_pattern);
                                    }

                                    b.setContentTitle(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message_title));
                                    b.setAutoCancel(true);
                                    b.setContentText(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message4));
                                    Notification notification3 = b.build();
                                    MainActivity.nmn3.notify(MainActivity.Notification_new_message_ID, notification3);
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
                    if (MainActivity.main_handler_s != null)
                    {
                        MainActivity.main_handler_s.post(myRunnable);
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        else if (msg_type == 2)
        {
            // msgV2 relay message
            long friend_number_real_sender = tox_friend_by_public_key__wrapper(original_sender_pubkey);
            // Log.i(TAG,
            //      "friend_message_v2:friend:" + friend_number + " ts:" + ts_sec + " systime" + System.currentTimeMillis() +
            //      " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            if (MainActivity.message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number_real_sender)
                {
                    // Log.i(TAG, "noti_and_badge:003:");
                    // no notifcation and no badge update
                    do_notification = false;
                    do_badge_update = false;
                }
            }

            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
            raw_message_buf.put(raw_message, 0, (int) raw_message_length);
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = MainActivity.tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = MainActivity.tox_messagev2_get_ts_ms(raw_message_buf);
            Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:raw_msg=" + bytes_to_hex(raw_message));
            String msg_id_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                     msg_id_buffer.limit());
            Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);
            int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number_real_sender)).and().msg_id_hashEq(
                    msg_id_as_hex_string).count();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer);
                return;
            }

            // add FT message to UI
            Message m = new Message();

            if (!do_badge_update)
            {
                Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            m.tox_friendpubkey = original_sender_pubkey;
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.filetransfer_id = -1;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_RESUME.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            Log.i(TAG,
                  "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

            if (MainActivity.message_list_activity != null)
            {
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number_real_sender)
                {
                    HelperMessage.insert_into_message_db(m, true);
                }
                else
                {
                    HelperMessage.insert_into_message_db(m, false);
                }
            }
            else
            {
                HelperMessage.insert_into_message_db(m, false);
            }

            // send message receipt v2 to the relay
            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                Log.i(TAG, "noti_and_badge:005:");
                // start "new" notification
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // allow notification every n seconds
                            if ((MainActivity.Notification_new_message_last_shown_timestamp +
                                 MainActivity.Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (MainActivity.PREF__notification)
                                {
                                    MainActivity.Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(MainActivity.context_s,
                                                                           StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
                                    NotificationCompat.Builder b;

                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                    {
                                        if ((MainActivity.PREF__notification_sound) &&
                                            (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound_and_vibrate);
                                        }
                                        else if ((MainActivity.PREF__notification_sound) &&
                                                 (!MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_sound);
                                        }
                                        else if ((!MainActivity.PREF__notification_sound) &&
                                                 (MainActivity.PREF__notification_vibrate))
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_vibrate);
                                        }
                                        else
                                        {
                                            b = new NotificationCompat.Builder(MainActivity.context_s,
                                                                               MainActivity.channelId_newmessage_silent);
                                        }
                                    }
                                    else
                                    {
                                        b = new NotificationCompat.Builder(MainActivity.context_s);
                                    }

                                    b.setContentIntent(pendingIntent);
                                    b.setSmallIcon(R.drawable.circle_orange);
                                    b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                    Uri default_notification_sound = RingtoneManager.getDefaultUri(
                                            RingtoneManager.TYPE_NOTIFICATION);

                                    if (MainActivity.PREF__notification_sound)
                                    {
                                        b.setSound(default_notification_sound);
                                    }

                                    if (MainActivity.PREF__notification_vibrate)
                                    {
                                        long[] vibrate_pattern = {100, 300};
                                        b.setVibrate(vibrate_pattern);
                                    }

                                    b.setContentTitle(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message_title));
                                    b.setAutoCancel(true);
                                    b.setContentText(MainActivity.context_s.getString(
                                            R.string.MainActivity_notification_new_message5));
                                    Notification notification3 = b.build();
                                    MainActivity.nmn3.notify(MainActivity.Notification_new_message_ID, notification3);
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
                    if (MainActivity.main_handler_s != null)
                    {
                        MainActivity.main_handler_s.post(myRunnable);
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    static void reverse_u_and_v_planes(byte[] buf, int frame_width_px, int frame_height_px)
    {
        // TODO: make this less slow and aweful!
        try
        {
            int pos = 0;
            int start = frame_width_px * frame_height_px;
            int off = (frame_width_px * frame_height_px) / 4;
            int start2 = start + off;
            byte b = 0;

            for (pos = 0; pos < off; pos++)
            {
                b = buf[start + pos];
                buf[start + pos] = buf[start2 + pos];
                buf[start2 + pos] = b;
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    static void save_sps_pps_nal(byte[] sps_pps_nal_unit_bytes, int length)
    {
        if (sps_pps_nal_unit_bytes != null)
        {
            global_sps_pps_nal_unit_bytes = Arrays.copyOf(sps_pps_nal_unit_bytes, length);
        }
    }

    public static byte[] YV12totoNV12(byte[] input, byte[] output, int width, int height)
    {
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        System.arraycopy(input, 0, output, 0, frameSize); // Y
        for (int i = 0; i < qFrameSize; i++)
        {
            output[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }

    public static byte[] YV12toNV21(final byte[] input, final byte[] output, final int width, final int height)
    {

        final int size = width * height;
        final int quarter = size / 4;
        final int vPosition = size; // This is where V starts
        final int uPosition = size + quarter; // This is where U starts

        System.arraycopy(input, 0, output, 0, size); // Y is same

        for (int i = 0; i < quarter; i++)
        {
            output[size + i * 2] = input[vPosition + i]; // For NV21, V first
            output[size + i * 2 + 1] = input[uPosition + i]; // For Nv21, U second
        }
        return output;
    }

    // the color transform, @see http://stackoverflow.com/questions/15739684/mediacodec-and-camera-color-space-incorrect
    public static byte[] NV21toNV12(byte[] input, byte[] output, int width, int height)
    {
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        System.arraycopy(input, 0, output, 0, frameSize); // Y
        for (int i = 0; i < qFrameSize; i++)
        {
            output[frameSize + i * 2] = input[frameSize + i * 2 + 1]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i * 2]; // Cr (V)
        }
        return output;
    }

    static int toxav_video_send_frame_uv_reversed_wrapper(final byte[] buf2, final long friendnum, final int frame_width_px, final int frame_height_px, long capture_ts)
    {
        //try
        //{
            // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
            // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //}
        //catch (Exception e)
        //{
        //    e.printStackTrace();
        //}

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (MainActivity.PREF__use_H264_hw_encoding) &&
            (Callstate.video_out_codec == VIDEO_CODEC_H264))
        {
            final long video_frame_age = capture_ts;

            if (java_video_encoder_first_frame_in == 1)
            {
                Callstate.java_video_encoder_first_frame_in = 0;
                Callstate.java_video_encoder_delay_start_ts = System.currentTimeMillis();
            }

            final Thread new_thread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (MainActivity.PREF__camera_get_preview_format.equals("YV12"))
                        {
                            if (buf_video_send_frame == null)
                            {
                                buf_video_send_frame = new byte[buf2.length];
                            }
                            else if (buf_video_send_frame.length < buf2.length)
                            {
                                buf_video_send_frame = new byte[buf2.length];
                            }
                            buf_video_send_frame = YV12totoNV12(buf2, buf_video_send_frame, frame_width_px,
                                                                frame_height_px);
                            feed_h264_encoder(buf_video_send_frame, frame_width_px, frame_height_px, video_frame_age);
                        }
                        else // (PREF__camera_get_preview_format == "NV21")
                        {
                            if (buf_video_send_frame == null)
                            {
                                buf_video_send_frame = new byte[buf2.length];
                            }
                            else if (buf_video_send_frame.length < buf2.length)
                            {
                                buf_video_send_frame = new byte[buf2.length];
                            }
                            buf_video_send_frame = NV21toNV12(buf2, buf_video_send_frame, frame_width_px,
                                                              frame_height_px);
                            feed_h264_encoder(buf_video_send_frame, frame_width_px, frame_height_px, video_frame_age);
                        }
                    }
                    catch (Exception e)
                    {
                        // e.printStackTrace();
                    }
                }
            };
            new_thread.start();

            final Thread new_thread2 = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        for (int jj = 0; jj < 2; jj++)
                        {
                            CallingActivity.h264_encoder_output_data h264_out_data = fetch_from_h264_encoder();

                            if (h264_out_data != null)
                            {
                                if (h264_out_data.sps_pps != null)
                                {
                                    save_sps_pps_nal(h264_out_data.sps_pps, h264_out_data.data_len);
                                }

                                if (h264_out_data.data != null)
                                {
                                    MainActivity.video_buffer_2.rewind();
                                    long data_length = 0;

                                    if (global_sps_pps_nal_unit_bytes != null)
                                    {
                                        if (send_sps_pps_every_x_frames_current >= send_sps_pps_every_x_frames)
                                        {
                                            // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:send_sps_pps:1");
                                            if (h264_out_data.sps_pps == null)
                                            {
                                                // only add sps/pps if this NALU does not contain it already
                                                MainActivity.video_buffer_2.put(global_sps_pps_nal_unit_bytes);
                                                data_length = data_length + global_sps_pps_nal_unit_bytes.length;
                                            }
                                            send_sps_pps_every_x_frames_current = 0;

                                            if (set_vdelay_every_x_frames_current >= set_vdelay_every_x_frames)
                                            {
                                                set_vdelay_every_x_frames_current = 0;

                                                video_frame_age_values[video_frame_age_values_cur_index] = (int) (
                                                        System.currentTimeMillis() - video_frame_age);
                                                video_frame_age_values_cur_index++;
                                                if (video_frame_age_values_cur_index >=
                                                    video_frame_age_values_cur_index_count)
                                                {
                                                    video_frame_age_values_cur_index = 0;
                                                }

                                                video_frame_age_mean = 0;
                                                for (int kk = 0; kk < video_frame_age_values_cur_index_count; kk++)
                                                {
                                                    video_frame_age_mean =
                                                            video_frame_age_mean + video_frame_age_values[kk];
                                                }

                                                video_frame_age_mean = video_frame_age_mean / 10;

                                                if (1 == 1)
                                                {
                                                    toxav_option_set(friendnum,
                                                                     TOXAV_CLIENT_VIDEO_CAPTURE_DELAY_MS.value,
                                                                     video_frame_age_mean + Callstate.delay_add);
                                                }
                                                else
                                                {
                                                    toxav_option_set(friendnum,
                                                                     TOXAV_CLIENT_VIDEO_CAPTURE_DELAY_MS.value,
                                                                     Callstate.java_video_encoder_delay);
                                                }
                                            }

                                            set_vdelay_every_x_frames_current++;

                                        }

                                        send_sps_pps_every_x_frames_current++;
                                    }

                                    MainActivity.video_buffer_2.put(h264_out_data.data, 0, h264_out_data.data_len);
                                    data_length = data_length + h264_out_data.data_len;

                                    // Log.i(TAG,
                                    //      "H264:video_frame_age=" + (System.currentTimeMillis() - video_frame_age));

                                    if (Callstate.java_video_encoder_delay_set == 0)
                                    {
                                        Callstate.java_video_encoder_delay = (System.currentTimeMillis() -
                                                                              Callstate.java_video_encoder_delay_start_ts);


                                        if ((Callstate.java_video_encoder_delay < 3) ||
                                            (Callstate.java_video_encoder_delay > 300))
                                        {
                                            Callstate.java_video_encoder_delay = 120;
                                        }

                                        Callstate.java_video_encoder_delay_set = 1;
                                        //Log.i(TAG,
                                        //      "java_video_encoder_delay=" + Callstate.java_video_encoder_delay + " ms");
                                    }

                                    //Log.i(TAG,
                                    //      "java_video_encoder_delay=" + Callstate.java_video_encoder_delay + " ms " +
                                    //      ((int) (System.currentTimeMillis() - h264_out_data.pts)) + " ms");

                                    //Log.i(TAG, "java_video_encoder_delay=" +
                                    //           (int) (System.currentTimeMillis() - video_frame_age) + " ms");


                                    Callstate.delay_add = (int) (System.currentTimeMillis() - h264_out_data.pts);
                                    if ((Callstate.delay_add < 1) || (Callstate.delay_add > 25))
                                    {
                                        Callstate.delay_add = 10;
                                    }

                                    //Log.i(TAG, "V:AGE:" + Callstate.delay_add + " : " + video_frame_age + " : " +
                                    //           ((int) (System.currentTimeMillis() - video_frame_age)));
                                    MainActivity.toxav_video_send_frame_h264_age(friendnum, frame_width_px,
                                                                                 frame_height_px, data_length,
                                                                                 (int) (System.currentTimeMillis() -
                                                                                        video_frame_age) +
                                                                                 Callstate.delay_add);

                                    // Log.i(TAG, "V:res:" + res);

                                }
                                else
                                {
                                    // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:buf_out==null:#" + jj);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        // e.printStackTrace();
                    }
                }
            };
            new_thread2.start();

            try
            {
                new_thread.join();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (1 == 2)
            {
                try
                {
                    new_thread2.join();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            return 0;
        }
        else
        {
            return MainActivity.toxav_video_send_frame_uv_reversed(friendnum, frame_width_px, frame_height_px);
        }
    }

    static int toxav_video_send_frame_wrapper(byte[] buf, long friendnum, int frame_width_px, int frame_height_px, long capture_ts)
    {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (MainActivity.PREF__use_H264_hw_encoding) &&
            (Callstate.video_out_codec == VIDEO_CODEC_H264))
        {
            feed_h264_encoder(buf, frame_width_px, frame_height_px, capture_ts);

            for (int jj = 0; jj < 2; jj++)
            {
                CallingActivity.h264_encoder_output_data h264_out_data = fetch_from_h264_encoder();
                byte[] buf_out = h264_out_data.data;

                if (h264_out_data.sps_pps != null)
                {
                    save_sps_pps_nal(h264_out_data.sps_pps, h264_out_data.data_len);
                }

                if (buf_out != null)
                {
                    if (global_sps_pps_nal_unit_bytes != null)
                    {
                        if (send_sps_pps_every_x_frames_current > send_sps_pps_every_x_frames)
                        {
                            // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:send_sps_pps:2");
                            MainActivity.video_buffer_2.rewind();
                            MainActivity.video_buffer_2.put(global_sps_pps_nal_unit_bytes);
                            MainActivity.toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px,
                                                                     global_sps_pps_nal_unit_bytes.length);
                            send_sps_pps_every_x_frames_current = 0;
                        }

                        send_sps_pps_every_x_frames_current++;
                    }

                    MainActivity.video_buffer_2.rewind();
                    MainActivity.video_buffer_2.put(buf_out, 0, h264_out_data.data_len);
                    MainActivity.toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px,
                                                             h264_out_data.data_len);
                }
                else
                {
                    // Log.i(TAG, "toxav_video_send_frame_wrapper:buf_out==null");
                }
            }

            return 0;
        }
        else
        {
            return MainActivity.toxav_video_send_frame(friendnum, frame_width_px, frame_height_px);
        }
    }

    static void import_toxsave_file_unsecure()
    {
        MainActivity.global_stop_tox();
        File f_src = new File(MainActivity.SD_CARD_FILES_EXPORT_DIR + "/" + "I_WANT_TO_IMPORT_savedata.tox");
        File f_dst = new File(MainActivity.app_files_directory + "/" + "savedata.tox");
        try
        {
            ls_file(f_src);
            ls_file(f_dst);

            io_file_copy(f_src, f_dst);

            ls_file(f_dst);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // after importing the file. just stop the app hard
        // tox_service_fg.stop_me(true);
        MainActivity.exit();
    }

    static void ls_file(File f)
    {
        try
        {
            Log.i(TAG, "ls_file:" + f.getAbsolutePath() + " size=" + f.length());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void io_file_copy(File src, File dst) throws java.io.IOException
    {
        try (java.io.InputStream in = new java.io.FileInputStream(src))
        {
            try (java.io.OutputStream out = new java.io.FileOutputStream(dst))
            {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
            }
        }
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

    public static void reset_audio_mode()
    {
        try
        {
            MainActivity.audio_manager_s.setMode(AudioManager.MODE_NORMAL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_calling_audio_mode()
    {
        try
        {
            MainActivity.audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
