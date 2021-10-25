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

import static com.zoffcc.applications.trifa.CallingActivity.loadModelFile;

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
    private org.tensorflow.lite.Interpreter interpreter = null;
    // private ImageSegmenter.ImageSegmenterOptions options;
    // private ImageSegmenter segmenter;
    private Activity a;
    private Context c;
    private final int imageSize = 256;
    private final int NUM_CLASSES = 21;
    private final float IMAGE_MEAN = 127.5f;
    private final float IMAGE_STD = 127.5f;
    String[] labelsArrays;
    int[] segmentColors;

    VideoFrameAnalyserTFLite(CameraDrawingOverlay drawingOverlay, Context c, Activity a)
    {
        this.drawingOverlay = drawingOverlay;
        this.a = a;
        this.c = c;

        labelsArrays = new String[]{"background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow", "dining table", "dog", "horse", "motorbike", "person", "potted plant", "sheep", "sofa", "train", "tv"};
        segmentColors = new int[NUM_CLASSES];

        Random random = new Random(System.currentTimeMillis());
        segmentColors[0] = Color.TRANSPARENT;
        for (int i = 1; i < NUM_CLASSES; i++)
        {
            segmentColors[i] = Color.argb((128), getRandomRGBInt(random), getRandomRGBInt(random),
                                          getRandomRGBInt(random));
        }

        /*
        // Initialization
        options = ImageSegmenter.ImageSegmenterOptions.
                builder().
                setNumThreads(4).
                setOutputType(OutputType.CONFIDENCE_MASK).
                build();

        try
        {
            segmenter = ImageSegmenter.createFromFileAndOptions(c, "deeplabv3_257_mv_gpu.tflite", options);
        }
        catch (IOException e)
        {
            Log.i(TAG, "segmenter:EE:" + e.getMessage());
            e.printStackTrace();
        }
        */

        Interpreter.Options options = new Interpreter.Options();
        /*
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
         */

        options.setNumThreads(4);

        ByteBuffer tfliteModel = null;
        try
        {
            tfliteModel = loadModelFile(a);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.interpreter = new Interpreter(tfliteModel, options);
    }

    int getRandomRGBInt(Random random)
    {
        return (int) ((255 * random.nextFloat()));
    }

    public void setInterpreter(Interpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy image)
    {
        frameMediaImage = image.getImage();
        if (frameMediaImage != null)
        {
            Log.i(TAG, "YYYYY:" + Thread.currentThread().getName());

            try
            {
                if (!Callstate.audio_call)
                {
                    final long capture_ts = System.currentTimeMillis();

                    long preprocessTime = SystemClock.uptimeMillis();

                    YuvToRgbConverter yuvToRgbConverter = new YuvToRgbConverter(this.c);
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
                    long imageSegmentationTime = SystemClock.uptimeMillis();
                    Log.i(TAG, "tensor_in_out:" + contentArray.limit() + " " + contentArray.limit() + " " +
                               segmentationMasks.limit());
                    /*
                        The general model operates on a 256x256x3 (HWC) tensor,
                        and outputs a 256x256x1 tensor representing the segmentation mask
                     */
                    interpreter.run(contentArray, segmentationMasks);
                    imageSegmentationTime = SystemClock.uptimeMillis() - imageSegmentationTime;

                    segmentationMasks.limit(imageSize * imageSize * 4);
                    segmentationMasks.rewind();
                    Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
                    Log.i(TAG,
                          "tensor_res_bitmaps:" + bitmap.getAllocationByteCount() + " " + bitmap.getByteCount() + " " +
                          bitmap.getRowBytes());
                    bitmap.copyPixelsFromBuffer(segmentationMasks);
                    drawingOverlay.maskBitmap = bitmap;
                    drawingOverlay.invalidate();


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

                    Log.d(TAG, "RUNNNNNNN:" + preprocessTime + " " + imageSegmentationTime);

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
                inputImage.putFloat(((value >> 0xFF) - mean) / std);
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

