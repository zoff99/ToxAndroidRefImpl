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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import id.zelory.compressor.Compressor;

import static com.zoffcc.applications.trifa.CallingActivity.initializeScreenshotSecurity;
import static com.zoffcc.applications.trifa.GroupMessageListFragment.group_search_messages_text;
import static com.zoffcc.applications.trifa.HelperFiletransfer.copy_outgoing_file_to_sdcard_dir;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytebuffer_to_hexstring;
import static com.zoffcc.applications.trifa.HelperGeneric.display_toast;
import static com.zoffcc.applications.trifa.HelperGeneric.do_fade_anim_on_fab;
import static com.zoffcc.applications.trifa.HelperGeneric.fourbytes_of_long_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.io_file_copy;
import static com.zoffcc.applications.trifa.HelperGroup.get_group_peernum_from_peer_pubkey;
import static com.zoffcc.applications.trifa.HelperGroup.insert_into_group_message_db;
import static com.zoffcc.applications.trifa.HelperGroup.is_group_active;
import static com.zoffcc.applications.trifa.HelperGroup.send_group_image;
import static com.zoffcc.applications.trifa.HelperGroup.shrink_image_file;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperMsgNotification.change_msg_notification;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_incognito_keyboard;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_text_only;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_text_only;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_offline_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_invite_friend;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_role;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_OUTGOING_FILESIZE_BYTE_USE_STORAGE_FRAMEWORK;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_OUTGOING_FILESIZE_NGC_MAX_TOTAL;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_EDIT_ACTION.NOTIFICATION_EDIT_ACTION_REMOVE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_1;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TEXT_QUOTE_STRING_2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE;
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
    static boolean oncreate_finished = false;
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
        oncreate_finished = false;
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        amode = null;
        amode_save_menu_item = null;
        amode_info_menu_item = null;
        selected_group_messages.clear();
        selected_group_messages_text_only.clear();
        selected_group_messages_incoming_file.clear();

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

        final Drawable drawer_header_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_group).color(
                getResources().getColor(R.color.md_dark_primary_text)).sizeDp(100);

        group_message_profile_item = new ProfileDrawerItem().withName("Userlist").withIcon(drawer_header_icon);

        // Create the AccountHeader
        group_message_drawer_header = new AccountHeaderBuilder().withActivity(
                this).withSelectionListEnabledForSingleProfile(false).withTextColor(
                getResources().getColor(R.color.md_dark_primary_text)).withHeaderBackground(
                R.color.colorHeader).withCompactStyle(true).addProfiles(
                group_message_profile_item).withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
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
        group_message_drawer = new DrawerBuilder().withActivity(this).withAccountHeader(
                group_message_drawer_header).withInnerShadow(false).withRootView(
                R.id.drawer_container).withShowDrawerOnFirstLaunch(false).withActionBarDrawerToggleAnimated(
                true).withActionBarDrawerToggle(true).withToolbar(toolbar).withTranslucentStatusBar(
                false).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
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
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
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
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
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
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
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
                        MainActivity.group_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
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

        final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(
                GoogleMaterial.Icon.gmd_sentiment_satisfied).color(getResources().getColor(R.color.icon_colors)).sizeDp(
                80);

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
        oncreate_finished = true;
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

    static class group_list_peer
    {
        String peer_name;
        long peer_num;
        String peer_pubkey;
        int peer_connection_status;
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
                List<group_list_peer> group_peers1 = new ArrayList<>();
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, peers[(int) i]);

                        int peerrole = ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value;
                        try
                        {
                            peerrole = tox_group_peer_get_role(conference_num,
                                                               get_group_peernum_from_peer_pubkey(group_id,
                                                                                                  peer_pubkey_temp));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        // Log.i(TAG,
                        //      "groupnum=" + conference_num + " peernum=" + peers[(int) i] + " peer_name=" + peer_name);
                        String peer_name_temp =
                                ToxVars.Tox_Group_Role.value_char(peerrole) + " " + peer_name + " :" + peers[(int) i] +
                                ": " + peer_pubkey_temp.substring(0, 6);

                        group_list_peer glp = new group_list_peer();
                        glp.peer_pubkey = peer_pubkey_temp;
                        glp.peer_num = i;
                        glp.peer_name = peer_name_temp;
                        glp.peer_connection_status = tox_group_peer_get_connection_status(conference_num, peers[(int) i]);
                        group_peers1.add(glp);
                    }
                    catch (Exception ignored)
                    {
                    }
                }

                try
                {
                    Collections.sort(group_peers1, new Comparator<group_list_peer>()
                    {
                        @Override
                        public int compare(group_list_peer p1, group_list_peer p2)
                        {
                            String name1 = p1.peer_name;
                            String name2 = p2.peer_name;
                            return name1.compareToIgnoreCase(name2);
                        }
                    });
                }
                catch(Exception ignored)
                {
                }

                for (group_list_peer peerl: group_peers1)
                {
                    add_group_user(peerl.peer_pubkey, peerl.peer_num, peerl.peer_name, peerl.peer_connection_status);
                }
            }
        }

        long offline_num_peers = tox_group_offline_peer_count(conference_num);

        if (offline_num_peers > 0)
        {
            long[] offline_peers = tox_group_get_offline_peerlist(conference_num);
            if (offline_peers != null)
            {
                List<group_list_peer> group_peers_offline = new ArrayList<group_list_peer>();
                long i = 0;
                for (i = 0; i < offline_num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, offline_peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, offline_peers[(int) i]);
                        // Log.i(TAG, "groupnum=" + conference_num + " peernum=" + offline_peers[(int) i] + " peer_name=" +
                        //           peer_name);
                        String peer_name_temp = "" + peer_name + " :" + offline_peers[(int) i] + ": " +
                                                peer_pubkey_temp.substring(0, 6);

                        group_list_peer glp3 = new group_list_peer();
                        glp3.peer_pubkey = peer_pubkey_temp;
                        glp3.peer_num = i;
                        glp3.peer_name = peer_name_temp;
                        glp3.peer_connection_status = ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value;
                        group_peers_offline.add(glp3);
                    }
                    catch (Exception ignored)
                    {
                    }
                }

                try
                {
                    Collections.sort(group_peers_offline, new Comparator<group_list_peer>()
                    {
                        @Override
                        public int compare(group_list_peer p1, group_list_peer p2)
                        {
                            String name1 = p1.peer_name;
                            String name2 = p2.peer_name;
                            return name1.compareToIgnoreCase(name2);
                        }
                    });
                }
                catch(Exception ignored)
                {
                }

                for (group_list_peer peerloffline: group_peers_offline)
                {
                    add_group_user(peerloffline.peer_pubkey, peerloffline.peer_num, peerloffline.peer_name, peerloffline.peer_connection_status);
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
                final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(FontAwesome.Icon.faw_keyboard).color(
                        getResources().getColor(R.color.icon_colors)).sizeDp(80);

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
                final Drawable d1 = new IconicsDrawable(getBaseContext()).icon(
                        GoogleMaterial.Icon.gmd_sentiment_satisfied).color(
                        getResources().getColor(R.color.icon_colors)).sizeDp(80);

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
                        if (tox_group_is_connected(tox_group_by_groupid__wrapper(group_id_prev)) ==
                            TRIFAGlobals.TOX_GROUP_CONNECTION_STATUS.TOX_GROUP_CONNECTION_STATUS_CONNECTED.value)
                        {
                            ml_icon.setImageResource(R.drawable.circle_green);
                        }
                        else
                        {
                            ml_icon.setImageResource(R.drawable.circle_orange);
                        }
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
                m.tox_group_peer_pubkey = tox_group_self_get_public_key(
                        tox_group_by_groupid__wrapper(group_id)).toUpperCase();
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
                m.TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;

                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    long message_id = tox_group_send_message(tox_group_by_groupid__wrapper(group_id), 0, msg);
                    Log.i(TAG, "tox_group_send_message:result=" + message_id + " m=" + m);
                    if (PREF__X_battery_saving_mode)
                    {
                        Log.i(TAG, "global_last_activity_for_battery_savings_ts:001:*PING*");
                    }
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

                    if (message_id > -1)
                    {
                        // message was sent OK
                        m.message_id_tox = fourbytes_of_long_to_hex(message_id);
                        // Log.i(TAG, "message_id_tox=" + m.message_id_tox + " message_id=" + message_id);
                        // TODO: m.msg_id_hash = hex(peerpubkey + message_id)
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

    static void add_attachment_ngc(Context c, Intent data, Intent orig_intent, String groupid_local, boolean activity_group_num)
    {
        Log.i(TAG, "add_attachment:001");

        try
        {
            String fileName = null;

            try
            {
                DocumentFile documentFile = DocumentFile.fromSingleUri(c, data.getData());

                fileName = documentFile.getName();
                // Log.i(TAG, "file_attach_for_send:documentFile:fileName=" + fileName);
                // Log.i(TAG, "file_attach_for_send:documentFile:fileLength=" + documentFile.length());

                ContentResolver cr = c.getApplicationContext().getContentResolver();
                Cursor metaCursor = cr.query(data.getData(), null, null, null, null);
                if (metaCursor != null)
                {
                    try
                    {
                        if (metaCursor.moveToFirst())
                        {
                            String file_path = metaCursor.getString(0);
                            // Log.i(TAG, "file_attach_for_send:metaCursor_path:fp=" + file_path);
                            // Log.i(TAG, "file_attach_for_send:metaCursor_path:column names=" +
                            //            metaCursor.getColumnNames().length);
                            int j;
                            for (j = 0; j < metaCursor.getColumnNames().length; j++)
                            {
                                // Log.i(TAG, "file_attach_for_send:metaCursor_path:column name=" +
                                //           metaCursor.getColumnName(j));
                                // Log.i(TAG,
                                //       "file_attach_for_send:metaCursor_path:column data=" + metaCursor.getString(j));
                                if (metaCursor.getColumnName(j).equals(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                                {
                                    if (metaCursor.getString(j) != null)
                                    {
                                        if (metaCursor.getString(j).length() > 0)
                                        {
                                            fileName = metaCursor.getString(j);
                                            // Log.i(TAG, "file_attach_for_send:filename new=" + fileName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        metaCursor.close();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            final String fileName_ = fileName;

            if (fileName_ != null)
            {
                if (activity_group_num)
                {
                    Log.i(TAG, "add_outgoing_file:activity_group_num:true");
                    final Thread t = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            if (groupid_local.equals("-1"))
                            {
                                // ok, we need to wait for onResume to finish and give us the friendnum
                                Log.i(TAG,
                                      "add_outgoing_file:ok, we need to wait for onResume to finish and give us the friendnum");
                                long loop = 0;
                                while (loop < 100)
                                {
                                    loop++;
                                    try
                                    {
                                        Thread.sleep(20);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (MainActivity.group_message_list_activity != null)
                                    {
                                        if (!MainActivity.group_message_list_activity.get_current_group_id().equals(
                                                "-1"))
                                        {
                                            // got friendnum
                                            Log.i(TAG, "add_outgoing_file:got groupnum");
                                            break;
                                        }
                                    }
                                }

                                loop = 0;
                                while (loop < 1000)
                                {
                                    loop++;
                                    try
                                    {
                                        Thread.sleep(20);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (oncreate_finished)
                                    {
                                        Log.i(TAG, "add_outgoing_file:oncreate_finished");
                                        break;
                                    }
                                }
                            }

                            try
                            {
                                Thread.sleep(50);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            if (MainActivity.group_message_list_activity != null)
                            {
                                if (!MainActivity.group_message_list_activity.get_current_group_id().equals("-1"))
                                {
                                    // sorry, still no friendnum
                                    Log.i(TAG, "add_outgoing_file:sorry, still no groupnum");
                                    return;
                                }
                            }
                            Log.i(TAG, "add_outgoing_file:add_outgoing_file:thread_01");
                            add_outgoing_file(c, MainActivity.group_message_list_activity.get_current_group_id(),
                                              data.getData().toString(), fileName_, data.getData(), false,
                                              activity_group_num);
                        }
                    };
                    t.start();
                }
                else
                {
                    Log.i(TAG, "add_outgoing_file:activity_group_num:FALSE");
                    final Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            Log.i(TAG, "add_outgoing_file:add_outgoing_file:thread_02");

                            long loop = 0;
                            while (loop < 100)
                            {
                                loop++;
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                if (MainActivity.group_message_list_activity != null)
                                {
                                    if (!MainActivity.group_message_list_activity.get_current_group_id().equals(
                                            "-1"))
                                    {
                                        // got friendnum
                                        Log.i(TAG, "add_outgoing_file:got groupnum:02");
                                        break;
                                    }
                                }
                            }

                            loop = 0;
                            while (loop < 1000)
                            {
                                loop++;
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                if (oncreate_finished)
                                {
                                    Log.i(TAG, "add_outgoing_file:oncreate_finished:02");
                                    break;
                                }
                            }
                            add_outgoing_file(c, groupid_local, data.getData().toString(), fileName_, data.getData(),
                                              false, activity_group_num);
                        }
                    };
                    t2.start();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "select_file:22:EE1:" + e.getMessage());
        }
    }

    static boolean onClick_message_helper(final View v, boolean is_selected, final GroupMessage message_)
    {
        try
        {
            if (is_selected)
            {
                v.setBackgroundColor(Color.TRANSPARENT);
                is_selected = false;
                selected_group_messages.remove(message_.id);
                selected_group_messages_text_only.remove(message_.id);
                selected_group_messages_incoming_file.remove(message_.id);
                if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                {
                    amode_save_menu_item.setVisible(true);
                }
                else
                {
                    amode_save_menu_item.setVisible(false);
                }

                if (selected_group_messages.size() == 1)
                {
                    amode_info_menu_item.setVisible(true);
                }
                else
                {
                    amode_info_menu_item.setVisible(false);
                }

                if (selected_group_messages.isEmpty())
                {
                    // last item was de-selected
                    amode.finish();
                }
                else
                {
                    if (amode != null)
                    {
                        amode.setTitle("" + selected_group_messages.size() + " selected");
                    }
                }
            }
            else
            {
                if (!selected_group_messages.isEmpty())
                {
                    v.setBackgroundColor(Color.GRAY);
                    is_selected = true;
                    selected_group_messages.add(message_.id);

                    if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                    {
                        selected_group_messages_text_only.add(message_.id);
                    }
                    else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (message_.direction == 0)
                        {
                            selected_group_messages_incoming_file.add(message_.id);
                        }
                    }

                    if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                    {
                        amode_save_menu_item.setVisible(true);
                    }
                    else
                    {
                        amode_save_menu_item.setVisible(false);
                    }


                    if (selected_group_messages.size() == 1)
                    {
                        amode_info_menu_item.setVisible(true);
                    }
                    else
                    {
                        amode_info_menu_item.setVisible(false);
                    }

                    if (amode != null)
                    {
                        amode.setTitle("" + selected_group_messages.size() + " selected");
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

    static GroupMessageListActivity.long_click_message_return onLongClick_message_helper(Context context, final View v, boolean is_selected, final GroupMessage message_)
    {
        GroupMessageListActivity.long_click_message_return ret = new GroupMessageListActivity.long_click_message_return();

        try
        {
            if (is_selected)
            {
                ret.is_selected = true;
            }
            else
            {
                if (selected_group_messages.isEmpty())
                {
                    try
                    {
                        amode = MainActivity.group_message_list_activity.startSupportActionMode(
                                new ToolbarActionMode(context));
                        amode_save_menu_item = amode.getMenu().findItem(R.id.action_save);
                        amode_info_menu_item = amode.getMenu().findItem(R.id.action_info);
                        v.setBackgroundColor(Color.GRAY);
                        ret.is_selected = true;
                        selected_group_messages.add(message_.id);

                        if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                        {
                            selected_group_messages_text_only.add(message_.id);
                        }
                        else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                        {
                            if (message_.direction == 0)
                            {
                                selected_group_messages_incoming_file.add(message_.id);
                            }
                        }

                        if (selected_group_messages_incoming_file.size() == selected_group_messages.size())
                        {
                            amode_save_menu_item.setVisible(true);
                        }
                        else
                        {
                            amode_save_menu_item.setVisible(false);
                        }


                        if (selected_group_messages.size() == 1)
                        {
                            amode_info_menu_item.setVisible(true);
                        }
                        else
                        {
                            amode_info_menu_item.setVisible(false);
                        }

                        if (amode != null)
                        {
                            amode.setTitle("" + selected_group_messages.size() + " selected");
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

        ret.ret_value = true;
        return ret;
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
                                                                                      peer_pubkey).withIdentifier(
                                                peernum).withName(name).withBadge("" + peernum).withBadgeStyle(
                                                new BadgeStyle().withTextColor(Color.WHITE).withColorRes(
                                                        badge_color)).withOnDrawerItemClickListener(
                                                new Drawer.OnDrawerItemClickListener()
                                                {
                                                    @Override
                                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                    {
                                                        Intent intent = new Intent(view.getContext(),
                                                                                   GroupPeerInfoActivity.class);
                                                        intent.putExtra("peer_pubkey", peer_pubkey);
                                                        intent.putExtra("group_id", group_id);
                                                        view.getContext().startActivity(intent);
                                                        return true;
                                                    }
                                                });
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        new_item = new ConferenceCustomDrawerPeerItem(false, null).withIdentifier(
                                                peernum).withName(name).withIcon(
                                                GoogleMaterial.Icon.gmd_face).withOnDrawerItemClickListener(
                                                new Drawer.OnDrawerItemClickListener()
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

        Log.i(TAG, "onActivityResult:tox_group_invite_friend:start");
        if (requestCode == SelectFriendSingleActivity_ID)
        {
            Log.i(TAG, "onActivityResult:tox_group_invite_friend:001");
            if (resultCode == RESULT_OK)
            {
                Log.i(TAG, "onActivityResult:tox_group_invite_friend:002");
                try
                {
                    int item_type = Integer.parseInt( data.getData().toString().substring(0, 1));
                    String result_friend_pubkey = data.getData().toString().substring(2);
                    Log.i(TAG, "onActivityResult:tox_group_invite_friend:003:");
                    if (result_friend_pubkey != null)
                    {
                        Log.i(TAG, "onActivityResult:tox_group_invite_friend:004:");
                        if (result_friend_pubkey.length() == TOX_PUBLIC_KEY_SIZE * 2)
                        {
                            Log.i(TAG, "onActivityResult:tox_group_invite_friend:result_friend_pubkey:ok");
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

    static void add_outgoing_file(Context c, String groupid, String filepath, String filename, Uri uri, boolean real_file_path, boolean update_message_view)
    {
        long file_size = -1;
        try
        {
            DocumentFile documentFile = DocumentFile.fromSingleUri(c, uri);
            String fileName = documentFile.getName();
            Log.i(TAG, "add_outgoing_file:documentFile:fileName=" + fileName);
            Log.i(TAG, "add_outgoing_file:documentFile:fileLength=" + documentFile.length());

            file_size = documentFile.length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            // file length "zero"?
            return;
        }

        if (file_size > FT_OUTGOING_FILESIZE_NGC_MAX_TOTAL)
        {
            display_toast("File too large", true, 100);
            return;
        }

        if (file_size < FT_OUTGOING_FILESIZE_BYTE_USE_STORAGE_FRAMEWORK) // less than xxx Bytes filesize
        {
            MessageListActivity.outgoing_file_wrapped ofw = copy_outgoing_file_to_sdcard_dir(filepath, filename,
                                                                                             file_size);

            if (ofw == null)
            {
                return;
            }

            if (file_size > TOX_MAX_NGC_FILESIZE)
            {
                // reducing the file size down to hopefully 37kbytes -------------------
                Log.i(TAG, "add_outgoing_file:shrink:start");
                shrink_image_file(c, ofw);
                Log.i(TAG, "add_outgoing_file:shrink:done");
                // reducing the file size down to hopefully 37kbytes -------------------
            }
            else
            {
                Log.i(TAG, "add_outgoing_file:no_need_to_shrink_file");
            }

            Log.i(TAG, "add_outgoing_file:001");

            // add FT message to UI
            GroupMessage m = new GroupMessage();
            m.is_new = false; // own messages are always "not new"
            m.tox_group_peer_pubkey = tox_group_self_get_public_key(
                    tox_group_by_groupid__wrapper(groupid)).toUpperCase();
            m.direction = 1; // msg sent
            m.TOX_MESSAGE_TYPE = 0;
            m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
            m.tox_group_peername = null;
            m.private_message = 0;
            m.group_identifier = groupid.toLowerCase();
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.sent_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
            m.text = ofw.filename_wrapped + "\n" + ofw.file_size_wrapped + " bytes";
            m.was_synced = false;
            m.TRIFA_SYNC_TYPE = TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NONE.value;
            m.path_name = ofw.filepath_wrapped;
            m.file_name = ofw.filename_wrapped;
            m.filename_fullpath = new java.io.File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).getAbsolutePath();
            m.storage_frame_work = false;
            try
            {
                m.filesize = new java.io.File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).length();
            }
            catch (Exception ee)
            {
                m.filesize = 0;
            }

            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(hash_bytes);
            m.msg_id_hash = bytebuffer_to_hexstring(hash_bytes, true);
            m.message_id_tox = "";
            insert_into_group_message_db(m, true);
            Log.i(TAG, "add_outgoing_file:090");

            // now send the file to the group as custom package ----------
            Log.i(TAG, "add_outgoing_file:091:send_group_image:start");
            send_group_image(m);
            Log.i(TAG, "add_outgoing_file:092:send_group_image:done");
            // now send the file to the group as custom package ----------
        }
        else
        {
            // HINT: should never get here, since ngc has max filesize of about 37kbytes only
        }
    }

    static void show_messagelist_for_id(Context c, String id, String fill_out_text)
    {
        Intent intent = new Intent(c, GroupMessageListActivity.class);
        if (fill_out_text != null)
        {
            intent.putExtra("fillouttext", fill_out_text);
        }
        intent.putExtra("group_id", id);
        c.startActivity(intent);
    }

    public void scroll_to_bottom(View v)
    {
        try
        {
            MainActivity.group_message_list_fragment.listingsView.scrollToPosition(
                    MainActivity.group_message_list_fragment.adapter.getItemCount() - 1);
        }
        catch (Exception ignored)
        {
        }

        GroupMessageListFragment.is_at_bottom = true;

        try
        {
            do_fade_anim_on_fab(MainActivity.group_message_list_fragment.unread_messages_notice_button, false,
                                this.getClass().getName());
            MainActivity.group_message_list_fragment.unread_messages_notice_button.setSupportBackgroundTintList(
                    (ContextCompat.getColorStateList(context_s, R.color.message_list_scroll_to_bottom_fab_bg_normal)));
        }
        catch (Exception ignored)
        {
        }
    }
}
