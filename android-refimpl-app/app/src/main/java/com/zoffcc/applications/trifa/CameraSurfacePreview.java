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

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback
{

    public static final String TAG = "trifa.CameraSurfacePrv";
    SurfaceHolder mSurfaceHolder;
    Context mContext;
    static List<Camera.Size> mSupportedPreviewSizes = null;
    static Camera.Size mPreviewSize = null;

    CameraWrapper mCameraWrapper;

    @SuppressWarnings("deprecation")
    public CameraSurfacePreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mSurfaceHolder = getHolder();
        this.mContext = getContext();
        this.mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.mSurfaceHolder.addCallback(this);
    }

    private Camera.Size getOptimalPreviewSize_2(List<Camera.Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
        {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
            {
                continue;
            }

            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes)
            {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private Camera.Size getOptimalPreviewSize_1(List<Camera.Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
        {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
            {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes)
            {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    public static int convertDpToPixels(float dp, Context context)
    {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static int convertSpToPixels(float sp, Context context)
    {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Log.i(TAG, "onMeasure");
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        // setMeasuredDimension(width, height);
        width = convertDpToPixels(120, getContext());
        height = convertDpToPixels(120, getContext());


        if (mSupportedPreviewSizes != null)
        {
            // mPreviewSize = getOptimalPreviewSize_1(mSupportedPreviewSizes, width, height);
            // Log.i(TAG, "mOptimalPreviewSize(1)=" + mPreviewSize.width + "," + mPreviewSize.height);
            mPreviewSize = getOptimalPreviewSize_2(mSupportedPreviewSizes, width, height);
            Log.i(TAG, "mOptimalPreviewSize(2)=" + mPreviewSize.width + "," + mPreviewSize.height);
        }

        if (mPreviewSize != null)
        {
            float ratio;
            if (mPreviewSize.height >= mPreviewSize.width)
            {
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            }
            else
            {
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;
            }

            Log.i(TAG, "raio=" + ratio + " w=" + width + " h=" + height + " wmin=" + widthMeasureSpec + " hmin=" + heightMeasureSpec);

            // One of these methods should be used, second method squishes preview slightly
            setMeasuredDimension(width, (int) (width * ratio));
            //  setMeasuredDimension((int) (width * ratio), height);
        }
        else
        {
            setMeasuredDimension(width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.i(TAG, "surfaceCreated...");

        try
        {
            Log.i(TAG, "surfaceCreated:re init camera:START");
            // CallingActivity.reinit_camera(CallingActivity.ca);
            Log.i(TAG, "surfaceCreated:re init camera:READY");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "surfaceCreated:EE1:" + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.i(TAG, "surfaceChanged...");
//        try
//        {
//            CameraWrapper.mCamera.startPreview();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Log.i(TAG, "surfaceChanged:EE1:" + e.getMessage());
//        }
//
//        try
//        {
//            CameraWrapper.mCamera.startPreview();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Log.i(TAG, "surfaceChanged:EE2:" + e.getMessage());
//        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.i(TAG, "surfaceDestroyed...");
        CameraWrapper.getInstance().doStopCamera();
    }

    public SurfaceHolder getSurfaceHolder()
    {
        return this.mSurfaceHolder;
    }
}
