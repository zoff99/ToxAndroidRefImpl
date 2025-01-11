/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.HelperGeneric.display_toast;
import static com.zoffcc.applications.trifa.HelperGeneric.io_file_copy;
import static com.zoffcc.applications.trifa.MainActivity.DB_SHM_EXT;
import static com.zoffcc.applications.trifa.MainActivity.DB_WAL_EXT;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_DB_NAME;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_VFS_NAME;
import static com.zoffcc.applications.trifa.MainActivity.PREF__DB_secrect_key;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FULL_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.export_savedata_file_unsecure;
import static com.zoffcc.applications.trifa.MainActivity.manually_log_out;

public class ExportActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // prevent screenshots and also dont show the window content in recent activity screen
        initializeScreenshotSecurity(this);

        TextView text_toxpass = findViewById(R.id.text_toxpass);
        TextView text_dbpass = findViewById(R.id.text_dbpass);

        Intent intent = getIntent();
        final String act = intent.getStringExtra("act");

        if (act.equals("MaintenanceActivity"))
        {
            text_toxpass.setText(TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2(PREF__DB_secrect_key))));
            text_dbpass.setText(PREF__DB_secrect_key);
        }
        else
        {
            text_toxpass.setText("");
            text_dbpass.setText("");
        }

        Button export_full_as_zip = findViewById(R.id.export_full_as_zip);

        export_full_as_zip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Export All Encrypted Files");
                    builder.setMessage(
                            "Your Encrypted files will be exported to:" + "\n\n" +
                            MainActivity.SD_CARD_FILES_EXPORT_DIR + SD_CARD_FULL_FILES_EXPORT_DIR);

                    builder.setPositiveButton("Yes, I want to export", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            export_all_files(v.getContext());
                        }
                    });
                    builder.setNegativeButton("Cancel", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void export_all_files(final Context context)
    {
        try
        {
            new export_all_files_async_task(context).execute();
        }
        catch (Exception e)
        {
        }
    }

    private static class export_all_files_async_task extends AsyncTask<Void, Void, Boolean>
    {
        private ProgressDialog dialog;
        private final Context c;

        public export_all_files_async_task(Context c)
        {
            this.c = c;
            dialog = new ProgressDialog(c);
        }

        @Override
        protected void onPreExecute()
        {
            manually_log_out();

            dialog.setMessage("exporting ...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... args)
        {
            try
            {
                String export_dir_string = c.getExternalFilesDir(null).getAbsolutePath() + SD_CARD_FULL_FILES_EXPORT_DIR;

                // make directory
                File export_dir = new File(export_dir_string);
                export_dir.mkdirs();

                // first export the tox save file unencrypted
                final String export_tox_file = export_dir_string + "/" + "unsecure_export_savedata.tox";
                System.out.println("XXXXXXXXX:" + export_tox_file);
                export_savedata_file_unsecure("_", export_tox_file);

                // now export all other files
                final ArrayList<String> files_to_export = new ArrayList<>();
                final ArrayList<String> files_to_export_optional = new ArrayList<>();
                files_to_export.add(c.getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME);
                files_to_export_optional.add(c.getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME + DB_SHM_EXT);
                files_to_export_optional.add(c.getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME + DB_WAL_EXT);
                //
                files_to_export.add(c.getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME);
                files_to_export_optional.add(c.getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME + DB_SHM_EXT);
                files_to_export_optional.add(c.getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME + DB_WAL_EXT);

                for (String export_file : files_to_export)
                {
                    System.out.println("XXXXXXXXX:" + export_file + " -> " + export_dir_string + "/" + new File(export_file).getName());
                    final String dst_file = export_dir_string + "/" + new File(export_file).getName();
                    io_file_copy(new File(export_file), new File(dst_file));
                }

                for (String export_file : files_to_export_optional)
                {
                    try
                    {
                        System.out.println("XXXXXXXXX:optinal:" + export_file + " -> " + export_dir_string + "/" + new File(export_file).getName());
                        final String dst_file = export_dir_string + "/" + new File(export_file).getName();
                        io_file_copy(new File(export_file), new File(dst_file));
                    }
                    catch(Exception e)
                    {
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                display_toast("!! ERROR on export !!", true, 0);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (dialog.isShowing())
            {
                dialog.dismiss();
            }

            if (result)
            {
                display_toast("export ready", true, 0);
            }
        }
    }
}