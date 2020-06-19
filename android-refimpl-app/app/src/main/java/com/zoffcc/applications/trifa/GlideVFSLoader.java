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

import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.security.MessageDigest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import info.guardianproject.iocipher.FileInputStream;

public class GlideVFSLoader implements ModelLoader<info.guardianproject.iocipher.FileInputStream, java.io.InputStream>
{

    public GlideVFSLoader()
    {
    }

    @Nullable
    @Override
    public LoadData<java.io.InputStream> buildLoadData(info.guardianproject.iocipher.FileInputStream model, int width, int height, Options options)
    {
        return new LoadData<>(new GlideVFSCacheKey(model), new VFSDataFetcher(model));
    }

    @Override
    public boolean handles(info.guardianproject.iocipher.FileInputStream model)
    {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<info.guardianproject.iocipher.FileInputStream, java.io.InputStream>
    {
        @Override
        public ModelLoader<FileInputStream, java.io.InputStream> build(MultiModelLoaderFactory multiFactory)
        {
            return new GlideVFSLoader();
        }

        @Override
        public void teardown()
        {
        }
    }
}

class GlideVFSCacheKey implements Key
{
    FileInputStream fis;

    public GlideVFSCacheKey(FileInputStream fis)
    {
        this.fis = fis;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest)
    {
        //  Log.d(getClass().getName(),"updateDiskCacheKey=" + messageDigest.toString());
    }

    @Override
    public int hashCode()
    {
        try
        {
            int hashCode = fis.getFD().hashCode();
            //   Log.d("GlideKey","HashCode="+hashCode);
            return hashCode;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj.hashCode() == (hashCode());
    }
}

class VFSDataFetcher implements DataFetcher<java.io.InputStream>
{
    private java.io.InputStream vfsFileStream;

    public VFSDataFetcher(info.guardianproject.iocipher.FileInputStream vfsFileStream)
    {
        // explode model fields so that they can't be modified (finals in OBBFile are optional)
        this.vfsFileStream = vfsFileStream;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super java.io.InputStream> callback)
    {
        callback.onDataReady(vfsFileStream);
    }

    @Override
    public void cleanup()
    {
        try
        {
            if (vfsFileStream != null)
            {
                vfsFileStream.close();
            }
        }
        catch (java.io.IOException e)
        {
            Log.w("VFSDataFetcher", "Cannot clean up after stream", e);
        }
    }

    @Override
    public void cancel()
    {
        // do nothing
        try
        {
            if (vfsFileStream != null)
            {
                vfsFileStream.close();
            }
        }
        catch (java.io.IOException e)
        {
            Log.w("VFSDataFetcher", "Cannot clean up after stream", e);
        }
    }

    @NonNull
    @Override
    public Class<java.io.InputStream> getDataClass()
    {
        return java.io.InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource()
    {
        return DataSource.LOCAL;
    }
}
