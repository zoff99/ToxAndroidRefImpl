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

import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.util.Log;

import com.zoffcc.applications.nativeaudio.AudioProcessing;
import com.zoffcc.applications.nativeaudio.NativeAudio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.ByteBuffer;

import static com.zoffcc.applications.nativeaudio.AudioProcessing.native_aec_lib_ready;
import static com.zoffcc.applications.nativeaudio.NativeAudio.n_rec_audio_in_buffer_max_count;
import static com.zoffcc.applications.nativeaudio.NativeAudio.native_audio_engine_down;
import static com.zoffcc.applications.nativeaudio.NativeAudio.semaphore_audioprocessing_02;
import static com.zoffcc.applications.nativeaudio.NativeAudio.setMicGainFactor;
import static com.zoffcc.applications.trifa.AudioReceiver.native_audio_engine_running;
import static com.zoffcc.applications.trifa.CallingActivity.calling_activity_start_ms;
import static com.zoffcc.applications.trifa.CallingActivity.trifa_is_MicrophoneMute;
import static com.zoffcc.applications.trifa.ConferenceAudioActivity.push_to_talk_active;
import static com.zoffcc.applications.trifa.ConferenceAudioActivity.update_group_audio_send_icon;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.AEC_DEBUG_DUMP;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_audio_recording_frame_size;
import static com.zoffcc.applications.trifa.MainActivity.PREF__min_audio_samplingrate_out;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_native_audio_play;
import static com.zoffcc.applications.trifa.MainActivity.PREF_mic_gain_factor;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_audio_buffer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_audio_send_frame;
import static com.zoffcc.applications.trifa.MainActivity.toxav_group_send_audio;

public class AudioRecording extends Thread
{
    static final String TAG = "trifa.AudioRecording";
    static boolean stopped = true;
    static boolean finished = true;

    // the audio recording options
    static int RECORDING_RATE = 48000; // 16000; // 44100;
    static final int CHANNELS_TOX = 1;
    public static long SMAPLINGRATE_TOX = 48000; // 16000;
    final static int milliseconds_audio_samples_max = 120;
    public static boolean microphone_muted = false;
    static final boolean DEBUG_MIC_DATA_LOGGING = false;
    static boolean audio_engine_starting = false;
    static int global_audio_group_send_res = -9999;

    // -----------------------
    static ByteBuffer _recBuffer = null;
    // -----------------------

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public AudioRecording()
    {
        stopped = false;
        finished = false;

        audio_manager_s.setMicrophoneMute(false);
        audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
        // audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
        start();
    }

    @Override
    public void run()
    {
        try
        {
            // android.os.Process.setThreadPriority(Thread.MAX_PRIORITY);
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            this.setName("AudioRecording");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "Audio Thread [OUT]:EETPr:" + e.getMessage());
        }

        if (PREF__use_native_audio_play)
        {

            try
            {
                semaphore_audioprocessing_02.acquire();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        audio_engine_starting = true;

        Log.i(TAG, "Running Audio Thread [OUT]");
        AudioRecord recorder = null;

        try
        {

            if (PREF__use_native_audio_play)
            {
                SMAPLINGRATE_TOX = PREF__min_audio_samplingrate_out;
                RECORDING_RATE = PREF__min_audio_samplingrate_out;

                int sampling_rate = PREF__min_audio_samplingrate_out;
                int channel_count = 1;


                while (native_audio_engine_running == false)
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (Exception e)
                    {
                    }
                }

                NativeAudio.createAudioRecorder((int) SMAPLINGRATE_TOX, n_rec_audio_in_buffer_max_count);

                int want_buf_size_in_bytes = (int) ((48000.0f / 1000.0f) * milliseconds_audio_samples_max * 2 * 2);
                Log.i(TAG, "want_buf_size_in_bytes(1)=" + want_buf_size_in_bytes);

                _recBuffer = ByteBuffer.allocateDirect(
                    (int) ((48000.0f / 1000.0f) * milliseconds_audio_samples_max * 2 * 2)); // Max 120 ms @ 48 kHz
                set_JNI_audio_buffer(_recBuffer);

                // for 60 ms --> 5760 bytes
                NativeAudio.n_rec_buf_size_in_bytes =
                    ((sampling_rate * channel_count * 2) / 1000) * PREF__X_audio_recording_frame_size;

                Log.i(TAG, "NativeAudio.n_rec_buf_size_in_bytes=" + NativeAudio.n_rec_buf_size_in_bytes +
                           " PREF__X_audio_recording_frame_size=" + PREF__X_audio_recording_frame_size +
                           " sampling_rate=" + sampling_rate + " channel_count=" + channel_count);

                for (int i = 0; i < n_rec_audio_in_buffer_max_count; i++)
                {
                    NativeAudio.n_rec_audio_buffer[i] = ByteBuffer.allocateDirect(NativeAudio.n_rec_buf_size_in_bytes);
                    NativeAudio.n_rec_bytes_in_buffer[i] = 0;
                    NativeAudio.set_JNI_audio_rec_buffer(NativeAudio.n_rec_audio_buffer[i],
                                                         NativeAudio.n_rec_buf_size_in_bytes, i);
                }

                NativeAudio.n_rec_cur_buf = 0;
                for (int i = 0; i < n_rec_audio_in_buffer_max_count; i++)
                {
                    NativeAudio.n_rec_bytes_in_buffer[i] = 0;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "Audio Thread [OUT]:EE:" + e.getMessage());
        }

        /*
         * Loops until something outside of this thread stops it.
         * Reads the data from the recorder and writes it to the audio track for playback.
         */

        int readBytes = 0;
        int audio_send_res2 = 0;

        int want_to_read_bytes = (int) (((float) SMAPLINGRATE_TOX / 1000.0f) *
                                        (float) PREF__X_audio_recording_frame_size * (float) CHANNELS_TOX * 2.0f);
        // try to read "PREF__X_audio_recording_frame_size" of audio data

        microphone_muted = false;

        try
        {
            // if (audio_manager_s.isMicrophoneMute())
            if (trifa_is_MicrophoneMute)
            {
                microphone_muted = true;
            }
        }
        catch (Exception e)
        {
        }


        if (PREF__use_native_audio_play)
        {
            Log.i(TAG, "audio_rec:StartREC:001");
            Log.i(TAG, "PREF_MicGainFactor=" + PREF_mic_gain_factor);
            setMicGainFactor(PREF_mic_gain_factor);
            NativeAudio.StartREC();
            Log.i(TAG, "audio_rec:StartREC:002");

            audio_engine_starting = false;

            semaphore_audioprocessing_02.release();


            while (!stopped)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "audio_rec:EE2:" + e.getMessage());
                }

                try
                {
                    // if (audio_manager_s.isMicrophoneMute())
                    microphone_muted = trifa_is_MicrophoneMute;
                }
                catch (Exception e)
                {
                    microphone_muted = false;
                }
            }
        }

        finished = true;

        Log.i(TAG, "Audio Thread [OUT]:finished");
    }

    public static void close()
    {
        stopped = true;
    }

    public static class send_audio_frame_to_toxcore_from_native extends AsyncTask<Void, Void, String>
    {
        int audio_send_res = 0;
        int bufnum_ = 0;

        public send_audio_frame_to_toxcore_from_native(int bufnum)
        {
            bufnum_ = bufnum;
            // Log.i(TAG, "send_audio_frame_to_toxcore_from_native:bufnum=" + bufnum);
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            try
            {
                Thread.currentThread().setName("t_s_outN:" + bufnum_);
            }
            catch (Exception e)
            {
            }

            try
            {
                if (native_audio_engine_down == false)
                {

                    // -------------- apply AudioProcessing: AEC -----------------------
                    if (native_aec_lib_ready)
                    {
                        AudioProcessing.audio_rec_buffer.position(0);
                        NativeAudio.n_rec_audio_buffer[bufnum_].position(0);
                        NativeAudio.n_rec_audio_buffer[bufnum_].rewind();

                        //Log.i(TAG, "audio_rec:AEC:s:" +
                        //           Arrays.toString(NativeAudio.n_rec_audio_buffer[bufnum_].array()));

                        long save_timestamp = System.currentTimeMillis();

                        if (AEC_DEBUG_DUMP)
                        {

                            BufferedWriter debug_audio_writer_rec_s = null;
                            try
                            {
                                debug_audio_writer_rec_s = new BufferedWriter(
                                    new FileWriter("/sdcard/audio_rec_s.txt", true));
                                long ts = (save_timestamp - calling_activity_start_ms) * 1000;
                                int value = 0;
                                for (int xx = 0; xx < 160; xx++)
                                {
                                    value = NativeAudio.n_rec_audio_buffer[bufnum_].getShort(xx * 2);
                                    debug_audio_writer_rec_s.write(("" + ts) + "," + value + "\n");
                                    ts = ts + 16;
                                }

                                if (debug_audio_writer_rec_s != null)
                                {
                                    debug_audio_writer_rec_s.close();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            debug_audio_writer_rec_s = new BufferedWriter(
                                new FileWriter("/sdcard/audio_rec_s_ts.txt", true));
                            debug_audio_writer_rec_s.write("" + save_timestamp + "\n");

                            if (debug_audio_writer_rec_s != null)
                            {
                                debug_audio_writer_rec_s.close();
                            }
                        }

                        // Log.i(TAG, "audio_rec:buf_len1=" + NativeAudio.n_rec_audio_buffer[bufnum_].remaining());
                        // Log.i(TAG, "audio_rec:buf_len2=" + AudioProcessing.audio_rec_buffer.remaining());
                        AudioProcessing.audio_rec_buffer.put(NativeAudio.n_rec_audio_buffer[bufnum_]);
                        AudioProcessing.record_buffer();
                        AudioProcessing.audio_rec_buffer.position(0);
                        NativeAudio.n_rec_audio_buffer[bufnum_].position(0);
                        NativeAudio.n_rec_audio_buffer[bufnum_].rewind();

                        // Log.i(TAG, "audio_rec:AEC:d:" + Arrays.toString(AudioProcessing.audio_rec_buffer.array()));

                        NativeAudio.n_rec_audio_buffer[bufnum_].put(AudioProcessing.audio_rec_buffer);
                        NativeAudio.n_rec_audio_buffer[bufnum_].position(0);
                        NativeAudio.n_rec_audio_buffer[bufnum_].rewind();

                        if (AEC_DEBUG_DUMP)
                        {
                            BufferedWriter debug_audio_writer_rec_d = null;
                            try
                            {
                                debug_audio_writer_rec_d = new BufferedWriter(
                                    new FileWriter("/sdcard/audio_rec_d.txt", true));
                                long ts = (save_timestamp - calling_activity_start_ms) * 1000;
                                int value = 0;
                                for (int xx = 0; xx < 160; xx++)
                                {
                                    value = NativeAudio.n_rec_audio_buffer[bufnum_].getShort(xx * 2);
                                    debug_audio_writer_rec_d.write(("" + ts) + "," + value + "\n");
                                    ts = ts + 16;
                                }

                                if (debug_audio_writer_rec_d != null)
                                {
                                    debug_audio_writer_rec_d.close();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }
                    // -------------- apply AudioProcessing: AEC -----------------------

                    _recBuffer.rewind();
                    NativeAudio.n_rec_audio_buffer[bufnum_].rewind();
                    _recBuffer.put(NativeAudio.n_rec_audio_buffer[bufnum_]);

                    if (DEBUG_MIC_DATA_LOGGING)
                    {
                        Log.i(TAG, "send_audio_frame_to_toxcore_from_native:1:" +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(0) + " " +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(1) + " " +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(2) + " " +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(3) + " " +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(4) + " " +
                                   NativeAudio.n_rec_audio_buffer[bufnum_].get(5) + " ");


                        Log.i(TAG, "send_audio_frame_to_toxcore_from_native:2:" + _recBuffer.get(0) + " " +
                                   _recBuffer.get(1) + " " + _recBuffer.get(2) + " " + _recBuffer.get(3) + " " +
                                   _recBuffer.get(4) + " " + _recBuffer.get(5) + " ");
                    }

                    //Log.i(TAG,
                    //      "send_audio_frame_to_toxcore_from_native:CHANNELS_TOX=" + CHANNELS_TOX + " SMAPLINGRATE_TOX=" +
                    //      SMAPLINGRATE_TOX);

                    if (Callstate.audio_group_active)
                    {
                        if (push_to_talk_active)
                        {
                            int audio_group_send_res = toxav_group_send_audio(
                                tox_conference_by_confid__wrapper(ConferenceAudioActivity.conf_id),
                                (long) ((NativeAudio.n_rec_buf_size_in_bytes) / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);

                            //Log.i(TAG,
                            //      "audio_group_send_res__global_audio_group_send_res=" + audio_group_send_res + " " +
                            //      global_audio_group_send_res);

                            if (audio_group_send_res != global_audio_group_send_res)
                            {
                                if (push_to_talk_active)
                                {
                                    if (audio_group_send_res == 0)
                                    {
                                        try
                                        {
                                            update_group_audio_send_icon(1);
                                        }
                                        catch (Exception e2)
                                        {
                                        }
                                        global_audio_group_send_res = audio_group_send_res;
                                    }
                                    else
                                    {
                                        try
                                        {
                                            update_group_audio_send_icon(2);
                                        }
                                        catch (Exception e2)
                                        {
                                        }
                                        global_audio_group_send_res = audio_group_send_res;
                                    }
                                }
                                else
                                {
                                    try
                                    {
                                        update_group_audio_send_icon(0);
                                    }
                                    catch (Exception e2)
                                    {
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        // Log.i(TAG,
                        //       "toxav_audio_send_frame:002:native:Callstate.friend_pubkey=" + Callstate.friend_pubkey +
                        //       " fnum=" + tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));
                        audio_send_res = toxav_audio_send_frame(
                            tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                            ((NativeAudio.n_rec_buf_size_in_bytes) / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);

                        if (audio_send_res == ToxVars.TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_SYNC.value)
                        {
                            audio_send_res = toxav_audio_send_frame(
                                tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                ((NativeAudio.n_rec_buf_size_in_bytes) / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);

                            if (audio_send_res == ToxVars.TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_SYNC.value)
                            {
                                audio_send_res = toxav_audio_send_frame(
                                    tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                    ((NativeAudio.n_rec_buf_size_in_bytes) / 2), CHANNELS_TOX, SMAPLINGRATE_TOX);
                            }
                        }

                        // if (audio_send_res != 0)
                        // {
                        //   Log.i(TAG, "send_audio_frame_to_toxcore_from_native:res=" + audio_send_res + ":" +
                        //               ToxVars.TOXAV_ERR_SEND_FRAME.value_str(audio_send_res));
                        // }

                    }

                    if (DEBUG_MIC_DATA_LOGGING)
                    {
                        if (audio_send_res != 0)
                        {
                            Log.i(TAG, "send_audio_frame_to_toxcore_from_native:res=" + audio_send_res + ":" +
                                       ToxVars.TOXAV_ERR_SEND_FRAME.value_str(audio_send_res));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "send_audio_frame_to_toxcore_from_native:EE6:" + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
        }

        @Override
        protected void onPreExecute()
        {
        }
    }
}