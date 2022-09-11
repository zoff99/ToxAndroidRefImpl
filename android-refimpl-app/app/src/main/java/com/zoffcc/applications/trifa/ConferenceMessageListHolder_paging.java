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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.luseen.autolinklibrary.EmojiTextViewLinks;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGeneric.darkenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGeneric.isColorDarkBrightness;
import static com.zoffcc.applications.trifa.MainActivity.PREF__message_paging_num_msgs_per_page;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_EMOJI_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE;

public class ConferenceMessageListHolder_paging extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private static final String TAG = "trifa.MdgLstHdrPaging";

    private ConferenceMessage message_;
    private Context context;

    EmojiTextViewLinks textView;
    ViewGroup textView_container;
    ViewGroup layout_message_container;
    boolean is_selected = false;

    public ConferenceMessageListHolder_paging(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView_container = (ViewGroup) itemView.findViewById(R.id.m_container);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
    }

    public void bindMessageList(ConferenceMessage m)
    {
        message_ = m;
        String message__text = m.text;
        String message__tox_peerpubkey = m.tox_peerpubkey;

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);
        is_selected = false;
        layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        layout_message_container.setOnClickListener(onclick_listener);
        textView.setOnClickListener(onclick_listener);
        textView.setEmojiSize((int) dp2px(MESSAGE_EMOJI_SIZE[PREF__global_font_size]));
        int peer_color_bg = context.getResources().getColor(R.color.material_drawer_background);
        textView.setTextColor(Color.BLACK);

        try
        {
            peer_color_bg = ChatColors.get_shade(
                    ChatColors.PeerAvatarColors[hash_to_bucket(message__tox_peerpubkey, ChatColors.get_size())],
                    message__tox_peerpubkey);
            textView.setTextColor(Color.BLACK);
            if (isColorDarkBrightness(peer_color_bg))
            {
                textView.setTextColor(darkenColor(Color.WHITE, 0.1f));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        textView.setAutoLinkText(message__text);
        // we need to do the rounded corner background manually here, to change the color ---------------
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(
                new float[]{CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX});
        shape.setColor(peer_color_bg);
        textView_container.setBackground(shape);
        // we need to do the rounded corner background manually here, to change the color ---------------

        /*
        // textView_container.setMinimumHeight(4);
        textView_container.setPadding(textView_container.getPaddingLeft(), textView_container.getPaddingTop(),
                                      textView_container.getPaddingRight(),
                                      textView_container.getPaddingBottom()); // left, top, right, bottom
        LinearLayout.LayoutParams parameter = (LinearLayout.LayoutParams) textView_container.getLayoutParams();
        parameter.setMargins(parameter.leftMargin, parameter.topMargin, parameter.rightMargin,
                             parameter.bottomMargin); // left, top, right, bottom
        textView_container.setLayoutParams(parameter);
        */
    }

    @Override
    public void onClick(View v)
    {
        do_click();
    }

    private final View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            do_click();
        }
    };

    private void do_click()
    {
        try
        {
            if (message_.message_id_tox.equals(MESSAGE_PAGING_SHOW_OLDER_HASH))
            {
                if ((ConferenceMessageListFragment.current_page_offset - PREF__message_paging_num_msgs_per_page) < 1)
                {
                    ConferenceMessageListFragment.current_page_offset = 0;
                }
                else
                {
                    ConferenceMessageListFragment.current_page_offset =
                            ConferenceMessageListFragment.current_page_offset - PREF__message_paging_num_msgs_per_page;
                }
                MainActivity.conference_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
            }
            else
            {
                ConferenceMessageListFragment.current_page_offset =
                        ConferenceMessageListFragment.current_page_offset + PREF__message_paging_num_msgs_per_page;
                MainActivity.conference_message_list_fragment.update_all_messages(false, PREF__messageview_paging);
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
