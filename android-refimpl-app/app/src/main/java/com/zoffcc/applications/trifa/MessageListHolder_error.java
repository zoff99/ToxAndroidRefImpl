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

public class MessageListHolder_error extends RecyclerView.ViewHolder
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message;
    private Context context;

    public MessageListHolder_error(View itemView, Context c)
    {
        super(itemView);
        Log.i(TAG, "MessageListHolder");
        this.context = c;
    }

    public void bindMessageList(Message m)
    {
        Log.i(TAG, "bindMessageList");
    }
}
