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

import android.os.SystemClock;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.l4digital.fastscroll.FastScroller;
import com.zoffcc.applications.sorm.ConferenceMessage;

import java.util.Iterator;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGeneric.only_date_time_format;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LOAD_ALL_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;

public class ConferenceMessagelistAdapter extends RecyclerView.Adapter implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.CnfMesgelistAdptr";

    private final List<ConferenceMessage> messagelistitems;
    // HINT: lazy per-position cache for the "text consists only of emojis" check
    private final SparseArray<Boolean> only_emojis_cache = new SparseArray<>();
    private Context context;

    private ConferenceMessage getSectionText_message_object = null;
    long getSectionText_message_object_ts = -1L;
    long getSectionText_message_object_ts2 = -1L;
    String getSectionText_message_object_ts_string = " ";
    String getSectionText_message_object_ts_string2 = " ";


    public ConferenceMessagelistAdapter(Context context, List<ConferenceMessage> items)
    {
        // Log.i(TAG, "ConferenceMessagelistAdapter");

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
            case Message_model.PAGING_NEWER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_paging_newer, parent,
                                                                        false);
                return new ConferenceMessageListHolder_paging(view, this.context);
            case Message_model.PAGING_OLDER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_paging_older, parent,
                                                                        false);
                return new ConferenceMessageListHolder_paging(view, this.context);
            case Message_model.TEXT_INCOMING_NOT_READ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read_compact,
                                                                        parent, false);
                return new ConferenceMessageListHolder_text_incoming_not_read(view, this.context);
            case Message_model.TEXT_INCOMING_HAVE_READ:
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read_compact, parent,
                                                                        false);
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                //return new ConferenceMessageListHolder_text_incoming_read(view, this.context);
                return new ConferenceMessageListHolder_error(view, this.context);

            case Message_model.TEXT_OUTGOING_NOT_READ:
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_self_entry_compact, parent,
                                                                        false);
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // return new ConferenceMessageListHolder_text_outgoing_not_read(view, this.context);
                return new ConferenceMessageListHolder_error(view, this.context);
            case Message_model.TEXT_OUTGOING_HAVE_READ:
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.message_list_self_entry_read_compact, parent, false);
                return new ConferenceMessageListHolder_text_outgoing_read(view, this.context);

        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_error, parent, false);
        return new ConferenceMessageListHolder_error(view, this.context);
    }

    // used by the swipe-to-quote callback: read the item at a position safely
    public com.zoffcc.applications.sorm.ConferenceMessage get_message_item(int position)
    {
        try
        {
            if ((position >= 0) && (position < this.messagelistitems.size()))
            {
                return this.messagelistitems.get(position);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "get_message_item:" + e.getMessage());
        }
        return null;
    }

    @Override
    public int getItemViewType(int position)
    {
        ConferenceMessage my_msg = this.messagelistitems.get(position);

        {
            // TEXT -------------
            if ((my_msg.tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) && (

                    ((my_msg.message_id_tox.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)) ||
                     (my_msg.message_id_tox.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)) ||
                     (my_msg.message_id_tox.equals(MESSAGE_PAGING_LOAD_ALL_HASH)))

            ))
            {
                if (my_msg.message_id_tox.equals(MESSAGE_PAGING_SHOW_NEWER_HASH))
                {
                    return Message_model.PAGING_NEWER;
                }
                else
                {
                    // "older" and "load all" dummy entries both use the "older" layout
                    return Message_model.PAGING_OLDER;
                }
            }
            else if (my_msg.direction == 0)
            {
                // msg to me
                if (my_msg.read)
                {
                    // has read ***NOT USED***
                    // Log.i(TAG, "Message_model.TEXT_INCOMING_HAVE_READ");
                    return Message_model.TEXT_INCOMING_HAVE_READ;
                }
                else
                {
                    // not yet read
                    // Log.i(TAG, "Message_model.TEXT_INCOMING_NOT_READ");
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
                    // Log.i(TAG, "Message_model.TEXT_OUTGOING_HAVE_READ");
                    return Message_model.TEXT_OUTGOING_HAVE_READ;
                }
                else
                {
                    // not yet read ***NOT USED***
                    // Log.i(TAG, "Message_model.TEXT_OUTGOING_NOT_READ");
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
                case Message_model.PAGING_NEWER:
                    ((ConferenceMessageListHolder_paging) holder).bindMessageList(m2);
                    break;
                case Message_model.PAGING_OLDER:
                    ((ConferenceMessageListHolder_paging) holder).bindMessageList(m2);
                    break;
                case Message_model.TEXT_INCOMING_NOT_READ:
                    ((ConferenceMessageListHolder_text_incoming_not_read) holder).bindMessageList(m2);
                    break;
                case Message_model.TEXT_INCOMING_HAVE_READ:
                    // NOT USED ----------
                    // NOT USED ----------
                    // NOT USED ----------
                    //((ConferenceMessageListHolder_text_incoming_read) holder).bindMessageList(m2);
                    // NOT USED ----------
                    // NOT USED ----------
                    // NOT USED ----------
                    break;
                case Message_model.TEXT_OUTGOING_NOT_READ:
                    // ******** NOT USED ******** //
                    // ******** NOT USED ******** //
                    // ******** NOT USED ******** //
                    //((ConferenceMessageListHolder_text_outgoing_not_read) holder).bindMessageList(m2);
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
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;
        // Log.i(TAG, "add_list_clear:" + new_items);

        try
        {
            // Log.i(TAG, "add_list_clear:001:new_items=" + new_items);
            this.messagelistitems.clear();
            this.messagelistitems.addAll(new_items);
            HelperGeneric.incmsgflow_log(TAG, "conf/adapter.add_list_clear: rows=" + this.messagelistitems.size());
            only_emojis_cache.clear();
            this.notifyDataSetChanged();
            HelperGeneric.perf_log(TAG, "conference/adapter:add_list_clear rows=" + this.messagelistitems.size() + " +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
            // Log.i(TAG, "add_list_clear:002");
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

    public boolean add_item(ConferenceMessage new_item)
    {
        // Log.i(TAG, "add_item:" + new_item + ":" + this.messagelistitems.size());

        try
        {
            // A: dedup by DB row id. a live append can race a load/reload that already placed this
            //    same row in the list; appending it again would display it twice. the scan is bounded
            //    by the load limit and live appends are infrequent, so it is cheap.
            for (ConferenceMessage existing : this.messagelistitems)
            {
                if ((existing != null) && (existing.id == new_item.id))
                {
                    HelperGeneric.incmsgflow_log(TAG, "conf/adapter.add_item: row=" + new_item.id + " DEDUP already-present -> skip");
                    return false;
                }
            }

            // HINT: synced messages can arrive out of order (older timestamps). while the chat is open
            //       they are appended at the end; the next full reload re-sorts them. make sure the
            //       resume-skip logic does not hide such an out-of-order append.
            int cnt_old = this.messagelistitems.size();
            if (cnt_old > 0)
            {
                ConferenceMessage prev_last = this.messagelistitems.get(cnt_old - 1);
                if ((prev_last != null) && (new_item.sent_timestamp < prev_last.sent_timestamp))
                {
                    HelperGeneric.incmsgflow_log(TAG, "conf/adapter.add_item: row=" + new_item.id + " out-of-order ts=" +
                                                  new_item.sent_timestamp + "<" + prev_last.sent_timestamp + " -> dirty=true");
                    MainActivity.conference_message_list_dirty_while_away = true;
                }
            }

            this.messagelistitems.add(new_item);
            // TODO: use "notifyItemInserted" !!
            this.notifyDataSetChanged();
            // Log.i(TAG, "add_item:002:" + this.messagelistitems.size());
            HelperGeneric.incmsgflow_log(TAG, "conf/adapter.add_item: row=" + new_item.id + " appended size=" + this.messagelistitems.size());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_item:EE:" + e.getMessage());
        }
        return false;
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
                    only_emojis_cache.delete(pos);
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

        HelperGeneric.incmsgflow_log(TAG, "conf/adapter.update_item: row=" + new_item.id + " found=" + found_item);
        return found_item;
    }

    synchronized public void remove_item(final ConferenceMessage del_item)
    {
        try
        {
            Iterator it = this.messagelistitems.iterator();
            while (it.hasNext())
            {
                ConferenceMessage m2 = (ConferenceMessage) it.next();

                if (m2.id == del_item.id)
                {
                    int pos = this.messagelistitems.indexOf(m2);
                    this.messagelistitems.remove(pos);
                    HelperGeneric.incmsgflow_log(TAG, "conf/adapter.remove_item: row=" + del_item.id + " pos=" + pos);
                    // positions shifted: rebuild the position-keyed cache
                    only_emojis_cache.clear();
                    this.notifyItemRemoved(pos);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_item:EE:" + e.getMessage());
        }
    }

    @Override
    public String getSectionText(int position)
    {
        // set fastscroller bluble text
        return " ";
    }

    /**
     * Lazy per-position cache for EmojiUtils.isOnlyEmojis() (avoids re-scanning the text on every rebind).
     */
    public boolean is_only_emojis_cached(int position, String text)
    {
        int idx = only_emojis_cache.indexOfKey(position);
        if (idx >= 0)
        {
            Boolean v = only_emojis_cache.valueAt(idx);
            if (v != null)
            {
                return v;
            }
        }
        boolean result = com.vanniktech.emoji.EmojiUtils.isOnlyEmojis(text);
        only_emojis_cache.put(position, result);
        return result;
    }

    public String getDateHeaderText(int position)
    {
        try
        {
            ConferenceMessage getSectionText_message_object2 = messagelistitems.get(position);

            // "load all messages" dummy entry has no meaningful timestamp
            if ((getSectionText_message_object2.tox_peerpubkey != null) &&
                (getSectionText_message_object2.tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                (getSectionText_message_object2.message_id_tox != null) &&
                (getSectionText_message_object2.message_id_tox.equals(MESSAGE_PAGING_LOAD_ALL_HASH)))
            {
                return " ";
            }

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
                    getSectionText_message_object_ts_string2 =
                            "" + only_date_time_format(getSectionText_message_object2.rcvd_timestamp);
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
                    getSectionText_message_object_ts_string2 =
                            "" + only_date_time_format(getSectionText_message_object2.sent_timestamp);
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

    public ConferenceMessage get_item(int position)
    {
        try
        {
            return messagelistitems.get(position);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public String getPrvPeer(int position)
    {
        try
        {
            ConferenceMessage getSectionText_message_object2 = messagelistitems.get(position);

            if (getSectionText_message_object2.direction == 0)
            {
                // incoming msg
                return ("I_" + getSectionText_message_object2.tox_peerpubkey);
            }
            else
            {
                // outgoing msg
                return ("O_" + getSectionText_message_object2.tox_peerpubkey);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static class DateTime_in_out
    {
        long timestamp;
        int direction;
        String pk;
    }

    public DateTime_in_out getDateTime(int position)
    {
        DateTime_in_out ret = new DateTime_in_out();

        try
        {
            // direction: 0 -> msg received, 1 -> msg sent
            ConferenceMessage getSectionText_message_object2 = messagelistitems.get(position);

            if (getSectionText_message_object2.direction == 0)
            {
                // incoming msg
                ret.direction = 0;
                ret.timestamp = getSectionText_message_object2.sent_timestamp;
                ret.pk = getSectionText_message_object2.tox_peerpubkey;
                return ret;
            }
            else
            {
                // outgoing msg
                ret.direction = 1;
                ret.timestamp = getSectionText_message_object2.sent_timestamp;
                ret.pk = getSectionText_message_object2.tox_peerpubkey;
                return ret;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
