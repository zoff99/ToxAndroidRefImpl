package com.zoffcc.applications.trifa;


import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.set_JNI_video_buffer2;
import static com.zoffcc.applications.trifa.MainActivity.toxav_video_send_frame;
import static com.zoffcc.applications.trifa.MainActivity.video_buffer_2;

public class CameraPreviewSurfaceview extends SurfaceView implements Camera.PreviewCallback
{
    public static final String TAG = "PreviewSurfaceview";
    long frameNumber = 0;
    static Camera.Size camera_preview_size_ = null;

    public CameraPreviewSurfaceview(Context context)
    {
        super(context);
    }

    // !!this one is actually used!!
    public CameraPreviewSurfaceview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    // !!this one is actually used!!

    @Override
    public void onPreviewFrame(byte[] data, Camera camera2)
    {
        if (data == null)
        {
        }
        else
        {
            if (camera_preview_size_ == null)
            {
                Camera.Parameters p = camera2.getParameters();
                camera_preview_size_ = p.getPreviewSize();
                Log.i(TAG, "onPreviewFrame:w=" + camera_preview_size_.width + " h=" + camera_preview_size_.height);

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
                int y_layer_size = (int) camera_preview_size_.width * camera_preview_size_.height;
                int u_layer_size = (int) (camera_preview_size_.width / 2) * (camera_preview_size_.height / 2);
                int v_layer_size = (int) (camera_preview_size_.width / 2) * (camera_preview_size_.height / 2);

                int frame_width_px = (int) camera_preview_size_.width;
                int frame_height_px = (int) camera_preview_size_.height;

                int buffer_size_in_bytes2 = y_layer_size + v_layer_size + u_layer_size;

                Log.i(TAG, "YUV420 frame w1=" + camera_preview_size_.width + " h1=" + camera_preview_size_.height + " bytes=" + buffer_size_in_bytes2);
                Log.i(TAG, "YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" + buffer_size_in_bytes2);
                video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2 + 1);
                set_JNI_video_buffer2(video_buffer_2, camera_preview_size_.width, camera_preview_size_.height);
            }

            try
            {
                video_buffer_2.rewind();
                Camera.Parameters p = camera2.getParameters();
                camera_preview_size_ = p.getPreviewSize();
                Log.i(TAG, "onPreviewFrame:w=" + camera_preview_size_.width + " h=" + camera_preview_size_.height);
                Log.i(TAG, "YUV420 data bytes=" + data.length);

                video_buffer_2.put(data);
                toxav_video_send_frame(Callstate.friend_number, camera_preview_size_.width, camera_preview_size_.height);
            }
            catch (java.nio.BufferOverflowException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // camera.addCallbackBuffer(data);
        }
    }
}
