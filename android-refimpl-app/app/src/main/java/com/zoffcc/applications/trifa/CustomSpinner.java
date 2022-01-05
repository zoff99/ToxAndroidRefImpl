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
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AdapterView;

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner
{
    private static final String TAG = "trifa.CustomSpinner";

    AdapterView.OnItemSelectedListener listener;

    public CustomSpinner(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position, boolean animate)
    {
        super.setSelection(position, animate);

        if (position == getSelectedItemPosition())
        {
            try
            {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
            catch (Exception e)
            {
                Log.i(TAG, "EE:02:" + e.getMessage());
            }
        }
    }

    @Override
    public void setSelection(int position)
    {
        super.setSelection(position);

        if (position == getSelectedItemPosition())
        {
            try
            {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
            catch (Exception e)
            {
                // Log.i(TAG, "EE:01:" + e.getMessage());
            }
        }
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener)
    {
        this.listener = listener;
    }
}
