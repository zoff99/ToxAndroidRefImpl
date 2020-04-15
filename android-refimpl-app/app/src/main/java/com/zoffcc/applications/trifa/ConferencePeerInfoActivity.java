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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.HelperConference.get_conference_num_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.StringSignature2;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.dp2px;
import static com.zoffcc.applications.trifa.MainActivity.hash_to_bucket;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferencePeerInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.CnfPeerInfoActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    TextView peer_toxid = null;
    TextView peer_name = null;
    String peer_pubkey = null;
    String conf_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_peer_info);

        Intent intent = getIntent();
        peer_pubkey = intent.getStringExtra("peer_pubkey");
        conf_id = intent.getStringExtra("conf_id");
        final long conference_num = get_conference_num_from_confid(conf_id);

        profile_icon = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.pi_profile_icon);
        peer_toxid = (TextView) findViewById(R.id.pi_toxprvkey_textview);
        peer_name = (TextView) findViewById(R.id.pi_nick_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
        profile_icon.setImageDrawable(d1);

        String peer_name_txt = tox_conference_peer_get_name__wrapper(conf_id, peer_pubkey);

        if ((peer_name_txt == null) || (peer_name_txt.equals("")) || (peer_name_txt.equals("-1")))
        {
            peer_name_txt = "Unknown";
        }

        peer_toxid.setText(peer_pubkey);
        peer_name.setText(peer_name_txt);


        try
        {
            FriendList fl_temp = null;
            boolean have_avatar_for_pubkey = false;

            try
            {
                fl_temp = orma.selectFromFriendList().
                        tox_public_key_stringEq(peer_pubkey).toList().get(0);

                if ((fl_temp.avatar_filename != null) && (fl_temp.avatar_pathname != null))
                {
                    info.guardianproject.iocipher.File f1 = null;
                    try
                    {
                        f1 = new info.guardianproject.iocipher.File(
                                fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename);
                        if (f1.length() > 0)
                        {
                            have_avatar_for_pubkey = true;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    have_avatar_for_pubkey = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                have_avatar_for_pubkey = false;
            }

            try
            {
                if ((have_avatar_for_pubkey) && (fl_temp != null))
                {
                    if (VFS_ENCRYPT)
                    {
                        info.guardianproject.iocipher.File f1 = null;
                        try
                        {
                            f1 = new info.guardianproject.iocipher.File(
                                    fl_temp.avatar_pathname + "/" + fl_temp.avatar_filename);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if ((f1 != null) && (fl_temp.avatar_pathname != null))
                        {
                            if (f1.length() > 0)
                            {
                                final RequestOptions glide_options = new RequestOptions().
                                        fitCenter().
                                        circleCrop();

                                GlideApp.
                                        with(this).
                                        load(f1).
                                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                        signature(StringSignature2("_conf_avatar_" + fl_temp.avatar_pathname + "/" +
                                                                   fl_temp.avatar_filename + "_" +
                                                                   fl_temp.avatar_update_timestamp)).
                                        priority(Priority.HIGH).
                                        placeholder(R.drawable.round_loading_animation).
                                        skipMemoryCache(false).
                                        apply(glide_options).
                                        into(profile_icon);
                            }
                        }
                    }
                }
                else
                {
                    try
                    {
                        int peer_color_fg = getResources().getColor(R.color.colorPrimaryDark);
                        int peer_color_bg = getResources().getColor(R.color.material_drawer_background);
                        int alpha_value = 160;

                        peer_color_bg = ChatColors.PeerAvatarColors[hash_to_bucket(peer_pubkey, ChatColors.get_size())];

                        final Drawable smiley_face = new IconicsDrawable(context_s).
                                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                                backgroundColor(Color.TRANSPARENT).
                                color(peer_color_fg).sizeDp(70);

                        profile_icon.setPadding((int) dp2px(0), (int) dp2px(0), (int) dp2px(0), (int) dp2px(0));
                        profile_icon.setImageDrawable(smiley_face);

                        //**//LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams((int) dp2px(35), (int) dp2px(35));
                        //**//parameter.setMargins(parameter.leftMargin, (int) dp2px(6), (int) dp2px(10),
                        //**//                     (int) dp2px(0)); // left, top, right, bottom
                        //**//profile_icon.setLayoutParams(parameter);

                        // we need to do the rounded corner background manually here, to change the color ---------------
                        GradientDrawable shape = new GradientDrawable();
                        shape.setShape(GradientDrawable.OVAL);
                        //**// shape.setSize((int) dp2px(35), (int) dp2px(35));
                        //**// shape.setCornerRadii(new float[]{CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX * 2,
                        //**//         CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX *
                        //**//         2, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX,
                        //**//         CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX * 2,
                        //**//         CONFERENCE_CHAT_DRAWER_ICON_CORNER_RADIUS_IN_PX * 2});
                        shape.setColor(peer_color_bg);
                        profile_icon.setBackground(shape);
                        // we need to do the rounded corner background manually here, to change the color ---------------
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }
}
