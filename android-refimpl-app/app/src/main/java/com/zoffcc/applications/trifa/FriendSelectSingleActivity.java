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

        Log.i(TAG, "onCreate:001");

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

        Log.i(TAG, "onCreate:002:fl.size()=" + fl.size());

        try
        {
            Log.i(TAG, "onCreate:003");

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

            Log.i(TAG, "onCreate:004:j=" + j);

            if (j == 0)
            {
                this.finish();
            }

            Log.i(TAG, "onCreate:005");

            String[] friend_pubkey_and_names = new String[j];

            Log.i(TAG, "onCreate:006:friend_pubkey_and_names.len=" + friend_pubkey_and_names.length);

            i = 0;
            boolean need_increase = false;
            for (j = 0; j < fl.size(); j++)
            {
                need_increase = false;

                // Log.i(TAG, "onCreate:006a:i=" + i + " n=" + fl.get(j).alias_name);
                if (is_any_relay(fl.get(j).tox_public_key_string))
                {
                    // do not show any relays
                    // Log.i(TAG, "onCreate:006b:RELAY:n=" + fl.get(j).alias_name);
                }
                else
                {
                    if (fl.get(j).alias_name == null)
                    {
                        friend_pubkey_and_names[i] = fl.get(j).tox_public_key_string + ":\n\n" + fl.get(j).name;
                        need_increase = true;
                    }
                    else if (fl.get(j).alias_name.length() < 1)
                    {
                        friend_pubkey_and_names[i] = fl.get(j).tox_public_key_string + ":\n\n" + fl.get(j).name;
                        need_increase = true;
                    }
                    else
                    {
                        friend_pubkey_and_names[i] = fl.get(j).tox_public_key_string + ":\n\n" + fl.get(j).alias_name;
                        need_increase = true;
                    }

                    // Log.i(TAG, "onCreate:006c:n=" + friend_pubkey_and_names[i]);
                }

                // Log.i(TAG, "onCreate:006b:res=" + friend_pubkey_and_names[i]);

                try
                {
                    if (friend_pubkey_and_names[i] == null)
                    {
                        friend_pubkey_and_names[i] = "***";
                        Log.i(TAG, "onCreate:006d");
                    }
                    else if (friend_pubkey_and_names[i].length() == 0)
                    {
                        friend_pubkey_and_names[i] = "+++";
                        Log.i(TAG, "onCreate:006e");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (need_increase)
                {
                    i++;
                }
            }

            Log.i(TAG, "onCreate:007");

            this.setListAdapter(
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friend_pubkey_and_names));
            ListView lv = getListView();

            Log.i(TAG, "onCreate:008");

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    String friend_pubkey_and_name = ((TextView) view).getText().toString();
                    // Log.i(TAG, "onItemClick:friend_pubkey_and_name=" + friend_pubkey_and_name);

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

            Log.i(TAG, "onCreate:009");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.finish();
        }

        Log.i(TAG, "onCreate:010");
    }
}
