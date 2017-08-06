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

import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.util.Random;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.zoffcc.applications.trifa.MainActivity.clipboard;
import static com.zoffcc.applications.trifa.MainActivity.copy_real_file_to_vfs_file;
import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_own_avatar;
import static com.zoffcc.applications.trifa.MainActivity.put_vfs_image_on_imageview;
import static com.zoffcc.applications.trifa.MainActivity.set_g_opts;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.update_savedata_file;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_OWN_AVATAR_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.ToxVars.OX_MAX_STATUS_MESSAGE_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NAME_LENGTH;

public class ProfileActivity extends AppCompatActivity
{
    static final String TAG = "trifa.ProfileActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    ImageView mytoxid_imageview = null;
    TextView mytoxid_textview = null;
    EditText mynick_edittext = null;
    EditText mystatus_message_edittext = null;
    Button new_nospam_button = null;
    Button copy_toxid_button = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_icon = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.profile_icon);
        mytoxid_imageview = (ImageView) findViewById(R.id.mytoxid_imageview);
        mytoxid_textview = (TextView) findViewById(R.id.mytoxid_textview);
        mynick_edittext = (EditText) findViewById(R.id.mynick_edittext);
        mystatus_message_edittext = (EditText) findViewById(R.id.mystatus_message_edittext);

        new_nospam_button = (Button) findViewById(R.id.new_nospam_button);
        copy_toxid_button = (Button) findViewById(R.id.copy_toxid_button);

        new_nospam_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    // Log.i(TAG, "old ToxID=" + MainActivity.get_my_toxid());
                    // Log.i(TAG, "old NOSPAM=" + MainActivity.tox_self_get_nospam());
                    Random random = new Random();
                    long new_nospam = (long) random.nextInt() + (1L << 31);
                    // Log.i(TAG, "generated NOSPAM=" + new_nospam);
                    MainActivity.tox_self_set_nospam(new_nospam);
                    // Log.i(TAG, "new ToxID=" + MainActivity.get_my_toxid());
                    // Log.i(TAG, "new NOSPAM=" + MainActivity.tox_self_get_nospam());
                    Toast.makeText(v.getContext(), "generated new Random NOSPAM value", Toast.LENGTH_SHORT).show();

                    // ---- change display to the new ToxID ----
                    update_toxid_display();
                    // ---- change display to the new ToxID ----
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        copy_toxid_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", "tox:" + MainActivity.get_my_toxid()));
                    Toast.makeText(v.getContext(), "ToxID copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
        profile_icon.setImageDrawable(d1);

        mytoxid_textview.setText("");
        mynick_edittext.setText(global_my_name);
        mystatus_message_edittext.setText(global_my_status_message);

        update_toxid_display();

        profile_icon.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    // select new avatar image
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.FILE_SELECT;
                    properties.root = new java.io.File("/");
                    properties.error_dir = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    properties.offset = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    // TODO: hardcoded is always bad
                    properties.extensions = new String[]{"jpg", "jpeg", "png", "gif", "JPG", "PNG", "GIF"};
                    FilePickerDialog dialog = new FilePickerDialog(ProfileActivity.this, properties);
                    dialog.setTitle("Select Avatar");

                    dialog.setDialogSelectionListener(new DialogSelectionListener()
                    {
                        @Override
                        public void onSelectedFilePaths(String[] files)
                        {
                            try
                            {
                                Log.i(TAG, "select_avatar:" + files);
                                String src_path = new File(new File(files[0]).getAbsolutePath()).getParent();
                                String src_filename = new File(files[0]).getName();
                                Log.i(TAG, "select_avatar:p=" + src_path + " f=" + src_filename);
                                copy_real_file_to_vfs_file(src_path, src_filename, VFS_PREFIX + VFS_OWN_AVATAR_DIR, "avatar.png");
                                set_g_opts("VFS_OWN_AVATAR_FNAME", VFS_PREFIX + VFS_OWN_AVATAR_DIR + "/" + "avatar.png");

                                put_vfs_image_on_imageview(ProfileActivity.this, profile_icon, d1, VFS_PREFIX + VFS_OWN_AVATAR_DIR + "/" + "avatar.png");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "select_avatar:EE1:" + e.getMessage());
                            }
                        }
                    });

                    dialog.show();
                }
                else
                {
                }
                return true;
            }
        });

        try
        {
            String fname = get_vfs_image_filename_own_avatar();
            if (fname != null)
            {
                put_vfs_image_on_imageview(this, profile_icon, d1, fname);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void update_toxid_display()
    {
        try
        {
            mytoxid_imageview.setImageBitmap(encodeAsBitmap("tox:" + MainActivity.get_my_toxid()));
            mytoxid_textview.setText(MainActivity.get_my_toxid());
        }
        catch (WriterException e)
        {
            e.printStackTrace();

            try
            {
                mytoxid_imageview.setImageBitmap(encodeAsBitmap("123")); // in case something goes wrong
                mytoxid_textview.setText(MainActivity.get_my_toxid());
            }
            catch (WriterException e2)
            {
                e2.printStackTrace();
            }

        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // TODO dirty hack, just write "name" and message all the time, and send to "tox core"

        try
        {
            global_my_name = mynick_edittext.getText().toString().substring(0, Math.min(mynick_edittext.getText().toString().length(), TOX_MAX_NAME_LENGTH));
            global_my_status_message = mystatus_message_edittext.getText().toString().substring(0, Math.min(mystatus_message_edittext.getText().toString().length(), OX_MAX_STATUS_MESSAGE_LENGTH));
            tox_self_set_name(global_my_name);
            tox_self_set_status_message(global_my_status_message);
            update_savedata_file();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException
    {
        BitMatrix result;
        try
        {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 200, 200, null);
        }
        catch (IllegalArgumentException iae)
        {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++)
        {
            int offset = y * w;
            for (int x = 0; x < w; x++)
            {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 200, 0, 0, w, h);
        return bitmap;
    }
}
