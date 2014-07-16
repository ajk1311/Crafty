package com.akausejr.crafty.service;

import android.app.IntentService;
import android.content.Intent;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class LocationDetailsUpdateService extends IntentService {

    private static final String TAG = LocationDetailsUpdateService.class.getSimpleName();

    public LocationDetailsUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
