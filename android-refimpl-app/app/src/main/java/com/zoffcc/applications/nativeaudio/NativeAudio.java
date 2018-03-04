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

import android.content.Context;

import java.nio.ByteBuffer;

public class NativeAudio
{
    public static final int n_audio_in_buffer_max_count = 5;
    public static ByteBuffer[] n_audio_buffer = new ByteBuffer[n_audio_in_buffer_max_count];
    public static int n_cur_buf = 1;
    public static int n_buf_size_in_bytes = 0;
    public static int[] n_bytes_in_buffer = new int[n_audio_in_buffer_max_count];
    public static int sampling_rate = 44100;
    public static int channel_count = 2;

    public static void demo(Context c)
    {
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------

        // initialize native audio system
        createEngine(n_audio_in_buffer_max_count);
        int sampleRate = 48000;
        int channels = 1;
        System.out.println("NativeAudio:sampleRate=" + sampleRate);

        createBufferQueueAudioPlayer(sampleRate, channels, n_audio_in_buffer_max_count);

        n_buf_size_in_bytes = (sampleRate * channels * 2) / 10; // = 100ms // (48000*1*2) = 96000;

        for (int i = 0; i < n_audio_in_buffer_max_count; i++)
        {
            n_audio_buffer[i] = ByteBuffer.allocateDirect(n_buf_size_in_bytes);
            n_bytes_in_buffer[i] = 0;
            set_JNI_audio_buffer(n_audio_buffer[i], n_buf_size_in_bytes, i);
        }
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------

    }


    public static void restartNativeAudioPlayEngine(int sampleRate, int channels)
    {
        System.out.println("restartNativeAudioPlayEngine:sampleRate=" + sampleRate + " channels=" + channels);

        if (isPlaying() == 1)
        {
            NativeAudio.StopPCM16();
            NativeAudio.shutdownEngine();
        }

        NativeAudio.createEngine(n_audio_in_buffer_max_count);
        NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, n_audio_in_buffer_max_count);
        NativeAudio.n_cur_buf = 1;

        for (int i = 0; i < n_audio_in_buffer_max_count; i++)
        {
            n_bytes_in_buffer[i] = 0;
        }
    }

    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

    public static void rec_buffer_ready(int rec_buffer_num)
    {

    }

    /**
     * Native methods, implemented in jni folder
     */
    public static native void createEngine(int num_bufs);

    public static native void createBufferQueueAudioPlayer(int sampleRate, int channels, int num_bufs);

    public static native void set_JNI_audio_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native int PlayPCM16(int buf_num);

    public static native boolean StopPCM16();

    public static native int isPlaying();

    public static native boolean enableReverb(boolean enabled);

    public static native void shutdownEngine();
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

}
