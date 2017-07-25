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
import android.widget.Toast;

import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MessageListActivity.amode;

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
                Toast.makeText(context, "You selected Delete menu.", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish(); // Finish action mode
                break;

            case R.id.action_copy:
                Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show();//Show toast
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
            }

            if (!selected_messages.isEmpty())
            {
                selected_messages.clear();
                try
                {
                    MainActivity.message_list_fragment.adapter.redraw_all_items();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode
        //        recyclerView_adapter.removeSelection();  // remove selection
        //        Fragment recyclerFragment = new MainActivity().getFragment(1);//Get recycler fragment
        //        if (recyclerFragment != null)
        //        {
        //            ((RecyclerView_Fragment) recyclerFragment).setNullToActionMode();//Set action mode null
        //        }
    }
}