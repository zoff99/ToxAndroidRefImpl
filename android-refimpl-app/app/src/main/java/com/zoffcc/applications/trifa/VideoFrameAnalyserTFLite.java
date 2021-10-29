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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.SystemClock;
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

/*
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;
*/

public class VideoFrameAnalyserTFLite implements ImageAnalysis.Analyzer
{
    private static final String TAG = "trifa.FrameAnalyserTFL";

    private final CameraDrawingOverlay drawingOverlay;
    private Image frameMediaImage = null;
    static org.tensorflow.lite.Interpreter interpreter = null;
    // private ImageSegmenter.ImageSegmenterOptions options;
    // private ImageSegmenter segmenter;
    private Activity a;
    private Context c;
    private final int imageSize = 256;
    private final int NUM_CLASSES = 21;
    private final float IMAGE_MEAN = 0f; // 127.5f;
    private final float IMAGE_STD = 127.5f;
    String[] labelsArrays;
    int[] segmentColors;
    YuvToRgbConverter yuvToRgbConverter;

    VideoFrameAnalyserTFLite(CameraDrawingOverlay drawingOverlay, Context c, Activity a)
    {
        this.drawingOverlay = drawingOverlay;
        this.a = a;
        this.c = c;

        labelsArrays = new String[]{"background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow", "dining table", "dog", "horse", "motorbike", "person", "potted plant", "sheep", "sofa", "train", "tv"};
        segmentColors = new int[NUM_CLASSES];

        yuvToRgbConverter = new YuvToRgbConverter(this.c);

        Random random = new Random(System.currentTimeMillis());
        segmentColors[0] = Color.TRANSPARENT;
        /*
        for (int i = 1; i < NUM_CLASSES; i++)
        {
            segmentColors[i] = Color.argb((128), getRandomRGBInt(random), getRandomRGBInt(random),
                                          getRandomRGBInt(random));
        }
         */

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
            // Log.i(TAG, "YYYYY:" + Thread.currentThread().getName());

            try
            {
                if (!Callstate.audio_call)
                {
                    final long capture_ts = System.currentTimeMillis();

                    long preprocessTime = SystemClock.uptimeMillis();
                    long overallTime = preprocessTime;

                    Bitmap bmp = Bitmap.createBitmap(frameMediaImage.getWidth(), frameMediaImage.getHeight(),
                                                     Bitmap.Config.ARGB_8888);
                    yuvToRgbConverter.yuvToRgb(frameMediaImage, bmp);

                    Log.i(TAG, "bbbb:1:" + bmp.getWidth() + " " + bmp.getHeight() + " " + imageSize);
                    Bitmap scaledBitmap = scaleBitmapAndKeepRatio(bmp, imageSize, imageSize);
                    Log.i(TAG, "bbbb:2:" + scaledBitmap.getWidth() + " " + scaledBitmap.getHeight() + " " + imageSize);
                    ByteBuffer contentArray = bitmapToByteBuffer(scaledBitmap, imageSize, imageSize, IMAGE_MEAN,
                                                                 IMAGE_STD);

                    ByteBuffer segmentationMasks = ByteBuffer.allocateDirect(imageSize * imageSize * 4);
                    segmentationMasks.order(ByteOrder.nativeOrder());

                    preprocessTime = SystemClock.uptimeMillis() - preprocessTime;
                    Log.i(TAG, "tensor_in_out:" + contentArray.limit() + " " + contentArray.limit() + " " +
                               segmentationMasks.limit());
                    /*
                        The general model operates on a 256x256x3 (HWC) tensor,
                        and outputs a 256x256x1 tensor representing the segmentation mask
                     */
                    long imageSegmentationTime = SystemClock.uptimeMillis();
                    interpreter.run(contentArray, segmentationMasks);
                    imageSegmentationTime = SystemClock.uptimeMillis() - imageSegmentationTime;

                    long postrocessTime = SystemClock.uptimeMillis();
                    // segmentationMasks.limit(imageSize * imageSize * 4);
                    segmentationMasks.rewind();
                    Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
                    Log.i(TAG,
                          "tensor_res_bitmaps:" + bitmap.getAllocationByteCount() + " " + bitmap.getByteCount() + " " +
                          bitmap.getRowBytes());
                    bitmap.copyPixelsFromBuffer(segmentationMasks);
                    drawingOverlay.maskBitmap = bitmap;
                    drawingOverlay.invalidate();
                    postrocessTime = SystemClock.uptimeMillis() - postrocessTime;

                    overallTime = SystemClock.uptimeMillis() - overallTime;

                    /*
                    long maskFlatteningTime = SystemClock.uptimeMillis();
                    Triple<Bitmap, Bitmap, Map<String, Integer>> ret;

                    ret = convertBytebufferMaskToBitmap(segmentationMasks, imageSize, imageSize, scaledBitmap,
                                                        segmentColors);
                    maskFlatteningTime = SystemClock.uptimeMillis() - maskFlatteningTime;
                    drawingOverlay.maskBitmap = ret.component2();
                    drawingOverlay.invalidate();
                     */

                    //segmentationMasks.limit(imageSize * imageSize * 4);
                    //segmentationMasks.rewind();
                    //Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
                    //bitmap.copyPixelsFromBuffer(segmentationMasks);
                    //drawingOverlay.maskBitmap = bitmap;
                    //drawingOverlay.invalidate();

                    Log.d(TAG,
                          "RUNNNNNNN:" + preprocessTime + " " + imageSegmentationTime + " " + postrocessTime + " ALL=" +
                          overallTime);

                    /*
                    ImageProcessor imageProcessor = new ImageProcessor.Builder().
                            add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR)).
                            build();

                    TensorImage tensorImage = new TensorImage(DataType.UINT8);
                    Log.i(TAG, "YYYYY:1:" + tensorImage);
                    tensorImage.load(frameMediaImage);
                    // tensorImage = imageProcessor.process(tensorImage);
                    Log.i(TAG, "YYYYY:2:" + tensorImage + " segmenter=" + segmenter);
                    List<Segmentation> results = segmenter.segment(tensorImage);

                    Log.i(TAG, "YYYYY:3:" + results.size() + " " + results.get(0).getMasks().get(0).getWidth() + " " +
                               results.get(0).getMasks().get(0).getHeight());

                    ByteBuffer buf_mask = results.get(0).getMasks().get(0).getBuffer();
                    Log.i(TAG, "YYYYY:4:" + buf_mask.limit());

                    buf_mask.rewind();
                    Bitmap bitmap = Bitmap.createBitmap(results.get(0).getMasks().get(0).getWidth(),
                                                        results.get(0).getMasks().get(0).getHeight(),
                                                        Bitmap.Config.ARGB_8888);
                    // bitmap.copyPixelsFromBuffer(buf_mask);
                    for (int j = 0; j < results.get(0).getMasks().get(0).getHeight(); j++)
                    {
                        for (int i = 0; i < results.get(0).getMasks().get(0).getWidth(); i++)
                        {
                            float f = buf_mask.getFloat();
                            // Log.i(TAG, "f=" + f);
                            if (f > 0.9)
                            {
                                bitmap.setPixel(i, j, Color.RED);
                            }
                            else
                            {
                                bitmap.setPixel(i, j, Color.BLUE);
                            }
                        }
                    }
                    drawingOverlay.maskBitmap = bitmap;
                    drawingOverlay.invalidate();

                     */

                    // final ByteBuffer input = ByteBuffer.allocateDirect(640 * 480 * 3 / 2);
                    // final ByteBuffer buf = ByteBuffer.allocateDirect(640 * 480 * 4); // 4 byte float output mask buffer
                    // interpreter.run(input, buf);


                    // only send video frame if call has started
                    if (!((Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_NONE.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_ERROR.value) ||
                          (Callstate.tox_call_state == TOXAV_FRIEND_CALL_STATE_FINISHED.value)))
                    {

                        ByteBuffer buf = segmentationMasks;
                        buf.rewind();
                        byte[] buf2 = new byte[((640 * 480) * 3 / 2)];
                        byte[] buf3 = new byte[((640 * 480) * 3 / 2)];

                        // Log.i(TAG, "format:" + image.getPlanes().length + " " + image.getFormat() + " " +
                        //           image.getImageInfo() + " " + image.getPlanes()[1].getPixelStride() +
                        //           " " + image.getPlanes()[1].getRowStride());
                        ByteBuffer y_ = image.getPlanes()[0].getBuffer();
                        ByteBuffer u_ = image.getPlanes()[1].getBuffer();
                        ByteBuffer v_ = image.getPlanes()[2].getBuffer();

                        // y_.rewind();
                        // u_.rewind();
                        // v_.rewind();
                        //Log.i(TAG, "format:0:" +image.getPlanes()[0].getBuffer().limit()+" "+image.getPlanes()[0].getRowStride()+" "+image.getPlanes()[0].getPixelStride());
                        //Log.i(TAG, "format:1:" +image.getPlanes()[1].getBuffer().limit()+" "+image.getPlanes()[1].getRowStride()+" "+image.getPlanes()[1].getPixelStride());
                        //Log.i(TAG, "format:2:" +image.getPlanes()[2].getBuffer().limit()+" "+image.getPlanes()[2].getRowStride()+" "+image.getPlanes()[2].getPixelStride());

                        y_.rewind();
                        u_.rewind();
                        v_.rewind();

                        y_.get(buf2, 0, 640 * 480);
                        final int off_u = 640 * 480;
                        final int off_v = (640 * 480) + (640 * 480) / 4;
                        if ((image.getPlanes()[1].getPixelStride() > 1) || (image.getPlanes()[2].getPixelStride() > 1))
                        {
                            for (int k = 0; k < ((640 * 480) / 4); k++)
                            {
                                buf2[off_u + k] = v_.get(k * image.getPlanes()[1].getPixelStride());
                                buf2[off_v + k] = u_.get(k * image.getPlanes()[2].getPixelStride());
                            }
                        }
                        else // pixelstrides for u and v both are "1"
                        {
                            // Log.i(TAG, "SSSSSSSSSS:" + image.getPlanes()[1].getPixelStride());
                            v_.get(buf2, off_u, (640 * 480) / 4);
                            u_.get(buf2, off_v, (640 * 480) / 4);
                        }

                        // TODO: haxx0r, make better and actually check all the angles
                        //       and rotate always correctly
                        if (active_camera_type == FRONT_CAMERA_USED)
                        {
                            buf3 = YUV420rotate90(buf2, buf3, 640, 480);
                            buf2 = YUV420rotate90(buf3, buf2, 480, 640);
                        }

                        int y_size = 640 * 480;
                        int u_v_size = (640 * 480) / 4;

                        float foregroundConfidence = 1.0f;
                        int y_pos = 0;
                        int u_pos = 0;
                        int v_pos = 0;
                        int x1 = 0;
                        int y1 = 0;
                        int _x = 0;
                        int _y = 0;
                        int rotated_width;
                        int buf_pos = 0;

                        try
                        {
                            final int mwidth = 480;
                            final int mheight = 640;
                            boolean need_read_float = false;
                            int count_read = 0;
                            int last_x_buf = 0;
                            int tmp = 0;
                            final float factor = 480f / 256f;
                            // TODO: reverse this process, to only reveal the foreground pixels
                            //       so that when an error occurs not the whole image is revealed by mistake!
                            for (int y = 0; y < mheight; y++)
                            {
                                // Log.i(TAG, "tmp==Y== " + y + " ===");
                                for (int x = 0; x < mwidth; x++)
                                {
                                    // Log.i(TAG, "tmp==X== " + x + " ===");
                                    if (x == 0)
                                    {
                                        need_read_float = true;
                                        count_read = 0;
                                        last_x_buf = 0;
                                    }
                                    else
                                    {
                                        if (count_read >= 255)
                                        {
                                            need_read_float = false;
                                        }
                                        else
                                        {
                                            tmp = (int) ((float) (x) / factor);
                                            if (tmp > last_x_buf)
                                            {
                                                last_x_buf = tmp;
                                                need_read_float = true;
                                            }
                                            else
                                            {
                                                need_read_float = false;
                                            }
                                        }
                                    }

                                    // Log.i(TAG, "tmp=" + tmp + " x=" + x + " y=" + y + " count_read=" + count_read +
                                    //           " last_x_buf=" + last_x_buf);


                                    // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                    // 1.0 being foreground
                                    // 0.0 background
                                    // use values greater than the threshold value CAM_REMOVE_BACKGROUND_CONFIDENCE_THRESHOLD
                                    /*
                                    final float buf_x = 256;
                                    final float buf_y = 256;
                                    buf_pos = (int) (buf_y) * (int) ((float) y * (buf_y / (float) mheight)) +
                                              (int) ((float) x * (buf_x / (float) mwidth));
                                    foregroundConfidence = buf.getFloat(buf_pos); //buf.getFloat();
                                     */

                                    if (need_read_float)
                                    {
                                        foregroundConfidence = buf.getFloat();
                                        count_read++;
                                    }

                                    // Log.i(TAG, "x=" + x + " y=" + y + " float=" + foregroundConfidence);
                                    if (foregroundConfidence < CAM_REMOVE_BACKGROUND_CONFIDENCE_THRESHOLD)
                                    {
                                        y1 = mwidth - x - 1;
                                        x1 = y;
                                        rotated_width = 640;
                                        y_pos = (y1 * rotated_width) + x1;
                                        u_pos = y_size + (y1 / 2 * rotated_width / 2) + (x1 / 2);
                                        v_pos = y_size + u_v_size + (y1 / 2 * rotated_width / 2) + (x1 / 2);

                                        buf2[y_pos] = 0;
                                        buf2[u_pos] = (byte) 128;
                                        buf2[v_pos] = (byte) 128;
                                        // Log.i(TAG, "iiiiii:" + y_pos + " " + u_pos + " " + v_pos);
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                            // Log.i(TAG, "tmp=EEEEEE:" + e.getMessage());
                            //Log.i(TAG,
                            //      "iiiiii:y_pos=" + y_pos + " u_pos=" + u_pos + " v_pos=" + v_pos + " x=" + x + " y=" +
                            //      y + " x1=" + x1 + " y1=" + y1 + " y_size=" + y_size + " u_v_size=" + u_v_size +
                            //      " buf_pos=" + buf_pos);
                        }

                        if (MainActivity.video_buffer_2 == null)
                        {
                            MainActivity.video_buffer_2 = ByteBuffer.allocateDirect(((640 * 480 * 3) / 2) + 1000);
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
                        // MainActivity.video_buffer_2.put(YUV_420_888toNV21_x(buf2, 640, 480));


                        buf3 = YUV420rotate90(buf2, buf3, 640, 480);
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
                        // Log.i(TAG, "XXXX:res:" + res);

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


                }
            }
            catch (Exception ea)
            {
                ea.printStackTrace();
                Log.i(TAG, "XXXX:EE:" + ea.getMessage());
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

        Log.i(TAG, "imageWidth=" + imageWidth + " imageHeight" + imageHeight);

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

    Bitmap scaleBitmapAndKeepRatio(Bitmap targetBmp, int reqHeightInPixels, int reqWidthInPixels)
    {
        if (targetBmp.getHeight() == reqHeightInPixels && targetBmp.getWidth() == reqWidthInPixels)
        {
            return targetBmp;
        }
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0f, 0f, (float) targetBmp.getWidth(), (float) targetBmp.getHeight()),
                             new RectF(0f, 0f, (float) reqWidthInPixels, (float) reqHeightInPixels),
                             Matrix.ScaleToFit.FILL);

        return Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), matrix, true);
    }

    ByteBuffer bitmapToByteBuffer(Bitmap bitmapIn, int width, int height, float mean, float std)
    {
        Bitmap bitmap = scaleBitmapAndKeepRatio(bitmapIn, width, height);
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

