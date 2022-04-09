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

/***
 Copyright (c) 2014-2015 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.zoffcc.applications.trifa;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.net.URLConnection;

abstract class AbstractFileProvider extends ContentProvider
{
    public static final String TAG = "AbstractFileProvider";
    private final static String[] OPENABLE_PROJECTION = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Log.i(TAG, "query:" + uri + " sel=" + selection);

        if (projection == null)
        {
            projection = OPENABLE_PROJECTION;
        }

        final MatrixCursor cursor = new MatrixCursor(projection, 1);

        MatrixCursor.RowBuilder b = cursor.newRow();

        for (String col : projection)
        {
            if (OpenableColumns.DISPLAY_NAME.equals(col))
            {
                b.add(getFileName(uri));
            }
            else if (OpenableColumns.SIZE.equals(col))
            {
                b.add(getDataLength(uri));
            }
            else
            { // unknown, so just add null
                b.add(null);
            }
        }

        // Log.i(TAG, "query:ret=" + cursor);
        return (new LegacyCompatCursorWrapper(cursor));
    }

    @Override
    public String getType(Uri uri)
    {
        // Log.i(TAG, "getType:ret=" + URLConnection.guessContentTypeFromName(uri.toString()));
        return (URLConnection.guessContentTypeFromName(uri.toString()));
    }

    protected String getFileName(Uri uri)
    {
        // Log.i(TAG, "getFileName:ret=" + uri.getLastPathSegment());
        return (uri.getLastPathSegment());
    }

    public long getDataLength(Uri uri)
    {
        // Log.i(TAG, "getDataLength:ret=" + AssetFileDescriptor.UNKNOWN_LENGTH);
        return (AssetFileDescriptor.UNKNOWN_LENGTH);
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
}
