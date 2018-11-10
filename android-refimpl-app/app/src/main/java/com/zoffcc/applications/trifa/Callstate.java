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


import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_VP8;

public class Callstate
{
    static int state = 0; // 0 -> not in a call, 1 -> ringing/calling
    static int tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value;
    static String friend_pubkey = "-1";
    static String friend_alias_name = "";
    static int other_audio_enabled = 1;
    static int other_video_enabled = 1;
    static int my_audio_enabled = 1;
    static int my_video_enabled = 1;
    static long frame_width_px = -1;
    static long frame_height_px = -1;
    static long ystride = -1;
    static long ustride = -1;
    static long vstride = -1;
    static long audio_bitrate = GLOBAL_AUDIO_BITRATE;
    static long video_bitrate = GLOBAL_VIDEO_BITRATE;
    static long video_in_bitrate = 0;
    static long video_out_codec = VIDEO_CODEC_VP8;
    static long video_in_codec = VIDEO_CODEC_VP8;
    static int accepted_call = 0;
    static long call_init_timestamp = -1L; // when it starts ringing (someone calls us)
    static long call_start_timestamp = -1L; // when we actually start the call (someone calls us)
    static long call_first_video_frame_received = -1L; // when we receive the first video frame (someone calls us)
    static long call_first_audio_frame_received = -1L; // when we receive the first audio frame (someone calls us)
    static boolean camera_opened = false;
    static boolean audio_speaker = true; // true -> loudspeaker, false -> for your ear-speaker
    static int audio_device = 0; // 0 -> phone, 1 -> headset, 2 -> bluetoothdevice
    static long play_delay = 0;

    static void reset_values()
    {
        Callstate.state = 0;
        Callstate.call_first_video_frame_received = -1;
        Callstate.call_first_audio_frame_received = -1;
        Callstate.call_start_timestamp = -1;
        Callstate.call_init_timestamp = -1;
        Callstate.friend_pubkey = "-1";
        Callstate.friend_alias_name = "";
        Callstate.other_audio_enabled = 1;
        Callstate.other_video_enabled = 1;
        Callstate.my_audio_enabled = 1;
        Callstate.my_video_enabled = 1;
        Callstate.audio_bitrate = GLOBAL_AUDIO_BITRATE;
        Callstate.video_bitrate = GLOBAL_VIDEO_BITRATE;
        Callstate.video_in_bitrate = 0;
        Callstate.video_out_codec = VIDEO_CODEC_VP8;
        Callstate.video_in_codec = VIDEO_CODEC_VP8;
        Callstate.accepted_call = 0;
        Callstate.audio_speaker = true;
        Callstate.audio_device = 0;
        Callstate.play_delay = 0;
        MainActivity.set_av_call_status(Callstate.state);
    }

    public static String codec_to_str(long v)
    {
        if (v == VIDEO_CODEC_VP8)
        {
            return "VP8";
        }
        else
        {
            return "H264";
        }
    }
}
