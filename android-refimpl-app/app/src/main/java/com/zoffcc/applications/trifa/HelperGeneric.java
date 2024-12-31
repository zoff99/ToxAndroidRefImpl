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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Detector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.exifinterface.media.ExifInterface;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static com.zoffcc.applications.nativeaudio.NativeAudio.set_rec_preset;
import static com.zoffcc.applications.trifa.CallingActivity.feed_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.fetch_from_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.global_sps_pps_nal_unit_bytes;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames_current;
import static com.zoffcc.applications.trifa.CallingActivity.set_vdelay_every_x_frames;
import static com.zoffcc.applications.trifa.CallingActivity.set_vdelay_every_x_frames_current;
import static com.zoffcc.applications.trifa.Callstate.java_video_encoder_first_frame_in;
import static com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.update_friend_msgv3_capability;
import static com.zoffcc.applications.trifa.HelperMessage.process_msgv3_high_level_ack;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message;
import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__DB_secrect_key;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.VFS_CUSTOM_WRITE_CACHE;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_2;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.ProfileActivity.update_toxid_display_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FAB_SCROLL_TO_BOTTOM_FADEIN_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FAB_SCROLL_TO_BOTTOM_FADEOUT_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_AVATAR_HEIGHT_COMPACT_LAYOUT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_AVATAR_HEIGHT_NORMAL_LAYOUT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_V2_MSG_SENT_OK;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_ADD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NGC_PEERS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_OWN_AVATAR_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_OWN_AVATAR_DIR_FILENAME_WITH_EXTENSION;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_H264;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fis_saf;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fos;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_online_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_anygroupview;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_messageview;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_CLIENT_VIDEO_CAPTURE_DELAY_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_MSGV2;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILETRANSFER_SIZE_MSGV2;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static com.zoffcc.applications.trifa.TrifaToxService.stop_tox_fg_done;
import static com.zoffcc.applications.trifa.TrifaToxService.vfs;

public class HelperGeneric
{
    private static final String TAG = "trifa.Hlp.Generic";

    /*
     all stuff here should be moved somewhere else at some point
     */
    static final Object audio_system_start_stop_lock = new Object();

    private static final Pattern PATTERN_IPV4 = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final int INTERVAL_UPDATE_SAVE_FILE_WRAPPER_THROTTLED_MS = 200;

    static long video_frame_age_mean = 0;
    static int video_frame_age_values_cur_index = 0;
    final static int video_frame_age_values_cur_index_count = 10;
    static long[] video_frame_age_values = new long[video_frame_age_values_cur_index_count];
    static byte[] buf_video_send_frame = null;
    static long last_log_battery_savings_criteria_ts = -1;
    static long update_savedata_file_wrapper_throttled_last_trigger_ts = 0;
    static long update_savedata_file_wrapper_last_ts = 0;

    public static void clearCache_s()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    clearCache(context_s);
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

                Log.i(TAG, "M:STARTUP:cleanup_temp_dirs PART 1 starting ...");

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

                Log.i(TAG, "M:STARTUP:cleanup_temp_dirs PART 2 starting ...");

                try
                {
                    vfs_deleteFilesAndFilesSubDirectories_real(MainActivity.SD_CARD_TMP_DIR + "/");
                }
                catch (Exception e)
                {
                    e.getMessage();
                }

                Log.i(TAG, "M:STARTUP:cleanup_temp_dirs ---READY---");

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

    static void conference_message_add_from_sync(long conference_number, long peer_number2, String peer_pubkey, int a_TOX_MESSAGE_TYPE, String message, long length, long sent_timestamp_in_ms, String message_id)
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
        String conference_name = null;

        try
        {
            // TODO: cache me!!
            conf_temp = orma.selectFromConferenceDB().tox_conference_numberEq(
                    conference_number).and().conference_activeEq(true).toList().get(0);
            conf_id = conf_temp.conference_identifier;
            // Log.i(TAG, "conference_message_add_from_sync:conf_id=" + conf_id);
            conference_name = conf_temp.name;
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
        m.message_id_tox = message_id;
        m.was_synced = true;

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

        // HelperConference.update_single_conference_in_friendlist_view(conf_temp);
        HelperFriend.add_all_friends_clear_wrapper(0);

        if (do_notification)
        {
            change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.conference_identifier, conference_name, m.text);
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

        if (!from_relay)
        {
            if (went_online)
            {
                f.last_online_timestamp_real = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
            }
            else
            {
                f.last_online_timestamp_real = System.currentTimeMillis();
            }
            HelperFriend.update_friend_in_db_last_online_timestamp_real(f);
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

                                    if (filenum < 0)
                                    {
                                        Log.i(TAG, "Send_own_Avatar: error " + filenum);
                                        return;
                                    }

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

    public static boolean need_rotate_image_to_exif(Bitmap bitmap, String filename_with_path)
    {
        try
        {
            ExifInterface exifInterface = new ExifInterface(filename_with_path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                            ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return true;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return true;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static Bitmap rotate_image_to_exif(Bitmap bitmap, String filename_with_path)
    {
        try
        {
            ExifInterface exifInterface = new ExifInterface(filename_with_path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                            ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            // Log.i(TAG, "rotate_image_to_exif:orig w x h=" + w + " " + h);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            Bitmap out = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
            // Log.i(TAG, "rotate_image_to_exif:new w x h=" + out.getWidth() + " " + out.getHeight());
            return out;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap scale_bitmap_keep_aspect(Bitmap originalImage, int width, int height)
    {
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();

        // Log.i(TAG, "scale_bitmap_keep_aspect:orig w x h=" + originalWidth + " " + originalHeight);

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        // Log.i(TAG, "scale_bitmap_keep_aspect:new w x h=" + background.getWidth() + " " + background.getHeight());

        return background;
    }

    public static void send_avatar_to_all_friends()
    {
        try
        {
            List<FriendList> fl = orma.selectFromFriendList().toList();

            if (fl != null)
            {
                if (fl.size() > 0)
                {
                    int i = 0;
                    for (i = 0; i < fl.size(); i++)
                    {
                        FriendList n = fl.get(i);
                        // iterate over all online friends, and send them our new avatar
                        if (n.TOX_CONNECTION != TOX_CONNECTION_NONE.value)
                        {
                            // Log.i(TAG, "select_avatar:send_avatar_to_friend:online:i=" + i);
                            send_avatar_to_friend(
                                    HelperFriend.tox_friend_by_public_key__wrapper(n.tox_public_key_string));
                        }
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    public static void del_own_avatar()
    {
        delete_vfs_file(VFS_PREFIX + VFS_OWN_AVATAR_DIR + "/", VFS_OWN_AVATAR_DIR_FILENAME_WITH_EXTENSION);
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
                // Log.i(TAG, "move_tmp_file_to_real_file:ft.len=" + f1.length());
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

            // Log.i(TAG, "move_tmp_file_to_real_file:OK");
        }
        catch (Exception e)
        {
            Log.i(TAG, "move_tmp_file_to_real_file:EE:" + e.getMessage());
            // e.printStackTrace();
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
            java.io.InputStream ins = context.getResources().openRawResource(
                    context.getResources().getIdentifier("ic_plus_sign", "drawable", context.getPackageName()));
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
                    byte[] buffer = new byte[8192];
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

                if (!f_real.exists())
                {
                    return;
                }

                try
                {
                    is = new info.guardianproject.iocipher.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[8192];
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
                        GlideApp.with(c).load(f1).placeholder(R.drawable.round_loading_animation).diskCacheStrategy(
                                DiskCacheStrategy.RESOURCE).signature(new com.bumptech.glide.signature.StringSignatureZ(
                                "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                fl.avatar_update_timestamp)).skipMemoryCache(false).into(v);
                    }
                    else
                    {
                        GlideApp.with(c).load(f1).placeholder(R.drawable.round_loading_animation).diskCacheStrategy(
                                DiskCacheStrategy.RESOURCE).skipMemoryCache(force_update).into(v);
                    }
                }
                else
                {
                    if (is_friend_avatar)
                    {
                        GlideApp.with(c).load(f1).placeholder(placholder).diskCacheStrategy(
                                DiskCacheStrategy.RESOURCE).signature(new com.bumptech.glide.signature.StringSignatureZ(
                                "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                fl.avatar_update_timestamp)).skipMemoryCache(false).into(v);
                    }
                    else
                    {
                        GlideApp.with(c).load(f1).placeholder(placholder).diskCacheStrategy(
                                DiskCacheStrategy.RESOURCE).skipMemoryCache(force_update).into(v);
                    }
                }
            }
            else
            {
                File f1 = new File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);
                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());
                GlideApp.with(c).load(byteArray).placeholder(placholder).diskCacheStrategy(
                        DiskCacheStrategy.RESOURCE).skipMemoryCache(force_update).into(v);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "put_vfs_image_on_imageview:EE1:" + e.getMessage());
        }
    }

    static void put_vfs_image_on_imageview_real_remoteviews(Context c, Notification notification, int notification_id, int viewid, RemoteViews rv, Drawable placholder, String vfs_image_filename, boolean force_update, boolean is_friend_avatar, FriendList fl)
    {
        try
        {
            NotificationTarget notificationTarget = new NotificationTarget(c, viewid, rv, notification,
                                                                           notification_id);

            info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
            if (placholder == null)
            {
                if (is_friend_avatar)
                {
                    GlideApp.with(c).asBitmap().load(f1).placeholder(
                            R.drawable.round_loading_animation).diskCacheStrategy(DiskCacheStrategy.NONE).signature(
                            new com.bumptech.glide.signature.StringSignatureZ(
                                    "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                    fl.avatar_update_timestamp)).skipMemoryCache(false).into(notificationTarget);
                }
                else
                {
                    GlideApp.with(c).asBitmap().load(f1).placeholder(
                            R.drawable.round_loading_animation).diskCacheStrategy(
                            DiskCacheStrategy.NONE).skipMemoryCache(force_update).into(notificationTarget);
                }
            }
            else
            {
                if (is_friend_avatar)
                {
                    GlideApp.with(c).asBitmap().load(f1).placeholder(placholder).diskCacheStrategy(
                            DiskCacheStrategy.NONE).signature(new com.bumptech.glide.signature.StringSignatureZ(
                            "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                            fl.avatar_update_timestamp)).skipMemoryCache(false).into(notificationTarget);
                }
                else
                {
                    GlideApp.with(c).asBitmap().load(f1).placeholder(placholder).diskCacheStrategy(
                            DiskCacheStrategy.NONE).skipMemoryCache(force_update).into(notificationTarget);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "put_vfs_image_on_imageview_real_remoteviews:EE1:" + e.getMessage());
        }
    }

    static String get_g_opts(String key)
    {
        try
        {
            if (orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobalsNew g_opts = orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).get(0);
                // Log.i(TAG, "get_g_opts:(SELECT):key=" + key);
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
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + "xxxxxxxxxxxxx");
            }
            catch (android.database.sqlite.SQLiteConstraintException e)
            {
                // e.printStackTrace();
                try
                {
                    orma.updateTRIFADatabaseGlobalsNew().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + "xxxxxxxxxxxxxxx");
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

    static void del_g_opts(String key)
    {
        try
        {
            orma.deleteFromTRIFADatabaseGlobalsNew().keyEq(key).execute();
            Log.i(TAG, "del_g_opts:(DELETE):key=" + key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "del_g_opts:EE2:" + e.getMessage());
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
                                CallingActivity.ca.right_top_text_2.setText(
                                        "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay);
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

    public static ByteBuffer hexstring_to_bytebuffer(String in)
    {
        try
        {
            byte[] in_bytes = in.getBytes(StandardCharsets.US_ASCII);
            ByteBuffer ret = ByteBuffer.allocateDirect(in_bytes.length / 2);
            for (int i = 0; i < in_bytes.length; i = i + 2)
            {
                // Log.i(TAG, "hexstring_to_bytebuffer:i=" + i + " byte=" + in_bytes[i] + " byte2=" + in_bytes[i + 1]);
                byte b1 = in_bytes[i + 1];
                byte b2 = in_bytes[i];
                // Log.i(TAG, "hexstring_to_bytebuffer:res=" + two_hex_bytes_to_dec_int(b1, b2));
                ret.put((byte) two_hex_bytes_to_dec_int(b1, b2));
            }

            ret.rewind();
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] bytebuffer_to_bytearray(ByteBuffer in)
    {
        byte[] out = new byte[in.limit()];
        in.rewind();
        in.get(out);
        return out;
    }

    public static int two_hex_bytes_to_dec_int(byte b1, byte b2)
    {
        int res = 0;
        // ascii:0 .. 9 -> byte:48 ..  57
        // ascii:A .. F -> byte:65 ..  70
        // ascii:a .. f -> byte:97 .. 102
        if (48 <= b1 && b1 <= 57)
        {
            res = b1 - 48;
        }
        else if ('A' <= b1 && b1 <= 'F')
        {
            res = b1 - 65 + 10;
        }
        else if ('a' <= b1 && b1 <= 'f')
        {
            res = b1 - 97 + 10;
        }

        if (48 <= b2 && b2 <= 57)
        {
            res = res + ((b2 - 48) * 16);
        }
        else if ('A' <= b2 && b2 <= 'F')
        {
            res = res + ((b2 - 65 + 10) * 16);
        }
        else if ('a' <= b2 && b2 <= 'f')
        {
            res = res + ((b2 - 97 + 10) * 16);
        }

        return res;
    }

    public static String utf8_string_from_bytes_with_padding(final ByteBuffer buf, final int max_bytes_output,
                                                             final String default_str)
    {
        String ret = default_str;
        try
        {
            byte[] byte_buf = new byte[max_bytes_output];
            Arrays.fill(byte_buf, (byte)0x0);
            buf.rewind();
            buf.get(byte_buf);

            int start_index = 0;
            int end_index = max_bytes_output - 1;
            for(int j=0;j<max_bytes_output;j++)
            {
                if (byte_buf[j] == 0)
                {
                    start_index = j+1;
                }
                else
                {
                    break;
                }
            }

            for(int j=(max_bytes_output-1);j>=0;j--)
            {
                if (byte_buf[j] == 0)
                {
                    end_index = j;
                }
                else
                {
                    break;
                }
            }

            byte[] byte_buf_stripped = Arrays.copyOfRange(byte_buf, start_index,end_index);
            ret = new String(byte_buf_stripped, StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static String fourbytes_of_long_to_hex(final long in)
    {
        return String.format("%08x", in);
    }

    public static String bytebuffer_to_hexstring(ByteBuffer in, boolean upper_case)
    {
        try
        {
            in.rewind();
            StringBuilder sb = new StringBuilder("");
            while (in.hasRemaining())
            {
                if (upper_case)
                {
                    sb.append(String.format("%02X", in.get()));
                }
                else
                {
                    sb.append(String.format("%02x", in.get()));
                }
            }
            in.rewind();
            return sb.toString();
        }
        catch (Exception e)
        {
            return null;
        }
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
                // e.printStackTrace();
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
            Log.i(TAG, "get_network_connections:EE01:" + e.getMessage());
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

    static String long_date_time_format_for_filename(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_time_long_for_filename.format(new Date(timestamp_in_millis));
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

    static String seconds_time_format_or_empty(long time_in_seconds)
    {
        try
        {
            return MainActivity.df_seconds_time.format(new Date(time_in_seconds * 1000));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
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

    static byte[] read_chunk_from_SD_file(String file_name_with_path, long position, long file_chunk_length, boolean real_file_path)
    {
        final byte[] out = new byte[(int) file_chunk_length];

        if (real_file_path)
        {
            try
            {
                RandomAccessFile raf = new RandomAccessFile(file_name_with_path, "r");
                FileChannel inChannel = raf.getChannel();
                MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, position, file_chunk_length);

                // Log.i(TAG, "read_chunk_from_SD_file:" + buffer.limit() + " <-> " + file_chunk_length);
                buffer.get(out);

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
        else
        {
            try
            {
                PositionInputStream fis = cache_ft_fis_saf.get(file_name_with_path);
                if (fis == null)
                {
                    InputStream fis_regular = context_s.getContentResolver().openInputStream(
                            Uri.parse(file_name_with_path));
                    fis = new PositionInputStream(fis_regular);
                    if (fis != null)
                    {
                        try
                        {
                            cache_ft_fis_saf.remove(file_name_with_path);
                        }
                        catch (Exception e)
                        {
                        }
                        cache_ft_fis_saf.put(file_name_with_path, fis);
                    }

                    long actually_skipped = fis.skip(position);
                    if (actually_skipped != position)
                    {
                        Log.i(TAG, "can NOT read check at position:" + position + " got pos=" + actually_skipped);
                    }
                }
                else
                {
                    if (fis.getPosition() < position)
                    {
                        fis.skip(position - fis.getPosition());
                    }
                    else
                    {
                        if (fis.getPosition() > position)
                        {
                            fis.close();
                            cache_ft_fis_saf.remove(file_name_with_path);
                            InputStream fis_regular = context_s.getContentResolver().openInputStream(
                                    Uri.parse(file_name_with_path));
                            fis = new PositionInputStream(fis_regular);
                            if (fis != null)
                            {
                                cache_ft_fis_saf.put(file_name_with_path, fis);
                            }

                            long actually_skipped = fis.skip(position);
                            if (actually_skipped != position)
                            {
                                Log.i(TAG,
                                      "can NOT read check at position:" + position + " got pos=" + actually_skipped);
                            }
                        }
                    }
                }

                fis.read(out, 0, (int) file_chunk_length);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return out;
    }

    static void write_chunk_to_VFS_file(String file_name_with_path, long position, long file_chunk_length, final byte[] data)
    {
        if (VFS_CUSTOM_WRITE_CACHE)
        {
            write_chunk_to_VFS_file__with_custom_write_cache(file_name_with_path, position, file_chunk_length, data);
        }
        else
        {
            write_chunk_to_VFS_file__no_extra_write_cache(file_name_with_path, position, file_chunk_length, data);
        }
    }

    static void write_chunk_to_VFS_file__no_extra_write_cache(String file_name_with_path, long position, long file_chunk_length, final byte[] data)
    {
        try
        {
            final ByteBuffer data_bb = ByteBuffer.wrap(data);
            info.guardianproject.iocipher.RandomAccessFile raf = new info.guardianproject.iocipher.RandomAccessFile(
                    file_name_with_path, "rw");
            info.guardianproject.iocipher.IOCipherFileChannel inChannel = raf.getChannel();
            // inChannel.lseek(position, OsConstants.SEEK_SET);
            inChannel.write(data_bb, position);

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

    static void write_chunk_to_VFS_file__with_custom_write_cache(String file_name_with_path, final long position, long file_chunk_length, final byte[] data)
    {
        try
        {
            BufferedOutputStreamCustom fos = cache_ft_fos.get(file_name_with_path);
            if (fos == null)
            {
                // Log.i(TAG, "write_chunk_to_VFS_file:cache:fail");
                fos = new BufferedOutputStreamCustom(file_name_with_path);
                try
                {
                    cache_ft_fos.remove(file_name_with_path);
                }
                catch (Exception e)
                {
                }
                cache_ft_fos.put(file_name_with_path, fos);
            }
            else
            {
                // Log.i(TAG,"write_chunk_to_VFS_file:cache:HIT");
            }

            // Log.i(TAG, "write_chunk_to_VFS_file:write:pos=" + position + " len=" + file_chunk_length);
            fos.seek(position);
            fos.write(data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static String get_fileExt(final String filename)
    {
        try
        {

            final Uri f = Uri.fromFile(new File(filename));
            return MimeTypeMap.getFileExtensionFromUrl(f.toString());
        }
        catch (Exception e)
        {
            return "";
        }
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

    public static float getColorLuminance(int color)
    {
        double gamma = 2.2;
        double r = Math.pow((Color.red(color) / 255.0), gamma);
        double g = Math.pow((Color.green(color) / 255.0), gamma);
        double b = Math.pow((Color.blue(color) / 255.0), gamma);

        return (float) ((0.2126 * r) + (0.7152 * g) + (0.0722 * b));
    }

    public static boolean isColorDarkLum(int color)
    {
        if (getColorLuminance(color) <= 0.5)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static double getColorDarkBrightness(int color)
    {
        double luminosity = Math.sqrt(Math.pow(Color.red(color), 2) * 0.299 + Math.pow(Color.green(color), 2) * 0.587 +
                                      Math.pow(Color.blue(color), 2) * 0.114);

        return luminosity;
    }

    public static boolean isColorDarkBrightness(int color)
    {
        double luminosity = Math.sqrt(Math.pow(Color.red(color), 2) * 0.299 + Math.pow(Color.green(color), 2) * 0.587 +
                                      Math.pow(Color.blue(color), 2) * 0.114);

        if (luminosity > 146)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static int lightenColor(int inColor, float inAmount)
    {
        return Color.argb(Color.alpha(inColor), (int) Math.min(255, red(inColor) + 255 * inAmount),
                          (int) Math.min(255, green(inColor) + 255 * inAmount),
                          (int) Math.min(255, blue(inColor) + 255 * inAmount));
    }

    public static int darkenColor(int inColor, float inAmount)
    {
        return Color.argb(Color.alpha(inColor), (int) Math.max(0, red(inColor) - 255 * inAmount),
                          (int) Math.max(0, green(inColor) - 255 * inAmount),
                          (int) Math.max(0, blue(inColor) - 255 * inAmount));
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
                    int friend_con_status = orma.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).get(
                            0).TOX_CONNECTION_real;
                    int relay_con_status = orma.selectFromFriendList().tox_public_key_stringEq(relay_).get(
                            0).TOX_CONNECTION_real;

                    if ((friend_con_status != TOX_CONNECTION_NONE.value) ||
                        (relay_con_status != TOX_CONNECTION_NONE.value))
                    {
                        // if one of them is online, return combined "online" as status
                        // TODO: do not always return TCP, make this better
                        ret = TOX_CONNECTION_TCP.value;
                    }
                }
            }
        }

        return ret;
    }

    /*************************************************************************/

    public static boolean tox_friend_resend_msgv3_wrapper(Message m)
    {
        if (m.msg_idv3_hash == null)
        {
            m.resend_count++;
            update_message_in_db_resend_count(m);
            return false;
        }

        if (m.msg_idv3_hash.length() < TOX_HASH_LENGTH)
        {
            m.resend_count++;
            update_message_in_db_resend_count(m);
            return false;
        }
        ByteBuffer hash_bytes = hexstring_to_bytebuffer(m.msg_idv3_hash);
        long res = MainActivity.tox_messagev3_friend_send_message(tox_friend_by_public_key__wrapper(m.tox_friendpubkey),
                                                                  TRIFA_MSG_TYPE_TEXT.value, m.text, hash_bytes,
                                                                  (m.sent_timestamp / 1000));

        m.resend_count++;
        m.message_id = res;
        update_message_in_db_resend_count(m);
        update_message_in_db_messageid(m);
        update_single_message(m, true);

        return true;
    }

    /*
     * returns: send_message_result class
     *
     *    send_message_result: NULL (if friend does not exist)
     *
     *    long msg_num: -98 (msgv3 error), -1 -2 -3 -4 -5 -6 (msgv3 error), -99 (msgv3 error),
     *                  "0..(UINT32_MAX-1)" (message sent OK)
     *
     *    long msg_num: -1 -2 -3 -4 -5 -6 -99 (msgv2 error),
     *                  MESSAGE_V2_MSG_SENT_OK (msgv2 sent OK), "0..(UINT32_MAX-1)" (message "old" sent OK)
     *
     *    boolean msg_v2: true if msgv2 was used, false otherwise
     *
     */
    public static MainActivity.send_message_result tox_friend_send_message_wrapper(final String friend_pubkey, int a_TOX_MESSAGE_TYPE, @NonNull String message, long timestamp_unixtime_seconds)
    {
        // Log.d(TAG, "tox_friend_send_message_wrapper:" + friend_pubkey);
        FriendList f = main_get_friend(friend_pubkey);
        long friendnum_to_use = tox_friend_by_public_key__wrapper(friend_pubkey);
        boolean need_call_push_url = false;

        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

        boolean msgv1 = true;

        if (f == null)
        {
            return null;
        }

        if ((f.capabilities & TOX_CAPABILITY_MSGV2) != 0)
        {
            msgv1 = false;
        }

        // Log.d(TAG, "tox_friend_send_message_wrapper:f conn" + f.TOX_CONNECTION_real);
        if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
        {
            String relay_pubkey = HelperRelay.get_relay_for_friend(f.tox_public_key_string);

            if (relay_pubkey != null)
            {
                // friend has a relay
                friendnum_to_use = tox_friend_by_public_key__wrapper(relay_pubkey);
                msgv1 = false;
                // Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
            }
            else // if friend is NOT online and does not have a relay, try if he has a push url
            {
                need_call_push_url = true;
            }
        }

        if (msgv1)
        {
            // old msgV1 message (but always send msgV3 format)
            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(hash_bytes);
            MainActivity.send_message_result result = new MainActivity.send_message_result();

            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_messagev3_friend_send_message(friendnum_to_use, a_TOX_MESSAGE_TYPE, message,
                                                                      hash_bytes, timestamp_unixtime_seconds);
            // Log.i(TAG, "tox_friend_send_message_wrapper:msg=" + message + " " + timestamp_unixtime_seconds + " " +
            //           long_date_time_format(timestamp_unixtime_seconds * 1000));

            result.msg_num = res;
            result.msg_v2 = false;
            result.msg_hash_hex = "";
            result.msg_hash_v3_hex = bytebuffer_to_hexstring(hash_bytes, true);
            result.raw_message_buf_hex = "";

            if (need_call_push_url)
            {
                friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
            }

            return result;
        }
        else // use msgV2
        {
            MainActivity.send_message_result result = new MainActivity.send_message_result();
            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
            ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            // use msg V2 API Call
            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE,
                                                                    timestamp_unixtime_seconds, message,
                                                                    message.length(), raw_message_buf,
                                                                    raw_message_length_buf, msg_id_buffer);
            if (PREF__X_battery_saving_mode)
            {
                Log.i(TAG, "global_last_activity_for_battery_savings_ts:002:*PING*");
            }
            final int len_low_byte = raw_message_length_buf.array()[raw_message_length_buf.arrayOffset()] & 0xFF;
            final int len_high_byte =
                    (raw_message_length_buf.array()[raw_message_length_buf.arrayOffset() + 1] & 0xFF) * 256;
            final int raw_message_length_int = len_low_byte + len_high_byte;

            result.error_num = res;

            if (res == -9999)
            {
                // msg V2 OK
                result.msg_num = MESSAGE_V2_MSG_SENT_OK;
                result.msg_v2 = true;
                result.msg_hash_hex = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                 msg_id_buffer.limit());
                result.raw_message_buf_hex = bytesToHex(raw_message_buf.array(), raw_message_buf.arrayOffset(),
                                                        raw_message_length_int);
                if (need_call_push_url)
                {
                    friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
                }
                return result;
            }
            else
            {
                if (res == -9991)
                {
                    result.msg_num = -1;
                }
                else
                {
                    result.msg_num = res;
                }

                result.msg_v2 = true;
                result.msg_hash_hex = "";
                result.raw_message_buf_hex = "";
                if (need_call_push_url)
                {
                    friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
                }
                return result;
            }
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

    static void update_savedata_file_wrapper_throttled()
    {
        // Log.i(TAG, "update_savedata_file_wrapper_throttled:** CALL");
        long currentTime = System.currentTimeMillis();

        if (currentTime - update_savedata_file_wrapper_throttled_last_trigger_ts >=
            INTERVAL_UPDATE_SAVE_FILE_WRAPPER_THROTTLED_MS)
        {
            update_savedata_file_wrapper_throttled_last_trigger_ts = currentTime;
            // Log.i(TAG, "update_savedata_file_wrapper_throttled:-> REAL");
            update_savedata_file_wrapper();
        }
        else
        {
            long delta_t_ms = currentTime - update_savedata_file_wrapper_throttled_last_trigger_ts;
            // Log.i(TAG, "update_savedata_file_wrapper_throttled:  TRIG delta ms=" + delta_t_ms);
            long trigger_in_ms_again = INTERVAL_UPDATE_SAVE_FILE_WRAPPER_THROTTLED_MS - delta_t_ms;
            if ((trigger_in_ms_again < 1) || (trigger_in_ms_again > (INTERVAL_UPDATE_SAVE_FILE_WRAPPER_THROTTLED_MS + 1)))
            {
                trigger_in_ms_again = INTERVAL_UPDATE_SAVE_FILE_WRAPPER_THROTTLED_MS;
            }
            final long trigger_in_ms_again_ = trigger_in_ms_again + 2;
            final Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(trigger_in_ms_again_);
                    // Log.i(TAG, "update_savedata_file_wrapper_throttled:__ CALL from Trigger");
                    update_savedata_file_wrapper_throttled();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    static void update_savedata_file_wrapper()
    {
        if (is_tox_started == true)
        {
            //Log.i(TAG, "update_savedata_file:delta_ms="
            //           + (System.currentTimeMillis() - update_savedata_file_wrapper_last_ts));
            //update_savedata_file_wrapper_last_ts = System.currentTimeMillis();
            try
            {
                MainActivity.semaphore_tox_savedata.acquire();
                long start_timestamp = System.currentTimeMillis();
                MainActivity.update_savedata_file(TrifaSetPatternActivity.bytesToString(
                        TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(PREF__DB_secrect_key))));
                long end_timestamp = System.currentTimeMillis();
                MainActivity.semaphore_tox_savedata.release();
                // Log.i(TAG,
                //      "update_savedata_file() took:" + (((float) (end_timestamp - start_timestamp)) / 1000f) + "s");
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

    static void receive_incoming_message(int msg_type, int tox_message_type, long friend_number, String friend_message_text_utf8, byte[] raw_message, long raw_message_length, String original_sender_pubkey, byte[] msgV3hash_bin, long message_timestamp)
    {
        // incoming msg can be:
        // (msg_type == 0) msgV1 text only message -> msg_type, friend_number, friend_message_text_utf8 [, msgV3hash_bin, message_timestamp]
        // (msg_type == 1) msgV2 direct message    -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length
        // (msg_type == 2) msgV2 relay message     -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length, original_sender_pubkey
        if (msg_type == 0)
        {
            // msgV1 text only message
            // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash:" + msgV3hash_bin);

            String msgV3hash_hex_string = null;
            if (msgV3hash_bin != null)
            {
                msgV3hash_hex_string = HelperGeneric.bytesToHex(msgV3hash_bin, 0, msgV3hash_bin.length);

                int got_messages = orma.selectFromMessage().tox_friendpubkeyEq(
                        HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).directionEq(0).msg_idv3_hashEq(
                        msgV3hash_hex_string).count();

                // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash_hex_string:" + msgV3hash_hex_string +
                //           " got_messages=" + got_messages);

                if (got_messages > 0)
                {
                    // HINT: we already have received a message with this hash
                    // still send the msgV3 high level ACK, and then ignore
                    HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
                    return;
                }
            }

            if (tox_message_type == TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value)
            {
                // TODO: ack message in database and update messagelist UI
                process_msgv3_high_level_ack(friend_number, msgV3hash_hex_string, message_timestamp);
                return;
            }

            if (msgV3hash_bin != null)
            {
                int got_messages_mirrored = orma.selectFromMessage().tox_friendpubkeyEq(
                        HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).directionEq(1).msg_idv3_hashEq(
                        msgV3hash_hex_string).count();

                //Log.i(TAG, "update_friend_msgv3_capability:got_messages_mirrored=" + got_messages_mirrored + " hash1=" +
                //           msgV3hash_bin + " " + msgV3hash_hex_string);
                if (got_messages_mirrored > 0)
                {
                    update_friend_msgv3_capability(friend_number, 0);
                }
                else
                {
                    update_friend_msgv3_capability(friend_number, 1);
                }
            }
            else
            {
                // Log.i(TAG, "update_friend_msgv3_capability:hash0=" + msgV3hash_bin + " " + msgV3hash_hex_string);
                update_friend_msgv3_capability(friend_number, 0);
            }

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
            if (message_timestamp > 0)
            {
                m.sent_timestamp = message_timestamp * 1000;
            }
            else
            {
                m.sent_timestamp = System.currentTimeMillis();
            }
            m.sent_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 0;
            m.msg_idv3_hash = msgV3hash_hex_string;
            m.sent_push = 0;

            long db_result = -1;

            if (MainActivity.message_list_activity != null)
            {
                if (MainActivity.message_list_activity.get_current_friendnum() == friend_number)
                {
                    db_result = HelperMessage.insert_into_message_db(m, true);
                }
                else
                {
                    db_result = HelperMessage.insert_into_message_db(m, false);
                }
            }
            else
            {
                db_result = HelperMessage.insert_into_message_db(m, false);
            }

            if (db_result == -1)
            {
                // error on inserting message into db, most likely msgV3 double message
                return;
            }

            String friendname = null;
            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                // HelperFriend.update_single_friend_in_friendlist_view(f);
                HelperFriend.add_all_friends_clear_wrapper(0);

                if (f.notification_silent)
                {
                    do_notification = false;
                }

                if (f.alias_name == null)
                {
                    friendname = f.name;
                }
                else if (f.alias_name.length() < 1)
                {
                    friendname = f.name;
                }
                else
                {
                    friendname = f.alias_name;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey, friendname, m.text);
            }

            if (msgV3hash_hex_string != null)
            {
                HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
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
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string + " len=" +
            //           msg_id_as_hex_string.length());
            int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).and().msg_id_hashEq(
                    msg_id_as_hex_string).count();

            long pin_timestamp = System.currentTimeMillis();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // TODO: use received timstamp, not "now" here!
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer,
                                                                (pin_timestamp / 1000));
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
            m.ft_outgoing_queued = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = pin_timestamp;
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            m.sent_push = 0;
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

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

            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer,
                                                            (pin_timestamp / 1000));
            String friendname = null;
            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                // HelperFriend.update_single_friend_in_friendlist_view(f);
                HelperFriend.add_all_friends_clear_wrapper(0);

                if (f.notification_silent)
                {
                    do_notification = false;
                }

                if (f.alias_name == null)
                {
                    friendname = f.name;
                }
                else if (f.alias_name.length() < 1)
                {
                    friendname = f.name;
                }
                else
                {
                    friendname = f.alias_name;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey, friendname, m.text);
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

            long pin_timestamp = System.currentTimeMillis();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // Log.i(TAG,
                //      "receive_incoming_message:ACK1:" + get_friend_name_from_num(friend_number_real_sender) + " " +
                //      msg_id_as_hex_string);
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer,
                                                                (pin_timestamp / 1000));
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
            m.ft_outgoing_queued = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = pin_timestamp;
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            m.sent_push = 0;
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
            // Log.i(TAG, "receive_incoming_message:ACK2:" + get_friend_name_from_num(friend_number_real_sender) + " " +
            //           msg_id_as_hex_string);
            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer,
                                                            (pin_timestamp / 1000));

            String friendname = null;
            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                // HelperFriend.update_single_friend_in_friendlist_view(f);
                HelperFriend.add_all_friends_clear_wrapper(0);

                if (f.notification_silent)
                {
                    do_notification = false;
                }

                if (f.alias_name == null)
                {
                    friendname = f.name;
                }
                else if (f.alias_name.length() < 1)
                {
                    friendname = f.name;
                }
                else
                {
                    friendname = f.alias_name;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey, friendname, m.text);
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
        // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:all:--START--:");
        // long s_time_a = System.currentTimeMillis();

/*
        try
        {
            android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
            // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/

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
                        // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:feed:--START--:");
                        // long s_time = System.currentTimeMillis();

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

                        // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:feed:--END--:" + (System.currentTimeMillis() - s_time) + "ms");
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
                        // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:fetch:--START--:");
                        // long s_time = System.currentTimeMillis();

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
                                    // Log.i(TAG, "toxav_video_send_frame_h264_age:--START--");
                                    // long s_time_send = System.currentTimeMillis();
                                    MainActivity.toxav_video_send_frame_h264_age(friendnum, frame_width_px,
                                                                                 frame_height_px, data_length,
                                                                                 (int) (System.currentTimeMillis() -
                                                                                        video_frame_age) +
                                                                                 Callstate.delay_add);

                                    // Log.i(TAG, "toxav_video_send_frame_h264_age:--END--:" + (System.currentTimeMillis() - s_time_send) + "ms");
                                }
                                else
                                {
                                    // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:buf_out==null:#" + jj);
                                }
                            }
                        }

                        // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:fetch:--END--:" + (System.currentTimeMillis() - s_time) + "ms");

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

            if (1 == 1)
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

            // Log.i(TAG, "toxav_video_send_frame_uv_reversed_wrapper:all:--END--:" + (System.currentTimeMillis() - s_time_a) + "ms");

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

    static void import_toxsave_file_unsecure(final Context context)
    {
        MainActivity.global_stop_tox();
        File f_src = new File(MainActivity.SD_CARD_FILES_EXPORT_DIR + "/" + "I_WANT_TO_IMPORT_savedata.tox");

        if (!f_src.exists())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Import Tox Savedata");
            builder.setMessage("Import ERROR:" + "\n\n" + "import file does not exist");

            builder.setPositiveButton("Ok", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }

        Log.i(TAG, "Waiting for ToxThread to stop ...");
        try
        {
            while(true)
            {
                // HINT: wait for tox thread to stop ...
                //noinspection BusyWait
                Thread.sleep(50);
                if ((stop_tox_fg_done) && (!is_tox_started))
                {
                    Log.i(TAG, "ToxThread has ended");
                    break;
                }
            }
        }
        catch (Exception ignored)
        {
        }

        //
        orma.deleteFromFriendList().execute();
        orma.deleteFromConferenceDB().execute();
        orma.deleteFromGroupDB().execute();
        //
        orma.deleteFromMessage();
        orma.deleteFromConferenceMessage();
        orma.deleteFromGroupMessage();
        //
        orma.deleteFromConferencePeerCacheDB();
        orma.deleteFromFileDB();
        orma.deleteFromFiletransfer();
        orma.deleteFromRelayListDB();
        //

        File f_dst = new File(MainActivity.app_files_directory + "/" + "savedata.tox");
        try
        {
            ls_file(f_src);
            ls_file(f_dst);

            // write toxsave data
            io_file_copy(f_src, f_dst);

            ls_file(f_dst);

            try
            {
                // delete unencrypted import file
                f_src.delete();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Import Tox ToxSaveFile");
        builder.setMessage("Import OK:" + "\n\n" + "Now TRIfA will restart");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                try
                {
                    Log.i(TAG, "Import ToxSaveFile complete, now exiting the application.");
                    System.exit(0);
                }
                catch (Exception ignored)
                {
                }
                return;
            }
        });

        // create and show the alert dialog
        Runnable myRunnable = () -> {
            try
            {
                final AlertDialog dialog = builder.create();
                Log.i(TAG, "Import ToxSaveFile: show final Dialog.");
                dialog.show();
            }
            catch (Exception e)
            {
                Log.i(TAG, "Import ToxSaveFile with ERRORs, still exiting the application.");
                System.exit(0);
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
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

    static boolean battery_saving_can_sleep()
    {
        if ((last_log_battery_savings_criteria_ts + 60000) < System.currentTimeMillis())
        {
            last_log_battery_savings_criteria_ts = System.currentTimeMillis();
            Log.i(TAG, "battery_saving_can_sleep:global_self_connection_status:" + global_self_connection_status +
                       " global_showing_messageview=" + global_showing_messageview + " global_showing_anygroupview=" +
                       global_showing_anygroupview + " Callstate.state=" + Callstate.state +
                       " Callstate.audio_group_active=" + Callstate.audio_group_active +
                       " global_self_last_went_online_timestamp=" + global_self_last_went_online_timestamp +
                       " global_self_last_went_online_timestamp2=" +
                       (System.currentTimeMillis() - global_self_last_went_online_timestamp) +
                       " global_last_activity_for_battery_savings_ts=" + global_last_activity_for_battery_savings_ts +
                       " global_last_activity_for_battery_savings_ts2=" +
                       (System.currentTimeMillis() - global_last_activity_for_battery_savings_ts) +
                       " SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE=" +
                       SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE + " System.currentTimeMillis()=" +
                       System.currentTimeMillis());
        }

        if ((!global_showing_messageview) && (!global_showing_anygroupview) && (Callstate.state == 0) &&
            (!Callstate.audio_group_active) && (!Callstate.audio_ngc_group_active))
        {
            if (global_self_last_went_online_timestamp != -1)
            {
                if ((global_self_last_went_online_timestamp + SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE * 1000) <
                    System.currentTimeMillis())
                {
                    if ((global_last_activity_for_battery_savings_ts +
                         SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE * 1000) < System.currentTimeMillis())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static void vfs__detach()
    {
        try
        {
            //Log.i(TAG, "VFS:detachThread:" + Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
            //vfs.detachThread();
            //Log.i(TAG, "VFS:detachThread:OK");

            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    Log.i(TAG, "VFS:detachThread:" + Thread.currentThread().getId() + ":" +
                               Thread.currentThread().getName());
                    vfs.detachThread();
                    Log.i(TAG, "VFS:detachThread:OK");
                }
            };
            if (main_handler_s != null)
            {
                //main_handler_s.post(myRunnable);
            }
            //Thread.sleep(400);

            Log.i(TAG, "VFS:detachThread:END");
        }
        catch (Exception e5)
        {
            Log.i(TAG, "VFS:detachThread:EE5:" + e5.getMessage());
            e5.printStackTrace();
        }
    }

    static void vfs__unmount()
    {
        Log.i(TAG, "VFS:unmount:start ...");

        try
        {
            Log.i(TAG, "VFS:unmount:start[2] ...");

            if (vfs.isMounted())
            {
                Log.i(TAG, "VFS:unmount:2:" + Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
                vfs.unmount();
            }
            Log.i(TAG, "VFS:unmount:2:OK");

            Thread.sleep(10);
            Log.i(TAG, "VFS:unmount:END");
        }
        catch (Exception e5)
        {
            Log.i(TAG, "VFS:unmount:EE01:" + e5.getMessage());
            e5.printStackTrace();
        }

    }

    static String get_sqlite_search_string(String search_string_in)
    {
        String sql_like_pattern = "";

        try
        {
            // TODO: seems default "ESCAPE" is "\"
            // and "like" is set to caseinsensitive

            // TODO: check if there is a possible SQL injection here
            //       the good news is that only text that the user entered himself will go here
            //       so a user would have to want to self sabotage, by trying to inject here
            String tmp_str = search_string_in.replace("\\", "\\\\"). // \ -> \\
                    replace("%", "\\%"). // % -> \%
                    replace("_", "\\_"). // _ -> \_
                    replace("'", "''"); // ' -> ''
            // Log.i(TAG, "get_sqlite_search_string:" + tmp_str);
            sql_like_pattern = "%" + tmp_str + "%";
        }
        catch (Exception e)
        {
        }

        return sql_like_pattern;
    }

    static void print_stack_trace()
    {
        try
        {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (int i = 3; i < elements.length; i++)
            {
                StackTraceElement s = elements[i];
                Log.i("STACK_TRACE:",
                      "STACK_TRACE:\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" +
                      s.getLineNumber() + ")");
            }
        }
        catch (Exception e)
        {
        }
    }

    /*
     *
     * print the caller method and filename and linenumer
     *
     */
    static void get_caller_method()
    {
        try
        {
            Log.i(TAG, "CALLED_BY:" + Thread.currentThread().getStackTrace()[4].getMethodName() + " " +
                       Thread.currentThread().getStackTrace()[4].getFileName() + ":" + Thread.currentThread().getStackTrace()[4].getLineNumber());
        }
        catch(Exception ignored)
        {
        }
    }

    static void draw_main_top_icon__real(ImageView view, Context c, int blur_color, boolean is_fg)
    {
        try
        {
            view.setBackgroundColor(Color.TRANSPARENT);

            Drawable d = c.getResources().getDrawable(R.drawable.web_hi_res_512);
            Drawable currentState = d.getCurrent();
            if (currentState instanceof BitmapDrawable)
            {
                Bitmap bm1 = ((BitmapDrawable) currentState).getBitmap();
                Bitmap bm = bm1.copy(bm1.getConfig(), true);

                Bitmap alpha = bm.extractAlpha();

                int radius = 40;
                if (is_nightmode_active(c))
                {
                    radius = 80;
                }

                BlurMaskFilter blurMaskFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER);

                Paint paint = new Paint();
                paint.setMaskFilter(blurMaskFilter);
                paint.setColor(blur_color);

                Canvas canvas = new Canvas(bm);
                canvas.drawBitmap(alpha, 0, 0, paint);

                view.setImageBitmap(bm);
            }
        }
        catch (Exception e)
        {
        }
    }

    static void draw_main_top_icon(ImageView view, Context c, int blur_color, boolean is_fg)
    {
        try
        {
            if (!is_fg)
            {
                Runnable myRunnable = () -> {
                    try
                    {
                        draw_main_top_icon__real(view, c, blur_color, is_fg);
                    }
                    catch (Exception e)
                    {
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
            else
            {
                draw_main_top_icon__real(view, c, blur_color, is_fg);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void touch(File file) throws IOException
    {
        if (!file.exists())
        {
            File parent = file.getParentFile();
            if (parent != null)
            {
                if (!parent.exists())
                {
                    if (!parent.mkdirs())
                    {
                        throw new IOException("Cannot create parent directories for file: " + file);
                    }
                }
            }
            file.createNewFile();
        }

        boolean success = file.setLastModified(System.currentTimeMillis());
        if (!success)
        {
            throw new IOException("Unable to set the last modification time for " + file);
        }
    }

    public static boolean string_is_in_list(String input, String[] list)
    {
        try
        {
            return (Arrays.asList(list).contains(input));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean validate_ipv4(final String ip)
    {
        try
        {
            return PATTERN_IPV4.matcher(ip).matches();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Validate the given IPv4 or IPv6 address.
     *
     * @param address the IP address as a String.
     * @return true if a valid address, false otherwise
     */
    public static boolean IPisValid(String address)
    {
        return isValidIPv4(address) || isValidIPv6(address);
    }

    /**
     * Validate the given IPv4 or IPv6 address and netmask.
     *
     * @param address the IP address as a String.
     * @return true if a valid address with netmask, false otherwise
     */
    public static boolean IPisValidWithNetMask(String address)
    {
        return isValidIPv4WithNetmask(address) || isValidIPv6WithNetmask(address);
    }

    /**
     * Validate the given IPv4 address.
     *
     * @param address the IP address as a String.
     * @return true if a valid IPv4 address, false otherwise
     */
    public static boolean isValidIPv4(String address)
    {
        if (address.length() == 0)
        {
            return false;
        }

        int octet;
        int octets = 0;

        String temp = address + ".";

        int pos;
        int start = 0;
        while (start < temp.length() && (pos = temp.indexOf('.', start)) > start)
        {
            if (octets == 4)
            {
                return false;
            }
            try
            {
                octet = Integer.parseInt(temp.substring(start, pos));
            }
            catch (NumberFormatException ex)
            {
                return false;
            }
            if (octet < 0 || octet > 255)
            {
                return false;
            }
            start = pos + 1;
            octets++;
        }

        return octets == 4;
    }

    public static boolean isValidIPv4WithNetmask(String address)
    {
        int index = address.indexOf("/");
        String mask = address.substring(index + 1);

        return (index > 0) && isValidIPv4(address.substring(0, index)) && (isValidIPv4(mask) || isMaskValue(mask, 32));
    }

    public static boolean isValidIPv6WithNetmask(String address)
    {
        int index = address.indexOf("/");
        String mask = address.substring(index + 1);

        return (index > 0) &&
               (isValidIPv6(address.substring(0, index)) && (isValidIPv6(mask) || isMaskValue(mask, 128)));
    }

    public static boolean isIPPortValid(String port)
    {
        // HINT: valid number for IP ports: 1 - 65535
        try
        {
            int port_int = Integer.parseInt(port);
            if ((port_int > 0) && (port_int < 65535))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    private static boolean isMaskValue(String component, int size)
    {
        try
        {
            int value = Integer.parseInt(component);

            return value >= 0 && value <= size;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Validate the given IPv6 address.
     *
     * @param address the IP address as a String.
     * @return true if a valid IPv4 address, false otherwise
     */
    public static boolean isValidIPv6(String address)
    {
        if (address.length() == 0)
        {
            return false;
        }

        int octet;
        int octets = 0;

        String temp = address + ":";
        boolean doubleColonFound = false;
        int pos;
        int start = 0;
        while (start < temp.length() && (pos = temp.indexOf(':', start)) >= start)
        {
            if (octets == 8)
            {
                return false;
            }

            if (start != pos)
            {
                String value = temp.substring(start, pos);

                if (pos == (temp.length() - 1) && value.indexOf('.') > 0)
                {
                    if (!isValidIPv4(value))
                    {
                        return false;
                    }

                    octets++; // add an extra one as address covers 2 words.
                }
                else
                {
                    try
                    {
                        octet = Integer.parseInt(temp.substring(start, pos), 16);
                    }
                    catch (NumberFormatException ex)
                    {
                        return false;
                    }
                    if (octet < 0 || octet > 0xffff)
                    {
                        return false;
                    }
                }
            }
            else
            {
                if (pos != 1 && pos != temp.length() - 1 && doubleColonFound)
                {
                    return false;
                }
                doubleColonFound = true;
            }
            start = pos + 1;
            octets++;
        }

        return octets == 8 || doubleColonFound;
    }


    public static String trim_to_utf8_length_bytes(String input_string, final int max_length_in_bytes)
    {
        try
        {
            do
            {
                byte[] valueInBytes = null;
                valueInBytes = input_string.getBytes(StandardCharsets.UTF_8);

                if (valueInBytes.length > max_length_in_bytes)
                {
                    input_string = input_string.substring(0, input_string.length() - 1);
                }
                else
                {
                    return input_string;
                }
            }
            while (input_string.length() > 0);
        }
        catch (Exception e)
        {
        }

        return null;
    }

    static boolean is_nightmode_active(final Context c)
    {
        try
        {
            final int nightModeFlags = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags)
            {
                case Configuration.UI_MODE_NIGHT_YES:
                    return true;
                case Configuration.UI_MODE_NIGHT_NO:
                    return false;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    return false;
            }
        }
        catch (Exception e)
        {

        }
        return false;
    }

    static void display_toast(final String toast_text, final boolean length_long, final int delay_ms)
    {
        display_toast_with_context(context_s, toast_text, length_long, delay_ms);
    }

    static void display_toast_with_context(final Context c, final String toast_text, final boolean length_long, final int delay_ms)
    {
        try
        {
            int toast_length_ = Toast.LENGTH_SHORT;

            if (length_long)
            {
                toast_length_ = Toast.LENGTH_LONG;
            }

            final int toast_length = toast_length_;

            if (delay_ms == 0)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Toast.makeText(c, toast_text, toast_length).show();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
            else
            {
                try
                {
                    Thread t = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sleep(delay_ms);
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                            }

                            Runnable myRunnable = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        Toast.makeText(c, toast_text, toast_length).show();
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            if (main_handler_s != null)
                            {
                                main_handler_s.post(myRunnable);
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "display_toast_with_context:EE01:" + e.getMessage());
        }
    }

    static void display_toast_with_context_custom_duration(final Context c, final String toast_text, final int custom_duration_ms, final int delay_ms)
    {
        try
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    final Toast toast = Toast.makeText(c, toast_text, Toast.LENGTH_SHORT);
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            toast.cancel();
                        }
                    }, custom_duration_ms);
                }
            }, delay_ms);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "display_toast_with_context_custom_duration:EE01:" + e.getMessage());
        }
    }

    static String filter_out_non_hex_chars(final String in)
    {
        if (in == null)
        {
            // string is null
            return null;
        }

        final String out = in.toUpperCase().replaceAll("[^0-9A-F]", "");
        return out;
    }

    static boolean is_valid_tox_public_key(final String pubkey)
    {
        if (pubkey == null)
        {
            // string is null
            return false;
        }

        if (pubkey.length() != (TOX_PUBLIC_KEY_SIZE * 2))
        {
            // string length is not correct
            return false;
        }

        if (!pubkey.toUpperCase().matches("^[A-Z0-9]+$"))
        {
            // string contains illegal characters
            return false;
        }

        return true;
    }

    public static void do_fade_anim_on_fab(final FloatingActionButton fab, boolean fade_in, final String caller_class_name)
    {
        try
        {
            // Log.i(TAG, "caller_class_name:" + caller_class_name);
            // com.zoffcc.applications.trifa.MessageListFragment
            // com.zoffcc.applications.trifa.MessageListActivity
            // com.zoffcc.applications.trifa.ConferenceMessageListFragment
            // com.zoffcc.applications.trifa.ConferenceMessageListActivity
            // com.zoffcc.applications.trifa.GroupMessageListFragment
            // com.zoffcc.applications.trifa.GroupMessageListActivity

            if (fade_in)
            {
                if (caller_class_name.contains("rifa.ConferenceMessage"))
                {
                    ConferenceMessageListFragment.faded_in = true;
                }
                else if (caller_class_name.contains("rifa.GroupMessage"))
                {
                    GroupMessageListFragment.faded_in = true;
                }
                else
                {
                    MessageListFragment.faded_in = true;
                }

                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(fab, "scaleX", 0.0f, 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(fab, "scaleY", 0.0f, 1f);
                scaleUpX.setDuration(FAB_SCROLL_TO_BOTTOM_FADEOUT_MS);
                scaleUpY.setDuration(FAB_SCROLL_TO_BOTTOM_FADEOUT_MS);

                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.play(scaleUpX).with(scaleUpY);

                scaleUp.start();
            }
            else
            {
                if (caller_class_name.contains("rifa.ConferenceMessage"))
                {
                    ConferenceMessageListFragment.faded_in = false;
                }
                else if (caller_class_name.contains("rifa.GroupMessage"))
                {
                    GroupMessageListFragment.faded_in = false;
                }
                else
                {
                    MessageListFragment.faded_in = false;
                }

                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0.0f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0.0f);
                scaleDownX.setDuration(FAB_SCROLL_TO_BOTTOM_FADEIN_MS);
                scaleDownY.setDuration(FAB_SCROLL_TO_BOTTOM_FADEIN_MS);

                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);

                scaleDown.start();
            }
        }
        catch (Exception ignored)
        {
        }
    }

    public static void set_fade_anim_on_fab(final FloatingActionButton fab, boolean fade_in)
    {
        try
        {
            int fade_ms = FAB_SCROLL_TO_BOTTOM_FADEOUT_MS;
            int start = 1;
            int end = 0;

            if (fade_in)
            {
                fade_ms = FAB_SCROLL_TO_BOTTOM_FADEIN_MS;
                start = 0;
                end = 1;
            }

            final AlphaAnimation anim_fade_out = new AlphaAnimation(start, end);
            anim_fade_out.setDuration(fade_ms);
            anim_fade_out.setStartOffset(fade_ms);
            anim_fade_out.setFillAfter(false);

            fab.setAnimation(anim_fade_out);
        }
        catch (Exception ignored)
        {
        }
    }

    public static void fill_own_avatar_icon(Context context, CircleImageView img_avatar)
    {
        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(
                context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                String fname = get_vfs_image_filename_own_avatar();

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    if (fname != null)
                    {
                        f1 = new info.guardianproject.iocipher.File(fname);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fname != null))
                {
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.with(context).load(f1).diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(
                                false).apply(glide_options).into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void fill_friend_avatar_icon(Message m, Context context, CircleImageView img_avatar)
    {
        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(
                context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                FriendList fl = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).get(0);

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.with(context).load(f1).diskCacheStrategy(DiskCacheStrategy.RESOURCE).signature(
                                new com.bumptech.glide.signature.StringSignatureZ(
                                        "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                        fl.avatar_update_timestamp)).skipMemoryCache(false).apply(
                                glide_options).priority(Priority.HIGH).into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_avatar_img_height_in_chat(CircleImageView v)
    {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        if (PREF__compact_chatlist)
        {
            p.height = (int) dp2px(MESSAGE_AVATAR_HEIGHT_COMPACT_LAYOUT[PREF__global_font_size]);
        }
        else
        {
            p.height = (int) dp2px(MESSAGE_AVATAR_HEIGHT_NORMAL_LAYOUT[PREF__global_font_size]);
        }
        v.setLayoutParams(p);
    }

    public static void clear_audio_play_buffers()
    {
        try
        {
            audio_buffer_2.clear();
            byte[] b2 = new byte[audio_buffer_2.limit()];
            audio_buffer_2.put(b2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "clear audio play buffers:003:EE03" + e.getMessage());
        }

        try
        {
            for (int i = 0; i < NativeAudio.n_audio_in_buffer_max_count; i++)
            {
                NativeAudio.n_audio_buffer[i].clear();
                Log.i(TAG, "clear audio play buffers:008:limit=" + NativeAudio.n_audio_buffer[i].limit());
                byte[] b3 = new byte[NativeAudio.n_audio_buffer[i].limit()];
                NativeAudio.n_audio_buffer[i].put(b3);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "clear audio play buffers:008:EE08" + e.getMessage());
        }
    }

    synchronized public static void set_calling_audio_mode()
    {
        // NOOP , now!
    }

    synchronized public static void reset_audio_mode()
    {
        get_caller_method();
        try
        {
            print_stack_trace();
            MainActivity.audio_manager_s.setMode(AudioManager.MODE_NORMAL);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            {
                MainActivity.audio_manager_s.clearCommunicationDevice();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            set_rec_preset(true);
        }
        catch(Exception ignored)
        {
        }

        if ((Callstate.state != 0) || (Callstate.audio_group_active) || (Callstate.audio_ngc_group_active))
        {
            Log.i(TAG,"stop_audio_system:001:preset_TRUE");
            stop_audio_system();
        }
    }

    synchronized public static void set_audio_to_loudspeaker(AudioManager manager)
    {
        try
        {
            set_rec_preset(true);
        }
        catch(Exception ignored)
        {
        }

        if ((Callstate.state != 0) || (Callstate.audio_group_active) || (Callstate.audio_ngc_group_active))
        {
            Log.i(TAG,"restart_audio_system__normal_call:002:preset_TRUE");
            restart_audio_system();
        }

        try
        {
            Log.i(TAG, "AUDIOROUTE:set_audio_to_loudspeaker");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            {
                manager.setMode(AudioManager.MODE_NORMAL);
                Boolean result = null;
                ArrayList<Integer> targetTypes = new ArrayList<>();
                targetTypes.add(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
                List<AudioDeviceInfo> devices = manager.getAvailableCommunicationDevices();
                for (Integer targetType : targetTypes)
                {
                    for (AudioDeviceInfo device : devices)
                    {
                        if (device.getType() == targetType)
                        {
                            result = manager.setCommunicationDevice(device);
                            Log.i(TAG, "AUDIOROUTE:setCommunicationDevice type:" + targetType + " result:" + result);
                            if (result)
                            {
                                Callstate.audio_speaker = true;
                            }
                        }
                    }
                }
            }
            else
            {
                print_stack_trace();
                manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                manager.setWiredHeadsetOn(false);
                manager.setBluetoothScoOn(false);
                manager.setSpeakerphoneOn(true);
                Callstate.audio_speaker = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "AUDIOROUTE:set_audio_to_loudspeaker:EE:" + e.getMessage());
        }
    }

    synchronized public static void set_audio_to_ear(AudioManager manager)
    {
        try
        {
            Log.i(TAG, "AUDIOROUTE:set_audio_to_ear");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            {
                manager.setMode(AudioManager.MODE_NORMAL);
                Boolean result = null;
                ArrayList<Integer> targetTypes = new ArrayList<>();
                targetTypes.add(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
                List<AudioDeviceInfo> devices = manager.getAvailableCommunicationDevices();
                for (Integer targetType : targetTypes)
                {
                    for (AudioDeviceInfo device : devices)
                    {
                        if (device.getType() == targetType)
                        {
                            result = manager.setCommunicationDevice(device);
                            Log.i(TAG, "AUDIOROUTE:setCommunicationDevice type:" + targetType + " result:" + result);
                            if (result)
                            {
                                Callstate.audio_speaker = false;
                            }
                        }
                    }
                }
            }
            else
            {
                print_stack_trace();
                manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                manager.setWiredHeadsetOn(false);
                manager.setBluetoothScoOn(false);
                manager.setSpeakerphoneOn(false);
                Callstate.audio_speaker = false;
            }

            try
            {
                set_rec_preset(false);
            }
            catch(Exception ignored)
            {
            }

            if ((Callstate.state != 0) || (Callstate.audio_group_active) || (Callstate.audio_ngc_group_active))
            {
                Log.i(TAG,"restart_audio_system__normal_call:003:preset_false");
                restart_audio_system();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "AUDIOROUTE:set_audio_to_ear:EE:" + e.getMessage());
        }
    }

    synchronized public static void set_audio_to_headset(AudioManager manager)
    {
        try
        {
            Log.i(TAG, "AUDIOROUTE:set_audio_to_headset");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            {
                manager.setMode(AudioManager.MODE_NORMAL);
                Boolean result = null;
                ArrayList<Integer> targetTypes = new ArrayList<>();
                targetTypes.add(AudioDeviceInfo.TYPE_WIRED_HEADSET);
                List<AudioDeviceInfo> devices = manager.getAvailableCommunicationDevices();
                for (Integer targetType : targetTypes)
                {
                    for (AudioDeviceInfo device : devices)
                    {
                        if (device.getType() == targetType)
                        {
                            result = manager.setCommunicationDevice(device);
                            Log.i(TAG, "AUDIOROUTE:setCommunicationDevice type:" + targetType + " result:" + result);
                            if (result)
                            {
                                // HINT: set some var here?
                            }
                        }
                    }
                }
            }
            else
            {
                print_stack_trace();
                manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                manager.setBluetoothScoOn(false);
                manager.setSpeakerphoneOn(false);
                manager.setWiredHeadsetOn(true);
            }

            try
            {
                set_rec_preset(false);
            }
            catch(Exception ignored)
            {
            }

            if ((Callstate.state != 0) || (Callstate.audio_group_active) || (Callstate.audio_ngc_group_active))
            {
                Log.i(TAG,"restart_audio_system__normal_call:004:preset_false");
                restart_audio_system();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "AUDIOROUTE:set_audio_to_headset:EE:" + e.getMessage());
        }
    }

    static void restart_audio_system()
    {
        get_caller_method();
        Log.i(TAG, "restart_audio_system:enter");
        stop_audio_system();
        start_audio_system();
        Log.i(TAG, "restart_audio_system:DONE");
    }

    static void start_audio_system()
    {
        get_caller_method();
        Log.i(TAG, "start_audio_system:enter");
        synchronized (audio_system_start_stop_lock)
        {
            try
            {
                if (AudioReceiver.stopped)
                {
                    CallingActivity.audio_receiver_thread = new AudioReceiver();
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
                    CallingActivity.audio_thread = new AudioRecording();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "start_audio_system:DONE");
    }

    static void stop_ngc_audio_system()
    {
        get_caller_method();
        Log.i(TAG, "stop_ngc_audio_system:enter");
        synchronized (audio_system_start_stop_lock)
        {
            try
            {
                if (!AudioRecording.stopped)
                {
                    AudioRecording.close();
                    GroupMessageListActivity.NGC_Group_video_play_thread.join();
                    GroupMessageListActivity.NGC_Group_video_play_thread = null;
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
                    GroupMessageListActivity.NGC_Group_video_play_thread.join();
                    GroupMessageListActivity.NGC_Group_video_play_thread = null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "stop_ngc_audio_system:DONE");
    }

    static void stop_audio_system()
    {
        get_caller_method();
        Log.i(TAG, "stop_audio_system:enter");
        synchronized (audio_system_start_stop_lock)
        {
            try
            {
                if (!AudioRecording.stopped)
                {
                    AudioRecording.close();
                    CallingActivity.audio_thread.join();
                    CallingActivity.audio_thread = null;
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
                    CallingActivity.audio_receiver_thread.join();
                    CallingActivity.audio_receiver_thread = null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "stop_audio_system:DONE");
    }

    static public File saveBitmapToFile(File file, final int REQUIRED_SIZE)
    {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                  o.outHeight / scale / 2 >= REQUIRED_SIZE)
            {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new java.io.FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80 , outputStream);

            return file;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static public String trfia_sync_type_to_str(int sync_type)
    {
        if (sync_type == TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_TOXPROXY.value)
        {
            return "TRIFA_SYNC_TYPE_TOXPROXY";
        }
        else if (sync_type == TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NGC_PEERS.value)
        {
            return "TRIFA_SYNC_TYPE_NGC_PEERS";
        }
        else if (sync_type == TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value)
        {
            return "TRIFA_SYNC_TYPE_NONE";
        }
        else
        {
            return "TRIFA_SYNC_TYPE UNKNOWN";
        }
    }
}
