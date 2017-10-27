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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.l4digital.fastscroll.FastScroller;

import java.util.Iterator;
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.only_date_time_format;

public class ConferenceMessagelistAdapter extends RecyclerView.Adapter implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.CnfMesgelistAdptr";

    private final List<ConferenceMessage> messagelistitems;
    private Context context;

    private ConferenceMessage getSectionText_message_object = null;
    private ConferenceMessage getSectionText_message_object2 = null;
    long getSectionText_message_object_ts = -1L;
    long getSectionText_message_object_ts2 = -1L;
    String getSectionText_message_object_ts_string = " ";
    String getSectionText_message_object_ts_string2 = " ";


    public ConferenceMessagelistAdapter(Context context, List<ConferenceMessage> items)
    {
        Log.i(TAG, "ConferenceMessagelistAdapter");

        this.messagelistitems = items;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Log.i(TAG, "MessageListHolder");

        View view = null;

        switch (viewType)
        {
            case Message_model.TEXT_INCOMING_NOT_READ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read, parent, false);
                return new ConferenceMessageListHolder_text_incoming_not_read(view, this.context);
            case Message_model.TEXT_INCOMING_HAVE_READ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read, parent, false);
                return new ConferenceMessageListHolder_text_incoming_read(view, this.context);

            case Message_model.TEXT_OUTGOING_NOT_READ:
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_self_entry, parent, false);
                return new ConferenceMessageListHolder_text_outgoing_not_read(view, this.context);
            case Message_model.TEXT_OUTGOING_HAVE_READ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_self_entry_read, parent, false);
                return new ConferenceMessageListHolder_text_outgoing_read(view, this.context);

        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_error, parent, false);
        return new ConferenceMessageListHolder_error(view, this.context);
    }

    @Override
    public int getItemViewType(int position)
    {
        ConferenceMessage my_msg = this.messagelistitems.get(position);

        {
            // TEXT -------------
            if (my_msg.direction == 0)
            {
                // msg to me
                if (my_msg.read)
                {
                    // has read
                    return Message_model.TEXT_INCOMING_HAVE_READ;
                }
                else
                {
                    // not yet read
                    return Message_model.TEXT_INCOMING_NOT_READ;
                }
                // msg to me
            }
            else
            {
                // msg from me
                if (my_msg.read)
                {
                    // has read
                    return Message_model.TEXT_OUTGOING_HAVE_READ;
                }
                else
                {
                    // not yet read
                    return Message_model.TEXT_OUTGOING_NOT_READ;
                }
                // msg from me
            }
            // TEXT -------------
        }

        // return Message_model.ERROR_UNKNOWN;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        // Log.i(TAG, "onBindViewHolder:position=" + position);

        try
        {
            ConferenceMessage m2 = this.messagelistitems.get(position);

            switch (getItemViewType(position))
            {
                case Message_model.TEXT_INCOMING_NOT_READ:
                    ((ConferenceMessageListHolder_text_incoming_not_read) holder).bindMessageList(m2);
                    break;
                case Message_model.TEXT_INCOMING_HAVE_READ:
                    // NOT USED ----------
                    // NOT USED ----------
                    // NOT USED ----------
                    ((ConferenceMessageListHolder_text_incoming_read) holder).bindMessageList(m2);
                    // NOT USED ----------
                    // NOT USED ----------
                    // NOT USED ----------
                    break;
                case Message_model.TEXT_OUTGOING_NOT_READ:
                    // ******** NOT USED ******** //
                    // ******** NOT USED ******** //
                    // ******** NOT USED ******** //
                    ((ConferenceMessageListHolder_text_outgoing_not_read) holder).bindMessageList(m2);
                    break;
                case Message_model.TEXT_OUTGOING_HAVE_READ:
                    ((ConferenceMessageListHolder_text_outgoing_read) holder).bindMessageList(m2);
                    break;

                default:
                    ((ConferenceMessageListHolder_error) holder).bindMessageList(null);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "onBindViewHolder:EE1:" + e.getMessage());
            e.printStackTrace();
            ((ConferenceMessageListHolder_error) holder).bindMessageList(null);
        }
    }

    @Override
    public int getItemCount()
    {
        if (this.messagelistitems != null)
        {
            // Log.i(TAG, "getItemCount:" + this.messagelistitems.size());
            return this.messagelistitems.size();
        }
        else
        {
            return 0;
        }
    }

    public void add_list_clear(List<ConferenceMessage> new_items)
    {
        Log.i(TAG, "add_list_clear:" + new_items);

        try
        {
            Log.i(TAG, "add_list_clear:001:new_items=" + new_items);
            this.messagelistitems.clear();
            this.messagelistitems.addAll(new_items);
            this.notifyDataSetChanged();
            Log.i(TAG, "add_list_clear:002");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_list_clear:EE:" + e.getMessage());
        }
    }

    synchronized public void redraw_all_items()
    {
        this.notifyDataSetChanged();
    }

    public void add_item(ConferenceMessage new_item)
    {
        // Log.i(TAG, "add_item:" + new_item + ":" + this.messagelistitems.size());

        try
        {
            this.messagelistitems.add(new_item);
            // TODO: use "notifyItemInserted" !!
            this.notifyDataSetChanged();
            // Log.i(TAG, "add_item:002:" + this.messagelistitems.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_item:EE:" + e.getMessage());
        }
    }

    synchronized public boolean update_item(final ConferenceMessage new_item)
    {
        // Log.i(TAG, "update_item:" + new_item);

        boolean found_item = false;

        try
        {
            Iterator it = this.messagelistitems.iterator();
            while (it.hasNext())
            {
                ConferenceMessage m2 = (ConferenceMessage) it.next();

                if (m2.id == new_item.id)
                {
                    found_item = true;
                    int pos = this.messagelistitems.indexOf(m2);
                    // Log.i(TAG, "update_item:003:" + pos);
                    this.messagelistitems.set(pos, new_item);
                    this.notifyItemChanged(pos);
                    break;
                }
            }

            // this.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_item:EE:" + e.getMessage());
        }

        return found_item;
    }

    @Override
    public String getSectionText(int position)
    {
        return " ";
    }

    public String getDateHeaderText(int position)
    {
        try
        {
            getSectionText_message_object2 = messagelistitems.get(position);

            if (getSectionText_message_object2.direction == 0)
            {
                // incoming msg
                if (getSectionText_message_object2.rcvd_timestamp == getSectionText_message_object_ts2)
                {
                    return getSectionText_message_object_ts_string2;
                }
                else
                {
                    getSectionText_message_object_ts2 = getSectionText_message_object2.rcvd_timestamp;
                    getSectionText_message_object_ts_string2 = "" + only_date_time_format(getSectionText_message_object2.rcvd_timestamp);
                    return getSectionText_message_object_ts_string2;
                }
            }
            else
            {
                // outgoing msg
                if (getSectionText_message_object2.sent_timestamp == getSectionText_message_object_ts2)
                {
                    return getSectionText_message_object_ts_string2;
                }
                else
                {
                    getSectionText_message_object_ts2 = getSectionText_message_object2.sent_timestamp;
                    getSectionText_message_object_ts_string2 = "" + only_date_time_format(getSectionText_message_object2.sent_timestamp);
                    return getSectionText_message_object_ts_string2;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return " ";
        }
    }
}
