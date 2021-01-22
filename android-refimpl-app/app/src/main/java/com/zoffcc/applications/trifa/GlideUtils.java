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
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import info.guardianproject.iocipher.FileInputStream;

public class GlideUtils
{
    private static final String TAG = "trifa.glideutils";

    public static RequestOptions noDiskCacheOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE);

    public static boolean loadVideoFromUri(Context context, Uri uri, ImageView imageView, boolean vfs)
    {
        if (vfs)
        {
            try
            {
                info.guardianproject.iocipher.File fileVideo = new info.guardianproject.iocipher.File(uri.getPath());
                if (fileVideo.exists())
                {
                    Glide.with(context).load(new info.guardianproject.iocipher.FileInputStream(fileVideo)).apply(
                            noDiskCacheOptions).into(imageView);
                    return true;
                }
                return false;
            }
            catch (Exception e)
            {
                Log.w(TAG, "unable to load image: " + uri.toString());
            }
        }
        else
        {
            Glide.with(context).load(uri).into(imageView);
            return true;
        }

        return false;
    }

    public static void loadImageFromUri(Context context, Uri uri, CircleImageView imageView, boolean vfs)
    {
        if (vfs)
        {
            try
            {

                Log.i(TAG, "loadImageFromUri:uri=" + uri.getPath());

                info.guardianproject.iocipher.File fileImage = new info.guardianproject.iocipher.File(uri.getPath());
                if (fileImage.exists())
                {
                    FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(fileImage);

                    final RequestOptions glide_options = new RequestOptions().fitCenter();
                    GlideApp.
                            with(context).
                            load(fis).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            priority(Priority.HIGH).
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
