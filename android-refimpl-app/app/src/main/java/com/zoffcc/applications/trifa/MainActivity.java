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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    TextView mt = null;
    static boolean native_lib_loaded = false;
    static String app_files_directory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mt = (TextView) this.findViewById(R.id.maintext);
        mt.setText("...");

        if (native_lib_loaded)
        {
            mt.setText("successfully loaded native library");
        }
        else
        {
            mt.setText("loadLibrary jni-c-toxcore failed!");
        }

        String native_api = getNativeLibAPI();
        mt.setText(mt.getText() + "\n" + native_api);

        app_files_directory = getFilesDir().getAbsolutePath();
        init(app_files_directory);

        tox_thread_start();
    }


    void tox_thread_start()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                toxloop();

                try
                {
                    while (true)
                    {
                        sleep(1000);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public native void init(String data_dir);

    public native void toxloop();

    public native String getNativeLibAPI();
    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "a_TOX_CONNECTION:" + a_TOX_CONNECTION);
    }

    static void android_tox_callback_friend_name_cb_method(long a, String b, long c)
    {
    }

    static void android_tox_callback_friend_status_message_cb_method(long a, String b, long c)
    {
    }

    static void android_tox_callback_friend_status_cb_method(long a, int b)
    {
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
    }

    static void android_tox_callback_friend_typing_cb_method(long a, int b)
    {
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long a, long b)
    {
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        Log.i(TAG, "_friend_request:friend:" + friend_public_key + " message:" + friend_request_message);
    }

    static void android_tox_callback_friend_message_cb_method(long a, int b, String c, long d)
    {
    }

    void test(int i)
    {
        Log.i(TAG, "test:" + i);
    }

    static void logger(int level, String text)
    {
        Log.i(TAG, text);
    }
    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    /*
     * this is used to load the native library on
	 * application startup. The library has already been unpacked at
	 * installation time by the package manager.
	 */
    static
    {
        try
        {
            System.loadLibrary("jni-c-toxcore");
            native_lib_loaded = true;
            Log.i(TAG, "successfully loaded native library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
        }
    }
}
