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


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import static com.zoffcc.applications.trifa.MainActivity.set_friend_avatar;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;

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

        // Log.i(TAG, "create_identicon:in=" + input);

        byte[] pubkey_as_unsigned_bytes = new byte[input.length() / 2];

        int jj;
        for (jj = 0; jj < (input.length() / 2); jj++)
        {
            String hex_ = "0x" + input.substring(jj * 2, (jj * 2) + 2).toLowerCase();
            int cur_byte = Integer.decode(hex_);
            // Log.i(TAG, "create_identicon:loop:byte=" + cur_byte + " hex=" + hex_);
            pubkey_as_unsigned_bytes[jj] = (byte) cur_byte;
        }

        byte[] hash = TrifaSetPatternActivity.sha256(pubkey_as_unsigned_bytes);
        // Log.i(TAG, "create_identicon:hash=" + bytesToHex(hash).toLowerCase() + " len=" + hash.length);

        int c[] = new int[COLORS];

        for (int colorIndex = 0; colorIndex < COLORS; colorIndex++)
        {
            // 79d2cb856b57
            String hashpart_as_hex_string = bytesToHex(hash).toLowerCase().
                    substring(bytesToHex(hash).length() - (12 * (colorIndex + 1)), bytesToHex(hash).length() - (12 * colorIndex));

            float hue = bytesToColor(hashpart_as_hex_string);
            // Log.i(TAG, "create_identicon:hue=" + hue);

            float lig = (float) colorIndex / (float) COLORS + 0.3f; // 0.5 and 0.8
            float sat = 0.5f;

            // Log.i(TAG, "create_identicon:sat=" + sat);
            // Log.i(TAG, "create_identicon:lig=" + lig);

            int[] c2 = HSL_RGB.hslToRgb(hue, sat, lig);
            c[colorIndex] = Color.rgb(c2[0], c2[1], c2[2]);

            // Log.i(TAG, "create_identicon:color[" + colorIndex + "]=" + Color.red(c[colorIndex]) + " " + Color.green(c[colorIndex]) + " " + Color.blue(c[colorIndex]));
        }

        // save calculated color values
        ret.color_a = c[0];
        ret.color_b = c[1];

        ret.dot_color = new boolean[IDENTICON_ROWS][ACTIVE_COLS];

        String first_20_hex_bytes = bytesToHex(hash).toLowerCase().substring(0, 40);
        // Log.i(TAG, "create_identicon:20bytes=" + first_20_hex_bytes);

        // compute the block colors from the hash
        for (int row = 0; row < IDENTICON_ROWS; ++row)
        {
            for (int col = 0; col < ACTIVE_COLS; ++col)
            {
                // Log.i(TAG, "create_identicon:col=" + col + " row=" + row);
                int hashIdx = row * ACTIVE_COLS + col;

                // Log.i(TAG, "create_identicon:hashIdx=" + hashIdx);
                String cur_hex_byte = first_20_hex_bytes.substring(hashIdx * 2, (hashIdx * 2) + 2);
                // Log.i(TAG, "create_identicon:cur_hex_byte=" + cur_hex_byte);

                int right_most_bit = Integer.decode("0x" + cur_hex_byte);
                // Log.i(TAG, "create_identicon:bin=" + right_most_bit);

                int colorIndex = right_most_bit % COLORS;

                // Log.i(TAG, "create_identicon:colorIndex=" + colorIndex);

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
        // Log.i(TAG, "bytesToColor:as_hex_string=" + as_hex_string);

        long maximum = Long.decode("0x" + "ffffffffffff"); // 281474976710655L;
        long hue = Long.decode("0x" + as_hex_string);

        // Log.i(TAG, "bytesToColor:3:" + hue);
        // Log.i(TAG, "bytesToColor:3 max:" + maximum);

        // normalize to 0.0 ... 1.0
        // return (static_cast<float>(hue)) / (((static_cast<uint64_t>(1)) << (8 * IDENTICON_COLOR_BYTES)) - 1);
        float ret = (float) ((double) hue / (double) maximum);
        // Log.i(TAG, "bytesToColor:4:" + ret + " ," + maximum);

        return ret;
    }


    public static void save_bitmap_to_vfs_file(Bitmap bitmap, String vfs_path, String vfs_filename, String pubkey)
    {
        info.guardianproject.iocipher.File path = new info.guardianproject.iocipher.File(vfs_path);
        path.mkdirs();
        info.guardianproject.iocipher.File imageFile = new info.guardianproject.iocipher.File(path, vfs_filename);
        info.guardianproject.iocipher.FileOutputStream out = null;
        try
        {
            out = new info.guardianproject.iocipher.FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            set_friend_avatar(pubkey, vfs_path, vfs_filename);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void create_avatar_identicon_for_pubkey(String pubkey)
    {
        try
        {
            if (pubkey.length() >= ToxVars.TOX_PUBLIC_KEY_SIZE * 2)
            {

                Identicon.Identicon_data id_data = Identicon.create_identicon(pubkey.substring(0, (ToxVars.TOX_PUBLIC_KEY_SIZE * 2)));
                int w = 275;
                int h = 275;
                int w_icon = 200;
                int h_icon = 200;
                int w_offset = (w - w_icon) / 2;
                int h_offset = (h - h_icon) / 2;

                // Log.i(TAG, "create_avatar_identicon_for_pubkey:w=" + w);
                // Log.i(TAG, "create_avatar_identicon_for_pubkey:h=" + w);

                Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
                Canvas canvas = new Canvas(bmp);

                Paint p0 = new Paint();
                p0.setColor(id_data.color_a);
                p0.setStyle(Paint.Style.FILL);

                Paint p1 = new Paint();
                p1.setColor(id_data.color_b);
                p1.setStyle(Paint.Style.FILL);

                canvas.drawColor(id_data.color_a);

                int x1 = 0;
                int y1 = 0;
                int x2 = 0;
                int y2 = 0;
                int dot_width = w_icon / IDENTICON_ROWS;
                int dot_height = h_icon / IDENTICON_ROWS;
                int columnIdx;

                // Log.i(TAG, "create_avatar_identicon_for_pubkey:dot_width=" + dot_width + " ACTIVE_COLS=" + IDENTICON_ROWS);
                // Log.i(TAG, "create_avatar_identicon_for_pubkey:dot_height=" + dot_height + " IDENTICON_ROWS=" + IDENTICON_ROWS);

                for (int row = 0; row < IDENTICON_ROWS; ++row)
                {
                    for (int col = 0; col < IDENTICON_ROWS; ++col)
                    {
                        columnIdx = Math.abs((col * 2 - (IDENTICON_ROWS - 1)) / 2);
                        // Log.i(TAG, "create_avatar_identicon_for_pubkey:col=" + col + " columnIdx=" + columnIdx + " row=" + row);

                        x1 = col * dot_width;
                        x2 = (col + 1) * dot_width;
                        y1 = row * dot_height;
                        y2 = (row + 1) * dot_height;

                        // Log.i(TAG, "create_avatar_identicon_for_pubkey:x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2);

                        if (id_data.dot_color[row][columnIdx] == true)
                        {
                            canvas.drawRect(x1 + w_offset, y1 + h_offset, x2 + w_offset, y2 + h_offset, p1);
                        }
                        else
                        {
                            canvas.drawRect(x1 + w_offset, y1 + h_offset, x2 + w_offset, y2 + h_offset, p0);
                        }
                    }
                }

                try
                {
                    String path_name = VFS_PREFIX + VFS_FILE_DIR + "/" + pubkey + "/";
                    String file_name = FRIEND_AVATAR_FILENAME;
                    save_bitmap_to_vfs_file(bmp, path_name, file_name, pubkey);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "create_avatar_identicon_for_pubkey:EE:" + e.getMessage());
        }

    }
}
