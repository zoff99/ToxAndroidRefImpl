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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.ConferenceMessageListFragment.conf_search_messages_text;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.darkenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGeneric.isColorDarkBrightness;
import static com.zoffcc.applications.trifa.HelperGeneric.lightenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_EMOJI_ONLY_EMOJI_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_EMOJI_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOXURL_PATTERN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;

public class ConferenceMessageListHolder_paging extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private static final String TAG = "trifa.MdgLstHdrPaging";

    private ConferenceMessage message_;
    private Context context;

    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    ViewGroup textView_container;
    ViewGroup layout_peer_name_container;
    TextView peer_name_text;
    ViewGroup layout_message_container;
    boolean is_selected = false;
    boolean is_system_message = false;
    ImageView img_corner;
    int swipe_state = 0;
    int swipe_state_done = 0;
    SwipeLayout swipeLayout = null;

    public ConferenceMessageListHolder_paging(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        textView_container = (ViewGroup) itemView.findViewById(R.id.m_container);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        layout_peer_name_container = (ViewGroup) itemView.findViewById(R.id.layout_peer_name_container);
        peer_name_text = (TextView) itemView.findViewById(R.id.peer_name_text);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
        img_corner = (ImageView) itemView.findViewById(R.id.img_corner);

        swipe_state = 0;
        swipe_state_done = 0;

        swipeLayout = (SwipeLayout) itemView.findViewById(R.id.msg_swipe_container);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
    }

    public void bindMessageList(ConferenceMessage m)
    {
        message_ = m;

        String message__text = m.text;
        String message__tox_peername = m.tox_peername;
        String message__tox_peerpubkey = m.tox_peerpubkey;

        boolean handle_special_name = false;

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);

        // Log.i(TAG, "have_avatar_for_pubkey:0000:==========================");

        is_system_message = message__tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY);
        // Log.i(TAG, "is_system_message=" + is_system_message + " message__tox_peerpubkey=" + message__tox_peerpubkey);

        is_selected = false;
        if (selected_messages.isEmpty())
        {
            is_selected = false;
        }
        else
        {
            is_selected = selected_messages.contains(m.id);
        }

        if (is_selected)
        {
            layout_message_container.setBackgroundColor(Color.GRAY);
        }
        else
        {
            layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        }

        layout_message_container.setOnClickListener(onclick_listener);
        textView.setOnClickListener(onclick_listener);


        // Log.i(TAG, "bindMessageList");

        // textView.setText("#" + m.id + ":" + message__text);
        textView.setCustomRegex(TOXURL_PATTERN);
        textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG,
                                 AutoLinkMode.MODE_MENTION, AutoLinkMode.MODE_CUSTOM);

        try
        {
            String peer_name = tox_conference_peer_get_name__wrapper(m.conference_identifier, message__tox_peerpubkey);

            if (peer_name == null)
            {
                peer_name = message__tox_peername;

                if ((peer_name == null) || (message__tox_peername.equals("")) || (peer_name.equals("-1")))
                {
                    peer_name = "Unknown";
                }
            }
            else
            {
                if (peer_name.equals("-1"))
                {
                    if ((message__tox_peername == null) || (message__tox_peername.equals("")))
                    {
                        peer_name = "Unknown";
                    }
                    else
                    {
                        peer_name = message__tox_peername;
                    }
                }
            }

            layout_peer_name_container.setVisibility(View.VISIBLE);
            try
            {
                peer_name_text.setText(peer_name + " / " +
                                       message__tox_peerpubkey.substring((message__tox_peerpubkey.length() - 6),
                                                                         message__tox_peerpubkey.length()));
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "bindMessageList:EE2:" + e2.getMessage());

                peer_name_text.setText(peer_name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "bindMessageList:EE:" + e.getMessage());
        }

        //        textView.setAutoLinkText("" + message__tox_peerpubkey.substring((message__tox_peerpubkey.length() - 6),
        //                //
        //                message__tox_peerpubkey.length())
        //                //
        //                + ":" + message__text);

        if (com.vanniktech.emoji.EmojiUtils.isOnlyEmojis(message__text))
        {
            // text consits only of emojis -> increase size
            textView.setEmojiSize((int) dp2px(MESSAGE_EMOJI_ONLY_EMOJI_SIZE[PREF__global_font_size]));
        }
        else
        {
            textView.setEmojiSize((int) dp2px(MESSAGE_EMOJI_SIZE[PREF__global_font_size]));
        }

        int peer_color_fg = context.getResources().getColor(R.color.colorPrimaryDark);
        int peer_color_bg = context.getResources().getColor(R.color.material_drawer_background);
        int alpha_value = 160;
        // int peer_color_bg_with_alpha = (peer_color_bg & 0x00FFFFFF) | (alpha_value << 24);

        final int linkcolor = lightenColor(Color.BLUE, 0.3f);
        textView.setMentionModeColor(linkcolor);
        textView.setHashtagModeColor(linkcolor);
        textView.setUrlModeColor(linkcolor);
        textView.setPhoneModeColor(linkcolor);
        textView.setEmailModeColor(linkcolor);
        textView.setCustomModeColor(linkcolor);
        textView.setLinkTextColor(linkcolor);
        //
        textView.setTextColor(Color.BLACK);

        try
        {
            peer_color_bg = ChatColors.get_shade(
                    ChatColors.PeerAvatarColors[hash_to_bucket(message__tox_peerpubkey, ChatColors.get_size())],
                    message__tox_peerpubkey);
            // peer_color_bg_with_alpha = (peer_color_bg & 0x00FFFFFF) | (alpha_value << 24);
            textView.setTextColor(Color.BLACK);

            if (isColorDarkBrightness(peer_color_bg))
            {
                textView.setTextColor(darkenColor(Color.WHITE, 0.1f));
                //
                final int linkcolor_for_other_bg = darkenColor(Color.YELLOW, 0.1f);
                textView.setMentionModeColor(linkcolor_for_other_bg);
                textView.setHashtagModeColor(linkcolor_for_other_bg);
                textView.setUrlModeColor(linkcolor_for_other_bg);
                textView.setPhoneModeColor(linkcolor_for_other_bg);
                textView.setEmailModeColor(linkcolor_for_other_bg);
                textView.setCustomModeColor(linkcolor_for_other_bg);
                textView.setLinkTextColor(linkcolor_for_other_bg);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0))
        {
            textView.setAutoLinkText(message__text);
        }
        else
        {
            textView.setAutoLinkTextHighlight(message__text, conf_search_messages_text);
        }

        date_time.setText(long_date_time_format(m.sent_timestamp));


        boolean have_avatar_for_pubkey = false;
        // Log.i(TAG, "have_avatar_for_pubkey:00a01:" + have_avatar_for_pubkey);


        // we need to do the rounded corner background manually here, to change the color ---------------
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(
                new float[]{CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX});
        shape.setColor(peer_color_bg);
        // shape.setStroke(3, borderColor);
        textView_container.setBackground(shape);
        // we need to do the rounded corner background manually here, to change the color ---------------

        final Drawable smiley_face = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                backgroundColor(peer_color_bg).
                color(peer_color_fg).sizeDp(70);

        have_avatar_for_pubkey = false;

        img_corner.setVisibility(View.GONE);
        date_time.setVisibility(View.VISIBLE);

        if (is_system_message)
        {
            img_avatar.setVisibility(View.GONE);
            img_corner.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            textView_container.setMinimumHeight(4);
            textView_container.setPadding((int) dp2px(4), textView_container.getPaddingTop(), (int) dp2px(4),
                                          textView_container.getPaddingBottom()); // left, top, right, bottom
            LinearLayout.LayoutParams parameter = (LinearLayout.LayoutParams) textView_container.getLayoutParams();
            parameter.setMargins((int) dp2px(20), parameter.topMargin, parameter.rightMargin,
                                 parameter.bottomMargin); // left, top, right, bottom
            textView_container.setLayoutParams(parameter);
            // peer_name_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            peer_name_text.setVisibility(View.GONE);
        }
        else
        {
            // TODO: do we need to reset here? -> yes
            img_avatar.setVisibility(View.VISIBLE);
            if (PREF__compact_chatlist)
            {
                img_corner.setVisibility(View.GONE);
            }
            else
            {
                img_corner.setVisibility(View.VISIBLE);
            }
            imageView.setVisibility(View.VISIBLE);
            textView_container.setMinimumHeight((int) dp2px(0));
            textView_container.setPadding(0, textView_container.getPaddingTop(), 0,
                                          textView_container.getPaddingBottom()); // left, top, right, bottom
            LinearLayout.LayoutParams parameter = (LinearLayout.LayoutParams) textView_container.getLayoutParams();
            parameter.setMargins(0, parameter.topMargin, parameter.rightMargin,
                                 parameter.bottomMargin); // left, top, right, bottom
            textView_container.setLayoutParams(parameter);
            // peer_name_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

            if (!have_avatar_for_pubkey)
            {
                // Log.i(TAG, "have_avatar_for_pubkey:005+" + have_avatar_for_pubkey);
                img_avatar.setImageDrawable(smiley_face);
            }

            if (m.was_synced)
            {
                // synced from ToxProxy
                imageView.setImageResource(R.drawable.circle_orange);
            }
            else
            {
                // received directly
                imageView.setImageResource(R.drawable.circle_green);
            }

            peer_name_text.setVisibility(View.GONE);
            date_time.setVisibility(View.GONE);
        }

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
                if ((ConferenceMessageListFragment.current_page_offset - MESSAGE_PAGING_NUM_MSGS_PER_PAGE) < 1)
                {
                    ConferenceMessageListFragment.current_page_offset = 0;
                }
                else
                {
                    ConferenceMessageListFragment.current_page_offset =
                            ConferenceMessageListFragment.current_page_offset - MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
                }
            }
            else
            {
                ConferenceMessageListFragment.current_page_offset =
                        ConferenceMessageListFragment.current_page_offset + MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
            }
            MainActivity.conference_message_list_fragment.update_all_messages(true, PREF__messageview_paging);
        }
        catch (Exception ignored)
        {
        }
    }
}
