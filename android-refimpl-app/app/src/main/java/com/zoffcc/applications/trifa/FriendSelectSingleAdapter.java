/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.l4digital.fastscroll.FastScroller;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.GroupDB;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.is_nightmode_active;
import static com.zoffcc.applications.trifa.HelperGroup.group_identifier_short;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.Identicon.create_avatar_identicon_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_UDP;

public class FriendSelectSingleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements FastScroller.SectionIndexer
{
    private static final String TAG = "trifa.FrndSelSglAdptr";

    private static final int VIEW_TYPE_FRIEND = 0;
    private static final int VIEW_TYPE_GROUP = 1;
    private static final int VIEW_TYPE_SEPARATOR = 2;
    private static final int VIEW_TYPE_EMPTY_INFO = 3;

    private final Context context;
    private final List<FriendSelectSingle> datalist;
    private final OnEntrySelectedListener listener;

    public interface OnEntrySelectedListener
    {
        void onEntrySelected(FriendSelectSingle entry);
    }

    public FriendSelectSingleAdapter(@NonNull Context context, @NonNull List<FriendSelectSingle> input_datalist,
                                     @Nullable OnEntrySelectedListener listener)
    {
        this.context = context;
        this.datalist = input_datalist;
        this.listener = listener;
        setHasStableIds(false);
    }

    // ------------------------------------------------------------------
    // view holders
    // ------------------------------------------------------------------
    private static class EntryViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewName;
        de.hdodenhof.circleimageview.CircleImageView avatar;
        ImageView f_status_icon;
        ImageView f_relay_icon;      // friend layout only
        TextView f_group_subtext;    // group layout only

        EntryViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.f_avatar_icon);
            f_status_icon = (ImageView) itemView.findViewById(R.id.f_status_icon);
            f_relay_icon = (ImageView) itemView.findViewById(R.id.f_relay_icon);
            f_group_subtext = (TextView) itemView.findViewById(R.id.f_group_subtext);
        }
    }

    private static class SeparatorViewHolder extends RecyclerView.ViewHolder
    {
        SeparatorViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }

    private static class EmptyInfoViewHolder extends RecyclerView.ViewHolder
    {
        ImageView icon;
        TextView text;

        EmptyInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.empty_info_icon);
            text = (TextView) itemView.findViewById(R.id.empty_info_text);
        }
    }

    // ------------------------------------------------------------------
    // adapter basics
    // ------------------------------------------------------------------
    @Override
    public int getItemCount()
    {
        return (datalist != null) ? datalist.size() : 0;
    }

    @Override
    public int getItemViewType(int position)
    {
        final int type = datalist.get(position).getType();

        if (type == FriendSelectSingle.TYPE_EMPTY_INFO)
        {
            return VIEW_TYPE_EMPTY_INFO;
        }
        else if (type == FriendSelectSingle.TYPE_SEPARATOR)
        {
            return VIEW_TYPE_SEPARATOR;
        }
        else if (type == FriendSelectSingle.TYPE_NGC_GROUP)
        {
            return VIEW_TYPE_GROUP;
        }
        return VIEW_TYPE_FRIEND;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_EMPTY_INFO)
        {
            return new EmptyInfoViewHolder(inflater.inflate(R.layout.friend_select_single_empty_info_view,
                                                            parent, false));
        }
        else if (viewType == VIEW_TYPE_SEPARATOR)
        {
            // placeholder, real content is built in onBindViewHolder (label differs per section)
            final LinearLayout container = new LinearLayout(parent.getContext());
            container.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.setOrientation(LinearLayout.VERTICAL);
            return new SeparatorViewHolder(container);
        }
        else if (viewType == VIEW_TYPE_GROUP)
        {
            return new EntryViewHolder(inflater.inflate(R.layout.friend_select_single_group_list_item_view,
                                                        parent, false));
        }

        return new EntryViewHolder(inflater.inflate(R.layout.friend_select_single_single_list_item_view_custom,
                                                    parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        final FriendSelectSingle entry = datalist.get(position);

        if (holder instanceof EmptyInfoViewHolder)
        {
            final EmptyInfoViewHolder e = (EmptyInfoViewHolder) holder;
            e.text.setText(entry.getName());
            try
            {
                final Drawable info_icon = new IconicsDrawable(context).
                        icon(GoogleMaterial.Icon.gmd_info_outline).
                        color(context.getResources().getColor(R.color.friend_list_text_color)).sizeDp(32);
                e.icon.setImageDrawable(info_icon);
            }
            catch (Exception ignored)
            {
            }
            e.itemView.setOnClickListener(null);
            e.itemView.setClickable(false);
            return;
        }

        if (holder instanceof SeparatorViewHolder)
        {
            final LinearLayout container = (LinearLayout) holder.itemView;
            container.removeAllViews();
            container.addView(build_separator_view(entry.getName()));
            return;
        }

        final EntryViewHolder h = (EntryViewHolder) holder;
        h.textViewName.setText(entry.getName());

        if (getItemViewType(position) == VIEW_TYPE_GROUP)
        {
            bind_group(h, entry);
        }
        else
        {
            bind_friend(h, entry.friend_item);
        }

        h.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener == null)
                {
                    return;
                }
                final int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                {
                    return;
                }
                final FriendSelectSingle clicked = datalist.get(pos);
                if ((clicked.getType() == FriendSelectSingle.TYPE_SEPARATOR) ||
                    (clicked.getType() == FriendSelectSingle.TYPE_EMPTY_INFO))
                {
                    return;
                }
                listener.onEntrySelected(clicked);
            }
        });
    }

    /** fastscroller bubble text */
    @NonNull
    @Override
    public CharSequence getSectionText(int position)
    {
        try
        {
            final FriendSelectSingle e = datalist.get(position);
            if (e.section_text != null)
            {
                return e.section_text;
            }
        }
        catch (Exception ignored)
        {
        }
        return " ";
    }

    // ------------------------------------------------------------------
    // friend row
    // ------------------------------------------------------------------
    private void bind_friend(@NonNull EntryViewHolder holder, @Nullable FriendList fl)
    {
        if (fl == null)
        {
            holder.f_status_icon.setVisibility(View.INVISIBLE);
            holder.f_relay_icon.setVisibility(View.INVISIBLE);
            set_connection_border(holder.avatar, TOX_CONNECTION_NONE.value, 4);
            return;
        }

        final de.hdodenhof.circleimageview.CircleImageView avatar = holder.avatar;
        final Drawable d_lock = new IconicsDrawable(context).
                icon(FontAwesome.Icon.faw_lock).color(context.getResources().
                getColor(R.color.colorPrimaryDark)).sizeDp(80);

        // ------ now fill with data ------
        avatar.setImageDrawable(d_lock);
        try
        {
            if (VFS_ENCRYPT)
            {
                boolean need_create_identicon = true;

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
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(avatar.getContext()).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                placeholder(d_lock).
                                priority(Priority.HIGH).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(avatar);

                        need_create_identicon = false;
                    }
                    else
                    {
                        avatar.setImageDrawable(d_lock);
                    }
                }

                if (need_create_identicon)
                {
                    // no avatar icon? create and use Identicon ------------
                    create_avatar_identicon_for_pubkey(fl.tox_public_key_string);

                    // -- ok, now try to show the avtar icon again --
                    String new_avatar_pathname = VFS_PREFIX + VFS_FILE_DIR + "/" + fl.tox_public_key_string + "/";
                    String new_avatar_filename = FRIEND_AVATAR_FILENAME;
                    f1 = null;
                    try
                    {
                        f1 = new info.guardianproject.iocipher.File(new_avatar_pathname + "/" + new_avatar_filename);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if ((f1 != null) && (new_avatar_pathname != null))
                    {
                        if (f1.length() > 0)
                        {
                            final RequestOptions glide_options = new RequestOptions().fitCenter();
                            GlideApp.
                                    with(avatar.getContext()).
                                    load(f1).
                                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                    signature(new com.bumptech.glide.signature.StringSignatureZ(
                                    "_avatar_" + new_avatar_pathname + "/" + FRIEND_AVATAR_FILENAME + "_" +
                                    fl.avatar_update_timestamp)).
                                    placeholder(d_lock).
                                    priority(Priority.HIGH).
                                    skipMemoryCache(false).
                                    apply(glide_options).
                                    into(avatar);
                        }
                        else
                        {
                            // ok still nothing, show that default "lock" icon
                            avatar.setImageDrawable(d_lock);
                        }
                    }
                    // -- ok, now try to show the avtar icon again --
                }
            } // VFS_ENCRYPT -- END --
            else
            {
                java.io.File f1 = null;
                try
                {
                    f1 = new java.io.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                    byte[] byteArray = new byte[(int) f1.length()];
                    fis.read(byteArray, 0, (int) f1.length());
                    fis.close();

                    final RequestOptions glide_options = new RequestOptions().fitCenter();
                    GlideApp.
                            with(context).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            signature(new com.bumptech.glide.signature.StringSignatureZ(
                            "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                            fl.avatar_update_timestamp)).
                            skipMemoryCache(false).
                            apply(glide_options).
                            into(avatar);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // ------ status dots (friend + relay/push), same semantics as main friend list ------
        holder.f_status_icon.setVisibility(View.VISIBLE);
        holder.f_relay_icon.setVisibility(View.INVISIBLE);

        try
        {
            final String relay_ = get_relay_for_friend(fl.tox_public_key_string);
            FriendList relay_fl = null;

            if (relay_ != null) // friend HAS a relay
            {
                relay_fl = main_get_friend(tox_friend_by_public_key__wrapper(relay_));
            }

            if (relay_fl != null)
            {
                if (fl.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
                {
                    holder.f_status_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    holder.f_status_icon.setImageResource(R.drawable.circle_green);
                }

                if (relay_fl.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
                {
                    holder.f_relay_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    holder.f_relay_icon.setImageResource(R.drawable.circle_green);
                }

                holder.f_status_icon.setVisibility(View.VISIBLE);
                holder.f_relay_icon.setVisibility(View.VISIBLE);
            }
            else // no relay (or relay not in DB)
            {
                final String push_url = get_pushurl_for_friend(fl.tox_public_key_string);

                if ((push_url != null) && (push_url.length() > "https:".length()))
                {
                    // friend has push support
                    holder.f_relay_icon.setImageResource(R.drawable.circle_orange);
                    holder.f_relay_icon.setVisibility(View.VISIBLE);
                }

                if (fl.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    holder.f_status_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    holder.f_status_icon.setImageResource(R.drawable.circle_green);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        set_connection_border(holder.avatar, fl.TOX_CONNECTION_real, 4);
        // ------ now fill with data ------
    }

    // ------------------------------------------------------------------
    // NGC group row: blue rounded card + GROUP badge + monospace name + short id
    // ------------------------------------------------------------------
    private void bind_group(@NonNull EntryViewHolder holder, @NonNull FriendSelectSingle friend_entry)
    {
        final GroupDB g = friend_entry.group_item;

        holder.f_status_icon.setVisibility(View.VISIBLE);

        int privacy_state = ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PRIVATE.value;
        if (g != null)
        {
            privacy_state = g.privacy_state;
        }

        final boolean is_public =
                (privacy_state == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value);

        final Drawable group_icon = new IconicsDrawable(context).
                icon(is_public ? GoogleMaterial.Icon.gmd_public : GoogleMaterial.Icon.gmd_security).
                color(context.getResources().getColor(R.color.icon_colors)).sizeDp(80);
        holder.avatar.setImageDrawable(group_icon);

        if ((g != null) && (g.group_we_left))
        {
            holder.f_status_icon.setImageResource(R.drawable.circle_pink);
        }
        else if (friend_entry.online)
        {
            holder.f_status_icon.setImageResource(R.drawable.circle_green);
        }
        else if ((g != null) && (g.group_active))
        {
            holder.f_status_icon.setImageResource(R.drawable.circle_orange);
        }
        else
        {
            holder.f_status_icon.setImageResource(R.drawable.circle_red);
        }

        // subtext: "#ABC123 · public" / "#ABC123 · private"
        if (holder.f_group_subtext != null)
        {
            String sub = "";
            try
            {
                if (g != null)
                {
                    sub = "#" + group_identifier_short(g.group_identifier, true);
                }
            }
            catch (Exception ignored)
            {
            }
            sub = sub + " · " + (is_public ? "public" : "private");
            holder.f_group_subtext.setText(sub);
        }

        set_connection_border(holder.avatar,
                              friend_entry.online ? TOX_CONNECTION_UDP.value : TOX_CONNECTION_NONE.value, 2);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /**
     * avatar border colors taken from the main friend list (FriendListHolder),
     * including the night mode variants.
     */
    private void set_connection_border(@NonNull de.hdodenhof.circleimageview.CircleImageView avatar,
                                       int connection, int border_dp)
    {
        try
        {
            final boolean night = is_nightmode_active(context);

            if (connection == TOX_CONNECTION_NONE.value)
            {
                avatar.setBorderColor(Color.parseColor("#40000000"));
            }
            else if (connection == TOX_CONNECTION_TCP.value)
            {
                avatar.setBorderColor(Color.parseColor(night ? "#A8FFCE00" : "#FFCE00"));
            }
            else // UDP
            {
                avatar.setBorderColor(Color.parseColor(night ? "#A804B431" : "#04B431"));
            }

            avatar.setBorderWidth((int) dp2px(border_dp));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * same look as the "Pinned" separator in the main friend list
     * (FriendlistAdapter.buildPinnedSeparatorView), just a bit tighter.
     */
    private View build_separator_view(String text)
    {
        final boolean dark = is_nightmode_active(context);
        final int line_color = dark ? Color.parseColor("#4A4A4A") : Color.parseColor("#DDDDDD");
        final int accent = dark ? Color.parseColor("#9E9E9E") : Color.parseColor("#8A8A8A");

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding((int) dp2px(16), (int) dp2px(8), (int) dp2px(16), (int) dp2px(8));

        // left line
        View left_line = new View(context);
        left_line.setBackgroundColor(line_color);
        row.addView(left_line, new LinearLayout.LayoutParams(0, (int) dp2px(1), 1f));

        // centered label with pin icon
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextSize(11f);
        label.setTextColor(accent);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setAllCaps(true);
        try
        {
            Drawable pin = new IconicsDrawable(context).
                    icon(FontAwesome.Icon.faw_thumbtack).color(accent).sizeDp(11);
            label.setCompoundDrawablesWithIntrinsicBounds(pin, null, null, null);
            label.setCompoundDrawablePadding((int) dp2px(6));
        }
        catch (Exception ignored)
        {
        }

        LinearLayout.LayoutParams label_lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        label_lp.leftMargin = (int) dp2px(12);
        label_lp.rightMargin = (int) dp2px(12);
        row.addView(label, label_lp);

        // right line
        View right_line = new View(context);
        right_line.setBackgroundColor(line_color);
        row.addView(right_line, new LinearLayout.LayoutParams(0, (int) dp2px(1), 1f));

        return row;
    }
}
