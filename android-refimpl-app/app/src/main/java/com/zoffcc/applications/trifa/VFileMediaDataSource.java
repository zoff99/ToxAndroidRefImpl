package com.zoffcc.applications.trifa;

import android.media.MediaDataSource;
import android.os.Build;

import java.io.IOException;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class VFileMediaDataSource extends MediaDataSource
{
    private info.guardianproject.iocipher.RandomAccessFile f;
    private long streamLength = -1, lastReadEndPosition;

    public VFileMediaDataSource(info.guardianproject.iocipher.RandomAccessFile f)
    {
        this.f = f;
        try
        {
            this.streamLength = f.length();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        if (streamLength <= 0)
        {
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void close()
    {
    }

    @Override
    public synchronized int readAt(long position, byte[] buffer, int offset, int size)
    {
        if (position >= streamLength)
        {
            return -1;
        }

        if (position + size > streamLength)
        {
            size -= (position + size) - streamLength;
        }

        if (position < 0)
        {
            position = 0;
        }

        try
        {
            this.f.seek(position);
            return this.f.read(buffer, offset, size);
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    @Override
    public synchronized long getSize()
    {
        return streamLength;
    }
}
