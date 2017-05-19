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
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.util.Log;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_audio_buffer;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.toxav_audio_send_frame;

public class AudioRecording extends Thread
{
    static final String TAG = "trifa.AudioRecording";
    static boolean stopped = true;
    static boolean finished = true;

    // the audio recording options
    static final int RECORDING_RATE = 48000; // 16000; // 44100;
    static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final int CHANNELS_TOX = 1;
    static final long SMAPLINGRATE_TOX = 48000; // 16000;

    ByteBuffer audio_buffer = null;
    static int buffer_size = 0;
    static int audio_session_id = -1;
    AutomaticGainControl agc = null;
    AcousticEchoCanceler aec = null;

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public AudioRecording()
    {

        // AcousticEchoCanceler
        // AutomaticGainControl
        // LoudnessEnhancer

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        stopped = false;
        finished = false;

        audio_manager_s.setMicrophoneMute(false);
        audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
        audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
        start();
    }

    @Override
    public void run()
    {
        Log.i(TAG, "Running Audio Thread [OUT]");
        AudioRecord recorder = null;
        byte[] buffer = null;

        try
        {
            /*
             * Initialize buffer to hold continuously recorded audio data, start recording
             */
            buffer_size = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL, FORMAT);
            buffer = new byte[buffer_size];

            audio_buffer = ByteBuffer.allocateDirect(buffer_size);
            set_JNI_audio_buffer(audio_buffer);

            // recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, RECORDING_RATE, CHANNEL, FORMAT, buffer_size * 5);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDING_RATE, CHANNEL, FORMAT, buffer_size * 5);
            audio_session_id = recorder.getAudioSessionId();

            Log.i(TAG, "Audio Thread [OUT]:AutomaticGainControl:===============================");
            agc = null;
            try
            {
                Log.i(TAG, "Audio Thread [OUT]:AutomaticGainControl:isAvailable:" + AutomaticGainControl.isAvailable());
                agc = AutomaticGainControl.create(audio_session_id);
                int res = agc.setEnabled(true);
                Log.i(TAG, "Audio Thread [OUT]:AutomaticGainControl:setEnabled:" + res + " audio_session_id=" + audio_session_id);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Audio Thread [OUT]:EE1:" + e.getMessage());
            }
            Log.i(TAG, "Audio Thread [OUT]:AutomaticGainControl:===============================");

            Log.i(TAG, "Audio Thread [OUT]:AcousticEchoCanceler:===============================");
            aec = null;
            try
            {
                Log.i(TAG, "Audio Thread [OUT]:AcousticEchoCanceler:isAvailable:" + AcousticEchoCanceler.isAvailable());
                aec = AcousticEchoCanceler.create(audio_session_id);
                int res = aec.setEnabled(true);
                Log.i(TAG, "Audio Thread [OUT]:AcousticEchoCanceler:setEnabled:" + res + " audio_session_id=" + audio_session_id);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Audio Thread [OUT]:EE2:" + e.getMessage());
            }
            Log.i(TAG, "Audio Thread [OUT]:AcousticEchoCanceler:===============================");

            recorder.startRecording();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*
         * Loops until something outside of this thread stops it.
         * Reads the data from the recorder and writes it to the audio track for playback.
         */
        int res = 0;
        int read_bytes = 0;
        while (!stopped)
        {
            try
            {
                // only send audio frame if call has started
                // Log.i(TAG, "Callstate.tox_call_state=" + Callstate.tox_call_state);
                if (!((Callstate.tox_call_state == 0) || (Callstate.tox_call_state == 1) || (Callstate.tox_call_state == 2)))
                {
                    if (Callstate.my_audio_enabled == 1)
                    {
                        read_bytes = recorder.read(buffer, 0, buffer.length);
                        // Log.i(TAG, "audio buffer:" + "read_bytes=" + read_bytes + " buffer.length=" + buffer.length + " buffer_size=" + buffer_size);

                        audio_buffer.rewind();
                        audio_buffer.put(buffer);

                        // Log.i(TAG, "audio length=" + ((float) read_bytes * (float) 1000 / (float) SMAPLINGRATE_TOX));
                        // Log.i(TAG, "audio length=" + ((float) read_bytes / (float) SMAPLINGRATE_TOX * (float) 1000))
                        // Log.i(TAG, "audio xxxxxx=" + (((float) SMAPLINGRATE_TOX) * (float) (60) / (float) 1000));

                        res = toxav_audio_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), (long) (read_bytes / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);
                        // Log.i(TAG, "audio:res=" + res);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Audio Thread [OUT]:EE3:" + e.getMessage());
            }
        }

        try
        {
            agc.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "Audio Thread [OUT]:EE4:" + e.getMessage());
        }

        try
        {
            aec.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "Audio Thread [OUT]:EE5:" + e.getMessage());
        }

        recorder.stop();
        recorder.release();

        finished = true;
        Log.i(TAG, "Audio Thread [OUT]:finished");
    }

    public static void close()
    {
        stopped = true;
    }

}