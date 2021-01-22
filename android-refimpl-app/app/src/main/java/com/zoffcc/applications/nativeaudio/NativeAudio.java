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

package com.zoffcc.applications.nativeaudio;

import android.util.Log;

import com.zoffcc.applications.trifa.AudioRecording;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import static com.zoffcc.applications.trifa.AudioReceiver.reinit_audio_play_buffers;
import static com.zoffcc.applications.trifa.AudioRecording.microphone_muted;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_eac_delay_ms;
import static com.zoffcc.applications.trifa.MainActivity.PREF_mic_gain_factor;

public class NativeAudio
{
    private static final String TAG = "trifa.NativeAudio";

    public static final int n_audio_in_buffer_max_count = 10; // NORM= 5;
    public static ByteBuffer[] n_audio_buffer = new ByteBuffer[n_audio_in_buffer_max_count];
    public static int n_cur_buf = 0;
    public static int n_buf_size_in_bytes = 0;
    public static int n_buf_iterate_ms = 40; // fixed ms interval for audio play (call and groups)
    public static int[] n_bytes_in_buffer = new int[n_audio_in_buffer_max_count];
    public static int sampling_rate = 48000;
    public static int channel_count = 2;

    public static final int n_rec_audio_in_buffer_max_count = 3; // BAD: !!! always keep in sync with native-audio-jni.c `int num_rec_bufs = 2;` !!!
    public static ByteBuffer[] n_rec_audio_buffer = new ByteBuffer[n_rec_audio_in_buffer_max_count];
    public static int n_rec_cur_buf = 0;
    public static int n_rec_buf_size_in_bytes = 0;
    public static int[] n_rec_bytes_in_buffer = new int[n_rec_audio_in_buffer_max_count];

    public static boolean native_audio_engine_down = false;
    public static Semaphore semaphore_audioprocessing_02 = new Semaphore(1);

    /**
     * problem switching to audio only
     * <p>
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:--> EAR
     * com.zoffcc.applications.trifa I/trifa.ToxService: I:setting filteraudio_active=0
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:setMode(AudioManager.MODE_IN_COMMUNICATION)
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: turnOffScreen
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:turnOffScreen()
     * com.zoffcc.applications.trifa I/System.out: AVCS:VOICE:1
     * com.zoffcc.applications.trifa I/trifa.nativeaudio: player_state:res_011=0 SL_RESULT_SUCCESS=0 PAUSED
     * com.zoffcc.applications.trifa W/AudioSystem: AudioPolicyService server died!
     * com.zoffcc.applications.trifa W/AudioSystem: AudioFlinger server died!
     * com.zoffcc.applications.trifa W/AudioRecord: dead IAudioRecord, creating a new one from obtainBuffer()
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_policy...
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_flinger...
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_policy...
     * com.zoffcc.applications.trifa D/AudioSystem: make AudioPortCallbacksEnabled to TRUE
     * com.zoffcc.applications.trifa E/AudioRecord: AudioFlinger could not create record track, status: -22
     * com.zoffcc.applications.trifa W/AudioRecord: restoreRecord_l() failed status -22
     * com.zoffcc.applications.trifa E/AudioRecord: Error -22 obtaining an audio buffer, giving up.
     * com.zoffcc.applications.trifa D/SensorManager: Proximity, val = 8.0  [far]
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:--> speaker
     * com.zoffcc.applications.trifa I/trifa.ToxService: I:setting filteraudio_active=1
     */

    /*
     *
     Low Latency Verification
     ------------------------

   1. execute "adb shell dumpsys media.audio_flinger". Find a list of the running processes
    Name Active Client Type      Fmt Chn mask Session fCount S F SRate  L dB  R dB    Server Main buf  Aux Buf Flags UndFrmCnt
    F  2     no    704    1 00000001 00000003     562  13248 S 1 48000  -inf  -inf  000033C0 0xabab8480 0x0 0x600         0
    F  5     no    597    1 00000001 00000003     257   6000 A 2 48000    -6    -6  00073B90 0xabab8480 0x0 0x600         0
    F  1     no    597    1 00000001 00000003       9   6000 S 1 48000  -inf  -inf  00075300 0xabab8480 0x0 0x600         0
    F  6    yes   9345    3 00000001 00000001     576    128 A 1 48000     0     0  0376AA00 0xabab8480 0x0 0x400       256

   2. execute adb shell ps  | grep echo; find the sample app pid; with the pid, check with result
    on step 1.: if there is one "F" in the front of your echo pid, player is on fast audio path;
    otherwise, it is not; for fast audio capture[it is totally different story],
    if you do NOT see com.example.nativeaudio W/AudioRecord﹕ AUDIO_INPUT_FLAG_FAST denied by client
    in your logcat output when you create audio recorder, you could "assume" you are on the fast path.
     If your system image was built muted ALOGW, you will not see the above warning message;
    in which case you would pray and trust [if you created recorder with optimized frequency!].

    run:  pid=$(adb shell ps | grep -i trifa|awk '{ print $2}');adb shell dumpsys media.audio_flinger|grep "$pid"

     */
    public static void restartNativeAudioPlayEngine(int sampleRate, int channels)
    {
        try
        {
            semaphore_audioprocessing_02.acquire();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("restartNativeAudioPlayEngine:sampleRate=" + sampleRate + " channels=" + channels);

        native_audio_engine_down = true;

        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // if (isPlaying() == 1)
        //{
        NativeAudio.StopPCM16();
        //}

        // if (isRecording() == 1)
        //{
        NativeAudio.StopREC();
        //}
        NativeAudio.shutdownEngine();

        System.out.println("restartNativeAudioPlayEngine:startup ...");

        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        NativeAudio.createEngine(n_audio_in_buffer_max_count);

        reinit_audio_play_buffers(sampleRate, channels);

        NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, n_audio_in_buffer_max_count,
                                                 PREF__X_eac_delay_ms);
        NativeAudio.createAudioRecorder((int) AudioRecording.SMAPLINGRATE_TOX, n_rec_audio_in_buffer_max_count);

        NativeAudio.n_cur_buf = 0;

        for (int i = 0; i < n_audio_in_buffer_max_count; i++)
        {
            n_bytes_in_buffer[i] = 0;
        }

        native_audio_engine_down = false;

        Log.i(TAG, "audio_rec:StartREC:003:-restart-");
        Log.i(TAG, "PREF_MicGainFactor=" + PREF_mic_gain_factor);
        setMicGainFactor(PREF_mic_gain_factor);
        NativeAudio.StartREC();
        Log.i(TAG, "audio_rec:StartREC:004:-restart-");

        semaphore_audioprocessing_02.release();
        Log.i(TAG, "audio_rec:StartREC:004:-restart-:done");
    }

    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

    public static void rec_buffer_ready(int rec_buffer_num)
    {
        // Log.i(TAG, "rec_buffer_ready:num=" + rec_buffer_num);
        // TODO: workaround. sometimes mute button does not mute mic? find a real fix
        if (!microphone_muted)
        {
            // Log.i(TAG, "rec_buffer_ready:002");
            new AudioRecording.send_audio_frame_to_toxcore_from_native(rec_buffer_num).execute();
        }
    }

    /**
     * Native methods, implemented in jni folder
     */
    public static native void createEngine(int num_bufs);

    // ---------------------

    public static native float get_vu_in();

    public static native float get_vu_out();
    // ---------------------

    public static native void createBufferQueueAudioPlayer(int sampleRate, int channels, int num_bufs, int eac_delay_ms);

    public static native void set_JNI_audio_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native int PlayPCM16(int buf_num);

    public static native boolean StopPCM16();

    public static native int isPlaying();

    // public static native boolean enableReverb(boolean enabled);

    // ---------------------

    public static native void createAudioRecorder(int sampleRate, int num_bufs);

    public static native void set_JNI_audio_rec_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native void setMicGainFactor(float gain_factor);

    public static native int isRecording();

    public static native int StartREC();

    public static native boolean StopREC();

    // ---------------------

    public static native void shutdownEngine();
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

}
