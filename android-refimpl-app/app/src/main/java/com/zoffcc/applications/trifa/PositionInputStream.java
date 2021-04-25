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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class PositionInputStream extends FilterInputStream
{
    private long pos = 0;
    private long mark = 0;

    public PositionInputStream(InputStream in)
    {
        super(in);
    }

    /**
     * <p>Get the stream position.</p>
     *
     * <p>Eventually, the position will roll over to a negative number.
     * Reading 1 Tb per second, this would occur after approximately three
     * months. Applications should account for this possibility in their
     * design.</p>
     *
     * @return the current stream position.
     */
    public synchronized long getPosition()
    {
        return pos;
    }

    @Override
    public synchronized int read() throws IOException
    {
        int b = super.read();
        if (b >= 0)
        {
            pos += 1;
        }
        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
        int n = super.read(b, off, len);
        if (n > 0)
        {
            pos += n;
        }
        return n;
    }

    @Override
    public synchronized long skip(long skip) throws IOException
    {
        long n = super.skip(skip);
        if (n > 0)
        {
            pos += n;
        }
        return n;
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        super.mark(readlimit);
        mark = pos;
    }

    @Override
    public synchronized void reset() throws IOException
    {
        /* A call to reset can still succeed if mark is not supported, but the
         * resulting stream position is undefined, so it's not allowed here. */
        if (!markSupported())
        {
            throw new IOException("Mark not supported.");
        }
        super.reset();
        pos = mark;
    }

}
