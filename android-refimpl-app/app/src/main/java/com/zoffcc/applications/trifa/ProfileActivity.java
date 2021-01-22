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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.zoffcc.applications.trifa.HelperGeneric.copy_real_file_to_vfs_file;
import static com.zoffcc.applications.trifa.HelperGeneric.get_network_connections;
import static com.zoffcc.applications.trifa.HelperGeneric.get_vfs_image_filename_own_avatar;
import static com.zoffcc.applications.trifa.HelperGeneric.put_vfs_image_on_imageview_real;
import static com.zoffcc.applications.trifa.HelperGeneric.send_avatar_to_friend;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.set_new_random_nospam_value;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.HelperRelay.get_own_relay_pubkey;
import static com.zoffcc.applications.trifa.HelperRelay.have_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db;
import static com.zoffcc.applications.trifa.Identicon.IDENTICON_ROWS;
import static com.zoffcc.applications.trifa.MainActivity.clipboard;
import static com.zoffcc.applications.trifa.MainActivity.friend_list_fragment;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.TRIFAGlobals.AVATAR_SELF_MAX_BYTE_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_OWN_AVATAR_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NAME_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_STATUS_MESSAGE_LENGTH;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ProfileActivity extends AppCompatActivity
{
    static final String TAG = "trifa.ProfileActy";
    CircleImageView profile_icon = null;
    FloatingActionButton profile_icon_edit = null;
    ImageView mytoxid_imageview = null;
    TextView mytoxid_textview = null;
    EditText mynick_edittext = null;
    EditText mystatus_message_edittext = null;
    Button new_nospam_button = null;
    Button copy_toxid_button = null;
    Button remove_own_relay_button = null;
    TextView my_relay_toxid_textview = null;
    ImageView my_identicon_imageview = null;
    TextView mytox_network_connections = null;

    static Handler profile_handler_s = null;
    Identicon.Identicon_data id_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_icon = findViewById(R.id.profile_icon);
        profile_icon_edit = findViewById(R.id.profile_icon_edit);
        mytoxid_imageview = findViewById(R.id.mytoxid_imageview);
        mytoxid_textview = findViewById(R.id.mytoxid_textview);
        mynick_edittext = findViewById(R.id.mynick_edittext);
        mystatus_message_edittext = findViewById(R.id.mystatus_message_edittext);
        my_identicon_imageview = findViewById(R.id.my_identicon_imageview);

        new_nospam_button = findViewById(R.id.new_nospam_button);
        remove_own_relay_button = findViewById(R.id.remove_relay_button);
        my_relay_toxid_textview = findViewById(R.id.my_relay_toxid_textview);
        copy_toxid_button = findViewById(R.id.copy_toxid_button);

        mytox_network_connections = findViewById(R.id.mytox_network_connections);
        mytox_network_connections.setText(get_network_connections());

        new_nospam_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    set_new_random_nospam_value();
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

        if (have_own_relay())
        {
            remove_own_relay_button.setText("remove own Relay");
            remove_own_relay_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        remove_own_relay_in_db();

                        // load all friends into data list ---
                        Log.i(TAG, "onMenuItemClick:6");
                        try
                        {
                            if (friend_list_fragment != null)
                            {
                                // reload friendlist
                                friend_list_fragment.add_all_friends_clear(200);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        my_relay_toxid_textview.setVisibility(View.INVISIBLE);
                        remove_own_relay_button.setVisibility(View.INVISIBLE);

                        try
                        {
                            remove_own_relay_button.setVisibility(View.GONE);
                        }
                        catch (Exception e1)
                        {
                        }

                        try
                        {
                            my_relay_toxid_textview.setVisibility(View.GONE);
                        }
                        catch (Exception e1)
                        {
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            my_relay_toxid_textview.setText(get_own_relay_pubkey());
            my_relay_toxid_textview.setVisibility(View.VISIBLE);
        }
        else
        {
            remove_own_relay_button.setText("- no Relay set -");
            remove_own_relay_button.setVisibility(View.INVISIBLE);
            try
            {
                remove_own_relay_button.setVisibility(View.GONE);
            }
            catch (Exception e1)
            {
            }

            my_relay_toxid_textview.setText("--");
            my_relay_toxid_textview.setVisibility(View.INVISIBLE);
            try
            {
                my_relay_toxid_textview.setVisibility(View.GONE);
            }
            catch (Exception e1)
            {
            }
        }

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // don't show keyboard when activity starts
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
        profile_icon.setImageDrawable(d1);

        mytoxid_textview.setText("");
        mynick_edittext.setText(global_my_name);
        mystatus_message_edittext.setText(global_my_status_message);

        update_toxid_display();

        profile_icon_edit.setOnClickListener(v -> {
            // select new avatar image
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.root = new File("/");
            properties.error_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            properties.offset = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
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

                        File f_real = new File(src_path + "/" + src_filename);
                        if (f_real.canRead())
                        {
                            if (f_real.length() <= AVATAR_SELF_MAX_BYTE_SIZE)
                            {
                                if (f_real.length() > 0)
                                {

                                    String avatar_file_name_corrected = TrifaSetPatternActivity.filter_out_specials_from_filepath(
                                            src_filename.toLowerCase());

                                    Log.i(TAG, "select_avatar:p=" + src_path + " f=" + avatar_file_name_corrected);
                                    copy_real_file_to_vfs_file(src_path, src_filename, VFS_PREFIX + VFS_OWN_AVATAR_DIR,
                                                               "avatar.png");

                                    String mimeType = URLConnection.guessContentTypeFromName(
                                            avatar_file_name_corrected.toLowerCase());

                                    set_g_opts("VFS_OWN_AVATAR_FNAME",
                                               VFS_PREFIX + VFS_OWN_AVATAR_DIR + "/" + "avatar.png");

                                    //if (mimeType.equalsIgnoreCase("image/gif"))
                                    //{
                                    //    set_g_opts("VFS_OWN_AVATAR_FILE_EXTENSION", ".gif");
                                    //}
                                    //else if (mimeType.equalsIgnoreCase("image/jpeg"))
                                    //{
                                    //    set_g_opts("VFS_OWN_AVATAR_FILE_EXTENSION", ".jpg");
                                    //}
                                    //else
                                    //{
                                    set_g_opts("VFS_OWN_AVATAR_FILE_EXTENSION", ".png");
                                    //}

                                    put_vfs_image_on_imageview_real(ProfileActivity.this, profile_icon, d1,
                                                                    VFS_PREFIX + VFS_OWN_AVATAR_DIR + "/" +
                                                                    "avatar.png", true, false, null);
                                    Log.i(TAG, "select_avatar:put_vfs_image_on_imageview");


                                    List<FriendList> fl = orma.selectFromFriendList().
                                            toList();

                                    if (fl != null)
                                    {
                                        if (fl.size() > 0)
                                        {
                                            int i = 0;
                                            for (i = 0; i < fl.size(); i++)
                                            {
                                                FriendList n = fl.get(i);

                                                // Log.i(TAG, "select_avatar:send_avatar_to_friend:i=" + i);

                                                // iterate over all online friends, and send them our new avatar
                                                if (n.TOX_CONNECTION != TOX_CONNECTION_NONE.value)
                                                {
                                                    Log.i(TAG, "select_avatar:send_avatar_to_friend:online:i=" + i);

                                                    send_avatar_to_friend(
                                                            HelperFriend.tox_friend_by_public_key__wrapper(
                                                                    n.tox_public_key_string));
                                                }
                                            }
                                        }
                                    }


                                }
                            }
                            else
                            {
                                Log.i(TAG, "select_avatar:TOO BIG:" + f_real.length());
                            }
                        }
                        else
                        {
                            Log.i(TAG, "select_avatar:CAN NOT READ:" + src_path + " f=" + src_filename);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "select_avatar:EE1:" + e.getMessage());
                    }
                }
            });

            dialog.show();
        });

        try
        {
            String fname = get_vfs_image_filename_own_avatar();
            if (fname != null)
            {
                put_vfs_image_on_imageview_real(this, profile_icon, d1, fname, true, false, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        profile_handler_s = profile_handler;
    }

    static void update_toxid_display_s()
    {
        try
        {
            android.os.Message msg2 = new android.os.Message();
            Bundle b2 = new Bundle();
            msg2.what = 1;
            msg2.setData(b2);
            profile_handler_s.sendMessage(msg2);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    void update_toxid_display()
    {
        try
        {
            mytoxid_imageview.setImageBitmap(encodeAsBitmap("tox:" + MainActivity.get_my_toxid()));
            // HINT: https://toktok.ltd/spec.html#messenger -> "Tox ID:"
            // 32 	long term public key
            // 4 	nospam
            // 2 	checksum
            String my_tox_id_temp = MainActivity.get_my_toxid();

            if (my_tox_id_temp == null)
            {
                // on error use Echobots ToxID
                // TODO: do something else here
                my_tox_id_temp = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6";
            }

            String my_pk_key_temp = my_tox_id_temp.substring(0, 64);
            String my_nospam_temp = my_tox_id_temp.substring(64, 72);
            String my_chksum_temp = my_tox_id_temp.substring(72, my_tox_id_temp.length());
            String color_pkey = "<font color=\"#331bc5\">";
            String color_nospam = "<font color=\"#990d45\">";
            String color_chksum = "<font color=\"#006600\">";
            String ec = "</font>";
            mytoxid_textview.setText(Html.fromHtml(
                    color_pkey + my_pk_key_temp + ec + color_nospam + my_nospam_temp + ec + color_chksum +
                    my_chksum_temp + ec));
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
        catch (Exception e3)
        {
            e3.printStackTrace();
        }

        try
        {
            id_data = Identicon.create_identicon(
                    MainActivity.get_my_toxid().substring(0, (ToxVars.TOX_PUBLIC_KEY_SIZE * 2))); // Pubkey

            int w = my_identicon_imageview.getWidth();
            int h = my_identicon_imageview.getHeight();

            if ((w == 0) || (h == 0))
            {
                w = 400;
                h = 400;
            }

            // Log.i(TAG, "update_toxid_display:w=" + w);
            // Log.i(TAG, "update_toxid_display:h=" + w);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(bmp);

            Paint p0 = new Paint();
            p0.setColor(id_data.color_a);
            p0.setStyle(Paint.Style.FILL);

            Paint p1 = new Paint();
            p1.setColor(id_data.color_b);
            p1.setStyle(Paint.Style.FILL);

            int x1 = 0;
            int y1 = 0;
            int x2 = 0;
            int y2 = 0;
            int dot_width = w / IDENTICON_ROWS;
            int dot_height = h / IDENTICON_ROWS;
            int columnIdx;

            // Log.i(TAG, "update_toxid_display:dot_width=" + dot_width + " ACTIVE_COLS=" + IDENTICON_ROWS);
            // Log.i(TAG, "update_toxid_display:dot_height=" + dot_height + " IDENTICON_ROWS=" + IDENTICON_ROWS);

            for (int row = 0; row < IDENTICON_ROWS; ++row)
            {
                for (int col = 0; col < IDENTICON_ROWS; ++col)
                {
                    columnIdx = Math.abs((col * 2 - (IDENTICON_ROWS - 1)) / 2);
                    // Log.i(TAG, "update_toxid_display:col=" + col + " columnIdx=" + columnIdx + " row=" + row);

                    x1 = col * dot_width;
                    x2 = (col + 1) * dot_width;
                    y1 = row * dot_height;
                    y2 = (row + 1) * dot_height;

                    // Log.i(TAG, "update_toxid_display:x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2);

                    if (id_data.dot_color[row][columnIdx] == true)
                    {
                        canvas.drawRect(x1, y1, x2, y2, p1);
                    }
                    else
                    {
                        canvas.drawRect(x1, y1, x2, y2, p0);
                    }
                }
            }

            my_identicon_imageview.setImageBitmap(bmp);
            my_identicon_imageview.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_toxid_display:EE:" + e.getMessage());
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // TODO dirty hack, just write "name" and message all the time, and send to "tox core"

        try
        {
            global_my_name = mynick_edittext.getText().toString().substring(0, Math.min(
                    mynick_edittext.getText().toString().length(), TOX_MAX_NAME_LENGTH));
            global_my_status_message = mystatus_message_edittext.getText().toString().substring(0, Math.min(
                    mystatus_message_edittext.getText().toString().length(), TOX_MAX_STATUS_MESSAGE_LENGTH));
            tox_self_set_name(global_my_name);
            tox_self_set_status_message(global_my_status_message);
            update_savedata_file_wrapper(); // after exiting from Profile Activity
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

    Handler profile_handler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            super.handleMessage(msg);

            try
            {
                int id = msg.what;

                if (id == 1)
                {
                    update_toxid_display();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

}
