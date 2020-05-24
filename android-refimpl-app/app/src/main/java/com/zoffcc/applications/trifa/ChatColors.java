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

import android.graphics.Color;

public class ChatColors
{
    private static final String TAG = "trifa.ChatCols";

    static int[] PeerAvatarColors = {
        //
        // https://www.w3schools.com/colors/colors_picker.asp
        //
        // ** too dark ** // Color.parseColor("#0000FF"), // Blue
        Color.parseColor("#6666ff"), // * lighter blue *
        //
        // ** // Color.parseColor("#FF00FF"), // Fuchsia
        //
        Color.parseColor("#00FFFF"), // Aqua
        //
        Color.parseColor("#008000"), // Green
        //
        Color.parseColor("#dce775"), // Lime
        //
        // ** too dark ** // Color.parseColor("#800000"), // Maroon
        Color.parseColor("#f06292"), // * lighter red *
        //
        // ** too dark ** // Color.parseColor("#000080"), // Navy
        Color.parseColor("#42a5f5"), // * lighter blue *
        //
        Color.parseColor("#808000"), // Olive
        //
        Color.parseColor("#800080"), // Purple
        //
        // ** too dark ** // Color.parseColor("#FF0000"), // Red
        Color.parseColor("#ff4d4d"), // * lighter red *
        //
        Color.parseColor("#008080"), // Teal
        //
        // ** too bright ** // Color.parseColor("#FFFF00")  // Yellow
        Color.parseColor("#cccc00"), // * darker yellow *
        //
    };

    static int get_size()
    {
        return PeerAvatarColors.length;
    }

    static int get_shade(int color, String pubkey)
    {
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(0, 1)=" + pubkey.substring(0, 1));
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(1, 2)=" + pubkey.substring(1, 2));

        float factor =
            (Integer.parseInt(pubkey.substring(0, 1), 16) + (Integer.parseInt(pubkey.substring(1, 2), 16) * 16)) /
            255.0f;

        final float range = 0.5f;
        final float min_value = 1.0f - (range * 0.6f);
        factor = (factor * range) + min_value;

        return manipulateColor(color, factor);
    }

    public static int manipulateColor(int color, float factor)
    {
        // Log.i(TAG, "manipulateColor:color=" + color + " factor=" + factor);

        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
