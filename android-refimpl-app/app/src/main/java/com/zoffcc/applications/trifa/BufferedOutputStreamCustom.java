/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
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

import java.io.FileNotFoundException;
import java.io.IOException;

public class BufferedOutputStreamCustom extends info.guardianproject.iocipher.FileOutputStream
{
    /**
     * The internal buffer where data is stored.
     */
    protected byte[] buf;
    protected int count;
    protected long cur_pos;
    final protected int PAGE_SIZE = 8192 * 4; // in bytes (should be a multiple of 8192)

    public BufferedOutputStreamCustom(String path) throws FileNotFoundException
    {
        super(path);
        count = 0;
        cur_pos = 0;
        buf = new byte[PAGE_SIZE];
        // Log.i("BufOutStream:new", "buf=" + buf);
    }

    /**
     * Flush the internal buffer
     */
    private void flushBuffer() throws IOException
    {
        if (count > 0)
        {
            // Log.i("BufOutStream", "buf=" + buf + " count=" + count);
            super.write(buf, 0, count);
            count = 0;
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this buffered output stream.
     *
     * <p> Ordinarily this method stores bytes from the given array into this
     * stream's buffer, flushing the buffer to the underlying output stream as
     * needed.  If the requested length is at least as large as this stream's
     * buffer, however, then this method will flush the buffer and write the
     * bytes directly to the underlying output stream.  Thus redundant
     * <code>BufferedOutputStream</code>s will not copy data unnecessarily.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void write(byte b[], int off, int len) throws IOException
    {
        // Log.i("BufOutStream:write", "buf=" + buf + " off=" + off + " len=" + len + " count=" + count);

        if (len >= buf.length)
        {
            /* If the request length exceeds the size of the output buffer,
               flush the output buffer and then write the data directly.
               In this way buffered streams will cascade harmlessly. */
            flushBuffer();
            super.write(b, off, len);
            cur_pos = cur_pos + len;
            return;
        }
        if (len > buf.length - count)
        {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
        cur_pos = cur_pos + len;
    }

    public synchronized void seek(long seek_position) throws IOException
    {
        if (seek_position != cur_pos)
        {
            // Log.i("BufOutStream:seek:11:", "buf=" + buf + " cur_pos=" + cur_pos + " seek_position=" + seek_position);
            flushBuffer();
            super.flush();
            count = 0;
            cur_pos = 0;
            buf = new byte[8192];
            super.getChannel().lseek(seek_position, info.guardianproject.libcore.io.OsConstants.SEEK_SET);
            // Log.i("BufOutStream:seek:22:", "buf=" + buf + " cur_pos=" + cur_pos + " seek_position=" + seek_position);
        }
    }

    /**
     * Flushes this buffered output stream. This forces any buffered
     * output bytes to be written out to the underlying output stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void flush() throws IOException
    {
        // Log.i("BufOutStream:flush", "buf=" + buf + " count=" + count);
        flushBuffer();
        super.flush();
    }

    public void close() throws IOException
    {
        // Log.i("BufOutStream:close", "buf=" + buf + " count=" + count);
        flushBuffer();
        super.flush();
    }
}