package com.akausejr.crafty.legacy.provider;

import android.net.Uri;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/24/14.
 */
public class BeerDetailsContract {
    private BeerDetailsContract() {}

    public static final Uri CONTENT_URI =
        CraftyContentProvider.BASE_CONTENT_URI.buildUpon().appendPath("beer").build();

    public static final String CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".beer";
    public static final String CONTENT_ITEM_TYPE =
        CraftyContentProvider.BASE_CONTENT_ITEM_TYPE + ".beer";

    public static final String ID = CraftyDbHelper.BeerColumns.ID;
    public static final String STATUS = CraftyDbHelper.BeerColumns.STATUS;
    public static final String NAME = CraftyDbHelper.BeerColumns.NAME;
    public static final String DESCRIPTION = CraftyDbHelper.BeerColumns.DESCRIPTION;
    public static final String AVAILABILITY_NAME = CraftyDbHelper.BeerColumns.AVAILABILITY_NAME;
    public static final String AVAILABILITY_DESCRIPTION = CraftyDbHelper.BeerColumns.AVAILABILITY_DESCRIPTION;
    public static final String IS_ORGANIC = CraftyDbHelper.BeerColumns.IS_ORGANIC;
    public static final String IBU = CraftyDbHelper.BeerColumns.IBU;
    public static final String ORIGINAL_GRAVITY = CraftyDbHelper.BeerColumns.ORIGINAL_GRAVITY;
    public static final String YEAR = CraftyDbHelper.BeerColumns.YEAR;
    public static final String ABV = CraftyDbHelper.BeerColumns.ABV;
    public static final String STYLE_NAME = CraftyDbHelper.BeerColumns.STYLE_NAME;
    public static final String STYLE_DESCRIPTION = CraftyDbHelper.BeerColumns.STYLE_DESCRIPTION;
    public static final String STYLE_CATEGORY_NAME = CraftyDbHelper.BeerColumns.STYLE_CATEGORY_NAME;
    public static final String SERVING_TEMPERATURE = CraftyDbHelper.BeerColumns.SERVING_TEMPERATURE;
    public static final String SERVING_TEMPERATURE_DISPLAY = CraftyDbHelper.BeerColumns.SERVING_TEMPERATURE_DISPLAY;
    public static final String LABEL_URL_MEDIUM = CraftyDbHelper.BeerColumns.LABEL_URL_MEDIUM;
    public static final String LABEL_URL_LARGE = CraftyDbHelper.BeerColumns.LABEL_URL_LARGE;
    public static final String LABEL_URL_ICON = CraftyDbHelper.BeerColumns.LABEL_URL_ICON;
    public static final String FOOD_PAIRINGS = CraftyDbHelper.BeerColumns.FOOD_PAIRINGS;
    public static final String BREWERY_ID = CraftyDbHelper.BeerColumns.BREWERY_ID;

    public static Uri buildBeerDetailsUri(String beerId) {
        return CONTENT_URI.buildUpon().appendPath(beerId).build();
    }

    public static String parseBeerDetailsUri(Uri uri) {
        return uri.getLastPathSegment();
    }
}
