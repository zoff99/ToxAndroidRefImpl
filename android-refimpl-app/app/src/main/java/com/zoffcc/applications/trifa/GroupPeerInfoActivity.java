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
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.context_s;

public class GroupPeerInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.GrpPeerInfoActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    TextView peer_toxid = null;
    TextView peer_name = null;
    String peer_pubkey = null;
    String group_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_peer_info);

        Intent intent = getIntent();
        peer_pubkey = intent.getStringExtra("peer_pubkey");
        group_id = intent.getStringExtra("group_id");
        final long conference_num = tox_group_by_groupid__wrapper(group_id);

        profile_icon = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.pi_profile_icon);
        peer_toxid = (TextView) findViewById(R.id.pi_toxprvkey_textview);
        peer_name = (TextView) findViewById(R.id.pi_nick_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
        profile_icon.setImageDrawable(d1);

        String peer_name_txt = tox_group_peer_get_name__wrapper(group_id, peer_pubkey);

        if ((peer_name_txt == null) || (peer_name_txt.equals("")) || (peer_name_txt.equals("-1")))
        {
            peer_name_txt = "Unknown";
        }

        peer_toxid.setText(peer_pubkey);
        peer_name.setText(peer_name_txt);

        try
        {
            int peer_color_fg = getResources().getColor(R.color.colorPrimaryDark);
            int peer_color_bg = ChatColors.get_shade(
                    ChatColors.PeerAvatarColors[hash_to_bucket(peer_pubkey, ChatColors.get_size())], peer_pubkey);

            final Drawable smiley_face = new IconicsDrawable(context_s).
                    icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                    backgroundColor(Color.TRANSPARENT).
                    color(peer_color_fg).sizeDp(70);

            profile_icon.setPadding((int) dp2px(0), (int) dp2px(0), (int) dp2px(0), (int) dp2px(0));
            profile_icon.setImageDrawable(smiley_face);

            // we need to do the rounded corner background manually here, to change the color ---------------
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(peer_color_bg);
            profile_icon.setBackground(shape);
            // we need to do the rounded corner background manually here, to change the color ---------------

        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }
}
