package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
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
    private static final float EXPORT_DP_PER_MINUTE = 3.0f;
    private static final int MAX_EXPORT_WIDTH_PX = 30000; // hard safety clamp

    private HorizontalScrollView scrollView;
    private HistoryChartView chartView;

    // Tracks if the user has scrolled away from the live "now" (right) edge
    private boolean userScrolledAway = false;
    private long targetTimestamp = 0; // The exact historical time the user is looking at
    private boolean programmaticScroll = false;

    private volatile boolean exporting = false;

    // Our new reusable exporter instance
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
                else
                {
                    // Keep the viewport glued to targetTimestamp: as the 24h window rolls,
                    // that timestamp's x shrinks by minuteWidthPx/12 per 5s tick; follow it exactly.
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

        scrollView = new HorizontalScrollView(this);
        scrollView.setBackgroundColor(0xFF121212);

        chartView = new HistoryChartView(this);

        // Chart width is WRAP_CONTENT (dictated by zoom), height MATCH_PARENT so the
        // whole screen receives scroll / pinch gestures.
        scrollView.addView(chartView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                   FrameLayout.LayoutParams.MATCH_PARENT));

        // Track if the user scrolls away from the live "now" edge
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (programmaticScroll || chartView == null) return; // our own corrections: don't re-record
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
            public boolean onInterceptTouchEvent(android.view.MotionEvent ev) { return false; }
            @Override
            public boolean onTouchEvent(android.view.MotionEvent event) { return false; }
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
                int contentH = exportView.getContentHeightPx();

                int wSpec = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
                int hSpec = View.MeasureSpec.makeMeasureSpec(contentH, View.MeasureSpec.EXACTLY);
                exportView.measure(wSpec, hSpec);
                exportView.layout(0, 0, w, contentH);

                bmp = Bitmap.createBitmap(w, contentH, Bitmap.Config.ARGB_8888);
                bmp.eraseColor(0xFF121212);
                Canvas c = new Canvas(bmp);
                exportView.draw(c);
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
        if (exporter.handleActivityResult(requestCode, resultCode, data)) {
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
}
