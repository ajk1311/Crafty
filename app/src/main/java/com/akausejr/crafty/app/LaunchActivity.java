package com.akausejr.crafty.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.akausejr.crafty.R;
import com.akausejr.crafty.util.PreferenceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Responsible for employing any checks needed before starting the main content of the app
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class LaunchActivity extends Activity {

    /** Request code for resolving Play Services errors */
    private static final int RC_PLAY_SERVICES = 777;

    /** Request code for enabling location settings */
    private static final int RC_LOCATION_SETTINGS = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceHelper prefs = new PreferenceHelper(this);
        prefs.setAppWasOpenedOnce();
        if (checkForPlayServices()) {
            if (checkForLocation()) {
                // If we have Play Services and location enabled, go ahead and start main
                startMain();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PLAY_SERVICES:
                switch (resultCode) {
                    case RESULT_OK:
                        checkForLocation();
                        break;

                    case RESULT_CANCELED:
                        finish();
                        break;
                }
                break;

            case RC_LOCATION_SETTINGS:
                if (isLocationAvailable()) {
                    startMain();
                }
                break;
        }
    }

    private void startMain() {
        startActivity(new Intent(this, BreweryActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private boolean checkForPlayServices() {
        final int playServiceResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (playServiceResult == ConnectionResult.SUCCESS) {
            return true;
        }
        GooglePlayServicesUtil.showErrorDialogFragment(playServiceResult, this, RC_PLAY_SERVICES,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
        return false;
    }

    private boolean checkForLocation() {
        if (!isLocationAvailable()) {
            new LocationSettingsDialog().show(getFragmentManager(), null);
            return false;
        }
        return true;
    }

    private boolean isLocationAvailable() {
        final LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static class LocationSettingsDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_settings_dialog_msg)
                    .setNegativeButton(R.string.location_settings_dialog_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().finish();
                                }
                            })
                    .setPositiveButton(R.string.location_settings_dialog_go,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().startActivityForResult(
                                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                            RC_LOCATION_SETTINGS);
                                }
                            })
                    .setCancelable(false)
                    .create();
        }
    }
}
