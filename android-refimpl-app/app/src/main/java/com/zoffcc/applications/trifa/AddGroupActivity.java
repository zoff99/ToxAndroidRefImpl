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
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.ToxVars.TOX_ADDRESS_SIZE;

public class AddGroupActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.AddGrpActivity";
    EditText groupname_text = null;
    Button button_add = null;
    TextInputLayout new_group_inputlayout = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgroup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupname_text = (EditText) findViewById(R.id.group_new_group_name);
        button_add = (Button) findViewById(R.id.friend_addgroup);
        new_group_inputlayout = (TextInputLayout) findViewById(R.id.new_group_inputlayout);

        groupname_text.setText("");
        new_group_inputlayout.setError("No Group Name");
        // new_group_inputlayout.setError(null);
        button_add.setEnabled(false);

        groupname_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable editable)
            {
                String group_name = editable.toString().
                        replace("\r", "").
                        replace("\n", "");

                if ((group_name != null) && (group_name.length() > 0))
                {
                    button_add.setEnabled(true);
                    new_group_inputlayout.setErrorEnabled(false);
                    new_group_inputlayout.setError(null);
                }
                else
                {
                    button_add.setEnabled(false);
                    new_group_inputlayout.setErrorEnabled(true);
                    new_group_inputlayout.setError("No Group Name");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }
        });
    }

    public void add_group_clicked(View v)
    {
        Intent intent = new Intent();
        boolean group_name_ok = false;
        if (groupname_text.getText() != null)
        {
            if (groupname_text.getText().length() > 0)
            {
                group_name_ok = true;
            }
        }

        if (group_name_ok == true)
        {
            String group_name_clean = groupname_text.getText().toString().
                    replace("\r", "").
                    replace("\n", "");

            intent.putExtra("group_name", group_name_clean);
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
