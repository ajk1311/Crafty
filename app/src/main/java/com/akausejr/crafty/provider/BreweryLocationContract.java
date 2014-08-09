package com.akausejr.crafty.provider;

import android.net.Uri;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class BreweryLocationContract {
    private BreweryLocationContract() {}

    public static final Uri CONTENT_URI =
        CraftyContentProvider.BASE_CONTENT_URI.buildUpon().appendPath("location").build();

    public static final String CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".location";
    public static final String CONTENT_ITEM_TYPE =
        CraftyContentProvider.BASE_CONTENT_ITEM_TYPE + ".location";

    public static String ID = CraftyDbHelper.LocationColumns.ID;
    public static String STATUS = CraftyDbHelper.LocationColumns.STATUS;
    public static String LOCATION_TYPE = CraftyDbHelper.LocationColumns.LOCATION_TYPE;
    public static String LOCATION_TYPE_DISPLAY = CraftyDbHelper.LocationColumns.LOCATION_TYPE_DISPLAY;
    public static String LATITUDE = CraftyDbHelper.LocationColumns.LATITUDE;
    public static String LONGITUDE = CraftyDbHelper.LocationColumns.LONGITUDE;
    public static String ADDRESS = CraftyDbHelper.LocationColumns.ADDRESS;
    public static String PHONE = CraftyDbHelper.LocationColumns.PHONE;
    public static String DISTANCE = CraftyDbHelper.LocationColumns.DISTANCE;
    public static String BREWERY_ID = CraftyDbHelper.LocationColumns.BREWERY_ID;
    public static String BREWERY_NAME = CraftyDbHelper.LocationColumns.BREWERY_NAME;
    public static String BREWERY_ICON_URL = CraftyDbHelper.LocationColumns.BREWERY_ICON_URL;

    public static final String DISTANCE_SORT = DISTANCE + " ASC";
    public static final String NAME_SORT = BREWERY_NAME + " COLLATE NOCASE ASC";
    public static final String TYPE_SORT = LOCATION_TYPE + " COLLATE NOCASE ASC";

    public static final String DEFAULT_SORT = DISTANCE_SORT;

    public static Uri buildLocationUri(String locationId) {
        return CONTENT_URI.buildUpon().appendPath(locationId).build();
    }

    public static String parseLocationId(Uri uri) {
        return uri.getLastPathSegment();
    }
}
