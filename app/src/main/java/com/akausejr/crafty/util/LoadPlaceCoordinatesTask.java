package com.akausejr.crafty.util;

import android.os.AsyncTask;

import com.akausejr.crafty.BuildConfig;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Given a place id, this task loads the coordinates for that place and returns the results on
 * the UI thread. Uses Google Place details API.
 *
 * @author AJ Kause
 * Created on 7/18/14.
 */
public class LoadPlaceCoordinatesTask extends AsyncTask<String, Void, LatLng> {

    /** Interface for responding to to loading of a place's coordinates */
    public interface OnCoordinatesLoadedListener {

        /**
         * Called when the coordinates for a place are loaded
         * @param coordinates The place's coordinates
         */
        public void onCoordinatesLoaded(LatLng coordinates);
    }

    private OnCoordinatesLoadedListener mListener;

    @Override
    protected LatLng doInBackground(String... params) {
        final String placeId = params[0];
        final String detailsUrl = BuildConfig.GOOGLE_PLACES_BASE_URL + "/details/json" +
            "?key=" + BuildConfig.GOOGLE_PLACES_API_KEY +
            "&placeid=" + placeId;
        try {
            final HttpURLConnection connection =
                (HttpURLConnection) new URL(detailsUrl).openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (isCancelled()) {
                    return null;
                }
                final InputStream inStream = connection.getInputStream();
                final JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));
                final LatLng coordinates = readResponse(reader);
                reader.close();
                return coordinates;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the response stream into coordinates
     * @param reader Reads the http response
     * @return The place's coordinates
     * @throws IOException
     */
    private LatLng readResponse(JsonReader reader) throws IOException {
        LatLng coordinates = null;
        reader.beginObject();
        while (reader.hasNext()) {
            if (isCancelled()) {
                return null;
            }
            switch (reader.nextName()) {
                case "result":
                    coordinates = readGeometry(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return coordinates;
    }

    /**
     * Given a reader pointed at a place's details, returns the coordinates of that
     * place by reading the geometry field in the details object
     * @param reader The reader pointed at the details
     * @return The place's coordinates
     * @throws IOException
     */
    private LatLng readGeometry(JsonReader reader) throws IOException {
        LatLng coordinates = null;
        reader.beginObject();
        while (reader.hasNext()) {
            if (isCancelled()) {
                return null;
            }
            switch (reader.nextName()) {
                case "geometry":
                    final Geometry geometry = new Gson().fromJson(reader, Geometry.class);
                    coordinates = new LatLng(geometry.location.lat, geometry.location.lng);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return coordinates;
    }

    @Override
    protected void onPostExecute(LatLng coordinates) {
        if (mListener != null) {
            mListener.onCoordinatesLoaded(coordinates);
        }
    }

    /** @see android.os.AsyncTask#execute(Object[]) */
    public LoadPlaceCoordinatesTask execute(String id, OnCoordinatesLoadedListener listener) {
        mListener = listener;
        return (LoadPlaceCoordinatesTask) execute(id);
    }

    /** Releases a reference to any listeners and tries to stop the task */
    public void release() {
        mListener = null;
        cancel(true);
    }

    /** Represents the 'geometry' field of a place's details */
    private static class Geometry {
        public Location location;
        private static class Location {
            public double lat;
            public double lng;
        }
    }
}
