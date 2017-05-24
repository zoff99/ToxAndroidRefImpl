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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;

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
    synchronized public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = null;

        try
        {
            if (values.get(position).TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
            {
                if (values.get(position).direction == 0)
                {
                    // incoming file
                    rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);


                    ImageButton button_ok = (ImageButton) rowView.findViewById(R.id.ft_button_ok);
                    final Drawable d1 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
                    button_ok.setImageDrawable(d1);

                    ImageButton button_cancel = (ImageButton) rowView.findViewById(R.id.ft_button_cancel);
                    final Drawable d2 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
                    button_cancel.setImageDrawable(d2);

                    TextView textView = (TextView) rowView.findViewById(R.id.m_text);

                    // TODO: maybe make the FT text here?
                    textView.setText("" + values.get(position).text);

                    ProgressBar ft_progressbar = (ProgressBar) rowView.findViewById(R.id.ft_progressbar);
                    if (values.get(position).state == TOX_FILE_CONTROL_CANCEL.value)
                    {
                        ft_progressbar.setVisibility(View.GONE);
                        textView.setText("" + values.get(position).text + "\n*canceled*");
                    }
                    else
                    {
                        ft_progressbar.setIndeterminate(true);
                        ft_progressbar.setVisibility(View.VISIBLE);
                    }

                }
                else
                {
                    // outgoing file
                }
            }
            else
            {

                if (values.get(position).direction == 0)
                {
                    // msg to me
                    if (values.get(position).read)
                    {
                        rowView = inflater.inflate(R.layout.message_list_entry_read, parent, false);
                    }
                    else
                    {
                        rowView = inflater.inflate(R.layout.message_list_entry, parent, false);
                    }
                }
                else
                {
                    // msg from me
                    if (values.get(position).read)
                    {
                        rowView = inflater.inflate(R.layout.message_list_self_entry_read, parent, false);
                    }
                    else
                    {
                        rowView = inflater.inflate(R.layout.message_list_self_entry, parent, false);
                    }
                }

                TextView textView = (TextView) rowView.findViewById(R.id.m_text);
                textView.setText("#" + values.get(position).id + ":" + values.get(position).text);

                ImageView imageView = (ImageView) rowView.findViewById(R.id.m_icon);

                if (!values.get(position).read)
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

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return rowView;
    }
}
