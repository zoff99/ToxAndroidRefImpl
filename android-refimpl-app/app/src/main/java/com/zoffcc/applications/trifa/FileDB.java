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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class FileDB
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int kind = TOX_FILE_KIND_DATA.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = TRIFA_FT_DIRECTION_INCOMING.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_public_key_string = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String file_name = "";

    @Column(defaultExpr = "-1", indexed = true, helpers = Column.Helpers.ALL)
    long filesize = -1;

    @Column(indexed = true, defaultExpr = "true", helpers = Column.Helpers.ALL)
    boolean is_in_VFS = true;

    static FileDB deep_copy(FileDB in)
    {
        FileDB out = new FileDB();
        out.kind = in.kind;
        out.direction = in.direction;
        out.tox_public_key_string = in.tox_public_key_string;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.filesize = in.filesize;
        out.is_in_VFS = in.is_in_VFS;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", kind=" + kind + ", is_in_VFS=" + is_in_VFS + ", path_name=" + path_name + ", file_name" + file_name + ", filesize=" + filesize + ", direction=" + direction;
    }
}
