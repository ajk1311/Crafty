package com.akausejr.crafty.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.akausejr.crafty.model.NamedLocation;
import com.google.android.gms.location.DetectedActivity;

/**
 * Manages the app's {@link android.content.SharedPreferences} in a client-friendly way
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class PreferenceHelper {

    private static final String PREFERENCES_NAME = "Crafty";
    private static final int PREFERENCES_MODE = Context.MODE_PRIVATE;

    private enum Keys {
        OPENED_ONCE,
        IN_BACKGROUND,
        TRACK_USER_LOCATION,
        RECENT_LOCATION_LAT,
        RECENT_LOCATION_LNG,
        RECENT_LOCATION_NAME,
        RECENT_USER_ACTIVITY,
        LAST_UPDATE_TIME
    }

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public PreferenceHelper(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, PREFERENCES_MODE);
        mEditor = mPreferences.edit();
        mEditor.apply();
    }

    public boolean wasAppOpenedOnce() {
        return mPreferences.getBoolean(Keys.OPENED_ONCE.name(), false);
    }

    public void setAppWasOpenedOnce() {
        mEditor.putBoolean(Keys.OPENED_ONCE.name(), true);
    }

    public boolean isAppInBackground() {
        return mPreferences.getBoolean(Keys.IN_BACKGROUND.name(), false);
    }

    public void setAppIsInBackground(boolean inBackground) {
        mEditor.putBoolean(Keys.IN_BACKGROUND.name(), inBackground).apply();
    }

    public NamedLocation getRecentLocation() {
        final String name = mPreferences.getString(Keys.RECENT_LOCATION_NAME.name(), null);
        if (name == null) {
            return null;
        }
        final double lat = mPreferences.getFloat(Keys.RECENT_LOCATION_LAT.name(), Float.MIN_VALUE);
        final double lng = mPreferences.getFloat(Keys.RECENT_LOCATION_LNG.name(), Float.MIN_VALUE);
        return new NamedLocation(lat, lng, name);
    }

    public void saveRecentLocation(NamedLocation location) {
        mEditor.putFloat(Keys.RECENT_LOCATION_LAT.name(), (float) location.latitude())
            .putFloat(Keys.RECENT_LOCATION_LNG.name(), (float) location.longitude())
            .putString(Keys.RECENT_LOCATION_NAME.name(), location.getName())
            .apply();
    }

    public void saveRecentUserActivity(int activity) {
        mEditor.putInt(Keys.RECENT_USER_ACTIVITY.name(), activity).apply();
    }

    public int getRecentUserActivity() {
        return mPreferences.getInt(Keys.RECENT_USER_ACTIVITY.name(), DetectedActivity.UNKNOWN);
    }

    public long getLastUpdateTime() {
        return mPreferences.getLong(Keys.LAST_UPDATE_TIME.name(), 0L);
    }

    public void setLastUpdateTimeToNow() {
        mEditor.putLong(Keys.LAST_UPDATE_TIME.name(), System.currentTimeMillis()).apply();
    }
}
