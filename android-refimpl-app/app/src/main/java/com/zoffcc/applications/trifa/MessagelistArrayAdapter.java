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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MessagelistArrayAdapter extends ArrayAdapter<Message>
{
    private final Context context;
    private final List<Message> values;

    public MessagelistArrayAdapter(Context context, List<Message> values)
    {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = null;
        if (values.get(position).direction == 0)
        {
            // msg to me
            rowView = inflater.inflate(R.layout.message_list_entry, parent, false);
        }
        else
        {
            // msg from me
            rowView = inflater.inflate(R.layout.message_list_self_entry, parent, false);
        }

        TextView textView = (TextView) rowView.findViewById(R.id.m_text);
        textView.setText("#" + values.get(position).id + ":" + values.get(position).text);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.m_icon);

        if (values.get(position).direction == 0)
        {
            // msg to me
            imageView.setImageResource(R.drawable.circle_red);
        }
        else
        {
            // msg from me
            imageView.setImageResource(R.drawable.circle_green);
        }

        return rowView;
    }
}
