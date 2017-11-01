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
import android.util.Log;

public class Identicon
{
    private static final String TAG = "trifa.Identicon";

    static int COLORS = 2;
    static int IDENTICON_ROWS = 5;
    static int ACTIVE_COLS = (IDENTICON_ROWS + 1) / 2;
    static int IDENTICON_COLOR_BYTES = 6;
    static int HASH_MIN_LEN = ACTIVE_COLS * IDENTICON_ROWS + COLORS * IDENTICON_COLOR_BYTES;

    static class Identicon_data
    {
        int color_a;
        int color_b;
        boolean[][] dot_color;
    }


    static Identicon_data create_identicon(String input)
    {
        Identicon_data ret = new Identicon_data();

        Log.i(TAG, "create_identicon:in=" + input);
        byte[] hash = TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(input));
        Log.i(TAG, "create_identicon:hash=" + hash.toString() + " len=" + hash.length);

        int c[] = new int[COLORS];

        byte[] hashPart = new byte[IDENTICON_COLOR_BYTES];

        for (int j = 0; j < IDENTICON_COLOR_BYTES; j++)
        {
            Log.i(TAG, "create_identicon:loop:j=" + j + " hash.length=" + hash.length + " val=" + (hash.length - IDENTICON_COLOR_BYTES + j));
            // hashPart[j] = hash[hash.length - IDENTICON_COLOR_BYTES + j];
            hashPart[j] = hash[j];
        }
        Log.i(TAG, "create_identicon:hashPart=" + hashPart);


        for (int colorIndex = 0; colorIndex < COLORS; ++colorIndex)
        {

            float hue = bytesToColor(hashPart, hashPart.length);
            Log.i(TAG, "create_identicon:hue=" + hue);

            // change offset when COLORS != 2
            float lig = (float) colorIndex / (float) COLORS + 0.3f;
            float sat = 0.5f;
            int[] c2 = HSL_RGB.hslToRgb(hue, sat, lig);
            c[colorIndex] = Color.rgb(c2[0], c2[1], c2[2]);

            Log.i(TAG, "create_identicon:color[" + colorIndex + "]=" + Color.red(c[colorIndex]) + " " + Color.green(c[colorIndex]) + " " + Color.blue(c[colorIndex]));
        }

        // save calculated color values
        ret.color_a = c[0];
        ret.color_b = c[1];

        ret.dot_color = new boolean[IDENTICON_ROWS][ACTIVE_COLS];

        // compute the block colors from the hash
        for (int row = 0; row < IDENTICON_ROWS; ++row)
        {
            for (int col = 0; col < ACTIVE_COLS; ++col)
            {
                Log.i(TAG, "create_identicon:col=" + col + " row=" + row);
                int hashIdx = row * ACTIVE_COLS + col;
                Log.i(TAG, "create_identicon:hashIdx=" + hashIdx);
                if (hashIdx >= IDENTICON_COLOR_BYTES)
                {
                    hashIdx = hashIdx - IDENTICON_COLOR_BYTES;
                }
                if (hashIdx >= IDENTICON_COLOR_BYTES)
                {
                    hashIdx = hashIdx - IDENTICON_COLOR_BYTES;
                }
                Log.i(TAG, "create_identicon:hashIdx=" + hashIdx);
                Log.i(TAG, "create_identicon:hashPart[hashIdx]=" + hashPart[hashIdx]);
                Log.i(TAG, "create_identicon:hashPart[hashIdx]=" + bytesChr(hashPart[hashIdx]));
                int colorIndex = (hashPart[hashIdx]) % COLORS;
                Log.i(TAG, "create_identicon:colorIndex=" + colorIndex);

                if (colorIndex == 0)
                {
                    ret.dot_color[row][col] = false;
                }
                else
                {
                    ret.dot_color[row][col] = true;
                }
            }
        }

        return ret;
    }

    public static long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }

    static int bytesChr(byte data)
    {
        Byte b = new Byte(data);
        int reta = b.intValue() + 128;
        Log.i(TAG, "bytesChr:ret[a]=" + reta);
        // long retb = getUnsignedInt(reta);
        // long retb = reta;
        // Log.i(TAG, "bytesChr:ret[b]=" + retb);

        return reta;
    }

    /**
     * @param data_bytes Bytes to convert to a color
     * @param len        how many input bytes
     * @return Value in the range of 0.0..1.0
     * @brief Converts a series of IDENTICON_COLOR_BYTES bytes to a value in the range 0.0..1.0
     */
    static float bytesToColor(byte[] data_bytes, int len)
    {

        // get foreground color
        long hue = (data_bytes[0]);

        // convert the last bytes to an uint
        for (int i = 1; i < IDENTICON_COLOR_BYTES; ++i)
        {
            hue = hue << 8;
            hue += (data_bytes[i]);
        }

        // normalize to 0.0 ... 1.0
        // return (static_cast<float>(hue)) / (((static_cast<uint64_t>(1)) << (8 * IDENTICON_COLOR_BYTES)) - 1);

        float ret = (float) hue / ((long) 1 << (8 * IDENTICON_COLOR_BYTES) - 1);

        return ret;
    }
}
