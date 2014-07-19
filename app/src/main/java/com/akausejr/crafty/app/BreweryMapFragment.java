package com.akausejr.crafty.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.R;
import com.akausejr.crafty.graphics.CircleColorLetterDrawable;
import com.akausejr.crafty.model.BreweryLocation;
import com.akausejr.crafty.model.LocationType;
import com.akausejr.crafty.model.NamedLocation;
import com.akausejr.crafty.provider.BreweryLocationContract;
import com.akausejr.crafty.util.CircleTransform;
import com.akausejr.crafty.util.CompatUtil;
import com.akausejr.crafty.util.GeocodeUtils;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Displays brewery locations around the user's current location on a
 * {@link com.google.android.gms.maps.GoogleMap}
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class BreweryMapFragment extends MapFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Controller interface for handling map interaction */
    public static interface BreweryMapController {

        /**
         * Tells the controller to use the provided area as the search parameters
         * @param location The location to search
         * @param radius The search radius
         * @param isMyLocation {@code true} if {@code location} is the map's 'my location'
         */
        public void searchArea(NamedLocation location, double radius, boolean isMyLocation);

        /**
         * Tells the controller to react to a brewery selection
         * @param breweryId The id of the brewery location
         */
        public void onBrewerySelectedFromMap(String breweryId);

        /**
         * Tells the controller that the user wants to filter content. All other views should
         * be updated accordingly
         * @param filterType The type of location to filter for
         */
        public void onLocationFilterTypeSelectedFromMap(LocationType filterType);
    }

    /** Used for prefixing and debugging */
    private static final String TAG = BreweryMapFragment.class.getSimpleName();

    // Saved state keys
    private static final String KEY_START_LOCATION = TAG + ".START_LOCATION";
    private static final String KEY_CURRENT_ZOOM = TAG + ".CURRENT_ZOOM";

    /** The zoom level for zooming to the user's current location */
    public static final float DEFAULT_ZOOM = 10f;

    /** The columns to fetch */
    private static final String[] QUERY_PROJECTION = new String[] {
        BaseColumns._ID,
        BreweryLocationContract.ID,
        BreweryLocationContract.BREWERY_NAME,
        BreweryLocationContract.LATITUDE,
        BreweryLocationContract.LONGITUDE,
        BreweryLocationContract.DISTANCE,
        BreweryLocationContract.FULL_ADDRESS,
        BreweryLocationContract.LOCATION_TYPE,
        BreweryLocationContract.BREWERY_ICON_IMAGE_URL
    };

    /** Only display locations that are verified by brewerydb.com */
    private static final String QUERY_SELECTION = BreweryLocationContract.STATUS + "=\"verified\"";

    // Cursor indices
    private static final int ID_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int LATITUDE_INDEX = 3;
    private static final int LONGITUDE_INDEX = 4;
    private static final int DISTANCE_INDEX = 5;
    private static final int ADDRESS_INDEX = 6;
    private static final int LOCATION_TYPE_INDEX = 7;
    private static final int ICON_URL_INDEX = 8;

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
    private boolean mMyLocationPressed = false;

    /** Displays the options for filtering content by location type */
    private Spinner mTypeSpinner;

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

            ImageView mDummy;

            @Override
            public boolean onMarkerClick(final Marker marker) {
                mCameraAnimating = true;

                final BreweryLocationRecord record = mMarkerMap.get(marker);

                // First, load the image. This will ensure that the icon is in Picasso's cache
                // when we go to load it in the info window adapter
                if (mDummy == null) {
                    // Use a dummy ImageView so we can get a Callback on the main thread
                    mDummy = new ImageView(getActivity());
                }
                Picasso.with(getActivity()).load(record.iconUrl).into(mDummy, new Callback() {
                    @Override
                    public void onSuccess() {
                        // If the info window gets hidden before the image is loaded, we don't
                        // want to re-show the window and disrupt the user
                        if (marker.isInfoWindowShown()) {
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onError() {
                        if (marker.isInfoWindowShown()) {
                            marker.showInfoWindow();
                        }
                    }
                });
                return false;
            }
        };

    /** Notifies the controller that a brewery was selected */
    private final GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener =
        new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final BreweryLocationRecord record = mMarkerMap.get(marker);
                if (mController != null) {
                    mController.onBrewerySelectedFromMap(record.id);
                }
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
                case R.id.btn_my_location:
                    zoomToMyLocation();
                    break;
            }
        }
    };

    /** Responds to user clicks on the type filter, thus filtering search results by type */
    private AdapterView.OnItemSelectedListener mTypeSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final LocationType filterType = (LocationType) parent.getItemAtPosition(position);
                setCurrentLocationFilterType(filterType.type, true);
                if (mController != null) {
                    mController.onLocationFilterTypeSelectedFromMap(filterType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
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
        ((ViewGroup) root.findViewById(R.id.map_container)).addView(superView, 0);

        root.findViewById(R.id.btn_zoom_in).setOnClickListener(mButtonClickListener);
        root.findViewById(R.id.btn_zoom_out).setOnClickListener(mButtonClickListener);
        root.findViewById(R.id.btn_my_location).setOnClickListener(mButtonClickListener);

        mTypeSpinner = (Spinner) root.findViewById(R.id.type_spinner);
        mTypeSpinner.setAdapter(new BreweryLocationTypeSpinnerAdapter(getActivity()));
        mTypeSpinner.setOnItemSelectedListener(mTypeSelectedListener);

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
            final float currentZoom = savedInstanceState == null ?
                DEFAULT_ZOOM : savedInstanceState.getFloat(KEY_CURRENT_ZOOM);
            if (startLocation == null) {
                // TODO handle first app open
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, currentZoom));
                mPreviousCameraPosition = map.getCameraPosition();
            }

            map.setOnCameraChangeListener(mCameraChangeListener);
            map.setOnMarkerClickListener(mMarkerClickListener);
            map.setOnInfoWindowClickListener(mInfoWindowClickListener);
            map.setInfoWindowAdapter(new BreweryLocationInfoWindowAdapter(getActivity()));
        }

        // Loads locations from the content provider. The loader manager will be notified whenever
        // the data changes so our UI stays up to date
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KEY_CURRENT_ZOOM, getMap().getCameraPosition().zoom);
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

        final boolean shouldLoad = zoomedEnough || movedEnough;

        if (mMyLocationPressed) {
            final NamedLocation recentLocation = mPreferenceHelper.getRecentLocation();
            mController.searchArea(recentLocation, getSearchRadius(), true);
        } else if (shouldLoad) {
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
        // Finally, restore state
        mPreviousCameraPosition = position;
        mMyLocationPressed = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = QUERY_SELECTION;
        final LocationType filterType = (LocationType) mTypeSpinner.getSelectedItem();
        if (!TextUtils.isEmpty(filterType.type)) {
            selection += " AND " +
                BreweryLocationContract.LOCATION_TYPE + "=\"" + filterType.type + '"';
        }
        return new CursorLoader(getActivity(),
            BreweryLocationContract.CONTENT_URI,
            QUERY_PROJECTION,
            selection,
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
                cursor.getString(NAME_INDEX),
                cursor.getString(ADDRESS_INDEX),
                cursor.getString(ICON_URL_INDEX),
                cursor.getString(LOCATION_TYPE_INDEX),
                cursor.getDouble(DISTANCE_INDEX));
            final MarkerOptions ops = new MarkerOptions()
                .position(new LatLng(cursor.getDouble(LATITUDE_INDEX),
                    cursor.getDouble(LONGITUDE_INDEX)));

            // Assign our custom marker icon if one applies
            final int markerResId = BreweryLocation.getMarkerResIdForType(record.type);
            if (markerResId > 0) {
                ops.icon(BitmapDescriptorFactory.fromResource(markerResId));
            }

            // Plot the marker
            final Marker marker = map.addMarker(ops);
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

    /**
     * Filters the content based on the given type
     * @param filterType The type to filter by
     */
    public void setCurrentLocationFilterType(LocationType filterType) {
        setCurrentLocationFilterType(filterType.type, false);
    }

    /**
     * Filters the content based on the given type
     * @param filterType The type to filter by
     * @param fromSelf {@code true} if this instance called the method
     */
    private void setCurrentLocationFilterType(String filterType, boolean fromSelf) {
        if (!fromSelf) {
            mTypeSpinner.setSelection(getIndexForType(filterType));
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * @param filterType The location type to filter by
     * @return The index in the Spinner of the given type
     */
    private int getIndexForType(String filterType) {
        for (int i = 0, sz = mTypeSpinner.getAdapter().getCount(); i < sz; i++) {
            final LocationType type = (LocationType) mTypeSpinner.getAdapter().getItem(i);
            if (type.type.equals(filterType)) {
                return i;
            }
        }
        return 0;
    }

    /** Zooms to the current location, re-enables location tracking, and updates the content */
    private void zoomToMyLocation() {
        final GoogleMap map = getMap();
        if (map == null) {
            // If the map isn't ready or we are already tracking the current location,
            // go ahead and return
            return;
        }
        mMyLocationPressed = true;
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

    /** Stores information about a brewery to be retrieved from map markers */
    private class BreweryLocationRecord {
        public final String id;
        public final String name;
        public final String address;
        public final String iconUrl;
        public final String type;
        public final double distance;
        public BreweryLocationRecord(String id, String name, String address,
                                     String iconUrl, String type, double distance) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.iconUrl = iconUrl;
            this.type = type;
            this.distance = distance;
        }
    }

    /** Custom info window for showing brewery details when a map marker is clicked */
    private class BreweryLocationInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private static final double FEET_IN_METER = 3.28084;

        private static final double FEET_IN_MILE = 5280;

        // Format strings for different distances
        private final String mFeetFormatString;
        private final String mMilesFormatString;

        /** Each brewery icon is transformed into a circle */
        private final Transformation mCircleTransform;

        public BreweryLocationInfoWindowAdapter(Context context) {
            mFeetFormatString = context.getString(R.string.feet_away);
            mMilesFormatString = context.getString(R.string.miles_away);
            mCircleTransform = new CircleTransform(getResources()
                .getDimensionPixelSize(R.dimen.list_item_bg_padding));
        }

        @Override
        public View getInfoWindow(Marker marker) {
            final View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.brewery_info_window, null);
            final BreweryLocationRecord record = mMarkerMap.get(marker);

            // Icon
            final ImageView icon = (ImageView) view.findViewById(R.id.brewery_icon);
            CompatUtil.setViewBackground(icon, new CircleColorLetterDrawable(getResources(),
                BreweryLocation.getColorResIdForType(record.type), record.name.charAt(0)));
            Picasso.with(getActivity()).load(record.iconUrl).transform(mCircleTransform).into(icon);

            // Name
            ((TextView) view.findViewById(R.id.brewery_name)).setText(record.name);

            // Address
            ((TextView) view.findViewById(R.id.brewery_address)).setText(record.address);

            // Distance
            ((TextView) view.findViewById(R.id.brewery_distance))
                .setText(distanceString(record.distance));

            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
           return null;
        }

        /**
         * @param meters The distance in meters
         * @return The string to display given a brewery's distance from the search location
         */
        private String distanceString(double meters) {
            final double feet = meters * FEET_IN_METER;
            if (feet > 1000) {
                final double miles = feet / FEET_IN_MILE;
                return String.format(mMilesFormatString, miles);
            }
            return String.format(mFeetFormatString, feet);
        }
    }
}
