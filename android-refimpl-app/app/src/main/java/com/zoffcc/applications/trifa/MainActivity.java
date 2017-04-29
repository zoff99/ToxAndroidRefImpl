package com.zoffcc.applications.trifa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

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
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
        }
    }
}
