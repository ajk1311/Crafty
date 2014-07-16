package com.akausejr.crafty.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.akausejr.crafty.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Common utilities for geocoding a location. Uses the built-in Android geocoding location for now.
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class GeocodeUtils {

    public interface LocationNameListener {
        public void onLocationNameDecoded(String locationName);
    }

    public static void reverseGeocode(Context context,
                                      Location location,
                                      LocationNameListener listener) {
        reverseGeocode(context, new LatLng(location.getLatitude(), location.getLongitude()),
            listener);
    }

    public static void reverseGeocode(final Context context,
                                      final LatLng location,
                                      final LocationNameListener listener) {
        new AsyncTask<LatLng, Void, String>() {
            @Override
            protected String doInBackground(LatLng... params) {
                try {
                    final LatLng toDecode = params[0];
                    final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    final List<Address> addresses = geocoder.getFromLocation(toDecode.latitude,
                        toDecode.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        final Address address = addresses.get(0);

                        final StringBuilder name = new StringBuilder();
                        if (!TextUtils.isEmpty(address.getLocality())) {
                            name.append(address.getLocality());
                        }
                        if (!TextUtils.isEmpty(address.getAdminArea())) {
                            if (name.length() > 0) {
                                name.append(", ");
                            }
                            name.append(address.getAdminArea());
                        }
                        if (name.length() > 0) {
                            return name.toString();
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return context.getString(R.string.location_unknown);
            }

            @Override
            protected void onPostExecute(String s) {
                if (listener != null) {
                    listener.onLocationNameDecoded(s);
                }
            }
        }.execute(location);
    }
}
