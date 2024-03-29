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
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.MainActivity.selected_conference_messages;

public class ConferenceMessageListHolder_error extends RecyclerView.ViewHolder
{
    private static final String TAG = "trifa.MessageListHolder";

    private ConferenceMessage message_;
    private Context context;
    EmojiTextViewLinks textView;
    ViewGroup layout_message_container;
    boolean is_selected = false;

    public ConferenceMessageListHolder_error(View itemView, Context c)
    {
        super(itemView);
        Log.i(TAG, "MessageListHolder");
        this.context = c;

        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        textView.addAutoLinkMode(AutoLinkMode.MODE_URL);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
    }

    public void bindMessageList(ConferenceMessage m)
    {
        // Log.i(TAG, "bindMessageList");
        message_ = m;

        is_selected = false;
        if (selected_conference_messages.isEmpty())
        {
            is_selected = false;
        }
        else
        {
            is_selected = selected_conference_messages.contains(m.id);
        }

        if (is_selected)
        {
            layout_message_container.setBackgroundColor(Color.GRAY);
        }
        else
        {
            layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        }

        layout_message_container.setOnClickListener(onclick_listener);
        layout_message_container.setOnLongClickListener(onlongclick_listener);
    }

    private View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            is_selected = ConferenceMessageListActivity.onClick_message_helper(v, is_selected, message_);
        }
    };

    private View.OnLongClickListener onlongclick_listener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(final View v)
        {
            ConferenceMessageListActivity.long_click_message_return res = ConferenceMessageListActivity.onLongClick_message_helper(
                    context, v, is_selected, message_);
            is_selected = res.is_selected;
            return res.ret_value;
        }
    };
}
