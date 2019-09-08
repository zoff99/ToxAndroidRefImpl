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

import static com.zoffcc.applications.trifa.TrifaToxService.trifa_service_thread;

public class WakeupAlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent2)
    {
        System.out.println("AlarmReceiver:" + "onReceive");
        TrifaToxService.write_debug_file("AlarmReceiver_onReceive");
        if (trifa_service_thread != null)
        {
            trifa_service_thread.interrupt();
            TrifaToxService.write_debug_file("AlarmReceiver_interrupt");
        }
    }
}
