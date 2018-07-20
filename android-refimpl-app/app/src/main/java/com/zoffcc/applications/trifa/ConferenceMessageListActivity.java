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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import static com.zoffcc.applications.trifa.MainActivity.get_conference_num_from_confid;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_conference_message_db;
import static com.zoffcc.applications.trifa.MainActivity.is_conference_active;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.resolve_name_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.selected_conference_messages;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

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
    static ActionMode amode = null;
    static MenuItem amode_save_menu_item = null;

    // main drawer ----------
    Drawer conference_message_drawer = null;
    AccountHeader conference_message_drawer_header = null;
    ProfileDrawerItem conference_message_profile_item = null;
    // long peers_in_list_next_num = 0;
    // main drawer ----------

    Handler conferences_handler = null;
    static Handler conferences_handler_s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        amode = null;
        amode_save_menu_item = null;
        selected_conference_messages.clear();

        conferences_handler = new Handler(getMainLooper());
        conferences_handler_s = conferences_handler;

        Intent intent = getIntent();
        conf_id = intent.getStringExtra("conf_id");
        Log.i(TAG, "onCreate:003:conf_id=" + conf_id + " conf_id_prev=" + conf_id_prev);
        conf_id_prev = conf_id;

        setContentView(R.layout.activity_conference_message_list);

        MainActivity.conference_message_list_activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable drawer_header_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_group).
                color(getResources().getColor(R.color.md_dark_primary_text)).sizeDp(100);

        conference_message_profile_item = new ProfileDrawerItem().
                withName("Userlist").
                withIcon(drawer_header_icon);

        // Create the AccountHeader
        conference_message_drawer_header = new AccountHeaderBuilder().
                withActivity(this).
                withSelectionListEnabledForSingleProfile(false).
                withTextColor(getResources().getColor(R.color.md_dark_primary_text)).
                withHeaderBackground(R.color.colorHeader).
                withCompactStyle(true).
                addProfiles(conference_message_profile_item).
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


        // peers_in_list_next_num = 1;
        lookup_peer_listnum_pubkey.clear();

        // create the drawer and remember the `Drawer` result object
        conference_message_drawer = new DrawerBuilder().
                withActivity(this).
                withAccountHeader(conference_message_drawer_header).
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
        ml_new_message = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.ml_new_message);
        insert_emoji = (ImageView) findViewById(R.id.insert_emoji);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);
        ml_button_01 = (ImageButton) findViewById(R.id.ml_button_01);

        ml_phone_icon.setVisibility(View.GONE);
        ml_status_icon.setVisibility(View.INVISIBLE);

        ml_icon.setImageResource(R.drawable.circle_red);
        set_conference_connection_status_icon();

        setUpEmojiPopup();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                color(getResources().
                        getColor(R.color.colorPrimaryDark)).
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
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);

        attachemnt_instead_of_send = true;
        ml_button_01.setImageDrawable(send_message_icon);

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        ml_phone_icon.setImageDrawable(d2);

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
                final String f_name = MainActivity.get_conference_title_from_confid(conf_id);
                final long conference_num = get_conference_num_from_confid(conf_id);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long peer_count = tox_conference_peer_count(conference_num);

                            if (peer_count > -1)
                            {
                                ml_maintext.setText(f_name + "\n" + "Users: " + peer_count);
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
        if (is_conference_active(conf_id))
        {
            Log.d(TAG, "set_peer_names_and_avatars:001");

            try
            {
                remove_group_all_users();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Log.d(TAG, "set_peer_names_and_avatars:002");

            final long conference_num = get_conference_num_from_confid(conf_id);
            long num_peers = tox_conference_peer_count(conference_num);

            Log.d(TAG, "set_peer_names_and_avatars:003:peer count=" + num_peers);

            if (num_peers > 0)
            {
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    String peer_pubkey_temp = tox_conference_peer_get_public_key(conference_num, i);
                    String peer_name_temp = tox_conference_peer_get_name(conference_num, i);
                    if (peer_name_temp.equals(""))
                    {
                        peer_name_temp = null;
                    }
                    // Log.d(TAG, "set_peer_names_and_avatars:004:add:" + peer_name_temp);
                    add_group_user(peer_pubkey_temp, i, peer_name_temp);
                }
            }
        }
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
                        icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
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
                    if (is_conference_active(conf_id))
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

    synchronized public void send_message_onclick(View view)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";
        try
        {
            if (is_conference_active(conf_id))
            {
                // send typed message to friend
                msg = ml_new_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                      ml_new_message.getText().toString().length()));

                try
                {
                    ConferenceMessage m = new ConferenceMessage();
                    m.is_new = false; // own messages are always "not new"
                    m.tox_peerpubkey = global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2));
                    m.direction = 1; // msg sent
                    m.TOX_MESSAGE_TYPE = 0;
                    m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
                    m.tox_peername = null;
                    m.conference_identifier = conf_id;
                    m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                    m.sent_timestamp = System.currentTimeMillis();
                    m.text = msg;

                    if ((msg != null) && (!msg.equalsIgnoreCase("")))
                    {
                        int res = tox_conference_send_message(get_conference_num_from_confid(conf_id), 0, msg);
                        Log.i(TAG, "tox_conference_send_message:result=" + res + " m=" + m);

                        if (res > -1)
                        {
                            // message was sent OK
                            insert_into_conference_message_db(m, true);
                            ml_new_message.setText("");
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static boolean onClick_message_helper(final View v, boolean is_selected, final ConferenceMessage message_)
    {
        try
        {
            if (is_selected)
            {
                v.setBackgroundColor(Color.TRANSPARENT);
                is_selected = false;
                selected_conference_messages.remove(message_.id);
                amode_save_menu_item.setVisible(false);

                if (selected_conference_messages.isEmpty())
                {
                    // last item was de-selected
                    amode.finish();
                }
                else
                {
                    if (amode != null)
                    {
                        amode.setTitle("" + selected_conference_messages.size() + " selected");
                    }
                }
            }
            else
            {
                if (!selected_conference_messages.isEmpty())
                {
                    v.setBackgroundColor(Color.GRAY);
                    is_selected = true;
                    selected_conference_messages.add(message_.id);
                    amode_save_menu_item.setVisible(false);

                    if (amode != null)
                    {
                        amode.setTitle("" + selected_conference_messages.size() + " selected");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return is_selected;
    }

    static class long_click_message_return
    {
        boolean is_selected;
        boolean ret_value;
    }

    static long_click_message_return onLongClick_message_helper(Context context, final View v, boolean is_selected, final ConferenceMessage message_)
    {
        long_click_message_return ret = new long_click_message_return();

        try
        {
            if (is_selected)
            {
            }
            else
            {
                if (selected_conference_messages.isEmpty())
                {
                    try
                    {
                        amode = MainActivity.conference_message_list_activity.startSupportActionMode(
                                new ToolbarActionMode(context));
                        amode_save_menu_item = amode.getMenu().findItem(R.id.action_save);
                        v.setBackgroundColor(Color.GRAY);
                        ret.is_selected = true;
                        selected_conference_messages.add(message_.id);
                        amode_save_menu_item.setVisible(false);

                        if (amode != null)
                        {
                            amode.setTitle("" + selected_conference_messages.size() + " selected");
                        }
                        ret.ret_value = true;
                        return ret;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ret.ret_value = false;
        return ret;
    }

    @Override
    public void onBackPressed()
    {
        if (conference_message_drawer.isDrawerOpen())
        {
            conference_message_drawer.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }

    long peer_pubkey_to_long_in_list(String peer_pubkey)
    {
        long ret = -1L;

        if (lookup_peer_listnum_pubkey.containsKey(peer_pubkey))
        {
            ret = lookup_peer_listnum_pubkey.get(peer_pubkey);
        }

        return ret;
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
        Log.d(TAG, "remove_group_all_users:001");

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
                                    conference_message_drawer.removeAllItems();
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                }
                            }
                        };

                        if (conferences_handler_s != null)
                        {
                            conferences_handler_s.post(myRunnable);
                        }

                        // TODO: hack to be synced with additions later
                        Thread.sleep(120);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "remove_group_all_users:T:END");

                }
            };
            t.start();
            t.join();

            Log.d(TAG, "remove_group_all_users:T:099");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "remove_group_user:EE:" + e.getMessage());
        }

        Log.d(TAG, "remove_group_all_users:002");

    }

    synchronized void remove_group_user(String peer_pubkey)
    {
        try
        {
            final long peer_num_in_list = peer_pubkey_to_long_in_list(peer_pubkey);
            if (peer_num_in_list != -1)
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
                                        conference_message_drawer.removeItem(peer_num_in_list);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                    }
                                }
                            };

                            if (conferences_handler_s != null)
                            {
                                conferences_handler_s.post(myRunnable);
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "remove_group_user:EE:" + e.getMessage());
        }
    }

    synchronized void add_group_user(final String peer_pubkey, final long peernum, String name)
    {
        try
        {
            long peer_num_in_list = peer_pubkey_to_long_in_list(peer_pubkey);
            if (peer_num_in_list == -1)
            {
                // -- ADD --
                String name2 = "";
                if (name != null)
                {
                    name2 = name;
                }
                else
                {
                    name2 = peer_pubkey.substring(peer_pubkey.length() - 5, peer_pubkey.length());
                }

                try
                {
                    name2 = resolve_name_for_pubkey(peer_pubkey, name2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                final String name3 = name2;

                lookup_peer_listnum_pubkey.put(peer_pubkey, peernum);

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
                                        FriendList fl_temp = null;
                                        boolean have_avatar_for_pubkey = false;

                                        try
                                        {
                                            fl_temp = orma.selectFromFriendList().
                                                    tox_public_key_stringEq(peer_pubkey).get(0);

                                            if ((fl_temp.avatar_filename != null) && (fl_temp.avatar_pathname != null))
                                            {
                                                info.guardianproject.iocipher.File f1 = null;
                                                try
                                                {
                                                    f1 = new info.guardianproject.iocipher.File(
                                                            fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename);
                                                    if (f1.length() > 0)
                                                    {
                                                        have_avatar_for_pubkey = true;
                                                    }
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else
                                            {
                                                have_avatar_for_pubkey = false;
                                                fl_temp = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            have_avatar_for_pubkey = false;
                                            // Log.i(TAG, "have_avatar_for_pubkey:00a04:" + have_avatar_for_pubkey);
                                            fl_temp = null;
                                        }

                                        try
                                        {
                                            new_item = new ConferenceCustomDrawerPeerItem(have_avatar_for_pubkey,
                                                                                          peer_pubkey).
                                                    withIdentifier(peernum).
                                                    withName(name3);
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            new_item = new ConferenceCustomDrawerPeerItem(false, null).
                                                    withIdentifier(peernum).
                                                    withName(name3).
                                                    withIcon(GoogleMaterial.Icon.gmd_face);
                                        }

                                        // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                        conference_message_drawer.addItem(new_item);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                        Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                    }
                                }
                            };

                            if (conferences_handler_s != null)
                            {
                                conferences_handler_s.post(myRunnable);
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
            else
            {
                // -- UPDATE --
                String name2 = "";
                if (name != null)
                {
                    name2 = name;
                }
                else
                {
                    name2 = peer_pubkey.substring(peer_pubkey.length() - 5, peer_pubkey.length());
                }

                try
                {
                    name2 = resolve_name_for_pubkey(peer_pubkey, name2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                final String name3 = name2;

                lookup_peer_listnum_pubkey.put(peer_pubkey, peernum);

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
                                        StringHolder sh = new StringHolder(name3);
                                        // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                        conference_message_drawer.updateName(peernum, sh);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                        Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                    }
                                }
                            };

                            if (conferences_handler_s != null)
                            {
                                conferences_handler_s.post(myRunnable);
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_group_user:EE:" + e.getMessage());
        }
    }

}
