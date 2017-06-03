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
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URLConnection;
import java.util.List;

import info.guardianproject.iocipher.File;

import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_TMP_DIR;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.copy_vfs_file_to_real_file;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.get_filetransfer_filenum_from_id;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_accepted_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_accepted_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessagelistArrayAdapter extends ArrayAdapter<Message>
{
    private static final String TAG = "trifa.MessagelstAAdptr";
    private final Context context;
    private final List<Message> values_msg;

    public MessagelistArrayAdapter(Context context, List<Message> values)
    {
        super(context, -1, values);
        this.context = context;
        this.values_msg = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        Log.i(TAG, "getView:001:pos=" + position + " data size=" + values_msg.size());
        Log.i(TAG, "getView:001:pos=" + position + " data=" + values_msg.get(position));

        Log.i(TAG, "getView:001:pos=" + position + " data=" + values_msg.get(position));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = null;

        Log.i(TAG, "getView:001.c:" + values_msg.get(position).direction + ":" + values_msg.get(position).state);

        try
        {
            if (values_msg.get(position).TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
            {
                // FILE -------------
                if (values_msg.get(position).direction == 0)
                {
                    // incoming file
                    if (values_msg.get(position).state == TOX_FILE_CONTROL_CANCEL.value)
                    {
                        // ------- STATE: CANCEL -------------
                        Log.i(TAG, "getView:033:STATE:CANCEL:" + values_msg.get(position).text + ":" + values_msg.get(position).filetransfer_id + " " + values_msg.get(position).message_id + " " + values_msg.get(position).filedb_id);

                        rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);

                        ImageButton button_ok = (ImageButton) rowView.findViewById(R.id.ft_button_ok);

                        ImageButton button_cancel = (ImageButton) rowView.findViewById(R.id.ft_button_cancel);
                        button_ok.setVisibility(View.GONE);
                        button_cancel.setVisibility(View.GONE);

                        ProgressBar ft_progressbar = (ProgressBar) rowView.findViewById(R.id.ft_progressbar);
                        ft_progressbar.setVisibility(View.GONE);

                        final ViewGroup ft_preview_container = (ViewGroup) rowView.findViewById(R.id.ft_preview_container);
                        final ViewGroup ft_buttons_container = (ViewGroup) rowView.findViewById(R.id.ft_buttons_container);
                        final ImageButton ft_preview_image = (ImageButton) rowView.findViewById(R.id.ft_preview_image);

                        ft_buttons_container.setVisibility(View.GONE);

                        TextView textView = (TextView) rowView.findViewById(R.id.m_text);
                        if (values_msg.get(position).filedb_id == -1)
                        {
                            textView.setText("" + values_msg.get(position).text + "\n *canceled*");
                        }
                        else
                        {
                            // TODO: show preview and "click" to open/delete file
                            textView.setText("" + values_msg.get(position).text + "\n +OK+");

                            boolean is_image = false;
                            try
                            {
                                String mimeType = URLConnection.guessContentTypeFromName(values_msg.get(position).filename_fullpath.toLowerCase());
                                if (mimeType.startsWith("image"))
                                {
                                    is_image = true;
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            Log.i(TAG, "getView:033:STATE:CANCEL:OK:is_image=" + is_image);

                            if (is_image)
                            {

                                final Drawable d3 = new IconicsDrawable(parent.getContext()).
                                        icon(GoogleMaterial.Icon.gmd_photo).
                                        backgroundColor(Color.TRANSPARENT).
                                        color(Color.parseColor("#AA000000")).sizeDp(50);

                                ft_preview_image.setImageDrawable(d3);

                                if (VFS_ENCRYPT)
                                {
                                    ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                                    {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event)
                                        {
                                            if (event.getAction() == MotionEvent.ACTION_UP)
                                            {
                                                try
                                                {
                                                    Intent intent = new Intent(v.getContext(), ImageviewerActivity.class);
                                                    intent.putExtra("image_filename", values_msg.get(position).filename_fullpath);
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


                                    // TODO: this is just to show that it work. really bad and slow!!!!!
                                    final View v_ = rowView;
                                    final Thread t_image_preview = new Thread()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                                            }
                                            catch (Exception e)
                                            {
                                            }

                                            info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(values_msg.get(position).filename_fullpath);
                                            final String temp_file_name = copy_vfs_file_to_real_file(f2.getParent(), f2.getName(), SD_CARD_TMP_DIR, "_3");
                                            Log.i(TAG, "glide:loadData:000a:temp_file_name=" + temp_file_name);

                                            //  load(new info.guardianproject.iocipher.File(values_msg.get(position).filename_fullpath)).

                                            final Runnable myRunnable = new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    try
                                                    {
                                                        Log.i(TAG, "glide:img:001");

                                                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(new RoundedCorners((int) dp2px(40)));
                                                        GlideApp.
                                                                with(v_).
                                                                load(new File(SD_CARD_TMP_DIR + "/" + temp_file_name)).
                                                                diskCacheStrategy(DiskCacheStrategy.NONE).
                                                                skipMemoryCache(false).
                                                                placeholder(d3).
                                                                listener(new com.bumptech.glide.request.RequestListener<Drawable>()
                                                                {
                                                                    @Override
                                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
                                                                    {
                                                                        Log.i(TAG, "glide:onResourceReady:model=" + model);

                                                                        try
                                                                        {
                                                                            java.io.File f = (java.io.File) model;
                                                                            f.delete();
                                                                            Log.i(TAG, "glide:cleanup:001");
                                                                        }
                                                                        catch (Exception e2)
                                                                        {
                                                                            e2.printStackTrace();
                                                                            Log.i(TAG, "glide:onResourceReady:EE:" + e2.getMessage());
                                                                        }

                                                                        return false;
                                                                    }

                                                                    @Override
                                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource)
                                                                    {
                                                                        Log.i(TAG, "glide:onLoadFailed:model=" + model);

                                                                        try
                                                                        {
                                                                            java.io.File f = (java.io.File) model;
                                                                            f.delete();
                                                                            Log.i(TAG, "glide:cleanup:002");
                                                                        }
                                                                        catch (Exception e2)
                                                                        {
                                                                            e2.printStackTrace();
                                                                            Log.i(TAG, "glide:onLoadFailed:EE:" + e2.getMessage());
                                                                        }

                                                                        return false;
                                                                    }

                                                                }).
                                                                apply(glide_options).
                                                                into(ft_preview_image);
                                                        Log.i(TAG, "glide:img:002");

                                                    }
                                                    catch (Exception e)
                                                    {
                                                        e.printStackTrace();

                                                        try
                                                        {
                                                            java.io.File f = new java.io.File(SD_CARD_TMP_DIR + "/" + temp_file_name);
                                                            f.delete();
                                                            Log.i(TAG, "glide:cleanup:003");
                                                        }
                                                        catch (Exception e2)
                                                        {
                                                            e2.printStackTrace();
                                                            Log.i(TAG, "glide:cleanup:EE2:" + e2.getMessage());
                                                        }

                                                    }
                                                }
                                            };
                                            main_handler_s.post(myRunnable);
                                        }
                                    };
                                    t_image_preview.start();
                                    // TODO: this is just to show that it work. really bad and slow!!!!!
                                }
                            }
                            else
                            {
                                final Drawable d3 = new IconicsDrawable(parent.getContext()).
                                        icon(GoogleMaterial.Icon.gmd_attachment).
                                        backgroundColor(Color.TRANSPARENT).
                                        color(Color.parseColor("#AA000000")).sizeDp(50);

                                ft_preview_image.setImageDrawable(d3);
                            }

                            ft_preview_container.setVisibility(View.VISIBLE);
                            ft_preview_image.setVisibility(View.VISIBLE);
                        }
                        // ------- STATE: CANCEL -------------
                    }
                    else if (values_msg.get(position).state == TOX_FILE_CONTROL_PAUSE.value)
                    {
                        // ------- STATE: PAUSE -------------
                        if (values_msg.get(position).ft_accepted == false)
                        {
                            Log.i(TAG, "getView:033:STATE:PAUSE:!ft_accepted:" + values_msg.get(position).text + ":" + values_msg.get(position).filetransfer_id + " " + values_msg.get(position).message_id + " " + values_msg.get(position).filedb_id);

                            // not yet accepted
                            rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);

                            final ImageButton button_ok = (ImageButton) rowView.findViewById(R.id.ft_button_ok);
                            final Drawable d1 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
                            button_ok.setImageDrawable(d1);

                            final ImageButton button_cancel = (ImageButton) rowView.findViewById(R.id.ft_button_cancel);
                            final Drawable d2 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
                            button_cancel.setImageDrawable(d2);

                            final ProgressBar ft_progressbar = (ProgressBar) rowView.findViewById(R.id.ft_progressbar);

                            final ViewGroup ft_buttons_container = (ViewGroup) rowView.findViewById(R.id.ft_buttons_container);
                            ft_buttons_container.setVisibility(View.VISIBLE);

                            button_ok.setVisibility(View.VISIBLE);
                            button_cancel.setVisibility(View.VISIBLE);

                            button_ok.setOnTouchListener(new View.OnTouchListener()
                            {
                                @Override
                                public boolean onTouch(View v, MotionEvent event)
                                {
                                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                                    {
                                        try
                                        {
                                            // accept FT
                                            set_filetransfer_accepted_from_id(values_msg.get(position).filetransfer_id);
                                            set_filetransfer_state_from_id(values_msg.get(position).filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                                            set_message_accepted_from_id(values_msg.get(position).id);
                                            set_message_state_from_id(values_msg.get(position).id, TOX_FILE_CONTROL_RESUME.value);
                                            tox_file_control(tox_friend_by_public_key__wrapper(values_msg.get(position).tox_friendpubkey), get_filetransfer_filenum_from_id(values_msg.get(position).filetransfer_id), TOX_FILE_CONTROL_RESUME.value);

                                            ft_progressbar.setProgress(0);
                                            ft_progressbar.setMax(100);
                                            ft_progressbar.setIndeterminate(true);
                                            ft_progressbar.setVisibility(View.VISIBLE);
                                            button_ok.setVisibility(View.GONE);

                                            // update message view
                                            update_single_message_from_messge_id(values_msg.get(position).id, true);

                                            Log.i(TAG, "button_ok:OnTouch:009");
                                        }
                                        catch (Exception e)
                                        {
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
                                            // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                                            tox_file_control(tox_friend_by_public_key__wrapper(values_msg.get(position).tox_friendpubkey), get_filetransfer_filenum_from_id(values_msg.get(position).filetransfer_id), TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(values_msg.get(position).filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(values_msg.get(position).id, TOX_FILE_CONTROL_CANCEL.value);

                                            button_ok.setVisibility(View.GONE);
                                            button_cancel.setVisibility(View.GONE);
                                            ft_progressbar.setVisibility(View.GONE);

                                            // update message view
                                            update_single_message_from_messge_id(values_msg.get(position).id, true);
                                        }
                                        catch (Exception e)
                                        {
                                        }
                                    }
                                    else
                                    {
                                    }
                                    return true;
                                }
                            });

                            TextView textView = (TextView) rowView.findViewById(R.id.m_text);

                            // TODO: make text betters
                            textView.setText("" + values_msg.get(position).text + "\n Accept File?");

                            ft_progressbar.setIndeterminate(true);
                            ft_progressbar.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            // has accepted
                            Log.i(TAG, "getView:033:STATE:PAUSE:ft_accepted:" + values_msg.get(position).text + ":" + values_msg.get(position).filetransfer_id + " " + values_msg.get(position).message_id + " " + values_msg.get(position).filedb_id);

                            rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);

                            TextView textView = (TextView) rowView.findViewById(R.id.m_text);
                            final ImageButton button_ok = (ImageButton) rowView.findViewById(R.id.ft_button_ok);
                            final Drawable d1 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
                            button_ok.setImageDrawable(d1);
                            button_ok.setVisibility(View.GONE);

                            final ImageButton button_cancel = (ImageButton) rowView.findViewById(R.id.ft_button_cancel);
                            final Drawable d2 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
                            button_cancel.setImageDrawable(d2);

                            final ProgressBar ft_progressbar = (ProgressBar) rowView.findViewById(R.id.ft_progressbar);

                            final ViewGroup ft_buttons_container = (ViewGroup) rowView.findViewById(R.id.ft_buttons_container);
                            ft_buttons_container.setVisibility(View.VISIBLE);

                            button_ok.setVisibility(View.GONE);
                            button_cancel.setVisibility(View.VISIBLE);

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
                                            // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                                            tox_file_control(tox_friend_by_public_key__wrapper(values_msg.get(position).tox_friendpubkey), get_filetransfer_filenum_from_id(values_msg.get(position).filetransfer_id), TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(values_msg.get(position).filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(values_msg.get(position).id, TOX_FILE_CONTROL_CANCEL.value);

                                            button_ok.setVisibility(View.GONE);
                                            button_cancel.setVisibility(View.GONE);
                                            ft_progressbar.setVisibility(View.GONE);

                                            // update message view
                                            update_single_message_from_messge_id(values_msg.get(position).id, true);
                                        }
                                        catch (Exception e)
                                        {
                                        }
                                    }
                                    else
                                    {
                                    }
                                    return true;
                                }
                            });


                            // TODO: make text betters
                            textView.setText("" + values_msg.get(position).text + "\n PAUSED");
                        }
                        // ------- STATE: PAUSE -------------
                    }
                    else
                    {
                        // ------- STATE: RESUME -------------

                        Log.i(TAG, "getView:033:STATE:RESUME:" + values_msg.get(position).text + ":" + values_msg.get(position).filetransfer_id + " " + values_msg.get(position).message_id + " " + values_msg.get(position).filedb_id);

                        rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);

                        final ImageButton button_ok = (ImageButton) rowView.findViewById(R.id.ft_button_ok);
                        final Drawable d1 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
                        button_ok.setImageDrawable(d1);
                        button_ok.setVisibility(View.GONE);

                        final ImageButton button_cancel = (ImageButton) rowView.findViewById(R.id.ft_button_cancel);
                        final Drawable d2 = new IconicsDrawable(parent.getContext()).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
                        button_cancel.setImageDrawable(d2);

                        final ProgressBar ft_progressbar = (ProgressBar) rowView.findViewById(R.id.ft_progressbar);
                        TextView textView = (TextView) rowView.findViewById(R.id.m_text);

                        final ViewGroup ft_buttons_container = (ViewGroup) rowView.findViewById(R.id.ft_buttons_container);
                        ft_buttons_container.setVisibility(View.VISIBLE);

                        button_cancel.setVisibility(View.VISIBLE);

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
                                        // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                                        tox_file_control(tox_friend_by_public_key__wrapper(values_msg.get(position).tox_friendpubkey), get_filetransfer_filenum_from_id(values_msg.get(position).filetransfer_id), TOX_FILE_CONTROL_CANCEL.value);
                                        set_filetransfer_state_from_id(values_msg.get(position).filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                                        set_message_state_from_id(values_msg.get(position).id, TOX_FILE_CONTROL_CANCEL.value);

                                        button_ok.setVisibility(View.GONE);
                                        button_cancel.setVisibility(View.GONE);
                                        ft_progressbar.setVisibility(View.GONE);

                                        // update message view
                                        update_single_message_from_messge_id(values_msg.get(position).id, true);
                                    }
                                    catch (Exception e)
                                    {
                                    }
                                }
                                else
                                {
                                }
                                return true;
                            }
                        });


                        // TODO:
                        long ft_id = values_msg.get(position).filetransfer_id;
                        Log.i(TAG, "getView:033:STATE:RESUME:ft_id=" + ft_id);
                        if (ft_id != -1)
                        {
                            final Filetransfer ft_ = orma.selectFromFiletransfer().idEq(ft_id).get(0);
                            final int percent = (int) (100f * (float) ft_.current_position / (float) ft_.filesize);
                            Log.i(TAG, "getView:033:STATE:RESUME:percent=" + percent + " cur=" + ft_.current_position + " size=" + ft_.filesize);
                            ft_progressbar.setProgress(percent);
                            // TODO: make text betters
                            textView.setText("" + values_msg.get(position).text + "\n" + ft_.current_position + "/" + ft_.filesize + "\n receiving ...");
                        }
                        else
                        {
                            ft_progressbar.setProgress(0);
                            // TODO: make text betters
                            textView.setText("" + values_msg.get(position).text + "\n receiving ...");
                        }

                        ft_progressbar.setMax(100);
                        ft_progressbar.setIndeterminate(false);
                        // ------- STATE: RESUME -------------
                    }
                }
                else
                {
                    // outgoing file

                    Log.i(TAG, "getView:033:outgoing_file:" + values_msg.get(position).filetransfer_id + " " + values_msg.get(position).message_id + " " + values_msg.get(position).filedb_id);

                    rowView = inflater.inflate(R.layout.message_list_ft_incoming, parent, false);
                }
                // FILE -------------
            }
            else
            {
                // TEXT -------------
                if (values_msg.get(position).direction == 0)
                {
                    // msg to me
                    if (values_msg.get(position).read)
                    {
                        rowView = inflater.inflate(R.layout.message_list_entry_read, parent, false);
                    }
                    else
                    {
                        rowView = inflater.inflate(R.layout.message_list_entry, parent, false);
                    }
                }
                else
                {
                    // msg from me
                    if (values_msg.get(position).read)
                    {
                        rowView = inflater.inflate(R.layout.message_list_self_entry_read, parent, false);
                    }
                    else
                    {
                        rowView = inflater.inflate(R.layout.message_list_self_entry, parent, false);
                    }
                }

                TextView textView = (TextView) rowView.findViewById(R.id.m_text);
                textView.setText("#" + values_msg.get(position).id + ":" + values_msg.get(position).text);

                ImageView imageView = (ImageView) rowView.findViewById(R.id.m_icon);

                if (!values_msg.get(position).read)
                {
                    // not yet read
                    imageView.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    // msg read by other party
                    imageView.setImageResource(R.drawable.circle_green);
                }
                // TEXT -------------
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "getView:EE1:" + e.getMessage());
        }

        if (rowView == null)
        {
            // should never get here, you missed something about!!
            rowView = inflater.inflate(R.layout.message_list_error, parent, false);
        }

        Log.i(TAG, "getView:099:rowView=" + rowView);

        return rowView;
    }
}
