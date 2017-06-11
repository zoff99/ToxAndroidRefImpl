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
import java.nio.ByteOrder;

import static com.zoffcc.applications.trifa.MainActivity.PREF__min_audio_samplingrate_out;
import static com.zoffcc.applications.trifa.MainActivity.PREF__software_echo_cancel;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_audio_buffer;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.toxav_audio_send_frame;
import static com.zoffcc.applications.trifa.TrifaToxService.canceller;

public class AudioRecording extends Thread
{
    static final String TAG = "trifa.AudioRecording";
    static boolean stopped = true;
    static boolean finished = true;

    // the audio recording options
    static int RECORDING_RATE = 48000; // 16000; // 44100;
    static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final int CHANNELS_TOX = 1;
    static long SMAPLINGRATE_TOX = 48000; // 16000;
    static boolean soft_echo_canceller_ready = false;

    ByteBuffer audio_buffer = null;
    static int buffer_size = 0;
    static int audio_session_id = -1;
    AutomaticGainControl agc = null;
    AcousticEchoCanceler aec = null;

    static short[] buffer_short = null;


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
        // byte[] buffer = null;

        try
        {
            int min_sampling_rate = -1;
            // try user set min freq first
            min_sampling_rate = getMinSupportedSampleRate(PREF__min_audio_samplingrate_out);
            Log.i(TAG, "Running Audio Thread [OUT]:try sampling rate:1:" + min_sampling_rate);
            if (min_sampling_rate == -1)
            {
                // ok, now try also with 8kHz
                min_sampling_rate = getMinSupportedSampleRate(8000);
                Log.i(TAG, "Running Audio Thread [OUT]:try sampling rate:2:" + min_sampling_rate);
            }

            if (min_sampling_rate != -1)
            {
                RECORDING_RATE = min_sampling_rate;
            }
            SMAPLINGRATE_TOX = RECORDING_RATE;
            Log.i(TAG, "Running Audio Thread [OUT]:using sampling rate:" + RECORDING_RATE + " kHz (min=" + min_sampling_rate + ")");

            /*
             * Initialize buffer to hold continuously recorded audio data, start recording
             */
            buffer_size = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL, FORMAT);
            // buffer = new byte[buffer_size];
            buffer_short = new short[buffer_size / 2];

            // init echo canceller -----------
            if (PREF__software_echo_cancel)
            {
                canceller.open(RECORDING_RATE, buffer_size * 4, (int) ((float) SMAPLINGRATE_TOX / 10f));
                soft_echo_canceller_ready = true;
            }
            // init echo canceller -----------

            audio_buffer = ByteBuffer.allocateDirect(buffer_size);
            set_JNI_audio_buffer(audio_buffer);

            Log.i(TAG, "buffer_sizes:buffer_size=" + buffer_size + " buffer_short.length=" + buffer_short.length + " audio_buffer.limit=" + audio_buffer.limit());

            //**// recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, RECORDING_RATE, CHANNEL, FORMAT, buffer_size * 5);
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
        // int read_bytes = 0;
        int read_shorts = 0;
        short[] buffer_short_with_soft_ec = null;

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
                        read_shorts = recorder.read(buffer_short, 0, buffer_short.length);
                        // read_bytes = recorder.read(buffer, 0, buffer.length);
                        Log.i(TAG, "audio buffer:" + "read_shorts=" + read_shorts + " buffer_short.length=" + buffer_short.length + " buffer_size=" + buffer_size);
                        // Log.i(TAG, "audio buffer:" + "read_bytes=" + read_bytes + " buffer.length=" + buffer.length + " buffer_size=" + buffer_size);

                        if (PREF__software_echo_cancel)
                        {
                            if (read_shorts != buffer_short.length)
                            {
                                short[] buffer_short_copy = java.util.Arrays.copyOf(buffer_short, read_shorts);
                                try
                                {
                                    buffer_short_with_soft_ec = canceller.capture(buffer_short_copy);
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                try
                                {
                                    buffer_short_with_soft_ec = canceller.capture(buffer_short);
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            audio_buffer.rewind();
                            // audio_buffer.put(buffer);
                            audio_buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer_short_with_soft_ec);
                        }
                        else
                        {
                            audio_buffer.rewind();
                            audio_buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer_short);
                        }

                        // Log.i(TAG, "audio length=" + ((float) read_bytes * (float) 1000 / (float) SMAPLINGRATE_TOX));
                        // Log.i(TAG, "audio length=" + ((float) read_bytes / (float) SMAPLINGRATE_TOX * (float) 1000))
                        // Log.i(TAG, "audio xxxxxx=" + (((float) SMAPLINGRATE_TOX) * (float) (60) / (float) 1000));

                        res = toxav_audio_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), (long) (read_shorts), CHANNELS_TOX, SMAPLINGRATE_TOX);
                        // res = toxav_audio_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), (long) (read_bytes / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);
                        Log.i(TAG, "audio:res=" + res);
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

        if (PREF__software_echo_cancel)
        {
            soft_echo_canceller_ready = false;
            canceller.close();
        }

        finished = true;

        Log.i(TAG, "Audio Thread [OUT]:finished");
    }

    public static void close()
    {
        stopped = true;
    }


    /*
     * thanks to: http://stackoverflow.com/questions/8043387/android-audiorecord-supported-sampling-rates
     */
    int getMinSupportedSampleRate(int min_rate)
    {
    /*
     * Valid Audio Sample rates
     *
     * @see <a
     * href="http://en.wikipedia.org/wiki/Sampling_%28signal_processing%29"
     * >Wikipedia</a>
     */
        int validSampleRates[];
        validSampleRates = new int[]{8000, 16000, 22050,
                //
                32000, 37800, 44056, 44100, 47250, 48000, 50000, 50400,
                //
                88200, 96000, 176400, 192000, 352800, 2822400, 5644800};
    /*
     * Selecting default audio input source for recording since
     * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
     * default encoding format.
     */
        for (int i = 0; i < validSampleRates.length; i++)
        {
            if (validSampleRates[i] >= min_rate)
            {
                int result = AudioRecord.getMinBufferSize(validSampleRates[i], CHANNEL, FORMAT);
                if (result != AudioRecord.ERROR && result != AudioRecord.ERROR_BAD_VALUE && result > 0)
                {
                    // return the mininum supported audio sample rate
                    return validSampleRates[i];
                }
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return -1;
    }
}