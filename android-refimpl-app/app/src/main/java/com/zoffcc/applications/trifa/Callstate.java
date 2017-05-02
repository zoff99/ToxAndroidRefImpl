package com.zoffcc.applications.trifa;


public class Callstate
{
    static int state = 0; // 0 -> not in a call, 1 -> ringing/calling
    static int tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value;
    static long friend_number = -1;
    static int other_audio_enabled = 0;
    static int other_video_enabled = 0;
    static int my_audio_enabled = 0;
    static int my_video_enabled = 0;
    static long audio_bitrate = 0;
    static long video_bitrate = 0;
    static long call_init_timestamp = -1L; // when it starts ringing (someone calls us)
    static long call_start_timestamp = -1L; // when we actually start the call (someone calls us)
    static long call_first_video_frame_received = -1L; // when we receive the first video frame (someone calls us)
}
