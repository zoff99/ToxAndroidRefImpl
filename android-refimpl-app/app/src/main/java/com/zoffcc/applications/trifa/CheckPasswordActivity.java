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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import static com.zoffcc.applications.trifa.SetPasswordActivity.isPasswordValid;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF__DB_secrect_key__user_hash;

public class CheckPasswordActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CheckPasswordActy";

    // UI references.
    private EditText mPasswordView1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_password);

        mPasswordView1 = (EditText) findViewById(R.id.password_1_c);
        mPasswordView1.requestFocus();
        mPasswordView1.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                return true;
            }
        });

        Button SignInButton = (Button) findViewById(R.id.set_button_2);
        SignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.i(TAG, "unlock:002");
                attemptUnlock_new(view.getContext(), false, mPasswordView1.getText().toString());
            }
        });

        try
        {
            /* if the database secret key is saved in preferences, it means the user did NOT want to set a password.
             * and for our purpose we can also skip the password enter screen
             */
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            final String DB_secrect_key__tmp = settings.getString("DB_secrect_key", "");

            Log.i(TAG, "001");

            if (!TextUtils.isEmpty(DB_secrect_key__tmp))
            {
                Log.i(TAG, "auto_generated_password:true");
                Log.i(TAG, "003");
                attemptUnlock_new(this, true, DB_secrect_key__tmp);
                Log.i(TAG, "004");
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "EE:002");
        }
        Log.i(TAG, "006");
    }

    private void attemptUnlock_new(Context c, boolean auto_generated_pass, String pass)
    {
        if (TextUtils.isEmpty(pass))
        {
            return;
        }

        if (!isPasswordValid(pass))
        {
            return;
        }

        String try_password_hash = "";

        if (auto_generated_pass)
        {
            try_password_hash = pass;
        }
        else
        {
            try
            {
                try_password_hash = TrifaSetPatternActivity.bytesToString(
                        TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(pass)));
            }
            catch (Exception e)
            {
                Log.i(TAG, "EE:001");
            }
        }

        PREF__DB_secrect_key__user_hash = try_password_hash;

        Intent main_act = new Intent(c, MainActivity.class);
        startActivity(main_act);
        finish();
    }

    /*
    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();
        // do nothing!!
    }
     */
}

