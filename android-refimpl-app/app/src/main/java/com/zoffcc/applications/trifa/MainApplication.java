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

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.tox_service_fg;
import static com.zoffcc.applications.trifa.TrifaToxService.ONGOING_NOTIFICATION_ID;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;


public class MainApplication extends Application
{
    // -----------------------
    // -----------------------
    // -----------------------
    final static boolean CATCH_EXCEPTIONS = true; // set "true" for release builds!
    // -----------------------
    // -----------------------
    // -----------------------
    static String last_stack_trace_as_string = "";
    int i = 0;
    int crashes = 0;
    long last_crash_time = 0L;
    long prevlast_crash_time = 0L;
    int randnum = -1;
    static final String TAG = "trifa.MainApplication";


    @Override
    public void onCreate()
    {
        randnum = (int) (Math.random() * 1000d);

        Log.i(TAG, "MainApplication:" + randnum + ":" + "onCreate");
        super.onCreate();

        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(new ConnectionManager(), intentFilter);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        crashes = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getInt("crashes", 0);

        if (crashes > 10000)
        {
            crashes = 0;
            PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putInt("crashes", crashes).commit();
        }

        Log.i(TAG, "MainApplication:" + randnum + ":" + "crashes[load]=" + crashes);
        last_crash_time = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getLong("last_crash_time", 0);
        Log.i(TAG, "MainApplication:" + randnum + ":" + "last_crash_time[load]=" + last_crash_time);
        prevlast_crash_time = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getLong("prevlast_crash_time", 0);
        Log.i(TAG, "MainApplication:" + randnum + ":" + "prevlast_crash_time[load]=" + prevlast_crash_time);

        if (CATCH_EXCEPTIONS)
        {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable e)
                {
                    handleUncaughtException(thread, e);
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static String run_adb_command()
    {
        try
        {
            final Process process = Runtime.getRuntime().exec("ps -w -e -T -o PID,TID,CMDLINE,CMD,PRI,NI,STAT,PCY,CPU"); // |grep -i trifa

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final StringBuilder log = new StringBuilder();
            final String separator = System.getProperty("line.separator");

            String line = "";
            log.append("=======================================");
            log.append(separator);
            log.append("=======================================");
            log.append(separator);
            log.append("=======================================");
            log.append(separator);
            log.append("PID,TID,CMDLINE,CMD,PRI,NI,STAT,PCY,CPU");
            log.append(separator);
            log.append("=======================================");
            log.append(separator);
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line);
                log.append(separator);
            }
            log.append("=======================================");
            log.append(separator);
            log.append("=======================================");
            log.append(separator);
            log.append("=======================================");
            log.append(separator);

            return line;
        }
        catch (Exception e)
        {
            Log.i(TAG, "MainApplication:" + "EE3:" + e.getMessage());
            return null;
        }
    }

    private String grabLogcat()
    {
        try
        {
            // grep -r 'Log\.' *|sed -e 's#^.*Log..("##'|grep -v TAG|sed -e 's#",.*$##'|sort |uniq

            final Process process = Runtime.getRuntime().exec("logcat -d -v threadtime");

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final StringBuilder log = new StringBuilder();
            final String separator = System.getProperty("line.separator");

            String line = "";
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line);
                log.append(separator);
            }

            if ((log.length() < 100) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT))
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

                return log2.toString();
            }
            else
            {
                return log.toString();
            }
        }
        catch (IOException ioe)
        {
            Log.i(TAG, "MainApplication:" + randnum + ":" + "IOException when trying to read logcat.");
            return null;
        }
        catch (Exception e)
        {
            Log.i(TAG, "MainApplication:" + randnum + ":" + "Exception when trying to read logcat.");
            return null;
        }
    }

    void save_error_msg() throws IOException
    {

        String log_detailed = grabLogcat();

        try
        {
            // also save to crash file ----
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
            String formattedDate = df.format(c.getTime());
            // File myDir = new File(getExternalFilesDir(null).getAbsolutePath() + "/crashes");
            File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/trifa/crashes");

            myDir.mkdirs();
            File myFile = new File(myDir.getAbsolutePath() + "/crash_" + formattedDate + ".txt");
            Log.i(TAG, "MainApplication:" + randnum + ":" + "crash file=" + myFile.getAbsolutePath());
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append("Errormesage:\n" + last_stack_trace_as_string + "\n\n===================================\n\n" + log_detailed);
            myOutWriter.close();
            fOut.close();
            // also save to crash file ----
        }
        catch (Exception e)
        {
        }
    }

    private void handleUncaughtException(Thread thread, Throwable e)
    {
        last_stack_trace_as_string = e.getMessage();
        boolean stack_trace_ok = false;

        try
        {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            last_stack_trace_as_string = writer.toString();

            Log.i(TAG, "MainApplication:" + randnum + ":" + "stack trace ok");
            stack_trace_ok = true;
        }
        catch (Exception ee)
        {
        }
        catch (OutOfMemoryError ex2)
        {
            Log.i(TAG, "MainApplication:" + randnum + ":" + "stack trace *error*");
        }

        if (!stack_trace_ok)
        {
            try
            {
                last_stack_trace_as_string = Log.getStackTraceString(e);
                Log.i(TAG, "MainApplication:" + randnum + ":" + "stack trace ok (addon 1)");
                stack_trace_ok = true;
            }
            catch (Exception ee)
            {
            }
            catch (OutOfMemoryError ex2)
            {
                Log.i(TAG, "MainApplication:" + randnum + ":" + "stack trace *error* (addon 1)");
            }
        }

        crashes++;
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putInt("crashes", crashes).commit();

        try
        {
            save_error_msg();
        }
        catch (Exception ee)
        {
        }
        catch (OutOfMemoryError ex2)
        {
        }

        Log.i(TAG, "MainApplication:" + randnum + ":" + "crashes[set]=" + crashes);
        Log.i(TAG, "MainApplication:" + randnum + ":" + "?:" + (prevlast_crash_time + (60 * 1000)) + " < " + System.currentTimeMillis());
        Log.i(TAG, "MainApplication:" + randnum + ":" + "?:" + (System.currentTimeMillis() - (prevlast_crash_time + (60 * 1000))));


        try
        {
            // try to shutdown service (but don't exit the app yet!)
            if (is_tox_started)
            {
                tox_service_fg.stop_tox_fg();
                tox_service_fg.stop_me(false);
            }
        }
        catch (Exception e2)
        {
            Log.i(TAG, "MainApplication:EE1:" + e2.getMessage());
            e2.printStackTrace();
        }

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        Log.i(TAG, "MainApplication:" + randnum + ":" + "componentInfo=" + componentInfo + " class=" + componentInfo.getClassName());

        try
        {
            // remove the notofication
            NotificationManager nmn2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nmn2.cancel(ONGOING_NOTIFICATION_ID);
        }
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        Intent intent = new Intent(this, com.zoffcc.applications.trifa.CrashActivity.class);
        Log.i(TAG, "MainApplication:" + randnum + ":" + "xx1 intent(1)=" + intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.i(TAG, "MainApplication:" + randnum + ":" + "xx1 intent(2)=" + intent);
        startActivity(intent); // show CrashActivity
        Log.i(TAG, "MainApplication:" + randnum + ":" + "xx2");
        android.os.Process.killProcess(android.os.Process.myPid());
        Log.i(TAG, "MainApplication:" + randnum + ":" + "xx3");
        System.exit(2);
        System.out.println("MainApplication:" + randnum + ":" + "xx4");

    }
}
