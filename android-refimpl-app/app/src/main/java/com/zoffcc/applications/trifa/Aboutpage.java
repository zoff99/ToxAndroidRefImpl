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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zoffcc.applications.logging.Logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

import static com.zoffcc.applications.trifa.MainActivity.dp2px;
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
                    addWebsite("https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/dev003/README.md");

            mehdi.sakout.aboutpage.Element e001 = new mehdi.sakout.aboutpage.Element();
            e001.setTitle("send Crash report via Email");
            e001.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    try
                    {
                        progressDialog2 = ProgressDialog.show(Aboutpage.this, "", "reading crash info ...");

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
            aboutPage.setDescription("TRIfa a Tox Client for Android\nVersion: " + MainActivity.versionName);

            Element tox_link = new Element();
            tox_link.setTitle("What is Tox?");
            Intent tox_faq_page = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tox.zoff.cc"));
            tox_link.setIntent(tox_faq_page);
            aboutPage.addItem(tox_link);

            //  --------------------------------
            Element el2 = null;
            Intent link2 = null;
            //  --------------------------------
            //  --------------------------------
            //  --------------------------------
            //  --------- used libs ------------
            el2 = new Element();
            el2.setTitle("used libraries:");
            el2.setIconDrawable(R.drawable.about_icon_github);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.gfx.android.orma");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gfx/Android-Orma"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("info.guardianproject.iocipher:IOCipher");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/guardianproject/IOCipher"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.l4digital.fastscroll:fastscroll");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/L4Digital/FastScroll"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.bumptech.glide");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bumptech/glide"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("info.guardianproject.netcipher");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/guardianproject/NetCipher"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.mikepenz:fontawesome-typeface");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mikepenz/Android-Iconics"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.mikepenz:google-material-typeface");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mikepenz/Android-Iconics"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.google.zxing:core");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zxing/zxing"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.hotchemi:permissionsdispatcher");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/permissions-dispatcher/PermissionsDispatcher"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.angads25:filepicker");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Angads25/android-filepicker"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.vanniktech:emoji-ios");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vanniktech/Emoji"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.google.code.gson");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/google/gson"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.medyo:android-about-page");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/medyo/android-about-page"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("de.hdodenhof:circleimageview");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hdodenhof/CircleImageView"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.armcha:AutoLinkTextView");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/armcha/AutoLinkTextView"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.github.chrisbanes:PhotoView");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chrisbanes/PhotoView"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.squareup.okhttp3");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/square/okhttp"));
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle("com.daimajia.numberprogressbar");
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/daimajia/NumberProgressBar"));
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
            Log.i(TAG, "onCreate:bm1.getWidth()=" + bm1.getWidth() + " bm1.getHeight()=" + bm1.getHeight());
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
        String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") + "LastStackTrace:" + System.getProperty("line.separator") + MainApplication.last_stack_trace_as_string;
        MainApplication.last_stack_trace_as_string = ""; // reset last stacktrace

        // String DATA_DEBUG_DIR = new File(getExternalFilesDir(null).getAbsolutePath() + "/crashes").toString();
        String DATA_DEBUG_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/trifa/crashes").toString();

        String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
        String full_file_name = DATA_DEBUG_DIR + "/crash_" + date + ".txt";
        String full_file_name_suppl = DATA_DEBUG_DIR + "/crash_single.txt";
        String feedback_text = "If there is no file attached, please attach:\n" + full_file_name + "\nto this email.";

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

        main_activity_s.sendEmailWithAttachment(this, "feedback@zanavi.cc", "TRIfA Crashlog (a:" + android.os.Build.VERSION.SDK + ")", feedback_text, full_file_name, full_file_name_suppl);
    }
}
