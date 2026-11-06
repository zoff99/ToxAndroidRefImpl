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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.ProxyFileDescriptorCallback;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;

import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_open_encrypted_file_via_intent;

public class IOCipherContentProvider extends AbstractFileProvider
{
    public static final String TAG = "IOCipherContentProvider";
    public static final Uri FILES_URI = Uri.parse("content://com.zoffcc.applications.trifa.ext1_fileprovider/");
    private static final int PIPE_BLOCKSIZE = 8192; // do not change this. its the blocksize of IOCipher.
    private MimeTypeMap mimeTypeMap;

    @Override
    public boolean onCreate()
    {
        mimeTypeMap = MimeTypeMap.getSingleton();
        return true;
    }

    @Override
    public long getDataLength(Uri uri)
    {
        if (!PREF__allow_open_encrypted_file_via_intent)
        {
            // Log.i(TAG, "getDataLength:ret:001:allow_open_encrypted_file_via_intent");
            return 0;
        }

        String path = uri.getPath();
        long filesize = new File(path).length();
        if ((filesize < 1) || (filesize > (Long.MAX_VALUE - 2)))
        {
            filesize = AssetFileDescriptor.UNKNOWN_LENGTH;
        }
        // Log.i(TAG, "getDataLength:ret=" + filesize);

        return filesize;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
    {
        if (!PREF__allow_open_encrypted_file_via_intent)
        {
            // Log.i(TAG, "openFile:err:002:allow_open_encrypted_file_via_intent");
            throw new FileNotFoundException("No Access");
        }

        final String path = uri.getPath();

        // --- ANDROID 8.0+ (API 26) PROXY FILE DESCRIPTOR ---
        // This allows native apps (PDF/Video viewers) to use kernel-level lseek() on our virtual file system
        // without decrypting to a temporary file on disk. It intercepts native read/seek calls and routes them
        // to our Java callback, keeping the file strictly in memory/encrypted storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                StorageManager sm = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);

                // We need a dedicated background thread to handle the asynchronous read/seek requests from the kernel
                HandlerThread ht = new HandlerThread("IOCipherProxy_" + System.currentTimeMillis());
                ht.start();
                Handler handler = new Handler(ht.getLooper());

                return sm.openProxyFileDescriptor(ParcelFileDescriptor.MODE_READ_ONLY, new ProxyFileDescriptorCallback() {
                    private info.guardianproject.iocipher.FileInputStream currentStream;
                    private long currentStreamPos = 0;
                    private final Object lock = new Object();

                    /**
                     * Helper to ensure our InputStream is at the exact byte offset requested by the viewer app.
                     * Since standard InputStreams cannot seek backwards efficiently, we reopen the stream if needed.
                     */
                    private void ensureStreamAt(long offset) throws IOException {
                        synchronized (lock) {
                            if (currentStream == null) {
                                currentStream = new info.guardianproject.iocipher.FileInputStream(new info.guardianproject.iocipher.File(path));
                                currentStreamPos = 0;
                            }
                            if (offset != currentStreamPos) {
                                if (offset < currentStreamPos) {
                                    // IOCipher streams cannot seek backwards, so we must reopen and skip forward
                                    currentStream.close();
                                    currentStream = new info.guardianproject.iocipher.FileInputStream(new info.guardianproject.iocipher.File(path));
                                    currentStreamPos = 0;
                                }
                                long toSkip = offset - currentStreamPos;
                                while (toSkip > 0) {
                                    long skipped = currentStream.skip(toSkip);
                                    if (skipped <= 0) break; // EOF or error
                                    toSkip -= skipped;
                                    currentStreamPos += skipped;
                                }
                            }
                        }
                    }

                    @Override
                    public long onGetSize() throws ErrnoException {
                        return new info.guardianproject.iocipher.File(path).length();
                    }

                    @Override
                    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
                        try {
                            ensureStreamAt(offset);
                            synchronized (lock) {
                                int bytesRead = currentStream.read(data, 0, size);
                                if (bytesRead > 0) {
                                    currentStreamPos += bytesRead;
                                }
                                return bytesRead;
                            }
                        } catch (IOException e) {
                            throw new ErrnoException("onRead", OsConstants.EIO);
                        }
                    }

                    @Override
                    public void onFsync() throws ErrnoException {
                        // Read-only virtual file, nothing to sync to disk
                    }

                    @Override
                    public void onRelease() {
                        // Called when the external app closes the file. Clean up resources.
                        synchronized (lock) {
                            if (currentStream != null) {
                                try { currentStream.close(); } catch (IOException ignored) {}
                                currentStream = null;
                            }
                        }
                        ht.quitSafely();
                    }
                }, handler);
            } catch (Exception e) {
                Log.e(TAG, "Error opening ProxyFileDescriptor, falling back to pipe", e);
            }
        }

        // --- FALLBACK FOR ANDROID < 8.0 (PIPE) ---
        // Pipes do not support seeking (lseek), but we keep this for legacy devices.
        // Sharing apps (Telegram, Drive) will still work fine on this fallback.
        ParcelFileDescriptor[] pipe = null;
        InputStream in = null;

        try
        {
            pipe = ParcelFileDescriptor.createPipe();
            // Log.i(TAG, "openFile:streaming " + path + " mode=" + mode + "tid = " + Thread.currentThread().getId());

            long filesize = new File(path).length();
            if ((filesize < 1) || (filesize > (Long.MAX_VALUE - 2)))
            {
                filesize = AssetFileDescriptor.UNKNOWN_LENGTH;
            }
            // Log.i(TAG, "openFile:getDataLength:ret=" + filesize);

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
            byte[] buf = new byte[PIPE_BLOCKSIZE];
            int len;

            try
            {
                while ((len = in.read(buf)) >= 0)
                {
                    // Log.i(TAG, "write_data:" + len + " buf.length=" + buf.length + " buf=" + buf + " tid=" +
                    //            Thread.currentThread().getId());
                    out.write(buf, 0, len);
                }

                in.close();
                out.flush();
                out.close();
            }
            catch (IOException e)
            {
                // Log.e(TAG, "File transfer failed:", e);
                try
                {
                    in.close();
                }
                catch (Exception ignored)
                {
                }
                try
                {
                    out.flush();
                }
                catch (Exception ignored)
                {
                }
                try
                {
                    out.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }
}
