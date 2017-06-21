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

public class FriendlistAdapter extends RecyclerView.Adapter<FriendListHolder>
{
    private static final String TAG = "trifa.FriendlistAdapter";

    private final List<FriendList> friendlistitems;
    private Context context;
    private int itemResource;


    public FriendlistAdapter(Context context, int itemResource, List<FriendList> items)
    {
        Log.i(TAG, "FriendlistAdapter");

        this.friendlistitems = items;
        this.context = context;
        this.itemResource = itemResource;
    }

    @Override
    public FriendListHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Log.i(TAG, "onCreateViewHolder");

        View view = LayoutInflater.from(parent.getContext()).inflate(this.itemResource, parent, false);
        return new FriendListHolder(view, this.context);
    }

    @Override
    public void onBindViewHolder(FriendListHolder holder, int position)
    {
        Log.i(TAG, "onBindViewHolder:position=" + position);

        try
        {
            FriendList fl2 = this.friendlistitems.get(position);
            holder.bindFriendList(fl2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            holder.bindFriendList(null);
        }
    }

    @Override
    public int getItemCount()
    {
        // Log.i(TAG, "getItemCount:" + this.friendlistitems.size());
        return this.friendlistitems.size();
    }

    public void add_list_clear(List<FriendList> new_items)
    {
        // Log.i(TAG, "add_list_clear:" + new_items);

        try
        {
            // Log.i(TAG, "add_list_clear:001:new_items=" + new_items);
            this.friendlistitems.clear();
            this.friendlistitems.addAll(new_items);
            this.notifyDataSetChanged();
            // Log.i(TAG, "add_list_clear:002");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_list_clear:EE:" + e.getMessage());
        }
    }

    public void add_item(FriendList new_item)
    {
        // Log.i(TAG, "add_item:" + new_item + ":" + this.friendlistitems.size());

        try
        {
            this.friendlistitems.add(new_item);
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

    public boolean update_item(FriendList new_item)
    {
        // Log.i(TAG, "update_item:" + new_item);

        boolean found_item = false;

        try
        {
            Iterator it = this.friendlistitems.iterator();
            while (it.hasNext())
            {
                FriendList f = (FriendList) it.next();

                // Log.i(TAG, "update_item:001:" + f);
                // Log.i(TAG, "update_item:002:" + f.tox_public_key_string + ":" + new_item.tox_public_key_string);
                if (f.tox_public_key_string.compareTo(new_item.tox_public_key_string) == 0)
                {
                    found_item = true;
                    int pos = this.friendlistitems.indexOf(f);
                    // Log.i(TAG, "update_item:003:" + pos);
                    this.friendlistitems.set(pos, new_item);
                    this.notifyDataSetChanged();
                    break;
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
