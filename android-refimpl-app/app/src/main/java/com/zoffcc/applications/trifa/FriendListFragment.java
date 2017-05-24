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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_files;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_filetransfers;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_messages;
import static com.zoffcc.applications.trifa.MainActivity.main_activity_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_delete;
import static com.zoffcc.applications.trifa.MainActivity.update_savedata_file;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListFragment extends ListFragment
{
    private static final String TAG = "trifa.FriendListFrgnt";
    static final int MessageListActivity_ID = 2;
    static final int FriendInfoActivity_ID = 3;
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

        try
        {
            ListView lv = getListView();
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
                {
                    final int position_ = position;
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            int id = item.getItemId();
                            switch (id)
                            {
                                case R.id.item_info:
                                    // show friend info page -----------------
                                    long friend_num_temp = tox_friend_by_public_key(data_values.get(position_).tox_public_key_string);
                                    long friend_num_temp_safety = tox_friend_by_public_key(data_values.get(position_).tox_public_key_string);

                                    Log.i(TAG, "onMenuItemClick:info:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp_safety);

                                    Intent intent = new Intent(main_activity_s, FriendInfoActivity.class);
                                    intent.putExtra("friendnum", friend_num_temp_safety);
                                    startActivityForResult(intent, FriendInfoActivity_ID);
                                    // show friend info page -----------------
                                    break;
                                case R.id.item_delete:
                                    // delete friend -----------------
                                    Runnable myRunnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                long friend_num_temp = tox_friend_by_public_key(data_values.get(position_).tox_public_key_string);

                                                Log.i(TAG, "onMenuItemClick:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp);

                                                // delete friend -------
                                                Log.i(TAG, "onMenuItemClick:1.a:pubkey=" + data_values.get(position_).tox_public_key_string);
                                                delete_friend(data_values.get(position_).tox_public_key_string);
                                                // delete friend -------

                                                // delete friends messages -------
                                                Log.i(TAG, "onMenuItemClick:1.b:fnum=" + friend_num_temp);
                                                delete_friend_all_messages(friend_num_temp);
                                                // delete friend  messages -------

                                                // delete friends files -------
                                                Log.i(TAG, "onMenuItemClick:1.c:fnum=" + friend_num_temp);
                                                delete_friend_all_files(friend_num_temp);
                                                // delete friend  files -------

                                                // delete friends FTs -------
                                                Log.i(TAG, "onMenuItemClick:1.d:fnum=" + friend_num_temp);
                                                delete_friend_all_filetransfers(friend_num_temp);
                                                // delete friend  FTs -------


                                                // delete friend - tox ----
                                                Log.i(TAG, "onMenuItemClick:4");
                                                if (friend_num_temp > -1)
                                                {
                                                    int res = tox_friend_delete(friend_num_temp);
                                                    cache_pubkey_fnum.clear();
                                                    cache_fnum_pubkey.clear();
                                                    update_savedata_file(); // save toxcore datafile (friend removed)
                                                    Log.i(TAG, "onMenuItemClick:5:res=" + res);
                                                }
                                                // delete friend - tox ----

                                                // load all friends into data list ---
                                                Log.i(TAG, "onMenuItemClick:6");
                                                add_all_friends_clear(200);
                                                Log.i(TAG, "onMenuItemClick:7");
                                                // load all friends into data list ---
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                                Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                                            }
                                        }
                                    };
                                    main_handler_s.post(myRunnable);
                                    // delete friend -----------------
                                    break;
                            }
                            return true;
                        }
                    });
                    menu.inflate(R.menu.menu_friendlist_item);
                    menu.show();

                    return true;
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreateView:2:EE:" + e.getMessage());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
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
        Log.i(TAG, "onAttach(Activity)");
        super.onAttach(activity);
        //data_values.clear();
        //a = new FriendlistArrayAdapter(activity, data_values);
        //setListAdapter(a);
    }

    void modify_friend(final FriendList f, final long friendnum)
    {
        // Log.i(TAG, "modify_friend:start");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                // Log.i(TAG, "modify_friend:run---");
                try
                {
                    boolean found_friend = false;
                    int size = data_values.size();
                    int i = 0;
                    for (i = 0; i < size; i++)
                    {
                        if (tox_friend_by_public_key(data_values.get(i).tox_public_key_string) == friendnum)
                        {
                            found_friend = true;
                            FriendList n = deep_copy(f);
                            data_values.set(i, n);
                            // Log.i(TAG, "modify_friend:found friend:" + friendnum);
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
                // Log.i(TAG, "modify_friend:end---");
            }
        };
        try
        {
            main_handler_s.post(myRunnable);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "modify_friend:EE1:" + e.getMessage());
        }
        // Log.i(TAG, "modify_friend:finished");
    }

    @Override
    public void onResume()
    {
        super.onResume();

        try
        {
            // update "new" status on friendlist fragment
            a.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void clear_friends()
    {
        Log.i(TAG, "clear_friends");
        data_values.clear();
    }

    void add_friends_clear(final FriendList f)
    {
        Log.i(TAG, "add_friends_clear");
        data_values.clear();
        add_friends(f);
    }

    void add_all_friends_clear(final int delay)
    {
        Log.i(TAG, "add_all_friends_clear");
        data_values.clear();

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(delay);

                    List<FriendList> fl = orma.selectFromFriendList().toList();
                    if (fl != null)
                    {
                        Log.i(TAG, "add_all_friends_clear:fl.size=" + fl.size());
                        if (fl.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl.size(); i++)
                            {
                                FriendList n = deep_copy(fl.get(i));
                                data_values.add(n);
                                Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    a.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "add_all_friends_clear:EE:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        Log.i(TAG, "add_all_friends_clear:A:");
        main_handler_s.post(myRunnable);
        Log.i(TAG, "add_all_friends_clear:B:");
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

    public void set_all_friends_to_offline()
    {
        Log.i(TAG, "add_friends");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int i = 0;
                    for (i = 0; i < data_values.size(); i++)
                    {
                        data_values.get(i).TOX_CONNECTION = 0;
                    }
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
        Log.i(TAG, "onListItemClick pos=" + position + " id=" + id + " friendnum=" + data_values.get(position).tox_public_key_string);

        Intent intent = new Intent(this.getActivity(), MessageListActivity.class);
        intent.putExtra("friendnum", tox_friend_by_public_key(data_values.get(position).tox_public_key_string));
        startActivityForResult(intent, MessageListActivity_ID);
    }

}
