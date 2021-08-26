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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.appcompat.app.AppCompatActivity;

import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.send_text_messge;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;

public class CallingWaitingActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CallWtActivity";

    String calling_friend_pk = null;
    static boolean running = false;
    static boolean got_online = false;
    static Thread CallWThread = null;
    ImageButton decline_waiting_button = null;
    TextView call_waiting_friend_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate:01");

        calling_friend_pk = null;
        got_online = false;

        try
        {
            GroupAudioService.stop_me();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calling_waiting);

        if (PREF__window_security)
        {
            initializeScreenshotSecurity(this);
        }

        decline_waiting_button = findViewById(R.id.decline_waiting_button);
        call_waiting_friend_name = findViewById(R.id.call_waiting_friend_name);
        call_waiting_friend_name.setText("");

        final Drawable d3 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(
                Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
        decline_waiting_button.setImageDrawable(d3);

        decline_waiting_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i(TAG, "decline_button_pressed:000");

                try
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        Log.i(TAG, "decline_button_pressed:DOWN");
                        try
                        {
                            Log.i(TAG, "decline_button_pressed:stop_me()");
                            stop_me();
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return true;
            }
        });

        try
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                calling_friend_pk = extras.getString("calling_friend_pk", null);
                if (calling_friend_pk != null)
                {
                    final String resolve_name = HelperFriend.resolve_name_for_pubkey(calling_friend_pk, "");
                    if ((resolve_name.length() > 0) && (resolve_name.length() < 80))
                    {
                        call_waiting_friend_name.setText(resolve_name);
                    }
                }
            }
        }
        catch (Exception e)
        {
            calling_friend_pk = null;
        }

        if (calling_friend_pk != null)
        {
            running = true;
            CallWThread = new Thread()
            {
                @Override
                public void run()
                {
                    Log.i(TAG, "CallWThread:starting");

                    boolean sent_ping_message = false;
                    while (running)
                    {
                        try
                        {
                            if (is_friend_online_real(tox_friend_by_public_key__wrapper(calling_friend_pk)) != 0)
                            {
                                running = false;
                                got_online = true;
                            }
                            else
                            {
                                if (!sent_ping_message)
                                {
                                    final String relay_for_friend = get_relay_for_friend(calling_friend_pk);
                                    if (relay_for_friend != null)
                                    {
                                        if (is_friend_online_real(
                                                tox_friend_by_public_key__wrapper(relay_for_friend)) != 0)
                                        {
                                            send_text_messge(calling_friend_pk, "calling you now ...");
                                            Log.i(TAG, "send_text_messge:calling you");
                                            sent_ping_message = true;
                                        }
                                    }
                                    else // if friend is NOT online and does not have a relay, try if he has a push url
                                    {
                                        friend_call_push_url(calling_friend_pk);
                                        Log.i(TAG, "sent ping to push url");
                                        sent_ping_message = true;
                                    }
                                }
                            }
                            Thread.sleep(60);
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                    Log.i(TAG, "finish_me():002");
                    finish_me();
                }
            };

            Log.i(TAG, "onCreate:thread:2");
            CallWThread.start();
            Log.i(TAG, "onCreate:thread:3");
        }
        else
        {
            Log.i(TAG, "stop_me():002");
            stop_me();
            Log.i(TAG, "finish_me():003");
            finish_me();
        }

        Log.i(TAG, "onCreate:99");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, "stop_me():004");
        stop_me();
    }

    void finish_me()
    {
        Log.i(TAG, "finish_me:001");
        if (got_online)
        {
            Log.i(TAG, "finish_me:RESULT_OK");
            final Intent data = new Intent();
            data.putExtra("friendnum_pk", calling_friend_pk);
            setResult(Activity.RESULT_OK, data);
        }
        else
        {
            final Intent data = new Intent();
            data.putExtra("friendnum_pk", -1);
            Log.i(TAG, "finish_me:RESULT_CANCELED");
            setResult(Activity.RESULT_CANCELED, data);
        }
        finish();
    }

    public static void stop_me()
    {
        running = false;
        try
        {
            if (CallWThread != null)
            {
                CallWThread.join();
                CallWThread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
