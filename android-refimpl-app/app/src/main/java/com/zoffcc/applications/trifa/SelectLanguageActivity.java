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

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectLanguageActivity extends ListActivity
{
    private static final String TAG = "trifa.SelectLngActy";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate:001");

        try
        {
            String[] lang_key_and_names = new String[]{
                 "_default_" + ":\n" + "System Default",
                        "en" + ":\n" + "English",
                     // RTL is not fully working yet, so use english for now
                     // "ar" + ":\n" + "Arabic",
                        "de" + ":\n" + "German - Deutsch",
                        "es" + ":\n" + "Spanish",
                     // RTL is not fully working yet, so use english for now
                     // "fa" + ":\n" + "Persian",
                        "fr" + ":\n" + "French",
                        "hi" + ":\n" + "Hindu",
                        "hu" + ":\n" + "Hungarian",
                        "it" + ":\n" + "Italian",
                        "kn" + ":\n" + "Kannada",
                    "pt-rBR" + ":\n" + "Brazilian - Portuguese",
                        "ru" + ":\n" + "Russian",
                        "sv" + ":\n" + "Swedish",
                        "tr" + ":\n" + "Turkish",
                    "zh-rCN" + ":\n" + "Chinese - 中国人",
            };

            this.setListAdapter(
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lang_key_and_names));
            ListView lv = getListView();

            Log.i(TAG, "onCreate:008");

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    String lang_key_and_name = ((TextView) view).getText().toString();
                    Log.i(TAG, "onItemClick:" + lang_key_and_name);

                    Intent data = new Intent();
                    String return_lang = null;

                    try
                    {
                        int iend = lang_key_and_name.indexOf(":"); // find the first occurrence of ":"
                        if (iend != -1)
                        {
                            return_lang = lang_key_and_name.substring(0, iend);
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    try
                    {
                        data.setData(Uri.parse(return_lang));
                        setResult(RESULT_OK, data);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    finish();
                }
            });

            Log.i(TAG, "onCreate:009");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.finish();
        }

        Log.i(TAG, "onCreate:010");
    }
}
