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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.format_timeduration_from_seconds;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;
import static com.zoffcc.applications.trifa.MainActivity.update_bitrates;
import static com.zoffcc.applications.trifa.MainActivity.update_fps;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;

public class CallingActivity extends AppCompatActivity implements CameraWrapper.CamOpenOverCallback, SensorEventListener
{
    // private static final boolean AUTO_HIDE = true;
    // private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    // private static final int UI_ANIMATION_DELAY = 300;
    private static final int FRONT_CAMERA_USED = 1;
    private static final int BACK_CAMERA_USED = 2;
    static int active_camera_type = FRONT_CAMERA_USED;
    // private final Handler mHideHandler = new Handler();
    static ImageView mContentView;
    static ImageButton accept_button = null;
    ImageButton decline_button = null;
    static ImageButton camera_toggle_button = null;
    static ImageButton mute_button = null;
    static ImageView audio_device_icon = null;
    static TextView top_text_line = null;
    static CallingActivity ca = null;
    static String top_text_line_str1 = "";
    static String top_text_line_str2 = "";
    static String top_text_line_str3 = "";
    static String top_text_line_str4 = "";
    Handler callactivity_handler = null;
    static Handler callactivity_handler_s = null;
    private static final String TAG = "trifa.CallingActivity";
    // Camera camera = null;
    static CameraSurfacePreview cameraSurfacePreview = null;
    static float mPreviewRate = -1f;
    // static int front_camera_id = -1;
    // static int back_camera_id = -1;
    // static int active_camera_id = 0;
    static AudioRecording audio_thread = null;
    static AudioReceiver audio_receiver_thread = null;
    private SensorManager sensor_manager = null;
    private Sensor proximity_sensor = null;
    PowerManager pm = null;
    PowerManager.WakeLock wl1 = null;
    PowerManager.WakeLock wl2 = null;
    TextView right_top_text_1 = null;
    TextView right_top_text_2 = null;
    TextView right_top_text_3 = null;
    TextView right_top_text_4 = null;
    TextView right_left_text_1 = null;
    static int activity_state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calling);

        ca = this;

        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity_sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        callactivity_handler = new Handler(getMainLooper());
        callactivity_handler_s = callactivity_handler;

        // set volume control -------------
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        manager.setMode(AudioManager.MODE_NORMAL);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        // set volume control -------------

        mVisible = true;
        mContentView = (ImageView) findViewById(R.id.video_view);

        right_top_text_1 = (TextView) findViewById(R.id.right_top_text_1);
        right_top_text_2 = (TextView) findViewById(R.id.right_top_text_2);
        right_top_text_3 = (TextView) findViewById(R.id.right_top_text_3);
        right_top_text_4 = (TextView) findViewById(R.id.right_top_text_4);
        right_left_text_1 = (TextView) findViewById(R.id.right_left_text_1);

        update_bitrates();
        update_fps();
        update_call_time();

        top_text_line = (TextView) findViewById(R.id.top_text_line);
        accept_button = (ImageButton) findViewById(R.id.accept_button);
        decline_button = (ImageButton) findViewById(R.id.decline_button);
        camera_toggle_button = (ImageButton) findViewById(R.id.camera_toggle_button);
        mute_button = (ImageButton) findViewById(R.id.mute_button);
        audio_device_icon = (ImageView) findViewById(R.id.audio_device_icon);

        Drawable d2 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_headset).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        audio_device_icon.setImageDrawable(null);

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        mute_button.setImageDrawable(d1);
        mute_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    try
                    {
                        if (audio_manager_s.isMicrophoneMute())
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
                        }
                        else
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
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
                        if (audio_manager_s.isMicrophoneMute())
                        {
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                            audio_manager_s.setMicrophoneMute(false);
                        }
                        else
                        {
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                            audio_manager_s.setMicrophoneMute(true);
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

        final Drawable d5 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_camera).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        camera_toggle_button.setImageDrawable(d5);

        final Drawable d3 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
        decline_button.setImageDrawable(d3);
        // #AARRGGBB

        final Drawable d4 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
        accept_button.setImageDrawable(d4);

        if (Callstate.accepted_call == 1)
        {
            accept_button.setVisibility(View.GONE);
            camera_toggle_button.setVisibility(View.VISIBLE);
            mute_button.setVisibility(View.VISIBLE);
        }
        else
        {
            accept_button.setVisibility(View.VISIBLE);
            camera_toggle_button.setVisibility(View.GONE);
            mute_button.setVisibility(View.GONE);
        }


        camera_toggle_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    Drawable d2a = new IconicsDrawable(v.getContext()).icon(FontAwesome.Icon.faw_camera).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.md_green_600)).sizeDp(7);
                    camera_toggle_button.setImageDrawable(d2a);
                }
                else
                {
                    Drawable d2a = new IconicsDrawable(v.getContext()).icon(FontAwesome.Icon.faw_camera).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                    camera_toggle_button.setImageDrawable(d2a);

                    final Thread toggle_thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(20);
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }
                            toggle_camera();
                        }
                    };
                    toggle_thread.start();
                }

                return true;
            }
        });

        initUI();
        initViewParams();

        top_text_line_str1 = Callstate.friend_name;
        top_text_line_str2 = "";
        top_text_line_str3 = "";
        top_text_line_str4 = "";
        update_top_text_line();

        accept_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                    {
                    }
                    else
                    {
                        if (Callstate.accepted_call != 1)
                        {
                            Callstate.accepted_call = 1;

                            Log.i(TAG, "answer button pressed");
                            toxav_answer(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE); // these 2 bitrate values are very strange!! sometimes no video incoming!!
                            // need to set our state manually here, no callback from toxcore :-(
                            Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V.value;
                            // need to set our state manually here, no callback from toxcore :-(
                            accept_button.setVisibility(View.GONE);
                            camera_toggle_button.setVisibility(View.VISIBLE);
                            mute_button.setVisibility(View.VISIBLE);

                            Callstate.call_start_timestamp = System.currentTimeMillis();
                            String a = "" + (int) ((Callstate.call_start_timestamp - Callstate.call_init_timestamp) / 1000) + "s";
                            top_text_line_str2 = a;
                            update_top_text_line();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });

        decline_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                    {
                    }
                    else
                    {
                        Log.i(TAG, "decline button pressed");
                        toxav_call_control(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
                        close_calling_activity();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    public static void close_calling_activity()
    {
        Callstate.reset_values();
        // close calling activity --------
        ca.finish();
        // close calling activity --------
    }

    synchronized public static void update_top_text_line()
    {
        Log.i(TAG, "update_top_text_line(1):top_text_line_str3=" + top_text_line_str3);
        update_top_text_line(top_text_line_str3, 3);
    }

    synchronized public static void update_top_text_line(String text2, int linenum)
    {
        Log.i(TAG, "update_top_text_line(2):str=" + text2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str1=" + top_text_line_str1);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str2=" + top_text_line_str2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str3=" + top_text_line_str3);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str4=" + top_text_line_str4);

        if (linenum == 3)
        {
            top_text_line_str3 = text2;
        }
        else if (linenum == 4)
        {
            top_text_line_str4 = text2;
        }

        Log.i(TAG, "update_top_text_line(2b):top_text_line_str3=" + top_text_line_str3);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "update_top_text_line(2c):top_text_line_str3=" + top_text_line_str3);

                    if ((top_text_line_str3 != "") || (top_text_line_str4 != ""))
                    {
                        top_text_line.setText(top_text_line_str1 + ":" + top_text_line_str2 + ":" + top_text_line_str3 + ":" + top_text_line_str4);
                    }
                    else
                    {
                        if (top_text_line_str2 != "")
                        {
                            top_text_line.setText(top_text_line_str1 + ":" + top_text_line_str2);
                        }
                        else
                        {
                            top_text_line.setText(top_text_line_str1);
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    private void initializeScreenshotSecurity()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        else
        {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onBackPressed()
    {
        // dont let the user use the back button to close the activity
    }


    //    private final Runnable mHidePart2Runnable = new Runnable()
    //    {
    //        @SuppressLint("InlinedApi")
    //        @Override
    //        public void run()
    //        {
    //            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    //        }
    //    };

    //    private final Runnable mShowPart2Runnable = new Runnable()
    //    {
    //        @Override
    //        public void run()
    //        {
    //            ActionBar actionBar = getSupportActionBar();
    //            if (actionBar != null)
    //            {
    //                actionBar.show();
    //            }
    //        }
    //    };

    private boolean mVisible;
    //    private final Runnable mHideRunnable = new Runnable()
    //    {
    //        @Override
    //        public void run()
    //        {
    //            hide();
    //        }
    //    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // delayedHide(100);
    }

    //    private void toggle()
    //    {
    //        if (mVisible)
    //        {
    //            hide();
    //        }
    //        else
    //        {
    //            show();
    //        }
    //    }

    //    private void hide()
    //    {
    //        // Hide UI first
    //        ActionBar actionBar = getSupportActionBar();
    //        if (actionBar != null)
    //        {
    //            actionBar.hide();
    //        }
    //        mVisible = false;
    //
    //        // Schedule a runnable to remove the status and navigation bar after a delay
    //        mHideHandler.removeCallbacks(mShowPart2Runnable);
    //        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    //    }

    //    @SuppressLint("InlinedApi")
    //    private void show()
    //    {
    //        // Show the system bar
    //        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    //        mVisible = true;
    //
    //        // Schedule a runnable to display UI elements after a delay
    //        mHideHandler.removeCallbacks(mHidePart2Runnable);
    //        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    //    }

    //    private void delayedHide(int delayMillis)
    //    {
    //        mHideHandler.removeCallbacks(mHideRunnable);
    //        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    //    }


    @Override
    protected void onResume()
    {
        super.onResume();

        activity_state = 1;

        sensor_manager.registerListener(this, proximity_sensor, SensorManager.SENSOR_DELAY_NORMAL);

        try
        {
            if (audio_thread.stopped)
            {
                audio_thread = new AudioRecording();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (audio_receiver_thread.stopped)
            {
                audio_receiver_thread = new AudioReceiver();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // update call time every second -----------
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                // Log.i(TAG, "update_call_time -> call");
                update_call_time();
                if (activity_state != 0)
                {
                    ha.postDelayed(this, 1000);
                }
            }
        }, 1000);
        // update call time every second -----------

    }

    void toggle_camera()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CameraWrapper.getInstance().doStopCamera();

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        CameraWrapper.camera_preview_size2 = null;
                        active_camera_type = BACK_CAMERA_USED;
                        CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, false);
                    }
                    else
                    {
                        CameraWrapper.camera_preview_size2 = null;
                        active_camera_type = FRONT_CAMERA_USED;
                        CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    // -------------------------------------------------------
    // TODO
    // this is a bad workaround to really show the cam preview
    // -------------------------------------------------------
    static void reinit_camera(CallingActivity c)
    {
        try
        {
            Log.i(TAG, "active_camera_type(1)=" + active_camera_type);
            CameraWrapper.getInstance().doStopCamera();
            Log.i(TAG, "active_camera_type(2)=" + active_camera_type);
            CameraWrapper.camera_preview_size2 = null;
            Log.i(TAG, "active_camera_type(3)=" + active_camera_type);
            CameraWrapper.getInstance().doOpenCamera(c, true);
            Log.i(TAG, "active_camera_type(4)=" + active_camera_type);
        }
        catch (Exception e)
        {
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        sensor_manager.unregisterListener(this);
        activity_state = 0;

        try
        {
            if (wl1 != null)
            {
                if (wl1.isHeld())
                {
                    wl1.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            if (wl2 != null)
            {
                if (wl2.isHeld())
                {
                    wl2.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!audio_thread.stopped)
            {
                audio_thread.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!audio_receiver_thread.stopped)
            {
                audio_receiver_thread.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // ---------------
    @Override
    protected void onStart()
    {
        Log.i(TAG, "onStart");
        super.onStart();

        Thread openThread = new Thread()
        {
            @Override
            public void run()
            {
                active_camera_type = FRONT_CAMERA_USED;
                CameraWrapper.camera_preview_size2 = null;
                CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);
            }
        };
        openThread.start();
    }

    private void initUI()
    {
        cameraSurfacePreview = (CameraSurfacePreview) findViewById(R.id.camera_surfaceview);
    }

    private void initViewParams()
    {
        ViewGroup.LayoutParams params = cameraSurfacePreview.getLayoutParams();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        params.width = screenWidth;
        params.height = screenHeight;
        this.mPreviewRate = (float) screenHeight / (float) screenWidth;
        cameraSurfacePreview.setLayoutParams(params);
    }

    @Override
    public void cameraHasOpened()
    {
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Callstate.camera_opened = true;
        SurfaceHolder holder = this.cameraSurfacePreview.getSurfaceHolder();
        CameraWrapper.getInstance().doStartPreview(holder, mPreviewRate);
    }

    public void turnOnScreen()
    {
        // turn on screen
        try
        {
            if (wl2 != null)
            {
                if (wl2.isHeld())
                {
                    wl2.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turn*ON*Screen");
        Callstate.other_video_enabled = 1;
        Callstate.my_video_enabled = 1;

        if (wl1 == null)
        {
            wl1 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "trifa_screen_on");
        }

        try
        {
            if (wl1 != null)
            {
                if (!wl1.isHeld())
                {
                    wl1.acquire();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @TargetApi(21)
    public void turnOffScreen()
    {
        try
        {
            if (wl1 != null)
            {
                if (wl1.isHeld())
                {
                    wl1.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        Log.i(TAG, "turnOffScreen");
        Callstate.other_video_enabled = 0;
        Callstate.my_video_enabled = 0;

        // turn off screen
        if (wl2 == null)
        {
            wl2 = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "trifa_screen_OFF");
        }


        try
        {
            if (wl2 != null)
            {
                if (!wl2.isHeld())
                {
                    wl2.acquire();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY)
        {
            // Log.i(TAG, "onSensorChanged:value=" + event.values[0] + " max=" + proximity_sensor.getMaximumRange());

            if (event.values[0] < proximity_sensor.getMaximumRange())
            {
                // close to ear
                if (Callstate.audio_speaker == true)
                {
                    Callstate.audio_speaker = false;
                    try
                    {
                        if (!audio_manager_s.isWiredHeadsetOn())
                        {
                            audio_manager_s.setSpeakerphoneOn(false);
                        }

                        try
                        {
                            turnOffScreen();
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Callstate.audio_speaker = true;
                    }
                }
            }
            else
            {
                // away from ear
                if (Callstate.audio_speaker == false)
                {
                    Callstate.audio_speaker = true;
                    try
                    {
                        if (!audio_manager_s.isWiredHeadsetOn())
                        {
                            audio_manager_s.setSpeakerphoneOn(true);
                        }

                        try
                        {
                            turnOnScreen();
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Callstate.audio_speaker = false;
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    void update_call_time()
    {
        if (Callstate.accepted_call == 1)
        {
            if (Callstate.call_start_timestamp != -1)
            {

                right_left_text_1.setText(format_timeduration_from_seconds((System.currentTimeMillis() - Callstate.call_start_timestamp) / 1000));
            }
            else
            {
                right_left_text_1.setText("...");
            }
        }
        else
        {
            right_left_text_1.setText("...");
        }
    }

    static void update_audio_device_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Callstate.audio_device == 0)
                    {
                        audio_device_icon.setImageDrawable(null);
                    }
                    else if (Callstate.audio_device == 1)
                    {
                        Drawable d4 = new IconicsDrawable(ca).icon(GoogleMaterial.Icon.gmd_headset).backgroundColor(Color.TRANSPARENT).color(ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else if (Callstate.audio_device == 2)
                    {
                        Drawable d4 = new IconicsDrawable(ca).icon(GoogleMaterial.Icon.gmd_bluetooth_audio).backgroundColor(Color.TRANSPARENT).color(ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else // audio_device == ??
                    {
                        audio_device_icon.setImageDrawable(null);
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

}
