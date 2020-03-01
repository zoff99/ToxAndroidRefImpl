/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2020 Zoff <zoff@zoff.cc>
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

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.encryption.EncryptedDatabase;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.zoffcc.applications.nativeaudio.AudioProcessing;
import com.zoffcc.applications.nativeaudio.NativeAudio;
import com.zoffcc.applications.trifa.CallingActivity.h264_decoder_output_data;
import com.zoffcc.applications.trifa.CallingActivity.h264_encoder_output_data;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Detector;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import info.guardianproject.iocipher.VirtualFileSystem;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import info.guardianproject.netcipher.proxy.StatusCallback;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.webkit.MimeTypeMap.getFileExtensionFromUrl;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.destroy_buffers;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.init_buffers;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.native_aec_lib_ready;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.play_buffer;
import static com.zoffcc.applications.nativeaudio.NativeAudio.n_audio_in_buffer_max_count;
import static com.zoffcc.applications.trifa.AudioReceiver.channels_;
import static com.zoffcc.applications.trifa.AudioReceiver.sampling_rate_;
import static com.zoffcc.applications.trifa.CallingActivity.calling_activity_start_ms;
import static com.zoffcc.applications.trifa.CallingActivity.feed_h264_decoder;
import static com.zoffcc.applications.trifa.CallingActivity.feed_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.fetch_from_h264_decoder;
import static com.zoffcc.applications.trifa.CallingActivity.fetch_from_h264_encoder;
import static com.zoffcc.applications.trifa.CallingActivity.global_sps_pps_nal_unit_bytes;
import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.CallingActivity.on_call_ended_actions;
import static com.zoffcc.applications.trifa.CallingActivity.on_call_started_actions;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames;
import static com.zoffcc.applications.trifa.CallingActivity.send_sps_pps_every_x_frames_current;
import static com.zoffcc.applications.trifa.HelperConference.get_last_conference_message_in_this_conference_within_n_seconds;
import static com.zoffcc.applications.trifa.MessageListActivity.ml_friend_typing;
import static com.zoffcc.applications.trifa.ProfileActivity.update_toxid_display_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND;
import static com.zoffcc.applications.trifa.TRIFAGlobals.DELETE_SQL_AND_VFS_ON_ERROR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_MIN_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_MIN_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HIGHER_GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HIGHER_GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NORMAL_GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NORMAL_GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_HOST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF__DB_secrect_key__user_hash;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_H264;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_VP8;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fos;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fos_normal;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_online_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_messageview;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_tox_self_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.orbot_is_really_running;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_DELAY;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_STATE_CHANGE.TOX_CONFERENCE_STATE_CHANGE_PEER_EXIT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_STATE_CHANGE.TOX_CONFERENCE_STATE_CHANGE_PEER_JOIN;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_STATE_CHANGE.TOX_CONFERENCE_STATE_CHANGE_PEER_NAME_CHANGE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILETRANSFER_SIZE_MSGV2;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_USER_STATUS.TOX_USER_STATUS_AWAY;
import static com.zoffcc.applications.trifa.ToxVars.TOX_USER_STATUS.TOX_USER_STATUS_BUSY;
import static com.zoffcc.applications.trifa.ToxVars.TOX_USER_STATUS.TOX_USER_STATUS_NONE;
import static com.zoffcc.applications.trifa.TrifaToxService.TOX_SERVICE_STARTED;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static com.zoffcc.applications.trifa.TrifaToxService.vfs;

/*

first actually relayed message via ToxProxy

2019-08-28 22:20:43.286148 [D] friend_message_v2_cb:
fn=1 res=1 msg=🍔👍😜👍😜 @%\4äö ubnc Ovid n JB von in BK ni ubvzv8 ctcitccccccizzvvcvvv        u  tiigi gig i g35667u 6 66

 */

@SuppressWarnings("JniMissingFunction")
@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = false; // set "false" for release builds
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true; // set "true" always!
    final static boolean VFS_ENCRYPT = true; // set "true" always!
    final static boolean AEC_DEBUG_DUMP = false; // set "false" for release builds
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

    static TextView mt = null;
    ImageView top_imageview = null;
    static boolean native_lib_loaded = false;
    static boolean native_audio_lib_loaded = false;
    static String app_files_directory = "";
    // static boolean stop_me = false;
    // static Thread ToxServiceThread = null;
    static Semaphore semaphore_videoout_bitmap = new Semaphore(1);
    Handler main_handler = null;
    static Handler main_handler_s = null;
    static Context context_s = null;
    static MainActivity main_activity_s = null;
    static AudioManager audio_manager_s = null;
    static Resources resources = null;
    static DisplayMetrics metrics = null;
    static int AudioMode_old;
    static int RingerMode_old;
    static boolean isSpeakerPhoneOn_old;
    static boolean isWiredHeadsetOn_old;
    static boolean isBluetoothScoOn_old;
    static Notification notification = null;
    static NotificationManager nmn3 = null;
    static NotificationChannel notification_channel_toxservice = null;
    static NotificationChannel notification_channel_newmessage_sound_and_vibrate = null;
    static NotificationChannel notification_channel_newmessage_sound = null;
    static NotificationChannel notification_channel_newmessage_vibrate = null;
    static NotificationChannel notification_channel_newmessage_silent = null;
    static String channelId_toxservice = null;
    static String channelId_newmessage_sound_and_vibrate = null;
    static String channelId_newmessage_sound = null;
    static String channelId_newmessage_vibrate = null;
    static String channelId_newmessage_silent = null;
    static int NOTIFICATION_ID = 293821038;
    static RemoteViews notification_view = null;
    static long[] friends = null;
    static FriendListFragment friend_list_fragment = null;
    static MessageListFragment message_list_fragment = null;
    static MessageListActivity message_list_activity = null;
    static ConferenceMessageListFragment conference_message_list_fragment = null;
    static ConferenceMessageListActivity conference_message_list_activity = null;
    final static String MAIN_DB_NAME = "main.db";
    final static String MAIN_VFS_NAME = "files.db";
    static String SD_CARD_TMP_DIR = "";
    static String SD_CARD_STATIC_DIR = "";
    static String SD_CARD_FILES_EXPORT_DIR = "";
    static String SD_CARD_TMP_DUMMYFILE = null;
    final static int AddFriendActivity_ID = 10001;
    final static int CallingActivity_ID = 10002;
    final static int ProfileActivity_ID = 10003;
    final static int SettingsActivity_ID = 10004;
    final static int AboutpageActivity_ID = 10005;
    final static int MaintenanceActivity_ID = 10006;
    final static int WhiteListFromDozeActivity_ID = 10008;
    final static int Notification_new_message_ID = 10023;
    static long Notification_new_message_last_shown_timestamp = -1;
    final static long Notification_new_message_every_millis = 2000; // ~2 seconds between notifications
    final static long UPDATE_MESSAGES_WHILE_FT_ACTIVE_MILLIS = 30000; // ~30 seconds
    final static long UPDATE_MESSAGES_NORMAL_MILLIS = 500; // ~0.5 seconds
    static String temp_string_a = "";
    static ByteBuffer video_buffer_1 = null;
    static ByteBuffer video_buffer_2 = null;
    final static int audio_in_buffer_max_count = 2; // how many out play buffers? [we are now only using buffer "0" !!]
    public final static int audio_out_buffer_mult = 1;
    static int audio_in_buffer_element_count = 0;
    static ByteBuffer[] audio_buffer_2 = new ByteBuffer[audio_in_buffer_max_count];
    public static long[] audio_buffer_2_ts = new long[n_audio_in_buffer_max_count];
    static ByteBuffer audio_buffer_play = null;
    static int audio_buffer_play_length = 0;
    static int[] audio_buffer_2_read_length = new int[audio_in_buffer_max_count];
    static TrifaToxService tox_service_fg = null;
    static long update_all_messages_global_timestamp = -1;
    final static SimpleDateFormat df_date_time_long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final static SimpleDateFormat df_date_only = new SimpleDateFormat("yyyy-MM-dd");
    static long last_updated_fps = -1;
    final static long update_fps_every_ms = 1500L; // update every 1.5 seconds
    //
    static boolean PREF__UV_reversed = true; // TODO: on older phones this needs to be "false"
    static boolean PREF__notification_sound = true;
    static boolean PREF__notification_vibrate = false;
    static boolean PREF__notification = true;
    static final int MIN_AUDIO_SAMPLINGRATE_OUT = 48000;
    static final int SAMPLE_RATE_FIXED = 48000;
    static int PREF__min_audio_samplingrate_out = SAMPLE_RATE_FIXED;
    static String PREF__DB_secrect_key = "98rj93ßjw3j8j4vj9w8p9eüiü9aci092"; // this is just a dummy, this value is not used!
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!§$%&()=?,.;:-_+";
    static boolean PREF__software_echo_cancel = false;
    static int PREF__higher_video_quality = 1;
    static int PREF__higher_audio_quality = 1;
    static int PREF__udp_enabled = 0; // 0 -> Tox TCP mode, 1 -> Tox UDP mode
    static int PREF__audiosource = 2; // 1 -> VOICE_COMMUNICATION, 2 -> VOICE_RECOGNITION
    static boolean PREF__orbot_enabled = false;
    static boolean PREF__local_discovery_enabled = false;
    static boolean PREF__audiorec_asynctask = true;
    static boolean PREF__cam_recording_hint = false; // careful with this paramter!! it can break camerapreview buffer size!!
    static boolean PREF__set_fps = false;
    static boolean PREF__fps_half = true;
    static boolean PREF__conference_show_system_messages = false;
    static boolean PREF__X_battery_saving_mode = false;
    static int PREF__X_battery_saving_timeout = 15; // in minutes
    static boolean PREF__X_misc_button_enabled = false;
    static String PREF__X_misc_button_msg = "t"; // TODO: hardcoded for now!
    static boolean PREF__U_keep_nospam = false;
    static boolean PREF__use_native_audio_play = true;
    static boolean PREF__use_audio_rec_effects = false;
    static boolean PREF__window_security = false;
    static int PREF__X_eac_delay_ms = 60;
    // from toxav/toxav.h -> valid values: 2.5, 5, 10, 20, 40 or 60 millseconds
    // 120 is also valid!!
    static int FRAME_SIZE_FIXED = 20;
    static int PREF__X_audio_recording_frame_size = FRAME_SIZE_FIXED; // !! 120 seems to work also !!
    static boolean PREF__X_zoom_incoming_video = false;
    static boolean PREF__use_software_aec = true;
    static boolean PREF__allow_screen_off_in_audio_call = true;
    static boolean PREF__use_H264_hw_encoding = true;
    static String PREF__camera_get_preview_format = "YV12"; // "YV12"; // "NV21";
    static boolean PREF__NO_RECYCLE_VIDEO_FRAME_BITMAP = true;
    static int PREF__audio_play_volume_percent = 100;
    static int PREF__video_play_delay_ms = 180;

    static String versionName = "";
    static int versionCode = -1;
    static PackageInfo packageInfo_s = null;
    IntentFilter receiverFilter1 = null;
    IntentFilter receiverFilter2 = null;
    static HeadsetStateReceiver receiver1 = null;
    static HeadsetStateReceiver receiver2 = null;
    static TextView waiting_view = null;
    static ProgressBar waiting_image = null;
    static ViewGroup normal_container = null;
    static ClipboardManager clipboard;
    private ClipData clip;
    static List<Long> selected_messages = new ArrayList<Long>();
    static List<Long> selected_messages_text_only = new ArrayList<Long>();
    static List<Long> selected_messages_incoming_file = new ArrayList<Long>();
    static List<Long> selected_conference_messages = new ArrayList<Long>();
    //
    // YUV conversion -------
    static ScriptIntrinsicYuvToRGB yuvToRgb = null;
    static Allocation alloc_in = null;
    static Allocation alloc_out = null;
    static Bitmap video_frame_image = null;
    static boolean video_frame_image_valid = false;
    static int buffer_size_in_bytes = 0;
    // YUV conversion -------

    // ---- lookup cache ----
    static Map<String, Long> cache_pubkey_fnum = new HashMap<String, Long>();
    static Map<Long, String> cache_fnum_pubkey = new HashMap<Long, String>();
    static Map<String, String> cache_peernum_pubkey = new HashMap<String, String>();
    // static Map<String, String> cache_peername_pubkey = new HashMap<String, String>();
    static Map<String, String> cache_peername_pubkey2 = new HashMap<String, String>();
    // ---- lookup cache ----

    // ---- lookup cache for conference drawer ----
    static Map<String, Long> lookup_peer_listnum_pubkey = new HashMap<String, Long>();
    // ---- lookup cache for conference drawer ----

    // main drawer ----------
    Drawer main_drawer = null;
    AccountHeader main_drawer_header = null;
    ProfileDrawerItem profile_d_item = null;
    // main drawer ----------

    Spinner spinner_own_status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        EmojiManager.install(new IosEmojiProvider());
        // EmojiManager.install(new EmojiOneProvider());
        resources = this.getResources();
        metrics = resources.getDisplayMetrics();
        global_showing_messageview = false;
        super.onCreate(savedInstanceState);
        main_handler = new Handler(getMainLooper());
        main_handler_s = main_handler;
        context_s = this.getBaseContext();
        main_activity_s = this;
        TRIFAGlobals.CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX = (int) dp2px(10);
        TRIFAGlobals.CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX = (int) dp2px(20);

        try
        {
            if (FriendListHolder.progressDialog != null)
            {
                if (FriendListHolder.progressDialog.isShowing())
                {
                    FriendListHolder.progressDialog.dismiss();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (FriendListHolder.progressDialog != null)
            {
                FriendListHolder.progressDialog = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        if (PREF__window_security)
        {
            // prevent screenshots and also dont show the window content in recent activity screen
            initializeScreenshotSecurity(this);
        }

        //        try
        //        {
        //            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            Log.i(TAG, "onCreate:setThreadPriority:EE:" + e.getMessage());
        //        }
        getVersionInfo();

        try
        {
            packageInfo_s = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //        if (canceller == null)
        //        {
        //            canceller = new EchoCanceller();
        //        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //        try
        //        {
        //            ((Toolbar) getSupportActionBar().getCustomView().getParent()).setContentInsetsAbsolute(0, 0);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //        }
        bootstrapping = false;
        waiting_view = (TextView) findViewById(R.id.waiting_view);
        waiting_image = (ProgressBar) findViewById(R.id.waiting_image);
        normal_container = (ViewGroup) findViewById(R.id.normal_container);
        waiting_view.setVisibility(View.GONE);
        waiting_image.setVisibility(View.GONE);
        normal_container.setVisibility(View.VISIBLE);
        SD_CARD_TMP_DIR = getExternalFilesDir(null).getAbsolutePath() + "/tmpdir/";
        SD_CARD_STATIC_DIR = getExternalFilesDir(null).getAbsolutePath() + "/_staticdir/";
        SD_CARD_FILES_EXPORT_DIR = getExternalFilesDir(null).getAbsolutePath() + "/vfs_export/";
        Log.i(TAG, "SD_CARD_FILES_EXPORT_DIR:" + SD_CARD_FILES_EXPORT_DIR);
        SD_CARD_TMP_DUMMYFILE = make_some_static_dummy_file(this.getBaseContext());
        audio_manager_s = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"));
        nmn3 = (NotificationManager) context_s.getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            String channelName;
            // ---------------------
            channelId_newmessage_sound_and_vibrate = "trifa_new_message_sound_and_vibrate";
            channelName = "New Message Sound and Vibrate";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            notification_channel_newmessage_sound_and_vibrate = new NotificationChannel(
                    channelId_newmessage_sound_and_vibrate, channelName, importance);
            notification_channel_newmessage_sound_and_vibrate.setDescription(channelId_newmessage_sound_and_vibrate);
            notification_channel_newmessage_sound_and_vibrate.enableVibration(true);
            nmn3.createNotificationChannel(notification_channel_newmessage_sound_and_vibrate);
            // ---------------------
            channelId_newmessage_sound = "trifa_new_message_sound";
            channelName = "New Message Sound";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            notification_channel_newmessage_sound = new NotificationChannel(channelId_newmessage_sound, channelName,
                                                                            importance);
            notification_channel_newmessage_sound.setDescription(channelId_newmessage_sound);
            notification_channel_newmessage_sound.enableVibration(false);
            nmn3.createNotificationChannel(notification_channel_newmessage_sound);
            // ---------------------
            channelId_newmessage_vibrate = "trifa_new_message_vibrate";
            channelName = "New Message Vibrate";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            notification_channel_newmessage_vibrate = new NotificationChannel(channelId_newmessage_vibrate, channelName,
                                                                              importance);
            notification_channel_newmessage_vibrate.setDescription(channelId_newmessage_vibrate);
            notification_channel_newmessage_vibrate.setSound(null, null);
            notification_channel_newmessage_vibrate.enableVibration(true);
            nmn3.createNotificationChannel(notification_channel_newmessage_vibrate);
            // ---------------------
            channelId_newmessage_silent = "trifa_new_message_silent";
            channelName = "New Message Silent";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            notification_channel_newmessage_silent = new NotificationChannel(channelId_newmessage_silent, channelName,
                                                                             importance);
            notification_channel_newmessage_silent.setDescription(channelId_newmessage_silent);
            notification_channel_newmessage_silent.setSound(null, null);
            notification_channel_newmessage_silent.enableVibration(false);
            nmn3.createNotificationChannel(notification_channel_newmessage_silent);
            // ---------------------
            channelId_toxservice = "trifa_tox_service";
            channelName = "Tox Service";
            importance = NotificationManager.IMPORTANCE_LOW;
            notification_channel_toxservice = new NotificationChannel(channelId_toxservice, channelName, importance);
            notification_channel_toxservice.setDescription(channelId_toxservice);
            notification_channel_toxservice.setSound(null, null);
            notification_channel_toxservice.enableVibration(false);
            nmn3.createNotificationChannel(notification_channel_toxservice);
        }

        // prefs ----------
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        PREF__UV_reversed = settings.getBoolean("video_uv_reversed", true);
        PREF__notification_sound = settings.getBoolean("notifications_new_message_sound", true);
        PREF__notification_vibrate = settings.getBoolean("notifications_new_message_vibrate", false);
        PREF__notification = settings.getBoolean("notifications_new_message", true);
        PREF__software_echo_cancel = settings.getBoolean("software_echo_cancel", false);
        PREF__fps_half = settings.getBoolean("fps_half", true);
        PREF__U_keep_nospam = settings.getBoolean("U_keep_nospam", false);
        PREF__set_fps = settings.getBoolean("set_fps", false);
        PREF__conference_show_system_messages = settings.getBoolean("conference_show_system_messages", false);
        PREF__X_battery_saving_mode = settings.getBoolean("X_battery_saving_mode", false);
        PREF__X_misc_button_enabled = settings.getBoolean("X_misc_button_enabled", false);
        PREF__local_discovery_enabled = settings.getBoolean("local_discovery_enabled", false);
        PREF__use_native_audio_play = settings.getBoolean("X_use_native_audio_play", true);

        try
        {
            if (settings.getString("X_battery_saving_timeout", "15").compareTo("15") == 0)
            {
                PREF__X_battery_saving_timeout = 15;
            }
            else
            {
                PREF__X_battery_saving_timeout = Integer.parseInt(settings.getString("X_battery_saving_timeout", "15"));
                Log.i(TAG, "PREF__X_battery_saving_timeout:1:=" + PREF__X_battery_saving_timeout);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_battery_saving_timeout = 15;
        }

        boolean tmp1 = settings.getBoolean("udp_enabled", false);

        if (tmp1)
        {
            PREF__udp_enabled = 1;
        }
        else
        {
            PREF__udp_enabled = 0;
        }

        try
        {
            PREF__higher_video_quality = Integer.parseInt(settings.getString("higher_video_quality", "1"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__higher_video_quality = 1;
            // -------- convert old boolean value to String, otherwise -> crash --------
            settings.edit().putString("higher_video_quality", "" + PREF__higher_video_quality).commit();
            // -------- convert old boolean value to String, otherwise -> crash --------
        }

        if (PREF__higher_video_quality == 2)
        {
            GLOBAL_VIDEO_BITRATE = HIGHER_GLOBAL_VIDEO_BITRATE;
        }
        else if (PREF__higher_video_quality == 1)
        {
            GLOBAL_VIDEO_BITRATE = NORMAL_GLOBAL_VIDEO_BITRATE;
        }
        else
        {
            GLOBAL_VIDEO_BITRATE = LOWER_GLOBAL_VIDEO_BITRATE;
        }

        try
        {
            PREF__higher_audio_quality = Integer.parseInt(settings.getString("higher_audio_quality", "1"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__higher_audio_quality = 1;
        }

        if (PREF__higher_audio_quality == 2)
        {
            GLOBAL_AUDIO_BITRATE = HIGHER_GLOBAL_AUDIO_BITRATE;
        }
        else if (PREF__higher_audio_quality == 1)
        {
            GLOBAL_AUDIO_BITRATE = NORMAL_GLOBAL_AUDIO_BITRATE;
        }
        else
        {
            GLOBAL_AUDIO_BITRATE = LOWER_GLOBAL_AUDIO_BITRATE;
        }

        // ------- access the clipboard -------
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        // ------- access the clipboard -------
        PREF__orbot_enabled = false;
        boolean PREF__orbot_enabled__temp = settings.getBoolean("orbot_enabled", false);

        if (PREF__orbot_enabled__temp)
        {
            boolean orbot_installed = OrbotHelper.isOrbotInstalled(this);

            if (orbot_installed)
            {
                boolean orbot_running = orbot_is_really_running; // OrbotHelper.isOrbotRunning(this);
                Log.i(TAG, "waiting_for_orbot_info:orbot_running=" + orbot_running);

                if (orbot_running)
                {
                    PREF__orbot_enabled = true;
                    Log.i(TAG, "waiting_for_orbot_info:F1");
                    waiting_for_orbot_info(false);
                    OrbotHelper.get(this).statusTimeout(120 * 1000).
                            addStatusCallback(new StatusCallback()
                            {
                                @Override
                                public void onEnabled(Intent statusIntent)
                                {
                                }

                                @Override
                                public void onStarting()
                                {
                                }

                                @Override
                                public void onStopping()
                                {
                                }

                                @Override
                                public void onDisabled()
                                {
                                    // we got a broadcast with a status of off, so keep waiting
                                }

                                @Override
                                public void onStatusTimeout()
                                {
                                    // throw new RuntimeException("Orbot status request timed out");
                                    Log.i(TAG, "waiting_for_orbot_info:EEO1:" + "Orbot status request timed out");
                                }

                                @Override
                                public void onNotYetInstalled()
                                {
                                }
                            }).
                            init(); // allow 60 seconds to connect to Orbot
                }
                else
                {
                    orbot_is_really_running = false;

                    if (OrbotHelper.requestStartTor(this))
                    {
                        PREF__orbot_enabled = true;
                        Log.i(TAG, "waiting_for_orbot_info:*T2");
                        waiting_for_orbot_info(true);
                    }
                    else
                    {
                        // should never get here
                        Log.i(TAG, "waiting_for_orbot_info:F3");
                        waiting_for_orbot_info(false);
                    }

                    OrbotHelper.get(this).statusTimeout(120 * 1000).
                            addStatusCallback(new StatusCallback()
                            {
                                @Override
                                public void onEnabled(Intent statusIntent)
                                {
                                }

                                @Override
                                public void onStarting()
                                {
                                }

                                @Override
                                public void onStopping()
                                {
                                }

                                @Override
                                public void onDisabled()
                                {
                                    // we got a broadcast with a status of off, so keep waiting
                                }

                                @Override
                                public void onStatusTimeout()
                                {
                                    // throw new RuntimeException("Orbot status request timed out");
                                    Log.i(TAG, "waiting_for_orbot_info:EEO2:" + "Orbot status request timed out");
                                }

                                @Override
                                public void onNotYetInstalled()
                                {
                                }
                            }).
                            init(); // allow 60 seconds to connect to Orbot
                }
            }
            else
            {
                Log.i(TAG, "waiting_for_orbot_info:F4");
                waiting_for_orbot_info(false);
                Intent orbot_get = OrbotHelper.getOrbotInstallIntent(this);

                try
                {
                    startActivity(orbot_get);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            Log.i(TAG, "waiting_for_orbot_info:F5");
            waiting_for_orbot_info(false);
        }

        Log.i(TAG, "PREF__UV_reversed:2=" + PREF__UV_reversed);
        Log.i(TAG, "PREF__notification_sound:2=" + PREF__notification_sound);
        Log.i(TAG, "PREF__notification_vibrate:2=" + PREF__notification_vibrate);

        try
        {
            if (settings.getString("min_audio_samplingrate_out", "8000").compareTo("Auto") == 0)
            {
                PREF__min_audio_samplingrate_out = 8000;
            }
            else
            {
                PREF__min_audio_samplingrate_out = Integer.parseInt(
                        settings.getString("min_audio_samplingrate_out", "" + MIN_AUDIO_SAMPLINGRATE_OUT));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
        }

        // ------- FIXED -------
        PREF__min_audio_samplingrate_out = SAMPLE_RATE_FIXED;
        // ------- FIXED -------

        try
        {
            PREF__allow_screen_off_in_audio_call = settings.getBoolean("allow_screen_off_in_audio_call", true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__allow_screen_off_in_audio_call = true;
        }

        try
        {
            PREF__X_zoom_incoming_video = settings.getBoolean("X_zoom_incoming_video", false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_zoom_incoming_video = false;
        }

        try
        {
            PREF__X_audio_recording_frame_size = Integer.parseInt(
                    settings.getString("X_audio_recording_frame_size", "" + 40));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_audio_recording_frame_size = 40;
        }

        // ------- FIXED -------
        PREF__X_audio_recording_frame_size = FRAME_SIZE_FIXED;
        // ------- FIXED -------

        PREF__camera_get_preview_format = settings.getString("camera_get_preview_format", "YV12");

        // prefs ----------
        PREF__DB_secrect_key = settings.getString("DB_secrect_key", "");

        if (PREF__DB_secrect_key.isEmpty())
        {
            // ok, use hash of user entered password
            PREF__DB_secrect_key = PREF__DB_secrect_key__user_hash;
        }

        mt = (TextView) this.findViewById(R.id.main_maintext);
        mt.setText("...");
        mt.setVisibility(View.VISIBLE);
        // TODO: remake this into something nicer ----------
        top_imageview = (ImageView) this.findViewById(R.id.main_maintopimage);
        top_imageview.setVisibility(View.GONE);

        if (PREF__U_keep_nospam == true)
        {
            top_imageview.setBackgroundColor(Color.TRANSPARENT);
            // top_imageview.setBackgroundColor(Color.parseColor("#C62828"));
            final Drawable d1 = new IconicsDrawable(this).
                    icon(FontAwesome.Icon.faw_exclamation_circle).
                    paddingDp(20).
                    color(getResources().getColor(R.color.md_red_600)).
                    sizeDp(100);
            top_imageview.setImageDrawable(d1);
        }
        else
        {
            top_imageview.setBackgroundColor(Color.TRANSPARENT);
            top_imageview.setImageResource(R.drawable.web_hi_res_512);
        }

        fadeInAndShowImage(top_imageview, 5000);
        fadeOutAndHideImage(mt, 4000);
        // TODO: remake this into something nicer ----------
        // --------- status spinner ---------
        spinner_own_status = (Spinner) findViewById(R.id.spinner_own_status);
        ArrayList<String> own_online_status_string_values = new ArrayList<String>(
                Arrays.asList("Available", "Away", "Busy"));
        ArrayAdapter<String> myAdapter = new OwnStatusSpinnerAdapter(this, R.layout.own_status_spinner_item,
                                                                     own_online_status_string_values);

        if (spinner_own_status != null)
        {
            spinner_own_status.setAdapter(myAdapter);
            spinner_own_status.setSelection(global_tox_self_status);
            spinner_own_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View v, int position, long id)
                {
                    if (is_tox_started)
                    {
                        try
                        {
                            if (id == 0)
                            {
                                // status: available
                                tox_self_set_status(TOX_USER_STATUS_NONE.value);
                                global_tox_self_status = TOX_USER_STATUS_NONE.value;
                            }
                            else if (id == 1)
                            {
                                // status: away
                                tox_self_set_status(TOX_USER_STATUS_AWAY.value);
                                global_tox_self_status = TOX_USER_STATUS_AWAY.value;
                            }
                            else if (id == 2)
                            {
                                // status: busy
                                tox_self_set_status(TOX_USER_STATUS_BUSY.value);
                                global_tox_self_status = TOX_USER_STATUS_BUSY.value;
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView)
                {
                    // your code here
                }
            });
        }

        // --------- status spinner ---------
        // get permission ----------
        MainActivityPermissionsDispatcher.dummyForPermissions001WithPermissionCheck(this);
        // get permission ----------
        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Profile").withIcon(
                GoogleMaterial.Icon.gmd_face);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Settings").withIcon(
                GoogleMaterial.Icon.gmd_settings);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Logout/Login").withIcon(
                GoogleMaterial.Icon.gmd_refresh);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Maintenance").withIcon(
                GoogleMaterial.Icon.gmd_build);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName("About").withIcon(
                GoogleMaterial.Icon.gmd_info);
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName("Exit").withIcon(
                GoogleMaterial.Icon.gmd_exit_to_app);
        final Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_lock).
                color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(100);
        profile_d_item = new ProfileDrawerItem().
                withName("me").
                withIcon(d1);
        // Create the AccountHeader
        main_drawer_header = new AccountHeaderBuilder().
                withSelectionListEnabledForSingleProfile(false).
                withActivity(this).
                withCompactStyle(true).
                addProfiles(profile_d_item).
                withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
                {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                }).build();
        // create the drawer and remember the `Drawer` result object
        main_drawer = new DrawerBuilder().
                withActivity(this).
                withInnerShadow(false).
                withRootView(R.id.drawer_container).
                withShowDrawerOnFirstLaunch(false).
                withActionBarDrawerToggleAnimated(true).
                withActionBarDrawerToggle(true).
                withToolbar(toolbar).
                addDrawerItems(item1, new DividerDrawerItem(), item2, item3, item4, item5, new DividerDrawerItem(),
                               item6).
                withTranslucentStatusBar(false).
                withAccountHeader(main_drawer_header).
                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        Log.i(TAG, "drawer:item=" + position);

                        if (position == 1)
                        {
                            // profile
                            try
                            {
                                if (Callstate.state == 0)
                                {
                                    Log.i(TAG, "start profile activity");
                                    Intent intent = new Intent(context_s, ProfileActivity.class);
                                    startActivityForResult(intent, ProfileActivity_ID);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 3)
                        {
                            // settings
                            try
                            {
                                if (Callstate.state == 0)
                                {
                                    Log.i(TAG, "start settings activity");
                                    Intent intent = new Intent(context_s, SettingsActivity.class);
                                    startActivityForResult(intent, SettingsActivity_ID);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 4)
                        {
                            // logout/login
                            try
                            {
                                if (is_tox_started)
                                {
                                    tox_service_fg.stop_tox_fg();
                                }
                                else
                                {
                                    int PREF__orbot_enabled_to_int = 0;

                                    if (PREF__orbot_enabled)
                                    {
                                        PREF__orbot_enabled_to_int = 1;
                                    }

                                    int PREF__local_discovery_enabled_to_int = 0;

                                    if (PREF__local_discovery_enabled)
                                    {
                                        PREF__local_discovery_enabled_to_int = 1;
                                    }

                                    init(app_files_directory, PREF__udp_enabled, PREF__local_discovery_enabled_to_int,
                                         PREF__orbot_enabled_to_int, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT,
                                         TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(
                                                 TrifaSetPatternActivity.StringToBytes2(PREF__DB_secrect_key))));
                                    Log.i(TAG, "set_all_conferences_inactive:001");
                                    HelperConference.set_all_conferences_inactive();
                                    tox_service_fg.tox_thread_start_fg();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 6)
                        {
                            // About
                            try
                            {
                                Log.i(TAG, "start aboutpage activity");
                                Intent intent = new Intent(context_s, Aboutpage.class);
                                startActivityForResult(intent, AboutpageActivity_ID);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 5)
                        {
                            // Maintenance
                            try
                            {
                                Log.i(TAG, "start Maintenance activity");
                                Intent intent = new Intent(context_s, MaintenanceActivity.class);
                                startActivityForResult(intent, MaintenanceActivity_ID);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            // -- clear Glide cache --
                            // -- clear Glide cache --
                            // clearCache();
                            // -- clear Glide cache --
                            // -- clear Glide cache --
                        }
                        else if (position == 8)
                        {
                            // Exit
                            try
                            {
                                if (is_tox_started)
                                {
                                    tox_service_fg.stop_tox_fg();
                                    tox_service_fg.stop_me(true);
                                }
                                else
                                {
                                    // just exit
                                    tox_service_fg.stop_me(true);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }
                }).build();
        //        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.material_drawer_layout);
        //        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.faw_envelope_open, R.string.faw_envelope_open);
        //
        //        drawer_layout.setDrawerListener(drawerToggle);
        //
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //        getSupportActionBar().setHomeButtonEnabled(true);
        //        drawerToggle.syncState();
        // show hambuger icon -------
        // getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // main_drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        // show back icon -------
        // main_drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------
        // reset calling state
        Callstate.state = 0;
        Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value;
        Callstate.call_first_video_frame_received = -1;
        Callstate.call_first_audio_frame_received = -1;
        VIDEO_FRAME_RATE_OUTGOING = 0;
        last_video_frame_sent = -1;
        VIDEO_FRAME_RATE_INCOMING = 0;
        last_video_frame_received = -1;
        count_video_frame_received = 0;
        count_video_frame_sent = 0;
        Callstate.friend_pubkey = "-1";
        Callstate.audio_speaker = true;
        Callstate.other_audio_enabled = 1;
        Callstate.other_video_enabled = 1;
        Callstate.my_audio_enabled = 1;
        Callstate.my_video_enabled = 1;

        if (native_lib_loaded)
        {
            mt.setText("successfully loaded native library");
        }
        else
        {
            mt.setText("loadLibrary jni-c-toxcore failed!");
        }

        String native_api = getNativeLibAPI();
        mt.setText(mt.getText() + "\n" + native_api);
        mt.setText(mt.getText() + "\n" + "c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." +
                   tox_version_patch());
        mt.setText(mt.getText() + ", " + "jni-c-toxcore:v" + jnictoxcore_version());
        Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version());

        if ((!TOX_SERVICE_STARTED) || (orma == null))
        {
            try
            {
                String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;
                Log.i(TAG, "db:path=" + dbs_path);
                File database_dir = new File(new File(dbs_path).getParent());
                database_dir.mkdirs();
                OrmaDatabase.Builder builder = OrmaDatabase.builder(this);

                if (DB_ENCRYPT)
                {
                    builder = builder.provider(new EncryptedDatabase.Provider(PREF__DB_secrect_key));
                }

                orma = builder.name(dbs_path).
                        readOnMainThread(AccessThreadConstraint.NONE).
                        writeOnMainThread(AccessThreadConstraint.NONE).
                        trace(ORMA_TRACE).
                        build();
                Log.i(TAG, "db:open=OK:path=" + dbs_path);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "db:EE1:" + e.getMessage());
                String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;

                if (DELETE_SQL_AND_VFS_ON_ERROR)
                {
                    try
                    {
                        Log.i(TAG, "db:deleting database:" + dbs_path);
                        new File(dbs_path).delete();
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                        Log.i(TAG, "db:EE3:" + e3.getMessage());
                    }
                }

                Log.i(TAG, "db:path(2)=" + dbs_path);
                OrmaDatabase.Builder builder = OrmaDatabase.builder(this);

                if (DB_ENCRYPT)
                {
                    builder = builder.provider(new EncryptedDatabase.Provider(PREF__DB_secrect_key));
                }

                orma = builder.name(dbs_path).
                        readOnMainThread(AccessThreadConstraint.WARNING).
                        writeOnMainThread(AccessThreadConstraint.WARNING).
                        trace(ORMA_TRACE).
                        build();
                Log.i(TAG, "db:open(2)=OK:path=" + dbs_path);
            }

            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ** // ** // orma.deleteFromMessage().execute();
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
        }

        if ((!TOX_SERVICE_STARTED) || (vfs == null))
        {
            if (VFS_ENCRYPT)
            {
                try
                {
                    String dbFile = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;
                    File database_dir = new File(new File(dbFile).getParent());
                    database_dir.mkdirs();
                    Log.i(TAG, "vfs:path=" + dbFile);
                    vfs = VirtualFileSystem.get();

                    try
                    {
                        if (!vfs.isMounted())
                        {
                            vfs.mount(dbFile, PREF__DB_secrect_key);
                        }
                    }
                    catch (Exception ee)
                    {
                        Log.i(TAG, "vfs:EE1:" + ee.getMessage());
                        ee.printStackTrace();
                        vfs.mount(dbFile, PREF__DB_secrect_key);
                    }

                    Log.i(TAG, "vfs:open(1)=OK:path=" + dbFile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "vfs:EE2:" + e.getMessage());
                    String dbFile = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;

                    if (DELETE_SQL_AND_VFS_ON_ERROR)
                    {
                        try
                        {
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**--------:" + dbFile);
                            new File(dbFile).delete();
                            Log.i(TAG, "vfs:**deleting database**--------:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                            Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        }
                        catch (Exception e3)
                        {
                            e3.printStackTrace();
                            Log.i(TAG, "vfs:EE3:" + e3.getMessage());
                        }
                    }

                    try
                    {
                        Log.i(TAG, "vfs:path=" + dbFile);
                        vfs = VirtualFileSystem.get();
                        vfs.createNewContainer(dbFile, PREF__DB_secrect_key);
                        vfs.mount(PREF__DB_secrect_key);
                        Log.i(TAG, "vfs:open(2)=OK:path=" + dbFile);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        Log.i(TAG, "vfs:EE4:" + e.getMessage());
                    }
                }

                Log.i(TAG, "vfs:encrypted:(1)prefix=" + VFS_PREFIX);
            }
            else
            {
                // VFS not encrypted -------------
                VFS_PREFIX = getExternalFilesDir(null).getAbsolutePath() + "/vfs/";
                Log.i(TAG, "vfs:not_encrypted:(2)prefix=" + VFS_PREFIX);
                // VFS not encrypted -------------
            }
        }

        // cleanup temp dirs --------
        if (!TOX_SERVICE_STARTED)
        {
            cleanup_temp_dirs();
        }

        // cleanup temp dirs --------
        // ---------- DEBUG, just a test ----------
        // ---------- DEBUG, just a test ----------
        // ---------- DEBUG, just a test ----------
        //        if (VFS_ENCRYPT)
        //        {
        //            if (vfs.isMounted())
        //            {
        //                vfs_listFilesAndFilesSubDirectories("/", 0, "");
        //            }
        //        }
        //        // ---------- DEBUG, just a test ----------
        //        // ---------- DEBUG, just a test ----------
        //        // ---------- DEBUG, just a test ----------
        app_files_directory = getFilesDir().getAbsolutePath();
        // --- forground service ---
        // --- forground service ---
        // --- forground service ---
        Intent i = new Intent(this, TrifaToxService.class);

        if (!TOX_SERVICE_STARTED)
        {
            Log.i(TAG, "set_all_conferences_inactive:005");
            HelperConference.set_all_conferences_inactive();
            startService(i);
        }

        if (!TOX_SERVICE_STARTED)
        {
            tox_thread_start();
        }

        // --- forground service ---
        // --- forground service ---
        // --- forground service ---
        receiverFilter1 = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        receiver1 = new HeadsetStateReceiver();
        registerReceiver(receiver1, receiverFilter1);
        receiverFilter2 = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        receiver2 = new HeadsetStateReceiver();
        registerReceiver(receiver2, receiverFilter2);
        MainActivity.set_av_call_status(Callstate.state);
    }

    public static void clearCache_s()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    MainActivity.clearCache(context_s);
                }
                catch (Exception e)
                {
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
                    if (VFS_ENCRYPT)
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
                    vfs_deleteFilesAndFilesSubDirectories_real(SD_CARD_TMP_DIR + "/");
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
        java.io.File directory1 = new java.io.File(directoryName);
        java.io.File[] fList1 = directory1.listFiles();

        for (java.io.File file : fList1)
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
        if (VFS_ENCRYPT)
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

    public void vfs_listFilesAndFilesSubDirectories(String directoryName, int depth, String parent)
    {
        if (VFS_ENCRYPT)
        {
            info.guardianproject.iocipher.File directory1 = new info.guardianproject.iocipher.File(directoryName);
            info.guardianproject.iocipher.File[] fList1 = directory1.listFiles();

            for (info.guardianproject.iocipher.File file : fList1)
            {
                if (file.isFile())
                {
                    // final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // final String human_datetime = df.format(new Date(file.lastModified()));
                    Log.i(TAG, "VFS:f:" + parent + "/" + file.getName() + " bytes=" + file.length());
                }
                else if (file.isDirectory())
                {
                    Log.i(TAG, "VFS:d:" + parent + "/" + file.getName() + "/");
                    vfs_listFilesAndFilesSubDirectories(file.getAbsolutePath(), depth + 1,
                                                        parent + "/" + file.getName());
                }
            }
        }
        else
        {
            java.io.File directory1 = new java.io.File(directoryName);
            java.io.File[] fList1 = directory1.listFiles();

            for (File file : fList1)
            {
                if (file.isFile())
                {
                    // final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // final String human_datetime = df.format(new Date(file.lastModified()));
                    Log.i(TAG, "VFS:f:" + parent + "/" + file.getName() + " bytes=" + file.length());
                }
                else if (file.isDirectory())
                {
                    Log.i(TAG, "VFS:d:" + parent + "/" + file.getName() + "/");
                    vfs_listFilesAndFilesSubDirectories(file.getAbsolutePath(), depth + 1,
                                                        parent + "/" + file.getName());
                }
            }
        }
    }


    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    void dummyForPermissions001()
    {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------

    // this is NOT a crpytographically secure random string generator!!
    // it should only be used to generate status messages or tox user strings to be sort of unique
    static String getRandomString(final int sizeOfRandomString)
    {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);

        for (int i = 0; i < sizeOfRandomString; ++i)
        {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return sb.toString();
    }

    void tox_thread_start()
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    long counter = 0;

                    while (tox_service_fg == null)
                    {
                        counter++;

                        if (counter > 100)
                        {
                            break;
                        }

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
                        // [TODO: move this also to Service.]
                        // HINT: seems to work pretty ok now.
                        if (!is_tox_started)
                        {
                            int PREF__orbot_enabled_to_int = 0;

                            if (PREF__orbot_enabled)
                            {
                                PREF__orbot_enabled_to_int = 1;
                                // need to wait for Orbot to be active ...
                                // max 20 seconds!
                                int max_sleep_iterations = 40;
                                int sleep_iteration = 0;

                                while (!OrbotHelper.isOrbotRunning(context_s))
                                {
                                    // sleep 0.5 seconds
                                    sleep_iteration++;

                                    try
                                    {
                                        Thread.sleep(500);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (sleep_iteration > max_sleep_iterations)
                                    {
                                        // giving up
                                        break;
                                    }
                                }

                                try
                                {
                                    Thread.sleep(1000);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                                // remove "waiting for orbot view"
                                Log.i(TAG, "waiting_for_orbot_info:+F99");
                                orbot_is_really_running = true;
                                waiting_for_orbot_info(false);
                            }

                            int PREF__local_discovery_enabled_to_int = 0;

                            if (PREF__local_discovery_enabled)
                            {
                                PREF__local_discovery_enabled_to_int = 1;
                            }

                            init(app_files_directory, PREF__udp_enabled, PREF__local_discovery_enabled_to_int,
                                 PREF__orbot_enabled_to_int, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT,
                                 TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(
                                         TrifaSetPatternActivity.StringToBytes2(PREF__DB_secrect_key))));
                        }

                        Log.i(TAG, "set_all_conferences_inactive:002");
                        HelperConference.set_all_conferences_inactive();
                        tox_service_fg.tox_thread_start_fg();
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
            Log.i(TAG, "tox_thread_start:EE:" + e.getMessage());
        }
    }

    //    static void stop_tox()
    //    {
    //        try
    //        {
    //            Thread t = new Thread()
    //            {
    //                @Override
    //                public void run()
    //                {
    //                    long counter = 0;
    //                    while (tox_service_fg == null)
    //                    {
    //                        counter++;
    //                        if (counter > 100)
    //                        {
    //                            break;
    //                        }
    //
    //                        try
    //                        {
    //                            Thread.sleep(100);
    //                        }
    //                        catch (Exception e)
    //                        {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //
    //                    try
    //                    {
    //
    //                        tox_service_fg.stop_tox_fg();
    //                    }
    //                    catch (Exception e)
    //                    {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            };
    //            t.start();
    //        }
    //        catch (Exception e)
    //        {
    //            e.printStackTrace();
    //            Log.i(TAG, "stop_tox:EE:" + e.getMessage());
    //        }
    //    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver1);
        unregisterReceiver(receiver2);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // just in case, update own activity pointer!
        main_activity_s = this;
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();
        MainActivity.friend_list_fragment = null;
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();
        // prefs ----------
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        PREF__UV_reversed = settings.getBoolean("video_uv_reversed", true);
        PREF__notification_sound = settings.getBoolean("notifications_new_message_sound", true);
        PREF__notification_vibrate = settings.getBoolean("notifications_new_message_vibrate", true);
        PREF__notification = settings.getBoolean("notifications_new_message", true);
        PREF__software_echo_cancel = settings.getBoolean("software_echo_cancel", false);
        PREF__fps_half = settings.getBoolean("fps_half", true);
        PREF__U_keep_nospam = settings.getBoolean("U_keep_nospam", false);
        PREF__set_fps = settings.getBoolean("set_fps", false);
        PREF__conference_show_system_messages = settings.getBoolean("conference_show_system_messages", false);
        PREF__X_battery_saving_mode = settings.getBoolean("X_battery_saving_mode", false);
        PREF__X_misc_button_enabled = settings.getBoolean("X_misc_button_enabled", false);
        PREF__local_discovery_enabled = settings.getBoolean("local_discovery_enabled", false);
        PREF__use_native_audio_play = settings.getBoolean("X_use_native_audio_play", true);

        try
        {
            if (settings.getString("X_battery_saving_timeout", "15").compareTo("15") == 0)
            {
                PREF__X_battery_saving_timeout = 15;
            }
            else
            {
                PREF__X_battery_saving_timeout = Integer.parseInt(settings.getString("X_battery_saving_timeout", "15"));
                Log.i(TAG, "PREF__X_battery_saving_timeout:2:=" + PREF__X_battery_saving_timeout);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_battery_saving_timeout = 15;
        }

        try
        {
            PREF__X_eac_delay_ms = Integer.parseInt(settings.getString("X_eac_delay_ms", "60"));
        }
        catch (Exception e)
        {
            PREF__X_eac_delay_ms = 60;
            e.printStackTrace();
        }

        set_audio_frame_duration_ms(PREF__X_eac_delay_ms);

        if (PREF__U_keep_nospam == true)
        {
            top_imageview.setBackgroundColor(Color.TRANSPARENT);
            // top_imageview.setBackgroundColor(Color.parseColor("#C62828"));
            final Drawable d1 = new IconicsDrawable(this).
                    icon(FontAwesome.Icon.faw_exclamation_circle).
                    paddingDp(20).
                    color(getResources().getColor(R.color.md_red_600)).
                    sizeDp(100);
            top_imageview.setImageDrawable(d1);
        }
        else
        {
            top_imageview.setBackgroundColor(Color.TRANSPARENT);
            top_imageview.setImageResource(R.drawable.web_hi_res_512);
        }

        boolean tmp1 = settings.getBoolean("udp_enabled", false);

        if (tmp1)
        {
            PREF__udp_enabled = 1;
        }
        else
        {
            PREF__udp_enabled = 0;
        }

        try
        {
            PREF__higher_video_quality = Integer.parseInt(settings.getString("higher_video_quality", "1"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__higher_video_quality = 1;
            // -------- convert old boolean value to String, otherwise -> crash --------
            settings.edit().putString("higher_video_quality", "" + PREF__higher_video_quality).commit();
            // -------- convert old boolean value to String, otherwise -> crash --------
        }

        if (PREF__higher_video_quality == 2)
        {
            GLOBAL_VIDEO_BITRATE = HIGHER_GLOBAL_VIDEO_BITRATE;
        }
        else if (PREF__higher_video_quality == 1)
        {
            GLOBAL_VIDEO_BITRATE = NORMAL_GLOBAL_VIDEO_BITRATE;
        }
        else
        {
            GLOBAL_VIDEO_BITRATE = LOWER_GLOBAL_VIDEO_BITRATE;
        }

        try
        {
            PREF__higher_audio_quality = Integer.parseInt(settings.getString("higher_audio_quality", "1"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__higher_audio_quality = 1;
        }

        if (PREF__higher_audio_quality == 2)
        {
            GLOBAL_AUDIO_BITRATE = HIGHER_GLOBAL_AUDIO_BITRATE;
        }
        else if (PREF__higher_audio_quality == 1)
        {
            GLOBAL_AUDIO_BITRATE = NORMAL_GLOBAL_AUDIO_BITRATE;
        }
        else
        {
            GLOBAL_AUDIO_BITRATE = LOWER_GLOBAL_AUDIO_BITRATE;
        }

        try
        {
            if (settings.getString("min_audio_samplingrate_out", "8000").compareTo("Auto") == 0)
            {
                PREF__min_audio_samplingrate_out = 8000;
            }
            else
            {
                PREF__min_audio_samplingrate_out = Integer.parseInt(
                        settings.getString("min_audio_samplingrate_out", "" + MIN_AUDIO_SAMPLINGRATE_OUT));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
        }

        // ------- FIXED -------
        PREF__min_audio_samplingrate_out = SAMPLE_RATE_FIXED;
        // ------- FIXED -------


        Log.i(TAG, "PREF__UV_reversed:2=" + PREF__UV_reversed);
        Log.i(TAG, "PREF__min_audio_samplingrate_out:2=" + PREF__min_audio_samplingrate_out);

        try
        {
            PREF__allow_screen_off_in_audio_call = settings.getBoolean("allow_screen_off_in_audio_call", true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__allow_screen_off_in_audio_call = true;
        }

        try
        {
            PREF__X_zoom_incoming_video = settings.getBoolean("X_zoom_incoming_video", false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_zoom_incoming_video = false;
        }

        try
        {
            PREF__X_audio_recording_frame_size = Integer.parseInt(
                    settings.getString("X_audio_recording_frame_size", "" + 40));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_audio_recording_frame_size = 40;
        }

        // ------- FIXED -------
        PREF__X_audio_recording_frame_size = FRAME_SIZE_FIXED;
        // ------- FIXED -------


        PREF__camera_get_preview_format = settings.getString("camera_get_preview_format", "YV12");

        // prefs ----------

        try
        {
            profile_d_item.withIcon(get_drawable_from_vfs_image(get_vfs_image_filename_own_avatar()));
            main_drawer_header.updateProfile(profile_d_item);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onResume:EE1:" + e.getMessage());

            try
            {
                final Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_lock).color(
                        getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                profile_d_item.withIcon(d1);
                main_drawer_header.updateProfile(profile_d_item);
            }
            catch (Exception e2)
            {
                Log.i(TAG, "onResume:EE2:" + e2.getMessage());
                e2.printStackTrace();
            }
        }

        spinner_own_status.setSelection(global_tox_self_status);
        // just in case, update own activity pointer!
        main_activity_s = this;

        try
        {
            // ask user to whitelist app from DozeMode/BatteryOptimizations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(this);
                boolean asked_for_whitelist_doze_already = settings2.getBoolean("asked_whitelist_doze", false);

                if (!asked_for_whitelist_doze_already)
                {
                    settings2.edit().putBoolean("asked_whitelist_doze", true).commit();
                    final Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    ResolveInfo resolve_activity = getPackageManager().resolveActivity(intent, 0);

                    if (resolve_activity != null)
                    {
                        AlertDialog ad = new AlertDialog.Builder(this).
                                setNegativeButton("no", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        return;
                                    }
                                }).
                                setPositiveButton("OK, take me there", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        startActivity(intent);
                                    }
                                }).create();
                        ad.setTitle("INFO");
                        ad.setMessage("Please add TRIfA to the exception list for Batteryoptimization!");
                        ad.setCancelable(false);
                        ad.setCanceledOnTouchOutside(false);
                        ad.show();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent i)
    {
        Log.i(TAG, "onNewIntent:i=" + i);
        super.onNewIntent(i);
    }

    @Override
    public void onBackPressed()
    {
        if (main_drawer.isDrawerOpen())
        {
            main_drawer.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }

    // -- this is for incoming video --
    // -- this is for incoming video --
    static void allocate_video_buffer_1(int frame_width_px1, int frame_height_px1, long ystride, long ustride, long vstride)
    {
        try
        {
            //Log.i("semaphore_01","acquire:01");
            semaphore_videoout_bitmap.acquire();
            //Log.i("semaphore_01","acquire:01:OK");
        }
        catch (InterruptedException e)
        {
            //Log.i("semaphore_01","release:01");
            semaphore_videoout_bitmap.release();
            //Log.i("semaphore_01","release:01:OK");
            return;
        }

        if (video_buffer_1 != null)
        {
            video_buffer_1 = null;
        }

        if (video_frame_image != null)
        {
            video_frame_image_valid = false;

            if (!video_frame_image.isRecycled())
            {
                if (!PREF__NO_RECYCLE_VIDEO_FRAME_BITMAP)
                {
                    Log.i(TAG, "video_frame_image.recycle:start");
                    video_frame_image.recycle();
                    Log.i(TAG, "video_frame_image.recycle:end");
                }
            }

            video_frame_image = null;
        }

        /*
         * YUV420 frame with width * height
         *
         * @param y Luminosity plane. Size = MAX(width, abs(ystride)) * height.
         * @param u U chroma plane. Size = MAX(width/2, abs(ustride)) * (height/2).
         * @param v V chroma plane. Size = MAX(width/2, abs(vstride)) * (height/2).
         */
        int y_layer_size = (int) Math.max(frame_width_px1, Math.abs(ystride)) * frame_height_px1;
        int u_layer_size = (int) Math.max((frame_width_px1 / 2), Math.abs(ustride)) * (frame_height_px1 / 2);
        int v_layer_size = (int) Math.max((frame_width_px1 / 2), Math.abs(vstride)) * (frame_height_px1 / 2);
        int frame_width_px = (int) Math.max(frame_width_px1, Math.abs(ystride));
        int frame_height_px = (int) frame_height_px1;
        buffer_size_in_bytes = y_layer_size + v_layer_size + u_layer_size;
        Log.i(TAG, "YUV420 frame w1=" + frame_width_px1 + " h1=" + frame_height_px1 + " bytes=" + buffer_size_in_bytes);
        Log.i(TAG, "YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" + buffer_size_in_bytes);
        Log.i(TAG, "YUV420 frame ystride=" + ystride + " ustride=" + ustride + " vstride=" + vstride);
        video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
        set_JNI_video_buffer(video_buffer_1, frame_width_px, frame_height_px);
        RenderScript rs = RenderScript.create(context_s);
        yuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(frame_width_px).setY(frame_height_px);
        yuvType.setYuvFormat(ImageFormat.YV12);
        alloc_in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(frame_width_px).setY(frame_height_px);
        alloc_out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        video_frame_image = Bitmap.createBitmap(frame_width_px, frame_height_px, Bitmap.Config.ARGB_8888);

        if (video_frame_image == null)
        {
            video_frame_image_valid = false;
            video_buffer_1 = null;
        }
        else
        {
            video_frame_image_valid = true;
        }

        //Log.i("semaphore_01","release:02");
        semaphore_videoout_bitmap.release();
        //Log.i("semaphore_01","relase:02:OK");
    }
    // -- this is for incoming video --
    // -- this is for incoming video --

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public native void init(@NonNull String data_dir, int udp_enabled, int local_discovery_enabled, int orbot_enabled, String orbot_host, long orbot_port, String tox_encrypt_passphrase_hash);

    public native String getNativeLibAPI();

    public static native void update_savedata_file(String tox_encrypt_passphrase_hash);

    public static native String get_my_toxid();

    // *UNUSED* public static native void bootstrap();

    public static native int add_tcp_relay_single(String ip, String key_hex, long port);

    public static native int bootstrap_single(String ip, String key_hex, long port);

    public static native int tox_self_get_connection_status();

    public static native void init_tox_callbacks();

    public static native long tox_iteration_interval();

    public static native long tox_iterate();

    public static native long tox_kill();

    public static native void exit();

    public static native long tox_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, @NonNull String message);

    public static native long tox_version_major();

    public static native long tox_version_minor();

    public static native long tox_version_patch();

    public static native String jnictoxcore_version();

    public static native long tox_max_filename_length();

    public static native long tox_file_id_length();

    public static native long tox_max_message_length();

    public static native long tox_friend_add(@NonNull String toxid_str, @NonNull String message);

    public static native long tox_friend_add_norequest(@NonNull String public_key_str);

    public static native long tox_self_get_friend_list_size();

    public static native void tox_self_set_nospam(long nospam); // this actually needs an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_self_get_nospam(); // this actually returns an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_friend_by_public_key(@NonNull String friend_public_key_string);

    public static native String tox_friend_get_public_key(long friend_number);

    public static native long[] tox_self_get_friend_list();

    public static native int tox_self_set_name(@NonNull String name);

    public static native int tox_self_set_status_message(@NonNull String status_message);

    public static native void tox_self_set_status(int a_TOX_USER_STATUS);

    public static native int tox_self_set_typing(long friend_number, int typing);

    public static native int tox_friend_get_connection_status(long friend_number);

    public static native int tox_friend_delete(long friend_number);

    public static native String tox_self_get_name();

    public static native long tox_self_get_name_size();

    public static native long tox_self_get_status_message_size();

    public static native String tox_self_get_status_message();

    public static native int tox_friend_send_lossless_packet(long friend_number, @NonNull byte[] data, int data_length);

    public static native int tox_file_control(long friend_number, long file_number, int a_TOX_FILE_CONTROL);

    public static native int tox_hash(ByteBuffer hash_buffer, ByteBuffer data_buffer, long data_length);

    public static native int tox_file_seek(long friend_number, long file_number, long position);

    public static native int tox_file_get_file_id(long friend_number, long file_number, ByteBuffer file_id_buffer);

    public static native long tox_file_send(long friend_number, long kind, long file_size, ByteBuffer file_id_buffer, String file_name, long filename_length);

    public static native int tox_file_send_chunk(long friend_number, long file_number, long position, ByteBuffer data_buffer, long data_length);


    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    public static native long tox_messagev2_size(long text_length, long type, long alter_type);

    public static native int tox_messagev2_wrap(long text_length, long type, long alter_type, ByteBuffer message_text_buffer, long ts_sec, long ts_ms, ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native int tox_messagev2_get_message_id(ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native long tox_messagev2_get_ts_sec(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_ts_ms(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_message_text(ByteBuffer raw_message_buffer, long raw_message_len, int is_alter_msg, long alter_type, ByteBuffer message_text_buffer);

    public static native String tox_messagev2_get_sync_message_pubkey(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_sync_message_type(ByteBuffer raw_message_buffer);

    public static native int tox_util_friend_send_msg_receipt_v2(long friend_number, long ts_sec, ByteBuffer msgid_buffer);

    public static native long tox_util_friend_send_message_v2(long friend_number, int type, long ts_sec, String message, long length, ByteBuffer raw_message_back_buffer, ByteBuffer raw_message_back_buffer_length, ByteBuffer msgid_back_buffer);

    public static native int tox_util_friend_resend_message_v2(long friend_number, ByteBuffer raw_message_buffer, long raw_msg_len);
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------


    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------

    public static native long tox_conference_join(long friend_number, ByteBuffer cookie_buffer, long cookie_length);

    public static native String tox_conference_peer_get_public_key(long conference_number, long peer_number);

    public static native long tox_conference_peer_count(long conference_number);

    public static native long tox_conference_offline_peer_count(long conference_number);

    public static native long tox_conference_peer_get_name_size(long conference_number, long peer_number);

    public static native String tox_conference_peer_get_name(long conference_number, long peer_number);

    public static native int tox_conference_peer_number_is_ours(long conference_number, long peer_number);

    public static native long tox_conference_get_title_size(long conference_number);

    public static native String tox_conference_get_title(long conference_number);

    public static native int tox_conference_get_type(long conference_number);

    public static native int tox_conference_send_message(long conference_number, int a_TOX_MESSAGE_TYPE, @NonNull String message);

    public static native int tox_conference_delete(long conference_number);

    public static native long tox_conference_get_chatlist_size();

    public static native long[] tox_conference_get_chatlist();

    public static native int tox_conference_get_id(long conference_number, ByteBuffer cookie_buffer);

    public static native int tox_conference_new();

    public static native int tox_conference_invite(long friend_number, long conference_number);

    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------


    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------
    public static native int toxav_answer(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native long toxav_iteration_interval();

    public static native int toxav_call(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_bit_rate_set(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_call_control(long friendnum, int a_TOXAV_CALL_CONTROL);

    public static native int toxav_video_send_frame_uv_reversed(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame_h264(long friendnum, int frame_width_px, int frame_height_px, long data_len);

    public static native int toxav_option_set(long friendnum, long a_TOXAV_OPTIONS_OPTION, long value);

    public static native void set_av_call_status(int status);

    public static native void set_audio_play_volume_percent(int volume_percent);

    public static native long set_JNI_video_buffer(ByteBuffer buffer, int frame_width_px, int frame_height_px);

    public static native void set_JNI_video_buffer2(ByteBuffer buffer2, int frame_width_px, int frame_height_px);

    public static native void set_JNI_audio_buffer(ByteBuffer audio_buffer);

    // buffer2 is for incoming audio
    public static native void set_JNI_audio_buffer2(ByteBuffer audio_buffer2);

    // for AEC (libfilteraudio)
    public static native void restart_filteraudio(long sampling_rate);

    // for AEC (libfilteraudio)
    public static native void set_audio_frame_duration_ms(int audio_frame_duration_ms);

    // for AEC (libfilteraudio)
    public static native void set_filteraudio_active(int filteraudio_active);

    /**
     * Send an audio frame to a friend.
     * <p>
     * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
     * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
     * For mono audio, this has no meaning, every sample is subsequent. For stereo,
     * this means the expected format is LRLRLR... with samples for left and right
     * alternating.
     *
     * @param friend_number The friend number of the friend to which to send an
     *                      audio frame.
     * @param sample_count  Number of samples in this frame. Valid numbers here are
     *                      ((sample rate) * (audio length) / 1000), where audio length can be
     *                      2.5, 5, 10, 20, 40 or 60 millseconds.
     * @param channels      Number of audio channels. Supported values are 1 and 2.
     * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
     *                      rates are 8000, 12000, 16000, 24000, or 48000.
     */
    public static native int toxav_audio_send_frame(long friend_number, long sample_count, int channels, long sampling_rate);
    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------

    static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled)
    {
        if (Callstate.state != 0)
        {
            // don't accept a new call if we already are in a call
            return;
        }

        Log.i(TAG, "toxav_call:from=" + friend_number + " audio=" + audio_enabled + " video=" + video_enabled);
        final long fn = friend_number;
        final int f_audio_enabled = audio_enabled;
        final int f_video_enabled = video_enabled;
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Callstate.state == 0)
                    {
                        Log.i(TAG, "CALL:start:show activity");

                        if (PREF__use_software_aec)
                        {
                            set_filteraudio_active(1);
                        }
                        else
                        {
                            set_filteraudio_active(0);
                        }

                        Callstate.state = 1;
                        Callstate.accepted_call = 0;
                        Callstate.call_first_video_frame_received = -1;
                        Callstate.call_first_audio_frame_received = -1;
                        Callstate.call_start_timestamp = -1;
                        Callstate.audio_speaker = true;
                        Callstate.other_audio_enabled = 1;
                        Callstate.other_video_enabled = 1;
                        Callstate.my_audio_enabled = 1;
                        Callstate.my_video_enabled = 1;
                        VIDEO_FRAME_RATE_OUTGOING = 0;
                        last_video_frame_sent = -1;
                        count_video_frame_received = 0;
                        count_video_frame_sent = 0;
                        VIDEO_FRAME_RATE_INCOMING = 0;
                        last_video_frame_received = -1;
                        MainActivity.set_av_call_status(Callstate.state);
                        Intent intent = new Intent(context_s.getApplicationContext(), CallingActivity.class);
                        Callstate.friend_pubkey = HelperFriend.tox_friend_get_public_key__wrapper(fn);
                        Callstate.friend_alias_name = HelperFriend.get_friend_name_from_pubkey(Callstate.friend_pubkey);
                        Callstate.other_audio_enabled = f_audio_enabled;
                        Callstate.other_video_enabled = f_video_enabled;
                        Callstate.call_init_timestamp = System.currentTimeMillis();
                        main_activity_s.startActivityForResult(intent, CallingActivity_ID);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
        if (Callstate.other_video_enabled == 0)
        {
            return;
        }

        if (HelperFriend.tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        // Log.i(TAG,
        //      "---> VIDEO_FRAME_RATE_INCOMING w=" + frame_width_px + " h=" + frame_height_px + " ystride=" + ystride);

        //        Log.i(TAG,
        //              "toxav_video_receive_frame:from=" + friend_number + " video width=" + frame_width_px + " video height=" +
        //              frame_height_px + " call_first_video_frame_received=" + Callstate.call_first_video_frame_received);

        if ((Callstate.call_first_video_frame_received == -1) || (Callstate.frame_width_px != frame_width_px) ||
            (Callstate.frame_height_px != frame_height_px) || (Callstate.ystride != ystride) ||
            (Callstate.ustride != ustride) || (Callstate.vstride != vstride))
        {
            //            Log.i(TAG, "toxav_video_receive_frame:from=" + friend_number + " video width=" + frame_width_px +
            //                       " video height=" + frame_height_px);
            Callstate.call_first_video_frame_received = System.currentTimeMillis();
            last_video_frame_received = System.currentTimeMillis();
            count_video_frame_received++;
            // allocate new video buffer on 1 frame
            allocate_video_buffer_1((int) frame_width_px, (int) frame_height_px, ystride, ustride, vstride);
            temp_string_a =
                    "" + (int) ((Callstate.call_first_video_frame_received - Callstate.call_start_timestamp) / 1000) +
                    "s";
            CallingActivity.update_top_text_line(temp_string_a, 3);
            Callstate.frame_width_px = frame_width_px;
            Callstate.frame_height_px = frame_height_px;
            Callstate.ystride = ystride;
            Callstate.ustride = ustride;
            Callstate.vstride = vstride;
        }
        else
        {
            if ((count_video_frame_received > 20) || ((last_video_frame_sent + 2000) < System.currentTimeMillis()))
            {
                VIDEO_FRAME_RATE_INCOMING = (int) ((((float) count_video_frame_received / ((float) (
                        (System.currentTimeMillis() - last_video_frame_received) / 1000.0f))) / 1.0f) + 0.5);
                // Log.i(TAG, "VIDEO_FRAME_RATE_INCOMING=" + VIDEO_FRAME_RATE_INCOMING + " fps");
                update_fps();
                last_video_frame_received = System.currentTimeMillis();
                count_video_frame_received = -1;
            }

            count_video_frame_received++;
        }

        try
        {
            try
            {
                //Log.i("semaphore_01","acquire:05");
                semaphore_videoout_bitmap.acquire();
                //Log.i("semaphore_01","acquire:05:OK");
            }
            catch (InterruptedException e)
            {
                //Log.i("semaphore_01","release:05");
                semaphore_videoout_bitmap.release();
                //Log.i("semaphore_01","release:05:OK");
                return;
            }

            if ((video_frame_image_valid == true) && (video_frame_image != null))
            {
                if (!video_frame_image.isRecycled())
                {
                    alloc_in.copyFrom(video_buffer_1.array());
                    yuvToRgb.setInput(alloc_in);
                    yuvToRgb.forEach(alloc_out);
                    alloc_out.copyTo(video_frame_image);
                }
            }

            //Log.i("semaphore_01","release:06");
            semaphore_videoout_bitmap.release();
            //Log.i("semaphore_01","release:06:OK");
        }
        catch (Exception e)
        {
            e.printStackTrace();

            try
            {
                //Log.i("semaphore_01","release:07");
                semaphore_videoout_bitmap.release();
                //Log.i("semaphore_01","release:07:OK");
            }
            catch (Exception e2)
            {
            }
        }

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try
                    {
                        //Log.i("semaphore_01","acquire:08");
                        semaphore_videoout_bitmap.acquire();
                        //Log.i("semaphore_01","acquire:08:OK");
                    }
                    catch (InterruptedException e)
                    {
                        //Log.i("semaphore_01","release:08");
                        semaphore_videoout_bitmap.release();
                        //Log.i("semaphore_01","release:08:OK");
                        return;
                    }

                    if (video_frame_image_valid == true)
                    {
                        CallingActivity.mContentView.setBitmap(video_frame_image);
                    }

                    //Log.i("semaphore_01","release:09");
                    semaphore_videoout_bitmap.release();
                    //Log.i("semaphore_01","release:09:OK");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //Log.i("semaphore_01","release:10");
                    semaphore_videoout_bitmap.release();
                    //Log.i("semaphore_01","release:10:OK");
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_toxav_callback_video_receive_frame_h264_cb_method(long friend_number, long buf_size)
    {
        // HINT: Disabled. this is now handled by c-toxcore. how nice.
        if (1 == 2)
        {
            Log.i(TAG, "callback_video_receive_frame_h264_cb:arrayOffset=" + video_buffer_1.arrayOffset());
            int off = video_buffer_1.arrayOffset();
            Log.i(TAG, "callback_video_receive_frame_h264_cb:video_buffer_1:" +
                       video_buffer_1.array()[off + ((int) buf_size) - 4] + " " +
                       video_buffer_1.array()[off + ((int) buf_size) - 3] + " " +
                       video_buffer_1.array()[off + ((int) buf_size) - 2] + " " +
                       video_buffer_1.array()[off + ((int) buf_size) - 1]);
            feed_h264_decoder(video_buffer_1.array(), buf_size, off);
            h264_decoder_output_data buf_decoder_out = fetch_from_h264_decoder();

            if (buf_decoder_out.data != null)
            {
                Log.i(TAG, "callback_video_receive_frame_h264_cb:decoded_data:len=" + buf_decoder_out.data.length +
                           " data=" + buf_decoder_out.data);
            }
        }
    }

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        if (HelperFriend.tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG, "toxav_call_state:from=" + friend_number + " state=" + a_TOXAV_FRIEND_CALL_STATE);
        Log.i(TAG, "Callstate.tox_call_state=" + a_TOXAV_FRIEND_CALL_STATE + " old=" + Callstate.tox_call_state);

        if (Callstate.state == 1)
        {
            int old_value = Callstate.tox_call_state;
            Callstate.tox_call_state = a_TOXAV_FRIEND_CALL_STATE;

            if ((a_TOXAV_FRIEND_CALL_STATE & (4 + 8 + 16 + 32)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call starting");
                Callstate.call_start_timestamp = System.currentTimeMillis();
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            CallingActivity.accept_button.setVisibility(View.GONE);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            CallingActivity.caller_avatar_view.setVisibility(View.GONE);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
                Log.i(TAG, "on_call_started_actions:02");
                on_call_started_actions();
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (2)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(1)");
                on_call_ended_actions();
            }
            else if ((old_value > 0) && (a_TOXAV_FRIEND_CALL_STATE == 0))
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(2)");
                on_call_ended_actions();
            }
        }
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
        if (HelperFriend.tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG,
              "toxav_bit_rate_status:from=" + friend_number + " audio_bit_rate=" + audio_bit_rate + " video_bit_rate=" +
              video_bit_rate);

        // TODO: suggested bitrates!!!! ---------------
        if (Callstate.state == 1)
        {
            final long friend_number_ = friend_number;
            long audio_bit_rate2 = audio_bit_rate;
            long video_bit_rate2 = video_bit_rate;

            if (audio_bit_rate2 < GLOBAL_MIN_AUDIO_BITRATE)
            {
                audio_bit_rate2 = GLOBAL_MIN_AUDIO_BITRATE;
            }

            if (video_bit_rate2 < GLOBAL_MIN_VIDEO_BITRATE)
            {
                video_bit_rate2 = GLOBAL_MIN_VIDEO_BITRATE;
            }

            final long audio_bit_rate_ = audio_bit_rate2;
            final long video_bit_rate_ = video_bit_rate2;
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // HINT: disabled (for VP8 this does not work properly anyway)
                        // set only video bitrate according to suggestion from c-toxcore
                        // Callstate.video_bitrate = video_bit_rate_;
                        // toxav_bit_rate_set(friend_number_, Callstate.audio_bitrate, video_bit_rate_);
                        update_bitrates();
                        Log.i(TAG, "toxav_bit_rate_status:CALL:toxav_bit_rate_set");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "toxav_bit_rate_status:CALL:EE:" + e.getMessage());
                    }
                }
            };

            if (main_handler_s != null)
            {
                main_handler_s.post(myRunnable);
            }
        }

        // TODO: suggested bitrates!!!! ---------------
    }

    static void android_toxav_callback_call_comm_cb_method(long friend_number, long a_TOXAV_CALL_COMM_INFO, long comm_number)
    {
        // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_VP8.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:3:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_VP8;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_H264.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:4:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_H264;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_VP8.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_VP8;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:1:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_H264.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_H264;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:2:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE.value)
        {
            Callstate.video_in_bitrate = comm_number;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:66B:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE.value)
        {
            Callstate.video_bitrate = comm_number;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_PLAY_DELAY.value)
        {
            if (comm_number < 0)
            {
                Callstate.play_delay = 0;
            }
            else if (comm_number > 11000)
            {
                Callstate.play_delay = 11000;
            }
            else
            {
                Callstate.play_delay = comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:play_delay=:" + Callstate.play_delay);
            }
        }

        try
        {
            update_bitrates();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_toxav_callback_call_comm_cb_method:EE:" + e.getMessage());
        }
    }

    static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate)
    {
        // Log.i(TAG,
        //      "audio_play:android_toxav_callback_audio_receive_frame_cb_method:" + friend_number + " " + sample_count +
        //      " " + channels + " " + sampling_rate);

        // long timestamp_audio_frame = System.currentTimeMillis();
        if (HelperFriend.tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        if (Callstate.other_audio_enabled == 0)
        {
            if (Callstate.call_first_audio_frame_received == -1)
            {
                sampling_rate_ = sampling_rate;
                Log.i(TAG, "audio_play:read:incoming sampling_rate[0]=" + sampling_rate + " kHz");
                channels_ = channels;
            }

            return;
        }

        if (Callstate.call_first_audio_frame_received == -1)
        {
            Callstate.call_first_audio_frame_received = System.currentTimeMillis();
            sampling_rate_ = sampling_rate;
            Log.i(TAG, "audio_play:read:incoming sampling_rate[1]=" + sampling_rate + " Hz");
            channels_ = channels;
            Log.i(TAG,
                  "audio_play:read:init sample_count=" + sample_count + " channels=" + channels + " sampling_rate=" +
                  sampling_rate);
            temp_string_a =
                    "" + (int) ((Callstate.call_first_audio_frame_received - Callstate.call_start_timestamp) / 1000) +
                    "s";
            CallingActivity.update_top_text_line(temp_string_a, 4);
            // HINT: PCM_16 needs 2 bytes per sample per channel
            AudioReceiver.buffer_size =
                    ((int) ((sample_count * channels) * 2)) * audio_out_buffer_mult;  // TODO: this is really bad
            AudioReceiver.sleep_millis = (int) (((float) sample_count / (float) sampling_rate) * 1000.0f *
                                                0.9f); // TODO: this is bad also
            Log.i(TAG, "audio_play:read:init buffer_size=" + AudioReceiver.buffer_size);
            Log.i(TAG, "audio_play:read:init sleep_millis=" + AudioReceiver.sleep_millis);
            // reset audio in buffers
            int i = 0;

            for (i = 0; i < audio_in_buffer_max_count; i++)
            {
                try
                {
                    if (audio_buffer_2 != null)
                    {
                        if (audio_buffer_2[i] != null)
                        {
                            audio_buffer_2[i].clear();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    audio_buffer_2[i] = null;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                audio_buffer_2[i] = ByteBuffer.allocateDirect(AudioReceiver.buffer_size);
                audio_buffer_2_read_length[i] = 0;
                Log.i(TAG, "audio_play:audio_buffer_2[" + i + "] size=" + AudioReceiver.buffer_size);
            }

            audio_in_buffer_element_count = 0;
            audio_buffer_play = ByteBuffer.allocateDirect(AudioReceiver.buffer_size);
            // always write to buffer[0] in the pipeline !! -----------
            set_JNI_audio_buffer2(audio_buffer_2[0]);

            int frame_size_ = (int) ((sample_count * 1000) / sampling_rate);

            // always write to buffer[0] in the pipeline !! -----------
            Log.i(TAG, "audio_play:audio_buffer_play size=" + AudioReceiver.buffer_size);

            if (native_aec_lib_ready)
            {
                destroy_buffers();
                Log.i(TAG, "audio_play:restart_aec:1:channels_=" + channels_ + " sampling_rate_=" + sampling_rate_);
                init_buffers(frame_size_, channels_, (int) sampling_rate_, 1, SAMPLE_RATE_FIXED);
            }

        }

        // TODO: dirty hack, "make good"
        try
        {
            if (PREF__use_native_audio_play)
            {
                // Log.i(TAG, "audio_play:NativeAudio Play:001");
                if ((NativeAudio.sampling_rate != (int) sampling_rate_) || (NativeAudio.channel_count != channels_))
                {
                    Log.i(TAG, "audio_play:values_changed");
                    NativeAudio.sampling_rate = (int) sampling_rate_;
                    NativeAudio.channel_count = channels_;
                    Log.i(TAG, "audio_play:NativeAudio restart Engine");
                    // TODO: locking? or something like that
                    NativeAudio.restartNativeAudioPlayEngine((int) sampling_rate_, channels_);

                    if (native_aec_lib_ready)
                    {
                        destroy_buffers();
                        int frame_size_ = (int) ((sample_count * 1000) / sampling_rate);
                        Log.i(TAG,
                              "audio_play:restart_aec:2:channels_=" + channels_ + " sampling_rate_=" + sampling_rate_);
                        init_buffers(frame_size_, channels_, (int) sampling_rate_, 1, SAMPLE_RATE_FIXED);
                    }

                }

                audio_buffer_2[0].position(0);
                int incoming_bytes = (int) ((sample_count * channels) * 2);
                // Log.i(TAG, "audio_play:sample_count=" + sample_count + " channels=" + channels + " sampling_rate=" +
                //           sampling_rate);

                // -------------- apply AudioProcessing: AEC -----------------------
                if (native_aec_lib_ready)
                {
                    AudioProcessing.audio_buffer.position(0);
                    audio_buffer_2[0].position(0);
                    // Log.i(TAG, "audio_play:buf_len1=" + audio_buffer_2[0].remaining());
                    // Log.i(TAG, "audio_play:buf_len2=" + AudioProcessing.audio_buffer.remaining());

                    if (AEC_DEBUG_DUMP)
                    {

                        BufferedWriter debug_audio_writer_play_s = null;
                        try
                        {
                            debug_audio_writer_play_s = new BufferedWriter(
                                    new FileWriter("/sdcard/audio_play_s.txt", true));
                            long ts = (System.currentTimeMillis() - calling_activity_start_ms) * 1000;
                            int value = 0;
                            for (int xx = 0; xx < 160; xx++)
                            {
                                value = audio_buffer_2[0].getShort(xx * 2);
                                debug_audio_writer_play_s.write(("" + ts) + "," + value + "\n");
                                ts = ts + 16;
                            }

                            if (debug_audio_writer_play_s != null)
                            {
                                debug_audio_writer_play_s.close();
                            }

                            debug_audio_writer_play_s = new BufferedWriter(
                                    new FileWriter("/sdcard/audio_play_s_ts.txt", true));
                            debug_audio_writer_play_s.write("" + System.currentTimeMillis() + "\n");

                            if (debug_audio_writer_play_s != null)
                            {
                                debug_audio_writer_play_s.close();
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    AudioProcessing.audio_buffer.put(audio_buffer_2[0]);
                    play_buffer();

                    audio_buffer_2[0].position(0);
                }
                // -------------- apply AudioProcessing: AEC -----------------------

                if (NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] < NativeAudio.n_buf_size_in_bytes)
                {
                    int remain_bytes = incoming_bytes - (NativeAudio.n_buf_size_in_bytes -
                                                         NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf]);
                    int remain_start_pos = (incoming_bytes - remain_bytes);
                    NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].position(
                            NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf]);
                    NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].put(audio_buffer_2[0].array(),
                                                                          audio_buffer_2[0].arrayOffset(),
                                                                          Math.min(incoming_bytes,
                                                                                   NativeAudio.n_buf_size_in_bytes -
                                                                                   NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf]));

                    // audio_buffer_2_ts[NativeAudio.n_cur_buf] = timestamp_audio_frame;

                    // Log.i(TAG, "audio_play:NativeAudio:put 1:remain_bytes=" + remain_bytes);

                    if (remain_bytes > 0)
                    {
                        //audio_buffer_2[0].position(remain_start_pos);
                        audio_buffer_2[0].position(0);
                        int res = NativeAudio.PlayPCM16(NativeAudio.n_cur_buf);
                        //                        System.out.println("NativeAudioPlay:001:res=" + res + " bytes=" +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(0) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(1) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(2) + " ");
                        NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] = 0;

                        if (NativeAudio.n_cur_buf + 1 >= n_audio_in_buffer_max_count)
                        {
                            NativeAudio.n_cur_buf = 0;
                        }
                        else
                        {
                            NativeAudio.n_cur_buf++;
                        }

                        NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].position(0);
                        NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].put(audio_buffer_2[0].array(),
                                                                              remain_start_pos, remain_bytes);
                        NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] = remain_bytes;
                    }
                    else if (remain_bytes == 0)
                    {
                        NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] = 0;
                        int res = NativeAudio.PlayPCM16(NativeAudio.n_cur_buf);

                        //                        System.out.println("NativeAudioPlay:002:res=" + res + " bytes=" +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(0) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(1) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(2) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(3) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(4) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(5) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(6) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(7) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(8) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(9) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(10) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(11) + " " +
                        //                                           NativeAudio.n_audio_buffer[NativeAudio.n_cur_buf].get(12) + " ");

                        if (NativeAudio.n_cur_buf + 1 >= n_audio_in_buffer_max_count)
                        {
                            NativeAudio.n_cur_buf = 0;
                        }
                        else
                        {
                            NativeAudio.n_cur_buf++;
                        }

                        // System.out.println("NativeAudioPlay:002:res=" + res);
                    }
                    else
                    {
                        NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] =
                                NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] + incoming_bytes;
                        //                            for (int j = 0; j < NativeAudio.n_buf_size_in_bytes - 1; j++)
                        //                            {
                        //                                NativeAudio.n_audio_buffer_1.position(j);
                        //                                NativeAudio.n_audio_buffer_1.put((byte) (j % 200));
                        //                            }
                        // System.out.println("NativeAudioPlay:002a");
                    }
                }
                else
                {
                    NativeAudio.n_bytes_in_buffer[NativeAudio.n_cur_buf] = 0;

                    if (NativeAudio.n_cur_buf + 1 >= n_audio_in_buffer_max_count)
                    {
                        NativeAudio.n_cur_buf = 0;
                    }
                    else
                    {
                        NativeAudio.n_cur_buf++;
                    }

                    // System.out.println("NativeAudioPlay:002b");
                }
            } // PREF__use_native_audio_play -----
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "audio_play:EE3:" + e.getMessage());
        }
    }

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------


    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_cb_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "self_connection_status:" + a_TOX_CONNECTION);
        global_self_connection_status = a_TOX_CONNECTION;

        if (bootstrapping)
        {
            Log.i(TAG, "self_connection_status:bootstrapping=true");

            // we just went online
            if (a_TOX_CONNECTION != 0)
            {
                Log.i(TAG, "self_connection_status:bootstrapping set to false");
                bootstrapping = false;
                global_self_last_went_online_timestamp = System.currentTimeMillis();
                global_self_last_went_offline_timestamp = -1;
            }
            else
            {
                global_self_last_went_offline_timestamp = System.currentTimeMillis();
            }
        }
        else
        {
            if (a_TOX_CONNECTION != 0)
            {
                global_self_last_went_online_timestamp = System.currentTimeMillis();
                global_self_last_went_offline_timestamp = -1;
            }
            else
            {
                global_self_last_went_offline_timestamp = System.currentTimeMillis();
            }
        }

        // -- notification ------------------
        // -- notification ------------------
        change_notification(a_TOX_CONNECTION, "");
        // -- notification ------------------
        // -- notification ------------------
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        // Log.i(TAG, "friend_alias_name:friend:" + friend_number + " name:" + friend_alias_name);
        FriendList f = HelperFriend.main_get_friend(friend_number);

        // Log.i(TAG, "friend_alias_name:002:" + f);
        if (f != null)
        {
            f.name = friend_name;
            HelperFriend.update_friend_in_db_name(f);
            HelperFriend.update_single_friend_in_friendlist_view(f);
        }
    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
        // Log.i(TAG, "friend_status_message:friend:" + friend_number + " status message:" + status_message);
        FriendList f = HelperFriend.main_get_friend(friend_number);

        if (f != null)
        {
            f.status_message = status_message;
            HelperFriend.update_friend_in_db_status_message(f);
            HelperFriend.update_single_friend_in_friendlist_view(f);
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
        // Log.i(TAG, "friend_status:friend:" + friend_number + " status:" + a_TOX_USER_STATUS);
        FriendList f = HelperFriend.main_get_friend(friend_number);

        if (f != null)
        {
            f.TOX_USER_STATUS = a_TOX_USER_STATUS;
            Log.i(TAG, "friend_status:2:f.TOX_USER_STATUS=" + f.TOX_USER_STATUS);
            HelperFriend.update_friend_in_db_status(f);

            try
            {
                if (message_list_activity != null)
                {
                    Log.i(TAG, "friend_status:002");
                    message_list_activity.set_friend_status_icon();
                    Log.i(TAG, "friend_status:003");
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "friend_status:EE1:" + e.getMessage());
            }

            HelperFriend.update_single_friend_in_friendlist_view(f);
        }
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
        FriendList f = HelperFriend.main_get_friend(friend_number);

        if (f != null)
        {
            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                f.TOX_CONNECTION_real = a_TOX_CONNECTION;
                f.TOX_CONNECTION_on_off_real = get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status_real(f);
            }

            if (f.TOX_CONNECTION != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    // ******** friend just came online ********
                    if (HelperRelay.have_own_relay())
                    {
                        if (!HelperRelay.is_any_relay(f.tox_public_key_string))
                        {
                            HelperRelay.send_relay_pubkey_to_friend(HelperRelay.get_own_relay_pubkey(),
                                                                    f.tox_public_key_string);
                            Log.i(TAG, "send relay pubkey to friend");
                        }
                        else
                        {
                            HelperRelay.send_friend_pubkey_to_relay(HelperRelay.get_own_relay_pubkey(),
                                                                    f.tox_public_key_string);
                            Log.i(TAG, "send friend pubkey to relay");
                            HelperRelay.invite_to_all_conferences_own_relay(f.tox_public_key_string);
                        }
                    }
                }
            }

            if (HelperRelay.is_any_relay(f.tox_public_key_string))
            {
                if (!HelperRelay.is_own_relay(f.tox_public_key_string))
                {
                    FriendList f_real = HelperRelay.get_friend_for_relay(f.tox_public_key_string);

                    if (f_real != null)
                    {
                        update_friend_connection_status_helper(a_TOX_CONNECTION, f_real, true);
                    }
                }
            }

            update_friend_connection_status_helper(a_TOX_CONNECTION, f, false);

            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                f.TOX_CONNECTION_real = a_TOX_CONNECTION;
                f.TOX_CONNECTION_on_off_real = get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status_real(f);
            }
        }
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
        Log.i(TAG, "friend_typing_cb:fn=" + friend_number + " typing=" + typing);
        final long friend_number_ = friend_number;
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (message_list_activity != null)
                    {
                        if (ml_friend_typing != null)
                        {
                            if (message_list_activity.get_current_friendnum() == friend_number_)
                            {
                                if (typing == 1)
                                {
                                    ml_friend_typing.setText("friend is typing ...");
                                }
                                else
                                {
                                    ml_friend_typing.setText("");
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                    Log.i(TAG, "friend_typing_cb:EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_tox_callback_friend_read_receipt_message_v2_cb_method(final long friend_number, long ts_sec, byte[] msg_id)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        msg_id_buffer.put(msg_id, 0, (int) TOX_HASH_LENGTH);
        final String message_id_hash_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                                msg_id_buffer.limit());
        Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string);

        try
        {
            final Message m = orma.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    readEq(false).
                    toList().get(0);

            if (m != null)
            {
                Log.i(TAG, "receipt_message_v2_cb:msgid found");
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (!HelperRelay.is_any_relay(
                                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number)))
                            {
                                // only update if the "read receipt" comes from a friend, but not it's relay!
                                m.raw_msgv2_bytes = "";
                                m.rcvd_timestamp = System.currentTimeMillis();
                                m.read = true;
                                HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                            }

                            m.resend_count = 2;
                            HelperMessage.update_message_in_db_resend_count(m);
                            HelperMessage.update_single_message(m, true);
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
                Log.i(TAG, "receipt_message_v2_cb:msgid *NOT* found");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
        // Log.i(TAG, "friend_read_receipt:friend:" + friend_number + " message_id:" + message_id);
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

        try
        {
            // there can be older messages with same message_id for this friend! so always take the latest one! -------
            final Message m = orma.selectFromMessage().
                    message_idEq(message_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    orderByIdDesc().
                    toList().get(0);
            // there can be older messages with same message_id for this friend! so always take the latest one! -------

            // Log.i(TAG, "friend_read_receipt:m=" + m);
            // Log.i(TAG, "friend_read_receipt:m:message_id=" + m.message_id + " text=" + m.text + " friendpubkey=" + m.tox_friendpubkey + " read=" + m.read + " direction=" + m.direction);

            if (m != null)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            m.rcvd_timestamp = System.currentTimeMillis();
                            m.read = true;
                            HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                            // TODO this updates all messages. should be done nicer and faster!
                            // update_message_view();
                            HelperMessage.update_single_message(m, true);
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
        }
        catch (Exception e)
        {
            Log.i(TAG, "friend_read_receipt:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);
        Log.i(TAG, "friend_request:friend:" + friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2) +
                   " friend request message:" + friend_request_message);
        String friend_public_key__ = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);
        HelperFriend.add_friend_to_system(friend_public_key__, false, null);
    }

    static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        receive_incoming_message(1, friend_number, friend_message, raw_message, raw_message_length, null);
    }

    static void android_tox_callback_friend_lossless_packet_cb_method(long friend_number, byte[] data, long length)
    {
        Log.i(TAG, "friend_lossless_packet_cb:fn=" + friend_number + " len=" + length + " data=" + bytes_to_hex(data));

        if (length > 0)
        {
            if (data[0] == (byte) CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value)
            {
                if (length == (TOX_PUBLIC_KEY_SIZE + 1))
                {
                    Log.i(TAG, "friend_lossless_packet_cb:recevied CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND");
                    String relay_pubkey = bytes_to_hex(data).substring(2);
                    Log.i(TAG, "friend_lossless_packet_cb:recevied pubkey:" + relay_pubkey);
                    HelperFriend.add_friend_to_system(relay_pubkey.toUpperCase(), true,
                                                      HelperFriend.tox_friend_get_public_key__wrapper(friend_number));
                }
            }
        }
    }

    static void android_tox_callback_friend_sync_message_v2_cb_method(long friend_number, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length, byte[] raw_data, long raw_data_length)
    {
        if (!HelperRelay.is_own_relay(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)))
        {
            // sync message only accepted from my own relay
            return;
        }

        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        Log.i(TAG, "friend_sync_message_v2_cb:fn=" + friend_number + " full rawmsg    =" + bytes_to_hex(raw_message));
        Log.i(TAG, "friend_sync_message_v2_cb:fn=" + friend_number + " wrapped rawdata=" + bytes_to_hex(raw_data));
        final ByteBuffer raw_message_buf_wrapped = ByteBuffer.allocateDirect((int) raw_data_length);
        raw_message_buf_wrapped.put(raw_data, 0, (int) raw_data_length);
        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
        raw_message_buf.put(raw_message, 0, (int) raw_message_length);
        long msg_sec = tox_messagev2_get_ts_sec(raw_message_buf);
        long msg_ms = tox_messagev2_get_ts_ms(raw_message_buf);
        Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_sec + " ms=" + msg_ms);
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
        String msg_id_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                 msg_id_buffer.limit());
        Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string);
        String real_sender_as_hex_string = tox_messagev2_get_sync_message_pubkey(raw_message_buf);
        Log.i(TAG, "friend_sync_message_v2_cb:real sender pubkey=" + real_sender_as_hex_string);
        long msgv2_type = tox_messagev2_get_sync_message_type(raw_message_buf);
        Log.i(TAG, "friend_sync_message_v2_cb:msg type=" + ToxVars.TOX_FILE_KIND.value_str((int) msgv2_type));
        ByteBuffer msg_id_buffer_wrapped = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        tox_messagev2_get_message_id(raw_message_buf_wrapped, msg_id_buffer_wrapped);
        String msg_id_as_hex_string_wrapped = bytesToHex(msg_id_buffer_wrapped.array(),
                                                         msg_id_buffer_wrapped.arrayOffset(),
                                                         msg_id_buffer_wrapped.limit());
        Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string_wrapped);

        if (msgv2_type == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_SEND.value)
        {
            long msg_wrapped_sec = tox_messagev2_get_ts_sec(raw_message_buf_wrapped);
            long msg_wrapped_ms = tox_messagev2_get_ts_ms(raw_message_buf_wrapped);
            Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_wrapped_sec + " ms=" + msg_wrapped_ms);
            ByteBuffer msg_text_buffer_wrapped = ByteBuffer.allocateDirect((int) raw_data_length);
            long text_length = tox_messagev2_get_message_text(raw_message_buf_wrapped, raw_data_length, 0, 0,
                                                              msg_text_buffer_wrapped);
            String wrapped_msg_text_as_string = "";

            try
            {
                wrapped_msg_text_as_string = new String(msg_text_buffer_wrapped.array(),
                                                        msg_text_buffer_wrapped.arrayOffset(), (int) text_length,
                                                        "UTF-8");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String msg_text_as_hex_string_wrapped = bytesToHex(msg_text_buffer_wrapped.array(),
                                                               msg_text_buffer_wrapped.arrayOffset(),
                                                               msg_text_buffer_wrapped.limit());
            Log.i(TAG, "friend_sync_message_v2_cb:len=" + text_length + " wrapped msg text str=" +
                       wrapped_msg_text_as_string);
            Log.i(TAG, "friend_sync_message_v2_cb:wrapped msg text hex=" + msg_text_as_hex_string_wrapped);

            try
            {
                if (HelperFriend.tox_friend_by_public_key__wrapper(real_sender_as_hex_string) == -1)
                {
                    // pubkey does NOT belong to a friend. it is probably a conference id
                    // check it here

                    // Log.i(TAG, "friend_sync_message_v2_cb:LL:" + orma.selectFromConferenceDB().toList());
                    String real_conference_id = real_sender_as_hex_string;

                    long conference_num = HelperConference.get_conference_num_from_confid(real_conference_id);
                    Log.i(TAG, "friend_sync_message_v2_cb:conference_num=" + conference_num);
                    if (conference_num > -1)
                    {
                        String real_sender_peer_pubkey = wrapped_msg_text_as_string.substring(0, 64);
                        String real_sender_text = wrapped_msg_text_as_string.substring(64);
                        long real_text_length = (text_length - 64);

                        // add text as conference message
                        long sender_peer_num = HelperConference.get_peernum_from_peer_pubkey(real_conference_id,
                                                                                             real_sender_peer_pubkey);
                        Log.i(TAG, "friend_sync_message_v2_cb:sender_peer_num=" + sender_peer_num);

                        // now check if this is "potentially" a double message, we can not be sure a 100%
                        // since there is no uniqe key for each message
                        ConferenceMessage cm = get_last_conference_message_in_this_conference_within_n_seconds(
                                real_conference_id, 20);

                        // Log.i(TAG, "friend_sync_message_v2_cb:last_cm=" + cm);
                        // Log.i(TAG, "friend_sync_message_v2_cb:real_sender_peer_pubkey=" + real_sender_peer_pubkey);
                        // Log.i(TAG, "friend_sync_message_v2_cb:cm.tox_peerpubkey=" + cm.tox_peerpubkey);
                        // Log.i(TAG, "friend_sync_message_v2_cb:real_sender_text=" + real_sender_text);
                        // Log.i(TAG, "friend_sync_message_v2_cb:cm.text=" + cm.tox_peerpubkey);

                        if (cm != null)
                        {
                            if (cm.tox_peerpubkey.equalsIgnoreCase(real_sender_peer_pubkey))
                            {
                                if (cm.text.equals(real_sender_text))
                                {
                                    // ok it's a "potentially" double message
                                    return;
                                }
                            }
                        }

                        conference_message_add_from_sync(
                                HelperConference.get_conference_num_from_confid(real_conference_id), sender_peer_num,
                                TRIFA_MSG_TYPE_TEXT.value, real_sender_text, real_text_length);
                    }
                    else
                    {
                        return;
                    }
                }
                else
                {
                    receive_incoming_message(2,
                                             HelperFriend.tox_friend_by_public_key__wrapper(real_sender_as_hex_string),
                                             wrapped_msg_text_as_string, raw_data, raw_data_length,
                                             real_sender_as_hex_string);
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
        else if (msgv2_type == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_ANSWER.value)
        {
            // we got an "msg receipt" from the relay
            Log.i(TAG, "friend_sync_message_v2_cb:TOX_FILE_KIND_MESSAGEV2_ANSWER");
            final String message_id_hash_as_hex_string = msg_id_as_hex_string_wrapped;

            try
            {
                Log.i(TAG, "friend_sync_message_v2_cb:message_id_hash_as_hex_string=" + message_id_hash_as_hex_string +
                           " friendpubkey=" + real_sender_as_hex_string);
                final Message m = orma.selectFromMessage().
                        msg_id_hashEq(message_id_hash_as_hex_string).
                        tox_friendpubkeyEq(real_sender_as_hex_string).
                        directionEq(1).
                        readEq(false).
                        toList().get(0);

                if (m != null)
                {
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                long msg_wrapped_sec = tox_messagev2_get_ts_sec(raw_message_buf_wrapped);
                                long msg_wrapped_ms = tox_messagev2_get_ts_ms(raw_message_buf_wrapped);
                                m.raw_msgv2_bytes = "";
                                m.rcvd_timestamp = (msg_wrapped_sec * 1000) + msg_wrapped_ms;
                                m.read = true;
                                HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                                m.resend_count = 2;
                                HelperMessage.update_message_in_db_resend_count(m);
                                HelperMessage.update_single_message(m, true);
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static void conference_message_add_from_sync(long conference_number, long peer_number, int a_TOX_MESSAGE_TYPE, String message, long length)
    {
        Log.i(TAG, "conference_message_add_from_sync:cf_num=" + conference_number + " pnum=" + peer_number + " msg=" +
                   message);
        int res = tox_conference_peer_number_is_ours(conference_number, peer_number);

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            Log.i(TAG, "conference_message_add_from_sync:own peer");
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
            Log.i(TAG, "conference_message_add_from_sync:conf_id=" + conf_id);

        }
        catch (Exception e)
        {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        if (conference_message_list_activity != null)
        {
            Log.i(TAG, "conference_message_add_from_sync:noti_and_badge:002conf:" +
                       conference_message_list_activity.get_current_conf_id() + ":" + conf_id);

            if (conference_message_list_activity.get_current_conf_id().equals(conf_id))
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
        m.tox_peerpubkey = HelperConference.tox_conference_peer_get_public_key__wrapper(conference_number, peer_number);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_peername = null;
        m.conference_identifier = conf_id;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
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

        if (conference_message_list_activity != null)
        {
            if (conference_message_list_activity.get_current_conf_id().equals(conf_id))
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
            Log.i(TAG, "conference_message_add_from_sync:new_msg_id=" + new_msg_id);
        }

        HelperConference.update_single_conference_in_friendlist_view(conf_temp);

        if (do_notification)
        {
            Log.i(TAG, "noti_and_badge:005conf:");
            // start "new" notification
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
                                PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0,
                                                                                        notificationIntent, 0);
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
                                b.setContentText("new Message");
                                Notification notification3 = b.build();
                                nmn3.notify(Notification_new_message_ID, notification3);
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

    // --- incoming message ---
    // --- incoming message ---
    // --- incoming message ---
    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        receive_incoming_message(0, friend_number, friend_message, null, 0, null);
    }
    // --- incoming message ---
    // --- incoming message ---
    // --- incoming message ---

    static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int a_TOX_FILE_CONTROL)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        Log.i(TAG, "file_recv_control:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_CONTROL);

        if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_CANCEL.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_CANCEL");
            HelperFiletransfer.cancel_filetransfer(friend_number, file_number);
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_RESUME.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME");

            try
            {
                long ft_id = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number,
                                                                                               file_number);
                Filetransfer ft_check = orma.selectFromFiletransfer().idEq(ft_id).get(0);

                // -------- DEBUG --------
                //                List<Filetransfer> ft_res = orma.selectFromFiletransfer().
                //                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                //                        orderByIdDesc().
                //                        limit(30).toList();
                //                int ii;
                //                Log.i(TAG, "file_recv_control:SQL:===============================================");
                //                for (ii = 0; ii < ft_res.size(); ii++)
                //                {
                //                    Log.i(TAG, "file_recv_control:SQL:" + ft_res.get(ii));
                //                }
                //                Log.i(TAG, "file_recv_control:SQL:===============================================");
                // -------- DEBUG --------

                if (ft_check.kind == TOX_FILE_KIND_AVATAR.value)
                {
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME::+AVATAR+");
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
                    HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
                    // if outgoing FT set "ft_accepted" to true
                    HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id);
                }
                else
                {
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME::*DATA*");
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:msg_id=" + msg_id);
                    HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_RESUME.value);
                    // if outgoing FT set "ft_accepted" to true
                    HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id);
                    set_message_accepted_from_id(msg_id);

                    // update_all_messages_global(true);
                    try
                    {
                        if (ft_id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_PAUSE.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_PAUSE");

            try
            {
                long ft_id = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number,
                                                                                               file_number);
                long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_PAUSE.value);
                HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_PAUSE.value);

                // update_all_messages_global(true);
                try
                {
                    if (ft_id != -1)
                    {
                        HelperMessage.update_single_message_from_messge_id(msg_id, true);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
    }

    static void android_tox_callback_file_chunk_request_cb_method(long friend_number, long file_number, long position, long length)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

        // Log.i(TAG, "file_chunk_request:" + friend_number + ":" + file_number + ":" + position + ":" + length);

        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_OUTGOING.value).
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0);

            if (ft == null)
            {
                Log.i(TAG, "file_chunk_request:ft=NULL");
                return;
            }

            // Log.i(TAG, "file_chunk_request:ft=" + ft.kind + ":" + ft);

            if (ft.kind == TOX_FILE_KIND_AVATAR.value)
            {
                if (length == 0)
                {
                    // avatar transfer finished -----------
                    // done below // orma.deleteFromFiletransfer().idEq(ft.id);
                    // avatar transfer finished -----------
                    ByteBuffer avatar_chunk = ByteBuffer.allocateDirect(1);
                    int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0);
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
                    // Log.i(TAG, "file_chunk_request:res(2)=" + res);
                    // remove FT from DB
                    HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
                else
                {
                    // TODO: this is really aweful and slow. FIX ME -------------
                    if (VFS_ENCRYPT)
                    {
                        final String fname = get_vfs_image_filename_own_avatar();

                        if (fname != null)
                        {
                            final ByteBuffer avatar_bytes = file_to_bytebuffer(fname, true);

                            if (avatar_bytes != null)
                            {
                                long avatar_chunk_length = length;
                                byte[] bytes_chunck = new byte[(int) avatar_chunk_length];
                                avatar_bytes.position((int) position);
                                avatar_bytes.get(bytes_chunck, 0, (int) avatar_chunk_length);
                                ByteBuffer avatar_chunk = ByteBuffer.allocateDirect((int) avatar_chunk_length);
                                avatar_chunk.put(bytes_chunck);
                                int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk,
                                                              avatar_chunk_length);
                                global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
                                // Log.i(TAG, "file_chunk_request:res(1)=" + res);
                                // int res = tox_hash(hash_bytes, avatar_bytes, avatar_bytes.capacity());
                            }
                        }
                    }
                }
            }
            else // TOX_FILE_KIND_DATA.value
            {
                if (length == 0)
                {
                    Log.i(TAG, "file_chunk_request:file fully sent");
                    // transfer finished -----------
                    long filedb_id = -1;

                    if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                    {
                        // put into "FileDB" table
                        FileDB file_ = new FileDB();
                        file_.kind = ft.kind;
                        file_.direction = ft.direction;
                        file_.tox_public_key_string = ft.tox_public_key_string;
                        file_.path_name = ft.path_name;
                        file_.file_name = ft.file_name;
                        file_.is_in_VFS = false;
                        file_.filesize = ft.filesize;
                        long row_id = orma.insertIntoFileDB(file_);
                        Log.i(TAG, "file_chunk_request:FileDB:row_id=" + row_id);
                        filedb_id = orma.selectFromFileDB().
                                tox_public_key_stringEq(ft.tox_public_key_string).
                                and().file_nameEq(ft.file_name).
                                and().path_nameEq(ft.path_name).
                                and().directionEq(ft.direction).
                                and().filesizeEq(ft.filesize).
                                orderByIdDesc().get(0).id;
                        Log.i(TAG, "file_chunk_request:FileDB:filedb_id=" + filedb_id);
                    }

                    Log.i(TAG, "file_chunk_request:file_READY:001:f.id=" + ft.id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft.id, friend_number);
                    Log.i(TAG, "file_chunk_request:file_READY:001a:msg_id=" + msg_id);
                    HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                                                                                               file_number,
                                                                                               ft.path_name + "/" +
                                                                                               ft.file_name);
                    HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                               TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                                                                                               file_number, -1);

                    try
                    {
                        Log.i(TAG, "file_chunk_request:file_READY:002");

                        if (ft.id != -1)
                        {
                            Log.i(TAG, "file_chunk_request:file_READY:003:f.id=" + ft.id + " msg_id=" + msg_id);
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_chunk_request:file_READY:EE:" + e.getMessage());
                    }

                    // transfer finished -----------
                    ByteBuffer avatar_chunk = ByteBuffer.allocateDirect(1);
                    int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0);
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
                    // Log.i(TAG, "file_chunk_request:res(2)=" + res);
                    // remove FT from DB
                    HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
                else
                {
                    final String fname = new File(ft.path_name + "/" + ft.file_name).getAbsolutePath();
                    // Log.i(TAG, "file_chunk_request:fname=" + fname);
                    long file_chunk_length = length;
                    byte[] bytes_chunck = read_chunk_from_SD_file(fname, position, file_chunk_length);
                    // byte[] bytes_chunck = new byte[(int) file_chunk_length];
                    // avatar_bytes.position((int) position);
                    // avatar_bytes.get(bytes_chunck, 0, (int) file_chunk_length);
                    ByteBuffer file_chunk = ByteBuffer.allocateDirect((int) file_chunk_length);
                    file_chunk.put(bytes_chunck);
                    int res = tox_file_send_chunk(friend_number, file_number, position, file_chunk, file_chunk_length);
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
                    // Log.i(TAG, "file_chunk_request:res(1)=" + res);

                    if (ft.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                    {
                        if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                        {
                            ft.current_position = position;
                            HelperFiletransfer.update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        HelperMessage.update_single_message_from_ftid(ft.id, false);
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                    else
                    {
                        if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                        {
                            ft.current_position = position;
                            HelperFiletransfer.update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        HelperMessage.update_single_message_from_ftid(ft.id, false);
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "file_chunk_request:EE1:" + e.getMessage());
        }
    }

    static void android_tox_callback_file_recv_cb_method(long friend_number, long file_number, int a_TOX_FILE_KIND, long file_size, String filename, long filename_length)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        Log.i(TAG,
              "file_recv:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_KIND + ":" + file_size + ":" +
              filename + ":" + filename_length);

        if (a_TOX_FILE_KIND == TOX_FILE_KIND_AVATAR.value)
        {
            Log.i(TAG, "file_recv:incoming avatar");
            String file_name_avatar = FRIEND_AVATAR_FILENAME;
            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.message_id = -1;
            f.state = TOX_FILE_CONTROL_RESUME.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = file_name_avatar;
            f.filesize = file_size;
            f.current_position = 0;
            long row_id = HelperFiletransfer.insert_into_filetransfer_db(f);
            f.id = row_id;
            // TODO: we just accept incoming avatar, maybe make some checks first?
            tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_RESUME.value);
        }
        else // DATA file ft
        {
            Log.i(TAG, "file_recv:incoming regular file");
            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.state = TOX_FILE_CONTROL_PAUSE.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = filename;
            f.filesize = file_size;
            f.ft_accepted = false;
            f.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            f.current_position = 0;
            long ft_id = HelperFiletransfer.insert_into_filetransfer_db(f);
            f.id = ft_id;
            // add FT message to UI
            Message m = new Message();
            m.tox_friendpubkey = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.filetransfer_id = ft_id;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_PAUSE.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            m.rcvd_timestamp = System.currentTimeMillis();
            m.sent_timestamp = m.rcvd_timestamp;
            m.text = filename + "\n" + file_size + " bytes";
            long new_msg_id = -1;

            if (message_list_activity != null)
            {
                if (message_list_activity.get_current_friendnum() == friend_number)
                {
                    new_msg_id = HelperMessage.insert_into_message_db(m, true);
                    m.id = new_msg_id;
                }
                else
                {
                    new_msg_id = HelperMessage.insert_into_message_db(m, false);
                    m.id = new_msg_id;
                }
            }
            else
            {
                new_msg_id = HelperMessage.insert_into_message_db(m, false);
                m.id = new_msg_id;
            }

            f.message_id = new_msg_id;
            HelperFiletransfer.update_filetransfer_db_full(f);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f2);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }
        }
    }

    static void android_tox_callback_file_recv_chunk_cb_method(long friend_number, long file_number, long position, byte[] data, long length)
    {
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":fn==" + file_number + ":position=" + position + ":length=" + length + ":data len=" + data.length + ":data=" + data);
        // Log.i(TAG, "file_recv_chunk:--START--");
        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":" + file_number + ":" + position + ":" + length);
        Filetransfer f = null;

        try
        {
            f = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_INCOMING.value).
                    file_numberEq(file_number).
                    and().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    get(0);

            // Log.i(TAG, "file_recv_chunk:filesize==" + f.filesize);

            if (position == 0)
            {
                Log.i(TAG, "file_recv_chunk:START-O-F:filesize==" + f.filesize);

                // file start. just to be sure, make directories
                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(
                            f.path_name + "/" + f.file_name);
                    info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(f1.getParent());
                    // Log.i(TAG, "file_recv_chunk:f1=" + f1.getAbsolutePath());
                    // Log.i(TAG, "file_recv_chunk:f2=" + f2.getAbsolutePath());
                    f2.mkdirs();
                }
                else
                {
                    java.io.File f1 = new java.io.File(f.path_name + "/" + f.file_name);
                    java.io.File f2 = new java.io.File(f1.getParent());
                    // Log.i(TAG, "file_recv_chunk:f1=" + f1.getAbsolutePath());
                    // Log.i(TAG, "file_recv_chunk:f2=" + f2.getAbsolutePath());
                    f2.mkdirs();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (length == 0)
        {
            Log.i(TAG, "file_recv_chunk:END-O-F:filesize==" + f.filesize);

            try
            {
                Log.i(TAG, "file_recv_chunk:file fully received");

                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.FileOutputStream fos = null;
                    fos = cache_ft_fos.get(
                            HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                    if (f.fos_open)
                    {
                        try
                        {
                            fos.close();
                        }
                        catch (Exception e3)
                        {
                            Log.i(TAG, "file_recv_chunk:EE3:" + e3.getMessage());
                        }
                    }

                    f.fos_open = false;
                }
                else
                {
                    java.io.FileOutputStream fos = null;
                    fos = cache_ft_fos_normal.get(
                            HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                    if (f.fos_open)
                    {
                        try
                        {
                            fos.close();
                        }
                        catch (Exception e3)
                        {
                            Log.i(TAG, "file_recv_chunk:EE3:" + e3.getMessage());
                        }
                    }

                    f.fos_open = false;
                }

                HelperFiletransfer.update_filetransfer_db_fos_open(f);
                move_tmp_file_to_real_file(f.path_name, f.file_name,
                                           VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/",
                                           f.file_name);
                long filedb_id = -1;

                if (f.kind != TOX_FILE_KIND_AVATAR.value)
                {
                    // put into "FileDB" table
                    FileDB file_ = new FileDB();
                    file_.kind = f.kind;
                    file_.direction = f.direction;
                    file_.tox_public_key_string = f.tox_public_key_string;
                    file_.path_name = VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/";
                    file_.file_name = f.file_name;
                    file_.filesize = f.filesize;
                    long row_id = orma.insertIntoFileDB(file_);
                    Log.i(TAG, "file_recv_chunk:FileDB:row_id=" + row_id);
                    filedb_id = orma.selectFromFileDB().tox_public_key_stringEq(
                            f.tox_public_key_string).and().file_nameEq(f.file_name).orderByIdDesc().get(0).id;
                    Log.i(TAG, "file_recv_chunk:FileDB:filedb_id=" + filedb_id);
                }

                Log.i(TAG, "file_recv_chunk:kind=" + f.kind);

                if (f.kind == TOX_FILE_KIND_AVATAR.value)
                {
                    HelperFriend.set_friend_avatar(HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                                   VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/",
                                                   f.file_name);
                }
                else
                {
                    Log.i(TAG, "file_recv_chunk:file_READY:001:f.id=" + f.id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(f.id, friend_number);
                    Log.i(TAG, "file_recv_chunk:file_READY:001a:msg_id=" + msg_id);
                    HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                                                                                               file_number, VFS_PREFIX +
                                                                                                            VFS_FILE_DIR +
                                                                                                            "/" +
                                                                                                            f.tox_public_key_string +
                                                                                                            "/" +
                                                                                                            f.file_name);
                    HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                               TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                                                                                               file_number, -1);

                    try
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:002");

                        if (f.id != -1)
                        {
                            Log.i(TAG, "file_recv_chunk:file_READY:003:f.id=" + f.id + " msg_id=" + msg_id);
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:EE:" + e.getMessage());
                    }
                }

                // remove FT from DB
                HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "file_recv_chunk:EE2:" + e2.getMessage());
            }
        }
        else // normal chunck recevied ---------- (NOT start, and NOT end)
        {
            try
            {
                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.FileOutputStream fos = null;

                    if (!f.fos_open)
                    {
                        fos = new info.guardianproject.iocipher.FileOutputStream(f.path_name + "/" + f.file_name);
                        // Log.i(TAG, "file_recv_chunk:new fos[1]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                        cache_ft_fos.put(
                                HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                fos);
                        f.fos_open = true;
                        HelperFiletransfer.update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos.get(
                                HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                        if (fos == null)
                        {
                            fos = new info.guardianproject.iocipher.FileOutputStream(f.path_name + "/" + f.file_name);
                            // Log.i(TAG,
                            //       "file_recv_chunk:new fos[2]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos.put(
                                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                    fos);
                            f.fos_open = true;
                            HelperFiletransfer.update_filetransfer_db_fos_open(f);
                        }

                        // Log.i(TAG, "file_recv_chunk:fos=" + fos + " file=" + f.path_name + "/" + f.file_name);
                    }

                    // Log.i(TAG, "file_recv_chunk:fos:" + fos);
                    fos.write(data);
                }
                else
                {
                    java.io.FileOutputStream fos = null;

                    if (!f.fos_open)
                    {
                        fos = new java.io.FileOutputStream(f.path_name + "/" + f.file_name);
                        // Log.i(TAG, "file_recv_chunk:new fos[3]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                        cache_ft_fos_normal.put(
                                HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                fos);
                        f.fos_open = true;
                        HelperFiletransfer.update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos_normal.get(
                                HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                        if (fos == null)
                        {
                            fos = new java.io.FileOutputStream(f.path_name + "/" + f.file_name);
                            // Log.i(TAG,
                            //      "file_recv_chunk:new fos[4]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos_normal.put(
                                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                    fos);
                            f.fos_open = true;
                            HelperFiletransfer.update_filetransfer_db_fos_open(f);
                        }

                        // Log.i(TAG, "file_recv_chunk:fos=" + fos + " file=" + f.path_name + "/" + f.file_name);
                    }

                    // Log.i(TAG, "file_recv_chunk:fos:" + fos);
                    fos.write(data);
                }

                if (f.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                {
                    if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                    {
                        f.current_position = position;
                        // Log.i(TAG, "file_recv_chunk:filesize==:2:" + f.filesize);
                        HelperFiletransfer.update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    HelperMessage.update_single_message_from_ftid(f.id, false);
                                }
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }
                else
                {
                    if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                    {
                        f.current_position = position;
                        // Log.i(TAG, "file_recv_chunk:filesize==:2:" + f.filesize);
                        HelperFiletransfer.update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    HelperMessage.update_single_message_from_ftid(f.id, false);
                                }
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "file_recv_chunk:EE1:" + e.getMessage());
            }
        }

        // Log.i(TAG, "file_recv_chunk:--END--");
    }

    // void test(int i)
    // {
    //    Log.i(TAG, "test:" + i);
    // }

    static void android_tox_log_cb_method(int a_TOX_LOG_LEVEL, String file, long line, String function, String message)
    {
        if (CTOXCORE_NATIVE_LOGGING)
        {
            Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" +
                       line + ":func=" + function + ":msg=" + message);
        }
    }

    static void logger_XX(int level, String text)
    {
        Log.i(TAG, text);
    }
    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------

    static void android_tox_callback_conference_connected_cb_method(long conference_number)
    {
        // invite also my ToxProxy -------------
        if (HelperRelay.have_own_relay())
        {
            tox_conference_invite(HelperFriend.tox_friend_by_public_key__wrapper(HelperRelay.get_own_relay_pubkey()),
                                  conference_number);
        }
        // invite also my ToxProxy -------------

        Log.i(TAG, "conference_connected_cb:cf_num=" + conference_number);
        update_savedata_file_wrapper();
    }

    static void android_tox_callback_conference_invite_cb_method(final long friend_number, final int a_TOX_CONFERENCE_TYPE, final byte[] cookie_buffer, final long cookie_length)
    {
        Log.i(TAG, "conference_invite_cb:fn=" + friend_number + " type=" + a_TOX_CONFERENCE_TYPE + " cookie_length=" +
                   cookie_length + " cookie=" + bytes_to_hex(cookie_buffer));
        //try
        //{
        //Thread t = new Thread()
        //{
        // @Override
        //public void run()
        //{
        ByteBuffer cookie_buf2 = ByteBuffer.allocateDirect((int) cookie_length);
        cookie_buf2.put(cookie_buffer);
        Log.i(TAG, "conference_invite_cb:bytebuffer offset=" + cookie_buf2.arrayOffset());
        long conference_num = tox_conference_join(friend_number, cookie_buf2, cookie_length);
        Log.i(TAG, "conference_invite_cb:tox_conference_join res=" + conference_num);
        // strip first 3 bytes of cookie to get the conference_id.
        // this is aweful and hardcoded
        String conference_identifier = bytes_to_hex(
                Arrays.copyOfRange(cookie_buffer, 3, (int) (3 + CONFERENCE_ID_LENGTH)));
        Log.i(TAG, "conference_invite_cb:conferenc ID=" + conference_identifier);

        // invite also my ToxProxy -------------
        if (HelperRelay.have_own_relay())
        {
            tox_conference_invite(HelperFriend.tox_friend_by_public_key__wrapper(HelperRelay.get_own_relay_pubkey()),
                                  conference_num);
        }
        // invite also my ToxProxy -------------


        HelperConference.add_conference_wrapper(friend_number, conference_num, conference_identifier,
                                                a_TOX_CONFERENCE_TYPE, true);
    }

    static void android_tox_callback_conference_message_cb_method(long conference_number, long peer_number, int a_TOX_MESSAGE_TYPE, String message, long length)
    {
        // Log.i(TAG, "conference_message_cb:cf_num=" + conference_number + " pnum=" + peer_number + " msg=" + message);
        int res = tox_conference_peer_number_is_ours(conference_number, peer_number);

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        if (conference_message_list_activity != null)
        {
            Log.i(TAG,
                  "noti_and_badge:002conf:" + conference_message_list_activity.get_current_conf_id() + ":" + conf_id);

            if (conference_message_list_activity.get_current_conf_id().equals(conf_id))
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
        m.tox_peerpubkey = HelperConference.tox_conference_peer_get_public_key__wrapper(conference_number, peer_number);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_peername = null;
        m.conference_identifier = conf_id;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
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

        if (conference_message_list_activity != null)
        {
            if (conference_message_list_activity.get_current_conf_id().equals(conf_id))
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
            Log.i(TAG, "conference_message_cb:new_msg_id=" + new_msg_id);
        }

        HelperConference.update_single_conference_in_friendlist_view(conf_temp);

        if (do_notification)
        {
            Log.i(TAG, "noti_and_badge:005conf:");
            // start "new" notification
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
                                PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0,
                                                                                        notificationIntent, 0);
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
                                b.setContentText("new Message");
                                Notification notification3 = b.build();
                                nmn3.notify(Notification_new_message_ID, notification3);
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

    static void android_tox_callback_conference_title_cb_method(long conference_number, long peer_number, String title, long title_length)
    {
        Log.i(TAG, "conference_title_cb:" + "confnum=" + conference_number + " peernum=" + peer_number + " new_title=" +
                   title + " title_length=" + title_length);

        try
        {
            ConferenceDB conf_temp2 = null;

            try
            {
                try
                {
                    // TODO: cache me!!
                    conf_temp2 = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                            and().
                            conference_activeEq(true).
                            get(0);

                    if (conf_temp2 != null)
                    {
                        // update it in the Database
                        orma.updateConferenceDB().
                                conference_identifierEq(conf_temp2.conference_identifier).
                                name(title).execute();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "get_conference_title_from_confid:EE:3:" + e2.getMessage());
            }

            HelperConference.update_single_conference_in_friendlist_view(conf_temp2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_tox_callback_conference_title_cb_method:EE1:" + e.getMessage());
        }
    }

    static void android_tox_callback_conference_peer_name_cb_method(long conference_number, long peer_number, String name, long name_length)
    {
        Log.i(TAG, "conference_peer_name_cb:cf_num=" + conference_number);

        try
        {
            ConferenceDB conf_temp = null;

            try
            {
                // TODO: cache me!!
                conf_temp = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                        and().
                        conference_activeEq(true).
                        get(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (conf_temp != null)
            {
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // workaround to fix a bug, where messages would appear to be from the wrong user in a conference
                // because toxcore changes all "peer numbers" when somebody leaves the confrence
                // but this can happen very often. so for now, clear cache every time
                cache_peernum_pubkey.clear();
                cache_peername_pubkey2.clear();
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                ConferenceMessage m = new ConferenceMessage();
                m.is_new = false;
                // m.tox_friendnum = friend_number;
                m.tox_peerpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                m.direction = 0; // msg received
                m.TOX_MESSAGE_TYPE = 0;
                m.read = false;
                m.tox_peername = "* System Message *";
                m.conference_identifier = conf_temp.conference_identifier;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.rcvd_timestamp = System.currentTimeMillis();
                String peer_name_temp = "Unknown";
                String peer_name_temp2 = null;

                try
                {
                    // don't use the wrapper here!
                    String peer_pubkey_temp = tox_conference_peer_get_public_key(conference_number, peer_number);
                    // Log.i(TAG, "namelist_change_cb:003:peer_pubkey_temp=" + peer_pubkey_temp);
                    // don't use the wrapper here!
                    peer_name_temp = name;
                    // Log.i(TAG, "namelist_change_cb:004:peer_name_temp=" + peer_name_temp);

                    try
                    {
                        if ((peer_name_temp != null) && (!peer_name_temp.equals("")))
                        {
                            peer_name_temp2 = peer_name_temp;
                        }
                    }
                    catch (Exception e5)
                    {
                        e5.printStackTrace();
                    }

                    if (peer_name_temp == null)
                    {
                        peer_name_temp = "Unknown";
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "namelist_change_cb:005:EE1:" + e.getMessage());
                }

                try
                {
                    if (conference_message_list_activity != null)
                    {
                        Log.i(TAG, "namelist_change_cb:INFO:" + " 001");

                        if (conference_message_list_activity.get_current_conf_id().equals(
                                conf_temp.conference_identifier))
                        {
                            String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number,
                                                                                          peer_number);
                            Log.i(TAG,
                                  "namelist_change_cb:INFO:" + " 002 " + conference_number + ":" + peer_number + ":" +
                                  peer_pubkey_temp2);
                            conference_message_list_activity.add_group_user(peer_pubkey_temp2, peer_number,
                                                                            peer_name_temp2);
                            Log.i(TAG, "namelist_change_cb:INFO:" + " 003");
                        }
                    }
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                }

                try
                {
                    // TODO: this is just doing nothing useful! check me! FIX ME!
                    ConferencePeerCacheDB cpcdb = new ConferencePeerCacheDB();
                    cpcdb.conference_identifier = conf_temp.conference_identifier;
                    String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number, peer_number);
                    cpcdb.peer_pubkey = peer_pubkey_temp2;
                    cpcdb.peer_name = peer_name_temp2;
                    cpcdb.last_update_timestamp = System.currentTimeMillis();
                    orma.insertIntoConferencePeerCacheDB(cpcdb);
                    // Log.i(TAG, "namelist_change_cb:insertIntoConferencePeerCacheDB:" + cpcdb);
                }
                catch (Exception e4)
                {
                    e4.printStackTrace();
                }

                m.text = "" + peer_name_temp + " changed name.";
                Log.i(TAG, "namelist_change_cb:INFO:" + peer_name_temp + " changed name.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void android_tox_callback_conference_peer_list_changed_cb_method(long conference_number)
    {
        // TODO: write me ...
        // update all peers in this conference
        Log.i(TAG, "conference_peer_list_changed_cb:cf_num=" + conference_number);

        try
        {
            ConferenceDB conf_temp = null;

            try
            {
                // TODO: cache me!!
                conf_temp = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                        and().
                        conference_activeEq(true).
                        get(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (conf_temp != null)
            {
                try
                {
                    if (conference_message_list_activity != null)
                    {
                        Log.i(TAG, "peer_list_changed_cb:INFO:" + " 001.1");

                        if (conference_message_list_activity.get_current_conf_id().equals(
                                conf_temp.conference_identifier))
                        {
                            Log.i(TAG, "peer_list_changed_cb:INFO:" + " 002.1 " + conference_number);
                            conference_message_list_activity.update_group_all_users();
                        }
                    }
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        update_savedata_file_wrapper();
    }

    // ------------------------
    // this is an old toxcore 0.1.x callback
    // not called anymore!!
    static void android_tox_callback_conference_namelist_change_cb_method(long conference_number, long peer_number, int a_TOX_CONFERENCE_STATE_CHANGE)
    {
        // Log.i(TAG, "namelist_change_cb:" + "confnum=" + conference_number + " peernum=" + peer_number + " state=" + a_TOX_CONFERENCE_STATE_CHANGE);
        // TODO: update peer status
        Log.i(TAG, "conference_namelist_change_cb:cf_num=" + conference_number);

        try
        {
            ConferenceDB conf_temp = null;
            String pubkey_save = "";

            try
            {
                // TODO: cache me!!
                conf_temp = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                        and().
                        conference_activeEq(true).
                        get(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (conf_temp != null)
            {
                // a_TOX_CONFERENCE_STATE_CHANGE:
                // 0 -> join
                // 1 -> leave
                // 2 -> name change
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // workaround to fix a bug, where messages would appear to be from the wrong user in a conference
                // because toxcore changes all "peer numbers" when somebody leaves the confrence
                // but this can happen very often. so for now, clear cache every time
                cache_peernum_pubkey.clear();
                cache_peername_pubkey2.clear();
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                // --------------- BAD !! ---------------
                ConferenceMessage m = new ConferenceMessage();
                m.is_new = false;
                // m.tox_friendnum = friend_number;
                m.tox_peerpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                m.direction = 0; // msg received
                m.TOX_MESSAGE_TYPE = 0;
                m.read = false;
                m.tox_peername = "* System Message *";
                m.conference_identifier = conf_temp.conference_identifier;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.rcvd_timestamp = System.currentTimeMillis();
                String peer_name_temp = "Unknown";
                String peer_name_temp2 = null;

                try
                {
                    // don't use the wrapper here!
                    String peer_pubkey_temp = tox_conference_peer_get_public_key(conference_number, peer_number);
                    Log.i(TAG, "namelist_change_cb:003:peer_pubkey_temp=" + peer_pubkey_temp);
                    // don't use the wrapper here!
                    peer_name_temp = tox_conference_peer_get_name(conference_number, peer_number);
                    Log.i(TAG, "namelist_change_cb:004:peer_name_temp=" + peer_name_temp);

                    try
                    {
                        if ((peer_name_temp != null) && (!peer_name_temp.equals("")))
                        {
                            peer_name_temp2 = peer_name_temp;
                        }
                    }
                    catch (Exception e5)
                    {
                        e5.printStackTrace();
                    }

                    if (peer_name_temp == null)
                    {
                        peer_name_temp = "Unknown";
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "namelist_change_cb:005:EE1:" + e.getMessage());
                }

                if (a_TOX_CONFERENCE_STATE_CHANGE == TOX_CONFERENCE_STATE_CHANGE_PEER_JOIN.value)
                {
                    m.text = "" + peer_name_temp + " joined.";
                    Log.i(TAG, "namelist_change_cb:INFO:" + peer_name_temp + " joined.");
                    // TODO: here it's always "Tox User" !! bad!

                    try
                    {
                        if (conference_message_list_activity != null)
                        {
                            Log.i(TAG, "namelist_change_cb:INFO:" + " 001.1");

                            if (conference_message_list_activity.get_current_conf_id().equals(
                                    conf_temp.conference_identifier))
                            {
                                String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number,
                                                                                              peer_number);
                                Log.i(TAG,
                                      "namelist_change_cb:INFO:" + " 002.1 " + conference_number + ":" + peer_number +
                                      ":" + peer_pubkey_temp2);
                                conference_message_list_activity.add_group_user(peer_pubkey_temp2, peer_number, null);
                                // TODO: because here the name is always "Tox User" !!
                            }
                        }
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    return;
                }
                else if (a_TOX_CONFERENCE_STATE_CHANGE == TOX_CONFERENCE_STATE_CHANGE_PEER_NAME_CHANGE.value)
                {
                    try
                    {
                        if (conference_message_list_activity != null)
                        {
                            Log.i(TAG, "namelist_change_cb:INFO:" + " 001");

                            if (conference_message_list_activity.get_current_conf_id().equals(
                                    conf_temp.conference_identifier))
                            {
                                String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number,
                                                                                              peer_number);
                                Log.i(TAG,
                                      "namelist_change_cb:INFO:" + " 002 " + conference_number + ":" + peer_number +
                                      ":" + peer_pubkey_temp2);
                                conference_message_list_activity.add_group_user(peer_pubkey_temp2, peer_number,
                                                                                peer_name_temp2);
                                Log.i(TAG, "namelist_change_cb:INFO:" + " 003");
                            }
                        }
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    try
                    {
                        ConferencePeerCacheDB cpcdb = new ConferencePeerCacheDB();
                        cpcdb.conference_identifier = conf_temp.conference_identifier;
                        String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number, peer_number);
                        cpcdb.peer_pubkey = peer_pubkey_temp2;
                        cpcdb.peer_name = peer_name_temp2;
                        cpcdb.last_update_timestamp = System.currentTimeMillis();
                        orma.insertIntoConferencePeerCacheDB(cpcdb);
                        Log.i(TAG, "namelist_change_cb:insertIntoConferencePeerCacheDB:" + cpcdb);
                    }
                    catch (Exception e4)
                    {
                        e4.printStackTrace();
                    }

                    m.text = "" + peer_name_temp + " changed name or joined.";
                    Log.i(TAG, "namelist_change_cb:INFO:" + peer_name_temp + " changed name or joined.");
                    // HINT: this happend also after each peer joins
                    // return;
                }
                else if (a_TOX_CONFERENCE_STATE_CHANGE == TOX_CONFERENCE_STATE_CHANGE_PEER_EXIT.value)
                {
                    try
                    {
                        if (conference_message_list_activity != null)
                        {
                            if (conference_message_list_activity.get_current_conf_id().equals(
                                    conf_temp.conference_identifier))
                            {
                                String peer_pubkey_temp2 = tox_conference_peer_get_public_key(conference_number,
                                                                                              peer_number);
                                conference_message_list_activity.remove_group_user(peer_pubkey_temp2);
                            }
                        }
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    try
                    {
                        String name_from_cacheDB = orma.selectFromConferencePeerCacheDB().
                                conference_identifierEq(conf_temp.conference_identifier).
                                peer_pubkeyEq(tox_conference_peer_get_public_key(conference_number, peer_number)).
                                orderByLast_update_timestampAsc().get(0).peer_name;

                        if (name_from_cacheDB != null)
                        {
                            if (name_from_cacheDB.length() > 0)
                            {
                                peer_name_temp = name_from_cacheDB;
                                Log.i(TAG, "namelist_change_cb:peer_name_temp from DB=" + name_from_cacheDB);
                            }
                        }
                    }
                    catch (Exception e4)
                    {
                        e4.printStackTrace();
                    }

                    m.text = "" + peer_name_temp + " left.";
                    Log.i(TAG, "namelist_change_cb:INFO:" + peer_name_temp + " left.");
                    // return;
                }
                else
                {
                    // unknown status
                    return;
                }

                if (conference_message_list_activity != null)
                {
                    if (conference_message_list_activity.get_current_conf_id().equals(conf_temp.conference_identifier))
                    {
                        Log.i(TAG, "namelist_change_cb:009");
                        HelperConference.insert_into_conference_message_db_system_message(m, true);
                    }
                    else
                    {
                        Log.i(TAG, "namelist_change_cb:010");
                        HelperConference.insert_into_conference_message_db_system_message(m, false);
                    }
                }
                else
                {
                    long new_msg_id = HelperConference.insert_into_conference_message_db_system_message(m, false);
                    Log.i(TAG, "conference_message_cb:new_msg_id=" + new_msg_id);
                }

                // update conferenc item -------
                HelperConference.update_single_conference_in_friendlist_view(conf_temp);
                // update conferenc item -------

                if (a_TOX_CONFERENCE_STATE_CHANGE == TOX_CONFERENCE_STATE_CHANGE_PEER_EXIT.value)
                {
                    try
                    {
                        orma.deleteFromConferencePeerCacheDB().
                                conference_identifierEq(conf_temp.conference_identifier).
                                peer_pubkeyEq(pubkey_save).execute();
                        Log.i(TAG, "namelist_change_cb:deleteFromConferencePeerCacheDB");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        // TODO: make better? clean up -> if more than 300 entries, delete oldest 50 entries
                        long count_cache_entries = (long) orma.selectFromConferencePeerCacheDB().count();

                        if (count_cache_entries > 300)
                        {
                            Log.i(TAG,
                                  "namelist_change_cb:selectFromConferencePeerCacheDB().count=" + count_cache_entries);

                            for (ConferencePeerCacheDB entry : orma.selectFromConferencePeerCacheDB().offset(0).limit(
                                    50))
                            {
                                Log.i(TAG, "namelist_change_cb:delete peer cache entry ID=" + entry.id);
                                orma.deleteFromConferencePeerCacheDB().idEq(entry.id).execute();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_tox_callback_conference_title_cb_method:EE1:" + e.getMessage());
        }
    }
    // ------------------------
    // this is an old toxcore 0.1.x callback
    // not called anymore!!

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------


    /*
     * this is used to load the native library on
     * application startup. The library has already been unpacked at
     * installation time by the package manager.
     */
    static
    {
        try
        {
            System.loadLibrary("jni-c-toxcore");
            native_lib_loaded = true;
            Log.i(TAG, "successfully loaded jni-c-toxcore library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
        }

        try
        {
            System.loadLibrary("native-audio-jni");
            native_audio_lib_loaded = true;
            Log.i(TAG, "successfully loaded native-audio-jni library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_audio_lib_loaded = false;
            Log.i(TAG, "loadLibrary native-audio-jni failed!");
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

                    while (tox_service_fg == null)
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
                        tox_service_fg.change_notification_fg(a_TOXCONNECTION_f, message);
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

    public static void update_friend_connection_status_helper(int a_TOX_CONNECTION, FriendList f, boolean from_relay)
    {
        final long friend_number_ = HelperFriend.tox_friend_by_public_key__wrapper(f.tox_public_key_string);
        boolean went_online = false;

        if (f.TOX_CONNECTION != a_TOX_CONNECTION)
        {
            if ((!from_relay) && (!HelperRelay.is_any_relay(f.tox_public_key_string)))
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if (VFS_ENCRYPT)
                                {
                                    String fname = get_vfs_image_filename_own_avatar();

                                    if (fname != null)
                                    {
                                        ByteBuffer avatar_bytes = file_to_bytebuffer(fname, true);

                                        if (avatar_bytes != null)
                                        {
                                            // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:avatar_bytes=" + bytes_to_hex(avatar_bytes));
                                            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
                                            int res = tox_hash(hash_bytes, avatar_bytes, avatar_bytes.capacity());

                                            if (res == 0)
                                            {
                                                // Log.i(TAG,
                                                //       "android_tox_callback_friend_connection_status_cb_method:hash(1)=" +
                                                //       bytes_to_hex(hash_bytes));
                                                // send avatar to friend -------
                                                long filenum = tox_file_send(friend_number_, TOX_FILE_KIND_AVATAR.value,
                                                                             avatar_bytes.capacity(), hash_bytes,
                                                                             "avatar.png", "avatar.png".length());
                                                Log.i(TAG,
                                                      "android_tox_callback_friend_connection_status_cb_method:filenum=" +
                                                      filenum);
                                                // save FT to db ---------------
                                                Filetransfer ft_avatar_outgoing = new Filetransfer();
                                                ft_avatar_outgoing.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(
                                                        friend_number_);
                                                ft_avatar_outgoing.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
                                                ft_avatar_outgoing.file_number = filenum;
                                                ft_avatar_outgoing.kind = TOX_FILE_KIND_AVATAR.value;
                                                ft_avatar_outgoing.filesize = avatar_bytes.capacity();
                                                long rowid = HelperFiletransfer.insert_into_filetransfer_db(
                                                        ft_avatar_outgoing);
                                                ft_avatar_outgoing.id = rowid;
                                            }
                                            else
                                            {
                                                Log.i(TAG,
                                                      "android_tox_callback_friend_connection_status_cb_method:tox_hash res=" +
                                                      res);
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
                            }
                        }
                    };
                    main_handler_s.post(myRunnable);
                }
            }

            if (a_TOX_CONNECTION == TOX_CONNECTION_NONE.value)
            {
                // ******** friend going offline ********
                Log.i(TAG, "friend_connection_status:friend going offline:" + System.currentTimeMillis());
            }
            else
            {
                went_online = true;
                // ******** friend coming online ********
                Log.i(TAG, "friend_connection_status:friend coming online:" + LAST_ONLINE_TIMSTAMP_ONLINE_NOW);
            }
        }

        if (went_online)
        {
            Log.i(TAG, "friend_connection_status:friend status seems: ONLINE");
            f.last_online_timestamp = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
            HelperFriend.update_friend_in_db_last_online_timestamp(f);
            f.TOX_CONNECTION = a_TOX_CONNECTION;
            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
            HelperFriend.update_friend_in_db_connection_status(f);

            try
            {
                if (message_list_activity != null)
                {
                    if (message_list_activity.get_current_friendnum() == friend_number_)
                    {
                        message_list_activity.set_friend_connection_status_icon();
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
            Log.i(TAG, "friend_connection_status:friend status combined con status:" + combined_connection_status_);

            if (get_toxconnection_wrapper(combined_connection_status_) == TOX_CONNECTION_NONE.value)
            {
                Log.i(TAG, "friend_connection_status:friend status combined: OFFLINE");
                f.last_online_timestamp = System.currentTimeMillis();
                HelperFriend.update_friend_in_db_last_online_timestamp(f);
                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status(f);

                try
                {
                    if (message_list_activity != null)
                    {
                        if (message_list_activity.get_current_friendnum() == friend_number_)
                        {
                            message_list_activity.set_friend_connection_status_icon();
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
                Log.i(TAG, "friend or relay offline, combined still ONLINE");
                HelperFriend.update_single_friend_in_friendlist_view(f);
            }
        }
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

    public void show_add_friend(View view)
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        // intent.putExtra("key", value);
        startActivityForResult(intent, AddFriendActivity_ID);
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
        // TODO: write me ----
        // try to load avatar image, and set in friendlist fragment
    }

    static void move_tmp_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        // Log.i(TAG, "move_tmp_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" +
        //           dst_file_name);
        try
        {
            if (VFS_ENCRYPT)
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
                java.io.File f1 = new java.io.File(src_path_name + "/" + src_file_name);
                java.io.File f2 = new java.io.File(dst_path_name + "/" + dst_file_name);
                java.io.File dst_dir = new java.io.File(dst_path_name + "/");
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
            if (VFS_ENCRYPT)
            {
                java.io.File f_real = new java.io.File(src_path_name + "/" + src_file_name);
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
                java.io.File f_real = new java.io.File(src_path_name + "/" + src_file_name);
                String uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length());
                Log.i(TAG, "copy_real_file_to_vfs_file:uniq_temp_filename=" + uniq_temp_filename);
                java.io.File f2 = new java.io.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + uniq_temp_filename);
                java.io.File dst_dir = new java.io.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
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
            java.io.File dst_dir = new java.io.File(SD_CARD_STATIC_DIR + "/");
            dst_dir.mkdirs();
            java.io.File fout = new java.io.File(SD_CARD_STATIC_DIR + "/" + "__dummy__dummy_.jpg");
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
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f_real = new info.guardianproject.iocipher.File(
                        src_path_name + "/" + src_file_name);
                uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length()) + appl;
                // Log.i(TAG, "copy_vfs_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" + uniq_temp_filename);
                java.io.File f2 = new java.io.File(dst_path_name + "/" + uniq_temp_filename);
                java.io.File dst_dir = new java.io.File(dst_path_name + "/");
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
            Log.i(TAG, "copy_vfs_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }

        return uniq_temp_filename;
    }

    static void export_vfs_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        try
        {
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f_real = new info.guardianproject.iocipher.File(
                        src_path_name + "/" + src_file_name);
                java.io.File f2 = new java.io.File(dst_path_name + "/" + dst_file_name);
                java.io.File dst_dir = new java.io.File(dst_path_name + "/");
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
            if (VFS_ENCRYPT)
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
                java.io.File f1 = new java.io.File(vfs_image_filename);
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

    static void put_vfs_image_on_imageview(Context c, ImageView v, Drawable placholder, String vfs_image_filename)
    {
        try
        {
            // Log.i(TAG, "put_vfs_image_on_imageview:" + vfs_image_filename);
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
                // info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                //byte[] byteArray = new byte[(int) f1.length()];
                // fis.read(byteArray, 0, (int) f1.length());

                if (placholder == null)
                {
                    GlideApp.
                            with(c).
                            load(f1).
                            placeholder(R.drawable.round_loading_animation).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
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
                            skipMemoryCache(false).
                            into(v);
                }
            }
            else
            {
                java.io.File f1 = new java.io.File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);
                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());
                GlideApp.
                        with(c).
                        load(byteArray).
                        placeholder(placholder).
                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        skipMemoryCache(false).
                        into(v);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static String get_g_opts(String key)
    {
        try
        {
            if (orma.selectFromTRIFADatabaseGlobals().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobals g_opts = orma.selectFromTRIFADatabaseGlobals().keyEq(key).get(0);
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
            TRIFADatabaseGlobals g_opts = new TRIFADatabaseGlobals();
            g_opts.key = key;
            g_opts.value = value;

            try
            {
                orma.insertIntoTRIFADatabaseGlobals(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT)");
            }
            catch (android.database.sqlite.SQLiteConstraintException e)
            {
                e.printStackTrace();

                try
                {
                    orma.updateTRIFADatabaseGlobals().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE)");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddFriendActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                String friend_tox_id1 = data.getStringExtra("toxid");
                String friend_tox_id = "";
                friend_tox_id = friend_tox_id1.toUpperCase().replace(" ", "").replaceFirst("tox:", "").replaceFirst(
                        "TOX:", "").replaceFirst("Tox:", "");
                HelperFriend.add_friend_real(friend_tox_id);
            }
            else
            {
                // (resultCode == RESULT_CANCELED)
            }
        }
    }


    static int add_tcp_relay_single_wrapper(String ip, long port, String key_hex)
    {
        return add_tcp_relay_single(ip, key_hex, port);
    }

    static int bootstrap_single_wrapper(String ip, long port, String key_hex)
    {
        return bootstrap_single(ip, key_hex, port);
    }

    void sendEmailWithAttachment(Context c, final String recipient, final String subject, final String message, final String full_file_name, final String full_file_name_suppl)
    {
        try
        {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipient, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(Uri.parse("file://" + full_file_name));
            Log.i(TAG, "email:full_file_name=" + full_file_name);
            File ff = new File(full_file_name);
            Log.i(TAG, "email:full_file_name exists:" + ff.exists());

            try
            {
                if (new File(full_file_name_suppl).length() > 0)
                {
                    uris.add(Uri.parse("file://" + full_file_name_suppl));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "email:EE1:" + e.getMessage());
            }

            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
            List<LabeledIntent> intents = new ArrayList<>();

            if (resolveInfos.size() != 0)
            {
                for (ResolveInfo info : resolveInfos)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    Log.i(TAG, "email:" + "comp=" + info.activityInfo.packageName + " " + info.activityInfo.name);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});

                    if (subject != null)
                    {
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    }

                    if (message != null)
                    {
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        // ArrayList<String> extra_text = new ArrayList<String>();
                        // extra_text.add(message);
                        // intent.putStringArrayListExtra(android.content.Intent.EXTRA_TEXT, extra_text);
                        // Log.i(TAG, "email:" + "message=" + message);
                        // Log.i(TAG, "email:" + "intent extra_text=" + extra_text);
                    }

                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intents.add(new LabeledIntent(intent, info.activityInfo.packageName,
                                                  info.loadLabel(getPackageManager()), info.icon));
                }

                try
                {
                    Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1),
                                                          "Send email with attachments");
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                    startActivity(chooser);
                }
                catch (Exception email_app)
                {
                    email_app.printStackTrace();
                    Log.i(TAG, "email:" + "Error starting Email App");
                    new AlertDialog.Builder(c).setMessage("Error starting Email App").setPositiveButton("Ok",
                                                                                                        null).show();
                }
            }
            else
            {
                Log.i(TAG, "email:" + "No Email App found");
                new AlertDialog.Builder(c).setMessage("No Email App found").setPositiveButton("Ok", null).show();
            }
        }
        catch (ActivityNotFoundException e)
        {
            // cannot send email for some reason
            e.printStackTrace();
            Log.i(TAG, "email:EE2:" + e.getMessage());
        }
    }

    static String safe_string_XX(byte[] in)
    {
        Log.i(TAG, "safe_string:in=" + in);
        String out = "";

        try
        {
            out = new String(in, "UTF-8");  // Best way to decode using "UTF-8"
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "safe_string:EE:" + e.getMessage());

            try
            {
                out = new String(in);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "safe_string:EE2:" + e2.getMessage());
            }
        }

        Log.i(TAG, "safe_string:out=" + out);
        return out;
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
            float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
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
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    void getVersionInfo()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void update_fps()
    {
        if ((last_updated_fps + update_fps_every_ms) < System.currentTimeMillis())
        {
            last_updated_fps = System.currentTimeMillis();

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
            info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(filename_with_fullpath);
            int size = (int) file.length();
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
            }
        }

        return null;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int start, int len)
    {
        char[] hexChars = new char[(len) * 2];
        System.out.println("blen=" + (len));

        for (int j = start; j < (start + len); j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = hexArray[v >>> 4];
            hexChars[(j - start) * 2 + 1] = hexArray[v & 0x0F];
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
        return packageInfo_s;
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
            return df_date_time_long.format(new Date(timestamp_in_millis));
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
            return df_date_time_long.format(new Date(timestamp_in_millis));
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
            return df_date_only.format(new Date(timestamp_in_millis));
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
                        waiting_view.setVisibility(View.VISIBLE);
                        waiting_image.setVisibility(View.VISIBLE);
                        normal_container.setVisibility(View.INVISIBLE);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "waiting_for_orbot_info:EE:" + e.getMessage());
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
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Log.i(TAG, "waiting_for_orbot_info:" + enable);
                        waiting_view.setVisibility(View.GONE);
                        waiting_image.setVisibility(View.GONE);
                        normal_container.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "waiting_for_orbot_info:EE:" + e.getMessage());
                    }
                }
            };

            if (main_handler_s != null)
            {
                main_handler_s.post(myRunnable);
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
            BigInteger bigInt = new BigInteger(1, hash_value.getBytes());
            int ret = (int) (bigInt.longValue() % (long) number_of_buckets);
            // Log.i(TAG, "hash_to_bucket:" + "ret=" + ret + " hash_as_int=" + bigInt + " hash=" + hash_value);
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

    static class delete_selected_messages_asynchtask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog progressDialog2;
        private WeakReference<Context> weakContext;
        boolean update_message_list = false;
        boolean update_friend_list = false;
        String dialog_text = "";

        delete_selected_messages_asynchtask(Context c, ProgressDialog progressDialog2, boolean update_message_list, boolean update_friend_list, String dialog_text)
        {
            this.weakContext = new WeakReference<>(c);
            this.progressDialog2 = progressDialog2;
            this.update_message_list = update_message_list;
            this.update_friend_list = update_friend_list;
            this.dialog_text = dialog_text;
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            // sort ascending (lowest ID on top)
            Collections.sort(selected_messages, new Comparator<Long>()
            {
                public int compare(Long o1, Long o2)
                {
                    return o1.compareTo(o2);
                }
            });
            Iterator i = selected_messages.iterator();

            while (i.hasNext())
            {
                try
                {
                    long mid = (Long) i.next();
                    final Message m_to_delete = orma.selectFromMessage().idEq(mid).get(0);

                    // ---------- delete fileDB if this message is an outgoing file ----------
                    if (m_to_delete.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (m_to_delete.direction == 1)
                        {
                            try
                            {
                                // FileDB file_ = orma.selectFromFileDB().idEq(m_to_delete.filedb_id).get(0);
                                orma.deleteFromFileDB().idEq(m_to_delete.filedb_id).execute();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "delete_selected_messages_asynchtask:EE4:" + e.getMessage());
                            }
                        }
                    }

                    // ---------- delete fileDB if this message is an outgoing file ----------

                    // ---------- delete fileDB and VFS file if this message is an incoming file ----------
                    if (m_to_delete.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (m_to_delete.direction == 0)
                        {
                            try
                            {
                                FileDB file_ = orma.selectFromFileDB().idEq(m_to_delete.filedb_id).get(0);

                                try
                                {
                                    info.guardianproject.iocipher.File f_vfs = new info.guardianproject.iocipher.File(
                                            file_.path_name + "/" + file_.file_name);

                                    if (f_vfs.exists())
                                    {
                                        f_vfs.delete();
                                    }
                                }
                                catch (Exception e6)
                                {
                                    e6.printStackTrace();
                                    Log.i(TAG, "delete_selected_messages_asynchtask:EE5:" + e6.getMessage());
                                }

                                orma.deleteFromFileDB().idEq(m_to_delete.filedb_id).execute();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "delete_selected_messages_asynchtask:EE4:" + e.getMessage());
                            }
                        }
                    }

                    // ---------- delete fileDB and VFS file if this message is an incoming file ----------

                    // ---------- delete the message itself ----------
                    try
                    {
                        long message_id_to_delete = m_to_delete.id;

                        try
                        {
                            if (update_message_list)
                            {
                                Runnable myRunnable = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            MainActivity.message_list_fragment.adapter.remove_item(m_to_delete);
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

                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            try
                            {
                                if (update_message_list)
                                {
                                    Thread.sleep(50);
                                }
                            }
                            catch (Exception sleep_ex)
                            {
                                sleep_ex.printStackTrace();
                            }

                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            orma.deleteFromMessage().idEq(message_id_to_delete).execute();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "delete_selected_messages_asynchtask:EE1:" + e.getMessage());
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        Log.i(TAG, "delete_selected_messages_asynchtask:EE2:" + e2.getMessage());
                    }

                    // ---------- delete the message itself ----------
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "delete_selected_messages_asynchtask:EE3:" + e2.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            selected_messages.clear();
            selected_messages_incoming_file.clear();
            selected_messages_text_only.clear();

            try
            {
                progressDialog2.dismiss();
                Context c = weakContext.get();
                Toast.makeText(c, "Messages deleted", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e4)
            {
                e4.printStackTrace();
                Log.i(TAG, "save_selected_messages_asynchtask:EE3:" + e4.getMessage());
            }
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if (this.progressDialog2 == null)
            {
                try
                {
                    Context c = weakContext.get();
                    progressDialog2 = ProgressDialog.show(c, "", dialog_text);
                    progressDialog2.setCanceledOnTouchOutside(false);
                    progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "onPreExecute:start:EE:" + e.getMessage());
                }
            }
        }
    }

    static class save_selected_messages_asynchtask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog progressDialog2;
        private WeakReference<Context> weakContext;

        save_selected_messages_asynchtask(Context c, ProgressDialog progressDialog2)
        {
            this.weakContext = new WeakReference<>(c);
            this.progressDialog2 = progressDialog2;
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            Iterator i = selected_messages_incoming_file.iterator();

            while (i.hasNext())
            {
                try
                {
                    long mid = (Long) i.next();
                    Message m = orma.selectFromMessage().idEq(mid).get(0);
                    FileDB file_ = orma.selectFromFileDB().idEq(m.filedb_id).get(0);
                    export_vfs_file_to_real_file(file_.path_name, file_.file_name,
                                                 SD_CARD_FILES_EXPORT_DIR + "/" + m.tox_friendpubkey + "/",
                                                 file_.file_name);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "save_selected_messages_asynchtask:EE1:" + e2.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            selected_messages.clear();
            selected_messages_incoming_file.clear();
            selected_messages_text_only.clear();

            try
            {
                // need to redraw all items again here, to remove the selections
                MainActivity.message_list_fragment.adapter.redraw_all_items();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "save_selected_messages_asynchtask:EE2:" + e.getMessage());
            }

            try
            {
                progressDialog2.dismiss();
                Context c = weakContext.get();
                Toast.makeText(c, "Messages exported", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e4)
            {
                e4.printStackTrace();
                Log.i(TAG, "save_selected_messages_asynchtask:EE3:" + e4.getMessage());
            }
        }

        @Override
        protected void onPreExecute()
        {
        }
    }

    static class delete_selected_conference_messages_asynchtask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog progressDialog2;
        private WeakReference<Context> weakContext;
        boolean update_conf_message_list = false;
        String dialog_text = "";

        delete_selected_conference_messages_asynchtask(Context c, ProgressDialog progressDialog2, boolean update_conf_message_list, String dialog_text)
        {
            this.weakContext = new WeakReference<>(c);
            this.progressDialog2 = progressDialog2;
            this.update_conf_message_list = update_conf_message_list;
            this.dialog_text = dialog_text;
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            // sort ascending (lowest ID on top)
            Collections.sort(selected_conference_messages, new Comparator<Long>()
            {
                public int compare(Long o1, Long o2)
                {
                    return o1.compareTo(o2);
                }
            });
            Iterator i = selected_conference_messages.iterator();

            while (i.hasNext())
            {
                try
                {
                    long mid = (Long) i.next();
                    final ConferenceMessage m_to_delete = orma.selectFromConferenceMessage().idEq(mid).get(0);

                    // ---------- delete the message itself ----------
                    try
                    {
                        long message_id_to_delete = m_to_delete.id;

                        try
                        {
                            if (update_conf_message_list)
                            {
                                Runnable myRunnable = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            MainActivity.conference_message_list_fragment.adapter.remove_item(m_to_delete);
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

                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            try
                            {
                                if (update_conf_message_list)
                                {
                                    Thread.sleep(50);
                                }
                            }
                            catch (Exception sleep_ex)
                            {
                                sleep_ex.printStackTrace();
                            }

                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            orma.deleteFromConferenceMessage().idEq(message_id_to_delete).execute();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "delete_selected_conference_messages_asynchtask:EE1:" + e.getMessage());
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        Log.i(TAG, "delete_selected_conference_messages_asynchtask:EE2:" + e2.getMessage());
                    }

                    // ---------- delete the message itself ----------
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "delete_selected_conference_messages_asynchtask:EE3:" + e2.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            selected_conference_messages.clear();

            try
            {
                progressDialog2.dismiss();
                Context c = weakContext.get();
                Toast.makeText(c, "Messages deleted", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e4)
            {
                e4.printStackTrace();
                Log.i(TAG, "delete_selected_conference_messages_asynchtask:EE3:" + e4.getMessage());
            }
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if (this.progressDialog2 == null)
            {
                try
                {
                    Context c = weakContext.get();
                    progressDialog2 = ProgressDialog.show(c, "", dialog_text);
                    progressDialog2.setCanceledOnTouchOutside(false);
                    progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "onPreExecute:start:EE:" + e.getMessage());
                }
            }
        }
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

    static class send_message_result
    {
        long msg_num;
        boolean msg_v2;
        String msg_hash_hex;
        String raw_message_buf_hex;
        long error_num;
    }

    /*************************************************************************/
    /* this function now really sends a 1:1 to a friend (or a friends relay) */

    /*************************************************************************/
    public static send_message_result tox_friend_send_message_wrapper(long friendnum, int a_TOX_MESSAGE_TYPE, @NonNull String message)
    {
        Log.d(TAG, "tox_friend_send_message_wrapper:" + friendnum);
        long friendnum_to_use = friendnum;
        FriendList f = HelperFriend.main_get_friend(friendnum);
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
                    friendnum_to_use = HelperFriend.tox_friend_by_public_key__wrapper(relay_pubkey);
                    Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
                }
            }
        }

        send_message_result result = new send_message_result();
        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
        ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        // use msg V2 API Call
        long t_sec = (System.currentTimeMillis() / 1000);
        long res = tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE, t_sec, message,
                                                   message.length(), raw_message_buf, raw_message_length_buf,
                                                   msg_id_buffer);
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
        tox_self_set_nospam(new_nospam);
        update_savedata_file_wrapper(); // set new random nospam

        try
        {
            update_toxid_display_s();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.i(TAG, "new ToxID=" + MainActivity.get_my_toxid());
        // Log.i(TAG, "new NOSPAM=" + MainActivity.tox_self_get_nospam());
    }

    static void update_savedata_file_wrapper()
    {
        if (is_tox_started == true)
        {
            long start_timestamp = System.currentTimeMillis();
            update_savedata_file(TrifaSetPatternActivity.bytesToString(
                    TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(PREF__DB_secrect_key))));
            long end_timestamp = System.currentTimeMillis();
            Log.i(TAG, "update_savedata_file() took:" + (((float) (end_timestamp - start_timestamp)) / 1000f) + "s");
        }
        else
        {
            Log.i(TAG, "update_savedata_file(): ERROR:Tox not ready:001");
        }
    }

    private void fadeInAndShowImage(final View img, long start_after_millis)
    {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(start_after_millis);
        fadeIn.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
            }
        });
        img.startAnimation(fadeIn);
    }

    private void fadeOutAndHideImage(final View img, long start_after_millis)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);
        fadeOut.setStartOffset(start_after_millis);
        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }
        });
        img.startAnimation(fadeOut);
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
            if (message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (message_list_activity.get_current_friendnum() == friend_number)
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

            if (message_list_activity != null)
            {
                if (message_list_activity.get_current_friendnum() == friend_number)
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
                            if ((Notification_new_message_last_shown_timestamp +
                                 Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (PREF__notification)
                                {
                                    Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
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
                                    b.setContentText("new Message");
                                    Notification notification3 = b.build();
                                    nmn3.notify(Notification_new_message_ID, notification3);
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
            if (message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (message_list_activity.get_current_friendnum() == friend_number)
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
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = tox_messagev2_get_ts_ms(raw_message_buf);
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

            if (message_list_activity != null)
            {
                if (message_list_activity.get_current_friendnum() == friend_number)
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
                            if ((Notification_new_message_last_shown_timestamp +
                                 Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (PREF__notification)
                                {
                                    Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
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
                                    b.setContentText("new Message");
                                    Notification notification3 = b.build();
                                    nmn3.notify(Notification_new_message_ID, notification3);
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
        else if (msg_type == 2)
        {
            // msgV2 relay message
            long friend_number_real_sender = HelperFriend.tox_friend_by_public_key__wrapper(original_sender_pubkey);
            // Log.i(TAG,
            //      "friend_message_v2:friend:" + friend_number + " ts:" + ts_sec + " systime" + System.currentTimeMillis() +
            //      " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            if (message_list_activity != null)
            {
                // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
                if (message_list_activity.get_current_friendnum() == friend_number_real_sender)
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
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = tox_messagev2_get_ts_ms(raw_message_buf);
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

            if (message_list_activity != null)
            {
                if (message_list_activity.get_current_friendnum() == friend_number_real_sender)
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
                            if ((Notification_new_message_last_shown_timestamp +
                                 Notification_new_message_every_millis) < System.currentTimeMillis())
                            {
                                if (PREF__notification)
                                {
                                    Notification_new_message_last_shown_timestamp = System.currentTimeMillis();
                                    Intent notificationIntent = new Intent(context_s, StartMainActivityWrapper.class);
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0,
                                                                                            notificationIntent, 0);
                                    // -- notification ------------------
                                    // -- notification ------------------
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
                                    b.setContentText("new Message");
                                    Notification notification3 = b.build();
                                    nmn3.notify(Notification_new_message_ID, notification3);
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

    static void save_sps_pps_nal(byte[] sps_pps_nal_unit_bytes)
    {
        global_sps_pps_nal_unit_bytes = sps_pps_nal_unit_bytes;
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

    static int toxav_video_send_frame_uv_reversed_wrapper(byte[] buf2, long friendnum, int frame_width_px, int frame_height_px)
    {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (PREF__use_H264_hw_encoding) &&
            (Callstate.video_out_codec == VIDEO_CODEC_H264))
        {
            if (PREF__camera_get_preview_format.equals("YV12"))
            {
                byte[] buf = new byte[buf2.length];
                buf = YV12totoNV12(buf2, buf, frame_width_px, frame_height_px);
                feed_h264_encoder(buf, frame_width_px, frame_height_px);
            }
            else // (PREF__camera_get_preview_format == "NV21")
            {
                byte[] buf = new byte[buf2.length];
                buf = NV21toNV12(buf2, buf, frame_width_px, frame_height_px);
                feed_h264_encoder(buf, frame_width_px, frame_height_px);
            }

            // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:--------------------------");
            for (int jj = 0; jj < 2; jj++)
            {
                h264_encoder_output_data h264_out_data = fetch_from_h264_encoder();

                if (h264_out_data != null)
                {
                    byte[] buf_out = h264_out_data.data;

                    if (buf_out != null)
                    {
                        if (h264_out_data.sps_pps != null)
                        {
                            save_sps_pps_nal(h264_out_data.sps_pps);
                        }

                        if (global_sps_pps_nal_unit_bytes != null)
                        {
                            if (send_sps_pps_every_x_frames_current > send_sps_pps_every_x_frames)
                            {
                                // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:send_sps_pps:1");
                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(global_sps_pps_nal_unit_bytes);
                                toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px,
                                                            global_sps_pps_nal_unit_bytes.length);
                                send_sps_pps_every_x_frames_current = 0;
                            }

                            send_sps_pps_every_x_frames_current++;
                        }

                        MainActivity.video_buffer_2.rewind();
                        MainActivity.video_buffer_2.put(buf_out);
                        toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px, buf_out.length);
                    }
                    else
                    {
                        // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:buf_out==null:#" + jj);
                    }
                }
            }

            return 0;
        }
        else
        {
            return toxav_video_send_frame_uv_reversed(friendnum, frame_width_px, frame_height_px);
        }
    }

    static int toxav_video_send_frame_wrapper(byte[] buf, long friendnum, int frame_width_px, int frame_height_px)
    {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (PREF__use_H264_hw_encoding) &&
            (Callstate.video_out_codec == VIDEO_CODEC_H264))
        {
            feed_h264_encoder(buf, frame_width_px, frame_height_px);

            for (int jj = 0; jj < 2; jj++)
            {
                h264_encoder_output_data h264_out_data = fetch_from_h264_encoder();
                byte[] buf_out = h264_out_data.data;

                if (buf_out != null)
                {
                    if (h264_out_data.sps_pps != null)
                    {
                        save_sps_pps_nal(h264_out_data.sps_pps);
                    }

                    if (global_sps_pps_nal_unit_bytes != null)
                    {
                        if (send_sps_pps_every_x_frames_current > send_sps_pps_every_x_frames)
                        {
                            // Log.i(TAG, "video_send_frame_uv_reversed_wrapper:send_sps_pps:2");
                            MainActivity.video_buffer_2.rewind();
                            MainActivity.video_buffer_2.put(global_sps_pps_nal_unit_bytes);
                            toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px,
                                                        global_sps_pps_nal_unit_bytes.length);
                            send_sps_pps_every_x_frames_current = 0;
                        }

                        send_sps_pps_every_x_frames_current++;
                    }

                    MainActivity.video_buffer_2.rewind();
                    MainActivity.video_buffer_2.put(buf_out);
                    toxav_video_send_frame_h264(friendnum, frame_width_px, frame_height_px, buf_out.length);
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
            return toxav_video_send_frame(friendnum, frame_width_px, frame_height_px);
        }
    }

    // --------- make app crash ---------
    // --------- make app crash ---------
    // --------- make app crash ---------
    public static void crash_app_java(int type)
    {
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======================+++++");
        System.out.println("+++++======= TYPE:J =======+++++");
        System.out.println("+++++======================+++++");

        if (type == 1)
        {
            Java_Crash_001();
        }
        else if (type == 2)
        {
            Java_Crash_002();
        }
        else
        {
            stackOverflow();
        }
    }

    public static void Java_Crash_001()
    {
        Integer i = null;
        i.byteValue();
    }

    public static void Java_Crash_002()
    {
        View v = null;
        v.bringToFront();
    }

    public static void stackOverflow()
    {
        stackOverflow();
    }

    public static void crash_app_C()
    {
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======================+++++");
        System.out.println("+++++======= TYPE:C =======+++++");
        System.out.println("+++++======================+++++");
        AppCrashC();
    }

    public static native void AppCrashC();
    // --------- make app crash ---------
    // --------- make app crash ---------
    // --------- make app crash ---------

}

