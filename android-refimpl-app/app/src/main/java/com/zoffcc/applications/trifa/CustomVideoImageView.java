package com.zoffcc.applications.trifa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static com.zoffcc.applications.trifa.CallingActivity.device_orientation;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_zoom_incoming_video;

public class CustomVideoImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener
{
    private static final String TAG = "trifa.CustomVImgVw";
    private static Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    private int mode = NONE;

    private PointF mStartPoint = new PointF();
    private PointF mMiddlePoint = new PointF();
    private Point mBitmapMiddlePoint = new Point();

    private float oldDist = 1f;
    private float matrixValues[] = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private float oldEventX = 0;
    private float oldEventY = 0;
    private float oldStartPointX = 0;
    private float oldStartPointY = 0;
    private int mViewWidth = -1;
    private int mViewHeight = -1;
    private float scaled_mBitmapWidth = 1;
    private float scaled_mBitmapHeight = 1;
    private boolean mDraggable = false;
    private static boolean matrix_was_reset = false;
    private float sum_scale_factor = 1;


    public CustomVideoImageView(Context context)
    {
        this(context, null, 0);
        if (PREF__X_zoom_incoming_video)
        {
            this.setScaleType(ScaleType.MATRIX);
        }
        else
        {
            this.setScaleType(ScaleType.FIT_CENTER);
        }
        this.setOnTouchListener(this);
        matrix_was_reset = true;
    }

    public CustomVideoImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
        if (PREF__X_zoom_incoming_video)
        {
            this.setScaleType(ScaleType.MATRIX);
        }
        else
        {
            this.setScaleType(ScaleType.FIT_CENTER);
        }
        this.setOnTouchListener(this);
        matrix_was_reset = true;
    }

    public CustomVideoImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (PREF__X_zoom_incoming_video)
        {
            this.setScaleType(ScaleType.MATRIX);
        }
        else
        {
            this.setScaleType(ScaleType.FIT_CENTER);
        }
        this.setOnTouchListener(this);
        matrix_was_reset = true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        if (PREF__X_zoom_incoming_video)
        {
            matrix.reset();
            savedMatrix.reset();
            sum_scale_factor = 1;
            scaled_mBitmapWidth = 1;
            scaled_mBitmapHeight = 1;
        }

        matrix_was_reset = true;
    }

    public static void video_output_orentation_update()
    {
        matrix.reset();
        matrix_was_reset = true;
    }

    public void setBitmap(Bitmap bitmap)
    {
        if (bitmap != null)
        {
            if (!bitmap.isRecycled())
            {
                if (PREF__X_zoom_incoming_video)
                {
                    if (matrix_was_reset)
                    {
                        try
                        {
                            int mBitmapWidth = bitmap.getWidth();
                            int mBitmapHeight = bitmap.getHeight();

                            float scale_h = (float) mViewHeight / (float) mBitmapHeight;
                            float scale_w = (float) mViewWidth / (float) mBitmapWidth;

                            // System.out.println("__onTouch__:" + "scale_w=" + scale_w + ",scale_h=" + scale_h);
                            float scale = Math.min(scale_h, scale_w);
                            float MAX_SCALE_BITMAP = 10.0f;
                            float MIN_SCALE_BITMAP = 0.0001f;
                            if (scale < MIN_SCALE_BITMAP)
                            {
                                scale = MIN_SCALE_BITMAP;
                            }
                            else if (scale > MAX_SCALE_BITMAP)
                            {
                                scale = MAX_SCALE_BITMAP;
                            }

                            sum_scale_factor = 1;

                            scaled_mBitmapWidth = (float) mBitmapWidth * scale;
                            scaled_mBitmapHeight = (float) mBitmapHeight * scale;

                            mBitmapMiddlePoint.x = (mViewWidth / 2) - ((int) scaled_mBitmapWidth / 2);
                            mBitmapMiddlePoint.y = (mViewHeight / 2) - ((int) scaled_mBitmapHeight / 2);

                            System.out.println("__onTouch__:" + "001:" + scale + ":" + mBitmapMiddlePoint.x + "," +
                                               mBitmapMiddlePoint.y + ":bm=" + mBitmapWidth + "," + mBitmapHeight +
                                               ":view=" + mViewWidth + "," + mViewHeight);

                            matrix.postTranslate(mBitmapMiddlePoint.x, mBitmapMiddlePoint.y);
                            matrix_was_reset = false;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    this.setImageMatrix(matrix);
                }
                else
                {
                    if (matrix_was_reset)
                    {
                        try
                        {
                            int rot_needed = 0;

                            if (device_orientation == 90)
                            {
                                Log.i(TAG, "rot_dev:270");
                                rot_needed = 270;
                            }
                            else if (device_orientation == 270)
                            {
                                Log.i(TAG, "rot_dev:90");
                                rot_needed = 90;
                            }
                            else if (device_orientation == 180)
                            {
                                Log.i(TAG, "rot_dev:180");
                                rot_needed = 180;
                            }
                            else
                            {
                                Log.i(TAG, "rot_dev:0");
                                rot_needed = 0;
                            }

                            float scale_up = 1.0f;

                            int img_w = bitmap.getWidth();
                            int img_h = bitmap.getHeight();

                            if ((rot_needed == 0) || (rot_needed == 180))
                            {
                                Log.i(TAG, "scale:1=1.0 " + img_w + " " + img_h + " " + mViewHeight + " " + mViewWidth);
                                this.setScaleX(1.0f);
                                this.setScaleY(1.0f);
                            }
                            else
                            {
                                int tmp = img_w;
                                img_w = img_h;
                                img_h = tmp;

                                if (img_w < img_h)
                                {
                                    // TODO: this is NOT correct yet!!
                                    scale_up = (float) (mViewHeight / 2) / (float) (mViewWidth / 2);
                                    Log.i(TAG,
                                          "scale:2=" + scale_up + " " + img_w + " " + img_h + " " + mViewHeight + " " +
                                          mViewWidth);
                                    this.setScaleX(scale_up);
                                    this.setScaleY(scale_up);
                                }
                                else
                                {
                                    Log.i(TAG,
                                          "scale:3=1.0 " + img_w + " " + img_h + " " + mViewHeight + " " + mViewWidth);
                                    this.setScaleX(1.0f);
                                    this.setScaleY(1.0f);
                                }
                            }

                            this.setRotation(rot_needed);

                            matrix_was_reset = false;
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                            // System.out.println("rot_dev:ERR:" + e.getMessage());
                        }
                    }
                }


                if (!bitmap.isRecycled())
                {
                    setImageBitmap(bitmap);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // System.out.println("__onTouch__:" + "mask=" + (event.getAction() & MotionEvent.ACTION_MASK));

        if (PREF__X_zoom_incoming_video)
        {

            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                    // System.out.println("__onTouch__:" + "ACTION_DOWN");
                    savedMatrix.set(matrix);
                    mStartPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    // System.out.println("__onTouch__:" + "ACTION_POINTER_DOWN");
                    oldDist = spacing(event);
                    if (oldDist > 10f)
                    {
                        savedMatrix.set(matrix);
                        midPoint(mMiddlePoint, event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG)
                    {
                        drag(event);
                    }
                    else if (mode == ZOOM)
                    {
                        // System.out.println("__onTouch__:" + "ZOOM");
                        zoom(event);
                    }
                    break;
            }


            return true;
        }
        else
        {
            return false;
        }
    }


    public void drag(MotionEvent event)
    {
        matrix.getValues(matrixValues);

        float left = matrixValues[2];
        float top = matrixValues[5];
        float bottom = (top + (matrixValues[0] * scaled_mBitmapHeight)) - mViewHeight;
        float right = (left + (matrixValues[0] * scaled_mBitmapWidth)) - mViewWidth;

        float eventX = event.getX();
        float eventY = event.getY();
        float spacingX = eventX - mStartPoint.x;
        float spacingY = eventY - mStartPoint.y;
        float newPositionLeft = (left < 0 ? spacingX : spacingX * -1) + left;
        float newPositionRight = (spacingX) + right;
        float newPositionTop = (top < 0 ? spacingY : spacingY * -1) + top;
        float newPositionBottom = (spacingY) + bottom;
        boolean x = true;
        boolean y = true;

        if (newPositionRight < 0.0f || newPositionLeft > 0.0f)
        {
            if (newPositionRight < 0.0f && newPositionLeft > 0.0f)
            {
                x = false;
            }
            else
            {
                eventX = oldEventX;
                mStartPoint.x = oldStartPointX;
            }
        }
        if (newPositionBottom < 0.0f || newPositionTop > 0.0f)
        {
            if (newPositionBottom < 0.0f && newPositionTop > 0.0f)
            {
                y = false;
            }
            else
            {
                eventY = oldEventY;
                mStartPoint.y = oldStartPointY;
            }
        }

        if (mDraggable)
        {
            matrix.set(savedMatrix);
            matrix.postTranslate(x ? eventX - mStartPoint.x : 0, y ? eventY - mStartPoint.y : 0);
            this.setImageMatrix(matrix);
            if (x)
            {
                oldEventX = eventX;
            }
            if (y)
            {
                oldEventY = eventY;
            }
            if (x)
            {
                oldStartPointX = mStartPoint.x;
            }
            if (y)
            {
                oldStartPointY = mStartPoint.y;
            }
        }

    }

    public void zoom(MotionEvent event)
    {
        matrix.getValues(matrixValues);

        float newDist = spacing(event);
        float bitmapWidth = matrixValues[0] * scaled_mBitmapWidth;
        float bimtapHeight = matrixValues[0] * scaled_mBitmapHeight;
        // System.out.println("__onTouch__:" + "w=" + bitmapWidth + ",h=" + bimtapHeight);
        boolean in = newDist > oldDist;

        if (!in && matrixValues[0] < 1)
        {
            return;
        }

        mDraggable = bitmapWidth > mViewWidth || bimtapHeight > mViewHeight;

        float midX = (mViewWidth / 2);
        float midY = (mViewHeight / 2);

        float scale = newDist / oldDist;
        // System.out.println("__onTouch__:" + "scf=" + scale);
        boolean do_scale = true;

        //        if ((bitmapWidth < 40) || (mBitmapHeight < 40))
        //        {
        //            do_scale = false;
        //        }
        //        else if ((bitmapWidth > (50 * mViewWidth)) || (mBitmapHeight > (50 * mViewHeight)))
        //        {
        //            do_scale = false;
        //        }

        if (do_scale)
        {
            matrix.set(savedMatrix);
            matrix.postScale(scale, scale, midX, midY);
        }

        this.setImageMatrix(matrix);
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


}

