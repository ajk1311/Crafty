package com.akausejr.crafty.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.model.NamedLocation;
import com.akausejr.crafty.service.BreweryLocationUpdateService;
import com.akausejr.crafty.util.DebugLog;
import com.akausejr.crafty.util.GeocodeUtils;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.location.FusedLocationProviderApi;

/**
 * This receiver passively waits for location updates. This means that our process isn't actively
 * polling for location, it is only notified when other apps are notified. When an update occurs,
 * we save the current location and update the content provider.
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class PassiveLocationReceiver extends BroadcastReceiver {

    private static final String TAG = PassiveLocationReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        final PreferenceHelper prefs = new PreferenceHelper(context);
        if (!prefs.isAppInBackground()) {
            // Let the active listener handle saving location and updating content
            // if the app is in the foreground
            return;
        }

        final Location location = intent.
            getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if (location != null) {
            GeocodeUtils.reverseGeocode(context, location, new GeocodeUtils.LocationNameListener() {
                @Override
                public void onLocationNameDecoded(String name) {
                    final NamedLocation currentLocation = new NamedLocation(location, name);
                    prefs.saveRecentLocation(currentLocation);
                    DebugLog.w(TAG, "calling update service at " + System.currentTimeMillis());
                    context.startService(new Intent(context, BreweryLocationUpdateService.class)
                        .putExtra(BreweryLocationUpdateService.EXTRA_LOCATION,
                            currentLocation.toLatLng())
                        .putExtra(BreweryLocationUpdateService.EXTRA_RADIUS,
                            CraftyApp.DEFAULT_SEARCH_RADIUS)
                        .putExtra(BreweryLocationUpdateService.EXTRA_FORCED, false));
                }
            });
        }
    }
}
