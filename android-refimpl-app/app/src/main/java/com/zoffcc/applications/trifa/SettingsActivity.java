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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import static com.zoffcc.applications.trifa.HelperGeneric.IPisValid;
import static com.zoffcc.applications.trifa.HelperGeneric.isIPPortValid;
import static com.zoffcc.applications.trifa.HelperGeneric.is_valid_tox_public_key;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_persistent_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.ngcmidenable;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_PUSH_MSG_APP_PLAYSTORE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_PUSH_MSG_APP_WEBDOWNLOAD;

public class SettingsActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.SettingsActivity";

    private SettingsFragment settingsFragment;
    private EditText searchInput;
    private View searchClear;
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Toolbar bar = findViewById(R.id.toolbar);
        bar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        searchInput = findViewById(R.id.settings_search_input);
        searchClear = findViewById(R.id.settings_search_clear);
        noResults = findViewById(R.id.settings_no_results);

        if (savedInstanceState == null)
        {
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, settingsFragment)
                    .commit();
        }
        else
        {
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings_container);
        }

        searchInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                applySearch(s.toString());
            }
        });

        searchClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchInput.setText("");
            }
        });

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Called by the fragment once its preference screen exists, so a search term
     * that survived a recreate (e.g. dark-mode change) is applied again.
     */
    void onSettingsReady()
    {
        applySearch(searchInput.getText().toString());
    }

    private void applySearch(String query)
    {
        final boolean empty = (query == null) || (query.trim().length() == 0);
        searchClear.setVisibility(empty ? View.GONE : View.VISIBLE);

        int matches = -1;
        if (settingsFragment != null)
        {
            matches = settingsFragment.applySearchFilter(query);
        }
        noResults.setVisibility((matches == 0) ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard()
    {
        try
        {
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
            {
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void addWarningScenario(LinearLayout container,
                                           String title,
                                           String likelihood,
                                           int severityColor,
                                           String effect)
    {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View item = inflater.inflate(R.layout.dialog_warning_scenario_item, container, false);

        item.findViewById(R.id.severity_stripe).setBackgroundColor(severityColor);

        TextView titleView = item.findViewById(R.id.scenario_title);
        titleView.setText(title);

        TextView likeView = item.findViewById(R.id.scenario_likelihood);
        likeView.setText("Likelihood: " + likelihood);
        likeView.setTextColor(severityColor);

        TextView effectView = item.findViewById(R.id.scenario_effect);
        effectView.setText(effect);

        container.addView(item);
    }

    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value)
                {
                    String stringValue = value.toString();

                    if (preference instanceof ListPreference)
                    {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                    }
                    else if (preference instanceof RingtonePreferenceCompat)
                    {
                        // For the ringtone preference, look up the correct display value
                        // using RingtoneManager.
                        if (TextUtils.isEmpty(stringValue))
                        {
                            // Empty values correspond to 'silent' (no ringtone).
                            preference.setSummary(R.string.pref_ringtone_silent);
                        }
                        else
                        {
                            Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));

                            if (ringtone == null)
                            {
                                // Clear the summary if there was a lookup error.
                                preference.setSummary(null);
                            }
                            else
                            {
                                // Set the summary to reflect the new ringtone display name.
                                preference.setSummary(ringtone.getTitle(preference.getContext()));
                            }
                        }
                    }
                    else
                    {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.setSummary(stringValue);
                    }
                    return true;
                }
            };

    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                 PreferenceManager.getDefaultSharedPreferences(
                                                                         preference.getContext()).getString(
                                                                         preference.getKey(), ""));
    }

    @NonNull
    private static SharedPreferences change_custombootstrapnode_summary_text(Preference pref_custom_bootstrap_nodes)
    {
        pref_custom_bootstrap_nodes.setSummary("using default bootstrap nodes.");

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                pref_custom_bootstrap_nodes.getContext());
        final String bs_udp_ip = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP, "");
        final String bs_udp_port = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT, "");
        final String bs_udp_keyhex = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX, "");
        final String bs_tcp_ip = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP, "");
        final String bs_tcp_port = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT, "");
        final String bs_tcp_keyhex = settings.getString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX, "");

        boolean udp_valid = false;
        boolean tcp_valid = false;

        if ((bs_udp_ip.length() > 0) && (bs_udp_port.length() > 0) && (IPisValid(bs_udp_ip)) &&
            (isIPPortValid(bs_udp_port)) && (is_valid_tox_public_key(bs_udp_keyhex)))
        {
            udp_valid = true;
        }

        if ((bs_tcp_ip.length() > 0) && (bs_tcp_port.length() > 0) && (IPisValid(bs_tcp_ip)) &&
            (isIPPortValid(bs_tcp_port)) && (is_valid_tox_public_key(bs_tcp_keyhex)))
        {
            tcp_valid = true;
        }

        if (udp_valid)
        {
            pref_custom_bootstrap_nodes.setSummary(
                    "using custom bootstrapnode:\nfor UDP: " + bs_udp_ip + ":" + bs_udp_port + "\nKEY: " +
                    bs_udp_keyhex);
        }

        if (tcp_valid)
        {
            pref_custom_bootstrap_nodes.setSummary(
                    "using custom bootstrapnode:\nfor TCP: " + bs_tcp_ip + ":" + bs_tcp_port + "\nKEY: " +
                    bs_tcp_keyhex);
        }
        return settings;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.pref_settings, rootKey);

            // ---- section icons ----
            setSectionIcon("section_appearance", R.drawable.ic_section_appearance);
            setSectionIcon("section_notifications", R.drawable.ic_section_notifications);
            setSectionIcon("section_push", R.drawable.ic_section_push);
            setSectionIcon("section_chats_files", R.drawable.ic_section_chats);
            setSectionIcon("section_privacy_security", R.drawable.ic_section_privacy);
            setSectionIcon("section_network", R.drawable.ic_section_network);
            setSectionIcon("section_background", R.drawable.ic_section_background);
            setSectionIcon("section_voice_calls", R.drawable.ic_section_voice);
            setSectionIcon("section_video_calls", R.drawable.ic_section_video);

            // ---- warning accents on privacy/security-risk toggles (visible even when off) ----
            setWarningIcon("X_persistent_peerlist", R.color.md_red_600);
            setWarningIcon("allow_open_encrypted_file_via_intent", R.color.md_orange_600);
            setWarningIcon("allow_push_server_ntfy", R.color.md_orange_600);
            setWarningIcon("allow_push_server_sunup", R.color.md_orange_600);

            // ---- unsafe: keep nospam (warning icon when enabled) ----
            final SwitchPreference pref_keepnpspam = (SwitchPreference) findPreference("U_keep_nospam");

            if (pref_keepnpspam.isChecked())
            {
                try
                {
                    final Drawable d1 = new IconicsDrawable(pref_keepnpspam.getContext()).
                            icon(FontAwesome.Icon.faw_exclamation_circle).
                            color(getResources().getColor(R.color.md_red_700)).sizeDp(100);
                    pref_keepnpspam.setIcon(d1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    pref_keepnpspam.setIcon(null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            pref_keepnpspam.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (newValue == (Object) true)
                    {
                        try
                        {
                            final Drawable d1 = new IconicsDrawable(preference.getContext()).
                                    icon(FontAwesome.Icon.faw_exclamation_circle).
                                    color(getResources().getColor(R.color.md_red_600)).
                                    sizeDp(100);
                            preference.setIcon(d1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try
                        {
                            preference.setIcon(null);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });

            // ---- experimental: persistent peerlist (confirmation dialog before enabling) ----
            final SwitchPreference pref_persistent_peerlist = (SwitchPreference) findPreference("X_persistent_peerlist");
            if (pref_persistent_peerlist != null)
            {
                pref_persistent_peerlist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(final Preference preference, Object newValue)
                    {
                        boolean isChecked = (Boolean) newValue;

                        if (isChecked)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                            builder.setTitle("Persistent Peerlist");

                            try
                            {
                                final Drawable d1 = new IconicsDrawable(preference.getContext()).
                                        icon(FontAwesome.Icon.faw_exclamation_circle).
                                        color(getResources().getColor(R.color.md_red_600)).sizeDp(100);
                                builder.setIcon(d1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            final View dialogView = LayoutInflater.from(preference.getContext())
                                    .inflate(R.layout.dialog_persistent_peerlist_warning, null);

                            // Cap the scroll area to 70% of the screen so the button bar
                            // can never fall below the display edge.
                            MaxHeightScrollView scroll = dialogView.findViewById(R.id.warning_scroll);
                            android.util.DisplayMetrics dm = preference.getContext().getResources().getDisplayMetrics();
                            scroll.setMaxHeight((int) (dm.heightPixels * 0.70f));

                            LinearLayout container = dialogView.findViewById(R.id.scenarios_container);

                            addWarningScenario(container,
                                               "Everyday use: friends, family, hobby groups",
                                               "Negligible",
                                               0xFF4CAF50, // Green
                                               "This feature provides undeniable proof that your PeerID was in the group. " +
                                               "Without it, someone can only say 'I saw them there'. With it, they have " +
                                               "proof they can show to others. For most people, this changes nothing.");

                            addWarningScenario(container,
                                               "Activism, journalism, support group",
                                               "Think carefully",
                                               0xFFF44336, // Red
                                               "In sensitive groups, the danger is someone reporting on you. " +
                                               "Normally, an infiltrator can only say 'I saw them in the chat' " +
                                               "(which you can deny). With this feature, they get a mathematical " +
                                               "proof they can hand to others: 'Here is undeniable proof this PeerID " +
                                               "was in this group.'");

                            addWarningScenario(container,
                                               "State actor or high-risk target",
                                               "Changes nothing",
                                               0xFF9E9E9E, // Grey
                                               "If a powerful adversary is targeting you, they don't need this " +
                                               "feature to prove your PeerID is in a group. They likely already have " +
                                               "spyware on your phone, can read your screen, and can see your " +
                                               "messages. This feature doesn't make you safer, but it also " +
                                               "doesn't give them anything they didn't already have.");

                            builder.setView(dialogView);
                            builder.setCancelable(true);

                            final AlertDialog dialog = builder.create();

                            dialogView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    dialog.dismiss();   // switch stays OFF
                                }
                            });

                            dialogView.findViewById(R.id.btn_enable).setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    dialog.dismiss();
                                    ((SwitchPreference) preference).setChecked(true);
                                    PREF__X_persistent_peerlist = true;
                                    ngcmidenable(1);
                                }
                            });

                            dialog.show();

                            return false;   // do not flip the switch until confirmed
                        }
                        else
                        {
                            PREF__X_persistent_peerlist = false;
                            ngcmidenable(0);
                            return true;    // turning OFF needs no confirmation
                        }
                    }
                });
            }

            // ---- start on boot (Android 10+ note) ----
            final SwitchPreference pref_startonboot = (SwitchPreference) findPreference("start_on_boot");

            pref_startonboot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (newValue == (Object) true)
                    {
                        try
                        {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                                builder.setTitle("Android 10 and up");
                                // @formatter:off
                                builder.setMessage(
                                        "\n"+
                                        "Starting with Android 10 you must enable a setting that the App can start on Boot and display the Password Screen.\n"+
                                        "Go to Android App Settings for TRIfA and select:\n\n"+
                                        "     \"Display over other apps\"\n\n"+
                                        "then enable\n\n"+
                                        "     \"Allow display over other apps\""+
                                        "\n\n"+
                                        "Sorry for the inconvenience, Google Android does not allow it any other way.\n\n"+
                                        "See:\n\n"+
                                        "https://developer.android.com/guide/components/activities/background-starts\n\n"+
                                        "for more details.\n"
                                );
                                // @formatter:on

                                builder.setPositiveButton("OK", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });

            // ---- download push message app (chooser) ----
            Preference pref_download_push_msg_app = findPreference("download_push_msg_app");
            pref_download_push_msg_app.setSummary(TOX_PUSH_MSG_APP_PLAYSTORE);

            pref_download_push_msg_app.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    String[] destinations = {"Google Play", "Github"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                    builder.setTitle("Download from:");
                    builder.setItems(destinations, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (which == 1)
                            {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(TOX_PUSH_MSG_APP_WEBDOWNLOAD));
                                startActivity(i);
                            }
                            else
                            {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(TOX_PUSH_MSG_APP_PLAYSTORE));
                                startActivity(i);
                            }
                        }
                    });
                    builder.show();

                    return true;
                }
            });

            // ---- custom bootstrap nodes (editor dialog + summary) ----
            Preference pref_custom_bootstrap_nodes = findPreference("custom_bootstrap_nodes");

            final SharedPreferences settings = change_custombootstrapnode_summary_text(pref_custom_bootstrap_nodes);

            pref_custom_bootstrap_nodes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    LayoutInflater li = LayoutInflater.from(preference.getContext());
                    View promptsView = li.inflate(R.layout.custom_bootstrap_prompt, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(preference.getContext());

                    alertDialogBuilder.setView(promptsView);
                    final EditText edit_bootstrap_udp_ip = (EditText) promptsView.findViewById(R.id.edit_bootstrap_udp_ip);
                    final EditText edit_bootstrap_udp_port = (EditText) promptsView.findViewById(R.id.edit_bootstrap_udp_port);
                    final EditText edit_bootstrap_udp_keyhex = (EditText) promptsView.findViewById(R.id.edit_bootstrap_udp_keyhex);
                    final EditText edit_bootstrap_tcp_ip = (EditText) promptsView.findViewById(R.id.edit_bootstrap_tcp_ip);
                    final EditText edit_bootstrap_tcp_port = (EditText) promptsView.findViewById(R.id.edit_bootstrap_tcp_port);
                    final EditText edit_bootstrap_tcp_keyhex = (EditText) promptsView.findViewById(R.id.edit_bootstrap_tcp_keyhex);

                    alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP,
                                                edit_bootstrap_udp_ip.getText().toString()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP, "").apply();
                                    }

                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT,
                                                edit_bootstrap_udp_port.getText().toString()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT, "").apply();
                                    }

                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX,
                                                edit_bootstrap_udp_keyhex.getText().toString().toUpperCase()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX, "").apply();
                                    }

                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP,
                                                edit_bootstrap_tcp_ip.getText().toString()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP, "").apply();
                                    }

                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT,
                                                edit_bootstrap_tcp_port.getText().toString()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT, "").apply();
                                    }

                                    try
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX,
                                                edit_bootstrap_tcp_keyhex.getText().toString().toUpperCase()).apply();
                                    }
                                    catch (Exception e)
                                    {
                                        settings.edit().putString(PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX, "").apply();
                                    }

                                    try
                                    {
                                        change_custombootstrapnode_summary_text(pref_custom_bootstrap_nodes);
                                    }
                                    catch (Exception e)
                                    {
                                    }
                                }
                            }).setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return true;
                }
            });

            // ---- dark mode (apply + recreate) ----
            final ListPreference pref_dark_mode_pref = (ListPreference) findPreference("dark_mode_pref");

            pref_dark_mode_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    try
                    {
                        if (((String) newValue).equals("0"))
                        {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        }
                        else if (((String) newValue).equals("1"))
                        {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }
                        else
                        {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        getActivity().recreate();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            // ---- notification ringtone (system picker + summary) ----
            final RingtonePreferenceCompat pref_ringtone = findPreference("notifications_new_message_ringtone");
            if (pref_ringtone != null)
            {
                bindPreferenceSummaryToValue(pref_ringtone);
                pref_ringtone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        pref_ringtone.launchPicker(SettingsFragment.this);
                        return true;
                    }
                });
            }

            // ---- numeric input constraints (parity with the old android:maxLength / android:numeric) ----
            bindNumericEditText("message_paging_num_msgs_per_page", 5);
            bindNumericEditText("X_eac_delay_ms_2", 3);
            bindNumericEditText("X_audio_play_buffer_custom", 6);

            // let the activity (re)apply any search term now that the screen exists
            if (getActivity() instanceof SettingsActivity)
            {
                ((SettingsActivity) getActivity()).onSettingsReady();
            }
        }

        /**
         * Live settings search. Matches a preference's title, summary, key (and, for
         * lists, its entries); a matching section title reveals the whole section.
         * Sections with hits are expanded and shown, everything else is hidden.
         * Clearing the query restores each section's own remembered fold state.
         *
         * @return number of matching preferences, or -1 when not filtering / not ready.
         */
        public int applySearchFilter(String query)
        {
            final PreferenceScreen screen = getPreferenceScreen();
            if (screen == null)
            {
                return -1;   // preference screen not built yet
            }

            final String q = (query == null) ? "" : query.trim().toLowerCase();

            if (q.length() == 0)
            {
                // clear: show every section again and restore the user's fold state
                for (int i = 0; i < screen.getPreferenceCount(); i++)
                {
                    final Preference p = screen.getPreference(i);
                    if (p instanceof CollapsiblePreferenceCategory)
                    {
                        final CollapsiblePreferenceCategory cat = (CollapsiblePreferenceCategory) p;
                        cat.setSearchMode(false);
                        cat.setVisible(true);
                        cat.restorePersistedState();
                    }
                }
                return -1;
            }

            int matches = 0;
            for (int i = 0; i < screen.getPreferenceCount(); i++)
            {
                final Preference p = screen.getPreference(i);
                if (!(p instanceof CollapsiblePreferenceCategory))
                {
                    continue;
                }
                final CollapsiblePreferenceCategory cat = (CollapsiblePreferenceCategory) p;
                cat.setSearchMode(true);

                final boolean sectionMatch = textMatches(cat.getTitle(), q) || textMatches(cat.getSummary(), q);
                boolean anyMatch = false;
                for (int j = 0; j < cat.getPreferenceCount(); j++)
                {
                    final Preference child = cat.getPreference(j);
                    final boolean m = sectionMatch || prefMatches(child, q);
                    child.setVisible(m);
                    if (m)
                    {
                        anyMatch = true;
                        matches++;
                    }
                }
                cat.setVisible(anyMatch);
                if (anyMatch)
                {
                    // chevron up; child visibility stays as filtered (searchMode is on)
                    cat.setExpandedTransient(true);
                }
            }
            return matches;
        }

        private static boolean textMatches(CharSequence cs, String q)
        {
            return (cs != null) && cs.toString().toLowerCase().contains(q);
        }

        private boolean prefMatches(Preference p, String q)
        {
            if (textMatches(p.getTitle(), q) || textMatches(p.getSummary(), q))
            {
                return true;
            }
            final String key = p.getKey();
            if ((key != null) && key.toLowerCase().contains(q))
            {
                return true;
            }
            if (p instanceof ListPreference)
            {
                final CharSequence[] entries = ((ListPreference) p).getEntries();
                if (entries != null)
                {
                    for (CharSequence e : entries)
                    {
                        if (textMatches(e, q))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void setSectionIcon(String key, int drawableRes)
        {
            final Preference cat = findPreference(key);
            if (cat != null)
            {
                final Drawable d = ContextCompat.getDrawable(requireContext(), drawableRes);
                if (d != null)
                {
                    cat.setIcon(d);
                }
            }
        }

        private void setWarningIcon(String key, int colorRes)
        {
            final Preference p = findPreference(key);
            if (p != null)
            {
                try
                {
                    final Drawable d = new IconicsDrawable(requireContext()).
                            icon(FontAwesome.Icon.faw_exclamation_circle).
                            color(getResources().getColor(colorRes)).
                            sizeDp(24);
                    p.setIcon(d);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        private void bindNumericEditText(String key, final int maxLength)
        {
            EditTextPreference pref = findPreference(key);
            if (pref != null)
            {
                pref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener()
                {
                    @Override
                    public void onBindEditText(@NonNull EditText editText)
                    {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                    }
                });
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            if (requestCode == RingtonePreferenceCompat.REQUEST_RINGTONE)
            {
                if ((resultCode == Activity.RESULT_OK) && (data != null))
                {
                    Uri picked = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    RingtonePreferenceCompat pref = findPreference("notifications_new_message_ringtone");
                    if (pref != null)
                    {
                        pref.savePickedRingtone(picked);
                    }
                }
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
