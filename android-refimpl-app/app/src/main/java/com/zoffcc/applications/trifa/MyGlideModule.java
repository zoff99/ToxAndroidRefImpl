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

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.resource.bytes.ByteBufferRewinder;
import com.bumptech.glide.module.GlideModule;

public class MyGlideModule implements GlideModule
{
    private static final String TAG = "trifa.MyGlideModule";

    @Override
    public void applyOptions(Context context, GlideBuilder builder)
    {
        Log.i(TAG, "applyOptions");
        // Apply options to the builder here.
    }

    @Override
    public void registerComponents(Context context, Registry registry)
    {
        Log.i(TAG, "registerComponents");

        registry.
                register(new ByteBufferRewinder.Factory()).
                prepend(info.guardianproject.iocipher.File.class, java.io.FileInputStream.class, new com.zoffcc.applications.trifa.FileLoader2.StreamFactory());
    }
}