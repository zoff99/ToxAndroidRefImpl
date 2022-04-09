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
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type.Builder;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class YuvToRgbConverterJ
{
    private static final String TAG = "trifa.YuvToRgbConvJ";

    private final RenderScript rs;
    private final ScriptIntrinsicYuvToRGB scriptYuvToRgb;
    private ByteBuffer yuvBits;
    private byte[] bytes;
    private Allocation inputAllocation;
    private Allocation outputAllocation;

    public final synchronized void yuvToRgb(@NotNull Image image, @NotNull Bitmap output)
    {
        YuvByteBuffer yuvBuffer = new YuvByteBuffer(image, this.yuvBits);
        this.yuvBits = yuvBuffer.getBuffer();
        if (this.needCreateAllocations(image, yuvBuffer))
        {
            Builder yuvType = (new Builder(this.rs, Element.U8(this.rs))).setX(image.getWidth()).setY(
                    image.getHeight()).setYuvFormat(yuvBuffer.getType());
            this.inputAllocation = Allocation.createTyped(this.rs, yuvType.create(), 1);
            this.bytes = new byte[yuvBuffer.getBuffer().capacity()];
            Builder rgbaType = (new Builder(this.rs, Element.RGBA_8888(this.rs))).setX(image.getWidth()).setY(
                    image.getHeight());
            this.outputAllocation = Allocation.createTyped(this.rs, rgbaType.create(), 1);
        }

        yuvBuffer.getBuffer().get(this.bytes);
        this.inputAllocation.copyFrom(this.bytes);
        this.scriptYuvToRgb.setInput(this.inputAllocation);
        this.scriptYuvToRgb.forEach(this.outputAllocation);
        this.outputAllocation.copyTo(output);
    }

    private final boolean needCreateAllocations(Image image, YuvByteBuffer yuvBuffer)
    {
        if (this.inputAllocation != null)
        {
            if (this.inputAllocation.getType().getX() == image.getWidth())
            {
                if (this.inputAllocation.getType().getY() == image.getHeight())
                {
                    if ((this.inputAllocation.getType().getYuv() == yuvBuffer.getType()) &&
                        (this.bytes.length == yuvBuffer.getBuffer().capacity()))
                    {
                        // Log.i(TAG, "needCreateAllocations:false");
                        return false;
                    }
                }
            }
        }

        // Log.i(TAG, "needCreateAllocations:**TRUE**");
        return true;
    }

    public YuvToRgbConverterJ(@NotNull Context context)
    {
        this.rs = RenderScript.create(context);
        this.scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(this.rs, Element.U8_4(this.rs));
        this.bytes = new byte[0];
    }
}
