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
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.LoudnessEnhancer;
import android.util.Log;

import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.PREF__use_native_audio_play;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;

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
    static int buffer_size = 1920 * buffer_multiplier; // TODO: hardcoded is bad!!!!
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
            // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
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
                NativeAudio.sampling_rate = 48000;
                NativeAudio.channel_count = 2;

                NativeAudio.createEngine(NativeAudio.n_audio_in_buffer_max_count);

                native_audio_engine_running = true;

                int sampleRate = NativeAudio.sampling_rate;
                int channels = NativeAudio.channel_count;

                System.out.println("NativeAudio:[2]sampleRate=" + sampleRate + " channels=" + channels);

                if (1 == 1)
                {

                    int buffer_size22 = AudioTrack.getMinBufferSize(sampleRate, channels, FORMAT);
                    Log.i(TAG, "audio_play:read:init min buffer size(x)=" + buffer_size);
                    Log.i(TAG, "audio_play:read:init min buffer size(2)=" + buffer_size22);

                    String sampleRateStr = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        sampleRateStr = audio_manager_s.getProperty(
                            AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE); // in decimal Hz
                        int sampleRate__ = Integer.parseInt(sampleRateStr);
                        Log.i(TAG, "audio_play:PROPERTY_OUTPUT_SAMPLE_RATE=" + sampleRate__);

                        String framesPerBuffer = audio_manager_s.getProperty(
                            AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER); // in decimal PCM frames
                        int framesPerBufferInt = Integer.parseInt(framesPerBuffer);
                        Log.i(TAG, "audio_play:PROPERTY_OUTPUT_FRAMES_PER_BUFFER=" + framesPerBufferInt);
                    }
                }

                NativeAudio.n_buf_size_in_bytes = (sampleRate * channels * 2) / 10; // = 100ms // (48000*1*2) = 96000;

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

                NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, NativeAudio.n_audio_in_buffer_max_count);
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

}