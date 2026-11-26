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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ScrollView;

/**
 * a vertical ScrollView that also understands pinch zoom.
 *
 * the pinch gestures are detected with a ScaleGestureDetector that is fed
 * every touch event in dispatchTouchEvent() (so it sees the events even
 * when a child view consumes them).
 *
 * IMPORTANT touch stream rule (this is what makes it crash-proof):
 * ScrollView keeps internal pointer bookkeeping that is only correct when
 * it sees EVERY event of a touch stream starting from ACTION_DOWN. as soon
 * as a pinch takes over a stream ("hijacks" it), that bookkeeping becomes
 * stale, because we consume events that ScrollView never sees. therefore a
 * hijacked stream is consumed COMPLETELY by this class until it ends --
 * ScrollView is never fed events from the middle of a stream. only the
 * next fresh ACTION_DOWN starts a new stream and re-enables the normal
 * ScrollView scrolling. (feeding ScrollView mid-stream caused
 * "Invalid pointerId=-1" crashes on pinch release.)
 *
 * normal one-finger scrolling is completely unchanged. the zoom action
 * itself is supplied by the caller via init_pinch_zoom().
 */
public class PinchZoomScrollView extends ScrollView
{
    private static final String TAG = "trifa.PinchZoomScroll";

    private ScaleGestureDetector scale_detector = null;

    // true once a pinch has taken over the current touch stream. while set,
    // this class consumes every event itself and never passes anything to
    // ScrollView, until the next fresh ACTION_DOWN resets it.
    private boolean pinch_hijacked_stream = false;

    public PinchZoomScrollView(Context context)
    {
        super(context);
    }

    public PinchZoomScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PinchZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    // the caller supplies what should happen while the user pinches
    public void init_pinch_zoom(ScaleGestureDetector.OnScaleGestureListener listener)
    {
        scale_detector = new ScaleGestureDetector(getContext(), listener);
    }

    private boolean is_pinching()
    {
        return (scale_detector != null) && (scale_detector.isInProgress());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        try
        {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN)
            {
                // a fresh touch stream starts: ScrollView may own it again
                pinch_hijacked_stream = false;
            }
            if (scale_detector != null)
            {
                scale_detector.onTouchEvent(ev);
            }
            return super.dispatchTouchEvent(ev);
        }
        catch (Exception e)
        {
            Log.i(TAG, "dispatchTouchEvent:" + e.getMessage());
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        try
        {
            if (is_pinching())
            {
                // pinch in progress: take the touch stream away from the
                // children (they receive a proper CANCEL)
                pinch_hijacked_stream = true;
                return true;
            }
            if (pinch_hijacked_stream)
            {
                // keep owning the rest of this hijacked stream
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        }
        catch (Exception e)
        {
            Log.i(TAG, "onInterceptTouchEvent:" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        try
        {
            if (is_pinching())
            {
                // consume the pinch without scrolling
                pinch_hijacked_stream = true;
                return true;
            }
            if (pinch_hijacked_stream)
            {
                // ScrollView's pointer bookkeeping is stale for this stream:
                // consume everything until the next fresh ACTION_DOWN,
                // never feed ScrollView from the middle of a stream
                return true;
            }
            return super.onTouchEvent(ev);
        }
        catch (Exception e)
        {
            // should never happen: consume the rest of this stream so the
            // next fresh ACTION_DOWN can start over cleanly
            Log.i(TAG, "onTouchEvent:" + e.getMessage());
            pinch_hijacked_stream = true;
            return true;
        }
    }
}
