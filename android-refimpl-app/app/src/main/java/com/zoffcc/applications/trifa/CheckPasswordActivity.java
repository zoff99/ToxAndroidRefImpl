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

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.encryption.EncryptedDatabase;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static com.zoffcc.applications.trifa.MainActivity.DB_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_DB_NAME;
import static com.zoffcc.applications.trifa.MainActivity.ORMA_TRACE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF__DB_secrect_key__user_hash;

public class CheckPasswordActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CheckPasswordActy";

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPasswordView1;
    private View mProgressView;
    private View mLoginFormView;
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

        if (!TextUtils.isEmpty(DB_secrect_key__tmp))
        {
            mPasswordView1.setVisibility(View.INVISIBLE);
            mLoginFormView.setVisibility(View.INVISIBLE);

            auto_generated_password = true;
            Log.i(TAG, "auto_generated_password:true");
            attemptUnlock(true, DB_secrect_key__tmp);
        }
        else
        {
            mPasswordView1.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.VISIBLE);
        }
    }


    private void attemptUnlock(boolean auto_generated_pass, String pass)
    {
        Log.i(TAG, "attemptUnlock:auto_generated_pass=" + auto_generated_pass);

        if (mAuthTask != null)
        {
            return;
        }

        try
        {
            mPasswordView1.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.GONE);
        }
        catch (Exception e)
        {
        }

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
                showProgress(true, auto_generated_pass);
                mAuthTask = new UserLoginTask(pass);
                mAuthTask.execute((Boolean) true);
            }
        }

        boolean cancel = false;
        View focusView = null;

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

        if (!cancel)
        {
            if (TextUtils.isEmpty(password1))
            {
                mPasswordView1.setError("Enter Password");
                focusView = mPasswordView1;
                cancel = true;
            }

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password1) && !SetPasswordActivity.isPasswordValid(password1))
            {
                mPasswordView1.setError("Invalid Password");
                focusView = mPasswordView1;
                cancel = true;
            }
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            try
            {
                focusView.requestFocus();
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true, auto_generated_pass);
            mAuthTask = new UserLoginTask(password1);
            mAuthTask.execute((Boolean) false);
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
            mAuthTask = null;
            showProgress(false, auto_gen_pass_flag);

            if (success)
            {
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
                    return false;
                }
                catch (Exception e2)
                {
                    return false;
                }

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
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "db:open=ERROR:" + e.getMessage());
        }

        return ret;
    }

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();
        // do nothing!!
    }
}

