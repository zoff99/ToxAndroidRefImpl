/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2022 Zoff <zoff@zoff.cc>
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

import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_PRIVACY_STATE;

@Table
public class GroupDB
{
    // group id is always saved as lower case hex string!! -----------------
    @PrimaryKey
    String group_identifier = "";
    // group id is always saved as lower case hex string!! -----------------

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String who_invited__tox_public_key_string = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String name = "";

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String topic = "";

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long peer_count = -1;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long own_peer_number = -1;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int privacy_state = TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value;

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    long tox_group_number = -1; // this changes often!!

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean group_active = false; // is this conference active now? are we invited?

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean group_we_left = false; // did we "leave" keeping our credentials? (we can rejoin)

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    @Nullable
    boolean notification_silent = false; // show notifications for this conference?

    // ______@@SORMA_END@@______

}
