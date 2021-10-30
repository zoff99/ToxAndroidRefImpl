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
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import kotlin.Triple;

import static com.zoffcc.applications.trifa.CallingActivity.FRONT_CAMERA_USED;
import static com.zoffcc.applications.trifa.CallingActivity.active_camera_type;
import static com.zoffcc.applications.trifa.CallingActivity.loadModelFile;
import static com.zoffcc.applications.trifa.CameraWrapper.YUV420flipHorizontal;
import static com.zoffcc.applications.trifa.CameraWrapper.YUV420rotate90;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.update_fps;
import static com.zoffcc.applications.trifa.MainActivity.PREF__UV_reversed;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CAM_REMOVE_BACKGROUND_CONFIDENCE_THRESHOLD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE;

public class VideoFrameAnalyserTFLite implements ImageAnalysis.Analyzer
{
    private static final String TAG = "trifa.FrameAnalyserTFL";

    static org.tensorflow.lite.Interpreter interpreter = null;
    private final CameraDrawingOverlay drawingOverlay;
    private Image frameMediaImage = null;
    private Activity a;
    private Context c;
    private final int imageSize = 256;
    private final int NUM_CLASSES = 21;
    private final float IMAGE_MEAN = 0f; // 127.5f;
    private final float IMAGE_STD = 127.5f;
    private YuvToRgbConverter yuvToRgbConverter;
    private Bitmap camera_video_frame_bitmap = null;
    private ByteBuffer segmentationMasks = null;
    private Bitmap bitmap_for_segmentationmask = null;
    private byte[] buf2 = null;
    private byte[] buf3 = null;

    VideoFrameAnalyserTFLite(CameraDrawingOverlay drawingOverlay, Context c, Activity a)
    {
        this.drawingOverlay = drawingOverlay;
        this.a = a;
        this.c = c;

        yuvToRgbConverter = new YuvToRgbConverter(this.c);

        camera_video_frame_bitmap = null;
        segmentationMasks = ByteBuffer.allocateDirect(imageSize * imageSize * 4);
        segmentationMasks.order(ByteOrder.nativeOrder());
        bitmap_for_segmentationmask = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        buf2 = new byte[((640 * 480) * 3 / 2)];
        buf3 = new byte[((640 * 480) * 3 / 2)];

        Interpreter.Options options = new Interpreter.Options();

        CompatibilityList compatList = new CompatibilityList();
        if (compatList.isDelegateSupportedOnThisDevice())
        {
            // if the device has a supported GPU, add the GPU delegate
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
            Log.i(TAG, "tflite:use gpu");
        }
        else
        {
            // if the GPU is not supported, run on 4 threads
            options.setNumThreads(4);
            Log.i(TAG, "tflite:use CPU with 4 threads");
        }

        ByteBuffer tfliteModel = null;
        try
        {
            tfliteModel = loadModelFile(a);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "Interpreter:start:001");
        // HINT: this is very slow and can take a few seconds
        // https://github.com/tensorflow/tensorflow/issues/44612
        // so initialze only once
        if (interpreter == null)
        {
            interpreter = new Interpreter(tfliteModel, options);
        }
        Log.i(TAG, "Interpreter:ready:002");
    }

    int getRandomRGBInt(Random random)
    {
        return (int) ((255 * random.nextFloat()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy image)
    {
        frameMediaImage = image.getImage();
        if (frameMediaImage != null)
        {
            try
            {
                if (!Callstate.audio_call)
                {
                    final long capture_ts = System.currentTimeMillis();

                    // long preprocessTime = SystemClock.uptimeMillis();
                    // long overallTime = preprocessTime;

                    if (camera_video_frame_bitmap == null)
                    {
                        camera_video_frame_bitmap = Bitmap.createBitmap(frameMediaImage.getWidth(),
                                                                        frameMediaImage.getHeight(),
                                                                        Bitmap.Config.ARGB_8888);
                    }
                    else if ((camera_video_frame_bitmap.getWidth() != frameMediaImage.getWidth()) ||
                             (camera_video_frame_bitmap.getHeight() != frameMediaImage.getHeight()))
                    {
                        camera_video_frame_bitmap = Bitmap.createBitmap(frameMediaImage.getWidth(),
                                                                        frameMediaImage.getHeight(),
                                                                        Bitmap.Config.ARGB_8888);

                    }

                    yuvToRgbConverter.yuvToRgb(frameMediaImage, camera_video_frame_bitmap);

                    // Log.i(TAG, "bbbb:1:" + bmp.getWidth() + " " + bmp.getHeight() + " " + imageSize + " rot=" +
                    //           image.getImageInfo().getRotationDegrees());

                    int rotate_input = image.getImageInfo().getRotationDegrees();
                    Bitmap scaledBitmap = scaleBitmapAndKeepRatio(camera_video_frame_bitmap, imageSize, imageSize, true,
                                                                  rotate_input, active_camera_type);
                    // Log.i(TAG, "bbbb:2:" + scaledBitmap.getWidth() + " " + scaledBitmap.getHeight() + " " + imageSize);
                    ByteBuffer contentArray = bitmapToByteBuffer(scaledBitmap, imageSize, imageSize, IMAGE_MEAN,
                                                                 IMAGE_STD);


                    // preprocessTime = SystemClock.uptimeMillis() - preprocessTime;
                    // Log.i(TAG, "tensor_in_out:" + contentArray.limit() + " " + segmentationMasks.limit());
                    /*
                     *
                     *  Run the TFLite model here --------------------------------
                     *
                     */
                    /*
                        The general model operates on a 256x256x3 (HWC) tensor,
                        and outputs a 256x256x1 tensor representing the segmentation mask
                     */
                    // long imageSegmentationTime = SystemClock.uptimeMillis();
                    contentArray.rewind();
                    segmentationMasks.rewind();
                    interpreter.run(contentArray, segmentationMasks);
                    // imageSegmentationTime = SystemClock.uptimeMillis() - imageSegmentationTime;
                    /*
                     *
                     *  Run the TFLite model here --------------------------------
                     *
                     */

                    // long postrocessTime = SystemClock.uptimeMillis();
                    segmentationMasks.rewind();
                    //Log.i(TAG,
                    //      "tensor_res_bitmaps:" + bitmap.getAllocationByteCount() + " " + bitmap.getByteCount() + " " +
                    //      bitmap.getRowBytes());
                    bitmap_for_segmentationmask.copyPixelsFromBuffer(segmentationMasks);
                    drawingOverlay.maskBitmap = bitmap_for_segmentationmask;
                    drawingOverlay.invalidate();
                    // postrocessTime = SystemClock.uptimeMillis() - postrocessTime;


                    // only send video frame if call has started
                    if (!((Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_NONE.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_ERROR.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_FINISHED.value)))
                    {

                        ByteBuffer buf = segmentationMasks;
                        buf.rewind();

                        // HINT: format "35" --> android.graphics.ImageFormat.YUV_420_888
                        //Log.i(TAG, "format:" + image.getPlanes().length + " " + image.getFormat() + " " +
                        //           image.getPlanes()[1].getPixelStride() + " " + image.getPlanes()[1].getRowStride());

                        // image -->
                        // Y = w:640 h:480 bytes=640*480
                        ByteBuffer y_ = image.getPlanes()[0].getBuffer();
                        // Y = w:320 h:240 bytes=320*2*240 (pixelstride == 2)
                        ByteBuffer u_ = image.getPlanes()[1].getBuffer();
                        // Y = w:320 h:240 bytes=320*2*240 (pixelstride == 2)
                        ByteBuffer v_ = image.getPlanes()[2].getBuffer();
                        y_.rewind();
                        u_.rewind();
                        v_.rewind();

                        y_.get(buf2, 0, 640 * 480);
                        final int off_u = 640 * 480;
                        final int off_v = (640 * 480) + (640 * 480) / 4;
                        if ((image.getPlanes()[1].getPixelStride() > 1) || (image.getPlanes()[2].getPixelStride() > 1))
                        {
                            final int pixelstride_1 = image.getPlanes()[1].getPixelStride();
                            final int pixelstride_2 = image.getPlanes()[2].getPixelStride();
                            for (int k = 0; k < ((640 * 480) / 4); k++)
                            {
                                buf2[off_u + k] = v_.get(k * pixelstride_1);
                                buf2[off_v + k] = u_.get(k * pixelstride_2);
                            }
                        }
                        else // pixelstrides for u and v both are "1"
                        {
                            v_.get(buf2, off_u, (640 * 480) / 4);
                            u_.get(buf2, off_v, (640 * 480) / 4);
                        }

                        // ---------------------------------------------------------------
                        // TODO: haxx0r, make better and actually check all the angles
                        //       and rotate always correctly
                        if (active_camera_type == FRONT_CAMERA_USED)
                        {
                            buf3 = YUV420rotate90(buf2, buf3, 640, 480);
                            buf2 = YUV420rotate90(buf3, buf2, 480, 640);
                        }
                        // ---------------------------------------------------------------

                        // ---------------------------------------------------------------
                        // make the final rotation here
                        buf3 = YUV420rotate90(buf2, buf3, 640, 480);
                        // ---------------------------------------------------------------

                        // ---------------------------------------------------------------
                        // need 1 more horizontal flipping on front camera
                        if (active_camera_type == FRONT_CAMERA_USED)
                        {
                            buf3 = YUV420flipHorizontal(buf3, 480, 640);
                        }
                        // ---------------------------------------------------------------

                        int y_size = 640 * 480;
                        int u_v_size = (640 * 480) / 4;

                        float foregroundConfidence = 1.0f;
                        int y_pos = 0;
                        int u_pos = 0;
                        int v_pos = 0;
                        buf.rewind();

                        try
                        {
                            final int mwidth = 480;
                            final int mheight = 640;
                            final float factor_w = 255.0f / (float) mwidth;
                            final float factor_h = 255.0f / (float) mheight;
                            int read_index_y = 0;
                            int read_index_xy = 0;
                            // TODO: reverse this process, to only reveal the foreground pixels
                            //       so that when an error occurs not the whole image is revealed by mistake!
                            for (int y = 0; y < mheight; y++)
                            {
                                read_index_y = (int) (factor_h * (float) y) * 256;
                                for (int x = 0; x < mwidth; x++)
                                {
                                    read_index_xy = read_index_y + ((int) (factor_w * (float) x));
                                    // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                    // 1.0 being foreground
                                    // 0.0 background
                                    // use values greater than the threshold value CAM_REMOVE_BACKGROUND_CONFIDENCE_THRESHOLD

                                    try
                                    {
                                        foregroundConfidence = buf.getFloat(read_index_xy * 4);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                        foregroundConfidence = 0;
                                    }

                                    /*
                                    if (x < 255)
                                    {
                                        foregroundConfidence = 1.0f;
                                    }
                                    else
                                    {
                                        foregroundConfidence = 0.0f;
                                    }
                                     */

                                    // Log.i(TAG,
                                    //       "x=" + x + " y=" + y + " float=" + foregroundConfidence + " read_index_y=" +
                                    //       read_index_y + " read_index_xy=" + read_index_xy);
                                    if (foregroundConfidence < CAM_REMOVE_BACKGROUND_CONFIDENCE_THRESHOLD)
                                    {
                                        y_pos = (y * mwidth) + x;
                                        u_pos = y_size + (y / 2 * mwidth / 2) + (x / 2);
                                        v_pos = y_size + u_v_size + (y / 2 * mwidth / 2) + (x / 2);

                                        buf3[y_pos] = 0;
                                        buf3[u_pos] = (byte) 128;
                                        buf3[v_pos] = (byte) 128;
                                        // Log.i(TAG, "iiiiii:" + y_pos + " " + u_pos + " " + v_pos);
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if (MainActivity.video_buffer_2 == null)
                        {
                            MainActivity.video_buffer_2 = ByteBuffer.allocateDirect(((640 * 480 * 3) / 2) + 1000);
                            MainActivity.set_JNI_video_buffer2(MainActivity.video_buffer_2, 480, 640);
                        }

                        MainActivity.video_buffer_2.rewind();
                        MainActivity.video_buffer_2.put(buf3);

                        if (PREF__UV_reversed)
                        {
                            int res = HelperGeneric.toxav_video_send_frame_uv_reversed_wrapper(buf3,
                                                                                               tox_friend_by_public_key__wrapper(
                                                                                                       Callstate.friend_pubkey),
                                                                                               480, 640, capture_ts);
                        }
                        else
                        {
                            int res = HelperGeneric.toxav_video_send_frame_wrapper(buf3,
                                                                                   tox_friend_by_public_key__wrapper(
                                                                                           Callstate.friend_pubkey),
                                                                                   480, 640, capture_ts);
                        }

                        if (last_video_frame_sent == -1)
                        {
                            last_video_frame_sent = System.currentTimeMillis();
                            count_video_frame_sent++;
                        }
                        else
                        {
                            if ((count_video_frame_sent > 20) ||
                                ((last_video_frame_sent + 2000) < System.currentTimeMillis()))
                            {
                                VIDEO_FRAME_RATE_OUTGOING = (int) ((((float) count_video_frame_sent / ((float) (
                                        (System.currentTimeMillis() - last_video_frame_sent) / 1000.0f))) / 1.0f) +
                                                                   0.5);
                                // Log.i(TAG, "VIDEO_FRAME_RATE_OUTGOING=" + VIDEO_FRAME_RATE_OUTGOING + " fps");
                                update_fps();
                                last_video_frame_sent = System.currentTimeMillis();
                                count_video_frame_sent = -1;
                            }

                            count_video_frame_sent++;
                        }
                    }

                    // overallTime = SystemClock.uptimeMillis() - overallTime;
                    //Log.d(TAG,
                    //      "RUNNNNNNN:" + preprocessTime + " " + imageSegmentationTime + " " + postrocessTime + " ALL=" +
                    //      overallTime);

                }
            }
            catch (Exception ea)
            {
                ea.printStackTrace();
                // Log.i(TAG, "XXXX:EE:" + ea.getMessage());
            }
            image.close();
        }
    }

    Triple<Bitmap, Bitmap, Map<String, Integer>> convertBytebufferMaskToBitmap(ByteBuffer inputBuffer, int imageWidth, int imageHeight, Bitmap backgroundImage, int[] colors)
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap maskBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf);
        Bitmap resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf);
        // Bitmap scaledBackgroundImage = scaleBitmapAndKeepRatio(backgroundImage, imageWidth, imageHeight);
        int[][] mSegmentBits = new int[imageWidth][imageHeight]; //new Array(imageWidth) { IntArray(imageHeight) }
        HashMap itemsFound = new HashMap<String, Integer>();
        inputBuffer.rewind();

        // Log.i(TAG, "imageWidth=" + imageWidth + " imageHeight" + imageHeight);

        for (int y = 0; y < imageHeight; y++)
        {
            for (int x = 0; x < imageWidth; x++)
            {
                float maxVal = 0f;
                mSegmentBits[x][y] = 0;

                for (int c = 0; c < NUM_CLASSES; c++)
                // int c = 0;
                {
                    float value = inputBuffer.getFloat((y * imageWidth * NUM_CLASSES + x * NUM_CLASSES + c) * 4);
                    if (c == 0 || value > maxVal)
                    {
                        maxVal = value;
                        mSegmentBits[x][y] = c;
                    }
                }
                // String label = labelsArrays[mSegmentBits[x][y]];
                // int color = colors[mSegmentBits[x][y]];
                // itemsFound.put(label, color);
                //int newPixelColor = ColorUtils.compositeColors(colors[mSegmentBits[x][y]],
                //                                               scaledBackgroundImage.getPixel(x, y));
                // resultBitmap.setPixel(x, y, newPixelColor);
                maskBitmap.setPixel(x, y, colors[mSegmentBits[x][y]]);
                // maskBitmap.setPixel(x, y, Color.BLUE);
            }
        }

        return new Triple(resultBitmap, maskBitmap, itemsFound);
    }

    Bitmap scaleBitmapAndKeepRatio(Bitmap targetBmp, int reqHeightInPixels, int reqWidthInPixels, boolean rotate, int rotate_degrees, int camera_type)
    {
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0f, 0f, (float) targetBmp.getWidth(), (float) targetBmp.getHeight()),
                             new RectF(0f, 0f, (float) reqWidthInPixels, (float) reqHeightInPixels),
                             Matrix.ScaleToFit.FILL);

        if (camera_type == FRONT_CAMERA_USED)
        {
            // flip image vertically, after scaling
            matrix.postScale(1, -1, (float) reqWidthInPixels, (float) reqHeightInPixels);
        }

        if (rotate)
        {
            // rotate after scaling
            matrix.postRotate(rotate_degrees);
        }

        return Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), matrix, true);
    }

    ByteBuffer bitmapToByteBuffer(Bitmap bitmap, int width, int height, float mean, float std)
    {
        ByteBuffer inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4);
        inputImage.order(ByteOrder.nativeOrder());
        inputImage.rewind();

        int[] intValues = new int[width * height];
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);
        int pixel = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int value = intValues[pixel++];

                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                inputImage.putFloat((((value >> 16) & 0xFF) - mean) / std);
                inputImage.putFloat((((value >> 8) & 0xFF) - mean) / std);
                inputImage.putFloat(((value & 0xFF) - mean) / std);
            }
        }

        inputImage.rewind();
        return inputImage;
    }

    Bitmap createEmptyBitmap(int imageWidth, int imageHeigth, int color)
    {
        Bitmap ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565);
        if (color != 0)
        {
            ret.eraseColor(color);
        }
        return ret;
    }
}

