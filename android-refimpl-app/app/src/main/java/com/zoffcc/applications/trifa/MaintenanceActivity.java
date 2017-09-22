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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MaintenanceActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MaintActy";

    Button button_clear_glide_cache;
    Button button_sql_vacuum;
    Button button_sql_analyze;
    Button button_fav_emoji_reset;
    Handler maint_handler_s = null;

    // ----------------------------------------------------
    // TODO: this is copied over from:
    //       https://github.com/vanniktech/Emoji/blob/master/emoji/src/main/java/com/vanniktech/emoji/RecentEmojiManager.java
    //
    private static final String PREFERENCE_NAME = "emoji-recent-manager";
    // private static final String TIME_DELIMITER = ";";
    // private static final String EMOJI_DELIMITER = "~";
    private static final String RECENT_EMOJIS = "recent-emojis";
    //
    // ----------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        button_clear_glide_cache = (Button) findViewById(R.id.button_clear_glide_cache);
        button_sql_vacuum = (Button) findViewById(R.id.button_sql_vacuum);
        button_sql_analyze = (Button) findViewById(R.id.button_sql_analyze);
        button_fav_emoji_reset = (Button) findViewById(R.id.button_fav_emoji_reset);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button_clear_glide_cache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    MainActivity.clearCache_s();
                    // Toast.makeText(v.getContext(), "cleared Glide Cache", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_sql_vacuum.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    final Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Log.i(TAG, "VACUUM:start");
                                Cursor cursor = orma.getConnection().rawQuery("VACUUM");
                                cursor.moveToFirst();
                                cursor.close();
                                Log.i(TAG, "VACUUM:ready");
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                                Log.i(TAG, "VACUUM:EE:" + e2.getMessage());
                            }
                        }
                    };
                    t2.start();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });


        button_sql_analyze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    final Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Log.i(TAG, "ANALYZE:start");
                                Cursor cursor = orma.getConnection().rawQuery("ANALYZE");
                                cursor.moveToFirst();
                                cursor.close();
                                Log.i(TAG, "ANALYZE:ready");
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                                Log.i(TAG, "ANALYZE:EE:" + e2.getMessage());
                            }
                        }
                    };
                    t2.start();

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_fav_emoji_reset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).
                            edit().putString(RECENT_EMOJIS, "").commit();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        maint_handler_s = maint_handler;
    }

    Handler maint_handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int id = msg.what;
        }
    };
}
