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
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

import static com.zoffcc.applications.trifa.CallingActivity.toggle_cam_preview;
import static com.zoffcc.applications.trifa.CallingActivity.toggle_osd_views;

public class CameraDrawingOverlay extends SurfaceView
{
    private static final String TAG = "trifa.CamDOverlay";

    Bitmap maskBitmap = null;
    boolean flipimage = false;
    Rect r = new Rect(0, 0, (int) (480 * 0.75), (int) (640 * 0.75));
    private float my_alpha = 1.0f;

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

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_CANCEL))
        {
            if (my_alpha == 1.0f)
            {
                // make view INVISIBLE (totally transparent)
                my_alpha = 0.0f;
                this.setAlpha(0.0f);
                toggle_cam_preview(false, true);
                toggle_osd_views(false);
            }
            else
            {
                // make view visible
                my_alpha = 1.0f;
                this.setAlpha(1.0f);
                toggle_cam_preview(true, true);
                toggle_osd_views(true);
            }
            return true;
        }
        else
        {
            return false;
        }
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
                if (MainActivity.IS_GPLAY_VERSION)
                {
                    canvas.drawBitmap(flipBitmap(maskBitmap), null, r, null);
                }
                else
                {
                    // canvas.drawBitmap(RotateBitmap(maskBitmap, 0), null, r, null);
                    canvas.drawBitmap(maskBitmap, null, r, null);
                }
            }
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
