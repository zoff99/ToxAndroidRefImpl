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
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

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

import java.nio.ByteBuffer;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.zoffcc.applications.trifa.CallingActivity.close_calling_activity;
import static com.zoffcc.applications.trifa.MessageListActivity.ml_friend_typing;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    static TextView mt = null;
    static boolean native_lib_loaded = false;
    static String app_files_directory = "";
    // static boolean stop_me = false;
    // static Thread ToxServiceThread = null;
    Handler main_handler = null;
    static Handler main_handler_s = null;
    static Context context_s = null;
    static Activity main_activity_s = null;
    static Notification notification = null;
    // static NotificationManager nMN = null;
    static int NOTIFICATION_ID = 293821038;
    static RemoteViews notification_view = null;
    static long[] friends = null;
    static FriendListFragment friend_list_fragment = null;
    static MessageListFragment message_list_fragment = null;
    static MessageListActivity message_list_activity = null;
    final static String MAIN_DB_NAME = "main.db";
    final static int AddFriendActivity_ID = 10001;
    final static int CallingActivity_ID = 10002;
    final static int ProfileActivity_ID = 10003;
    final static int SettingsActivity_ID = 10004;
    static String temp_string_a = "";
    static ByteBuffer video_buffer_1 = null;
    static ByteBuffer video_buffer_2 = null;
    static TrifaToxService tox_service_fg = null;

    // YUV conversion -------
    static ScriptIntrinsicYuvToRGB yuvToRgb = null;
    static Allocation alloc_in = null;
    static Allocation alloc_out = null;
    static Bitmap video_frame_image = null;
    static int buffer_size_in_bytes = 0;
    // YUV conversion -------

    // main drawer ----------
    Drawer main_drawer = null;
    AccountHeader main_drawer_header = null;
    // main drawer ----------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);


        mt = (TextView) this.findViewById(R.id.main_maintext);
        mt.setText("...");

        main_handler = new Handler(getMainLooper());
        main_handler_s = main_handler;
        context_s = this.getBaseContext();
        main_activity_s = this;

        // get permission ----------
        MainActivityPermissionsDispatcher.dummyForPermissions001WithCheck(this);
        // get permission ----------

        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Profile").withIcon(GoogleMaterial.Icon.gmd_face);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Logout/Login").withIcon(GoogleMaterial.Icon.gmd_refresh);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Exit").withIcon(GoogleMaterial.Icon.gmd_exit_to_app);

        Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_lock).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);

        // Create the AccountHeader
        main_drawer_header = new AccountHeaderBuilder().withActivity(this).addProfiles(new ProfileDrawerItem().withName("me").withIcon(d1)).withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
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
                addDrawerItems(item1, new DividerDrawerItem(), item2, item3, item4).
                withTranslucentStatusBar(true).withAccountHeader(main_drawer_header).
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
                                    main_activity_s.startActivityForResult(intent, ProfileActivity_ID);
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
                                    main_activity_s.startActivityForResult(intent, SettingsActivity_ID);
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
                                    init(app_files_directory);
                                    tox_service_fg.tox_thread_start_fg();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 5)
                        {
                            // Exit
                            try
                            {
                                if (is_tox_started)
                                {
                                    tox_service_fg.stop_tox_fg();
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

        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------

        // reset calling state
        Callstate.state = 0;
        Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value;
        Callstate.call_first_video_frame_received = -1;

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
        mt.setText(mt.getText() + "\n" + "c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        mt.setText(mt.getText() + "\n" + "jni-c-toxcore:v" + jnictoxcore_version());

        // --- forground service ---
        // --- forground service ---
        // --- forground service ---
        Intent i = new Intent(this, TrifaToxService.class);
        startService(i);
        // --- forground service ---
        // --- forground service ---
        // --- forground service ---

        // See OrmaDatabaseBuilderBase for other options.
        TrifaToxService.orma = OrmaDatabase.builder(this).name(MAIN_DB_NAME).build();
        // default: "${applicationId}.orma.db"

        app_files_directory = getFilesDir().getAbsolutePath();
        tox_thread_start();
    }

    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
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
                            e.printStackTrace();
                        }
                    }

                    try
                    {
                        if (!is_tox_started)
                        {
                            init(app_files_directory);
                        }

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
    }

    @Override
    protected void onNewIntent(Intent i)
    {
        Log.i(TAG, "onNewIntent:i=" + i);
        super.onNewIntent(i);
    }

    static FriendList main_get_friend(long friendnum)
    {
        FriendList f;
        List<FriendList> fl = TrifaToxService.orma.selectFromFriendList().tox_friendnumEq(friendnum).toList();
        if (fl.size() > 0)
        {
            f = fl.get(0);
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
            return (TrifaToxService.orma.selectFromFriendList().tox_friendnumEq(friendnum).toList().get(0).TOX_CONNECTION);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }

    }

    synchronized static void set_all_friends_offline()
    {

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                TrifaToxService.orma.updateFriendList().
                        TOX_CONNECTION(0).
                        execute();
                friend_list_fragment.set_all_friends_to_offline();
            }
        };
        t.start();
    }

    synchronized static void update_friend_in_db(FriendList f)
    {
        TrifaToxService.orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                tox_public_key_string(f.tox_public_key_string).
                name(f.name).
                status_message(f.status_message).
                TOX_CONNECTION(f.TOX_CONNECTION).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();
    }

    synchronized static void update_friend_in_db_status_message(FriendList f)
    {
        TrifaToxService.orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                status_message(f.status_message).
                execute();
    }

    synchronized static void update_friend_in_db_status(FriendList f)
    {
        TrifaToxService.orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();
    }

    synchronized static void update_friend_in_db_connection_status(FriendList f)
    {
        TrifaToxService.orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                TOX_CONNECTION(f.TOX_CONNECTION).
                execute();
    }

    synchronized static void update_friend_in_db_name(FriendList f)
    {
        TrifaToxService.orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                status_message(f.name).
                execute();
    }

    synchronized static void update_message_in_db(Message m)
    {
        TrifaToxService.orma.updateMessage().
                idEq(m.id).
                read(m.read).
                text(m.text).
                sent_timestamp(m.sent_timestamp).
                rcvd_timestamp(m.rcvd_timestamp).
                filename_fullpath(m.filename_fullpath).
                execute();
    }

    static void update_message_in_db_read_rcvd_timestamp(Message m)
    {
        TrifaToxService.orma.updateMessage().
                idEq(m.id).
                read(m.read).
                rcvd_timestamp(m.rcvd_timestamp).
                execute();
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
                            e.printStackTrace();
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
        super.onBackPressed();
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public native void init(@NonNull String data_dir);

    public native String getNativeLibAPI();

    public static native void update_savedata_file();

    public static native String get_my_toxid();

    public static native void bootstrap();

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

    public static native long tox_friend_by_public_key(@NonNull String friend_public_key_string);

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

    public static native long set_JNI_video_buffer(ByteBuffer buffer, int frame_width_px, int frame_height_px);

    public static native void set_JNI_video_buffer2(ByteBuffer buffer, int frame_width_px, int frame_height_px);
    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------

    // -- this is for incoming video --
    // -- this is for incoming video --
    static void allocate_video_buffer_1(int frame_width_px1, int frame_height_px1, long ystride, long ustride, long vstride)
    {
        if (video_buffer_1 != null)
        {
            // video_buffer_1.clear();
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

    static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled)
    {
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
                        Callstate.state = 1;
                        Callstate.accepted_call = 0;
                        Callstate.call_first_video_frame_received = -1;
                        Callstate.call_start_timestamp = -1;
                        Intent intent = new Intent(context_s, CallingActivity.class);
                        Callstate.friend_number = fn;
                        try
                        {
                            Callstate.friend_name = TrifaToxService.orma.selectFromFriendList().tox_friendnumEq(Callstate.friend_number).toList().get(0).name;
                        }
                        catch (Exception e)
                        {
                            Callstate.friend_name = "Unknown";
                            e.printStackTrace();
                        }
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
        main_handler_s.post(myRunnable);
    }

    synchronized static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
        // Log.i(TAG, "toxav_video_receive_frame:from=" + friend_number + " video width=" + frame_width_px + " video height=" + frame_height_px);
        if (Callstate.call_first_video_frame_received == -1)
        {
            Callstate.call_first_video_frame_received = System.currentTimeMillis();

            // allocate new video buffer on 1 frame
            allocate_video_buffer_1((int) frame_width_px, (int) frame_height_px, ystride, ustride, vstride);

            temp_string_a = "" + (int) ((Callstate.call_first_video_frame_received - Callstate.call_start_timestamp) / 1000) + "s";
            CallingActivity.update_top_text_line(temp_string_a);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
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
                        CallingActivity.mContentView.setImageBitmap(video_frame_image);
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

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        Log.i(TAG, "toxav_call_state:from=" + friend_number + " state=" + a_TOXAV_FRIEND_CALL_STATE);

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
                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (2)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(1)");
                close_calling_activity();
            }
            else if ((old_value > 0) && (a_TOXAV_FRIEND_CALL_STATE == 0))
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(2)");
                close_calling_activity();
            }

        }
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
        Log.i(TAG, "toxav_bit_rate_status:from=" + friend_number + " audio_bit_rate=" + audio_bit_rate + " video_bit_rate=" + video_bit_rate);

        if (Callstate.state == 1)
        {
            Callstate.audio_bitrate = audio_bit_rate;
            Callstate.video_bitrate = video_bit_rate;
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

        // -- notification ------------------
        // -- notification ------------------
        change_notification(a_TOX_CONNECTION);
        // -- notification ------------------
        // -- notification ------------------
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        Log.i(TAG, "friend_name:friend:" + friend_number + " name:" + friend_name);

        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.name = friend_name;
                update_friend_in_db_name(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
        Log.i(TAG, "friend_status_message:friend:" + friend_number + " status message:" + status_message);

        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.status_message = status_message;
                update_friend_in_db_status_message(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
        Log.i(TAG, "friend_status:friend:" + friend_number + " status:" + a_TOX_USER_STATUS);

        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            f.TOX_USER_STATUS = a_TOX_USER_STATUS;
            update_friend_in_db_status(f);

            try
            {
                message_list_activity.set_friend_status_icon();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                friend_list_fragment.modify_friend(f, friend_number);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.TOX_CONNECTION = a_TOX_CONNECTION;
                update_friend_in_db_connection_status(f);

                try
                {
                    message_list_activity.set_friend_connection_status_icon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    friend_list_fragment.modify_friend(f, friend_number);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
        Log.i(TAG, "friend_typing_cb:fn=" + friend_number + " typing=" + typing);
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
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
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        main_handler_s.post(myRunnable);
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
        Log.i(TAG, "friend_read_receipt:friend:" + friend_number + " message_id:" + message_id);

        try
        {
            // there can be older messages with same message_id for this friend! so always take the latest one! -------
            Message m = TrifaToxService.orma.selectFromMessage().
                    message_idEq(message_id).
                    tox_friendnumEq(friend_number).
                    directionEq(1).
                    orderByIdDesc().
                    toList().get(0);
            // there can be older messages with same message_id for this friend! so always take the latest one! -------

            // Log.i(TAG, "friend_read_receipt:m=" + m);
            Log.i(TAG, "friend_read_receipt:m:message_id=" + m.message_id + " text=" + m.text + " friendnum=" + m.tox_friendnum + " read=" + m.read + " direction=" + m.direction);

            if (m != null)
            {
                m.rcvd_timestamp = System.currentTimeMillis();
                m.read = true;
                update_message_in_db_read_rcvd_timestamp(m);
                // TODO this updates all messages. should be done nicer and faster!
                update_message_view();
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

        final String friend_public_key__final = friend_public_key;

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(20); // wait a bit
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                long friendnum = tox_friend_add_norequest(friend_public_key__final); // add friend
                update_savedata_file(); // save toxcore datafile (new friend added)

                FriendList f = new FriendList();
                f.tox_public_key_string = friend_public_key__final;
                f.tox_friendnum = friendnum;
                f.TOX_USER_STATUS = 0;
                f.TOX_CONNECTION = 0;

                try
                {
                    TrifaToxService.orma.insertIntoFriendList(f);
                }
                catch (android.database.sqlite.SQLiteConstraintException e)
                {
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
            }
        };
        t.start();
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        Log.i(TAG, "friend_message:friend:" + friend_number + " message:" + friend_message);

        Message m = new Message();
        m.tox_friendnum = friend_number;
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = friend_message;

        insert_into_message_db(m, true);
    }

    // void test(int i)
    // {
    //    Log.i(TAG, "test:" + i);
    // }

    static void logger(int level, String text)
    {
        Log.i(TAG, text);
    }
    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

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
            Log.i(TAG, "successfully loaded native library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
        }
    }

    public void show_add_friend(View view)
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        // intent.putExtra("key", value);
        startActivityForResult(intent, AddFriendActivity_ID);
    }

    static void insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                TrifaToxService.orma.insertIntoMessage(m);
                if (update_message_view_flag)
                {
                    update_message_view();
                }
            }
        };
        t.start();
    }

    static void insert_into_friendlist_db(final FriendList f)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                TrifaToxService.orma.insertIntoFriendList(f);
            }
        };
        t.start();
    }

    static void delete_friend_all_messages(final long friendnum)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                TrifaToxService.orma.deleteFromMessage().tox_friendnumEq(friendnum).execute();
            }
        };
        t.start();
    }


    static void delete_friend(final long friendnum)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                TrifaToxService.orma.deleteFromFriendList().tox_friendnumEq(friendnum).execute();
            }
        };
        t.start();
    }

    static void update_message_view()
    {
        try
        {
            Log.i(TAG, "update_message_view:001 " + message_list_fragment);
            Log.i(TAG, "update_message_view:002 " + message_list_fragment.isAdded() + " " + message_list_fragment.isVisible());
            // update the message view (if possbile)
            if ((message_list_fragment.isAdded()) && (message_list_fragment.isVisible()))
            {
                Log.i(TAG, "update_message_view:003");
                MainActivity.message_list_fragment.update_all_messages();
                Log.i(TAG, "update_message_view:004");
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
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
                friend_tox_id = friend_tox_id1.toUpperCase().replace(" ", "").replaceFirst("tox:", "").replaceFirst("TOX:", "").replaceFirst("Tox:", "");

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
        Log.i(TAG, "add friend ID:" + friend_tox_id);

        // add friend ---------------
        long friendnum = tox_friend_add(friend_tox_id, "please add me"); // add friend
        Log.i(TAG, "add friend  #:" + friendnum);
        update_savedata_file(); // save toxcore datafile (new friend added)

        if (friendnum > -1)
        {
            // nospam=8 chars, checksum=4 chars
            String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
            Log.i(TAG, "add friend PK:" + friend_public_key);

            FriendList f = new FriendList();
            f.tox_public_key_string = friend_public_key;
            f.tox_friendnum = friendnum;
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

            try
            {
                insert_into_friendlist_db(f);
            }
            catch (android.database.sqlite.SQLiteConstraintException e)
            {
                e.printStackTrace();
            }

            friend_list_fragment.modify_friend(f, friendnum);
        }

        if (friendnum == -1)
        {
            Log.i(TAG, "friend already added, or request already sent");
        }
        // add friend ---------------
    }

    static String get_friend_name_from_num(long friendnum)
    {
        String result = "Unknown";
        try
        {
            result = TrifaToxService.orma.selectFromFriendList().tox_friendnumEq(friendnum).toList().get(0).name;
        }
        catch (Exception e)
        {
            result = "Unknown";
            e.printStackTrace();
        }

        return result;
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

