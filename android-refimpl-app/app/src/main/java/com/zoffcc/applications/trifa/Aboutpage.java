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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zoffcc.applications.logging.Logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.main_activity_s;

public class Aboutpage extends AppCompatActivity implements Logging.AsyncResponse
{
    private static final String TAG = "trifa.Aboutpage";
    ProgressDialog progressDialog2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            AboutPage aboutPage = new AboutPage(this).
                    isRTL(false).
                    setImage(R.drawable.web_hi_res_512).
                    addWebsite(getString(R.string.Aboutpage_1));

            aboutPage.setDescription(getString(R.string.Aboutpage_5a) + "\n" + getString(R.string.Aboutpage_5b) + " " +
                                     MainActivity.versionName + "\n\n" + "TRIfA commit hash:" + BuildConfig.GitHash +
                                     "\n" + "JNI commit hash:" + MainActivity.getNativeLibGITHASH() + "\n" +
                                     "c-toxcore commit hash:" + MainActivity.getNativeLibTOXGITHASH());

            Element tox_link = new Element();
            tox_link.setTitle(getString(R.string.Aboutpage_6));
            Intent tox_faq_page = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tox.zoff.cc"));
            tox_link.setIntent(tox_faq_page);
            aboutPage.addItem(tox_link);

            mehdi.sakout.aboutpage.Element e001 = new mehdi.sakout.aboutpage.Element();
            e001.setTitle(getString(R.string.Aboutpage_2));
            e001.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    try
                    {
                        progressDialog2 = ProgressDialog.show(Aboutpage.this, "", getString(
                                R.string.Aboutpage_4));

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
                        Logging.delegate = Aboutpage.this;
                        x.new PopulateLogcatAsyncTask(Aboutpage.this.getApplicationContext()).execute();
                        // get logcat messages ----------------

                    }
                    catch (Exception e)
                    {
                    }
                }
            });
            aboutPage.addItem(e001);

            Element trifa_commit = new Element();
            trifa_commit.setTitle("TRIfA commit hash link");
            Intent trifa_commit_page = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/zoff99/ToxAndroidRefImpl/commit/" + BuildConfig.GitHash));
            trifa_commit.setIntent(trifa_commit_page);
            aboutPage.addItem(trifa_commit);

            Element jni_commit = new Element();
            jni_commit.setTitle("JNI commit hash link");
            Intent jni_commit_page = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/zoff99/ToxAndroidRefImpl/commit/" + MainActivity.getNativeLibGITHASH()));
            jni_commit.setIntent(jni_commit_page);
            aboutPage.addItem(jni_commit);

            Element ct_commit = new Element();
            ct_commit.setTitle("c-toxcore commit hash link");
            Intent ct_commit_page = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/zoff99/c-toxcore/commit/" + MainActivity.getNativeLibTOXGITHASH()));
            ct_commit.setIntent(ct_commit_page);
            aboutPage.addItem(ct_commit);

            //  --------------------------------
            Element el2 = null;
            Intent link2 = null;
            //  --------------------------------
            //  --------------------------------
            //  --------------------------------
            //  --------- used libs ------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_8));
            el2.setIconDrawable(R.drawable.about_icon_github);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_9));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_10)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_11));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_12)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_13));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_14)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_15));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_16)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_17));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_18)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_19));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_20)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_21));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_22)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_23));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_24)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_25));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_26)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_27));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_28)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_29));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_30)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_31));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_32)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_33));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_34)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_35));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_36)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_37));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_38)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_39));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_40)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_41));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_42)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_43));
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_44)));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------- used libs ------------

            setContentView(aboutPage.create());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE1:" + e.getMessage());
        }

        try
        {
            // find the large top icon in aboutpage layout
            ImageView icon_big = (ImageView) findViewById(R.id.image);
            Log.i(TAG, "onCreate:icon_big=" + icon_big);

            final Bitmap bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.web_hi_res_512);
            Log.i(TAG, "onCreate:bm1.getWidth()=" + bm1.getWidth() + " bm1.getHeight()=" +
                       bm1.getHeight());
            final Bitmap bm1_scaled = Bitmap.createScaledBitmap(bm1, (int) dp2px(200), (int) dp2px(200), true);
            Log.i(TAG, "onCreate:dp2px(200)=" + dp2px(200));

            icon_big.setImageBitmap(bm1_scaled);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE2:" + e.getMessage());
        }
    }


    @Override
    public void processFinish(String output_part1)
    {
        String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") +
                        "LastStackTrace:" + System.getProperty("line.separator") +
                        MainApplication.last_stack_trace_as_string;
        MainApplication.last_stack_trace_as_string = ""; // reset last stacktrace

        // String DATA_DEBUG_DIR = new File(getExternalFilesDir(null).getAbsolutePath() + "/crashes").toString();
        String DATA_DEBUG_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                         "/trifa/crashes").toString();

        String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
        String full_file_name = DATA_DEBUG_DIR + "/crash_" + date + ".txt";
        String full_file_name_suppl = DATA_DEBUG_DIR + "/crash_single.txt";
        String feedback_text = "If there is no file attached, please attach:\n" + full_file_name +
                               "\nto this email.";

        Logging.writeToFile(output, Aboutpage.this, full_file_name);

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

        main_activity_s.sendEmailWithAttachment(this, "feedback@zanavi.cc", getString(R.string.Aboutpage_0) + " (a:" +
                                                                            android.os.Build.VERSION.SDK + ")",
                                                feedback_text, full_file_name,
                                                full_file_name_suppl);
    }
}
