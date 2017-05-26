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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListFragment extends ListFragment
{
    private static final String TAG = "trifa.MsgListFrgnt";
    List<Message> data_values = null;
    MessagelistArrayAdapter a = null;
    long current_friendnum = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.message_list_layout, container, false);
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

        MessageListActivity mla = (MessageListActivity) (getActivity());
        current_friendnum = mla.get_current_friendnum();
        Log.i(TAG, "current_friendnum=" + current_friendnum);

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
            Log.i(TAG, "current_friendpublic_key=" + tox_friend_get_public_key__wrapper(current_friendnum));
            data_values = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).toList();
            // Log.i(TAG, "current_friendpublic_key:data_values=" + data_values);
            // Log.i(TAG, "current_friendpublic_key:data_values size=" + data_values.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // data_values is NULL here!!
        }
        a = new MessagelistArrayAdapter(context, data_values);
        setListAdapter(a);

        // TODO this is just a bad hack, fix me!! -----------------
        final Thread t1 = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "scroll to bottom:1");
                    Thread.sleep(350); // TODO: really back hack!!
                    // scroll to bottom of message list
                    scroll_to_bottom();
                    Log.i(TAG, "scroll to bottom:1.a");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "scroll to bottom:EE1:" + e.getMessage());
                }
            }
        };
        t1.start();
        // TODO this is just a bad hack, fix me!! -----------------

    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach(Activity)");
        super.onAttach(activity);
    }


    void scroll_to_bottom()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // scroll to bottom
                    Log.i(TAG, "scroll_to_bottom:data_values.size()=" + data_values.size() + " data_values=" + data_values);
                    setSelection(data_values.size());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "scroll_to_bottom:EE1:" + e.getMessage());
                }
            }
        };
        main_handler_s.post(myRunnable);
    }

    void update_all_messages()
    {
        Log.i(TAG, "update_all_messages");

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "current_friendnum=" + current_friendnum);

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
                    Log.i(TAG, "data_values:001");
                    if (data_values != null)
                    {
                        Log.i(TAG, "data_values:002");
                        data_values.clear();
                        Log.i(TAG, "data_values:003");
                        data_values.addAll(orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).toList());
                        Log.i(TAG, "data_values:004");
                    }
                    Log.i(TAG, "data_values:005");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Log.i(TAG, "notifyDataSetChanged:1");
                    a.notifyDataSetChanged();
                    Log.i(TAG, "scroll to bottom:2");
                    scroll_to_bottom();
                    Log.i(TAG, "scroll to bottom:2.a");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        main_handler_s.post(myRunnable);
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
                    if (data_values != null)
                    {
                        int i = 0;
                        for (i = 0; i < data_values.size(); i++)
                        {
                            if (data_values.get(i).id == m.id)
                            {
                                data_values.set(i, m);
                                a.notifyDataSetChanged();
                                break;
                            }
                        }
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

    void add_message(final Message m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO
            }
        };

        main_handler_s.post(myRunnable);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        // TODO
    }
}
