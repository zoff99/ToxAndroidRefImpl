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
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListFragment extends Fragment
{
    private static final String TAG = "trifa.MsgListFrgnt";
    List<Message> data_values = null;
    // MessagelistArrayAdapter a = null;
    long current_friendnum = -1;
    RecyclerView listingsView = null;
    MessagelistAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.message_list_layout, container, false);


        MessageListActivity mla = (MessageListActivity) (getActivity());
        current_friendnum = mla.get_current_friendnum();
        Log.i(TAG, "current_friendnum=" + current_friendnum);

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(false).execute();
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (orma != null)
            {
                Log.i(TAG, "current_friendpublic_key=" + tox_friend_get_public_key__wrapper(current_friendnum));
                data_values = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).toList();
                // Log.i(TAG, "current_friendpublic_key:data_values=" + data_values);
                // Log.i(TAG, "current_friendpublic_key:data_values size=" + data_values.size());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // data_values is NULL here!!
        }

        // --------------
        // --------------
        // --------------
        adapter = new MessagelistAdapter(view.getContext(), data_values);
        Log.i(TAG, "onCreateView:adapter=" + adapter);
        listingsView = (RecyclerView) view.findViewById(R.id.msg_rv_list);
        Log.i(TAG, "onCreateView:listingsView=" + listingsView);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true); // pin to bottom element
        listingsView.setLayoutManager(linearLayoutManager);
        listingsView.setItemAnimator(new DefaultItemAnimator());
        listingsView.setHasFixedSize(false);

        listingsView.setAdapter(adapter);
        // --------------
        // --------------


        // a = new MessagelistArrayAdapter(context, data_values);
        // setListAdapter(a);

        MainActivity.message_list_fragment = this;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach(Context)");
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach(Activity)");
        super.onAttach(activity);
    }

    @Override
    public void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        MainActivity.message_list_fragment = this;
    }

    void update_all_messages()
    {
        Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(false).execute();
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (data_values != null)
            {
                data_values.clear();
                adapter.add_list_clear(orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).toList());
            }
            Log.i(TAG, "data_values:005");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    synchronized void modify_message(final Message m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    adapter.update_item(m);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    synchronized void add_message(final Message m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    adapter.add_item(m);
                    listingsView.scrollToPosition(adapter.getItemCount() - 1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }
}
