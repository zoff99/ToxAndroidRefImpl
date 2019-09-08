package com.zoffcc.applications.trifa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.zoffcc.applications.trifa.TrifaToxService.trifa_service_thread;

public class WakeupAlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent2)
    {
        System.out.println("AlarmReceiver:" + "onReceive");
        TrifaToxService.write_debug_file("AlarmReceiver_onReceive");
        if (trifa_service_thread != null)
        {
            trifa_service_thread.interrupt();
            TrifaToxService.write_debug_file("AlarmReceiver_interrupt");
        }
    }
}
