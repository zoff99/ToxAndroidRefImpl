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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.model.AbstractBadgeableDrawerItem;

import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.StringSignature2;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.hash_to_bucket;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceCustomDrawerPeerItem extends AbstractBadgeableDrawerItem<ConferenceCustomDrawerPeerItem>
{
    private static final String TAG = "trifa.ConfPeerDItem";
    ImageView icon = null;
    String peer_pubkey = null;
    boolean have_avatar_for_pubkey = false;

    ConferenceCustomDrawerPeerItem(boolean have_avatar_for_pubkey, String peer_pubkey)
    {
        this.peer_pubkey = peer_pubkey;
        this.have_avatar_for_pubkey = have_avatar_for_pubkey;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads)
    {
        super.bindView(viewHolder, payloads);

        Context c = viewHolder.itemView.getContext();
        // Log.i(TAG, "bindView:context=" + c);
        icon = (ImageView) viewHolder.itemView.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_icon);

        FriendList fl_temp = null;

        try
        {
            if (have_avatar_for_pubkey)
            {
                fl_temp = orma.selectFromFriendList().
                        tox_public_key_stringEq(peer_pubkey).get(0);

                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.File f1 = null;
                    try
                    {
                        f1 = new info.guardianproject.iocipher.File(fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if ((f1 != null) && (fl_temp.avatar_pathname != null))
                    {
                        if (f1.length() > 0)
                        {
                            icon.setVisibility(View.VISIBLE);
                            icon.setPadding((int) dp2px(2), (int) dp2px(2), (int) dp2px(2), (int) dp2px(2));
                            LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams((int) dp2px(35), (int) dp2px(35));
                            parameter.setMargins(parameter.leftMargin, (int) dp2px(6), (int) dp2px(10), (int) dp2px(0)); // left, top, right, bottom
                            icon.setLayoutParams(parameter);

                            // --------------------
                            icon.setBackgroundResource(R.drawable.bg_circular_border);
                            // --------------------

                            final RequestOptions glide_options = new RequestOptions().
                                    fitCenter().
                                    circleCrop();

                            GlideApp.
                                    with(c).
                                    load(f1).
                                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                    signature(StringSignature2("_conf_avatar_" + fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename)).
                                    priority(Priority.HIGH).
                                    placeholder(R.drawable.round_loading_animation).
                                    skipMemoryCache(false).
                                    apply(glide_options).
                                    into(this.icon);
                        }
                    }
                }
            }
        }
        catch (Exception a01)
        {
            a01.printStackTrace();
            have_avatar_for_pubkey = false;
        }

        if (!have_avatar_for_pubkey)
        {
            try
            {
                int peer_color_fg = c.getResources().getColor(R.color.colorPrimaryDark);
                int peer_color_bg = c.getResources().getColor(R.color.material_drawer_background);
                int alpha_value = 160;

                peer_color_bg = ChatColors.PeerAvatarColors[hash_to_bucket(peer_pubkey, ChatColors.get_size())];

                final Drawable smiley_face = new IconicsDrawable(context_s).
                        icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                        backgroundColor(Color.TRANSPARENT).
                        color(peer_color_fg).sizeDp(70);

                icon.setVisibility(View.VISIBLE);
                icon.setPadding((int) dp2px(0), (int) dp2px(0), (int) dp2px(0), (int) dp2px(0));
                icon.setImageDrawable(smiley_face);

                LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams((int) dp2px(35), (int) dp2px(35));
                parameter.setMargins(parameter.leftMargin, (int) dp2px(6), (int) dp2px(10), (int) dp2px(0)); // left, top, right, bottom
                icon.setLayoutParams(parameter);

                // we need to do the rounded corner background manually here, to change the color ---------------
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setSize((int) dp2px(35), (int) dp2px(35));
                shape.setCornerRadii(new float[]{CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX});
                shape.setColor(peer_color_bg);
                icon.setBackground(shape);
                // we need to do the rounded corner background manually here, to change the color ---------------
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}