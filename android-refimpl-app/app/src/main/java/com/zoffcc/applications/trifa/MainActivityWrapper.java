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

public class MainActivityWrapper extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivityWrpr";
    boolean set_pattern = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (set_pattern)
        {
            // Intent pattern = new Intent(this, TrifaSetPatternActivity.class);
            Intent pattern = new Intent(this, SetPasswordActivity.class);
            startActivity(pattern);
            finish();
        }
        else
        {
            // Intent pattern = new Intent(this, TrifaCheckPatternActivity.class);
            Intent pattern = new Intent(this, CheckPasswordActivity.class);
            startActivity(pattern);
            finish();
        }
    }
}
