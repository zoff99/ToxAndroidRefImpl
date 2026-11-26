/*
 * Original main conversation-list presentation helper for TRIfA.
 * SPDX-License-Identifier: GPL-2.0-or-later
 *
 * This program is free software; you may redistribute and/or modify it under
 * the GNU General Public License, version 2 or (at your option) any later version.
 * Distributed without ANY WARRANTY; see LICENSE-zzGPLv2 and LICENSE-GPLv3.
 */
package com.zoffcc.applications.trifa;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shared decoration for the combined friend/conference/group list. It keeps the
 * legacy row IDs and click/swipe behavior, while applying a flat original
 * conversation-list style after the existing holders bind their data.
 */
final class MainListUI
{
    private MainListUI()
    {
    }

    static void decorate(View row, int type)
    {
        if (row == null) return;
        final Context c = row.getContext();

        View card = row.findViewById(R.id.outside_friend_info_container);
        if (card == null) card = row.findViewById(R.id.f_conf_container_parent);
        if (card != null)
        {
            card.setBackground(null);
            card.setElevation(0f);
        }

        View line = row.findViewById(R.id.friend_line_container);
        if (line == null) line = row.findViewById(R.id.f_conf_container_parent);
        if (line != null)
        {
            line.setBackgroundColor(Color.TRANSPARENT);
            line.setElevation(0f);
            line.setPadding(dp(c, 20), dp(c, 8), dp(c, 20), dp(c, 8));
            selectable(line);
        }

        final TextView name = row.findViewById(R.id.f_name);
        if (name != null)
        {
            name.setTextColor(color(c, R.color.main_list_text));
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setMaxLines(1);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setPadding(0, 0, 0, 0);
        }

        final TextView status = row.findViewById(R.id.f_status_message);
        if (status != null)
        {
            status.setTextColor(color(c, R.color.main_list_metadata));
            status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            status.setTypeface(Typeface.DEFAULT);
            status.setMaxLines(1);
            status.setEllipsize(TextUtils.TruncateAt.END);
            status.setBackground(null);
            status.setPadding(0, 0, 0, 0);
        }

        final TextView ip = row.findViewById(R.id.f_ip_addr_text);
        if (ip != null) ip.setVisibility(View.GONE);

        final TextView last = row.findViewById(R.id.f_last_online_timestamp);
        if (last != null)
        {
            last.setTextColor(color(c, R.color.main_list_metadata));
            last.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            last.setTypeface(Typeface.DEFAULT);
            last.setMaxLines(1);
            last.setEllipsize(TextUtils.TruncateAt.END);
            last.setMaxWidth(dp(c, 120));
            last.setBackground(null);
            last.setPadding(0, 0, 0, 0);
        }

        final TextView unread = row.findViewById(R.id.f_unread_count);
        if (unread != null)
        {
            if (unread.getVisibility() != View.VISIBLE || TextUtils.isEmpty(unread.getText()) || "0".contentEquals(unread.getText()))
            {
                unread.setVisibility(View.GONE);
            }
            unread.setBackgroundResource(R.drawable.main_list_unread_badge);
            unread.setTextColor(color(c, R.color.main_list_badge_text));
            unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            unread.setTypeface(Typeface.DEFAULT_BOLD);
            unread.setGravity(android.view.Gravity.CENTER);
            unread.setMinWidth(dp(c, 20));
            unread.setMinHeight(dp(c, 20));
            unread.setPadding(dp(c, 4), 0, dp(c, 4), 0);
            ViewGroup.LayoutParams lp = unread.getLayoutParams();
            if (lp != null)
            {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.height = dp(c, 20);
                unread.setLayoutParams(lp);
            }
        }

        final ImageView notification = row.findViewById(R.id.f_notification);
        if (notification != null)
        {
            notification.setAlpha(0.72f);
            ViewGroup.LayoutParams lp = notification.getLayoutParams();
            if (lp != null)
            {
                lp.width = dp(c, 16);
                lp.height = dp(c, 16);
                notification.setLayoutParams(lp);
            }
        }

        final ImageView presence = row.findViewById(R.id.f_status_icon);
        if (presence != null) presence.setAlpha(0.95f);

        final ImageView relay = row.findViewById(R.id.f_relay_icon);
        if (relay != null) relay.setAlpha(0.9f);

        final ImageView userStatus = row.findViewById(R.id.f_user_status_icon);
        if (userStatus != null) userStatus.setAlpha(0.9f);

        final ImageView avatar = row.findViewById(R.id.f_avatar_icon);
        if (avatar instanceof de.hdodenhof.circleimageview.CircleImageView)
        {
            ((de.hdodenhof.circleimageview.CircleImageView) avatar).setBorderWidth(0);
        }
    }

    private static void selectable(View v)
    {
        try
        {
            TypedValue tv = new TypedValue();
            if (v.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true))
            {
                v.setBackgroundResource(tv.resourceId);
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private static int color(Context c, int res)
    {
        return c.getResources().getColor(res);
    }

    private static int dp(Context c, int value)
    {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, c.getResources().getDisplayMetrics()));
    }
}
