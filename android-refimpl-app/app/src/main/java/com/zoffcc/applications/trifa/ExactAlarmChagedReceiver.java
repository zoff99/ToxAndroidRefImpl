/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2020 Zoff <zoff@zoff.cc>
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

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import static android.content.Context.ALARM_SERVICE;

public class ExactAlarmChagedReceiver extends BroadcastReceiver
{
    private static final String TAG = "trifa.ExAlrmRecvr";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "-- ON_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGE -- :000:" + intent.getAction());
        if (intent.getAction().equals("android.intent.action.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"))
        {
            Log.i(TAG, "-- ON_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGE -- :" + intent.getAction());
            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                {
                    Log.i(TAG, "-- ON_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGE -- :001");
                    AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
                    if (alarmManager.canScheduleExactAlarms())
                    {
                        Log.i(TAG, "canScheduleExactAlarms:true");
                    }
                    else
                    {
                        Log.i(TAG, "canScheduleExactAlarms:**FALSE**");
                    }
                }
                Log.i(TAG, "-- ON_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGE -- :099");
            }
            catch (Exception e)
            {
                Log.i(TAG, "-- ON_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGE -- :EE01:" + e.getMessage());
            }
        }
    }
}
