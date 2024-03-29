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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.view.ActionMode;
import androidx.core.view.MenuItemCompat;

import static com.zoffcc.applications.trifa.HelperConference.copy_selected_conference_messages;
import static com.zoffcc.applications.trifa.HelperConference.copy_selected_group_messages;
import static com.zoffcc.applications.trifa.HelperConference.delete_selected_conference_messages;
import static com.zoffcc.applications.trifa.HelperConference.delete_selected_group_messages;
import static com.zoffcc.applications.trifa.HelperMessage.copy_selected_messages;
import static com.zoffcc.applications.trifa.HelperMessage.delete_selected_messages;
import static com.zoffcc.applications.trifa.HelperMessage.save_selected_messages;
import static com.zoffcc.applications.trifa.HelperMessage.show_select_conference_message_info;
import static com.zoffcc.applications.trifa.HelperMessage.show_select_group_message_info;
import static com.zoffcc.applications.trifa.HelperMessage.show_select_message_info;
import static com.zoffcc.applications.trifa.MainActivity.selected_conference_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_group_messages_text_only;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_text_only;
import static com.zoffcc.applications.trifa.MessageListActivity.amode;
import static com.zoffcc.applications.trifa.MessageListActivity.amode_info_menu_item;
import static com.zoffcc.applications.trifa.MessageListActivity.amode_save_menu_item;

public class ToolbarActionMode implements ActionMode.Callback
{
    private static final String TAG = "trifa.ToolbarActionMode";

    private Context context;
    private boolean action_active = false;

    public ToolbarActionMode(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        Log.i(TAG, "onCreateActionMode");
        mode.getMenuInflater().inflate(R.menu.toolbar_message_activity, menu); // Inflate the menu over action mode
        action_active = false;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        // Log.i(TAG, "onPrepareActionMode");

        //Sometimes the menu will not be visible so for that we need to set their visibility manually in this method
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

        action_active = false;
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_delete:
                action_active = true;
                // Toast.makeText(context, "You selected Delete menu.", Toast.LENGTH_SHORT).show(); // Show toast
                if ((selected_conference_messages.isEmpty()) && (MainActivity.conference_message_list_activity == null))
                {

                    if ((selected_group_messages.isEmpty()) && (MainActivity.group_message_list_activity == null))
                    {
                        // normal chat view
                        delete_selected_messages(context, true, false, "deleting Messages ...");
                    }
                    else
                    {
                        // group chat view
                        delete_selected_group_messages(context, true, "deleting Messages ...");
                    }
                }
                else
                {
                    // conference view
                    delete_selected_conference_messages(context, true, "deleting Messages ...");
                }
                mode.finish(); // Finish action mode
                break;

            case R.id.action_copy:
                action_active = true;
                // Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show(); // Show toast
                if ((selected_conference_messages.isEmpty()) && (MainActivity.conference_message_list_activity == null))
                {
                    if ((selected_group_messages.isEmpty()) && (MainActivity.group_message_list_activity == null))
                    {
                        // normal chat view
                        copy_selected_messages(context);
                    }
                    else
                    {
                        // group chat view
                        copy_selected_group_messages(context);
                    }
                }
                else
                {
                    // conference view
                    copy_selected_conference_messages(context);
                }
                mode.finish(); // Finish action mode
                break;

            case R.id.action_save:
                action_active = true;
                // Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show(); // Show toast
                if ((selected_group_messages.isEmpty()) && (MainActivity.group_message_list_activity == null))
                {
                    // normal chat view
                    save_selected_messages(context);
                }
                else
                {
                    // group chat view
                    // TODO: write me
                }

                mode.finish(); // Finish action mode
                break;

            case R.id.action_info:
                action_active = true;

                if ((selected_conference_messages.isEmpty()) && (MainActivity.conference_message_list_activity == null))
                {
                    if ((selected_group_messages.isEmpty()) && (MainActivity.group_message_list_activity == null))
                    {
                        // normal chat view
                        show_select_message_info(context);
                    }
                    else
                    {
                        // group chat view
                        show_select_group_message_info(context);
                    }
                }
                else
                {
                    // conference view
                    show_select_conference_message_info(context);
                }
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
            if (action_active == false)
            {
                selected_conference_messages.clear();

                selected_group_messages.clear();
                selected_group_messages_incoming_file.clear();
                selected_group_messages_text_only.clear();

                selected_messages.clear();
                selected_messages_incoming_file.clear();
                selected_messages_text_only.clear();

                try
                {
                    if (MainActivity.conference_message_list_fragment != null)
                    {
                        // need to redraw all items again here, to remove the selections
                        MainActivity.conference_message_list_fragment.adapter.redraw_all_items();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (MainActivity.group_message_list_fragment != null)
                    {
                        // need to redraw all items again here, to remove the selections
                        MainActivity.group_message_list_fragment.adapter.redraw_all_items();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (MainActivity.message_list_fragment != null)
                    {
                        // need to redraw all items again here, to remove the selections
                        MainActivity.message_list_fragment.adapter.redraw_all_items();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            try
            {
                if (amode != null)
                {
                    amode = null;
                    amode_save_menu_item = null;
                    amode_info_menu_item = null;
                }
            }
            catch (Exception ignored)
            {
            }

            try
            {
                if (GroupMessageListActivity.amode != null)
                {
                    GroupMessageListActivity.amode = null;
                    GroupMessageListActivity.amode_save_menu_item = null;
                    GroupMessageListActivity.amode_info_menu_item = null;
                }
            }
            catch (Exception ignored)
            {
            }

            try
            {
                if (ConferenceMessageListActivity.amode != null)
                {
                    ConferenceMessageListActivity.amode = null;
                    ConferenceMessageListActivity.amode_save_menu_item = null;
                    ConferenceMessageListActivity.amode_info_menu_item = null;
                }
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}