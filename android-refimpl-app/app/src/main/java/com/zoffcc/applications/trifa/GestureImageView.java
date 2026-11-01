package com.zoffcc.applications.trifa;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class GestureImageView extends AppCompatImageView {

    private Matrix matrix = new Matrix();
    private float imageWidth, imageHeight;
    private float viewWidth, viewHeight;
    private float baseScale = 1f;

    // State
    private float zoom = 1f;
    private float currentAngle = 0f; // degrees
    private float panX = 0f, panY = 0f;

    // Gesture variables
    private boolean isTwoFingerGesture = false;
    private float startDist, startZoom, startAngle, startAngleState;
    private float startCenterX, startCenterY, startPanX, startPanY;
    private float focalPointImgX, focalPointImgY;
    private int activePointerId = -1;
    private float lastTouchX, lastTouchY;

    private GestureDetector gestureDetector;
    private float doubleTapFocalX, doubleTapFocalY;
    private float doubleTapImgX, doubleTapImgY;

    // --- FLING VARIABLES ---
    private OverScroller scroller;
    private VelocityTracker velocityTracker;
    private boolean isFlinging = false;
    private static final int MIN_FLING_VELOCITY = 800; // pixels per second

    // --- SWIPE INTERCEPT VARIABLE ---
    private boolean isCurrentGestureConsumable = true;

    public GestureImageView(Context context) {
        super(context);
        init();
    }

    public GestureImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);

        // Initialize Fling components
        scroller = new OverScroller(getContext(), new DecelerateInterpolator());
        velocityTracker = VelocityTracker.obtain();

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                stopFling(); // Stop any ongoing fling on double tap
                float targetZoom = (zoom > 1.5f) ? 1.0f : 2.5f;

                doubleTapFocalX = e.getX();
                doubleTapFocalY = e.getY();
                float[] imgPoint = new float[]{doubleTapFocalX, doubleTapFocalY};
                Matrix inverse = new Matrix();
                if (matrix.invert(inverse)) {
                    inverse.mapPoints(imgPoint);
                }
                doubleTapImgX = imgPoint[0];
                doubleTapImgY = imgPoint[1];

                animateZoom(targetZoom);
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        if (getDrawable() != null && imageWidth == 0) {
            setupBaseScale();
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        stopFling(); // CRITICAL: Stop fling when a new image is loaded/recycled
        if (drawable != null && viewWidth > 0) {
            setupBaseScale();
        }
    }

    private void setupBaseScale() {
        Drawable d = getDrawable();
        if (d == null) return;
        imageWidth = d.getIntrinsicWidth();
        imageHeight = d.getIntrinsicHeight();
        if (imageWidth <= 0 || imageHeight <= 0) return;

        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        baseScale = Math.min(scaleX, scaleY);

        zoom = 1f;
        currentAngle = 0f;
        panX = viewWidth / 2f;
        panY = viewHeight / 2f;
        updateMatrix();
    }

    private void updateMatrix() {
        matrix.reset();
        if (imageWidth <= 0 || imageHeight <= 0) return;

        float imgCenterX = imageWidth / 2f;
        float imgCenterY = imageHeight / 2f;
        float totalScale = baseScale * zoom;

        matrix.postTranslate(-imgCenterX, -imgCenterY);
        matrix.postScale(totalScale, totalScale);
        matrix.postRotate(currentAngle);
        matrix.postTranslate(panX, panY);

        setImageMatrix(matrix);
    }

    // --- FLING HELPER METHODS ---
    private void stopFling() {
        if (isFlinging) {
            isFlinging = false;
            scroller.abortAnimation();
        }
    }

    private void startFling(float velocityX, float velocityY) {
        // Only allow flinging if zoomed in or rotated enough to have pannable area
        boolean canPan = (canScrollHorizontally(1) || canScrollHorizontally(-1) ||
                          canScrollVertically(1) || canScrollVertically(-1));

        if (!canPan) return;

        isFlinging = true;
        // OverScroller expects integers for coordinates and velocities
        scroller.fling(
                (int) panX, (int) panY,
                (int) velocityX, (int) velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE
        );
        postInvalidateOnAnimation();
    }

    /**
     * Called every frame while flinging to update position and check bounds
     */
    private void computeFling() {
        if (!isFlinging) return;

        if (scroller.computeScrollOffset()) {
            float oldPanX = panX;
            float oldPanY = panY;

            panX = scroller.getCurrX();
            panY = scroller.getCurrY();

            // Apply our strict boundary clamping
            clampPan();

            // If clamping changed the values significantly, we hit an edge.
            // Stop the fling so it doesn't grind against the boundary.
            if (Math.abs(panX - oldPanX) > 0.1f || Math.abs(panY - oldPanY) > 0.1f) {
                // We didn't hit an edge, continue
                updateMatrix();
                postInvalidateOnAnimation();
            } else {
                // Hit an edge, stop flinging
                stopFling();
            }
        } else {
            // Fling finished naturally
            isFlinging = false;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        computeFling();
    }

    // Added vertical scroll check for fling validation
    @Override
    public boolean canScrollVertically(int direction) {
        if (zoom <= 1.01f) return false;

        if (imageWidth <= 0 || imageHeight <= 0) return false;
        float w = imageWidth * baseScale * zoom;
        float h = imageHeight * baseScale * zoom;

        float theta = (float) Math.toRadians(currentAngle);
        float cos = Math.abs((float) Math.cos(theta));
        float sin = Math.abs((float) Math.sin(theta));

        float boundH = w * sin + h * cos;

        float top = panY - boundH / 2f;
        float bottom = panY + boundH / 2f;

        if (direction < 0) return top < -1f;
        if (direction > 0) return bottom > viewHeight + 1f;

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        // Track velocity for fling
        velocityTracker.addMovement(event);

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                stopFling(); // CRITICAL: Stop fling immediately on touch
                activePointerId = event.getPointerId(0);
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isTwoFingerGesture = false;
                isCurrentGestureConsumable = true; // Reset on new touch
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                stopFling(); // Stop fling on multi-touch
                isTwoFingerGesture = true;
                isCurrentGestureConsumable = true; // 2-finger gestures are always consumed
                float x0 = event.getX(0), y0 = event.getY(0);
                float x1 = event.getX(1), y1 = event.getY(1);

                startDist = distance(x0, x1, y0, y1);
                startAngle = (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
                startZoom = zoom;
                startAngleState = currentAngle;
                startCenterX = (x0 + x1) / 2f;
                startCenterY = (y0 + y1) / 2f;
                startPanX = panX;
                startPanY = panY;

                // Calculate the image point under the focal point (center of fingers)
                float[] imgPoint = new float[]{startCenterX, startCenterY};
                Matrix inverse = new Matrix();
                if (matrix.invert(inverse)) {
                    inverse.mapPoints(imgPoint);
                }
                focalPointImgX = imgPoint[0];
                focalPointImgY = imgPoint[1];
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isTwoFingerGesture && event.getPointerCount() >= 2) {
                    float x0 = event.getX(0), y0 = event.getY(0);
                    float x1 = event.getX(1), y1 = event.getY(1);
                    float currentDist = distance(x0, x1, y0, y1);
                    float currentAngleDeg = (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));

                    float deltaAngle = currentAngleDeg - startAngle;
                    while (deltaAngle > 180) deltaAngle -= 360;
                    while (deltaAngle < -180) deltaAngle += 360;

                    zoom = startZoom * (currentDist / startDist);
                    currentAngle = startAngleState + deltaAngle;

                    float centerEndX = (x0 + x1) / 2f;
                    float centerEndY = (y0 + y1) / 2f;

                    // Calculate new pan to keep the focal point locked under the fingers
                    float totalScale = baseScale * zoom;
                    float relX = focalPointImgX - imageWidth / 2f;
                    float relY = focalPointImgY - imageHeight / 2f;

                    float scaledX = relX * totalScale;
                    float scaledY = relY * totalScale;

                    float theta = (float) Math.toRadians(currentAngle);
                    float cos = (float) Math.cos(theta);
                    float sin = (float) Math.sin(theta);

                    float rotX = scaledX * cos - scaledY * sin;
                    float rotY = scaledX * sin + scaledY * cos;

                    panX = centerEndX - rotX;
                    panY = centerEndY - rotY;

                    clampPan();
                    updateMatrix();
                    isCurrentGestureConsumable = true;
                } else if (!isTwoFingerGesture && activePointerId != -1) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);

                        float dx = x - lastTouchX;
                        float dy = y - lastTouchY;

                        // Determine if we can pan in the direction of the gesture
                        if (Math.abs(dx) > Math.abs(dy)) {
                            // Horizontal dominant swipe
                            isCurrentGestureConsumable = (dx < 0) ? canScrollHorizontally(1) : canScrollHorizontally(-1);
                        } else if (Math.abs(dy) > 0) {
                            // Vertical dominant swipe
                            isCurrentGestureConsumable = (dy < 0) ? canScrollVertically(1) : canScrollVertically(-1);
                        } else {
                            isCurrentGestureConsumable = true;
                        }

                        panX += dx;
                        panY += dy;
                        lastTouchX = x;
                        lastTouchY = y;

                        clampPan();
                        updateMatrix();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (isTwoFingerGesture) {
                    isTwoFingerGesture = false;
                    snapToNearestRightAngle();
                } else if (activePointerId != -1) {
                    // Calculate fling velocity on single-finger release
                    velocityTracker.computeCurrentVelocity(1000);
                    float vx = velocityTracker.getXVelocity(activePointerId);
                    float vy = velocityTracker.getYVelocity(activePointerId);

                    if (Math.abs(vx) > MIN_FLING_VELOCITY || Math.abs(vy) > MIN_FLING_VELOCITY) {
                        startFling(vx, vy);
                    }
                }
                activePointerId = -1;
                velocityTracker.clear();
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == activePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    if (event.getPointerCount() > newPointerIndex) {
                        lastTouchX = event.getX(newPointerIndex);
                        lastTouchY = event.getY(newPointerIndex);
                        activePointerId = event.getPointerId(newPointerIndex);
                    } else {
                        activePointerId = -1;
                    }
                }

                if (isTwoFingerGesture && event.getPointerCount() == 2) {
                    isTwoFingerGesture = false;
                    snapToNearestRightAngle();
                }
                break;
            }
        }

        // Handle RecyclerView Swiping vs Panning
        boolean isZoomedOrRotated = (zoom > 1.01f || Math.abs(currentAngle % 360) > 5f);

        // Only consume the event if zoomed/rotated AND we are allowed to pan in the current swipe direction
        if (isZoomedOrRotated && isCurrentGestureConsumable) {
            getParent().requestDisallowInterceptTouchEvent(true);
        } else {
            // Give control back to RecyclerView so it can swipe to the next/prev page
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        return true;
    }

    private void snapToNearestRightAngle() {
        // Find the nearest 90 degree multiple
        float targetAngle = Math.round(currentAngle / 90f) * 90f;

        if (Math.abs(currentAngle - targetAngle) > 0.1f) {
            float snapFocalX = viewWidth / 2f;
            float snapFocalY = viewHeight / 2f;
            float[] imgPoint = new float[]{snapFocalX, snapFocalY};
            Matrix inverse = new Matrix();
            float snapImgX = focalPointImgX;
            float snapImgY = focalPointImgY;
            if (matrix.invert(inverse)) {
                inverse.mapPoints(imgPoint);
                snapImgX = imgPoint[0];
                snapImgY = imgPoint[1];
            }

            final float finalSnapImgX = snapImgX;
            final float finalSnapImgY = snapImgY;

            ValueAnimator animator = ValueAnimator.ofFloat(currentAngle, targetAngle);
            animator.setDuration(200);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                currentAngle = (float) animation.getAnimatedValue();

                float totalScale = baseScale * zoom;
                float relX = finalSnapImgX - imageWidth / 2f;
                float relY = finalSnapImgY - imageHeight / 2f;

                float scaledX = relX * totalScale;
                float scaledY = relY * totalScale;

                float theta = (float) Math.toRadians(currentAngle);
                float cos = (float) Math.cos(theta);
                float sin = (float) Math.sin(theta);

                float rotX = scaledX * cos - scaledY * sin;
                float rotY = scaledX * sin + scaledY * cos;

                panX = snapFocalX - rotX;
                panY = snapFocalY - rotY;

                clampPan();
                updateMatrix();
            });
            animator.start();
        }
    }

    private void animateZoom(float targetZoom) {
        ValueAnimator animator = ValueAnimator.ofFloat(zoom, targetZoom);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            zoom = (float) a.getAnimatedValue();

            float totalScale = baseScale * zoom;
            float relX = doubleTapImgX - imageWidth / 2f;
            float relY = doubleTapImgY - imageHeight / 2f;

            float scaledX = relX * totalScale;
            float scaledY = relY * totalScale;

            float theta = (float) Math.toRadians(currentAngle);
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            float rotX = scaledX * cos - scaledY * sin;
            float rotY = scaledX * sin + scaledY * cos;

            panX = doubleTapFocalX - rotX;
            panY = doubleTapFocalY - rotY;

            clampPan();
            updateMatrix();
        });
        animator.start();
    }

    private float distance(float x0, float x1, float y0, float y1) {
        float dx = x0 - x1;
        float dy = y0 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        float normAngle = currentAngle % 360;
        if (normAngle > 180) normAngle -= 360;
        if (normAngle < -180) normAngle += 360;

        // Rotated, consume all horizontal to allow panning (handled dynamically in onTouchEvent now)
        // if (Math.abs(normAngle) > 5f) return true;

        if (zoom <= 1.01f) return false; // Let parent (RecyclerView) scroll

        RectF rect = getDisplayRect();
        if (rect != null) {
            if (direction < 0) return rect.left < -1f;
            if (direction > 0) return rect.right > viewWidth + 1f;
        }
        return false;
    }

    private RectF getDisplayRect() {
        if (imageWidth <= 0 || imageHeight <= 0) return null;
        float w = imageWidth * baseScale * zoom;
        float h = imageHeight * baseScale * zoom;

        float theta = (float) Math.toRadians(currentAngle);
        float cos = Math.abs((float) Math.cos(theta));
        float sin = Math.abs((float) Math.sin(theta));

        float boundW = w * cos + h * sin;

        float left = panX - boundW / 2f;
        float right = panX + boundW / 2f;

        return new RectF(left, 0, right, 0);
    }

    private void clampPan() {
        if (imageWidth <= 0 || imageHeight <= 0) return;

        float w = imageWidth * baseScale * zoom;
        float h = imageHeight * baseScale * zoom;

        float theta = (float) Math.toRadians(currentAngle);
        float cos = Math.abs((float) Math.cos(theta));
        float sin = Math.abs((float) Math.sin(theta));

        // Calculate bounding box of rotated/scaled image
        float boundW = w * cos + h * sin;
        float boundH = w * sin + h * cos;

        float minPanX, maxPanX;
        if (boundW < viewWidth) {
            minPanX = maxPanX = viewWidth / 2f; // Center horizontally if smaller than view
        } else {
            minPanX = viewWidth - boundW / 2f;
            maxPanX = boundW / 2f;
        }

        float minPanY, maxPanY;
        if (boundH < viewHeight) {
            minPanY = maxPanY = viewHeight / 2f; // Center vertically if smaller than view
        } else {
            minPanY = viewHeight - boundH / 2f;
            maxPanY = boundH / 2f;
        }

        // Hard clamp so the image cannot be dragged off-screen
        panX = Math.max(minPanX, Math.min(maxPanX, panX));
        panY = Math.max(minPanY, Math.min(maxPanY, panY));
    }

    public void resetState() {
        stopFling();
        zoom = 1f;
        currentAngle = 0f;
        if (viewWidth > 0 && viewHeight > 0) {
            panX = viewWidth / 2f;
            panY = viewHeight / 2f;
        } else {
            panX = 0f;
            panY = 0f;
        }
        updateMatrix();
        invalidate(); // Force redraw
    }
}
