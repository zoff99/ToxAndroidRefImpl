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
    static long global_last_activity_for_battery_savings_ts = -1;
    static long global_self_last_entered_battery_saving_timestamp = -1;
    static boolean global_showing_messageview = false;
    static boolean global_showing_anygroupview = false;
    static int global_tox_self_status = ToxVars.TOX_USER_STATUS.TOX_USER_STATUS_NONE.value;

    static String global_notification_token = null;
    final static String NOTIFICATION_TOKEN_DB_KEY = "NotificationToken";
    final static String NOTIFICATION_TOKEN_DB_KEY_NEED_ACK = "NotificationTokenNeedACK";
    final static String NOTIFICATION_FCM_PUSH_URL_PREFIX = "https://tox.zoff.xyz/toxfcm/fcm.php?id=";
    final static String NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD = "https://toxcon2020.zoff.cc/toxfcm/fcm.php?id=";
    final static String NOTIFICATION_UP_PUSH_URL_PREFIX = "https://gotify1.unifiedpush.org/UP?token=";

    final static String TOX_PUSH_MSG_APP = "https://github.com/zoff99/tox_push_msg_app/releases/latest/download/play.pushmsg.apk";

    final static String FRIEND_AVATAR_FILENAME = "_____xyz____avatar.png";

    static boolean HAVE_INTERNET_CONNECTIVITY = true;
    final static int TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS =
            1000 * 60 * 2; // bootstrap again after 2 minutes offline
    final static int SECONDS_TO_STAY_ONLINE_IN_BATTERY_SAVINGS_MODE = 120; // x secs.
    static long BATTERY_OPTIMIZATION_SLEEP_IN_MILLIS = 15 * 1000 * 60; // 15 minutes default
    static int BATTERY_OPTIMIZATION_LAST_SLEEP1 = -1;
    static int BATTERY_OPTIMIZATION_LAST_SLEEP2 = -1;
    static int BATTERY_OPTIMIZATION_LAST_SLEEP3 = -1;

    static int AUTO_ACCEPT_FT_MAX_IMAGE_SIZE_IN_MB = 12;
    static int AUTO_ACCEPT_FT_MAX_VIDEO_SIZE_IN_MB = 40;
    static int AUTO_ACCEPT_FT_MAX_ANYKIND_SIZE_IN_MB = 200;

    public static final String MY_PACKAGE_NAME = "com.zoffcc.applications.trifa";
    public static final int CONFERENCE_COOKIE_LENGTH = 35;
    public static final int CONFERENCE_ID_LENGTH = 32;

    public static final String TEXT_QUOTE_STRING_1 = "----\n";
    public static final String TEXT_QUOTE_STRING_2 = "\n----";

    // ----------
    // https://toxme.io/u/echobot
    //  echobot@toxme.io
    final static String ECHOBOT_TOXID = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6";
    final static String TOXIRC_TOKTOK_CONFID = "b0d5292414685a53341d8126b67dfe260baf5881c9aff48a6ea211dcf3bfe34f";
    final static String TOXIRC_PUBKEY = "A922A51E1C91205B9F7992E2273107D47C72E8AE909C61C28A77A4A2A115431B";
    // ----------

    final static boolean ADD_BOTS_ON_STARTUP = true;
    final static boolean DELETE_SQL_AND_VFS_ON_ERROR = false; // true -> will delete all data on any ERROR with SQL and VFS !!!

    final static String VFS_TMP_FILE_DIR = "/tempdir/files/";
    // final static String VFS_TMP_AVATAR_DIR = "/avatar_tempdir/files/"; // TODO: avatar should get their own directory!
    final static String VFS_FILE_DIR = "/datadir/files/";
    final static String VFS_OWN_AVATAR_DIR = "/datadir/myavatar/";
    static String VFS_PREFIX = ""; // only set for normal (unencrypted) storage

    final static long AVATAR_INCOMING_MAX_BYTE_SIZE = 1 * 1024 * 1024; // limit incoming avatars at 1MByte size
    final static long AVATAR_SELF_MAX_BYTE_SIZE = 1 * 1024 * 1024; // limit incoming avatars at 1MByte size

    static boolean orbot_is_really_running = false;

    final static int HIGHER_GLOBAL_VIDEO_BITRATE = 2500;
    final static int NORMAL_GLOBAL_VIDEO_BITRATE = 1200;
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

    final static int VIDEO_DECODER_BUFFER_DELAY = 0; // 50; // delay video and audio playback this many milliseconds
    final static int DECODER_VIDEO_ADD_DELAY_MS = 0; // -80; // additionally delay audio playback this many milliseconds

    final static int VIDEO_ENCODER_MAX_QUANTIZER_LOW = 63;
    final static int VIDEO_ENCODER_MAX_QUANTIZER_MED = 45;
    final static int VIDEO_ENCODER_MAX_QUANTIZER_HIGH = 10;

    final static int VIDEO_ENCODER_MAX_BITRATE_LOW = 250;
    final static int VIDEO_ENCODER_MAX_BITRATE_MED = 1200;
    final static int VIDEO_ENCODER_MAX_BITRATE_HIGH = 2500;

    final static int VIDEO_ENCODER_MIN_BITRATE_LOW = 0; // use 0 here since other factors will set a limit anyway
    final static int VIDEO_ENCODER_MIN_BITRATE_MED = 400;
    final static int VIDEO_ENCODER_MIN_BITRATE_HIGH = 1000;

    final static int GLOBAL_MIN_VIDEO_BITRATE = 100;
    final static int GLOBAL_MIN_AUDIO_BITRATE = 6; // allowed values: (xx>=6) && (xx<=510)

    final static int GLOBAL_INIT_PLAY_DELAY = 100;
    final static String GLOBAL_PLAY_DELAY_SETTING_NAME = "video_play_delay_ms4";

    static final int CAMPREVIEW_NUM_BUFFERS = 4;

    static final String ORBOT_PROXY_HOST = "127.0.0.1";
    static final long ORBOT_PROXY_PORT = 9050;

    static final String TOX_NODELIST_HOST = "nodes.tox.chat";
    static final String TOX_NODELIST_URL = "https://" + TOX_NODELIST_HOST + "/json";

    static final String TOXURL_PATTERN = "(?:^|\\s|$)[Tt][Oo][Xx]:[a-fA-F0-9]*";

    static String PREF__DB_secrect_key__user_hash = "";

    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES = 250000L; // 250 kBytes // update FT and progress bars every XX bytes
    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES = 15000L; // 15 kBytes
    static final long UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES = 250000L; // 250 kByzes, less than this in bytes is a small file

    static final int FILE_PICK_METHOD = 2;
    static final String TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY = "-1";

    static final int FL_NOTIFICATION_ICON_ALPHA_SELECTED = 135;
    static final int FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED = 50;
    static final int FL_NOTIFICATION_ICON_SIZE_DP_SELECTED = 90;
    static final int FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED = 15;

    static final String TOX_SERVICE_NOTIFICATION_TEXT_COLOR = "#3498DB"; // blue-ish

    static final int VIDEO_CODEC_VP8 = 0;
    static final int VIDEO_CODEC_H264 = 1;

    static final int MAX_LEN_TOXENCRYPTSAVE_PASSPHRASE = 256;
    static final int LEN_TRIFA_AUTOGEN_PASSWORD = 32;

    static final int USE_MAX_NUMBER_OF_BOOTSTRAP_NODES = 10;
    static final int USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS = 10;

    // ---- lookup cache ----
    // static Map<String, info.guardianproject.iocipher.RandomAccessFile> cache_ft_fos = new HashMap<String, info.guardianproject.iocipher.RandomAccessFile>();
    static Map<String, BufferedOutputStreamCustom> cache_ft_fos = new HashMap<String, BufferedOutputStreamCustom>();
    // static Map<String, java.io.FileOutputStream> cache_ft_fos_normal = new HashMap<String, java.io.FileOutputStream>();
    static Map<String, PositionInputStream> cache_ft_fis_saf = new HashMap<String, PositionInputStream>();
    // ---- lookup cache ----

    static List<BootstrapNodeEntryDB> bootstrap_node_list = new ArrayList<>();
    static List<BootstrapNodeEntryDB> tcprelay_node_list = new ArrayList<>();

    static final int[] MESSAGE_TEXT_SIZE = {9, 11, 15, 20}; // values in "sp"
    static final int[] MESSAGE_EMOJI_SIZE = {13, 18, 25, 36}; // values in "dp"
    static final int MESSAGE_TEXT_SIZE_FT_SMALL = 12;
    static final int MESSAGE_TEXT_SIZE_FT_NORMAL = 13;
    static final int[] MESSAGE_EMOJI_ONLY_EMOJI_SIZE = {13 * 2, 18 * 2, 25 * 2, 36 * 2}; // values in "dp"

    static long LAST_ONLINE_TIMSTAMP_ONLINE_NOW = Long.MAX_VALUE - 1;
    static long LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE = -1;

    static long ONE_HOUR_IN_MS = 3600 * 1000;
    static int MESSAGES_TIMEDELTA_NO_TIMESTAMP_MS = 30 * 1000;

    static int CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX = 10;
    static int CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX = 20;

    static int MESSAGE_SYNC_DOUBLE_INTERVAL_SECS = 20;

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


    public static enum CONTROL_PROXY_MESSAGE_TYPE
    {
        CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY(175), CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND(
            176), CONTROL_PROXY_MESSAGE_TYPE_ALL_MESSAGES_SENT(177), CONTROL_PROXY_MESSAGE_TYPE_PROXY_KILLSWITCH(
            178), CONTROL_PROXY_MESSAGE_TYPE_NOTIFICATION_TOKEN(179), CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND(
            181);

        public int value;

        private CONTROL_PROXY_MESSAGE_TYPE(int value)
        {
            this.value = value;
        }
    }

    public static enum NOTIFICATION_EDIT_ACTION
    {
        NOTIFICATION_EDIT_ACTION_CLEAR(0), NOTIFICATION_EDIT_ACTION_ADD(1), NOTIFICATION_EDIT_ACTION_REMOVE(
            2), NOTIFICATION_EDIT_ACTION_EMPTY_THE_LIST(3);

        public int value;

        private NOTIFICATION_EDIT_ACTION(int value)
        {
            this.value = value;
        }
    }

}
