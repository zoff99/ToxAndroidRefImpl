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
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback
{

    public static final String TAG = "CameraSurfacePreview";
    SurfaceHolder mSurfaceHolder;
    Context mContext;
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

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.i(TAG, "surfaceCreated...");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.i(TAG, "surfaceChanged...");
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
