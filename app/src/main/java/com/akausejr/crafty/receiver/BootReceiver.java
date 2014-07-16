package com.akausejr.crafty.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.akausejr.crafty.service.EnablePassiveLocationReceiverService;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Receiver that gets notified when the device boots up. Upon boot completion, if the app has been
 * opened at least once, then start passively listening for location updates.
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PreferenceHelper prefs = new PreferenceHelper(context);
        final int playServicesResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (playServicesResult == ConnectionResult.SUCCESS && prefs.wasAppOpenedOnce()) {
            // If the app has already been opened, start the passive location receiver on boot
            context.startService(new Intent(context, EnablePassiveLocationReceiverService.class));
        }
    }
}
