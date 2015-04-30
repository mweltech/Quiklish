package com.example.maw.quiklish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by maw on 11/04/2015.
 *
 * Implement the settings check box with the enable/disable on boot see:
 *
 * http://developer.android.com/training/scheduling/alarms.html#boot
 *
 * and also implement the alarm to kick of download
 *
 */

public class StartMyAppAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}
