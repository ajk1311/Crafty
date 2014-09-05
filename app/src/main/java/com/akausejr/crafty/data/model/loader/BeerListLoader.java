package com.akausejr.crafty.data.model.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.net.Uri;

import com.akausejr.crafty.BuildConfig;
import com.akausejr.crafty.data.model.Beer;

import java.util.List;

/**
 * Loads the beer list for a give brewery id
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public class BeerListLoader extends ModelListLoader<Beer> {

    /** Id of the brewery to load beers for */
    private final String mBreweryId;

    public BeerListLoader(Context context, String breweryId) {
        super(context, Beer.class, BuildConfig.BDB_BASE_URL);
        mBreweryId = breweryId;
    }

    @Override
    protected void onBuildUrl(Uri.Builder urlBuilder) {
        urlBuilder.appendPath("breweries").appendPath(mBreweryId).appendPath("beers")
            .appendQueryParameter("key", BuildConfig.BDB_API_KEY);
    }

    /** Callback interface for interacting with the LoaderManager loading beers */
    public static interface Callbacks extends LoaderManager.LoaderCallbacks<Result<List<Beer>>> {
    }
}
