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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddFriendActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.AddFrdActivity";
    EditText t = null;
    Button b_add = null;
    Button b_qr = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        t = (EditText) findViewById(R.id.friend_toxid);
        b_add = (Button) findViewById(R.id.friend_addbutton);
        b_qr = (Button) findViewById(R.id.friend_qrbutton);

        t.setText("");
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
        if (t.getText() != null)
        {
            if (t.getText().length() > 0)
            {
                toxid_ok = true;
            }
        }

        if (toxid_ok == true)
        {
            intent.putExtra("toxid", t.getText().toString());
            setResult(RESULT_OK, intent);
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }
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
                t.setText(contents);
            }
        }
    }
}
