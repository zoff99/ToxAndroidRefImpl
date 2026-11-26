/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * shows a single text message full screen for comfortable reading of long
 * and/or formatted messages. plain native views only, no WebView, no HTML:
 * - the message is a plain TextView with a fixed width (monospace) font;
 *   whitespace and line breaks are preserved by the TextView itself
 * - pinch zoom in/out changes the text size (between the limits below);
 *   the zoom is centered on the midpoint between the two fingers, so the
 *   text point under the fingers stays under the fingers
 * - vertical scrolling (PinchZoomScrollView) and horizontal scrolling
 *   (HorizontalScrollView) for very long or wide messages
 * - the text is selectable, so parts of it can be copied
 *
 * the message text arrives via the "message_text" intent extra.
 */
public class MessageFullscreenActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MsgFullscreenActy";

    // monospace text size of the reader: start value and pinch zoom limits (sp)
    private static final float FULLSCREEN_TEXT_SIZE_SP = 16f;
    private static final float FULLSCREEN_TEXT_SIZE_MIN_SP = 8f;
    private static final float FULLSCREEN_TEXT_SIZE_MAX_SP = 64f;

    private TextView message_text_view = null;
    private float current_text_size_sp = FULLSCREEN_TEXT_SIZE_SP;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_fullscreen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.message_fullscreen_activity_name);
        }

        message_text_view = (TextView) findViewById(R.id.message_fullscreen_text);
        try
        {
            String message_text = getIntent().getStringExtra("message_text");
            if (message_text == null)
            {
                message_text = "";
            }
            // plain text, exactly as received: no parsing, no escaping needed
            message_text_view.setText(message_text);
            message_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, current_text_size_sp);

            PinchZoomScrollView scroll_view = (PinchZoomScrollView) findViewById(R.id.message_fullscreen_scroll);
            final HorizontalScrollView h_scroll = (HorizontalScrollView) findViewById(R.id.message_fullscreen_hscroll);
            scroll_view.init_pinch_zoom(new ScaleGestureDetector.SimpleOnScaleGestureListener()
            {
                @Override
                public boolean onScale(ScaleGestureDetector detector)
                {
                    try
                    {
                        // grow/shrink the text with the pinch, clamped to the limits
                        float new_size_sp = current_text_size_sp * detector.getScaleFactor();
                        if (new_size_sp < FULLSCREEN_TEXT_SIZE_MIN_SP)
                        {
                            new_size_sp = FULLSCREEN_TEXT_SIZE_MIN_SP;
                        }
                        if (new_size_sp > FULLSCREEN_TEXT_SIZE_MAX_SP)
                        {
                            new_size_sp = FULLSCREEN_TEXT_SIZE_MAX_SP;
                        }
                        if (new_size_sp == current_text_size_sp)
                        {
                            return true; // already at a zoom limit, nothing to do
                        }

                        // focal point zoom: remember the content point that is
                        // currently between the fingers, so we can keep it there.
                        // the focus is in the outer scroll view's coordinates.
                        final float focus_x = detector.getFocusX();
                        final float focus_y = detector.getFocusY();
                        final float content_x = focus_x + h_scroll.getScrollX();
                        final float content_y = focus_y + scroll_view.getScrollY();
                        final float old_width = message_text_view.getWidth();
                        final float old_height = message_text_view.getHeight();

                        current_text_size_sp = new_size_sp;
                        message_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, new_size_sp);

                        // the text size change triggers a relayout. right after it
                        // (before the frame is drawn, so there is no flicker) scroll
                        // both axes so the focal content point stays under the fingers.
                        scroll_view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
                        {
                            @Override
                            public boolean onPreDraw()
                            {
                                scroll_view.getViewTreeObserver().removeOnPreDrawListener(this);
                                try
                                {
                                    // how much the content actually grew on each axis
                                    float scale_x = 1f;
                                    float scale_y = 1f;
                                    if (old_width > 0)
                                    {
                                        scale_x = message_text_view.getWidth() / old_width;
                                    }
                                    if (old_height > 0)
                                    {
                                        scale_y = message_text_view.getHeight() / old_height;
                                    }
                                    int target_x = Math.round((content_x * scale_x) - focus_x);
                                    int target_y = Math.round((content_y * scale_y) - focus_y);
                                    // both scroll views clamp to their valid range
                                    h_scroll.scrollTo(target_x, 0);
                                    scroll_view.scrollTo(0, target_y);
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "onPreDraw:" + e.getMessage());
                                }
                                return true;
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "onScale:" + e.getMessage());
                    }
                    return true;
                }
            });
        }
        catch (Exception e)
        {
            Log.i(TAG, "onCreate:" + e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }
}
