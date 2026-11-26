/*
 * Original conversation UI for TRIfA.
 * SPDX-License-Identifier: GPL-2.0-or-later
 *
 * This program is free software; you may redistribute and/or modify it under
 * the GNU General Public License, version 2 or (at your option) any later version.
 * Distributed without ANY WARRANTY; see LICENSE-zzGPLv2 and LICENSE-GPLv3.
 * All conversation icons and wallpaper in this redesign are original project
 * resources. No third-party logos, artwork, fonts, or proprietary assets are used.
 */
package com.zoffcc.applications.trifa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.sorm.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

/** Presentation only: existing adapters keep ownership of data and click handlers. */
final class ChatUI
{
    static final int FRIEND = 0;
    static final int CONFERENCE = 1;
    static final int GROUP = 2;
    private ChatUI() {}

    static int color(View v, int id)
    {
        return ContextCompat.getColor(v.getContext(), id);
    }

    static boolean night(Context c)
    {
        return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    static int textColor(Context c)
    {
        return ContextCompat.getColor(c, night(c) ? R.color.chat_text_dark : R.color.chat_text);
    }

    static int metadataColor(Context c)
    {
        return ContextCompat.getColor(c, night(c) ? R.color.chat_metadata_dark : R.color.chat_metadata);
    }

    static void setup(final AppCompatActivity a, final int kind, final SearchView search)
    {
        a.getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (kind == FRIEND)
        {
            a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        a.getWindow().setStatusBarColor(color(a.findViewById(R.id.toolbar), night(a) ? R.color.chat_header_dark : R.color.chat_header));
        final View bar = a.findViewById(R.id.chat_search_bar);
        search.setIconifiedByDefault(false);
        search.setQueryHint(a.getString(R.string.chat_search));
        final TextView searchText = search.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null)
        {
            searchText.setTextColor(textColor(a));
            searchText.setHintTextColor(metadataColor(a));
        }
        a.findViewById(R.id.chat_search_close).setOnClickListener(v -> {
            search.setQuery("", false);
            bar.setVisibility(View.GONE);
            search.clearFocus();
        });
        final Runnable openSearch = () -> {
            bar.setVisibility(View.VISIBLE);
            search.setIconified(false);
            search.requestFocus();
        };
        final ImageButton more = a.findViewById(R.id.chat_more);
        more.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(a, v);
            menu.getMenu().add(0, 1, 0, R.string.chat_search);
            if (kind == FRIEND)
            {
                menu.getMenu().add(0, 2, 1, R.string.chat_media_gallery);
                menu.getMenu().add(0, 3, 2, R.string.chat_all_messages);
                menu.getMenu().add(0, 4, 3, R.string.chat_only_files);
            }
            else
            {
                menu.getMenu().add(0, 5, 1, R.string.chat_participants);
                if (kind == GROUP)
                {
                    menu.getMenu().add(0, 2, 2, R.string.chat_media_gallery);
                    menu.getMenu().add(0, 6, 3, a.getString(R.string.add_friend_activity_name));
                }
            }
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId())
                {
                    case 1: openSearch.run(); return true;
                    case 2: a.findViewById(kind == FRIEND ? R.id.gallery_btn : R.id.group_gallery_btn).performClick(); return true;
                    case 3: ((CustomSpinner) a.findViewById(R.id.spinner_filter_msgs)).setSelection(0); return true;
                    case 4: ((CustomSpinner) a.findViewById(R.id.spinner_filter_msgs)).setSelection(1); return true;
                    case 5:
                        if (kind == GROUP) MainActivity.group_message_list_activity.group_message_drawer.openDrawer();
                        else MainActivity.conference_message_list_activity.conference_message_drawer.openDrawer();
                        return true;
                    case 6: a.findViewById(R.id.add_friend_conference).performClick(); return true;
                }
                return false;
            });
            menu.show();
        });
        final View headerText = a.findViewById(R.id.chat_header_text);
        headerText.setOnClickListener(v -> {
            if (kind == FRIEND)
            {
                Intent i = new Intent(a, FriendInfoActivity.class);
                i.putExtra("friendnum", ((MessageListActivity) a).get_current_friendnum());
                a.startActivity(i);
            }
            else more.performClick();
        });
        final TextView oldTitle = a.findViewById(R.id.ml_maintext);
        oldTitle.addTextChangedListener(new SimpleWatcher()
        {
            @Override public void afterTextChanged(Editable s)
            {
                String[] parts = s.toString().split("\\n", 2);
                setHeader(a, parts[0], parts.length > 1 ? parts[1] : "");
            }
        });
        final TextView typing = a.findViewById(R.id.ml_friend_typing);
        typing.addTextChangedListener(new SimpleWatcher()
        {
            @Override public void afterTextChanged(Editable s)
            {
                typing.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        final ImageView avatar = a.findViewById(R.id.chat_avatar);
        if (kind != FRIEND)
        {
            avatar.setImageResource(R.drawable.chat_ic_person);
            avatar.setColorFilter(Color.WHITE);
            a.findViewById(R.id.chat_avatar_badge).setVisibility(View.VISIBLE);
            a.findViewById(R.id.chat_avatar_badge).setContentDescription(a.getString(R.string.chat_participants));
        }
        else
        {
            avatar.setColorFilter(Color.WHITE);
        }
        // Existing controllers update these hidden status views. Mirror their meaning in text.
        final ImageView connection = a.findViewById(R.id.ml_icon);
        connection.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> updatePresence(a, kind));
        setupComposer(a, kind);
        updatePresence(a, kind);
    }

    static void setHeader(Activity a, String title, String subtitle)
    {
        ((TextView) a.findViewById(R.id.chat_title)).setText(title == null ? "" : title);
        ((TextView) a.findViewById(R.id.chat_subtitle)).setText(subtitle == null ? "" : subtitle);
        a.findViewById(R.id.chat_subtitle).setVisibility(subtitle == null || subtitle.isEmpty() ? View.GONE : View.VISIBLE);
    }

    static void friendAvatar(Activity a, String pubkey)
    {
        Message m = new Message();
        m.tox_friendpubkey = pubkey;
        de.hdodenhof.circleimageview.CircleImageView avatar = a.findViewById(R.id.chat_avatar);
        avatar.clearColorFilter();
        HelperGeneric.fill_friend_avatar_icon(m, a, avatar);
    }

    static void updatePresence(Activity a, int kind)
    {
        if (kind != FRIEND) return;
        final ImageView connection = a.findViewById(R.id.ml_icon);
        final ImageView status = a.findViewById(R.id.ml_status_icon);
        // The existing controller sets circle_green/orange/red. Compare resource identity
        // through drawable constant state rather than introducing another network call.
        boolean online = sameDrawable(connection, R.drawable.circle_green);
        int label = R.string.chat_offline;
        if (online)
        {
            if (sameDrawable(status, R.drawable.circle_orange)) label = R.string.chat_away;
            else if (sameDrawable(status, R.drawable.circle_red)) label = R.string.chat_busy;
            else label = R.string.chat_online;
        }
        String subtitle = a.getString(label);
        ((TextView) a.findViewById(R.id.chat_subtitle)).setText(subtitle);
        ((TextView) a.findViewById(R.id.chat_subtitle)).setVisibility(View.VISIBLE);
    }

    private static boolean sameDrawable(ImageView view, int drawable)
    {
        if (view.getDrawable() == null) return false;
        {
            android.graphics.drawable.Drawable d = ContextCompat.getDrawable(view.getContext(), drawable);
            return d != null && d.getConstantState() != null && d.getConstantState().equals(view.getDrawable().getConstantState());
        }
    }

    static void setupComposer(final AppCompatActivity a, int kind)
    {
        final EditText input = a.findViewById(R.id.ml_new_message);
        final ImageButton action = a.findViewById(R.id.ml_button_01);
        final ImageButton emoji = a.findViewById(R.id.insert_emoji);
        emoji.setImageResource(R.drawable.chat_ic_smile);
        emoji.setColorFilter(metadataColor(a));
        if (kind == CONFERENCE)
        {
            action.setImageResource(R.drawable.chat_ic_send);
            action.setColorFilter(Color.WHITE);
            return;
        }
        final ImageButton record = a.findViewById(R.id.ml_button_recaudio);
        record.setImageResource(R.drawable.chat_ic_mic);
        record.setColorFilter(Color.WHITE);
        final ViewGroup outer = (ViewGroup) action.getParent().getParent();
        final ViewGroup inputRow = (ViewGroup) action.getParent();
        final int actionIndex = inputRow.indexOfChild(action);
        final Runnable update = () -> {
            boolean empty = input.getText().toString().trim().isEmpty();
            emoji.setImageResource(R.drawable.chat_ic_smile);
            emoji.setColorFilter(metadataColor(a));
            record.setVisibility(empty ? View.VISIBLE : View.GONE);
            action.setImageResource(empty ? R.drawable.chat_ic_attach : R.drawable.chat_ic_send);
            action.setContentDescription(a.getString(empty ? R.string.chat_attach : R.string.chat_send));
            if (empty && action.getParent() != inputRow)
            {
                outer.removeView(action);
                inputRow.addView(action, actionIndex);
                action.setLayoutParams(new LinearLayout.LayoutParams(dp(a, 42), dp(a, 48)));
            }
            else if (!empty && action.getParent() != outer)
            {
                inputRow.removeView(action);
                outer.addView(action);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(a, 50), dp(a, 50));
                p.setMarginStart(dp(a, 6));
                action.setLayoutParams(p);
            }
            action.setBackgroundResource(empty ? android.R.color.transparent : R.drawable.chat_send_bg);
            action.setColorFilter(empty ? metadataColor(a) : Color.WHITE);
            action.setPadding(dp(a, empty ? 10 : 13), dp(a, empty ? 12 : 13), dp(a, empty ? 10 : 13), dp(a, empty ? 12 : 13));
        };
        input.addTextChangedListener(new SimpleWatcher()
        {
            @Override public void afterTextChanged(Editable s) { update.run(); }
        });
        update.run();
    }

    static int dp(Context c, int value)
    {
        return Math.round(c.getResources().getDisplayMetrics().density * value);
    }

    static String time(Context c, long millis)
    {
        if (millis <= 0) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    /** Local calendar day, not a fixed 24-hour interval. */
    static String date(Context c, long millis)
    {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(millis);
        Calendar today = Calendar.getInstance();
        int dayOfYear = day.get(Calendar.DAY_OF_YEAR);
        int year = day.get(Calendar.YEAR);
        if (sameDay(day, today)) return c.getString(R.string.chat_today);
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (sameDay(day, today)) return c.getString(R.string.chat_yesterday);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
        return DateUtils.formatDateTime(c, millis, flags);
    }

    static boolean sameDay(Calendar a, Calendar b)
    {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    static void select(View container, boolean selected)
    {
        container.setBackgroundColor(Color.TRANSPARENT);
        View surface = container.getParent() instanceof View ? (View) container.getParent() : container;
        View row = surface.getParent() instanceof View ? (View) surface.getParent() : surface;
        row.setBackgroundColor(selected ? color(row, R.color.chat_selection) : Color.TRANSPARENT);
    }

    /** Called AFTER legacy binding, so recycled rows never retain old colors or alignment. */
    static long messageTimestamp(Message m)
    {
        return m.direction != 0 || m.msg_version == 1 ? m.sent_timestamp : m.rcvd_timestamp;
    }

    static long messageTimestamp(com.zoffcc.applications.sorm.ConferenceMessage m)
    {
        return m.sent_timestamp;
    }

    static long messageTimestamp(com.zoffcc.applications.sorm.GroupMessage m)
    {
        return m.sent_timestamp;
    }

    static void bindDate(View row, String current, String previous, long timestamp, boolean outgoing)
    {
        View header = row.findViewById(R.id.message_text_date);
        TextView label = row.findViewById(R.id.message_text_date_string);
        if (header == null || label == null) return;
        boolean show = timestamp > 0 && current != null && !current.isEmpty() && !current.equals(previous);
        header.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) label.setText(current);
    }

    static void decorate(View row, int type, long timestamp, boolean outgoing, boolean read,
                         boolean relay, boolean push, boolean selected, boolean multiUser, boolean synced, int confirmations)
    {
        final Context c = row.getContext();
        final boolean text = type == Message_model.TEXT_INCOMING_NOT_READ || type == Message_model.TEXT_OUTGOING_NOT_READ || type == Message_model.TEXT_OUTGOING_HAVE_READ;
        final boolean system = type == Message_model.PAGING_OLDER || type == Message_model.PAGING_NEWER;
        final View container = row.findViewById(R.id.layout_message_container);
        if (container == null) return;
        select(container, selected);
        final TextView body = row.findViewById(R.id.m_text);
        if (system)
        {
            View header = row.findViewById(R.id.message_text_date);
            if (header != null) header.setVisibility(View.GONE);
            if (container instanceof LinearLayout) ((LinearLayout) container).setGravity(android.view.Gravity.CENTER);
            View pagingBubble = row.findViewById(R.id.m_container);
            if (pagingBubble != null)
            {
                pagingBubble.setBackgroundResource(R.drawable.chat_date_bg);
                pagingBubble.setPadding(dp(c, 12), dp(c, 6), dp(c, 12), dp(c, 6));
            }
            if (body != null) body.setTextColor(metadataColor(c));
            return;
        }
        final TextView date = row.findViewById(R.id.date_time);
        final ImageView icon = row.findViewById(R.id.m_icon);
        final View avatar = row.findViewById(R.id.img_avatar);
        final View corner = row.findViewById(R.id.img_corner);
        if (avatar != null) avatar.setVisibility(View.GONE);
        if (corner != null) corner.setVisibility(View.GONE);
        final View bubble = row.findViewById(outgoing ? R.id.ft_outgoing_rounded_bg : R.id.ft_incoming_rounded_bg) != null
                ? row.findViewById(outgoing ? R.id.ft_outgoing_rounded_bg : R.id.ft_incoming_rounded_bg)
                : row.findViewById(R.id.text_block_group) != null ? row.findViewById(R.id.text_block_group) : row.findViewById(R.id.m_container);
        if (bubble != null)
        {
            bubble.setBackgroundResource(outgoing ? R.drawable.chat_bubble_outgoing : R.drawable.chat_bubble_incoming);
            bubble.setPadding(dp(c, 9), dp(c, 6), dp(c, 9), dp(c, 5));
            bubble.setMinimumHeight(0);
            if (bubble.getLayoutParams() instanceof LinearLayout.LayoutParams)
            {
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) bubble.getLayoutParams();
                p.weight = 0;
                p.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                p.setMargins(dp(c, outgoing ? 48 : 0), 0, dp(c, outgoing ? 0 : 48), 0);
                bubble.setLayoutParams(p);
            }
        }
        if (container instanceof LinearLayout)
        {
            ((LinearLayout) container).setGravity(outgoing ? android.view.Gravity.END : android.view.Gravity.START);
            ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
        }
        // File rows retain their original nested controls; their outer row supplies alignment.
        final View alignment = row.findViewById(R.id.layout2) != null ? row.findViewById(R.id.layout2) : row.findViewById(R.id.layout1);
        if (alignment instanceof LinearLayout)
        {
            ((LinearLayout) alignment).setGravity(outgoing ? android.view.Gravity.END : android.view.Gravity.START);
        }
        if (body != null)
        {
            body.setTextColor(textColor(c));
            body.setLinkTextColor(color(row, night(c) ? R.color.chat_link_dark : R.color.chat_link));
            body.setMaxWidth(Math.max(dp(c, 160), c.getResources().getDisplayMetrics().widthPixels - dp(c, 92)));
        }
        if (date != null)
        {
            date.setVisibility(View.VISIBLE);
            date.setText(time(c, timestamp));
            date.setTextColor(metadataColor(c));
            date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            date.setMaxLines(1);
            if (!text) date.setGravity(android.view.Gravity.END);
        }
        final TextView peer = row.findViewById(R.id.peer_name_text);
        if (peer != null && peer.getVisibility() == View.VISIBLE)
        {
            peer.setTextColor(senderColor(c, peer.getText().toString()));
        }
        if (icon != null)
        {
            if (!outgoing)
            {
                if (text)
                {
                    // Do not replace NGC sync confirmation badges with fake delivery receipts.
                    icon.setVisibility(synced ? View.VISIBLE : View.GONE);
                    if (synced) icon.setContentDescription(c.getString(R.string.chat_sync_confirmations, confirmations));
                }
                else
                {
                    // Incoming transfer-state icons remain meaningful and keep their bind handler.
                    icon.setVisibility(View.VISIBLE);
                    icon.setContentDescription(c.getString(R.string.chat_attachment_state));
                }
            }
            else if (text)
            {
                icon.setVisibility(View.VISIBLE);
                icon.clearColorFilter();
                int label;
                if (multiUser)
                {
                    icon.setImageResource(R.drawable.chat_ic_check);
                    icon.setColorFilter(metadataColor(c));
                    label = R.string.chat_sent;
                }
                else if (read)
                {
                    icon.setImageResource(R.drawable.chat_ic_checks);
                    icon.setColorFilter(color(row, R.color.chat_read));
                    label = R.string.chat_read;
                }
                else if (relay || push)
                {
                    icon.setImageResource(R.drawable.chat_ic_check);
                    icon.setColorFilter(metadataColor(c));
                    label = relay ? R.string.chat_relayed : R.string.chat_push_requested;
                }
                else
                {
                    icon.setImageResource(R.drawable.chat_ic_pending);
                    icon.setColorFilter(metadataColor(c));
                    label = R.string.chat_pending;
                }
                icon.setContentDescription(c.getString(label));
            }
            else
            {
                // Outgoing files show a conservative sent marker; Tox file transfer state is
                // already represented by the existing progress/action controls.
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.chat_ic_check);
                icon.setColorFilter(metadataColor(c));
                icon.setContentDescription(c.getString(R.string.chat_sent));
            }
        }
        final View header = row.findViewById(R.id.message_text_date);
        if (header != null)
        {
            for (int i = 0; i < ((ViewGroup) header).getChildCount(); i++)
            {
                View child = ((ViewGroup) header).getChildAt(i);
                if (child instanceof ImageView) child.setVisibility(View.GONE);
            }
            if (header instanceof LinearLayout) ((LinearLayout) header).setGravity(android.view.Gravity.CENTER);
            TextView label = row.findViewById(R.id.message_text_date_string);
            if (label != null)
            {
                label.setBackgroundResource(R.drawable.chat_date_bg);
                label.setTextColor(metadataColor(c));
                label.setPadding(dp(c, 12), dp(c, 5), dp(c, 12), dp(c, 5));
            }
        }
    }

    static int senderColor(Context c, String name)
    {
        int[] light = {0xFF087A83, 0xFF8A4B08, 0xFF7B3F91, 0xFF286B36, 0xFFA03A52, 0xFF345CA0};
        int[] dark = {0xFF7DD6DC, 0xFFE7B574, 0xFFD2A6E7, 0xFF9AD6A4, 0xFFECA3B4, 0xFFA6BDF0};
        int hash = name == null ? 0 : name.hashCode();
        return (night(c) ? dark : light)[Math.floorMod(hash, light.length)];
    }

    abstract static class SimpleWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}