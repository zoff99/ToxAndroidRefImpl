package com.zoffcc.applications.trifa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.zoffcc.applications.trifa.TrifaToxService.PUSH_WARN_THRESHOLD_PER_MINUTE;

public class HistoryChartView extends View
{
    static final String TAG = "trifa.HstChart";

    private static final long WINDOW_MS = 24L * 60L * 60L * 1000L;

    // Blank "breathing room" appended AFTER now, so the latest data is not flush
    // against the right edge. The view is widened by this many minutes.
    private static final int RIGHT_PAD_MINUTES = 5;

    private static final float MIN_DP_PER_MINUTE = 2.0f;
    private static final float MAX_DP_PER_MINUTE = 60.0f;

    // Fixed height per lane to pack them tightly at the top
    private static final float LANE_HEIGHT_DP = 16.0f;

    // Extra vertical room inside the push section for the caption band
    private static final float PUSH_LABEL_PAD_DP = 10.0f;

    // [ADDED] small bottom margin used only by the off-screen export renderer,
    // so the last (no-internet) lane is not flush against the PNG edge.
    private static final float EXPORT_BOTTOM_PAD_DP = 8.0f;

    // Push lane colors (top -> bottom): red flood, orange warning, green normal
    private static final int PUSH_COLOR_FLOOD = 0xFFFF0055;
    private static final int PUSH_COLOR_WARN = 0xFFFF9800;
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

    public HistoryChartView(Context context)
    {
        super(context);
        init();
    }

    public HistoryChartView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

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

    // ===================== [ADDED] public API for the off-screen exporter =====================

    /** Set the horizontal resolution (dp per minute) used by the NEXT measure/draw.
     *  Used by the exporter to render at a fixed zoom independent of the live pinch state. */
    public void setDpPerMinute(float dp)
    {
        this.currentDpPerMinute = dp;
    }

    /** Total horizontal span in minutes (24h window + right padding strip). */
    public static int getTotalSpanMinutes()
    {
        return 1440 + RIGHT_PAD_MINUTES;
    }

    /** Exact pixel height of the *content* (all state lanes + push caption + 3 push lanes +
     *  no-internet caption + 1 no-internet lane + bottom pad). The exporter sizes its bitmap
     *  to this so the PNG contains no empty black space below the chart. */
    public int getContentHeightPx()
    {
        float density = getResources().getDisplayMetrics().density;
        float laneHeight = LANE_HEIGHT_DP * density;
        float labelPad = PUSH_LABEL_PAD_DP * density;
        int lanes = (maxLanes > 0) ? maxLanes : TRIFAGlobals.APP_STATE.values().length;
        float bottom = (lanes + 4f) * laneHeight + 2f * labelPad + EXPORT_BOTTOM_PAD_DP * density;
        return Math.max(1, (int) Math.ceil(bottom));
    }

    // ==========================================================================================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        float density = getResources().getDisplayMetrics().density;

        // 1. Width covers the 24h window PLUS the right padding strip
        int totalSpanMinutes = 1440 + RIGHT_PAD_MINUTES;
        int wantedWidthPx = (int) (totalSpanMinutes * currentDpPerMinute * density);
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
        final long startTs = now - WINDOW_MS;                 // left edge = 24h ago
        final long endTs = startTs + (WINDOW_MS + RIGHT_PAD_MINUTES * 60000L); // right edge = now + pad
        final int totalSpanMinutes = 1440 + RIGHT_PAD_MINUTES;
        final float minuteWidthPx = w / (float) totalSpanMinutes;

        final int maxLanes = TRIFAGlobals.APP_STATE.values().length;
        float density = getResources().getDisplayMetrics().density;

        // 1. Calculate the fixed vertical space used by captions and padding
        float fixedVerticalPadding = (2f * PUSH_LABEL_PAD_DP * density) + (EXPORT_BOTTOM_PAD_DP * density);

        // 2. Total number of horizontal lanes we need to draw (States + 3 Push + 1 No-Internet)
        int totalLaneSlots = maxLanes + 4;

        // 3. DYNAMIC LANE HEIGHT:
        // Calculate the maximum height a lane can be without exceeding the View's actual pixel height (h)
        float idealLaneHeight = LANE_HEIGHT_DP * density;
        float maxAvailableLaneHeight = (h - fixedVerticalPadding) / totalLaneSlots;

        // Use the ideal height, but shrink it if the screen is too short to prevent clipping
        final float laneHeight = Math.min(idealLaneHeight, Math.max(4f * density, maxAvailableLaneHeight));
        final float squareSize = Math.max(3f, Math.min(laneHeight * 0.75f, minuteWidthPx * 0.9f));

        // Hour grid + labels. Lines run across the FULL span (incl. padding strip)
        // so the blank area reads as a ruler; labels only up to "now".
        for (long hh = ceilToHour(startTs); hh <= endTs; hh += 3600000L)
        {
            float x = (hh - startTs) / 60000f * minuteWidthPx;
            canvas.drawLine(x, 0, x, h, linePaint);
            if (hh <= now)
            {
                canvas.drawText(hourFmt.format(new Date(hh)) + ":00", x + 6, 36, textPaint);
            }
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
            for (long t = ceilToInterval(startTs, gridIntervalMs); t <= endTs; t += gridIntervalMs) {
                if (t % 3600000L == 0) continue;
                float x = (t - startTs) / 60000f * minuteWidthPx;
                canvas.drawLine(x, 0, x, h, subGridPaint);

                if ((currentDpPerMinute >= 20) && (t <= now)) {
                    String subLabel = new SimpleDateFormat("mm", Locale.US).format(new Date(t));
                    textPaint.setTextSize(20f);
                    textPaint.setColor(Color.parseColor("#666666"));
                    canvas.drawText(subLabel, x + 4, 60, textPaint);
                    textPaint.setTextSize(30f);
                    textPaint.setColor(Color.parseColor("#DDDDDD"));
                }
            }
        }

        // NOW marker: drawn at the TRUE now position (left of the right edge),
        // so the latest data has breathing room to its right.
        final float nowX = xOf(now, startTs, minuteWidthPx);
        textPaint.setColor(Color.WHITE);
        canvas.drawLine(nowX, 0, nowX, h, textPaint);
        textPaint.setColor(Color.parseColor("#DDDDDD"));

        // ====================================================================
        // PARALLEL STATE LANES: Draw each enum state independently using the
        // new boolean[][] app_state_histories ring buffer.
        // Drawn as individual squares per minute, exactly like the push boxes.
        // ====================================================================

        // Check if we have ANY valid timestamps in the shared TS array
        boolean hasData = false;
        for (int i = 0; i < TrifaToxService.HISTORY_SIZE; i++) {
            if (TrifaToxService.app_state_history_ts[i] > 0) {
                hasData = true;
                break;
            }
        }

        if (!hasData)
        {
            textPaint.setTextSize(44f);
            // Draw "No data" in the middle of the packed lanes area
            canvas.drawText("No data yet - waiting for first sample ...", 60, (maxLanes * laneHeight) / 2f, textPaint);
            textPaint.setTextSize(30f);
        }
        else
        {
            int currentIndex = TrifaToxService.app_state_history_index;
            long prevTs = -1;

            // Walk through the entire ring buffer chronologically
            for (int offset = 0; offset < TrifaToxService.HISTORY_SIZE; offset++)
            {
                int idx = (currentIndex + offset) % TrifaToxService.HISTORY_SIZE;
                long ts = TrifaToxService.app_state_history_ts[idx];

                if (ts <= 0) continue;
                if (ts < startTs) { prevTs = ts; continue; }
                if (ts > now) break;

                // 1. Fill gaps between recordings with an ASLEEP bar (exactly like the old version)
                if (prevTs > 0 && (ts - prevTs) > 60000L)
                {
                    drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx),
                            xOf(ts, startTs, minuteWidthPx),
                            TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, squareSize, w, maxLanes);
                }

                // 2. Draw individual squares for EVERY active parallel state at this minute
                for (int s = 0; s < maxLanes; s++)
                {
                    if (TrifaToxService.app_state_histories[s] != null && TrifaToxService.app_state_histories[s][idx])
                    {
                        drawSquare(canvas, xOf(ts, startTs, minuteWidthPx), s, laneHeight, squareSize, w, maxLanes);
                    }
                }

                prevTs = ts;
            }

            // trailing asleep bar ends at "now", not at the right edge
            if (prevTs > 0 && (now - prevTs) > 60000L)
            {
                drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx), nowX,
                        TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, squareSize, w, maxLanes);
            }
        }

        // ====================================================================
        // Push notification section: 3 lanes at the very bottom.
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

        // caption, right-aligned to the NOW edge so it sits just left of the padding strip
        {
            String pushCaption = "push/min   green<" + PUSH_WARN_THRESHOLD_PER_MINUTE +
                                 "   orange>=" + PUSH_WARN_THRESHOLD_PER_MINUTE +
                                 "   red>=" + TrifaToxService.PUSH_FLOOD_THRESHOLD_PER_MINUTE + " \u25BC";
            textPaint.setTextSize(11f * density);
            textPaint.setColor(Color.parseColor("#B0BEC5"));
            float tw = textPaint.measureText(pushCaption);
            float cx = nowX - tw - (8f * density);
            if (cx < (8f * density)) cx = (8f * density);
            canvas.drawText(pushCaption, cx, pushSectionTop + (10f * density), textPaint);
        }

        // Bucket the exact-timestamp push ring into per-minute counts
        java.util.HashMap<Long, Integer> pushPerMinute = new java.util.HashMap<>();
        if (TrifaToxService.push_history_count > 0)
        {
            int pstart = (TrifaToxService.push_history_index - TrifaToxService.push_history_count +
                          TrifaToxService.PUSH_HISTORY_SIZE) % TrifaToxService.PUSH_HISTORY_SIZE;
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

        for (java.util.Map.Entry<Long, Integer> e : pushPerMinute.entrySet())
        {
            long minuteTs = e.getKey() * TrifaToxService.MINUTE_IN_MILLIS;
            float x = xOf(minuteTs, startTs, minuteWidthPx);
            if (x < -squareSize || x > nowX) continue; // never draw into the padding strip

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


        // ====================================================================
        // No-Internet lane: ONE lane below the 3 push lanes.
        // Shows, independent of state priority, every minute where the OS had
        // no connectivity. Consecutive minutes are merged into outage bars.
        // Color is tied to STATE_NO_INTERNET.color so it matches the state block.
        // ====================================================================
        final float noInternetLaneTop = pushLanesTop + (3f * laneHeight);   // directly below green push lane
        final float niLabelPad        = PUSH_LABEL_PAD_DP * density;        // caption band, same as push section
        final float noInternetY       = noInternetLaneTop + niLabelPad + (laneHeight - squareSize) / 2f;
        final int noInternetColor     = TRIFAGlobals.APP_STATE.STATE_NO_INTERNET.color;

        // separator between push section and no-internet lane
        canvas.drawLine(0, noInternetLaneTop, w, noInternetLaneTop, linePaint);

        // caption, right-anchored to "now" so it stays visible in the default view
        {
            String niCaption = "no internet";
            textPaint.setTextSize(11f * density);
            float tw = textPaint.measureText(niCaption);
            float cx = nowX - tw - (8f * density);
            if (cx < (8f * density)) cx = (8f * density);
            canvas.drawText(niCaption, cx, noInternetLaneTop + (10f * density), textPaint);
        }

        if (hasData)
        {
            squarePaint.setColor(noInternetColor);
            boolean ni_in_run  = false;
            float   ni_run_x1  = 0f;
            float   ni_run_x2  = 0f;

            int oldest2 = (TrifaToxService.app_state_history_index - TrifaToxService.HISTORY_SIZE + TrifaToxService.HISTORY_SIZE) %
                          TrifaToxService.HISTORY_SIZE;

            for (int i = 0; i < TrifaToxService.HISTORY_SIZE; i++)
            {
                int  idx = (oldest2 + i) % TrifaToxService.HISTORY_SIZE;
                long ts  = TrifaToxService.app_state_history_ts[idx];
                if (ts <= 0) continue;

                if (ts > now)
                {
                    break;            // ring is chronological
                }

                if (TrifaToxService.no_internet_history[idx])
                {
                    if (ts < startTs)
                    {
                        continue;     // older than 24h window (head of ring)
                    }

                    float x1 = xOf(ts, startTs, minuteWidthPx);
                    float x2 = xOf(ts + TrifaToxService.MINUTE_IN_MILLIS, startTs, minuteWidthPx);
                    if (x2 > nowX) x2 = nowX;   // never draw into the right padding strip
                    if (x2 <= x1)
                    {
                        continue;
                    }

                    if (!ni_in_run)
                    {
                        ni_in_run = true;
                        ni_run_x1 = x1;
                        ni_run_x2 = x2;
                    }
                    else if (x1 <= ni_run_x2 + 1f)
                    {
                        ni_run_x2 = x2;         // contiguous -> extend the bar
                    }
                    else
                    {
                        canvas.drawRoundRect(new RectF(ni_run_x1, noInternetY, ni_run_x2, noInternetY + squareSize), 4f, 4f, squarePaint);
                        ni_run_x1 = x1;
                        ni_run_x2 = x2;
                    }
                }
                else
                {
                    if (ni_in_run)
                    {
                        canvas.drawRoundRect(new RectF(ni_run_x1, noInternetY, ni_run_x2, noInternetY + squareSize), 4f, 4f, squarePaint);
                        ni_in_run = false;
                    }
                }
            }

            if (ni_in_run)
            {
                canvas.drawRoundRect(new RectF(ni_run_x1, noInternetY, ni_run_x2, noInternetY + squareSize), 4f, 4f, squarePaint);
            }
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

    /** Exact timestamp (ms) at a given x pixel coordinate. Same mapping as onDraw(). */
    public long getTimestampAtX(float x)
    {
        int w = getWidth();
        if (w <= 0) return System.currentTimeMillis();
        float minuteWidthPx = w / (float) getTotalSpanMinutes();
        long startTs = System.currentTimeMillis() - WINDOW_MS;
        return startTs + (long) ((x / minuteWidthPx) * 60000f);   // float math, ms precision
    }

    /** Exact x pixel coordinate for a timestamp. Same mapping as onDraw(). */
    public float getXForTimestamp(long timestamp)
    {
        int w = getWidth();
        if (w <= 0) return 0f;
        float minuteWidthPx = w / (float) getTotalSpanMinutes();
        long startTs = System.currentTimeMillis() - WINDOW_MS;
        return ((timestamp - startTs) / 60000f) * minuteWidthPx;   // float math, no truncation
    }

    private float xOf(long ts, long startTs, float minuteWidthPx) { return (ts - startTs) / 60000f * minuteWidthPx; }
    private long ceilToHour(long ts) { return ((ts + 3599999L) / 3600000L) * 3600000L; }
    private long ceilToInterval(long ts, long interval) { return ((ts + interval - 1) / interval) * interval; }

    /** Draws a single square for an active state lane. */
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

    /** Draws a continuous bar spanning multiple minutes for a specific state lane (used for ASLEEP gaps). */
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

    /** Helper for the parallel loop to draw a bar segment without recalculating Y repeatedly. */
    private void drawBarRect(Canvas canvas, float x1, float x2, int state, float laneHeight, float squareSize, int w, int maxLanes)
    {
        if (x2 <= 0 || x1 >= w) return;
        x1 = Math.max(0, x1);
        x2 = Math.min(w, x2);
        int s = state;
        if (s < 0 || s >= maxLanes) s = 0;

        float y = ((maxLanes - 1) - s) * laneHeight + (laneHeight - squareSize) / 2f;
        canvas.drawRoundRect(new RectF(x1, y, x2, y + squareSize), 4f, 4f, squarePaint);
    }
}
