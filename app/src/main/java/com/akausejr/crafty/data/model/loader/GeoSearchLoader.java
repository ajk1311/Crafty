package com.akausejr.crafty.data.model.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.net.Uri;

import com.akausejr.crafty.BuildConfig;
import com.akausejr.crafty.data.model.BreweryLocation;

import java.util.List;

/**
 * Loads the brewery locations near the given coordinates within the given radius
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public class GeoSearchLoader extends ModelListLoader<BreweryLocation> {

    private final double mLat;

    private final double mLng;

    private final double mRadius;

    public GeoSearchLoader(Context context, double lat, double lng, double radius) {
        super(context, BreweryLocation.class, BuildConfig.BDB_BASE_URL);
        mLat = lat;
        mLng = lng;
        mRadius = radius;
    }

    @Override
    protected void onBuildUrl(Uri.Builder urlBuilder) {
        urlBuilder.appendPath("search/geo/point")
            .appendQueryParameter("key", BuildConfig.BDB_API_KEY)
            .appendQueryParameter("lat", String.valueOf(mLat))
            .appendQueryParameter("lng", String.valueOf(mLng))
            .appendQueryParameter("radius", String.valueOf(mRadius))
            .appendQueryParameter("unit", "km");
    }

    /** Callback interface for interacting with the LoaderManager loading BreweryLocations */
    public static interface Callbacks extends
        LoaderManager.LoaderCallbacks<Result<List<BreweryLocation>>> {
    }
}
