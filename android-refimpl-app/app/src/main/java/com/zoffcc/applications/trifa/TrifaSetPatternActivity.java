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
import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import me.zhanghai.android.patternlock.PatternView;
import me.zhanghai.android.patternlock.SetPatternActivity;

public class TrifaSetPatternActivity extends SetPatternActivity
{
    private static final String TAG = "trifa.TrifaSetPattrnAcy";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern)
    {
        String patternSha256 = patternToSha256String(pattern);
        Log.i(TAG, "onSetPattern:pattern=" + patternSha256);

        // ok open main activity
        Intent main_act = new Intent(TrifaSetPatternActivity.this, MainActivity.class);
        startActivity(main_act);
        finish();
    }


    public static byte[] patternToBytes(List<PatternView.Cell> pattern, int columnCount)
    {
        int patternSize = pattern.size();
        byte[] bytes = new byte[patternSize];
        for (int i = 0; i < patternSize; ++i)
        {
            PatternView.Cell cell = pattern.get(i);
            bytes[i] = (byte) (cell.getRow() * columnCount + cell.getColumn());
        }
        return bytes;
    }

    public static byte[] patternToSha256(List<PatternView.Cell> pattern, int columnCount)
    {
        return sha256(patternToBytes(pattern, columnCount));
    }

    public static String bytesToString(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static String patternToSha256String(List<PatternView.Cell> pattern, int columnCount)
    {
        return bytesToString(patternToSha256(pattern, columnCount));
    }

    public static String patternToSha256String(List<PatternView.Cell> pattern)
    {
        return patternToSha256String(pattern, PatternView.PATTERN_SIZE_DEFAULT);
    }

    public static byte[] StringToBytes(String in)
    {
        return Base64.decode(in, Base64.NO_WRAP);
    }

    public static byte[] StringToBytes2(String in)
    {
        try
        {
            return in.getBytes(Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            e.printStackTrace();
            return null;
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
        }
    }

    public static byte[] sha256(byte[] input)
    {
        try
        {
            return MessageDigest.getInstance("SHA-256").digest(input);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static byte[] md5(byte[] input)
    {
        try
        {
            return MessageDigest.getInstance("MD5").digest(input);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
