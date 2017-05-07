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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class ProfileActivity extends AppCompatActivity
{
    ImageView profile_icon = null;
    ImageView mytoxid_imageview = null;
    TextView mytoxid_textview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_icon = (ImageView) findViewById(R.id.profile_icon);
        mytoxid_imageview = (ImageView) findViewById(R.id.mytoxid_imageview);
        mytoxid_textview = (TextView) findViewById(R.id.mytoxid_textview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        profile_icon.setImageDrawable(d1);

        mytoxid_textview.setText("");

        try
        {
            mytoxid_imageview.setImageBitmap(encodeAsBitmap("tox:" + MainActivity.get_my_toxid()));
            mytoxid_textview.setText(MainActivity.get_my_toxid());
        }
        catch (WriterException e)
        {
            e.printStackTrace();

            try
            {
                mytoxid_imageview.setImageBitmap(encodeAsBitmap("123")); // in case something goes wrong
                mytoxid_textview.setText(MainActivity.get_my_toxid());
            }
            catch (WriterException e2)
            {
                e2.printStackTrace();
            }

        }

    }


    Bitmap encodeAsBitmap(String str) throws WriterException
    {
        BitMatrix result;
        try
        {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 200, 200, null);
        }
        catch (IllegalArgumentException iae)
        {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++)
        {
            int offset = y * w;
            for (int x = 0; x < w; x++)
            {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 200, 0, 0, w, h);
        return bitmap;
    }
}
