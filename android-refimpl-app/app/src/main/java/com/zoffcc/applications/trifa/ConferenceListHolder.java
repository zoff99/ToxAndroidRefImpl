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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.conference_identifier_short;
import static com.zoffcc.applications.trifa.MainActivity.delete_conference;
import static com.zoffcc.applications.trifa.MainActivity.delete_conference_all_messages;
import static com.zoffcc.applications.trifa.MainActivity.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.set_conference_inactive;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_delete;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_count;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_ALPHA_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FL_NOTIFICATION_ICON_SIZE_DP_SELECTED;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceListHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.ConferenceLstHldr";

    private ConferenceDB conference;
    private Context context;

    private TextView textView;
    private TextView statusText;
    private TextView unread_count;
    private de.hdodenhof.circleimageview.CircleImageView avatar;
    private ImageView imageView;
    private ImageView imageView2;
    private ImageView f_notification;

    public ConferenceListHolder(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "FriendListHolder");

        this.context = c;

        textView = (TextView) itemView.findViewById(R.id.f_name);
        statusText = (TextView) itemView.findViewById(R.id.f_status_message);
        unread_count = (TextView) itemView.findViewById(R.id.f_unread_count);
        avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.f_avatar_icon);
        imageView = (ImageView) itemView.findViewById(R.id.f_status_icon);
        imageView2 = (ImageView) itemView.findViewById(R.id.f_user_status_icon);
        f_notification = (ImageView) itemView.findViewById(R.id.f_notification);
    }

    public void bindFriendList(ConferenceDB fl)
    {
        if (fl == null)
        {
            textView.setText("*ERROR*");
            statusText.setText("fl == null");
            return;
        }

        Log.i(TAG, "bindFriendList:" + fl.tox_conference_number);

        this.conference = fl;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

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

        final Drawable d_lock = new IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_group).
                color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        avatar.setImageDrawable(d_lock);

        try
        {
            if (fl.conference_active)
            {
                long user_count = tox_conference_peer_count(fl.tox_conference_number);

                if (user_count < 0)
                {
                    user_count = 0;
                }

                statusText.setText(Html.fromHtml("#" + fl.tox_conference_number + " "
                        //
                        + conference_identifier_short(fl.conference_identifier, true)
                        //
                        + " " + "<b><font color=\"#000000\">Users:" + user_count + "</font></b>"));
            }
            else
            {
                statusText.setText("#" + fl.tox_conference_number + " "
                                //
                                + conference_identifier_short(fl.conference_identifier, true)
                        //
                );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            statusText.setText("#" + fl.tox_conference_number);
        }

        if (fl.conference_active)
        {
            imageView.setImageResource(R.drawable.circle_green);
        }
        else
        {
            imageView.setImageResource(R.drawable.circle_red);
        }

        // use this field as "conference title"
        textView.setText(get_conference_title_from_confid(fl.conference_identifier));

        imageView2.setVisibility(View.INVISIBLE);

        try
        {
            int new_messages_count = orma.selectFromConferenceMessage().
                    conference_identifierEq(fl.conference_identifier).and().is_newEq(true).count();

            if (new_messages_count > 0)
            {
                if (new_messages_count > 300)
                {
                    unread_count.setText("+");
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
                if (!this.conference.notification_silent)
                {
                    this.conference.notification_silent = true;
                    orma.updateConferenceDB().conference_identifierEq(this.conference.conference_identifier).
                            notification_silent(this.conference.notification_silent).execute();

                    final Drawable d_notification = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_notifications_off).
                            color(context.getResources().
                                    getColor(R.color.colorPrimaryDark)).
                            alpha(FL_NOTIFICATION_ICON_ALPHA_NOT_SELECTED).sizeDp(FL_NOTIFICATION_ICON_SIZE_DP_NOT_SELECTED);
                    f_notification.setImageDrawable(d_notification);
                }
                else
                {
                    this.conference.notification_silent = false;
                    orma.updateConferenceDB().conference_identifierEq(this.conference.conference_identifier).
                            notification_silent(this.conference.notification_silent).execute();

                    final Drawable d_notification = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_notifications_active).
                            color(context.getResources().
                                    getColor(R.color.colorPrimaryDark)).
                            alpha(FL_NOTIFICATION_ICON_ALPHA_SELECTED).sizeDp(FL_NOTIFICATION_ICON_SIZE_DP_SELECTED);
                    f_notification.setImageDrawable(d_notification);
                }
            }
            else
            {
                Intent intent = new Intent(v.getContext(), ConferenceMessageListActivity.class);
                intent.putExtra("conf_id", this.conference.conference_identifier);
                v.getContext().startActivity(intent);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onClick:EE:" + e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(final View v)
    {
        Log.i(TAG, "onLongClick");

        final ConferenceDB f2 = this.conference;

        PopupMenu menu = new PopupMenu(v.getContext(), v);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                int id = item.getItemId();
                switch (id)
                {
                    //                    case R.id.item_info:
                    //                        // show conference info page -----------------
                    //                        long friend_num_temp_safety = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);
                    //
                    //                        Log.i(TAG, "onMenuItemClick:info:1:fn_safety=" + friend_num_temp_safety);
                    //
                    //                        Intent intent = new Intent(v.getContext(), FriendInfoActivity.class);
                    //                        intent.putExtra("friendnum", friend_num_temp_safety);
                    //                        v.getContext().startActivity(intent);
                    //                        // show conference info page -----------------
                    //                        break;
                    case R.id.item_leave:
                        // leave conference -----------------
                        if ((f2.tox_conference_number > -1) && (f2.conference_active))
                        {
                            tox_conference_delete(f2.tox_conference_number);
                        }

                        set_conference_inactive(f2.conference_identifier);

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    try
                                    {
                                        if (MainActivity.friend_list_fragment != null)
                                        {
                                            // reload friendlist
                                            // TODO: only remove 1 item, don't clear all!! this can crash
                                            MainActivity.friend_list_fragment.add_all_friends_clear(200);
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
                                    Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                                }
                            }
                        };

                        // TODO: use own handler
                        if (main_handler_s != null)
                        {
                            main_handler_s.post(myRunnable);
                        }
                        // leave conference -----------------
                        break;
                    case R.id.item_delete:
                        // delete conference -----------------

                        if ((f2.tox_conference_number > -1) && (f2.conference_active))
                        {
                            tox_conference_delete(f2.tox_conference_number);
                        }

                        Log.i(TAG, "onMenuItemClick:info:33");
                        delete_conference_all_messages(f2.conference_identifier);
                        delete_conference(f2.conference_identifier);
                        Log.i(TAG, "onMenuItemClick:info:34");

                        set_conference_inactive(f2.conference_identifier);

                        Runnable myRunnable2 = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    try
                                    {
                                        if (MainActivity.friend_list_fragment != null)
                                        {
                                            // reload friendlist
                                            // TODO: only remove 1 item, don't clear all!! this can crash
                                            MainActivity.friend_list_fragment.add_all_friends_clear(200);
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
                                    Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                                }
                            }
                        };

                        // TODO: use own handler
                        if (main_handler_s != null)
                        {
                            main_handler_s.post(myRunnable2);
                        }
                        // delete conference -----------------
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_conferencelist_item);

        try
        {
            if ((f2.tox_conference_number == -1) || (!f2.conference_active))
            {
                menu.getMenu().findItem(R.id.item_leave).setVisible(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        menu.show();

        return true;

    }
}
