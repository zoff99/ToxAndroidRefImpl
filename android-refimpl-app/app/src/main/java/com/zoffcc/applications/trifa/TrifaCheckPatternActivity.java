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
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import me.zhanghai.android.patternlock.ConfirmPatternActivity;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

public class TrifaCheckPatternActivity extends ConfirmPatternActivity
{
    private static final String TAG = "trifa.TrifaChkPattrnAcy";

    @Override
    protected boolean isStealthModeEnabled()
    {
        // TODO: Return the value from SharedPreferences.
        return false;
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern)
    {
        // TODO: Get saved pattern sha256.
        // TODO: real hash??
        String patternSha256 = "UwvLdISRY0NnQVVPGF9YLYmvkEw2TxrntGQBJq64sIw="; // hardcoded for debug
        return TextUtils.equals(TrifaSetPatternActivity.patternToSha256String(pattern), patternSha256);
    }


    protected void onConfirmed()
    {
        Log.i(TAG,"onConfirmed");

        setResult(RESULT_OK);

        // ok open main activity
        Intent main_act = new Intent(this, MainActivity.class);
        startActivity(main_act);
        finish();
    }

    protected void onWrongPattern()
    {
        Log.i(TAG,"onWrongPattern");

        ++mNumFailedAttempts;

        // ok open main activity
        Intent main_act = new Intent(this, MainActivity.class);
        startActivity(main_act);
        finish();
    }

    protected void onCancel()
    {
        Log.i(TAG,"onCancel");

        setResult(RESULT_CANCELED);

        // ok open main activity
        Intent main_act = new Intent(this, MainActivity.class);
        startActivity(main_act);
        finish();
    }

    @Override
    protected void onForgotPassword()
    {
        // startActivity(new Intent(this, YourResetPatternActivity.class));
        // Finish with RESULT_FORGOT_PASSWORD.
        // super.onForgotPassword();

        // ok open main activity
        Intent main_act = new Intent(this, MainActivity.class);
        startActivity(main_act);
        finish();
    }
}
