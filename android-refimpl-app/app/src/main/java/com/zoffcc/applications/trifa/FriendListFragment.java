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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListFragment extends Fragment
{
    private static final String TAG = "trifa.FriendListFrgnt";
    static final int MessageListActivity_ID = 2;
    static final int FriendInfoActivity_ID = 3;
    List<FriendList> data_values2 = new ArrayList<FriendList>();
    // FriendlistArrayAdapter a = null;
    static Boolean in_update_data = false;
    //  View view1 = null;
    RecyclerView listingsView = null;
    FriendlistAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view1 = inflater.inflate(R.layout.friend_list_layout, container, false);
        Log.i(TAG, "onCreateView:view1=" + view1);

        // -------------------------------------------
        // -------------------------------------------
        // -------------------------------------------
        List<CombinedFriendsAndConferences> data_values = new ArrayList<CombinedFriendsAndConferences>();
        data_values.clear();

        listingsView = (RecyclerView) view1.findViewById(R.id.rv_list);
        listingsView.getRecycledViewPool().clear();
        listingsView.setHasFixedSize(true);
        listingsView.setLayoutManager(new LinearLayoutManager(view1.getContext()));

        adapter = new FriendlistAdapter(view1.getContext(), data_values);
        Log.i(TAG, "onCreateView:adapter=" + adapter);
        Log.i(TAG, "onCreateView:listingsView=" + listingsView);
        listingsView.setAdapter(adapter);
        listingsView.getRecycledViewPool().clear();
        adapter.clear_items(); // clears friends AND conferences!!
        adapter.notifyDataSetChanged();

        MainActivity.friend_list_fragment = this;
        // -------------------------------------------
        // -------------------------------------------
        // -------------------------------------------

        return view1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        MainActivity.friend_list_fragment = this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        Log.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach(Context)");
        super.onAttach(context);

        in_update_data = false;
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach(Activity)");
        super.onAttach(activity);
    }

    synchronized void modify_friend(final CombinedFriendsAndConferences c, boolean is_friend)
    {
        if (is_friend)
        {
            final FriendList f = c.friend_item;

            // Log.i(TAG, "modify_friend:start");
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final FriendList f2 = orma.selectFromFriendList().
                                tox_public_key_stringEq(f.tox_public_key_string).
                                toList().get(0);

                        if (f2 != null)
                        {
                            FriendList n = deep_copy(f2);
                            CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                            cfac.is_friend = true;
                            cfac.friend_item = n;
                            boolean found_friend = adapter.update_item(cfac, cfac.is_friend);
                            // Log.i(TAG, "modify_friend:found_friend=" + found_friend + " n=" + n);

                            if (!found_friend)
                            {
                                adapter.add_item(cfac);
                                // Log.i(TAG, "modify_friend:add_item");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            try
            {
                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "modify_friend:EE1:" + e.getMessage());
            }
        }
        else // is conference
        {
            final ConferenceDB cc = c.conference_item;

            // Log.i(TAG, "modify_friend:start");
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // who_invited__tox_public_key_stringEq(cc.who_invited__tox_public_key_string).
                        // and().
                        final ConferenceDB conf2 = orma.selectFromConferenceDB().
                                conference_identifierEq(cc.conference_identifier).
                                toList().get(0);

                        if (conf2 != null)
                        {
                            ConferenceDB n = ConferenceDB.deep_copy(conf2);
                            CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                            cfac.is_friend = false;
                            cfac.conference_item = n;
                            boolean found_friend = adapter.update_item(cfac, cfac.is_friend);
                            // Log.i(TAG, "modify_friend:found_friend=" + found_friend + " n=" + n);

                            if (!found_friend)
                            {
                                adapter.add_item(cfac);
                                // Log.i(TAG, "modify_friend:add_item");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            try
            {
                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "modify_friend:EE1:" + e.getMessage());
            }
        }
    }

    @Override
    public void onStart()
    {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume()
    {
        try
        {
            FriendListHolder.remove_progress_dialog();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "onResume");
        super.onResume();

        try
        {
            // reload friendlist
            Log.i(TAG, "onResume:AA");
            List<FriendList> fl = orma.selectFromFriendList().
                    orderByTOX_CONNECTION_on_offDesc().
                    orderByNotification_silentAsc().
                    orderByLast_online_timestampDesc().
                    toList();

            if (fl != null)
            {
                Log.i(TAG, "onResume:fl.size=" + fl.size());
                if (fl.size() > 0)
                {
                    int i = 0;
                    for (i = 0; i < fl.size(); i++)
                    {
                        FriendList n = deep_copy(fl.get(i));
                        final CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                        cc.is_friend = true;
                        cc.friend_item = n;
                        modify_friend(cc, cc.is_friend);
                        // Log.i(TAG, "onResume:modify_friend:" + n);
                    }
                }
            }

            // reload conferences
            List<ConferenceDB> confs = orma.selectFromConferenceDB().
                    orderByConference_activeDesc().
                    orderByNotification_silentAsc().
                    toList();

            if (confs != null)
            {
                if (confs.size() > 0)
                {
                    int i = 0;
                    for (i = 0; i < confs.size(); i++)
                    {
                        ConferenceDB n = ConferenceDB.deep_copy(confs.get(i));
                        CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                        cfac.is_friend = false;
                        cfac.conference_item = n;
                        modify_friend(cfac, cfac.is_friend);
                        // Log.i(TAG, "onResume:modify_friend:" + n);
                    }
                }
            }

            Log.i(TAG, "onResume:BB");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        MainActivity.friend_list_fragment = this;
    }

    synchronized void add_all_friends_clear(final int delay)
    {
        // Log.i(TAG, "add_all_friends_clear");

        final Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    synchronized (in_update_data)
                    {
                        if (in_update_data == true)
                        {
                            // Log.i(TAG, "add_all_friends_clear:already updating!");
                        }
                        else
                        {
                            in_update_data = true;

                            Thread.sleep(delay);
                            adapter.clear_items(); // clears friends AND conferences!!

                            List<FriendList> fl = orma.selectFromFriendList().
                                    orderByTOX_CONNECTION_on_offDesc().
                                    orderByNotification_silentAsc().
                                    orderByLast_online_timestampDesc().
                                    toList();

                            if (fl != null)
                            {
                                // Log.i(TAG, "add_all_friends_clear:fl.size=" + fl.size());
                                if (fl.size() > 0)
                                {
                                    int i = 0;
                                    for (i = 0; i < fl.size(); i++)
                                    {
                                        FriendList n = FriendList.deep_copy(fl.get(i));
                                        CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                        cfac.is_friend = true;
                                        cfac.friend_item = n;
                                        adapter.add_item(cfac);
                                        // Log.i(TAG, "add_all_friends_clear:add:" + n);
                                    }
                                }
                            }

                            List<ConferenceDB> confs = orma.selectFromConferenceDB().
                                    orderByConference_activeDesc().
                                    orderByNotification_silentAsc().
                                    toList();

                            if (confs != null)
                            {
                                if (confs.size() > 0)
                                {
                                    int i = 0;
                                    for (i = 0; i < confs.size(); i++)
                                    {
                                        ConferenceDB n = ConferenceDB.deep_copy(confs.get(i));
                                        CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                        cfac.is_friend = false;
                                        cfac.conference_item = n;
                                        adapter.add_item(cfac);
                                        // Log.i(TAG, "add_all_friends_clear:add:" + n);
                                    }
                                }
                            }

                        }
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "add_all_friends_clear:EE:" + e.getMessage());
                    e.printStackTrace();
                }

                in_update_data = false;

                // Log.i(TAG, "add_all_friends_clear:READY");
            }
        };
        // Log.i(TAG, "add_all_friends_clear:A:");
        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
        // Log.i(TAG, "add_all_friends_clear:B:");
    }

    // name is confusing, just update all friends!! already set to offline in DB
    public void set_all_friends_to_offline()
    {
        add_all_friends_clear(0);
    }
}
