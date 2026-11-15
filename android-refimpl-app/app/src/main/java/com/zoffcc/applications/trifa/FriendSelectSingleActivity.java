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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.l4digital.fastscroll.FastScrollRecyclerView;
import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.GroupDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendSelectSingleActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.FrndSelSingleActy";

    private static final int BUCKET_PINNED = 0;
    private static final int BUCKET_ONLINE = 1;
    private static final int BUCKET_REST = 2;

    List<FriendSelectSingle> friends_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_select_single_layout);

        Log.i(TAG, "onCreate:001");

        final Bundle extras = getIntent().getExtras();
        final int also_offline_friends = (extras != null) ? extras.getInt("offline", 0) : 0;
        final int also_ngc_groups = (extras != null) ? extras.getInt("ngc_groups", 0) : 0;

        Log.i(TAG, "onCreate:also_ngc_groups=" + also_ngc_groups);

        List<FriendList> fl = null;
        try
        {
            if (also_offline_friends == 1)
            {
                fl = orma.selectFromFriendList().
                        is_relayNotEq(true).
                        orderByPinnedDesc().
                        orderByTOX_CONNECTION_on_offDesc().
                        orderByNotification_silentAsc().
                        orderByLast_online_timestampDesc().
                        toList();
            }
            else
            {
                fl = orma.selectFromFriendList().
                        is_relayNotEq(true).
                        TOX_CONNECTION_realNotEq(0).
                        orderByPinnedDesc().
                        orderByTOX_CONNECTION_on_offDesc().
                        orderByNotification_silentAsc().
                        orderByLast_online_timestampDesc().
                        toList();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        List<GroupDB> gr = null;
        if (also_ngc_groups == 1)
        {
            try
            {
                gr = orma.selectFromGroupDB().
                        orderByPinnedDesc().
                        orderByGroup_activeDesc().
                        orderByNotification_silentAsc().
                        orderByNameAsc().
                        toList();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "onCreate:002:fl.size()=" + ((fl != null) ? fl.size() : -1));
        Log.i(TAG, "onCreate:002:gr.size()=" + ((gr != null) ? gr.size() : -1));

        final List<FriendSelectSingle> entries = new ArrayList<>();

        // ------------- friends -------------
        if (fl != null)
        {
            for (FriendList f : fl)
            {
                final String display_name = ((f.alias_name != null) && (f.alias_name.length() > 0))
                        ? f.alias_name : f.name;

                final FriendSelectSingle entry = new FriendSelectSingle(display_name, f.tox_public_key_string,
                                                                        FriendSelectSingle.TYPE_FRIEND);
                entry.friend_item = f;
                entry.pinned = f.pinned;
                entry.online = (f.TOX_CONNECTION_real != TOX_CONNECTION_NONE.value);
                entry.section_text = bubble_char(display_name);
                entries.add(entry);
            }
        }

        // ------------- ngc groups -------------
        if (gr != null)
        {
            for (GroupDB g : gr)
            {
                final FriendSelectSingle entry = new FriendSelectSingle(g.name, g.group_identifier,
                                                                        FriendSelectSingle.TYPE_NGC_GROUP);
                entry.group_item = g;
                entry.pinned = g.pinned;
                entry.online = is_group_connected_for_selection(g);
                entry.section_text = bubble_char(g.name);
                entries.add(entry);
            }
        }

        // ------------- ordering: pinned -> online friends / connected groups -> rest -------------
        // stable sort, so the DB ordering is kept inside each bucket
        Collections.sort(entries, new Comparator<FriendSelectSingle>()
        {
            @Override
            public int compare(FriendSelectSingle a, FriendSelectSingle b)
            {
                return selection_bucket(a) - selection_bucket(b);
            }
        });

        // ------------- section headers -------------
        friends_list = new ArrayList<>();
        int last_bucket = -1;
        for (FriendSelectSingle e : entries)
        {
            final int bucket = selection_bucket(e);
            if (bucket != last_bucket)
            {
                if (bucket == BUCKET_PINNED)
                {
                    friends_list.add(FriendSelectSingle.separator("Pinned"));
                }
                else if (bucket == BUCKET_ONLINE)
                {
                    friends_list.add(FriendSelectSingle.separator("Online"));
                }
                else
                {
                    friends_list.add(FriendSelectSingle.separator("Offline"));
                }
                last_bucket = bucket;
            }
            friends_list.add(e);
        }

        // ------------- nothing selectable? show an explanation instead of closing -------------
        if (entries.size() < 1)
        {
            friends_list.add(FriendSelectSingle.empty_info(
                    build_empty_message(fl, gr, also_offline_friends, also_ngc_groups)));
        }

        final FastScrollRecyclerView listingsView =
                (FastScrollRecyclerView) findViewById(R.id.friend_select_rv_list);
        listingsView.setLayoutManager(new LinearLayoutManager(this));
        listingsView.setItemAnimator(new DefaultItemAnimator());
        listingsView.setHasFixedSize(true);

        final FriendSelectSingleAdapter adapter = new FriendSelectSingleAdapter(this, friends_list,
                                                                                new FriendSelectSingleAdapter.OnEntrySelectedListener()
                                                                                {
                                                                                    @Override
                                                                                    public void onEntrySelected(FriendSelectSingle entry)
                                                                                    {
                                                                                        return_selected_entry(entry);
                                                                                    }
                                                                                });

        // FastScrollRecyclerView.setAdapter() wires the SectionIndexer automatically
        listingsView.setAdapter(adapter);

        Log.i(TAG, "onCreate:010");
    }

    private void return_selected_entry(FriendSelectSingle entry)
    {
        if (entry == null)
        {
            return;
        }

        final Intent data = new Intent();
        if (entry.getType() == FriendSelectSingle.TYPE_FRIEND)
        {
            data.setData(Uri.parse("0:" + entry.pubkey));
        }
        else if (entry.getType() == FriendSelectSingle.TYPE_NGC_GROUP)
        {
            data.setData(Uri.parse("2:" + entry.pubkey));
        }
        else
        {
            return;
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private String build_empty_message(@Nullable List<FriendList> fl, @Nullable List<GroupDB> gr,
                                       int also_offline_friends, int also_ngc_groups)
    {
        final int friend_count = (fl != null) ? fl.size() : 0;
        final int group_count = (gr != null) ? gr.size() : 0;

        final boolean wants_groups = (also_ngc_groups == 1);
        final boolean online_only = (also_offline_friends != 1);

        if (wants_groups)
        {
            if ((friend_count == 0) && (group_count == 0))
            {
                if (online_only)
                {
                    return "Nothing to select.\n\nYou have no friends or groups that are online right now.";
                }
                return "Nothing to select.\n\nYou have no friends and no groups yet.";
            }

            if (friend_count == 0)
            {
                if (online_only)
                {
                    return "Nothing to select.\n\nNone of your friends is online right now, " +
                           "and you have no groups.";
                }
                return "Nothing to select.\n\nYou have no friends yet, and no groups.";
            }

            if (group_count == 0)
            {
                return "Nothing to select.\n\nNone of your friends is online right now, " +
                       "and you have no groups.";
            }

            return "Nothing to select.\n\nNo friend is online right now, and no group is connected.";
        }

        // friends only
        if (friend_count == 0)
        {
            if (online_only)
            {
                return "Nothing to select.\n\nNone of your friends is online right now.\n\n" +
                       "Wait for a friend to come online, or add a friend first.";
            }
            return "Nothing to select.\n\nYou have no friends yet.\n\nAdd a friend first.";
        }

        return "Nothing to select.\n\nNone of your friends is online right now.";
    }

    private static String bubble_char(String name)
    {
        try
        {
            if ((name != null) && (name.trim().length() > 0))
            {
                return name.trim().substring(0, 1).toUpperCase(Locale.ENGLISH);
            }
        }
        catch (Exception ignored)
        {
        }
        return "#";
    }

    private static int selection_bucket(FriendSelectSingle e)
    {
        if (e.pinned)
        {
            return BUCKET_PINNED;
        }
        if (e.online)
        {
            return BUCKET_ONLINE;
        }
        return BUCKET_REST;
    }

    private static boolean is_group_connected_for_selection(GroupDB g)
    {
        try
        {
            if ((g == null) || (!g.group_active) || (g.group_we_left))
            {
                return false;
            }

            return (tox_group_is_connected(tox_group_by_groupid__wrapper(g.group_identifier)) ==
                    TRIFAGlobals.TOX_GROUP_CONNECTION_STATUS.TOX_GROUP_CONNECTION_STATUS_CONNECTED.value);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
