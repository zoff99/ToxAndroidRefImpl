package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;

public class HistoryChartActivity extends AppCompatActivity
{
    private HorizontalScrollView scrollView;
    private HistoryChartView chartView;
    private boolean user_took_over = false;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (chartView != null)
            {
                chartView.invalidate();
                if (!user_took_over) scroll_to_now();
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
            getSupportActionBar().setTitle("24h App State Holter");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 1. Root FrameLayout to allow overlaying the Legend
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF121212);

        scrollView = new HorizontalScrollView(this);
        scrollView.setBackgroundColor(0xFF121212);

        chartView = new HistoryChartView(this);

        // [FIXED] Chart width is WRAP_CONTENT (dictated by zoom), but height is MATCH_PARENT
        // so it fills the screen and receives touches everywhere.
        scrollView.addView(chartView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) user_took_over = true;
            return false;
        });

        rootLayout.addView(scrollView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 2. Legend Overlay (Touch-Transparent)
        // We use a custom LinearLayout that overrides touch methods to return false.
        // This makes it completely "click-through", allowing pinch-to-zoom and scrolling
        // on the chart underneath, even if the user's fingers are on the legend.
        LinearLayout legendContainer = new LinearLayout(this) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return false; // Pass all touches through to the chart
            }
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return false; // Pass all touches through to the chart
            }
        };

        legendContainer.setOrientation(LinearLayout.VERTICAL);
        legendContainer.setBackgroundColor(0xDD121212); // Semi-transparent dark background
        legendContainer.setPadding(15, 15, 15, 15);

        // Dynamically build the legend sorted strictly by enum value (Descending: Highest value = Top Lane)
        TRIFAGlobals.APP_STATE[] allStates = TRIFAGlobals.APP_STATE.values();
        java.util.Arrays.sort(allStates, (a, b) -> Integer.compare(b.value, a.value));

        int[] statesToShow = new int[allStates.length];
        for (int i = 0; i < allStates.length; i++) {
            statesToShow[i] = allStates[i].value;
        }

        for (int state : statesToShow) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 4, 0, 4);

            View box = new View(this);
            LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(24, 24);
            boxParams.rightMargin = 12;
            box.setLayoutParams(boxParams);

            // Fetch color directly from the Enum based on priority value
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

        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        scrollParams.gravity = Gravity.BOTTOM | Gravity.START;
        scrollParams.bottomMargin = 40;
        scrollParams.leftMargin = 20;

        // Add the touch-transparent legend directly to the root layout (no ScrollView wrapper)
        rootLayout.addView(legendContainer, scrollParams);

        setContentView(rootLayout);
        scrollView.post(this::scroll_to_now);
    }

    private void scroll_to_now()
    {
        int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
        scrollView.scrollTo(maxScroll, 0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        user_took_over = false;
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
