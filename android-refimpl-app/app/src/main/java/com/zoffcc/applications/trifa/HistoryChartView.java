package com.zoffcc.applications.trifa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.zoffcc.applications.trifa.TrifaToxService.PUSH_WARN_THRESHOLD_PER_MINUTE;

public class HistoryChartView extends View
{
    private static final long WINDOW_MS = 24L * 60L * 60L * 1000L;

    private static final float MIN_DP_PER_MINUTE = 2.0f;
    private static final float MAX_DP_PER_MINUTE = 60.0f;

    // Fixed height per lane to pack them tightly at the top
    private static final float LANE_HEIGHT_DP = 16.0f;

    // Extra vertical room inside the push section for the caption band
    private static final float PUSH_LABEL_PAD_DP = 13.0f;

    // Push lane colors (top -> bottom): red flood, orange warning, green normal
    private static final int PUSH_COLOR_FLOOD = 0xFFFF0055;
    private static final int PUSH_COLOR_WARN  = 0xFFFF9800;
    private static final int PUSH_COLOR_NORMAL = 0xFF4CAF50;

    private float currentDpPerMinute = 6.0f;

    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] yLane;
    private final SimpleDateFormat hourFmt = new SimpleDateFormat("HH", Locale.US);

    private ScaleGestureDetector scaleGestureDetector;
    private float lastRawX = 0f;

    // Touch marker variables
    private float touchX = -1;
    private boolean isTouching = false;

    public HistoryChartView(Context context) { super(context); init(); }
    public HistoryChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    int maxLanes = 0;

    private void init()
    {
        linePaint.setColor(Color.parseColor("#444444"));
        linePaint.setStrokeWidth(2f);
        textPaint.setColor(Color.parseColor("#DDDDDD"));
        textPaint.setTextSize(30f);

        maxLanes = TRIFAGlobals.APP_STATE.values().length;
        yLane = new int[maxLanes];
        for (int i = 0; i < maxLanes; i++) {
            yLane[i] = i; // Enum value == Y-lane
        }

        setClickable(true);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        float density = getResources().getDisplayMetrics().density;

        // 1. Calculate dynamic width based on current zoom level
        int wantedWidthPx = (int) (1440 * currentDpPerMinute * density);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(wantedWidthPx, MeasureSpec.EXACTLY);

        // 2. Let the height be dictated by the parent (MATCH_PARENT) so it fills the screen
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        scaleGestureDetector.onTouchEvent(event);

        if (scaleGestureDetector.isInProgress())
        {
            isTouching = false;
            invalidate();
            return true;
        }

        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                lastRawX = event.getRawX();
                touchX = event.getX();
                isTouching = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1)
                {
                    float currentRawX = event.getRawX();
                    float dx = lastRawX - currentRawX;

                    if (getParent() instanceof HorizontalScrollView)
                    {
                        ((HorizontalScrollView) getParent()).scrollBy((int) dx, 0);
                    }
                    lastRawX = currentRawX;

                    touchX = event.getX();
                    isTouching = true;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                touchX = -1;
                invalidate();
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            float oldDp = currentDpPerMinute;
            currentDpPerMinute *= detector.getScaleFactor();
            currentDpPerMinute = Math.max(MIN_DP_PER_MINUTE, Math.min(MAX_DP_PER_MINUTE, currentDpPerMinute));

            if (currentDpPerMinute != oldDp)
            {
                float density = getResources().getDisplayMetrics().density;
                float oldPxPerMin = oldDp * density;
                float newPxPerMin = currentDpPerMinute * density;
                float focusX = detector.getFocusX();
                float timeAtFocus = focusX / oldPxPerMin;
                float newFocusX = timeAtFocus * newPxPerMin;
                final int scrollDelta = (int) (newFocusX - focusX);

                requestLayout();
                post(() -> {
                    if (getParent() instanceof HorizontalScrollView)
                    {
                        ((HorizontalScrollView) getParent()).scrollBy(scrollDelta, 0);
                    }
                });
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawColor(0xFF121212);

        final int w = getWidth();
        final int h = getHeight();
        if (w == 0 || h == 0) return;

        final long now = System.currentTimeMillis();
        final long startTs = now - WINDOW_MS;
        final float minuteWidthPx = w / 1440f;

        final int maxLanes = TRIFAGlobals.APP_STATE.values().length;
        float density = getResources().getDisplayMetrics().density;

        // Use fixed DP height for lanes so they are tightly packed at the top
        final float laneHeight = LANE_HEIGHT_DP * density;
        final float squareSize = Math.max(3f, Math.min(laneHeight * 0.75f, minuteWidthPx * 0.9f));

        // Hour grid + labels (spans full screen height for visual guidance)
        for (long hh = ceilToHour(startTs); hh <= now; hh += 3600000L)
        {
            float x = (hh - startTs) / 60000f * minuteWidthPx;
            canvas.drawLine(x, 0, x, h, linePaint);
            canvas.drawText(hourFmt.format(new Date(hh)) + ":00", x + 6, 36, textPaint);
        }

        // Dynamic sub-gridlines based on zoom level
        long gridIntervalMs = 3600000L;
        if (currentDpPerMinute >= 40) gridIntervalMs = 60000L;      // 1 min
        else if (currentDpPerMinute >= 15) gridIntervalMs = 300000L; // 5 mins
        else if (currentDpPerMinute >= 5) gridIntervalMs = 900000L;  // 15 mins

        if (gridIntervalMs < 3600000L) {
            Paint subGridPaint = new Paint();
            subGridPaint.setColor(Color.parseColor("#2A2A2A"));
            subGridPaint.setStrokeWidth(1f);
            for (long t = ceilToInterval(startTs, gridIntervalMs); t <= now; t += gridIntervalMs) {
                if (t % 3600000L == 0) continue;
                float x = (t - startTs) / 60000f * minuteWidthPx;
                canvas.drawLine(x, 0, x, h, subGridPaint);

                if (currentDpPerMinute >= 20) {
                    String subLabel = new SimpleDateFormat("mm", Locale.US).format(new Date(t));
                    textPaint.setTextSize(20f);
                    textPaint.setColor(Color.parseColor("#666666"));
                    canvas.drawText(subLabel, x + 4, 60, textPaint);
                    textPaint.setTextSize(30f);
                    textPaint.setColor(Color.parseColor("#DDDDDD"));
                }
            }
        }

        // NOW marker (spans full screen height)
        textPaint.setColor(Color.WHITE);
        canvas.drawLine(w - 2, 0, w - 2, h, textPaint);
        textPaint.setColor(Color.parseColor("#DDDDDD"));

        int count = TrifaToxService.app_state_history_count;
        if (count == 0)
        {
            textPaint.setTextSize(44f);
            // Draw "No data" in the middle of the packed lanes area
            canvas.drawText("No data yet - waiting for first sample ...", 60, (maxLanes * laneHeight) / 2f, textPaint);
            textPaint.setTextSize(30f);
        }
        else
        {
            int oldest = (TrifaToxService.app_state_history_index - count + TrifaToxService.HISTORY_SIZE)
                         % TrifaToxService.HISTORY_SIZE;

            long prevTs = -1;
            for (int i = 0; i < count; i++)
            {
                int idx = (oldest + i) % TrifaToxService.HISTORY_SIZE;
                long ts = TrifaToxService.app_state_history_ts[idx];
                int state = TrifaToxService.app_state_history[idx];
                if (ts <= 0) continue;
                if (ts < startTs) { prevTs = ts; continue; }

                if (prevTs > 0 && (ts - prevTs) > 60000L)
                {
                    drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx),
                            xOf(ts, startTs, minuteWidthPx),
                            TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, squareSize, w, maxLanes);
                }

                drawSquare(canvas, xOf(ts, startTs, minuteWidthPx), state, laneHeight, squareSize, w, maxLanes);
                prevTs = ts;
            }

            if (prevTs > 0 && (now - prevTs) > 60000L)
            {
                drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx), w,
                        TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, squareSize, w, maxLanes);
            }
        }

        // ====================================================================
        // [ADDED] Push notification section: 3 lanes at the very bottom.
        //   top    = RED    -> flood  (count >= PUSH_FLOOD_THRESHOLD_PER_MINUTE)
        //   middle = ORANGE -> warning (count >= PUSH_WARN_THRESHOLD_PER_MINUTE)
        //   bottom = GREEN  -> normal  (count >= 1)
        // One square per minute, colored by how many pushes arrived in that minute.
        // ====================================================================
        final float pushSectionTop = maxLanes * laneHeight;          // separator position
        final float labelPad = PUSH_LABEL_PAD_DP * density;          // caption band height
        final float pushLanesTop = pushSectionTop + labelPad;        // 3 lanes start below caption

        // subtle separator line between the state lanes and the push section
        canvas.drawLine(0, pushSectionTop, w, pushSectionTop, linePaint);

        // caption, right-aligned so it is visible at "now"
        {
            String pushCaption = "push/min   green<" + PUSH_WARN_THRESHOLD_PER_MINUTE +
                                 "   orange>=" + PUSH_WARN_THRESHOLD_PER_MINUTE +
                                 "   red>=" + TrifaToxService.PUSH_FLOOD_THRESHOLD_PER_MINUTE + " \u25BC";
            textPaint.setTextSize(11f * density);
            textPaint.setColor(Color.parseColor("#B0BEC5"));
            float tw = textPaint.measureText(pushCaption);
            float cx = w - tw - (8f * density);
            if (cx < (8f * density)) cx = (8f * density);
            canvas.drawText(pushCaption, cx, pushSectionTop + (10f * density), textPaint);
        }

        // Bucket the exact-timestamp push ring into per-minute counts
        HashMap<Long, Integer> pushPerMinute = new HashMap<>();
        if (TrifaToxService.push_history_count > 0)
        {
            int pstart = (TrifaToxService.push_history_index - TrifaToxService.push_history_count
                          + TrifaToxService.PUSH_HISTORY_SIZE) % TrifaToxService.PUSH_HISTORY_SIZE;
            for (int i = 0; i < TrifaToxService.push_history_count; i++)
            {
                long pts = TrifaToxService.push_history_ts[(pstart + i) % TrifaToxService.PUSH_HISTORY_SIZE];
                if (pts <= 0) continue;
                if (pts < startTs) continue;   // older than 24h window
                if (pts > now) break;          // ring is chronological
                long minuteBucket = pts / TrifaToxService.MINUTE_IN_MILLIS;
                Integer c = pushPerMinute.get(minuteBucket);
                pushPerMinute.put(minuteBucket, (c == null) ? 1 : (c + 1));
            }
        }

        // Y positions of the 3 push lanes (red top, orange middle, green bottom)
        final float pushRedY    = pushLanesTop + (laneHeight - squareSize) / 2f;
        final float pushOrangeY = pushLanesTop + laneHeight + (laneHeight - squareSize) / 2f;
        final float pushGreenY  = pushLanesTop + (2f * laneHeight) + (laneHeight - squareSize) / 2f;

        for (Map.Entry<Long, Integer> e : pushPerMinute.entrySet())
        {
            long minuteTs = e.getKey() * TrifaToxService.MINUTE_IN_MILLIS;
            float x = xOf(minuteTs, startTs, minuteWidthPx);
            if (x < -squareSize || x > w) continue;

            int c = e.getValue();
            int color;
            float y;
            if (c >= TrifaToxService.PUSH_FLOOD_THRESHOLD_PER_MINUTE)
            {
                color = PUSH_COLOR_FLOOD;
                y = pushRedY;
            }
            else if (c >= PUSH_WARN_THRESHOLD_PER_MINUTE)
            {
                color = PUSH_COLOR_WARN;
                y = pushOrangeY;
            }
            else
            {
                color = PUSH_COLOR_NORMAL;
                y = pushGreenY;
            }

            squarePaint.setColor(color);
            canvas.drawRoundRect(new RectF(x, y, x + squareSize, y + squareSize), 4f, 4f, squarePaint);
        }

        // Touch Marker
        if (isTouching && touchX >= 0 && touchX <= w) {
            Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            markerPaint.setColor(Color.WHITE);
            markerPaint.setStrokeWidth(2f);
            canvas.drawLine(touchX, 0, touchX, h, markerPaint);

            long timeAtTouch = startTs + (long)((touchX / minuteWidthPx) * 60000f);
            String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(timeAtTouch));

            Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgePaint.setColor(Color.parseColor("#EEEEEE"));
            badgePaint.setTextSize(32f);
            float textWidth = badgePaint.measureText(timeStr);

            float badgeX = touchX - textWidth / 2;
            if (badgeX < 10) badgeX = 10;
            if (badgeX + textWidth > w - 10) badgeX = w - textWidth - 10;

            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(Color.parseColor("#CC000000"));
            canvas.drawRoundRect(new RectF(badgeX - 15, 10, badgeX + textWidth + 15, 55), 12, 12, bgPaint);

            badgePaint.setColor(Color.WHITE);
            canvas.drawText(timeStr, badgeX, 42, badgePaint);
        }
    }

    private float xOf(long ts, long startTs, float minuteWidthPx) { return (ts - startTs) / 60000f * minuteWidthPx; }
    private long ceilToHour(long ts) { return ((ts + 3599999L) / 3600000L) * 3600000L; }
    private long ceilToInterval(long ts, long interval) { return ((ts + interval - 1) / interval) * interval; }

    private void drawSquare(Canvas canvas, float x, int state, float laneHeight, float squareSize, int w, int maxLanes)
    {
        if (x < -squareSize || x > w) return;
        int s = state;
        if (s < 0 || s >= maxLanes) s = 0;

        // Draw tightly packed starting from y=0 (top of screen)
        // Highest priority (15) -> y=0. Lowest priority (0) -> y=15*laneHeight
        float y = ((maxLanes - 1) - s) * laneHeight + (laneHeight - squareSize) / 2f;

        squarePaint.setColor(TRIFAGlobals.APP_STATE.getColorForState(s));
        canvas.drawRoundRect(new RectF(x, y, x + squareSize, y + squareSize), 4f, 4f, squarePaint);
    }

    private void drawBar(Canvas canvas, float x1, float x2, int state, float laneHeight, float squareSize, int w, int maxLanes)
    {
        if (x2 <= 0 || x1 >= w) return;
        x1 = Math.max(0, x1);
        x2 = Math.min(w, x2);
        int s = state;
        if (s < 0 || s >= maxLanes) s = 0;

        // Draw tightly packed starting from y=0 (top of screen)
        float y = ((maxLanes - 1) - s) * laneHeight + (laneHeight - squareSize) / 2f;

        squarePaint.setColor(TRIFAGlobals.APP_STATE.getColorForState(s));
        canvas.drawRoundRect(new RectF(x1, y, x2, y + squareSize), 4f, 4f, squarePaint);
    }
}
