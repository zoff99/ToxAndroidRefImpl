/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

public class CameraDrawingOverlay extends SurfaceView
{
    private static final String TAG = "trifa.CamDOverlay";

    Bitmap maskBitmap = null;
    boolean flipimage = false;
    Rect r = new Rect(0, 0, (int) (480 * 0.75), (int) (640 * 0.75));

    public CameraDrawingOverlay(Context context)
    {
        super(context);
    }

    public CameraDrawingOverlay(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CameraDrawingOverlay(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraDrawingOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Bitmap flipBitmap(Bitmap source)
    {
        // Log.i(TAG, "flipbitmap:camerax_res:" + source.getWidth() + " " + source.getHeight());

        if (flipimage)
        {
            Matrix matrix = new Matrix();
            matrix.postScale(-1f, 1f, source.getWidth() / 2f, source.getHeight() / 2f);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }
        else
        {
            return source;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (maskBitmap != null)
        {
            if (canvas != null)
            {
                canvas.drawBitmap(flipBitmap(maskBitmap), null, r, null);
            }
        }
    }
}
