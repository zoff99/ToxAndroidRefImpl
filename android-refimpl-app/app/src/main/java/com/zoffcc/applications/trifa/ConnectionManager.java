package com.zoffcc.applications.trifa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import static com.zoffcc.applications.trifa.HelperGeneric.append_logger_msg;
import static com.zoffcc.applications.trifa.TRIFAGlobals.HAVE_INTERNET_CONNECTIVITY;
import static com.zoffcc.applications.trifa.TrifaToxService.bootstrap_me;

public class ConnectionManager extends BroadcastReceiver
{
    private static final String TAG = "trifa.ConManager";

    // [ADDED] Holds the modern NetworkCallback. If non-null, the modern monitor is active.
    private static ConnectivityManager.NetworkCallback modernNetworkCallback = null;

    // [ADDED] WeakReferences to UI views so we can update them without leaking the Activity.
    private static WeakReference<ImageView> uiIconRef = null;
    private static WeakReference<TextView> uiTextRef = null;

    // [ADDED] Static enum describing every connectivity / monitor state.
    //
    // REGISTERED / UNREGISTERED  -> lifecycle of the modern NetworkCallback.
    // VALIDATED / UNVALIDATED / LOST -> modern NET_CAPABILITY_VALIDATED results.
    // OLD_FALLBACK_COMBINED_* -> used by the legacy BroadcastReceiver path.
    // ERROR_FALLBACK_ASSUME_CONNECTED -> used when we fall back to "assume online".
    public enum InternetConnectivityState
    {
        UNREGISTERED(0, "unregistered"),
        REGISTERED(1, "registered"),

        VALIDATED(2, "validated"),
        UNVALIDATED(3, "unvalidated"),
        LOST(4, "lost"),

        OLD_FALLBACK_COMBINED_CONNECTED(5, "old_fallback_combined_connected"),
        OLD_FALLBACK_COMBINED_DISCONNECTED(6, "old_fallback_combined_disconnected"),

        ERROR_FALLBACK_ASSUME_CONNECTED(7, "error_fallback_assume_connected");

        public final int value;
        public final String text;

        InternetConnectivityState(int value, String text)
        {
            this.value = value;
            this.text = text;
        }

        public static InternetConnectivityState fromInt(int value)
        {
            for (InternetConnectivityState s : values())
            {
                if (s.value == value)
                {
                    return s;
                }
            }

            return UNREGISTERED;
        }
    }

    // [ADDED] Current state as enum.
    public static volatile InternetConnectivityState CURRENT_INTERNET_CONNECTIVITY_STATE =
            InternetConnectivityState.UNREGISTERED;

    // [ADDED] Current state as int.
    public static volatile int CURRENT_INTERNET_CONNECTIVITY_STATE_INT =
            InternetConnectivityState.UNREGISTERED.value;

    // [ADDED] Current state as human-readable String.
    public static volatile String CURRENT_INTERNET_CONNECTIVITY_STATE_TEXT =
            InternetConnectivityState.UNREGISTERED.text;

    // [ADDED] Central helper that updates the state variables, HAVE_INTERNET_CONNECTIVITY,
    // notifies internet_conn_state_changed(...), and triggers bootstrap_me() when needed.
    //
    // haveInternetOrNull:
    //   true  -> set HAVE_INTERNET_CONNECTIVITY = true
    //   false -> set HAVE_INTERNET_CONNECTIVITY = false
    //   null  -> do not modify HAVE_INTERNET_CONNECTIVITY (lifecycle-only states)
    //
    // forceNotify:
    //   true  -> call internet_conn_state_changed(...) even if nothing changed
    //   false -> call only when the state actually changed
    private static void set_internet_conn_state_internal(InternetConnectivityState newState,
                                                         Boolean haveInternetOrNull,
                                                         boolean forceNotify)
    {
        boolean shouldBootstrap = false;
        boolean shouldNotify = false;

        synchronized (ConnectionManager.class)
        {
            final InternetConnectivityState oldState = CURRENT_INTERNET_CONNECTIVITY_STATE;
            final String oldText = CURRENT_INTERNET_CONNECTIVITY_STATE_TEXT;
            final boolean oldConnectivity = HAVE_INTERNET_CONNECTIVITY;

            CURRENT_INTERNET_CONNECTIVITY_STATE = newState;
            CURRENT_INTERNET_CONNECTIVITY_STATE_INT = newState.value;
            CURRENT_INTERNET_CONNECTIVITY_STATE_TEXT = newState.text;

            if (haveInternetOrNull != null)
            {
                HAVE_INTERNET_CONNECTIVITY = haveInternetOrNull;
            }

            if (oldState != newState)
            {
                shouldNotify = true;
            }

            if (!oldText.equals(newState.text))
            {
                shouldNotify = true;
            }

            if (oldConnectivity != HAVE_INTERNET_CONNECTIVITY)
            {
                shouldNotify = true;
            }

            if (forceNotify)
            {
                shouldNotify = true;
            }

            if ((oldConnectivity != HAVE_INTERNET_CONNECTIVITY) && (HAVE_INTERNET_CONNECTIVITY == true))
            {
                shouldBootstrap = true;
            }

            append_logger_msg(TAG + "::" +
                              "state=" + CURRENT_INTERNET_CONNECTIVITY_STATE_INT +
                              " text=" + CURRENT_INTERNET_CONNECTIVITY_STATE_TEXT +
                              " HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY +
                              " old_state=" + oldState.text +
                              " old_connectivity=" + oldConnectivity);
        }

        if (shouldNotify)
        {
            internet_conn_state_changed(newState.value);
        }

        if (shouldBootstrap)
        {
            append_logger_msg(TAG + "::" + "bootstrap_me()");
            bootstrap_me(false);
        }
    }

    // [ADDED] Required callback. Called whenever the state changes, and also
    // force-called on registering and unregistering.
    static void internet_conn_state_changed(int state)
    {
        InternetConnectivityState s = InternetConnectivityState.fromInt(state);

        Log.i(TAG, "internet_conn_state_changed: state=" + state + " text=" + s.text +
                   " HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY);

        append_logger_msg(TAG + "::" +
                          "internet_conn_state_changed state=" + state +
                          " text=" + s.text +
                          " HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY);

        // [ADDED] Push the new state to the UI.
        updateConnectivityUI(s);
    }

    /**
     * [ADDED] Call this once from your Activity/Fragment after the layout is inflated.
     * Example:
     *   ConnectionManager.attachUIViews(
     *       findViewById(R.id.internet_conn_icon),
     *       findViewById(R.id.internet_conn_text));
     */
    public static void attachUIViews(ImageView icon, TextView text)
    {
        uiIconRef = new WeakReference<>(icon);
        uiTextRef = new WeakReference<>(text);

        // Re-apply the current state now that the views exist.
        refreshConnectivityUI();
    }

    // [ADDED] Main-thread handler so UI updates ALWAYS run on the UI thread,
    // no matter which thread the state change came from.
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    // [CHANGED] Kept for compatibility with internet_conn_state_changed().
    // It now simply schedules a refresh; the real work reads the LATEST state.
    private static void updateConnectivityUI(InternetConnectivityState state)
    {
        refreshConnectivityUI();
    }

    /**
     * [ADDED] Public: force the icon/text to redraw from the CURRENT state.
     * Call this in Activity.onResume() so the views are correct after
     * screen rotation / Activity recreation.
     */
    public static void refreshConnectivityUI()
    {
        MAIN_HANDLER.post(ConnectionManager::applyCurrentStateToViews);
    }

    // [CHANGED] Applies the CURRENT state to the views. Must run on the main thread.
    // Reads the latest state at execution time so it never shows a stale value.
    private static void applyCurrentStateToViews()
    {
        if (uiIconRef == null || uiTextRef == null)
        {
            return;
        }

        final ImageView iconView = uiIconRef.get();
        final TextView textView = uiTextRef.get();

        if (iconView == null || textView == null)
        {
            return;
        }

        // Always read the latest state (never a captured value).
        final InternetConnectivityState state = CURRENT_INTERNET_CONNECTIVITY_STATE;

        int iconRes;
        int textColor;
        String displayText;
        Context context = iconView.getContext();

        switch (state)
        {
            case REGISTERED:
                displayText = "Checking";
                iconRes = R.drawable.ic_network_unknown;
                textColor = ContextCompat.getColor(context, R.color.md_blue_700);
                break;

            case UNREGISTERED:
                displayText = "Mon. off";
                iconRes = R.drawable.ic_network_unknown;
                textColor = ContextCompat.getColor(context, R.color.md_grey_700);
                break;

            case VALIDATED:
                displayText = "Online";
                iconRes = R.drawable.ic_network_validated;
                textColor = ContextCompat.getColor(context, R.color.md_green_700);
                break;

            case UNVALIDATED:
                displayText = "Limited";
                iconRes = R.drawable.ic_network_unvalidated;
                textColor = ContextCompat.getColor(context, R.color.md_orange_700);
                break;

            case LOST:
                displayText = "Offline";
                iconRes = R.drawable.ic_network_offline;
                textColor = ContextCompat.getColor(context, R.color.md_red_700);
                break;

            case OLD_FALLBACK_COMBINED_CONNECTED:
                displayText = "Legacy on";
                iconRes = R.drawable.ic_network_validated;
                textColor = ContextCompat.getColor(context, R.color.md_green_700);
                break;

            case OLD_FALLBACK_COMBINED_DISCONNECTED:
                displayText = "Legacy off";
                iconRes = R.drawable.ic_network_offline;
                textColor = ContextCompat.getColor(context, R.color.md_red_700);
                break;

            case ERROR_FALLBACK_ASSUME_CONNECTED:
                displayText = "Assumed";
                iconRes = R.drawable.ic_network_validated;
                textColor = ContextCompat.getColor(context, R.color.md_amber_700);
                break;

            default:
                displayText = "??";
                iconRes = R.drawable.ic_network_unknown;
                textColor = ContextCompat.getColor(context, R.color.md_grey_700);
                break;
        }
        Log.i(TAG, "applyCurrentStateToViews: applying state=" + state.text + " text=" + displayText);

        iconView.setImageResource(iconRes);
        textView.setText(displayText);
        textView.setTextColor(textColor);
    }

    /**
     * [ADDED] Register the modern NetworkCallback.
     * Call this once from your service/application startup.
     * This gives you NET_CAPABILITY_VALIDATED, which the old BroadcastReceiver cannot provide.
     */
    public static void registerModernNetworkCallback(Context context)
    {
        try
        {
            if (context == null)
            {
                return;
            }

            final Context appContext = context.getApplicationContext();

            if (modernNetworkCallback != null)
            {
                // Already registered. Still force-notify as requested.
                set_internet_conn_state_internal(InternetConnectivityState.REGISTERED, null, true);
                return;
            }

            ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm == null)
            {
                // If we cannot get ConnectivityManager, fall back to "assume connected".
                set_internet_conn_state_internal(
                        InternetConnectivityState.ERROR_FALLBACK_ASSUME_CONNECTED,
                        true,
                        true);
                return;
            }

            // NET_CAPABILITY_VALIDATED is only reliable on Android M (API 23) and above.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            {
                NetworkInfo activeInfo = cm.getActiveNetworkInfo();
                boolean connected = (activeInfo != null) && activeInfo.isConnected();

                set_internet_conn_state_internal(
                        connected ?
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_CONNECTED :
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_DISCONNECTED,
                        connected,
                        true);

                return;
            }

            // Notify that the modern monitor is registered (lifecycle state).
            set_internet_conn_state_internal(InternetConnectivityState.REGISTERED, null, true);

            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            modernNetworkCallback = new ConnectivityManager.NetworkCallback()
            {
                @Override
                public void onAvailable(Network network)
                {
                    // A network appeared. Re-check the currently active/default network.
                    Log.i(TAG, "onAvailable: network=" + network);
                    update_state_from_active_network(appContext, "onAvailable");
                }

                @Override
                public void onLost(Network network)
                {
                    // A network was lost. Another network may still be active, so re-check.
                    Log.i(TAG, "onLost: network=" + network);
                    update_state_from_active_network(appContext, "onLost");
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities)
                {
                    // Capabilities changed. This is where VALIDATED may appear/disappear.
                    Log.i(TAG, "onCapabilitiesChanged: network=" + network + " caps=" + networkCapabilities);
                    update_state_from_active_network(appContext, "onCapabilitiesChanged");
                }

                @Override
                public void onUnavailable()
                {
                    // No matching network available.
                    Log.i(TAG, "onUnavailable");
                    set_internet_conn_state_internal(InternetConnectivityState.LOST, false, false);
                }
            };

            cm.registerNetworkCallback(request, modernNetworkCallback);

            append_logger_msg(TAG + "::" + "Modern NetworkCallback registered.");

            // Immediately initialize state from the currently active network.
            update_state_from_active_network(appContext, "register_initial_check");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "registerModernNetworkCallback:EE:" + e.getMessage());

            // Preserve existing design: if in doubt, assume connectivity.
            set_internet_conn_state_internal(
                    InternetConnectivityState.ERROR_FALLBACK_ASSUME_CONNECTED,
                    true,
                    true);
        }
    }

    /**
     * [ADDED] Unregister the modern NetworkCallback.
     * Only call this when you truly want to stop network monitoring.
     * If your Tox service keeps running in the background, do NOT call this from Activity.onDestroy().
     */
    public static void unregisterModernNetworkCallback(Context context)
    {
        try
        {
            if (context == null)
            {
                set_internet_conn_state_internal(InternetConnectivityState.UNREGISTERED, false, true);
                return;
            }

            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if ((cm != null) && (modernNetworkCallback != null))
            {
                cm.unregisterNetworkCallback(modernNetworkCallback);
            }

            modernNetworkCallback = null;

            // Force-notify as requested on unregistering.
            set_internet_conn_state_internal(InternetConnectivityState.UNREGISTERED, false, true);

            append_logger_msg(TAG + "::" + "Modern NetworkCallback unregistered.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "unregisterModernNetworkCallback:EE:" + e.getMessage());

            modernNetworkCallback = null;

            set_internet_conn_state_internal(InternetConnectivityState.UNREGISTERED, false, true);
        }
    }

    // [ADDED] Reads the currently active/default Android network and maps it to our enum state.
    // Uses NET_CAPABILITY_VALIDATED on Android M+.
    private static void update_state_from_active_network(Context context, String source)
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm == null)
            {
                set_internet_conn_state_internal(
                        InternetConnectivityState.ERROR_FALLBACK_ASSUME_CONNECTED,
                        true,
                        false);
                return;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            {
                NetworkInfo activeInfo = cm.getActiveNetworkInfo();
                boolean connected = (activeInfo != null) && activeInfo.isConnected();

                append_logger_msg(TAG + "::" + source + " old fallback activeInfo=" + activeInfo);

                set_internet_conn_state_internal(
                        connected ?
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_CONNECTED :
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_DISCONNECTED,
                        connected,
                        false);

                return;
            }

            Network activeNetwork = cm.getActiveNetwork();

            if (activeNetwork == null)
            {
                append_logger_msg(TAG + "::" + source + " activeNetwork=null");
                set_internet_conn_state_internal(InternetConnectivityState.LOST, false, false);
                return;
            }

            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);

            if (caps == null)
            {
                append_logger_msg(TAG + "::" + source + " caps=null");
                set_internet_conn_state_internal(InternetConnectivityState.LOST, false, false);
                return;
            }

            boolean hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            append_logger_msg(TAG + "::" + source +
                              " hasInternet=" + hasInternet +
                              " isValidated=" + isValidated +
                              " caps=" + caps);

            if (hasInternet && isValidated)
            {
                set_internet_conn_state_internal(InternetConnectivityState.VALIDATED, true, false);
            }
            else
            {
                set_internet_conn_state_internal(InternetConnectivityState.UNVALIDATED, false, false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_state_from_active_network:EE:" + e.getMessage());

            // Preserve conservative existing behavior.
            set_internet_conn_state_internal(
                    InternetConnectivityState.ERROR_FALLBACK_ASSUME_CONNECTED,
                    true,
                    false);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            Log.i(TAG, "onReceive:intent=" + intent);

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            final boolean connectivity_old = HAVE_INTERNET_CONNECTIVITY;

            // [CHANGED] Old BroadcastReceiver fallback.
            //
            // If modernNetworkCallback is active, we do not allow this old receiver to override
            // NET_CAPABILITY_VALIDATED decisions.
            //
            // If modernNetworkCallback is not active, we use the old fallback combined states.
            if (modernNetworkCallback == null)
            {
                // [CHANGED] Do not set HAVE_INTERNET_CONNECTIVITY directly here.
                // Let set_internet_conn_state_internal() own the transition so it can
                // correctly detect false->true changes and call bootstrap_me() exactly once.
                final boolean newValue = !noConnectivity;

                set_internet_conn_state_internal(
                        newValue ?
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_CONNECTED :
                                InternetConnectivityState.OLD_FALLBACK_COMBINED_DISCONNECTED,
                        newValue,
                        false);

                append_logger_msg(TAG + "::" + "HAVE_INTERNET_CONNECTIVITY=" + HAVE_INTERNET_CONNECTIVITY + " connectivity_old=" + connectivity_old);

                // [CHANGED] bootstrap_me() is now handled inside set_internet_conn_state_internal()
                // when connectivity transitions from false to true.
            }
            else
            {
                // [ADDED] Modern callback is active, so this BroadcastReceiver is diagnostic only.
                append_logger_msg(TAG + "::" + "legacy onReceive ignored because modernNetworkCallback is active");
            }

            NetworkInfo info1 = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo info2 = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean failOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            Log.i(TAG, "onReceive:reason=" + reason);
            Log.i(TAG, "onReceive:failOver=" + failOver);
            Log.i(TAG, "onReceive:noConnectivity=" + noConnectivity);
            Log.i(TAG, "onReceive: mNetworkInfo=" + info1 + " mOtherNetworkInfo = " + (info2 == null ? "[none]" : info2 + " noConn=" + noConnectivity));
            append_logger_msg(TAG + "::" + "reason=" + reason + " failOver=" + failOver);
            append_logger_msg(TAG + "::" + "mNetworkInfo=" + info1);
            append_logger_msg(TAG + "::" + "mOtherNetworkInfo=" + (info2 == null ? "[none]" : info2));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onReceive:EE:" + e.getMessage());

            // HINT: if in doubt, say that we have connectivity
            set_internet_conn_state_internal(
                    InternetConnectivityState.ERROR_FALLBACK_ASSUME_CONNECTED,
                    true,
                    false);
            append_logger_msg(TAG + "::" + "HAVE_INTERNET_CONNECTIVITY[hardcoded]=" + HAVE_INTERNET_CONNECTIVITY);
        }
    }
}
