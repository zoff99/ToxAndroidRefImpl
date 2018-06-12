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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.zoffcc.applications.trifa.ToxVars.TOX_ADDRESS_SIZE;

public class AddFriendActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.AddFrdActivity";
    EditText toxid_text = null;
    Button button_add = null;
    TextInputLayout friend_toxid_inputlayout = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toxid_text = (EditText) findViewById(R.id.friend_toxid);
        button_add = (Button) findViewById(R.id.friend_addbutton);
        friend_toxid_inputlayout = (TextInputLayout) findViewById(R.id.friend_toxid_inputlayout);

        toxid_text.setText("");
        // friend_toxid_inputlayout.setError("No ToxID");
        friend_toxid_inputlayout.setError(null);

        toxid_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable editable)
            {
                if (editable.length() == (TOX_ADDRESS_SIZE * 2))
                {
                    button_add.setEnabled(true);
                    friend_toxid_inputlayout.setErrorEnabled(false);
                }
                else if (editable.length() == ((TOX_ADDRESS_SIZE * 2) + "tox:".length()))
                {
                    // TODO: acutally see if editable starts with "tox:", but it can be in any case (ToX: or toX: or TOX: ....)
                    button_add.setEnabled(true);
                    friend_toxid_inputlayout.setErrorEnabled(false);
                }
                else
                {
                    button_add.setEnabled(false);
                    if (editable.length() > 0)
                    {
                        friend_toxid_inputlayout.setError("incorrect ToxID");
                    }
                    else
                    {
                        friend_toxid_inputlayout.setError("No ToxID");
                    }
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

    public void read_qr_code(View v)
    {
        try
        {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);
        }
        catch (Exception e)
        {
            try
            {
                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                startActivity(marketIntent);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
    }

    public void add_friend_clicked(View v)
    {
        Intent intent = new Intent();
        boolean toxid_ok = false;
        if (toxid_text.getText() != null)
        {
            if (toxid_text.getText().length() > 0)
            {
                toxid_ok = true;
            }
        }

        if (toxid_ok == true)
        {
            intent.putExtra("toxid", toxid_text.getText().toString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0)
        {
            if (resultCode == RESULT_OK)
            {
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                toxid_text.setText(contents);
            }
        }
    }
}
