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

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.CameraWrapper.YUV420rotate90;
import static com.zoffcc.applications.trifa.HelperGeneric.display_toast;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_ENC_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_founder_set_peer_limit;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peer_limit;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_reconnect;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_peer_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_role;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_set_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class GroupInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.GrpInfoActy";
    TextView this_group_id = null;
    EditText this_title = null;
    EditText group_myname_text = null;
    EditText peer_limit_text = null;
    TextView this_privacy_status_text = null;
    TextView group_connection_status_text = null;
    TextView group_myrole_text = null;
    TextView group_mypubkey_text = null;
    static TextView group_num_msgs_text = null;
    static TextView group_num_system_msgs_text = null;
    Button group_reconnect_button = null;
    Button group_del_sysmsgs_button = null;
    String group_id = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupinfo);

        Intent intent = getIntent();
        group_id = intent.getStringExtra("group_id");

        this_group_id = (TextView) findViewById(R.id.group_id_text);
        group_mypubkey_text = (TextView) findViewById(R.id.group_mypubkey_text);
        this_title = (EditText) findViewById(R.id.group_name_text);
        group_myname_text = (EditText) findViewById(R.id.group_myname_text);
        peer_limit_text = (EditText) findViewById(R.id.peer_limit_text);
        this_privacy_status_text = (TextView) findViewById(R.id.group_privacy_status_text);
        group_connection_status_text = (TextView) findViewById(R.id.group_connection_status_text);
        group_myrole_text = (TextView) findViewById(R.id.group_myrole_text);
        group_num_msgs_text = (TextView) findViewById(R.id.group_num_msgs_text);
        group_num_system_msgs_text = (TextView) findViewById(R.id.group_num_system_msgs_text);
        group_reconnect_button = (Button) findViewById(R.id.group_reconnect_button);
        group_del_sysmsgs_button = (Button) findViewById(R.id.group_del_sysmsgs_button);

        try
        {
            group_reconnect_button.setVisibility(View.GONE);
        }
        catch(Exception ignored)
        {
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ((group_id == null) || (group_id.equals("-1")))
        {
            this_group_id.setText("*error*");
        }
        else
        {
            this_group_id.setText(group_id.toLowerCase());
        }
        this_title.setText("*error*");

        long group_num = -1;

        try
        {
            group_num = tox_group_by_groupid__wrapper(group_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            peer_limit_text.setText("" + tox_group_get_peer_limit(group_num));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            group_myname_text.setText(tox_group_peer_get_name(group_num, tox_group_self_get_peer_id(group_num)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            group_mypubkey_text.setText(tox_group_self_get_public_key(group_num));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            this_title.setText(orma.selectFromGroupDB().
                    group_identifierEq(group_id.toLowerCase()).
                    toList().get(0).name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String privacy_state_text = "Unknown Group Privacy State";

        try
        {
            final int privacy_state = orma.selectFromGroupDB().
                    group_identifierEq(group_id.toLowerCase()).
                    toList().get(0).privacy_state;

            if (privacy_state == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value)
            {
                privacy_state_text = "Public Group";
            }
            else if (privacy_state == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PRIVATE.value)
            {
                privacy_state_text = "Private (Invitation only) Group";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this_privacy_status_text.setText(privacy_state_text);

        group_update_connected_status_on_groupinfo(group_num);

        final long group_num_ = group_num;
        group_reconnect_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    tox_group_reconnect(group_num_);
                    group_update_connected_status_on_groupinfo(group_num_);
                    update_savedata_file_wrapper();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        group_del_sysmsgs_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Delete System Messages");
                    builder.setMessage(
                            "Do you want to delete ALL system generated messages (like join/leave/exit messages) in this group permanently?");

                    builder.setPositiveButton("Yes, I want to delete them!", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            try
                            {
                                final Thread t2 = new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            Log.i(TAG, "del_group_system_messages:START:");
                                            display_toast("starting to delete, please wait ...", true, 0);
                                            orma.deleteFromGroupMessage().
                                                    group_identifierEq(group_id).
                                                    tox_group_peer_pubkeyEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                                    execute();
                                            Log.i(TAG, "del_group_system_messages:DONE:");
                                            display_toast("System Messages deleted", true, 0);
                                            reload_message_counts(group_id);
                                        }
                                        catch (Exception e2)
                                        {
                                            e2.printStackTrace();
                                            Log.i(TAG, "del_group_system_messages:EE:" + e2.getMessage());
                                        }
                                    }
                                };
                                t2.start();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        try
        {
            final int myrole = tox_group_self_get_role(group_num);
            group_myrole_text.setText(ToxVars.Tox_Group_Role.value_str(myrole));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void group_update_connected_status_on_groupinfo(final long group_num)
    {
        try
        {
            final int is_connected = tox_group_is_connected(group_num);
            group_connection_status_text.setText(TRIFAGlobals.TOX_GROUP_CONNECTION_STATUS.value_str(is_connected));
            if (is_connected == TRIFAGlobals.TOX_GROUP_CONNECTION_STATUS.TOX_GROUP_CONNECTION_STATUS_CONNECTED.value)
            {
                group_reconnect_button.setVisibility(View.GONE);
            }
            else
            {
                group_reconnect_button.setVisibility(View.VISIBLE);
            }
        }
        catch(Exception ignored)
        {
        }
    }

    static void reload_message_counts(final String group_id)
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    String num_str1 = "*ERROR*";
                    String num_str2 = "*ERROR*";
                    try
                    {
                        num_str1 = "" + orma.selectFromGroupMessage().group_identifierEq(group_id).tox_group_peer_pubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).count();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        num_str2 = "" + orma.selectFromGroupMessage().group_identifierEq(group_id).tox_group_peer_pubkeyEq(
                                TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).count();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    final String num_str_1 = num_str1;
                    final String num_str_2 = num_str2;

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                group_num_msgs_text.setText("Non System Messages: " + num_str_1);
                                group_num_system_msgs_text.setText("System Messages: " + num_str_2);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };

                    if (main_handler_s != null)
                    {
                        main_handler_s.post(myRunnable);
                    }
                }
            };
            t.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        reload_message_counts(group_id);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // TODO dirty hack, just write "conf title"

        try
        {
            String my_new_name = group_myname_text.getText().toString();
            if (my_new_name != null)
            {
                if (my_new_name.length() > 0)
                {
                    int res = tox_group_self_set_name(tox_group_by_groupid__wrapper(group_id),
                                                      my_new_name);
                    update_savedata_file_wrapper();
                }
            }
        }
        catch (Exception ignored)
        {
        }

        try
        {
            String new_peer_limit = peer_limit_text.getText().toString();
            if (new_peer_limit != null)
            {
                if (new_peer_limit.length() > 0)
                {

                    int res = tox_group_founder_set_peer_limit(tox_group_by_groupid__wrapper(group_id),
                                                      Integer.parseInt(new_peer_limit));
                    update_savedata_file_wrapper();
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
