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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.add_friend_real;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MessageListActivity.onClick_message_helper;
import static com.zoffcc.applications.trifa.MessageListActivity.onLongClick_message_helper;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOXURL_PATTERN;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListHolder_text_incoming_not_read extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message_;
    private Context context;

    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    ViewGroup layout_message_container;
    boolean is_selected = false;
    TextView message_text_date_string;
    ViewGroup message_text_date;

    public MessageListHolder_text_incoming_not_read(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
        message_text_date_string = (TextView) itemView.findViewById(R.id.message_text_date_string);
        message_text_date = (ViewGroup) itemView.findViewById(R.id.message_text_date);
    }

    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        message_ = m;

        is_selected = false;
        if (selected_messages.isEmpty())
        {
            is_selected = false;
        }
        else
        {
            if (selected_messages.contains(m.id))
            {
                is_selected = true;
            }
            else
            {
                is_selected = false;
            }
        }

        if (is_selected)
        {
            layout_message_container.setBackgroundColor(Color.GRAY);
        }
        else
        {
            layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        }

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
                        message_text_date_string.setText(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                        message_text_date.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if (!MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position).equals(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position - 1)))
                        {
                            message_text_date_string.setText(
                                    MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                            message_text_date.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        layout_message_container.setOnClickListener(onclick_listener);
        layout_message_container.setOnLongClickListener(onlongclick_listener);


        // textView.setText("#" + m.id + ":" + m.text);
        textView.setCustomRegex(TOXURL_PATTERN);
        textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG,
                                 AutoLinkMode.MODE_MENTION, AutoLinkMode.MODE_CUSTOM);

        if (com.vanniktech.emoji.EmojiUtils.isOnlyEmojis(m.text))
        {
            // text consits only of emojis -> increase size
            textView.setEmojiSize((int) dp2px(36 * 2));
        }
        else
        {
            textView.setEmojiSize((int) dp2px(36));
        }

        textView.setAutoLinkText(m.text);

        final String unicode_PERSONAL_COMPUTER = "\uD83D\uDCBB";
        final String unicode_INCOMING_ENVELOPE = "\uD83D\uDCE8";
        final String unicode_Mobile_Phone_With_Arrow = "\uD83D\uDCF2";
        final String unicode_MEMO = "\uD83D\uDCDD";
        final String unicode_ARROW_LEFT="←";

        if (m.msg_version == 1)
        {
            date_time.setText(
                    unicode_ARROW_LEFT + long_date_time_format(m.sent_timestamp) + "\n" + unicode_Mobile_Phone_With_Arrow +
                    long_date_time_format(m.rcvd_timestamp));
        }
        else
        {
            date_time.setText(long_date_time_format(m.rcvd_timestamp));
        }

        textView.setAutoLinkOnClickListener(new AutoLinkOnClickListener()
        {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText)
            {
                if (autoLinkMode == AutoLinkMode.MODE_URL)
                {
                    showDialog_url(context, "open URL?", matchedText.replaceFirst("^\\s", ""));
                }
                else if (autoLinkMode == AutoLinkMode.MODE_EMAIL)
                {
                    showDialog_email(context, "send Email?", matchedText.replaceFirst("^\\s", ""));
                }
                else if (autoLinkMode == AutoLinkMode.MODE_MENTION)
                {
                    showDialog_url(context, "open URL?", "https://twitter.com/" +
                                                         matchedText.replaceFirst("^\\s", "").replaceFirst("^@", ""));
                }
                else if (autoLinkMode == AutoLinkMode.MODE_HASHTAG)
                {
                    showDialog_url(context, "open URL?", "https://twitter.com/hashtag/" +
                                                         matchedText.replaceFirst("^\\s", "").replaceFirst("^#", ""));
                }
                else if (autoLinkMode == AutoLinkMode.MODE_CUSTOM) // tox: urls
                {
                    showDialog_tox(context, "add ToxID?", matchedText.replaceFirst("^\\s", ""));
                }
            }
        });


        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(
                context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                FriendList fl = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).get(0);

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    // info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                    if (f1.length() > 0)
                    {
                        // byte[] byteArray = new byte[(int) f1.length()];
                        // fis.read(byteArray, 0, (int) f1.length());
                        // fis.close();

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
    }

    @Override
    public void onClick(View v)
    {
        // Log.i(TAG, "onClick");
    }

    @Override
    public boolean onLongClick(final View v)
    {
        // Log.i(TAG, "onLongClick");
        return true;
    }

    private void showDialog_url(final Context c, final String title, final String url1)
    {
        String url2 = url1;

        // check to see if protocol is specified in URL, otherwise add "http://"
        if (!url2.contains("://"))
        {
            url2 = "http://" + url1;
        }
        final String url = url2;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(url).setTitle(title).
                setCancelable(false).
                setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        try
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            c.startActivity(intent);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialog_email(final Context c, final String title, final String email_addr)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(email_addr).setTitle(title).
                setCancelable(false).
                setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        try
                        {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                                                            Uri.fromParts("mailto", email_addr, null));
                            emailIntent.setType("message/rfc822");
                            // emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                            // emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                            c.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialog_tox(final Context c, final String title, final String toxid)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(toxid.toUpperCase()).setTitle(title).
                setCancelable(false).
                setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        try
                        {
                            String friend_tox_id = toxid.toUpperCase().replace(" ", "").replaceFirst("tox:",
                                                                                                     "").replaceFirst(
                                    "TOX:", "").replaceFirst("Tox:", "");
                            add_friend_real(friend_tox_id);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }


    private View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            is_selected = onClick_message_helper(v, is_selected, message_);
        }
    };

    private View.OnLongClickListener onlongclick_listener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(final View v)
        {
            MessageListActivity.long_click_message_return res = onLongClick_message_helper(context, v, is_selected,
                                                                                           message_);
            is_selected = res.is_selected;
            return res.ret_value;
        }
    };

}
