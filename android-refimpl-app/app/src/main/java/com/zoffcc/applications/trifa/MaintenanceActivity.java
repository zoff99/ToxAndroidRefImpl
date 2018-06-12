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
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.guardianproject.netcipher.client.StrongBuilder;
import info.guardianproject.netcipher.client.StrongOkHttpClientBuilder2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.insert_default_tcprelay_nodes_into_db;
import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.insert_default_udp_nodes_into_db;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_DB_NAME;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_VFS_NAME;
import static com.zoffcc.applications.trifa.MainActivity.PREF__orbot_enabled;
import static com.zoffcc.applications.trifa.MainActivity.delete_vfs_file;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_NODELIST_URL;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MaintenanceActivity extends AppCompatActivity implements StrongBuilder.Callback<OkHttpClient>
{
    private static final String TAG = "trifa.MaintActy";

    Button button_clear_glide_cache;
    Button button_sql_vacuum;
    Button button_sql_analyze;
    Button button_fav_emoji_reset;
    Button button_avatar_icons_delete;
    Button button_update_nodelist;
    Button button_reset_nodelist;

    TextView text_sqlstats = null;

    Handler maint_handler_s = null;

    // ----------------------------------------------------
    // TODO: this is copied over from:
    //       https://github.com/vanniktech/Emoji/blob/master/emoji/src/main/java/com/vanniktech/emoji/RecentEmojiManager.java
    //
    private static final String PREFERENCE_NAME = "emoji-recent-manager";
    // private static final String TIME_DELIMITER = ";";
    // private static final String EMOJI_DELIMITER = "~";
    private static final String RECENT_EMOJIS = "recent-emojis";
    //
    // ----------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        button_clear_glide_cache = (Button) findViewById(R.id.button_clear_glide_cache);
        button_sql_vacuum = (Button) findViewById(R.id.button_sql_vacuum);
        button_sql_analyze = (Button) findViewById(R.id.button_sql_analyze);
        button_fav_emoji_reset = (Button) findViewById(R.id.button_fav_emoji_reset);
        button_avatar_icons_delete = (Button) findViewById(R.id.button_avatar_icons_delete);
        button_update_nodelist = (Button) findViewById(R.id.button_update_nodelist);
        button_reset_nodelist = (Button) findViewById(R.id.button_reset_nodelist);
        text_sqlstats = (TextView) findViewById(R.id.text_sqlstats);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button_clear_glide_cache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    MainActivity.clearCache_s();
                    // Toast.makeText(v.getContext(), "cleared Glide Cache", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_sql_vacuum.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
                                Log.i(TAG, "VACUUM:start");
                                Cursor cursor = orma.getConnection().rawQuery("VACUUM");
                                cursor.moveToFirst();
                                cursor.close();
                                Log.i(TAG, "VACUUM:ready");
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                                Log.i(TAG, "VACUUM:EE:" + e2.getMessage());
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


        button_sql_analyze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
                                Log.i(TAG, "ANALYZE:start");
                                Cursor cursor = orma.getConnection().rawQuery("ANALYZE");
                                cursor.moveToFirst();
                                cursor.close();
                                Log.i(TAG, "ANALYZE:ready");
                            }
                            catch (Exception e2)
                            {
                                e2.printStackTrace();
                                Log.i(TAG, "ANALYZE:EE:" + e2.getMessage());
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

        button_fav_emoji_reset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).
                            edit().putString(RECENT_EMOJIS, "").commit();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });


        button_avatar_icons_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Iterator it = orma.selectFromFriendList().toList().iterator();
                    while (it.hasNext())
                    {
                        FriendList f = (FriendList) it.next();
                        if ((f.avatar_pathname != null) && (f.avatar_filename != null))
                        {
                            delete_vfs_file(f.avatar_pathname, f.avatar_filename);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_update_nodelist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "StrongOkHttpClientBuilder:001");
                try
                {
                    if (PREF__orbot_enabled)
                    {
                        Log.i(TAG, "StrongOkHttpClientBuilder:002T");
                        StrongOkHttpClientBuilder2 bb = StrongOkHttpClientBuilder2.
                                forMaxSecurity(MaintenanceActivity.this).
                                withTorValidation();
                        bb.build(MaintenanceActivity.this);
                        Log.i(TAG, "StrongOkHttpClientBuilder:003T");
                    }
                    else
                    {
                        Log.i(TAG, "StrongOkHttpClientBuilder:002");
                        // StrongOkHttpClientBuilder2 bb = StrongOkHttpClientBuilder2.
                        //        forMaxSecurity(MaintenanceActivity.this);
                        // bb.build(MaintenanceActivity.this);
                        onConnected(new OkHttpClient());
                        Log.i(TAG, "StrongOkHttpClientBuilder:003");
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "StrongOkHttpClientBuilder:EE:" + e.getMessage());
                }
            }
        });


        button_reset_nodelist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    orma.deleteFromBootstrapNodeEntryDB().execute();
                    insert_default_udp_nodes_into_db();
                    insert_default_tcprelay_nodes_into_db();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        String num_msgs = "*ERROR*";
        try
        {
            num_msgs = "" + orma.selectFromMessage().count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String num_confmsgs = "*ERROR*";
        try
        {
            num_confmsgs = "" + orma.selectFromConferenceMessage().count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String num_dbfriends = "*ERROR*";
        try
        {
            num_dbfriends = "" + orma.selectFromFriendList().count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String num_dbconfs = "*ERROR*";
        try
        {
            num_dbconfs = "" + orma.selectFromConferenceDB().count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String vfs_size = "*ERROR*";
        try
        {
            String dbFile = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;
            File database_dir = new File(new File(dbFile).getParent());
            vfs_size = files_and_sizes_in_dir(database_dir);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String dbmain_size = "*ERROR*";
        try
        {
            String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;
            File database_dir = new File(new File(dbs_path).getParent());
            dbmain_size = files_and_sizes_in_dir(database_dir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        text_sqlstats.setText("Database:\n" + "Messages: " + num_msgs + "\nConference Messages: " + num_confmsgs + "\nFriends: " + num_dbfriends + "\nConferences: " + num_dbconfs + "\n\n" + vfs_size + "\n\n" + dbmain_size);

        maint_handler_s = maint_handler;
    }

    Handler maint_handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int id = msg.what;
        }
    };

    @Override
    public void onConnected(final OkHttpClient okHttpClient)
    {
        Log.i(TAG, "onConnected:001");

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "onConnected:002");
                    Request request = new Request.Builder().url(TOX_NODELIST_URL).
                            build();

                    Response response = okHttpClient.
                            newCall(request).
                            execute();
                    Log.i(TAG, "onConnected:003");

                    // Type type = new TypeToken<List<NodeListJS>>()
                    // {
                    // }.getType();
                    // List<NodeListJS> fromJson = new Gson().fromJson(response.body().charStream(), type);

                    NodeListJS fromJson = new Gson().fromJson(response.body().charStream(), NodeListJS.class);
                    Log.i(TAG, "getLastRefresh=" + fromJson.getLastRefresh().longValue());
                    Log.i(TAG, "getLastScan=" + fromJson.getLastScan().longValue());
                    Log.i(TAG, "getNodes=" + fromJson.getNodes().size());

                    List<NodeJS> bootstrap_nodes_list_from_internet = fromJson.getNodes();

                    List<BootstrapNodeEntryDB> BootstrapNodeEntryDB_ids_full = orma.selectFromBootstrapNodeEntryDB().orderByIdAsc().toList();
                    List<Long> BootstrapNodeEntryDB_ids = new ArrayList<Long>();
                    for (BootstrapNodeEntryDB bn1 : BootstrapNodeEntryDB_ids_full)
                    {
                        BootstrapNodeEntryDB_ids.add(bn1.id);
                    }
                    // HINT: set null to GC it soon
                    BootstrapNodeEntryDB_ids_full = null;

                    int num_udp = 0;
                    int num_tcp = 0;
                    for (NodeJS nl_entry : bootstrap_nodes_list_from_internet)
                    {
                        try
                        {
                            if (nl_entry.getStatusUdp())
                            {
                                try
                                {
                                    BootstrapNodeEntryDB bn2 = new BootstrapNodeEntryDB();
                                    bn2.ip = nl_entry.getIpv4();
                                    bn2.port = nl_entry.getPort();
                                    bn2.key_hex = nl_entry.getPublicKey();
                                    bn2.udp_node = true;
                                    bn2.num = num_udp;
                                    if ((bn2.ip != null) && (bn2.port > 0) && (bn2.key_hex != null))
                                    {
                                        orma.insertIntoBootstrapNodeEntryDB(bn2);
                                        Log.i(TAG, "add UDP node:" + bn2);
                                        num_udp++;
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "add UDP node:EE4:" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            if (nl_entry.getStatusTcp())
                            {
                                int k = 0;
                                //for (k = 0; k < nl_entry.getTcpPorts().size(); k++)
                                // HINT: use only the first port for now!
                                {
                                    try
                                    {
                                        BootstrapNodeEntryDB bn2 = new BootstrapNodeEntryDB();
                                        bn2.ip = nl_entry.getIpv4();
                                        bn2.port = nl_entry.getTcpPorts().get(k);
                                        bn2.key_hex = nl_entry.getPublicKey();
                                        bn2.udp_node = false;
                                        bn2.num = num_tcp;
                                        if ((bn2.ip != null) && (bn2.port > 0) && (bn2.key_hex != null))
                                        {
                                            orma.insertIntoBootstrapNodeEntryDB(bn2);
                                            Log.i(TAG, "add tcp node:" + bn2);
                                            num_tcp++;
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        Log.i(TAG, "add tcp node:EE5:" + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "onConnected:EE3:" + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    try
                    {
                        if ((num_tcp > 1) && (num_udp > 1))
                        {
                            // HINT: we added at least 2 UDP and 2 TCP nodes
                            // delete previous nodes from DB
                            for (Long bn_old__id : BootstrapNodeEntryDB_ids)
                            {
                                orma.deleteFromBootstrapNodeEntryDB().idEq((long) bn_old__id).execute();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "onConnected:EE6:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                catch (IOException e)
                {
                    Log.i(TAG, "onConnected:EE1:" + e.getMessage());
                    onConnectionException(e);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onConnected:EE2:" + e.getMessage());
                }
            }
        }.start();
    }

    @Override
    public void onConnectionException(Exception e)
    {

    }

    @Override
    public void onTimeout()
    {

    }

    @Override
    public void onInvalid()
    {

    }

    public static String files_and_sizes_in_dir(File directory)
    {
        String ret = "Files:";
        long size_sum = 0L;

        Log.i(TAG, "files_and_sizes_in_dir:" + directory);

        try
        {
            for (File file : directory.listFiles())
            {
                Log.i(TAG, "files_and_sizes_in_dir:file=" + file);

                try
                {
                    if (file.isFile())
                    {
                        Log.i(TAG, "files_and_sizes_in_dir:file name=" + file.getName());
                        Log.i(TAG, "files_and_sizes_in_dir:file len=" + file.length());
                        if ((file.length() / 1024 / 1024) < 1)
                        {
                            ret = ret + "\n" + file.getName() + "\t" + (file.length()) + " Bytes";
                        }
                        else
                        {
                            ret = ret + "\n" + file.getName() + "\t" + (file.length() / 1024 / 1024) + " MBytes";
                        }
                        size_sum = size_sum + file.length();
                        Log.i(TAG, "files_and_sizes_in_dir:size_sum=" + size_sum);
                    }
                    else
                    {
                        Log.i(TAG, "files_and_sizes_in_dir:file? " + file);
                    }
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

        if ((size_sum / 1024 / 1024) < 1)
        {
            ret = ret + "\nSize: " + (size_sum) + " Bytes";
        }
        else
        {
            ret = ret + "\nSize: " + (size_sum / 1024 / 1024) + " MBytes";
        }

        return ret;
    }
}
