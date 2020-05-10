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

package com.bumptech.glide.signature;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

/**
 * A unique Signature that wraps a String.
 */
public class StringSignatureZ implements Key
{
    private final String signature;

    public StringSignatureZ(String signature)
    {
        if (signature == null)
        {
            throw new NullPointerException("Signature cannot be null!");
        }
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        StringSignatureZ that = (StringSignatureZ) o;
        boolean res = signature.equals(that.signature);
        // System.out.println("StringSignature:equals=" + res + " " + signature + " " + that.signature);
        return res;
    }

    @Override
    public int hashCode()
    {
        return signature.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest)
    {
        try
        {
            messageDigest.update(signature.getBytes(STRING_CHARSET_NAME));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}