package com.zoffcc.applications.trifa;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.zoffcc.applications.trifa.CallingActivity.update_audio_device_icon;
import static com.zoffcc.applications.trifa.ConferenceAudioActivity.update_group_audio_device_icon;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;

class HeadsetStateReceiver extends BroadcastReceiver
{
    static final String TAG = "trifa.HeadsetStReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            if (isInitialStickyBroadcast())
            {
                Log.i(TAG, "onReceive:headset:isInitialStickyBroadcast");
            }

            Log.i(TAG, "onReceive:" + intent + " isBluetoothConnected=" + isBluetoothConnected());

            if ((CallingActivity.activity_state == 1) || (ConferenceAudioActivity.activity_state == 1) ||
                (GroupAudioService.activity_state == 1))
            {
                if (intent.getAction().equals("android.intent.action.HEADSET_PLUG"))
                {
                    // Log.i(TAG, "onReceive:" + intent + ":" + intent.getIntExtra("state", 0) + ":" + intent.getStringExtra("name") + ":" + intent.getIntExtra("microphone", 0));

                    if (intent.getIntExtra("state", 0) == 1)
                    {
                        // headset plugged in
                        Log.i(TAG, "onReceive:headset:plugged in");
                        audio_manager_s.setSpeakerphoneOn(false);
                        audio_manager_s.setWiredHeadsetOn(true);
                        Callstate.audio_device = 1;
                        try
                        {
                            update_audio_device_icon();
                        }
                        catch (Exception e2)
                        {
                        }
                        try
                        {
                            update_group_audio_device_icon();
                        }
                        catch (Exception e2)
                        {
                        }
                        audio_manager_s.setBluetoothScoOn(false);
                    }
                    else
                    {
                        // headset unplugged
                        Log.i(TAG, "onReceive:headset:unplugged");
                        audio_manager_s.setWiredHeadsetOn(false);
                        Callstate.audio_device = 0;
                        try
                        {
                            update_audio_device_icon();
                        }
                        catch (Exception e2)
                        {
                        }
                        try
                        {
                            update_group_audio_device_icon();
                        }
                        catch (Exception e2)
                        {
                        }

                        if ((ConferenceAudioActivity.activity_state == 1) || (GroupAudioService.activity_state == 1))
                        {
                            audio_manager_s.setSpeakerphoneOn(true);
                        }
                        else
                        {
                            if (Callstate.audio_speaker)
                            {
                                audio_manager_s.setSpeakerphoneOn(true);
                            }
                            else
                            {
                                audio_manager_s.setSpeakerphoneOn(false);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (intent.getAction().equals("android.media.ACTION_SCO_AUDIO_STATE_UPDATED"))
            {
                Log.i(TAG, "onReceive:" + intent + ":" + intent.getStringExtra("EXTRA_SCO_AUDIO_STATE") + ":" +
                           intent.getStringExtra("EXTRA_SCO_AUDIO_PREVIOUS_STATE"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static boolean isBluetoothConnected()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            {
                int a2dpState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
                int headSetState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
                return (
                    (a2dpState == BluetoothAdapter.STATE_CONNECTED || a2dpState == BluetoothAdapter.STATE_CONNECTING) &&
                    (headSetState == BluetoothAdapter.STATE_CONNECTED ||
                     headSetState == BluetoothAdapter.STATE_CONNECTING));
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}
