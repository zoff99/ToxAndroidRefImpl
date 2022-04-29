/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2022 Zoff <zoff@zoff.cc>
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class JoinPublicGroupActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.JoinPubGrpActy";
    EditText groupid_text = null;
    Button button_join = null;
    TextInputLayout join_group_inputlayout = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinpublicgroup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupid_text = (EditText) findViewById(R.id.group_join_group_id);
        button_join = (Button) findViewById(R.id.friend_joingroup);
        join_group_inputlayout = (TextInputLayout) findViewById(R.id.join_group_inputlayout);

        groupid_text.setText("");
        join_group_inputlayout.setError("No Group ID");
        // new_group_inputlayout.setError(null);
        button_join.setEnabled(false);
    }

    public void join_group_public_clicked(View v)
    {
        Intent intent = new Intent();
        boolean group_name_ok = false;
        if (groupid_text.getText() != null)
        {
            if (groupid_text.getText().length() > 0)
            {
                group_name_ok = true;
            }
        }

        if (group_name_ok == true)
        {
            String group_id_clean = groupid_text.getText().toString().
                    replace("\t", "").
                    replace(" ", "").
                    replace("\r", "").
                    replace("\n", "");

            intent.putExtra("group_id", group_id_clean);
            setResult(RESULT_OK, intent);
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    public void cancel_clicked(View v)
    {
        finish();
    }
}
