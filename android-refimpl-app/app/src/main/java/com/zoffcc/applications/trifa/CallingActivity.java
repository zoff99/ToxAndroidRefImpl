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
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.speech.levelmeter.BarLevelDrawable;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_aec_active;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_in;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_out;
import static com.zoffcc.applications.nativeaudio.NativeAudio.set_aec_active;
import static com.zoffcc.applications.trifa.CameraWrapper.camera_preview_call_back_ts_first_frame;
import static com.zoffcc.applications.trifa.CameraWrapper.getRotation;
import static com.zoffcc.applications.trifa.CustomVideoImageView.video_output_orentation_update;
import static com.zoffcc.applications.trifa.HeadsetStateReceiver.isBluetoothConnected;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.format_timeduration_from_seconds;
import static com.zoffcc.applications.trifa.HelperGeneric.get_vfs_image_filename_friend_avatar;
import static com.zoffcc.applications.trifa.HelperGeneric.put_vfs_image_on_imageview_real;
import static com.zoffcc.applications.trifa.HelperGeneric.reset_audio_mode;
import static com.zoffcc.applications.trifa.HelperGeneric.set_audio_to_ear;
import static com.zoffcc.applications.trifa.HelperGeneric.set_audio_to_loudspeaker;
import static com.zoffcc.applications.trifa.HelperGeneric.set_calling_audio_mode;
import static com.zoffcc.applications.trifa.HelperGeneric.update_bitrates;
import static com.zoffcc.applications.trifa.HelperGeneric.update_fps;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_misc_button_enabled;
import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_screen_off_in_audio_call;
import static com.zoffcc.applications.trifa.MainActivity.PREF__audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.PREF__h264_encoder_use_intra_refresh;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_H264_hw_encoding;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_camera_x;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_software_aec;
import static com.zoffcc.applications.trifa.MainActivity.PREF__video_call_quality;
import static com.zoffcc.applications.trifa.MainActivity.PREF__video_cam_resolution;
import static com.zoffcc.applications.trifa.MainActivity.PREF__video_play_delay_ms;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.set_audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_set_onion_active;
import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_INIT_PLAY_DELAY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_PLAY_DELAY_SETTING_NAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_HIGH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_LOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_MED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_HIGH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_LOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_MED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MIN_BITRATE_HIGH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MIN_BITRATE_LOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MIN_BITRATE_MED;

public class CallingActivity extends AppCompatActivity implements CameraWrapper.CamOpenOverCallback, SensorEventListener
{
    // private static final boolean AUTO_HIDE = true;
    // private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    // private static final int UI_ANIMATION_DELAY = 300;
    static final int FRONT_CAMERA_USED = 1;
    static final int BACK_CAMERA_USED = 2;
    static int active_camera_type = FRONT_CAMERA_USED;
    static CustomVideoImageView mContentView;
    private Thread Videocall_audio_play_thread = null;
    private boolean Videocall_audio_play_thread_running = false;
    static ViewGroup calling_activity_top_viewgroup_vg;
    static ImageView caller_avatar_view;
    static ImageButton accept_button = null;
    ImageButton decline_button = null;
    static ImageButton camera_toggle_button = null;
    static ImageButton mute_button = null;
    static Button video_box_speaker_button = null;
    static boolean video_speaker_state = true;
    static Button video_box_aec_button = null;
    static View video_box_aec = null;
    ImageButton misc_button = null;
    TextView misc_button_pad = null;
    static View calling_friend_online_status = null;
    static ImageView audio_device_icon = null;
    static TextView top_text_line = null;
    static CallingActivity ca = null;
    static String top_text_line_str1 = "";
    static String top_text_line_str2 = "";
    static String top_text_line_str3 = "";
    static String top_text_line_str4 = "";
    Handler callactivity_handler = null;
    static Handler callactivity_handler_s = null;
    static boolean trifa_is_MicrophoneMute = false;
    private static final String TAG = "trifa.CallingActivity";
    static CameraSurfacePreview cameraSurfacePreview = null;
    static PreviewView cameraXPreview = null;
    static CameraDrawingOverlay drawingOverlay = null;
    static Object videoFrameAnalyser = null;
    static VideoFrameAnalyserTFLite videoFrameAnalyser_tflite = null;
    static ProcessCameraProvider cameraProvider = null;
    static ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = null;
    static float mPreviewRate = -1f;
    // static int front_camera_id = -1;
    // static int back_camera_id = -1;
    // static int active_camera_id = 0;
    static AudioRecording audio_thread = null;
    static AudioReceiver audio_receiver_thread = null;
    private SensorManager sensor_manager = null;
    private Sensor proximity_sensor = null;
    private Sensor accelerometer_sensor = null;
    static int device_orientation = 0;
    public static String channelId = "";
    static NotificationChannel notification_channel_call_audio_play_service = null;
    static NotificationManager nmn3 = null;
    PowerManager pm = null;
    PowerManager.WakeLock wl1 = null;
    PowerManager.WakeLock wl2 = null;
    static TextView right_top_text_1 = null;
    static TextView right_top_text_1b = null;
    static TextView right_top_text_2 = null;
    static TextView right_top_text_3 = null;
    static TextView right_top_text_4 = null;
    static TextView right_left_text_1 = null;
    static TextView debug001_text = null;
    static View box_right_volumeslider_01 = null;
    static SeekBar volume_slider_seekbar_01 = null;
    View box_right_video_add_delay_slider_01 = null;
    static SeekBar video_add_delay_slider_seekbar_01 = null;
    static TextView video_add_delay_slider_infotext_01 = null;
    BarLevelDrawable audio_bar_in_v = null;
    BarLevelDrawable audio_bar_out_v = null;
    static int activity_state = 0;
    com.etiennelawlor.discreteslider.library.ui.DiscreteSlider quality_slider = null;
    TextView text_vq_low = null;
    TextView text_vq_med = null;
    TextView text_vq_high = null;
    static View video_box_self_preview_01 = null;
    static View video_box_left_top_01 = null;
    static View video_box_right_top_01 = null;
    final static String MIME_TYPE = "video/avc";   // H.264 Advanced Video Coding
    final static int FRAME_RATE = 20;              // ~ estimated fps
    final static int IFRAME_INTERVAL = 1;          // n seconds between I-frames
    final static int IFRAME_INTERVAL_START = 1;          // n seconds between I-frames
    private static MediaCodec.BufferInfo mBufferInfo;
    private static MediaCodec mEncoder;
    private static MediaPlayer mMediaPlayer = null;
    private static MediaFormat video_encoder_format = null;
    private static int video_encoder_width = 640; // start a dummy start value, DO NOT CHANGE
    private static int video_encoder_height = 480; // start a dummy start value, DO NOT CHANGE
    private static int v_bitrate_bits_per_second = 20 * 1000; // video bitrate <n> bps, in bits per second
    private static long encode_last_v_bitrate_change = -1;
    public static byte[] global_sps_pps_nal_unit_bytes = null;
    public static int send_sps_pps_every_x_frames_current = 0;
    public static int send_sps_pps_every_x_frames = 2;
    public static int set_vdelay_every_x_frames_current = 0;
    public static int set_vdelay_every_x_frames = 100;
    private static float slider_alpha = 0.3f;
    static boolean camera_toggle_button_pressed = false;
    static byte[] arr_h264_enc = null;

    private static MediaCodec.BufferInfo mBufferInfo_h264_decoder;
    private static MediaCodec mDecoder_h264;
    private static MediaFormat video_decoder_h264_format = null;
    private static DetectHeadset dh = null;
    public static long calling_activity_start_ms = 0;
    private static int BUFFER_DEQUEUE_TIMEOUT_US = 0; // "us" fetch encoded data from encoder
    private static int BUFFER_DEQUEUE_FEEDER_TIMEOUT_US = 0; // "us" feed raw data to encoder

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // startActivity called from non-Activity context
        Log.i(TAG, "onCreate:01");

        try
        {
            ConfGroupAudioService.stop_me(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 27)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null)
            {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }
        else
        {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                 WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        super.onCreate(savedInstanceState);

        calling_activity_start_ms = System.currentTimeMillis();

        Log.i(TAG, "onCreate:02");

        dh = new DetectHeadset(this);

        setContentView(R.layout.activity_calling);

        SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PREF__video_play_delay_ms = settings_cs1.getInt(GLOBAL_PLAY_DELAY_SETTING_NAME, GLOBAL_INIT_PLAY_DELAY);
        Log.i(TAG, "pref:get:PREF__video_play_delay_ms=" + PREF__video_play_delay_ms);
        PREF__audio_play_volume_percent = settings_cs1.getInt("audio_play_volume_percent", 100);
        Log.i(TAG, "pref:get:PREF__audio_play_volume_percent=" + PREF__audio_play_volume_percent);

        top_text_line = (TextView) findViewById(R.id.top_text_line);
        accept_button = (ImageButton) findViewById(R.id.accept_button);
        decline_button = (ImageButton) findViewById(R.id.decline_button);
        camera_toggle_button = (ImageButton) findViewById(R.id.camera_toggle_button);
        mute_button = (ImageButton) findViewById(R.id.mute_button);
        video_box_aec_button = findViewById(R.id.video_box_aec_button);
        video_box_speaker_button = findViewById(R.id.video_box_speaker_button);
        video_box_aec = findViewById(R.id.video_box_aec);
        audio_device_icon = (ImageView) findViewById(R.id.audio_device_icon);
        misc_button = (ImageButton) findViewById(R.id.misc_button);
        misc_button_pad = (TextView) findViewById(R.id.misc_button_pad);
        calling_friend_online_status = (View) findViewById(R.id.calling_friend_online_status);

        int conn_status = tox_friend_get_connection_status(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));
        if (conn_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_UDP.value)
        {
            calling_friend_online_status.setBackgroundColor(Color.parseColor("#04b431"));
        }
        else if (conn_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value)
        {
            calling_friend_online_status.setBackgroundColor(Color.parseColor("#ffce00"));
        }
        else
        {
            calling_friend_online_status.setBackgroundColor(Color.parseColor("#ff0000"));
        }

        audio_bar_in_v = (BarLevelDrawable) findViewById(R.id.audio_bar_in_v);
        audio_bar_out_v = (BarLevelDrawable) findViewById(R.id.audio_bar_out_v);

        if (PREF__window_security)
        {
            // prevent screenshots and also dont show the window content in recent activity screen
            initializeScreenshotSecurity(this);
        }

        trifa_is_MicrophoneMute = false;
        ca = this;

        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity_sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometer_sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        callactivity_handler = new Handler(getMainLooper());
        callactivity_handler_s = callactivity_handler;

        // set volume control -------------
        System.out.println("AVCS:MUSIC:0");
        try
        {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }

        // set volume control -------------

        boolean mVisible = true;
        mContentView = (CustomVideoImageView) findViewById(R.id.video_view);

        calling_activity_top_viewgroup_vg = (ViewGroup) findViewById(R.id.calling_activity_top_viewgroup);

        caller_avatar_view = (ImageView) findViewById(R.id.caller_avatar_view);

        video_box_self_preview_01 = findViewById(R.id.video_box_self_preview_01);
        video_box_left_top_01 = findViewById(R.id.video_box_left_top_01);
        video_box_right_top_01 = findViewById(R.id.video_box_right_top_01);

        video_box_self_preview_01.setVisibility(View.INVISIBLE);
        video_box_left_top_01.setVisibility(View.INVISIBLE);
        video_box_right_top_01.setVisibility(View.INVISIBLE);

        volume_slider_seekbar_01 = (SeekBar) findViewById(R.id.volume_slider_seekbar);
        video_add_delay_slider_seekbar_01 = (SeekBar) findViewById(R.id.video_add_delay_slider_seekbar);
        video_add_delay_slider_infotext_01 = (TextView) findViewById(R.id.video_add_delay_slider_infotext);

        box_right_volumeslider_01 = (View) findViewById(R.id.video_box_right_volumeslider_01);
        box_right_volumeslider_01.setVisibility(View.VISIBLE);
        box_right_volumeslider_01.setAlpha(slider_alpha);


        volume_slider_seekbar_01.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:action:" + event.getAction());

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        box_right_volumeslider_01.setAlpha(slider_alpha);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                else if ((event.getAction() == MotionEvent.ACTION_DOWN) ||
                         (event.getAction() == MotionEvent.ACTION_CANCEL))
                {
                    try
                    {
                        box_right_volumeslider_01.setAlpha(1.0f);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                return false;
            }
        });

        volume_slider_seekbar_01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar s, int progress_value, boolean from_user)
            {
                if ((progress_value >= 0) && (progress_value <= 100))
                {
                    PREF__audio_play_volume_percent = progress_value;
                    try
                    {
                        set_audio_play_volume_percent(PREF__audio_play_volume_percent);
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                    try
                    {
                        SharedPreferences settings_cs2 = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());
                        settings_cs2.edit().putInt("audio_play_volume_percent",
                                                   PREF__audio_play_volume_percent).apply();
                        Log.i(TAG, "pref:set:PREF__audio_play_volume_percent=" + PREF__audio_play_volume_percent);
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                        Log.i(TAG, "pref:set:PREF__audio_play_volume_percent:EE:" + ee.getMessage());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });


        box_right_video_add_delay_slider_01 = (View) findViewById(R.id.video_box_right_video_add_delay_slider_01);
        box_right_video_add_delay_slider_01.setVisibility(View.VISIBLE);
        box_right_video_add_delay_slider_01.setAlpha(slider_alpha);


        video_add_delay_slider_seekbar_01.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:action:" + event.getAction());

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        box_right_video_add_delay_slider_01.setAlpha(slider_alpha);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                else if ((event.getAction() == MotionEvent.ACTION_DOWN) ||
                         (event.getAction() == MotionEvent.ACTION_CANCEL))
                {
                    try
                    {
                        box_right_video_add_delay_slider_01.setAlpha(1.0f);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                return false;
            }
        });


        video_add_delay_slider_seekbar_01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar s, int progress_value, boolean from_user)
            {
                if ((progress_value >= 0) && (progress_value <= 100))
                {
                    try
                    {
                        PREF__video_play_delay_ms = progress_value * 5;
                        try
                        {
                            toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                             ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_ADD_DELAY_MS.value,
                                             -PREF__video_play_delay_ms);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "TOXAV_DECODER_VIDEO_ADD_DELAY_MS:EE:" + e.getMessage());
                        }

                        try
                        {
                            int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                       ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value,
                                                       PREF__video_play_delay_ms);
                            Log.i(TAG, "decoder buffer set to ms=" + TRIFAGlobals.VIDEO_DECODER_BUFFER_DELAY + ":res=" +
                                       res);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "TOXAV_DECODER_VIDEO_BUFFER_MS:EE:" + e.getMessage());
                        }

                        video_add_delay_slider_infotext_01.setText(
                                getString(R.string.CallingActivity_23) + " " + PREF__video_play_delay_ms + " ms");

                        SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());

                        settings_cs1.edit().putInt(GLOBAL_PLAY_DELAY_SETTING_NAME, PREF__video_play_delay_ms).apply();
                        Log.i(TAG, "pref:set:PREF__video_play_delay_ms=" + PREF__video_play_delay_ms);

                        if (PREF__video_play_delay_ms > 490)
                        {
                        }
                        else if (PREF__video_play_delay_ms < 0)
                        {
                        }
                        else
                        {
                        }
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                        Log.i(TAG, "pref:set:PREF__video_play_delay_ms:EE:" + ee.getMessage());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });


        try
        {
            final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
            caller_avatar_view.setImageDrawable(d1);

            String fname = get_vfs_image_filename_friend_avatar(
                    tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));

            if (fname != null)
            {
                put_vfs_image_on_imageview_real(this, caller_avatar_view, d1, fname, false, true, main_get_friend(
                        tox_friend_by_public_key__wrapper(Callstate.friend_pubkey)));
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        right_top_text_1 = (TextView) findViewById(R.id.right_top_text_1);
        right_top_text_1b = (TextView) findViewById(R.id.right_top_text_1b);
        right_top_text_2 = (TextView) findViewById(R.id.right_top_text_2);
        right_top_text_3 = (TextView) findViewById(R.id.right_top_text_3);
        right_top_text_4 = (TextView) findViewById(R.id.right_top_text_4);
        right_left_text_1 = (TextView) findViewById(R.id.right_left_text_1);
        quality_slider = (DiscreteSlider) findViewById(R.id.quality_slider);
        text_vq_low = (TextView) findViewById(R.id.text_vq_low);
        text_vq_med = (TextView) findViewById(R.id.text_vq_med);
        text_vq_high = (TextView) findViewById(R.id.text_vq_high);

        debug001_text = (TextView) findViewById(R.id.debug001_text);
        debug001_text.setText("VIDEO");
        if (Callstate.audio_call)
        {
            debug001_text.setText("*AUDIO*");
        }

        // ------ only for debugging ------
        debug001_text.setVisibility(View.INVISIBLE);
        // ------ only for debugging ------

        text_vq_low.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_LOW;

                        if (PREF__video_cam_resolution == 2)
                        {
                            max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_LOW * 2;
                        }
                        else if (PREF__video_cam_resolution == 1)
                        {
                            max_video_bitrate_ = (int) (VIDEO_ENCODER_MAX_BITRATE_LOW * 1.5);
                        }

                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    max_video_bitrate_);

                        int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value,
                                                    VIDEO_ENCODER_MIN_BITRATE_LOW);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_LOW);
                        if (res != 0)
                        {
                            PREF__video_call_quality = 0;
                            quality_slider.setPosition(PREF__video_call_quality);

                            SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext());
                            settings_cs1.edit().putString("video_call_quality", "" + PREF__video_call_quality).apply();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        text_vq_med.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_MED;

                        if (PREF__video_cam_resolution == 2)
                        {
                            max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_MED * 2;
                        }
                        else if (PREF__video_cam_resolution == 1)
                        {
                            max_video_bitrate_ = (int) (VIDEO_ENCODER_MAX_BITRATE_MED * 1.5);
                        }

                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    max_video_bitrate_);

                        int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value,
                                                    VIDEO_ENCODER_MIN_BITRATE_MED);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_MED);
                        if (res != 0)
                        {
                            PREF__video_call_quality = 1;
                            quality_slider.setPosition(PREF__video_call_quality);

                            SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext());
                            settings_cs1.edit().putString("video_call_quality", "" + PREF__video_call_quality).apply();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        text_vq_high.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_HIGH;

                        if (PREF__video_cam_resolution == 2)
                        {
                            max_video_bitrate_ = VIDEO_ENCODER_MAX_BITRATE_HIGH * 2;
                        }
                        else if (PREF__video_cam_resolution == 1)
                        {
                            max_video_bitrate_ = (int) (VIDEO_ENCODER_MAX_BITRATE_HIGH * 1.5);
                        }

                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    max_video_bitrate_);

                        int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value,
                                                    VIDEO_ENCODER_MIN_BITRATE_HIGH);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_HIGH);
                        if (res != 0)
                        {
                            PREF__video_call_quality = 2;
                            quality_slider.setPosition(PREF__video_call_quality);

                            SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext());
                            settings_cs1.edit().putString("video_call_quality", "" + PREF__video_call_quality).apply();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        update_bitrates();
        update_fps();
        update_call_time();
        set_video_delay_ms();
        set_audio_play_volume();

        quality_slider.setPosition(PREF__video_call_quality);

        try
        {
            video_box_speaker_button.setText("Speaker: ON");
            video_speaker_state = true;
            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            set_audio_to_loudspeaker(manager);
        }
        catch (Exception ignored)
        {
        }

        video_box_speaker_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    try
                    {
                        if (video_speaker_state == false)
                        {
                            video_box_speaker_button.setText("Speaker: OFF");
                        }
                        else
                        {
                            video_box_speaker_button.setText("Speaker: ON");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    try
                    {
                        if (video_speaker_state == false)
                        {
                            set_audio_to_ear(manager);
                            video_box_speaker_button.setText("Speaker: ON");
                            video_speaker_state = true;
                        }
                        else
                        {
                            set_audio_to_loudspeaker(manager);
                            video_box_speaker_button.setText("Speaker: OFF");
                            video_speaker_state = false;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                }
                return true;
            }
        });


        try
        {
            video_box_aec_button.setText("AEC: " + get_aec_active());
        }
        catch (Exception e)
        {
        }
        video_box_aec_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    try
                    {
                        video_box_aec_button.setText("AEC: " + get_aec_active());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        set_aec_active(1 - get_aec_active());
                        video_box_aec_button.setText("AEC: " + get_aec_active());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                }
                return true;
            }
        });


        // Detect when slider position changes
        quality_slider.setOnDiscreteSliderChangeListener(new DiscreteSlider.OnDiscreteSliderChangeListener()
        {
            @Override
            public void onPositionChanged(int position)
            {
                Log.i(TAG, "setOnDiscreteSliderChangeListener:pos=" + position);
                final int prev_position = PREF__video_call_quality;

                if (prev_position != position)
                {
                    int value = VIDEO_ENCODER_MAX_QUANTIZER_LOW;
                    int value1 = VIDEO_ENCODER_MAX_BITRATE_LOW;
                    int value2 = VIDEO_ENCODER_MIN_BITRATE_LOW;
                    if (position == 1)
                    {
                        value = VIDEO_ENCODER_MAX_QUANTIZER_MED;
                        value1 = VIDEO_ENCODER_MAX_BITRATE_MED;
                        value2 = VIDEO_ENCODER_MIN_BITRATE_MED;
                    }
                    else if (position == 2)
                    {
                        value = VIDEO_ENCODER_MAX_QUANTIZER_HIGH;
                        value1 = VIDEO_ENCODER_MAX_BITRATE_HIGH;
                        value2 = VIDEO_ENCODER_MIN_BITRATE_HIGH;
                    }

                    if (PREF__video_cam_resolution == 2)
                    {
                        value1 = value1 * 2;
                    }
                    else if (PREF__video_cam_resolution == 1)
                    {
                        value1 = (int) (value1 * 1.5);
                    }

                    int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                value1);

                    int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value,
                                                value2);

                    int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                               ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                               value);
                    Log.i(TAG, "setOnDiscreteSliderChangeListener:res:" + res);

                    if (res != 0)
                    {
                        PREF__video_call_quality = position;
                        Log.i(TAG, "setOnDiscreteSliderChangeListener:pos_NEW:" + quality_slider.getPosition());

                        SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());
                        settings_cs1.edit().putString("video_call_quality", "" + PREF__video_call_quality).apply();
                    }
                    else
                    {
                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(100);
                                    // set slide back to prev. position
                                    quality_slider.setPosition(prev_position);
                                    Log.i(TAG, "setOnDiscreteSliderChangeListener:pos_revert:" +
                                               quality_slider.getPosition());
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "setOnDiscreteSliderChangeListener:001:EE:" + e.getMessage());
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        });

        if (PREF__X_misc_button_enabled)
        {
            misc_button.setVisibility(View.VISIBLE);
            misc_button_pad.setVisibility(View.VISIBLE);

            Drawable d8 = new IconicsDrawable(this).
                    icon(GoogleMaterial.Icon.gmd_touch_app).
                    backgroundColor(Color.TRANSPARENT).color(getResources().
                    getColor(R.color.colorPrimaryDark)).sizeDp(50);
            misc_button.setImageDrawable(d8);

            misc_button.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                    {
                        try
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).
                                    icon(GoogleMaterial.Icon.gmd_touch_app).
                                    backgroundColor(Color.TRANSPARENT).color(getResources().
                                    getColor(R.color.md_green_600)).sizeDp(50);
                            misc_button.setImageDrawable(d1a);
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
                            Drawable d2a = new IconicsDrawable(v.getContext()).
                                    icon(GoogleMaterial.Icon.gmd_touch_app).
                                    backgroundColor(Color.TRANSPARENT).color(getResources().
                                    getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            misc_button.setImageDrawable(d2a);

                            // send misc. message to friend, and do NOT save to DB
                            // TODO: use new functions to send the message
                            //
                            //MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                            //        tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), 0,
                            //        PREF__X_misc_button_msg);
                            //long res = result.msg_num;
                            //Log.i(TAG, "tox_friend_send_message_wrapper:result=" + res);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });


        }
        else
        {
            misc_button.setVisibility(View.GONE);
            misc_button_pad.setVisibility(View.GONE);
        }

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(
                Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
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
                        // if (audio_manager_s.isMicrophoneMute())
                        if (trifa_is_MicrophoneMute)
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
                        }
                        else
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        // if (audio_manager_s.isMicrophoneMute())
                        if (trifa_is_MicrophoneMute)
                        {
                            audio_manager_s.setMicrophoneMute(false);
                            trifa_is_MicrophoneMute = false;
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                        }
                        else
                        {
                            audio_manager_s.setMicrophoneMute(true);
                            trifa_is_MicrophoneMute = true;
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "setMicrophoneMute:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        // on startup always use front camera
        active_camera_type = FRONT_CAMERA_USED;

        final Drawable d3 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(
                Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
        decline_button.setImageDrawable(d3);
        // #AARRGGBB

        final Drawable d4 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(
                Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
        accept_button.setImageDrawable(d4);

        if (Callstate.accepted_call == 1)
        {
            // HINT: also when starting a call ourselves!!
            caller_avatar_view.setVisibility(View.VISIBLE);
            accept_button.setVisibility(View.GONE);
            camera_toggle_button.setVisibility(View.VISIBLE);
            mute_button.setVisibility(View.VISIBLE);

            stop_ringtone();
            set_calling_audio_mode();
        }
        else
        {
            caller_avatar_view.setVisibility(View.VISIBLE);
            accept_button.setVisibility(View.VISIBLE);
            camera_toggle_button.setVisibility(View.GONE);
            mute_button.setVisibility(View.GONE);

            start_ringtone();
        }

        if (active_camera_type == FRONT_CAMERA_USED)
        {
            final Drawable d5 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_front).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            camera_toggle_button.setImageDrawable(d5);
            Log.i(TAG, "active_camera_type(5)=" + active_camera_type);
        }
        else
        {
            final Drawable d6 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            camera_toggle_button.setImageDrawable(d6);
            Log.i(TAG, "active_camera_type(6)=" + active_camera_type);
        }

        camera_toggle_button.setOnTouchListener(new View.OnTouchListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    Log.i(TAG, "active_camera_type(7)=" + active_camera_type);

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    Log.i(TAG, "active_camera_type(8)=" + active_camera_type);

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }

                    camera_toggle_button_pressed = true;
                    Log.i(TAG, "camera_toggle_button_pressed[press start]=" + camera_toggle_button_pressed);
                    if (PREF__use_camera_x)
                    {
                        toggle_camera();
                        camera_toggle_button_pressed = false;
                        Log.i(TAG, "camera_toggle_button_pressed[press end:M]=" + camera_toggle_button_pressed);
                    }
                    else
                    {
                        final Thread toggle_thread = new Thread()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(30);
                                }
                                catch (Exception e)
                                {
                                    // e.printStackTrace();
                                }
                                toggle_camera();
                                camera_toggle_button_pressed = false;
                                Log.i(TAG, "camera_toggle_button_pressed[press end]=" + camera_toggle_button_pressed);
                            }
                        };
                        toggle_thread.start();
                    }
                }

                return true;
            }
        });

        Log.i(TAG, "start:0001");
        initUI();
        Log.i(TAG, "start:0002");
        initViewParams();
        Log.i(TAG, "start:0003");

        top_text_line_str1 = Callstate.friend_alias_name;
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
                            Log.i(TAG, "answer button pressed");

                            if (Callstate.audio_call)
                            {
                                int res1 = toxav_answer(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                        GLOBAL_AUDIO_BITRATE, 0);

                                if (res1 != 1)
                                {
                                    return true;
                                }
                                Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V.value;
                            }
                            else
                            {
                                int res2 = toxav_answer(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                        GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE);

                                if (res2 != 1)
                                {
                                    return true;
                                }

                                Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V.value;

                            }

                            Callstate.accepted_call = 1;

                            caller_avatar_view.setVisibility(View.GONE);
                            accept_button.setVisibility(View.GONE);
                            camera_toggle_button.setVisibility(View.VISIBLE);
                            mute_button.setVisibility(View.VISIBLE);

                            Callstate.call_start_timestamp = System.currentTimeMillis();
                            String a = "" +
                                       (int) ((Callstate.call_start_timestamp - Callstate.call_init_timestamp) / 1000) +
                                       "s";
                            top_text_line_str2 = a;
                            update_top_text_line();

                            Log.i(TAG, "on_call_started_actions:01");
                            on_call_started_actions();
                            if (Callstate.audio_call)
                            {
                                toggle_osd_view_including_cam_preview(!Callstate.audio_call);
                            }

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
                Log.i(TAG, "decline_button_pressed:000");

                try
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        Log.i(TAG, "decline_button_pressed:DOWN");
                        Log.i(TAG, "decline_button_pressed:on_call_ended_actions");
                        CallAudioService.stop_me(true);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return true;
            }
        });

        Log.i(TAG, "onCreate:99");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            hideSystemUI();
        }
    }

    private void hideSystemUI()
    {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        // Set the content to appear under the system bars so that the
                                        // content doesn't resize when the system bars hide and show.
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        // Hide the nav bar and status bar
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI()
    {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    void update_call_audio_bars()
    {
        try
        {
            audio_bar_in_v.setLevel(get_vu_in() / 90.0f);
            audio_bar_out_v.setLevel(get_vu_out() / 140.0f);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_min_and_max_video_bitrate()
    {
        try
        {
            int value1 = VIDEO_ENCODER_MAX_BITRATE_LOW;
            int value2 = VIDEO_ENCODER_MIN_BITRATE_LOW;

            if (PREF__video_call_quality == 1)
            {
                value1 = VIDEO_ENCODER_MAX_BITRATE_MED;
                value2 = VIDEO_ENCODER_MIN_BITRATE_MED;
            }
            else if (PREF__video_call_quality == 2)
            {
                value1 = VIDEO_ENCODER_MAX_BITRATE_HIGH;
                value2 = VIDEO_ENCODER_MIN_BITRATE_HIGH;
            }

            if (PREF__video_cam_resolution == 2)
            {
                value1 = value1 * 2;
            }
            else if (PREF__video_cam_resolution == 1)
            {
                value1 = (int) (value1 * 1.5);
            }

            int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                       ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value, value1);
            Log.i(TAG, "max_v_birate_set:res=" + res);

            int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                        ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value, value2);
            Log.i(TAG, "min_v_birate_set:res=" + res2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "min_and_max_v_birate_set:EE:" + e.getMessage());
        }
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
        // Log.i(TAG, "update_top_text_line(1):top_text_line_str3=" + top_text_line_str3);
        update_top_text_line(top_text_line_str3, 3);
    }

    synchronized public static void update_calling_friend_connection_status(final int conn_status)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (conn_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_UDP.value)
                    {
                        calling_friend_online_status.setBackgroundColor(Color.parseColor("#04b431"));
                    }
                    else if (conn_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value)
                    {
                        calling_friend_online_status.setBackgroundColor(Color.parseColor("#ffce00"));
                    }
                    else
                    {
                        calling_friend_online_status.setBackgroundColor(Color.parseColor("#ff0000"));
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    synchronized public static void update_top_text_line(String text2, int linenum)
    {
        // Log.i(TAG, "update_top_text_line(2):str=" + text2);
        // Log.i(TAG, "update_top_text_line(2):top_text_line_str1=" + top_text_line_str1);
        // Log.i(TAG, "update_top_text_line(2):top_text_line_str2=" + top_text_line_str2);
        // Log.i(TAG, "update_top_text_line(2):top_text_line_str3=" + top_text_line_str3);
        // Log.i(TAG, "update_top_text_line(2):top_text_line_str4=" + top_text_line_str4);

        if (linenum == 3)
        {
            top_text_line_str3 = text2;
        }
        else if (linenum == 4)
        {
            top_text_line_str4 = text2;
        }

        // Log.i(TAG, "update_top_text_line(2b):top_text_line_str3=" + top_text_line_str3);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Log.i(TAG, "update_top_text_line(2c):top_text_line_str3=" + top_text_line_str3);

                    if ((top_text_line_str3 != "") || (top_text_line_str4 != ""))
                    {
                        top_text_line.setText(
                                top_text_line_str1 + ":" + top_text_line_str2 + ":" + top_text_line_str3 + ":" +
                                top_text_line_str4);
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

    public static void set_debug_text(final String text)
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
                        debug001_text.setText(text);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "set_debug_text:EE01:" + e.getMessage());
                    }
                }
            };
            callactivity_handler_s.post(myRunnable);
        }
        catch (Exception e2)
        {
            Log.i(TAG, "set_debug_text:EE02:" + e2.getMessage());
        }
    }

    public static void initializeScreenshotSecurity(Activity a)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        else
        {
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onBackPressed()
    {
        // dont let the user use the back button to close the activity
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume:01");

        super.onResume();

        tox_set_onion_active(0);

        try
        {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            MainActivity.PREF__camera_get_preview_format = settings.getString("camera_get_preview_format", "YV12");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            if (PREF__use_H264_hw_encoding)
            {
                prepareEncoder();
                // prepareDecoder();
            }
        }
        activity_state = 1;

        // ------ set audio device ------
        // ------ set audio device ------
        // ------ set audio device ------
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audio_device_icon.setVisibility(View.VISIBLE);
        try
        {
            // AAAAAAAAAAAAUDIO:1111111
            if (dh._Detect())
            {
                if (isBluetoothConnected())
                {
                    Log.i(TAG, "startBluetoothSco");
                    manager.startBluetoothSco();
                    // manager.setBluetoothScoOn(true);
                    Callstate.audio_device = 2;
                    update_audio_device_icon();
                }
                else
                {
                    // headset plugged in
                    Log.i(TAG, "onReceive:headset:plugged in");
                    manager.setSpeakerphoneOn(false);
                    manager.setWiredHeadsetOn(true);
                    Callstate.audio_device = 1;
                    Callstate.audio_speaker = false;
                    update_audio_device_icon();
                    // manager.setBluetoothScoOn(false);
                }
            }
            else
            {
                audio_device_icon.setImageDrawable(null);
                Log.i(TAG, "onReceive:headset:setImageDrawable:null1");
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
            audio_device_icon.setImageDrawable(null);
            Log.i(TAG, "onReceive:headset:setImageDrawable:null2");
        }
        // ------ set audio device ------
        // ------ set audio device ------
        // ------ set audio device ------

        sensor_manager.registerListener(this, proximity_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_manager.registerListener(this, accelerometer_sensor, SensorManager.SENSOR_DELAY_NORMAL);

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
                audio_thread = null;
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
                audio_receiver_thread.join();
                audio_receiver_thread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (AudioReceiver.stopped)
            {
                audio_receiver_thread = new AudioReceiver();
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
                audio_thread = new AudioRecording();
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

        // update every x times per second -----------
        final int update_per_sec = 8;
        final Handler ha2 = new Handler();
        ha.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                // Log.i(TAG, "update_call_time -> call");
                update_call_audio_bars();
                if (activity_state != 0)
                {
                    ha2.postDelayed(this, 1000 / update_per_sec);
                }
            }
        }, 1000 / update_per_sec);
        // update every x times per second -----------

        if (Callstate.audio_call)
        {
            toggle_osd_view_including_cam_preview(!Callstate.audio_call);
        }

        // ------- start audio calling service and notification -------
        Log.i(TAG, "call_audio_service:start");
        NotificationManager nmn3 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            String channelName = "Tox Call";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channelId = "trifa_call_audio_play";
            notification_channel_call_audio_play_service = new NotificationChannel(channelId, channelName, importance);
            notification_channel_call_audio_play_service.setDescription(channelId);
            notification_channel_call_audio_play_service.setSound(null, null);
            notification_channel_call_audio_play_service.enableVibration(false);
            nmn3.createNotificationChannel(notification_channel_call_audio_play_service);
        }
        try
        {
            Intent i = new Intent(this, CallAudioService.class);
            startService(i);
        }
        catch (Exception e)
        {
            Log.i(TAG, "call_audio_service:EE01:" + e.getMessage());
            e.printStackTrace();
        }
        // ------- start audio calling service and notification -------

        Log.i(TAG, "onResume:99");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void toggle_camera()
    {
        if (PREF__use_camera_x)
        {
            try
            {
                cameraProvider.unbindAll();

                if (active_camera_type == FRONT_CAMERA_USED)
                {
                    active_camera_type = BACK_CAMERA_USED;
                    Log.i(TAG, "active_camera_type(8a)=" + active_camera_type);
                    bindImageAnalysis(cameraProvider);
                }
                else
                {
                    active_camera_type = FRONT_CAMERA_USED;
                    Log.i(TAG, "active_camera_type(8b)=" + active_camera_type);
                    bindImageAnalysis(cameraProvider);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Thread openThread = new Thread()
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
                            Log.i(TAG, "active_camera_type(8a)=" + active_camera_type);
                            CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, false);
                        }
                        else
                        {
                            CameraWrapper.camera_preview_size2 = null;
                            active_camera_type = FRONT_CAMERA_USED;
                            Log.i(TAG, "active_camera_type(8b)=" + active_camera_type);
                            CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);
                        }
                    }
                    catch (Exception e)
                    {
                        // e.printStackTrace();
                    }
                }
            };
            openThread.start();
        }
    }

    // -------------------------------------------------------
    // TODO
    // this is a bad workaround to really show the cam preview
    // -------------------------------------------------------
    static void reinit_camera(CallingActivity c)
    {
        try
        {
            if (PREF__use_camera_x)
            {
            }
            else
            {
                Log.i(TAG, "active_camera_type(1)=" + active_camera_type);
                CameraWrapper.getInstance().doStopCamera();
                Log.i(TAG, "active_camera_type(2)=" + active_camera_type);
                CameraWrapper.camera_preview_size2 = null;
                Log.i(TAG, "active_camera_type(3)=" + active_camera_type);
                CameraWrapper.getInstance().doOpenCamera(c, true);
                Log.i(TAG, "active_camera_type(4)=" + active_camera_type);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "reinit_camera:EE01:" + e.getMessage());
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        sensor_manager.unregisterListener(this);

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
    }

    static void stop_active_call()
    {
        activity_state = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            if (PREF__use_H264_hw_encoding)
            {
                releaseEncoder();
                // releaseDecoder_h264();
            }
        }

        stop_ringtone();

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
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
                audio_receiver_thread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // ------ shutdown audio device ------
        // ------ shutdown audio device ------
        // ------ shutdown audio device ------
        AudioManager manager = (AudioManager) context_s.getSystemService(Context.AUDIO_SERVICE);
        try
        {
            if (dh._Detect())
            {
                if (isBluetoothConnected())
                {
                    Log.i(TAG, "stopBluetoothSco:2");
                    // manager.setBluetoothScoOn(false);
                    Callstate.audio_device = 0;
                    Callstate.audio_speaker = true;
                    audio_manager_s.setSpeakerphoneOn(true);
                    manager.stopBluetoothSco();
                }
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
        // ------ shutdown audio device ------
        // ------ shutdown audio device ------
        // ------ shutdown audio device ------

        reset_audio_mode();

        tox_set_onion_active(1);

        Log.i(TAG, "onPause:on_call_ended_actions");
        on_call_ended_actions();
    }

    // ---------------
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart()
    {
        Log.i(TAG, "onStart:01");
        super.onStart();
        final CallingActivity c_this = this;

        if (PREF__use_camera_x)
        {
            active_camera_type = FRONT_CAMERA_USED;
            cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
            cameraProviderListenableFuture.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cameraProvider = cameraProviderListenableFuture.get();
                        bindImageAnalysis(cameraProvider);
                        Callstate.camera_opened = true;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, ContextCompat.getMainExecutor(this));
        }
        else
        {
            Thread openThread = new Thread()
            {
                @Override
                public void run()
                {
                    active_camera_type = FRONT_CAMERA_USED;
                    Log.i(TAG, "active_camera_type(01)=" + active_camera_type);
                    CameraWrapper.camera_preview_size2 = null;

                    try
                    {
                        CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);

                        // wait for 1 seconds to actually get a camera preview. if not, restart camera
                        int WAIT_SECONDS = 2;
                        long startup_ts = System.currentTimeMillis();
                        for (int j = 0; j < 100 * WAIT_SECONDS; j++)
                        {
                            // Log.i(TAG, "onStart:01:ts=" + camera_preview_call_back_ts_first_frame + " " +
                            //            camera_preview_call_back_start_ts);

                            if (camera_toggle_button_pressed == true)
                            {
                                break;
                            }

                            if (camera_preview_call_back_ts_first_frame > startup_ts)
                            {
                                Log.i(TAG, "onStart:01:ts:got a frame");
                                // ok we got a video frame from the camera
                                break;
                            }

                            try
                            {
                                Thread.sleep(10);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        try
                        {
                            if (camera_toggle_button_pressed != true)
                            {
                                Log.i(TAG, "onStart:01:ts:NO FRAME from camera, restarting ...");
                                reinit_camera(c_this);
                            }
                            else
                            {
                                Log.i(TAG, "onStart:01:ts:camera toggle button pressed");
                            }
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "onStart:01:ts:NO FRAME from camera, restart:EE:" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    catch (Exception e33)
                    {
                        Log.i(TAG, "onStart:EE33:" + e33.getMessage());
                    }
                }
            };
            openThread.start();
        }

        Log.i(TAG, "onStart:99");
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider)
    {
        OrientationEventListener orientationEventListener = new OrientationEventListener(this)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
            }
        };

        orientationEventListener.enable();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            Preview preview = new Preview.Builder().build();

            CameraSelector cameraSelector = null;
            if (active_camera_type == FRONT_CAMERA_USED)
            {
                cameraSelector = new CameraSelector.Builder().
                        requireLensFacing(CameraSelector.LENS_FACING_FRONT).
                        build();
                drawingOverlay.flipimage = true;
            }
            else
            {
                cameraSelector = new CameraSelector.Builder().
                        requireLensFacing(CameraSelector.LENS_FACING_BACK).
                        build();
                drawingOverlay.flipimage = false;
            }

            preview.setSurfaceProvider(cameraXPreview.getSurfaceProvider());
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().
                    setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888).
                    setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).
                    build();

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                                      (VideoFrameAnalyserTFLite) videoFrameAnalyser_tflite);
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
        }
    }

    static MappedByteBuffer loadModelFile(Activity activity) throws IOException
    {
        // final String tf_model_file = "deeplabv3_257_mv_gpu.tflite";
        final String tf_model_file = "selfiesegmentation_mlkit-256x256-2021_01_19-v1215.f16.tflite";

        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(tf_model_file);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void initUI()
    {
        cameraXPreview = findViewById(R.id.camera_preview_view);
        drawingOverlay = findViewById(R.id.camera_drawing_overlay);
        cameraSurfacePreview = (CameraSurfacePreview) findViewById(R.id.camera_surfaceview);

        if (PREF__use_camera_x)
        {
            cameraSurfacePreview.setVisibility(View.INVISIBLE);
            //
            drawingOverlay.setWillNotDraw(false);
            drawingOverlay.setZOrderOnTop(true);
            videoFrameAnalyser_tflite = new VideoFrameAnalyserTFLite(drawingOverlay, this, this);
        }
        else
        {
            cameraXPreview.setVisibility(View.INVISIBLE);
            drawingOverlay.setVisibility(View.INVISIBLE);
        }
    }

    private void initViewParams()
    {
        /*
        ViewGroup.LayoutParams params = cameraSurfacePreview.getLayoutParams();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        params.width = MATCH_PARENT; // screenWidth;
        params.height = MATCH_PARENT; // screenHeight;
        mPreviewRate = (float) screenHeight / (float) screenWidth;
        cameraSurfacePreview.setLayoutParams(params);
        */
    }

    @Override
    public void cameraHasOpened()
    {
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Callstate.camera_opened = true;
        try
        {
            if (PREF__use_camera_x)
            {

            }
            else
            {
                SurfaceHolder holder = cameraSurfacePreview.getSurfaceHolder();
                Log.i(TAG, "cameraHasOpened:holder=" + holder);
                Log.i(TAG, "cameraHasOpened:CameraWrapper.getInstance()=" + CameraWrapper.getInstance());
                CameraWrapper.getInstance().doStartPreview(holder, mPreviewRate);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "cameraHasOpened:EE01:" + e.getMessage());
        }
    }

    public void turnOnScreen()
    {
        if (PREF__allow_screen_off_in_audio_call)
        {
            turnOnScreen__old();
        }
        else
        {
            turnOnScreen__new();
        }
    }

    public void turnOffScreen()
    {
        if (PREF__allow_screen_off_in_audio_call)
        {
            turnOffScreen__old();
        }
        else
        {
            turnOffScreen__new();
        }
    }

    public void turnOnScreen__new()
    {
        try
        {
            // calling_activity_top_viewgroup_vg.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turn*ON*Screen");
        Callstate.other_video_enabled = 1;
        Callstate.my_video_enabled = 1;
    }

    public void turnOffScreen__new()
    {
        try
        {
            // in case the phone does not really turn the screen off
            mContentView.setVisibility(View.INVISIBLE);
            // calling_activity_top_viewgroup_vg.setVisibility(View.INVISIBLE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turnOffScreen");
        Callstate.other_video_enabled = 0;
        Callstate.my_video_enabled = 0;
    }

    public void turnOnScreen__old()
    {
        mContentView.setVisibility(View.VISIBLE);

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
            wl1 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                 "trifa:trifa_screen_on");
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
    public void turnOffScreen__old()
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
            wl2 = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "trifa:trifa_screen_OFF");
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


        // in case the phone does not really turn the screen off
        mContentView.setVisibility(View.INVISIBLE);
    }

    private void requestAudioFocus()
    {
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        //        {
        //            AudioAttributes playbackAttributes = new AudioAttributes.Builder().setUsage(
        //                    AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(
        //                    AudioAttributes.CONTENT_TYPE_SPEECH).build();
        //            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(
        //                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(
        //                    playbackAttributes).setAcceptsDelayedFocusGain(true).setOnAudioFocusChangeListener(
        //                    new AudioManager.OnAudioFocusChangeListener()
        //                    {
        //                        @Override
        //                        public void onAudioFocusChange(int i)
        //                        {
        //                        }
        //                    }).build();
        //            audio_manager_s.requestAudioFocus(focusRequest);
        //        }
        //        else
        //        {
        //            audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
        //                                              AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //        }
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
                    Log.i(TAG, "onSensorChanged:--> EAR");

                    try
                    {
                        if (!dh._Detect())
                        {
                            set_aec_active(0);
                            AudioManager manager = (AudioManager) context_s.getSystemService(Context.AUDIO_SERVICE);
                            set_audio_to_ear(manager);
                            Log.i(TAG, "onSensorChanged:--> EAR:set_audio_to_ear()");
                            turnOffScreen();
                            Log.i(TAG, "onSensorChanged:--> EAR:turnOffScreen()");
                            Callstate.audio_speaker = false;
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            else
            {
                // away from ear
                if (Callstate.audio_speaker == false)
                {
                    Log.i(TAG, "onSensorChanged:--> speaker");

                    try
                    {
                        if (!dh._Detect())
                        {
                            if (PREF__use_software_aec)
                            {
                                set_aec_active(0); // --ACTIVE--
                            }
                            else
                            {
                                set_aec_active(0);
                            }
                            AudioManager manager = (AudioManager) context_s.getSystemService(Context.AUDIO_SERVICE);
                            set_audio_to_loudspeaker(manager);
                            turnOnScreen();
                            Log.i(TAG, "onSensorChanged:--> speaker:turnOnScreen()");
                            Callstate.audio_speaker = true;
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            try
            {
                if (event.values.length > 2)
                {
                    //Log.d(TAG, "onSensorChanged: x=" + event.values[0] + "  y=" + event.values[1] + "  z=" +
                    //           event.values[2]);

                    float x = event.values[0];
                    float y = event.values[1];

                    if (x < 5 && x > -5 && y > 5)
                    {
                        if (device_orientation != 0)
                        {
                            device_orientation = 0;
                            //Log.d(TAG, "onSensorChanged:device_orientation=" + device_orientation);
                            CameraWrapper.camera_video_rotate_angle = getRotation();
                            video_output_orentation_update();
                        }
                    }
                    else if (x < -5 && y < 5 && y > -5)
                    {
                        if (device_orientation != 90)
                        {
                            device_orientation = 90;
                            //Log.d(TAG, "onSensorChanged:device_orientation=" + device_orientation);
                            CameraWrapper.camera_video_rotate_angle = getRotation();
                            video_output_orentation_update();
                        }
                    }
                    else if (x < 5 && x > -5 && y < -5)
                    {
                        if (device_orientation != 180)
                        {
                            device_orientation = 180;
                            //Log.d(TAG, "onSensorChanged:device_orientation=" + device_orientation);
                            CameraWrapper.camera_video_rotate_angle = getRotation();
                            video_output_orentation_update();
                        }
                    }
                    else if (x > 5 && y < 5 && y > -5)
                    {
                        if (device_orientation != 270)
                        {
                            device_orientation = 270;
                            //Log.d(TAG, "onSensorChanged:device_orientation=" + device_orientation);
                            CameraWrapper.camera_video_rotate_angle = getRotation();
                            video_output_orentation_update();
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    static void toggle_cam_preview(boolean visible, boolean alpha_only)
    {
        if (alpha_only)
        {
            if (visible)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (PREF__use_camera_x)
                            {
                                cameraXPreview.setAlpha(1.0f);
                                drawingOverlay.setAlpha(1.0f);
                            }
                            else
                            {
                                cameraSurfacePreview.setAlpha(1.0f);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
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
                            if (PREF__use_camera_x)
                            {
                                cameraXPreview.setAlpha(0.0f);
                                drawingOverlay.setAlpha(0.0f);
                            }
                            else
                            {
                                cameraSurfacePreview.setAlpha(0.0f);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
            }
        }
        else
        {
            if (visible)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (PREF__use_camera_x)
                            {
                                cameraXPreview.setVisibility(View.VISIBLE);
                                drawingOverlay.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                cameraSurfacePreview.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
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
                            if (PREF__use_camera_x)
                            {
                                cameraXPreview.setVisibility(View.INVISIBLE);
                                drawingOverlay.setVisibility(View.INVISIBLE);
                            }
                            else
                            {
                                cameraSurfacePreview.setVisibility(View.INVISIBLE);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
            }
        }
    }

    static void toggle_osd_view_including_cam_preview(boolean visible)
    {
        toggle_cam_preview(visible, false);
        toggle_osd_views(visible, true);
    }

    static void toggle_osd_views(boolean visible, boolean for_audio_call)
    {
        if (visible)
        {
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        CallingActivity.video_box_left_top_01.setVisibility(View.VISIBLE);

                        CallingActivity.video_box_right_top_01.setVisibility(View.VISIBLE);
                        CallingActivity.right_top_text_1.setVisibility(View.VISIBLE);
                        CallingActivity.right_top_text_1b.setVisibility(View.VISIBLE);
                        CallingActivity.right_top_text_2.setVisibility(View.VISIBLE);
                        CallingActivity.right_top_text_3.setVisibility(View.VISIBLE);
                        CallingActivity.right_top_text_4.setVisibility(View.VISIBLE);
                        CallingActivity.box_right_volumeslider_01.setVisibility(View.VISIBLE);

                        CallingActivity.video_add_delay_slider_infotext_01.setVisibility(View.VISIBLE);
                        CallingActivity.video_add_delay_slider_seekbar_01.setVisibility(View.VISIBLE);
                        CallingActivity.video_box_aec.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            };
            CallingActivity.callactivity_handler_s.post(myRunnable);
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
                        if (for_audio_call)
                        {
                            CallingActivity.video_box_left_top_01.setVisibility(View.VISIBLE);
                            CallingActivity.video_box_right_top_01.setVisibility(View.VISIBLE);
                            CallingActivity.right_top_text_1.setVisibility(View.INVISIBLE);
                            CallingActivity.right_top_text_1b.setVisibility(View.INVISIBLE);
                            CallingActivity.right_top_text_2.setVisibility(View.INVISIBLE);
                            CallingActivity.right_top_text_3.setVisibility(View.INVISIBLE);
                            CallingActivity.right_top_text_4.setVisibility(View.INVISIBLE);
                            CallingActivity.box_right_volumeslider_01.setVisibility(View.VISIBLE);

                            CallingActivity.video_add_delay_slider_infotext_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_add_delay_slider_seekbar_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_box_aec.setVisibility(View.VISIBLE);

                        }
                        else
                        {
                            CallingActivity.video_box_left_top_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_box_right_top_01.setVisibility(View.INVISIBLE);
                            CallingActivity.box_right_volumeslider_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_add_delay_slider_infotext_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_add_delay_slider_seekbar_01.setVisibility(View.INVISIBLE);
                            CallingActivity.video_box_aec.setVisibility(View.INVISIBLE);

                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            };
            CallingActivity.callactivity_handler_s.post(myRunnable);
        }
    }

    void update_call_time()
    {
        if (Callstate.accepted_call == 1)
        {
            if (Callstate.call_start_timestamp != -1)
            {

                right_left_text_1.setText(format_timeduration_from_seconds(
                        (System.currentTimeMillis() - Callstate.call_start_timestamp) / 1000));
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
                    Log.i(TAG, "update_audio_device_icon:enter");

                    if (Callstate.audio_device == 0)
                    {
                        Log.i(TAG, "update_audio_device_icon:clear");
                        audio_device_icon.setImageDrawable(null);
                    }
                    else if (Callstate.audio_device == 1)
                    {
                        Log.i(TAG, "update_audio_device_icon:headset");
                        Drawable d4 = new IconicsDrawable(ca).icon(GoogleMaterial.Icon.gmd_headset).backgroundColor(
                                Color.TRANSPARENT).color(ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(
                                80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else if (Callstate.audio_device == 2)
                    {
                        Log.i(TAG, "update_audio_device_icon:bluetooth");
                        Drawable d4 = new IconicsDrawable(ca).icon(
                                GoogleMaterial.Icon.gmd_bluetooth_audio).backgroundColor(Color.TRANSPARENT).color(
                                ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else // audio_device == ??
                    {
                        Log.i(TAG, "update_audio_device_icon:null");
                        audio_device_icon.setImageDrawable(null);
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "update_audio_device_icon:EE:" + e.getMessage());
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    static void set_video_delay_ms()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try
                    {
                        toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                         ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_ADD_DELAY_MS.value,
                                         -PREF__video_play_delay_ms);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "TOXAV_DECODER_VIDEO_ADD_DELAY_MS:EE:" + e.getMessage());
                    }

                    try
                    {
                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value,
                                                   PREF__video_play_delay_ms);
                        Log.i(TAG,
                              "decoder buffer set to ms=" + TRIFAGlobals.VIDEO_DECODER_BUFFER_DELAY + ":res=" + res);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "TOXAV_DECODER_VIDEO_BUFFER_MS:EE:" + e.getMessage());
                    }

                    video_add_delay_slider_infotext_01.setText(context_s.getString(R.string.
                                                                                           CallingActivity_0) + " " +
                                                               PREF__video_play_delay_ms + " ms");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        video_add_delay_slider_seekbar_01.setProgress((PREF__video_play_delay_ms / 5), true);
                    }
                    else
                    {
                        video_add_delay_slider_seekbar_01.setProgress((PREF__video_play_delay_ms / 5));
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    static void set_audio_play_volume()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        volume_slider_seekbar_01.setProgress(PREF__audio_play_volume_percent, true);
                    }
                    else
                    {
                        volume_slider_seekbar_01.setProgress(PREF__audio_play_volume_percent);
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

                try
                {
                    set_audio_play_volume_percent(PREF__audio_play_volume_percent);
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    // actions to take when a call starts by:
    // a) accepting an incoming call
    // b) the other party accepting our call invitation
    static void on_call_started_actions()
    {
        set_min_and_max_video_bitrate();
        set_video_delay_ms();
        set_audio_play_volume();
        stop_ringtone();
        set_calling_audio_mode();

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CallingActivity.video_box_self_preview_01.setVisibility(View.VISIBLE);
                    CallingActivity.video_box_left_top_01.setVisibility(View.VISIBLE);
                    CallingActivity.video_box_right_top_01.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    // actions to take when a call ends by:
    // a) us ending the call
    // b) the other party ending the call
    static void on_call_ended_actions()
    {
        try
        {
            close_calling_activity();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------

    /* Use this method to provide YUV420 buffers for encoding */
    static class h264_encoder_output_data
    {
        byte[] data;
        int data_len;
        byte[] sps_pps;
        long pts;
    }

    public static void feed_h264_encoder(final byte[] buf, int frame_width_px, int frame_height_px, long pts)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
/*
            int v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_LOW * 1000;

            if (quality_slider_position == 2)
            {
                v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_HIGH * 1000;
            }
            else if (quality_slider_position == 1)
            {
                v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_MED * 1000;
            }

*/
            final int v_bitrate_bps = (int) Callstate.video_bitrate * 1000;

            reconfigure_h264_encoder(v_bitrate_bps, frame_width_px, frame_height_px);

            /* Find an unallocated input buffer to store food */
            try
            {
                final int inputBufferIndex = mEncoder.dequeueInputBuffer(
                        BUFFER_DEQUEUE_FEEDER_TIMEOUT_US); // This method will return immediately if timeoutUs == 0
                if (inputBufferIndex >= 0)
                {
                    /* Get input buffer and fill it with our input */
                    ByteBuffer inputBuffer = mEncoder.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(buf);
                    /* Enqueue buffer */
                    // Log.d(TAG, "feed_h264_encoder:Enqueued input index: " + inputBufferIndex);

                    final long ptsUsec = computePresentationTime(pts);
                    mEncoder.queueInputBuffer(inputBufferIndex, 0, buf.length, ptsUsec, 0);
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "feed_h264_encoder:Get free buffer failed");
            }
        }
        else
        {
            // TODO: do something here?
        }
    }

    public static h264_encoder_output_data fetch_from_h264_encoder()
    {
        h264_encoder_output_data ret = new h264_encoder_output_data();
        ret.data = null;
        ret.data_len = 0;
        ret.sps_pps = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int encoderStatus = MediaCodec.INFO_TRY_AGAIN_LATER;

            try
            {
                if (mEncoder == null)
                {
                    return ret;
                }
                encoderStatus = mEncoder.dequeueOutputBuffer(info,
                                                             BUFFER_DEQUEUE_TIMEOUT_US); // Dequeue an output buffer, block at most "timeoutUs" microseconds.
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                return ret;
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                // no output available yet
                // Log.d(TAG, "fetch_from_h264_encoder:no output from encoder available");
                return ret;
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // not expected for an encoder
                // Log.d(TAG, "fetch_from_h264_encoder:old_format: " + video_encoder_format);
                final MediaFormat newFormat = mEncoder.getOutputFormat();
                // Log.d(TAG, "fetch_from_h264_encoder:new_format: " + newFormat);
                //**//ByteBuffer csd0 = newFormat.getByteBuffer("csd-0");
                //**//ByteBuffer csd1 = newFormat.getByteBuffer("csd-1");
                // System.out.println("fetch_from_h264_encoder:csd-0:len=" + csd0.limit());
                // System.out.println("fetch_from_h264_encoder:csd-1:len=" + csd1.limit());

                video_encoder_format = newFormat;

                return ret;
            }
            else if (encoderStatus < 0)
            {
                // Log.d(TAG,
                //      "fetch_from_h264_encoder:unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                return ret;
            }
            else // encoderStatus >= 0
            {
                /* Compressed frame is ready! */
                final ByteBuffer compressed = mEncoder.getOutputBuffer(encoderStatus);
                if (compressed == null)
                {
                    // Log.d(TAG, "fetch_from_h264_encoder:encoderOutputBuffer " + encoderStatus + " was null");
                    return ret;
                }
                else
                {
                    // Log.d(TAG, "fetch_from_h264_encoder:Dequeue output index: " + encoderStatus);
                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    compressed.position(info.offset);
                    compressed.limit(info.offset + info.size);
                    /* Copy to byte array for further processing */
                    ret.data_len = compressed.remaining();

                    if (arr_h264_enc == null)
                    {
                        arr_h264_enc = new byte[ret.data_len];
                    }
                    else if (arr_h264_enc.length < ret.data_len)
                    {
                        arr_h264_enc = new byte[ret.data_len];
                    }
                    compressed.get(arr_h264_enc, 0, ret.data_len);
                    compressed.position(info.offset);

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                    {
                        // Codec config info.  Only expected on first packet.  One way to
                        // handle this is to manually stuff the data into the MediaFormat
                        // and pass that to configure().  We do that here to exercise the API.
                        //**// MediaFormat video_encoder_format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
                        //**// video_encoder_format.setByteBuffer("csd-0", compressed);
                        //
                        // --> "csd-0" or SPS/PPS data
                        //
                        ret.sps_pps = arr_h264_enc;
                        //System.out.println(
                        //        "fetch_from_h264_encoder:SPS_PPS:len=" + arr.length + " data=" + arr.toString());
                    }
                    else
                    {
                        // Get a decoder input buffer, blocking until it's available.
                    }

                    /* Release MediaCodec buffer */
                    mEncoder.releaseOutputBuffer(encoderStatus, false);
                    ret.data = arr_h264_enc;
                    ret.pts = (info.presentationTimeUs / 1000);
                    return ret;
                }
            }
        }
        else
        {
            // TODO: do something here?
        }

        return ret;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private static long computePresentationTime(long ts)
    {
        return (System.currentTimeMillis() * 1000) + 10;
    }

    static void reconfigure_h264_encoder(int bitrate_bits_per_second, int width, int height)
    {
        if ((video_encoder_width == width) && (video_encoder_height == height) &&
            (v_bitrate_bits_per_second != bitrate_bits_per_second))
        {
            try
            {
                // change encoder bitrate only every x seconds max.
                if ((encode_last_v_bitrate_change + 1000) < System.currentTimeMillis())
                {
                    encode_last_v_bitrate_change = System.currentTimeMillis();
                    v_bitrate_bits_per_second = bitrate_bits_per_second;
                    if (bitrate_bits_per_second >= 150)
                    {
                        Bundle bitrate_new = new Bundle();
                        video_encoder_format.setInteger(MediaFormat.KEY_BIT_RATE, v_bitrate_bits_per_second);
                        bitrate_new.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, v_bitrate_bits_per_second);
                        mEncoder.setParameters(bitrate_new);
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            if ((video_encoder_width != width) || (video_encoder_height != height) ||
                (v_bitrate_bits_per_second != bitrate_bits_per_second))
            {
                releaseEncoder();
                video_encoder_width = width;
                video_encoder_height = height;
                v_bitrate_bits_per_second = bitrate_bits_per_second;
                // Log.d(TAG, "reconfigure_h264_encoder:vbrate: " + v_bitrate_bits_per_second);
                prepareEncoder();
            }
        }
    }

    /**
     * Configures the H264 encoder
     */
    static void prepareEncoder()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {

            /**
             {
             mime=video/avc,
             width=480,
             height=640,
             bitrate=120000,
             max-bitrate=120000,
             frame-rate=25,
             opaque_handle=0,
             csd-0=java.nio.HeapByteBuffer[pos=0 lim=13 cap=13]
             csd-1=java.nio.HeapByteBuffer[pos=0 lim=8 cap=8],
             vendor.rtc-ext-enc-base-layer-pid=0,
             vendor.rtc-ext-enc-bitrate-mode=120000,
             vendor.rtc-ext-enc-caps-ltr.max-count=4,
             vendor.rtc-ext-enc-caps-preprocess.max-downscale-factor=0,
             vendor.rtc-ext-enc-caps-preprocess.rotation=0,
             vendor.rtc-ext-enc-caps-temporal-layers.max-b-count=6,
             vendor.rtc-ext-enc-caps-temporal-layers.max-p-count=7,
             vendor.rtc-ext-enc-caps-vt-driver-version.number=180315
             vendor.rtc-ext-enc-custom-profile-level.level=1,
             vendor.rtc-ext-enc-custom-profile-level.profile=1,
             vendor.rtc-ext-enc-low-latency.enable=0,
             vendor.rtc-ext-enc-ltr-count.num-ltr-frames=0,
             vendor.rtc-ext-enc-sar.height=0,
             vendor.rtc-ext-enc-sar.width=0,
             vendor.rtc-ext-enc-slice.spacing=0,
             vendor.sec-ext-enc-qp-range.B-maxQP=50,
             vendor.sec-ext-enc-qp-range.B-minQP=5,
             vendor.sec-ext-enc-qp-range.I-maxQP=50,
             vendor.sec-ext-enc-qp-range.I-minQP=5,
             vendor.sec-ext-enc-qp-range.P-maxQP=50,
             vendor.sec-ext-enc-qp-range.P-minQP=5,
             }
             *
             */

            mBufferInfo = new MediaCodec.BufferInfo();
            Log.d(TAG, "prepareEncoder:before:w,h:" + video_encoder_width + "," + video_encoder_height);
            video_encoder_format = MediaFormat.createVideoFormat(MIME_TYPE, video_encoder_width, video_encoder_height);
            Log.d(TAG, "prepareEncoder:after:w,h:" + video_encoder_width + "," + video_encoder_height);

            try
            {
                mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
                Log.d(TAG, "prepareEncoder:SUCCESS: " + mEncoder.getCodecInfo());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG, "prepareEncoder:EE1: " + e.getMessage());
            }

            // video_encoder_format.setInteger(MediaFormat.KEY_COLOR_STANDARD, COLOR_STANDARD_BT601_PAL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_PRIORITY, 0); // 0: realtime priority
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                //++//video_encoder_format.setInteger(MediaFormat.KEY_OPERATING_RATE, 25);
            }
            // -----------------------------------------------------------------------------
            // HINT: https://stackoverflow.com/questions/21284874/illegal-state-exception-when-calling-mediacodec-configure
            video_encoder_format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
            //
            // -----------------------------------------------------------------------------

            // -- vendor extensions --
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-low-latency.enable",1);
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-caps-temporal-layers.max-b-count",1);
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-caps-temporal-layers.max-p-count",1);
            // -- vendor extensions --

            video_encoder_format.setInteger(MediaFormat.KEY_BIT_RATE, v_bitrate_bits_per_second);

            //            video_encoder_format.setInteger(MediaFormat.KEY_BITRATE_MODE,
            //                                            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);

            video_encoder_format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            video_encoder_format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL_START);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                if (PREF__h264_encoder_use_intra_refresh)
                {
                    video_encoder_format.setInteger(MediaFormat.KEY_INTRA_REFRESH_PERIOD, 5);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_LATENCY, 1);
            }

            Log.d(TAG, "prepareEncoder:video_encoder_format: " + video_encoder_format);


            MediaCodecInfo.CodecCapabilities capabilities = mEncoder.getCodecInfo().getCapabilitiesForType(MIME_TYPE);
            int selectedColorFormat = 0;

            for (int i = 0; i < capabilities.colorFormats.length && selectedColorFormat == 0; i++)
            {
                int format = capabilities.colorFormats[i];
                switch (format)
                {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        selectedColorFormat = format;
                        Log.i(TAG, "prepareEncoder:1a:using format [" + i + "] " + format);
                        break;
                    default:
                        Log.i(TAG, "prepareEncoder:1a:Unsupported color format [" + i + "] " + format);
                        break;
                }
            }

            for (int i = 0; i < capabilities.colorFormats.length && selectedColorFormat == 0; i++)
            {
                int format = capabilities.colorFormats[i];
                switch (format)
                {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                        selectedColorFormat = format;
                        Log.i(TAG, "prepareEncoder:1b:using format [" + i + "] " + format);
                        break;
                    default:
                        Log.i(TAG, "prepareEncoder:1b:Unsupported color format [" + i + "] " + format);
                        break;
                }
            }

            for (int i = 0; i < capabilities.colorFormats.length && selectedColorFormat == 0; i++)
            {
                int format = capabilities.colorFormats[i];
                switch (format)
                {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                    case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                    case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
                        selectedColorFormat = format;
                        Log.i(TAG, "prepareEncoder:2:using format [" + i + "] " + format);
                        break;
                    default:
                        Log.i(TAG, "prepareEncoder:2:Unsupported color format [" + i + "] " + format);
                        break;
                }
            }

            if (selectedColorFormat != 0)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_COLOR_FORMAT, selectedColorFormat);

            }
            else
            {
                video_encoder_format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                                                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            }


            Log.i(TAG, "prepareEncoder:mEncoder.getName=" + mEncoder.getName());

            mEncoder.configure(video_encoder_format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();

        }
    }

    /**
     * Extracts all pending data from the encoder.
     */
    private static void drainEncoder()
    {
        final int TIMEOUT_USEC = 0;
        Log.d(TAG, "drainEncoder:start");

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true)
        {
            int encoderStatus = MediaCodec.INFO_TRY_AGAIN_LATER;

            try
            {
                encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            }
            catch (IllegalStateException e)
            {
                Log.d(TAG, "drainEncoder:dequeueOutputBuffer:EE:" + e.getMessage());
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                break;      // out of while
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
            {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "drainEncoder:encoder output video_encoder_format changed: " + newFormat);
            }
            else if (encoderStatus < 0)
            {
                Log.w(TAG, "drainEncoder:unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                // let's ignore it
            }
            else
            {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null)
                {
                    throw new RuntimeException("drainEncoder:encoderOutputBuffer " + encoderStatus + " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(TAG, "drainEncoder:ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0)
                {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & BUFFER_FLAG_END_OF_STREAM) != 0)
                {
                    Log.w(TAG, "drainEncoder:reached end of stream unexpectedly");
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private static void releaseEncoder()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG, "releaseEncoder:start ...");
            if (mEncoder != null)
            {
                try
                {
                    drainEncoder();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mEncoder.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mEncoder.release();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                mEncoder = null;
                Log.d(TAG, "releaseEncoder:SUCCESS");
            }
            else
            {
                Log.d(TAG, "releaseEncoder:already released");
            }
        }
    }

    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------

    static void stop_ringtone()
    {
        try
        {
            if (mMediaPlayer != null)
            {
                mMediaPlayer.stop();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            if (mMediaPlayer != null)
            {
                mMediaPlayer.release();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    void start_ringtone()
    {
        try
        {
            Uri ringtone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getApplicationContext(), ringtone_uri);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0)
            {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(true);
                try
                {
                    mMediaPlayer.prepare();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                mMediaPlayer.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
