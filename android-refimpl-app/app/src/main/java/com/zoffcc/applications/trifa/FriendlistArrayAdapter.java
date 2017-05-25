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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
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
        Log.i(TAG, "getView:fpubkey=" + values.get(position).tox_public_key_string);
        Log.i(TAG, "getView:avatar_filename=" + values.get(position).avatar_filename);
        Log.i(TAG, "getView:avatar_pathname=" + values.get(position).avatar_pathname);

        if ((values!=null) &&(values.size()>=position))
        {
            Log.i(TAG, "getView:data=" + values.get(position));
        }
        else
        {
            Log.i(TAG, "getView:data=" + "*NULL*");
        }


        Log.i(TAG, "getView:001");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_list_entry, parent, false);

        Log.i(TAG, "getView:002");

        TextView textView = (TextView) rowView.findViewById(R.id.f_name);
        textView.setText(values.get(position).name);

        TextView statusText = (TextView) rowView.findViewById(R.id.f_status_message);
        statusText.setText(values.get(position).status_message);

        TextView unread_count = (TextView) rowView.findViewById(R.id.f_unread_count);

        Log.i(TAG, "getView:003");

        de.hdodenhof.circleimageview.CircleImageView avatar = (de.hdodenhof.circleimageview.CircleImageView) rowView.findViewById(R.id.f_avatar_icon);
        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        avatar.setImageDrawable(d_lock);

        Log.i(TAG, "getView:004");

        try
        {
            if (VFS_ENCRYPT)
            {
                Log.i(TAG, "getView:005");

                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(values.get(position).avatar_pathname + "/" + values.get(position).avatar_filename);
                if ((f1 != null) && (values.get(position).avatar_pathname != null))
                {
                    Log.i(TAG, "getView:f1=" + f1);
                    info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                    byte[] byteArray = new byte[(int) f1.length()];
                    fis.read(byteArray, 0, (int) f1.length());

                    GlideApp.
                            with(parent).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(false).
                            into(avatar);
                }

                Log.i(TAG, "getView:006");

            }
            else
            {
                java.io.File f1 = new java.io.File(values.get(position).avatar_pathname + "/" + values.get(position).avatar_filename);
                if ((f1 != null) && (values.get(position).avatar_pathname != null))
                {
                    Log.i(TAG, "getView:f1=" + f1);
                    java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                    byte[] byteArray = new byte[(int) f1.length()];
                    fis.read(byteArray, 0, (int) f1.length());

                    GlideApp.
                            with(parent).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(false).
                            into(avatar);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG,"getView:EE1:"+e.getMessage());
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
