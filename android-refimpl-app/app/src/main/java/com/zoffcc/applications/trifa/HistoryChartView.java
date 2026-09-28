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
import android.view.ViewTreeObserver;
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

    // small bottom margin used only by the off-screen export renderer,
    // so the last (no-internet) lane is not flush against the PNG edge.
    private static final float EXPORT_BOTTOM_PAD_DP = 8.0f;

    // Push lane colors (top -> bottom): red flood, orange warning, green normal
    private static final int PUSH_COLOR_FLOOD = 0xFFFF0055;
    private static final int PUSH_COLOR_WARN = 0xFFFF9800;
    private static final int PUSH_COLOR_NORMAL = 0xFF4CAF50;

    private float currentDpPerMinute = 6.0f;

    // --- Matrix zoom state (visual-only during the gesture, committed on release) ---
    private boolean isZooming = false;
    private float liveScaleFactor = 1f;
    private float livePivotX = 0f;

    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] yLane;
    private final SimpleDateFormat hourFmt = new SimpleDateFormat("HH", Locale.US);

    private ScaleGestureDetector scaleGestureDetector;

    // Touch marker variables
    private float touchX = -1;
    private boolean isTouching = false;

    // --- EXPORT FLAG ---
    // Set this to true before drawing to an off-screen bitmap for PNG export.
    // This changes the boxes to be solid, and sharp-cornered.
    public boolean isExporting = false;

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
        for (int i = 0; i < maxLanes; i++)
        {
            yLane[i] = i; // Enum value == Y-lane
        }

        setClickable(true);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    // ===================== public API for the off-screen exporter =====================

    /** Set the horizontal resolution (dp per minute) used by the NEXT measure/draw. */
    public void setDpPerMinute(float dp)
    {
        this.currentDpPerMinute = dp;
    }

    /** Total horizontal span in minutes (24h window + right padding strip). */
    public static int getTotalSpanMinutes()
    {
        return 1440 + RIGHT_PAD_MINUTES;
    }

    /** Exact pixel height of the *content* (all lanes + captions + bottom pad). */
    public int getContentHeightPx()
    {
        float density = getResources().getDisplayMetrics().density;
        float laneHeight = LANE_HEIGHT_DP * density;
        float labelPad = PUSH_LABEL_PAD_DP * density;
        int lanes = (maxLanes > 0) ? maxLanes : TRIFAGlobals.APP_STATE.values().length;
        int transportLanes = ConnectionManager.NetworkTransportType.values().length;
        // lanes + 3 push + transport lanes
        float bottom = (lanes + 3f + transportLanes) * laneHeight + 2f * labelPad + EXPORT_BOTTOM_PAD_DP * density;
        return Math.max(1, (int) Math.ceil(bottom));
    }

    // ==================================================================================

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

    // Catch multi-touch early so the HorizontalScrollView never steals the pinch gesture
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (ev.getPointerCount() > 1)
        {
            if (getParent() != null)
            {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        scaleGestureDetector.onTouchEvent(event);

        // While pinch-zooming we own the gesture completely
        if (scaleGestureDetector.isInProgress())
        {
            return true;
        }

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
        {
            touchX = event.getX();
            isTouching = true;
            invalidate();
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            isTouching = false;
            invalidate();
        }
        return true;
    }

    /**
     * Matrix-based pinch zoom.
     * During the gesture we ONLY scale the canvas (zero layout passes, zero ScrollView fights).
     * On release we commit the new dpPerMinute once and re-anchor the scroll position
     * AFTER the new width has been laid out (OnGlobalLayoutListener), which eliminates
     * the huge horizontal jump that happened when the anchor was applied too early.
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        private float startDp = 0f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            isZooming = true;
            startDp = currentDpPerMinute;
            liveScaleFactor = 1f;
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            float newScale = liveScaleFactor * detector.getScaleFactor();
            float projectedDp = startDp * newScale;

            // clamp visually during the gesture
            if (projectedDp < MIN_DP_PER_MINUTE) newScale = MIN_DP_PER_MINUTE / startDp;
            else if (projectedDp > MAX_DP_PER_MINUTE) newScale = MAX_DP_PER_MINUTE / startDp;

            liveScaleFactor = newScale;
            livePivotX = detector.getFocusX();

            invalidate(); // redraw with matrix, NO requestLayout()
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
            isZooming = false;
            float oldDp = startDp;
            float newDp = startDp * liveScaleFactor;
            newDp = Math.max(MIN_DP_PER_MINUTE, Math.min(MAX_DP_PER_MINUTE, newDp));

            // The pivot is the fixed point of the matrix scale, so its content coord
            // equals its view coord. Capture the anchor WITH THE PRE-COMMIT MAPPING
            // (width and dpPerMinute are still the old ones here - exactly what was drawn).
            final long anchorTs = getTimestampAtX(livePivotX);

            float scrollNow = 0f;
            if (getParent() instanceof HorizontalScrollView)
            {
                scrollNow = ((HorizontalScrollView) getParent()).getScrollX();
            }
            final float focusScreenX = livePivotX - scrollNow;   // where the pivot sits on screen

            // commit the zoom
            liveScaleFactor = 1f;
            currentDpPerMinute = newDp;
            requestLayout();

            // Apply the anchor AFTER the new width has been measured + laid out.
            // This guarantees fresh widths for the clamp (no stale-width jump)
            // and runs after any internal HorizontalScrollView adjustment.
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
            {
                @Override
                public void onGlobalLayout()
                {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (!(getParent() instanceof HorizontalScrollView)) return;
                    HorizontalScrollView sv = (HorizontalScrollView) getParent();

                    float newX = getXForTimestamp(anchorTs) - focusScreenX;
                    int maxScroll = Math.max(0, getWidth() - sv.getWidth());   // FRESH width
                    int target = (int) newX;
                    if (target < 0) target = 0;
                    if (target > maxScroll) target = maxScroll;
                    sv.scrollTo(target, 0);
                }
            });
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

        // Apply visual matrix zoom during the pinch gesture
        boolean applyMatrix = isZooming && liveScaleFactor != 1f;
        if (applyMatrix)
        {
            canvas.save();
            canvas.scale(liveScaleFactor, 1f, livePivotX, 0f);
        }

        final long now = System.currentTimeMillis();
        final long startTs = now - WINDOW_MS;                 // left edge = 24h ago
        final long endTs = startTs + (WINDOW_MS + RIGHT_PAD_MINUTES * 60000L); // right edge = now + pad
        final int totalSpanMinutes = 1440 + RIGHT_PAD_MINUTES;
        final float minuteWidthPx = w / (float) totalSpanMinutes;

        final int maxLanes = TRIFAGlobals.APP_STATE.values().length;
        float density = getResources().getDisplayMetrics().density;

        // 1. Fixed vertical space used by captions and padding
        float fixedVerticalPadding = (2f * PUSH_LABEL_PAD_DP * density) + (EXPORT_BOTTOM_PAD_DP * density);
        int transportLanesCount = ConnectionManager.NetworkTransportType.values().length;
        // 2. Total number of horizontal lanes (States + 3 Push + Transport Lanes)
        int totalLaneSlots = maxLanes + 3 + transportLanesCount;
        // 3. DYNAMIC LANE HEIGHT: shrink lanes if the screen is too short (prevents clipping)
        float idealLaneHeight = LANE_HEIGHT_DP * density;
        float maxAvailableLaneHeight = (h - fixedVerticalPadding) / totalLaneSlots;
        final float laneHeight = Math.min(idealLaneHeight, Math.max(4f * density, maxAvailableLaneHeight));

        // REGULAR DISPLAY: Small squares with gaps
        final float squareSize = Math.max(3f, Math.min(laneHeight * 0.75f, minuteWidthPx * 0.9f));

        // --- EXPORT vs REGULAR DISPLAY ---
        // REGULAR DISPLAY: small rounded squares with gaps (unchanged).
        // EXPORT: tall, sharp-cornered blocks, but WITH tiny gaps between minutes
        //         so the segmented look is preserved in the PNG.
        final float drawW;
        final float drawH;
        final float cornerRadius;
        if (isExporting) {
            // Tiny gap between blocks: at least 2px, otherwise 10% of the minute cell
            final float gap = Math.max(2f, minuteWidthPx * 0.1f);
            drawW = minuteWidthPx - gap;
            drawH = Math.max(4f * density, laneHeight * 0.85f);
            cornerRadius = 0f;
        } else {
            drawW = squareSize;
            drawH = squareSize;
            cornerRadius = 4f;
        }
        // ------------------------------

        // Hour grid + labels
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

        if (gridIntervalMs < 3600000L)
        {
            Paint subGridPaint = new Paint();
            subGridPaint.setColor(Color.parseColor("#2A2A2A"));
            subGridPaint.setStrokeWidth(1f);
            for (long t = ceilToInterval(startTs, gridIntervalMs); t <= endTs; t += gridIntervalMs)
            {
                if (t % 3600000L == 0) continue;
                float x = (t - startTs) / 60000f * minuteWidthPx;
                canvas.drawLine(x, 0, x, h, subGridPaint);

                if ((currentDpPerMinute >= 20) && (t <= now))
                {
                    String subLabel = new SimpleDateFormat("mm", Locale.US).format(new Date(t));
                    textPaint.setTextSize(20f);
                    textPaint.setColor(Color.parseColor("#666666"));
                    canvas.drawText(subLabel, x + 4, 60, textPaint);
                    textPaint.setTextSize(30f);
                    textPaint.setColor(Color.parseColor("#DDDDDD"));
                }
            }
        }

        // NOW marker: drawn at the TRUE now position (left of the right edge)
        final float nowX = xOf(now, startTs, minuteWidthPx);
        textPaint.setColor(Color.WHITE);
        canvas.drawLine(nowX, 0, nowX, h, textPaint);
        textPaint.setColor(Color.parseColor("#DDDDDD"));

        // ====================================================================
        // PARALLEL STATE LANES: one independent track per APP_STATE.
        // Individual squares per minute (like the push boxes) + ASLEEP gap bars.
        // ====================================================================
        boolean hasData = false;
        for (int i = 0; i < TrifaToxService.HISTORY_SIZE; i++)
        {
            if (TrifaToxService.app_state_history_ts[i] > 0)
            {
                hasData = true;
                break;
            }
        }

        if (!hasData)
        {
            textPaint.setTextSize(44f);
            canvas.drawText("No data yet - waiting for first sample ...", 60, (maxLanes * laneHeight) / 2f, textPaint);
            textPaint.setTextSize(30f);
        }
        else
        {
            int currentIndex = TrifaToxService.app_state_history_index;
            long prevTs = -1;

            // Walk the ring buffer chronologically
            for (int offset = 0; offset < TrifaToxService.HISTORY_SIZE; offset++)
            {
                int idx = (currentIndex + offset) % TrifaToxService.HISTORY_SIZE;
                long ts = TrifaToxService.app_state_history_ts[idx];

                if (ts <= 0) continue;
                if (ts < startTs) { prevTs = ts; continue; }
                if (ts > now) break;

                // 1. Fill gaps between recordings with an ASLEEP bar
                if (prevTs > 0 && (ts - prevTs) > 60000L)
                {
                    drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx),
                            xOf(ts, startTs, minuteWidthPx),
                            TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, drawH, cornerRadius, w, maxLanes);
                }

                // 2. Draw individual squares for EVERY active parallel state at this minute
                for (int s = 0; s < maxLanes; s++)
                {
                    if (TrifaToxService.app_state_histories[s] != null && TrifaToxService.app_state_histories[s][idx])
                    {
                        drawSquare(canvas, xOf(ts, startTs, minuteWidthPx), s, laneHeight, drawW, drawH, cornerRadius, w, maxLanes);
                    }
                }

                prevTs = ts;
            }

            // trailing asleep bar ends at "now", not at the right edge
            if (prevTs > 0 && (now - prevTs) > 60000L)
            {
                drawBar(canvas, xOf(prevTs + 60000L, startTs, minuteWidthPx), nowX,
                        TRIFAGlobals.APP_STATE.STATE_ASLEEP.value, laneHeight, drawH, cornerRadius, w, maxLanes);
            }
        }

        // ====================================================================
        // Push notification section: 3 lanes at the very bottom.
        //   top    = RED    -> flood
        //   middle = ORANGE -> warning
        //   bottom = GREEN  -> normal
        // ====================================================================
        final float pushSectionTop = maxLanes * laneHeight;          // separator position
        final float labelPad = PUSH_LABEL_PAD_DP * density;          // caption band height
        final float pushLanesTop = pushSectionTop + labelPad;        // 3 lanes start below caption

        canvas.drawLine(0, pushSectionTop, w, pushSectionTop, linePaint);

        // caption, right-aligned to the NOW edge
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
        final float pushRedY    = pushLanesTop + (laneHeight - drawH) / 2f;
        final float pushOrangeY = pushLanesTop + laneHeight + (laneHeight - drawH) / 2f;
        final float pushGreenY  = pushLanesTop + (2f * laneHeight) + (laneHeight - drawH) / 2f;

        for (java.util.Map.Entry<Long, Integer> e : pushPerMinute.entrySet())
        {
            long minuteTs = e.getKey() * TrifaToxService.MINUTE_IN_MILLIS;
            float x = xOf(minuteTs, startTs, minuteWidthPx);
            if (x < -drawW || x > nowX) continue; // never draw into the padding strip

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
            canvas.drawRoundRect(new RectF(x, y, x + drawW, y + drawH), cornerRadius, cornerRadius, squarePaint);
        }

        // ====================================================================
        // Network State section: lanes below the 3 push lanes.
        // Sorted strictly by NetworkTransportType enum .value (0 at bottom, max at top).
        // ====================================================================
        final int maxTransportValue = ConnectionManager.NetworkTransportType.getMaxValue();

        final float transportSectionTop = pushLanesTop + (3f * laneHeight);
        final float transportLabelPad   = PUSH_LABEL_PAD_DP * density;
        final float transportLanesTop   = transportSectionTop + transportLabelPad;

        canvas.drawLine(0, transportSectionTop, w, transportSectionTop, linePaint);

        {
            String transportCaption = "network state";
            textPaint.setTextSize(11f * density);
            textPaint.setColor(Color.parseColor("#B0BEC5"));
            float tw = textPaint.measureText(transportCaption);
            float cx = nowX - tw - (8f * density);
            if (cx < (8f * density)) cx = (8f * density);
            canvas.drawText(transportCaption, cx, transportSectionTop + (10f * density), textPaint);
            textPaint.setColor(Color.parseColor("#DDDDDD"));
        }

        if (hasData)
        {
            int oldest3 = (TrifaToxService.app_state_history_index - TrifaToxService.HISTORY_SIZE + TrifaToxService.HISTORY_SIZE) %
                          TrifaToxService.HISTORY_SIZE;

            for (int i = 0; i < TrifaToxService.HISTORY_SIZE; i++)
            {
                int idx = (oldest3 + i) % TrifaToxService.HISTORY_SIZE;
                long ts = TrifaToxService.app_state_history_ts[idx];
                if (ts <= 0) continue;
                if (ts < startTs) continue;
                if (ts > now) break;

                float x = xOf(ts, startTs, minuteWidthPx);
                if (x < -drawW || x > nowX) continue;

                // Iterate through all defined transport types
                for (ConnectionManager.NetworkTransportType type : ConnectionManager.NetworkTransportType.values())
                {
                    int arrayIndex = type.value;

                    // Safety check to prevent out-of-bounds if array sizing mismatches
                    if (arrayIndex < TrifaToxService.app_transport_histories.length &&
                        TrifaToxService.app_transport_histories[arrayIndex][idx])
                    {
                        float y = transportLanesTop + ((maxTransportValue - type.value) * laneHeight) + (laneHeight - drawH) / 2f;

                        // Use the color embedded in the enum
                        squarePaint.setColor(type.color);
                        canvas.drawRoundRect(new RectF(x, y, x + drawW, y + drawH), cornerRadius, cornerRadius, squarePaint);
                    }
                }
            }
        }

        // Restore canvas BEFORE drawing UI overlays so they don't get stretched
        if (applyMatrix) canvas.restore();

        // Touch Marker (drawn unstretched, hidden while pinch-zooming)
        if (isTouching && !isZooming && touchX >= 0 && touchX <= w)
        {
            Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            markerPaint.setColor(Color.WHITE);
            markerPaint.setStrokeWidth(2f);
            canvas.drawLine(touchX, 0, touchX, h, markerPaint);

            long timeAtTouch = startTs + (long) ((touchX / minuteWidthPx) * 60000f);
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

    /** Draws a single box for an active state lane. */
    private void drawSquare(Canvas canvas, float x, int state, float laneHeight, float boxW, float boxH, float cornerRadius, int w, int maxLanes)
    {
        if (x < -boxW || x > w) return;
        int s = state;
        if (s < 0 || s >= maxLanes) s = 0;

        float y = ((maxLanes - 1) - s) * laneHeight + (laneHeight - boxH) / 2f;

        squarePaint.setColor(TRIFAGlobals.APP_STATE.getColorForState(s));
        canvas.drawRoundRect(new RectF(x, y, x + boxW, y + boxH), cornerRadius, cornerRadius, squarePaint);
    }

    /** Draws a continuous bar spanning multiple minutes (used for ASLEEP gaps). */
    private void drawBar(Canvas canvas, float x1, float x2, int state, float laneHeight, float boxH, float cornerRadius, int w, int maxLanes)
    {
        if (x2 <= 0 || x1 >= w) return;
        x1 = Math.max(0, x1);
        x2 = Math.min(w, x2);
        int s = state;
        if (s < 0 || s >= maxLanes) s = 0;

        float y = ((maxLanes - 1) - s) * laneHeight + (laneHeight - boxH) / 2f;

        squarePaint.setColor(TRIFAGlobals.APP_STATE.getColorForState(s));
        canvas.drawRoundRect(new RectF(x1, y, x2, y + boxH), cornerRadius, cornerRadius, squarePaint);
    }
}
