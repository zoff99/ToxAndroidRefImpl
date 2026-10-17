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

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public final class MyGlideModule extends AppGlideModule
{
    private static final String TAG = "trifa.MyGlideModule";

    //@Override
    //public void applyOptions(Context context, GlideBuilder builder)
    //{
    //    Log.i(TAG, "applyOptions");
    //    // Apply options to the builder here.
    //}

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        Log.i(TAG, "registerComponents");

        // NEW: Register iocipher.File -> InputStream loader
        // This replaces the old FileInputStream-based loader.
        // Using File as the model type gives us stable path/length for cache keys,
        // and lets VFSDataFetcher open the stream safely on Glide's worker thread.
        registry.prepend(info.guardianproject.iocipher.File.class, java.io.InputStream.class,
                         new GlideVFSLoader.Factory());

        // LEGACY: Keep old FileInputStream loader temporarily
        // Remove this once all callers have been migrated to load(info.guardianproject.iocipher.File)
        // instead of load(new java.io.FileInputStream(...)).
        // NOTE: The old FileLoader2 still has the broken FD-based cache key.
        registry.prepend(info.guardianproject.iocipher.FileInputStream.class, java.io.InputStream.class,
                         new com.zoffcc.applications.trifa.FileLoader2.StreamFactory());
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled()
    {
        Log.i(TAG, "isManifestParsingEnabled");
        return false;
    }
}
