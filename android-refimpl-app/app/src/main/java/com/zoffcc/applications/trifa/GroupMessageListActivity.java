/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2022 Zoff <zoff@zoff.cc>
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

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.speech.levelmeter.BarLevelDrawable;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.impl.utils.CompareSizesByArea;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicYuvToRGB;
import androidx.renderscript.Type;

import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_in;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_out;
import static com.zoffcc.applications.nativeaudio.NativeAudio.na_set_audio_play_volume_percent;
import static com.zoffcc.applications.trifa.AudioReceiver.channels_;
import static com.zoffcc.applications.trifa.AudioReceiver.sampling_rate_;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.CameraWrapper.YUV420rotate90;
import static com.zoffcc.applications.trifa.GroupMessageListFragment.group_search_messages_text;
import static com.zoffcc.applications.trifa.HelperFiletransfer.copy_outgoing_file_to_sdcard_dir;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytebuffer_to_hexstring;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.display_toast;
import static com.zoffcc.applications.trifa.HelperGeneric.do_fade_anim_on_fab;
import static com.zoffcc.applications.trifa.HelperGeneric.fourbytes_of_long_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.set_calling_audio_mode;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.get_group_peernum_from_peer_pubkey;
import static com.zoffcc.applications.trifa.HelperGroup.insert_into_group_message_db;
import static com.zoffcc.applications.trifa.HelperGroup.is_group_active;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_get_index_video_incoming_peer_list;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_purge_video_incoming_peer_list;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_set_video_call_icon;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_set_video_info_text;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_update_video_incoming_peer_list;
import static com.zoffcc.applications.trifa.HelperGroup.ngc_update_video_incoming_peer_list_ts;
import static com.zoffcc.applications.trifa.HelperGroup.send_group_image;
import static com.zoffcc.applications.trifa.HelperGroup.shrink_image_file;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_peer_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.MainActivity.PREF__ngc_video_bitrate;
import static com.zoffcc.applications.trifa.MainActivity.PREF__ngc_video_frame_delta_ms;
import static com.zoffcc.applications.trifa.MainActivity.PREF__ngc_video_max_quantizer;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_incognito_keyboard;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.audio_out_buffer_mult;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_text_only;
import static com.zoffcc.applications.trifa.MainActivity.set_audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_invite_friend;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_role;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_savedpeer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_peer_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_custom_packet;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_audio_decode;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_audio_encode;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_video_decode;
import static com.zoffcc.applications.trifa.MainActivity.toxav_ngc_video_encode;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_OUTGOING_FILESIZE_BYTE_USE_STORAGE_FRAMEWORK;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_OUTGOING_FILESIZE_NGC_MAX_TOTAL;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HIGHER_NGC_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HIGHER_NGC_VIDEO_QUANTIZER;
import static com.zoffcc.applications.trifa.TRIFAGlobals.INTERVAL_UPDATE_NGC_GROUP_ALL_USERS_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_QUANTIZER;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NGC_AUDIO_PCM_BUFFER_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NGC_AUDIO_PCM_BUFFER_SAMPLES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NGC_NEW_PEERS_TIMEDELTA_IN_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_REMOVE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_1;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.ToxVars.GC_MAX_SAVED_PEERS;
import static com.zoffcc.applications.trifa.ToxVars.MAX_GC_PACKET_CHUNK_SIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_VIDEO_AND_HEADER_SIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static com.zoffcc.applications.trifa.TrifaToxService.wakeup_tox_thread;

public class GroupMessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.GrpMsgLstActivity";
    static String group_id = "-1";
    String group_id_prev = "-1";
    //
    static com.vanniktech.emoji.EmojiEditText ml_new_group_message = null;
    EmojiPopup emojiPopup = null;
    ImageView insert_emoji = null;
    TextView ml_maintext = null;
    ViewGroup rootView = null;
    //
    private static Thread NGC_Group_video_check_incoming_thread = null;
    private static boolean NGC_Group_video_check_incoming_thread_running = false;
    static Thread NGC_Group_video_play_thread = null;
    static Thread NGC_Group_audio_record_thread = null;
    private static boolean NGC_Group_video_play_thread_running = false;
    private static boolean NGC_Group_audio_record_thread_running = false;
    static Map<String, Long> lookup_ngc_incoming_video_peer_list = new HashMap<String, Long>();
    static int ngc_incoming_video_peer_toggle_current_index = 0;
    static int flush_decoder = 0;
    static long last_video_seq_num = -1;
    static BarLevelDrawable ngc_audio_bar_in_v = null;
    static BarLevelDrawable ngc_audio_bar_out_v = null;
    //
    static GroupGroupAudioService ngc_group_audio_service = null;
    public static String ngc_channelId = "";
    static NotificationChannel ngc_notification_channel_group_audio_play_service = null;
    static NotificationManager ngc_nmn3 = null;
    static BlockingQueue<byte[]> ngc_audio_in_queue = new LinkedBlockingQueue<byte[]>(3 * 5);
    static BlockingQueue<byte[]> ngc_audio_out_queue = new LinkedBlockingQueue<byte[]>(3 * 5);
    //
    ImageView ml_icon = null;
    ImageView ml_status_icon = null;
    static View ngc_video_view_container = null;
    static CustomVideoImageView ngc_video_view = null;
    static CustomVideoImageView ngc_video_own_view = null;
    ImageButton ngc_camera_toggle_button = null;
    ImageButton ngc_camera_next_button = null;
    ImageButton ngc_video_quality_toggle_button = null;
    ImageButton ngc_mute_button = null;
    ImageButton ngc_video_off_button = null;
    static TextView ngc_camera_info_text = null;
    static final int NGC_FRONT_CAMERA_USED = 1;
    static final int NGC_BACK_CAMERA_USED = 2;
    static int ngc_active_camera_type = NGC_BACK_CAMERA_USED;
    static boolean ngc_audio_mute = true;
    static boolean ngc_video_off = true;
    static final int NGC_VIDEO_ICON_STATE_INACTIVE = 0;
    static final int NGC_VIDEO_ICON_STATE_INCOMING = 1;
    static final int NGC_VIDEO_ICON_STATE_ACTIVE = 2;
    ImageButton ml_phone_icon = null;
    ImageButton ml_button_01 = null;
    static ImageButton ml_video_icon = null;
    static boolean sending_video_to_group = false;
    static String ngc_video_showing_video_from_peer_pubkey = "-1";
    static long ngc_video_frame_last_incoming_ts = -1L;
    static long ngc_video_packet_last_incoming_ts = -1L;
    static long ngc_audio_packet_last_incoming_ts = -1L;
    static Bitmap ngc_video_frame_image = null;
    static Bitmap ngc_own_video_frame_image = null;
    static Allocation ngc_alloc_in = null;
    static Allocation ngc_own_alloc_in = null;
    static Allocation ngc_alloc_out = null;
    static Allocation ngc_own_alloc_out = null;
    static ScriptIntrinsicYuvToRGB ngc_yuvToRgb = null;
    static ScriptIntrinsicYuvToRGB ngc_own_yuvToRgb = null;
    static boolean attachemnt_instead_of_send = true;
    static ActionMode amode = null;
    static MenuItem amode_save_menu_item = null;
    static MenuItem amode_info_menu_item = null;
    static boolean oncreate_finished = false;
    SearchView messageSearchView = null;
    private static long update_group_all_users_last_trigger_ts = 0;
    //
    static long last_processed_camera_frame = -1;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    // static byte[] y_buf__ = new byte[240 * 320];
    static byte[] y_buf__ = new byte[480 * 640];
    static byte[] u_buf__ = new byte[(480/2) * (640/2)];
    static byte[] v_buf__ = new byte[(480/2) * (640/2)];

    private static final int CAMERAX_NGC_IMAGE_WIDTH = 640;
    private static final int CAMERAX_NGC_IMAGE_HEIGHT = 480;
    private static final int CAMERAX_NGC_CAPTURE_MAX_IMAGES = 2;
    //


    // main drawer ----------
    Drawer group_message_drawer = null;
    AccountHeader group_message_drawer_header = null;
    ProfileDrawerItem group_message_profile_item = null;
    // long peers_in_list_next_num = 0;
    // main drawer ----------

    Handler group_handler = null;
    static Handler group_handler_s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        oncreate_finished = false;
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        // ---------- video stuff ----------
        final int ngc_frame_width_px = 480; // + 32; // 240 + 16;
        final int ngc_frame_height_px = 640; // 320;
        ngc_video_frame_image = Bitmap.createBitmap(ngc_frame_width_px, ngc_frame_height_px, Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(this);
        ngc_yuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(ngc_frame_width_px).setY(ngc_frame_height_px);
        yuvType.setYuvFormat(ImageFormat.YV12);
        ngc_alloc_in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(ngc_frame_width_px).setY(ngc_frame_height_px);
        ngc_alloc_out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        //
        //
        final int ngc_own_frame_width_px = 480;
        final int ngc_own_frame_height_px = 640;
        ngc_own_video_frame_image = Bitmap.createBitmap(ngc_own_frame_width_px, ngc_own_frame_height_px, Bitmap.Config.ARGB_8888);
        RenderScript own_rs = RenderScript.create(this);
        ngc_own_yuvToRgb = ScriptIntrinsicYuvToRGB.create(own_rs, Element.U8_4(own_rs));
        Type.Builder own_yuvType = new Type.Builder(own_rs, Element.U8(own_rs)).setX(ngc_own_frame_width_px).setY(ngc_own_frame_height_px);
        own_yuvType.setYuvFormat(ImageFormat.YV12);
        ngc_own_alloc_in = Allocation.createTyped(own_rs, own_yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder own_rgbaType = new Type.Builder(own_rs, Element.RGBA_8888(own_rs)).setX(ngc_own_frame_width_px).setY(ngc_own_frame_height_px);
        ngc_own_alloc_out = Allocation.createTyped(own_rs, own_rgbaType.create(), Allocation.USAGE_SCRIPT);
        //
        //
        sending_video_to_group = false;
        // ---------- video stuff ----------

        amode = null;
        amode_save_menu_item = null;
        amode_info_menu_item = null;
        selected_group_messages.clear();
        selected_group_messages_text_only.clear();
        selected_group_messages_incoming_file.clear();

        try
        {
            // reset search and filter flags, sooner
            group_search_messages_text = null;
        }
        catch (Exception e)
        {
        }

        group_handler = new Handler(getMainLooper());
        group_handler_s = group_handler;

        Intent intent = getIntent();
        group_id = intent.getStringExtra("group_id");
        // Log.i(TAG, "onCreate:003:conf_id=" + conf_id + " conf_id_prev=" + conf_id_prev);
        group_id_prev = group_id;

        setContentView(R.layout.activity_group_message_list);

        MainActivity.group_message_list_activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable drawer_header_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_group).color(
                getResources().getColor(R.color.md_dark_primary_text)).sizeDp(100);

        group_message_profile_item = new ProfileDrawerItem().withName("Userlist").withIcon(drawer_header_icon);

        // Create the AccountHeader
        group_message_drawer_header = new AccountHeaderBuilder().withActivity(
                this).withSelectionListEnabledForSingleProfile(false).withTextColor(
                getResources().getColor(R.color.md_dark_primary_text)).withHeaderBackground(
                R.color.colorHeader).withCompactStyle(true).addProfiles(
                group_message_profile_item).withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
        {
            @Override
            public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
            {
                return false;
            }
        }).build();

        //        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).
        //                withIdentifier(1L).
        //                withName("User1").
        //                withIcon(GoogleMaterial.Icon.gmd_face);


        // create the drawer and remember the `Drawer` result object
        group_message_drawer = new DrawerBuilder().withActivity(this).withAccountHeader(
                group_message_drawer_header).withInnerShadow(false).withRootView(
                R.id.drawer_container).withShowDrawerOnFirstLaunch(false).withActionBarDrawerToggleAnimated(
                true).withActionBarDrawerToggle(true).withToolbar(toolbar).withTranslucentStatusBar(
                false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
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
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }).build();


        rootView = (ViewGroup) findViewById(R.id.emoji_bar);
        ml_new_group_message = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.ml_new_message);

        messageSearchView = (SearchView) findViewById(R.id.group_search_view_messages);
        messageSearchView.setQueryHint(getString(R.string.messages_search_default_text));
        messageSearchView.setIconifiedByDefault(true);

        try
        {
            // reset search and filter flags
            messageSearchView.setQuery("", false);
            messageSearchView.setIconified(true);
            group_search_messages_text = null;
        }
        catch (Exception e)
        {
        }

        // give focus to text input
        ml_new_group_message.requestFocus();
        try
        {
            // hide softkeyboard initially
            // since it takes a lot of screen space
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        catch (Exception e)
        {
        }

        insert_emoji = (ImageView) findViewById(R.id.insert_emoji);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);
        ml_video_icon = (ImageButton) findViewById(R.id.ml_video_icon);
        ngc_video_view_container = findViewById(R.id.ngc_video_view_container);
        ngc_video_view = findViewById(R.id.ngc_video_view);
        ngc_video_own_view = findViewById(R.id.ngc_video_own_view);
        ngc_camera_toggle_button = (ImageButton) findViewById(R.id.ngc_camera_toggle_button);
        ngc_camera_next_button = (ImageButton) findViewById(R.id.ngc_camera_next_button);
        ngc_video_quality_toggle_button = (ImageButton) findViewById(R.id.ngc_video_quality_toggle_button);
        ngc_mute_button = (ImageButton) findViewById(R.id.ngc_mute_button);
        ngc_video_off_button = (ImageButton) findViewById(R.id.ngc_video_off_button);
        ngc_camera_info_text = findViewById(R.id.ngc_camera_info_text);
        ml_button_01 = (ImageButton) findViewById(R.id.ml_button_01);
        ngc_audio_bar_in_v = (BarLevelDrawable) findViewById(R.id.ngc_audio_bar_in_v);
        ngc_audio_bar_out_v = (BarLevelDrawable) findViewById(R.id.ngc_audio_bar_out_v);

        ngc_camera_info_text.setText("");

        ngc_nmn3 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            String channelName = "Tox NGC Group Audio Play";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            ngc_channelId = "trifa_ngc_audio_play";
            ngc_notification_channel_group_audio_play_service = new NotificationChannel(ngc_channelId, channelName, importance);
            ngc_notification_channel_group_audio_play_service.setDescription(ngc_channelId);
            ngc_notification_channel_group_audio_play_service.setSound(null, null);
            ngc_notification_channel_group_audio_play_service.enableVibration(false);
            ngc_nmn3.createNotificationChannel(ngc_notification_channel_group_audio_play_service);
        }

        final Drawable d9 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_rotate_right).backgroundColor(
                Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        ngc_camera_next_button.setImageDrawable(d9);

        // on startup always use back camera
        ngc_active_camera_type = NGC_BACK_CAMERA_USED;

        if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
        {
            final Drawable d5 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_front).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_camera_toggle_button.setImageDrawable(d5);
            Log.i(TAG, "ngc_active_camera_type(5)=" + ngc_active_camera_type);
        }
        else
        {
            final Drawable d6 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_camera_toggle_button.setImageDrawable(d6);
            Log.i(TAG, "ngc_active_camera_type(6)=" + ngc_active_camera_type);
        }

        PREF__ngc_video_bitrate = LOWER_NGC_VIDEO_BITRATE;
        if (PREF__ngc_video_bitrate == LOWER_NGC_VIDEO_BITRATE)
        {
            Drawable d2a = new IconicsDrawable(this).icon(
                    GoogleMaterial.Icon.gmd_photo_size_select_small).backgroundColor(Color.TRANSPARENT).color(
                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
            ngc_video_quality_toggle_button.setImageDrawable(d2a);
        }
        else
        {
            Drawable d2a = new IconicsDrawable(this).icon(
                    GoogleMaterial.Icon.gmd_hd).backgroundColor(Color.TRANSPARENT).color(
                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
            ngc_video_quality_toggle_button.setImageDrawable(d2a);
        }

        ngc_camera_toggle_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    Log.i(TAG, "active_camera_type(7)=" + ngc_active_camera_type);

                    if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_camera_toggle_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    Log.i(TAG, "ngc_active_camera_type(8)=" + ngc_active_camera_type);

                    if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_camera_toggle_button.setImageDrawable(d2a);
                    }

                    try
                    {
                        if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
                        {
                            ngc_active_camera_type = NGC_BACK_CAMERA_USED;
                            Log.i(TAG, "ngc_active_camera_type(8a)=" + ngc_active_camera_type);
                        }
                        else
                        {
                            ngc_active_camera_type = NGC_FRONT_CAMERA_USED;
                            Log.i(TAG, "ngc_active_camera_type(8b)=" + ngc_active_camera_type);
                        }
                        closeCamera();

                        if (ngc_video_off == false)
                        {
                            openCamera();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        ngc_camera_next_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                            GoogleMaterial.Icon.gmd_rotate_right).backgroundColor(Color.TRANSPARENT).color(
                            getResources().getColor(R.color.md_green_600)).sizeDp(7);
                    ngc_camera_next_button.setImageDrawable(d2a);
                }
                else
                {
                    Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                            GoogleMaterial.Icon.gmd_rotate_right).backgroundColor(Color.TRANSPARENT).color(
                            getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                    ngc_camera_next_button.setImageDrawable(d2a);

                    try
                    {
                        // toggle to next ngc peer with incoming video
                        if (ngc_incoming_video_peer_toggle_current_index < 1000)
                        {
                            ngc_incoming_video_peer_toggle_current_index++;
                        }
                        else
                        {
                            ngc_incoming_video_peer_toggle_current_index = 0;
                        }
                        flush_decoder = 1;
                        ngc_video_showing_video_from_peer_pubkey = ngc_get_index_video_incoming_peer_list(ngc_incoming_video_peer_toggle_current_index);
                        ngc_video_frame_last_incoming_ts = System.currentTimeMillis();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        // on startup always mute mic
        ngc_audio_mute = true;
        ngc_audio_bar_in_v.setLevel(0);

        if (ngc_audio_mute == true)
        {
            final Drawable d5 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic_off).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_mute_button.setImageDrawable(d5);
        }
        else
        {
            final Drawable d6 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_mute_button.setImageDrawable(d6);
        }

        ngc_mute_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    if (ngc_audio_mute == true)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_mute_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_mute_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    if (ngc_audio_mute == true)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_mute_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_mute_button.setImageDrawable(d2a);
                    }

                    try
                    {
                        if (ngc_audio_mute == true)
                        {
                            ngc_audio_mute = false;
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
                        }
                        else
                        {
                            ngc_audio_mute = true;
                            ngc_audio_bar_in_v.setLevel(0);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        ngc_video_off = true;
        if (ngc_video_off == true)
        {
            final Drawable d5 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_videocam_off).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_video_off_button.setImageDrawable(d5);
        }
        else
        {
            final Drawable d6 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_videocam).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            ngc_video_off_button.setImageDrawable(d6);
        }

        ngc_video_off_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    if (ngc_video_off == true)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_videocam_off).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_video_off_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_videocam).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_video_off_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    if (ngc_video_off == true)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_videocam).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_video_off_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_videocam_off).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_video_off_button.setImageDrawable(d2a);
                    }

                    try
                    {
                        if (ngc_video_off == true)
                        {
                            ngc_video_off = false;
                            try
                            {
                                openCamera();
                            }
                            catch(Exception e)
                            {
                            }
                        }
                        else
                        {
                            ngc_video_off = true;
                            try
                            {
                                closeCamera();
                                // clear preview View
                                final Bitmap empty_bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
                                ngc_video_own_view.setBitmap(empty_bitmap);
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        ngc_video_quality_toggle_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    if (PREF__ngc_video_bitrate == LOWER_NGC_VIDEO_BITRATE)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_photo_size_select_small).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_video_quality_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_hd).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        ngc_video_quality_toggle_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    if (PREF__ngc_video_bitrate == LOWER_NGC_VIDEO_BITRATE)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_hd).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_video_quality_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_photo_size_select_small).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        ngc_video_quality_toggle_button.setImageDrawable(d2a);
                    }

                    try
                    {
                        if (PREF__ngc_video_bitrate == LOWER_NGC_VIDEO_BITRATE)
                        {
                            PREF__ngc_video_bitrate = HIGHER_NGC_VIDEO_BITRATE;
                            PREF__ngc_video_max_quantizer = HIGHER_NGC_VIDEO_QUANTIZER;
                            Log.i(TAG, "PREF__ngc_video_bitrate(8a)=" + PREF__ngc_video_bitrate);
                        }
                        else
                        {
                            PREF__ngc_video_bitrate = LOWER_NGC_VIDEO_BITRATE;
                            PREF__ngc_video_max_quantizer = LOWER_NGC_VIDEO_QUANTIZER;
                            Log.i(TAG, "PREF__ngc_video_bitrate(8b)=" + PREF__ngc_video_bitrate);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        ml_phone_icon.setVisibility(View.GONE);
        ml_status_icon.setVisibility(View.INVISIBLE);

        final Drawable d3 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_video).color(
                getResources().getColor(R.color.icon_colors)).sizeDp(80);
        ml_video_icon.setImageDrawable(d3);
        ngc_video_view_container.setVisibility(View.GONE);

        ml_icon.setImageResource(R.drawable.circle_red);
        set_group_connection_status_icon();

        messageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // Log.i(TAG, "search:1:" + query);

                if ((query == null) || (query.length() == 0))
                {
                    try
                    {
                        // all messages
                        group_search_messages_text = null;
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        // all messages and search string
                        group_search_messages_text = query;
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query)
            {
                // Log.i(TAG, "search:2:" + query);

                if ((query == null) || (query.length() == 0))
                {
                    try
                    {
                        // all messages
                        group_search_messages_text = null;
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        // all messages and search string
                        group_search_messages_text = query;
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                return true;
            }
        });

        setUpEmojiPopup();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(
                GoogleMaterial.Icon.gmd_sentiment_satisfied).color(getResources().getColor(R.color.icon_colors)).sizeDp(
                80);

        insert_emoji.setImageDrawable(d1);

        insert_emoji.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                emojiPopup.toggle();
            }
        });

        // final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(
                getResources().getColor(R.color.icon_colors)).sizeDp(80);

        attachemnt_instead_of_send = true;
        ml_button_01.setImageDrawable(send_message_icon);

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(
                getResources().getColor(R.color.icon_colors)).sizeDp(80);
        ml_phone_icon.setImageDrawable(d2);

        if (PREF__use_incognito_keyboard)
        {
            ml_new_group_message.setImeOptions(
                    EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }
        else
        {
            ml_new_group_message.setImeOptions(EditorInfo.IME_ACTION_SEND);
        }

        if (PREF__window_security)
        {
            // prevent screenshots and also dont show the window content in recent activity screen
            initializeScreenshotSecurity(this);
        }

        set_peer_count_header();
        set_peer_names_and_avatars();

        Log.i(TAG, "onCreate:099");
        oncreate_finished = true;
    }

    synchronized void set_peer_count_header()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final long conference_num = tox_group_by_groupid__wrapper(group_id);
                String group_topic = tox_group_get_name(conference_num);
                if (group_topic == null)
                {
                    group_topic = "";
                }
                final String f_name = group_topic;

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long peer_count = tox_group_peer_count(conference_num);
                            long frozen_peer_count = tox_group_offline_peer_count(conference_num);

                            if (peer_count > -1)
                            {
                                ml_maintext.setText(
                                        f_name + "\n" + getString(R.string.GroupActivityActive) + " " + peer_count +
                                        " " + getString(R.string.GroupActivityOffline) + " " + frozen_peer_count);
                            }
                            else
                            {
                                ml_maintext.setText(f_name);
                                // ml_maintext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            }
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

    static class group_list_peer
    {
        String peer_name;
        long peer_num;
        String peer_pubkey;
        int peer_connection_status;
        boolean self;
    }

    synchronized void set_peer_names_and_avatars()
    {
        try
        {
            remove_group_all_users();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.d(TAG, "set_peer_names_and_avatars:002");

        final long conference_num = tox_group_by_groupid__wrapper(group_id);
        long num_peers = tox_group_peer_count(conference_num);
        final long self_peer_id = tox_group_self_get_peer_id(conference_num);

        if (num_peers > 0)
        {
            long[] peers = tox_group_get_peerlist(conference_num);
            if (peers != null)
            {
                List<group_list_peer> group_peers1 = new ArrayList<>();
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, peers[(int) i]);

                        int peerrole = ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value;
                        try
                        {
                            peerrole = tox_group_peer_get_role(conference_num,
                                                               get_group_peernum_from_peer_pubkey(group_id,
                                                                                                  peer_pubkey_temp));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        GroupPeerDB peer_from_db = null;
                        try
                        {
                            peer_from_db = orma.selectFromGroupPeerDB().group_identifierEq(
                                    group_id).tox_group_peer_pubkeyEq(peer_pubkey_temp).toList().get(0);
                        }
                        catch (Exception e)
                        {
                        }

                        if (peer_from_db != null)
                        {
                            if ((peer_from_db.first_join_timestamp + NGC_NEW_PEERS_TIMEDELTA_IN_MS) >
                                System.currentTimeMillis())
                            {
                                peer_name = "_NEW_ " + peer_name;
                            }
                        }

                        // Log.i(TAG,
                        //      "groupnum=" + conference_num + " peernum=" + peers[(int) i] + " peer_name=" + peer_name);
                        String peer_name_temp =
                                ToxVars.Tox_Group_Role.value_char(peerrole) + " " + peer_name + " :" + peers[(int) i] +
                                ": " + peer_pubkey_temp.substring(0, 6);

                        group_list_peer glp = new group_list_peer();
                        if (peers[(int) i] == self_peer_id)
                        {
                            glp.self = true;
                        }
                        else
                        {
                            glp.self = false;
                        }
                        glp.peer_pubkey = peer_pubkey_temp;
                        glp.peer_num = i;
                        glp.peer_name = peer_name_temp;
                        glp.peer_connection_status = tox_group_peer_get_connection_status(conference_num,
                                                                                          peers[(int) i]);
                        group_peers1.add(glp);
                    }
                    catch (Exception ignored)
                    {
                    }
                }

                try
                {
                    Collections.sort(group_peers1, new Comparator<group_list_peer>()
                    {
                        @Override
                        public int compare(group_list_peer p1, group_list_peer p2)
                        {
                            String name1 = p1.peer_name;
                            String name2 = p2.peer_name;
                            return name1.compareToIgnoreCase(name2);
                        }
                    });
                }
                catch (Exception ignored)
                {
                }

                for (group_list_peer peerl : group_peers1)
                {
                    add_group_user(peerl.peer_pubkey, peerl.peer_num, peerl.peer_name, peerl.peer_connection_status, peerl.self);
                }
            }
        }

        long offline_num_peers = tox_group_offline_peer_count(conference_num);

        if (offline_num_peers > 0)
        {
            List<group_list_peer> group_peers_offline = new ArrayList<group_list_peer>();
            long i = 0;
            for (i = 0; i < GC_MAX_SAVED_PEERS; i++)
            {
                try
                {
                    String peer_pubkey_temp = tox_group_savedpeer_get_public_key(conference_num, i);
                    String peer_name = "zzzzzoffline " + i;
                    GroupPeerDB peer_from_db = null;
                    try
                    {
                        peer_from_db = orma.selectFromGroupPeerDB().group_identifierEq(
                                group_id).tox_group_peer_pubkeyEq(peer_pubkey_temp).toList().get(0);
                    }
                    catch (Exception e)
                    {
                    }

                    String peerrole = "";

                    if (peer_from_db != null)
                    {
                        peer_name = peer_from_db.peer_name;
                        if ((peer_from_db.first_join_timestamp + NGC_NEW_PEERS_TIMEDELTA_IN_MS) >
                            System.currentTimeMillis())
                        {
                            peer_name = "_NEW_ " + peer_name;
                        }
                        peerrole = ToxVars.Tox_Group_Role.value_char(peer_from_db.Tox_Group_Role) + " ";
                    }

                    // Log.i(TAG, "groupnum=" + conference_num + " peernum=" + offline_peers[(int) i] + " peer_name=" +
                    //           peer_name);
                    String peer_name_temp =  peerrole + peer_name + " :" + i + ": " + peer_pubkey_temp.substring(0, 6);

                    group_list_peer glp3 = new group_list_peer();
                    glp3.peer_pubkey = peer_pubkey_temp;
                    glp3.peer_num = i;
                    glp3.peer_name = peer_name_temp;
                    glp3.peer_connection_status = ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value;
                    group_peers_offline.add(glp3);
                }
                catch (Exception ignored)
                {
                }
            }

            try
            {
                Collections.sort(group_peers_offline, new Comparator<group_list_peer>()
                {
                    @Override
                    public int compare(group_list_peer p1, group_list_peer p2)
                    {
                        String name1 = p1.peer_name;
                        String name2 = p2.peer_name;
                        return name1.compareToIgnoreCase(name2);
                    }
                });
            }
            catch (Exception ignored)
            {
            }

            for (group_list_peer peerloffline : group_peers_offline)
            {
                add_group_user(peerloffline.peer_pubkey, peerloffline.peer_num, peerloffline.peer_name,
                               peerloffline.peer_connection_status, false);
            }

        }

    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        stop_group_video(this);
        closeCamera();
        MainActivity.group_message_list_fragment = null;
        MainActivity.group_message_list_activity = null;
        ngc_video_packet_last_incoming_ts = -1;
        NGC_Group_video_check_incoming_thread_running = false;
        try
        {
            NGC_Group_video_check_incoming_thread.interrupt();
        }
        catch(Exception e)
        {
        }
        ngc_purge_video_incoming_peer_list();
        ngc_incoming_video_peer_toggle_current_index = 0;
        flush_decoder = 1;
        // Log.i(TAG, "onPause:001:conf_id=" + conf_id);
        group_id = "-1";
        // Log.i(TAG, "onPause:002:conf_id=" + conf_id);
    }

    @Override
    protected void onStop()
    {
        try
        {
            na_set_audio_play_volume_percent(100);
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }

        if (emojiPopup != null)
        {
            emojiPopup.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        // reset update trigger timestamp
        update_group_all_users_last_trigger_ts = 0;

        stop_group_video(this);
        closeCamera();
        ngc_video_packet_last_incoming_ts = -1;
        ngc_incoming_video_peer_toggle_current_index = 0;
        flush_decoder = 1;
        ngc_purge_video_incoming_peer_list();

        try
        {
            na_set_audio_play_volume_percent(PREF__audio_play_volume_percent);
            Log.i(TAG,"NGC:PREF__audio_play_volume_percent=" + PREF__audio_play_volume_percent);
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }

        // Log.i(TAG, "onResume:001:conf_id=" + conf_id);

        if (group_id.equals("-1"))
        {
            group_id = group_id_prev;
            // Log.i(TAG, "onResume:001:conf_id=" + conf_id);
        }

        change_msg_notification(NOTIFICATION_EDIT_ACTION_REMOVE.value, group_id, null, null);

        MainActivity.group_message_list_activity = this;
        wakeup_tox_thread();
        NGC_Group_video_check_incoming_thread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "NGC_Group_video_check_incoming_thread:starting ...");
                Log.i(TAG, "NGC_Group_video_check_incoming_thread_running:true:003");
                while (NGC_Group_video_check_incoming_thread_running)
                {
                    try
                    {
                        // Log.i(TAG, "NGC_Group_video_check_incoming_thread:running --=> " + (ngc_video_packet_last_incoming_ts + (2 * 1000)) + " " + System.currentTimeMillis());
                        ngc_update_video_incoming_peer_list_ts();
                        if ((ngc_video_packet_last_incoming_ts + (2 * 1000)) < System.currentTimeMillis())
                        {
                            if (sending_video_to_group)
                            {
                            }
                            else
                            {
                                ngc_set_video_call_icon(NGC_VIDEO_ICON_STATE_INACTIVE);
                            }
                        }
                        else
                        {
                            if (sending_video_to_group)
                            {
                            }
                            else
                            {
                                ngc_set_video_call_icon(NGC_VIDEO_ICON_STATE_INCOMING);
                            }
                        }

                        if (sending_video_to_group)
                        {
                            String peer_name_txt = "";
                            if (!lookup_ngc_incoming_video_peer_list.isEmpty())
                            {
                                peer_name_txt = tox_group_peer_get_name__wrapper(group_id, ngc_video_showing_video_from_peer_pubkey);
                                if ((peer_name_txt == null) || (peer_name_txt.equals("")) || (peer_name_txt.equals("-1")))
                                {
                                    peer_name_txt = "Unknown";
                                }
                            }
                            if (lookup_ngc_incoming_video_peer_list.isEmpty())
                            {
                                ngc_set_video_info_text("streams:0");
                            }
                            else
                            {
                                ngc_set_video_info_text("streams:" + lookup_ngc_incoming_video_peer_list.size() + "\n" +
                                                        peer_name_txt);
                            }
                        }

                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "NGC_Group_video_check_incoming_thread:finished");
            }
        };
        NGC_Group_video_check_incoming_thread_running = true;
        NGC_Group_video_check_incoming_thread.start();
    }

    private void setUpEmojiPopup()
    {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(
                new OnEmojiBackspaceClickListener()
                {
                    @Override
                    public void onEmojiBackspaceClick(View v)
                    {

                    }

                }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener()
        {
            @Override
            public void onEmojiPopupShown()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(FontAwesome.Icon.faw_keyboard).color(
                        getResources().getColor(R.color.icon_colors)).sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.about_icon_email);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener()
        {
            @Override
            public void onKeyboardOpen(@Px final int keyBoardHeight)
            {
                // Log.d(TAG, "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener()
        {
            @Override
            public void onEmojiPopupDismiss()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(
                        GoogleMaterial.Icon.gmd_sentiment_satisfied).color(
                        getResources().getColor(R.color.icon_colors)).sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener()
        {
            @Override
            public void onKeyboardClose()
            {
                // Log.d(TAG, "Closed soft keyboard");
            }
        }).build(ml_new_group_message);
    }

    String get_current_group_id()
    {
        return group_id;
    }

    public void set_group_connection_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (is_group_active(group_id_prev))
                    {
                        if (tox_group_is_connected(tox_group_by_groupid__wrapper(group_id_prev)) ==
                            TRIFAGlobals.TOX_GROUP_CONNECTION_STATUS.TOX_GROUP_CONNECTION_STATUS_CONNECTED.value)
                        {
                            ml_icon.setImageResource(R.drawable.circle_green);
                        }
                        else
                        {
                            ml_icon.setImageResource(R.drawable.circle_orange);
                        }
                    }
                    else
                    {
                        ml_icon.setImageResource(R.drawable.circle_red);
                    }
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (!event.isShiftPressed())
                    {
                        // Log.i(TAG, "dispatchKeyEvent:KEYCODE_ENTER");
                        send_message_onclick(null);
                        return true;
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public static void add_quote_group_message_text(final String quote_text)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if ((ml_new_group_message.getText().toString() == null) ||
                        (ml_new_group_message.getText().toString().length() == 0))
                    {
                        ml_new_group_message.append(TEXT_QUOTE_STRING_1 + quote_text + TEXT_QUOTE_STRING_2 + "\n");
                    }
                    else
                    {
                        String old_text = ml_new_group_message.getText().toString();
                        ml_new_group_message.setText("");
                        // need to do it this way, or else the text input cursor will not be in the correct place
                        ml_new_group_message.append(
                                old_text + "\n" + TEXT_QUOTE_STRING_1 + quote_text + TEXT_QUOTE_STRING_2 + "\n");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "add_quote_message_text:EE01:" + e.getMessage());
                }
            }
        };

        if (group_handler_s != null)
        {
            group_handler_s.post(myRunnable);
        }
    }

    synchronized public void send_message_onclick(View view)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";
        try
        {
            // send typed message to friend
            msg = ml_new_group_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                        ml_new_group_message.getText().toString().length()));

            try
            {
                GroupMessage m = new GroupMessage();
                m.is_new = false; // own messages are always "not new"
                m.tox_group_peer_pubkey = tox_group_self_get_public_key(
                        tox_group_by_groupid__wrapper(group_id)).toUpperCase();
                m.direction = 1; // msg sent
                m.TOX_MESSAGE_TYPE = 0;
                m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
                m.tox_group_peername = null;
                m.private_message = 0;
                m.group_identifier = group_id;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.sent_timestamp = System.currentTimeMillis();
                m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
                m.text = msg;
                m.was_synced = false;
                m.TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;

                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    long message_id = tox_group_send_message(tox_group_by_groupid__wrapper(group_id), 0, msg);
                    // Log.i(TAG, "tox_group_send_message:result=" + message_id + " m=" + m);
                    if (PREF__X_battery_saving_mode)
                    {
                        Log.i(TAG, "global_last_activity_for_battery_savings_ts:001:*PING*");
                    }
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

                    if (message_id > -1)
                    {
                        // message was sent OK
                        m.message_id_tox = fourbytes_of_long_to_hex(message_id);
                        // Log.i(TAG, "message_id_tox=" + m.message_id_tox + " message_id=" + message_id);
                        // TODO: m.msg_id_hash = hex(peerpubkey + message_id)
                        insert_into_group_message_db(m, true);
                        ml_new_group_message.setText("");
                    }
                }
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

    static void add_attachment_ngc(Context c, Intent data, Intent orig_intent, String groupid_local, boolean activity_group_num)
    {
        Log.i(TAG, "add_attachment:001");

        try
        {
            String fileName = null;

            try
            {
                DocumentFile documentFile = DocumentFile.fromSingleUri(c, data.getData());

                fileName = documentFile.getName();
                // Log.i(TAG, "file_attach_for_send:documentFile:fileName=" + fileName);
                // Log.i(TAG, "file_attach_for_send:documentFile:fileLength=" + documentFile.length());

                ContentResolver cr = c.getApplicationContext().getContentResolver();
                Cursor metaCursor = cr.query(data.getData(), null, null, null, null);
                if (metaCursor != null)
                {
                    try
                    {
                        if (metaCursor.moveToFirst())
                        {
                            String file_path = metaCursor.getString(0);
                            // Log.i(TAG, "file_attach_for_send:metaCursor_path:fp=" + file_path);
                            // Log.i(TAG, "file_attach_for_send:metaCursor_path:column names=" +
                            //            metaCursor.getColumnNames().length);
                            int j;
                            for (j = 0; j < metaCursor.getColumnNames().length; j++)
                            {
                                // Log.i(TAG, "file_attach_for_send:metaCursor_path:column name=" +
                                //           metaCursor.getColumnName(j));
                                // Log.i(TAG,
                                //       "file_attach_for_send:metaCursor_path:column data=" + metaCursor.getString(j));
                                if (metaCursor.getColumnName(j).equals(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                                {
                                    if (metaCursor.getString(j) != null)
                                    {
                                        if (metaCursor.getString(j).length() > 0)
                                        {
                                            fileName = metaCursor.getString(j);
                                            // Log.i(TAG, "file_attach_for_send:filename new=" + fileName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        metaCursor.close();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            final String fileName_ = fileName;

            if (fileName_ != null)
            {
                if (activity_group_num)
                {
                    Log.i(TAG, "add_outgoing_file:activity_group_num:true");
                    final Thread t = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            if (groupid_local.equals("-1"))
                            {
                                // ok, we need to wait for onResume to finish and give us the friendnum
                                Log.i(TAG,
                                      "add_outgoing_file:ok, we need to wait for onResume to finish and give us the friendnum");
                                long loop = 0;
                                while (loop < 100)
                                {
                                    loop++;
                                    try
                                    {
                                        Thread.sleep(20);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (MainActivity.group_message_list_activity != null)
                                    {
                                        if (!MainActivity.group_message_list_activity.get_current_group_id().equals(
                                                "-1"))
                                        {
                                            // got friendnum
                                            Log.i(TAG, "add_outgoing_file:got groupnum");
                                            break;
                                        }
                                    }
                                }

                                loop = 0;
                                while (loop < 1000)
                                {
                                    loop++;
                                    try
                                    {
                                        Thread.sleep(20);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (oncreate_finished)
                                    {
                                        Log.i(TAG, "add_outgoing_file:oncreate_finished");
                                        break;
                                    }
                                }
                            }

                            try
                            {
                                Thread.sleep(50);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            if (MainActivity.group_message_list_activity != null)
                            {
                                if (!MainActivity.group_message_list_activity.get_current_group_id().equals("-1"))
                                {
                                    // sorry, still no friendnum
                                    Log.i(TAG, "add_outgoing_file:sorry, still no groupnum");
                                    return;
                                }
                            }
                            Log.i(TAG, "add_outgoing_file:add_outgoing_file:thread_01");
                            add_outgoing_file(c, MainActivity.group_message_list_activity.get_current_group_id(),
                                              data.getData().toString(), fileName_, data.getData(), false,
                                              activity_group_num);
                        }
                    };
                    t.start();
                }
                else
                {
                    Log.i(TAG, "add_outgoing_file:activity_group_num:FALSE");
                    final Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            Log.i(TAG, "add_outgoing_file:add_outgoing_file:thread_02");

                            long loop = 0;
                            while (loop < 100)
                            {
                                loop++;
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                if (MainActivity.group_message_list_activity != null)
                                {
                                    if (!MainActivity.group_message_list_activity.get_current_group_id().equals("-1"))
                                    {
                                        // got friendnum
                                        Log.i(TAG, "add_outgoing_file:got groupnum:02");
                                        break;
                                    }
                                }
                            }

                            loop = 0;
                            while (loop < 1000)
                            {
                                loop++;
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                if (oncreate_finished)
                                {
                                    Log.i(TAG, "add_outgoing_file:oncreate_finished:02");
                                    break;
                                }
                            }
                            add_outgoing_file(c, groupid_local, data.getData().toString(), fileName_, data.getData(),
                                              false, activity_group_num);
                        }
                    };
                    t2.start();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "select_file:22:EE1:" + e.getMessage());
        }
    }

    static boolean onClick_message_helper(final View v, boolean is_selected, final GroupMessage message_)
    {
        try
        {
            if (is_selected)
            {
                v.setBackgroundColor(Color.TRANSPARENT);
                is_selected = false;
                selected_group_messages.remove(message_.id);
                selected_group_messages_text_only.remove(message_.id);
                selected_group_messages_incoming_file.remove(message_.id);
                if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                {
                    amode_save_menu_item.setVisible(true);
                }
                else
                {
                    amode_save_menu_item.setVisible(false);
                }

                if (selected_group_messages.size() == 1)
                {
                    amode_info_menu_item.setVisible(true);
                }
                else
                {
                    amode_info_menu_item.setVisible(false);
                }

                if (selected_group_messages.isEmpty())
                {
                    // last item was de-selected
                    amode.finish();
                }
                else
                {
                    if (amode != null)
                    {
                        amode.setTitle("" + selected_group_messages.size() + " selected");
                    }
                }
            }
            else
            {
                if (!selected_group_messages.isEmpty())
                {
                    v.setBackgroundColor(Color.GRAY);
                    is_selected = true;
                    selected_group_messages.add(message_.id);

                    if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                    {
                        selected_group_messages_text_only.add(message_.id);
                    }
                    else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (message_.direction == 0)
                        {
                            selected_group_messages_incoming_file.add(message_.id);
                        }
                    }

                    if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                    {
                        amode_save_menu_item.setVisible(true);
                    }
                    else
                    {
                        amode_save_menu_item.setVisible(false);
                    }


                    if (selected_group_messages.size() == 1)
                    {
                        amode_info_menu_item.setVisible(true);
                    }
                    else
                    {
                        amode_info_menu_item.setVisible(false);
                    }

                    if (amode != null)
                    {
                        amode.setTitle("" + selected_group_messages.size() + " selected");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return is_selected;
    }

    static class long_click_message_return
    {
        boolean is_selected;
        boolean ret_value;
    }

    static GroupMessageListActivity.long_click_message_return onLongClick_message_helper(Context context, final View v, boolean is_selected, final GroupMessage message_)
    {
        GroupMessageListActivity.long_click_message_return ret = new GroupMessageListActivity.long_click_message_return();

        try
        {
            if (is_selected)
            {
                ret.is_selected = true;
            }
            else
            {
                if (selected_group_messages.isEmpty())
                {
                    try
                    {
                        amode = MainActivity.group_message_list_activity.startSupportActionMode(
                                new ToolbarActionMode(context));
                        amode_save_menu_item = amode.getMenu().findItem(R.id.action_save);
                        amode_info_menu_item = amode.getMenu().findItem(R.id.action_info);
                        v.setBackgroundColor(Color.GRAY);
                        ret.is_selected = true;
                        selected_group_messages.add(message_.id);

                        if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                        {
                            selected_group_messages_text_only.add(message_.id);
                        }
                        else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                        {
                            if (message_.direction == 0)
                            {
                                selected_group_messages_incoming_file.add(message_.id);
                            }
                        }

                        if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                        {
                            amode_save_menu_item.setVisible(true);
                        }
                        else
                        {
                            amode_save_menu_item.setVisible(false);
                        }


                        if (selected_group_messages.size() == 1)
                        {
                            amode_info_menu_item.setVisible(true);
                        }
                        else
                        {
                            amode_info_menu_item.setVisible(false);
                        }

                        if (amode != null)
                        {
                            amode.setTitle("" + selected_group_messages.size() + " selected");
                        }
                        ret.ret_value = true;
                        return ret;
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
        }

        ret.ret_value = true;
        return ret;
    }

    @Override
    public void onBackPressed()
    {
        if (group_message_drawer.isDrawerOpen())
        {
            group_message_drawer.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }

    synchronized void update_group_all_users()
    {
        // Log.i(TAG, "update_group_all_users:** CALL");
        long currentTime = System.currentTimeMillis();

        if (currentTime - update_group_all_users_last_trigger_ts >= INTERVAL_UPDATE_NGC_GROUP_ALL_USERS_MS)
        {
            update_group_all_users_last_trigger_ts = currentTime;
            // Log.i(TAG, "update_group_all_users:-> REAL");
            update_group_all_users_real();
        }
        else
        {
            long delta_t_ms = currentTime - update_group_all_users_last_trigger_ts;
            // Log.i(TAG, "update_group_all_users:  TRIG delta ms=" + delta_t_ms);
            long trigger_in_ms_again = INTERVAL_UPDATE_NGC_GROUP_ALL_USERS_MS - delta_t_ms;
            if ((trigger_in_ms_again < 1) || (trigger_in_ms_again > (INTERVAL_UPDATE_NGC_GROUP_ALL_USERS_MS + 1)))
            {
                trigger_in_ms_again = INTERVAL_UPDATE_NGC_GROUP_ALL_USERS_MS;
            }
            final long trigger_in_ms_again_ = trigger_in_ms_again + 2;
            final Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(trigger_in_ms_again_);
                    // Log.i(TAG, "update_group_all_users:__ CALL from Trigger");
                    update_group_all_users();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    void update_group_all_users_real()
    {
        try
        {
            set_peer_count_header();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            set_peer_names_and_avatars();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized void remove_group_all_users()
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
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    lookup_peer_listnum_pubkey.clear();
                                    group_message_drawer.removeAllItems();
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                }
                            }
                        };

                        if (group_handler_s != null)
                        {
                            group_handler_s.post(myRunnable);
                        }

                        // TODO: hack to be synced with additions later
                        Thread.sleep(120);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "remove_group_user:EE:" + e.getMessage());
        }
    }

    synchronized void remove_group_user(String peer_pubkey)
    {
        // TODO: write me
    }

    synchronized void add_group_user(final String peer_pubkey, final long peernum, String name, int connection_status, boolean self)
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
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    ConferenceCustomDrawerPeerItem new_item = null;
                                    boolean have_avatar_for_pubkey = false;

                                    try
                                    {
                                        int badge_color = R.color.md_green_700;
                                        if (connection_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value)
                                        {
                                            badge_color = R.color.md_red_700;
                                        }
                                        else if (connection_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value)
                                        {
                                            badge_color = R.color.md_orange_700;
                                        }

                                        new_item = new ConferenceCustomDrawerPeerItem(have_avatar_for_pubkey,
                                                                                      peer_pubkey).withIdentifier(
                                                peernum).withName(name).withBadge("" + peernum).withBadgeStyle(
                                                new BadgeStyle().withTextColor(Color.WHITE).withColorRes(
                                                        badge_color)).withOnDrawerItemClickListener(
                                                new Drawer.OnDrawerItemClickListener()
                                                {
                                                    @Override
                                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                    {
                                                        Intent intent = new Intent(view.getContext(),
                                                                                   GroupPeerInfoActivity.class);
                                                        intent.putExtra("peer_pubkey", peer_pubkey);
                                                        intent.putExtra("group_id", group_id);
                                                        view.getContext().startActivity(intent);
                                                        return true;
                                                    }
                                                });

                                        if (self)
                                        {
                                            new_item.withTextColor(Color.parseColor("#FF5733"));
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        new_item = new ConferenceCustomDrawerPeerItem(false, null).withIdentifier(
                                                peernum).withName(name).withIcon(
                                                GoogleMaterial.Icon.gmd_face).withOnDrawerItemClickListener(
                                                new Drawer.OnDrawerItemClickListener()
                                                {
                                                    @Override
                                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                    {
                                                        /*
                                                        Intent intent = new Intent(view.getContext(),
                                                                                   ConferencePeerInfoActivity.class);
                                                        intent.putExtra("peer_pubkey", peer_pubkey);
                                                        intent.putExtra("group_id", group_id);
                                                        intent.putExtra("offline", offline);
                                                        view.getContext().startActivity(intent);
                                                        */
                                                        return true;
                                                    }
                                                });
                                    }

                                    // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                    group_message_drawer.addItem(new_item);
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                    Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                }
                            }
                        };

                        if (group_handler_s != null)
                        {
                            group_handler_s.post(myRunnable);
                        }

                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                        Log.i(TAG, "add_group_user:EE3:" + e3.getMessage());
                    }
                }
            };
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_group_user:EE:" + e.getMessage());
        }
    }

    public void show_add_friend_group(View view)
    {
        Log.i(TAG, "show_add_friend_group");
        Intent intent = new Intent(this, FriendSelectSingleActivity.class);
        intent.putExtra("group_id", group_id);
        startActivityForResult(intent, SelectFriendSingleActivity_ID);
    }

    public void openCamera()
    {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            try
            {
                if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
                {
                    for (String cameraId_iter : manager.getCameraIdList())
                    {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId_iter);
                        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                        {
                            cameraId = cameraId_iter;
                        }
                    }
                }
                else
                {
                    for (String cameraId_iter : manager.getCameraIdList())
                    {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId_iter);
                        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)
                        {
                            cameraId = cameraId_iter;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] outputSizes = map.getOutputSizes(ImageFormat.YUV_420_888);
            Size selectedSize = chooseOptimalSize(outputSizes, CAMERAX_NGC_IMAGE_WIDTH, CAMERAX_NGC_IMAGE_HEIGHT);
            mImageReader = ImageReader.newInstance(selectedSize.getWidth(), selectedSize.getHeight(), ImageFormat.YUV_420_888,
                                                   CAMERAX_NGC_CAPTURE_MAX_IMAGES);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private void createCaptureSession() {
        try {
            Surface surface = mImageReader.getSurface();
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession = session;
                    startPreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // Handle configuration failure
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try
            {
                if (ngc_video_off)
                {
                    return;
                }

                Image image = reader.acquireLatestImage();
                if (image == null)
                {
                    return;
                }

                if ((last_processed_camera_frame == -1) || ((last_processed_camera_frame + PREF__ngc_video_frame_delta_ms)
                                                            < System.currentTimeMillis()))
                {
                    last_processed_camera_frame = System.currentTimeMillis();

                    int yRowStride = image.getPlanes()[0].getRowStride();
                    int uRowStride = image.getPlanes()[1].getRowStride();
                    int vRowStride = image.getPlanes()[2].getRowStride();
                    int ypixelStride = image.getPlanes()[0].getPixelStride();
                    int upixelStride = image.getPlanes()[1].getPixelStride();
                    int vpixelStride = image.getPlanes()[2].getPixelStride();
                    //Log.i(TAG, "yRowStride="+ yRowStride +  " uRowStride=" + uRowStride + " vRowStride=" + vRowStride +
                    //           " ypixelStride=" + ypixelStride + " upixelStride=" + upixelStride + " vpixelStride=" + vpixelStride);

                    ByteBuffer y_buffer = image.getPlanes()[0].getBuffer();
                    final int y_size_in_bytes = y_buffer.remaining();
                    byte[] y_data = new byte[y_size_in_bytes];
                    y_buffer.get(y_data);

                    ByteBuffer u_buffer = image.getPlanes()[1].getBuffer();
                    final int u_size_in_bytes = u_buffer.remaining();
                    byte[] u_data = new byte[u_size_in_bytes];
                    u_buffer.get(u_data);

                    ByteBuffer v_buffer = image.getPlanes()[2].getBuffer();
                    final int v_size_in_bytes = v_buffer.remaining();
                    byte[] v_data = new byte[v_size_in_bytes];
                    v_buffer.get(v_data);

                    if ((upixelStride == 2) && (vpixelStride == upixelStride))
                    {
                        for (int b = 1; b < (u_size_in_bytes / upixelStride); b++)
                        {
                            u_data[b] = u_data[b * upixelStride];
                            v_data[b] = v_data[b * vpixelStride];
                        }
                    }

                    // Process the captured YUV frame data
                    //Log.i(TAG, "IIIIIIIIII:camera_image:bytes=" + y_size_in_bytes + " " + u_size_in_bytes + " " +
                    //           v_size_in_bytes);

                    //ByteBuffer yuv_frame_data_buf = ByteBuffer.allocateDirect(y_size_in_bytes + u_size_in_bytes + v_size_in_bytes);
                    //yuv_frame_data_buf.rewind();
                    //
                    //yuv_frame_data_buf.put(y_data);
                    //yuv_frame_data_buf.put(u_data);
                    //yuv_frame_data_buf.put(v_data);

                    //y_buffer.rewind();
                    //u_buffer.rewind();
                    //v_buffer.rewind();

                    final byte[][] buf3 = {null};
                    byte[] buf2 = new byte[((640 * 480) * 3 / 2)];
                    buf3[0] = new byte[((640 * 480) * 3 / 2)];

                    final int off_u = 640 * 480;
                    final int off_v = (640 * 480) + (640 * 480) / 4;
                    //y_buffer.get(buf2, 0, 640 * 480);
                    //u_buffer.get(buf2, off_u, (640 * 480) / 4);
                    //v_buffer.get(buf2, off_v, (640 * 480) / 4);

                    System.arraycopy(y_data, 0, buf2, 0, off_u);
                    System.arraycopy(u_data, 0, buf2, off_u, (off_u/4));
                    System.arraycopy(v_data, 0, buf2, off_v, (off_u/4));

                    byte[] finalBuf = buf2;
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                buf3[0] = YUV420rotate90(finalBuf, buf3[0], 640, 480);
                                if (ngc_active_camera_type == NGC_FRONT_CAMERA_USED)
                                {
                                    // rotate 180 degrees more
                                    byte[] buf4 = new byte[((640 * 480) * 3 / 2)];
                                    buf4 = YUV420rotate90(buf3[0], buf4, 480, 640);
                                    buf3[0] = YUV420rotate90(buf4, buf3[0], 640, 480);
                                }
                                //
                                ngc_own_alloc_in.copyFrom(buf3[0]);
                                ngc_own_yuvToRgb.setInput(ngc_own_alloc_in);
                                ngc_own_yuvToRgb.forEach(ngc_own_alloc_out);
                                ngc_own_alloc_out.copyTo(ngc_own_video_frame_image);
                                ngc_video_own_view.setBitmap(ngc_own_video_frame_image);
                                final int y_bytes_ = 640 * 480;
                                final int uv_bytes_ = (640/2) * (480/2);
                                System.arraycopy(buf3[0], 0, y_buf__, 0, y_bytes_);
                                System.arraycopy(buf3[0], y_bytes_, u_buf__, 0, uv_bytes_);
                                System.arraycopy(buf3[0], y_bytes_ + uv_bytes_, v_buf__, 0, uv_bytes_);
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
                    // Log.i(TAG, "IIIIIIIIII:camera_image:skip_frame");
                }
                image.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    public void closeCamera()
    {
        try
        {
            if (mCaptureSession != null)
            {
                mCaptureSession.close();
                mCaptureSession = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            flush_output_image();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void flush_output_image()
    {
        try
        {
            ngc_video_view.setBitmap(null);
        }
        catch(Exception e)
        {
        }
    }

    @SuppressLint("RestrictedApi")
    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        try
        {
            List<Size> bigEnough = new ArrayList<>();
            for (Size option : choices)
            {
                if (option.getWidth() >= width && option.getHeight() >= height)
                {
                    bigEnough.add(option);
                }
            }
            if (bigEnough.size() > 0)
            {
                return Collections.min(bigEnough, new CompareSizesByArea());
            }
            else
            {
                return choices[0];
            }
        }
        catch(Exception e)
        {
            return new Size(640,480);
        }
    }

    synchronized public static void stop_group_video(final Context c)
    {
        ngc_video_showing_video_from_peer_pubkey = "-1";
        NGC_Group_video_play_thread_running = false;
        NGC_Group_audio_record_thread_running = false;
        Log.i(TAG,"NGC_Group_video_play_thread_running:false:001");
        ngc_video_view_container.setVisibility(View.GONE);
        sending_video_to_group = false;

        // ---- stop audio stuff
        try
        {
            GroupGroupAudioService.stop_me(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ---- stop audio stuff

        ngc_set_video_call_icon(NGC_VIDEO_ICON_STATE_INACTIVE);
    }

    public static void play_ngc_incoming_audio_frame(final long group_number,
                                                     final long peer_id,
                                                     final byte[] encoded_audio_and_header,
                                                     long length)
    {
        if (MainActivity.group_message_list_activity == null)
        {
            // NGC group activity not open
            return;
        }

        final long conference_num = tox_group_by_groupid__wrapper(group_id);
        if (conference_num != group_number)
        {
            // wrong NGC group
            return;
        }

        // Log.i(TAG, "play_ngc_incoming_audio_frame:delta=" + (System.currentTimeMillis() - ngc_audio_packet_last_incoming_ts));
        ngc_audio_packet_last_incoming_ts = System.currentTimeMillis();
        ngc_video_packet_last_incoming_ts = System.currentTimeMillis();

        if ((ngc_video_frame_image != null) && (!ngc_video_frame_image.isRecycled()))
        {
            if (sending_video_to_group == true)
            {
                final String ngc_incoming_audio_from_peer = tox_group_peer_get_public_key__wrapper(group_number, peer_id);
                ngc_update_video_incoming_peer_list(ngc_incoming_audio_from_peer);
                if (ngc_video_showing_video_from_peer_pubkey.equals("-1"))
                {
                    ngc_video_showing_video_from_peer_pubkey = ngc_incoming_audio_from_peer;
                }
                else if (!ngc_video_showing_video_from_peer_pubkey.equalsIgnoreCase(ngc_incoming_audio_from_peer))
                {
                    // we are already playing the video/audio of a different peer in the group
                    return;
                }
                // remove header from data (10 bytes)
                final int pcm_encoded_length = (int) (length - 10);
                final byte[] pcm_encoded_buf = new byte[pcm_encoded_length];
                final byte[] pcm_decoded_buf = new byte[20000];
                final int bytes_in_40ms = 1920;
                final byte[] pcm_decoded_buf_delta_1 = new byte[bytes_in_40ms * 2];
                final byte[] pcm_decoded_buf_delta_2 = new byte[bytes_in_40ms * 2];
                final byte[] pcm_decoded_buf_delta_3 = new byte[bytes_in_40ms * 2];
                try
                {
                    System.arraycopy(encoded_audio_and_header, 10, pcm_encoded_buf, 0, pcm_encoded_length);
                    //
                    int decoded_samples = toxav_ngc_audio_decode(pcm_encoded_buf, pcm_encoded_length, pcm_decoded_buf);
                    // Log.i(TAG, "play_ngc_incoming_audio_frame:toxav_ngc_audio_decode:decoded_samples=" + decoded_samples);

                    // put pcm data into a FIFO
                    System.arraycopy(pcm_decoded_buf, 0, pcm_decoded_buf_delta_1, 0, (bytes_in_40ms*2));
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_1);
                    System.arraycopy(pcm_decoded_buf, (bytes_in_40ms*2), pcm_decoded_buf_delta_2, 0, (bytes_in_40ms*2));
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_2);
                    System.arraycopy(pcm_decoded_buf, ((bytes_in_40ms*2)*2), pcm_decoded_buf_delta_3, 0, (bytes_in_40ms*2));
                    ngc_audio_in_queue.offer(pcm_decoded_buf_delta_3);
                    ngc_video_frame_last_incoming_ts = System.currentTimeMillis();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void show_ngc_incoming_video_frame_v1(final long group_number,
                                                        final long peer_id,
                                                        final byte[] encoded_video_and_header,
                                                        long length)
    {
        if (MainActivity.group_message_list_activity == null)
        {
            // NGC group activity not open
            return;
        }

        final long conference_num = tox_group_by_groupid__wrapper(group_id);
        if (conference_num != group_number)
        {
            // wrong NGC group
            return;
        }

        ngc_video_packet_last_incoming_ts = System.currentTimeMillis();

        if ((ngc_video_frame_image != null) && (!ngc_video_frame_image.isRecycled()))
        {
            if (sending_video_to_group == true)
            {
                final String ngc_incoming_video_from_peer = tox_group_peer_get_public_key__wrapper(group_number, peer_id);
                ngc_update_video_incoming_peer_list(ngc_incoming_video_from_peer);

                if (ngc_video_showing_video_from_peer_pubkey.equals("-1"))
                {
                    ngc_video_showing_video_from_peer_pubkey = ngc_incoming_video_from_peer;
                }
                else if (!ngc_video_showing_video_from_peer_pubkey.equalsIgnoreCase(ngc_incoming_video_from_peer))
                {
                    // we are already showing the video of a different peer in the group
                    return;
                }

                // remove header from data (11 bytes)
                final int yuv_frame_encoded_bytes = (int) (length - 11);
                if ((yuv_frame_encoded_bytes > 0) && (yuv_frame_encoded_bytes < 40000))
                {
                    // TODO: make faster and better. this is not optimized.
                    final byte[] yuv_frame_encoded_buf = new byte[yuv_frame_encoded_bytes];
                    int w2 = 480 + 32; // 240 + 16; // encoder stride added
                    int h2 = 640; // 320;
                    final int y_bytes2 = w2 * h2;
                    final int u_bytes2 = (w2 * h2) / 4;
                    final int v_bytes2 = (w2 * h2) / 4;
                    final byte[] y_buf2 = new byte[y_bytes2];
                    final byte[] u_buf2 = new byte[u_bytes2];
                    final byte[] v_buf2 = new byte[v_bytes2];
                    int ystride = -1;
                    try
                    {
                        System.arraycopy(encoded_video_and_header, 11, yuv_frame_encoded_buf, 0, yuv_frame_encoded_bytes);
                        //
                        ystride = toxav_ngc_video_decode(yuv_frame_encoded_buf, yuv_frame_encoded_bytes,
                                                         w2, h2, y_buf2, u_buf2, v_buf2, flush_decoder);
                        //if (ystride != -1)
                        //{
                        //    Log.i(TAG, "toxav_ngc_video_decode:ystride=" + ystride);
                        //}
                        flush_decoder = 0;
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }
                    final int ystride_ = ystride;
                    //
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if (ystride_ == -1)
                                {
                                    ngc_video_view.setImageResource(R.drawable.round_loading_animation);
                                }
                                else
                                {
                                    int w2_decoder = ystride_; // encoder stride
                                    int w2_decoder_uv = ystride_ / 2; // encoder stride
                                    int h2_decoder = 640; // 320;
                                    int h2_decoder_uv = h2_decoder / 2;
                                    final int y_bytes2_decoder = h2_decoder * w2_decoder;
                                    final int u_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv);
                                    final int v_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv);

                                    ByteBuffer yuv_frame_data_buf = ByteBuffer.allocateDirect(
                                            y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder);
                                    yuv_frame_data_buf.rewind();
                                    //
                                    yuv_frame_data_buf.put(y_buf2, 0, y_bytes2_decoder);
                                    yuv_frame_data_buf.put(u_buf2, 0, u_bytes2_decoder);
                                    yuv_frame_data_buf.put(v_buf2, 0, v_bytes2_decoder);
                                    //
                                    yuv_frame_data_buf.rewind();
                                    ngc_alloc_in.copyFrom(yuv_frame_data_buf.array());
                                    ngc_yuvToRgb.setInput(ngc_alloc_in);
                                    ngc_yuvToRgb.forEach(ngc_alloc_out);
                                    ngc_alloc_out.copyTo(ngc_video_frame_image);
                                    ngc_video_view.setBitmap(ngc_video_frame_image);
                                }
                                ngc_video_frame_last_incoming_ts = System.currentTimeMillis();
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
        }
    }

    public static void show_ngc_incoming_video_frame_v2(final long group_number,
                                                        final long peer_id,
                                                        final byte[] encoded_video_and_header,
                                                        long length)
    {
        if (MainActivity.group_message_list_activity == null)
        {
            // NGC group activity not open
            return;
        }

        final long conference_num = tox_group_by_groupid__wrapper(group_id);
        if (conference_num != group_number)
        {
            // wrong NGC group
            return;
        }

        ngc_video_packet_last_incoming_ts = System.currentTimeMillis();

        if ((ngc_video_frame_image != null) && (!ngc_video_frame_image.isRecycled()))
        {
            if (sending_video_to_group == true)
            {
                final String ngc_incoming_video_from_peer = tox_group_peer_get_public_key__wrapper(group_number, peer_id);
                ngc_update_video_incoming_peer_list(ngc_incoming_video_from_peer);

                if (ngc_video_showing_video_from_peer_pubkey.equals("-1"))
                {
                    ngc_video_showing_video_from_peer_pubkey = ngc_incoming_video_from_peer;
                }
                else if (!ngc_video_showing_video_from_peer_pubkey.equalsIgnoreCase(ngc_incoming_video_from_peer))
                {
                    // we are already showing the video of a different peer in the group
                    return;
                }

                // remove header from data (14 bytes)
                final int yuv_frame_encoded_bytes = (int) (length - 14);
                if ((yuv_frame_encoded_bytes > 0) && (yuv_frame_encoded_bytes < 40000))
                {
                    // TODO: make faster and better. this is not optimized.
                    final byte[] yuv_frame_encoded_buf = new byte[yuv_frame_encoded_bytes];
                    int w2 = 480 + 32; // 240 + 16; // encoder stride added
                    int h2 = 640; // 320;
                    final int y_bytes2 = w2 * h2;
                    final int u_bytes2 = (w2 * h2) / 4;
                    final int v_bytes2 = (w2 * h2) / 4;
                    final byte[] y_buf2 = new byte[y_bytes2];
                    final byte[] u_buf2 = new byte[u_bytes2];
                    final byte[] v_buf2 = new byte[v_bytes2];
                    int ystride = -1;
                    final byte[] chkskum = new byte[1];
                    final byte[] low_seqnum = new byte[1];
                    final byte[] high_seqnum = new byte[1];
                    try
                    {
                        System.arraycopy(encoded_video_and_header, 14, yuv_frame_encoded_buf, 0, yuv_frame_encoded_bytes);

                        System.arraycopy(encoded_video_and_header, 11, low_seqnum, 0, 1);
                        System.arraycopy(encoded_video_and_header, 12, high_seqnum, 0, 1);
                        System.arraycopy(encoded_video_and_header, 13, chkskum, 0, 1);
                        long seqnum = Byte.toUnsignedInt(low_seqnum[0]) + Integer.toUnsignedLong((high_seqnum[0] << 8));
                        if (seqnum != (last_video_seq_num + 1))
                        {
                            //Log.i(TAG, "!!!!!!!seqnumber_missing!!!!! " + seqnum + " -> " + (last_video_seq_num + 1));
                        }
                        last_video_seq_num = seqnum;
                        final long crc_8 = Integer.toUnsignedLong(calc_crc_8(yuv_frame_encoded_buf));
                        if (Byte.toUnsignedInt(chkskum[0]) != crc_8)
                        {
                            //Log.i(TAG, "checksum=" + Byte.toUnsignedInt(chkskum[0])
                            //       + " crc8=" + crc_8 + " seqnum=" + seqnum
                            //       + " yuv_frame_encoded_bytes=" + (yuv_frame_encoded_bytes + 14));
                        }
                        //
                        ystride = toxav_ngc_video_decode(yuv_frame_encoded_buf, yuv_frame_encoded_bytes,
                                                         w2, h2, y_buf2, u_buf2, v_buf2, flush_decoder);
                        //if (ystride != -1)
                        //{
                        //    Log.i(TAG, "toxav_ngc_video_decode:ystride=" + ystride);
                        //}
                        flush_decoder = 0;
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }
                    final int ystride_ = ystride;
                    //
                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if (ystride_ == -1)
                                {
                                    ngc_video_view.setImageResource(R.drawable.round_loading_animation);
                                }
                                else
                                {
                                    int w2_decoder = ystride_; // encoder stride
                                    int w2_decoder_uv = ystride_ / 2; // encoder stride
                                    int h2_decoder = 640; // 320;
                                    int h2_decoder_uv = h2_decoder / 2;
                                    final int y_bytes2_decoder = h2_decoder * w2_decoder;
                                    final int u_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv);
                                    final int v_bytes2_decoder = (h2_decoder_uv * w2_decoder_uv);

                                    ByteBuffer yuv_frame_data_buf = ByteBuffer.allocateDirect(
                                            y_bytes2_decoder + u_bytes2_decoder + v_bytes2_decoder);
                                    yuv_frame_data_buf.rewind();
                                    //
                                    yuv_frame_data_buf.put(y_buf2, 0, y_bytes2_decoder);
                                    yuv_frame_data_buf.put(u_buf2, 0, u_bytes2_decoder);
                                    yuv_frame_data_buf.put(v_buf2, 0, v_bytes2_decoder);
                                    //
                                    yuv_frame_data_buf.rewind();
                                    ngc_alloc_in.copyFrom(yuv_frame_data_buf.array());
                                    ngc_yuvToRgb.setInput(ngc_alloc_in);
                                    ngc_yuvToRgb.forEach(ngc_alloc_out);
                                    ngc_alloc_out.copyTo(ngc_video_frame_image);
                                    ngc_video_view.setBitmap(ngc_video_frame_image);
                                }
                                ngc_video_frame_last_incoming_ts = System.currentTimeMillis();
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
        }
    }

    private static int calc_crc_8(byte[] yuv_frame_encoded_buf)
    {
        final int CRC_POLYNOM = 0x9c;
        final int CRC_PRESET = 0xFF;
        int crc_U = CRC_PRESET;
        for(int i = 0; i < yuv_frame_encoded_buf.length; i++){
            crc_U ^= Byte.toUnsignedInt(yuv_frame_encoded_buf[i]);
            for(int j = 0; j < 8; j++) {
                if((crc_U & 0x01) != 0) {
                    crc_U = (crc_U >>> 1) ^ CRC_POLYNOM;
                } else {
                    crc_U = (crc_U >>> 1);
                }
            }
        }
        return crc_U;
    }

    synchronized public static void start_group_video(final Context c)
    {
        // just in case there is a thread still running
        ngc_video_showing_video_from_peer_pubkey = "-1";
        NGC_Group_video_play_thread_running = false;
        NGC_Group_audio_record_thread_running = false;
        Log.i(TAG,"NGC_Group_video_play_thread_running:false:002");
        ngc_video_view_container.setVisibility(View.VISIBLE);
        sending_video_to_group = true;
        ngc_set_video_call_icon(NGC_VIDEO_ICON_STATE_ACTIVE);

        // init audio stuff ------
        try
        {
            set_calling_audio_mode();
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }

        Log.i(TAG, "group_audio_service:start");
        try
        {
            Intent i = new Intent(c, GroupGroupAudioService.class);
            i.putExtra("group_id", group_id);
            c.startService(i);
        }
        catch (Exception e)
        {
            Log.i(TAG, "group_audio_service:EE01:" + e.getMessage());
            e.printStackTrace();
        }
        // init audio stuff ------

        NGC_Group_video_play_thread = new Thread()
        {
            @Override
            public void run()
            {
                final int sleep_millis = PREF__ngc_video_frame_delta_ms;
                try
                {
                    this.setName("t_gv_play");
                    android.os.Process.setThreadPriority(Thread.NORM_PRIORITY);
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                try
                {
                    Thread.sleep(sleep_millis + 50);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Log.i(TAG, "NGC_Group_video_play_thread:starting ...");
                    NGC_Group_video_play_thread_running = true;
                    Log.i(TAG,"NGC_Group_video_play_thread_running:true:003");
                    while (NGC_Group_video_play_thread_running)
                    {
                        // Log.i(TAG, "NGC_Group_video_play_thread:running --=>");
                        final int w = 480; // 240;
                        final int h = 640; // 320;
                        final int video_enc_bitrate = PREF__ngc_video_bitrate;
                        final int y_bytes = w * h;
                        final int u_bytes = (w * h) / 4;
                        final int v_bytes = (w * h) / 4;
                        // byte[] y_buf = new byte[y_bytes];
                        byte[] y_buf = y_buf__;
                        byte[] u_buf = u_buf__;
                        byte[] v_buf = v_buf__;
                        byte[] encoded_vframe = new byte[40000];
                        int encoded_bytes = toxav_ngc_video_encode(video_enc_bitrate,
                                                                   PREF__ngc_video_max_quantizer,
                                                                   w,h,
                                                                   y_buf, y_bytes,
                                                                   u_buf, u_bytes,
                                                                   v_buf, v_bytes,
                                                                   encoded_vframe);
                        // Log.i(TAG, "toxav_ngc_video_encode:bytes=" + encoded_bytes + " video_enc_bitrate=" + video_enc_bitrate);

                        if ((encoded_bytes < 1)||(encoded_bytes > TOX_MAX_NGC_VIDEO_AND_HEADER_SIZE))
                        {
                            // some error with encoding
                        }
                        else
                        {
                            final int header_length = 6 + 1 + 1 + 1 + 1 + 1 + 2 + 1;
                            long data_length_ = header_length + encoded_bytes;
                            final int data_length = (int) data_length_;
                            //
                            try
                            {
                                ByteBuffer data_buf = ByteBuffer.allocateDirect(data_length);
                                data_buf.rewind();
                                //
                                data_buf.put((byte) 0x66);
                                data_buf.put((byte) 0x77);
                                data_buf.put((byte) 0x88);
                                data_buf.put((byte) 0x11);
                                data_buf.put((byte) 0x34);
                                data_buf.put((byte) 0x35);
                                //
                                data_buf.put((byte) 0x02);
                                //
                                data_buf.put((byte) 0x21);
                                //
                                data_buf.put((byte) w); // width: always 480 --> LOL
                                data_buf.put((byte) h); // height: always 640 --> LOL
                                data_buf.put((byte) 1); // codec: always 1  (1 -> H264)
                                //
                                data_buf.put((byte) 1); // seq num low byte
                                data_buf.put((byte) 0); // seq num high byte
                                data_buf.put((byte) 0); // CRC8 checksum
                                //
                                data_buf.put(encoded_vframe, 0, encoded_bytes); // put encoded video frame into buffer
                                //
                                byte[] data = new byte[data_length];
                                data_buf.rewind();
                                data_buf.get(data);
                                if (data_length < MAX_GC_PACKET_CHUNK_SIZE)
                                {
                                    int result = tox_group_send_custom_packet(tox_group_by_groupid__wrapper(group_id), 0,
                                                                              data, data_length);
                                    // Log.i(TAG, "toxav_ngc_video_encode:ls:tox_group_send_custom_packet:result=" + result + " bytes=" + encoded_bytes);
                                }
                                else
                                {
                                    int result = tox_group_send_custom_packet(tox_group_by_groupid__wrapper(group_id), 1,
                                                                              data, data_length);
                                    // Log.i(TAG, "toxav_ngc_video_encode:LL:tox_group_send_custom_packet:result=" + result + " bytes=" + encoded_bytes);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            //
                            if ((ngc_video_frame_last_incoming_ts + (5 * 1000)) < System.currentTimeMillis())
                            {
                                if (ngc_video_frame_last_incoming_ts != -1)
                                {
                                    Log.i(TAG,
                                          "toxav_ngc_video_encode:no incoming video for 5 seconds. resetting video and peer ...");
                                    Runnable myRunnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                ngc_video_view.setImageResource(R.drawable.round_loading_animation);
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
                                ngc_video_frame_last_incoming_ts = -1;
                                ngc_video_showing_video_from_peer_pubkey = "-1";
                            }
                        }
                        //
                        Thread.sleep(sleep_millis); // sleep
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "NGC_Group_video_play_thread:finished");
            }
        };
        NGC_Group_video_play_thread.start();

        NGC_Group_audio_record_thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    this.setName("t_ga_rec");
                    android.os.Process.setThreadPriority(Thread.NORM_PRIORITY);
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Log.i(TAG, "NGC_Group_audio_record_thread:starting ...");
                    NGC_Group_audio_record_thread_running = true;
                    Log.i(TAG,"NGC_Group_audio_record_thread:true:003");
                    while (NGC_Group_audio_record_thread_running)
                    {
                        try
                        {
                            if (ngc_audio_mute == true)
                            {
                                try
                                {
                                    ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                    ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                    ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                }
                                catch(Exception e)
                                {
                                }
                                Thread.sleep(120);
                            }
                            else
                            {
                                final int buffers_in_queue = ngc_audio_out_queue.size();
                                // Log.i(TAG, "NGC_Group_audio_record_thread:buffers_in_queue=" + buffers_in_queue);
                                if (buffers_in_queue > 2)
                                {
                                    final byte[] buf_out_1 = ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                    // Log.i(TAG, "NGC_Group_audio_record_thread:buf1=" + bytes_to_hex(buf_out_1));
                                    final byte[] buf_out_2 = ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                    // Log.i(TAG, "NGC_Group_audio_record_thread:buf2=" + bytes_to_hex(buf_out_2));
                                    final byte[] buf_out_3 = ngc_audio_out_queue.poll(1, TimeUnit.MILLISECONDS);
                                    // Log.i(TAG, "NGC_Group_audio_record_thread:buf3=" + bytes_to_hex(buf_out_3));
                                    if ((buf_out_1 == null) || (buf_out_2 == null) || (buf_out_3 == null))
                                    {
                                        Log.i(TAG, "NGC_Group_audio_record_thread:no data in buffers");
                                    }
                                    else if ((buf_out_1.length != NGC_AUDIO_PCM_BUFFER_BYTES) || (buf_out_2.length != NGC_AUDIO_PCM_BUFFER_BYTES) ||
                                             (buf_out_3.length != NGC_AUDIO_PCM_BUFFER_BYTES))
                                    {
                                        Log.i(TAG, "NGC_Group_audio_record_thread:wrong buffer sizes");
                                    }
                                    else
                                    {
                                        // send ngc audio packet
                                        final byte[] pcm_audio_buffer = new byte[3 * NGC_AUDIO_PCM_BUFFER_BYTES]; // 3 x 3840 bytes pcm data
                                        final int max_encoded_bytes = (MAX_GC_PACKET_CHUNK_SIZE - 10);
                                        final byte[] encoded_aframe = new byte[max_encoded_bytes];
                                        final int samples_per_120ms = NGC_AUDIO_PCM_BUFFER_SAMPLES;
                                        // copy the 3 pcm buffers into new buffer ---------
                                        System.arraycopy(buf_out_1, 0, pcm_audio_buffer, 0 * NGC_AUDIO_PCM_BUFFER_BYTES,
                                                         NGC_AUDIO_PCM_BUFFER_BYTES);
                                        System.arraycopy(buf_out_2, 0, pcm_audio_buffer, 1 * NGC_AUDIO_PCM_BUFFER_BYTES,
                                                         NGC_AUDIO_PCM_BUFFER_BYTES);
                                        System.arraycopy(buf_out_3, 0, pcm_audio_buffer, 2 * NGC_AUDIO_PCM_BUFFER_BYTES,
                                                         NGC_AUDIO_PCM_BUFFER_BYTES);
                                        // copy the 3 pcm buffers into new buffer ---------
                                        int encoded_bytes = toxav_ngc_audio_encode(pcm_audio_buffer, samples_per_120ms,
                                                                                   encoded_aframe);
                                        if ((encoded_bytes < 1) || (encoded_bytes > max_encoded_bytes))
                                        {
                                            // some error with encoding
                                        }
                                        else
                                        {
                                            // Log.i(TAG, "NGC_Group_audio_record_thread:encoded bytes=" + encoded_bytes);
                                            final int header_length = 6 + 1 + 1 + 1 + 1;
                                            long data_length_ = header_length + encoded_bytes;
                                            final int data_length = (int) data_length_;
                                            //
                                            try
                                            {
                                                ByteBuffer data_buf = ByteBuffer.allocateDirect(data_length);
                                                data_buf.rewind();
                                                //
                                                data_buf.put((byte) 0x66);
                                                data_buf.put((byte) 0x77);
                                                data_buf.put((byte) 0x88);
                                                data_buf.put((byte) 0x11);
                                                data_buf.put((byte) 0x34);
                                                data_buf.put((byte) 0x35);
                                                //
                                                data_buf.put((byte) 0x01);
                                                //
                                                data_buf.put((byte) 0x31);
                                                //
                                                data_buf.put((byte) 1); // always 1 (for MONO)
                                                data_buf.put((byte) 48); // always 48 (for 48kHz)
                                                //
                                                data_buf.put(encoded_aframe, 0, encoded_bytes); // put encoded audio frame into buffer
                                                //
                                                byte[] data = new byte[data_length];
                                                data_buf.rewind();
                                                data_buf.get(data);
                                                if (data_length < MAX_GC_PACKET_CHUNK_SIZE)
                                                {
                                                    int result = tox_group_send_custom_packet(
                                                            tox_group_by_groupid__wrapper(group_id), 0, data,
                                                            data_length);
                                                    // Log.i(TAG, "NGC_Group_audio_record_thread:ls:tox_group_send_custom_packet:result=" + result + " bytes=" + encoded_bytes);
                                                }
                                                else
                                                {
                                                    int result = tox_group_send_custom_packet(
                                                            tox_group_by_groupid__wrapper(group_id), 1, data,
                                                            data_length);
                                                    // Log.i(TAG, "NGC_Group_audio_record_thread:LL:tox_group_send_custom_packet:result=" + result + " bytes=" + encoded_bytes);
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                            //
                                        }

                                    }
                                }
                                else
                                {
                                    Thread.sleep(60);
                                }
                            }
                        }
                        catch(Exception e)
                        {
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "NGC_Group_audio_record_thread:finished");
            }
        };
        NGC_Group_audio_record_thread.start();

        // update every x times per second -----------
        final int update_per_sec = 8;
        final Handler ha2 = new Handler();
        ha2.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Log.i(TAG, "update_call_time -> call");
                    update_call_ngc_audio_bars();
                    if (NGC_Group_audio_record_thread_running)
                    {
                        ha2.postDelayed(this, 1000 / update_per_sec);
                    }
                }
                catch(Exception e)
                {
                }
            }
        }, 1000);
        // update every x times per second -----------
    }

    public void toggle_group_video(final View view)
    {
        if (sending_video_to_group)
        {
            stop_group_video(view.getContext());
            closeCamera();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Join Group Video?");
            builder.setMessage("Do you want really want to send your Video and Audio to everybody in this group?");

            builder.setNegativeButton("NO!", null);
            builder.setPositiveButton("Yes, I want", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ngc_incoming_video_peer_toggle_current_index = 0;
                    flush_decoder = 1;
                    start_group_video(view.getContext());
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult:tox_group_invite_friend:start");
        if (requestCode == SelectFriendSingleActivity_ID)
        {
            Log.i(TAG, "onActivityResult:tox_group_invite_friend:001");
            if (resultCode == RESULT_OK)
            {
                Log.i(TAG, "onActivityResult:tox_group_invite_friend:002");
                try
                {
                    int item_type = Integer.parseInt(data.getData().toString().substring(0, 1));
                    String result_friend_pubkey = data.getData().toString().substring(2);
                    Log.i(TAG, "onActivityResult:tox_group_invite_friend:003:");
                    if (result_friend_pubkey != null)
                    {
                        Log.i(TAG, "onActivityResult:tox_group_invite_friend:004:");
                        if (result_friend_pubkey.length() == TOX_PUBLIC_KEY_SIZE * 2)
                        {
                            Log.i(TAG, "onActivityResult:tox_group_invite_friend:result_friend_pubkey:ok");
                            long friend_num_temp_safety2 = tox_friend_by_public_key__wrapper(result_friend_pubkey);
                            if (friend_num_temp_safety2 > 0)
                            {
                                if (group_id.equals("-1"))
                                {
                                    group_id = group_id_prev;
                                    // Log.i(TAG, "onActivityResult:001:conf_id=" + conf_id);
                                }

                                final long group_num = tox_group_by_groupid__wrapper(group_id);

                                Log.d(TAG, "onActivityResult:info:tox_group_invite_friend:group_num=" + group_num);

                                if (group_num > -1)
                                {
                                    int res_conf_invite = tox_group_invite_friend(group_num, friend_num_temp_safety2);
                                    Log.d(TAG, "onActivityResult:info:tox_group_invite_friend:res_conf_invite=" +
                                               res_conf_invite);
                                    if (res_conf_invite != 1)
                                    {
                                        Log.d(TAG,
                                              "onActivityResult:info:tox_group_invite_friend:ERR:" + res_conf_invite);
                                    }
                                    update_savedata_file_wrapper();
                                }
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
    }

    static void add_outgoing_file(Context c, String groupid, String filepath, String filename, Uri uri, boolean real_file_path, boolean update_message_view)
    {
        long file_size = -1;
        try
        {
            DocumentFile documentFile = DocumentFile.fromSingleUri(c, uri);
            String fileName = documentFile.getName();
            Log.i(TAG, "add_outgoing_file:documentFile:fileName=" + fileName);
            Log.i(TAG, "add_outgoing_file:documentFile:fileLength=" + documentFile.length());

            file_size = documentFile.length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            // file length "zero"?
            return;
        }

        if (file_size > FT_OUTGOING_FILESIZE_NGC_MAX_TOTAL)
        {
            display_toast("File too large", true, 100);
            return;
        }

        if (file_size < FT_OUTGOING_FILESIZE_BYTE_USE_STORAGE_FRAMEWORK) // less than xxx Bytes filesize
        {
            MessageListActivity.outgoing_file_wrapped ofw = copy_outgoing_file_to_sdcard_dir(filepath, filename,
                                                                                             file_size);

            if (ofw == null)
            {
                return;
            }

            if (file_size > TOX_MAX_NGC_FILESIZE)
            {
                // reducing the file size down to hopefully 37kbytes -------------------
                Log.i(TAG, "add_outgoing_file:shrink:start");
                shrink_image_file(c, ofw);
                Log.i(TAG, "add_outgoing_file:shrink:done");
                // reducing the file size down to hopefully 37kbytes -------------------
            }
            else
            {
                Log.i(TAG, "add_outgoing_file:no_need_to_shrink_file");
            }

            Log.i(TAG, "add_outgoing_file:001");

            // add FT message to UI
            GroupMessage m = new GroupMessage();
            m.is_new = false; // own messages are always "not new"
            m.tox_group_peer_pubkey = tox_group_self_get_public_key(
                    tox_group_by_groupid__wrapper(groupid)).toUpperCase();
            m.direction = 1; // msg sent
            m.TOX_MESSAGE_TYPE = 0;
            m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
            m.tox_group_peername = null;
            m.private_message = 0;
            m.group_identifier = groupid.toLowerCase();
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.sent_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
            m.text = ofw.filename_wrapped + "\n" + ofw.file_size_wrapped + " bytes";
            m.was_synced = false;
            m.TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;
            m.path_name = ofw.filepath_wrapped;
            m.file_name = ofw.filename_wrapped;
            m.filename_fullpath = new java.io.File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).getAbsolutePath();
            m.storage_frame_work = false;
            try
            {
                m.filesize = new java.io.File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).length();
            }
            catch (Exception ee)
            {
                m.filesize = 0;
            }

            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(hash_bytes);
            m.msg_id_hash = bytebuffer_to_hexstring(hash_bytes, true);
            m.message_id_tox = "";
            insert_into_group_message_db(m, true);
            Log.i(TAG, "add_outgoing_file:090");

            // now send the file to the group as custom package ----------
            Log.i(TAG, "add_outgoing_file:091:send_group_image:start");
            send_group_image(m);
            Log.i(TAG, "add_outgoing_file:092:send_group_image:done");
            // now send the file to the group as custom package ----------
        }
        else
        {
            // HINT: should never get here, since ngc has max filesize of about 37kbytes only
        }
    }

    static void show_messagelist_for_id(Context c, String id, String fill_out_text)
    {
        Intent intent = new Intent(c, GroupMessageListActivity.class);
        if (fill_out_text != null)
        {
            intent.putExtra("fillouttext", fill_out_text);
        }
        intent.putExtra("group_id", id);
        c.startActivity(intent);
    }

    public void scroll_to_bottom(View v)
    {
        try
        {
            MainActivity.group_message_list_fragment.listingsView.scrollToPosition(
                    MainActivity.group_message_list_fragment.adapter.getItemCount() - 1);
        }
        catch (Exception ignored)
        {
        }

        GroupMessageListFragment.is_at_bottom = true;

        try
        {
            do_fade_anim_on_fab(MainActivity.group_message_list_fragment.unread_messages_notice_button, false,
                                this.getClass().getName());
            MainActivity.group_message_list_fragment.unread_messages_notice_button.setSupportBackgroundTintList(
                    (ContextCompat.getColorStateList(context_s, R.color.message_list_scroll_to_bottom_fab_bg_normal)));
        }
        catch (Exception ignored)
        {
        }
    }

    static void init_native_audio_stuff()
    {
        final int sampling_rate = 48000;
        final int channels = 1;
        final int sample_count = NGC_AUDIO_PCM_BUFFER_SAMPLES; // bytes = sample_count * 2

        if (Callstate.call_first_audio_frame_received == -1)
        {
            Callstate.call_first_audio_frame_received = System.currentTimeMillis();
            // HINT: PCM_16 needs 2 bytes per sample per channel
            AudioReceiver.buffer_size = ((int) ((48000 * 2) * 2)) * audio_out_buffer_mult; // TODO: this is really bad
            AudioReceiver.sleep_millis = (int) (((float) sample_count / (float) sampling_rate) * 1000.0f *
                                                0.9f); // TODO: this is bad also
            Log.i(TAG, "init_native_audio_stuff:read:init buffer_size=" + AudioReceiver.buffer_size);
            Log.i(TAG, "init_native_audio_stuff:read:init sleep_millis=" + AudioReceiver.sleep_millis);
        }

        if (sampling_rate_ != sampling_rate)
        {
            sampling_rate_ = sampling_rate;
        }

        if (channels_ != channels)
        {
            channels_ = channels;
        }

        if ((NativeAudio.sampling_rate != (int) sampling_rate_) || (NativeAudio.channel_count != channels_))
        {
            Log.i(TAG, "init_native_audio_stuff:values_changed");
            NativeAudio.sampling_rate = (int) sampling_rate_;
            NativeAudio.channel_count = channels_;
            Log.i(TAG, "init_native_audio_stuff:NativeAudio restart Engine");
            // TODO: locking? or something like that
            NativeAudio.restartNativeAudioPlayEngine((int) sampling_rate_, channels_);
        }
        NativeAudio.n_cur_buf = 0;
    }

    static void update_call_ngc_audio_bars()
    {
        try
        {
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (ngc_audio_mute == false)
                        {
                            ngc_audio_bar_in_v.setLevel(get_vu_in() / 90.0f);
                        }
                        ngc_audio_bar_out_v.setLevel(get_vu_out() / 140.0f);
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
