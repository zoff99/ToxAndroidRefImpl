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

    public native int open(int sampleRate, int bufSize, int totalSize);

    public native short[] process(short[] input_frame, short[] echo_frame);

    public native short[] capture(short[] input_frame);

    public native short[] playback(short[] echo_frame);

    public native void close();
}