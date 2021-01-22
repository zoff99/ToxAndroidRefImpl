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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperConference.add_conference_wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_files;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_filetransfers;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_messages;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.HelperRelay.get_own_relay_pubkey;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.have_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.invite_to_all_conferences_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.send_all_friend_pubkeys_to_relay;
import static com.zoffcc.applications.trifa.HelperRelay.send_relay_pubkey_to_all_friends;
import static com.zoffcc.applications.trifa.HelperRelay.set_friend_as_own_relay_in_db;
import static com.zoffcc.applications.trifa.Identicon.create_avatar_identicon_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.cache_confid_confnum;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.friend_list_fragment;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_invite;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_new;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_delete;
import static com.zoffcc.applications.trifa.MainActivity.toxav_add_av_groupchat;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_ALPHA_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_SIZE_DP_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ONE_HOUR_IN_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_AV;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.FriendListHolder";

    private FriendList friendlist;
    private Context context;

    private TextView textView;
    private TextView statusText;
    private TextView unread_count;
    private de.hdodenhof.circleimageview.CircleImageView avatar;
    private ImageView f_status_icon;
    private ImageView f_user_status_icon;
    private ImageView f_notification;
    private ImageView f_relay_icon;
    private TextView f_last_online_timestamp;
    private ViewGroup friend_line_container;
    static ProgressDialog progressDialog = null;

    synchronized static void remove_progress_dialog()
    {
        try
        {
            if (progressDialog != null)
            {
                if (FriendListHolder.progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
            }

            progressDialog = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public FriendListHolder(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "FriendListHolder");

        this.context = c;

        textView = (TextView) itemView.findViewById(R.id.f_name);
        statusText = (TextView) itemView.findViewById(R.id.f_status_message);
        unread_count = (TextView) itemView.findViewById(R.id.f_unread_count);
        avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.f_avatar_icon);
        f_status_icon = (ImageView) itemView.findViewById(R.id.f_status_icon);
        f_user_status_icon = (ImageView) itemView.findViewById(R.id.f_user_status_icon);
        f_relay_icon = (ImageView) itemView.findViewById(R.id.f_relay_icon);
        f_notification = (ImageView) itemView.findViewById(R.id.f_notification);
        f_last_online_timestamp = (TextView) itemView.findViewById(R.id.f_last_online_timestamp);

        friend_line_container = (ViewGroup) itemView.findViewById(R.id.friend_line_container);
    }

    public void bindFriendList(FriendList fl)
    {
        if (fl == null)
        {
            textView.setText("*ERROR*");
            statusText.setText("fl == null");
            return;
        }

        // Log.i(TAG, "bindFriendList:" + fl.name + " alias=" + fl.alias_name);

        this.friendlist = fl;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        if (fl.last_online_timestamp == LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE)
        {
            friend_line_container.setBackgroundResource(R.drawable.friend_list_neveronline_round_bg);
        }
        else
        {
            if (fl.added_timestamp > (System.currentTimeMillis() - ONE_HOUR_IN_MS))
            {
                friend_line_container.setBackgroundColor(context.getResources().getColor(R.color.md_amber_700));
            }
            else
            {
                friend_line_container.setBackgroundResource(R.drawable.friend_list_round_bg);
            }
        }

        // Log.i(TAG, "lot=" + fl.last_online_timestamp + " -> " + LAST_ONLINE_TIMSTAMP_ONLINE_NOW);
        if (fl.last_online_timestamp_real == LAST_ONLINE_TIMSTAMP_ONLINE_NOW)
        {
            f_last_online_timestamp.setText("now");
        }
        else if (fl.last_online_timestamp_real == LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE)
        {
            f_last_online_timestamp.setText("never");
        }
        else if (fl.last_online_timestamp_real > LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE)
        {
            f_last_online_timestamp.setText("" + long_date_time_format(fl.last_online_timestamp_real));
        }
        else
        {
            f_last_online_timestamp.setText("");
        }

        if (fl.notification_silent)
        {
            final Drawable d_notification = new IconicsDrawable(context).
                    icon(GoogleMaterial.Icon.gmd_notifications_off).
                    color(context.getResources().
                            getColor(R.color.colorPrimaryDark)).
                    alpha(FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED).sizeDp(FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED);
            f_notification.setImageDrawable(d_notification);
            f_notification.setOnClickListener(this);
        }
        else
        {
            final Drawable d_notification = new IconicsDrawable(context).
                    icon(GoogleMaterial.Icon.gmd_notifications_active).
                    color(context.getResources().
                            getColor(R.color.colorPrimaryDark)).
                    alpha(FL_NOTIFICATION_ICON_ALPHA_SELECTED).sizeDp(FL_NOTIFICATION_ICON_SIZE_DP_SELECTED);
            f_notification.setImageDrawable(d_notification);
            f_notification.setOnClickListener(this);
        }

        final Drawable d_lock = new IconicsDrawable(context).
                icon(FontAwesome.Icon.faw_lock).color(context.getResources().
                getColor(R.color.colorPrimaryDark)).sizeDp(80);

        textView.setText(fl.name);
        try
        {
            if (fl.alias_name != null)
            {
                if (fl.alias_name.length() > 0)
                {
                    textView.setText(fl.alias_name);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        statusText.setText(fl.status_message);

        avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                boolean need_create_identicon = true;

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    if (f1.length() > 0)
                    {
                        // Log.i(TAG, "AVATAR_GLIDE:" + ":" + fl.name + ":" + fl.avatar_filename);
                        final RequestOptions glide_options = new RequestOptions().fitCenter();

                        //                        GlideApp.
                        //                                with(avatar.getContext()).
                        //                                load(new info.guardianproject.iocipher.FileInputStream(f1)).
                        //                                diskCacheStrategy(DiskCacheStrategy.NONE).
                        //                                placeholder(d_lock).
                        //                                priority(Priority.HIGH).
                        //                                skipMemoryCache(false).
                        //                                apply(glide_options).
                        //                                into(avatar);

                        GlideApp.
                                with(avatar.getContext()).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                placeholder(d_lock).
                                priority(Priority.HIGH).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(avatar);

                        need_create_identicon = false;
                    }
                    else
                    {
                        avatar.setImageDrawable(d_lock);
                    }
                }

                if (need_create_identicon)
                {
                    // no avatar icon? create and use Identicon ------------
                    Log.i(TAG, "indenticon:002");

                    create_avatar_identicon_for_pubkey(fl.tox_public_key_string);


                    // -- ok, now try to show the avtar icon again --

                    String new_avatar_pathname = VFS_PREFIX + VFS_FILE_DIR + "/" + fl.tox_public_key_string + "/";
                    String new_avatar_filename = FRIEND_AVATAR_FILENAME;
                    f1 = null;
                    try
                    {
                        f1 = new info.guardianproject.iocipher.File(new_avatar_pathname + "/" + new_avatar_filename);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if ((f1 != null) && (new_avatar_pathname != null))
                    {
                        if (f1.length() > 0)
                        {
                            // Log.i(TAG, "AVATAR_GLIDE:" + ":" + fl.name + ":" + new_avatar_filename);

                            final RequestOptions glide_options = new RequestOptions().fitCenter();
                            GlideApp.
                                    with(avatar.getContext()).
                                    load(f1).
                                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                    signature(new com.bumptech.glide.signature.StringSignatureZ(
                                            "_avatar_" + new_avatar_pathname + "/" + FRIEND_AVATAR_FILENAME + "_" +
                                            fl.avatar_update_timestamp)).
                                    placeholder(d_lock).
                                    priority(Priority.HIGH).
                                    skipMemoryCache(false).
                                    apply(glide_options).
                                    into(avatar);
                        }
                        else
                        {
                            // ok still nothing, show that default "lock" icon
                            avatar.setImageDrawable(d_lock);
                        }
                    }
                    // -- ok, now try to show the avtar icon again --

                    // no avatar icon? create and use Identicon ------------
                }


            } // VFS_ENCRYPT -- END --
            else
            {
                java.io.File f1 = null;
                try
                {
                    f1 = new java.io.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                    byte[] byteArray = new byte[(int) f1.length()];
                    fis.read(byteArray, 0, (int) f1.length());
                    fis.close();

                    final RequestOptions glide_options = new RequestOptions().fitCenter();
                    GlideApp.
                            with(context).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            signature(new com.bumptech.glide.signature.StringSignatureZ(
                                    "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                    fl.avatar_update_timestamp)).
                            skipMemoryCache(false).
                            apply(glide_options).
                            into(avatar);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        f_status_icon.setVisibility(View.VISIBLE);
        f_relay_icon.setVisibility(View.INVISIBLE);

        String relay_ = get_relay_for_friend(fl.tox_public_key_string);

        // Log.d(TAG, "001:relay=" + relay_);
        if (relay_ != null)
        {
            long fnum_ = tox_friend_by_public_key__wrapper(relay_);

            FriendList relay_fl = main_get_friend(tox_friend_by_public_key__wrapper(relay_));
            // Log.d(TAG, "002 relay_fl=" + relay_fl + " fnum=" + fnum_);
            if (relay_fl != null)
            {
                if (fl.TOX_CONNECTION_real == 0)
                {
                    f_status_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    f_status_icon.setImageResource(R.drawable.circle_green);
                }
                // Log.d(TAG, "003 relay_fl=" + relay_fl);

                if (fl.TOX_CONNECTION == 0)
                {
                    f_relay_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    f_relay_icon.setImageResource(R.drawable.circle_green);
                }

                f_status_icon.setVisibility(View.VISIBLE);
                f_relay_icon.setVisibility(View.VISIBLE);
            }
        }
        else // friend has no relay
        {
            // Log.d(TAG, "004");

            if (fl.TOX_CONNECTION == 0)
            {
                f_status_icon.setImageResource(R.drawable.circle_red);
            }
            else
            {
                f_status_icon.setImageResource(R.drawable.circle_green);
            }

        }


        if (fl.TOX_USER_STATUS == 0)
        {
            f_user_status_icon.setImageResource(R.drawable.circle_green);
        }
        else if (fl.TOX_USER_STATUS == 1)
        {
            f_user_status_icon.setImageResource(R.drawable.circle_orange);
        }
        else
        {
            f_user_status_icon.setImageResource(R.drawable.circle_red);
        }

        try
        {
            if (fl.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
            {
                avatar.setBorderColor(Color.parseColor("#40000000"));
            }
            else if (fl.TOX_CONNECTION_real == TOX_CONNECTION_TCP.value)
            {
                avatar.setBorderColor(Color.parseColor("#FFCE00"));
            }
            else // UDP
            {
                avatar.setBorderColor(Color.parseColor("#04B431"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            int new_messages_count = orma.selectFromMessage().tox_friendpubkeyEq(
                    fl.tox_public_key_string).and().is_newEq(true).count();
            if (new_messages_count > 0)
            {
                if (new_messages_count > 99)
                {
                    unread_count.setText("+"); //("∞");
                }
                else
                {
                    unread_count.setText("" + new_messages_count);
                }
                unread_count.setVisibility(View.VISIBLE);
            }
            else
            {
                unread_count.setText("");
                unread_count.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            unread_count.setText("");
            unread_count.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
            if (v.equals(f_notification))
            {
                if (!this.friendlist.notification_silent)
                {
                    this.friendlist.notification_silent = true;
                    orma.updateFriendList().tox_public_key_stringEq(this.friendlist.tox_public_key_string).
                            notification_silent(this.friendlist.notification_silent).execute();

                    final Drawable d_notification = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_notifications_off).
                            color(context.getResources().
                                    getColor(R.color.colorPrimaryDark)).
                            alpha(FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED).sizeDp(
                            FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED);
                    f_notification.setImageDrawable(d_notification);

                    try
                    {
                        if (friend_list_fragment != null)
                        {
                            // TODO: dirty hack, make better
                            final boolean sorted_reload = true;
                            if (!sorted_reload)
                            {
                                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                                cc.is_friend = true;
                                cc.friend_item = this.friendlist;
                                friend_list_fragment.modify_friend(cc, cc.is_friend);
                            }
                            else
                            {
                                friend_list_fragment.add_all_friends_clear(0);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    this.friendlist.notification_silent = false;
                    orma.updateFriendList().tox_public_key_stringEq(this.friendlist.tox_public_key_string).
                            notification_silent(this.friendlist.notification_silent).execute();

                    final Drawable d_notification = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_notifications_active).
                            color(context.getResources().
                                    getColor(R.color.colorPrimaryDark)).
                            alpha(FL_NOTIFICATION_ICON_ALPHA_SELECTED).sizeDp(FL_NOTIFICATION_ICON_SIZE_DP_SELECTED);
                    f_notification.setImageDrawable(d_notification);

                    try
                    {
                        if (friend_list_fragment != null)
                        {
                            // TODO: dirty hack, make better
                            final boolean sorted_reload = true;
                            if (!sorted_reload)
                            {
                                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                                cc.is_friend = true;
                                cc.friend_item = this.friendlist;
                                friend_list_fragment.modify_friend(cc, cc.is_friend);
                            }
                            else
                            {
                                friend_list_fragment.add_all_friends_clear(0);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                try
                {
                    if (progressDialog == null)
                    {
                        progressDialog = new ProgressDialog(this.context);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    }
                    progressDialog.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Intent intent = new Intent(v.getContext(), MessageListActivity.class);
                intent.putExtra("friendnum", tox_friend_by_public_key__wrapper(this.friendlist.tox_public_key_string));
                v.getContext().startActivity(intent);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onClick:EE:" + e.getMessage());
        }
    }


    public void show_confirm_dialog(final View view, final FriendList f2)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete Friend?");
        builder.setMessage("Do you want to delete this Friend including all Messages and Files?");

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long friend_num_temp = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);

                            Log.i(TAG, "onMenuItemClick:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp);

                            // delete friends files -------
                            Log.i(TAG, "onMenuItemClick:1.c:fnum=" + friend_num_temp);
                            delete_friend_all_files(friend_num_temp);
                            // delete friend  files -------

                            // delete friends FTs -------
                            Log.i(TAG, "onMenuItemClick:1.d:fnum=" + friend_num_temp);
                            delete_friend_all_filetransfers(friend_num_temp);
                            // delete friend  FTs -------

                            // delete friends messages -------
                            Log.i(TAG, "onMenuItemClick:1.b:fnum=" + friend_num_temp);
                            delete_friend_all_messages(friend_num_temp);
                            // delete friend  messages -------

                            // delete friend -------
                            // Log.i(TAG, "onMenuItemClick:1.a:pubkey=" + f2.tox_public_key_string);
                            delete_friend(f2.tox_public_key_string);
                            // delete friend -------

                            // delete friend - tox ----
                            Log.i(TAG, "onMenuItemClick:4");
                            if (friend_num_temp > -1)
                            {
                                int res = tox_friend_delete(friend_num_temp);
                                cache_pubkey_fnum.clear();
                                cache_fnum_pubkey.clear();
                                update_savedata_file_wrapper(); // save toxcore datafile (friend removed)
                                Log.i(TAG, "onMenuItemClick:5:res=" + res);
                            }
                            // delete friend - tox ----

                            // load all friends into data list ---
                            Log.i(TAG, "onMenuItemClick:6");
                            try
                            {
                                if (friend_list_fragment != null)
                                {
                                    // reload friendlist
                                    // TODO: only remove 1 item, don't clear all!! this can crash
                                    friend_list_fragment.add_all_friends_clear(200);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            Log.i(TAG, "onMenuItemClick:7");
                            // load all friends into data list ---
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                        }
                    }
                };
                // TODO: use own handler
                if (view.getHandler() != null)
                {
                    view.getHandler().post(myRunnable);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void show_confirm_addrelay_dialog(final View view, final FriendList f2)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("add as Relay?");
        builder.setMessage("Do you want to add this Friend as your Relay?");

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // long friend_num_temp = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);
                            if (set_friend_as_own_relay_in_db(f2.tox_public_key_string))
                            {
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

                                Log.i(TAG, "onMenuItemClick:7");
                                // load all friends into data list ---
                            }

                            send_all_friend_pubkeys_to_relay(f2.tox_public_key_string);
                            send_relay_pubkey_to_all_friends(f2.tox_public_key_string);
                            invite_to_all_conferences_own_relay(f2.tox_public_key_string);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                        }
                    }
                };
                // TODO: use own handler
                if (view.getHandler() != null)
                {
                    view.getHandler().post(myRunnable);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onLongClick(final View v)
    {
        Log.i(TAG, "onLongClick");

        final FriendList f2 = this.friendlist;

        PopupMenu menu = new PopupMenu(v.getContext(), v);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                int id = item.getItemId();
                switch (id)
                {
                    case R.id.item_info:
                        // show friend info page -----------------
                        long friend_num_temp_safety = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);

                        Log.i(TAG, "onMenuItemClick:info:1:fn_safety=" + friend_num_temp_safety);

                        Intent intent = new Intent(v.getContext(), FriendInfoActivity.class);
                        intent.putExtra("friendnum", friend_num_temp_safety);
                        v.getContext().startActivity(intent);
                        // show friend info page -----------------
                        break;

                    case R.id.item_create_conference:
                        int res_conf_new = tox_conference_new();
                        if (res_conf_new >= 0)
                        {
                            cache_confid_confnum.clear();

                            // conference was created, now invite the selected friend
                            long friend_num_temp_safety2 = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);
                            if (friend_num_temp_safety2 > 0)
                            {
                                int res_conf_invite = tox_conference_invite(friend_num_temp_safety2, res_conf_new);
                                if (res_conf_invite < 1)
                                {
                                    Log.d(TAG, "onMenuItemClick:info:tox_conference_invite:ERR:" + res_conf_invite);
                                }
                                else
                                {
                                    // invite also my ToxProxy -------------
                                    if (have_own_relay())
                                    {
                                        tox_conference_invite(tox_friend_by_public_key__wrapper(get_own_relay_pubkey()),
                                                              res_conf_new);
                                    }
                                    // invite also my ToxProxy -------------
                                    add_conference_wrapper(friend_num_temp_safety2, res_conf_new, "",
                                                           TOX_CONFERENCE_TYPE_TEXT.value, false);
                                    HelperGeneric.update_savedata_file_wrapper();
                                }
                            }
                        }
                        break;
                    case R.id.item_create_av_conference:
                        long res_conf_av_new = toxav_add_av_groupchat();
                        if (res_conf_av_new >= 0)
                        {
                            update_savedata_file_wrapper();
                            // conference was created, now invite the selected friend
                            long friend_num_temp_safety2 = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);
                            if (friend_num_temp_safety2 > 0)
                            {
                                int res_conf_invite = tox_conference_invite(friend_num_temp_safety2, res_conf_av_new);
                                if (res_conf_invite < 1)
                                {
                                    Log.d(TAG, "onMenuItemClick:info:AV:tox_conference_invite:ERR:" + res_conf_invite);
                                }
                                else
                                {
                                    update_savedata_file_wrapper();
                                    add_conference_wrapper(friend_num_temp_safety2, res_conf_av_new, "",
                                                           TOX_CONFERENCE_TYPE_AV.value, false);
                                    HelperGeneric.update_savedata_file_wrapper();
                                }
                            }
                        }
                        break;
                    case R.id.item_add_toxproxy:
                        if (!have_own_relay())
                        {
                            show_confirm_addrelay_dialog(v, f2);
                            // add as ToxProxy relay -----------------
                        }
                        break;
                    case R.id.item_dummy01:
                        break;
                    case R.id.item_delete:
                        // delete friend -----------------
                        show_confirm_dialog(v, f2);
                        // delete friend -----------------
                        break;
                }
                return true;
            }
        });

        menu.inflate(R.menu.menu_friendlist_item);
        MenuItem add_toxproxy_item = menu.getMenu().findItem(R.id.item_add_toxproxy);
        if (have_own_relay())
        {
            add_toxproxy_item.setVisible(false);
        }
        else
        {
            add_toxproxy_item.setVisible(true);
        }
        menu.show();

        return true;
    }
}
