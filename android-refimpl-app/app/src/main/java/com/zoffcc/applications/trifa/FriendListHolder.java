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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.StringSignature2;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_files;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_filetransfers;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_messages;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_delete;
import static com.zoffcc.applications.trifa.MainActivity.update_savedata_file;
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
    private ImageView imageView;
    private ImageView imageView2;

    public FriendListHolder(View itemView, Context c)
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

    public void bindFriendList(FriendList fl)
    {
        if (fl == null)
        {
            textView.setText("*ERROR*");
            statusText.setText("fl == null");
            return;
        }

        Log.i(TAG, "bindFriendList:" + fl.name);

        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);

        this.friendlist = fl;

        textView.setText(fl.name);
        statusText.setText(fl.status_message);

        avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
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
                    // info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                    if (f1.length() > 0)
                    {
                        //                        byte[] byteArray = new byte[(int) f1.length()];
                        //                        fis.read(byteArray, 0, (int) f1.length());
                        //                        fis.close();
                        Log.i(TAG, "AVATAR_GLIDE:" + ":" + fl.name + ":" + fl.avatar_filename);

                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(avatar.getContext()).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                placeholder(d_lock).
                                priority(Priority.HIGH).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(avatar);

                        //                            GlideApp.
                        //                                    with(context).
                        //                                    load(byteArray).
                        //                                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        //                                    signature(StringSignature2(fl.avatar_pathname + "/" + fl.avatar_filename)).
                        //                                    placeholder(d_lock).
                        //                                    skipMemoryCache(false).
                        //                                    apply(glide_options).
                        //                                    into(avatar);
                    }
                }
            }
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
                            signature(StringSignature2(fl.avatar_pathname + "/" + fl.avatar_filename)).
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


        if (fl.TOX_CONNECTION == 0)
        {
            imageView.setImageResource(R.drawable.circle_red);
        }
        else
        {
            imageView.setImageResource(R.drawable.circle_green);
        }

        if (fl.TOX_USER_STATUS == 0)
        {
            imageView2.setImageResource(R.drawable.circle_green);
        }
        else if (fl.TOX_USER_STATUS == 1)
        {
            imageView2.setImageResource(R.drawable.circle_orange);
        }
        else
        {
            imageView2.setImageResource(R.drawable.circle_red);
        }

        try
        {
            int new_messages_count = orma.selectFromMessage().tox_friendpubkeyEq(fl.tox_public_key_string).and().is_newEq(true).count();
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
            Intent intent = new Intent(v.getContext(), MessageListActivity.class);
            intent.putExtra("friendnum", tox_friend_by_public_key__wrapper(this.friendlist.tox_public_key_string));
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
                    case R.id.item_delete:
                        // delete friend -----------------
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    long friend_num_temp = tox_friend_by_public_key__wrapper(f2.tox_public_key_string);

                                    Log.i(TAG, "onMenuItemClick:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp);

                                    // delete friend -------
                                    Log.i(TAG, "onMenuItemClick:1.a:pubkey=" + f2.tox_public_key_string);
                                    delete_friend(f2.tox_public_key_string);
                                    // delete friend -------

                                    // delete friends messages -------
                                    Log.i(TAG, "onMenuItemClick:1.b:fnum=" + friend_num_temp);
                                    delete_friend_all_messages(friend_num_temp);
                                    // delete friend  messages -------

                                    // delete friends files -------
                                    Log.i(TAG, "onMenuItemClick:1.c:fnum=" + friend_num_temp);
                                    delete_friend_all_files(friend_num_temp);
                                    // delete friend  files -------

                                    // delete friends FTs -------
                                    Log.i(TAG, "onMenuItemClick:1.d:fnum=" + friend_num_temp);
                                    delete_friend_all_filetransfers(friend_num_temp);
                                    // delete friend  FTs -------


                                    // delete friend - tox ----
                                    Log.i(TAG, "onMenuItemClick:4");
                                    if (friend_num_temp > -1)
                                    {
                                        int res = tox_friend_delete(friend_num_temp);
                                        cache_pubkey_fnum.clear();
                                        cache_fnum_pubkey.clear();
                                        update_savedata_file(); // save toxcore datafile (friend removed)
                                        Log.i(TAG, "onMenuItemClick:5:res=" + res);
                                    }
                                    // delete friend - tox ----

                                    // load all friends into data list ---
                                    Log.i(TAG, "onMenuItemClick:6");
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
                        if (main_handler_s != null)
                        {
                            main_handler_s.post(myRunnable);
                        }
                        // delete friend -----------------
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_friendlist_item);
        menu.show();

        return true;
    }
}
