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
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.EmojiTextViewLinks;

public class MessageListHolder_text_incoming_read___unused___ extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{

    /*
     *
     *
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *  --- NOT USED NOW --- !!!!!!!!
     *
     *
     */

    private static final String TAG = "trifa.MessageListHolder";

    private Message message;
    private Context context;

    EmojiTextViewLinks textView;
    ImageView imageView;

    public MessageListHolder_text_incoming_read___unused___(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        // textView.setText("#" + m.id + ":" + m.text);
        textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG, AutoLinkMode.MODE_MENTION);
        textView.setAutoLinkText(m.text);

        //        if (!m.read)
        //        {
        //            // not yet read
        //            imageView.setImageResource(R.drawable.circle_red);
        //        }
        //        else
        //        {
        //            // msg read by other party
        //            imageView.setImageResource(R.drawable.circle_green);
        //        }

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
                    showDialog_url(context, "open URL?", "https://twitter.com/" + matchedText.replaceFirst("^\\s", "").replaceFirst("^@", ""));
                }
                else if (autoLinkMode == AutoLinkMode.MODE_HASHTAG)
                {
                    showDialog_url(context, "open URL?", "https://twitter.com/hashtag/" + matchedText.replaceFirst("^\\s", "").replaceFirst("^#", ""));
                }
            }
        });

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

        final Message m2 = this.message;

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
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email_addr, null));
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
}
