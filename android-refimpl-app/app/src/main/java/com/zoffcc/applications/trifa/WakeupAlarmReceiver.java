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
import android.os.PowerManager;
import android.util.Log;

import static com.zoffcc.applications.trifa.HelperGeneric.append_logger_msg;
import static com.zoffcc.applications.trifa.HelperGeneric.trigger_proper_wakeup_outside_tox_service_thread;
import static com.zoffcc.applications.trifa.TrifaToxService.trifa_service_thread;

public class WakeupAlarmReceiver extends BroadcastReceiver
{
    private static final String TAG = "trifa.WakeupAlrmRcvr";
    private static PowerManager.WakeLock wakeup_wakelock;

    @Override
    public void onReceive(Context context, Intent intent2)
    {
        if (wakeup_wakelock == null)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeup_wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "trifa:trifa_wakeup_lock");
        }

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                if (!wakeup_wakelock.isHeld())
                {
                    wakeup_wakelock.acquire();
                    Log.i(TAG, "acquiring wakelock");
                    TrifaToxService.write_debug_file("AlarmReceiver_aq_wakelock");
                }

                try
                {
                    Log.i(TAG, "AlarmReceiver:" + "onReceive");
                    TrifaToxService.write_debug_file("AlarmReceiver_onReceive");

                    if (trifa_service_thread != null)
                    {
                        append_logger_msg(TAG + "::" + "need_wakeup_now trigger 004");
                        trigger_proper_wakeup_outside_tox_service_thread();
                        TrifaToxService.write_debug_file("AlarmReceiver_interrupt");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(20 * 1000); // keep wakelock for 20 seconds
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (wakeup_wakelock.isHeld())
                {
                    Log.i(TAG, "releasing wakelock");
                    TrifaToxService.write_debug_file("AlarmReceiver_rl_wakelock");
                    wakeup_wakelock.release();
                }
                wakeup_wakelock = null;
            }
        };
        t.start();

    }
}
