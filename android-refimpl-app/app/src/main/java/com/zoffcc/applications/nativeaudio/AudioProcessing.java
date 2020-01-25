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

import com.zoffcc.applications.trifa.AudioReceiver;

import java.nio.ByteBuffer;

@SuppressWarnings("JniMissingFunction")
public class AudioProcessing
{
    private static final String TAG = "trifa.AudioProc";

    static boolean native_aec_lib_loaded = false;
    static ByteBuffer audio_buffer;
    static ByteBuffer audio_rec_buffer;

    public static native void set_JNI_audio_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native void set_JNI_audio_rec_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native void init(int channels, int samplingfreq, int channels_rec, int samplingfreq_rec);

    public static native void destroy();

    static
    {
        try
        {
            System.loadLibrary("aec");
            native_aec_lib_loaded = true;
            Log.i(TAG, "successfully loaded aec library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_aec_lib_loaded = false;
            Log.i(TAG, "loadLibrary aec failed!");
            e.printStackTrace();
        }
    }

    public static void init_buffers(int channels, int samplingfreq, int channels_rec, int samplingfreq_rec)
    {
        // frame size must always be 10ms !!
        int buffer_size = (samplingfreq / 100) * channels * 2;

        audio_buffer = ByteBuffer.allocateDirect(buffer_size);
        set_JNI_audio_buffer(audio_buffer, buffer_size, 0);

        // frame size must always be 10ms !!
        int buffer_rec_size = (samplingfreq_rec / 100) * channels_rec * 2;

        audio_rec_buffer = ByteBuffer.allocateDirect(buffer_rec_size);
        set_JNI_audio_rec_buffer(audio_buffer, buffer_rec_size, 0);

        init(channels, samplingfreq, channels_rec, samplingfreq_rec);
    }

    public static void destroy_buffers()
    {
        audio_buffer = null;
        audio_rec_buffer = null;

        destroy();
    }
}
