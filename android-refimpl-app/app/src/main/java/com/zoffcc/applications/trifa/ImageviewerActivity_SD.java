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

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import androidx.appcompat.app.AppCompatActivity;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;

public class ImageviewerActivity_SD extends AppCompatActivity
{
    private static final String TAG = "trifa.ImageviewerActySD";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

        // TODO: bad!
        String image_filename = "/xx/xyz.png";
        String storage_frame_work = "0";

        try
        {
            storage_frame_work = getIntent().getStringExtra("storage_frame_work");
        }
        catch (Exception e)
        {
            e.getMessage();
        }

        if (storage_frame_work == null)
        {
            storage_frame_work = "0";
        }

        Uri uri = null;
        if (storage_frame_work.equals("1"))
        {
            uri = Uri.parse(getIntent().getStringExtra("image_filename"));
        }
        else
        {
            try
            {
                image_filename = getIntent().getStringExtra("image_filename");
                // Log.i(TAG, "onCreate:image_filename=" + image_filename);
            }
            catch (Exception e)
            {
                e.getMessage();
            }
        }

        final PhotoView photoView = (PhotoView) findViewById(R.id.big_image);
        photoView.setImageResource(R.drawable.round_loading_animation);

        final Handler imageviewer_handler = new Handler(getMainLooper());

        if (VFS_ENCRYPT)
        {
            if (storage_frame_work.equals("0"))
            {
                final String image_filename_ = image_filename;
                java.io.File f2 = new java.io.File(image_filename_);

                try
                {
                    RequestOptions req_options = new RequestOptions(); //.onlyRetrieveFromCache(true);

                    GlideApp.
                            with(this).
                            load(f2).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            skipMemoryCache(false).
                            apply(req_options).
                            placeholder(R.drawable.round_loading_animation).
                            into(photoView);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    RequestOptions req_options = new RequestOptions(); //.onlyRetrieveFromCache(true);

                    GlideApp.
                            with(this).
                            load(uri).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            skipMemoryCache(false).
                            apply(req_options).
                            placeholder(R.drawable.round_loading_animation).
                            into(photoView);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
