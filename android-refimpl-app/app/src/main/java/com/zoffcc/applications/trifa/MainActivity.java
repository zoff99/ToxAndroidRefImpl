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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import com.zoffcc.applications.nativeaudio.NativeAudio;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Detector;

import java.io.BufferedInputStream;
import java.io.File;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import info.guardianproject.iocipher.VirtualFileSystem;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import info.guardianproject.netcipher.proxy.StatusCallback;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.webkit.MimeTypeMap.getFileExtensionFromUrl;
import static com.zoffcc.applications.trifa.AudioReceiver.channels_;
import static com.zoffcc.applications.trifa.AudioReceiver.sampling_rate_;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.CallingActivity.on_call_ended_actions;
import static com.zoffcc.applications.trifa.CallingActivity.on_call_started_actions;
import static com.zoffcc.applications.trifa.MessageListActivity.ml_friend_typing;
import static com.zoffcc.applications.trifa.ProfileActivity.update_toxid_display_s;
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
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_online_timestamp;
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
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
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

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = true; // set "false" for release builds
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true; // set "true" always!
    final static boolean VFS_ENCRYPT = true; // set "true" always!
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

    static TextView mt = null;
    ImageView top_imageview = null;
    static boolean native_lib_loaded = false;
    static boolean native_audio_lib_loaded = false;
    static String app_files_directory = "";
    final static boolean DEBUG_show_net_connection = false;
    // static boolean stop_me = false;
    // static Thread ToxServiceThread = null;
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
    final static int audio_in_buffer_max_count = 8; // how many out play buffers?
    final static int audio_out_buffer_mult = 8;
    static int audio_in_buffer_element_count = 0;
    static ByteBuffer[] audio_buffer_2 = new ByteBuffer[audio_in_buffer_max_count];
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
    static int PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
    static String PREF__DB_secrect_key = "98rj93ßjw3j8j4vj9w8p9eüiü9aci092";
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
    static boolean PREF__set_fps = true;
    static boolean PREF__fps_half = true;
    static boolean PREF__conference_show_system_messages = false;
    static boolean PREF__X_battery_saving_mode = false;
    static boolean PREF__X_misc_button_enabled = false;
    static String PREF__X_misc_button_msg = "t"; // TODO: hardcoded for now!
    static boolean PREF__U_keep_nospam = false;
    static boolean PREF__use_native_audio_play = true;
    static boolean PREF__use_audio_rec_effects = false;
    static boolean PREF__window_security = false;
    static int PREF__X_eac_delay_ms = 60;
    // from toxav/toxav.h -> valid values: 2.5, 5, 10, 20, 40 or 60 millseconds
    // 120 is also valid!!
    static int PREF__X_audio_recording_frame_size = 60; // !! 120 seems to work best somehow !!
    static boolean PREF__X_zoom_incoming_video = false;
    static boolean PREF__use_software_aec = true;
    static boolean PREF__allow_screen_off_in_audio_call = true;

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
        PREF__set_fps = settings.getBoolean("set_fps", true);
        PREF__conference_show_system_messages = settings.getBoolean("conference_show_system_messages", false);
        PREF__X_battery_saving_mode = settings.getBoolean("X_battery_saving_mode", false);
        PREF__X_misc_button_enabled = settings.getBoolean("X_misc_button_enabled", false);
        PREF__local_discovery_enabled = settings.getBoolean("local_discovery_enabled", false);
        PREF__use_native_audio_play = settings.getBoolean("X_use_native_audio_play", true);

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
                    settings.getString("X_audio_recording_frame_size", "" + 60));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_audio_recording_frame_size = 60;
        }
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
                                    set_all_conferences_inactive();
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
            set_all_conferences_inactive();
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
                        set_all_conferences_inactive();
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
        PREF__set_fps = settings.getBoolean("set_fps", true);
        PREF__conference_show_system_messages = settings.getBoolean("conference_show_system_messages", false);
        PREF__X_battery_saving_mode = settings.getBoolean("X_battery_saving_mode", false);
        PREF__X_misc_button_enabled = settings.getBoolean("X_misc_button_enabled", false);
        PREF__local_discovery_enabled = settings.getBoolean("local_discovery_enabled", false);
        PREF__use_native_audio_play = settings.getBoolean("X_use_native_audio_play", true);

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
                    settings.getString("X_audio_recording_frame_size", "" + 60));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__X_audio_recording_frame_size = 60;
        }

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

    static FriendList main_get_friend(long friendnum)
    {

        String pubkey_temp = tox_friend_get_public_key__wrapper(friendnum);
        // Log.i(TAG, "main_get_friend:pubkey=" + pubkey_temp + " fnum=" + friendnum);

        FriendList f;
        List<FriendList> fl = orma.selectFromFriendList().
                tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                toList();

        // Log.i(TAG, "main_get_friend:fl=" + fl + " size=" + fl.size());

        if (fl.size() > 0)
        {
            f = fl.get(0);
            // Log.i(TAG, "main_get_friend:f=" + f);
        }
        else
        {
            f = null;
        }

        return f;
    }

    static int is_friend_online(long friendnum)
    {
        try
        {
            return (orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0).TOX_CONNECTION);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static boolean is_conference_active(String conference_identifier)
    {
        try
        {
            return (orma.selectFromConferenceDB().
                    conference_identifierEq(conference_identifier).
                    toList().get(0).conference_active);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    synchronized static void set_all_friends_offline()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateFriendList().
                            TOX_CONNECTION(0).
                            execute();
                }
                catch (Exception e)
                {
                }

                try
                {
                    orma.updateFriendList().
                            last_online_timestampEq(LAST_ONLINE_TIMSTAMP_ONLINE_NOW).
                            last_online_timestamp(System.currentTimeMillis()).
                            execute();
                }
                catch (Exception e)
                {
                }

                // ------ DEBUG ------
                // ------ set all friends to "never" seen online ------
                // ------ DEBUG ------
                // try
                // {
                //     orma.updateFriendList().
                //             last_online_timestamp(LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE).
                //             execute();
                // }
                // catch (Exception e)
                // {
                // }
                // ------ DEBUG ------
                // ------ set all friends to "never" seen online ------
                // ------ DEBUG ------


                try
                {
                    friend_list_fragment.set_all_friends_to_offline();
                }
                catch (Exception e)
                {
                }
            }
        };
        t.start();
    }

    synchronized static void update_friend_in_db(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_string(f.tox_public_key_string).
                name(f.name).
                status_message(f.status_message).
                TOX_CONNECTION(f.TOX_CONNECTION).
                TOX_CONNECTION_on_off(f.TOX_CONNECTION_on_off).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();
    }

    synchronized static void update_friend_in_db_status_message(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                status_message(f.status_message).
                execute();
    }

    synchronized static void update_friend_in_db_status(FriendList f)
    {
        // Log.i(TAG, "update_friend_in_db_status:f=" + f);

        int numrows = orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();

        Log.i(TAG, "update_friend_in_db_status:numrows=" + numrows);

    }

    synchronized static void update_friend_in_db_connection_status(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                TOX_CONNECTION(f.TOX_CONNECTION).
                TOX_CONNECTION_on_off(f.TOX_CONNECTION_on_off).
                execute();
    }

    synchronized static void update_friend_in_db_last_online_timestamp(FriendList f)
    {
        // Log.i(TAG, "update_friend_in_db_last_online_timestamp");
        try
        {
            orma.updateFriendList().
                    tox_public_key_stringEq(f.tox_public_key_string).
                    last_online_timestamp(f.last_online_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_name(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                name(f.name).
                execute();
    }

    synchronized static void update_message_in_db(final Message m)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateMessage().
                            idEq(m.id).
                            read(m.read).
                            text(m.text).
                            sent_timestamp(m.sent_timestamp).
                            rcvd_timestamp(m.rcvd_timestamp).
                            filename_fullpath(m.filename_fullpath).
                            execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;

            update_message_in_db_filename_fullpath_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            orma.updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    filename_fullpath(m.filename_fullpath).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_read_rcvd_timestamp_rawmsgbytes(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    read(m.read).
                    raw_msgv2_bytes(m.raw_msgv2_bytes).
                    rcvd_timestamp(m.rcvd_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void change_notification(int a_TOXCONNECTION)
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
                        tox_service_fg.change_notification_fg(a_TOXCONNECTION_f);
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
        if (video_buffer_1 != null)
        {
            video_buffer_1 = null;
        }

        if (video_frame_image != null)
        {
            video_frame_image.recycle();
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

    public static native void bootstrap();

    public static native int add_tcp_relay_single(String ip, String key_hex, long port);

    public static native int bootstrap_single(String ip, String key_hex, long port);

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

    public static native int tox_messagev2_get_message_text(ByteBuffer raw_message_buffer, long raw_message_len, int is_alter_msg, long alter_type, ByteBuffer message_text_buffer);

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

    public static native long tox_conference_peer_get_name_size(long conference_number, long peer_number);

    public static native String tox_conference_peer_get_name(long conference_number, long peer_number);

    public static native int tox_conference_peer_number_is_ours(long conference_number, long peer_number);

    public static native long tox_conference_get_title_size(long conference_number);

    public static native String tox_conference_get_title(long conference_number);

    public static native int tox_conference_get_type(long conference_number);

    public static native int tox_conference_send_message(long conference_number, int a_TOX_MESSAGE_TYPE, @NonNull String message);

    public static native int tox_conference_delete(long conference_number);
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

    public static native int toxav_option_set(long friendnum, long a_TOXAV_OPTIONS_OPTION, long value);

    public static native void set_av_call_status(int status);

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
                        Intent intent = new Intent(context_s, CallingActivity.class);
                        Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(fn);
                        Callstate.friend_alias_name = get_friend_name_from_pubkey(Callstate.friend_pubkey);
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

        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

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
            alloc_in.copyFrom(video_buffer_1.array());
            yuvToRgb.setInput(alloc_in);
            yuvToRgb.forEach(alloc_out);
            alloc_out.copyTo(video_frame_image);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CallingActivity.mContentView.setBitmap(video_frame_image);
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

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
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
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
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
            Callstate.play_delay = comm_number;
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

        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
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
                    ((int) ((sample_count * channels) * 2)) * audio_out_buffer_mult; // TODO: this is really bad
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
            // always write to buffer[0] in the pipeline !! -----------

            Log.i(TAG, "audio_play:audio_buffer_play size=" + AudioReceiver.buffer_size);
        }

        // TODO: dirty hack, "make good"
        try
        {
            if (PREF__use_native_audio_play)
            {
                // Log.i(TAG, "audio_play:NativeAudio Play:001");

                if ((NativeAudio.sampling_rate != (int) sampling_rate_) || (NativeAudio.channel_count != channels_))
                {
                    NativeAudio.sampling_rate = (int) sampling_rate_;
                    NativeAudio.channel_count = channels_;
                    Log.i(TAG, "audio_play:NativeAudio restart Engine");
                    // TODO: locking? or something like that
                    NativeAudio.restartNativeAudioPlayEngine((int) sampling_rate_, channels_);
                }

                audio_buffer_2[0].position(0);
                int incoming_bytes = (int) ((sample_count * channels) * 2);

                //                System.out.println("NativeAudioPlay:000a:size="+ incoming_bytes +
                //                                   " offset="+audio_buffer_2[0].arrayOffset() +
                //                                   " limit="+audio_buffer_2[0].limit() +
                //                                   " bytes=" +
                //                                   audio_buffer_2[0].get(0) + " " +
                //                                   audio_buffer_2[0].get(1) + " " +
                //                                   audio_buffer_2[0].get(2) + " " +
                //                                   audio_buffer_2[0].get(3) + " " +
                //                                   audio_buffer_2[0].get(4) + " " +
                //                                   audio_buffer_2[0].get(5) + " " +
                //                                   audio_buffer_2[0].get(6) + " " +
                //                                   audio_buffer_2[0].get(7) + " " +
                //                                   audio_buffer_2[0].get(8) + " " +
                //                                   audio_buffer_2[0].get(9) + " " +
                //                                   audio_buffer_2[0].get(10) + " " +
                //                                   audio_buffer_2[0].get(11) + " " +
                //                                   audio_buffer_2[0].get(12)
                //                                   );

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
                        if (NativeAudio.n_cur_buf + 1 >= NativeAudio.n_audio_in_buffer_max_count)
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


                        if (NativeAudio.n_cur_buf + 1 >= NativeAudio.n_audio_in_buffer_max_count)
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
                    if (NativeAudio.n_cur_buf + 1 >= NativeAudio.n_audio_in_buffer_max_count)
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
            else
            {
                // audio_buffer_read_write(sample_count, channels, sampling_rate, true);
                if (audio_receiver_thread != null)
                {
                    if (!audio_receiver_thread.stopped)
                    {
                        //                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        //                    {
                        //                        // AudioTrack.write() called with invalid size (3840) value
                        //                        audio_receiver_thread.track.write(audio_buffer_2[0].array(), 0, (int) ((sample_count * channels) * 2), AudioTrack.WRITE_NON_BLOCKING);
                        //                    }
                        if (android.os.Build.VERSION.SDK_INT >= 21)
                        {
                            // AudioTrack.write() called with invalid size (3840) value
                            audio_buffer_2[0].position(0);
                            audio_receiver_thread.track.write(audio_buffer_2[0], (int) ((sample_count * channels) * 2),
                                                              AudioTrack.WRITE_NON_BLOCKING);
                        }
                        else
                        {
                            audio_receiver_thread.track.write(audio_buffer_2[0].array(), 0,
                                                              (int) ((sample_count * channels) * 2));
                        }
                    }
                }
            }
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
        change_notification(a_TOX_CONNECTION);
        // -- notification ------------------
        // -- notification ------------------
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        // Log.i(TAG, "friend_alias_name:friend:" + friend_number + " name:" + friend_alias_name);

        FriendList f = main_get_friend(friend_number);
        // Log.i(TAG, "friend_alias_name:002:" + f);
        if (f != null)
        {
            f.name = friend_name;
            update_friend_in_db_name(f);
            if (friend_list_fragment != null)
            {
                Log.i(TAG, "friend_alias_name:003");
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = true;
                cc.friend_item = f;
                friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
        }

    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
        // Log.i(TAG, "friend_status_message:friend:" + friend_number + " status message:" + status_message);

        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            f.status_message = status_message;
            update_friend_in_db_status_message(f);
            if (friend_list_fragment != null)
            {
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = true;
                cc.friend_item = f;
                friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
        // Log.i(TAG, "friend_status:friend:" + friend_number + " status:" + a_TOX_USER_STATUS);

        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            // Log.i(TAG, "friend_status:f=" + f);
            Log.i(TAG, "friend_status:1:f.TOX_USER_STATUS=" + f.TOX_USER_STATUS);
        }

        if (f != null)
        {
            f.TOX_USER_STATUS = a_TOX_USER_STATUS;
            Log.i(TAG, "friend_status:2:f.TOX_USER_STATUS=" + f.TOX_USER_STATUS);
            update_friend_in_db_status(f);

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

            try
            {
                Log.i(TAG, "friend_status:004");
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = true;
                cc.friend_item = f;
                friend_list_fragment.modify_friend(cc, cc.is_friend);
                Log.i(TAG, "friend_status:004");
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "friend_status:EE2:" + e.getMessage());
            }
        }
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            if (f.TOX_CONNECTION != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {

                    final long friend_number_ = friend_number;
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                // friend just came online
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
                                                ft_avatar_outgoing.tox_public_key_string = tox_friend_get_public_key__wrapper(
                                                        friend_number_);
                                                ft_avatar_outgoing.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
                                                ft_avatar_outgoing.file_number = filenum;
                                                ft_avatar_outgoing.kind = TOX_FILE_KIND_AVATAR.value;
                                                ft_avatar_outgoing.filesize = avatar_bytes.capacity();
                                                long rowid = insert_into_filetransfer_db(ft_avatar_outgoing);
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

            if (f != null)
            {
                if (f.TOX_CONNECTION != a_TOX_CONNECTION)
                {
                    if (a_TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                    {
                        // friend going offline
                        Log.i(TAG, "friend going offline:" + System.currentTimeMillis());
                        f.last_online_timestamp = System.currentTimeMillis();
                        update_friend_in_db_last_online_timestamp(f);
                    }
                    else
                    {
                        // friend coming online
                        Log.i(TAG, "friend coming online:" + LAST_ONLINE_TIMSTAMP_ONLINE_NOW);
                        f.last_online_timestamp = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
                        update_friend_in_db_last_online_timestamp(f);
                    }
                }
            }

            f.TOX_CONNECTION = a_TOX_CONNECTION;
            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);

            update_friend_in_db_connection_status(f);

            try
            {
                if (message_list_activity != null)
                {
                    if (message_list_activity.get_current_friendnum() == friend_number)
                    {
                        message_list_activity.set_friend_connection_status_icon();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                if (friend_list_fragment != null)
                {
                    // TODO: dirty hack, make better
                    final boolean sorted_reload = true;
                    if (!sorted_reload)
                    {
                        CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                        cc.is_friend = true;
                        cc.friend_item = f;
                        friend_list_fragment.modify_friend(cc, cc.is_friend);
                    }
                    else
                    {
                        friend_list_fragment.add_all_friends_clear(0);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
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

        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        msg_id_buffer.put(msg_id, 0, (int) TOX_HASH_LENGTH);
        final String message_id_hash_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                                msg_id_buffer.limit());

        // Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string);

        try
        {
            final Message m = orma.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
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
                            m.raw_msgv2_bytes = "";
                            m.rcvd_timestamp = System.currentTimeMillis();
                            m.read = true;
                            update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);

                            // TODO this updates all messages. should be done nicer and faster!
                            // update_message_view();
                            update_single_message(m, true);
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

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
        // Log.i(TAG, "friend_read_receipt:friend:" + friend_number + " message_id:" + message_id);

        try
        {
            // there can be older messages with same message_id for this friend! so always take the latest one! -------
            final Message m = orma.selectFromMessage().
                    message_idEq(message_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
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
                            update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);

                            // TODO this updates all messages. should be done nicer and faster!
                            // update_message_view();
                            update_single_message(m, true);
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

        final String friend_public_key__final = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // toxcore needs this!!
                    Thread.sleep(120);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                long friendnum = tox_friend_add_norequest(friend_public_key__final); // add friend

                try
                {
                    Thread.sleep(20);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                update_savedata_file_wrapper(); // save toxcore datafile (new friend added)

                final FriendList f = new FriendList();
                f.tox_public_key_string = friend_public_key__final;
                f.TOX_USER_STATUS = 0;
                f.TOX_CONNECTION = 0;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                // set name as the last 5 char of the publickey (until we get a proper name)
                f.name = friend_public_key__final.substring(friend_public_key__final.length() - 5,
                                                            friend_public_key__final.length());
                f.avatar_pathname = null;
                f.avatar_filename = null;

                try
                {
                    Log.i(TAG, "friend_request:insert:001:f=" + f);
                    long res = orma.insertIntoFriendList(f);
                    Log.i(TAG, "friend_request:insert:002:res=" + res);
                }
                catch (android.database.sqlite.SQLiteConstraintException e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "friend_request:insert:EE1:" + e.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "friend_request:insert:EE2:" + e.getMessage());
                }

                if (friend_list_fragment != null)
                {
                    Log.i(TAG, "friend_request:003");
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = true;
                    cc.friend_item = f;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----

                try
                {
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (PREF__U_keep_nospam == false)
                {
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    set_new_random_nospam_value();
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                }
            }
        };
        t.start();
    }


    static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length)
    {
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

        String msg_id_as_hex_string = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(),
                                                 msg_id_buffer.limit());
        // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);

        int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                tox_friend_get_public_key__wrapper(friend_number)).and().msg_id_hashEq(msg_id_as_hex_string).count();
        if (already_have_message > 0)
        {
            // it's a double send, ignore it
            // send message receipt v2, most likely the other party did not get it yet
            // TODO: use received timstamp, not "now" here!
            long t_sec_receipt = (System.currentTimeMillis() / 1000);
            tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
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


        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
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
        m.text = friend_message;
        m.msg_version = 1;
        m.msg_id_hash = msg_id_as_hex_string;

        Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

        if (message_list_activity != null)
        {
            if (message_list_activity.get_current_friendnum() == friend_number)
            {
                insert_into_message_db(m, true);
            }
            else
            {
                insert_into_message_db(m, false);
            }
        }
        else
        {
            insert_into_message_db(m, false);
        }

        // send message receipt v2
        long t_sec_receipt = (System.currentTimeMillis() / 1000);
        tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);

        try
        {
            // update "new" status on friendlist fragment
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
            if (friend_list_fragment != null)
            {
                if (f != null)
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = true;
                    cc.friend_item = f;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
            }

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


    // --- incoming message ---
    // --- incoming message ---
    // --- incoming message ---
    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
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
        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.rcvd_timestamp_ms = 0;
        m.sent_timestamp = System.currentTimeMillis();
        m.sent_timestamp_ms = 0;
        m.text = friend_message;
        m.msg_version = 0;

        if (message_list_activity != null)
        {
            if (message_list_activity.get_current_friendnum() == friend_number)
            {
                insert_into_message_db(m, true);
            }
            else
            {
                insert_into_message_db(m, false);
            }
        }
        else
        {
            insert_into_message_db(m, false);
        }

        try
        {
            // update "new" status on friendlist fragment
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
            if (friend_list_fragment != null)
            {
                if (f != null)
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = true;
                    cc.friend_item = f;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
            }

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
    // --- incoming message ---
    // --- incoming message ---
    // --- incoming message ---

    static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int a_TOX_FILE_CONTROL)
    {
        Log.i(TAG, "file_recv_control:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_CONTROL);

        if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_CANCEL.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_CANCEL");
            cancel_filetransfer(friend_number, file_number);
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_RESUME.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME");

            try
            {
                long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
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

                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);

                    // if outgoing FT set "ft_accepted" to true
                    set_filetransfer_accepted_from_id(ft_id);
                }
                else
                {
                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME::*DATA*");

                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);

                    Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:msg_id=" + msg_id);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_RESUME.value);

                    // if outgoing FT set "ft_accepted" to true
                    set_filetransfer_accepted_from_id(ft_id);
                    set_message_accepted_from_id(msg_id);

                    // update_all_messages_global(true);
                    try
                    {
                        if (ft_id != -1)
                        {
                            update_single_message_from_messge_id(msg_id, true);
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

                long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_PAUSE.value);
                set_message_state_from_id(msg_id, TOX_FILE_CONTROL_PAUSE.value);

                // update_all_messages_global(true);
                try
                {
                    if (ft_id != -1)
                    {
                        update_single_message_from_messge_id(msg_id, true);
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
        // Log.i(TAG, "file_chunk_request:" + friend_number + ":" + file_number + ":" + position + ":" + length);

        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_OUTGOING.value).
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
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
                    // Log.i(TAG, "file_chunk_request:res(2)=" + res);

                    // remove FT from DB
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
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
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft.id, friend_number);
                    Log.i(TAG, "file_chunk_request:file_READY:001a:msg_id=" + msg_id);

                    update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number, file_number,
                                                                                 ft.path_name + "/" + ft.file_name);
                    set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                 TOX_FILE_CONTROL_CANCEL.value);
                    set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);

                    try
                    {
                        Log.i(TAG, "file_chunk_request:file_READY:002");
                        if (ft.id != -1)
                        {
                            Log.i(TAG, "file_chunk_request:file_READY:003:f.id=" + ft.id + " msg_id=" + msg_id);
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_chunk_request:file_READY:EE:" + e.getMessage());
                    }

                    // transfer finished -----------

                    ByteBuffer avatar_chunk = ByteBuffer.allocateDirect(1);
                    int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0);
                    // Log.i(TAG, "file_chunk_request:res(2)=" + res);

                    // remove FT from DB
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
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
                    // Log.i(TAG, "file_chunk_request:res(1)=" + res);

                    if (ft.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                    {
                        if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                        {
                            ft.current_position = position;
                            update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        update_single_message_from_ftid(ft.id, false);
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
                            update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        update_single_message_from_ftid(ft.id, false);
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
        Log.i(TAG,
              "file_recv:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_KIND + ":" + file_size + ":" +
              filename + ":" + filename_length);

        if (a_TOX_FILE_KIND == TOX_FILE_KIND_AVATAR.value)
        {
            Log.i(TAG, "file_recv:incoming avatar");

            String file_name_avatar = FRIEND_AVATAR_FILENAME;

            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.message_id = -1;
            f.state = TOX_FILE_CONTROL_RESUME.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = file_name_avatar;
            f.filesize = file_size;
            f.current_position = 0;

            long row_id = insert_into_filetransfer_db(f);
            f.id = row_id;

            // TODO: we just accept incoming avatar, maybe make some checks first?
            tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_RESUME.value);
        }
        else // DATA file ft
        {
            Log.i(TAG, "file_recv:incoming regular file");

            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = tox_friend_get_public_key__wrapper(friend_number);
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

            long ft_id = insert_into_filetransfer_db(f);
            f.id = ft_id;

            // add FT message to UI
            Message m = new Message();

            m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.filetransfer_id = ft_id;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_PAUSE.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            m.rcvd_timestamp = System.currentTimeMillis();
            m.text = filename + "\n" + file_size + " bytes";

            long new_msg_id = -1;

            if (message_list_activity != null)
            {
                if (message_list_activity.get_current_friendnum() == friend_number)
                {
                    new_msg_id = insert_into_message_db(m, true);
                    m.id = new_msg_id;
                }
                else
                {
                    new_msg_id = insert_into_message_db(m, false);
                    m.id = new_msg_id;
                }
            }
            else
            {
                new_msg_id = insert_into_message_db(m, false);
                m.id = new_msg_id;
            }


            f.message_id = new_msg_id;
            update_filetransfer_db_full(f);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = true;
                cc.friend_item = f2;
                if (friend_list_fragment != null)
                {
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
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
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
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
                    fos = cache_ft_fos.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

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
                            tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

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

                update_filetransfer_db_fos_open(f);

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
                    set_friend_avatar(tox_friend_get_public_key__wrapper(friend_number),
                                      VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/", f.file_name);
                }
                else
                {
                    Log.i(TAG, "file_recv_chunk:file_READY:001:f.id=" + f.id);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(f.id, friend_number);
                    Log.i(TAG, "file_recv_chunk:file_READY:001a:msg_id=" + msg_id);

                    update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number, file_number,
                                                                                 VFS_PREFIX + VFS_FILE_DIR + "/" +
                                                                                 f.tox_public_key_string + "/" +
                                                                                 f.file_name);
                    set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                 TOX_FILE_CONTROL_CANCEL.value);
                    set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);

                    try
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:002");
                        if (f.id != -1)
                        {
                            Log.i(TAG, "file_recv_chunk:file_READY:003:f.id=" + f.id + " msg_id=" + msg_id);
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:EE:" + e.getMessage());
                    }

                }

                // remove FT from DB
                delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

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
                        cache_ft_fos.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                        f.fos_open = true;
                        update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);
                        if (fos == null)
                        {
                            fos = new info.guardianproject.iocipher.FileOutputStream(f.path_name + "/" + f.file_name);
                            // Log.i(TAG,
                            //       "file_recv_chunk:new fos[2]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                             fos);
                            f.fos_open = true;
                            update_filetransfer_db_fos_open(f);
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
                        cache_ft_fos_normal.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number,
                                                fos);
                        f.fos_open = true;
                        update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos_normal.get(
                                tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);
                        if (fos == null)
                        {
                            fos = new java.io.FileOutputStream(f.path_name + "/" + f.file_name);
                            // Log.i(TAG,
                            //      "file_recv_chunk:new fos[4]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos_normal.put(
                                    tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                            f.fos_open = true;
                            update_filetransfer_db_fos_open(f);
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
                        update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    update_single_message_from_ftid(f.id, false);
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
                        update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    update_single_message_from_ftid(f.id, false);
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
    static void android_tox_callback_conference_invite_cb_method(long friend_number, int a_TOX_CONFERENCE_TYPE, byte[] cookie_buffer, long cookie_length)
    {
        Log.i(TAG, "conference_invite_cb:fn=" + friend_number + " type=" + a_TOX_CONFERENCE_TYPE + " cookie_length=" +
                   cookie_length + " cookie=" + bytes_to_hex(cookie_buffer));

        ByteBuffer cookie_buf2 = ByteBuffer.allocateDirect((int) cookie_length);
        cookie_buf2.put(cookie_buffer);

        long conference_num = tox_conference_join(friend_number, cookie_buf2, cookie_length);

        String conference_identifier = bytes_to_hex(cookie_buffer);

        if (conference_num >= 0)
        {
            new_or_updated_conference(conference_num, tox_friend_get_public_key__wrapper(friend_number),
                                      conference_identifier, a_TOX_CONFERENCE_TYPE);
        }
        else
        {
            Log.i(TAG, "conference_invite_cb:error=" + conference_num + " joining conference");
        }

        try
        {
            if (conference_message_list_activity != null)
            {
                if (conference_message_list_activity.get_current_conf_id().equals(conference_identifier))
                {
                    conference_message_list_activity.set_conference_connection_status_icon();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "conference_invite_cb:res=" + conference_num);
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

        if (!do_badge_update)
        {
            m.is_new = false;
        }
        else
        {
            m.is_new = true;
        }

        // m.tox_friendnum = friend_number;
        m.tox_peerpubkey = tox_conference_peer_get_public_key__wrapper(conference_number, peer_number);
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
            m.tox_peername = tox_conference_peer_get_name__wrapper(m.conference_identifier, m.tox_peerpubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (conference_message_list_activity != null)
        {
            if (conference_message_list_activity.get_current_conf_id().equals(conf_id))
            {
                insert_into_conference_message_db(m, true);
            }
            else
            {
                insert_into_conference_message_db(m, false);
            }
        }
        else
        {
            long new_msg_id = insert_into_conference_message_db(m, false);
            Log.i(TAG, "conference_message_cb:new_msg_id=" + new_msg_id);
        }

        try
        {
            // update "new" status on friendlist fragment (for this conference)
            if (friend_list_fragment != null)
            {
                if (conf_temp != null)
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = conf_temp;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
        }


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


            // update friendlist fragment (for this conference)
            if (friend_list_fragment != null)
            {
                // ConferenceDB conf_temp = null;
                //
                //                try
                //                {
                //                    // TODO: cache me!!
                //                    conf_temp = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                //                            and().
                //                            conference_activeEq(true).
                //                            get(0);
                //                }
                //                catch (Exception e)
                //                {
                //                    e.printStackTrace();
                //                }

                if (conf_temp2 != null)
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = conf_temp2;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_tox_callback_conference_title_cb_method:EE1:" + e.getMessage());
        }
    }

    static void android_tox_callback_conference_peer_name_cb_method(long conference_number, long peer_number, String name, long name_length)
    {
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
    }

    // ------------------------
    // this is an old toxcore 0.1.x callback
    // not called anymore!!
    static void android_tox_callback_conference_namelist_change_cb_method(long conference_number, long peer_number, int a_TOX_CONFERENCE_STATE_CHANGE)
    {
        // Log.i(TAG, "namelist_change_cb:" + "confnum=" + conference_number + " peernum=" + peer_number + " state=" + a_TOX_CONFERENCE_STATE_CHANGE);
        // TODO: update peer status

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

                        insert_into_conference_message_db_system_message(m, true);
                    }
                    else
                    {
                        Log.i(TAG, "namelist_change_cb:010");

                        insert_into_conference_message_db_system_message(m, false);
                    }
                }
                else
                {
                    long new_msg_id = insert_into_conference_message_db_system_message(m, false);
                    Log.i(TAG, "conference_message_cb:new_msg_id=" + new_msg_id);
                }

                // update conferenc item -------
                if (friend_list_fragment != null)
                {
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = conf_temp;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                }
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

    public static void add_single_conference_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (conference_message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                ConferenceMessage m = orma.selectFromConferenceMessage().idEq(
                                        message_id).orderByIdDesc().get(0);
                                if (m.id != -1)
                                {
                                    if ((force) ||
                                        (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS <
                                         System.currentTimeMillis()))
                                    {
                                        update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.conference_message_list_fragment.add_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void add_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);

            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    if (message_id != -1)
                    {
                        Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);

                        try
                        {
                            Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);
                            if (m.id != -1)
                            {
                                Log.i(TAG, "add_single_message_from_messge_id:m.id=" + m.id);

                                if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    Log.i(TAG, "add_single_message_from_messge_id:add_message()");

                                    if (message_list_fragment == null)
                                    {
                                        // ok, we need to wait for onResume to finish
                                        Log.i(TAG,
                                              "add_single_message_from_messge_id:ok, we need to wait for onResume to finish");
                                        long loop = 0;
                                        while (loop < 40) // wait 8 sec., then give up
                                        {
                                            loop++;
                                            try
                                            {
                                                Thread.sleep(200);
                                            }
                                            catch (InterruptedException e)
                                            {
                                                e.printStackTrace();
                                            }

                                            if (message_list_fragment != null)
                                            {
                                                // got it
                                                break;
                                            }
                                        }
                                    }

                                    if (message_list_fragment != null)
                                    {
                                        update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.add_message(m);
                                    }
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            Log.i(TAG, "add_single_message_from_messge_id:EE1:" + e2.getMessage());
                        }
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "add_single_message_from_messge_id:EE2:" + e.getMessage());
        }
    }

    public static void update_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);
                                if (m.id != -1)
                                {
                                    if ((force) ||
                                        (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS <
                                         System.currentTimeMillis()))
                                    {
                                        update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.modify_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void update_single_message_from_ftid(final long filetransfer_id, final boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Message m = orma.selectFromMessage().filetransfer_idEq(filetransfer_id).orderByIdDesc().get(
                                    0);
                            if (m.id != -1)
                            {
                                if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    update_all_messages_global_timestamp = System.currentTimeMillis();
                                    MainActivity.message_list_fragment.modify_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static void update_single_message(Message m, boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                if ((force) ||
                    (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                {
                    update_all_messages_global_timestamp = System.currentTimeMillis();
                    message_list_fragment.modify_message(m);
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static long tox_friend_by_public_key__wrapper(@NonNull String friend_public_key_string)
    {
        if (cache_pubkey_fnum.containsKey(friend_public_key_string))
        {
            // Log.i(TAG, "cache hit:1");
            return cache_pubkey_fnum.get(friend_public_key_string);
        }
        else
        {
            if (cache_pubkey_fnum.size() >= 20)
            {
                // TODO: bad!
                cache_pubkey_fnum.clear();
            }
            long result = tox_friend_by_public_key(friend_public_key_string);
            cache_pubkey_fnum.put(friend_public_key_string, result);
            return result;
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:" + orma.selectFromMessage().toList().size());
            //
            //            int i = 0;
            //            for (i = 0; i < orma.selectFromMessage().toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2" + orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size());
            //
            //            for (i = 0; i < orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //

            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:filetransfer_id=" + filetransfer_id +
                       " friend_number=" + friend_number);

            List<Message> m = orma.selectFromMessage().
                    filetransfer_idEq(filetransfer_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().toList();

            if (m.size() == 0)
            {
                return -1;
            }

            return m.get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static long get_filetransfer_id_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            // Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:friend_number=" + friend_number + " file_number=" + file_number);
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    toList().
                    get(0).id;
            // Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:ft_id=" + ft_id);

            // ----- DEBUG -----
            //            try
            //            {
            //                Filetransfer ft_tmp = orma.selectFromFiletransfer().idEq(ft_id).get(0);
            //                //if (ft_tmp.kind != TOX_FILE_KIND_AVATAR.value)
            //                //{
            //                Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:ft full=" + ft_tmp);
            //                //}
            //            }
            //            catch (Exception e)
            //            {
            //                e.printStackTrace();
            //                Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE2:" + e.getMessage());
            //            }
            // ----- DEBUG -----

            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static void delete_filetransfer_tmpfile(long friend_number, long file_number)
    {
        try
        {
            delete_filetransfer_tmpfile(orma.selectFromFiletransfer().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friend_number)).and().file_numberEq(file_number).get(0).id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfer_tmpfile(long filetransfer_id)
    {
        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().idEq(filetransfer_id).get(0);
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(
                        VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
                f1.delete();
            }
            else
            {
                java.io.File f1 = new java.io.File(
                        VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
                f1.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;

            Log.i(TAG,
                  "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
                  " file_number=" + file_number);

            set_message_state_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            orma.updateMessage().idEq(message_id).state(state).execute();
            Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_start_sending_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_sending_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;

            Log.i(TAG,
                  "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
                  " file_number=" + file_number);


            set_message_filedb_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long message_id, long filedb_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).filedb_id(filedb_id).execute();
            Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_filetransfer_state_from_id(long filetransfer_id, int state)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).state(state).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_start_sending_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    public static void set_filetransfer_accepted_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static long get_filetransfer_filenum_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).get(0).file_number;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static void set_filetransfer_for_message_from_friendnum_and_filenum(long friend_number, long file_number, long ft_id)
    {
        try
        {
            set_filetransfer_for_message_from_filetransfer_id(orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id, ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_for_message_from_filetransfer_id(long filetransfer_id, long ft_id)
    {
        try
        {
            orma.updateMessage().filetransfer_idEq(filetransfer_id).filetransfer_id(ft_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            long del_ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;

            // Log.i(TAG, "delete_ft:id=" + del_ft_id);
            delete_filetransfers_from_id(del_ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_id(long filetransfer_id)
    {
        try
        {
            Log.i(TAG, "delete_ft:id=" + filetransfer_id);
            orma.deleteFromFiletransfer().idEq(filetransfer_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String tox_friend_get_public_key__wrapper(long friend_number)
    {
        if (cache_fnum_pubkey.containsKey(friend_number))
        {
            // Log.i(TAG, "cache hit:2");
            return cache_fnum_pubkey.get(friend_number);
        }
        else
        {
            if (cache_fnum_pubkey.size() >= 20)
            {
                // TODO: bad!
                cache_fnum_pubkey.clear();
            }
            String result = tox_friend_get_public_key(friend_number);
            cache_fnum_pubkey.put(friend_number, result);
            return result;
        }
    }

    public static String tox_conference_peer_get_public_key__wrapper(long conference_number, long peer_number)
    {
        if (cache_peernum_pubkey.containsKey("" + conference_number + ":" + peer_number))
        {
            // Log.i(TAG, "cache hit:2");
            return cache_peernum_pubkey.get("" + conference_number + ":" + peer_number);
        }
        else
        {
            if (cache_peernum_pubkey.size() >= 100)
            {
                // TODO: bad!
                cache_peernum_pubkey.clear();
            }
            String result = tox_conference_peer_get_public_key(conference_number, peer_number);
            cache_peernum_pubkey.put("" + conference_number + ":" + peer_number, result);
            return result;
        }
    }

    public static String tox_conference_peer_get_name__wrapper(String conference_identifier, String peer_pubkey)
    {
        if (cache_peername_pubkey2.containsKey("" + conference_identifier + ":" + peer_pubkey))
        {
            // Log.i(TAG, "cache hit:2b");
            return cache_peername_pubkey2.get("" + conference_identifier + ":" + peer_pubkey);
        }
        else
        {
            if (cache_peername_pubkey2.size() >= 100)
            {
                // TODO: bad!
                cache_peername_pubkey2.clear();
            }

            long conf_num = get_conference_num_from_confid(conference_identifier);
            long peer_num = get_peernum_from_peer_pubkey(conference_identifier, peer_pubkey);

            if ((conf_num > -1) && (peer_num > -1))
            {
                String result = tox_conference_peer_get_name(conf_num, peer_num);

                if (result.equals("-1"))
                {
                    return null;
                }
                else
                {
                    cache_peername_pubkey2.put("" + conference_identifier + ":" + peer_pubkey, result);
                    return result;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public void show_add_friend(View view)
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        // intent.putExtra("key", value);
        startActivityForResult(intent, AddFriendActivity_ID);
    }

    static void cancel_filetransfer(long friend_number, long file_number)
    {
        Log.i(TAG, "FTFTFT:cancel_filetransfer");

        Filetransfer f = null;
        try
        {
            f = orma.selectFromFiletransfer().
                    file_numberEq(file_number).
                    and().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    toList().get(0);

            if (f.direction == TRIFA_FT_DIRECTION_INCOMING.value)
            {
                if (f.kind == TOX_FILE_KIND_DATA.value)
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);

                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // set state for FT in message
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                    // update UI
                    // TODO: updates all messages, this is bad
                    // update_all_messages_global(false);
                    try
                    {
                        if (f.id != -1)
                        {
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
            else // outgoing FT
            {
                if (f.kind == TOX_FILE_KIND_DATA.value)
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);

                    // set state for FT in message
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:OGFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                    // update UI
                    try
                    {
                        if (f.id != -1)
                        {
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);

                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    if (msg_id > -1)
                    {
                        set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    }
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:OGFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_filetransfer_db_fos_open(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                and().
                file_numberEq(f.file_number).
                fos_open(f.fos_open).
                execute();
    }

    static void update_filetransfer_db_current_position(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                and().
                file_numberEq(f.file_number).
                current_position(f.current_position).
                execute();
    }

    static void update_filetransfer_db_full(final Filetransfer f)
    {
        orma.updateFiletransfer().
                idEq(f.id).
                tox_public_key_string(f.tox_public_key_string).
                direction(f.direction).
                file_number(f.file_number).
                kind(f.kind).
                state(f.state).
                path_name(f.path_name).
                message_id(f.message_id).
                file_name(f.file_name).
                fos_open(f.fos_open).
                filesize(f.filesize).
                current_position(f.current_position).
                execute();
    }

    static void update_filetransfer_db_full_from_id(final Filetransfer f, long fid)
    {
        orma.updateFiletransfer().
                idEq(fid).
                tox_public_key_string(f.tox_public_key_string).
                direction(f.direction).
                file_number(f.file_number).
                kind(f.kind).
                state(f.state).
                path_name(f.path_name).
                message_id(f.message_id).
                file_name(f.file_name).
                fos_open(f.fos_open).
                filesize(f.filesize).
                current_position(f.current_position).
                execute();
    }

    static void update_filetransfer_db_messageid_from_id(final Filetransfer f, long fid)
    {
        orma.updateFiletransfer().
                idEq(fid).
                message_id(f.message_id).
                execute();
    }

    static long insert_into_filetransfer_db(final Filetransfer f)
    {
        //Thread t = new Thread()
        //{
        //    @Override
        //    public void run()
        //    {
        try
        {
            long row_id = orma.insertIntoFiletransfer(f);
            Log.i(TAG, "insert_into_filetransfer_db:row_id=" + row_id);

            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM Filetransfer where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_filetransfer_db:id res count=" + cursor.getColumnCount());
            long ft_id = cursor.getLong(0);
            cursor.close();

            Log.i(TAG, "insert_into_filetransfer_db:ft_id=" + ft_id);
            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            return -1;
        }
        //    }
        //};
        //t.start();
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

    static void set_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        try
        {
            boolean avatar_filesize_non_zero = false;
            info.guardianproject.iocipher.File f1 = null;
            try
            {
                f1 = new info.guardianproject.iocipher.File(avatar_path_name + "/" + avatar_file_name);
                if (f1.length() > 0)
                {
                    avatar_filesize_non_zero = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Log.i(TAG, "set_friend_avatar:update:pubkey=" + friend_pubkey.substring(0,4) + " path=" + avatar_path_name + " file=" +
            // avatar_file_name);

            if (avatar_filesize_non_zero)
            {
                orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                        avatar_pathname(avatar_path_name).
                        avatar_filename(avatar_file_name).
                        execute();
            }
            else
            {
                orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                        avatar_pathname(null).
                        avatar_filename(null).
                        execute();
            }

            update_display_friend_avatar(friend_pubkey, avatar_path_name, avatar_file_name);
        }
        catch (Exception e)
        {
            Log.i(TAG, "set_friend_avatar:EE:" + e.getMessage());
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
            String hashtext = bigInt.toString(16);

            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }

            ret = hashtext;
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

    static long insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        // Thread t = new Thread()
        //{
        //    @Override
        //    public void run()
        //    {
        Log.i(TAG, "insert_into_message_db:m=" + m);
        long row_id = orma.insertIntoMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM Message where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_message_view_flag)
            {
                Log.i(TAG, "insert_into_message_db:add_single_message_from_messge_id, force=true");
                add_single_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            Log.i(TAG, "insert_into_message_db:EE:" + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        //    }
        //};
        //t.start();
    }

    static long insert_into_conference_message_db_system_message(final ConferenceMessage m, final boolean update_conference_view_flag)
    {
        long row_id = orma.insertIntoConferenceMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery(
                    "SELECT id FROM ConferenceMessage where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_conference_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_conference_view_flag)
            {
                if (PREF__conference_show_system_messages)
                {
                    add_single_conference_message_from_messge_id(msg_id, true);
                }
            }

            return msg_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static long insert_into_conference_message_db(final ConferenceMessage m, final boolean update_conference_view_flag)
    {
        long row_id = orma.insertIntoConferenceMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery(
                    "SELECT id FROM ConferenceMessage where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_conference_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_conference_view_flag)
            {
                add_single_conference_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
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
                    tox_friend_get_public_key__wrapper(friendnum)).toList().get(0);

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

    synchronized static void insert_into_friendlist_db(final FriendList f)
    {
        //        Thread t = new Thread()
        //        {
        //            @Override
        //            public void run()
        //            {
        try
        {
            if (orma.selectFromFriendList().tox_public_key_stringEq(f.tox_public_key_string).count() == 0)
            {
                orma.insertIntoFriendList(f);
                // Log.i(TAG, "friend added to DB: " + f.tox_public_key_string);
            }
            else
            {
                // friend already in DB
                // Log.i(TAG, "friend already in DB: " + f.tox_public_key_string);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "friend added to DB:EE:" + e.getMessage());
        }
        //            }
        //        };
        //        t.start();
    }

    static void delete_friend_all_files(final long friendnum)
    {

        try
        {
            Iterator<FileDB> i1 = orma.selectFromFileDB().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friendnum)).
                    directionEq(TRIFA_FT_DIRECTION_INCOMING.value).
                    is_in_VFSEq(true).
                    toList().iterator();
            selected_messages.clear();
            selected_messages_text_only.clear();
            selected_messages_incoming_file.clear();
            while (i1.hasNext())
            {
                try
                {
                    long file_id = i1.next().id;
                    long msg_id = orma.selectFromMessage().filedb_idEq(file_id).directionEq(0).
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).get(0).id;
                    selected_messages.add(msg_id);
                    selected_messages_incoming_file.add(msg_id);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }
            delete_selected_messages(main_activity_s, false, false, "deleting Messages ...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            orma.deleteFromFileDB().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_filetransfers(final long friendnum)
    {
        try
        {
            Log.i(TAG, "delete_ft:ALL for friend=" + friendnum);
            orma.deleteFromFiletransfer().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_messages(final long friendnum)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
            }
        };
        t.start();
    }

    static void delete_conference(final String conference_id)
    {
        try
        {
            Log.i(TAG, "delete_conference:del");
            orma.deleteFromConferenceDB().conference_identifierEq(conference_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_conference:EE:" + e.getMessage());
        }
    }

    static void delete_conference_all_messages(final String conference_id)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "delete_conference_all_messages:del");
                    orma.deleteFromConferenceMessage().conference_identifierEq(conference_id).execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "delete_conference_all_messages:EE:" + e.getMessage());
                }
            }
        };
        t.start();
    }

    static void delete_friend(final String friend_pubkey)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        execute();
            }
        };
        t.start();
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

                add_friend_real(friend_tox_id);
            }
            else
            {
                // (resultCode == RESULT_CANCELED)
            }
        }
    }

    static void add_friend_real(String friend_tox_id)
    {
        // Log.i(TAG, "add_friend_real:add friend ID:" + friend_tox_id);

        // add friend ---------------
        long friendnum = tox_friend_add(friend_tox_id, "please add me"); // add friend
        Log.i(TAG, "add_friend_real:add friend  #:" + friendnum);
        update_savedata_file_wrapper(); // save toxcore datafile (new friend added)

        if (friendnum > -1)
        {
            // nospam=8 chars, checksum=4 chars
            String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
            // Log.i(TAG, "add_friend_real:add friend PK:" + friend_public_key);

            FriendList f = new FriendList();
            f.tox_public_key_string = friend_public_key;
            try
            {
                // set name as the last 5 char of TOXID (until we get a name sent from friend)
                f.name = friend_public_key.substring(friend_public_key.length() - 5, friend_public_key.length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                f.name = "Unknown";
            }
            f.TOX_USER_STATUS = 0;
            f.TOX_CONNECTION = 0;
            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
            f.avatar_filename = null;
            f.avatar_pathname = null;

            try
            {
                insert_into_friendlist_db(f);
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }

            try
            {
                if (friend_list_fragment != null)
                {
                    Log.i(TAG, "add_friend_real:add:001:friendnum=" + friendnum);
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = true;
                    cc.friend_item = f;
                    friend_list_fragment.modify_friend(cc, cc.is_friend);
                    Log.i(TAG, "add_friend_real:add:002:friendnum=" + friendnum);
                }
                else
                {
                    Log.i(TAG, "add_friend_real:add:003:no friend_list_fragment yet!!");
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_friend_real:EE1:" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (friendnum == -1)
        {
            Log.i(TAG, "add_friend_real:friend already added, or request already sent");
        }
        // add friend ---------------
    }

    static long get_peernum_from_peer_pubkey(String conference_id, String peer_pubkey)
    {
        try
        {
            long conf_num = get_conference_num_from_confid(conference_id);
            long num_peers = tox_conference_peer_count(conf_num);

            if (num_peers > 0)
            {
                int i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    String pubkey_try = tox_conference_peer_get_public_key(conf_num, i);
                    if (pubkey_try != null)
                    {
                        if (pubkey_try.equals(peer_pubkey))
                        {
                            // we found the peer number
                            return i;
                        }
                    }
                }
            }

            return -1;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    static String get_peer_name_from_conf_id(String conference_id, String peer_pubkey)
    {
        String result = "Unknown Peer";

        return result;
    }

    static String get_peer_name_from_num(String conference_id, String peer_pubkey)
    {
        String result = "Unknown Peer";

        return result;
    }

    static long get_conference_num_from_confid(String conference_id)
    {
        try
        {
            return orma.selectFromConferenceDB().
                    conference_activeEq(true).and().
                    conference_identifierEq(conference_id).get(0).tox_conference_number;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    static String get_conference_title_from_confid(String conference_id)
    {
        try
        {
            // try in the database
            String name = orma.selectFromConferenceDB().
                    conference_identifierEq(conference_id).get(0).name;

            if ((name == null) || (name.equals("-1")))
            {
                Log.i(TAG, "get_conference_title_from_confid:EE:1");
            }
            else
            {
                return name;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_conference_title_from_confid:EE:2:" + e.getMessage());
        }

        try
        {
            String name = tox_conference_get_title(orma.selectFromConferenceDB().
                    conference_activeEq(true).and().
                    conference_identifierEq(conference_id).get(0).tox_conference_number);

            if ((name == null) || (name.equals("-1")))
            {
                Log.i(TAG, "get_conference_title_from_confid:Unknown Conference:1");
                name = "Unknown Conference";
            }

            try
            {
                // remember it in the Database
                orma.updateConferenceDB().
                        conference_identifierEq(conference_id).
                        name(name).execute();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "get_conference_title_from_confid:EE:3:" + e2.getMessage());
            }

            return name;
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "get_conference_title_from_confid:EE:4:" + e2.getMessage());
        }

        Log.i(TAG, "get_conference_title_from_confid:Unknown Conference:2");
        return "Unknown Conference";
    }

    static String get_friend_name_from_pubkey(String friend_pubkey)
    // get_friend_alias_name_wrapper_pubkey(String friend_pubkey)
    {
        String ret = "Unknown";
        String friend_alias_name = "";
        String friend_name = "";

        try
        {
            friend_alias_name = orma.selectFromFriendList().
                    tox_public_key_stringEq(friend_pubkey).
                    toList().get(0).alias_name;
        }
        catch (Exception e)
        {
            friend_alias_name = "";
            e.printStackTrace();
        }

        if ((friend_alias_name == null) || (friend_alias_name.equals("")))
        {
            try
            {
                friend_name = orma.selectFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        toList().get(0).name;
            }
            catch (Exception e)
            {
                friend_name = "";
                e.printStackTrace();
            }

            if ((friend_name != null) && (!friend_name.equals("")))
            {
                ret = friend_name;
            }
        }
        else
        {
            ret = friend_alias_name;
        }

        return ret;
    }


    static String get_friend_name_from_num(long friendnum)
    {
        String result = "Unknown";
        try
        {
            if (orma != null)
            {
                try
                {
                    String result_alias = orma.selectFromFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                            toList().get(0).alias_name;

                    if (result_alias != null)
                    {
                        if (result_alias.length() > 0)
                        {
                            result = result_alias;
                            return result;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                result = orma.selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        toList().get(0).name;
            }
        }
        catch (Exception e)
        {
            result = "Unknown";
            e.printStackTrace();
        }

        return result;
    }

    synchronized static boolean audio_buffer_read_write(long sample_count, int channels, long sampling_rate, boolean write)
    {
        if (write)
        {
            // Log.i(TAG, "audio_buffer_read_write:write:START");
            int j = 0;
            if (audio_in_buffer_element_count < audio_in_buffer_max_count)
            {
                if (audio_in_buffer_element_count > 0)
                {
                    for (j = 0; j < audio_in_buffer_element_count; j++)
                    {
                        audio_buffer_2[audio_in_buffer_element_count - 1 - j].rewind();
                        audio_buffer_2[audio_in_buffer_element_count - j].rewind();
                        // Log.i(TAG, "audio_play:write:buffer size src=" + audio_buffer_2[audio_in_buffer_element_count - 1 - j].limit());
                        // Log.i(TAG, "audio_play:write:buffer pos src=" + audio_buffer_2[audio_in_buffer_element_count - 1 - j].position());
                        // Log.i(TAG, "audio_play:write:buffer size dst=" + audio_buffer_2[audio_in_buffer_element_count - j].limit());
                        // Log.i(TAG, "audio_play:write:buffer pos dst=" + audio_buffer_2[audio_in_buffer_element_count - j].position());
                        // audio_buffer_2[audio_in_buffer_element_count - j].put(audio_buffer_2[audio_in_buffer_element_count - 1 - j].array());
                        audio_buffer_2[audio_in_buffer_element_count - j].put(
                                audio_buffer_2[audio_in_buffer_element_count - 1 - j].array(), 0,
                                AudioReceiver.buffer_size);
                        audio_buffer_2[audio_in_buffer_element_count - j].rewind();
                        audio_buffer_2_read_length[audio_in_buffer_element_count - j] = audio_buffer_2_read_length[
                                audio_in_buffer_element_count - 1 - j];
                        // Log.i(TAG, "audio_play:write:mv " + (audio_in_buffer_element_count - 1 - j + " -> " + (audio_in_buffer_element_count - j)));
                    }
                }
                // Log.i(TAG, "audio_play:write:set buffer 0:len=" + sample_count);
                audio_buffer_2_read_length[0] = (int) (sample_count * channels * 2);
                audio_in_buffer_element_count++;
                // Log.i(TAG, "audio_play:write:element count new=" + audio_in_buffer_element_count);
                // Log.i(TAG, "audio_play:write:element count new=" + audio_in_buffer_element_count);

                // wake up audio thread -----------
                try
                {
                    audio_thread.interrupt();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "audio_buffer_read_write:write:wake up audio thread:EE:" + e.getMessage());
                }
                // wake up audio thread -----------
            }
            else
            {
                Log.i(TAG, "audio_buffer_read_write:write:* buffer FULL *");
            }

            // Log.i(TAG, "audio_buffer_read_write:write:END");

            return true;
        }
        else // read
        {
            // Log.i(TAG, "audio_buffer_read_write:READ:START");

            if (audio_in_buffer_element_count > 0)
            {
                // Log.i(TAG, "audio_play:read:load buffer " + (audio_in_buffer_element_count - 1) + ":len=" + audio_buffer_2_read_length[audio_in_buffer_element_count - 1]);

                audio_buffer_play.rewind();
                audio_buffer_play.put(audio_buffer_2[audio_in_buffer_element_count - 1].array(), 0,
                                      AudioReceiver.buffer_size);
                audio_buffer_play_length = audio_buffer_2_read_length[audio_in_buffer_element_count - 1];
                audio_in_buffer_element_count--;
                // Log.i(TAG, "audio_play:read:element count new=" + audio_in_buffer_element_count);

                // Log.i(TAG, "audio_buffer_read_write:READ:END01");

                return true;
            }

            // Log.i(TAG, "audio_buffer_read_write:READ:END02");

            return false;
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
                                CallingActivity.ca.right_top_text_2.setText(
                                        "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay);
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

    static void get_network_connections()
    {
        if (DEBUG_show_net_connection)
        {
            Detector.updateReportMap();
            Collector.updateReports();
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
            return "*Datetime ERROR*";
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
            return "*Datetime ERROR*";
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

    static String conference_identifier_short(String conference_identifier, boolean uppercase_result)
    {
        try
        {
            //            return conference_identifier.substring(0, 2) +
            //                    //
            //                    ".." +
            //                    //
            //                    conference_identifier.substring(conference_identifier.length() - 5, conference_identifier.length());
            if (uppercase_result)
            {
                return (conference_identifier.substring(conference_identifier.length() - 6,
                                                        conference_identifier.length())).toUpperCase(Locale.ENGLISH);
            }
            else
            {
                return conference_identifier.substring(conference_identifier.length() - 6,
                                                       conference_identifier.length());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return conference_identifier;
        }
    }

    static void set_all_conferences_inactive()
    {
        try
        {
            orma.updateConferenceDB().
                    conference_active(false).
                    tox_conference_number(-1).
                    execute();
            Log.i(TAG, "set_all_conferences_inactive");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_all_conferences_inactive:EE:" + e.getMessage());
        }
    }

    static void set_conference_inactive(String conference_identifier)
    {
        try
        {
            orma.updateConferenceDB().
                    conference_identifierEq(conference_identifier).
                    conference_active(false).
                    tox_conference_number(-1).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_conference_inactive:EE:" + e.getMessage());
        }
    }

    static void new_or_updated_conference(long conference_number, String who_invited_public_key, String conference_identifier, int conference_type)
    {
        try
        {
            Log.i(TAG, "new_or_updated_conference:" + "conference_number=" + conference_identifier);

            final ConferenceDB conf2 = orma.selectFromConferenceDB().
                    conference_identifierEq(conference_identifier).toList().get(0);

            // conference already exists -> update and connect
            orma.updateConferenceDB().
                    conference_identifierEq(conference_identifier).
                    conference_active(true).
                    tox_conference_number(conference_number).execute();

            try
            {
                Log.i(TAG, "new_or_updated_conference:*update*");

                final ConferenceDB conf3 = orma.selectFromConferenceDB().
                        conference_identifierEq(conference_identifier).toList().get(0);

                // update or add to "friendlist"
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = false;
                cc.conference_item = ConferenceDB.deep_copy(conf3);
                MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_conference:EE3:" + e3.getMessage());
            }

            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_or_updated_conference:EE1:" + e.getMessage());


            // conference is new -> add
            try
            {
                ConferenceDB conf_new = new ConferenceDB();
                conf_new.conference_identifier = conference_identifier;
                conf_new.who_invited__tox_public_key_string = who_invited_public_key;
                conf_new.peer_count = -1;
                conf_new.own_peer_number = -1;
                conf_new.kind = conference_type;
                conf_new.tox_conference_number = conference_number;
                conf_new.conference_active = true;
                //
                orma.insertIntoConferenceDB(conf_new);
                Log.i(TAG, "new_or_updated_conference:+ADD+");

                try
                {
                    // update or add to "friendlist"
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = ConferenceDB.deep_copy(conf_new);
                    MainActivity.friend_list_fragment.modify_friend(cc, cc.is_friend);

                }
                catch (Exception e4)
                {
                    Log.i(TAG, "new_or_updated_conference:EE4:" + e4.getMessage());
                }

                return;

            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Log.i(TAG, "new_or_updated_conference:EE2:" + e1.getMessage());
            }
        }
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

        if (hsv[2] < 0.5)
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

    private static class delete_selected_messages_asynchtask extends AsyncTask<Void, Void, String>
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

    static void delete_selected_messages(final Context c, final boolean update_message_list, final boolean update_friend_list, final String dialog_text)
    {
        ProgressDialog progressDialog2 = null;
        try
        {
            try
            {
                //                main_activity_s.runOnUiThread(new Runnable()
                //                {
                //                    @Override
                //                    public void run()
                //                    {
                //                        try
                //                        {
                //                            progressDialog2 = ProgressDialog.show(c, "", "deleting Messages ...");
                //                            progressDialog2.setCanceledOnTouchOutside(false);
                //                            progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                //                            {
                //                                @Override
                //                                public void onCancel(DialogInterface dialog)
                //                                {
                //                                }
                //                            });
                //                        }
                //                        catch (Exception e)
                //                        {
                //                            e.printStackTrace();
                //                            Log.i(TAG, "CALL:start:EE:" + e.getMessage());
                //                        }
                //                    }
                //                });

                //                if (main_handler_s != null)
                //                {
                //                    main_handler_s.post(myRunnable);
                //                }
            }
            catch (Exception e3)
            {
                e3.printStackTrace();
                Log.i(TAG, "delete_selected_messages:EE1:" + e3.getMessage());
            }
            new delete_selected_messages_asynchtask(c, progressDialog2, update_message_list, update_friend_list,
                                                    dialog_text).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_selected_messages:EE2:" + e.getMessage());
        }
    }

    static void copy_selected_messages(Context c)
    {
        try
        {
            if (!selected_messages_text_only.isEmpty())
            {
                // sort ascending (lowest ID on top)
                Collections.sort(selected_messages_text_only, new Comparator<Long>()
                {
                    public int compare(Long o1, Long o2)
                    {
                        return o1.compareTo(o2);
                    }
                });

                String copy_text = "";
                boolean first = true;
                Iterator i = selected_messages_text_only.iterator();
                while (i.hasNext())
                {
                    try
                    {
                        if (first)
                        {
                            first = false;
                            copy_text = "" + orma.selectFromMessage().idEq((Long) i.next()).get(0).text;
                        }
                        else
                        {
                            copy_text = copy_text + "\n" + orma.selectFromMessage().idEq((Long) i.next()).get(0).text;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                clipboard.setPrimaryClip(ClipData.newPlainText("", copy_text));
                Toast.makeText(c, "copied to Clipboard", Toast.LENGTH_SHORT).show();

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
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    static void copy_selected_conference_messages(Context c)
    {
        try
        {
            if (!selected_conference_messages.isEmpty())
            {
                // sort ascending (lowest ID on top)
                Collections.sort(selected_conference_messages, new Comparator<Long>()
                {
                    public int compare(Long o1, Long o2)
                    {
                        return o1.compareTo(o2);
                    }
                });

                String copy_text = "";
                boolean first = true;
                Iterator i = selected_conference_messages.iterator();
                while (i.hasNext())
                {
                    try
                    {
                        if (first)
                        {
                            first = false;
                            copy_text = "" + orma.selectFromConferenceMessage().idEq((Long) i.next()).get(0).text;
                        }
                        else
                        {
                            copy_text = copy_text + "\n" +
                                        orma.selectFromConferenceMessage().idEq((Long) i.next()).get(0).text;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                clipboard.setPrimaryClip(ClipData.newPlainText("", copy_text));
                Toast.makeText(c, "copied to Clipboard", Toast.LENGTH_SHORT).show();

                selected_conference_messages.clear();

                try
                {
                    // need to redraw all items again here, to remove the selections
                    MainActivity.conference_message_list_fragment.adapter.redraw_all_items();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    static void delete_selected_conference_messages(Context c)
    {
        // TODO: write me!
        try
        {
            if (!selected_conference_messages.isEmpty())
            {
                // sort ascending (lowest ID on top)
                Collections.sort(selected_conference_messages, new Comparator<Long>()
                {
                    public int compare(Long o1, Long o2)
                    {
                        return o1.compareTo(o2);
                    }
                });

                String copy_text = "";
                boolean first = true;
                Iterator i = selected_conference_messages.iterator();
                while (i.hasNext())
                {
                    try
                    {
                        // TODO: write me
                        i.next();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                selected_conference_messages.clear();

                try
                {
                    // need to redraw all items again here, to remove the selections
                    MainActivity.conference_message_list_fragment.adapter.redraw_all_items();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    private static class save_selected_messages_asynchtask extends AsyncTask<Void, Void, String>
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

    static class send_message_result
    {
        long msg_num;
        boolean msg_v2;
        String msg_hash_hex;
        String raw_message_buf_hex;
    }

    public static send_message_result tox_friend_send_message_wrapper(long friendnum, int a_TOX_MESSAGE_TYPE, @NonNull String message)
    {
        send_message_result result = new send_message_result();

        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
        ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);

        // use msg V2 API Call
        long t_sec = (System.currentTimeMillis() / 1000);
        long res = tox_util_friend_send_message_v2(friendnum, a_TOX_MESSAGE_TYPE, t_sec, message, message.length(),
                                                   raw_message_buf, raw_message_length_buf, msg_id_buffer);
        int raw_message_length_int = raw_message_length_buf.
                array()[raw_message_length_buf.arrayOffset()] & 0xFF + (raw_message_length_buf.
                array()[raw_message_length_buf.arrayOffset() + 1] & 0xFF) * 256;

        // Log.i(TAG,
        //      "tox_friend_send_message_wrapper:message=" + message + " res=" + res + " len=" + raw_message_length_int);

        if (res == -9999)
        {
            // msg V2 OK
            result.msg_num = (Long.MAX_VALUE - 1);
            result.msg_v2 = true;
            result.msg_hash_hex = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(), msg_id_buffer.limit());
            result.raw_message_buf_hex = bytesToHex(raw_message_buf.array(), raw_message_buf.arrayOffset(),
                                                    raw_message_length_int);

            // Log.i(TAG, "tox_friend_send_message_wrapper:hash_hex=" + result.msg_hash_hex + " raw_msg_hex" +
            //           result.raw_message_buf_hex);

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

    static void save_selected_messages(Context c)
    {
        ProgressDialog progressDialog2 = null;
        try
        {
            try
            {
                progressDialog2 = ProgressDialog.show(c, "", "exporting Messages ...");
                progressDialog2.setCanceledOnTouchOutside(false);
                progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                    }
                });
            }
            catch (Exception e3)
            {
                e3.printStackTrace();
                Log.i(TAG, "save_selected_messages:EE1:" + e3.getMessage());
            }
            new save_selected_messages_asynchtask(c, progressDialog2).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "save_selected_messages:EE2:" + e.getMessage());
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
        update_savedata_file_wrapper();
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

    static String resolve_name_for_pubkey(String pub_key, String default_name)
    {
        String ret = default_name;

        try
        {
            try
            {
                if (pub_key.equals(global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2))))
                {
                    // its our own key
                    ret = global_my_name;
                    return ret;
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }

            FriendList fl = orma.selectFromFriendList().
                    tox_public_key_stringEq(pub_key).
                    toList().get(0);

            if (fl.name != null)
            {
                if (fl.name.length() > 0)
                {
                    ret = fl.name;
                }
            }

            if (fl.alias_name != null)
            {
                if (fl.alias_name.length() > 0)
                {
                    ret = fl.alias_name;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret = default_name;
        }

        return ret;
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

