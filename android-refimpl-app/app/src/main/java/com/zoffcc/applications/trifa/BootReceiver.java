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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver
{
    private static final String TAG = "trifa.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            Log.i(TAG, "-- ON_BOOT -- :" + intent.getAction());

            try
            {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                boolean start_on_boot = settings.getBoolean("start_on_boot", false);

                if (start_on_boot)
                {
                    // TODO: this is not working anymore starting with Android 10
                    // https://developer.android.com/guide/components/activities/background-starts
                    // thanks Google

                    Log.i(TAG, "-- ON_BOOT -- :starting trifa ...");
                    Intent i = new Intent(context, StartMainActivityWrapper.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    Log.i(TAG, "-- ON_BOOT -- :activity started");
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "-- ON_BOOT -- :EE01:" + e.getMessage());
            }
        }
    }
}
