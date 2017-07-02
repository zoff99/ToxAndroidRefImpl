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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListHolder_file_outgoing_state_pause_has_accepted extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message2;
    private Context context;

    TextView textView;
    ImageView imageView;

    public MessageListHolder_file_outgoing_state_pause_has_accepted(View itemView, Context c)
    {
        super(itemView);

        Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView = (TextView) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindMessageList(Message m)
    {
        Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        textView.setText("#" + m.id + ":" + m.text);
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

        // final Message m2 = this.message;

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
