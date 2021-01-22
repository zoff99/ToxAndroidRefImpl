/**
 * Zoff Basic Logging Utils
 * Copyright (C) 2016 Zoff <zoff@zoff.cc>
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

package com.zoffcc.applications.logging;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Locale;

public class Logging
{

    private static final String TAG = Logging.class.getName();
    public static AsyncResponse delegate = null;

    // ------------------------------------
    // thanks to:
    //
    // https://github.com/WhisperSystems/Signal-Android/blob/master/src/org/thoughtcrime/securesms/LogSubmitActivity.java
    //
    // and new:
    //
    // https://github.com/signalapp/Signal-Android/blob/master/app/src/main/java/org/thoughtcrime/securesms/logsubmit/LogSectionLogcat.java
    //
    // ------------------------------------
    private static String grabLogcat()
    {
        try
        {
            final Process process = Runtime.getRuntime().exec("logcat -d -v threadtime " + "System.out:I AndroidRuntime:E *:S");

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final StringBuilder log = new StringBuilder();
            final String separator = System.getProperty("line.separator");

            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line);
                log.append(separator);
            }

            if ((log.length() < 100) || (VERSION.SDK_INT < VERSION_CODES.KITKAT))
            {
                // some problems with the params?
                final Process process2 = Runtime.getRuntime().exec("logcat -d");
                final BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
                final StringBuilder log2 = new StringBuilder();

                String line2;
                while ((line2 = bufferedReader2.readLine()) != null)
                {
                    log2.append(line2);
                    log2.append(separator);
                }

                Log.i("ZLOGGING", "======== 2 ========");
                Log.i("ZLOGGING", log2.toString());
                return log2.toString();
            }
            else
            {
                Log.i("ZLOGGING", "======== 1 ========");
                Log.i("ZLOGGING", log.toString());
                return log.toString();
            }
        }
        catch (IOException ioe)
        {
            Log.w(TAG, "IOException when trying to read logcat.", ioe);
            return null;
        }
        catch (Exception e)
        {
            Log.w(TAG, "Exception when trying to read logcat.", e);
            return null;
        }
    }

    // grab logs --------
    //public void grab_logs(Context context)
    //{
    //	new PopulateLogcatAsyncTask(context).execute();
    //}

    public class PopulateLogcatAsyncTask extends AsyncTask<Void, Void, String>
    {
        private WeakReference<Context> weakContext;

        public PopulateLogcatAsyncTask(Context context)
        {
            this.weakContext = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            Context context = weakContext.get();
            if (context == null)
            {
                return null;
            }

            return buildDescription(context) + "\n" + grabLogcat();
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String logcat)
        {
            try
            {
                delegate.processFinish(logcat);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private static String buildDescription(Context context)
    {
        final PackageManager pm = context.getPackageManager();
        final StringBuilder builder = new StringBuilder();

        try
        {
            builder.append("PID     : ").append(android.os.Process.myPid()).append("\n");
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        builder.append("Device  : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(")\n");

        try
        {
            if (VERSION.SDK_INT >= 21)
            {
                builder.append("ABI     : ").append(Arrays.toString(Build.SUPPORTED_ABIS)).append("\n");
            }
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        try
        {
            builder.append("ABI1    : ").append(Build.CPU_ABI).append("\n");
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        try
        {
            builder.append("ABI2    : ").append(Build.CPU_ABI2).append("\n");
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        builder.append("Android : ").append(VERSION.RELEASE).append(" (").append(VERSION.INCREMENTAL).append(", ").append(Build.DISPLAY).append(")\n");
        builder.append("Memory  : ").append(getMemoryUsage(context)).append("\n");
        builder.append("Memclass: ").append(getMemoryClass(context)).append("\n");
        builder.append("OS Host : ").append(Build.HOST).append("\n");
        builder.append("App     : ");
        try
        {
            builder.append(pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(), 0))).append(" ").append(pm.getPackageInfo(context.getPackageName(), 0).versionName).append("\n");
        }
        catch (PackageManager.NameNotFoundException nnfe)
        {
            builder.append("Unknown\n");
        }

        return builder.toString();
    }

    private static long asMegs(long bytes)
    {
        return bytes / 1048576L;
    }

    public static String getMemoryUsage(Context context)
    {
        Runtime info = Runtime.getRuntime();
        info.totalMemory();
        return String.format(Locale.ENGLISH, "%dM (%.2f%% free, %dM max)", asMegs(info.totalMemory()), (float) info.freeMemory() / info.totalMemory() * 100f, asMegs(info.maxMemory()));
    }

    @TargetApi(VERSION_CODES.KITKAT)
    public static String getMemoryClass(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String lowMem = "";

        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT && activityManager.isLowRamDevice())
        {
            lowMem = ", low-mem device";
        }
        return activityManager.getMemoryClass() + lowMem;
    }

    public static void writeToFile(String data, Context context, String full_path_filename)
    {
        try
        {
            final File file = new File(full_path_filename);
            if (!file.exists())
            {
                try
                {
                    file.createNewFile();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.w(TAG, "File write failed (01): " + e2.toString());
                }
            }
            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fout);
            outputStreamWriter.append(data);
            outputStreamWriter.close();
            fout.flush();
            fout.close();
        }
        catch (Exception e)
        {
            Log.w(TAG, "File write failed (02):" + e.toString());
        }
    }

    public interface AsyncResponse
    {
        void processFinish(String output);
    }
}
