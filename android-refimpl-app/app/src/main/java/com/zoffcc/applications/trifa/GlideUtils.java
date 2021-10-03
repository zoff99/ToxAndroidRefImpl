/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import info.guardianproject.iocipher.FileInputStream;

public class GlideUtils
{
    private static final String TAG = "trifa.glideutils";

    public static RequestOptions noDiskCacheOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE);

    public static boolean loadVideoFromUri(Context context, String filename_with_path, ImageView imageView, boolean vfs)
    {
        if (vfs)
        {
            try
            {
                info.guardianproject.iocipher.File fileVideo = new info.guardianproject.iocipher.File(
                        filename_with_path);

                Log.w(TAG, "trying to load video: " + filename_with_path);

                if (fileVideo.exists())
                {
                    final Drawable d3 = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_ondemand_video).
                            backgroundColor(Color.TRANSPARENT).
                            color(Color.parseColor("#AA000000")).sizeDp(60);

                    long interval = 10 * 1000;
                    RequestOptions options = new RequestOptions().frame(interval);

                    GlideApp.
                            with(context).
                            load(fileVideo).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            skipMemoryCache(false).
                            priority(Priority.LOW).
                            placeholder(R.drawable.round_loading_animation).
                            error(d3).
                            into(imageView);

                    return true;
                }
                return false;
            }
            catch (Exception e)
            {
                Log.w(TAG, "unable to load video: " + filename_with_path);
            }
        }
        else
        {
            Glide.with(context).
                    load(new java.io.File(filename_with_path)).
                    apply(noDiskCacheOptions).
                    priority(Priority.LOW).
                    into(imageView);
            return true;
        }

        return false;
    }

    public static void loadImageFromUri(Context context, Uri uri, ImageView imageView, boolean vfs)
    {
        if (vfs)
        {
            try
            {
                // Log.i(TAG, "loadImageFromUri:uri=" + uri.getPath());

                info.guardianproject.iocipher.File fileImage = new info.guardianproject.iocipher.File(uri.getPath());
                if (fileImage.exists())
                {
                    FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(fileImage);

                    final RequestOptions glide_options = new RequestOptions().fitCenter();
                    GlideApp.
                            with(context).
                            load(fis).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            priority(Priority.LOW).
                            skipMemoryCache(false).
                            apply(glide_options).
                            into(imageView);
                }
            }
            catch (Exception e)
            {
                Log.w(TAG, "loadImageFromUri:unable to load image: " + uri.toString());
            }
        }
        else
        {
            Glide.with(context).load(uri).into(imageView);
        }
    }
}
