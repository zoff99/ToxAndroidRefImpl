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

import com.zoffcc.applications.trifa.AudioRecording;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.AudioRecording.microphone_muted;

public class NativeAudio
{
    private static final String TAG = "trifa.NativeAudio";

    public static final int n_audio_in_buffer_max_count = 5;
    public static ByteBuffer[] n_audio_buffer = new ByteBuffer[n_audio_in_buffer_max_count];
    public static int n_cur_buf = 0;
    public static int n_buf_size_in_bytes = 0;
    public static int[] n_bytes_in_buffer = new int[n_audio_in_buffer_max_count];
    public static int sampling_rate = 44100;
    public static int channel_count = 2;

    public static final int n_rec_audio_in_buffer_max_count = 5;
    public static ByteBuffer[] n_rec_audio_buffer = new ByteBuffer[n_rec_audio_in_buffer_max_count];
    public static int n_rec_cur_buf = 0;
    public static int n_rec_buf_size_in_bytes = 0;
    public static int[] n_rec_bytes_in_buffer = new int[n_rec_audio_in_buffer_max_count];

    public static boolean native_audio_engine_down = false;

    public static void restartNativeAudioPlayEngine(int sampleRate, int channels)
    {
        System.out.println("restartNativeAudioPlayEngine:sampleRate=" + sampleRate + " channels=" + channels);

        native_audio_engine_down = true;

        if (isPlaying() == 1)
        {
            NativeAudio.StopPCM16();
            NativeAudio.StopREC();
            NativeAudio.shutdownEngine();
        }

        NativeAudio.createEngine(n_audio_in_buffer_max_count);
        NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, n_audio_in_buffer_max_count);
        NativeAudio.createAudioRecorder((int) AudioRecording.SMAPLINGRATE_TOX, n_rec_audio_in_buffer_max_count);

        NativeAudio.n_cur_buf = 0;

        for (int i = 0; i < n_audio_in_buffer_max_count; i++)
        {
            n_bytes_in_buffer[i] = 0;
        }

        native_audio_engine_down = false;

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

    public static native void createBufferQueueAudioPlayer(int sampleRate, int channels, int num_bufs);

    public static native void set_JNI_audio_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native int PlayPCM16(int buf_num);

    public static native boolean StopPCM16();

    public static native int isPlaying();

    // public static native boolean enableReverb(boolean enabled);

    // ---------------------

    public static native void createAudioRecorder(int sampleRate, int num_bufs);

    public static native void set_JNI_audio_rec_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

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
