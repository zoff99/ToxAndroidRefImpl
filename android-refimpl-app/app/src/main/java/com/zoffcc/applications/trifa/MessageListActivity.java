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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.io.File;

import static com.zoffcc.applications.trifa.CallingActivity.update_top_text_line;
import static com.zoffcc.applications.trifa.MainActivity.CallingActivity_ID;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_filetransfer_db;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.is_friend_online;
import static com.zoffcc.applications.trifa.MainActivity.main_activity_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_typing;
import static com.zoffcc.applications.trifa.MainActivity.update_filetransfer_db_full;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

// import com.vanniktech.emoji.listeners.OnEmojiClickedListener;

public class MessageListActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MsgListActivity";
    long friendnum = -1;
    long friendnum_prev = -1;
    //
    com.vanniktech.emoji.EmojiEditText ml_new_message = null;
    EmojiPopup emojiPopup = null;
    ImageView insert_emoji = null;
    TextView ml_maintext = null;
    ViewGroup rootView = null;
    //
    static TextView ml_friend_typing = null;
    ImageView ml_icon = null;
    ImageView ml_status_icon = null;
    ImageButton ml_phone_icon = null;
    ImageButton ml_button_01 = null;
    int global_typing = 0;
    Thread typing_flag_thread = null;
    final static int TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS = 1000; // 1 second
    static boolean attachemnt_instead_of_send = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);
        Log.i(TAG, "onCreate:003:friendnum=" + friendnum + " friendnum_prev=" + friendnum_prev);
        friendnum_prev = friendnum;

        setContentView(R.layout.activity_message_list);

        MainActivity.message_list_activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootView = (ViewGroup) findViewById(R.id.emoji_bar);
        ml_new_message = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.ml_new_message);
        insert_emoji = (ImageView) findViewById(R.id.insert_emoji);
        ml_friend_typing = (TextView) findViewById(R.id.ml_friend_typing);
        ml_maintext = (TextView) findViewById(R.id.ml_maintext);
        ml_icon = (ImageView) findViewById(R.id.ml_icon);
        ml_status_icon = (ImageView) findViewById(R.id.ml_status_icon);
        ml_phone_icon = (ImageButton) findViewById(R.id.ml_phone_icon);
        ml_button_01 = (ImageButton) findViewById(R.id.ml_button_01);

        final ImageButton button01_ = ml_button_01;

        ml_icon.setImageResource(R.drawable.circle_red);
        set_friend_connection_status_icon();
        ml_status_icon.setImageResource(R.drawable.circle_green);
        set_friend_status_icon();

        setUpEmojiPopup();

        final Drawable d1 = new IconicsDrawable(getBaseContext()).
                icon(FontAwesome.Icon.faw_smile_o).
                color(getResources().
                        getColor(R.color.colorPrimaryDark)).
                sizeDp(80);

        insert_emoji.setImageDrawable(d1);
        // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);


        insert_emoji.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                emojiPopup.toggle();
            }
        });

        final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);

        ml_friend_typing.setText("");
        attachemnt_instead_of_send = true;
        ml_button_01.setImageDrawable(add_attachement_icon);

        ml_new_message.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    attachemnt_instead_of_send = false;
                    button01_.setImageDrawable(send_message_icon);
                }
                else
                {
                    attachemnt_instead_of_send = true;
                    button01_.setImageDrawable(add_attachement_icon);
                }

                // TODO bad hack!
                Log.i(TAG, "TextWatcher:afterTextChanged");
                if (global_typing == 0)
                {
                    global_typing = 1;  // typing = 1

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                tox_self_set_typing(friendnum, global_typing);
                                Log.i(TAG, "typing:fn#" + friendnum + ":activated");
                            }
                            catch (Exception e)
                            {
                                Log.i(TAG, "typing:fn#" + friendnum + ":EE1" + e.getMessage());
                            }
                        }
                    };

                    if (main_handler_s != null)
                    {
                        main_handler_s.post(myRunnable);
                    }
                }

                try
                {
                    typing_flag_thread.interrupt();
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }

                typing_flag_thread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        boolean skip_flag_update = false;
                        try
                        {
                            Thread.sleep(TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS); // sleep for n seconds
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                            // ok, dont update typing flag
                            skip_flag_update = true;
                        }

                        if (global_typing == 1)
                        {
                            if (skip_flag_update == false)
                            {
                                global_typing = 0;  // typing = 0
                                Runnable myRunnable = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            tox_self_set_typing(friendnum, global_typing);
                                            Log.i(TAG, "typing:fn#" + friendnum + ":DEactivated");
                                        }
                                        catch (Exception e)
                                        {
                                            Log.i(TAG, "typing:fn#" + friendnum + ":EE2" + e.getMessage());
                                        }
                                    }
                                };

                                if (main_handler_s != null)
                                {
                                    main_handler_s.post(myRunnable);
                                }
                            }
                        }
                    }
                };
                typing_flag_thread.start();
                // TODO bad hack! sends way too many "typing" messages --------
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // Log.i(TAG,"TextWatcher:beforeTextChanged");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Log.i(TAG,"TextWatcher:onTextChanged");
            }
        });

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        ml_phone_icon.setImageDrawable(d2);

        final long fn = friendnum;
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                final String f_name = MainActivity.get_friend_name_from_num(fn);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ml_maintext.setText(f_name);
                    }
                };

                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
        };
        t.start();

        Log.i(TAG, "onCreate:099");
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        MainActivity.message_list_fragment = null;
        MainActivity.message_list_activity = null;
        Log.i(TAG, "onPause:001:friendnum=" + friendnum);
        friendnum = -1;
        Log.i(TAG, "onPause:002:friendnum=" + friendnum);
    }

    @Override
    protected void onStop()
    {
        if (emojiPopup != null)
        {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        Log.i(TAG, "onResume:001:friendnum=" + friendnum);

        if (friendnum == -1)
        {
            friendnum = friendnum_prev;
            Log.i(TAG, "onResume:001:friendnum=" + friendnum);
        }

        MainActivity.message_list_activity = this;
    }

    private void setUpEmojiPopup()
    {

        //        .setOnEmojiClickedListener(new OnEmojiClickedListener()
        //        {
        //        @Override
        //        public void onEmojiClicked(@NonNull final Emoji emoji)
        //        {
        //            Log.d(TAG, "Clicked on emoji");
        //        }})


        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener()
        {
            @Override
            public void onEmojiBackspaceClick(View v)
            {

            }

        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener()
        {
            @Override
            public void onEmojiPopupShown()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).
                        icon(FontAwesome.Icon.faw_keyboard_o).
                        color(getResources().
                                getColor(R.color.colorPrimaryDark)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.about_icon_email);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener()
        {
            @Override
            public void onKeyboardOpen(@Px final int keyBoardHeight)
            {
                Log.d(TAG, "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener()
        {
            @Override
            public void onEmojiPopupDismiss()
            {
                final Drawable d1 = new IconicsDrawable(getBaseContext()).
                        icon(FontAwesome.Icon.faw_smile_o).
                        color(getResources().
                                getColor(R.color.colorPrimaryDark)).
                        sizeDp(80);

                insert_emoji.setImageDrawable(d1);
                // insert_emoji.setImageResource(R.drawable.emoji_ios_category_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener()
        {
            @Override
            public void onKeyboardClose()
            {
                Log.d(TAG, "Closed soft keyboard");
            }
        }).build(ml_new_message);
    }

    long get_current_friendnum()
    {
        return friendnum;
    }

    public void set_friend_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int tox_user_status_friend = TrifaToxService.orma.selectFromFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                            toList().get(0).TOX_USER_STATUS;

                    if (tox_user_status_friend == 0)
                    {
                        ml_status_icon.setImageResource(R.drawable.circle_green);
                    }
                    else if (tox_user_status_friend == 1)
                    {
                        ml_status_icon.setImageResource(R.drawable.circle_orange);
                    }
                    else
                    {
                        ml_status_icon.setImageResource(R.drawable.circle_red);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:(2):EE:" + e.getMessage());
                }
            }
        };
        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    public void set_friend_connection_status_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (is_friend_online(friendnum) == 0)
                    {
                        ml_icon.setImageResource(R.drawable.circle_red);
                    }
                    else
                    {
                        ml_icon.setImageResource(R.drawable.circle_green);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:(2):EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    synchronized public void send_message_onclick(View view)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";
        try
        {
            if (is_friend_online(friendnum) != 0)
            {
                if (attachemnt_instead_of_send)
                {
                    // add attachement
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.FILE_SELECT;
                    properties.root = new java.io.File("/");
                    properties.error_dir = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    properties.offset = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    // TODO: hardcoded is always bad
                    properties.extensions = new String[]{"jpg", "jpeg", "png", "gif", "JPG", "PNG", "GIF", "zip", "ZIP", "avi", "AVI", "mp4", "MP4"};
                    FilePickerDialog dialog = new FilePickerDialog(this, properties);
                    dialog.setTitle("Select File");

                    dialog.setDialogSelectionListener(new DialogSelectionListener()
                    {
                        @Override
                        public void onSelectedFilePaths(String[] files)
                        {
                            try
                            {
                                Log.i(TAG, "select_file:" + files);
                                final String src_path = new File(new File(files[0]).getAbsolutePath()).getParent();
                                final String src_filename = new File(files[0]).getName();
                                Log.i(TAG, "select_file:p=" + src_path + " f=" + src_filename);

                                final Thread t = new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        add_outgoing_file(src_path, src_filename);
                                    }
                                };
                                t.start();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "select_file:EE1:" + e.getMessage());
                            }
                        }
                    });

                    dialog.show();
                }
                else
                {
                    // send typed message to friend
                    msg = ml_new_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(), ml_new_message.getText().toString().length()));

                    Message m = new Message();
                    m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
                    m.direction = 1; // msg sent
                    m.TOX_MESSAGE_TYPE = 0;
                    m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                    m.rcvd_timestamp = 0L;
                    m.is_new = false; // own messages are always "not new"
                    m.sent_timestamp = System.currentTimeMillis();
                    m.read = false;
                    m.text = msg;

                    if ((msg != null) && (!msg.equalsIgnoreCase("")))
                    {
                        long res = tox_friend_send_message(friendnum, 0, msg);
                        Log.i(TAG, "tox_friend_send_message:result=" + res + " m=" + m);

                        if (res > -1)
                        {
                            m.message_id = res;
                            insert_into_message_db(m, true);
                            ml_new_message.setText("");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            msg = "";
            e.printStackTrace();
        }

        // Log.i(TAG,"send_message_onclick:---end");
    }

    void add_outgoing_file(String filepath, String filename)
    {
        Log.i(TAG, "file_recv:incoming regular file");

        long file_size = -1;
        try
        {
            file_size = new java.io.File(filepath + "/" + filename).length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            // file length "zero"?
            return;
        }

        Filetransfer f = new Filetransfer();
        f.tox_public_key_string = tox_friend_get_public_key__wrapper(friendnum);
        f.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
        f.file_number = -1; // add later when we actually have the number
        f.kind = TOX_FILE_KIND_DATA.value;
        f.state = TOX_FILE_CONTROL_PAUSE.value;
        f.path_name = filepath;
        f.file_name = filename;
        f.filesize = file_size;
        f.ft_accepted = false;
        f.ft_outgoing_started = false;
        f.current_position = 0;

        long ft_id = insert_into_filetransfer_db(f);

        // Message m_tmp = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(3)).orderByMessage_idDesc().get(0);
        Log.i(TAG, "MM2MM:2:" + ft_id);


        // add FT message to UI
        Message m = new Message();

        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
        m.direction = 1; // msg outgoing
        m.TOX_MESSAGE_TYPE = 0;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
        m.filetransfer_id = ft_id;
        m.filedb_id = -1;
        m.state = TOX_FILE_CONTROL_PAUSE.value;
        m.ft_accepted = false;
        m.ft_outgoing_started = false;
        m.filename_fullpath = new java.io.File(filepath + "/" + filename).getAbsolutePath();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = filename + "\n" + file_size + " bytes";

        long new_msg_id = insert_into_message_db(m, true);

        Log.i(TAG, "MM2MM:3:" + new_msg_id);
        Message m_tmp = orma.selectFromMessage().idEq(new_msg_id).get(0);
        Log.i(TAG, "MM2MM:4:" + m.filetransfer_id + "::" + m_tmp);

        f.message_id = new_msg_id;
        update_filetransfer_db_full(f);

        m_tmp = orma.selectFromMessage().idEq(new_msg_id).get(0);
        Log.i(TAG, "MM2MM:5:" + m.filetransfer_id + "::" + m_tmp);


        // --- ??? should we do this here?
        //        try
        //        {
        //            // update "new" status on friendlist fragment
        //            FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
        //            friend_list_fragment.modify_friend(f2, friendnum);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
        //        }
        // --- ??? should we do this here?
    }

    public void start_call_to_friend(View view)
    {
        Log.i(TAG, "start_call_to_friend_real");

        if (!is_tox_started)
        {
            Log.i(TAG, "TOX:offline");
            return;
        }

        if (is_friend_online(friendnum) == 0)
        {
            Log.i(TAG, "TOX:friend offline");
            return;
        }

        final long fn = friendnum;

        // these 2 bitrate values are very strange!! sometimes no video!!
        final int f_audio_enabled = 1;
        final int f_video_enabled = 1;

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Callstate.state == 0)
                    {
                        Log.i(TAG, "CALL:start:(2):show activity");
                        Callstate.state = 1;
                        Callstate.accepted_call = 1; // we started the call, so it's already accepted on our side
                        Callstate.call_first_video_frame_received = -1;
                        Callstate.call_start_timestamp = -1;
                        Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(fn);
                        Callstate.camera_opened = false;
                        Callstate.audio_speaker = true;
                        Callstate.other_audio_enabled = 1;
                        Callstate.other_video_enabled = 1;
                        Callstate.my_audio_enabled = 1;
                        Callstate.my_video_enabled = 1;

                        Intent intent = new Intent(context_s, CallingActivity.class);
                        try
                        {
                            Callstate.friend_name = orma.selectFromFriendList().
                                    tox_public_key_stringEq(Callstate.friend_pubkey).
                                    toList().get(0).name;
                        }
                        catch (Exception e)
                        {
                            Callstate.friend_name = "Unknown";
                            e.printStackTrace();
                        }

                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                Log.i(TAG, "wating for camera open");

                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                                boolean waiting = true;
                                int i = 0;
                                while (waiting)
                                {
                                    i++;
                                    if (Callstate.camera_opened)
                                    {
                                        Log.i(TAG, "Callstate.camera_opened" + Callstate.camera_opened);
                                        waiting = false;
                                    }

                                    if (i > 80)
                                    {
                                        waiting = false;
                                    }

                                    try
                                    {
                                        Thread.sleep(200); // wait a bit
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                try
                                {
                                    CallingActivity.top_text_line_str2 = "0s";
                                    update_top_text_line();
                                    Log.i(TAG, "CALL_OUT:001:friendnum=" + fn + " f_audio_enabled=" + f_audio_enabled + " f_video_enabled=" + f_video_enabled);

                                    Callstate.audio_bitrate = GLOBAL_AUDIO_BITRATE;
                                    Callstate.video_bitrate = GLOBAL_VIDEO_BITRATE;

                                    MainActivity.toxav_call(fn, GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE);
                                    Log.i(TAG, "CALL_OUT:002");
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "CALL_OUT:EE1:" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        };
                        t.start();

                        Callstate.other_audio_enabled = f_audio_enabled;
                        Callstate.other_video_enabled = f_video_enabled;
                        Callstate.call_init_timestamp = System.currentTimeMillis();
                        main_activity_s.startActivityForResult(intent, CallingActivity_ID);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:(2):EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }
}
