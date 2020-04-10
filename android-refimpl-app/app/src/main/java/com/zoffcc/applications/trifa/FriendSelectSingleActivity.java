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

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import static com.zoffcc.applications.trifa.HelperRelay.is_any_relay;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendSelectSingleActivity extends ListActivity
{
    private static final String TAG = "trifa.FrndSelSingleActy";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        List<FriendList> fl = null;
        try
        {
            fl = orma.selectFromFriendList().
                    TOX_CONNECTION_realNotEq(0).
                    orderByAlias_nameAsc().
                    toList();

            if (fl == null)
            {
                this.finish();
            }

            if (fl.size() < 1)
            {
                this.finish();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.finish();
        }

        try
        {
            int i = 0;
            int j = 0;
            for (i = 0; i < fl.size(); i++)
            {
                if (is_any_relay(fl.get(i).tox_public_key_string))
                {
                    // do not show any relays
                }
                else
                {
                    j++;
                }
            }

            if (j == 0)
            {
                this.finish();
            }

            String[] friend_pubkey_and_names = new String[j];

            for (i = 0; i < j; i++)
            {
                if (is_any_relay(fl.get(i).tox_public_key_string))
                {
                    // do not show any relays
                }
                else
                {
                    if (fl.get(i).alias_name == null)
                    {
                        friend_pubkey_and_names[i] = fl.get(i).tox_public_key_string + ":\n\n" + fl.get(i).name;
                    }
                    else if (fl.get(i).alias_name.length() < 1)
                    {
                        friend_pubkey_and_names[i] = fl.get(i).tox_public_key_string + ":\n\n" + fl.get(i).name;
                    }
                    else
                    {
                        friend_pubkey_and_names[i] = fl.get(i).tox_public_key_string + ":\n\n" + fl.get(i).alias_name;
                    }
                }
            }

            this.setListAdapter(
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friend_pubkey_and_names));
            ListView lv = getListView();

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    String friend_pubkey_and_name = ((TextView) view).getText().toString();
                    Log.i(TAG, "onItemClick:friend_pubkey_and_name=" + friend_pubkey_and_name);

                    Intent data = new Intent();
                    String return_friend_pubkey = null;

                    try
                    {
                        int iend = friend_pubkey_and_name.indexOf(":"); // find the first occurrence of ":"
                        if (iend != -1)
                        {
                            return_friend_pubkey = friend_pubkey_and_name.substring(0, iend);
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    try
                    {
                        data.setData(Uri.parse(return_friend_pubkey));
                        setResult(RESULT_OK, data);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    finish();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.finish();
        }
    }
}
