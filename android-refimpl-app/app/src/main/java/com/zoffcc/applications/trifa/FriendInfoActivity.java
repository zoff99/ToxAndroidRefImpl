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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_friend_avatar;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.put_vfs_image_on_imageview;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.FriendInfoActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    TextView mytoxid = null;
    TextView mynick = null;
    TextView mystatus_message = null;
    long friendnum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendinfo);

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);

        profile_icon = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.fi_profile_icon);
        mytoxid = (TextView) findViewById(R.id.fi_toxprvkey_textview);
        mynick = (TextView) findViewById(R.id.fi_nick_text);
        mystatus_message = (TextView) findViewById(R.id.fi_status_message_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mytoxid.setText("*error*");
        mynick.setText("*error*");
        mystatus_message.setText("*error*");

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        profile_icon.setImageDrawable(d1);

        try
        {
            final long friendnum_ = friendnum;
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum_)).toList().get(0);

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    mytoxid.setText(f.tox_public_key_string);
                                    mynick.setText(f.name);
                                    mystatus_message.setText(f.status_message);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "CALL:start:EE:" + e.getMessage());
                                }
                            }
                        };
                        main_handler_s.post(myRunnable);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        try
        {
            String fname = get_vfs_image_filename_friend_avatar(friendnum);
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
}