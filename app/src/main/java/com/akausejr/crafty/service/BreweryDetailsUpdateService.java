package com.akausejr.crafty.service;

import android.app.IntentService;
import android.content.Intent;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class BreweryDetailsUpdateService extends IntentService {

    private static final String TAG = BreweryDetailsUpdateService.class.getSimpleName();

    public BreweryDetailsUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
