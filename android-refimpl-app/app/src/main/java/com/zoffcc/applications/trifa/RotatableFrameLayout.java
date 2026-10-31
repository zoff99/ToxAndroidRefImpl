package com.zoffcc.applications.trifa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class RotatableFrameLayout extends FrameLayout {

    private float currentRotation = 0f;
    private float gestureStartAngle = 0f;
    private float initialRotation = 0f;
    private boolean isRotating = false;
    private ValueAnimator currentAnimator = null;

    public RotatableFrameLayout(Context context) {
        super(context);
        init();
    }

    public RotatableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotatableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setChildRotation(float rotation) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
        this.currentRotation = rotation;
        if (getChildCount() > 0) {
            getChildAt(0).setRotation(rotation);
        }
    }

    public float getChildRotation() {
        return currentRotation;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 1. Detect Rotation Gesture
        if (ev.getPointerCount() == 2) {
            float deltaX = ev.getX(0) - ev.getX(1);
            float deltaY = ev.getY(0) - ev.getY(1);
            float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));

            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                gestureStartAngle = angle;
                initialRotation = currentRotation;
                isRotating = true;
            } else if (action == MotionEvent.ACTION_MOVE && isRotating) {
                float delta = angle - gestureStartAngle;
                // Normalize delta to be between -180 and 180
                while (delta > 180) delta -= 360;
                while (delta < -180) delta += 360;

                float newRotation = initialRotation + delta;
                setChildRotation(newRotation);
            }
        } else {
            int action = ev.getActionMasked();
            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) && isRotating) {
                isRotating = false;
                snapToNearestRightAngle();
            }
        }

        // 2. Un-rotate coordinates for the child (PhotoView)
        // This ensures the child receives touch events as if it were not rotated,
        // preserving pan/zoom functionality perfectly.
        if (currentRotation != 0) {
            MotionEvent copy = MotionEvent.obtain(ev);
            Matrix matrix = new Matrix();
            float cx = getWidth() > 0 ? getWidth() / 2f : 0f;
            float cy = getHeight() > 0 ? getHeight() / 2f : 0f;
            matrix.postRotate(-currentRotation, cx, cy);
            copy.transform(matrix);
            boolean res = super.dispatchTouchEvent(copy);
            copy.recycle();
            return res;
        }

        return super.dispatchTouchEvent(ev);
    }

    private void snapToNearestRightAngle() {
        float norm = currentRotation % 360;
        if (norm < 0) norm += 360;

        float snapped;
        if (norm < 45) {
            snapped = 0;
        } else if (norm < 135) {
            snapped = 90;
        } else if (norm < 225) {
            snapped = 180;
        } else if (norm < 315) {
            snapped = 270;
        } else {
            snapped = 360;
        }

        float base = currentRotation - norm;
        float targetRotation = base + snapped;

        animateSnap(targetRotation);
    }

    private void animateSnap(float targetRotation) {
        if (getChildCount() > 0) {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }
            float startRotation = currentRotation;
            currentAnimator = ValueAnimator.ofFloat(startRotation, targetRotation);
            currentAnimator.setDuration(150);
            currentAnimator.addUpdateListener(animation -> {
                float val = (float) animation.getAnimatedValue();
                currentRotation = val;
                getChildAt(0).setRotation(val);
            });
            currentAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    currentRotation = targetRotation;
                    getChildAt(0).setRotation(targetRotation);
                    currentAnimator = null;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    currentAnimator = null;
                }
            });
            currentAnimator.start();
        } else {
            currentRotation = targetRotation;
        }
    }
}
