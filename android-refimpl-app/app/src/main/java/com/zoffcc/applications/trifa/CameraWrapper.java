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
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.PREF__UV_reversed;
import static com.zoffcc.applications.trifa.MainActivity.PREF__cam_recording_hint;
import static com.zoffcc.applications.trifa.MainActivity.PREF__fps_half;
import static com.zoffcc.applications.trifa.MainActivity.PREF__set_fps;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.update_fps;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CAMPREVIEW_NUM_BUFFERS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;

public class CameraWrapper
{
    private static final String TAG = "CameraWrapper";
    static Camera mCamera;
    private Camera.Parameters mCameraParamters;
    static Camera.CameraInfo cameraInfo = null;
    private static CameraWrapper mCameraWrapper;
    static int camera_video_rotate_angle = 0;
    private boolean mIsPreviewing = false;
    private float mPreviewRate = -1.0f;
    public static final int IMAGE_WIDTH = 640; // 1280
    public static final int IMAGE_HEIGHT = 480; // 720
    static byte[] data_new = null;
    static byte[] data_new2 = null;
    static boolean mirror_cam_image = false; // mirror the camera image
    private CameraPreviewCallback mCameraPreviewCallback;
    // private byte[] mImageCallbackBuffer = new byte[(CameraWrapper.IMAGE_WIDTH * CameraWrapper.IMAGE_HEIGHT) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2)) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2))];
    static Camera.Size camera_preview_size2 = null;
    int video_send_res = 0;
    boolean use_frame = true;

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
        Log.i(TAG, "Camera open....");
        int numCameras = Camera.getNumberOfCameras();

        int camera_type = Camera.CameraInfo.CAMERA_FACING_FRONT;
        if (front_camera == false)
        {
            camera_type = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numCameras; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == camera_type)
            {
                try
                {
                    mCamera = Camera.open(i);
                }
                catch (Exception e)
                {
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
                Log.d(TAG, "this camera (" + front_camera + ") type found; opening default");
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
            throw new RuntimeException("Unable to open camera");
        }

        Log.i(TAG, "Camera open over....");
        callback.cameraHasOpened();
    }

    public void doStartPreview(SurfaceHolder holder, float previewRate)
    {
        Log.i(TAG, "doStartPreview...");
        if (mIsPreviewing)
        {
            Log.i(TAG, "doStartPreview:stopPreview");
            this.mCamera.stopPreview();
            return;
        }

        try
        {
            Log.i(TAG, "doStartPreview:setPreviewDisplay");
            this.mCamera.setPreviewDisplay(holder);
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

    public void doStartPreview(SurfaceTexture surface, float previewRate)
    {
        Log.i(TAG, "doStartPreview()");
        if (mIsPreviewing)
        {
            this.mCamera.stopPreview();
            return;
        }

        try
        {
            this.mCamera.setPreviewTexture(surface);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        initCamera();
    }

    public void doStopCamera()
    {
        Log.i(TAG, "doStopCamera");
        if (this.mCamera != null)
        {
            mCameraPreviewCallback.close();
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mIsPreviewing = false;
            this.mPreviewRate = -1f;
            mCamera.setPreviewCallback(null);
            this.mCamera.release();
            mCameraPreviewCallback.reset();
            this.mCamera = null;
        }
    }

    private int getRotation()
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

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (cameraInfo.orientation + degrees) % 360;
            Log.i(TAG, "[FRONT CAMERA] tmp=" + (cameraInfo.orientation + degrees) + " result=" + result);
            // result = (360 - result) % 360;    // compensate the mirror
        }
        else
        {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        Log.i(TAG, "[camera]cameraInfo.orientation=" + cameraInfo.orientation);
        Log.i(TAG, "[display]degrees=" + degrees);
        Log.i(TAG, "[sum]result=" + result);
        Log.i(TAG, "[sum]================");

        return result;
    }

    private void initCamera()
    {
        if (this.mCamera != null)
        {
            this.camera_video_rotate_angle = getRotation();

            CameraSurfacePreview.mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            try
            {
                int j;
                for (j = 0; j < CameraSurfacePreview.mSupportedPreviewSizes.size(); j++)
                {
                    Log.i(TAG, "SupportedPreviewSizes=" + CameraSurfacePreview.mSupportedPreviewSizes.get(j).width + "x" + CameraSurfacePreview.mSupportedPreviewSizes.get(j).height);
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
                    Log.i(TAG, "SupportedPreviewFormats=" + pfs.get(j));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            this.mCameraParamters = this.mCamera.getParameters();
            this.mCameraParamters.setPreviewFormat(ImageFormat.YV12); // order here is Y-V-U !!
            this.mCameraParamters.setFlashMode("off");

            try
            {
                List<Integer> preview_framerates = this.mCameraParamters.getSupportedPreviewFrameRates();
                Log.i(TAG, "preview_framerates=" + preview_framerates);
                List<int[]> preview_framerates2 = this.mCameraParamters.getSupportedPreviewFpsRange();
                int i;
                int j;
                for (i = 0; i < preview_framerates2.size(); i++)
                {
                    Log.i(TAG, "preview_framerates2[" + i + "]=" + preview_framerates2.get(i).length);
                    for (j = 0; j < preview_framerates2.get(i).length; j++)
                    {
                        Log.i(TAG, "preview_framerates2[" + i + "," + j + "]=" + preview_framerates2.get(i)[j]);
                    }
                }

                // HINT: this crashes some devices
                if (PREF__set_fps)
                {
                    Log.i(TAG, "preview_framerates2:SET:setting FPS to 15:START");
                    this.mCameraParamters.setPreviewFpsRange(15000, 15000);
                    Log.i(TAG, "preview_framerates2:SET:setting FPS to 15:Ready");
                }
                else
                {
                    Log.i(TAG, "preview_framerates2:SET:not setting FPS");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (PREF__cam_recording_hint)
            {
                this.mCameraParamters.setRecordingHint(true);
            }
            this.mCameraParamters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            this.mCameraParamters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            Log.i(TAG, "preview size before=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);
            this.mCameraParamters.setPreviewSize(IMAGE_WIDTH, IMAGE_HEIGHT);
            Log.i(TAG, "preview size after 1=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);
            this.mCamera.setDisplayOrientation(90); // always 90 ??
            Log.i(TAG, "preview size after 2=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);

            mCameraPreviewCallback = new CameraPreviewCallback();

            // ------ use buffer ------

            this.mCamera.setParameters(this.mCameraParamters);
            Camera.Parameters mCameraParamters2 = this.mCamera.getParameters();

            int previewFormat = mCameraParamters2.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(previewFormat);
            float byteperpixel = (float) bitsperpixel / 8.0f;
            Camera.Size camerasize = mCameraParamters2.getPreviewSize();
            int frame_bytesize = (int) (((float) camerasize.width * (float) camerasize.height) * byteperpixel);
            Log.i(TAG, "bitsperpixel=" + bitsperpixel + " byteperpixel=" + byteperpixel + " frame_bytesize=" + frame_bytesize);
            Log.i(TAG, "previewFormat=" + previewFormat + " camerasize.w=" + camerasize.width + " camerasize.h=" + camerasize.height);
            Log.i(TAG, "previewFormats:ImageFormat.YV12=" + ImageFormat.YV12);
            Log.i(TAG, "previewFormats:ImageFormat.NV21=" + ImageFormat.NV21);
            Log.i(TAG, "previewFormats:ImageFormat.YUY2=" + ImageFormat.YUY2);

            Camera.Size s = this.mCameraParamters.getPreviewSize();
            mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);    // assign the callback called when a frame is shown by the camera preview (for frame processing)
            // **broken ** // setupCallback((3 * s.width * s.height / 2));
            setupCallback(frame_bytesize);
            // mCamera.addCallbackBuffer(new byte[3 * s.width * s.height / 2]);  // create a reusable buffer for the data passed to onPreviewFrame call (in order to avoid GC)
            mCameraPreviewCallback.reset();
            Log.i(TAG, "previewFormats:999");
            // ------ use buffer ------

            List<String> focusModes = this.mCameraParamters.getSupportedFocusModes();
            if (focusModes.contains("continuous-video"))
            {
                this.mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            this.mCamera.setParameters(this.mCameraParamters);
            try
            {
                Log.i(TAG, "camera:startPreview:001");
                this.mCamera.startPreview();
                Log.i(TAG, "camera:startPreview:002");
            }
            catch (Exception pe)
            {
                pe.printStackTrace();
                Log.i(TAG, "camera:startPreview:EE:" + pe.getMessage());
            }

            this.mIsPreviewing = true;
        }
    }

    private void setupCallback(int bufferSize)
    {
        for (int i = 0; i <= CAMPREVIEW_NUM_BUFFERS; ++i)
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

        proccesImageOnBackground(byte[] _data, CameraPreviewCallback _cb)
        {
            data = _data;
            cb = _cb;
            // num = (int) ((Math.random() * 10000f));
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            // Log.i(TAG, "doInBackground:start:#" + num);
            try
            {
                // Log.i(TAG, "Callstate.tox_call_state=" + Callstate.tox_call_state + " my_video_enabled=" + Callstate.my_video_enabled);
                if (Callstate.my_video_enabled == 1)
                {
                    // only send video frame if call has started
                    if (!((Callstate.tox_call_state == 0) || (Callstate.tox_call_state == 1) || (Callstate.tox_call_state == 2)))
                    {
                        if ((PREF__fps_half) && (!use_frame))
                        {
                            use_frame = true;
                        }
                        else
                        {
                            // Log.i(TAG, "onPreviewFrame:sending video:YUV420 data bytes=" + data.length + " rotation=" + CameraWrapper.camera_video_rotate_angle);

                            if (CameraWrapper.camera_video_rotate_angle == 90)
                            {
                                data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);

                                if (mirror_cam_image)
                                {
                                    data_new2 = flipYUV420Horizontal(data_new, camera_preview_size2.height, camera_preview_size2.width);
                                    MainActivity.video_buffer_2.rewind();
                                    MainActivity.video_buffer_2.put(data_new2);
                                    if (PREF__UV_reversed)
                                    {
                                        video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                        if (video_send_res != 0)
                                        {
                                            Log.i(TAG, "video:001:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                        }
                                    }
                                    else
                                    {
                                        MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                        if (video_send_res != 0)
                                        {
                                            Log.i(TAG, "video:002:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                        }

                                    }
                                }
                                else
                                {
                                    MainActivity.video_buffer_2.rewind();
                                    MainActivity.video_buffer_2.put(data_new);
                                    if (PREF__UV_reversed)
                                    {
                                        video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                        if (video_send_res != 0)
                                        {
                                            Log.i(TAG, "video:004:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                        }
                                    }
                                    else
                                    {
                                        MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                    }
                                }
                            }
                            else if (CameraWrapper.camera_video_rotate_angle == 270)
                            {
                                data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);
                                data_new2 = rotateYUV420Degree90(data_new, camera_preview_size2.height, camera_preview_size2.width);
                                data_new = rotateYUV420Degree90(data_new2, camera_preview_size2.width, camera_preview_size2.height);

                                if (mirror_cam_image)
                                {
                                    data_new2 = flipYUV420Horizontal(data_new, camera_preview_size2.height, camera_preview_size2.width);
                                    MainActivity.video_buffer_2.rewind();
                                    MainActivity.video_buffer_2.put(data_new2);
                                    if (PREF__UV_reversed)
                                    {
                                        video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                        if (video_send_res != 0)
                                        {
                                            Log.i(TAG, "video:005:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                        }
                                    }
                                    else
                                    {
                                        MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                    }
                                }
                                else
                                {
                                    MainActivity.video_buffer_2.rewind();
                                    MainActivity.video_buffer_2.put(data_new);
                                    if (PREF__UV_reversed)
                                    {
                                        video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                        if (video_send_res != 0)
                                        {
                                            Log.i(TAG, "video:006:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                        }
                                    }
                                    else
                                    {
                                        MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.height, camera_preview_size2.width);
                                    }
                                }
                            }
                            else if (CameraWrapper.camera_video_rotate_angle == 180)
                            {
                                data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);
                                data_new2 = rotateYUV420Degree90(data_new, camera_preview_size2.height, camera_preview_size2.width);
                                MainActivity.video_buffer_2.rewind();
                                MainActivity.video_buffer_2.put(data_new2);

                                // -------------------------------------------------
                                // android has the order YVU (instead of YUV) !!
                                // so we need to call ..._uv_reversed here
                                // -------------------------------------------------
                                if (PREF__UV_reversed)
                                {
                                    video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.width, camera_preview_size2.height);
                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:007:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.width, camera_preview_size2.height);
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
                                    video_send_res = MainActivity.toxav_video_send_frame_uv_reversed(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.width, camera_preview_size2.height);
                                    if (video_send_res != 0)
                                    {
                                        Log.i(TAG, "video:008:res=" + video_send_res + ":" + ToxVars.TOXAV_ERR_SEND_FRAME.value_str(video_send_res));
                                    }
                                }
                                else
                                {
                                    MainActivity.toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), camera_preview_size2.width, camera_preview_size2.height);
                                }
                            }

                            if (last_video_frame_sent == -1)
                            {
                                last_video_frame_sent = System.currentTimeMillis();
                                count_video_frame_sent++;
                            }
                            else
                            {
                                if ((count_video_frame_sent > 20) || ((last_video_frame_sent + 2000) < System.currentTimeMillis()))
                                {
                                    VIDEO_FRAME_RATE_OUTGOING = (int) ((((float) count_video_frame_sent / ((float) ((System.currentTimeMillis() - last_video_frame_sent) / 1000.0f))) / 1.0f) + 0.5);
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

            return null;
        }


        @Override
        protected void onPostExecute(Void voids)
        {
            // Log.i(TAG, "doInBackground:end:#" + num);

            if (data != null)
            {
                if (mCamera != null)
                {
                    try
                    {
                        mCamera.addCallbackBuffer(data); // return the data buffer for then next onPreviewFrame call (no GC)
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
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
        }

        public void reset()
        {
            if (myProccesImageOnBackground != null)
            {
                myProccesImageOnBackground.cancel(true);
            }
        }

        void close()
        {
            try
            {
                // videoEncoder.close();
            }
            catch (Exception e) // java.lang.IllegalStateException
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera)
        {
            // ----------------------------
            if (data == null)
            {
                Log.i(TAG, "onPreviewFrame:data=null");
            }
            else
            {
                if (camera_preview_size2 == null)
                {
                    try
                    {
                        Camera.Parameters p = camera.getParameters();
                        camera_preview_size2 = p.getPreviewSize();
                        Log.i(TAG, "onPreviewFrame:w=" + camera_preview_size2.width + " h=" + camera_preview_size2.height + " camera_video_rotate_angle=" + CameraWrapper.camera_video_rotate_angle);

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
                        int y_layer_size = (int) camera_preview_size2.width * camera_preview_size2.height;
                        int u_layer_size = (int) (camera_preview_size2.width / 2) * (camera_preview_size2.height / 2);
                        int v_layer_size = (int) (camera_preview_size2.width / 2) * (camera_preview_size2.height / 2);

                        int frame_width_px = (int) camera_preview_size2.width;
                        int frame_height_px = (int) camera_preview_size2.height;

                        int buffer_size_in_bytes2 = y_layer_size + v_layer_size + u_layer_size;

                        Log.i(TAG, "onPreviewFrame:YUV420 frame w1=" + camera_preview_size2.width + " h1=" + camera_preview_size2.height + " bytes=" + buffer_size_in_bytes2);
                        Log.i(TAG, "onPreviewFrame:YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" + buffer_size_in_bytes2);
                        MainActivity.video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2 + 1);
                        MainActivity.set_JNI_video_buffer2(MainActivity.video_buffer_2, camera_preview_size2.width, camera_preview_size2.height);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        camera_preview_size2 = null;
                        Log.i(TAG, "onPreviewFrame:EE1:" + e2.getMessage());
                    }
                }
                myProccesImageOnBackground = (proccesImageOnBackground) new proccesImageOnBackground(data, this).execute();
            }
        }
    }

    private byte[] flipYUV420Horizontal(byte[] data, int imageWidth, int imageHeight)
    {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        // flip the Y plane
        int i = 0; // start of Y plane
        for (int y = 0; y < imageHeight; y++)
        {
            for (int x = imageWidth - 1; x >= 0; x--)
            {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }

        // flip the U+V plane at the same time
        int w = (imageWidth / 2);
        int h = (imageHeight / 2);
        i = (imageWidth * imageHeight); // start of U plane
        int i_2 = (imageWidth * imageHeight) + (w * h); // start of V plane
        int offset = i;
        int offset_2 = i_2;
        for (int y = 0; y < h; y++)
        {
            for (int x = w - 1; x >= 0; x--)
            {
                yuv[i] = data[offset + (y * w + x)];
                i++;
                yuv[i_2] = data[offset_2 + (y * w + x)];
                i_2++;
            }
        }

        return yuv;
    }

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0; // start of Y plane
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }

        //        // Rotate the U and V color components (interleaved)
        //        i = imageWidth * imageHeight * 3 / 2 - 1;
        //        for (int x = imageWidth - 1; x > 0; x = x - 2)
        //        {
        //            for (int y = 0; y < imageHeight / 2; y++)
        //            {
        //                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
        //                i--;
        //                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
        //                i--;
        //            }
        //        }

        // Rotate the U+V plane at the same time
        int w = (imageWidth / 2);
        int h = (imageHeight / 2);
        i = (imageWidth * imageHeight); // start of U plane
        int i_2 = (imageWidth * imageHeight) + (w * h); // start of V plane
        int offset = i;
        int offset_2 = i_2;
        for (int x = 0; x < w; x++)
        {
            for (int y = h - 1; y >= 0; y--)
            {
                yuv[i] = data[offset + (y * w + x)];
                i++;
                yuv[i_2] = data[offset_2 + (y * w + x)];
                i_2++;
            }
        }

        return yuv;
    }


}
