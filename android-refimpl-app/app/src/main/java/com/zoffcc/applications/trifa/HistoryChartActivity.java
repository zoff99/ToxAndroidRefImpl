package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.UriPermission;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class HistoryChartActivity extends AppCompatActivity
{
    private static final int REQ_PICK_TREE = 0x51;
    private static final String PREFS_NAME = "trifa_holter_prefs";
    private static final String PREF_TREE_URI = "export_tree_uri";

    // Fixed horizontal resolution for the EXPORT image (dp per minute), independent of
    // the live pinch zoom. 3.0f * density(3) ~= 9 px per minute square -> a ~13000 px
    // wide PNG that is far sharper than any screenshot, yet small enough to never OOM.
    private static final float EXPORT_DP_PER_MINUTE = 3.0f;
    private static final int MAX_EXPORT_WIDTH_PX = 30000; // hard safety clamp

    private HorizontalScrollView scrollView;
    private HistoryChartView chartView;
    private boolean user_took_over = false;

    private Uri treeUri = null;          // currently usable, persisted export folder
    private boolean pendingExport = false; // set when we open the picker *for* an export
    private volatile boolean exporting = false;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (chartView != null)
            {
                chartView.invalidate();
                if (!user_took_over)
                {
                    scroll_to_now();
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
            getSupportActionBar().setTitle("24h App State Holter");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // restore a previously chosen export folder, if we still hold its permission
        treeUri = loadSavedTreeUri();

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

        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                user_took_over = true;
            }
            return false;
        });

        rootLayout.addView(scrollView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                    FrameLayout.LayoutParams.MATCH_PARENT));

        // 2. Legend Overlay (touch-transparent) -- kept at BOTTOM-LEFT (original position).
        //    The export button lives at BOTTOM-RIGHT, so the two never collide.
        LinearLayout legendContainer = new LinearLayout(this)
        {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev)
            {
                return false;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event)
            {
                return false;
            }
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
        legendParams.gravity = Gravity.BOTTOM | Gravity.START;   // <-- bottom-left (original)
        legendParams.bottomMargin = dp(10);
        legendParams.leftMargin = dp(10);

        rootLayout.addView(legendContainer, legendParams);

        // 3. Export button (BOTTOM-RIGHT): tap = save PNG to the chosen folder,
        //    long-press = choose a different folder.
        FloatingActionButton fabExport = new FloatingActionButton(this);
        fabExport.setImageResource(R.drawable.ic_export);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            fabExport.setTooltipText(getString(R.string.holter_export_tooltip));
        }
        fabExport.setContentDescription(getString(R.string.holter_export_tooltip));
        fabExport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
        fabExport.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));

        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                          FrameLayout.LayoutParams.WRAP_CONTENT);
        fabParams.gravity = Gravity.BOTTOM | Gravity.END;        // <-- bottom-right
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

    // ------------------------------------------------------------------ export flow

    private void onExportClicked()
    {
        if (exporting)
        {
            return;
        }
        if (hasUsableTree(treeUri))
        {
            doExport(treeUri);
        }
        else
        {
            pendingExport = true;     // after the user picks, export immediately
            pickFolder();
        }
    }

    private void onChangeFolderClicked()
    {
        pendingExport = true;         // re-pick, then export to the new folder
        pickFolder();
    }

    private void pickFolder()
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQ_PICK_TREE);
        }
        catch (Exception e)
        {
            pendingExport = false;
            toast("Could not open folder picker: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_TREE)
        {
            return;
        }

        if (resultCode != Activity.RESULT_OK || data == null)
        {
            pendingExport = false;
            return;
        }

        Uri picked = data.getData();
        if (picked == null)
        {
            pendingExport = false;
            return;
        }

        // take a persistable read+write permission and remember it
        try
        {
            getContentResolver().takePersistableUriPermission(picked, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                                                      Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        catch (Exception ignored)
        {
        }

        treeUri = picked;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_TREE_URI, picked.toString()).apply();

        if (pendingExport)
        {
            pendingExport = false;
            if (hasUsableTree(treeUri))
            {
                doExport(treeUri);
            }
            else
            {
                toast("Selected folder is not writable");
            }
        }
    }

    /**
     * Render the WHOLE 24h chart at a fixed high resolution into an off-screen view,
     * then compress + write the PNG on a background thread (keeps the UI thread free).
     */
    private void doExport(final Uri tree)
    {
        if (exporting)
        {
            return;
        }
        exporting = true;

        final Bitmap bmp;
        try
        {
            float density = getResources().getDisplayMetrics().density;
            int totalSpan = HistoryChartView.getTotalSpanMinutes();

            int w = (int) (totalSpan * EXPORT_DP_PER_MINUTE * density);
            float effDp = EXPORT_DP_PER_MINUTE;
            if (w > MAX_EXPORT_WIDTH_PX)            // safety clamp on ultra-high-density devices
            {
                effDp *= (float) MAX_EXPORT_WIDTH_PX / (float) w;
                w = MAX_EXPORT_WIDTH_PX;
            }
            if (w < 1)
            {
                w = 1;
            }

            // throw-away view: NOT attached to the window, does not disturb the live chart
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
            exportView.draw(c);                     // full timeline, off-screen pixels included
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

        final String fileName =
                "trifa_holter_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";

        new Thread(() -> {
            try
            {
                // Pass your context and the Tree URI directly into DocumentFile
                DocumentFile directoryFile = DocumentFile.fromTreeUri(this, tree);

                // Create the file safely under the directory hierarchy
                DocumentFile newFile = directoryFile.createFile("image/png", fileName);
                try (OutputStream os = getContentResolver().openOutputStream(newFile.getUri(), "w"))
                {
                    if (os == null)
                    {
                        postUi("Export failed: cannot open output stream");
                        return;
                    }
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                }
                final String shown = fileName;
                postUi(() -> toast("Saved: " + shown));
            }
            catch (Throwable t)
            {
                final String msg = t.getMessage();
                postUi(() -> toast("Export failed: " + msg));
            }
            finally
            {
                bmp.recycle();   // huge temporary bitmap -> free it ASAP
                exporting = false;
            }
        }, "holter-export").start();
    }

    // ------------------------------------------------------------------ folder persistence

    private Uri loadSavedTreeUri()
    {
        String s = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_TREE_URI, null);
        if (s == null)
        {
            return null;
        }
        Uri u;
        try
        {
            u = Uri.parse(s);
        }
        catch (Exception e)
        {
            return null;
        }
        if (hasUsableTree(u))
        {
            return u;
        }
        // permission gone (folder removed / app updated) -> drop the stale entry
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(PREF_TREE_URI).apply();
        return null;
    }

    private boolean hasUsableTree(Uri u)
    {
        if (u == null)
        {
            return false;
        }
        try
        {
            List<UriPermission> perms = getContentResolver().getPersistedUriPermissions();
            if (perms == null)
            {
                return false;
            }
            for (UriPermission p : perms)
            {
                if (p.getUri().equals(u) && p.isWritePermission())
                {
                    return true;
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return false;
    }

    // ------------------------------------------------------------------ misc helpers

    private void scroll_to_now()
    {
        int maxScroll = Math.max(0, chartView.getWidth() - scrollView.getWidth());
        scrollView.scrollTo(maxScroll, 0);
    }

    private int dp(int v)
    {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void postUi(final String msg)
    {
        postUi(() -> toast(msg));
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
