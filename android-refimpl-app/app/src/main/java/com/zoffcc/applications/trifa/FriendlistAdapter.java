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

        // 1. Initialize our adapter
        this.friendlistitems = items;
        this.context = context;
        this.itemResource = itemResource;
    }

    // 2. Override the onCreateViewHolder method
    @Override
    public FriendListHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Log.i(TAG, "onCreateViewHolder");

        // 3. Inflate the view and return the new ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(this.itemResource, parent, false);
        return new FriendListHolder(view, this.context);
    }

    // 4. Override the onBindViewHolder method
    @Override
    public void onBindViewHolder(FriendListHolder holder, int position)
    {
        Log.i(TAG, "onBindViewHolder:position=" + position);

        // 5. Use position to access the correct Bakery object
        FriendList fl2 = this.friendlistitems.get(position);

        // 6. Bind the bakery object to the holder
        holder.bindFriendList(fl2);
    }

    @Override
    public int getItemCount()
    {
        Log.i(TAG, "getItemCount:" + this.friendlistitems.size());
        return this.friendlistitems.size();
    }

    public void add_list_clear(List<FriendList> new_items)
    {
        Log.i(TAG, "add_list_clear:" + new_items);

        try
        {
            Log.i(TAG, "add_list_clear:001:new_items=" + new_items);
            this.friendlistitems.clear();
            this.friendlistitems.addAll(new_items);
            this.notifyDataSetChanged();
            Log.i(TAG, "add_list_clear:002");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_list_clear:EE:" + e.getMessage());
        }
    }

    public void add_item(FriendList new_item)
    {
        Log.i(TAG, "add_item:" + new_item);

        try
        {
            this.friendlistitems.add(new_item);
            this.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_item:EE:" + e.getMessage());
        }
    }

    public void update_item(FriendList new_item)
    {
        Log.i(TAG, "update_item:" + new_item);

        try
        {

            Iterator it = this.friendlistitems.iterator();
            while (it.hasNext())
            {
                FriendList f = (FriendList) it.next();
                if (f.tox_public_key_string == new_item.tox_public_key_string)
                {
                    int pos = this.friendlistitems.indexOf(f);
                    this.friendlistitems.set(pos, new_item);
                    break;
                }
            }

            this.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_item:EE:" + e.getMessage());
        }
    }

}
