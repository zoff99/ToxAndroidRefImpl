package com.zoffcc.applications.trifa;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_2;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_2_read_length;

public class AudioReceiver extends Thread
{
    static final String TAG = "trifa.AudioReceiver";
    static boolean stopped = true;
    static boolean finished = true;

    // the audio recording options
    static final int RECORDING_RATE = 48000; // 16000; // 44100;
    static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final int CHANNELS_TOX = 1;
    static final long SMAPLINGRATE_TOX = 48000; // 16000;

    int buffer_size = 0;
    AudioTrack track = null;


    public AudioReceiver()
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        stopped = false;
        finished = false;
        start();
    }

    public AudioTrack findAudioTrack()
    {
        buffer_size = AudioTrack.getMinBufferSize((int) SMAPLINGRATE_TOX, CHANNELS_TOX, FORMAT);
        track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) SMAPLINGRATE_TOX, CHANNELS_TOX, FORMAT, buffer_size, AudioTrack.MODE_STREAM);
        return track;
    }

    @Override
    public void run()
    {
        Log.i(TAG, "Running Audio Thread");
        track = null;

        try
        {
            track = findAudioTrack();
            track.play();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        int res = 0;
        int read_bytes = 0;
        while (!stopped)
        {
            try
            {
                Thread.sleep(20);
                if ((audio_buffer_2 != null) && (audio_buffer_2_read_length > 0))
                {
                    track.write(audio_buffer_2.array(), 0, audio_buffer_2_read_length);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "audio:EE:" + e.getMessage());
            }
        }

        track.stop();
        track.release();

        finished = true;
    }

    public static void close()
    {
        stopped = true;
    }

}