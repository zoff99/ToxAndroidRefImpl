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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

/**
 * A settings section header that folds its child preferences up and down.
 * <p>
 * Tapping the header toggles the section; the expanded/collapsed state is
 * remembered per section key in the default SharedPreferences (under a private
 * "ui_settings_section_expanded_" prefix, so it can never collide with a real
 * setting). Collapsing only hides the children (Preference.setVisible); it does
 * not touch their values, and android:dependency keeps working because
 * dependencies disable rather than hide.
 */
public class CollapsiblePreferenceCategory extends PreferenceCategory
{
    private static final String STATE_KEY_PREFIX = "ui_settings_section_expanded_";

    private boolean defaultExpanded = false;
    private boolean expanded = false;
    private boolean stateLoaded = false;
    // while the settings search is active it owns child visibility directly;
    // the category must not blanket-apply its own fold state on top of that
    private boolean searchMode = false;

    private ImageView chevronView = null;
    private TextView countView = null;

    public CollapsiblePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public CollapsiblePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CollapsiblePreferenceCategory(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public CollapsiblePreferenceCategory(Context context)
    {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs)
    {
        setLayoutResource(R.layout.preference_category_collapsible);

        if (attrs != null)
        {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsiblePreferenceCategory);
            defaultExpanded = a.getBoolean(R.styleable.CollapsiblePreferenceCategory_defaultExpanded, false);
            a.recycle();
        }
        expanded = defaultExpanded;
    }

    private String stateKey()
    {
        return STATE_KEY_PREFIX + getKey();
    }

    private SharedPreferences statePrefs()
    {
        final PreferenceManager pm = getPreferenceManager();
        return (pm != null) ? pm.getSharedPreferences() : null;
    }

    private void loadState()
    {
        if (!stateLoaded)
        {
            final SharedPreferences sp = statePrefs();
            if ((sp != null) && (getKey() != null))
            {
                expanded = sp.getBoolean(stateKey(), defaultExpanded);
            }
            stateLoaded = true;
        }
    }

    public boolean isExpanded()
    {
        loadState();
        return expanded;
    }

    /**
     * User toggle: apply the new state and remember it.
     */
    public void setExpanded(boolean value)
    {
        loadState();
        expanded = value;

        final SharedPreferences sp = statePrefs();
        if ((sp != null) && (getKey() != null))
        {
            sp.edit().putBoolean(stateKey(), expanded).apply();
        }

        updateChevron(true);
        applyChildVisibility();
    }

    /**
     * Temporary override (used by the settings search): apply without remembering,
     * so clearing the search can restore the user's own fold state.
     */
    public void setExpandedTransient(boolean value)
    {
        loadState();
        expanded = value;
        updateChevron(false);
        applyChildVisibility();
    }

    /**
     * Return to the state the user last chose (after a search is cleared).
     */
    public void restorePersistedState()
    {
        stateLoaded = false;
        loadState();
        updateChevron(false);
        applyChildVisibility();
    }

    /**
     * While search is active the filter owns child visibility; the category's own
     * fold/expand must not override it (e.g. when a header re-binds on scroll).
     */
    public void setSearchMode(boolean on)
    {
        searchMode = on;
    }

    private void applyChildVisibility()
    {
        if (searchMode)
        {
            return;
        }
        for (int i = 0; i < getPreferenceCount(); i++)
        {
            // setVisible is a no-op when the value did not change, so this is cheap
            getPreference(i).setVisible(expanded);
        }
    }

    private void updateChevron(boolean animate)
    {
        if (chevronView != null)
        {
            final float target = expanded ? 180f : 0f;
            if (animate)
            {
                chevronView.animate().rotation(target).setDuration(150).start();
            }
            else
            {
                chevronView.setRotation(target);
            }
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        loadState();

        chevronView = (ImageView) holder.findViewById(R.id.section_chevron);
        countView = (TextView) holder.findViewById(R.id.section_count);
        if (countView != null)
        {
            countView.setText(String.valueOf(getPreferenceCount()));
        }
        updateChevron(false);

        final View itemView = holder.itemView;
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setExpanded(!expanded);
            }
        });

        // apply child visibility after the bind pass: changing preference visibility
        // while the RecyclerView is laying out would throw
        itemView.post(new Runnable()
        {
            @Override
            public void run()
            {
                applyChildVisibility();
            }
        });
    }
}
