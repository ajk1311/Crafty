package com.akausejr.crafty.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import com.akausejr.crafty.receiver.PassiveLocationReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Service that ensures that a passive location listener is registered with the system.
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class EnablePassiveLocationReceiverService extends IntentService {

    private static final String TAG = EnablePassiveLocationReceiverService.class.getSimpleName();

    private static final String EXTRA_RETRY_COUNT = TAG + ".RETRY_COUNT";

    private static final int MAX_RETRY_COUNT = 8;

    public EnablePassiveLocationReceiverService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Initialize a client to Play Services to access location services
        final GoogleApiClient playServices = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build();

        // Connect on this thread. This is in the background so it's ok
        final ConnectionResult result = playServices.blockingConnect();

        if (result.isSuccess()) {
            // Request passive location updates
            final LocationRequest request = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_NO_POWER);
            final PendingIntent passiveLocationReceiver = PendingIntent.getBroadcast(this, 0,
                new Intent(this, PassiveLocationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.FusedLocationApi.requestLocationUpdates(playServices, request,
                passiveLocationReceiver);

            // Disconnect from Play Services and stop the service
            playServices.disconnect();
        } else {
            // If we can't connect to Play Services, then schedule this service to try again
            // using some naive exponential back off
            final int retryCount = intent.getIntExtra(EXTRA_RETRY_COUNT, 0);
            if (retryCount < MAX_RETRY_COUNT) {
                try {
                    Thread.sleep((long) (Math.pow(2, retryCount) * 1000));
                } catch (InterruptedException e) {
                }
            }
            startService(new Intent(this, EnablePassiveLocationReceiverService.class)
                .putExtra(EXTRA_RETRY_COUNT, retryCount + 1));
        }
    }
}
