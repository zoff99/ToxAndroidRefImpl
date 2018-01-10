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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zoffcc.applications.logging.Logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CrashActivity extends AppCompatActivity implements Logging.AsyncResponse
{
    static final String TAG = "trifa.CrashActy";

    ImageView bug_button = null;
    ImageButton send_report_button_01 = null;
    Button send_report_button_02 = null;
    View CrashView = null;
    ProgressDialog progressDialog2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);


        CrashView = (View) this.findViewById(R.id.CrashView);
        CrashView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View arg0, MotionEvent e)
            {
                try
                {
                    int action = e.getAction();
                    if (action == MotionEvent.ACTION_DOWN)
                    {
                        CrashView.setBackgroundColor(Color.parseColor("#ee0000"));
                        return true;
                    }
                    else
                    {
                        if (action == MotionEvent.ACTION_UP)
                        {
                            CrashView.setBackgroundColor(Color.parseColor("#FF9900"));
                            restart_app();
                            finish();
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return false;
            }

        });


        send_report_button_01 = (ImageButton) this.findViewById(R.id.send_report_button_01);
        send_report_button_01.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                try
                {
                    int action = e.getAction();
                    if (action == MotionEvent.ACTION_DOWN)
                    {
                        // send_report_button_01.setLeft(bug_button.getLeft() + 10);
                        // send_report_button_01.setTop(bug_button.getTop() + 10);
                        return false;
                    }
                    else
                    {
                        if (action == MotionEvent.ACTION_UP)
                        {
                            // send_report_button_01.setLeft(bug_button.getLeft() - 10);
                            // send_report_button_01.setTop(bug_button.getTop() - 10);
                            // -------- send report -------- //
                            try
                            {
                                progressDialog2 = ProgressDialog.show(CrashActivity.this, "", "reading crash info ...");

                                progressDialog2.setCanceledOnTouchOutside(false);
                                progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                                {
                                    @Override
                                    public void onCancel(DialogInterface dialog)
                                    {
                                    }
                                });

                                // get logcat messages ----------------
                                Logging x = new Logging();
                                Logging.delegate = CrashActivity.this;
                                x.new PopulateLogcatAsyncTask(CrashActivity.this.getApplicationContext()).execute();
                                // get logcat messages ----------------

                            }
                            catch (Exception e_rep)
                            {
                                e_rep.printStackTrace();
                            }
                            // -------- send report -------- //
                            return false;
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return false;
            }

        });


        send_report_button_02 = (Button) this.findViewById(R.id.send_report_button_02);
        send_report_button_02.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                try
                {
                    int action = e.getAction();
                    if (action == MotionEvent.ACTION_DOWN)
                    {
                        // send_report_button_02.setLeft(bug_button.getLeft() + 10);
                        // send_report_button_02.setTop(bug_button.getTop() + 10);
                        return false;
                    }
                    else
                    {
                        if (action == MotionEvent.ACTION_UP)
                        {
                            // send_report_button_02.setLeft(bug_button.getLeft() - 10);
                            // send_report_button_02.setTop(bug_button.getTop() - 10);
                            // -------- send report -------- //
                            try
                            {
                                progressDialog2 = ProgressDialog.show(CrashActivity.this, "", "reading crash info ...");

                                progressDialog2.setCanceledOnTouchOutside(false);
                                progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                                {
                                    @Override
                                    public void onCancel(DialogInterface dialog)
                                    {
                                    }
                                });

                                // get logcat messages ----------------
                                Logging x = new Logging();
                                Logging.delegate = CrashActivity.this;
                                x.new PopulateLogcatAsyncTask(CrashActivity.this.getApplicationContext()).execute();
                                // get logcat messages ----------------

                            }
                            catch (Exception e_rep)
                            {
                                e_rep.printStackTrace();
                            }
                            // -------- send report -------- //
                            return false;
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return false;
            }

        });

        // ------- not working here -------
        send_report_button_01.setVisibility(View.GONE);
        send_report_button_02.setVisibility(View.GONE);
        // ------- not working here -------

        bug_button = (ImageView) this.findViewById(R.id.bugButton);
        bug_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View arg0, MotionEvent e)
            {
                try
                {
                    int action = e.getAction();
                    if (action == MotionEvent.ACTION_DOWN)
                    {
                        bug_button.setLeft(bug_button.getLeft() + 10);
                        bug_button.setTop(bug_button.getTop() + 10);
                        return true;
                    }
                    else
                    {
                        if (action == MotionEvent.ACTION_UP)
                        {
                            bug_button.setLeft(bug_button.getLeft() - 10);
                            bug_button.setTop(bug_button.getTop() - 10);
                            restart_app();
                            finish();
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return false;
            }

        });
    }

    public void restart_app()
    {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getApplicationContext(), com.zoffcc.applications.trifa.StartMainActivityWrapper.class), PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 300, intent); // restart app after n seconds delay
    }

    @Override
    public void processFinish(String output_part1)
    {
        String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") + "LastStackTrace:" + System.getProperty("line.separator") + MainApplication.last_stack_trace_as_string;
        MainApplication.last_stack_trace_as_string = ""; // reset last stacktrace

        // String DATA_DEBUG_DIR = new File(getExternalFilesDir(null).getAbsolutePath() + "/crashes").toString();
        String DATA_DEBUG_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/trifa/crashes").toString();

        Log.i(TAG, "processFinish:DATA_DEBUG_DIR=" + DATA_DEBUG_DIR);

        String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
        String full_file_name = DATA_DEBUG_DIR + "/crash_" + date + ".txt";
        String full_file_name_suppl = DATA_DEBUG_DIR + "/crash_single.txt";
        String feedback_text = "If there is no file attached, please attach:\n" + full_file_name + "\nto this email.";

        Logging.writeToFile(output, CrashActivity.this, full_file_name);

        try
        {
            new Handler().post(new Runnable()
            {
                @Override
                public void run()
                {
                    progressDialog2.dismiss();
                }
            });
        }
        catch (Exception ee)
        {
        }

        try
        {
            Log.i(TAG, "processFinish:MainActivity.main_activity_s=" + MainActivity.main_activity_s);
            MainActivity.main_activity_s.sendEmailWithAttachment(this, "feedback@zanavi.cc", "TRIfA Crashlog (a:" + android.os.Build.VERSION.SDK + ")", feedback_text, full_file_name, full_file_name_suppl);
        }
        catch (Exception ee3)
        {
        }

    }
}