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
    private static final String TAG = "trifa.Aboutpage"; //$NON-NLS-1$
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
                    addWebsite(getString(R.string.Aboutpage_1)); //$NON-NLS-1$

            mehdi.sakout.aboutpage.Element e001 = new mehdi.sakout.aboutpage.Element();
            e001.setTitle(getString(R.string.Aboutpage_2)); //$NON-NLS-1$
            e001.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    try
                    {
                        progressDialog2 = ProgressDialog.show(Aboutpage.this, "", getString(
                                R.string.Aboutpage_4)); //$NON-NLS-1$ //$NON-NLS-2$

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
            aboutPage.setDescription(getString(R.string.Aboutpage_5a) + "\n" + getString(R.string.Aboutpage_5b) + " " +
                                     MainActivity.versionName + "\n\n" + "TRIfA commit hash:" + BuildConfig.GitHash +
                                     "\n" + "JNI commit hash:" + MainActivity.getNativeLibGITHASH() + "\n" +
                                     "c-toxcore commit hash:" + MainActivity.getNativeLibTOXGITHASH());

            Element tox_link = new Element();
            tox_link.setTitle(getString(R.string.Aboutpage_6)); //$NON-NLS-1$
            Intent tox_faq_page = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tox.zoff.cc")); //$NON-NLS-1$
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
            el2.setTitle(getString(R.string.Aboutpage_8)); //$NON-NLS-1$
            el2.setIconDrawable(R.drawable.about_icon_github);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_9)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_10))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_11)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_12))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_13)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_14))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_15)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_16))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_17)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_18))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_19)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_20))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_21)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_22))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_23)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_24))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_25)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_26))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_27)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_28))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_29)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_30))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_31)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_32))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_33)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_34))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_35)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_36))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_37)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_38))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_39)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_40))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_41)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_42))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------------------------------
            el2 = new Element();
            el2.setTitle(getString(R.string.Aboutpage_43)); //$NON-NLS-1$
            link2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Aboutpage_44))); //$NON-NLS-1$
            el2.setIntent(link2);
            aboutPage.addItem(el2);
            //  --------------------------------
            //  --------- used libs ------------

            setContentView(aboutPage.create());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE1:" + e.getMessage()); //$NON-NLS-1$
        }

        try
        {
            // find the large top icon in aboutpage layout
            ImageView icon_big = (ImageView) findViewById(R.id.image);
            Log.i(TAG, "onCreate:icon_big=" + icon_big); //$NON-NLS-1$

            final Bitmap bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.web_hi_res_512);
            Log.i(TAG, "onCreate:bm1.getWidth()=" + bm1.getWidth() + " bm1.getHeight()=" +
                       bm1.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$
            final Bitmap bm1_scaled = Bitmap.createScaledBitmap(bm1, (int) dp2px(200), (int) dp2px(200), true);
            Log.i(TAG, "onCreate:dp2px(200)=" + dp2px(200)); //$NON-NLS-1$

            icon_big.setImageBitmap(bm1_scaled);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE2:" + e.getMessage()); //$NON-NLS-1$
        }
    }


    @Override
    public void processFinish(String output_part1)
    {
        String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") +
                        "LastStackTrace:" + System.getProperty("line.separator") +
                        MainApplication.last_stack_trace_as_string; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        MainApplication.last_stack_trace_as_string = ""; // reset last stacktrace //$NON-NLS-1$

        // String DATA_DEBUG_DIR = new File(getExternalFilesDir(null).getAbsolutePath() + "/crashes").toString();
        String DATA_DEBUG_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                         "/trifa/crashes").toString(); //$NON-NLS-1$

        String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date()); //$NON-NLS-1$
        String full_file_name = DATA_DEBUG_DIR + "/crash_" + date + ".txt"; //$NON-NLS-1$ //$NON-NLS-2$
        String full_file_name_suppl = DATA_DEBUG_DIR + "/crash_single.txt"; //$NON-NLS-1$
        String feedback_text = "If there is no file attached, please attach:\n" + full_file_name +
                               "\nto this email."; //$NON-NLS-1$ //$NON-NLS-2$

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
                                                full_file_name_suppl); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
