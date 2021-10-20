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

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.List;

import static com.zoffcc.applications.trifa.CallingActivity.device_orientation;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.update_fps;
import static com.zoffcc.applications.trifa.MainActivity.PREF__UV_reversed;
import static com.zoffcc.applications.trifa.MainActivity.PREF__cam_recording_hint;
import static com.zoffcc.applications.trifa.MainActivity.PREF__camera_get_preview_format;
import static com.zoffcc.applications.trifa.MainActivity.PREF__fps_half;
import static com.zoffcc.applications.trifa.MainActivity.PREF__set_fps;
import static com.zoffcc.applications.trifa.MainActivity.PREF__video_cam_resolution;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CAMPREVIEW_NUM_BUFFERS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE;

public class CameraWrapper
{
    private static final String TAG = "CameraWrapper";
    static Camera mCamera;
    static Camera.CameraInfo cameraInfo = null;
    private static CameraWrapper mCameraWrapper;
    static int camera_video_rotate_angle = 0;
    private boolean mIsPreviewing = false;
    static byte[] data_new = null;
    private CameraPreviewCallback mCameraPreviewCallback;
    // private byte[] mImageCallbackBuffer = new byte[(CameraWrapper.IMAGE_WIDTH * CameraWrapper.IMAGE_HEIGHT) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2)) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2))];
    static Camera.Size camera_preview_size2 = null;
    int video_send_res = 0;
    boolean use_frame = true;
    static long camera_preview_call_back_ts_first_frame = -1;
    static long camera_preview_call_back_start_ts = -1;

    public interface CamOpenOverCallback
    {
        public void cameraHasOpened();
    }

    private CameraWrapper()
    {
    }

    public static synchronized CameraWrapper getInstance()
    {
        if (mCameraWrapper == null)
        {
            mCameraWrapper = new CameraWrapper();
        }

        return mCameraWrapper;
    }

    public void doOpenCamera(CamOpenOverCallback callback, boolean front_camera)
    {
        doOpenCamera_wrapper(callback, front_camera);
    }

    public void doOpenCamera_wrapper(CamOpenOverCallback callback, boolean front_camera)
    {
        Log.i(TAG, "doOpenCamera_wrapper:Camera open....");
        int numCameras = Camera.getNumberOfCameras();
        int camera_type = Camera.CameraInfo.CAMERA_FACING_FRONT;

        if (front_camera == false)
        {
            camera_type = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        cameraInfo = new Camera.CameraInfo();
        Log.i(TAG, "doOpenCamera_wrapper:numCameras=" + numCameras);

        for (int i = 0; i < numCameras; i++)
        {
            Log.i(TAG, "doOpenCamera_wrapper:check camera num " + i);
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == camera_type)
            {
                try
                {
                    Log.i(TAG, "doOpenCamera_wrapper:Camera.open " + i);
                    mCamera = Camera.open(i);
                    Log.i(TAG, "doOpenCamera_wrapper:Camera.open " + i + " done:" + mCamera);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "doOpenCamera_wrapper:check camera num " + i + " EE1:" + e.getMessage());

                    e.printStackTrace();

                    try
                    {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e1)
                    {
                        // e1.printStackTrace();
                    }

                    try
                    {
                        mCamera = Camera.open(i);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        mCamera = null;
                    }
                }

                break;
            }
        }

        if (mCamera == null)
        {
            try
            {
                Log.d(TAG, "doOpenCamera_wrapper:this_camera (" + front_camera + ") type found; opening default");
                mCamera = Camera.open();    // opens first back-facing camera
            }
            catch (Exception e)
            {
                e.printStackTrace();
                mCamera = null;
            }
        }

        if (mCamera == null)
        {
            throw new RuntimeException("doOpenCamera_wrapper:Unable to open camera");
        }

        Log.i(TAG, "doOpenCamera_wrapper:Camera open finished");
        callback.cameraHasOpened();
    }

    public void doStartPreview(SurfaceHolder holder, float previewRate)
    {
        Log.i(TAG, "doStartPreview...");

        if (mIsPreviewing)
        {
            Log.i(TAG, "doStartPreview:stopPreview");
            mCamera.stopPreview();
            return;
        }

        try
        {
            Log.i(TAG, "doStartPreview:setPreviewDisplay");
            mCamera.setPreviewDisplay(holder);
        }
        catch (IOException e)
        {
            Log.i(TAG, "doStartPreview:EE:" + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Log.i(TAG, "doStartPreview:EE2:" + e.getMessage());
            e.printStackTrace();
        }

        Log.i(TAG, "doStartPreview:initCamera");
        initCamera();
    }

    /*
    public void doStartPreview(SurfaceTexture surface, float previewRate)
    {
        Log.i(TAG, "doStartPreview()");

        if (mIsPreviewing)
        {
            mCamera.stopPreview();
            return;
        }

        try
        {
            mCamera.setPreviewTexture(surface);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        initCamera();
    }
    */

    public void doStopCamera()
    {
        Log.i(TAG, "doStopCamera");

        if (mCamera != null)
        {
            mCameraPreviewCallback.close();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            this.mIsPreviewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCameraPreviewCallback.reset();
            mCamera = null;
        }
    }

    static int getRotation()
    {
        Log.i(TAG, "[sum]================");
        Display display = CallingActivity.ca.getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        int result = 0;

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

        Log.i(TAG, "cam:getRotation:[display]degrees:1:=" + degrees);

        // now compensate for device orientation
        if (device_orientation == 90)
        {
            degrees = degrees + 270;
        }
        else if (device_orientation == 270)
        {
            degrees = degrees + 90;
        }
        else if (device_orientation == 180)
        {
            degrees = degrees + 180;
        }

        Log.i(TAG, "cam:getRotation:[display]degrees:2:=" + degrees);

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (cameraInfo.orientation + degrees) % 360;
            Log.i(TAG,
                  "cam:getRotation:[FRONT CAMERA] tmp=" + (cameraInfo.orientation + degrees) + " result=" + result);
            // result = (360 - result) % 360;    // compensate the mirror
        }
        else
        {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }


        Log.i(TAG, "cam:getRotation:[camera]cameraInfo.orientation=" + cameraInfo.orientation);
        Log.i(TAG, "cam:getRotation:[display]degrees=" + degrees);
        Log.i(TAG, "cam:getRotation:[sum]result=" + result);
        Log.i(TAG, "cam:getRotation:[sum]================");
        return result;
    }

    private void initCamera()
    {
        Log.i(TAG, "initCamera:camera:startPreview:initCamera:start");

        if (mCamera != null)
        {
            camera_video_rotate_angle = getRotation();
            Log.i(TAG, "initCamera:camera_video_rotate_angle=" + camera_video_rotate_angle);
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // camera_video_rotate_angle = 0;
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            // TODO: ------ DEBUG --------
            CameraSurfacePreview.mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            try
            {
                int j;

                for (j = 0; j < CameraSurfacePreview.mSupportedPreviewSizes.size(); j++)
                {
                    Log.i(TAG, "initCamera:SupportedPreviewSizes=" +
                               CameraSurfacePreview.mSupportedPreviewSizes.get(j).width + "x" +
                               CameraSurfacePreview.mSupportedPreviewSizes.get(j).height);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            List<Integer> pfs = mCamera.getParameters().getSupportedPreviewFormats();

            try
            {
                int j;

                for (j = 0; j < pfs.size(); j++)
                {
                    Log.i(TAG, "initCamera:SupportedPreviewFormats=" + pfs.get(j));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "initCamera:camera:startPreview:EEE:" + e.getMessage());
            }

            Camera.Parameters mCameraParamters = mCamera.getParameters();
            List<Integer> camera_preview_formats = mCameraParamters.getSupportedPreviewFormats();
            int selectedCameraPreviewFormat = 0;

            if (PREF__camera_get_preview_format.equals("YV12"))
            {
                for (int i = 0; i < camera_preview_formats.size() && selectedCameraPreviewFormat == 0; i++)
                {
                    int format = camera_preview_formats.get(i);

                    switch (format)
                    {
                        case ImageFormat.YV12: // this is preferred
                            selectedCameraPreviewFormat = format;
                            Log.i(TAG, "initCamera:SupportedPreviewFormats:using Preview format [" + i + "] " + format);
                            break;

                        default:
                            Log.i(TAG, "initCamera:SupportedPreviewFormats:Unsupported Preview format [" + i + "] " +
                                       format);
                            break;
                    }
                }
            }

            if (selectedCameraPreviewFormat == 0)
            {
                // if YV12 is not supported then try NV21
                for (int i = 0; i < camera_preview_formats.size() && selectedCameraPreviewFormat == 0; i++)
                {
                    int format = camera_preview_formats.get(i);

                    switch (format)
                    {
                        case ImageFormat.NV21:
                            selectedCameraPreviewFormat = format;
                            Log.i(TAG,
                                  "initCamera:SupportedPreviewFormats:2:using Preview format [" + i + "] " + format);
                            break;

                        default:
                            Log.i(TAG, "initCamera:SupportedPreviewFormats:2:Unsupported Preview format [" + i + "] " +
                                       format);
                            break;
                    }
                }
            }

            if (selectedCameraPreviewFormat == 0)
            {
                mCameraParamters.setPreviewFormat(ImageFormat.YV12); // order here is Y-V-U !!
                Log.i(TAG, "initCamera:SupportedPreviewFormats:set default format YV12");
            }
            else
            {
                mCameraParamters.setPreviewFormat(selectedCameraPreviewFormat);
            }

            mCameraParamters.setFlashMode("off");
            mCameraParamters.setRotation(0);

            try
            {
                List<Integer> preview_framerates = mCameraParamters.getSupportedPreviewFrameRates();
                Log.i(TAG, "initCamera:preview_framerates=" + preview_framerates);
                List<int[]> preview_framerates2 = mCameraParamters.getSupportedPreviewFpsRange();
                int i;
                int j;

                for (i = 0; i < preview_framerates2.size(); i++)
                {
                    Log.i(TAG, "initCamera:preview_framerates2[" + i + "]=" + preview_framerates2.get(i).length);

                    for (j = 0; j < preview_framerates2.get(i).length; j++)
                    {
                        Log.i(TAG,
                              "initCamera:preview_framerates2[" + i + "," + j + "]=" + preview_framerates2.get(i)[j]);
                    }
                }

                // HINT: this crashes some devices
                if (PREF__set_fps)
                {
                    Log.i(TAG, "initCamera:preview_framerates2:SET:setting FPS to 15:START");
                    mCameraParamters.setPreviewFpsRange(15000, 15000);
                    Log.i(TAG, "initCamera:preview_framerates2:SET:setting FPS to 15:Ready");
                }
                else
                {
                    Log.i(TAG, "initCamera:preview_framerates2:SET:not setting FPS");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (PREF__cam_recording_hint)
            {
                mCameraParamters.setRecordingHint(true);
            }

            mCameraParamters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            mCameraParamters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            Log.i(TAG, "initCamera:preview size before=" + mCameraParamters.getPreviewSize().width + "," +
                       mCameraParamters.getPreviewSize().height);

            // ---------- configure camera resolution for video calling ------------
            int IMAGE_WIDTH = 640;
            int IMAGE_HEIGHT = 480;

            if (PREF__video_cam_resolution == 2)
            {
                IMAGE_WIDTH = 1920;
                IMAGE_HEIGHT = 1080;
            }
            else if (PREF__video_cam_resolution == 1)
            {
                IMAGE_WIDTH = 1280;
                IMAGE_HEIGHT = 720;
            }
            else
            {
                // default setting
                IMAGE_WIDTH = 640;
                IMAGE_HEIGHT = 480;
            }
            // ---------- configure camera resolution for video calling ------------

            mCameraParamters.setPreviewSize(IMAGE_WIDTH, IMAGE_HEIGHT);
            Log.i(TAG, "initCamera:preview size after 1=" + mCameraParamters.getPreviewSize().width + "," +
                       mCameraParamters.getPreviewSize().height);
            mCamera.setDisplayOrientation(90); // always 90 ??
            Log.i(TAG, "initCamera:preview size after 2=" + mCameraParamters.getPreviewSize().width + "," +
                       mCameraParamters.getPreviewSize().height);

            //if (mCameraParamters.isVideoStabilizationSupported())
            //{
            //    mCameraParamters.setVideoStabilization(true);
            //}

            List<String> focusModes = mCameraParamters.getSupportedFocusModes();
            if (focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
                mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            mCameraPreviewCallback = new CameraPreviewCallback();
            // ------ use buffer ------
            mCamera.setParameters(mCameraParamters);
            Camera.Parameters mCameraParamters2 = mCamera.getParameters();
            int previewFormat = mCameraParamters2.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(previewFormat);
            float byteperpixel = (float) bitsperpixel / 8.0f;
            Camera.Size camerasize = mCameraParamters2.getPreviewSize();
            int frame_bytesize = (int) (((float) mCameraParamters.getPreviewSize().width *
                                         (float) mCameraParamters.getPreviewSize().height) * byteperpixel);
            Log.i(TAG,
                  "initCamera:bitsperpixel=" + bitsperpixel + " byteperpixel=" + byteperpixel + " frame_bytesize=" +
                  frame_bytesize);
            Log.i(TAG, "initCamera:previewFormat=" + previewFormat + " camerasize.w=" +
                       mCameraParamters.getPreviewSize().width + " camerasize.h=" +
                       mCameraParamters.getPreviewSize().height);
            Log.i(TAG, "initCamera:previewFormats:ImageFormat.YV12=" + ImageFormat.YV12);
            Log.i(TAG, "initCamera:previewFormats:ImageFormat.NV21=" + ImageFormat.NV21);
            Log.i(TAG, "initCamera:previewFormats:ImageFormat.YUY2=" + ImageFormat.YUY2);
            Camera.Size s = mCameraParamters.getPreviewSize();
            mCamera.setPreviewCallbackWithBuffer(
                    mCameraPreviewCallback);    // assign the callback called when a frame is shown by the camera preview (for frame processing)
            // **broken ** // setupCallback((3 * s.width * s.height / 2));
            setupCallback(frame_bytesize);
            // mCamera.addCallbackBuffer(new byte[3 * s.width * s.height / 2]);  // create a reusable buffer for the data passed to onPreviewFrame call (in order to avoid GC)
            mCameraPreviewCallback.reset();
            Log.i(TAG, "initCamera:previewFormats:999");
            // ------ use buffer ------

            // mCamera.setParameters(mCameraParamters);

            try
            {
                Log.i(TAG, "initCamera:camera:startPreview:001");
                mCamera.startPreview();
                Log.i(TAG, "initCamera:camera:startPreview:002");
            }
            catch (Exception pe)
            {
                pe.printStackTrace();
                Log.i(TAG, "initCamera:camera:startPreview:EE:" + pe.getMessage());
            }

            this.mIsPreviewing = true;
        }
        else
        {
            Log.i(TAG, "initCamera:camera:startPreview:mCamera==NULL");
        }
    }

    private void setupCallback(int bufferSize)
    {
        for (int i = 0; i < CAMPREVIEW_NUM_BUFFERS; ++i)
        {
            byte[] cameraBuffer = new byte[bufferSize];
            mCamera.addCallbackBuffer(cameraBuffer);
        }
    }

    private class proccesImageOnBackground extends AsyncTask<Void, Void, Void>
    {
        private byte[] data;
        public int[] procImage;
        private int num = -1;
        CameraPreviewCallback cb;
        long capture_ts;
        long s_time;

        proccesImageOnBackground(final byte[] _data, CameraPreviewCallback _cb)
        {
            // Log.i(TAG, "proccesImageOnBackground:--START--");
            // s_time = System.currentTimeMillis();

            data = _data;
            cb = _cb;
            // num = (int) ((Math.random() * 10000f));
            capture_ts = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
/*
            try
            {
                Thread.currentThread().setName("t_vcam_get");
            }
            catch (Exception e)
            {
            }
*/

            // Log.i(TAG, "doInBackground:start:#" + num);
            try
            {
                // Log.i(TAG, "Callstate.tox_call_state=" + Callstate.tox_call_state + " my_video_enabled=" + Callstate.my_video_enabled);
                if (Callstate.my_video_enabled == 1)
                {
                    // only send video frame if call has started
                    if (!((Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_NONE.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_ERROR.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_FINISHED.value)))
                    {
                        if ((PREF__fps_half) && (!use_frame))
                        {
                            use_frame = true;
                        }
                        else
                        {
                            if (CameraWrapper.camera_video_rotate_angle == 90)
                            {
                                if (PREF__camera_get_preview_format.equals("YV12"))
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = YV12rotate90(data, data_new, camera_preview_size2.width,
                                                            camera_preview_size2.height);
                                }
                                else
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = NV21rotate90(data, data_new, camera_preview_size2.width,
                                                            camera_preview_size2.height);
                                }

                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(data_new);

                                if (PREF__UV_reversed)
                                {
                                    video_send_res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(data_new,
                                                                                                              tox_friend_by_public_key__wrapper(
                                                                                                                      Callstate.friend_pubkey),
                                                                                                              camera_preview_size2.height,
                                                                                                              camera_preview_size2.width,
                                                                                                              capture_ts);

                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:004:res=" + video_send_res + ":" +
                                                   ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    HelperGeneric.toxav_video_send_frame_wrapper(data_new,
                                                                                 tox_friend_by_public_key__wrapper(
                                                                                         Callstate.friend_pubkey),
                                                                                 camera_preview_size2.height,
                                                                                 camera_preview_size2.width,
                                                                                 capture_ts);
                                }

                            }
                            else if (CameraWrapper.camera_video_rotate_angle == 270)
                            {

                                if (PREF__camera_get_preview_format.equals("YV12"))
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = YV12rotate270(data, data_new, camera_preview_size2.width,
                                                             camera_preview_size2.height);
                                }
                                else
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = NV21rotate270(data, data_new, camera_preview_size2.width,
                                                             camera_preview_size2.height);
                                }

                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(data_new);

                                if (PREF__UV_reversed)
                                {
                                    video_send_res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(data_new,
                                                                                                              tox_friend_by_public_key__wrapper(
                                                                                                                      Callstate.friend_pubkey),
                                                                                                              camera_preview_size2.height,
                                                                                                              camera_preview_size2.width,
                                                                                                              capture_ts);

                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:006:res=" + video_send_res + ":" +
                                                   ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    HelperGeneric.toxav_video_send_frame_wrapper(data_new,
                                                                                 tox_friend_by_public_key__wrapper(
                                                                                         Callstate.friend_pubkey),
                                                                                 camera_preview_size2.height,
                                                                                 camera_preview_size2.width,
                                                                                 capture_ts);
                                }
                            }
                            else if (CameraWrapper.camera_video_rotate_angle == 180)
                            {
                                if (PREF__camera_get_preview_format.equals("YV12"))
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = YV12rotate180(data, data_new, camera_preview_size2.width,
                                                             camera_preview_size2.height);
                                }
                                else
                                {
                                    if (data_new == null)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    else if (data_new.length < data.length)
                                    {
                                        data_new = new byte[data.length];
                                    }
                                    data_new = NV21rotate180(data, data_new, camera_preview_size2.width,
                                                             camera_preview_size2.height);
                                }


                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(data_new);

                                // -------------------------------------------------
                                // android has the order YVU (instead of YUV) !!
                                // so we need to call ..._uv_reversed here
                                // -------------------------------------------------
                                if (PREF__UV_reversed)
                                {
                                    video_send_res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(data_new,
                                                                                                              tox_friend_by_public_key__wrapper(
                                                                                                                      Callstate.friend_pubkey),
                                                                                                              camera_preview_size2.width,
                                                                                                              camera_preview_size2.height,
                                                                                                              capture_ts);

                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:007:res=" + video_send_res + ":" +
                                                   ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    HelperGeneric.toxav_video_send_frame_wrapper(data_new,
                                                                                 tox_friend_by_public_key__wrapper(
                                                                                         Callstate.friend_pubkey),
                                                                                 camera_preview_size2.width,
                                                                                 camera_preview_size2.height,
                                                                                 capture_ts);
                                }
                            }
                            else
                            {
                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(data);

                                // -------------------------------------------------
                                // android has the order YVU (instead of YUV) !!
                                // so we need to call ..._uv_reversed here
                                // -------------------------------------------------
                                if (PREF__UV_reversed)
                                {
                                    video_send_res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(data,
                                                                                                              tox_friend_by_public_key__wrapper(
                                                                                                                      Callstate.friend_pubkey),
                                                                                                              camera_preview_size2.width,
                                                                                                              camera_preview_size2.height,
                                                                                                              capture_ts);

                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:008:res=" + video_send_res + ":" +
                                                   ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    HelperGeneric.toxav_video_send_frame_wrapper(data,
                                                                                 tox_friend_by_public_key__wrapper(
                                                                                         Callstate.friend_pubkey),
                                                                                 camera_preview_size2.width,
                                                                                 camera_preview_size2.height,
                                                                                 capture_ts);
                                }
                            }

                            if (last_video_frame_sent == -1)
                            {
                                last_video_frame_sent = System.currentTimeMillis();
                                count_video_frame_sent++;
                            }
                            else
                            {
                                if ((count_video_frame_sent > 20) ||
                                    ((last_video_frame_sent + 2000) < System.currentTimeMillis()))
                                {
                                    VIDEO_FRAME_RATE_OUTGOING = (int) ((((float) count_video_frame_sent / ((float) (
                                            (System.currentTimeMillis() - last_video_frame_sent) / 1000.0f))) / 1.0f) +
                                                                       0.5);
                                    // Log.i(TAG, "VIDEO_FRAME_RATE_OUTGOING=" + VIDEO_FRAME_RATE_OUTGOING + " fps");
                                    update_fps();
                                    last_video_frame_sent = System.currentTimeMillis();
                                    count_video_frame_sent = -1;
                                }

                                count_video_frame_sent++;
                            }

                            use_frame = false;
                        }
                    }
                    else
                    {
                        // Log.i(TAG, "onPreviewFrame:not sending video:Callstate.tox_call_state=" + Callstate.tox_call_state);
                    }
                }
            }
            catch (java.nio.BufferOverflowException e)
            {
                e.printStackTrace();
                Log.i(TAG, "onPreviewFrame:EE1:" + e.getMessage());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "onPreviewFrame:EE2:" + e.getMessage());
            }

            // Log.i(TAG, "doInBackground:END:#" + num);

            // Log.i(TAG, "proccesImageOnBackground:--END--:" + (System.currentTimeMillis() - s_time) + "ms");

            return null;
        }


        @Override
        protected void onPostExecute(Void voids)
        {
            // Log.i(TAG, "doInBackground:end:#" + num);
            if (data != null)
            {
                // Log.i(TAG, "onPostExecute:--START--");
                // long s_time_3 = System.currentTimeMillis();

                if (mCamera != null)
                {
                    try
                    {
                        mCamera.addCallbackBuffer(
                                data); // return the data buffer for then next onPreviewFrame call (no GC)
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                // Log.i(TAG, "onPostExecute:--END--:" + (System.currentTimeMillis() - s_time_3) + "ms");
            }
        }

        @Override
        protected void onCancelled(Void voids)
        {
        }
    }

    class CameraPreviewCallback implements Camera.PreviewCallback
    {
        private static final String TAG = "CameraPreviewCallback";
        public proccesImageOnBackground myProccesImageOnBackground;

        private CameraPreviewCallback()
        {
            Log.i(TAG, "CameraPreviewCallback");
            // videoEncoder = new VideoEncoderFromBuffer(CameraWrapper.IMAGE_WIDTH, CameraWrapper.IMAGE_HEIGHT);
            // HINT: save timestamp here, and if there is no buffercallback within 5 seconds, reopen the camera again!
            camera_preview_call_back_ts_first_frame = -1;
        }

        public void reset()
        {
            camera_preview_call_back_ts_first_frame = -1;
            camera_preview_call_back_start_ts = System.currentTimeMillis();

            Log.i(TAG, "CameraPreviewCallback:reset");
            if (myProccesImageOnBackground != null)
            {
                myProccesImageOnBackground.cancel(true);
            }
        }

        void close()
        {
            camera_preview_call_back_ts_first_frame = -1;
            camera_preview_call_back_start_ts = -1;

            Log.i(TAG, "CameraPreviewCallback:close");
            try
            {
                // videoEncoder.close();
            }
            catch (Exception e)  // java.lang.IllegalStateException
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(final byte[] data, Camera camera)
        {
            // ----------------------------
            if (data == null)
            {
                Log.i(TAG, "onPreviewFrame:data=null");
            }
            else
            {
                if (camera_preview_call_back_ts_first_frame == -1)
                {
                    camera_preview_call_back_ts_first_frame = System.currentTimeMillis();
                    camera_preview_call_back_start_ts = -1;
                }

                if (camera_preview_size2 == null)
                {
                    try
                    {
                        Camera.Parameters p = camera.getParameters();
                        camera_preview_size2 = p.getPreviewSize();
                        Log.i(TAG,
                              "onPreviewFrame:w=" + camera_preview_size2.width + " h=" + camera_preview_size2.height +
                              " camera_video_rotate_angle=" + CameraWrapper.camera_video_rotate_angle);
                        int actual_preview_format = p.getPreviewFormat();
                        Log.i(TAG, "onPreviewFrame:ImageFormat.YV12=" + ImageFormat.YV12);
                        Log.i(TAG, "onPreviewFrame:ImageFormat.NV21=" + ImageFormat.NV21);
                        Log.i(TAG, "onPreviewFrame:actual_preview_format=" + actual_preview_format);
                        Log.i(TAG, "onPreviewFrame:buffer size=" + data.length);
                        int stride = (int) Math.ceil(camera_preview_size2.width / 16.0) * 16;
                        int y_size = stride * camera_preview_size2.height;
                        int c_stride = (int) Math.ceil((stride / 2) / 16.0) * 16;
                        int c_size = c_stride * camera_preview_size2.height / 2;
                        int size = y_size + c_size * 2;
                        int cr_offset = y_size;
                        int cb_offset = y_size + c_size;
                        Log.i(TAG, "onPreviewFrame:stride=" + stride);
                        Log.i(TAG, "onPreviewFrame:y_size=" + y_size);
                        Log.i(TAG, "onPreviewFrame:c_stride=" + c_stride);
                        Log.i(TAG, "onPreviewFrame:c_size=" + c_size);
                        Log.i(TAG, "onPreviewFrame:size=" + size);
                        Log.i(TAG, "onPreviewFrame:cr_offset=" + cr_offset);
                        Log.i(TAG, "onPreviewFrame:cb_offset=" + cb_offset);

                        if (MainActivity.video_buffer_2 != null)
                        {
                            // video_buffer_2.clear();
                            MainActivity.video_buffer_2 = null;
                        }

                        /*
                         * YUV420 frame with width * height
                         *
                         * @param y Luminosity plane. Size = MAX(width, abs(ystride)) * height.
                         * @param u U chroma plane. Size = MAX(width/2, abs(ustride)) * (height/2).
                         * @param v V chroma plane. Size = MAX(width/2, abs(vstride)) * (height/2).
                         */
                        int y_layer_size = y_size; // (int) camera_preview_size2.width * camera_preview_size2.height;
                        int u_layer_size = c_size;// (int) (camera_preview_size2.width / 2) * (camera_preview_size2.height / 2);
                        int v_layer_size = c_size;// (int) (camera_preview_size2.width / 2) * (camera_preview_size2.height / 2);
                        int frame_width_px = (int) camera_preview_size2.width;
                        int frame_height_px = (int) camera_preview_size2.height;
                        int buffer_size_in_bytes2 = y_layer_size + v_layer_size + u_layer_size;
                        Log.i(TAG, "onPreviewFrame:YUV420 frame w1=" + camera_preview_size2.width + " h1=" +
                                   camera_preview_size2.height + " bytes=" + buffer_size_in_bytes2);
                        Log.i(TAG,
                              "onPreviewFrame:YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" +
                              buffer_size_in_bytes2);
                        MainActivity.video_buffer_2 = ByteBuffer.allocateDirect((2 * buffer_size_in_bytes2) + 1);
                        MainActivity.set_JNI_video_buffer2(MainActivity.video_buffer_2, camera_preview_size2.width,
                                                           camera_preview_size2.height);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        camera_preview_size2 = null;
                        Log.i(TAG, "onPreviewFrame:EE1:" + e2.getMessage());
                    }
                }

                try
                {
                    if (!Callstate.audio_call)
                    {
                        myProccesImageOnBackground = (proccesImageOnBackground) new proccesImageOnBackground(data,
                                                                                                             this).execute();
                    }
                }
                catch (Exception ea)
                {
                    ea.printStackTrace();
                }
            }
        }
    }

    public static byte[] NV21rotate90(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }
        // Rotate the U and V color components
        int size = imageWidth * imageHeight;
        i = size * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2)
        {
            for (int y = 0; y < imageHeight / 2; y++)
            {
                output[i--] = data[size + (y * imageWidth) + x];
                output[i--] = data[size + (y * imageWidth) + (x - 1)];
            }
        }
        return output;
    }

    public static byte[] YUV420rotate90(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }

        // Rotate the U and V color components
        int size = imageWidth * imageHeight;
        i = size;
        int j = size;
        int uv = size / 4;
        for (int x = 0; x < (imageWidth / 2); x++)
        {
            for (int y = (imageHeight / 2) - 1; y >= 0; y--)
            {
                try
                {
                    output[i] = data[j + (y * (imageWidth / 2) + x)];
                    output[i + uv] = data[j + uv + (y * (imageWidth / 2) + x)];
                }
                catch (Exception e)
                {
                    //Log.i(TAG, "iiiiiii:i=" + i + " j=" + j + " uv=" + uv + " y=" + y + " x=" + x + " imageWidth=" +
                    //           imageWidth);
                    //e.printStackTrace();
                }
                i++;
            }
        }
        return output;
    }

    public static byte[] NV21rotate180(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        int count = 0;
        for (int i = imageWidth * imageHeight - 1; i >= 0; i--)
        {
            output[count] = data[i];
            count++;
        }
        for (int i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth * imageHeight; i -= 2)
        {
            output[count++] = data[i - 1];
            output[count++] = data[i];
        }
        return output;
    }

    public static byte[] NV21rotate270(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = imageWidth - 1; x >= 0; x--)
        {
            for (int y = 0; y < imageHeight; y++)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }

        // Rotate the U and V color components
        i = imageWidth * imageHeight;
        int uvHeight = imageHeight / 2;
        for (int x = imageWidth - 1; x >= 0; x -= 2)
        {
            for (int y = imageHeight; y < uvHeight + imageHeight; y++)
            {
                output[i++] = data[y * imageWidth + x - 1];
                output[i++] = data[y * imageWidth + x];
            }
        }
        return output;
    }

    public static byte[] YV12rotate90(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }
        final int size = imageWidth * imageHeight;
        final int colorSize = size / 4;
        final int colorHeight = colorSize / imageWidth;
        // Rotate the U and V color components
        for (int x = 0; x < imageWidth / 2; x++)
        {
            for (int y = colorHeight - 1; y >= 0; y--)
            {
                //V
                output[i + colorSize] = data[colorSize + size + (imageWidth * y) + x + (imageWidth / 2)];
                output[i + colorSize + 1] = data[colorSize + size + (imageWidth * y) + x];
                //U
                output[i++] = data[size + (imageWidth * y) + x + (imageWidth / 2)];
                output[i++] = data[size + (imageWidth * y) + x];
            }
        }
        return output;
    }

    public static byte[] YV12rotate180(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        int count = 0;
        final int size = imageWidth * imageHeight;
        for (int i = size - 1; i >= 0; i--)
        {
            output[count++] = data[i];
        }
        final int midColorSize = size / 4;
        //U
        for (int i = size + midColorSize - 1; i >= size; i--)
        {
            output[count++] = data[i];
        }
        //V
        for (int i = data.length - 1; i >= imageWidth * imageHeight + midColorSize; i--)
        {
            output[count++] = data[i];
        }
        return output;
    }

    public static byte[] YV12rotate270(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = imageWidth - 1; x >= 0; x--)
        {
            for (int y = 0; y < imageHeight; y++)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }

        // Rotate the U and V color components
        final int size = imageWidth * imageHeight;
        final int colorSize = size / 4;
        final int colorHeight = colorSize / imageWidth;

        for (int x = 0; x < imageWidth / 2; x++)
        {
            for (int y = 0; y < colorHeight; y++)
            {
                //V
                output[i + colorSize] = data[colorSize + size + (imageWidth * y) - x + (imageWidth / 2) - 1];
                output[i + colorSize + 1] = data[colorSize + size + (imageWidth * y) - x + imageWidth - 1];
                //U
                output[i++] = data[size + (imageWidth * y) - x + (imageWidth / 2) - 1];
                output[i++] = data[size + (imageWidth * y) - x + imageWidth - 1];
            }
        }
        return output;
    }

    static byte[] YUV_420_888toNV21_x(byte[] input, int imageWidth, int imageHeight)
    {
        int width = imageWidth;
        int height = imageHeight;
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        int ySize1 = imageWidth * imageHeight;
        int uSize1 = ySize / 4;
        int vSize1 = ySize / 4;

        ByteBuffer yBuffer = ByteBuffer.allocateDirect(ySize1);
        ByteBuffer uBuffer = ByteBuffer.allocateDirect(uSize1);
        ByteBuffer vBuffer = ByteBuffer.allocateDirect(vSize1);
        yBuffer.put(input, 0, ySize1);
        uBuffer.put(input, ySize1, uSize1);
        vBuffer.put(input, ySize1 + uSize1, vSize1);

        yBuffer.rewind();
        uBuffer.rewind();
        vBuffer.rewind();

        int rowStride = width;
        int pos = 0;
        yBuffer.get(nv21, 0, ySize);
        pos += ySize;
        rowStride = width / 2;
        int pixelStride = 1;

        for (int row = 0; row < height / 2; row++)
        {
            for (int col = 0; col < width / 2; col++)
            {
                int vuPos = col * pixelStride + row * rowStride;
                try
                {
                    nv21[pos++] = vBuffer.get(vuPos);
                    nv21[pos++] = uBuffer.get(vuPos);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        return nv21;
    }

    static byte[] YUV_420_888toNV21(Image image)
    {

        int width = image.getWidth();
        int height = image.getHeight();
        // Log.i(TAG, "iiiiii:" + width + " " + height);
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V
        yBuffer.rewind();
        uBuffer.rewind();
        vBuffer.rewind();

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width)
        { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else
        {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos < ySize; pos += width)
            {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1))
        {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try
            {
                vBuffer.put(1, (byte) ~savePixel);
                if (uBuffer.get(0) == (byte) ~savePixel)
                {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex)
            {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++)
        {
            for (int col = 0; col < width / 2; col++)
            {
                int vuPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

}
