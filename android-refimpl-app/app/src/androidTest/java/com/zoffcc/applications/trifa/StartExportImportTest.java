package com.zoffcc.applications.trifa;

import android.Manifest;
import android.app.Activity;
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

import java.util.ArrayList;
import java.util.Collection;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.core.graphics.BitmapStorage.writeToTestStorage;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.screenshot.ViewInteractionCapture.captureToBitmap;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.runner.lifecycle.Stage.RESUMED;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_EXPORT_DIR;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartExportImportTest
{
    private static final String TAG = "TEST002";
    //
    private static final String MOCK_PASSWORD = "öWOIA>C9iq2v<q0230i2q4$&%$/S3p95ig0_92";
    private static final String MOCK_TEST_MSG = "Hello, test äößß";
    private static final String TRIFA_PUBLIC_GROUP_ID = "154b3973bd0e66304fd6179a8a54759073649e09e6e368f0334fc6ed666ab762";
    @Rule
    public ActivityScenarioRule<StartMainActivityWrapper> rule = new ActivityScenarioRule<>(
            StartMainActivityWrapper.class);
    //
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA,
                                                                               Manifest.permission.RECORD_AUDIO,
                                                                               Manifest.permission.WRITE_EXTERNAL_STORAGE);
    private Activity currentActivity = null;

    public static ViewAction custom_swipeDown()
    {
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER,
                                      Press.FINGER);
    }

    public static ViewAction custom_swipeUp()
    {
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER,
                                      Press.FINGER);
    }

    private static void screenshot(final String num)
    {
        try
        {
            writeToTestStorage(captureToBitmap(onView(isRoot())), "test_" + num);
            Log.i(TAG, "capture screenshot: " + "test_" + num + ".png");
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERROR on capturing screenshot: " + "test_" + num + ".png" + " E:" + e.getMessage());
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

        screenshot("004");

        Espresso.closeSoftKeyboard();
        MainActivity.main_drawer.openDrawer();
        wait_(1);
        onView(withText(R.string.MainActivity_maint)).perform(click());
        wait_(1, "for Maintenance to show");

        //onView(withId(R.id.buttons_scroll_view))
        //        .perform(repeatedlyUntil(swipeUp(),
        //          hasDescendant(withText(R.string.layout___export_tox_savedata_unsecure)),
        //                                                            10));

        onView(withId(R.id.buttons_scroll_view)).perform(custom_swipeUp());
        try
        {
            onView(withId(R.id.button_export_savedata)).check(matches(isDisplayed())).perform(click());
            Log.i(TAG, "exporting tox save file:01");
        }
        catch (Exception e)
        {
            onView(withId(R.id.buttons_scroll_view)).perform(custom_swipeUp());
            try
            {
                onView(withId(R.id.button_export_savedata)).check(matches(isDisplayed())).perform(click());
                Log.i(TAG, "exporting tox save file:02");
            }
            catch (Exception e2)
            {
                onView(withId(R.id.buttons_scroll_view)).perform(custom_swipeUp());
                onView(withId(R.id.button_export_savedata)).check(matches(isDisplayed())).perform(click());
                Log.i(TAG, "exporting tox save file:03");
            }
        }

        dialog_press_positive(3);

        wait_(1, "for tox save file to export");

        export_tox_save();
        wait_(2);
        import_tox_save();

        wait_(120);
        screenshot("099");
    }

    private static void import_tox_save()
    {
        try
        {
            onView(withId(R.id.button_import_savedata)).check(matches(isDisplayed())).perform(click());
            Log.i(TAG, "importing tox save file:01");
        }
        catch (Exception e)
        {
            onView(withId(R.id.buttons_scroll_view)).perform(custom_swipeUp());
            try
            {
                onView(withId(R.id.button_import_savedata)).check(matches(isDisplayed())).perform(click());
                Log.i(TAG, "importing tox save file:02");
            }
            catch (Exception e2)
            {
                onView(withId(R.id.buttons_scroll_view)).perform(custom_swipeUp());
                onView(withId(R.id.button_import_savedata)).check(matches(isDisplayed())).perform(click());
                Log.i(TAG, "importing tox save file:03");
            }
        }

        wait_(1);
        Log.i(TAG, "Pressing \"YES\" button to import");
        dialog_press_positive(7);
        wait_(5);
        Log.i(TAG, "Pressing \"YES\" button to restart TRIfA");
        dialog_press_positive(8);
    }

    private static void dialog_press_positive(final int errnum)
    {
        try
        {
            onView(withId(android.R.id.button1)).check(matches(isDisplayed()));

        /*
        For an AlertDialog, the id assigned for each button is:
        POSITIVE: android.R.id.button1
        NEGATIVE: android.R.id.button2
        NEUTRAL: android.R.id.button3
        */
            // click NO on Dialog asking to disable battery optimisations for app
            onView(withId(android.R.id.button1)).perform(click());
            Log.i(TAG, "Dialog: \"YES\" button clicked");

        }
        catch (Exception e)
        {
            // view not displayed
            Log.i(TAG, "View does not show");
            cause_error(errnum);
        }
    }

    private static void export_tox_save()
    {
        Log.i(TAG, "SD_CARD_FILES_EXPORT_DIR=" + SD_CARD_FILES_EXPORT_DIR);
        java.io.File export_file = new java.io.File(SD_CARD_FILES_EXPORT_DIR + "/" + "unsecure_export_savedata.tox");
        if ((export_file.exists()) && (export_file.length() > 0))
        {
            Log.i(TAG, "tox save file exists, filesize=" + export_file.length());
        }
        else
        {
            cause_error(4);
        }

        java.io.File import_file = new java.io.File(SD_CARD_FILES_EXPORT_DIR + "/" + "I_WANT_TO_IMPORT_savedata.tox");
        try
        {
            if (!export_file.renameTo(import_file))
            {
                cause_error(6);
            }
            else
            {
                Log.i(TAG, "rename export file to import file: " + export_file.getAbsolutePath() + " -> " +
                           import_file.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            cause_error(5);
        }
    }

    public void setSharedPrefs()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getInstrumentation().getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("window_security", false);
        editor.apply();
        editor.commit();
        Log.i(TAG, "Setting up shared prefs");
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