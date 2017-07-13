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
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;

public class ConferenceMessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CnfMsgLstActivity";
    String conf_id = "-1";
    String conf_id_prev = "-1";
    //
    com.vanniktech.emoji.EmojiEditText ml_new_message = null;
    EmojiPopup emojiPopup = null;
    ImageView insert_emoji = null;
    TextView ml_maintext = null;
    ViewGroup rootView = null;
    //
    ImageView ml_icon = null;
    ImageView ml_status_icon = null;
    ImageButton ml_phone_icon = null;
    ImageButton ml_button_01 = null;
    static boolean attachemnt_instead_of_send = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        Intent intent = getIntent();
        conf_id = intent.getStringExtra("conf_id");
        Log.i(TAG, "onCreate:003:conf_id=" + conf_id + " conf_id_prev=" + conf_id_prev);
        conf_id_prev = conf_id;

        setContentView(R.layout.activity_conference_message_list);

        MainActivity.conference_message_list_activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootView = (ViewGroup) findViewById(R.id.emoji_bar);
        ml_new_message = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.ml_new_message);
        insert_emoji = (ImageView) findViewById(R.id.insert_emoji);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);
        ml_button_01 = (ImageButton) findViewById(R.id.ml_button_01);

        ml_phone_icon.setVisibility(View.GONE);
        ml_status_icon.setVisibility(View.INVISIBLE);

        final ImageButton button01_ = ml_button_01;

        ml_icon.setImageResource(R.drawable.circle_red);

        setUpEmojiPopup();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).
                icon(FontAwesome.Icon.faw_smile_o).
                color(getResources().
                        getColor(R.color.colorPrimaryDark)).
                sizeDp(80);

        insert_emoji.setImageDrawable(d1);
        // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);


        insert_emoji.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                emojiPopup.toggle();
            }
        });

        final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);

        attachemnt_instead_of_send = true;
        ml_button_01.setImageDrawable(add_attachement_icon);

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        ml_phone_icon.setImageDrawable(d2);

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final String f_name = MainActivity.get_peer_name_from_conf_id(conf_id);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ml_maintext.setText(f_name);
                    }
                };

                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
        };
        t.start();

        Log.i(TAG, "onCreate:099");
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        MainActivity.conference_message_list_fragment = null;
        MainActivity.conference_message_list_activity = null;
        Log.i(TAG, "onPause:001:conf_id=" + conf_id);
        conf_id = "-1";
        Log.i(TAG, "onPause:002:conf_id=" + conf_id);
    }

    @Override
    protected void onStop()
    {
        if (emojiPopup != null)
        {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        Log.i(TAG, "onResume:001:conf_id=" + conf_id);

        if (conf_id.equals("-1"))
        {
            conf_id = conf_id_prev;
            Log.i(TAG, "onResume:001:conf_id=" + conf_id);
        }

        MainActivity.conference_message_list_activity = this;
    }

    private void setUpEmojiPopup()
    {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener()
        {
            @Override
            public void onEmojiBackspaceClick(View v)
            {

            }

        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener()
        {
            @Override
            public void onEmojiPopupShown()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).
                        icon(FontAwesome.Icon.faw_keyboard_o).
                        color(getResources().
                                getColor(R.color.colorPrimaryDark)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.about_icon_email);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener()
        {
            @Override
            public void onKeyboardOpen(@Px final int keyBoardHeight)
            {
                Log.d(TAG, "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener()
        {
            @Override
            public void onEmojiPopupDismiss()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).
                        icon(FontAwesome.Icon.faw_smile_o).
                        color(getResources().
                                getColor(R.color.colorPrimaryDark)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener()
        {
            @Override
            public void onKeyboardClose()
            {
                Log.d(TAG, "Closed soft keyboard");
            }
        }).build(ml_new_message);
    }

    String get_current_conf_id()
    {
        return conf_id;
    }

    public void set_conference_connection_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // TODO
                    ml_icon.setImageResource(R.drawable.circle_green);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    synchronized public void send_message_onclick(View view)
    {
        // TODO

        String msg = "";
        try
        {
        }
        catch (Exception e)
        {
            msg = "";
            e.printStackTrace();
        }
    }
}
