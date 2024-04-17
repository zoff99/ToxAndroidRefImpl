package com.zoffcc.applications.trifa;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.core.graphics.BitmapStorage.writeToTestStorage;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.screenshot.ViewInteractionCapture.captureToBitmap;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.runner.lifecycle.Stage.RESUMED;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class JavaFriendTester
{
    private static final String TAG = "TEST003";
    //
    private static final String MOCK_PASSWORD = "öWOIA>C9iq2v<q0230i2q4$&%$/S3p95ig0_92";
    @Rule
    public ActivityScenarioRule<StartMainActivityWrapper> rule = new ActivityScenarioRule<>(
            StartMainActivityWrapper.class);
    //
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA,
                                                                               Manifest.permission.RECORD_AUDIO,
                                                                               Manifest.permission.WRITE_EXTERNAL_STORAGE);
    private static Activity currentActivity = null;

    @Test
    public void Test_Startup()
    {
        Log.i(TAG, "Test_Startup");
        ActivityScenario<StartMainActivityWrapper> scenario = rule.getScenario();

        String cur_act = getActivityInstance().getLocalClassName();
        Log.i(TAG, "ACT:" + cur_act);

        boolean showing_app = false;
        int app_showing_cycles = 0;
        while (!showing_app)
        {
            if (app_showing_cycles > 120)
            {
                Log.i(TAG, "App did not load");
                cause_error(1);
            }
            cur_act = getActivityInstance().getLocalClassName();
            if (cur_act.equals("CheckPasswordActivity"))
            {
                showing_app = true;
            }
            else if (cur_act.equals("SetPasswordActivity"))
            {
                showing_app = true;
            }
            else
            {
                app_showing_cycles++;
            }
            wait_(1, "until app is showing");
        }

        setSharedPrefs();
        PREF__window_security = false;
        Log.i(TAG, "PREF__window_security:002=" + PREF__window_security);

        screenshot("001");

        if (cur_act.equals("CheckPasswordActivity"))
        {
            onView(withId(R.id.password_1_c)).perform(replaceText(MOCK_PASSWORD));
            screenshot("002a");
            onView(withId(R.id.set_button_2)).perform(click());
        }
        else if (cur_act.equals("SetPasswordActivity"))
        {
            onView(withId(R.id.password_1)).perform(replaceText(MOCK_PASSWORD));
            onView(withId(R.id.password_2)).perform(replaceText(MOCK_PASSWORD));
            screenshot("002b");
            onView(withId(R.id.set_button)).perform(click());
        }
        else
        {
            cause_error(2);
        }

        Log.i(TAG, "checking for AlertDialog");

        try
        {
            onView(withId(android.R.id.button2)).check(matches(isDisplayed()));

        /*
        For an AlertDialog, the id assigned for each button is:
        POSITIVE: android.R.id.button1
        NEGATIVE: android.R.id.button2
        NEUTRAL: android.R.id.button3
        */
            // click NO on Dialog asking to disable battery optimisations for app
            onView(withId(android.R.id.button2)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
            Log.i(TAG, "AlertDialog: \"NO\" button clicked");

        }
        catch (NoMatchingViewException e)
        {
            //view not displayed logic
            Log.i(TAG, "checking for AlertDialog:View does not show, that is ok");
        }

        boolean tox_online = false;
        while (!tox_online)
        {
            tox_online = MainActivity.tox_self_get_connection_status() != 0;
            // HINT: wait for tox to get online
            wait_(1, "for tox to get online");
        }

        setSharedPrefs();
        PREF__window_security = false;
        Log.i(TAG, "PREF__window_security:001=" + PREF__window_security);

        // HINT: after we are online give it another 5 seconds
        wait_(5);

        // HINT: we are online here ----------------------
        final String mytoxid = MainActivity.get_my_toxid();
        Log.i(TAG, "my_toxid:" + mytoxid);
        testwrite("001", mytoxid);

        long loops = 0;
        final long max_loops = 30;
        int count_friends = orma.selectFromFriendList().count();
        while (count_friends < 2)
        {
            wait_(2);
            loops++;
            if (loops > max_loops)
            {
                Log.i(TAG, "ERROR: waiting to long for friend");
                break;
            }
            count_friends = orma.selectFromFriendList().count();
        }

        screenshot("004a");
        wait_(12);
        screenshot("004b");
        onView(
                getElementFromMatchAtPosition(
                    allOf(withId(R.id.f_avatar_icon), withParent(withId(R.id.friend_line_container)))
                    ,0
                )
        ).perform(click());

        wait_(1);
        Espresso.closeSoftKeyboard();
        screenshot("005");
        wait_(120);
        screenshot("099");
    }

    private static void testwrite(final String num, final String text)
    {
        final String file_with_path = currentActivity.getExternalFilesDir(null).getAbsolutePath() + "/" + num + ".txt";
        try
        {
            java.io.File file = new java.io.File(file_with_path);
            java.io.FileOutputStream fileOutput = new java.io.FileOutputStream(file);
            java.io.OutputStreamWriter outputStreamWriter = new java.io.OutputStreamWriter(fileOutput);
            outputStreamWriter.write(text);
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
            Log.i(TAG, "testwrite:write text: "+ file_with_path);
        }
        catch (Exception e)
        {
            Log.i(TAG, "testwrite:ERROR writing text: "+ file_with_path + " E:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void screenshot(final String num)
    {
        try
        {
            writeToTestStorage(captureToBitmap(onView(isRoot())), "test_" + num);
            Log.i(TAG, "capture screenshot: "+ "test_" + num + ".png");
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERROR on ca pturing screenshot: "+ "test_" + num + ".png" + " E:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void wait_(final long seconds)
    {
        wait_(seconds, null);
    }

    private static void wait_(final long seconds, String custom_message_addon)
    {
        if (custom_message_addon != null)
        {
            Log.i(TAG, "sleeping " + seconds + " seconds " + custom_message_addon);
        }
        else
        {
            Log.i(TAG, "sleeping " + seconds + " seconds");
        }
        SystemClock.sleep(seconds * 1000);
        Log.i(TAG, "sleeping ended");
    }

    private static Matcher<View> getElementFromMatchAtPosition(final Matcher<View> matcher, final int position)
    {
        return new BaseMatcher<View>()
        {
            int counter = 0;

            @Override
            public boolean matches(final Object item)
            {
                if (matcher.matches(item))
                {
                    if (counter == position)
                    {
                        counter++;
                        return true;
                    }
                    counter++;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Element at hierarchy position " + position);
            }
        };
    }

    private static void cause_error(int errnum)
    {
        Log.i(TAG, "___ERROR_at_TESTS___:" + errnum);
        if (errnum == 1)
        {
            onView(withId(R.id.bugButton1)).perform(replaceText(MOCK_PASSWORD));
        }
        else if (errnum == 2)
        {
            onView(withId(R.id.bugButton2)).perform(replaceText(MOCK_PASSWORD));
        }
        else if (errnum == 3)
        {
            onView(withId(R.id.bugButton3)).perform(replaceText(MOCK_PASSWORD));
        }
        else
        {
            onView(withId(R.id.bugButton)).perform(replaceText(MOCK_PASSWORD));
        }
    }

    public void setSharedPrefs()
    {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit() ;
        editor.putBoolean("window_security", false) ;
        editor.apply();
        editor.commit();
        Log.i(TAG ,"Setting up shared prefs");
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

    public String getViewInteractionText(ViewInteraction matcher)
    {
        final String[] text = new String[1];
        ViewAction va = new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints()
            {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription()
            {
                return "Text of the view";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                TextView tv = (TextView) view;
                text[0] = tv.getText().toString();
            }
        };

        matcher.perform(va);
        return text[0];
    }
}