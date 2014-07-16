package com.akausejr.crafty;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;

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

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO enable Crashlytics

        sAppContext = getApplicationContext();
    }

    public static Context getContext() {
        return sAppContext;
    }
}
