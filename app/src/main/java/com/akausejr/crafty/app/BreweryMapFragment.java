package com.akausejr.crafty.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.R;
import com.akausejr.crafty.model.NamedLocation;
import com.akausejr.crafty.provider.BreweryLocationContract;
import com.akausejr.crafty.util.CompatUtil;
import com.akausejr.crafty.util.GeocodeUtils;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Displays brewery locations around the user's current location on a
 * {@link com.google.android.gms.maps.GoogleMap}
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class BreweryMapFragment extends SupportMapFragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * {@link android.app.Activity}s and {@link android.app.Fragment}s that implement this interface
     * are responsible for providing brewery data to this Fragment based on these methods
     */
    public static interface BreweryMapController {

        /** Tells the controller to use the provided area as the search parameters */
        public void searchArea(NamedLocation location, double radius, boolean enableTracking);
    }

    /** Used for prefixing and debugging */
    private static final String TAG = BreweryMapFragment.class.getSimpleName();

    // Saved state keys
    private static final String KEY_START_LOCATION = TAG + ".START_LOCATION";

    /** The zoom level for zooming to the user's current location */
    private static final float DEFAULT_ZOOM = 10f;

    /** The columns to fetch */
    private static final String[] QUERY_PROJECTION = new String[] {
        BaseColumns._ID,
        BreweryLocationContract.ID,
        BreweryLocationContract.BREWERY_NAME,
        BreweryLocationContract.LATITUDE,
        BreweryLocationContract.LONGITUDE,
        BreweryLocationContract.FULL_ADDRESS
    };

    /** Only display locations that are verified by brewerydb.com */
    private static final String QUERY_SELECTION = BreweryLocationContract.STATUS + "=\"verified\"";

    // Cursor indices
    private static final int ID_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int LATITUDE_INDEX = 3;
    private static final int LONGITUDE_INDEX = 4;
    private static final int ADDRESS_INDEX = 5;

    /** Keeps references to the markers on the map so we have the data to use when one is clicked */
    private Map<Marker, BreweryLocationRecord> mMarkerMap = new LinkedHashMap<>();

    /** App shared preferences */
    private PreferenceHelper mPreferenceHelper;

    /** Handle on this Fragment's controller */
    private BreweryMapController mController;

    /** When this flag is set, then the camera change listener should not take any action */
    private boolean mCameraAnimating = false;

    /** Used to determine if the user has moved or zoomed enough to warrant stop tracking location */
    private CameraPosition mPreviousCameraPosition;

    /** Set to true to override any condition checking when the camera moves  */
    private boolean mOverrideBoundChecks = false;

    /**
     * Notified when the map camera changes to determine if we should fetch results based on
     * location tracking or the current map camera position
     */
    private final GoogleMap.OnCameraChangeListener mCameraChangeListener =
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (mCameraAnimating) {
                    // If this flag is set, we don't want to trigger an update since we get
                    // caught in a loop of updating and zooming over and over
                    mCameraAnimating = false;
                } else {
                    handleCameraChange(position);
                }
            }
        };

    /** Set the animation flag so that results aren't fetched every time a marker is clicked */
    private final GoogleMap.OnMarkerClickListener mMarkerClickListener =
        new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mCameraAnimating = true;
                return false;
            }
        };

    /** Reacts to button clicks */
    private final View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_zoom_in:
                    zoomIn();
                    break;
                case R.id.btn_zoom_out:
                    zoomOut();
                    break;
                case R.id.btn_key:
                    showKey();
                    break;
                case R.id.btn_my_location:
                    zoomToMyLocation();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mPreferenceHelper = new PreferenceHelper(activity);

        if (activity instanceof BreweryMapController) {
            mController = (BreweryMapController) activity;
        } else if (getTargetFragment() instanceof  BreweryMapController) {
            mController = (BreweryMapController) getTargetFragment();
        } else {
            // Enforce this
            mController = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Prevent leaked Contexts
        mController = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inject the view created by the system into our custom view so that we can display
        // cool looking custom controls over the Google map

        final View superView = super.onCreateView(inflater, container, savedInstanceState);

        final ViewGroup root = (ViewGroup) inflater
                .inflate(R.layout.fragment_brewery_map, container, false);
        // Add the map view below all of our custom views
        root.addView(superView, 0);

        root.findViewById(R.id.btn_zoom_in).setOnClickListener(mButtonClickListener);
        root.findViewById(R.id.btn_zoom_out).setOnClickListener(mButtonClickListener);
        root.findViewById(R.id.btn_key).setOnClickListener(mButtonClickListener);
        root.findViewById(R.id.btn_my_location).setOnClickListener(mButtonClickListener);
        return root;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the map
        final GoogleMap map = getMap();
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setAllGesturesEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);

            final LatLng startLocation = savedInstanceState == null ?
                mPreferenceHelper.getRecentLocation().toLatLng() :
                (LatLng) savedInstanceState.get(KEY_START_LOCATION);
            if (startLocation == null) {
                // TODO handle first app open
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, DEFAULT_ZOOM));
                mPreviousCameraPosition = map.getCameraPosition();
            }

            map.setOnCameraChangeListener(mCameraChangeListener);
            map.setOnMarkerClickListener(mMarkerClickListener);
        }

        // Loads locations from the content provider. The loader manager will be notified whenever
        // the data changes so our UI stays up to date
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_START_LOCATION, getMap().getCameraPosition().target);
    }

    /**
     * Decides whether or not to trigger an update based on a change in camera position. If the
     * user zoomed too little or scrolled too little, no action is take. This prevents unnecessary
     * updates that would otherwise just be distracting.
     * @param position The new camera position of the map
     */
    private void handleCameraChange(final CameraPosition position) {
        if (mController ==  null || mPreviousCameraPosition == null) {
            // If nothing can respond to search requests, just return
            return;
        }

        final Location oldLocation = new Location("old");
        oldLocation.setLatitude(mPreviousCameraPosition.target.latitude);
        oldLocation.setLongitude(mPreviousCameraPosition.target.longitude);

        final Location newLocation = new Location("new");
        newLocation.setLatitude(position.target.latitude);
        newLocation.setLongitude(position.target.longitude);

        final boolean movedEnough = newLocation.distanceTo(oldLocation) >=
            CraftyApp.MINIMUM_SEARCH_DISTANCE;
        final boolean zoomedEnough =
            Math.abs(position.zoom - mPreviousCameraPosition.zoom) >
                CraftyApp.MINIMUM_ZOOM_CHANGE;

        if (mOverrideBoundChecks || (zoomedEnough && !movedEnough)) {
            // If the user just zoomed, we want to fetch results based on the current location,
            // but with the updated search radius. We also want to (re)enable location tracking
            final NamedLocation currentLocation = mPreferenceHelper.getRecentLocation();
            mController.searchArea(currentLocation, getSearchRadius(), true);
        } else if (movedEnough) {
            // Otherwise, we want to stop tracking location and get search results based on
            // the current map position
            GeocodeUtils.reverseGeocode(getActivity(), position.target,
                new GeocodeUtils.LocationNameListener() {
                    @Override
                    public void onLocationNameDecoded(String locationName) {
                        if (mController == null) {
                            // Check again in case the Fragment is detached before the
                            // reverse geocode is complete
                            return;
                        }
                        final NamedLocation location =
                            new NamedLocation(newLocation, locationName);
                        // Only disable tracking if the user moved the map camera
                        // enough to warrant a change in location.
                        mController.searchArea(location, getSearchRadius(), false);
                    }
                });
        }
        // Finally, save the new position
        mPreviousCameraPosition = position;
        mOverrideBoundChecks = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
            BreweryLocationContract.CONTENT_URI,
            QUERY_PROJECTION,
            QUERY_SELECTION,
            null,
            null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        final GoogleMap map = getMap();
        if (map == null) {
            // initLoader() is called after onViewCreated(), so this shouldn't happen
            return;
        }
        clearLocationMarkers();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // For each row in the cursor, extract the data, and save a record
            // that corresponds to a map marker
            final BreweryLocationRecord record = new BreweryLocationRecord(
                cursor.getString(ID_INDEX),
                cursor.getDouble(LATITUDE_INDEX),
                cursor.getDouble(LONGITUDE_INDEX));
            final Marker marker = map.addMarker(new MarkerOptions()
                .title(cursor.getString(NAME_INDEX))
                .snippet(cursor.getString(ADDRESS_INDEX))
                .position(new LatLng(record.lat, record.lng)));
                // TODO icon
            mMarkerMap.put(marker, record);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       clearLocationMarkers();
    }

    /** Removes the markers we added from the map */
    private void clearLocationMarkers() {
        for (Map.Entry<Marker, BreweryLocationRecord> entry : mMarkerMap.entrySet()) {
            entry.getKey().remove();
        }
        mMarkerMap.clear();
    }

    /**
     * Calculates the appropriate bounds for the map that would encompass every marker.
     * This method should only be called if mNeedsFitting is true
     */
    private void zoomToFitMarkers() {
        mCameraAnimating = true;

        if (mMarkerMap.isEmpty()) {
            final NamedLocation recentLocation = mPreferenceHelper.getRecentLocation();
            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(recentLocation.latitude(), recentLocation.longitude()), DEFAULT_ZOOM));
            return;
        }

        final LatLng centerLL = getMap().getCameraPosition().target;
        final Location center = new Location("center");
        center.setLatitude(centerLL.latitude);
        center.setLongitude(centerLL.longitude);

        // First, we need the farthest marker
        Location current = new Location("tmp");
        LatLng farthest = centerLL;
        double maxDistance = 0.0;

        for (Map.Entry<Marker, BreweryLocationRecord> entry : mMarkerMap.entrySet()) {
            final BreweryLocationRecord brewery = entry.getValue();
            current.setLatitude(brewery.lat);
            current.setLongitude(brewery.lng);
            final double distance = center.distanceTo(current);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthest = new LatLng(current.getLatitude(), current.getLongitude());
            }
        }

        // Next, we invert the line from the center to the farthest marker to make a rect
        final double dLat = Math.abs(center.getLatitude() - farthest.latitude);
        final double dLng = Math.abs(center.getLongitude() - farthest.longitude);
        final double iLat = (farthest.latitude < center.getLatitude()) ?
            center.getLatitude() + dLat : center.getLatitude() - dLat;
        final double iLng = (farthest.longitude < center.getLongitude()) ?
            center.getLongitude() + dLng : center.getLongitude() - dLng;

        // Finally, zoom the map accordingly
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
            .include(farthest)
            .include(new LatLng(iLat, iLng))
            .build(),
            100));
    }

    /** Zooms the map by 1 level */
    private void zoomIn() {
        final GoogleMap map = getMap();
        if (map == null) {
            return;
        }
        map.animateCamera(CameraUpdateFactory.zoomIn());
    }

    /** Zooms the map out by 1 level */
    private void zoomOut() {
        final GoogleMap map = getMap();
        if (map == null) {
            return;
        }
        map.animateCamera(CameraUpdateFactory.zoomOut());
    }

    /** Zooms to the current location, re-enables location tracking, and updates the content */
    private void zoomToMyLocation() {
        final GoogleMap map = getMap();
        if (map == null) {
            // If the map isn't ready or we are already tracking the current location,
            // go ahead and return
            return;
        }
        mOverrideBoundChecks = true;
        final NamedLocation recentLocation = mPreferenceHelper.getRecentLocation();
        map.animateCamera(CameraUpdateFactory
            .newLatLngZoom(recentLocation.toLatLng(), DEFAULT_ZOOM));
    }

    /**
     * @return The number of meters from the center of the map to its edge.
     * We'll use this radius to limit search results
     */
    public double getSearchRadius() {
        final GoogleMap map = getMap();
        if (map == null) {
            return 0.0;
        }

        final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        final Location center = new Location("center");
        center.setLatitude(bounds.getCenter().latitude);
        center.setLongitude(bounds.getCenter().longitude);

        final Display display = ((WindowManager) getActivity()
            .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                // "Portrait," so we use the width of the screen
                final Location middleTop = new Location("middleTop");
                middleTop.setLatitude(bounds.northeast.latitude);
                middleTop.setLongitude(center.getLongitude());
                return Math.min(center.distanceTo(middleTop), CraftyApp.MAXIMUM_SEARCH_RADIUS);

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                // "Landscape," so we use the height of the screen
                final Location middleLeft = new Location("middleLeft");
                middleLeft.setLatitude(center.getLatitude());
                middleLeft.setLongitude(bounds.southwest.longitude);
                return Math.min(center.distanceTo(middleLeft), CraftyApp.MAXIMUM_SEARCH_RADIUS);

            default:
                // Shouldn't happen
                return 0.0;
        }
    }

    private void showKey() {
        if (CompatUtil.isLPreview()) {
            // TODO circular reveal key
        } else {
            // TODO scale up key
        }
    }

    /** Stores information about a brewery to be retrieved from map markers */
    private class BreweryLocationRecord {
        public final String id;
        public final double lat;
        public final double lng;
        public BreweryLocationRecord(String id, double lat, double lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }
    }
}
