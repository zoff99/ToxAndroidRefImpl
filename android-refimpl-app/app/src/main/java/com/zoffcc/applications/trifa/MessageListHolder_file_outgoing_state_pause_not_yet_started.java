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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URLConnection;
import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.get_filetransfer_filenum_from_id;
import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_own_avatar;
import static com.zoffcc.applications.trifa.MainActivity.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_start_sending_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_start_sending_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_send;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.update_filetransfer_db_full;
import static com.zoffcc.applications.trifa.MainActivity.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListHolder_file_outgoing_state_pause_not_yet_started extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHldr01";

    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    com.daimajia.numberprogressbar.NumberProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    TextView message_text_date_string;
    ViewGroup message_text_date;

    public MessageListHolder_file_outgoing_state_pause_not_yet_started(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (com.daimajia.numberprogressbar.NumberProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        message_text_date_string = (TextView) itemView.findViewById(R.id.message_text_date_string);
        message_text_date = (ViewGroup) itemView.findViewById(R.id.message_text_date);
    }

    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        date_time.setText(long_date_time_format(m.sent_timestamp));

        final Message message = m;

        textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG, AutoLinkMode.MODE_MENTION);

        ft_progressbar.setVisibility(View.GONE);
        ft_buttons_container.setVisibility(View.VISIBLE);
        ft_preview_container.setVisibility(View.VISIBLE);
        ft_preview_image.setVisibility(View.VISIBLE);

        final Message message2 = message;

        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        message_text_date.setVisibility(View.GONE);
        int my_position = this.getAdapterPosition();
        if (my_position != RecyclerView.NO_POSITION)
        {
            if (MainActivity.message_list_fragment != null)
            {
                if (MainActivity.message_list_fragment.adapter != null)
                {
                    if (my_position < 1)
                    {
                        message_text_date_string.setText(MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                        message_text_date.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if (!MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position).equals(MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position - 1)))
                        {
                            message_text_date_string.setText(MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                            message_text_date.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------


        final Drawable d1 = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_check_circle).
                backgroundColor(Color.TRANSPARENT).
                color(Color.parseColor("#EF088A29")).sizeDp(50);
        button_ok.setImageDrawable(d1);
        final Drawable d2 = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_highlight_off).
                backgroundColor(Color.TRANSPARENT).
                color(Color.parseColor("#A0FF0000")).sizeDp(50);
        button_cancel.setImageDrawable(d2);
        ft_buttons_container.setVisibility(View.VISIBLE);

        button_ok.setVisibility(View.VISIBLE);
        button_cancel.setVisibility(View.VISIBLE);

        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                String fname = get_vfs_image_filename_own_avatar();

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fname);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fname != null))
                {
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(context).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        textView.setAutoLinkText("" + message.text + "\n\nSend this file?");

        boolean is_image = false;
        try
        {
            String mimeType = URLConnection.guessContentTypeFromName(message.filename_fullpath.toLowerCase());
            if (mimeType.startsWith("image"))
            {
                is_image = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        button_ok.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try
                    {
                        Log.i(TAG, "MM2MM:7:mid=" + message.id + " ftid:" + message.filetransfer_id);

                        // accept FT
                        set_message_start_sending_from_id(message.id);
                        set_filetransfer_start_sending_from_id(message.filetransfer_id);

                        button_ok.setVisibility(View.GONE);

                        // update message view
                        update_single_message_from_messge_id(message.id, true);

                        Filetransfer ft = orma.selectFromFiletransfer().
                                idEq(message.filetransfer_id).
                                orderByIdDesc().get(0);

                        Log.i(TAG, "MM2MM:8:ft.filesize=" + ft.filesize + " ftid=" + ft.id + " ft.mid=" + ft.message_id + " mid=" + message.id);

                        // ------ DEBUG ------
                        Log.i(TAG, "MM2MM:8a:ft full=" + ft);
                        // ------ DEBUG ------

                        // -------- DEBUG --------
                        //                        List<Filetransfer> ft_res = orma.selectFromFiletransfer().
                        //                                tox_public_key_stringEq(message.tox_friendpubkey).
                        //                                orderByIdDesc().
                        //                                limit(30).toList();
                        //                        int ii;
                        //                        Log.i(TAG, "file_recv_control:SQL:9:===============================================");
                        //                        for (ii = 0; ii < ft_res.size(); ii++)
                        //                        {
                        //                            Log.i(TAG, "file_recv_control:SQL:9:" + ft_res.get(ii));
                        //                        }
                        //                        Log.i(TAG, "file_recv_control:SQL:9:===============================================");
                        // -------- DEBUG --------


                        // -------- DEBUG --------
                        //                        ft_res = orma.selectFromFiletransfer().
                        //                                orderByIdDesc().
                        //                                limit(30).toList();
                        //                        Log.i(TAG, "file_recv_control:SQL:A:===============================================");
                        //                        for (ii = 0; ii < ft_res.size(); ii++)
                        //                        {
                        //                            Log.i(TAG, "file_recv_control:SQL:A:" + ft_res.get(ii));
                        //                        }
                        //                        Log.i(TAG, "file_recv_control:SQL:A:===============================================");
                        // -------- DEBUG --------

                        ByteBuffer file_id_buffer = ByteBuffer.allocateDirect(TOX_FILE_ID_LENGTH);
                        byte[] sha256_buf = TrifaSetPatternActivity.sha256(TrifaSetPatternActivity.StringToBytes2("" + ft.path_name + ":" + ft.file_name + ":" + ft.filesize));

                        Log.i(TAG, "TOX_FILE_ID_LENGTH=" + TOX_FILE_ID_LENGTH + " sha_byte=" + sha256_buf.length);

                        file_id_buffer.put(sha256_buf);

                        // actually start sending the file to friend
                        long file_number = tox_file_send(tox_friend_by_public_key__wrapper(message.tox_friendpubkey), ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value, ft.filesize, file_id_buffer, ft.file_name, ft.file_name.length());

                        Log.i(TAG, "MM2MM:9:new filenum=" + file_number);

                        // update the tox file number in DB -----------
                        ft.file_number = file_number;
                        update_filetransfer_db_full(ft);
                        // update the tox file number in DB -----------

                        Log.i(TAG, "button_ok:OnTouch:009:f_num=" + file_number);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "MM2MM:EE1:" + e.getMessage());
                    }
                }
                else
                {
                }
                return true;
            }
        });


        button_cancel.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try
                    {
                        // cancel FT
                        Log.i(TAG, "button_cancel:OnTouch:001");
                        int res = tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey), get_filetransfer_filenum_from_id(message.filetransfer_id), TOX_FILE_CONTROL_CANCEL.value);
                        Log.i(TAG, "button_cancel:OnTouch:res=" + res);
                        set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                        set_message_state_from_id(message.id, TOX_FILE_CONTROL_CANCEL.value);

                        button_ok.setVisibility(View.GONE);
                        button_cancel.setVisibility(View.GONE);
                        ft_progressbar.setVisibility(View.GONE);

                        // update message view
                        update_single_message_from_messge_id(message.id, true);
                        Log.i(TAG, "button_cancel:OnTouch:099");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "button_cancel:OnTouch:EE:" + e.getMessage());
                    }
                }
                else
                {
                }
                return true;
            }
        });


        if (is_image)
        {
            ft_preview_image.setImageResource(R.drawable.round_loading_animation);

            ft_preview_image.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        try
                        {
                            Intent intent = new Intent(v.getContext(), ImageviewerActivity_SD.class);
                            intent.putExtra("image_filename", message2.filename_fullpath);
                            v.getContext().startActivity(intent);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "open_attachment_intent:EE:" + e.getMessage());
                        }
                    }
                    else
                    {
                    }
                    return true;
                }
            });


            java.io.File f2 = new java.io.File(message2.filename_fullpath);
            try
            {
                final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(new RoundedCorners((int) dp2px(20)));

                GlideApp.
                        with(context).
                        load(f2).
                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        skipMemoryCache(false).
                        priority(Priority.LOW).
                        placeholder(R.drawable.round_loading_animation).
                        into(ft_preview_image);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            final Drawable d3 = new IconicsDrawable(this.context).
                    icon(GoogleMaterial.Icon.gmd_attachment).
                    backgroundColor(Color.TRANSPARENT).
                    color(Color.parseColor("#AA000000")).sizeDp(50);

            ft_preview_image.setImageDrawable(d3);
        }
    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onClick:EE:" + e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(final View v)
    {
        Log.i(TAG, "onLongClick");

        // final Message m2 = this.message;

        //        PopupMenu menu = new PopupMenu(v.getContext(), v);
        //        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        //        {
        //            @Override
        //            public boolean onMenuItemClick(MenuItem item)
        //            {
        //                int id = item.getItemId();
        //                return true;
        //            }
        //        });
        //        menu.inflate(R.menu.menu_friendlist_item);
        //        menu.show();

        return true;
    }
}
