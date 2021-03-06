package com.akausejr.crafty.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.akausejr.crafty.util.PreferenceHelper;

/**
 * Receiver that is notified when the device has low battery. If that happens, we stop passively
 * listening for location updates so that we don't use any network data when the power is low.
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PreferenceHelper prefs = new PreferenceHelper(context);
        if (!prefs.isAppInBackground()) {
            // If the app is in the foreground, then the Activity monitoring for location
            // updates will take care of handling power consumption
            return;
        }
        final boolean lowBattery = Intent.ACTION_BATTERY_LOW.equals(intent.getAction());
        PassiveLocationReceiver.setEnabled(context, !lowBattery);
    }
}
