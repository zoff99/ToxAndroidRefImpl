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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    TextView mt = null;
    static boolean native_lib_loaded = false;
    static String app_files_directory = "";
    static boolean stop_me = false;
    static Thread ToxServiceThread = null;
    Handler main_handler = null;
    static Handler main_handler_s = null;
    static Context context_s = null;
    static Notification notification = null;
    static NotificationManager nMN = null;
    static int NOTIFICATION_ID = 293821038;
    static RemoteViews notification_view = null;
    static long[] friends = null;
    static FriendListFragment friend_list_fragment = null;
    static OrmaDatabase orma = null;
    final static String FriendList_DB_NAME = "friendlist.db";
    final static int AddFriendActivity_ID = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mt = (TextView) this.findViewById(R.id.main_maintext);
        mt.setText("...");

        main_handler = new Handler(getMainLooper());
        main_handler_s = main_handler;
        context_s = this.getBaseContext();

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

        // See OrmaDatabaseBuilderBase for other options.
        orma = OrmaDatabase.builder(this).name(FriendList_DB_NAME).build();
        // default: "${applicationId}.orma.db"

        app_files_directory = getFilesDir().getAbsolutePath();
        init(app_files_directory);

        // -- notification ------------------
        // -- notification ------------------
        nMN = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification_view = new RemoteViews(getPackageName(), R.layout.custom_notification);
        Log.i(TAG, "contentView=" + notification_view);
        notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
        notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE");
        notification_view.setTextViewText(R.id.text, "");

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setContent(notification_view);
        b.setSmallIcon(R.drawable.circle_red);
        notification = b.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        nMN.notify(NOTIFICATION_ID, notification);
        // -- notification ------------------
        // -- notification ------------------

        tox_thread_start();
    }

    void tox_thread_start()
    {
        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {
                // ------ correct startup order ------
                bootstrap();
                final String my_ToxId = get_my_toxid();
                Log.i(TAG, "my_ToxId=" + my_ToxId);


                // -------------- DEBUG --------------
                // -------------- DEBUG --------------
                // -------------- DEBUG --------------
                // ------ // orma.deleteAll();
                // -------------- DEBUG --------------
                // -------------- DEBUG --------------
                // -------------- DEBUG --------------

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mt.setText(mt.getText() + "\n" + "my_ToxId=" + my_ToxId);
                    }
                };
                main_handler_s.post(myRunnable);

                init_tox_callbacks();
                update_savedata_file();
                // ------ correct startup order ------

                friends = tox_self_get_friend_list();
                Log.i(TAG, "number of friends=" + friends.length);

                int fc = 0;
                boolean exists_in_db = false;
                for (fc = 0; fc < friends.length; fc++)
                {
                    Log.i(TAG, "loading friend  #:" + fc);

                    FriendList f;
                    List<FriendList> fl = orma.selectFromFriendList().tox_friendnumEq(fc).toList();
                    if (fl.size() > 0)
                    {
                        f = fl.get(0);
                    }
                    else
                    {
                        f = null;
                    }

                    if (f == null)
                    {
                        f = new FriendList();
                        f.tox_public_key_string = "" + (Math.random() * 100000);
                        f.tox_friendnum = fc;
                        f.name = "friend #" + fc;
                        exists_in_db = false;
                    }
                    else
                    {
                        Log.i(TAG, "found friend in DB " + f.tox_friendnum + " f=" + f);
                        exists_in_db = true;
                    }

                    f.TOXCONNECTION = 0;
                    if (friend_list_fragment != null)
                    {
                        friend_list_fragment.add_friends(f);
                    }

                    // set all to OFFLINE and AVAILABLE
                    f.TOX_USER_STATUS = 0;
                    f.TOXCONNECTION = 0;
                    // set all to OFFLINE and AVAILABLE

                    if (exists_in_db == false)
                    {
                        orma.insertIntoFriendList(f);
                    }
                    else
                    {
                        orma.updateFriendList().tox_friendnumEq(f.tox_friendnum).tox_friendnum(f.tox_friendnum).tox_public_key_string(f.tox_public_key_string).name(f.name).status_message(f.status_message).TOXCONNECTION(f.TOXCONNECTION).TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                    }
                }

                long tox_iteration_interval_ms = tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                tox_iterate();

                while (!stop_me)
                {
                    try
                    {
                        sleep(tox_iteration_interval_ms);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    tox_iterate();

                }
            }
        };

        ToxServiceThread.start();
    }

    static void stop_tox()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                stop_me = true;
                ToxServiceThread.interrupt();
                try
                {
                    ToxServiceThread.join();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                nMN.cancel(NOTIFICATION_ID);
                MainActivity.exit();
            }
        };
        main_handler_s.post(myRunnable);
    }

    @Override
    protected void onDestroy()
    {
        nMN.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    static FriendList main_get_friend(long friendnum)
    {
        FriendList f;
        List<FriendList> fl = orma.selectFromFriendList().tox_friendnumEq(friendnum).toList();
        if (fl.size() > 0)
        {
            f = fl.get(0);
        }
        else
        {
            f = null;
        }

        return f;
    }

    synchronized static void update_friend_in_db(FriendList f)
    {
        orma.updateFriendList().
                tox_friendnumEq(f.tox_friendnum).
                tox_public_key_string(f.tox_public_key_string).
                name(f.name).
                status_message(f.status_message).
                TOXCONNECTION(f.TOXCONNECTION).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();
    }

    static void change_notification(int a_TOXCONNECTION)
    {

        final int a_TOXCONNECTION__f = a_TOXCONNECTION;
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                NotificationCompat.Builder b = new NotificationCompat.Builder(context_s);

                if (a_TOXCONNECTION__f == 0)
                {
                    notification_view.setImageViewResource(R.id.image, R.drawable.circle_red);
                    b.setSmallIcon(R.drawable.circle_red);
                    notification_view.setTextViewText(R.id.title, "Tox Service: " + "OFFLINE");
                }
                else
                {
                    if (a_TOXCONNECTION__f == 1)
                    {
                        notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                        b.setSmallIcon(R.drawable.circle_green);
                        notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [TCP]");
                    }
                    else // if (a_TOXCONNECTION__f == 2)
                    {
                        notification_view.setImageViewResource(R.id.image, R.drawable.circle_green);
                        b.setSmallIcon(R.drawable.circle_green);
                        notification_view.setTextViewText(R.id.title, "Tox Service: " + "ONLINE [UDP]");
                    }
                }
                notification_view.setTextViewText(R.id.text, "");

                b.setContent(notification_view);
                notification = b.build();
                notification.flags |= Notification.FLAG_NO_CLEAR;
                nMN.notify(NOTIFICATION_ID, notification);

            }
        };
        main_handler_s.post(myRunnable);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        stop_tox();
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public native void init(@NonNull String data_dir);

    // public native void toxloop();

    public native String getNativeLibAPI();

    public static native void update_savedata_file();

    public static native String get_my_toxid();

    public static native void bootstrap();

    public static native void init_tox_callbacks();

    public static native long tox_iteration_interval();

    public static native long tox_iterate();

    public static native long tox_kill();

    public static native void exit();

    public static native long tox_friend_add(@NonNull String toxid_str, @NonNull String message);

    public static native long tox_friend_add_norequest(@NonNull String public_key_str);

    public static native long tox_self_get_friend_list_size();

    public static native long tox_friend_by_public_key(@NonNull String friend_public_key_string);

    public static native long[] tox_self_get_friend_list();
    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_cb_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "self_connection_status:" + a_TOX_CONNECTION);

        // -- notification ------------------
        // -- notification ------------------
        change_notification(a_TOX_CONNECTION);
        // -- notification ------------------
        // -- notification ------------------
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        Log.i(TAG, "friend_name:friend:" + friend_number + " name:" + friend_name);

        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.name = friend_name;
                update_friend_in_db(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
        Log.i(TAG, "friend_status_message:friend:" + friend_number + " status message:" + status_message);

        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.status_message = status_message;
                update_friend_in_db(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
        Log.i(TAG, "friend_status:friend:" + friend_number + " status:" + a_TOX_USER_STATUS);

        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.TOX_USER_STATUS = a_TOX_USER_STATUS;
                update_friend_in_db(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
        if (friend_list_fragment != null)
        {
            FriendList f = main_get_friend(friend_number);
            if (f != null)
            {
                f.TOXCONNECTION = a_TOX_CONNECTION;
                update_friend_in_db(f);
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, int b)
    {
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long b)
    {
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);

        final String friend_public_key__final = friend_public_key;

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(20); // wait a bit
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                long friendnum = tox_friend_add_norequest(friend_public_key__final); // add friend
                update_savedata_file(); // save toxcore datafile (new friend added)

                FriendList f = new FriendList();
                f.tox_public_key_string = friend_public_key__final;
                f.tox_friendnum = friendnum;
                f.TOX_USER_STATUS = 0;
                f.TOXCONNECTION = 0;

                try
                {
                    orma.insertIntoFriendList(f);
                }
                catch (android.database.sqlite.SQLiteConstraintException)
                {
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
            }
        }; t.start();
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        Log.i(TAG, "friend_message:friend:" + friend_number + " message:" + friend_message);
    }

    // void test(int i)
    // {
    //    Log.i(TAG, "test:" + i);
    // }

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

    public void show_add_friend(View view)
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        // intent.putExtra("key", value);
        startActivityForResult(intent, AddFriendActivity_ID);
    }

    static void insert_into_friendlist_db(final FriendList f)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.insertIntoFriendList(f);
            }
        };
        t.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddFriendActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                String friend_tox_id1 = data.getStringExtra("toxid");
                String friend_tox_id = "";
                friend_tox_id = friend_tox_id1.toUpperCase().replace(" ", "").replaceFirst("tox:", "").replaceFirst("TOX:", "").replaceFirst("Tox:", "");

                Log.i(TAG, "add friend ID:" + friend_tox_id);

                // add friend ---------------
                long friendnum = tox_friend_add(friend_tox_id, "please add me"); // add friend
                Log.i(TAG, "add friend  #:" + friendnum);
                update_savedata_file(); // save toxcore datafile (new friend added)

                if (friendnum > -1)
                {
                    // nospam=8 chars, checksum=4 chars
                    String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
                    Log.i(TAG, "add friend PK:" + friend_public_key);

                    FriendList f = new FriendList();
                    f.tox_public_key_string = friend_public_key;
                    f.tox_friendnum = friendnum;
                    f.TOX_USER_STATUS = 0;
                    f.TOXCONNECTION = 0;

                    try
                    {
                        insert_into_friendlist_db(f);
                    }
                    catch (android.database.sqlite.SQLiteConstraintException)
                    {
                    }
                }

                if (friendnum == -1)
                {
                    Log.i(TAG, "friend already added, or request already sent");
                }

                // add friend ---------------
            }
            else
            {
                // (resultCode == RESULT_CANCELED)
            }
        }
    }
}

