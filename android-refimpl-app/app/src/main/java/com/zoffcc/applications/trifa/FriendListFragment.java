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

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;

public class FriendListFragment extends ListFragment
{
    private static final String TAG = "trifa.FriendListFrgnt";
    static final int MessageListActivity_ID = 2;
    List<FriendList> data_values = new ArrayList<FriendList>();
    FriendlistArrayAdapter a = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.friend_list_layout, container, false);
        MainActivity.friend_list_fragment = this;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach(Context)");
        super.onAttach(context);
        data_values.clear();
        a = new FriendlistArrayAdapter(context, data_values);
        setListAdapter(a);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach()");
        super.onAttach(activity);
        data_values.clear();
        a = new FriendlistArrayAdapter(activity, data_values);
        setListAdapter(a);
    }

    void modify_friend(final FriendList f, final long friendnum)
    {
        Log.i(TAG, "modify_friend");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean found_friend = false;
                    int size = data_values.size();
                    int i = 0;
                    for (i = 0; i < size; i++)
                    {
                        if (data_values.get(i).tox_friendnum == friendnum)
                        {
                            found_friend = true;
                            FriendList n = deep_copy(f);
                            data_values.set(i, n);
                            Log.i(TAG, "modify_friend:found friend:" + friendnum);
                            a.notifyDataSetChanged();
                        }
                    }

                    if (!found_friend)
                    {
                        add_friends(f);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        main_handler_s.post(myRunnable);
    }

    void add_friends(final FriendList f)
    {
        Log.i(TAG, "add_friends");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FriendList n = deep_copy(f);
                    data_values.add(n);
                    a.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        main_handler_s.post(myRunnable);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.i(TAG, "onListItemClick pos=" + position + " id=" + id + " friendnum=" + data_values.get(position).tox_friendnum);

        Intent intent = new Intent(this.getActivity(), MessageListActivity.class);
        intent.putExtra("friendnum", data_values.get(position).tox_friendnum);
        startActivityForResult(intent, MessageListActivity_ID);
    }
}
