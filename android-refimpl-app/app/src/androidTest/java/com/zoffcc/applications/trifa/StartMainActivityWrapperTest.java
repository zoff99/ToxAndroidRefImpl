package com.zoffcc.applications.trifa;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartMainActivityWrapperTest
{

    private static final String TAG = "TEST001";

    @Rule
    public ActivityScenarioRule<StartMainActivityWrapper> rule = new ActivityScenarioRule<>(
            StartMainActivityWrapper.class);

    @Test
    public void Test_Startup()
    {
        Log.i(TAG, "Test_Startup");
        ActivityScenario<StartMainActivityWrapper> scenario = rule.getScenario();
        // Your test code goes here.
    }

    @Before
    public void setUp() throws Exception
    {
        Log.i(TAG, "setUp");
    }

    @After
    public void tearDown() throws Exception
    {
        Log.i(TAG, "tearDown");
    }
}