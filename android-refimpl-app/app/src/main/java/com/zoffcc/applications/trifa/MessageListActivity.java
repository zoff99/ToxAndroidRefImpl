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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_software_aec;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_filetransfer_db;
import static com.zoffcc.applications.trifa.MainActivity.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.is_friend_online;
import static com.zoffcc.applications.trifa.MainActivity.main_activity_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.message_list_activity;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages_text_only;
import static com.zoffcc.applications.trifa.MainActivity.set_filteraudio_active;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_typing;
import static com.zoffcc.applications.trifa.MainActivity.update_filetransfer_db_full;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FILE_PICK_METHOD;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
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
    static final int MEDIAPICK_ID_001 = 8002;
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
    static int global_typing = 0;
    Thread typing_flag_thread = null;
    final static int TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS = 1000; // 1 second
    static boolean attachemnt_instead_of_send = true;
    static ActionMode amode = null;
    static MenuItem amode_save_menu_item = null;

    Handler mla_handler = null;
    static Handler mla_handler_s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:002");

        amode = null;
        amode_save_menu_item = null;
        selected_messages.clear();
        selected_messages_text_only.clear();
        selected_messages_incoming_file.clear();

        mla_handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(android.os.Message m)
            {
                try
                {
                    switch (m.what)
                    {
                        case 1:
                            stop_self_typing_indicator();
                            break;
                        default:
                            super.handleMessage(m);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    super.handleMessage(m);
                }
            }
        };

        mla_handler_s = mla_handler;

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);
        Log.i(TAG, "onCreate:003:friendnum=" + friendnum + " friendnum_prev=" + friendnum_prev);
        friendnum_prev = friendnum;

        setContentView(R.layout.activity_message_list);

        message_list_activity = this;

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
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
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

        final Drawable add_attachement_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attachment).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final Drawable send_message_icon = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_send).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);

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
                // Log.i(TAG, "TextWatcher:afterTextChanged");
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
                                // Log.i(TAG, "typing:fn#" + friendnum + ":activated");
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
                                            // Log.i(TAG, "typing:fn#" + friendnum + ":DEactivated");
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

        final Drawable d2 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_phone).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
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

        stop_self_typing_indicator_s();

        if (emojiPopup != null)
        {
            emojiPopup.dismiss();
        }

        // ** // MainActivity.message_list_fragment = null;
        message_list_activity = null;
        Log.i(TAG, "onPause:001:friendnum=" + friendnum);
        friendnum = -1;
        Log.i(TAG, "onPause:002:friendnum=" + friendnum);
    }

    @Override
    protected void onStop()
    {
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

        message_list_activity = this;
    }

    static void stop_friend_typing_indicator_s()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (message_list_activity != null)
                    {
                        if (ml_friend_typing != null)
                        {
                            ml_friend_typing.setText("");
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "friend_typing_cb:EE.b:" + e.getMessage());
                }
            }
        };

        if (mla_handler_s != null)
        {
            mla_handler_s.post(myRunnable);
        }
    }

    static void stop_self_typing_indicator_s()
    {
        try
        {
            Log.i(TAG, "stop_self_typing_indicator_s");
            android.os.Message m = new android.os.Message();
            m.what = 1;
            mla_handler_s.handleMessage(m);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }

    void stop_self_typing_indicator()
    {
        if (global_typing == 1)
        {
            global_typing = 0;  // typing = 0
            try
            {
                Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":stop_self_typing_indicator");
                tox_self_set_typing(get_current_friendnum(), global_typing);
            }
            catch (Exception e)
            {
                Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":EE2.b" + e.getMessage());
            }
        }
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


        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(
                new OnEmojiBackspaceClickListener()
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
                        icon(FontAwesome.Icon.faw_keyboard).
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
                        icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
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
                    // add attachement ------------
                    // add attachement ------------

                    stop_self_typing_indicator_s();

                    if (FILE_PICK_METHOD == 1)
                    {
                        // Method 1 ----------------
                        // Method 1 ----------------
                        // Method 1 ----------------
                        DialogProperties properties = new DialogProperties();
                        properties.selection_mode = DialogConfigs.SINGLE_MODE;
                        properties.selection_type = DialogConfigs.FILE_SELECT;
                        properties.root = new java.io.File("/");
                        properties.error_dir = new java.io.File(
                                Environment.getExternalStorageDirectory().getAbsolutePath());
                        properties.offset = new java.io.File(
                                Environment.getExternalStorageDirectory().getAbsolutePath());
                        properties.extensions = null;
                        // TODO: hardcoded is always bad
                        // properties.extensions = new String[]{"jpg", "jpeg", "png", "gif", "JPG", "PNG", "GIF", "zip", "ZIP", "avi", "AVI", "mp4", "MP4"};
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
                        // Method 1 ----------------
                        // Method 1 ----------------
                        // Method 1 ----------------
                    }
                    else
                    {
                        // Method 2 ----------------
                        // Method 2 ----------------
                        // Method 2 ----------------
                        //                        Log.i(TAG, "add_file:001");
                        //                        Intent intent_file1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        //                        Log.i(TAG, "add_file:002");
                        //                        try
                        //                        {
                        //                            startActivityForResult(intent_file1, MEDIAPICK_ID_001);
                        //                        }
                        //                        catch (Exception e)
                        //                        {
                        //                            e.printStackTrace();
                        //                            Log.i(TAG, "add_file:EE:" + e.getMessage());
                        //                        }
                        //                        Log.i(TAG, "add_file:003");


                        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                        // browser.
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                        // Filter to only show results that can be "opened", such as a
                        // file (as opposed to a list of contacts or timezones)
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        // Filter to show only images, using the image MIME data type.
                        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                        // To search for all documents available via installed storage providers,
                        // it would be "*/*".
                        // intent.setType("image/*");
                        intent.setType("*/*");

                        startActivityForResult(intent, MEDIAPICK_ID_001);


                        // Method 2 ----------------
                        // Method 2 ----------------
                        // Method 2 ----------------
                    }
                    // add attachement ------------
                    // add attachement ------------
                }
                else
                {
                    // send typed message to friend
                    msg = ml_new_message.getText().toString().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                          ml_new_message.getText().toString().length()));

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
                    m.msg_version = 0;

                    if ((msg != null) && (!msg.equalsIgnoreCase("")))
                    {
                        MainActivity.send_message_result result = tox_friend_send_message_wrapper(friendnum, 0, msg);
                        long res = result.msg_num;
                        // Log.i(TAG, "tox_friend_send_message_wrapper:result=" + res + " m=" + m);

                        if (res > -1) // sending was OK
                        {
                            m.message_id = res;
                            if (!result.msg_hash_hex.equalsIgnoreCase(""))
                            {
                                m.msg_id_hash = result.msg_hash_hex;
                                m.msg_version = 1; // msgV2 message
                            }
                            if (!result.raw_message_buf_hex.equalsIgnoreCase(""))
                            {
                                m.raw_msgv2_bytes = result.raw_message_buf_hex;
                            }
                            long row_id = insert_into_message_db(m, true);
                            m.id = row_id;
                            // Log.i(TAG, "MESSAGEV2_SEND:MSGv2HASH:3=" + m.msg_id_hash);
                            // Log.i(TAG, "MESSAGEV2_SEND:MSGv2HASH:3raw=" + m.raw_msgv2_bytes);
                            ml_new_message.setText("");

                            stop_self_typing_indicator_s();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIAPICK_ID_001 && resultCode == Activity.RESULT_OK)
        {
            if (data == null)
            {
                //Display an error
                return;
            }
            else
            {
                try
                {
                    // -- get real path of file --
                    Uri selectedImage = data.getData();
                    //                    Log.i(TAG, "data_uri=" + selectedImage.toString());
                    //                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //
                    //                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    //                    Log.i(TAG, "data_uri:" + cursor);
                    //                    cursor.moveToFirst();
                    //
                    //                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //                    Log.i(TAG, "data_uri:" + columnIndex);
                    //                    String picturePath = cursor.getString(columnIndex);
                    //                    cursor.close();

                    String picturePath = getPath(this, selectedImage);
                    Log.i(TAG, "data=" + data.getData() + ":" + picturePath);

                    //                    Uri uri = null;
                    //                    if (data != null)
                    //                    {
                    //                        uri = data.getData();
                    //                        Log.i(TAG, "data_uri=" + uri.toString());
                    //                    }
                    //                    String picturePath = "/xxx.png";

                    // -- get real path of file --


                    final String src_path = new File(new File(picturePath).getAbsolutePath()).getParent();
                    final String src_filename = new File(picturePath).getName();
                    Log.i(TAG, "select_file:22:p=" + src_path + " f=" + src_filename);

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
                    Log.i(TAG, "select_file:22:EE1:" + e.getMessage());
                }
            }
            // InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    void add_outgoing_file(String filepath, String filename)
    {
        Log.i(TAG, "add_outgoing_file:regular file");

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

        Log.i(TAG, "add_outgoing_file:friendnum=" + friendnum);

        if (friendnum == -1)
        {
            // ok, we need to wait for onResume to finish and give us the friendnum
            Log.i(TAG, "add_outgoing_file:ok, we need to wait for onResume to finish and give us the friendnum");
            long loop = 0;
            while (loop < 20) // wait 4 sec., then give up
            {
                loop++;
                try
                {
                    Thread.sleep(200);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                if (friendnum > -1)
                {
                    // got friendnum
                    break;
                }
            }
        }

        if (friendnum == -1)
        {
            // sorry, still no friendnum
            Log.i(TAG, "add_outgoing_file:sorry, still no friendnum");
            return;
        }

        Log.i(TAG, "add_outgoing_file:friendnum(2)=" + friendnum);

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

        Log.i(TAG, "add_outgoing_file:tox_public_key_string=" + f.tox_public_key_string);

        long ft_id = insert_into_filetransfer_db(f);
        f.id = ft_id;

        // Message m_tmp = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(3)).orderByMessage_idDesc().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:2:" + ft_id);


        // ---------- DEBUG ----------
        Filetransfer ft_tmp = orma.selectFromFiletransfer().idEq(ft_id).get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4a:" + "fid=" + ft_tmp.id + " mid=" + ft_tmp.message_id);
        // ---------- DEBUG ----------


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
        m.id = new_msg_id;

        // ---------- DEBUG ----------
        Log.i(TAG, "add_outgoing_file:MM2MM:3:" + new_msg_id);
        Message m_tmp = orma.selectFromMessage().idEq(new_msg_id).get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

        f.message_id = new_msg_id;
        // ** // update_filetransfer_db_messageid_from_id(f, ft_id);
        update_filetransfer_db_full(f);

        // ---------- DEBUG ----------
        Filetransfer ft_tmp2 = orma.selectFromFiletransfer().idEq(ft_id).get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4b:" + "fid=" + ft_tmp2.id + " mid=" + ft_tmp2.message_id);
        // ---------- DEBUG ----------

        // ---------- DEBUG ----------
        m_tmp = orma.selectFromMessage().idEq(new_msg_id).get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:5:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

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
                    Log.i(TAG, "CALL:start:(2.0):Callstate.state=" + Callstate.state);

                    if (Callstate.state == 0)
                    {
                        Log.i(TAG, "CALL:start:(2.1):show activity");
                        if (PREF__use_software_aec)
                        {
                            set_filteraudio_active(1);
                        }
                        else
                        {
                            set_filteraudio_active(0);
                        }

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
                        MainActivity.set_av_call_status(Callstate.state);

                        Intent intent = new Intent(context_s, CallingActivity.class);
                        Callstate.friend_alias_name = get_friend_name_from_pubkey(Callstate.friend_pubkey);

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
                                    Log.i(TAG, "CALL_OUT:001:friendnum=" + fn + " f_audio_enabled=" + f_audio_enabled +
                                               " f_video_enabled=" + f_video_enabled);

                                    Callstate.audio_bitrate = GLOBAL_AUDIO_BITRATE;
                                    Callstate.video_bitrate = GLOBAL_VIDEO_BITRATE;
                                    VIDEO_FRAME_RATE_OUTGOING = 0;
                                    last_video_frame_sent = -1;
                                    VIDEO_FRAME_RATE_INCOMING = 0;
                                    last_video_frame_received = -1;
                                    count_video_frame_received = 0;
                                    count_video_frame_sent = 0;

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

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri)
    {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
        {
            Log.i(TAG, "getPath:001:uri=" + uri);

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri))
            {
                Log.i(TAG, "getPath:002");
                final String docId = DocumentsContract.getDocumentId(uri);
                Log.i(TAG, "getPath:003:docId=" + docId);
                final String[] split = docId.split(":");
                Log.i(TAG, "getPath:004:split=" + split[0] + " " + split[1]);
                final String type = split[0];
                Log.i(TAG, "getPath:005:type=" + type);

                if ("primary".equalsIgnoreCase(type))
                {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                else
                {
                    // TODO handle non-primary volumes
                    try
                    {
                        String strSDCardPath = System.getenv("SECONDARY_STORAGE");
                        Log.i(TAG, "getPath:SECONDARY_STORAGE=" + strSDCardPath);

                        if ((strSDCardPath == null) || (strSDCardPath.length() == 0))
                        {
                            Log.i(TAG, "getPath:006");
                            strSDCardPath = System.getenv("EXTERNAL_SDCARD_STORAGE");
                            Log.i(TAG, "getPath:EXTERNAL_SDCARD_STORAGE=" + strSDCardPath);
                        }

                        if (strSDCardPath == null)
                        {
                            // ok, last try
                            strSDCardPath = "/storage/" + type;
                            return strSDCardPath + "/" + split[1];
                        }

                        //If may get a full path that is not the right one, even if we don't have the SD Card there.
                        //We just need the "/mnt/extSdCard/" i.e and check if it's writable
                        Log.i(TAG, "getPath:007");
                        if (strSDCardPath != null)
                        {
                            Log.i(TAG, "getPath:008");
                            if (strSDCardPath.contains(":"))
                            {
                                Log.i(TAG, "getPath:009");
                                strSDCardPath = strSDCardPath.substring(0, strSDCardPath.indexOf(":"));
                            }
                            // File externalFilePath = new File(strSDCardPath);
                            Log.i(TAG, "getPath:strSDCardPath=" + strSDCardPath);

                            return strSDCardPath + "/" + split[1];
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "getPath:EE3:" + e.getMessage());
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                                                  Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
    {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    static boolean onClick_message_helper(final View v, boolean is_selected, final Message message_)
    {
        try
        {
            if (is_selected)
            {
                v.setBackgroundColor(Color.TRANSPARENT);
                is_selected = false;
                selected_messages.remove(message_.id);
                selected_messages_text_only.remove(message_.id);
                selected_messages_incoming_file.remove(message_.id);
                if (selected_messages_incoming_file.size() == selected_messages.size())
                {
                    amode_save_menu_item.setVisible(true);
                }
                else
                {
                    amode_save_menu_item.setVisible(false);
                }

                if (selected_messages.isEmpty())
                {
                    // last item was de-selected
                    amode.finish();
                }
                else
                {
                    if (amode != null)
                    {
                        amode.setTitle("" + selected_messages.size() + " selected");
                    }
                }
            }
            else
            {
                if (!selected_messages.isEmpty())
                {
                    v.setBackgroundColor(Color.GRAY);
                    is_selected = true;
                    selected_messages.add(message_.id);
                    if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                    {
                        selected_messages_text_only.add(message_.id);
                    }
                    else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (message_.direction == 0)
                        {
                            selected_messages_incoming_file.add(message_.id);
                        }
                    }

                    if (selected_messages_incoming_file.size() == selected_messages.size())
                    {
                        amode_save_menu_item.setVisible(true);
                    }
                    else
                    {
                        amode_save_menu_item.setVisible(false);
                    }

                    if (amode != null)
                    {
                        amode.setTitle("" + selected_messages.size() + " selected");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return is_selected;
    }

    static class long_click_message_return
    {
        boolean is_selected;
        boolean ret_value;
    }

    static long_click_message_return onLongClick_message_helper(Context context, final View v, boolean is_selected, final Message message_)
    {
        long_click_message_return ret = new long_click_message_return();

        try
        {
            if (is_selected)
            {
            }
            else
            {
                if (selected_messages.isEmpty())
                {
                    try
                    {
                        amode = message_list_activity.startSupportActionMode(new ToolbarActionMode(context));
                        amode_save_menu_item = amode.getMenu().findItem(R.id.action_save);
                        v.setBackgroundColor(Color.GRAY);
                        ret.is_selected = true;
                        selected_messages.add(message_.id);
                        if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_TYPE_TEXT.value)
                        {
                            selected_messages_text_only.add(message_.id);
                        }
                        else if (message_.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                        {
                            if (message_.direction == 0)
                            {
                                selected_messages_incoming_file.add(message_.id);
                            }
                        }

                        if (selected_messages_incoming_file.size() == selected_messages.size())
                        {
                            amode_save_menu_item.setVisible(true);
                        }
                        else
                        {
                            amode_save_menu_item.setVisible(false);
                        }

                        if (amode != null)
                        {
                            amode.setTitle("" + selected_messages.size() + " selected");
                        }
                        ret.ret_value = true;
                        return ret;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ret.ret_value = false;
        return ret;
    }
}
