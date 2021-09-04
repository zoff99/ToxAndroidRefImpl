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
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_set_title;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.ConfInfoActy";
    de.hdodenhof.circleimageview.CircleImageView profile_icon = null;
    TextView this_conf_id = null;
    EditText this_title = null;
    String conf_id = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conferenceinfo);

        Intent intent = getIntent();
        conf_id = intent.getStringExtra("conf_id");

        this_conf_id = (TextView) findViewById(R.id.conf_id_text);
        this_title = (EditText) findViewById(R.id.conf_name_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ((conf_id == null) || (conf_id.equals("-1")))
        {
            this_conf_id.setText("*error*");
        }
        else
        {
            this_conf_id.setText(conf_id.toLowerCase());
        }
        this_title.setText("*error*");

        try
        {
            this_title.setText(orma.selectFromConferenceDB().
                    conference_identifierEq(conf_id.toLowerCase()).
                    toList().get(0).name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // TODO dirty hack, just write "conf title"

        try
        {
            String new_conf_title = this_title.getText().toString();
            if (new_conf_title != null)
            {
                if (new_conf_title.length() > 0)
                {
                    int res = tox_conference_set_title(tox_conference_by_confid__wrapper(conf_id), new_conf_title);
                    if (res == 1)
                    {
                        update_savedata_file_wrapper(); // after changing conference title
                        orma.updateConferenceDB().
                                conference_identifierEq(conf_id.toLowerCase()).
                                name(new_conf_title).execute();
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
