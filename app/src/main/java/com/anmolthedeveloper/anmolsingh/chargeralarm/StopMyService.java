package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopMyService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Power.getInstance().turnOffAlarm();
        NotificationManager mNotificationManager = (NotificationManager) MainActivity.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(101);
    }
}
