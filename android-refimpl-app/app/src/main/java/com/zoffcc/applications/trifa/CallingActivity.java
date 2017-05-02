package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;

public class CallingActivity extends AppCompatActivity
{
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    ImageButton accept_button = null;
    ImageButton decline_button = null;
    TextView top_text_line = null;
    static CallingActivity ca = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calling);

        ca = this;

        mVisible = true;
        mContentView = findViewById(R.id.video_view);

        top_text_line = (TextView) findViewById(R.id.top_text_line);
        accept_button = (ImageButton) findViewById(R.id.accept_button);
        decline_button = (ImageButton) findViewById(R.id.decline_button);

        top_text_line.setText(Callstate.friend_name);

        accept_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    toxav_answer(Callstate.friend_number, 10, 10);
                    Callstate.call_start_timestamp = System.currentTimeMillis();
                    String a = "" + (int) ((Callstate.call_start_timestamp - Callstate.call_init_timestamp) / 1000) + "s";
                    top_text_line.setText(Callstate.friend_name + " : " + a);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });

        decline_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    toxav_call_control(Callstate.friend_number, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
                    close_calling_activity();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });

    }

    public static void close_calling_activity()
    {
        Callstate.reset_values();
        // close calling activity --------
        ca.finish();
        // close calling activity --------
    }

    @Override
    public void onBackPressed()
    {
        // dont let the user use the back button to close the activity
    }


    private final Runnable mHidePart2Runnable = new Runnable()
    {
        @SuppressLint("InlinedApi")
        @Override
        public void run()
        {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable()
    {
        @Override
        public void run()
        {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            hide();
        }
    };
    //    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
    //    {
    //        @Override
    //        public boolean onTouch(View view, MotionEvent motionEvent)
    //        {
    //            if (AUTO_HIDE)
    //            {
    //                delayedHide(AUTO_HIDE_DELAY_MILLIS);
    //            }
    //            return false;
    //        }
    //    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle()
    {
        if (mVisible)
        {
            hide();
        }
        else
        {
            show();
        }
    }

    private void hide()
    {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show()
    {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
