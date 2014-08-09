package com.akausejr.crafty.provider;

import android.net.Uri;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/22/14.
 */
public class BreweryDetailsContract {
    private BreweryDetailsContract() {}

    public static final Uri BREWERY_CONTENT_URI =
        CraftyContentProvider.BASE_CONTENT_URI.buildUpon().appendPath("brewery").build();

    public static final Uri BEER_CONTENT_URI =
        BREWERY_CONTENT_URI.buildUpon().appendPath("beers").build();

    public static final Uri SOCIAL_SITE_URI =
        BREWERY_CONTENT_URI.buildUpon().appendPath("socialSites").build();

    public static final String CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".brewery";
    public static final String CONTENT_ITEM_TYPE =
        CraftyContentProvider.BASE_CONTENT_ITEM_TYPE + ".brewery";

    public static final String ID = CraftyDbHelper.BreweryColumns.ID;
    public static final String STATUS = CraftyDbHelper.BreweryColumns.STATUS;
    public static final String NAME = CraftyDbHelper.BreweryColumns.NAME;
    public static final String DESCRIPTION = CraftyDbHelper.BreweryColumns.DESCRIPTION;
    public static final String ICON_URL = CraftyDbHelper.BreweryColumns.ICON_URL;
    public static final String IMAGE_URL_MEDIUM = CraftyDbHelper.BreweryColumns.IMAGE_URL_MEDIUM;
    public static final String IMAGE_URL_LARGE = CraftyDbHelper.BreweryColumns.IMAGE_URL_large;
    public static final String ESTABLISHED = CraftyDbHelper.BreweryColumns.ESTABLISHED;
    public static final String IS_ORGANIC = CraftyDbHelper.BreweryColumns.IS_ORGANIC;
    public static final String WEBSITE = CraftyDbHelper.BreweryColumns.WEBSITE;
    public static final String BEER_ID = CraftyDbHelper.BeerColumns.ID;
    public static final String BEER_NAME = CraftyDbHelper.BeerColumns.NAME;
    public static final String BEER_STYLE_NAME = CraftyDbHelper.BeerColumns.STYLE_NAME;
    public static final String BEER_ABV = CraftyDbHelper.BeerColumns.ABV;
    public static final String BEER_LABEL_ICON_URL = CraftyDbHelper.BeerColumns.LABEL_URL_ICON;
    // TODO social sites

    public static final String DEFAULT_BEER_SORT = BEER_NAME + " COLLATE NOCASE ASC";

    public static Uri buildBreweryDetailsUri(String breweryId) {
        return BREWERY_CONTENT_URI.buildUpon().appendPath(breweryId).build();
    }

    public static String parseBreweryDetailsUri(Uri uri) {
        return uri.getLastPathSegment();
    }

    public static Uri buildBreweryBeersUir(String breweryId) {
        return BEER_CONTENT_URI.buildUpon().appendPath(breweryId).build();
    }

    public static String parseBreweryBeersUri(Uri uri) {
        return uri.getLastPathSegment();
    }
}
