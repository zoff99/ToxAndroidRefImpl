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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TRIFAGlobals
{
    static String global_my_toxid = "";
    static String global_my_name = "";
    static String global_my_status_message = "";
    static boolean bootstrapping = false;
    static int global_self_connection_status = ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value;
    static long global_self_last_went_online_timestamp = -1;
    static long global_self_last_went_offline_timestamp = -1;
    static int global_tox_self_status = ToxVars.TOX_USER_STATUS.TOX_USER_STATUS_NONE.value;

    static int FULL_SPEED_SECONDS_AFTER_WENT_ONLINE = 60; // 60 secs.
    static int TOX_ITERATE_MILLIS_IN_BATTERY_SAVINGS_MODE = 2000; // 2 secs.

    final static String FRIEND_AVATAR_FILENAME = "_____xyz____avatar.png";

    static boolean HAVE_INTERNET_CONNECTIVITY = true;
    static int TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS = 1000 * 60 * 2; // bootstrap again after 2 minutes offline

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
    // final static String VFS_TMP_AVATAR_DIR = "/avatar_tempdir/files/"; // TODO: avatar should get their own directory!
    final static String VFS_FILE_DIR = "/datadir/files/";
    final static String VFS_OWN_AVATAR_DIR = "/datadir/myavatar/";
    static String VFS_PREFIX = ""; // only set for normal (unencrypted) storage

    static boolean orbot_is_really_running = false;

    final static int HIGHER_GLOBAL_VIDEO_BITRATE = 3500;
    final static int NORMAL_GLOBAL_VIDEO_BITRATE = 2500;
    final static int LOWER_GLOBAL_VIDEO_BITRATE = 250;

    final static int HIGHER_GLOBAL_AUDIO_BITRATE = 64;
    final static int NORMAL_GLOBAL_AUDIO_BITRATE = 20;
    final static int LOWER_GLOBAL_AUDIO_BITRATE = 6;

    static int GLOBAL_VIDEO_BITRATE = NORMAL_GLOBAL_VIDEO_BITRATE; // this works nice: 2500;
    static int GLOBAL_AUDIO_BITRATE = LOWER_GLOBAL_AUDIO_BITRATE; // allowed values: (xx>=6) && (xx<=510)

    static int VIDEO_FRAME_RATE_OUTGOING = 0;
    static long last_video_frame_sent = -1;
    static int count_video_frame_sent = 0;
    static int VIDEO_FRAME_RATE_INCOMING = 0;
    static long last_video_frame_received = -1;
    static int count_video_frame_received = 0;

    final static int VIDEO_ENCODER_MAX_QUANTIZER_LOW = 63;
    final static int VIDEO_ENCODER_MAX_QUANTIZER_MED = 45;
    final static int VIDEO_ENCODER_MAX_QUANTIZER_HIGH = 20;

    final static int VIDEO_ENCODER_MAX_BITRATE_LOW = 120;
    final static int VIDEO_ENCODER_MAX_BITRATE_MED = 200;
    final static int VIDEO_ENCODER_MAX_BITRATE_HIGH = 400;

    final static int GLOBAL_MAX_VIDEO_BITRATE = 1600;
    final static int GLOBAL_MIN_VIDEO_BITRATE = 100;
    final static int GLOBAL_MIN_AUDIO_BITRATE = 6; // allowed values: (xx>=6) && (xx<=510)

    static final int CAMPREVIEW_NUM_BUFFERS = 3;

    static final String ORBOT_PROXY_HOST = "127.0.0.1";
    static final long ORBOT_PROXY_PORT = 9050;

    static final String TOX_NODELIST_HOST = "nodes.tox.chat";
    static final String TOX_NODELIST_URL = "https://" + TOX_NODELIST_HOST + "/json";

    static final String TOXURL_PATTERN = "(?:^|\\s|$)[Tt][Oo][Xx]:[a-fA-F0-9]*";

    static String PREF__DB_secrect_key__user_hash = "";

    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES = 150000L; // 150 kBytes // update FT and progress bars every XX bytes
    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES = 15000L; // 15 kBytes
    static final long UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES = 250000L; // less than this in bytes is a small file

    static final int FILE_PICK_METHOD = 2;
    static final String TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY = "-1";

    static final int FL_NOTIFICATION_ICON_ALPHA_SELECTED = 135;
    static final int FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED = 50;
    static final int FL_NOTIFICATION_ICON_SIZE_DP_SELECTED = 90;
    static final int FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED = 15;

    static final int VIDEO_CODEC_VP8 = 0;
    static final int VIDEO_CODEC_H264 = 1;

    static final int MAX_LEN_TOXENCRYPTSAVE_PASSPHRASE = 256;
    static final int LEN_TRIFA_AUTOGEN_PASSWORD = 32;

    static final int USE_MAX_NUMBER_OF_BOOTSTRAP_NODES = 8;

    // ---- lookup cache ----
    static Map<String, info.guardianproject.iocipher.FileOutputStream> cache_ft_fos = new HashMap<String, info.guardianproject.iocipher.FileOutputStream>();
    static Map<String, java.io.FileOutputStream> cache_ft_fos_normal = new HashMap<String, java.io.FileOutputStream>();
    // ---- lookup cache ----

    static List<BootstrapNodeEntryDB> bootstrap_node_list = new ArrayList<>();
    static List<BootstrapNodeEntryDB> tcprelay_node_list = new ArrayList<>();


    static long LAST_ONLINE_TIMSTAMP_ONLINE_NOW = Long.MAX_VALUE - 1;
    static long LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE = -1;

    static int CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX = 10;
    static int CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX = 20;

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
