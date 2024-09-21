package me.jagar.chatvoiceplayerlibrary;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUtils {

    public static final int MAX_FILE_SIZE_BYTES = 100000; // ~100 kByte

    public static void updateVisualizer(final Context context, final File file, final PlayerVisualizerSeekbar playerVisualizerSeekbar){
        new AsyncTask<Void, Void, byte[]>()
        {
            @Override
            protected byte[] doInBackground(Void... voids) {
                return fileToBytes(file);
            }

            @Override
            protected void onPostExecute(final byte[] bytes) {
                super.onPostExecute(bytes);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        playerVisualizerSeekbar.setBytes(bytes);
                        playerVisualizerSeekbar.invalidate();
                    }
                });
            }
        }.execute();
    }

    public static void vupdateVisualizer(final Context context, final info.guardianproject.iocipher.File vfile, final PlayerVisualizerSeekbar playerVisualizerSeekbar){
        new AsyncTask<Void, Void, byte[]>()
        {
            @Override
            protected byte[] doInBackground(Void... voids) {
                return vfileToBytes(vfile);
            }

            @Override
            protected void onPostExecute(final byte[] bytes) {
                super.onPostExecute(bytes);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        playerVisualizerSeekbar.setBytes(bytes);
                        playerVisualizerSeekbar.invalidate();
                    }
                });
            }
        }.execute();
    }

    public static byte[] fileToBytes(File file)
    {
        if (file.length() > MAX_FILE_SIZE_BYTES)
        {
            return null;
        }
        byte[] bytes = null;
        try
        {
            int size = (int) file.length();
            bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return bytes;
    }

    public static byte[] vfileToBytes(info.guardianproject.iocipher.File vfile)
    {
        if (vfile.length() > MAX_FILE_SIZE_BYTES)
        {
            return null;
        }
        byte[] bytes = null;
        try
        {
            int size = (int) vfile.length();
            bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new info.guardianproject.iocipher.FileInputStream(vfile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return bytes;
    }
}
