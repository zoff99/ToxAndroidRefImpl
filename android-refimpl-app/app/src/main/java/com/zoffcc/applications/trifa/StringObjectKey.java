package com.zoffcc.applications.trifa;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;

import java.security.MessageDigest;


public final class StringObjectKey implements Key
{
    private static final String TAG = "trifa.StringObjectKey";

    private final Object object;

    public StringObjectKey(Object object)
    {
        this.object = Preconditions.checkNotNull(object);
    }

    @Override
    public String toString()
    {
        return (String) object;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof StringObjectKey)
        {
            StringObjectKey other = (StringObjectKey) o;
            // Log.i(TAG, "equals:me=" + object.toString() + " o=" + o.toString());
            // Log.i(TAG, "equals:res=" + object.toString().equals(o.toString()));
            return object.toString().equals(o.toString());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return object.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest)
    {
        // Log.i(TAG, "updateDiskCacheKey:digest=" + object.toString().getBytes(CHARSET));
        messageDigest.update(object.toString().getBytes(CHARSET));
    }
}

