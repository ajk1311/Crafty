package com.akausejr.crafty.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.akausejr.crafty.util.PreferenceHelper;

/**
 * This receiver gets enabled when connectivity is lost so that we know when it is regained. When or
 * if that happens, we re-enable location update listening: passively if the app is in the background
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectivityReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            final PackageManager packageManager = context.getPackageManager();

            final ComponentName connectivityReceiver =
                new ComponentName(context, ConnectivityReceiver.class);
            packageManager.setComponentEnabledSetting(connectivityReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

            final PreferenceHelper prefs = new PreferenceHelper(context);
            if (prefs.isAppInBackground()) {
                final ComponentName passiveLocationReceiver =
                    new ComponentName(context, PassiveLocationReceiver.class);
                packageManager.setComponentEnabledSetting(passiveLocationReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            }

            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ConnectivityManager.CONNECTIVITY_ACTION)
                    .putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
        }
    }
}
