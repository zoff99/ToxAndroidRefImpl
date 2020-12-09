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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF__DB_secrect_key__user_hash;
import static com.zoffcc.applications.trifa.TrifaToxService.vfs;


public class StartMainActivityWrapper extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivityWrpr";
    boolean set_pattern = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // need this for "dp2px" to work !! -------
        // need this for "dp2px" to work !! -------
        MainActivity.resources = this.getResources();
        MainActivity.metrics = MainActivity.resources.getDisplayMetrics();
        // need this for "dp2px" to work !! -------
        // need this for "dp2px" to work !! -------

        try
        {
            Bundle extras = getIntent().getExtras();
            Log.i(TAG, "change_msg_notification:extras=" + extras + " i=" + getIntent());
            String should_clear_notification_list = getIntent().getStringExtra("CLEAR_NEW_MESSAGE_NOTIFICATION");
            Log.i(TAG, "change_msg_notification:extra:" + should_clear_notification_list);
            if (should_clear_notification_list != null)
            {
                if (should_clear_notification_list.equalsIgnoreCase("1"))
                {
                    change_msg_notification(NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST.value, "");
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "change_msg_notification:EE01:" + e.getMessage());
        }

        boolean already_mounted = false;

        /* if the database is already mounted, then it means its already unlocked */
        try
        {
            if (vfs.isMounted())
            {
                already_mounted = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (already_mounted)
        {
            /* skip the password enter screen
             * jump directly to MainActivity and try to mount/use the Database
             */
            Intent pattern = new Intent(this, MainActivity.class);
            startActivity(pattern);
            finish();
        }
        else
        {
            /* now we need to figure out if this is the first time the user starts this app
             */
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            boolean pw_set_screen_done = settings.getBoolean("PW_SET_SCREEN_DONE", false);
            set_pattern = !pw_set_screen_done;

            if (set_pattern)
            {
                /* this is the first time the user starts this app
                 * ask the user to enter a password or skip
                 */
                Intent pattern = new Intent(this, SetPasswordActivity.class);
                startActivity(pattern);
                finish();
            }
            else
            {
                /* the user has already started the app and set a password
                 * now prompt to enter the password
                 */
                Intent pattern = new Intent(this, CheckPasswordActivity.class);
                startActivity(pattern);
                finish();
            }
        }
    }
}

