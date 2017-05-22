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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.FileNotFoundException;
import java.util.List;

import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendlistArrayAdapter extends ArrayAdapter<FriendList>
{
    private static final String TAG = "trifa.FriendListAA";
    private final Context context;
    private final List<FriendList> values;

    public FriendlistArrayAdapter(Context context, List<FriendList> values)
    {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    //    @Override
    //    public void setNotifyOnChange(boolean notifyOnChange)
    //    {
    //        super.setNotifyOnChange(notifyOnChange);
    //        Log.i(TAG, "setNotifyOnChange");
    //    }

    @Override
    public View getView(int position, View recycled, final ViewGroup parent)
    {
        // Log.i(TAG, "getView:fpubkey=" + values.get(position).tox_public_key_string);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_list_entry, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.f_name);
        textView.setText(values.get(position).name);

        TextView statusText = (TextView) rowView.findViewById(R.id.f_status_message);
        statusText.setText(values.get(position).status_message);

        TextView unread_count = (TextView) rowView.findViewById(R.id.f_unread_count);
        // Log.i(TAG, "unread_count view=" + unread_count);

        de.hdodenhof.circleimageview.CircleImageView avatar = (de.hdodenhof.circleimageview.CircleImageView) rowView.findViewById(R.id.f_avatar_icon);
        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        avatar.setImageDrawable(d_lock);

        final de.hdodenhof.circleimageview.CircleImageView avatar_f = avatar;
        final int position_f = position;
        try
        {
            // TODO: broken -------------------
            FriendList fl = orma.selectFromFriendList().tox_public_key_stringEq(values.get(position_f).tox_public_key_string).toList().get(0);

            try
            {
                info.guardianproject.iocipher.FileReader fr = new info.guardianproject.iocipher.FileReader(fl.avatar_pathname + "/" + fl.avatar_filename);
            }
            catch (FileNotFoundException e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "getView:EE2:" + e2.getMessage());
            }

            GlideApp.with(parent).
                    load("").
                    placeholder(d_lock).
                    into(avatar_f);
            // TODO: broken -------------------
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "getView:EE:" + e.getMessage());
        }

        try
        {
            int new_messages_count = orma.selectFromMessage().tox_friendpubkeyEq(values.get(position).tox_public_key_string).and().is_newEq(true).count();
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

        ImageView imageView = (ImageView) rowView.findViewById(R.id.f_status_icon);

        if (values.get(position).TOX_CONNECTION == 0)
        {
            imageView.setImageResource(R.drawable.circle_red);
        }
        else
        {
            imageView.setImageResource(R.drawable.circle_green);
        }

        ImageView imageView2 = (ImageView) rowView.findViewById(R.id.f_user_status_icon);

        if (values.get(position).TOX_USER_STATUS == 0)
        {
            imageView2.setImageResource(R.drawable.circle_green);
        }
        else if (values.get(position).TOX_USER_STATUS == 1)
        {
            imageView2.setImageResource(R.drawable.circle_orange);
        }
        else
        {
            imageView2.setImageResource(R.drawable.circle_red);
        }

        return rowView;
    }
}
