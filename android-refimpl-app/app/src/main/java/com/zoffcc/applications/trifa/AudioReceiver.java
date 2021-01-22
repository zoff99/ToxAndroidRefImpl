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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.PREF__X_eac_delay_ms;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_audio_play_buffer_custom;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_native_audio_play;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf01;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf02;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf03;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf04;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf05;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf06;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf_count_max;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_factor;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_iter;

public class AudioReceiver extends Thread
{
    static final String TAG = "trifa.AudioReceiver";
    static boolean stopped = true;
    static boolean finished = true;

    static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final long SMAPLINGRATE_TOX = 48000; // 16000;
    static long sampling_rate_ = SMAPLINGRATE_TOX;
    static int channels_ = 1;

    static boolean native_audio_engine_running = false;

    static int sleep_millis = 50; // TODO: hardcoded is bad!!!!
    final static int buffer_multiplier = 4;
    static int buffer_size = 48000 * 2 * 2 * buffer_multiplier; // TODO: hardcoded is bad!!!!
    AudioTrack track = null;

    public AudioReceiver()
    {
        stopped = false;
        finished = false;
        start();
    }

    @Override
    public void run()
    {
        Log.i(TAG, "Running Audio Thread [IN]");
        track = null;

        try
        {
            this.setName("AudioReceiver");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (PREF__use_native_audio_play)
            {
                // NativeAudio.sampling_rate = 48000;
                // NativeAudio.channel_count = 2;

                NativeAudio.createEngine(NativeAudio.n_audio_in_buffer_max_count);

                native_audio_engine_running = true;

                int sampleRate = NativeAudio.sampling_rate;
                int channels = NativeAudio.channel_count;

                System.out.println("NativeAudio:[2]sampleRate=" + sampleRate + " channels=" + channels);

                reinit_audio_play_buffers(sampleRate, channels);

                debug__audio_play_buf_count_max = NativeAudio.n_audio_in_buffer_max_count;

                NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, NativeAudio.n_audio_in_buffer_max_count,
                                                         PREF__X_eac_delay_ms);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (PREF__use_native_audio_play)
        {
            while (!stopped)
            {
                try
                {
                    Thread.sleep(300);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "audio_play:recthr:EE2:" + e.getMessage());
                }
            }
        }

        finished = true;
        Log.i(TAG, "Audio Thread [IN]:finished");
    }

    public static void close()
    {
        stopped = true;

        native_audio_engine_running = false;
        NativeAudio.StopPCM16();
        NativeAudio.StopREC();
        NativeAudio.shutdownEngine();

        System.out.println("NativeAudio:shutdown");
    }

    public static void reinit_audio_play_buffers(int sampleRate, int channels)
    {
        int channel_config = AudioFormat.CHANNEL_OUT_MONO;
        if (channels == 2)
        {
            channel_config = AudioFormat.CHANNEL_OUT_STEREO;

        }
        int buffer_size22 = AudioTrack.getMinBufferSize(sampleRate, channel_config, FORMAT);
        Log.i(TAG, "audio_play:read:init min buffer size(x)=" + buffer_size);
        Log.i(TAG, "audio_play:read:init min buffer size(2)=" + buffer_size22);
        int buffer_size33 = -1;

        debug__audio_play_buf01 = buffer_size;
        debug__audio_play_buf02 = buffer_size22;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            String sampleRateStr = null;

            try
            {
                sampleRateStr = audio_manager_s.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE); // in decimal Hz
                int sampleRate__ = Integer.parseInt(sampleRateStr);
                Log.i(TAG, "audio_play:PROPERTY_OUTPUT_SAMPLE_RATE=" + sampleRate__);

                String framesPerBuffer = audio_manager_s.getProperty(
                        AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER); // in decimal PCM frames
                int framesPerBufferInt = Integer.parseInt(framesPerBuffer);
                Log.i(TAG, "audio_play:PROPERTY_OUTPUT_FRAMES_PER_BUFFER=" + framesPerBufferInt);

                int buffer_size33_b = (channels * 2) * framesPerBufferInt;
                if ((buffer_size33_b > 20) && (buffer_size33_b < 10000))
                {
                    buffer_size33 = buffer_size33_b;
                    debug__audio_play_buf03 = buffer_size33;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // use best buffer size for low latency audio play
        if (buffer_size33 > 0)
        {
            int factor = 10;

            float got_ms_iteration =
                    1000.0f / ((float) sampleRate / (((float) (buffer_size33 * 10) / 2.0f) / (float) channels));

            if (got_ms_iteration > 60)
            {
                factor = 5;
            }
            else if (got_ms_iteration < 20)
            {
                factor = 20;
            }

            debug__audio_play_factor = factor;

            NativeAudio.n_buf_size_in_bytes = buffer_size33 * factor;
            Log.i(TAG, "audio_play:read:init min buffer size(4)=" + NativeAudio.n_buf_size_in_bytes);

            debug__audio_play_buf04 = NativeAudio.n_buf_size_in_bytes;
        }
        else
        {
            // NativeAudio.n_buf_size_in_bytes = buffer_size22;
            NativeAudio.n_buf_size_in_bytes = (sampleRate * channels * 2) / 10; // = 100ms // (48000*1*2) = 96000;
            Log.i(TAG, "audio_play:read:init min buffer size(5)=" + NativeAudio.n_buf_size_in_bytes);

            debug__audio_play_buf05 = NativeAudio.n_buf_size_in_bytes;
        }

        if (PREF__X_audio_play_buffer_custom > 0)
        {
            if (PREF__X_audio_play_buffer_custom < 500000)
            {
                NativeAudio.n_buf_size_in_bytes = PREF__X_audio_play_buffer_custom;
            }
        }

        float interate_ms =
                1000.0f / ((float) sampleRate / (((float) NativeAudio.n_buf_size_in_bytes / 2.0f) / (float) channels));
        NativeAudio.n_buf_iterate_ms = (int) interate_ms;
        Log.i(TAG, "audio_play:read:init:interate_ms=" + interate_ms);

        debug__audio_play_iter = NativeAudio.n_buf_iterate_ms;
        debug__audio_play_buf06 = NativeAudio.n_buf_size_in_bytes;

        for (int i = 0; i < NativeAudio.n_audio_in_buffer_max_count; i++)
        {
            NativeAudio.n_audio_buffer[i] = ByteBuffer.allocateDirect(NativeAudio.n_buf_size_in_bytes);
            NativeAudio.n_bytes_in_buffer[i] = 0;
            NativeAudio.set_JNI_audio_buffer(NativeAudio.n_audio_buffer[i], NativeAudio.n_buf_size_in_bytes, i);
        }

        NativeAudio.n_cur_buf = 0;
        for (int i = 0; i < NativeAudio.n_audio_in_buffer_max_count; i++)
        {
            NativeAudio.n_bytes_in_buffer[i] = 0;
        }

    }

}