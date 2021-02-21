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

// inspired by https://github.com/commonsguy/cw-omnibus/tree/master/ContentProvider/Pipe

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;

public class IOCipherContentProvider extends ContentProvider
{
    public static final String TAG = "IOCipherContentProvider";
    public static final Uri FILES_URI = Uri.parse("content://com.zoffcc.applications.trifa/");
    private MimeTypeMap mimeTypeMap;

    @Override
    public boolean onCreate()
    {
        mimeTypeMap = MimeTypeMap.getSingleton();
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return mimeTypeMap.getMimeTypeFromExtension(fileExtension);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
    {
        ParcelFileDescriptor[] pipe = null;
        InputStream in = null;

        try
        {
            pipe = ParcelFileDescriptor.createPipe();
            String path = uri.getPath();
            // Log.i(TAG, "streaming " + path);
            // BufferedInputStream could help, AutoCloseOutputStream conflicts
            in = new FileInputStream(new File(path));
            new PipeFeederThread(in, new AutoCloseOutputStream(pipe[1])).start();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error opening pipe", e);
            throw new FileNotFoundException("Could not open pipe for: " + uri.toString());
        }

        return (pipe[0]);
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort)
    {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        throw new RuntimeException("Operation not supported");
    }

    static class PipeFeederThread extends Thread
    {
        InputStream in;
        OutputStream out;

        PipeFeederThread(InputStream in, OutputStream out)
        {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run()
        {
            byte[] buf = new byte[8192]; // TODO: is 8k here OK? in the example it's 1k
            int len;

            try
            {
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }

                in.close();
                out.flush();
                out.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "File transfer failed:", e);
            }
        }
    }
}
