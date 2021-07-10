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
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.send_text_messge;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;

public class CallingWaitingActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CallWtActivity";

    String calling_friend_pk = null;
    static boolean running = false;
    static boolean got_online = false;
    static Thread CallWThread = null;

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

        try
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                calling_friend_pk = extras.getString("calling_friend_pk", null);
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
                                            Log.i(TAG,"send_text_messge:calling you");
                                            sent_ping_message = true;
                                        }
                                    }
                                }
                            }
                            Thread.sleep(100);
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                    finish_me();
                }
            };

            Log.i(TAG, "onCreate:thread:2");
            CallWThread.start();
            Log.i(TAG, "onCreate:thread:3");
        }
        else
        {
            stop_me();
            finish_me();
        }

        Log.i(TAG, "onCreate:99");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
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
