package com.zoffcc.applications.trifa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static com.zoffcc.applications.trifa.TRIFAGlobals.HAVE_INTERNET_CONNECTIVITY;

public class ConnectionManager extends BroadcastReceiver
{
    private static final String TAG = "trifa.ConManager";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            Log.i(TAG, "onReceive:intent=" + intent);

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            HAVE_INTERNET_CONNECTIVITY = !noConnectivity;

            NetworkInfo info1 = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo info2 = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean failOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            Log.i(TAG, "onReceive:reason=" + reason);
            Log.i(TAG, "onReceive:failOver=" + failOver);
            Log.i(TAG, "onReceive:noConnectivity=" + noConnectivity);
            Log.i(TAG, "onReceive: mNetworkInfo=" + info1 + " mOtherNetworkInfo = " + (info2 == null ? "[none]" : info2 + " noConn=" + noConnectivity));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onReceive:EE:" + e.getMessage());

            // HINT: if in doubt, say that we have connectivity
            HAVE_INTERNET_CONNECTIVITY = true;
        }
    }
}
