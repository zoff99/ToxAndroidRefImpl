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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.CallingActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.main_activity_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.orma;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;

public class MessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MsgListActivity";
    long friendnum = -1;
    EditText ml_new_message = null;
    TextView ml_maintext = null;
    ImageView ml_icon = null;
    ImageButton ml_phone_icon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);

        setContentView(R.layout.activity_message_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ml_new_message = (EditText) findViewById(R.id.ml_new_message);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);

        ml_icon.setImageResource(R.drawable.circle_red);

        Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(20);
        ml_phone_icon.setImageDrawable(d1);

        final long fn = friendnum;
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final String f_name = MainActivity.get_friend_name_from_num(fn);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ml_maintext.setText(f_name);
                    }
                };
                main_handler_s.post(myRunnable);
            }
        };
        t.start();
    }

    long get_current_friendnum()
    {
        return friendnum;
    }

    public void send_message_onclick(View view)
    {
        String msg = "";
        try
        {
            // send typed message to friend
            msg = ml_new_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(), ml_new_message.getText().toString().length()));

            Message m = new Message();
            m.tox_friendnum = friendnum;
            m.direction = 1; // msg sent
            m.TOX_MESSAGE_TYPE = 0;
            m.rcvd_timestamp = 0L;
            m.sent_timestamp = System.currentTimeMillis();
            m.read = false;
            m.text = msg;

            long res = tox_friend_send_message(friendnum, 0, msg);

            if (res > -1)
            {
                insert_into_message_db(m, true);
                ml_new_message.setText("");
            }
            Log.i(TAG, "tox_friend_send_message:result=" + res);

        }
        catch (Exception e)
        {
            msg = "";
            e.printStackTrace();
        }
    }

    public void start_call_to_friend(View view)
    {
        Log.i(TAG,"start_call_to_friend");

        final long fn = friendnum;

        // these 2 bitrate values are very strange!! sometimes no video!!
        final int f_audio_enabled = 10;
        final int f_video_enabled = 10;

        MainActivity.toxav_call(friendnum, f_audio_enabled, f_video_enabled);
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Callstate.state == 0)
                    {
                        Log.i(TAG, "CALL:start:(2):show activity");
                        Callstate.state = 1;
                        Callstate.accepted_call = 1; // we started the call, so it's already accepted on our side
                        Callstate.call_first_video_frame_received = -1;
                        Callstate.call_start_timestamp = -1;
                        Intent intent = new Intent(context_s, CallingActivity.class);
                        Callstate.friend_number = fn;
                        try
                        {
                            Callstate.friend_name = orma.selectFromFriendList().tox_friendnumEq(Callstate.friend_number).toList().get(0).name;
                        }
                        catch (Exception e)
                        {
                            Callstate.friend_name = "Unknown";
                            e.printStackTrace();
                        }
                        Callstate.other_audio_enabled = f_audio_enabled;
                        Callstate.other_video_enabled = f_video_enabled;
                        Callstate.call_init_timestamp = System.currentTimeMillis();
                        main_activity_s.startActivityForResult(intent, CallingActivity_ID);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:(2):EE:" + e.getMessage());
                }
            }
        };
        main_handler_s.post(myRunnable);

    }
}
