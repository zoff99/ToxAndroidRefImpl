package com.zoffcc.applications.trifa;


/**
 * @author Jose Davis Nidhin
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

class Preview extends ViewGroup implements SurfaceHolder.Callback
{
    private final String TAG = "trifa.PreviewActivity";

    SurfaceView mSurfaceView = null;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;
    boolean first_called = true;

    public Preview(Context context, AttributeSet attrs)
    {
        // !!this one is actually used!!
        super(context, attrs);
        Log.i(TAG, "init:001:a");
        // !!this one is actually used!!
    }

    public Preview(Context context)
    {
        super(context);
        Log.i(TAG, "init:002");
    }

    public Preview(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "init:003");
    }

    //    @SuppressWarnings("deprecation")
    //    public Preview(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    //    {
    //        super(context, attrs, defStyleAttr, defStyleRes);
    //    }

    public void setCamera(Camera camera)
    {
        Log.i(TAG, "setCamera:camera=" + camera);

        if (first_called)
        {
            first_called = false;
            mSurfaceView = CallingActivity.camera_preview_surface_view;
            mHolder = mSurfaceView.getHolder();
            Log.i(TAG, "setPreviewDisplay:mSurfaceView=" + mSurfaceView);
            Log.i(TAG, "setPreviewDisplay:1 holder.a=" + mHolder);
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        mCamera = camera;
        if (mCamera != null)
        {
            Log.i(TAG, "setCamera:001");
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
            Log.i(TAG, "setCamera:002");

            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();
            Log.i(TAG, "setCamera:003");

            List<String> focusModes = params.getSupportedFocusModes();
            Log.i(TAG, "setCamera:004");

            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            {
                Log.i(TAG, "setCamera:005");

                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
                mCamera.setParameters(params);

                Log.i(TAG, "setCamera:006");
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Log.i(TAG, "onMeasure");

        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null)
        {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        Log.i(TAG, "onLayout");

        if (changed && getChildCount() > 0)
        {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null)
            {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth)
            {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            }
            else
            {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        Log.i(TAG, "surfaceCreated");

        try
        {
            if (mCamera != null)
            {
                Log.i(TAG, "setPreviewDisplay:1 holder=" + holder);
                mCamera.setPreviewDisplay(holder);
                Log.i(TAG, "setPreviewDisplay:2");
            }
        }
        catch (IOException exception)
        {
            Log.i(TAG, "setPreviewDisplay:EE:1");
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "setPreviewDisplay:EE:2");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null)
        {
            Log.i(TAG, "surfaceDestroyed");
            mCamera.stopPreview();
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
        {
            return null;
        }

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes)
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

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes)
            {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        Log.i(TAG, "getOptimalPreviewSize:w=" + optimalSize.width + " h=" + optimalSize.height);

        // -------- DEBUG --------
        // -------- DEBUG --------
        // -------- DEBUG --------
        // optimalSize.width = 120;
        // optimalSize.height = 120;
        // -------- DEBUG --------
        // -------- DEBUG --------
        // -------- DEBUG --------

        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        if (mCamera != null)
        {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            Log.i(TAG, "surfaceChanged:w=" + w + " h=" + h);

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }

}
