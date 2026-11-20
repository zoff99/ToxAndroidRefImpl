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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.l4digital.fastscroll.FastScroller;
import com.zoffcc.applications.sorm.GroupMessage;

import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGeneric.only_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LOAD_ALL_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;

public class GroupMessagelistAdapter extends RecyclerView.Adapter implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.GrpMesgelistAdptr";

    private final List<com.zoffcc.applications.sorm.GroupMessage> messagelistitems;
    // HINT: lazy per-position cache for the "text consists only of emojis" check
    private final SparseArray<Boolean> only_emojis_cache = new SparseArray<>();
    private Context context;

    private GroupMessage getSectionText_message_object = null;
    long getSectionText_message_object_ts = -1L;
    long getSectionText_message_object_ts2 = -1L;
    String getSectionText_message_object_ts_string = " ";
    String getSectionText_message_object_ts_string2 = " ";


    public GroupMessagelistAdapter(Context context, List<com.zoffcc.applications.sorm.GroupMessage> items)
    {
        // Log.i(TAG, "GroupMessagelistAdapter");

        this.messagelistitems = items;
        this.context = context;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        try
        {
            ((GroupMessageListHolder_file_outgoing_state_cancel) holder).DetachedFromWindow(false);
        }
        catch(Exception e)
        {
        }
        try
        {
            ((GroupMessageListHolder_file_incoming_state_cancel) holder).DetachedFromWindow(false);
        }
        catch(Exception e)
        {
        }
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = null;

        switch (viewType)
        {
            case Message_model.TEXT_INCOMING_NOT_READ:
                if (PREF__compact_chatlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read_compact,
                                                                            parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read, parent,
                                                                            false);
                }
                return new GroupMessageListHolder_text_incoming_not_read(view, this.context);

            case Message_model.TEXT_INCOMING_HAVE_READ:
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_entry_read, parent,
                                                                        false);
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                return new GroupMessageListHolder_error(view, this.context);

            case Message_model.TEXT_OUTGOING_NOT_READ:
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_self_entry, parent,
                                                                        false);
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                // ******** NOT USED ******** //
                return new GroupMessageListHolder_error(view, this.context);

            case Message_model.TEXT_OUTGOING_HAVE_READ:
                if (PREF__compact_chatlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.message_list_self_entry_read_compact, parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_self_entry_read,
                                                                            parent, false);
                }
                return new GroupMessageListHolder_text_outgoing_read(view, this.context);

            case Message_model.FILE_INCOMING_STATE_CANCEL:
                if (PREF__compact_chatlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_group_list_ft_incoming_compact,
                                                                            parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_group_list_ft_incoming, parent,
                                                                            false);
                }
                return new GroupMessageListHolder_file_incoming_state_cancel(view, this.context);

            case Message_model.FILE_OUTGOING_STATE_CANCEL:
                if (PREF__compact_chatlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_ft_outgoing_compact,
                                                                            parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_ft_outgoing, parent,
                                                                            false);
                }
                return new GroupMessageListHolder_file_outgoing_state_cancel(view, this.context);

            case Message_model.PAGING_OLDER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_paging_older, parent,
                                                                        false);
                return new GroupMessageListHolder_paging(view, this.context);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_error, parent, false);
        return new GroupMessageListHolder_error(view, this.context);
    }

    @Override
    public int getItemViewType(int position)
    {
        GroupMessage my_msg = (GroupMessage) this.messagelistitems.get(position);
        {
            // "load all messages" dummy entry (message load limit) -------------
            if ((my_msg.tox_group_peer_pubkey != null) &&
                (my_msg.tox_group_peer_pubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                (my_msg.message_id_tox != null) &&
                (my_msg.message_id_tox.equals(MESSAGE_PAGING_LOAD_ALL_HASH)))
            {
                return Message_model.PAGING_OLDER;
            }
            // "load all messages" dummy entry -------------

            if (my_msg.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
            {
                // FILE -------------
                if (my_msg.direction == 0)
                {
                    // incoming file -----------
                    // ------- STATE: CANCEL -------------
                    return Message_model.FILE_INCOMING_STATE_CANCEL;
                    // ------- STATE: CANCEL -------------
                }
                else
                {
                    // outgoing file -----------
                    // ------- STATE: CANCEL -------------
                    return Message_model.FILE_OUTGOING_STATE_CANCEL;
                    // ------- STATE: CANCEL -------------
                }
                // FILE -------------
            }
            else
            {
                // TEXT -------------
                if (my_msg.direction == 0)
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
        }

        // return Message_model.ERROR_UNKNOWN;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        // Log.i(TAG, "onBindViewHolder:position=" + position);

        try
        {
            GroupMessage m2 = (GroupMessage) this.messagelistitems.get(position);

            switch (getItemViewType(position))
            {
                case Message_model.TEXT_INCOMING_NOT_READ:
                    ((GroupMessageListHolder_text_incoming_not_read) holder).bindMessageList(m2);
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
                    ((GroupMessageListHolder_text_outgoing_read) holder).bindMessageList(m2);
                    break;

                case Message_model.FILE_INCOMING_STATE_CANCEL:
                    ((GroupMessageListHolder_file_incoming_state_cancel) holder).bindMessageList(m2);
                    break;

                case Message_model.FILE_OUTGOING_STATE_CANCEL:
                    ((GroupMessageListHolder_file_outgoing_state_cancel) holder).bindMessageList(m2);
                    break;

                case Message_model.PAGING_OLDER:
                    ((GroupMessageListHolder_paging) holder).bindMessageList(m2);
                    break;

                default:
                    ((GroupMessageListHolder_error) holder).bindMessageList(null);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "onBindViewHolder:EE1:" + e.getMessage());
            e.printStackTrace();
            ((GroupMessageListHolder_error) holder).bindMessageList(null);
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

    public void add_list_clear(List<com.zoffcc.applications.sorm.GroupMessage> new_items)
    {
        // Log.i(TAG, "add_list_clear:" + new_items);

        try
        {
            // Log.i(TAG, "add_list_clear:001:new_items=" + new_items);
            this.messagelistitems.clear();
            this.messagelistitems.addAll(new_items);
            only_emojis_cache.clear();
            this.notifyDataSetChanged();
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

    public void add_item(GroupMessage new_item)
    {
        // Log.i(TAG, "add_item:" + new_item + ":" + this.messagelistitems.size());

        try
        {
            // HINT: synced messages can arrive out of order (older timestamps). while the chat is open
            //       they are appended at the end; the next full reload re-sorts them. make sure the
            //       resume-skip logic does not hide such an out-of-order append.
            int cnt_old = this.messagelistitems.size();
            if (cnt_old > 0)
            {
                GroupMessage prev_last = this.messagelistitems.get(cnt_old - 1);
                if ((prev_last != null) && (new_item.sent_timestamp < prev_last.sent_timestamp))
                {
                    MainActivity.group_message_list_dirty_while_away = true;
                }
            }

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

    synchronized public boolean update_item(final GroupMessage new_item)
    {
        // Log.i(TAG, "update_item:" + new_item);

        boolean found_item = false;

        try
        {
            Iterator it = this.messagelistitems.iterator();
            while (it.hasNext())
            {
                GroupMessage m2 = (GroupMessage) it.next();

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

        return found_item;
    }

    synchronized public void remove_item(final GroupMessage del_item)
    {
        try
        {
            Iterator it = this.messagelistitems.iterator();
            while (it.hasNext())
            {
                GroupMessage m2 = (GroupMessage) it.next();

                if (m2.id == del_item.id)
                {
                    int pos = this.messagelistitems.indexOf(m2);
                    this.messagelistitems.remove(pos);
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
            GroupMessage getSectionText_message_object2 = (GroupMessage) messagelistitems.get(position);

            // "load all messages" dummy entry has no meaningful timestamp
            if ((getSectionText_message_object2.tox_group_peer_pubkey != null) &&
                (getSectionText_message_object2.tox_group_peer_pubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
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

    public GroupMessage get_item(int position)
    {
        try
        {
            return (GroupMessage) messagelistitems.get(position);
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
            GroupMessage getSectionText_message_object2 = (GroupMessage) messagelistitems.get(position);

            if (getSectionText_message_object2.direction == 0)
            {
                // incoming msg
                return ("I_" + getSectionText_message_object2.tox_group_peer_pubkey);
            }
            else
            {
                // outgoing msg
                return ("O_" + getSectionText_message_object2.tox_group_peer_pubkey);
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
            GroupMessage getSectionText_message_object2 = (GroupMessage) messagelistitems.get(position);

            if (getSectionText_message_object2.direction == 0)
            {
                // incoming msg
                ret.direction = 0;
                ret.timestamp = getSectionText_message_object2.sent_timestamp;
                ret.pk = getSectionText_message_object2.tox_group_peer_pubkey;
                return ret;
            }
            else
            {
                // outgoing msg
                ret.direction = 1;
                ret.timestamp = getSectionText_message_object2.sent_timestamp;
                ret.pk = getSectionText_message_object2.tox_group_peer_pubkey;
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
