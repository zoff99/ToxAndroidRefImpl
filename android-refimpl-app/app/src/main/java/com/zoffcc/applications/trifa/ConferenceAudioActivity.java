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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.speech.levelmeter.BarLevelDrawable;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.zoffcc.applications.nativeaudio.AudioProcessing;

import static com.zoffcc.applications.nativeaudio.AudioProcessing.destroy_buffers;
import static com.zoffcc.applications.nativeaudio.AudioProcessing.init_buffers;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_in;
import static com.zoffcc.applications.nativeaudio.NativeAudio.get_vu_out;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.HelperConference.get_conference_num_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.is_conference_active;
import static com.zoffcc.applications.trifa.HelperFriend.resolve_name_for_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.SAMPLE_RATE_FIXED;
import static com.zoffcc.applications.trifa.MainActivity.SelectFriendSingleActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_invite;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_public_key;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceAudioActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.CnfAudioActivity";
    static String conf_id = "-1";
    static String conf_id_prev = "-1";
    static ConferenceAudioActivity caa = null;

    private DetectHeadset dha = null;
    static int activity_state = 0;
    private boolean do_not_close_on_pause = false;

    // main drawer ----------
    Drawer conference_message_drawer = null;
    AccountHeader conference_message_drawer_header = null;
    ProfileDrawerItem conference_message_profile_item = null;
    // long peers_in_list_next_num = 0;
    // main drawer ----------

    BarLevelDrawable audio_bar_in = null;
    BarLevelDrawable audio_bar_out = null;
    ImageView ml_icon = null;
    ImageView ml_status_icon = null;
    TextView ml_maintext = null;
    Button AudioGroupPushToTalkButton = null;
    static boolean push_to_talk_active = false;

    Handler conferences_av_handler = null;
    static Handler conferences_av_handler_s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        do_not_close_on_pause = false;
        Callstate.audio_group_active = true;

        conferences_av_handler = new Handler(getMainLooper());
        conferences_av_handler_s = conferences_av_handler;

        Intent intent = getIntent();
        conf_id = intent.getStringExtra("conf_id");
        Log.i(TAG, "onCreate:003:conf_id=" + conf_id + " conf_id_prev=" + conf_id_prev);
        conf_id_prev = conf_id;

        dha = new DetectHeadset(this);

        setContentView(R.layout.activity_conference_audio);

        caa = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable drawer_header_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_group).
                color(getResources().getColor(R.color.md_dark_primary_text)).sizeDp(100);

        conference_message_profile_item = new ProfileDrawerItem().
                withName("Userlist").
                withIcon(drawer_header_icon);

        // Create the AccountHeader
        conference_message_drawer_header = new AccountHeaderBuilder().
                withActivity(this).
                withSelectionListEnabledForSingleProfile(false).
                withTextColor(getResources().getColor(R.color.md_dark_primary_text)).
                withHeaderBackground(R.color.colorHeader).
                withCompactStyle(true).
                addProfiles(conference_message_profile_item).
                withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
                {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                }).build();

        //        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).
        //                withIdentifier(1L).
        //                withName("User1").
        //                withIcon(GoogleMaterial.Icon.gmd_face);


        // peers_in_list_next_num = 1;
        lookup_peer_listnum_pubkey.clear();

        // create the drawer and remember the `Drawer` result object
        conference_message_drawer = new DrawerBuilder().
                withActivity(this).
                withAccountHeader(conference_message_drawer_header).
                withInnerShadow(false).
                withRootView(R.id.drawer_container).
                withShowDrawerOnFirstLaunch(false).
                withActionBarDrawerToggleAnimated(true).
                withActionBarDrawerToggle(true).
                withToolbar(toolbar).
                withTranslucentStatusBar(false).
                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        Log.i(TAG, "drawer:item=" + position);
                        if (position == 1)
                        {
                            // profile
                            try
                            {
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                }).build();


        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);

        audio_bar_in = (BarLevelDrawable) findViewById(R.id.audio_bar_in);
        audio_bar_out = (BarLevelDrawable) findViewById(R.id.audio_bar_out);

        AudioGroupPushToTalkButton = (Button) findViewById(R.id.AudioGroupPushToTalkButton);
        AudioGroupPushToTalkButton.setBackgroundResource(R.drawable.button_audio_round_bg);
        AudioGroupPushToTalkButton.setText("Push to Talk");

        AudioGroupPushToTalkButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    try
                    {
                        AudioGroupPushToTalkButton.setBackgroundResource(R.drawable.button_audio_round_bg_pressed);
                        AudioGroupPushToTalkButton.setText("talking ...");
                        push_to_talk_active = true;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        AudioGroupPushToTalkButton.setBackgroundResource(R.drawable.button_audio_round_bg);
                        AudioGroupPushToTalkButton.setText("Push to Talk");
                        push_to_talk_active = false;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "setMicrophoneMute:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        ml_status_icon.setVisibility(View.INVISIBLE);

        ml_icon.setImageResource(R.drawable.circle_red);
        set_conference_connection_status_icon();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                color(getResources().
                        getColor(R.color.colorPrimaryDark)).
                sizeDp(80);

        // final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);


        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        try
        {
            manager.setSpeakerphoneOn(true);
            Callstate.audio_speaker = true;
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }

        try
        {
            if (dha._Detect())
            {
                // headset plugged in
                Log.i(TAG, "onReceive:headset:plugged in");
                manager.setSpeakerphoneOn(false);
                manager.setWiredHeadsetOn(true);
                Callstate.audio_device = 1;
                Callstate.audio_speaker = false;
                manager.setBluetoothScoOn(false);
            }
            else
            {
                Log.i(TAG, "onReceive:headset:setImageDrawable:null1");
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
            Log.i(TAG, "onReceive:headset:setImageDrawable:null2");
        }

        set_peer_count_header();
        set_peer_names_and_avatars();

        Log.i(TAG, "onCreate:099");
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        do_not_close_on_pause = false;
        Callstate.audio_group_active = true;

        if (conf_id.equals("-1"))
        {
            conf_id = conf_id_prev;
            Log.i(TAG, "onResume:001:conf_id=" + conf_id);
        }

        Log.i(TAG, "onResume");
        super.onResume();

        Log.i(TAG, "onResume:001:conf_id=" + conf_id);

        activity_state = 1;
        push_to_talk_active = false;

        if (Build.VERSION.SDK_INT >= 27)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null)
            {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }
        else
        {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                 WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }


        try
        {
            CallingActivity.ap = new AudioProcessing();
            init_buffers(10, 1, SAMPLE_RATE_FIXED, 1, SAMPLE_RATE_FIXED);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
                audio_thread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioReceiver.stopped)
            {
                AudioReceiver.close();
                audio_receiver_thread.join();
                audio_receiver_thread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (AudioReceiver.stopped)
            {
                audio_receiver_thread = new AudioReceiver();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (AudioRecording.stopped)
            {
                audio_thread = new AudioRecording();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // update every x times per second -----------
        final int update_per_sec = 8;
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                // Log.i(TAG, "update_call_time -> call");
                update_group_audio_bars();
                if (activity_state != 0)
                {
                    ha.postDelayed(this, 1000 / update_per_sec);
                }
            }
        }, 1000 / update_per_sec);
        //        // update every x times per second -----------

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Callstate.audio_group_active = false;

        conf_id = "-1";

        push_to_talk_active = false;
        activity_state = 0;

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioReceiver.stopped)
            {
                AudioReceiver.close();
                audio_receiver_thread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            destroy_buffers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        boolean need_close_activity = true;
        if (do_not_close_on_pause)
        {
            need_close_activity = false;
        }

        Log.i(TAG, "onPause:on_groupaudio_ended_actions");
        on_groupaudio_ended_actions(need_close_activity);
    }

    public void set_conference_connection_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (is_conference_active(conf_id))
                    {
                        ml_icon.setImageResource(R.drawable.circle_green);
                    }
                    else
                    {
                        ml_icon.setImageResource(R.drawable.circle_red);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (conferences_av_handler_s != null)
        {
            conferences_av_handler_s.post(myRunnable);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (conference_message_drawer.isDrawerOpen())
        {
            conference_message_drawer.closeDrawer();
        }
        else
        {
            on_groupaudio_ended_actions(true);
        }
    }

    void update_group_audio_bars()
    {
        try
        {
            if (push_to_talk_active)
            {
                audio_bar_in.setLevel(get_vu_in() / 90.0f);
            }
            else
            {
                audio_bar_in.setLevel(0.0);
            }
            audio_bar_out.setLevel(get_vu_out());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    long peer_pubkey_to_long_in_list(String peer_pubkey)
    {
        long ret = -1L;

        if (lookup_peer_listnum_pubkey.containsKey(peer_pubkey))
        {
            ret = lookup_peer_listnum_pubkey.get(peer_pubkey);
        }

        return ret;
    }

    synchronized void set_peer_count_header()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final String f_name = HelperConference.get_conference_title_from_confid(conf_id);
                final long conference_num = get_conference_num_from_confid(conf_id);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long peer_count = tox_conference_peer_count(conference_num);
                            long frozen_peer_count = tox_conference_offline_peer_count(conference_num);

                            if (peer_count > -1)
                            {
                                ml_maintext.setText(
                                        f_name + "\n" + "Active: " + peer_count + " Offline: " + frozen_peer_count);
                            }
                            else
                            {
                                ml_maintext.setText(f_name);
                                // ml_maintext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            }
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

    synchronized void set_peer_names_and_avatars()
    {
        if (is_conference_active(conf_id))
        {
            Log.d(TAG, "set_peer_names_and_avatars:001");

            try
            {
                remove_group_all_users();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Log.d(TAG, "set_peer_names_and_avatars:002");

            final long conference_num = get_conference_num_from_confid(conf_id);
            long num_peers = tox_conference_peer_count(conference_num);

            Log.d(TAG, "set_peer_names_and_avatars:003:peer count=" + num_peers);

            if (num_peers > 0)
            {
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    String peer_pubkey_temp = tox_conference_peer_get_public_key(conference_num, i);
                    String peer_name_temp = tox_conference_peer_get_name(conference_num, i);
                    if (peer_name_temp.equals(""))
                    {
                        peer_name_temp = null;
                    }
                    // Log.d(TAG, "set_peer_names_and_avatars:004:add:" + peer_name_temp);
                    add_group_user(peer_pubkey_temp, i, peer_name_temp);
                }
            }
        }
    }

    synchronized void remove_group_all_users()
    {
        Log.d(TAG, "remove_group_all_users:001");

        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    lookup_peer_listnum_pubkey.clear();
                                    conference_message_drawer.removeAllItems();
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                }
                            }
                        };

                        if (conferences_av_handler_s != null)
                        {
                            conferences_av_handler_s.post(myRunnable);
                        }

                        // TODO: hack to be synced with additions later
                        Thread.sleep(120);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "remove_group_all_users:T:END");

                }
            };
            t.start();
            t.join();

            Log.d(TAG, "remove_group_all_users:T:099");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "remove_group_user:EE:" + e.getMessage());
        }

        Log.d(TAG, "remove_group_all_users:002");

    }

    synchronized void add_group_user(final String peer_pubkey, final long peernum, String name)
    {
        try
        {
            long peer_num_in_list = peer_pubkey_to_long_in_list(peer_pubkey);
            if (peer_num_in_list == -1)
            {
                // -- ADD --
                String name2 = "";
                if (name != null)
                {
                    name2 = name;
                }
                else
                {
                    name2 = peer_pubkey.substring(peer_pubkey.length() - 5, peer_pubkey.length());
                }

                try
                {
                    name2 = resolve_name_for_pubkey(peer_pubkey, name2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                final String name3 = name2;

                lookup_peer_listnum_pubkey.put(peer_pubkey, peernum);

                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Runnable myRunnable = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        ConferenceCustomDrawerPeerItem new_item = null;
                                        FriendList fl_temp = null;
                                        boolean have_avatar_for_pubkey = false;

                                        try
                                        {
                                            fl_temp = orma.selectFromFriendList().
                                                    tox_public_key_stringEq(peer_pubkey).toList().get(0);

                                            if ((fl_temp.avatar_filename != null) && (fl_temp.avatar_pathname != null))
                                            {
                                                info.guardianproject.iocipher.File f1 = null;
                                                try
                                                {
                                                    f1 = new info.guardianproject.iocipher.File(
                                                            fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename);
                                                    if (f1.length() > 0)
                                                    {
                                                        have_avatar_for_pubkey = true;
                                                    }
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else
                                            {
                                                have_avatar_for_pubkey = false;
                                                fl_temp = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            have_avatar_for_pubkey = false;
                                            // Log.i(TAG, "have_avatar_for_pubkey:00a04:" + have_avatar_for_pubkey);
                                            fl_temp = null;
                                        }

                                        try
                                        {
                                            new_item = new ConferenceCustomDrawerPeerItem(have_avatar_for_pubkey,
                                                                                          peer_pubkey).
                                                    withIdentifier(peernum).
                                                    withName(name3).
                                                    withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                                                    {
                                                        @Override
                                                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                        {
                                                            Intent intent = new Intent(view.getContext(),
                                                                                       ConferencePeerInfoActivity.class);
                                                            intent.putExtra("peer_pubkey", peer_pubkey);
                                                            intent.putExtra("conf_id", conf_id);
                                                            view.getContext().startActivity(intent);
                                                            return true;
                                                        }
                                                    });
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            new_item = new ConferenceCustomDrawerPeerItem(false, null).
                                                    withIdentifier(peernum).
                                                    withName(name3).
                                                    withIcon(GoogleMaterial.Icon.gmd_face).
                                                    withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                                                    {
                                                        @Override
                                                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                                                        {
                                                            Intent intent = new Intent(view.getContext(),
                                                                                       ConferencePeerInfoActivity.class);
                                                            intent.putExtra("peer_pubkey", peer_pubkey);
                                                            intent.putExtra("conf_id", conf_id);
                                                            view.getContext().startActivity(intent);
                                                            return true;
                                                        }
                                                    });
                                        }

                                        // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                        conference_message_drawer.addItem(new_item);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                        Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                    }
                                }
                            };

                            if (conferences_av_handler_s != null)
                            {
                                conferences_av_handler_s.post(myRunnable);
                            }

                        }
                        catch (Exception e3)
                        {
                            e3.printStackTrace();
                            Log.i(TAG, "add_group_user:EE3:" + e3.getMessage());
                        }
                    }
                };
                t.start();
                t.join();
            }
            else
            {
                // -- UPDATE --
                String name2 = "";
                if (name != null)
                {
                    name2 = name;
                }
                else
                {
                    name2 = peer_pubkey.substring(peer_pubkey.length() - 5, peer_pubkey.length());
                }

                try
                {
                    name2 = resolve_name_for_pubkey(peer_pubkey, name2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                final String name3 = name2;

                lookup_peer_listnum_pubkey.put(peer_pubkey, peernum);

                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Runnable myRunnable = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        StringHolder sh = new StringHolder(name3);
                                        // Log.i(TAG, "conference_message_drawer.addItem:1:" + name3 + ":" + peernum);
                                        conference_message_drawer.updateName(peernum, sh);
                                    }
                                    catch (Exception e2)
                                    {
                                        e2.printStackTrace();
                                        Log.i(TAG, "add_group_user:EE2:" + e2.getMessage());
                                    }
                                }
                            };

                            if (conferences_av_handler_s != null)
                            {
                                conferences_av_handler_s.post(myRunnable);
                            }

                        }
                        catch (Exception e3)
                        {
                            e3.printStackTrace();
                            Log.i(TAG, "add_group_user:EE3:" + e3.getMessage());
                        }
                    }
                };
                t.start();
                t.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_group_user:EE:" + e.getMessage());
        }
    }

    public void show_add_friend_conference(View view)
    {
        Log.i(TAG, "show_add_friend_conference");
        Intent intent = new Intent(this, FriendSelectSingleActivity.class);
        intent.putExtra("conf_id", conf_id);
        do_not_close_on_pause = true;
        startActivityForResult(intent, SelectFriendSingleActivity_ID);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
                            Log.i(TAG, "onActivityResult:result_friend_pubkey:" + result_friend_pubkey);

                            long friend_num_temp_safety2 = tox_friend_by_public_key__wrapper(result_friend_pubkey);
                            if (friend_num_temp_safety2 > 0)
                            {
                                Log.i(TAG, "onActivityResult:friend_num_temp_safety2:" + friend_num_temp_safety2);

                                if (conf_id.equals("-1"))
                                {
                                    conf_id = conf_id_prev;
                                    Log.i(TAG, "onActivityResult:001:conf_id=" + conf_id);
                                }

                                final long conference_num = get_conference_num_from_confid(conf_id);

                                Log.i(TAG, "onActivityResult:conference_num:" + conference_num + " conf_id=" + conf_id);

                                if (conference_num > -1)
                                {
                                    int res_conf_invite = tox_conference_invite(friend_num_temp_safety2,
                                                                                conference_num);

                                    Log.i(TAG, "onActivityResult:res_conf_invite:" + res_conf_invite);

                                    if (res_conf_invite < 1)
                                    {
                                        Log.d(TAG,
                                              "onActivityResult:info:tox_conference_invite:ERR:" + res_conf_invite);
                                    }
                                }
                            }
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


    // actions to take when group audio starts:
    static void on_groupaudio_started_actions()
    {
    }

    // actions to take when group audio ends by:
    static void on_groupaudio_ended_actions(boolean close)
    {
        if (close)
        {
            try
            {
                close_conference_audio_activity();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public static void close_conference_audio_activity()
    {
        Callstate.reset_values();
        conf_id = "-1";
        conf_id_prev = "-1";

        // close conference audio activity --------
        caa.finish();
        // close conference audio activity --------
    }
}