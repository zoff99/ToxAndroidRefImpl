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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import static com.zoffcc.applications.trifa.MainActivity.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;

public class MessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MsgListActivity";
    long friendnum = -1;
    EditText ml_new_message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);

        setContentView(R.layout.activity_message_list);

        ml_new_message = (EditText) findViewById(R.id.ml_new_message);
    }

    long get_current_friendnum()
    {
        return friendnum;
    }

    public void send_message_onclick(View view)
    {
        // send typed message to friend
        String msg = ml_new_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(), ml_new_message.getText().toString().length()));

        Message m = new Message();
        m.tox_friendnum = friendnum;
        m.direction = 1; // msg sent
        m.TOX_MESSAGE_TYPE = 0;
        m.rcvd_timestamp = 0L;
        m.sent_timestamp = System.currentTimeMillis();
        m.read = false;
        m.text = msg;

        insert_into_message_db(m);
        long res = tox_friend_send_message(friendnum, 0, msg);

        ml_new_message.setText("");

        Log.i(TAG, "tox_friend_send_message:result=" + res);
    }
}
