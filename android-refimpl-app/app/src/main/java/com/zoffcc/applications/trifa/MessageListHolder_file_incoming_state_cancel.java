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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import info.guardianproject.iocipher.File;

import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_TMP_DIR;
import static com.zoffcc.applications.trifa.MainActivity.StringSignature2;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.copy_vfs_file_to_real_file;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;

public class MessageListHolder_file_incoming_state_cancel extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message2;
    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    ProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    TextView textView;

    public MessageListHolder_file_incoming_state_cancel(View itemView, Context c)
    {
        super(itemView);

        Log.i(TAG, "MessageListHolder");

        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (ProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        textView = (TextView) itemView.findViewById(R.id.m_text);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindMessageList(Message m)
    {
        Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        final Message message = m;

        button_ok.setVisibility(View.GONE);
        button_cancel.setVisibility(View.GONE);
        ft_progressbar.setVisibility(View.GONE);
        ft_buttons_container.setVisibility(View.GONE);

        final Message message2 = message;

        if (message.filedb_id == -1)
        {
            textView.setText("" + message.text + "\n *canceled*");
            ft_preview_image.setImageDrawable(null);
            ft_preview_container.setVisibility(View.GONE);
            ft_preview_image.setVisibility(View.GONE);
        }
        else
        {
            // TODO: show preview and "click" to open/delete file
            textView.setText("" + message.text + "\n +OK+");

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

            Log.i(TAG, "getView:033:STATE:CANCEL:OK:is_image=" + is_image);

            if (is_image)
            {

                //                final Drawable d3 = new IconicsDrawable(this.context).
                //                        icon(GoogleMaterial.Icon.gmd_photo).
                //                        backgroundColor(Color.TRANSPARENT).
                //                        color(Color.parseColor("#AA000000")).sizeDp(50);

                // ft_preview_image.setImageDrawable(d3);
                ft_preview_image.setImageResource(R.drawable.round_loading_animation);
                // final ImageButton ft_preview_image_ = ft_preview_image;

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


                    // TODO: this is just to show that it works. really bad and slow!!!!!
                    //final Thread t_image_preview = new Thread()
                    //{
                        //@Override
                       // public void run()
                       // {
                            info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(message2.filename_fullpath);
                            // final String temp_file_name = copy_vfs_file_to_real_file(f2.getParent(), f2.getName(), SD_CARD_TMP_DIR, "_3");
                            //Log.i(TAG, "glide:loadData:000a:temp_file_name=" + temp_file_name);

                            //final Runnable myRunnable = new Runnable()
                           // {
                               // @Override
                               // public void run()
                               // {
                                    try
                                    {
                                        Log.i(TAG, "glide:img:001");

                                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(new RoundedCorners((int) dp2px(20)));
                                        // apply(glide_options).

                                        GlideApp.
                                                with(ft_preview_image.getContext()).
                                                load(f2).
                                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                                skipMemoryCache(false).
                                                placeholder(R.drawable.round_loading_animation).
                                                into(ft_preview_image);
                                        Log.i(TAG, "glide:img:002");

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                //}
                            //};

                            //if (main_handler_s != null)
                            //{
                            //    main_handler_s.post(myRunnable);
                            //}
                      //  }
                    //};
                    //t_image_preview.start();
                    // TODO: this is just to show that it work. really bad and slow!!!!!
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

            ft_preview_container.setVisibility(View.VISIBLE);
            ft_preview_image.setVisibility(View.VISIBLE);
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

        // sfinal Message m2 = this.message;

        return true;
    }
}
