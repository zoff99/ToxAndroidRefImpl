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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

import static com.zoffcc.applications.trifa.MainActivity.DB_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_DB_NAME;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_VFS_NAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF__DB_secrect_key__user_hash;

public class CheckPasswordActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CheckPasswordActy";

    private UserLoginTask mAuthTask = null;
    private static boolean migrationOccurred = false;

    // UI references.
    private EditText mPasswordView1;
    private View mProgressView;
    private View mLoginFormView;
    private static TextView migration_text_01;
    private static TextView migration_text_02;
    private boolean auto_generated_password = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_password);

        mPasswordView1 = (EditText) findViewById(R.id.password_1);
        mPasswordView1.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.set_button || id == EditorInfo.IME_NULL)
                {
                    attemptUnlock(false, null);
                    return true;
                }
                return false;
            }
        });

        migration_text_01 = (TextView) findViewById(R.id.migration_text_01);
        migration_text_02 = (TextView) findViewById(R.id.migration_text_02);
        migration_text_01.setVisibility(View.GONE);
        migration_text_02.setVisibility(View.GONE);

        Button SignInButton = (Button) findViewById(R.id.set_button);
        SignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptUnlock(false, null);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        /* if the database secret key is saves in preferences, it means the user did NOT want to set a password.
         * and for our purpose we can also skip the password enter screen
         */
        auto_generated_password = false;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final String DB_secrect_key__tmp = settings.getString("DB_secrect_key", "");

        Log.i(TAG, "001");

        if (!TextUtils.isEmpty(DB_secrect_key__tmp))
        {
            Log.i(TAG, "002");
            mPasswordView1.setVisibility(View.INVISIBLE);
            mLoginFormView.setVisibility(View.INVISIBLE);

            auto_generated_password = true;
            Log.i(TAG, "auto_generated_password:true");
            Log.i(TAG, "003");
            attemptUnlock(true, DB_secrect_key__tmp);
            Log.i(TAG, "004");
        }
        else
        {
            Log.i(TAG, "005");
            mPasswordView1.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.VISIBLE);
        }
        Log.i(TAG, "006");
    }


    private void attemptUnlock(boolean auto_generated_pass, String pass)
    {
        Log.i(TAG, "attemptUnlock:auto_generated_pass=" + auto_generated_pass);

        Log.i(TAG, "007");

        if (mAuthTask != null)
        {
            Log.i(TAG, "008");
            return;
        }

        Log.i(TAG, "009");

        try
        {
            mPasswordView1.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.GONE);
        }
        catch (Exception e)
        {
        }

        Log.i(TAG, "010");

        boolean skip_login_form = false;

        if (auto_generated_pass)
        {
            if (pass == null)
            {
                return;
            }
            else
            {
                /* we have an autogenerated password
                 * try to use it
                 */
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                Log.i(TAG, "010a");
                showProgress(true, auto_generated_pass);
                Log.i(TAG, "010b");
                mAuthTask = new UserLoginTask(pass);
                mAuthTask.execute((Boolean) true);
                skip_login_form = true;
            }
        }

        boolean cancel = false;
        View focusView = null;

        Log.i(TAG, "011");

        if (!skip_login_form)
        {

            // Reset errors.
            try
            {
                mPasswordView1.setError(null);
            }
            catch (Exception e)
            {
                cancel = true;
            }

            String password1 = null;
            try
            {
                password1 = mPasswordView1.getText().toString();
            }
            catch (Exception e)
            {
                cancel = true;
            }

            Log.i(TAG, "012");

            if (!cancel)
            {
                if (TextUtils.isEmpty(password1))
                {
                    mPasswordView1.setError("Enter Password");
                    focusView = mPasswordView1;
                    cancel = true;
                    Log.i(TAG, "013");
                }

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(password1) && !SetPasswordActivity.isPasswordValid(password1))
                {
                    mPasswordView1.setError("Invalid Password");
                    focusView = mPasswordView1;
                    cancel = true;
                    Log.i(TAG, "014");
                }
            }

            if (cancel)
            {
                Log.i(TAG, "015");

                // There was an error; don't attempt login and focus the first
                // form field with an error.
                try
                {
                    Log.i(TAG, "016");
                    showProgress(false, false);
                    mPasswordView1.setVisibility(View.VISIBLE);
                    mLoginFormView.setVisibility(View.VISIBLE);
                    focusView.requestFocus();
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                Log.i(TAG, "018");


                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true, auto_generated_pass);
                mAuthTask = new UserLoginTask(password1);
                mAuthTask.execute((Boolean) false);
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(boolean show, final boolean auto_generated_password5)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            try
            {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(
                        new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                            }
                        });

                if ((!show) && (auto_generated_password5))
                {
                    return;
                }
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(
                        new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                            }
                        });

            }
            catch (Exception e)
            {
            }
        }
        else
        {
            try
            {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                if ((!show) && (auto_generated_password5))
                {
                    return;
                }
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
            catch (Exception e)
            {
            }
        }
    }

    static void migration_warning(final boolean show)
    {
        try
        {
            if (show)
            {
                Log.i(TAG, "migration_warning:show");
                migration_text_01.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        migration_text_01.setVisibility(View.VISIBLE);
                    }
                });

                migration_text_02.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        migration_text_02.setVisibility(View.VISIBLE);
                    }
                });
            }
            else
            {
                Log.i(TAG, "migration_warning:hide");
                migration_text_01.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        migration_text_01.setVisibility(View.GONE);
                    }
                });

                migration_text_02.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        migration_text_02.setVisibility(View.GONE);
                    }
                });
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "migration_warning:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * asynchronous task used to check if password is correct to login into the DB
     */
    public class UserLoginTask extends AsyncTask<Boolean, Void, Boolean>
    {

        private final String mPassword1;
        private boolean auto_gen_pass_flag = false;

        UserLoginTask(String password1)
        {
            mPassword1 = password1;
        }

        @Override
        protected Boolean doInBackground(Boolean... params)
        {
            try
            {
                auto_gen_pass_flag = params[0];
            }
            catch (Exception e)
            {
            }
            return check_password(mPassword1, params);
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            Log.i(TAG, "onPostExecute:001");
            mAuthTask = null;
            showProgress(false, auto_gen_pass_flag);
            Log.i(TAG, "onPostExecute:002");

            if (success)
            {
                Log.i(TAG, "onPostExecute:003");
                // ok open main activity
                Intent main_act = new Intent(CheckPasswordActivity.this, MainActivity.class);
                startActivity(main_act);
                finish();
            }
            else
            {
                mPasswordView1.setVisibility(View.VISIBLE);
                mLoginFormView.setVisibility(View.VISIBLE);
                showProgress(false, false);
                mPasswordView1.setError("* Error *");
                mPasswordView1.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false, auto_gen_pass_flag);
        }
    }

    boolean check_password(String try_password, Boolean[] auto_gernerated_pass_flag)
    {
        boolean ret = false;

        try
        {
            String try_password_hash = TrifaSetPatternActivity.bytesToString(
                    TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(try_password)));

            try
            {
                if (auto_gernerated_pass_flag[0] == true)
                {
                    try_password_hash = try_password;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;

            File database_dir = new File(new File(dbs_path).getParent());
            database_dir.mkdirs();

            if (DB_ENCRYPT)
            {
                // builder = builder.provider(new EncryptedDatabase.Provider(try_password_hash));

                Log.i(TAG, "Before net.sqlcipher loadLibs");
                net.sqlcipher.database.SQLiteDatabase.loadLibs(this);
                Log.i(TAG, "After net.sqlcipher loadLibs");

                net.sqlcipher.database.SQLiteDatabase trifa_db = null;
                try
                {
                    trifa_db = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(dbs_path, try_password_hash,
                                                                                          null);

                }
                catch (net.sqlcipher.database.SQLiteException e)
                {
                    migration_warning(true);
                    if (try_migration_to_sqlcipher4(dbs_path, try_password_hash))
                    {
                        try_migration_to_sqlcipher4_vfs(try_password_hash);

                        try
                        {
                            trifa_db = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(dbs_path,
                                                                                                  try_password_hash,
                                                                                                  null);
                        }
                        catch (net.sqlcipher.database.SQLiteException e7)
                        {
                            migration_warning(false);
                            return false;
                        }
                        catch (Exception e2)
                        {
                            migration_warning(false);
                            return false;
                        }
                    }
                    else
                    {
                        migration_warning(false);
                        return false;
                    }

                    migration_warning(false);
                }
                catch (Exception e4)
                {
                    migration_warning(true);
                    if (try_migration_to_sqlcipher4(dbs_path, try_password_hash))
                    {
                        try_migration_to_sqlcipher4_vfs(try_password_hash);
                        try
                        {
                            trifa_db = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(dbs_path,
                                                                                                  try_password_hash,
                                                                                                  null);
                        }
                        catch (net.sqlcipher.database.SQLiteException e7)
                        {
                            migration_warning(false);
                            return false;
                        }
                        catch (Exception e2)
                        {
                            migration_warning(false);
                            return false;
                        }
                    }
                    else
                    {
                        migration_warning(false);
                        return false;
                    }

                    migration_warning(false);
                }

                migration_warning(false);
                if (trifa_db.isOpen())
                {
                    // Log.i(TAG, "db:open=OK:path=" + dbs_path + " trifa_db=" + trifa_db);
                    Log.i(TAG, "db:open=OK");

                    try
                    {
                        net.sqlcipher.Cursor resultSet = trifa_db.rawQuery("PRAGMA cipher_version", null);
                        resultSet.moveToFirst();
                        String cipher_version_ = resultSet.getString(0);
                        resultSet.close();
                        Log.i(TAG, "db:cipher_version_=" + cipher_version_);

                        // remember hash ---------------
                        PREF__DB_secrect_key__user_hash = try_password_hash;
                        // remember hash ---------------

                        ret = true;

                    }
                    catch (Exception e)
                    {

                    }
                    trifa_db.close();
                }
            }
            else
            {
                migration_warning(false);
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "db:open=ERROR:" + e.getMessage());
        }

        migration_warning(false);
        return ret;
    }

    boolean try_migration_to_sqlcipher4(final String old_db_path, final String db_pass)
    {
        try
        {
            Log.i(TAG, "try_migration_to_sqlcipher4");

            net.sqlcipher.database.SQLiteDatabaseHook mHook = new net.sqlcipher.database.SQLiteDatabaseHook()
            {
                public void preKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                    // database.rawExecSQL("PRAGMA kdf_iter=1000;");
                    // database.rawExecSQL("PRAGMA cipher_default_kdf_iter=1000;");
                    // database.rawExecSQL("PRAGMA cipher_page_size=4096;");
                }

                public void postKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                    // database.rawExecSQL("PRAGMA cipher_compatibility=3;");
                    net.sqlcipher.Cursor resultSet = database.rawQuery("PRAGMA cipher_migrate", null);

                    migrationOccurred = false;

                    if (resultSet.getCount() == 1)
                    {
                        resultSet.moveToFirst();
                        String selection = resultSet.getString(0);

                        migrationOccurred = selection.equals("0");
                    }

                    resultSet.close();
                    Log.i(TAG, "migrationOccurred[1]=" + migrationOccurred);
                }
            };

            net.sqlcipher.database.SQLiteDatabase database = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                    old_db_path, db_pass, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE |
                                                net.sqlcipher.database.SQLiteDatabase.CREATE_IF_NECESSARY, mHook);

            Log.i(TAG, "database=" + database);
            Log.i(TAG, "migrationOccurred[2]=" + migrationOccurred);
            database.close();

            if (migrationOccurred)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "try_migration_to_sqlcipher4:EE:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    boolean try_migration_to_sqlcipher4_vfs(final String db_pass)
    {
        try
        {
            Log.i(TAG, "try_migration_to_sqlcipher4_vfs");

            final String old_vfs_path = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;

            net.sqlcipher.database.SQLiteDatabaseHook mHook2 = new net.sqlcipher.database.SQLiteDatabaseHook()
            {
                public void preKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                }

                public void postKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                    database.rawExecSQL("PRAGMA cipher_compatibility=3;");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:22:cipher_compatibility");

                    database.rawExecSQL("PRAGMA cipher_page_size = 8192;");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:22:cipher_page_size");

                    net.sqlcipher.Cursor resultSet = database.rawQuery("select * from sqlite_master;", null);
                    if (resultSet.getCount() == 1)
                    {
                        resultSet.moveToFirst();
                        String selection = resultSet.getString(0);
                        Log.i(TAG, "try_migration_to_sqlcipher4_vfs:res=" + selection);
                    }
                    resultSet.close();

                    String sql_ = "";
                    String new_db_file_001 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + "newfile.db";
                    sql_ = "ATTACH DATABASE '" + new_db_file_001 + "' AS sqlcipher4 KEY '" + db_pass + "';";
                    database.rawExecSQL(sql_);
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:ATTACH DATABASE");
                    database.rawExecSQL("SELECT sqlcipher_export('sqlcipher4');");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:export");
                    database.rawExecSQL("DETACH DATABASE sqlcipher4;");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:DETACH DATABASE");
                }
            };

            net.sqlcipher.database.SQLiteDatabase database2 = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                    old_vfs_path, db_pass, null,
                    net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY, mHook2);

            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:A=" + database2);

            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:A=" + database2.isWriteAheadLoggingEnabled());
            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:A=" + database2.getPath());
            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:A=" + database2.getPageSize());
            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:A=" + database2.isDatabaseIntegrityOk());

            database2.rawExecSQL("select count(*) from meta_data;");
            database2.close();

            net.sqlcipher.database.SQLiteDatabaseHook mHook3 = new net.sqlcipher.database.SQLiteDatabaseHook()
            {
                public void preKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                }

                public void postKey(net.sqlcipher.database.SQLiteDatabase database)
                {
                    String new_db_file_001 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + "newpagesize.db";
                    database.rawExecSQL("ATTACH DATABASE '" + new_db_file_001 + "' AS sq4nps KEY '" + db_pass + "';");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:B:ATTACH DATABASE");
                    database.rawExecSQL("PRAGMA sq4nps.cipher_page_size = 8192;");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:B:PRAGMA cipher_page_size");
                    database.rawExecSQL("SELECT sqlcipher_export('sq4nps');");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:B:export");
                    database.rawExecSQL("DETACH DATABASE sq4nps;");
                    Log.i(TAG, "try_migration_to_sqlcipher4_vfs:B:DETACH DATABASE");
                }
            };

            String new_db_file_1 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + "newfile.db";

            net.sqlcipher.database.SQLiteDatabase database3 = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                    new_db_file_1, db_pass, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE |
                                                  net.sqlcipher.database.SQLiteDatabase.CREATE_IF_NECESSARY, mHook3);

            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:database:B=" + database3);

            database3.close();

            String encfs_path = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;
            File encfs_dbs = new File(encfs_path);
            encfs_dbs.delete();
            String encfs_path2 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME + "-shm";
            File encfs_dbs2 = new File(encfs_path2);
            encfs_dbs2.delete();
            String encfs_path3 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME + "-wal";
            File encfs_dbs3 = new File(encfs_path3);
            encfs_dbs3.delete();
            // --------
            String encfs_path_2 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + "newfile.db";
            File encfs_dbs_2 = new File(encfs_path_2);
            encfs_dbs_2.delete();
            // --------
            String encfs_path_3 = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + "newpagesize.db";
            File encfs_dbs_3 = new File(encfs_path_3);
            File encfs_dbs_dest = new File(encfs_path);
            encfs_dbs_3.renameTo(encfs_dbs_dest);

            return true;
        }
        catch (Exception e)
        {
            Log.i(TAG, "try_migration_to_sqlcipher4_vfs:EE:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();
        // do nothing!!
    }
}

