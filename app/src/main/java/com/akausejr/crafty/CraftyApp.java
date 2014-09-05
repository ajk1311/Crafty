package com.akausejr.crafty;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;

import com.akausejr.crafty.data.model.Beer;
import com.akausejr.crafty.data.model.BeerProvider;
import com.akausejr.crafty.data.model.Brewery;
import com.akausejr.crafty.data.model.BreweryLocation;
import com.akausejr.crafty.data.model.BreweryLocationProvider;
import com.akausejr.crafty.data.model.BreweryProvider;
import com.akausejr.crafty.data.model.ModelProvider;
import com.akausejr.crafty.data.model.Place;
import com.akausejr.crafty.data.model.PlaceCoordinatesProvider;
import com.akausejr.crafty.data.model.PlacesPrediction;
import com.akausejr.crafty.data.model.PlacesPredictionProvider;
import com.akausejr.crafty.data.model.PlacesProvider;
import com.google.android.gms.maps.model.LatLng;

/**
 * Maintains the state of the entire Crafty application
 */
public class CraftyApp extends Application {

    public static final double MAXIMUM_SEARCH_RADIUS = 100.0 * 1000.0; // meters
    public static final double DEFAULT_SEARCH_RADIUS = 10.0 * 1.60934 * 1000.0; // meters
    public static final double MINIMUM_SEARCH_DISTANCE = 100; // meters
    public static final float MINIMUM_ZOOM_CHANGE = 0.5f;
    public static final long MAX_UPDATE_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES; // ms
    public static final long MAX_CACHE_LIFETIME = AlarmManager.INTERVAL_DAY; // ms

    private static Context sAppContext;

    private static ModelProvider.Factory sProviderFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the application context
        sAppContext = getApplicationContext();

        // Initialize the models and providers
        sProviderFactory = new ModelProvider.Factory();

        final BreweryProvider breweryProvider = new BreweryProvider(sAppContext);
        sProviderFactory.registerProvider(Brewery.class, breweryProvider);

        final BreweryLocationProvider breweryLocationProvider =
            new BreweryLocationProvider(sAppContext, breweryProvider);
        sProviderFactory.registerProvider(BreweryLocation.class, breweryLocationProvider);

        final BeerProvider beerProvider = new BeerProvider(sAppContext);
        sProviderFactory.registerProvider(Beer.class, beerProvider);

        final PlacesProvider placesProvider = new PlacesProvider(sAppContext);
        sProviderFactory.registerProvider(Place.class, placesProvider);

        final PlacesPredictionProvider placesPredictionProvider =
            new PlacesPredictionProvider(sAppContext, placesProvider);
        sProviderFactory.registerProvider(PlacesPrediction.class, placesPredictionProvider);

        final PlaceCoordinatesProvider placeCoordinatesProvider =
            new PlaceCoordinatesProvider(sAppContext);
        sProviderFactory.registerProvider(LatLng.class, placeCoordinatesProvider);
    }

    public static Context getContext() {
        return sAppContext;
    }

    public static ModelProvider.Factory getModelProviderFactory() {
        return sProviderFactory;
    }
}
