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

import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class File
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int kind = TOX_FILE_KIND_DATA.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String full_path_name = "";

    static File deep_copy(File in)
    {
        File out = new File();
        out.kind = in.kind;
        out.full_path_name = in.full_path_name;
        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", kind=" + kind + ", full_path_name=" + full_path_name;
    }
}
