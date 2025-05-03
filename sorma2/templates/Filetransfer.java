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

package com.zoffcc.applications.sorm;

import com.zoffcc.applications.sorm.Column;
import com.zoffcc.applications.sorm.PrimaryKey;
import com.zoffcc.applications.sorm.Table;

import androidx.annotation.Nullable;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;

@Table
public class Filetransfer
{
    @PrimaryKey(autoincrement = true, auto = true)
    long id; // unique ID!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_public_key_string = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = TRIFA_FT_DIRECTION_INCOMING.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long file_number = -1; // given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int kind = TOX_FILE_KIND_DATA.value;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int state = TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_outgoing_started = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String path_name = "";

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String file_name = "";

    @Column(defaultExpr = "false")
    boolean fos_open = false;

    @Column(defaultExpr = "-1")
    long filesize = -1;

    @Column(defaultExpr = "0")
    long current_position = 0;

    @Column(indexed = true, defaultExpr = "-1")
    long message_id; // f_key -> Message.id

    @Column(indexed = true, defaultExpr = "false")
    boolean storage_frame_work = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String tox_file_id_hex = "";

    // ______@@SORMA_END@@______
}
