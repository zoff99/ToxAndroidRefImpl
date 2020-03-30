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