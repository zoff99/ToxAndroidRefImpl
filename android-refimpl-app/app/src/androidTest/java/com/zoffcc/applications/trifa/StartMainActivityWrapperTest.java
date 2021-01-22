package com.zoffcc.applications.trifa;

import android.Manifest;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.runner.lifecycle.Stage.RESUMED;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartMainActivityWrapperTest
{
    private static final String TAG = "TEST001";
    //
    private static final String MOCK_PASSWORD = "öWOIA>C9iq2v<q0230i2q4$&%$/S3p95ig0_92";
    private Activity currentActivity = null;
    //

    @Rule
    public ActivityScenarioRule<StartMainActivityWrapper> rule = new ActivityScenarioRule<>(
            StartMainActivityWrapper.class);

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA,
                                                                               Manifest.permission.RECORD_AUDIO,
                                                                               Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void Test_Startup()
    {
        Log.i(TAG, "Test_Startup");
        ActivityScenario<StartMainActivityWrapper> scenario = rule.getScenario();

        String cur_act = getActivityInstance().getLocalClassName();
        Log.i(TAG, "ACT:" + cur_act);

        if (cur_act.equals("CheckPasswordActivity"))
        {
            onView(withId(R.id.password_1)).perform(replaceText(MOCK_PASSWORD));
            onView(withId(R.id.set_button)).perform(click());
        }
        else if (cur_act.equals("SetPasswordActivity"))
        {
            onView(withId(R.id.password_1)).perform(replaceText(MOCK_PASSWORD));
            onView(withId(R.id.password_2)).perform(replaceText(MOCK_PASSWORD));
            onView(withId(R.id.set_button)).perform(click());
        }
        else
        {
            // cause ERROR -----
            onView(withId(R.id.bugButton)).perform(replaceText(MOCK_PASSWORD));
            // cause ERROR -----
        }

        /*
        long loops = 0;
        boolean wait_for_main_act = true;
        while (wait_for_main_act)
        {
            loops++;
            cur_act = getActivityInstance().getLocalClassName();

            if (cur_act.equals(""))
            {
                wait_for_main_act = false;
            }

            if (loops > 60)
            {
                // after 6 seconds cause ERROR.
                onView(withId(R.id.bugButton)).perform(replaceText(MOCK_PASSWORD));
                // after 6 seconds cause ERROR.
            }

            SystemClock.sleep(200);
        }
        */

        Log.i(TAG, "checking for AlertDialog");

        try
        {
            onView(withId(android.R.id.button2)).
                    check(matches(isDisplayed()));

        /*
        For an AlertDialog, the id assigned for each button is:
        POSITIVE: android.R.id.button1
        NEGATIVE: android.R.id.button2
        NEUTRAL: android.R.id.button3
        */
            // click NO on Dialog asking to disable battery optimisations for app
            onView(withId(android.R.id.button2)).
                    inRoot(isDialog()).
                    check(matches(isDisplayed())).
                    perform(click());

            Log.i(TAG, "AlertDialog: \"NO\" button clicked");

        }
        catch (NoMatchingViewException e)
        {
            //view not displayed logic
            Log.i(TAG, "checking for AlertDialog:View does not show, that is ok");
        }

        Log.i(TAG, "sleeping for 40 seconds");
        // sleep 10 seconds
        SystemClock.sleep(40000);
        Log.i(TAG, "sleeping ended");
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

    public Activity getActivityInstance()
    {
        getInstrumentation().runOnMainSync(new Runnable()
        {
            public void run()
            {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                        RESUMED);
                if (resumedActivities.iterator().hasNext())
                {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity;
    }

    public void grant_permissions()
    {
        // ----- persmission -----
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        for (int i = 0; i < permissions.size(); i++)
        {
            String command = String.format("pm grant %s %s", getTargetContext().getPackageName(), permissions.get(i));
            getInstrumentation().getUiAutomation().executeShellCommand(command);
            // wait a bit until the command is finished
            SystemClock.sleep(2000);
        }
        // ----- persmission -----
    }
}