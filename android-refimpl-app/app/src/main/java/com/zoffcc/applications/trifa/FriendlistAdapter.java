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

import java.util.Iterator;
import java.util.List;

public class FriendlistAdapter extends RecyclerView.Adapter
{
    private static final String TAG = "trifa.FriendlistAdapter";

    private final List<CombinedFriendsAndConferences> friendlistitems;
    private Context context;

    public FriendlistAdapter(Context context, List<CombinedFriendsAndConferences> items)
    {
        Log.i(TAG, "FriendlistAdapter");

        this.friendlistitems = items;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_entry, parent, false);
                return new FriendListHolder(view, this.context);

            case CombinedFriendsAndConferences_model.ITEM_IS_CONFERENCE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_conf_entry, parent, false);
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

        if (my_item.is_friend)
        {
            return CombinedFriendsAndConferences_model.ITEM_IS_FRIEND;
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
                    Log.i(TAG, "onBindViewHolder:ITEM_IS_FRIEND");
                    ((FriendListHolder) holder).bindFriendList(fl2.friend_item);
                    break;
                case CombinedFriendsAndConferences_model.ITEM_IS_CONFERENCE:
                    Log.i(TAG, "onBindViewHolder:ITEM_IS_CONFERENCE");
                    ((ConferenceListHolder) holder).bindFriendList(fl2.conference_item);
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

    public void add_item(CombinedFriendsAndConferences new_item)
    {
        // Log.i(TAG, "add_item:" + new_item + ":" + this.friendlistitems.size());

        try
        {
            this.friendlistitems.add(new_item);
            // TODO: use "notifyItemInserted" !!
            this.notifyDataSetChanged();
            // Log.i(TAG, "add_item:002:" + this.friendlistitems.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_item:EE:" + e.getMessage());
        }
    }

    public void clear_items()
    {
        this.friendlistitems.clear();
        this.notifyDataSetChanged();
    }

    public boolean update_item(CombinedFriendsAndConferences new_item_combined, boolean is_friend)
    {
        // Log.i(TAG, "update_item:" + new_item);
        boolean found_item = false;

        try
        {
            Iterator it = this.friendlistitems.iterator();
            while (it.hasNext())
            {
                CombinedFriendsAndConferences f_combined = (CombinedFriendsAndConferences) it.next();

                if (is_friend)
                {
                    if (f_combined.is_friend)
                    {
                        FriendList f = f_combined.friend_item;
                        FriendList new_item = new_item_combined.friend_item;

                        if (f.tox_public_key_string.compareTo(new_item.tox_public_key_string) == 0)
                        {
                            found_item = true;
                            int pos = this.friendlistitems.indexOf(f_combined);
                            this.friendlistitems.set(pos, new_item_combined);
                            this.notifyItemChanged(pos);
                            break;
                        }
                    }
                }
                else // is conference
                {
                    if (!f_combined.is_friend)
                    {
                        ConferenceDB f = f_combined.conference_item;
                        ConferenceDB new_item = new_item_combined.conference_item;

                        if (f.conference_identifier.compareTo(new_item.conference_identifier) == 0)
                        {
                            found_item = true;
                            int pos = this.friendlistitems.indexOf(f_combined);
                            this.friendlistitems.set(pos, new_item_combined);
                            this.notifyItemChanged(pos);
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_item:EE:" + e.getMessage());
        }

        return found_item;
    }
}
