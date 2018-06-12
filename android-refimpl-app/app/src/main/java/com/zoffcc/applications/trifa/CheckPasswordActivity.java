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
import android.support.v7.app.AppCompatActivity;
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

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_password);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        mPasswordView1 = (EditText) findViewById(R.id.password_1);
        mPasswordView1.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.set_button || id == EditorInfo.IME_NULL)
                {
                    attemptUnlock();
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
                attemptUnlock();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void attemptUnlock()
    {
        Log.i(TAG, "attemptUnlock");

        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mPasswordView1.setError(null);

        String password1 = mPasswordView1.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password1))
        {
            mPasswordView1.setError("Enter Password");
            focusView = mPasswordView1;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password1) && !isPasswordValid(password1))
        {
            mPasswordView1.setError("Invalid Password");
            focusView = mPasswordView1;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(password1);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 7;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mPassword1;

        UserLoginTask(String password1)
        {
            mPassword1 = password1;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            boolean pass_is_correct = check_password(mPassword1);

            if (!pass_is_correct)
            {
                // password seems not correct
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                // ok open main activity
                Intent main_act = new Intent(CheckPasswordActivity.this, MainActivity.class);
                startActivity(main_act);
                finish();
            }
            else
            {
                mPasswordView1.setError("* Error *");
                mPasswordView1.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }

    boolean check_password(String try_password)
    {
        boolean ret = false;

        try
        {
            String try_password_hash = TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(try_password)));

            String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;
            Log.i(TAG, "db:path=" + dbs_path);

            File database_dir = new File(new File(dbs_path).getParent());
            database_dir.mkdirs();

            OrmaDatabase.Builder builder = OrmaDatabase.builder(this);
            if (DB_ENCRYPT)
            {
                builder = builder.provider(new EncryptedDatabase.Provider(try_password_hash));
            }
            OrmaDatabase orma2 = builder.name(dbs_path).
                    readOnMainThread(AccessThreadConstraint.NONE).
                    writeOnMainThread(AccessThreadConstraint.NONE).
                    trace(ORMA_TRACE).
                    build();
            Log.i(TAG, "db:open=OK:path=" + dbs_path);

            try
            {
                orma2.getConnection().close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // remember hash ---------------
            PREF__DB_secrect_key__user_hash = try_password_hash;
            // remember hash ---------------

            ret = true;
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

