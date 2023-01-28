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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.HelperGroup.do_join_public_group;
import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_CHAT_ID_SIZE;

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

        groupid_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable editable)
            {
                String group_id_try = editable.toString().
                        replace(" ", "").
                        replace("\r", "").
                        replace("\n", "").
                        replaceAll("[^a-fA-F0-9]", "");

                if ((editable.toString() == null) || (editable.toString().length() == 0))
                {
                    button_join.setEnabled(false);
                    join_group_inputlayout.setErrorEnabled(true);
                    join_group_inputlayout.setError("No Group ID");
                    groupid_text.setError(null);
                    return;
                }

                if (editable.toString().toUpperCase().compareTo(group_id_try.toUpperCase()) != 0)
                {
                    button_join.setEnabled(false);
                    join_group_inputlayout.setErrorEnabled(false);
                    groupid_text.setError("Group ID contains illegal characters. allowed are A-F and 0-9");
                    return;
                }

                if ((group_id_try == null) || (group_id_try.length() != (TOX_GROUP_CHAT_ID_SIZE * 2)))
                {
                    button_join.setEnabled(false);
                    join_group_inputlayout.setErrorEnabled(false);
                    groupid_text.setError("Group ID must be 64 hex characters long");
                    return;
                }

                join_group_inputlayout.setErrorEnabled(false);
                groupid_text.setError(null);
                button_join.setEnabled(true);
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

        try
        {
            Intent intent = getIntent();
            String ngc_group_pubkey = intent.getStringExtra("ngc_group_pubkey");
            if ((ngc_group_pubkey != null) && (ngc_group_pubkey.length() > 1))
            {
                groupid_text.setText(ngc_group_pubkey);
            }
        }
        catch(Exception e)
        {
        }
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
                Log.i(TAG, "001");
            }
        }

        if (group_name_ok == true)
        {
            String group_id_clean = groupid_text.getText().toString().
                    replace(" ", "").
                    replace("\r", "").
                    replace("\n", "").
                    replaceAll("[^a-fA-F0-9]", "");

            Log.i(TAG, "002");
            intent.putExtra("group_id", group_id_clean);
            Log.i(TAG, "003");

            do_join_public_group(intent);

            setResult(RESULT_OK, intent);
            Log.i(TAG, "004");
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

    static void show_join_public_group_activity(Context c, final String ngc_group_pubkey)
    {
        Intent intent = new Intent(c, JoinPublicGroupActivity.class);
        intent.putExtra("ngc_group_pubkey", ngc_group_pubkey);
        c.startActivity(intent);
    }
}
