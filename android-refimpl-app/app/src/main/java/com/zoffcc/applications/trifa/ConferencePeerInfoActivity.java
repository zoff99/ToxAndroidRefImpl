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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.HelperConference.get_conference_num_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_peer_get_name__wrapper;

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
    }
}
