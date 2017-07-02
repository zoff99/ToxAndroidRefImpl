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


public class Message_model
{
    public static final int TEXT_INCOMING_NOT_READ = 1001;
    public static final int TEXT_INCOMING_HAVE_READ = 1002;

    public static final int TEXT_OUTGOING_NOT_READ = 2001;
    public static final int TEXT_OUTGOING_HAVE_READ = 2002;

    public static final int FILE_INCOMING_STATE_CANCEL = 3001;
    public static final int FILE_INCOMING_STATE_PAUSE_NOT_YET_ACCEPTED = 3002;
    public static final int FILE_INCOMING_STATE_PAUSE_HAS_ACCEPTED = 3003;
    public static final int FILE_INCOMING_STATE_RESUME = 3004;

    public static final int FILE_OUTGOING_STATE_CANCEL = 4001;
    public static final int FILE_OUTGOING_STATE_PAUSE_NOT_YET_STARTED = 4002;
    public static final int FILE_OUTGOING_STATE_PAUSE_NOT_YET_ACCEPTED = 4003;
    public static final int FILE_OUTGOING_STATE_PAUSE_HAS_ACCEPTED = 4004;
    public static final int FILE_OUTGOING_STATE_RESUME = 4005;

    public static final int ERROR_UNKNOWN = 9999;
}
