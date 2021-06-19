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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yariksoffice.lingver.Lingver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import info.guardianproject.netcipher.client.StrongBuilder;
import info.guardianproject.netcipher.client.StrongOkHttpClientBuilder2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.insert_default_tcprelay_nodes_into_db;
import static com.zoffcc.applications.trifa.BootstrapNodeEntryDB.insert_default_udp_nodes_into_db;
import static com.zoffcc.applications.trifa.HelperGeneric.delete_vfs_file;
import static com.zoffcc.applications.trifa.HelperGeneric.import_toxsave_file_unsecure;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format_for_filename;
import static com.zoffcc.applications.trifa.HelperGeneric.touch;
import static com.zoffcc.applications.trifa.IOBrowser.getFilesInDir;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_DB_NAME;
import static com.zoffcc.applications.trifa.MainActivity.MAIN_VFS_NAME;
import static com.zoffcc.applications.trifa.MainActivity.PREF__DB_secrect_key;
import static com.zoffcc.applications.trifa.MainActivity.PREF__orbot_enabled;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_ENC_CHATS_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_ENC_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.SelectLanguageActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_frame_played;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_pkt_incoming;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf01;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf02;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf03;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf04;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf05;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf06;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_buf_count_max;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_factor;
import static com.zoffcc.applications.trifa.MainActivity.debug__audio_play_iter;
import static com.zoffcc.applications.trifa.MainActivity.export_savedata_file_unsecure;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_NODELIST_URL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TrifaSetPatternActivity.filter_out_specials_from_filepath_stricter;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MaintenanceActivity extends AppCompatActivity implements StrongBuilder.Callback<OkHttpClient>
{
    private static final String TAG = "trifa.MaintActy";

    Button button_clear_glide_cache;
    Button button_set_app_language;
    Button button_sql_vacuum;
    Button button_sql_analyze;
    Button button_fav_emoji_reset;
    Button button_avatar_icons_delete;
    Button button_update_nodelist;
    Button button_reset_nodelist;
    Button button_test_notification;
    Button button_test_ringtone;
    Button button_iobrowser_start;
    Button button_export_savedata;
    Button button_export_encrypted_files;
    Button button_export_encrypted_chats;
    Button button_import_savedata;

    Boolean button_test_ringtone_start = true;
    MediaPlayer mMediaPlayer = null;

    TextView text_sqlstats = null;
    TextView debug_output = null;

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
        button_set_app_language = (Button) findViewById(R.id.button_set_app_language);
        button_sql_vacuum = (Button) findViewById(R.id.button_sql_vacuum);
        button_sql_analyze = (Button) findViewById(R.id.button_sql_analyze);
        button_fav_emoji_reset = (Button) findViewById(R.id.button_fav_emoji_reset);
        button_avatar_icons_delete = (Button) findViewById(R.id.button_avatar_icons_delete);
        button_update_nodelist = (Button) findViewById(R.id.button_update_nodelist);
        button_reset_nodelist = (Button) findViewById(R.id.button_reset_nodelist);
        button_test_notification = (Button) findViewById(R.id.button_test_notification);
        button_test_ringtone = (Button) findViewById(R.id.button_test_ringtone);
        button_iobrowser_start = (Button) findViewById(R.id.button_iobrowser_start);
        button_export_savedata = (Button) findViewById(R.id.button_export_savedata);
        button_export_encrypted_files = (Button) findViewById(R.id.button_export_encrypted_files);
        button_export_encrypted_chats = (Button) findViewById(R.id.button_export_encrypted_chats);
        button_import_savedata = (Button) findViewById(R.id.button_import_savedata);
        text_sqlstats = (TextView) findViewById(R.id.text_sqlstats);
        debug_output = (TextView) findViewById(R.id.debug_output);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button_iobrowser_start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent intent = new Intent(getBaseContext(), IOBrowser.class);
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_set_app_language.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent intent = new Intent(getBaseContext(), SelectLanguageActivity.class);
                    startActivityForResult(intent, SelectLanguageActivity_ID);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_clear_glide_cache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    HelperGeneric.clearCache_s();
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

        final Context this_context = this;

        button_export_savedata.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    export_savedata_unsecure(this_context);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_export_encrypted_files.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this_context);
                    builder.setTitle("Export Encrypted Files");
                    builder.setMessage(
                            "Your Encrypted received files will be exported unencrypted to this location:" + "\n\n" +
                            MainActivity.SD_CARD_FILES_EXPORT_DIR + SD_CARD_ENC_FILES_EXPORT_DIR);

                    builder.setPositiveButton("Yes, I want to export", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            export_encrypted_files_unsecure(this_context);
                            return;
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

        button_export_encrypted_chats.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this_context);
                    builder.setTitle("Export Encrypted Chats");
                    builder.setMessage("Your Encrypted Chats will be exported unencrypted to this location:" + "\n\n" +
                                       MainActivity.SD_CARD_FILES_EXPORT_DIR + SD_CARD_ENC_CHATS_EXPORT_DIR);

                    builder.setPositiveButton("Yes, I want to export", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            export_encrypted_chats_unsecure(this_context);
                            return;
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

        button_import_savedata.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this_context);
                    builder.setTitle("Import Tox Savedata");
                    builder.setMessage("Tox Savedata File will be imported unencrypted from this location:" + "\n\n" +
                                       MainActivity.SD_CARD_FILES_EXPORT_DIR + "/" + "I_WANT_TO_IMPORT_savedata.tox");

                    builder.setPositiveButton("Yes, I want to wipe all data and import",
                                              new DialogInterface.OnClickListener()
                                              {
                                                  public void onClick(DialogInterface dialog, int id)
                                                  {
                                                      import_toxsave_file_unsecure(this_context);
                                                      return;
                                                  }
                                              });
                    builder.setNegativeButton("Cancel", null);

                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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


        button_test_notification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    //play notification sound
                    Uri ringtone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtone_uri);
                    ringtone.play();

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });

        button_test_ringtone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (button_test_ringtone_start == true)
                {
                    button_test_ringtone_start = false;
                    button_test_ringtone.setText("-- stop Ringtone --");

                    try
                    {
                        Uri ringtone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDataSource(getApplicationContext(), ringtone_uri);
                        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0)
                        {
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                            mMediaPlayer.setLooping(true);
                            try
                            {
                                mMediaPlayer.prepare();
                            }
                            catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                            mMediaPlayer.start();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    button_test_ringtone_start = true;
                    button_test_ringtone.setText("test Ringtone sound");
                    try
                    {
                        mMediaPlayer.stop();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        mMediaPlayer.release();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        String debug__sqlite_version = "unknown";
        try
        {
            Cursor cursor = orma.getConnection().rawQuery("SELECT sqlite_version()");
            cursor.moveToFirst();
            debug__sqlite_version = cursor.getString(0);
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String debug__cipher_version = "unknown";
        try
        {
            Cursor cursor = orma.getConnection().rawQuery("PRAGMA cipher_version");
            cursor.moveToFirst();
            debug__cipher_version = cursor.getString(0);
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String debug__cipher_provider = "unknown";
        try
        {
            Cursor cursor = orma.getConnection().rawQuery("PRAGMA cipher_provider");
            cursor.moveToFirst();
            debug__cipher_provider = cursor.getString(0);
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String debug__cipher_provider_version = "unknown";
        try
        {
            Cursor cursor = orma.getConnection().rawQuery("PRAGMA cipher_provider_version");
            cursor.moveToFirst();
            debug__cipher_provider_version = cursor.getString(0);
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        debug_output_clear();
        debug_output_append("cipher_version=" + debug__cipher_version);
        debug_output_append("sqlite_version=" + debug__sqlite_version);
        debug_output_append("cipher_provider=" + debug__cipher_provider);
        debug_output_append("cipher_provider_version=" + debug__cipher_provider_version);
        debug_output_append("audio_pkt_incoming=" + debug__audio_pkt_incoming);
        debug_output_append("audio_frame_played=" + debug__audio_frame_played);
        debug_output_append("audio_play_buf_count_max=" + debug__audio_play_buf_count_max);
        debug_output_append("audio_play_buf01=" + debug__audio_play_buf01);
        debug_output_append("audio_play_buf02=" + debug__audio_play_buf02);
        debug_output_append("audio_play_buf03=" + debug__audio_play_buf03);
        debug_output_append("audio_play_buf04=" + debug__audio_play_buf04);
        debug_output_append("audio_play_buf05=" + debug__audio_play_buf05);
        debug_output_append("audio_play_buf06=" + debug__audio_play_buf06);
        debug_output_append("audio_play_factor=" + debug__audio_play_factor);
        debug_output_append("audio_play_iter=" + debug__audio_play_iter);

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

        text_sqlstats.setText(
                "Database:\n" + "Messages: " + num_msgs + "\nConference Messages: " + num_confmsgs + "\nFriends: " +
                num_dbfriends + "\nConferences: " + num_dbconfs + "\n\n" + vfs_size + "\n\n" + dbmain_size);

        maint_handler_s = maint_handler;
    }

    Handler maint_handler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            super.handleMessage(msg);
            int id = msg.what;
        }
    };

    void debug_output_clear()
    {
        debug_output.setText("");
    }

    void debug_output_append(String log_line)
    {
        debug_output.setText(debug_output.getText().toString() + log_line + "\n");
    }

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

    @Override
    public void onPause()
    {
        button_test_ringtone_start = true;
        try
        {
            button_test_ringtone.setText("test Ringtone sound");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            mMediaPlayer.stop();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            mMediaPlayer.release();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        super.onPause();
    }

    public static void export_savedata_unsecure(final Context context)
    {
        // create directory in case it does not exist yet
        try
        {
            File export_dir = new File(SD_CARD_FILES_EXPORT_DIR + "/");
            export_dir.mkdirs();
        }
        catch (Exception e)
        {
        }
        // passphrase is unused for now!
        export_savedata_file_unsecure("_", SD_CARD_FILES_EXPORT_DIR + "/" + "unsecure_export_savedata.tox");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Export Tox Savedata");
        builder.setMessage(
                "Tox Savedata File will be exported unencrypted to this location:" + "\n\n" + SD_CARD_FILES_EXPORT_DIR +
                "/" + "unsecure_export_savedata.tox");

        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void export_encrypted_chats_unsecure(final Context context)
    {
        try
        {
            new export_enc_chats_async_task(context).execute();
        }
        catch (Exception e)
        {
        }
    }

    public static void export_encrypted_files_unsecure(final Context context)
    {
        try
        {
            new export_enc_files_async_task(context).execute();
        }
        catch (Exception e)
        {
        }
    }

    private static class export_enc_chats_async_task extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog dialog;
        private final Context c;

        public export_enc_chats_async_task(Context c)
        {
            this.c = c;
            dialog = new ProgressDialog(c);
        }

        @Override
        protected void onPreExecute()
        {
            dialog.setMessage("exporting ...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... args)
        {
            String export_dir_string = MainActivity.SD_CARD_FILES_EXPORT_DIR + SD_CARD_ENC_CHATS_EXPORT_DIR;

            try
            {
                File export_dir = new File(export_dir_string);
                export_dir.mkdirs();

                List<FriendList> fl = orma.selectFromFriendList().
                        is_relayEq(false).
                        orderByTox_public_key_stringAsc().
                        toList();
                for (FriendList f : fl)
                {
                    String dirpath = export_dir_string + "/" + f.tox_public_key_string + "_" +
                                     filter_out_specials_from_filepath_stricter(f.name);
                    // Log.i(TAG, "friend:xxx:F:" + dirpath);
                    new File(dirpath).mkdirs();

                    List<Message> ml = orma.selectFromMessage().
                            tox_friendpubkeyEq(f.tox_public_key_string).
                            toList();
                    for (Message m : ml)
                    {
                        long ts = 0;
                        String msg_type_state = "";
                        if (m.direction == 0)
                        {
                            // incoming msg
                            ts = m.rcvd_timestamp;
                            msg_type_state = "I_";
                        }
                        else
                        {
                            // outgoing msg
                            ts = m.sent_timestamp;
                            if (m.read)
                            {
                                msg_type_state = "OR";
                            }
                            else
                            {
                                msg_type_state = "OU";
                            }
                        }
                        String msg_path =
                                dirpath + "/" + long_date_time_format_for_filename(ts) + "_" + msg_type_state + ".txt";
                        // Log.i(TAG, "friend:xxx:F:M:" + msg_path);

                        try
                        {
                            PrintWriter pr = new PrintWriter(msg_path, "UTF-8");
                            pr.print(m.text);
                            pr.close();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }

                List<ConferenceDB> cl = orma.selectFromConferenceDB().
                        kindEq(TOX_CONFERENCE_TYPE_TEXT.value).
                        orderByConference_identifierAsc().
                        toList();
                for (ConferenceDB conf : cl)
                {
                    String dirpath = export_dir_string + "/" + conf.conference_identifier + "_" +
                                     filter_out_specials_from_filepath_stricter(conf.name);
                    // Log.i(TAG, "friend:xxx:C:" + dirpath);
                    new File(dirpath).mkdirs();

                    List<ConferenceMessage> cml = orma.selectFromConferenceMessage().
                            conference_identifierEq(conf.conference_identifier).toList();
                    for (ConferenceMessage cm : cml)
                    {
                        long ts = 0;
                        String msg_type_state = "";
                        if (cm.direction == 0)
                        {
                            // incoming msg
                            ts = cm.rcvd_timestamp;
                            msg_type_state = "I_";
                        }
                        else
                        {
                            // outgoing msg
                            ts = cm.sent_timestamp;
                            if (cm.read)
                            {
                                msg_type_state = "OR";
                            }
                            else
                            {
                                msg_type_state = "OU";
                            }
                        }
                        String msg_path =
                                dirpath + "/" + long_date_time_format_for_filename(ts) + "_" + msg_type_state + "_" +
                                cm.tox_peerpubkey + "_" + filter_out_specials_from_filepath_stricter(cm.tox_peername) +
                                ".txt";
                        // Log.i(TAG, "friend:xxx:C:M:" + msg_path);

                        try
                        {
                            PrintWriter pr = new PrintWriter(msg_path, "UTF-8");
                            pr.print(cm.text);
                            pr.close();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }

                // now dump the DB to file in SQL format
                final String dbs_path = c.getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;
                net.sqlcipher.database.SQLiteDatabase database = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                        dbs_path, PREF__DB_secrect_key, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);

                final String sql_export_filename = export_dir_string + "/" + "export.sqlite";

                touch(new File(sql_export_filename));
                String sql = "ATTACH DATABASE '" + sql_export_filename + "' AS export KEY '';";
                net.sqlcipher.Cursor cursor = database.rawQuery(sql, null);

                Log.i(TAG, "export:chats:sqlfile:" + cursor.getColumnNames() + " " + cursor.getColumnCount() + " " +
                           cursor.getCount());
                cursor = database.rawQuery("SELECT sqlcipher_export('export');", null);
                Log.i(TAG, "export:chats:sqlfile:" + cursor.getColumnNames() + " " + cursor.getColumnCount() + " " +
                           cursor.getCount());
                cursor = database.rawQuery("DETACH DATABASE export;", null);
                Log.i(TAG, "export:chats:sqlfile:" + cursor.getColumnNames() + " " + cursor.getColumnCount() + " " +
                           cursor.getCount());
                database.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "export:chats:EE01:" + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (dialog.isShowing())
            {
                dialog.dismiss();
            }

            Toast.makeText(c, "export ready", Toast.LENGTH_LONG).show();
        }
    }

    private static class export_enc_files_async_task extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog dialog;
        private final Context c;

        public export_enc_files_async_task(Context c)
        {
            this.c = c;
            dialog = new ProgressDialog(c);
        }

        @Override
        protected void onPreExecute()
        {
            dialog.setMessage("exporting ...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... args)
        {
            String export_dir_string = MainActivity.SD_CARD_FILES_EXPORT_DIR + SD_CARD_ENC_FILES_EXPORT_DIR;

            try
            {
                File export_dir = new File(export_dir_string);
                export_dir.mkdirs();
                List<IOBrowser.dir_item> l = getFilesInDir("/datadir/files/");

                for (IOBrowser.dir_item friend_dir : l)
                {
                    if (friend_dir.get_is_dir())
                    {
                        File export_friend_dir = new File(export_dir_string + "/" + friend_dir.get_path());
                        // Log.i(TAG, "export:mkdir1:" + export_friend_dir);
                        export_friend_dir.mkdirs();
                        List<IOBrowser.dir_item> f = getFilesInDir("/datadir/files/" + friend_dir.get_path());

                        for (IOBrowser.dir_item friend_file : f)
                        {
                            if (!friend_file.get_is_dir())
                            {
                                // Log.i(TAG, "export:mkdir2:" + export_friend_dir);
                                export_friend_dir.mkdirs();

                                //Log.i(TAG, "export:2:" +
                                //           ("/datadir/files/" + friend_dir.get_path() + "/" + friend_file.get_path()) +
                                //           " --> " + export_friend_dir + "/" + friend_file.get_path());

                                try
                                {
                                    HelperGeneric.export_vfs_file_to_real_file(
                                            "/datadir/files/" + friend_dir.get_path() + "/", friend_file.get_path(),
                                            export_friend_dir + "/", friend_file.get_path());
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (dialog.isShowing())
            {
                dialog.dismiss();
            }

            Toast.makeText(c, "export ready", Toast.LENGTH_LONG).show();
        }
    }

    public static String files_and_sizes_in_dir(File directory)
    {
        StringBuilder ret = new StringBuilder("Files:");
        long size_sum = 0L;

        // Log.i(TAG, "files_and_sizes_in_dir:" + directory);

        try
        {
            for (File file : directory.listFiles())
            {
                // Log.i(TAG, "files_and_sizes_in_dir:file=" + file);

                try
                {
                    if (file.isFile())
                    {
                        // Log.i(TAG, "files_and_sizes_in_dir:file name=" + file.getName());
                        // Log.i(TAG, "files_and_sizes_in_dir:file len=" + file.length());
                        if ((file.length() / 1024 / 1024) < 1)
                        {
                            ret.append("\n").append(file.getName()).append("  \t").append(file.length()).append(
                                    " Bytes");
                        }
                        else
                        {
                            ret.append("\n").append(file.getName()).append("  \t").append(
                                    file.length() / 1024 / 1024).append(" MBytes");
                        }
                        size_sum = size_sum + file.length();
                        // Log.i(TAG, "files_and_sizes_in_dir:size_sum=" + size_sum);
                    }
                    else
                    {
                        // Log.i(TAG, "files_and_sizes_in_dir:file? " + file);
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
            ret.append("\nSize:  ").append(size_sum).append(" Bytes");
        }
        else
        {
            ret.append("\nSize:  ").append(size_sum / 1024 / 1024).append(" MBytes");
        }

        return ret.toString();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectLanguageActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                try
                {
                    String result_lang = data.getData().toString();
                    if (result_lang != null)
                    {
                        if (result_lang.length() > 0)
                        {
                            Log.i(TAG, "onActivityResult:result_lang:" + result_lang);

                            if (result_lang.compareTo("_default_") == 0)
                            {
                                Lingver.getInstance().setFollowSystemLocale(this);
                            }
                            else if (result_lang.compareTo("en") == 0)
                            {
                                Lingver.getInstance().setLocale(this, Locale.ENGLISH);
                            }
                            else if (result_lang.compareTo("de") == 0)
                            {
                                Lingver.getInstance().setLocale(this, Locale.GERMAN);
                            }
                            else if (result_lang.compareTo("zh-rCN") == 0)
                            {
                                Lingver.getInstance().setLocale(this, Locale.SIMPLIFIED_CHINESE);
                            }
                            // ------------------
                            // ------------------
                            // ------------------
                            //else if (result_lang.compareTo("ar") == 0)
                            //{
                            //    // TODO: left to right languages corrupt the layout now
                            //    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                            //}
                            else if (result_lang.compareTo("es") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            //else if (result_lang.compareTo("fa") == 0)
                            //{
                            //    // TODO: left to right languages corrupt the layout now
                            //    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                            //}
                            else if (result_lang.compareTo("fr") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("hi") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("hu") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("it") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("kn") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("tr") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("sv") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            else if (result_lang.compareTo("ru") == 0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    Lingver.getInstance().setLocale(this, Locale.forLanguageTag(result_lang));
                                }
                            }
                            // ------------------
                            // ------------------
                            // ------------------
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
