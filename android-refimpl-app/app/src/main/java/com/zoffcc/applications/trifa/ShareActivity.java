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

import android.app.SearchManager;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.GroupMessageListActivity.add_attachment_ngc;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.filter_out_non_hex_chars;
import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_file_sharing_to_trifa_via_intent;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MessageListActivity.add_attachment;
import static com.zoffcc.applications.trifa.ToxVars.TOX_ADDRESS_SIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_CHAT_ID_SIZE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;

public class ShareActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.ShareActivity";

    TextView t1;
    Intent intent;
    String action;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        t1 = (TextView) findViewById(R.id.text1);
        t1.setText("Share Content ...\nNot yet implemented via share.");

        Log.i(TAG, "onCreate");

        intent = getIntent();
        // Log.i(TAG, "onCreate:intent=" + intent);
        action = intent.getAction();
        type = intent.getType();

        if (!PREF__allow_file_sharing_to_trifa_via_intent)
        {
            return;
        }

        try
        {
            if (Intent.ACTION_SEARCH.equals(action))
            {
                String query = intent.getStringExtra(SearchManager.QUERY);
                // Log.i(TAG, "onCreate:query=" + query);
            }
            else if (Intent.ACTION_SEND.equals(action) && type != null)
            {
                if ("text/plain".equals(type))
                {
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
                else
                {
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    intent_friend_selection.putExtra("ngc_groups", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
            }
            else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
            {
                if ("text/plain".equals(type))
                {
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
                else
                {
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    intent_friend_selection.putExtra("ngc_groups", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
            }
            else if (Intent.ACTION_VIEW.equals(action))
            {
                ClipData cdata = intent.getClipData();
                // Log.i(TAG, "onCreate:cdata=" + cdata);
                if (cdata != null)
                {
                    int item_count = cdata.getItemCount();
                    // Log.i(TAG, "onCreate:item_count=" + item_count);
                    // Log.i(TAG, "onCreate:getDescription=" + cdata.getDescription());
                }

                Uri data = intent.getData();
                // Log.i(TAG, "onCreate:data=" + data);
                String dataString = intent.getDataString();
                // Log.i(TAG, "onCreate:dataString=" + dataString);
                String shareWith = dataString.substring(dataString.lastIndexOf('/') + 1);
                // Log.i(TAG, "onCreate:shareWith=" + shareWith);

                // handle tox:......... URL
                if ((dataString != null) && (dataString.length() > 5) && (dataString.startsWith("tox:")))
                {
                    t1.setText("ToxID:" + dataString.substring(4));

                    // TODO:
                    // check if app is running and unlocked -> otherwise start and unlock it
                    // then open "add" screen and fill in this ToxID
                    final String key_only = dataString.replaceFirst("tox:", "");
                    final String key_only_sanitzied = filter_out_non_hex_chars(key_only);
                    if (key_only_sanitzied.length() == (TOX_ADDRESS_SIZE * 2))
                    {
                        handleToxFriendInvite(key_only_sanitzied);
                    }
                    else if (key_only_sanitzied.length() == (TOX_GROUP_CHAT_ID_SIZE * 2))
                    {
                        handleToxNGCPublicGroupInvite(key_only_sanitzied);
                    }
                }
            }
            else
            {
                ClipData cdata = intent.getClipData();
                // Log.i(TAG, "onCreate:cdata=" + cdata);
                if (cdata != null)
                {
                    int item_count = cdata.getItemCount();
                    // Log.i(TAG, "onCreate:item_count=" + item_count);
                    // Log.i(TAG, "onCreate:getDescription=" + cdata.getDescription());
                }

                Uri data = intent.getData();
                // Log.i(TAG, "onCreate:data=" + data);
                String dataString = intent.getDataString();
                // Log.i(TAG, "onCreate:dataString=" + dataString);
                try
                {
                    String shareWith = dataString.substring(dataString.lastIndexOf('/') + 1);
                    // Log.i(TAG, "onCreate:shareWith=" + shareWith);
                }
                catch (Exception e2)
                {
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreate:EE:" + e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        // Log.i(TAG, "onNewIntent:intent=" + intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult:intent=" + data);
        if (requestCode == SelectFriendSingleActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                try
                {
                    String result_friend_pubkey = data.getData().toString();
                    Log.i(TAG, "onActivityResult:result_friend_pubkey=" + result_friend_pubkey);
                    if (result_friend_pubkey != null)
                    {
                        if (result_friend_pubkey.length() <= 2)
                        {
                            Log.i(TAG, "onActivityResult:result_friend_pubkey.length()=" + result_friend_pubkey.length());
                            return;
                        }

                        int item_type = Integer.parseInt(result_friend_pubkey.substring(0, 1));
                        String item_id = result_friend_pubkey.substring(2);

                        Log.i(TAG,"item_type=" + item_type + " item_id="+item_id.length()+ " "+item_id);

                        if ((item_id.length() == TOX_PUBLIC_KEY_SIZE * 2) && (item_type == 0))
                        {
                            // Log.i(TAG, "onActivityResult:result_friend_pubkey:" + result_friend_pubkey + " intent=" +
                            //            intent);

                            if (Intent.ACTION_SEND.equals(action) && type != null)
                            {
                                if ("text/plain".equals(type))
                                {
                                    handleSendText(intent, item_id);
                                }
                                else
                                {
                                    handleSendImage(intent, item_id, 0);
                                }
                                return;
                            }
                            else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
                            {
                                if (type.startsWith("image/"))
                                {
                                    handleSendMultipleImages(intent, item_id, 0);
                                }
                                else
                                {
                                    handleSendMultipleImages(intent, item_id, 0);
                                }
                                return;
                            }
                        }
                        else if (item_type == 2)
                        {
                            if (Intent.ACTION_SEND.equals(action) && type != null)
                            {
                                if ("text/plain".equals(type))
                                {
                                    // TODO: write me
                                }
                                else
                                {
                                    handleSendImage(intent, item_id, 2);
                                }
                                return;
                            }
                            else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
                            {
                                if (type.startsWith("image/"))
                                {
                                    handleSendMultipleImages(intent, item_id, 2);
                                }
                                else
                                {
                                    // TODO: write me
                                }
                                return;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "EE03:"+ e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    void handleToxFriendInvite(final String friend_pubkey)
    {
        Log.i(TAG, "handleToxFriendInvite:friend_pubkey=" + friend_pubkey);
        // ** // MessageListActivity.show_messagelist_for_friend(this, friend_pubkey);
        // close this share activity
        this.finish();
    }

    void handleToxNGCPublicGroupInvite(final String ngc_group_pubkey)
    {
        try
        {
            Log.i(TAG, "handleToxFriendInvite:ngc_group_pubkey=" + ngc_group_pubkey.substring(0, 5));
        }
        catch(Exception ignored)
        {
        }
        JoinPublicGroupActivity.show_join_public_group_activity(this, ngc_group_pubkey);
        // close this share activity
        this.finish();
    }

    void handleSendText(Intent intent, String friend_pubkey)
    {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null)
        {
            // Log.i(TAG, "handleSendText:sharedText=" + sharedText);
            MessageListActivity.show_messagelist_for_friend(this, friend_pubkey, sharedText);
            // close this share activity
            this.finish();
        }
    }

    void handleSendImage(Intent intent, String id, int type)
    {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null)
        {
            Intent intent_fixup = new Intent();
            intent_fixup.setData(imageUri);
            if (type == 0)
            {
                // Intent { dat=content://com.android.providers.media.documents/document/image:12345 flg=0x43 }
                add_attachment(this, intent_fixup, intent, tox_friend_by_public_key__wrapper(id), false);
                MessageListActivity.show_messagelist_for_friend(this, id, null);
            }
            else if (type == 2)
            {
                add_attachment_ngc(this, intent_fixup, intent, id, false);
                GroupMessageListActivity.show_messagelist_for_id(this, id, null);
            }
            // close this share activity
            this.finish();
        }
    }

    void handleSendMultipleImages(Intent intent, String id, int type)
    {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null)
        {
            for (Uri imageUri : imageUris)
            {
                Intent intent_fixup = new Intent();
                intent_fixup.setData(imageUri);
                if (type == 0)
                {
                    add_attachment(this, intent_fixup, intent, tox_friend_by_public_key__wrapper(id), false);
                }
                else if (type == 2)
                {
                    add_attachment_ngc(this, intent_fixup, intent, id, false);
                }
            }

            if (type == 0)
            {
                MessageListActivity.show_messagelist_for_friend(this, id, null);
            }
            else if (type == 2)
            {
                GroupMessageListActivity.show_messagelist_for_id(this, id, null);
            }
            // close this share activity
            this.finish();
        }
    }
}
