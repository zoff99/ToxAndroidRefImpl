package com.zoffcc.applications.nativeaudio;

import android.content.Context;

import java.nio.ByteBuffer;

public class NativeAudio
{
    public static ByteBuffer n_audio_buffer_1 = null;
    public static ByteBuffer n_audio_buffer_2 = null;
    public static int n_cur_buf = 1;
    public static int n_buf_size_in_bytes = 0;
    public static int n_bytes_in_buffer_1 = 0;
    public static int n_bytes_in_buffer_2 = 0;
    public static int sampling_rate = 44100;
    public static int channel_count = 2;

    public static void demo(Context c)
    {
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------
        // ------- DEBUG -------

        // initialize native audio system
        createEngine();
        int sampleRate = 0;
        sampleRate = 48000;
        System.out.println("NativeAudio:sampleRate=" + sampleRate );

        createBufferQueueAudioPlayer(sampleRate, 1);

        n_buf_size_in_bytes = 9600 * 10; // (48000*1*2) = 96000;
        n_audio_buffer_1 = ByteBuffer.allocateDirect(n_buf_size_in_bytes);
        set_JNI_audio_buffer(n_audio_buffer_1, n_buf_size_in_bytes, 1);

        n_audio_buffer_2 = ByteBuffer.allocateDirect(n_buf_size_in_bytes);
        set_JNI_audio_buffer(n_audio_buffer_2, n_buf_size_in_bytes, 2);

        for (int j = 0; j < n_buf_size_in_bytes - 1; j++)
        {
            n_audio_buffer_2.position(j);
            n_audio_buffer_2.put((byte) (j % 200));
        }

        n_bytes_in_buffer_1 = 0;
        n_bytes_in_buffer_2 = 0;

        // PlayPCM16(1);
        // enableReverb(false);
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
            System.out.println("restartNativeAudioPlayEngine:001");

            NativeAudio.StopPCM16();
            System.out.println("restartNativeAudioPlayEngine:002");
            System.out.println("restartNativeAudioPlayEngine:003");
            NativeAudio.shutdownEngine();
            System.out.println("restartNativeAudioPlayEngine:004");
        }

        System.out.println("restartNativeAudioPlayEngine:005");
        NativeAudio.createEngine();
        System.out.println("restartNativeAudioPlayEngine:006");
        NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels);
        System.out.println("restartNativeAudioPlayEngine:007");
        NativeAudio.n_cur_buf = 1;
        NativeAudio.n_bytes_in_buffer_1 = 0;
        NativeAudio.n_bytes_in_buffer_2 = 0;
        System.out.println("restartNativeAudioPlayEngine:008");
        int res = NativeAudio.PlayPCM16(1);
        System.out.println("restartNativeAudioPlayEngine:009:res=" + res);
    }

    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

    /**
     * Native methods, implemented in jni folder
     */
    public static native void createEngine();

    public static native void createBufferQueueAudioPlayer(int sampleRate, int channels);

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
