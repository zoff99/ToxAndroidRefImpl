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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;

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

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
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

        final Drawable d_lock = new IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_group).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        avatar.setImageDrawable(d_lock);

        try
        {
            textView.setText("#" + fl.tox_conference_number + " " + fl.conference_identifier.substring(fl.conference_identifier.length() - 7, fl.conference_identifier.length()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            textView.setText("#" + fl.tox_conference_number);
        }

        if (fl.conference_active)
        {
            imageView.setImageResource(R.drawable.circle_green);
        }
        else
        {
            imageView.setImageResource(R.drawable.circle_red);
        }

        if (fl.conference_active)
        {
            statusText.setText("* ACTIVE *");
        }
        else
        {
            statusText.setText("_inactive_");
        }

        unread_count.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
            Intent intent = new Intent(v.getContext(), ConferenceMessageListActivity.class);
            intent.putExtra("conf_id", this.conference.conference_identifier);
            v.getContext().startActivity(intent);
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
        return true;
    }
}
