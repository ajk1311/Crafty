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

    public static final String TABLE_NAME = "locations";

    public static final Uri CONTENT_URI =
        CraftyContentProvider.BASE_CONTENT_URI.buildUpon().appendPath("location").build();

    public static final String CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".location";
    public static final String CONTENT_ITEM_TYPE =
        CraftyContentProvider.BASE_CONTENT_ITEM_TYPE + ".location";

    public static String ID = "id";
    public static String NAME = "name";
    public static String STATUS = "status";
    public static String STATUS_DISPLAY = "statusDisplay";
    public static String LOCATION_TYPE = "locationType";
    public static String LOCATION_TYPE_DISPLAY = "locationTypeDisplay";
    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";
    public static String FULL_ADDRESS = "address";
    public static String PHONE = "phone";
    public static String DISTANCE = "distance";
    public static String BREWERY_ID = "breweryId";
    public static String BREWERY_NAME = "breweryName";
    public static String BREWERY_STATUS = "breweryStatus";
    public static String BREWERY_STATUS_DISPLAY = "breweryStatusDisplay";
    public static String BREWERY_DESCRIPTION = "breweryDescription";
    public static String BREWERY_ESTABLISH_DATE = "breweryEstablished";
    public static String BREWERY_ICON_IMAGE_URL = "breweryIconImage";
    public static String BREWERY_MEDIUM_IMAGE_URL = "breweryMediumImage";
    public static String BREWERY_LARGE_IMAGE_URL = "breweryLargeImage";
    public static String BREWERY_IS_ORGANIC = "breweryIsOrganic";
    public static String BREWERY_WEBSITE = "breweryWebsite";

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
