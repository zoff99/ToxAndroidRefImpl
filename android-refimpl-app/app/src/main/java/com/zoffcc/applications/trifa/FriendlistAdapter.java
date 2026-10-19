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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.l4digital.fastscroll.FastScroller;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
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
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_SEPARATOR;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.is_nightmode_active;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_friendlist;

public class FriendlistAdapter extends RecyclerView.Adapter implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.FriendlistAdapter";

    private static final int VIEW_TYPE_SEPARATOR = 1000;

    private final List<CombinedFriendsAndConferences> friendlistitems;
    private Context context;
    private List<CombinedFriendsAndConferences> all_items;
    private String current_filter = "";

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        public SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }

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
            case VIEW_TYPE_SEPARATOR:
                return new SeparatorViewHolder(buildPinnedSeparatorView(parent.getContext()));

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

        if (my_item.is_friend == COMBINED_IS_SEPARATOR)
        {
            return VIEW_TYPE_SEPARATOR;
        }
        else if (my_item.is_friend == COMBINED_IS_FRIEND)
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

        if (holder instanceof SeparatorViewHolder) {
            return;
        }

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

    public CombinedFriendsAndConferences getItem(int position) {
        return this.friendlistitems.get(position);
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
        insertPinnedSeparator();
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

    public void move_item_to_end(CombinedFriendsAndConferences item) {
        try {
            this.all_items.remove(item);
            this.all_items.add(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPinned(CombinedFriendsAndConferences item) {
        if (item == null) return false;
        if (item.is_friend == COMBINED_IS_FRIEND)
            return item.friend_item != null && item.friend_item.pinned;
        if (item.is_friend == COMBINED_IS_CONFERENCE)
            return item.conference_item != null && item.conference_item.pinned;
        if (item.is_friend == COMBINED_IS_GROUP)
            return item.group_item != null && item.group_item.pinned;
        return false;
    }

    private void insertPinnedSeparator() {
        // defensive: remove any existing separator first
        for (int i = this.friendlistitems.size() - 1; i >= 0; i--) {
            if (this.friendlistitems.get(i).is_friend == COMBINED_IS_SEPARATOR) {
                this.friendlistitems.remove(i);
            }
        }

        // find the boundary: first NON-pinned item that comes after at least one pinned item
        int pinnedSeen = 0;
        for (int i = 0; i < this.friendlistitems.size(); i++) {
            CombinedFriendsAndConferences it = this.friendlistitems.get(i);
            if (isPinned(it)) {
                pinnedSeen++;
            } else {
                if (pinnedSeen > 0) {
                    CombinedFriendsAndConferences sep = new CombinedFriendsAndConferences();
                    sep.is_friend = COMBINED_IS_SEPARATOR;
                    this.friendlistitems.add(i, sep);   // insert right below the pinned block
                    return;
                }
            }
        }
        // no separator if there are no pinned items, or if ALL items are pinned
    }

    private View buildPinnedSeparatorView(Context ctx) {
        boolean dark = is_nightmode_active(ctx);

        int lineColor = dark ? Color.parseColor("#4A4A4A") : Color.parseColor("#DDDDDD");
        int accent    = dark ? Color.parseColor("#9E9E9E") : Color.parseColor("#8A8A8A");

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding((int) dp2px(16), (int) dp2px(12), (int) dp2px(16), (int) dp2px(12));

        // left line
        View leftLine = new View(ctx);
        leftLine.setBackgroundColor(lineColor);
        row.addView(leftLine, new LinearLayout.LayoutParams(0, (int) dp2px(1), 1f));

        // centered "PINNED" label with a pin icon
        TextView label = new TextView(ctx);
        label.setText("Pinned");
        label.setTextSize(11f);
        label.setTextColor(accent);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setAllCaps(true);
        try {
            Drawable pin = new IconicsDrawable(ctx)
                    .icon(FontAwesome.Icon.faw_thumbtack)
                    .color(accent)
                    .sizeDp(11);
            label.setCompoundDrawablesWithIntrinsicBounds(pin, null, null, null);
            label.setCompoundDrawablePadding((int)dp2px(6));
        } catch (Exception ignored) {
        }

        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.leftMargin = (int)dp2px(12);
        labelLp.rightMargin = (int)dp2px(12);
        row.addView(label, labelLp);

        // right line
        View rightLine = new View(ctx);
        rightLine.setBackgroundColor(lineColor);
        row.addView(rightLine, new LinearLayout.LayoutParams(0, (int)dp2px(1), 1f));

        return row;
    }

    public void sort_items()
    {
        try
        {
            java.util.Comparator<CombinedFriendsAndConferences> comparator = new java.util.Comparator<CombinedFriendsAndConferences>()
            {
                @Override
                public int compare(CombinedFriendsAndConferences o1, CombinedFriendsAndConferences o2)
                {
                    boolean p1 = false, p2 = false;

                    if (o1.is_friend == COMBINED_IS_FRIEND) p1 = o1.friend_item.pinned;
                    else if (o1.is_friend == COMBINED_IS_CONFERENCE) p1 = o1.conference_item.pinned;
                    else if (o1.is_friend == COMBINED_IS_GROUP) p1 = o1.group_item.pinned;

                    if (o2.is_friend == COMBINED_IS_FRIEND) p2 = o2.friend_item.pinned;
                    else if (o2.is_friend == COMBINED_IS_CONFERENCE) p2 = o2.conference_item.pinned;
                    else if (o2.is_friend == COMBINED_IS_GROUP) p2 = o2.group_item.pinned;

                    if (p1 && !p2) return -1;  // o1 pinned, o2 not → o1 first
                    if (!p1 && p2) return 1;   // o2 pinned, o1 not → o2 first
                    return 0;                   // both same → keep original order
                }
            };

            java.util.Collections.sort(this.all_items, comparator);

            // Rebuild the visible filtered list
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

            insertPinnedSeparator();
            this.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}