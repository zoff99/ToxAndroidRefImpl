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


import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import static com.zoffcc.applications.trifa.MainActivity.copy_selected_conference_messages;
import static com.zoffcc.applications.trifa.MainActivity.copy_selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.delete_selected_conference_messages;
import static com.zoffcc.applications.trifa.MainActivity.delete_selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.save_selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_conference_messages;
import static com.zoffcc.applications.trifa.MessageListActivity.amode;
import static com.zoffcc.applications.trifa.MessageListActivity.amode_save_menu_item;

public class ToolbarActionMode implements ActionMode.Callback
{
    private static final String TAG = "trifa.ToolbarActionMode";

    private Context context;

    public ToolbarActionMode(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        Log.i(TAG, "onCreateActionMode");
        mode.getMenuInflater().inflate(R.menu.toolbar_message_activity, menu); // Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        // Log.i(TAG, "onPrepareActionMode");

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels
        if (Build.VERSION.SDK_INT < 11)
        {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_copy), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        }
        else
        {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_delete:
                // Toast.makeText(context, "You selected Delete menu.", Toast.LENGTH_SHORT).show(); // Show toast
                if ((selected_conference_messages.isEmpty()) && (MainActivity.conference_message_list_activity == null))
                {
                    // normal chat view
                    delete_selected_messages(context, true, false, "deleting Messages ...");
                }
                else
                {
                    // conference view
                    delete_selected_conference_messages(context);
                    // TODO: write me
                }
                mode.finish(); // Finish action mode
                break;

            case R.id.action_copy:
                // Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show(); // Show toast
                if ((selected_conference_messages.isEmpty()) && (MainActivity.conference_message_list_activity == null))
                {
                    // normal chat view
                    copy_selected_messages(context);
                }
                else
                {
                    // conference view
                    copy_selected_conference_messages(context);
                }
                mode.finish(); // Finish action mode
                break;

            case R.id.action_save:
                // Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show(); // Show toast
                save_selected_messages(context);
                mode.finish(); // Finish action mode
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        try
        {
            if (amode != null)
            {
                amode = null;
                amode_save_menu_item = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}