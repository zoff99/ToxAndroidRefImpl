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