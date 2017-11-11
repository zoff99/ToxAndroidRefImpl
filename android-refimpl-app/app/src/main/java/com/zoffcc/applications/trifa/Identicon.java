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

        byte[] pubkey_as_unsigned_bytes = new byte[input.length() / 2];

        int jj;
        for (jj = 0; jj < (input.length() / 2); jj++)
        {
            String hex_ = "0x" + input.substring(jj * 2, (jj * 2) + 2).toLowerCase();
            int cur_byte = Integer.decode(hex_);
            Log.i(TAG, "create_identicon:loop:byte=" + cur_byte + " hex=" + hex_);
            pubkey_as_unsigned_bytes[jj] = (byte) cur_byte;
        }

        byte[] hash = TrifaSetPatternActivity.sha256(pubkey_as_unsigned_bytes);
        Log.i(TAG, "create_identicon:hash=" + bytesToHex(hash).toLowerCase() + " len=" + hash.length);

        int c[] = new int[COLORS];

        for (int colorIndex = 0; colorIndex < COLORS; colorIndex++)
        {
            // 79d2cb856b57
            String hashpart_as_hex_string = bytesToHex(hash).toLowerCase().
                    substring(bytesToHex(hash).length() - (12 * (colorIndex + 1)), bytesToHex(hash).length() - (12 * colorIndex));

            float hue = bytesToColor(hashpart_as_hex_string);
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

        // 43252927de957f70158794c0d919be2ba2b4c058
        String first_20_hex_bytes = bytesToHex(hash).toLowerCase().substring(0, 40);
        Log.i(TAG, "create_identicon:20bytes=" + first_20_hex_bytes);

        // compute the block colors from the hash
        for (int row = 0; row < IDENTICON_ROWS; ++row)
        {
            for (int col = 0; col < ACTIVE_COLS; ++col)
            {
                Log.i(TAG, "create_identicon:col=" + col + " row=" + row);
                int hashIdx = row * ACTIVE_COLS + col;

                Log.i(TAG, "create_identicon:hashIdx=" + hashIdx);
                String cur_hex_byte = first_20_hex_bytes.substring(hashIdx * 2, (hashIdx * 2) + 2);
                Log.i(TAG, "create_identicon:cur_hex_byte=" + cur_hex_byte);

                int right_most_bit = Integer.decode("0x" + cur_hex_byte);
                Log.i(TAG, "create_identicon:bin=" + right_most_bit);

                int colorIndex = right_most_bit % COLORS;

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

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    static int bytesChr(byte data)
    {
        int reta = data;
        if (reta < 0)
        {
            // convert to unsigned int
            reta = 256 + data;
        }
        Log.i(TAG, "bytesChr:ret[a]=" + reta);
        return reta;
    }

    //    static String integerToBinary(int in)
    //    {
    //        return Integer.toBinaryString(in);
    //    }
    //
    //    static int binaryToInteger(String binary)
    //    {
    //        char[] numbers = binary.toCharArray();
    //        int result = 0;
    //        for (int i = numbers.length - 1; i >= 0; i--)
    //        {
    //            if (numbers[i] == '1')
    //            {
    //                result += Math.pow(2, (numbers.length - i - 1));
    //            }
    //        }
    //        return result;
    //    }

    /**
     * @return Value in the range of 0.0..1.0
     * @brief Converts a series of IDENTICON_COLOR_BYTES bytes to a value in the range 0.0..1.0
     */
    static float bytesToColor(String as_hex_string)
    {
        // 79d2cb856b57
        // 3edc675d3543
        Log.i(TAG, "bytesToColor:as_hex_string=" + as_hex_string);

        long maximum = Long.decode("0x" + "ffffffffffff"); // 281474976710655L;
        long hue = Long.decode("0x" + as_hex_string);

        Log.i(TAG, "bytesToColor:3:" + hue);
        Log.i(TAG, "bytesToColor:3 max:" + maximum);

        // normalize to 0.0 ... 1.0
        // return (static_cast<float>(hue)) / (((static_cast<uint64_t>(1)) << (8 * IDENTICON_COLOR_BYTES)) - 1);
        float ret = (float) ((double) hue / (double) maximum);
        Log.i(TAG, "bytesToColor:4:" + ret + " ," + maximum);

        return ret;
    }
}
