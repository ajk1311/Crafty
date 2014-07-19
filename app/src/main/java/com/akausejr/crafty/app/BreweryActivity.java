package com.akausejr.crafty.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.R;
import com.akausejr.crafty.model.LocationType;
import com.akausejr.crafty.model.NamedLocation;
import com.akausejr.crafty.provider.SearchSuggestionProvider;
import com.akausejr.crafty.receiver.PassiveLocationReceiver;
import com.akausejr.crafty.service.BreweryLocationUpdateService;
import com.akausejr.crafty.service.UserActivityService;
import com.akausejr.crafty.util.DebugLog;
import com.akausejr.crafty.util.GeocodeUtils;
import com.akausejr.crafty.util.LoadPlaceCoordinatesTask;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

/**
 * {@link android.app.Activity} that controls views to display a list of nearby breweries. The list
 * will update based on the user's current location.
 *
 * @author AJ Kause
 * Created on 7/7/2014
 */
public class BreweryActivity extends Activity implements LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    BreweryMapFragment.BreweryMapController, BreweryListFragment.BreweryListController {

    /** Used for tagging and debugging */
    private static final String TAG = BreweryActivity.class.getSimpleName();

    // Saved state keys
    private static final String KEY_CURRENT_TITLE = TAG + ".CURRENT_TITLE";
    private static final String KEY_CURRENT_VIEW = TAG + ".CURRENT_VIEW";
    private static final String KEY_TRACK_LOCATION = TAG + ".TRACK_LOCATION";

    private static final int VIEW_MAP = 0;
    private static final int VIEW_LIST = 1;

    /** How often to poll for the user's activity from Play Services */
    private static final int ACTIVITY_UPDATE_INTERVAL = 30 * 1000; // 30 seconds

    /** The fastest we'll ever try to poll a user's location */
    private static final int LOCATION_INTERVAL_FASTEST = 5 * 60 * 1000; // 5 minutes

    /** The interval for which a still user's location is polled */
    private static final int LOCATION_INTERVAL_STILL = 30 * 60 * 1000; // 30 minutes

    /** If we can't determine the user's activity, use this value for location updates */
    private static final int LOCATION_INTERVAL_UNKNOWN = 5 * 60 * 1000; // 5 minutes

    /** The interval for which a walking user's location is polled */
    private static final int LOCATION_INTERVAL_WALKING = 10 * 60 * 1000; // 10 minutes

    /** The interval for which a driving user's location is polled */
    private static final int LOCATION_INTERVAL_IN_VEHICLE = 2 * 60 * 1000; // 2 minutes

    /** Hold the current title so it survives a configuration change */
    private CharSequence mCurrentTitle;

    /** How the current results are being displayed */
    private int mCurrentView = VIEW_MAP;

    // Fragment references
    private BreweryMapFragment mMapFragment;
    private BreweryListFragment mListFragment;

    /**
     * The menu item that shows the options for displaying breweries. Its title and icon
     * are toggled on click, so we need a reference here.
     */
    private MenuItem mViewMenuItem;

    /** App shared preferences */
    private PreferenceHelper mPreferenceHelper;

    /** Client that lets us connect to Google Play location and activity recognition services */
    private GoogleApiClient mPlayServices;

    /** Flag indicating whether or not to fetch content for each location update */
    private boolean mTrackLocation = true;

    /** Task that loads the coordinates for a place */
    private LoadPlaceCoordinatesTask mPlacesTask;

    /** Notified when the user's detected activity changes */
    private BroadcastReceiver mActivityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // We receive broadcasts when the user changes activity, so refresh the
            // location request we are using for active listening
            setActiveLocationListenerEnabled(true);
        }
    };

    /** Notified when a change in network connectivity occurs */
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // We receiver broadcasts when we try to update the brewery locations but
            // we don't have connectivity. Listening for location updates is pointless now.
            final boolean noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            DebugLog.d(TAG, (noConnectivity ? "Lost" : "Gained") + " connectivity");
            setActiveLocationListenerEnabled(!noConnectivity);
            setActivityRecognitionEnabled(!noConnectivity);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferenceHelper = new PreferenceHelper(this);

        // Create the client and request location and activity recognition services
        mPlayServices = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addApi(ActivityRecognition.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        // The Fragments are added via xml, so we can just grab them here
        mMapFragment = (BreweryMapFragment) getFragmentManager()
            .findFragmentById(R.id.brewery_map);
        mListFragment = (BreweryListFragment) getFragmentManager()
            .findFragmentById(R.id.brewery_list);

        if (savedInstanceState != null) {
            // Restore the title and view option
            setTitle(savedInstanceState.getString(KEY_CURRENT_TITLE));
            mCurrentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            mTrackLocation = savedInstanceState.getBoolean(KEY_TRACK_LOCATION);
        }

        // We always need to show and hide the right fragment, whether a config change occurred
        // or not. If the Activity is starting for the first time, mCurrentView's default is used
        final FragmentTransaction tx = getFragmentManager().beginTransaction();
        switch (mCurrentView) {
            case VIEW_MAP:
                tx.show(mMapFragment).hide(mListFragment).commit();
                break;
            case VIEW_LIST:
                tx.show(mListFragment).hide(mMapFragment).commit();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        outState.putString(KEY_CURRENT_TITLE, mCurrentTitle.toString());
        outState.putBoolean(KEY_TRACK_LOCATION, mTrackLocation);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (SearchSuggestionProvider.ACTION_SEARCH_SUGGESTION.equals(intent.getAction())) {
            // If this activity got a search suggestion, execute the "search."
            // In our case, we just move the map to the loaded coordinates and let
            // the map fragment take care of updating the content
            final Uri suggestionUri = intent.getData();
            switch (getContentResolver().getType(suggestionUri)) {
                case SearchSuggestionProvider.PLACES_CONTENT_TYPE:
                    final String placeId = suggestionUri.getLastPathSegment();
                    moveMapToPlace(placeId);
                    break;
                case SearchSuggestionProvider.BREWERIES_CONTENT_TYPE:
                    final String breweryId = suggestionUri.getLastPathSegment();
                    showBreweryDetails(breweryId);
                    break;
            }
        }
    }

    private void moveMapToPlace(String placeId) {
        showProgress(true);
        mPlacesTask = new LoadPlaceCoordinatesTask().execute(placeId,
            new LoadPlaceCoordinatesTask.OnCoordinatesLoadedListener() {
                @Override
                public void onCoordinatesLoaded(LatLng coordinates) {
                    if (coordinates == null) {
                        return;
                    }
                    mMapFragment.getMap().animateCamera(CameraUpdateFactory
                        .newLatLngZoom(coordinates, BreweryMapFragment.DEFAULT_ZOOM));
                    showProgress(false);
                }
            });
    }

    private void showBreweryDetails(String breweryId) {
        Toast.makeText(getApplicationContext(),
            "TODO: fetch and display brewery details", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPreferenceHelper.setAppIsInBackground(false);

        final NamedLocation recentLocation = mPreferenceHelper.getRecentLocation();
        if (recentLocation == null) {
            // This would normally only happen the very first time the user opens the app
            Toast.makeText(this, R.string.main_activity_location_waiting, Toast.LENGTH_SHORT)
                .show();
        } else if (mTrackLocation) {
            // Fetch fresh results based on the last known location
            setTitle(recentLocation.getName());
            final LatLng ll = recentLocation.toLatLng();
            DebugLog.w(TAG, "calling update service from onResume() at " + System.currentTimeMillis());
            refreshLocations(ll, mMapFragment.getSearchRadius(), false);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityUpdateReceiver,
            new IntentFilter(UserActivityService.ACTION_ACTIVITY_UPDATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityReceiver,
            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityReceiver);

        if (isFinishing()) {
            mPreferenceHelper.setAppIsInBackground(true);
            setActiveLocationListenerEnabled(false);
            setActivityRecognitionEnabled(false);
        }

        if (mPlacesTask != null && mPlacesTask.getStatus() != AsyncTask.Status.FINISHED) {
            mPlacesTask.release();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mPlayServices.isConnected() && !mPlayServices.isConnecting()) {
            mPlayServices.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        setActivityRecognitionEnabled(true);
        startListeningForLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayServices.isConnected()) {
            mPlayServices.disconnect();
        }
    }

    /**
     * Registers a {@link android.app.PendingIntent} to be fired when Play Services detects
     * that the user's activity has changed
     * @param enabled {@code true} to register, {@code false} to unregister
     */
    private void setActivityRecognitionEnabled(boolean enabled) {
        final PendingIntent userActivityService = PendingIntent.getService(this, 0,
            new Intent(this, UserActivityService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if (enabled) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mPlayServices,
                ACTIVITY_UPDATE_INTERVAL, userActivityService);
        } else {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mPlayServices,
                userActivityService);
        }
    }

    /**
     * Enables location listening for our process. In the foreground via LocationListener, in
     * the background via PendingIntent with no power consumption
     */
    private void startListeningForLocation() {
        setActiveLocationListenerEnabled(true);

        final PendingIntent passiveReceiverIntent = PendingIntent.getBroadcast(this, 0,
            new Intent(this, PassiveLocationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        final LocationRequest request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_NO_POWER);
        LocationServices.FusedLocationApi.requestLocationUpdates(mPlayServices, request,
            passiveReceiverIntent);
    }

    /**
     * Registers a {@link com.google.android.gms.location.LocationListener} to be notified when
     * Play Services detects that the user's current location has changed
     * @param enabled {@code true} to register, {@code false} to unregister
     */
    private void setActiveLocationListenerEnabled(boolean enabled) {
        if (enabled) {
            final LocationRequest request = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(LOCATION_INTERVAL_FASTEST)
                .setInterval(calculateBestInterval());
            LocationServices.FusedLocationApi.requestLocationUpdates(mPlayServices, request, this);
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mPlayServices, this);
        }
    }

    /**
     * Based on the user's detected activity and other tbd factors, calculate the most efficient
     * interval to poll for location updates
     * @return the interval
     */
    private long calculateBestInterval() {
        // TODO factor in other things like battery power or charging
        int bestInterval;
        switch (mPreferenceHelper.getRecentUserActivity()) {
            case DetectedActivity.STILL:
                bestInterval = LOCATION_INTERVAL_STILL;
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.ON_BICYCLE:
                bestInterval = LOCATION_INTERVAL_WALKING;
                break;
            case DetectedActivity.IN_VEHICLE:
                bestInterval = LOCATION_INTERVAL_IN_VEHICLE;
                break;
            default:
                bestInterval = LOCATION_INTERVAL_UNKNOWN;
                break;
        }
        DebugLog.d(TAG, "Calculating best location update interval as " + bestInterval);
        return bestInterval;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(((SearchManager) getSystemService(SEARCH_SERVICE))
            .getSearchableInfo(getComponentName()));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchItem.collapseActionView();
                return false;
            }
        });

        mViewMenuItem = menu.findItem(R.id.action_change_view);
        switch (mCurrentView) {
            case VIEW_MAP:
                mViewMenuItem.setTitle(R.string.menu_main_list_view);
                mViewMenuItem.setIcon(R.drawable.ic_action_list);
                break;
            case VIEW_LIST:
                mViewMenuItem.setTitle(R.string.menu_main_map_view);
                mViewMenuItem.setIcon(R.drawable.ic_action_map);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_view:
                handleViewChange();
                return true;
            case R.id.action_settings:
                // TODO show settings
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Toggle the current view from map to list or vice-versa */
    private void handleViewChange() {
        final FragmentTransaction tx = getFragmentManager().beginTransaction();
        switch (mCurrentView) {
            case VIEW_MAP:
                mCurrentView = VIEW_LIST;
                mViewMenuItem.setTitle(R.string.menu_main_map_view);
                mViewMenuItem.setIcon(R.drawable.ic_action_map);
                tx.show(mListFragment).hide(mMapFragment);
                break;
            case VIEW_LIST:
                mCurrentView = VIEW_MAP;
                mViewMenuItem.setTitle(R.string.menu_main_list_view);
                mViewMenuItem.setIcon(R.drawable.ic_action_list);
                tx.show(mMapFragment).hide(mListFragment);
                break;
        }
        tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        tx.commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        mCurrentTitle = title;
        final ActionBar bar = getActionBar();
        if (bar == null) {
            super.setTitle(title);
            return;
        }
        bar.setTitle(title);
    }

    @Override
    public void onLocationChanged(final Location location) {
        DebugLog.d(TAG, "onLocationChanged(): " + location);
        // When the location has changed, immediately try to decode the name of the location
        GeocodeUtils.reverseGeocode(this, location, new GeocodeUtils.LocationNameListener() {
            @Override
            public void onLocationNameDecoded(String locationName) {
                final NamedLocation userLocation = new NamedLocation(location.getLatitude(),
                    location.getLongitude(), locationName);
                mPreferenceHelper.saveRecentLocation(userLocation);
                if (mTrackLocation) {
                    // Only fetch new results if we are tracking location. We always save the
                    // location anyway though so that when we re-enable tracking, we have the latest
                    setTitle(userLocation.getName());
                    DebugLog.w(TAG, "calling update service from onLocationChanged() at " + System.currentTimeMillis());
                    refreshLocations(userLocation.toLatLng(), mMapFragment.getSearchRadius(), true);
                }
            }
        });
    }

    @Override
    public void onBrewerySelectedFromList(String breweryId) {
        // TODO show brewery details
    }

    @Override
    public void onBrewerySelectedFromMap(String breweryId) {
        // TODO show brewery details
    }

    @Override
    public void onLocationFilterTypeSelectedFromList(LocationType filterType) {
        mMapFragment.setCurrentLocationFilterType(filterType);
    }

    @Override
    public void onLocationFilterTypeSelectedFromMap(LocationType filterType) {
        mListFragment.setCurrentLocationFilterType(filterType);
    }

    @Override
    public void searchArea(NamedLocation location, double radius, boolean isMyLocation) {
        if (isMyLocation) {
            // If the map's 'my location' button was pressed,
            // then definitely enable location tracking
            mTrackLocation = true;
        } else if (mTrackLocation) {
            // Otherwise, we need to get the distance from the search location to the
            // current location to determine if the user is still using his or her current location
            final Location currentLocation = new Location("current");
            currentLocation.setLatitude(mPreferenceHelper.getRecentLocation().latitude());
            currentLocation.setLongitude(mPreferenceHelper.getRecentLocation().longitude());

            final Location searchLocation = new Location("search");
            searchLocation.setLatitude(location.latitude());
            searchLocation.setLongitude(location.longitude());

            mTrackLocation = currentLocation.distanceTo(searchLocation) <
                CraftyApp.MINIMUM_SEARCH_DISTANCE;
        }
        setTitle(location.getName());
        DebugLog.w(TAG, "calling update service from searchArea() at " + System.currentTimeMillis());
        refreshLocations(location.toLatLng(), radius, true);
    }

    private void refreshLocations(LatLng location, double radius, boolean force) {
        startService(new Intent(this, BreweryLocationUpdateService.class)
            .putExtra(BreweryLocationUpdateService.EXTRA_LOCATION, location)
            .putExtra(BreweryLocationUpdateService.EXTRA_RADIUS, radius)
            .putExtra(BreweryLocationUpdateService.EXTRA_FORCED, force));
    }

    private void showProgress(boolean show) {
        showProgressAnimated(true, show);
    }

    private void showProgressAnimated(boolean animate, final boolean show) {
        final View progress = findViewById(R.id.progress);
        if (animate) {
            if (show) {
                progress.setVisibility(View.VISIBLE);
            }
            progress.setAlpha(show ? 0 : 1);
            progress.animate().alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        progress.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } else {
            progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
