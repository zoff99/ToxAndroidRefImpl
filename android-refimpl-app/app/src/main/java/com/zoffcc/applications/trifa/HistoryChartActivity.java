package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryChartActivity extends AppCompatActivity
{
    private static final int REQ_PICK_TREE = 0x51;
    private static final String PREF_TREE_URI_KEY = "stats_export_tree_uri";

    // Fixed horizontal resolution for the EXPORT image (dp per minute)
    // [CHANGED] Increased so the boxes are much wider and look more square.
    private static final float EXPORT_DP_PER_MINUTE = 13.0f;

    // [CHANGED] Increased the hard safety clamp to 70k so the wider image doesn't get artificially shrunk back down.
    private static final int MAX_EXPORT_WIDTH_PX = 70000;

    private LockableHorizontalScrollView scrollView;
    private HistoryChartView chartView;

    // Tracks if the user has scrolled away from the live "now" (right) edge
    private boolean userScrolledAway = false;
    private long targetTimestamp = 0; // The exact historical time the user is looking at
    private boolean programmaticScroll = false;
    private boolean userIsTouching = false; // Tracks if the user's finger(s) are currently on the screen

    private volatile boolean exporting = false;

    // Our reusable exporter instance
    private DocumentExporter exporter;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (chartView != null)
            {
                chartView.invalidate();

                if (!userScrolledAway)
                {
                    scroll_to_now();
                }
                else if (!userIsTouching)
                {
                    // Keep the viewport glued to targetTimestamp: as the 24h window rolls,
                    // that timestamp's x shrinks by minuteWidthPx/12 per 5s tick; follow it exactly.
                    // We ONLY do this if the user is NOT actively touching the screen.
                    int newScrollX = (int) chartView.getXForTimestamp(targetTimestamp);
                    int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
                    if (newScrollX < 0) newScrollX = 0;              // target aged out of the 24h window
                    if (newScrollX > maxScroll) newScrollX = maxScroll; // safety, cannot normally happen
                    scrollXTo(newScrollX);
                }
            }
            refreshHandler.postDelayed(this, 5000);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("24h App State");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize the reusable exporter
        exporter = new DocumentExporter(this, PREF_TREE_URI_KEY, REQ_PICK_TREE);

        // 1. Root FrameLayout to allow overlaying the Legend + the Export button
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF121212);

        scrollView = new LockableHorizontalScrollView(this);
        scrollView.setBackgroundColor(0xFF121212);

        chartView = new HistoryChartView(this);

        // Chart width is WRAP_CONTENT (dictated by zoom), height MATCH_PARENT so the
        // whole screen receives scroll / pinch gestures.
        scrollView.addView(chartView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                   FrameLayout.LayoutParams.MATCH_PARENT));

        // Intercept touch events to know when the user's fingers are on the screen
        scrollView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
            {
                userIsTouching = true;
            }
            else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
            {
                userIsTouching = false;

                // Post to the message queue so we calculate the timestamp AFTER
                // the HorizontalScrollView finishes any fling or edge-snap animations.
                scrollView.post(() -> {
                    if (chartView != null)
                    {
                        int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
                        int currentScroll = scrollView.getScrollX();
                        if (currentScroll < maxScroll - 20)
                        {
                            userScrolledAway = true;
                            targetTimestamp = chartView.getTimestampAtX(currentScroll);
                        }
                        else
                        {
                            userScrolledAway = false;
                        }
                    }
                });
            }
            // Note: we intentionally ignore ACTION_POINTER_UP (one finger lifts, another remains)
            // so userIsTouching stays true until the VERY LAST finger leaves the screen.

            return false;
        });

        // Track if the user scrolls away from the live "now" edge.
        // Only ignore OUR OWN corrections; user pans and pinch-commit scrolls must update the anchor.
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (programmaticScroll || chartView == null) return;

            int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
            int currentScroll = scrollView.getScrollX();

            if (currentScroll < maxScroll - 20)
            {
                userScrolledAway = true;
                // pin to the exact timestamp currently at the left edge of the viewport
                targetTimestamp = chartView.getTimestampAtX(currentScroll);
            }
            else
            {
                // user manually scrolled back to the live edge -> resume live tail
                userScrolledAway = false;
            }
        });

        rootLayout.addView(scrollView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                    FrameLayout.LayoutParams.MATCH_PARENT));

        // 2. Legend Overlay (touch-transparent)
        LinearLayout legendContainer = new LinearLayout(this)
        {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) { return false; }
            @Override
            public boolean onTouchEvent(MotionEvent event) { return false; }
        };

        legendContainer.setOrientation(LinearLayout.VERTICAL);
        legendContainer.setBackgroundColor(0xDD121212);
        legendContainer.setPadding(15, 15, 15, 15);

        TRIFAGlobals.APP_STATE[] allStates = TRIFAGlobals.APP_STATE.values();
        java.util.Arrays.sort(allStates, (a, b) -> Integer.compare(b.value, a.value));

        int[] statesToShow = new int[allStates.length];
        for (int i = 0; i < allStates.length; i++)
        {
            statesToShow[i] = allStates[i].value;
        }

        for (int state : statesToShow)
        {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 4, 0, 4);

            View box = new View(this);
            LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(24, 24);
            boxParams.rightMargin = 12;
            box.setLayoutParams(boxParams);
            box.setBackgroundColor(TRIFAGlobals.APP_STATE.getColorForState(state));

            TextView label = new TextView(this);
            String name = TRIFAGlobals.APP_STATE.value_str(state).replace("STATE_", "").replace("_", " ");
            label.setText(name);
            label.setTextColor(Color.parseColor("#DDDDDD"));
            label.setTextSize(11f);

            row.addView(box);
            row.addView(label);
            legendContainer.addView(row);
        }

        FrameLayout.LayoutParams legendParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                             FrameLayout.LayoutParams.WRAP_CONTENT);
        legendParams.gravity = Gravity.BOTTOM | Gravity.START;
        legendParams.bottomMargin = dp(10);
        legendParams.leftMargin = dp(10);

        rootLayout.addView(legendContainer, legendParams);

        // 3. Export button (BOTTOM-RIGHT)
        FloatingActionButton fabExport = new FloatingActionButton(this);
        fabExport.setImageResource(R.drawable.ic_export);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            fabExport.setTooltipText(getString(R.string.stats_export_tooltip));
        }
        fabExport.setContentDescription(getString(R.string.stats_export_tooltip));
        fabExport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
        fabExport.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));

        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                          FrameLayout.LayoutParams.WRAP_CONTENT);
        fabParams.gravity = Gravity.BOTTOM | Gravity.END;
        fabParams.rightMargin = dp(16);
        fabParams.bottomMargin = dp(16);
        fabExport.setLayoutParams(fabParams);

        fabExport.setOnClickListener(v -> onExportClicked());
        fabExport.setOnLongClickListener(v -> {
            onChangeFolderClicked();
            return true;
        });

        rootLayout.addView(fabExport, fabParams);

        setContentView(rootLayout);
        scrollView.post(this::scroll_to_now);
    }

    private void onExportClicked()
    {
        if (exporting) return;
        exporting = true;

        // Ask exporter to ensure we have a directory.
        // The callback runs on the UI thread, which is perfect for View measurement/drawing.
        exporter.ensureDirectory(() -> {

            final Bitmap bmp;
            try
            {
                float density = getResources().getDisplayMetrics().density;
                int totalSpan = HistoryChartView.getTotalSpanMinutes();

                int w = (int) (totalSpan * EXPORT_DP_PER_MINUTE * density);
                float effDp = EXPORT_DP_PER_MINUTE;
                if (w > MAX_EXPORT_WIDTH_PX)
                {
                    effDp *= (float) MAX_EXPORT_WIDTH_PX / (float) w;
                    w = MAX_EXPORT_WIDTH_PX;
                }
                if (w < 1) w = 1;

                HistoryChartView exportView = new HistoryChartView(this);
                exportView.setDpPerMinute(effDp);

                // [ADDED] Enable export mode for sharp, full-width boxes
                exportView.isExporting = true;

                int contentH = exportView.getContentHeightPx();

                int wSpec = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
                int hSpec = View.MeasureSpec.makeMeasureSpec(contentH, View.MeasureSpec.EXACTLY);
                exportView.measure(wSpec, hSpec);
                exportView.layout(0, 0, w, contentH);

                bmp = Bitmap.createBitmap(w, contentH, Bitmap.Config.ARGB_8888);
                bmp.eraseColor(0xFF121212);
                Canvas c = new Canvas(bmp);
                exportView.draw(c);

                // [ADDED] Reset flag (good practice, even for a temporary view)
                exportView.isExporting = false;
            }
            catch (OutOfMemoryError oom)
            {
                exporting = false;
                toast("Export failed: not enough memory (try zooming out first)");
                return;
            }
            catch (Throwable t)
            {
                exporting = false;
                toast("Export failed: " + t.getMessage());
                return;
            }

            final String fileName = "trifa_stats_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";

            // Now do the actual I/O on a background thread
            new Thread(() -> {
                try
                {
                    Uri fileUri = exporter.createFileUri(fileName, "image/png");
                    if (fileUri == null)
                    {
                        postUi(() -> toast("Export failed: cannot create file"));
                        return;
                    }

                    try (OutputStream os = getContentResolver().openOutputStream(fileUri, "w"))
                    {
                        if (os == null)
                        {
                            postUi(() -> toast("Export failed: cannot open output stream"));
                            return;
                        }
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                    }

                    postUi(() -> toast("Saved: " + fileName));
                }
                catch (Throwable t)
                {
                    postUi(() -> toast("Export failed: " + t.getMessage()));
                }
                finally
                {
                    bmp.recycle(); // Free memory ASAP
                    exporting = false;
                }
            }, "stats-export").start();

        }, error -> {
            exporting = false;
            toast(error);
        });
    }

    private void onChangeFolderClicked()
    {
        exporter.changeDirectory(() -> {
            toast("Export folder updated");
        }, error -> toast(error));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // Let the exporter handle the SAF result. If it returns true, we are done.
        if (exporter.handleActivityResult(requestCode, resultCode, data))
        {
            return;
        }
        // Handle other activity results here if necessary
    }

    private void scrollXTo(int x)
    {
        programmaticScroll = true;   // onScrollChanged fires synchronously inside scrollTo()
        scrollView.scrollTo(x, 0);
        programmaticScroll = false;
    }

    private void scroll_to_now()
    {
        int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
        scrollXTo(maxScroll);
    }

    private int dp(int v)
    {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void postUi(Runnable r)
    {
        refreshHandler.post(r);
    }

    private void toast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Snap back to live "now" when the user returns to this screen
        userScrolledAway = false;
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }

    /**
     * HorizontalScrollView that physically refuses to intercept or scroll while
     * multiple fingers are on the screen. This kills the "fight" between the
     * native scroll fling/interception and the pinch-zoom gesture.
     */
    public static class LockableHorizontalScrollView extends HorizontalScrollView
    {
        public LockableHorizontalScrollView(Context context)
        {
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev)
        {
            // Never steal the gesture while pinching
            if (ev.getPointerCount() > 1) return false;
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {
            // While pinching, consume silently (no scrolling, no fling velocity buildup)
            if (ev.getPointerCount() > 1) return true;
            return super.onTouchEvent(ev);
        }
    }
}
