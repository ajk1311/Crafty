package com.akausejr.crafty.legacy.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.akausejr.crafty.BuildConfig;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides search suggestions based on the Google Places autocomplete API for SearchManager.
 *
 * @author AJ Kause
 * Created on 7/17/14.
 */
public class SearchSuggestionProvider extends ContentProvider {

    public static final String SUGGEST_COLUMN_HEADER = "header";
    public static final String SUGGEST_COLUMN_DIVIDER = "divider";

    public static final String ACTION_SEARCH_SUGGESTION =
        "com.akausejr.crafty.action.SEARCH_SUGGESTION";

    public static final String AUTHORITY = "com.akausejr.crafty.legacy.provider.SearchSuggestionProvider";
    private static final String PLACES_PATH = "google/places";
    private static final String BREWERIES_PATH = "brewerydb/breweries";
    private static final String BEERS_PATH = "brewerydb/beers";

    private static final String SUGGEST_DATA_URI_PLACES =
        "content://" + AUTHORITY + '/' + PLACES_PATH + '/';
    private static final String SUGGEST_DATA_URI_BREWERIES =
        "content://" + AUTHORITY + '/' + BREWERIES_PATH + '/';

    private static final int MATCH_PLACES = 0;
    private static final int MATCH_BREWERIES = 1;

    public static final String PLACES_CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".placeSuggestion";
    public static final String BREWERIES_CONTENT_TYPE =
        CraftyContentProvider.BASE_CONTENT_TYPE + ".brewerySuggestion";

    /** The columns needed by SearchManager for our suggestion results */
    private static final String[] COLUMN_NAMES = new String[] {
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        SUGGEST_COLUMN_HEADER,
        SUGGEST_COLUMN_DIVIDER
    };

    private final static UriMatcher SUGGESTION_MATCHER;
    static {
        SUGGESTION_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        SUGGESTION_MATCHER.addURI(AUTHORITY, PLACES_PATH + "/*", MATCH_PLACES);
        SUGGESTION_MATCHER.addURI(AUTHORITY, BREWERIES_PATH + "/*", MATCH_BREWERIES);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (SUGGESTION_MATCHER.match(uri)) {
            case MATCH_PLACES: return PLACES_CONTENT_TYPE;
            case MATCH_BREWERIES: return BREWERIES_CONTENT_TYPE;
            default: throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortBy) {
        final String query = uri.getLastPathSegment();

        // Allocate the cursor to return
        final MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);

        try {
            final List<Object[]> placesRows = queryPlacesApi(query);
            final List<Object[]> breweriesRows = queryBreweryDbApi(query);
            if (placesRows != null) {
                for (Object[] row : placesRows) {
                    cursor.addRow(row);
                }
            }
            if (breweriesRows != null) {
                for (Object[] row : breweriesRows) {
                    cursor.addRow(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore since we return an empty cursor
        }
        return cursor;
    }

    private List<Object[]> queryPlacesApi(String query) throws IOException {
        final String searchUrl = BuildConfig.GOOGLE_PLACES_BASE_URL + "/autocomplete/json" +
            "?key=" + BuildConfig.GOOGLE_PLACES_API_KEY +
            "&input=" + URLEncoder.encode(query, "UTF-8") +
            "&types=(cities)" +
            "&components=country:us";
        final HttpURLConnection connection =
            (HttpURLConnection) new URL(searchUrl).openConnection();
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // If the connection succeeded, start parsing the response
            final InputStream inStream = connection.getInputStream();
            final JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));
            try {
                return readPlaceApiResponse(reader);
            } finally {
                reader.close();
            }
        } else {
            return null;
        }
    }

    private List<Object[]> readPlaceApiResponse(JsonReader reader) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "predictions":
                    rows.addAll(readPredictions(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return rows;
    }

    private List<Object[]> readPredictions(JsonReader reader) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            // TODO header
            rows.add(readPrediction(reader));
        }
        if (!rows.isEmpty()) {
            rows.get(rows.size() - 1)[4] = 1;
        }
        reader.endArray();
        return rows;
    }

    private Object[] readPrediction(JsonReader reader) throws IOException {
        String description = null;
        String placeId = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "description":
                    description = reader.nextString();
                    final int lastComma = description.lastIndexOf(',');
                    description = description.substring(0, lastComma);
                    break;
                case "place_id":
                    placeId = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Object[] {
            0,
            description,
            SUGGEST_DATA_URI_PLACES + placeId,
            0,
            0
        };
    }

    private List<Object[]> queryBreweryDbApi(String query) throws IOException {
        final String searchUrl = BuildConfig.BDB_BASE_URL + "/search/" +
            "?key=" + BuildConfig.BDB_API_KEY +
            "&q=" + URLEncoder.encode(query, "UTF-8") +
            "&type=brewery";
        final HttpURLConnection connection =
            (HttpURLConnection) new URL(searchUrl).openConnection();
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // If the connection succeeded, start parsing the response
            final InputStream inStream = connection.getInputStream();
            final JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));
            try {
                return readBreweryDbApiResponse(reader);
            } finally {
                reader.close();
            }
        } else {
            return null;
        }
    }

    private List<Object[]> readBreweryDbApiResponse(JsonReader reader) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "data":
                    rows.addAll(readData(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return rows;
    }

    private List<Object[]> readData(JsonReader reader) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            // TODO header
            rows.add(readBrewery(reader));
        }
        reader.endArray();
        return rows;
    }

    private Object[] readBrewery(JsonReader reader) throws IOException {
        String name = null;
        String breweryId = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "name":
                    name = reader.nextString();
                    break;
                case "id":
                    breweryId = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Object[] {
            0,
            name,
            SUGGEST_DATA_URI_BREWERIES + breweryId,
            0,
            0
        };
    }
    /* Unused */

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("insert() not supported in search provider");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException("delete() not supported in search provider");
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("update() not supported in search provider");
    }
}
