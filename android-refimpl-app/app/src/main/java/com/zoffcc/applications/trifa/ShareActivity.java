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

import android.app.SearchManager;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class ShareActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.ShareActivity";

    TextView t1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        t1 = (TextView) findViewById(R.id.text1);
        t1.setText("Share Content ...");

        Log.i(TAG, "onCreate");

        Intent intent = getIntent();
        Log.i(TAG, "onCreate:intent=" + intent);

        try
        {
            if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            {
                String query = intent.getStringExtra(SearchManager.QUERY);
                Log.i(TAG, "onCreate:query=" + query);
            }
            if (Intent.ACTION_VIEW.equals(intent.getAction()))
            {
                ClipData cdata = intent.getClipData();
                Log.i(TAG, "onCreate:cdata=" + cdata);
                if (cdata != null)
                {
                    int item_count = cdata.getItemCount();
                    Log.i(TAG, "onCreate:item_count=" + item_count);
                    Log.i(TAG, "onCreate:getDescription=" + cdata.getDescription());
                }

                Uri data = intent.getData();
                Log.i(TAG, "onCreate:data=" + data);
                String dataString = intent.getDataString();
                Log.i(TAG, "onCreate:dataString=" + dataString);
                String shareWith = dataString.substring(dataString.lastIndexOf('/') + 1);
                Log.i(TAG, "onCreate:shareWith=" + shareWith);

                // handle tox:......... URL
                if ((dataString != null) && (dataString.length() > 5) && (dataString.startsWith("tox:")))
                {
                    t1.setText("ToxID:" + dataString.substring(4));

                    // TODO:
                    // check if app is running and unlocked -> otherwise start and unlock it
                    // then open "add" screen and fill in this ToxID
                }
            }
            else
            {
                ClipData cdata = intent.getClipData();
                Log.i(TAG, "onCreate:cdata=" + cdata);
                if (cdata != null)
                {
                    int item_count = cdata.getItemCount();
                    Log.i(TAG, "onCreate:item_count=" + item_count);
                    Log.i(TAG, "onCreate:getDescription=" + cdata.getDescription());
                }

                Uri data = intent.getData();
                Log.i(TAG, "onCreate:data=" + data);
                String dataString = intent.getDataString();
                Log.i(TAG, "onCreate:dataString=" + dataString);
                String shareWith = dataString.substring(dataString.lastIndexOf('/') + 1);
                Log.i(TAG, "onCreate:shareWith=" + shareWith);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE:" + e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.i(TAG, "onNewIntent:intent=" + intent);
    }
}
