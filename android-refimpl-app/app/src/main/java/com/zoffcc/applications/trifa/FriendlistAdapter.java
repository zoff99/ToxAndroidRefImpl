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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.l4digital.fastscroll.FastScroller;
import com.zoffcc.applications.sorm.ConferenceDB;
import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.GroupDB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_FRIEND;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_GROUP;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_friendlist;

public class FriendlistAdapter extends RecyclerView.Adapter implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.FriendlistAdapter";

    private final List<CombinedFriendsAndConferences> friendlistitems;
    private Context context;
    private List<CombinedFriendsAndConferences> all_items;
    private String current_filter = "";

    public FriendlistAdapter(Context context, List<CombinedFriendsAndConferences> items)
    {
        // Log.i(TAG, "FriendlistAdapter");

        this.friendlistitems = items;
        this.all_items = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Log.i(TAG, "onCreateViewHolder");

        View view = null;
        switch (viewType)
        {
            case CombinedFriendsAndConferences_model.ITEM_IS_FRIEND:
                if (PREF__compact_friendlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_entry_compact, parent,
                                                                            false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_entry, parent, false);
                }
                return new FriendListHolder(view, this.context);

            case CombinedFriendsAndConferences_model.ITEM_IS_GROUP:
                if (PREF__compact_friendlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_conf_entry_compact,
                                                                            parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_conf_entry, parent,
                                                                            false);
                }
                return new GroupListHolder(view, this.context);

            case CombinedFriendsAndConferences_model.ITEM_IS_CONFERENCE:
                if (PREF__compact_friendlist)
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_conf_entry_compact,
                                                                            parent, false);
                }
                else
                {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_conf_entry, parent,
                                                                            false);
                }
                return new ConferenceListHolder(view, this.context);
        }

        // TODO: should never get here!?
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_entry, parent, false);
        return new FriendListHolder(view, this.context);
    }

    @Override
    public int getItemViewType(int position)
    {
        CombinedFriendsAndConferences my_item = this.friendlistitems.get(position);

        if (my_item.is_friend == COMBINED_IS_FRIEND)
        {
            return CombinedFriendsAndConferences_model.ITEM_IS_FRIEND;
        }
        else if (my_item.is_friend == COMBINED_IS_GROUP)
        {
            return CombinedFriendsAndConferences_model.ITEM_IS_GROUP;
        }
        else // is conference
        {
            return CombinedFriendsAndConferences_model.ITEM_IS_CONFERENCE;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        // Log.i(TAG, "onBindViewHolder:position=" + position);

        try
        {
            CombinedFriendsAndConferences fl2 = this.friendlistitems.get(position);
            // Log.i(TAG, "onBindViewHolder:fl2=" + fl2);

            int type = getItemViewType(position);
            // Log.i(TAG, "onBindViewHolder:type=" + type);

            switch (type)
            {
                case CombinedFriendsAndConferences_model.ITEM_IS_FRIEND:
                    // Log.i(TAG, "onBindViewHolder:ITEM_IS_FRIEND");
                    ((FriendListHolder) holder).bindFriendList(fl2.friend_item);
                    break;
                case CombinedFriendsAndConferences_model.ITEM_IS_CONFERENCE:
                    // Log.i(TAG, "onBindViewHolder:ITEM_IS_CONFERENCE");
                    ((ConferenceListHolder) holder).bindFriendList(fl2.conference_item);
                    break;
                case CombinedFriendsAndConferences_model.ITEM_IS_GROUP:
                    // Log.i(TAG, "onBindViewHolder:ITEM_IS_GROUP");
                    ((GroupListHolder) holder).bindFriendList(fl2.group_item);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "onBindViewHolder:EE1:" + e.getMessage());
            e.printStackTrace();
            ((FriendListHolder) holder).bindFriendList(null);
        }
    }

    @Override
    public int getItemCount()
    {
        if (this.friendlistitems != null)
        {
            return this.friendlistitems.size();
        }
        else
        {
            return 0;
        }
    }

    public void add_item(CombinedFriendsAndConferences new_item) {
        try {
            this.all_items.add(new_item);
            if (matchesFilter(new_item)) {
                this.friendlistitems.add(new_item);
                this.notifyItemInserted(this.friendlistitems.size() - 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "add_item:EE:" + e.getMessage());
        }
    }

    public void clear_items()
    {
        this.all_items.clear();
        this.friendlistitems.clear();
        this.notifyDataSetChanged();
    }

    public boolean update_item(CombinedFriendsAndConferences new_item_combined, int is_friend) {
        boolean found_item = false;
        try {
            Iterator it = this.all_items.iterator(); // Changed from friendlistitems to all_items
            while (it.hasNext()) {
                CombinedFriendsAndConferences f_combined = (CombinedFriendsAndConferences) it.next();
                if (is_friend == COMBINED_IS_FRIEND) {
                    if (f_combined.is_friend == COMBINED_IS_FRIEND) {
                        FriendList f = f_combined.friend_item;
                        FriendList new_item = new_item_combined.friend_item;
                        if (f.tox_public_key_string.compareTo(new_item.tox_public_key_string) == 0) {
                            found_item = true;
                            f_combined.friend_item = new_item; // Update the internal item directly
                            break;
                        }
                    }
                } else if (is_friend == COMBINED_IS_GROUP) {
                    if (f_combined.is_friend == COMBINED_IS_GROUP) {
                        GroupDB f = f_combined.group_item;
                        GroupDB new_item = new_item_combined.group_item;
                        if (f.group_identifier.compareTo(new_item.group_identifier) == 0) {
                            found_item = true;
                            f_combined.group_item = new_item;
                            break;
                        }
                    }
                } else { // is conference
                    if (f_combined.is_friend == COMBINED_IS_CONFERENCE) {
                        ConferenceDB f = f_combined.conference_item;
                        ConferenceDB new_item = new_item_combined.conference_item;
                        if (f.conference_identifier.compareTo(new_item.conference_identifier) == 0) {
                            found_item = true;
                            f_combined.conference_item = new_item;
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "update_item:EE:" + e.getMessage());
        }

        if (found_item) {
            filterList(); // Re-apply filter after an item changes (e.g., a friend renames themselves)
        }
        return found_item;
    }

    @Override
    public String getSectionText(int position)
    {
        // set fastscroller bluble text
        return " ";
    }

    public void setFilter(String filter) {
        this.current_filter = filter;
        filterList();
    }

    private void filterList() {
        this.friendlistitems.clear();
        if (this.current_filter == null || this.current_filter.isEmpty()) {
            this.friendlistitems.addAll(this.all_items);
        } else {
            for (CombinedFriendsAndConferences item : this.all_items) {
                if (matchesFilter(item)) {
                    this.friendlistitems.add(item);
                }
            }
        }
        this.notifyDataSetChanged();
    }

    private boolean matchesFilter(CombinedFriendsAndConferences row) {
        if (current_filter == null || current_filter.isEmpty()) return true;
        String lowerFilter = current_filter.toLowerCase();
        if (row.is_friend == COMBINED_IS_FRIEND) {
            if (row.friend_item != null) {
                if (row.friend_item.name != null && row.friend_item.name.toLowerCase().contains(lowerFilter)) return true;
                if (row.friend_item.alias_name != null && row.friend_item.alias_name.toLowerCase().contains(lowerFilter)) return true;
            }
        } else if (row.is_friend == COMBINED_IS_CONFERENCE) {
            if (row.conference_item != null) {
                if (row.conference_item.name != null && row.conference_item.name.toLowerCase().contains(lowerFilter)) return true;
            }
        } else if (row.is_friend == COMBINED_IS_GROUP) {
            if (row.group_item != null) {
                if (row.group_item.name != null && row.group_item.name.toLowerCase().contains(lowerFilter)) return true;
                if (row.group_item.topic != null && row.group_item.topic.toLowerCase().contains(lowerFilter)) return true;
            }
        }
        return false;
    }
}
