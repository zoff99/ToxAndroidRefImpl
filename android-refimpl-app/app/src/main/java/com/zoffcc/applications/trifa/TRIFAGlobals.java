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

import java.util.HashMap;
import java.util.Map;

import static com.zoffcc.applications.trifa.MainActivity.dp2px;

public class TRIFAGlobals
{
    static String global_my_toxid = "";
    static String global_my_name = "";
    static String global_my_status_message = "";
    static boolean bootstrapping = false;

    public static final String MY_PACKAGE_NAME = "com.zoffcc.applications.trifa";

    // ----------
    // https://toxme.io/u/echobot
    //  echobot@toxme.io
    final static String ECHOBOT_TOXID = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6";
    // ----------
    // https://toxme.io/u/groupbot
    //  groupbot@toxme.io
    final static String GROUPBOT_TOXID = "56A1ADE4B65B86BCD51CC73E2CD4E542179F47959FE3E0E21B4B0ACDADE51855D34D34D37CB5";

    final static boolean ADD_BOTS_ON_STARTUP = true;
    final static boolean DELETE_SQL_AND_VFS_ON_ERROR = false; // true -> will delete all data on any ERROR with SQL and VFS !!!

    final static String VFS_TMP_FILE_DIR = "/tempdir/files/";
    final static String VFS_FILE_DIR = "/datadir/files/";
    final static String VFS_OWN_AVATAR_DIR = "/datadir/myavatar/";
    static String VFS_PREFIX = ""; // only set for normal (unencrypted) storage

    static boolean orbot_is_really_running = false;

    static int GLOBAL_VIDEO_BITRATE = 2500;
    static int GLOBAL_AUDIO_BITRATE = 64; // allowed values: (xx>=6) && (xx<=510)

    final static int GLOBAL_MIN_VIDEO_BITRATE = 64;
    final static int GLOBAL_MIN_AUDIO_BITRATE = 16; // allowed values: (xx>=6) && (xx<=510)

    static final int CAMPREVIEW_NUM_BUFFERS = 3;

    static final String ORBOT_PROXY_HOST = "127.0.0.1";
    static final long ORBOT_PROXY_PORT = 9050;

    static final String TOXURL_PATTERN = "(?:^|\\s|$)[Tt][Oo][Xx]:[a-fA-F0-9]*";

    static String PREF__DB_secrect_key__user_hash = "";

    static long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES = 150000L; // 150 kBytes // update FT and progress bars every XX bytes
    static long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES = 15000L; // 15 kBytes
    static long UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES = 250000L; // less than this in bytes is a small file

    static int FILE_PICK_METHOD = 2;

    // ---- lookup cache ----
    static Map<String, info.guardianproject.iocipher.FileOutputStream> cache_ft_fos = new HashMap<String, info.guardianproject.iocipher.FileOutputStream>();
    static Map<String, java.io.FileOutputStream> cache_ft_fos_normal = new HashMap<String, java.io.FileOutputStream>();
    // ---- lookup cache ----

    static int CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX = (int) dp2px(10);

    public static enum TRIFA_FT_DIRECTION
    {
        TRIFA_FT_DIRECTION_INCOMING(0), TRIFA_FT_DIRECTION_OUTGOING(1);

        public int value;

        private TRIFA_FT_DIRECTION(int value)
        {
            this.value = value;
        }


    }

    public static enum TRIFA_MSG_TYPE
    {
        TRIFA_MSG_TYPE_TEXT(0), TRIFA_MSG_FILE(1);

        public int value;

        private TRIFA_MSG_TYPE(int value)
        {
            this.value = value;
        }


    }

}
