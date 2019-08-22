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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.Identicon.create_avatar_identicon_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.friend_list_fragment;
import static com.zoffcc.applications.trifa.MainActivity.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_friend_avatar;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.put_vfs_image_on_imageview;
import static com.zoffcc.applications.trifa.MainActivity.remove_friend_relay_in_db;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.FriendInfoActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    TextView mytoxid = null;
    TextView mynick = null;
    TextView mystatus_message = null;
    EditText alias_text = null;
    TextView fi_relay_pubkey_textview = null;
    TextView fi_relay_text = null;
    Button remove_friend_relay_button = null;
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
        alias_text = (EditText) findViewById(R.id.fi_alias_text);
        fi_relay_pubkey_textview = (TextView) findViewById(R.id.fi_relay_pubkey_textview);
        fi_relay_text = (TextView) findViewById(R.id.fi_relay_text);
        remove_friend_relay_button = (Button) findViewById(R.id.remove_friend_relay_button);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mytoxid.setText("*error*");
        mynick.setText("*error*");
        mystatus_message.setText("*error*");

        alias_text.setText("");

        try
        {
            alias_text.setText(orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0).alias_name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String friend_relay_pubkey = get_relay_for_friend(tox_friend_get_public_key__wrapper(friendnum));

        fi_relay_pubkey_textview.setText("");

        try
        {
            if (friend_relay_pubkey == null)
            {
                fi_relay_text.setVisibility(View.INVISIBLE);
                fi_relay_pubkey_textview.setVisibility(View.INVISIBLE);
                remove_friend_relay_button.setVisibility(View.INVISIBLE);
            }
            else
            {
                fi_relay_text.setVisibility(View.VISIBLE);
                fi_relay_pubkey_textview.setVisibility(View.VISIBLE);
                fi_relay_pubkey_textview.setText(friend_relay_pubkey);

                remove_friend_relay_button.setText("remove Friends Relay");
                remove_friend_relay_button.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            try
                            {
                                remove_friend_relay_in_db(tox_friend_get_public_key__wrapper(friendnum));
                                remove_friend_relay_button.setVisibility(View.INVISIBLE);
                                fi_relay_text.setVisibility(View.INVISIBLE);
                                fi_relay_pubkey_textview.setVisibility(View.INVISIBLE);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            // update friendlist on screen
                            try
                            {
                                friend_list_fragment.add_all_friends_clear(10);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                    });
                remove_friend_relay_button.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
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
                        final FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(
                                tox_friend_get_public_key__wrapper(friendnum_)).toList().get(0);

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    String pubkey_temp = f.tox_public_key_string;
                                    String color_pkey = "<font color=\"#331bc5\">";
                                    String ec = "</font>";
                                    mytoxid.setText(Html.fromHtml(color_pkey + pubkey_temp + ec));

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
            // Log.i(TAG, "fname=" + fname);
            if (fname != null)
            {
                put_vfs_image_on_imageview(this, profile_icon, d1, fname);
            }
            else
            {
                Log.i(TAG, "indenticon:001");

                final FriendList f = orma.selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        toList().get(0);

                create_avatar_identicon_for_pubkey(f.tox_public_key_string);

                String fname3 = get_vfs_image_filename_friend_avatar(friendnum);
                if (fname3 != null)
                {
                    put_vfs_image_on_imageview(this, profile_icon, d1, fname3);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "EE2:" + e.getMessage());
        }

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // TODO dirty hack, just write "alias"

        try
        {
            String alias_name = alias_text.getText().toString();
            if (alias_name != null)
            {
                if (alias_name.length() > 0)
                {
                    orma.updateFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                            alias_name(alias_name).execute();
                }
                else
                {
                    orma.updateFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                            alias_name("").execute();
                }
            }
            else
            {
                orma.updateFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        alias_name("").execute();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                orma.updateFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        alias_name("").execute();
            }
        }
    }
}
