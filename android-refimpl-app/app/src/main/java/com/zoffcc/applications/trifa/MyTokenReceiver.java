/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2019 Zoff <zoff@zoff.cc>
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_TOKEN_DB_KEY;
import static com.zoffcc.applications.trifa.TrifaToxService.trifa_service_thread;

public class MyTokenReceiver extends BroadcastReceiver
{
    private static final String TAG = "trifa.MyTokenRcvr";
    private static PowerManager.WakeLock token_wakeup_lock = null;

    @Override
    public void onReceive(Context context, Intent intent2)
    {
        Log.i(TAG, "got intent: " + intent2);
        //Log.i(TAG, "caller = " + intent2.getCallingActivity().getClassName());
        try
        {
            if (token_wakeup_lock == null)
            {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                token_wakeup_lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "trifa:trifa_token_wakeup_lock");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!token_wakeup_lock.isHeld())
                    {
                        token_wakeup_lock.acquire();
                        Log.i(TAG, "acquiring wakelock");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Bundle extras = intent2.getExtras();
                    if (extras == null)
                    {
                        Log.i(TAG, "couldn't get the token");
                    }
                    else
                    {
                        String tokenReceived = extras.getString("token");
                        if (tokenReceived != null)
                        {
                            TRIFAGlobals.global_notification_token = tokenReceived;
                            Log.i(TAG, "token received: " + "xxxxxxxxxxxxx");
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Log.i(TAG, "MyTokenReceiver:" + "onReceive");

                    if (trifa_service_thread != null)
                    {
                        TrifaToxService.need_wakeup_now = true;
                        trifa_service_thread.interrupt();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    set_g_opts(NOTIFICATION_TOKEN_DB_KEY, TRIFAGlobals.global_notification_token);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(10 * 1000); // keep wakelock for 10 seconds
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (token_wakeup_lock.isHeld())
                    {
                        Log.i(TAG, "releasing wakelock");
                        token_wakeup_lock.release();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                token_wakeup_lock = null;
            }
        };
        t.start();

    }
}
