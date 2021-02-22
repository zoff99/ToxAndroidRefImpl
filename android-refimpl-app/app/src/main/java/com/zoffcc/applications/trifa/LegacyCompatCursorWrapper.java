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
 Copyright (c) 2015-2016 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.zoffcc.applications.trifa;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.util.Arrays;

import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

/**
 * Wraps the Cursor returned by an ordinary FileProvider,
 * StreamProvider, or other ContentProvider. If the query()
 * requests _DATA or MIME_TYPE, adds in some values for
 * that column, so the client getting this Cursor is less
 * likely to crash. Of course, clients should not be requesting
 * either of these columns in the first place...
 */
public class LegacyCompatCursorWrapper extends CursorWrapper
{
    final private int fakeDataColumn;
    final private int fakeMimeTypeColumn;
    final private String mimeType;
    final private Uri uriForDataColumn;

    /**
     * Constructor.
     *
     * @param cursor the Cursor to be wrapped
     */
    public LegacyCompatCursorWrapper(Cursor cursor)
    {
        this(cursor, null);
    }

    /**
     * Constructor.
     *
     * @param cursor   the Cursor to be wrapped
     * @param mimeType the MIME type of the content represented
     *                 by the Uri that generated this Cursor, should
     *                 we need it
     */
    public LegacyCompatCursorWrapper(Cursor cursor, String mimeType)
    {
        this(cursor, mimeType, null);
    }

    /**
     * Constructor.
     *
     * @param cursor           the Cursor to be wrapped
     * @param mimeType         the MIME type of the content represented
     *                         by the Uri that generated this Cursor, should
     *                         we need it
     * @param uriForDataColumn Uri to return for the _DATA column
     */
    public LegacyCompatCursorWrapper(Cursor cursor, String mimeType, Uri uriForDataColumn)
    {
        super(cursor);

        this.uriForDataColumn = uriForDataColumn;

        if (cursor.getColumnIndex(DATA) >= 0)
        {
            fakeDataColumn = -1;
        }
        else
        {
            fakeDataColumn = cursor.getColumnCount();
        }

        if (cursor.getColumnIndex(MIME_TYPE) >= 0)
        {
            fakeMimeTypeColumn = -1;
        }
        else if (fakeDataColumn == -1)
        {
            fakeMimeTypeColumn = cursor.getColumnCount();
        }
        else
        {
            fakeMimeTypeColumn = fakeDataColumn + 1;
        }

        this.mimeType = mimeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount()
    {
        int count = super.getColumnCount();

        if (!cursorHasDataColumn())
        {
            count += 1;
        }

        if (!cursorHasMimeTypeColumn())
        {
            count += 1;
        }

        return (count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndex(String columnName)
    {
        if (!cursorHasDataColumn() && DATA.equalsIgnoreCase(columnName))
        {
            return (fakeDataColumn);
        }

        if (!cursorHasMimeTypeColumn() && MIME_TYPE.equalsIgnoreCase(columnName))
        {
            return (fakeMimeTypeColumn);
        }

        return (super.getColumnIndex(columnName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int columnIndex)
    {
        if (columnIndex == fakeDataColumn)
        {
            return (DATA);
        }

        if (columnIndex == fakeMimeTypeColumn)
        {
            return (MIME_TYPE);
        }

        return (super.getColumnName(columnIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getColumnNames()
    {
        if (cursorHasDataColumn() && cursorHasMimeTypeColumn())
        {
            return (super.getColumnNames());
        }

        String[] orig = super.getColumnNames();
        String[] result = Arrays.copyOf(orig, getColumnCount());

        if (!cursorHasDataColumn())
        {
            result[fakeDataColumn] = DATA;
        }

        if (!cursorHasMimeTypeColumn())
        {
            result[fakeMimeTypeColumn] = MIME_TYPE;
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(int columnIndex)
    {
        if (!cursorHasDataColumn() && columnIndex == fakeDataColumn)
        {
            if (uriForDataColumn != null)
            {
                return (uriForDataColumn.toString());
            }

            return (null);
        }

        if (!cursorHasMimeTypeColumn() && columnIndex == fakeMimeTypeColumn)
        {
            return (mimeType);
        }

        return (super.getString(columnIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType(int columnIndex)
    {
        if (!cursorHasDataColumn() && columnIndex == fakeDataColumn)
        {
            return (Cursor.FIELD_TYPE_STRING);
        }

        if (!cursorHasMimeTypeColumn() && columnIndex == fakeMimeTypeColumn)
        {
            return (Cursor.FIELD_TYPE_STRING);
        }

        return (super.getType(columnIndex));
    }

    /**
     * @return true if the Cursor has a _DATA column, false otherwise
     */
    private boolean cursorHasDataColumn()
    {
        return (fakeDataColumn == -1);
    }

    /**
     * @return true if the Cursor has a MIME_TYPE column, false
     * otherwise
     */
    private boolean cursorHasMimeTypeColumn()
    {
        return (fakeMimeTypeColumn == -1);
    }
}
