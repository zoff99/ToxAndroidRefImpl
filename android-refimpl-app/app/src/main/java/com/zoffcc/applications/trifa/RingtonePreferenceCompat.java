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
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Minimal replacement for the removed android.preference.RingtonePreference.
 * <p>
 * It launches the system ringtone picker and stores the picked ringtone URI as
 * a String under the same preference key, in exactly the same format the old
 * framework preference used: an empty String means "silent", the default
 * notification URI string means "follow the system default".
 */
public class RingtonePreferenceCompat extends Preference
{
    // request code used when launching the system ringtone picker
    public static final int REQUEST_RINGTONE = 0x52494e47; // "RING"

    public RingtonePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RingtonePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public RingtonePreferenceCompat(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RingtonePreferenceCompat(Context context)
    {
        super(context);
    }

    /**
     * Open the system notification-ringtone picker, pre-selecting the current value.
     */
    public void launchPicker(PreferenceFragmentCompat fragment)
    {
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

        final String current = getPersistedString(null);
        Uri existing = null;
        if ((current != null) && (current.length() > 0))
        {
            existing = Uri.parse(current);
        }
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existing);

        fragment.startActivityForResult(intent, REQUEST_RINGTONE);
    }

    /**
     * Persist the ringtone the user picked (null -> silent).
     */
    public void savePickedRingtone(Uri uri)
    {
        final String value = (uri == null) ? "" : uri.toString();
        if (callChangeListener(value))
        {
            persistString(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        final String def = (defaultValue instanceof String) ? (String) defaultValue : null;
        // make sure the key exists with the current (or default) value, like the old preference did
        persistString(getPersistedString(def));
    }
}
