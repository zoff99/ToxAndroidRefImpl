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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import static com.zoffcc.applications.trifa.CameraWrapper.NV21rotate90;
import static com.zoffcc.applications.trifa.CameraWrapper.YUV_420_888toNV21_x;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;

public class FrameAnalyser implements ImageAnalysis.Analyzer
{
    private static final String TAG = "trifa.FrameAnalyser";

    private final CameraDrawingOverlay drawingOverlay;
    private SelfieSegmenterOptions options;
    private Segmenter segmenter;
    private Image frameMediaImage = null;

    FrameAnalyser(CameraDrawingOverlay drawingOverlay)
    {
        this.drawingOverlay = drawingOverlay;
        options = new SelfieSegmenterOptions.
                Builder().
                setDetectorMode(SelfieSegmenterOptions.STREAM_MODE).
                build();
        segmenter = Segmentation.getClient(options);
    }

    @SuppressLint("UnsafeOptInUsageError")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void analyze(@NonNull final ImageProxy image)
    {
        // Log.i(TAG, "CCCCCCCCCCCCCCCCC");
        frameMediaImage = image.getImage();
        if (frameMediaImage != null)
        {
            InputImage inputImage = InputImage.
                    fromMediaImage(frameMediaImage, image.getImageInfo().getRotationDegrees());

            Task<SegmentationMask> result = segmenter.process(inputImage).addOnSuccessListener(
                    new OnSuccessListener<SegmentationMask>()
                    {
                        @Override
                        public void onSuccess(SegmentationMask mask)
                        {
                            ByteBuffer buf = mask.getBuffer();
                            int mwidth = mask.getWidth();
                            int mheight = mask.getHeight();
                            // Log.i(TAG, "XXXXXXX:" + mwidth + " " + mheight);
                            buf.rewind();
                            Bitmap bitmap = Bitmap.createBitmap(mwidth, mheight, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buf);
                            drawingOverlay.maskBitmap = bitmap;
                            drawingOverlay.invalidate();

                            try
                            {
                                if (!Callstate.audio_call)
                                {
                                    buf.rewind();
                                    byte[] buf2 = new byte[((640 * 480 * 3) / 2)];
                                    byte[] buf3 = new byte[((640 * 480 * 3) / 2)];

                                    // Log.i(TAG, "format:" + image.getPlanes().length + " " + image.getFormat() + " " +
                                    //           image.getImageInfo() + " " + image.getPlanes()[1].getPixelStride() +
                                    //           " " + image.getPlanes()[1].getRowStride());
                                    ByteBuffer y_ = image.getPlanes()[0].getBuffer();
                                    ByteBuffer u_ = image.getPlanes()[1].getBuffer();
                                    ByteBuffer v_ = image.getPlanes()[2].getBuffer();

                                    y_.rewind();
                                    u_.rewind();
                                    v_.rewind();

                                    //Log.i(TAG, "format:0:" +image.getPlanes()[0].getBuffer().limit()+" "+image.getPlanes()[0].getRowStride()+" "+image.getPlanes()[0].getPixelStride());
                                    //Log.i(TAG, "format:1:" +image.getPlanes()[1].getBuffer().limit()+" "+image.getPlanes()[1].getRowStride()+" "+image.getPlanes()[1].getPixelStride());
                                    //Log.i(TAG, "format:2:" +image.getPlanes()[2].getBuffer().limit()+" "+image.getPlanes()[2].getRowStride()+" "+image.getPlanes()[2].getPixelStride());

                                    y_.rewind();
                                    u_.rewind();
                                    v_.rewind();

                                    y_.get(buf2, 0, 640 * 480);
                                    if ((image.getPlanes()[1].getPixelStride() > 1) ||
                                        (image.getPlanes()[2].getPixelStride() > 1))
                                    {
                                        int off_u = 640 * 480;
                                        int off_v = (640 * 480) + (640 * 480) / 4;
                                        for (int k = 0; k < ((640 * 480) / 4); k++)
                                        {
                                            buf2[off_u + k] = u_.get(k * image.getPlanes()[1].getPixelStride());
                                            buf2[off_v + k] = v_.get(k * image.getPlanes()[2].getPixelStride());
                                        }
                                    }
                                    else
                                    {
                                        u_.get(buf2, 640 * 480, (640 * 480) / 4);
                                        v_.get(buf2, ((640 * 480) + (640 * 480) / 4), (640 * 480) / 4);
                                    }

                                    int y_size = 640 * 480;
                                    int u_v_size = (640 * 480) / 4;

                                    float foregroundConfidence;
                                    int y_pos;
                                    int u_pos;
                                    int v_pos;
                                    int x1;
                                    int y1;
                                    for (int y = 0; y < mheight; y++)
                                    {
                                        for (int x = 0; x < mwidth; x++)
                                        {
                                            // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                            // 1.0 being foreground
                                            // 0.0 background
                                            // use values greater than the threshold value 0.9 (90% Confidence)
                                            foregroundConfidence = buf.getFloat();
                                            // Log.i(TAG, "x=" + x + " y=" + y + " float=" + foregroundConfidence);
                                            if (foregroundConfidence < 0.9)
                                            {
                                                y1 = mwidth - x;
                                                x1 = y;
                                                y_pos = (y1 * 640) + x1;
                                                u_pos = y_size + (y1 * 640 / 2) + (x1 / 2);
                                                v_pos = y_size + u_v_size + (y1 * 640 / 2) + (x1 / 2);

                                                // buf2[y_pos] = 0;
                                                // buf2[u_pos] = (byte) 128;
                                                // buf2[v_pos] = (byte) 128;
                                            }
                                        }
                                    }

                                    if (MainActivity.video_buffer_2 == null)
                                    {
                                        MainActivity.video_buffer_2 = ByteBuffer.allocateDirect(
                                                ((640 * 480 * 3) / 2) + 1000);
                                        MainActivity.set_JNI_video_buffer2(MainActivity.video_buffer_2, 480, 640);
                                    }

                                    if (buf3 == null)
                                    {
                                        buf3 = new byte[buf2.length];
                                    }
                                    else if (buf3.length < buf2.length)
                                    {
                                        buf3 = new byte[buf2.length];
                                    }

                                    // buf3 = NV21rotate90(YUV_420_888toNV21_x(buf2, 640, 480), buf3, 640, 480);
                                    // buf3 = NV21rotate90(buf2, buf3, 640, 480);

                                    MainActivity.video_buffer_2.rewind();
                                    // MainActivity.video_buffer_2.put(buf3);
                                    MainActivity.video_buffer_2.put(YUV_420_888toNV21_x(buf2, 640, 480));

                                    int res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(buf2,
                                                                                                       tox_friend_by_public_key__wrapper(
                                                                                                               Callstate.friend_pubkey),
                                                                                                       640, 480,
                                                                                                       System.currentTimeMillis());
                                    // Log.i(TAG, "XXXX:res:" + res);
                                }
                            }
                            catch (Exception ea)
                            {
                                ea.printStackTrace();
                                Log.i(TAG, "XXXX:EE:" + ea.getMessage());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                }
            }).addOnCompleteListener(new OnCompleteListener<SegmentationMask>()
            {
                @Override
                public void onComplete(@NonNull Task<SegmentationMask> task)
                {
                    image.close();
                }
            });
        }
        // image.close();
    }
}
