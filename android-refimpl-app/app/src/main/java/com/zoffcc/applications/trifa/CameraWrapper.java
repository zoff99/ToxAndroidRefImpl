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
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.set_JNI_video_buffer2;
import static com.zoffcc.applications.trifa.MainActivity.toxav_video_send_frame_uv_reversed;
import static com.zoffcc.applications.trifa.MainActivity.video_buffer_2;

public class CameraWrapper
{
    private static final String TAG = "CameraWrapper";
    static  Camera mCamera;
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
    private CameraPreviewCallback mCameraPreviewCallback;
    private byte[] mImageCallbackBuffer = new byte[(CameraWrapper.IMAGE_WIDTH * CameraWrapper.IMAGE_HEIGHT) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2)) + ((CameraWrapper.IMAGE_WIDTH / 2) * (CameraWrapper.IMAGE_HEIGHT / 2))];
    static Camera.Size camera_preview_size2 = null;

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
                mCamera = Camera.open(i);
                break;
            }
        }
        if (mCamera == null)
        {
            Log.d(TAG, "this camera (" + front_camera + ") type found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
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
            this.mCamera.stopPreview();
            return;
        }

        try
        {
            this.mCamera.setPreviewDisplay(holder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
            this.mCamera.release();
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

            this.mCameraParamters = this.mCamera.getParameters();
            this.mCameraParamters.setPreviewFormat(ImageFormat.YV12); // order here is Y-V-U !!
            this.mCameraParamters.setFlashMode("off");
            this.mCameraParamters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            this.mCameraParamters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            Log.i(TAG, "preview size before=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);
            this.mCameraParamters.setPreviewSize(IMAGE_WIDTH, IMAGE_HEIGHT);
            Log.i(TAG, "preview size after 1=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);
            this.mCamera.setDisplayOrientation(90); // always 90 ??
            Log.i(TAG, "preview size after 2=" + this.mCameraParamters.getPreviewSize().width + "," + this.mCameraParamters.getPreviewSize().height);
            mCameraPreviewCallback = new CameraPreviewCallback();
            mCamera.addCallbackBuffer(mImageCallbackBuffer);
            mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);
            List<String> focusModes = this.mCameraParamters.getSupportedFocusModes();
            if (focusModes.contains("continuous-video"))
            {
                this.mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            this.mCamera.setParameters(this.mCameraParamters);
            this.mCamera.startPreview();

            this.mIsPreviewing = true;
        }
    }

    class CameraPreviewCallback implements Camera.PreviewCallback
    {
        private static final String TAG = "CameraPreviewCallback";
        private VideoEncoderFromBuffer videoEncoder = null;

        private CameraPreviewCallback()
        {
            videoEncoder = new VideoEncoderFromBuffer(CameraWrapper.IMAGE_WIDTH, CameraWrapper.IMAGE_HEIGHT);
        }

        void close()
        {
            try
            {
                videoEncoder.close();
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
            }
            else
            {
                if (camera_preview_size2 == null)
                {
                    Camera.Parameters p = camera.getParameters();
                    camera_preview_size2 = p.getPreviewSize();
                    Log.i(TAG, "onPreviewFrame:w=" + camera_preview_size2.width + " h=" + camera_preview_size2.height);

                    if (video_buffer_2 != null)
                    {
                        // video_buffer_2.clear();
                        video_buffer_2 = null;
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

                    Log.i(TAG, "YUV420 frame w1=" + camera_preview_size2.width + " h1=" + camera_preview_size2.height + " bytes=" + buffer_size_in_bytes2);
                    Log.i(TAG, "YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" + buffer_size_in_bytes2);
                    video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2 + 1);
                    set_JNI_video_buffer2(video_buffer_2, camera_preview_size2.width, camera_preview_size2.height);
                }

                try
                {
                    // Log.i(TAG, "YUV420 data bytes=" + data.length);

                    if (CameraWrapper.camera_video_rotate_angle == 90)
                    {
                        data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);
                        video_buffer_2.rewind();
                        video_buffer_2.put(data_new);

                        // -------------------------------------------------
                        // android has the order YVU (instead of YUV) !!
                        // so we need to call ..._uv_reversed here
                        // -------------------------------------------------
                        toxav_video_send_frame_uv_reversed(Callstate.friend_number, camera_preview_size2.height, camera_preview_size2.width);
                        camera.addCallbackBuffer(data_new);
                    }
                    else if (CameraWrapper.camera_video_rotate_angle == 270)
                    {
                        data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);
                        data_new2 = rotateYUV420Degree90(data_new, camera_preview_size2.height, camera_preview_size2.width);
                        data_new = rotateYUV420Degree90(data_new2, camera_preview_size2.width, camera_preview_size2.height);
                        video_buffer_2.rewind();
                        video_buffer_2.put(data_new);

                        // -------------------------------------------------
                        // android has the order YVU (instead of YUV) !!
                        // so we need to call ..._uv_reversed here
                        // -------------------------------------------------
                        toxav_video_send_frame_uv_reversed(Callstate.friend_number, camera_preview_size2.height, camera_preview_size2.width);
                        camera.addCallbackBuffer(data_new);
                    }
                    else if (CameraWrapper.camera_video_rotate_angle == 180)
                    {
                        data_new = rotateYUV420Degree90(data, camera_preview_size2.width, camera_preview_size2.height);
                        data_new2 = rotateYUV420Degree90(data_new, camera_preview_size2.height, camera_preview_size2.width);
                        video_buffer_2.rewind();
                        video_buffer_2.put(data_new2);

                        // -------------------------------------------------
                        // android has the order YVU (instead of YUV) !!
                        // so we need to call ..._uv_reversed here
                        // -------------------------------------------------
                        toxav_video_send_frame_uv_reversed(Callstate.friend_number, camera_preview_size2.width, camera_preview_size2.height);
                        camera.addCallbackBuffer(data_new2);
                    }
                    else
                    {
                        video_buffer_2.rewind();
                        video_buffer_2.put(data);

                        // -------------------------------------------------
                        // android has the order YVU (instead of YUV) !!
                        // so we need to call ..._uv_reversed here
                        // -------------------------------------------------
                        toxav_video_send_frame_uv_reversed(Callstate.friend_number, camera_preview_size2.width, camera_preview_size2.height);
                        camera.addCallbackBuffer(data);
                    }
                }
                catch (java.nio.BufferOverflowException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2)
        {
            for (int y = 0; y < imageHeight / 2; y++)
            {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }


}
