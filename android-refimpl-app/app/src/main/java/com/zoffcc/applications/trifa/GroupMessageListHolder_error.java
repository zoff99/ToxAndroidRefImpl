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

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.MainActivity.selected_messages;

public class GroupMessageListHolder_error extends RecyclerView.ViewHolder
{
    private static final String TAG = "trifa.MessageListHolder";

    private GroupMessage message_;
    private Context context;
    EmojiTextViewLinks textView;
    ViewGroup layout_message_container;
    boolean is_selected = false;

    public GroupMessageListHolder_error(View itemView, Context c)
    {
        super(itemView);
        Log.i(TAG, "MessageListHolder");
        this.context = c;

        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        textView.addAutoLinkMode(AutoLinkMode.MODE_URL);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
    }

    public void bindMessageList(GroupMessage m)
    {
        // Log.i(TAG, "bindMessageList");
        message_ = m;

        is_selected = false;
        if (selected_messages.isEmpty())
        {
            is_selected = false;
        }

        layout_message_container.setOnClickListener(onclick_listener);
        layout_message_container.setOnLongClickListener(onlongclick_listener);
    }

    private View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            // is_selected = GroupMessageListActivity.onClick_message_helper(v, is_selected, message_);
        }
    };

    private View.OnLongClickListener onlongclick_listener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(final View v)
        {
            // GroupMessageListActivity.long_click_message_return res = GroupMessageListActivity.onLongClick_message_helper(context, v, is_selected, message_);
            // is_selected = res.is_selected;
            // return res.ret_value;
            return false;
        }
    };
}
