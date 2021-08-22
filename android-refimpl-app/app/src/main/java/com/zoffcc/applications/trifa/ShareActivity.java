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

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_file_sharing_to_trifa_via_intent;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MessageListActivity.add_attachment;
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

        // HINT: disable sharing content via "share" for now. it does not yet work properly!
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
                    // Log.i(TAG, "select friend to share to ...");
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
                else if ((type.startsWith("image/")) || (type.startsWith("video/")) || (type.startsWith("audio/")))
                {
                    // Log.i(TAG, "select friend to share to ...");
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
                    startActivityForResult(intent_friend_selection, SelectFriendSingleActivity_ID);
                }
            }
            else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
            {
                if ((type.startsWith("image/")) || (type.startsWith("video/")) || (type.startsWith("audio/")))
                {
                    // Log.i(TAG, "select friend to share to ...");
                    Intent intent_friend_selection = new Intent(this, FriendSelectSingleActivity.class);
                    intent_friend_selection.putExtra("offline", 1);
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
        // Log.i(TAG, "onActivityResult:intent=" + data);
        if (requestCode == SelectFriendSingleActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                try
                {
                    String result_friend_pubkey = data.getData().toString();
                    if (result_friend_pubkey != null)
                    {
                        if (result_friend_pubkey.length() == TOX_PUBLIC_KEY_SIZE * 2)
                        {
                            // Log.i(TAG, "onActivityResult:result_friend_pubkey:" + result_friend_pubkey + " intent=" +
                            //            intent);

                            if (Intent.ACTION_SEND.equals(action) && type != null)
                            {
                                if ("text/plain".equals(type))
                                {
                                    handleSendText(intent, result_friend_pubkey);
                                }
                                else if (type.startsWith("image/"))
                                {
                                    handleSendImage(intent, result_friend_pubkey);
                                }
                                else if (type.startsWith("audio/"))
                                {
                                    handleSendImage(intent, result_friend_pubkey);
                                }
                                else if (type.startsWith("video/"))
                                {
                                    handleSendImage(intent, result_friend_pubkey);
                                }
                                return;
                            }
                            else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
                            {
                                if (type.startsWith("image/"))
                                {
                                    handleSendMultipleImages(intent, result_friend_pubkey);
                                }
                                else if (type.startsWith("audio/"))
                                {
                                    handleSendMultipleImages(intent, result_friend_pubkey);
                                }
                                else if (type.startsWith("video/"))
                                {
                                    handleSendMultipleImages(intent, result_friend_pubkey);
                                }
                                return;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
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

    void handleSendImage(Intent intent, String friend_pubkey)
    {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null)
        {
            Intent intent_fixup = new Intent();
            intent_fixup.setData(imageUri);
            // Intent { dat=content://com.android.providers.media.documents/document/image:12345 flg=0x43 }
            add_attachment(this, intent_fixup, intent, tox_friend_by_public_key__wrapper(friend_pubkey), false);
            MessageListActivity.show_messagelist_for_friend(this, friend_pubkey, null);
            // close this share activity
            this.finish();
        }
    }

    void handleSendMultipleImages(Intent intent, String friend_pubkey)
    {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null)
        {
            for (Uri imageUri : imageUris)
            {
                Intent intent_fixup = new Intent();
                intent_fixup.setData(imageUri);
                add_attachment(this, intent_fixup, intent, tox_friend_by_public_key__wrapper(friend_pubkey), false);
            }
            MessageListActivity.show_messagelist_for_friend(this, friend_pubkey, null);
            // close this share activity
            this.finish();
        }
    }
}
