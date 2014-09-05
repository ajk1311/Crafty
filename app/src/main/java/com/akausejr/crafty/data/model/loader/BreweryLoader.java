package com.akausejr.crafty.data.model.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.net.Uri;

import com.akausejr.crafty.BuildConfig;
import com.akausejr.crafty.data.model.Brewery;

/**
 * Loads details of the brewery with the given id
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public class BreweryLoader extends ModelLoader<Brewery> {

    /** The id of the brewery to load details for */
    private final String mBreweryId;

    public BreweryLoader(Context context, String breweryId) {
        super(context, Brewery.class, BuildConfig.BDB_BASE_URL);
        mBreweryId = breweryId;
    }

    @Override
    protected void onBuildUrl(Uri.Builder urlBuilder) {
        urlBuilder.appendPath("breweries").appendPath(mBreweryId)
            .appendQueryParameter("key", BuildConfig.BDB_API_KEY);
    }

    /** Callback interface for interacting with the LoaderManager loading brewery details */
    public static interface Callback extends LoaderManager.LoaderCallbacks<Result<Brewery>> {
    }
}
