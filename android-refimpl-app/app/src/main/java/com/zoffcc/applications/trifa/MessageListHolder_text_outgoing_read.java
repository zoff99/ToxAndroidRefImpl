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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_own_avatar;

public class MessageListHolder_text_outgoing_read extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message;
    private Context context;

    com.vanniktech.emoji.EmojiTextView textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    ImageView img_corner;
    LinearLayout text_block_group;

    public MessageListHolder_text_outgoing_read(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView = (com.vanniktech.emoji.EmojiTextView) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        img_corner = (ImageView) itemView.findViewById(R.id.img_corner);
        text_block_group = (LinearLayout) itemView.findViewById(R.id.text_block_group);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        // textView.setText("#" + m.id + ":" + m.text);
        textView.setText(m.text);
        if (!m.read)
        {
            // not yet read
            imageView.setImageResource(R.drawable.circle_red);
        }
        else
        {
            // msg read by other party
            imageView.setImageResource(R.drawable.circle_green);
        }


        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                String fname = get_vfs_image_filename_own_avatar();

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fname);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fname != null))
                {
                    //info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                    if (f1.length() > 0)
                    {
                        // byte[] byteArray = new byte[(int) f1.length()];
                        // fis.read(byteArray, 0, (int) f1.length());
                        // fis.close();

                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(context).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
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

        final Message m2 = this.message;

        //        PopupMenu menu = new PopupMenu(v.getContext(), v);
        //        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        //        {
        //            @Override
        //            public boolean onMenuItemClick(MenuItem item)
        //            {
        //                int id = item.getItemId();
        //                return true;
        //            }
        //        });
        //        menu.inflate(R.menu.menu_friendlist_item);
        //        menu.show();

        return true;
    }
}
