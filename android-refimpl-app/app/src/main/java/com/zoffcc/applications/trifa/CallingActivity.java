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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;

public class CallingActivity extends AppCompatActivity implements CameraWrapper.CamOpenOverCallback
{
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int FRONT_CAMERA_USED = 1;
    private static final int BACK_CAMERA_USED = 2;
    static int active_camera_type = FRONT_CAMERA_USED;
    private final Handler mHideHandler = new Handler();
    static ImageView mContentView;
    static ImageButton accept_button = null;
    ImageButton decline_button = null;
    static ImageButton camera_toggle_button = null;
    static ImageButton mute_button = null;
    static TextView top_text_line = null;
    static CallingActivity ca = null;
    static String top_text_line_str1 = "";
    static String top_text_line_str2 = "";
    static String top_text_line_str3 = "";
    Handler callactivity_handler = null;
    static Handler callactivity_handler_s = null;
    private static final String TAG = "trifa.CallingActivity";
    Preview preview = null;
    Camera camera = null;
    static CameraPreviewSurfaceview camera_preview_surface_view = null;
    static CameraSurfacePreview cameraSurfacePreview = null;
    static float mPreviewRate = -1f;
    static int front_camera_id = -1;
    static int back_camera_id = -1;
    static int active_camera_id = 0;
    public static final String FRAGMENT_TAG = "camera_preview_fragment_";

    //
    private static final boolean USE_CAM_001 = false;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calling);

        callactivity_handler = new Handler(getMainLooper());
        callactivity_handler_s = callactivity_handler;

        ca = this;

        mVisible = true;
        mContentView = (ImageView) findViewById(R.id.video_view);

        top_text_line = (TextView) findViewById(R.id.top_text_line);
        accept_button = (ImageButton) findViewById(R.id.accept_button);
        decline_button = (ImageButton) findViewById(R.id.decline_button);
        camera_toggle_button = (ImageButton) findViewById(R.id.camera_toggle_button);
        mute_button = (ImageButton) findViewById(R.id.mute_button);

        Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
        mute_button.setImageDrawable(d1);

        Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_camera).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
        camera_toggle_button.setImageDrawable(d2);

        Drawable d3 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(12);
        decline_button.setImageDrawable(d3);
        // #AARRGGBB

        Drawable d4 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(12);
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
                toggle_camera();
                return true;
            }
        });

        if (USE_CAM_001)
        {
            // camera_preview_surface_view = (CameraPreviewSurfaceview) this.findViewById(R.id.video_my_preview_surfaceview);
        }


        initUI();
        initViewParams();

        // ----- camera preview -----
        // ----- camera preview -----
        // ----- camera preview -----
        if (USE_CAM_001)
        {
            // preview = (com.zoffcc.applications.trifa.Preview) findViewById(R.id.video_my_preview);
            preview.setKeepScreenOn(true);
        }
        // ----- camera preview -----
        // ----- camera preview -----
        // ----- camera preview -----

        top_text_line_str1 = Callstate.friend_name;
        top_text_line_str2 = "";
        top_text_line_str3 = "";
        update_top_text_line();

        accept_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    Callstate.accepted_call = 1;

                    toxav_answer(Callstate.friend_number, 10, 10); // these 2 bitrate values are very strange!! sometimes no video incoming!!
                    accept_button.setVisibility(View.GONE);
                    camera_toggle_button.setVisibility(View.VISIBLE);
                    mute_button.setVisibility(View.VISIBLE);

                    Callstate.call_start_timestamp = System.currentTimeMillis();
                    String a = "" + (int) ((Callstate.call_start_timestamp - Callstate.call_init_timestamp) / 1000) + "s";
                    top_text_line_str2 = a;
                    update_top_text_line();
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
                    toxav_call_control(Callstate.friend_number, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
                    close_calling_activity();
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
        update_top_text_line(top_text_line_str3);
    }

    synchronized public static void update_top_text_line(String text2)
    {
        Log.i(TAG, "update_top_text_line(2):str=" + text2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str1=" + top_text_line_str1);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str2=" + top_text_line_str2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str3=" + top_text_line_str3);

        top_text_line_str3 = text2;

        Log.i(TAG, "update_top_text_line(2b):top_text_line_str3=" + top_text_line_str3);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "update_top_text_line(2c):top_text_line_str3=" + top_text_line_str3);

                    if (top_text_line_str3 != "")
                    {
                        top_text_line.setText(top_text_line_str1 + ":" + top_text_line_str2 + ":" + top_text_line_str3);
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

    @Override
    public void onBackPressed()
    {
        // dont let the user use the back button to close the activity
    }


    private final Runnable mHidePart2Runnable = new Runnable()
    {
        @SuppressLint("InlinedApi")
        @Override
        public void run()
        {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable()
    {
        @Override
        public void run()
        {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            hide();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle()
    {
        if (mVisible)
        {
            hide();
        }
        else
        {
            show();
        }
    }

    private void hide()
    {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show()
    {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    // ----- camera preview -----
    // ----- camera preview -----
    // ----- camera preview -----
    @Override
    protected void onResume()
    {
        super.onResume();

        if (USE_CAM_001)
        {

            int numCams = Camera.getNumberOfCameras();
            Toast.makeText(context_s, "Cameras=" + numCams, Toast.LENGTH_LONG).show();
            Log.i(TAG, "Cameras=" + numCams);
            if (numCams > 0)
            {
                try
                {
                    for (int camNo = 0; camNo < numCams; camNo++)
                    {
                        Camera.CameraInfo camInfo = new Camera.CameraInfo();
                        Camera.getCameraInfo(camNo, camInfo);
                        if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT))
                        {
                            front_camera_id = camNo;
                        }
                        else if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_BACK))
                        {
                            back_camera_id = camNo;
                        }
                    }

                    if (front_camera_id != -1)
                    {
                        camera = Camera.open(front_camera_id);
                        active_camera_id = front_camera_id;
                    }
                    else
                    {
                        camera = Camera.open(back_camera_id);
                        active_camera_id = back_camera_id;
                    }
                    camera.startPreview();
                    preview.setCamera(camera);
                }
                catch (RuntimeException ex)
                {
                    Log.i(TAG, "Camera:099:EE:" + ex.getMessage());
                    Toast.makeText(context_s, "Camera not found", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void toggle_camera()
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

            // -----------------------

            //            camera.stopPreview();
            //            camera.setPreviewCallback(null);
            //            CameraPreviewSurfaceview.camera_preview_size_ = null;
            //            camera.release();
            //
            //            if (active_camera_id == back_camera_id)
            //            {
            //                camera = Camera.open(front_camera_id);
            //                active_camera_id = front_camera_id;
            //            }
            //            else
            //            {
            //                camera = Camera.open(back_camera_id);
            //                active_camera_id = back_camera_id;
            //            }
            //
            //            setCameraDisplayOrientation(this, active_camera_id, camera);
            //
            //            camera.setPreviewDisplay(CallingActivity.camera_preview_surface_view.getHolder());
            //            camera.setPreviewCallback(CallingActivity.camera_preview_surface_view);
            //            camera.startPreview();
            //            // ----------------------------
            //            // ----------------------------
            //            preview.setCamera(camera);
            //            // ----------------------------
            //            // ----------------------------
        }
        catch (Exception e)
        {
        }
    }

    @Override
    protected void onPause()
    {
        if (USE_CAM_001)
        {
            if (camera != null)
            {
                camera.stopPreview();
                preview.setCamera(null);
                camera.setPreviewCallback(null);
                CameraPreviewSurfaceview.camera_preview_size_ = null;
                camera.release();
                camera = null;
            }
        }
        super.onPause();
    }

    private void resetCam()
    {
        if (USE_CAM_001)
        {
            camera.startPreview();
            preview.setCamera(camera);
        }
    }
    // ----- camera preview -----
    // ----- camera preview -----
    // ----- camera preview -----


    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera)
    {
        try
        {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation)
            {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            }
            else
            {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
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
        SurfaceHolder holder = this.cameraSurfacePreview.getSurfaceHolder();
        CameraWrapper.getInstance().doStartPreview(holder, mPreviewRate);
    }
}
