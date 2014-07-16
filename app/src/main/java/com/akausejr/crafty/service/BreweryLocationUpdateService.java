package com.akausejr.crafty.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.akausejr.crafty.BuildConfig;
import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.model.BreweryLocation;
import com.akausejr.crafty.provider.BreweryLocationContract;
import com.akausejr.crafty.provider.CraftyContentProvider;
import com.akausejr.crafty.receiver.ConnectivityReceiver;
import com.akausejr.crafty.receiver.PassiveLocationReceiver;
import com.akausejr.crafty.util.CompatUtil;
import com.akausejr.crafty.util.DebugLog;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class BreweryLocationUpdateService extends IntentService {

    private static final String TAG = LocationDetailsUpdateService.class.getSimpleName();

    public static final String EXTRA_LOCATION = TAG + ".LOCATION";
    public static final String EXTRA_RADIUS = TAG + ".RADIUS";
    public static final String EXTRA_FORCED = TAG + ".FORCED";

    private static final float[] DISTANCE_BUF = new float[3];

    public BreweryLocationUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final PreferenceHelper prefs = new PreferenceHelper(this);
        final ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        final boolean backgroundDataAllowed = CompatUtil.isIceCreamSandwich() ?
            activeNetwork != null && activeNetwork.isConnectedOrConnecting() :
            connectivityManager.getBackgroundDataSetting();

        if (!backgroundDataAllowed && prefs.isAppInBackground()) {
            // Don't access network if we aren't allowed to perform background data and the app
            // is indeed in the background
            return;
        }

        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            // If we aren't connected, activate the connectivity receiver
            // and stop listening for location updates
            final PackageManager packageManager = getPackageManager();

            final ComponentName connectivityReceiver =
                new ComponentName(this, ConnectivityReceiver.class);
            packageManager.setComponentEnabledSetting(connectivityReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

            final ComponentName passiveLocationReceiver =
                new ComponentName(this, PassiveLocationReceiver.class);
            packageManager.setComponentEnabledSetting(passiveLocationReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ConnectivityManager.CONNECTIVITY_ACTION)
                .putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true));
        } else {
            final LatLng location = intent.getParcelableExtra(EXTRA_LOCATION);
            final double radius = intent.getDoubleExtra(EXTRA_RADIUS,
                CraftyApp.DEFAULT_SEARCH_RADIUS);

            if (intent.getBooleanExtra(EXTRA_FORCED, false) ||
                prefs.getLastUpdateTime() < System.currentTimeMillis() - CraftyApp.MAX_UPDATE_TIME) {
                // If the service was requested to force an update or we have waited long enough
                try {
                    updateBreweryLocations(location, radius);
                    prefs.setLastUpdateTimeToNow();
                } catch (Exception e) {
                    DebugLog.w(TAG, "Locations update failed");
                    DebugLog.printStackTrace(e);
                }
            }
        }
    }

    /**
     * Updates the content provider with location data from brewerydb.com near the given coordinates
     * @param location Coordinates
     * @param radius Search radius
     * @throws IOException
     * @throws RemoteException
     * @throws OperationApplicationException
     */
    private void updateBreweryLocations(LatLng location, double radius)
    throws IOException, RemoteException, OperationApplicationException {
        // Contains all of the operations we want to execute on the content provider
        final ArrayList<ContentProviderOperation> providerOps = new ArrayList<>();

        // Fetch the updated results
        final Map<String, BreweryLocation> remote =
            getRemoteLocations(location.latitude, location.longitude, radius);

        // Fetch the local store
        final ContentResolver provider = getContentResolver();
        final Cursor local = provider.query(BreweryLocationContract.CONTENT_URI,
            new String[] {BreweryLocationContract.ID},
            null, null, null);

        // Merge the results
        for (local.moveToFirst(); !local.isAfterLast(); local.moveToNext()) {
            final String locationId = local.getString(0);
            final Uri locationUri = BreweryLocationContract.buildLocationUri(locationId);
            final BreweryLocation match = remote.get(locationId);
            if (match != null) {
                // We already have an entry for this location, so schedule an update
                remote.remove(locationId);
                tryPrefetchBreweryDetails(match.breweryId);
                providerOps.add(ContentProviderOperation.newUpdate(locationUri)
                    .withValues(getValuesForBreweryLocation(location, match))
                    .build());
            } else {
                // The location isn't in the updated results, so schedule a delete
                providerOps.add(ContentProviderOperation.newDelete(locationUri).build());
            }
        }
        local.close();
        if (!remote.isEmpty()) {
            // If there are any remaining new locations, schedule inserts
            for (Map.Entry<String, BreweryLocation> entry : remote.entrySet()) {
                final BreweryLocation toInsert = entry.getValue();
                tryPrefetchBreweryDetails(toInsert.breweryId);
                providerOps.add(ContentProviderOperation
                    .newInsert(BreweryLocationContract.CONTENT_URI)
                    .withValues(getValuesForBreweryLocation(location, toInsert))
                    .build());
            }
        }
        if (!providerOps.isEmpty()) {
            provider.applyBatch(CraftyContentProvider.CONTENT_AUTHORITY, providerOps);
        }
    }

    /**
     * @param location The update location
     * @param brewLocation The brewery location model
     * @return values to be put into the content provider
     */
    private ContentValues getValuesForBreweryLocation(LatLng location, BreweryLocation brewLocation) {
        final ContentValues values = new ContentValues();
        values.put(BreweryLocationContract.ID, brewLocation.id);
        values.put(BreweryLocationContract.NAME, brewLocation.name);
        values.put(BreweryLocationContract.STATUS, brewLocation.status);
        values.put(BreweryLocationContract.STATUS_DISPLAY, brewLocation.statusDisplay);
        values.put(BreweryLocationContract.LOCATION_TYPE, brewLocation.locationType);
        values.put(BreweryLocationContract.LOCATION_TYPE_DISPLAY, brewLocation.locationTypeDisplay);
        values.put(BreweryLocationContract.LATITUDE, brewLocation.latitude);
        values.put(BreweryLocationContract.LONGITUDE, brewLocation.longitude);
        values.put(BreweryLocationContract.FULL_ADDRESS, brewLocation.getDisplayAddress());
        values.put(BreweryLocationContract.PHONE, brewLocation.phone);
        values.put(BreweryLocationContract.DISTANCE,
            getDistanceToBreweryLocation(location, brewLocation));
        values.put(BreweryLocationContract.BREWERY_ID, brewLocation.breweryId);
        values.put(BreweryLocationContract.BREWERY_NAME, brewLocation.brewery.name);
        values.put(BreweryLocationContract.BREWERY_STATUS, brewLocation.brewery.status);
        values.put(BreweryLocationContract.BREWERY_STATUS_DISPLAY,
            brewLocation.brewery.statusDisplay);
        values.put(BreweryLocationContract.BREWERY_DESCRIPTION, brewLocation.brewery.description);
        values.put(BreweryLocationContract.BREWERY_ESTABLISH_DATE,
            brewLocation.brewery.established);
        if (brewLocation.brewery.images != null) {
            values.put(BreweryLocationContract.BREWERY_ICON_IMAGE_URL,
                brewLocation.brewery.images.icon);
            values.put(BreweryLocationContract.BREWERY_MEDIUM_IMAGE_URL,
                brewLocation.brewery.images.medium);
            values.put(BreweryLocationContract.BREWERY_LARGE_IMAGE_URL,
                brewLocation.brewery.images.large);
        }
        values.put(BreweryLocationContract.BREWERY_IS_ORGANIC, brewLocation.brewery.isOrganic);
        values.put(BreweryLocationContract.BREWERY_WEBSITE, brewLocation.brewery.website);
        return values;
    }

    /**
     * @param location The update location
     * @param brewLocation The brewery location model
     * @return the distance from the update location to the brewery location
     */
    private float getDistanceToBreweryLocation(LatLng location, BreweryLocation brewLocation) {
        Location.distanceBetween(location.latitude, location.longitude,
            brewLocation.latitude, brewLocation.longitude, DISTANCE_BUF);
        return DISTANCE_BUF[0];
    }

    /**
     *
     * @param breweryId
     */
    private void tryPrefetchBreweryDetails(String breweryId) {

    }

    /**
     * Retrieves a collection of BreweryLocations from brewerydb.com that are near the given
     * coordinates within the given radius. Uses Gson {@link com.google.gson.stream.JsonReader}
     * to parse the JSON stream from the connection.
     * @param lat Latitude
     * @param lng Longitude
     * @param radius Search radius in meters
     * @return A mapping of ids to locations
     * @throws IOException
     */
    private Map<String, BreweryLocation> getRemoteLocations(double lat, double lng, double radius)
    throws IOException {
        // Construct the url for searching breweries
        final String searchUrl = BuildConfig.BDB_BASE_URL + "/search/geo/point?" +
            "key=" + BuildConfig.BDB_API_KEY + "&lat=" + lat + "&lng=" + lng +
            "&radius=" + radius / 1000 + "&unit=km";
        DebugLog.d(TAG, "Search url: " + searchUrl);

        // Open the connection
        final HttpURLConnection connection = (HttpURLConnection) new URL(searchUrl).openConnection();

        // Only continue if the connection was successful
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Allocate the map to return
            final Map<String, BreweryLocation> remoteCollection = new HashMap<>();

            // If the connection was successful, start parsing the response
            final Gson gson = new Gson();
            final InputStream inStream = connection.getInputStream();
            final JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));
            reader.beginObject();
            while (reader.hasNext()) {
                // We only care about the data field of the response, which is an array of locations
                if ("data".equals(reader.nextName())) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        // For each location in the array, parse it and add it to the map
                        final BreweryLocation location = gson.fromJson(reader, BreweryLocation.class);
                        remoteCollection.put(location.id, location);
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            reader.close();
            return remoteCollection;
        } else {
            throw new IllegalStateException("Locations update failed: " +
                connection.getResponseMessage());
        }
    }
}
