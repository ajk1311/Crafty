package com.akausejr.crafty.service;

import android.app.IntentService;
import android.content.Intent;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/24/14.
 */
public class BeerDetailsUpdateService extends IntentService {

    private static final String TAG = BeerDetailsUpdateService.class.getSimpleName();

    public BeerDetailsUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
