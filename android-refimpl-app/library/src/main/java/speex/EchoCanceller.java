package speex;

import android.util.Log;

public class EchoCanceller
{
    private static final String TAG = "speex.EchoCanceller";

    static
    {
        try
        {
            System.loadLibrary("speex");
            Log.i(TAG, "successfully loaded speex library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            Log.i(TAG, "loadLibrary speex failed!");
            e.printStackTrace();
        }
    }

    /*
     for more info see:
     https://speex.org/docs/manual/speex-manual/node7.html#SECTION00740000000000000000
     */

    /*
     frame_size is the amount of data (in samples) you want to process at once and filter_length
     is the length (in samples) of the echo cancelling filter you want to use
     (also known as tail length). It is recommended to use a frame size in the order
     of 20 ms (or equal to the codec frame size) and make sure it is easy to perform
     an FFT of that size (powers of two are better than prime sizes). The recommended
     tail length is approximately the third of the room reverberation time. For example,
     in a small room, reverberation time is in the order of 300 ms, so a tail length of
     100 ms is a good choice (800 samples at 8000 Hz sampling rate).
     */
    public native int open(int sampleRate, int frame_size, int filter_length);

    public native short[] process(short[] input_frame, short[] echo_frame);

    public native short[] capture(short[] input_frame);

    public native short[] playback(short[] echo_frame);

    public native void close();
}