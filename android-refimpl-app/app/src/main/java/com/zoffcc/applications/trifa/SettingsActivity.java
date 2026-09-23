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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

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

public class SettingsActivity extends AppCompatPreferenceActivity
{
    private static final String TAG = "trifa.SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
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

    /*
     * Builds a richly formatted, readable warning text for the
     * "Persistent Peerlist" confirmation dialog.
     *
     * Layout per scenario:
     *   ————————————————   (separator)
     *   Scenario title     (bold)
     *   Likelihood: VALUE  (bold label, colored value)
     *   effect text        (gray body)
     */
    @NonNull
    private static SpannableStringBuilder build_persistent_peerlist_warning()
    {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        final int color_header = Color.parseColor("#D32F2F"); // red 700
        final int color_negl   = Color.parseColor("#43A047"); // green 600
        final int color_lowmod = Color.parseColor("#FB8C00"); // orange 600
        final int color_mod    = Color.parseColor("#F4511E"); // deep orange 600
        final int color_na     = Color.parseColor("#757575"); // grey 600
        final int color_sep    = Color.parseColor("#BDBDBD"); // grey 400
        final int color_body   = Color.parseColor("#424242"); // grey 800

        // ---- header ----
        append_line(sb, "PRIVACY WARNING - PERSISTENT PEERLIST",
                    new StyleSpan(Typeface.BOLD),
                    new ForegroundColorSpan(color_header),
                    new RelativeSizeSpan(1.15f));
        append_line(sb, "");
        append_line(sb,
                    "Enabling this makes your presence in NGC groups CRYPTOGRAPHICALLY " +
                    "PROVABLE and PERSISTENT (30-day cache) instead of ephemeral and deniable.",
                    new ForegroundColorSpan(color_body));
        append_line(sb, "");

        // ---- scenarios ----
        append_separator(sb, color_sep);
        append_scenario(sb, color_negl,
                        "Casual user (hobby/social group)",
                        "Negligible",
                        "~Zero. No one is trying to prove your presence. The signatures sit unused.");

        append_separator(sb, color_sep);
        append_scenario(sb, color_lowmod,
                        "Malicious or curious group member",
                        "Low-Moderate",
                        "A member can save your signed heartbeats/tombstones and later prove to a " +
                        "third party: \"Identity X was in this group at time T.\" Without this they " +
                        "could only say \"I saw them there\" (hearsay). This is the primary realistic threat.");

        append_separator(sb, color_sep);
        append_scenario(sb, color_lowmod,
                        "Sensitive group, partial data leak",
                        "Low-Moderate",
                        "e.g. seized phone without full forensics: the persistent 30-day roster with " +
                        "signed records could survive as evidence. The signatures make leaked data " +
                        "self-authenticating without the original device.");

        append_separator(sb, color_sep);
        append_scenario(sb, color_mod,
                        "Journalist / activist, group infiltrated",
                        "Moderate",
                        "An infiltrator can build a PROVABLE attendance log over time. The middleware " +
                        "upgrades \"I observed them\" to \"I can mathematically prove it to others.\"");

        append_separator(sb, color_sep);
        append_scenario(sb, color_na,
                        "State actor with full device access",
                        "N/A - middleware irrelevant",
                        "Keylogger / memory dump / malware already give them plaintext messages, keys, " +
                        "screenshots and network logs. The middleware adds nothing in this scenario.");

        append_separator(sb, color_sep);
        append_line(sb, "");
        append_line(sb,
                    "Do you understand these trade-offs and still want to enable the Persistent Peerlist?",
                    new StyleSpan(Typeface.BOLD),
                    new ForegroundColorSpan(color_body));

        return sb;
    }

    private static void append_line(SpannableStringBuilder sb, String text, Object... spans)
    {
        int start = sb.length();
        sb.append(text);
        int end = sb.length();
        if (spans != null)
        {
            for (Object sp : spans)
            {
                sb.setSpan(sp, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        sb.append("\n");
    }

    private static void append_separator(SpannableStringBuilder sb, int color)
    {
        int start = sb.length();
        sb.append("————————————————————————");
        sb.setSpan(new ForegroundColorSpan(color), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append("\n");
    }

    private static void append_scenario(SpannableStringBuilder sb, int likelihood_color,
                                        String title, String likelihood, String effect)
    {
        // title (bold)
        append_line(sb, title, new StyleSpan(Typeface.BOLD));

        // likelihood line: bold label + bold colored value
        int start = sb.length();
        sb.append("Likelihood: ");
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = sb.length();
        sb.append(likelihood);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(likelihood_color), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append("\n");

        // effect (gray body)
        append_line(sb, effect, new ForegroundColorSpan(Color.parseColor("#424242")));
        sb.append("\n");
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
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
            else if (preference instanceof RingtonePreference)
            {
                // For ringtone preferences, look up the correct display value
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
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
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

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
               Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                 PreferenceManager.getDefaultSharedPreferences(
                                                                         preference.getContext()).getString(
                                                                         preference.getKey(), ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName)
    {
        return PreferenceFragment.class.getName().equals(fragmentName) ||
               GeneralPreferenceFragment.class.getName().equals(fragmentName) ||
               NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final SwitchPreference pref_keepnpspam = (SwitchPreference) findPreference("U_keep_nospam");

            if (pref_keepnpspam.isChecked() == true)
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

            final SwitchPreference pref_persistent_peerlist = (SwitchPreference) findPreference("X_persistent_peerlist");
            if (pref_persistent_peerlist != null) {
                pref_persistent_peerlist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference, Object newValue) {
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
                                               "proof they can show to others. For most people, this changes nothing." +
                                               "\n(Note: 'you' here means your PeerID for that Group, not your real name or real identity.)");

                            addWarningScenario(container,
                                               "Activism, journalism, support group",
                                               "Think carefully",
                                               0xFFF44336, // Red
                                               "In sensitive groups, the danger is someone reporting on you. " +
                                               "Normally, an infiltrator can only say 'I saw them in the chat' " +
                                               "(which you can deny). With this feature, they get a mathematical " +
                                               "proof they can hand to others: 'Here is undeniable proof this PeerID " +
                                               "was in this group.'" +
                                               "\n(Note: 'you' means your PeerID for that Group, not your real name or real identity.)");

                            addWarningScenario(container,
                                               "State actor or high-risk target",
                                               "Changes nothing",
                                               0xFF9E9E9E, // Grey
                                               "If a powerful adversary is targeting you, they don't need this " +
                                               "feature to prove your PeerID is in a group. They likely already have " +
                                               "spyware on your phone, can read your screen, and can see your " +
                                               "messages. This feature doesn't make you safer, but it also " +
                                               "doesn't give them anything they didn't already have." +
                                               "\n(Note: 'you' means your PeerID for that Group, not your real name or real identity.)");

                            builder.setView(dialogView);
                            builder.setCancelable(true);

                            final AlertDialog dialog = builder.create();

                            dialogView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();   // switch stays OFF
                                }
                            });

                            dialogView.findViewById(R.id.btn_enable).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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

            pref_keepnpspam.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    // Here you can enable/disable whatever you need to
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
                    else
                    {
                    }
                    return true;
                }
            });

            Preference pref_download_push_msg_app = (Preference) findPreference("download_push_msg_app");
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


            Preference pref_custom_bootstrap_nodes = (Preference) findPreference("custom_bootstrap_nodes");

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
                    final EditText edit_bootstrap_udp_ip = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_udp_ip);
                    final EditText edit_bootstrap_udp_port = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_udp_port);
                    final EditText edit_bootstrap_udp_keyhex = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_udp_keyhex);
                    final EditText edit_bootstrap_tcp_ip = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_tcp_ip);
                    final EditText edit_bootstrap_tcp_port = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_tcp_port);
                    final EditText edit_bootstrap_tcp_keyhex = (EditText) promptsView.findViewById(
                            R.id.edit_bootstrap_tcp_keyhex);

                    alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                                                                              new DialogInterface.OnClickListener()
                                                                              {
                                                                                  public void onClick(DialogInterface dialog, int id)
                                                                                  {
                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP,
                                                                                                  edit_bootstrap_udp_ip.getText().toString()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_IP,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT,
                                                                                                  edit_bootstrap_udp_port.getText().toString()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_PORT,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX,
                                                                                                  edit_bootstrap_udp_keyhex.getText().toString().toUpperCase()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_UDP_KEYHEX,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP,
                                                                                                  edit_bootstrap_tcp_ip.getText().toString()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_IP,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT,
                                                                                                  edit_bootstrap_tcp_port.getText().toString()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_PORT,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX,
                                                                                                  edit_bootstrap_tcp_keyhex.getText().toString().toUpperCase()).apply();
                                                                                      }
                                                                                      catch (Exception e)
                                                                                      {
                                                                                          settings.edit().putString(
                                                                                                  PREF_KEY_CUSTOM_BOOTSTRAP_TCP_KEYHEX,
                                                                                                  "").apply();
                                                                                      }

                                                                                      try
                                                                                      {
                                                                                          change_custombootstrapnode_summary_text(
                                                                                                  pref_custom_bootstrap_nodes);
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
        }
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
            if (!udp_valid)
            {
                pref_custom_bootstrap_nodes.setSummary(
                        "using custom bootstrapnode:\nfor TCP: " + bs_tcp_ip + ":" + bs_tcp_port + "\nKEY: " +
                        bs_tcp_keyhex);
            }
            else
            {
                pref_custom_bootstrap_nodes.setSummary(
                        "using custom bootstrapnode:\nfor TCP: " + bs_tcp_ip + ":" + bs_tcp_port + "\nKEY: " +
                        bs_tcp_keyhex);
            }
        }
        return settings;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }
}
