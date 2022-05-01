/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2022 Zoff <zoff@zoff.cc>
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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.GroupMessageListFragment.group_search_messages_text;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.insert_into_group_message_db;
import static com.zoffcc.applications.trifa.HelperGroup.is_group_active;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_incognito_keyboard;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_offline_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_invite_friend;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_REMOVE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_1;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.wakeup_tox_thread;

public class GroupMessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.GrpMsgLstActivity";
    String group_id = "-1";
    String group_id_prev = "-1";
    //
    static com.vanniktech.emoji.EmojiEditText ml_new_group_message = null;
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
    static ActionMode amode = null;
    static MenuItem amode_save_menu_item = null;
    static MenuItem amode_info_menu_item = null;
    SearchView messageSearchView = null;

    // main drawer ----------
    Drawer group_message_drawer = null;
    AccountHeader group_message_drawer_header = null;
    ProfileDrawerItem group_message_profile_item = null;
    // long peers_in_list_next_num = 0;
    // main drawer ----------

    Handler group_handler = null;
    static Handler group_handler_s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        amode = null;
        amode_save_menu_item = null;
        amode_info_menu_item = null;
        selected_group_messages.clear();

        try
        {
            // reset search and filter flags, sooner
            group_search_messages_text = null;
        }
        catch (Exception e)
        {
        }

        group_handler = new Handler(getMainLooper());
        group_handler_s = group_handler;

        Intent intent = getIntent();
        group_id = intent.getStringExtra("group_id");
        // Log.i(TAG, "onCreate:003:conf_id=" + conf_id + " conf_id_prev=" + conf_id_prev);
        group_id_prev = group_id;

        setContentView(R.layout.activity_group_message_list);

        MainActivity.group_message_list_activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable drawer_header_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_group).
                color(getResources().getColor(R.color.md_dark_primary_text)).sizeDp(100);

        group_message_profile_item = new ProfileDrawerItem().
                withName("Userlist").
                withIcon(drawer_header_icon);

        // Create the AccountHeader
        group_message_drawer_header = new AccountHeaderBuilder().
                withActivity(this).
                withSelectionListEnabledForSingleProfile(false).
                withTextColor(getResources().getColor(R.color.md_dark_primary_text)).
                withHeaderBackground(R.color.colorHeader).
                withCompactStyle(true).
                addProfiles(group_message_profile_item).
                withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
                {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                }).build();

        //        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).
        //                withIdentifier(1L).
        //                withName("User1").
        //                withIcon(GoogleMaterial.Icon.gmd_face);


        // create the drawer and remember the `Drawer` result object
        group_message_drawer = new DrawerBuilder().
                withActivity(this).
                withAccountHeader(group_message_drawer_header).
                withInnerShadow(false).
                withRootView(R.id.drawer_container).
                withShowDrawerOnFirstLaunch(false).
                withActionBarDrawerToggleAnimated(true).
                withActionBarDrawerToggle(true).
                withToolbar(toolbar).
                withTranslucentStatusBar(false).
                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        Log.i(TAG, "drawer:item=" + position);
                        if (position == 1)
                        {
                            // profile
                            try
                            {
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                }).build();


        rootView = (ViewGroup) findViewById(R.id.emoji_bar);
        ml_new_group_message = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.ml_new_message);

        messageSearchView = (SearchView) findViewById(R.id.group_search_view_messages);
        messageSearchView.setQueryHint(getString(R.string.messages_search_default_text));
        messageSearchView.setIconifiedByDefault(true);

        try
        {
            // reset search and filter flags
            messageSearchView.setQuery("", false);
            messageSearchView.setIconified(true);
            group_search_messages_text = null;
        }
        catch (Exception e)
        {
        }

        // give focus to text input
        ml_new_group_message.requestFocus();
        try
        {
            // hide softkeyboard initially
            // since it takes a lot of screen space
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        catch (Exception e)
        {
        }

        insert_emoji = (ImageView) findViewById(R.id.insert_emoji);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);
        ml_button_01 = (ImageButton) findViewById(R.id.ml_button_01);

        ml_phone_icon.setVisibility(View.GONE);
        ml_status_icon.setVisibility(View.INVISIBLE);

        ml_icon.setImageResource(R.drawable.circle_red);
        set_group_connection_status_icon();

        messageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // Log.i(TAG, "search:1:" + query);

                if ((query == null) || (query.length() == 0))
                {
                    try
                    {
                        // all messages
                        group_search_messages_text = null;
                        MainActivity.group_message_list_fragment.update_all_messages(true);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        // all messages and search string
                        group_search_messages_text = query;
                        MainActivity.group_message_list_fragment.update_all_messages(true);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query)
            {
                // Log.i(TAG, "search:2:" + query);

                if ((query == null) || (query.length() == 0))
                {
                    try
                    {
                        // all messages
                        group_search_messages_text = null;
                        MainActivity.group_message_list_fragment.update_all_messages(true);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        // all messages and search string
                        group_search_messages_text = query;
                        MainActivity.group_message_list_fragment.update_all_messages(true);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                return true;
            }
        });

        setUpEmojiPopup();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                color(getResources().
                        getColor(R.color.icon_colors)).
                sizeDp(80);

        insert_emoji.setImageDrawable(d1);

        insert_emoji.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                emojiPopup.toggle();
            }
        });

        // final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(
                getResources().getColor(R.color.icon_colors)).sizeDp(80);

        attachemnt_instead_of_send = true;
        ml_button_01.setImageDrawable(send_message_icon);

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(
                getResources().getColor(R.color.icon_colors)).sizeDp(80);
        ml_phone_icon.setImageDrawable(d2);

        if (PREF__use_incognito_keyboard)
        {
            ml_new_group_message.setImeOptions(
                    EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }
        else
        {
            ml_new_group_message.setImeOptions(EditorInfo.IME_ACTION_SEND);
        }

        if (PREF__window_security)
        {
            // prevent screenshots and also dont show the window content in recent activity screen
            initializeScreenshotSecurity(this);
        }

        set_peer_count_header();
        set_peer_names_and_avatars();

        Log.i(TAG, "onCreate:099");
    }

    synchronized void set_peer_count_header()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final long conference_num = tox_group_by_groupid__wrapper(group_id);
                String group_topic = tox_group_get_name(conference_num);
                if (group_topic == null)
                {
                    group_topic = "";
                }
                final String f_name = group_topic;

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long peer_count = tox_group_peer_count(conference_num);
                            long frozen_peer_count = tox_group_offline_peer_count(conference_num);

                            if (peer_count > -1)
                            {
                                ml_maintext.setText(
                                        f_name + "\n" + getString(R.string.GroupActivityActive) + " " + peer_count +
                                        " " + getString(R.string.GroupActivityOffline) + " " + frozen_peer_count);
                            }
                            else
                            {
                                ml_maintext.setText(f_name);
                                // ml_maintext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            }
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
        };
        t.start();
    }

    synchronized void set_peer_names_and_avatars()
    {
        try
        {
            remove_group_all_users();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.d(TAG, "set_peer_names_and_avatars:002");

        final long conference_num = tox_group_by_groupid__wrapper(group_id);
        long num_peers = tox_group_peer_count(conference_num);

        if (num_peers > 0)
        {
            long[] peers = tox_group_get_peerlist(conference_num);
            if (peers != null)
            {
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, peers[(int) i]);
                        Log.i(TAG,
                              "groupnum=" + conference_num + " peernum=" + peers[(int) i] + " peer_name=" + peer_name);
                        String peer_name_temp =
                                "" + peer_name + " :" + peers[(int) i] + ": " + peer_pubkey_temp.substring(0, 6);

                        add_group_user(peer_pubkey_temp, i, peer_name_temp,
                                       tox_group_peer_get_connection_status(conference_num, peers[(int) i]));
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        long offline_num_peers = tox_group_offline_peer_count(conference_num);

        if (offline_num_peers > 0)
        {
            long[] offline_peers = tox_group_get_offline_peerlist(conference_num);
            if (offline_peers != null)
            {
                long i = 0;
                for (i = 0; i < offline_num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, offline_peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, offline_peers[(int) i]);
                        Log.i(TAG, "groupnum=" + conference_num + " peernum=" + offline_peers[(int) i] + " peer_name=" +
                                   peer_name);
                        String peer_name_temp = "" + peer_name + " :" + offline_peers[(int) i] + ": " +
                                                peer_pubkey_temp.substring(0, 6);

                        add_group_user(peer_pubkey_temp, i, peer_name_temp,
                                       ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        MainActivity.group_message_list_fragment = null;
        MainActivity.group_message_list_activity = null;
        // Log.i(TAG, "onPause:001:conf_id=" + conf_id);
        group_id = "-1";
        // Log.i(TAG, "onPause:002:conf_id=" + conf_id);
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

        // Log.i(TAG, "onResume:001:conf_id=" + conf_id);

        if (group_id.equals("-1"))
        {
            group_id = group_id_prev;
            // Log.i(TAG, "onResume:001:conf_id=" + conf_id);
        }

        change_msg_notification(NOTIFICATION_EDIT_ACTION_REMOVE.value, group_id);

        MainActivity.group_message_list_activity = this;
        wakeup_tox_thread();
    }

    private void setUpEmojiPopup()
    {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(
                new OnEmojiBackspaceClickListener()
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
                        icon(FontAwesome.Icon.faw_keyboard).
                        color(getResources().
                                getColor(R.color.icon_colors)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.about_icon_email);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener()
        {
            @Override
            public void onKeyboardOpen(@Px final int keyBoardHeight)
            {
                // Log.d(TAG, "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener()
        {
            @Override
            public void onEmojiPopupDismiss()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).
                        icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                        color(getResources().
                                getColor(R.color.icon_colors)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener()
        {
            @Override
            public void onKeyboardClose()
            {
                // Log.d(TAG, "Closed soft keyboard");
            }
        }).build(ml_new_group_message);
    }

    String get_current_group_id()
    {
        return group_id;
    }

    public void set_group_connection_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (is_group_active(group_id_prev))
                    {
                        ml_icon.setImageResource(R.drawable.circle_green);
                    }
                    else
                    {
                        ml_icon.setImageResource(R.drawable.circle_red);
                    }
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (!event.isShiftPressed())
                    {
                        // Log.i(TAG, "dispatchKeyEvent:KEYCODE_ENTER");
                        send_message_onclick(null);
                        return true;
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public static void add_quote_group_message_text(final String quote_text)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if ((ml_new_group_message.getText().toString() == null) ||
                        (ml_new_group_message.getText().toString().length() == 0))
                    {
                        ml_new_group_message.append(TEXT_QUOTE_STRING_1 + quote_text + TEXT_QUOTE_STRING_2 + "\n");
                    }
                    else
                    {
                        String old_text = ml_new_group_message.getText().toString();
                        ml_new_group_message.setText("");
                        // need to do it this way, or else the text input cursor will not be in the correct place
                        ml_new_group_message.append(
                                old_text + "\n" + TEXT_QUOTE_STRING_1 + quote_text + TEXT_QUOTE_STRING_2 + "\n");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "add_quote_message_text:EE01:" + e.getMessage());
                }
            }
        };

        if (group_handler_s != null)
        {
            group_handler_s.post(myRunnable);
        }
    }

    synchronized public void send_message_onclick(View view)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";
        try
        {
            // send typed message to friend
            msg = ml_new_group_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                        ml_new_group_message.getText().toString().length()));

            try
            {
                GroupMessage m = new GroupMessage();
                m.is_new = false; // own messages are always "not new"
                m.tox_group_peer_pubkey = global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2));
                m.direction = 1; // msg sent
                m.TOX_MESSAGE_TYPE = 0;
                m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
                m.tox_group_peername = null;
                m.private_message = 0;
                m.group_identifier = group_id;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.sent_timestamp = System.currentTimeMillis();
                m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
                m.text = msg;
                m.was_synced = false;

                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    int res = tox_group_send_message(tox_group_by_groupid__wrapper(group_id), 0, msg);
                    Log.i(TAG, "tox_group_send_message:result=" + res + " m=" + m);
                    if (PREF__X_battery_saving_mode)
                    {
                        Log.i(TAG, "global_last_activity_for_battery_savings_ts:001:*PING*");
                    }
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

                    if (res > -1)
                    {
                        // message was sent OK
                        insert_into_group_message_db(m, true);
                        ml_new_group_message.setText("");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static class long_click_message_return
    {
        boolean is_selected;
        boolean ret_value;
    }

    @Override
    public void onBackPressed()
    {
        if (group_message_drawer.isDrawerOpen())
        {
            group_message_drawer.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }

    synchronized void update_group_all_users()
    {
        try
        {
            set_peer_count_header();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            set_peer_names_and_avatars();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized void remove_group_all_users()
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    lookup_peer_listnum_pubkey.clear();
                                    group_message_drawer.removeAllItems();
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                }
                            }
                        };

                        if (group_handler_s != null)
                        {
                            group_handler_s.post(myRunnable);
                        }

                        // TODO: hack to be synced with additions later
                        Thread.sleep(120);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "remove_group_user:EE:" + e.getMessage());
        }
    }

    synchronized void remove_group_user(String peer_pubkey)
    {
        // TODO: write me
    }

    synchronized void add_group_user(final String peer_pubkey, final long peernum, String name, int connection_status)
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    ConferenceCustomDrawerPeerItem new_item = null;
                                    boolean have_avatar_for_pubkey = false;

                                    try
                                    {
                                        int badge_color = R.color.md_green_700;
                                        if (connection_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value)
                                        {
                                            badge_color = R.color.md_red_700;
                                        }
                                        else if (connection_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value)
                                        {
                                            badge_color = R.color.md_orange_700;
                                        }

                                        new_item = new ConferenceCustomDrawerPeerItem(have_avatar_for_pubkey,
                                                                                      peer_pubkey).
                                                withIdentifier(peernum).
                                                withName(name).
                                                withBadge("" + peernum).withBadgeStyle(
                                                new BadgeStyle().withTextColor(Color.WHITE).withColorRes(badge_color)).
                                                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                                                {
                                                    @Override
                                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                    {
                                                        /*
                                                        Intent intent = new Intent(view.getContext(),
                                                                                   ConferencePeerInfoActivity.class);
                                                        intent.putExtra("peer_pubkey", peer_pubkey);
                                                        intent.putExtra("group_id", group_id);
                                                        intent.putExtra("offline", offline);
                                                        view.getContext().startActivity(intent);
                                                        */
                                                        return true;
                                                    }
                                                });
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        new_item = new ConferenceCustomDrawerPeerItem(false, null).
                                                withIdentifier(peernum).
                                                withName(name).
                                                withIcon(GoogleMaterial.Icon.gmd_face).
                                                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                                                {
                                                    @Override
                                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                    {
                                                        /*
                                                        Intent intent = new Intent(view.getContext(),
                                                                                   ConferencePeerInfoActivity.class);
                                                        intent.putExtra("peer_pubkey", peer_pubkey);
                                                        intent.putExtra("group_id", group_id);
                                                        intent.putExtra("offline", offline);
                                                        view.getContext().startActivity(intent);
                                                        */
                                                        return true;
                                                    }
                                                });
                                    }

                                    // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                    group_message_drawer.addItem(new_item);
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                    Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                }
                            }
                        };

                        if (group_handler_s != null)
                        {
                            group_handler_s.post(myRunnable);
                        }

                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                        Log.i(TAG, "add_group_user:EE3:" + e3.getMessage());
                    }
                }
            };
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_group_user:EE:" + e.getMessage());
        }
    }

    public void show_add_friend_group(View view)
    {
        Log.i(TAG, "show_add_friend_group");
        Intent intent = new Intent(this, FriendSelectSingleActivity.class);
        intent.putExtra("group_id", group_id);
        startActivityForResult(intent, SelectFriendSingleActivity_ID);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectFriendSingleActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                try
                {
                    String result_friend_pubkey = data.getData().toString();
                    if (result_friend_pubkey != null)
                    {
                        if (result_friend_pubkey.length() == TOX_PUBLIC_KEY_SIZE * 2)
                        {
                            Log.i(TAG, "onActivityResult:tox_group_invite_friend:result_friend_pubkey:" +
                                       result_friend_pubkey);

                            long friend_num_temp_safety2 = tox_friend_by_public_key__wrapper(result_friend_pubkey);
                            if (friend_num_temp_safety2 > 0)
                            {
                                if (group_id.equals("-1"))
                                {
                                    group_id = group_id_prev;
                                    // Log.i(TAG, "onActivityResult:001:conf_id=" + conf_id);
                                }

                                final long group_num = tox_group_by_groupid__wrapper(group_id);

                                Log.d(TAG, "onActivityResult:info:tox_group_invite_friend:group_num=" + group_num);

                                if (group_num > -1)
                                {
                                    int res_conf_invite = tox_group_invite_friend(group_num, friend_num_temp_safety2);
                                    Log.d(TAG, "onActivityResult:info:tox_group_invite_friend:res_conf_invite=" +
                                               res_conf_invite);
                                    if (res_conf_invite != 1)
                                    {
                                        Log.d(TAG,
                                              "onActivityResult:info:tox_group_invite_friend:ERR:" + res_conf_invite);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
