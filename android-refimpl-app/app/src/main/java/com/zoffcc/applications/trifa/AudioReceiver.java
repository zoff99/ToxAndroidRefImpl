package com.zoffcc.applications.trifa;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;

import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_play;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_play_length;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_read_write;

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

    static int buffer_size = 1920 * 5; // TODO: hardcoded is bad!!!!
    AudioTrack track = null;


    public AudioReceiver()
    {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        stopped = false;
        finished = false;
        start();
    }

    public AudioTrack findAudioTrack()
    {
        int buffer_size22 = AudioTrack.getMinBufferSize((int) SMAPLINGRATE_TOX, CHANNELS_TOX, FORMAT);
        Log.i(TAG, "audio_play:read:init min buffer size(x)=" + buffer_size);
        Log.i(TAG, "audio_play:read:init min buffer size(2)=" + buffer_size22);

        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, (int) SMAPLINGRATE_TOX, CHANNELS_TOX, FORMAT, buffer_size, AudioTrack.MODE_STREAM);
        // track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) SMAPLINGRATE_TOX, CHANNELS_TOX, FORMAT, buffer_size, AudioTrack.MODE_STATIC);
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
                // puts data into "audio_buffer_play"
                // if ((audio_buffer_read_write(0, 0, 0, false)) && (audio_buffer_play_length > 0))
                if (audio_buffer_read_write(0, 0, 0, false))
                {
                    // Log.i(TAG, "audio_play:recthr:play");
                    // Log.i(TAG, "audio_play:RecThread:1:len=" + audio_buffer_play_length);
                    track.write(audio_buffer_play.array(), 0, audio_buffer_play_length);
                    // Log.i(TAG, "audio_play:RecThread:2:len=" + audio_buffer_play_length);
                }
                else
                {
                    try
                    {
                        // Log.i(TAG, "audio_play:recthr:no data");
                        Thread.sleep(30);
                    }
                    catch (InterruptedException esleep)
                    {
                        // Log.i(TAG, "audio_play:recthr:wake up:" + esleep.getMessage());
                    }
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "audio_play:recthr:EE:" + e.getMessage());
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