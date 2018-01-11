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
import android.widget.TextView;

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
    com.l4digital.fastscroll.FastScrollRecyclerView listingsView = null;
    MessagelistAdapter adapter = null;
    static boolean is_at_bottom = true;
    TextView scrollDateHeader = null;
    ConversationDateHeader conversationDateHeader = null;
    MessageListActivity mla = null;
    boolean is_data_loaded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.message_list_layout, container, false);


        mla = (MessageListActivity) (getActivity());
        if (mla != null)
        {
            current_friendnum = mla.get_current_friendnum();
        }
        Log.i(TAG, "current_friendnum=" + current_friendnum);

        // default is: at bottom
        is_at_bottom = true;

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(
                        false).execute();
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
                // Log.i(TAG, "current_friendpublic_key=" + tox_friend_get_public_key__wrapper(current_friendnum));
                data_values = orma.selectFromMessage().tox_friendpubkeyEq(
                        tox_friend_get_public_key__wrapper(current_friendnum)).toList();
                Log.i(TAG, "loading data:001");
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
        // Log.i(TAG, "onCreateView:adapter=" + adapter);
        listingsView = (com.l4digital.fastscroll.FastScrollRecyclerView) view.findViewById(R.id.msg_rv_list);
        // Log.i(TAG, "onCreateView:listingsView=" + listingsView);

        scrollDateHeader = (TextView) view.findViewById(R.id.scroll_date_header);
        scrollDateHeader.setText("");
        scrollDateHeader.setVisibility(View.INVISIBLE);
        conversationDateHeader = new ConversationDateHeader(view.getContext(), scrollDateHeader);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true); // pin to bottom element
        listingsView.setLayoutManager(linearLayoutManager);
        listingsView.setItemAnimator(new DefaultItemAnimator());
        listingsView.setHasFixedSize(false);

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                {
                    conversationDateHeader.show();
                }
                else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    conversationDateHeader.hide();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                scrollDateHeader.setText(adapter.getDateHeaderText(pastVisibleItems));

                if (pastVisibleItems + visibleItemCount >= totalItemCount)
                {
                    // Bottom of the list
                    if (!is_at_bottom)
                    {
                        // Log.i(TAG, "onScrolled:at bottom");
                        is_at_bottom = true;
                    }
                }
                else
                {
                    if (is_at_bottom)
                    {
                        // Log.i(TAG, "onScrolled:NOT at bottom");
                        is_at_bottom = false;
                    }
                }
            }
        };

        listingsView.addOnScrollListener(mScrollListener);
        listingsView.setAdapter(adapter);
        // --------------
        // --------------


        // a = new MessagelistArrayAdapter(context, data_values);
        // setListAdapter(a);

        // MainActivity.message_list_fragment = this;

        is_data_loaded = true;

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

        if (!is_data_loaded)
        {
            try
            {
                // reset "new" flags for messages -------
                if (orma != null)
                {
                    orma.updateMessage().tox_friendpubkeyEq(
                            tox_friend_get_public_key__wrapper(current_friendnum)).is_new(false).execute();
                    Log.i(TAG, "loading data:002");
                }
                // reset "new" flags for messages -------
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            update_all_messages(true);

            // default is: at bottom
            is_at_bottom = true;

            //        try
            //        {
            //            if (orma != null)
            //            {
            //                // Log.i(TAG, "current_friendpublic_key=" + tox_friend_get_public_key__wrapper(current_friendnum));
            //                data_values = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).toList();
            //                // Log.i(TAG, "current_friendpublic_key:data_values=" + data_values);
            //                // Log.i(TAG, "current_friendpublic_key:data_values size=" + data_values.size());
            //            }
            //        }
            //        catch (Exception e)
            //        {
            //            e.printStackTrace();
            //            // data_values is NULL here!!
            //        }

        }

        is_data_loaded = false;
        MainActivity.message_list_fragment = this;
    }

    @Override
    public void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        MainActivity.message_list_fragment = null;
    }

    void update_all_messages(boolean always)
    {
        Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(
                    false).execute();
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if ((always) || (data_values != null))
            {
                Log.i(TAG, "data_values:005a");
                if (data_values != null)
                {
                    data_values.clear();
                }
                Log.i(TAG, "data_values:005b");
                adapter.add_list_clear(orma.selectFromMessage().tox_friendpubkeyEq(
                        tox_friend_get_public_key__wrapper(current_friendnum)).toList());
                Log.i(TAG, "data_values:005c");
            }
            Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
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
                    if (is_at_bottom)
                    {
                        listingsView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "add_message:EE1:" + e.getMessage());
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
