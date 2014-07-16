package com.akausejr.crafty.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.akausejr.crafty.util.DebugLog;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Service that responds to updates in the user's detected activity.
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class UserActivityService extends IntentService {

    /** Used for prefixing and debugging */
    private static final String TAG = UserActivityService.class.getSimpleName();

    /** Action string used for filtering intents to listen for activity change broadcasts */
    public static final String ACTION_ACTIVITY_UPDATE = TAG + ".ACTIVITY_UPDATE";

    /** Accept activity changes if the system is at least 75% sure of the new activity */
    private static final int MINIMUM_CONFIDENCE_LEVEL = 75;

    public UserActivityService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Only respond if we have a result
            final ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            final DetectedActivity activity = result.getMostProbableActivity();
            final int activityType = activity.getType();

            // We need the last activity to determine if there was a change
            final PreferenceHelper preferenceHelper = new PreferenceHelper(this);
            final int lastActivityType = preferenceHelper.getRecentUserActivity();

            if (activity.getConfidence() >= MINIMUM_CONFIDENCE_LEVEL &&
                activityType != lastActivityType) {
                // Only respond if the result is confident enough and it differs from
                // our previously recorded user activity
                DebugLog.d(TAG, "User activity changed to " + nameForActivity(activityType));
                preferenceHelper.saveRecentUserActivity(activityType);
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(ACTION_ACTIVITY_UPDATE));
            }
        }
    }

    private String nameForActivity(int activityType) {
        switch (activityType) {
            case DetectedActivity.STILL: return "still";
            case DetectedActivity.WALKING: return "walking";
            case DetectedActivity.RUNNING: return "running";
            case DetectedActivity.ON_FOOT: return "on foot";
            case DetectedActivity.ON_BICYCLE: return "biking";
            case DetectedActivity.IN_VEHICLE: return "driving";
            default: return "unknown";
        }
    }
}
