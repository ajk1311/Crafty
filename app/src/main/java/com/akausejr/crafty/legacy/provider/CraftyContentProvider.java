package com.akausejr.crafty.legacy.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class CraftyContentProvider extends ContentProvider {

    private static final String TAG = CraftyContentProvider.class.getSimpleName();

    public static final String CONTENT_AUTHORITY =
        "com.akausejr.crafty.legacy.provider.CraftyContentProvider";

    /* package */ static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* package */ static final String BASE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.crafty";
    /* package */ static final String BASE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.crafty";

    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final int BREWERY = 200;
    private static final int BREWERY_ID = 201;
    private static final int BREWERY_BEERS = 202;

    private static final int BEER = 300;
    private static final int BEER_ID = 301;

    // TODO social sites

    private static final UriMatcher MATCHER;
    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(CONTENT_AUTHORITY, "location", LOCATION);
        MATCHER.addURI(CONTENT_AUTHORITY, "location/*", LOCATION_ID);
        MATCHER.addURI(CONTENT_AUTHORITY, "brewery", BREWERY);
        MATCHER.addURI(CONTENT_AUTHORITY, "brewery/beers/*", BREWERY_BEERS);
//        MATCHER.addURI(CONTENT_AUTHORITY, "brewery/socialSites", SOCIAL_SITE);
        MATCHER.addURI(CONTENT_AUTHORITY, "brewery/*", BREWERY_ID);
        MATCHER.addURI(CONTENT_AUTHORITY, "beer", BEER);
        MATCHER.addURI(CONTENT_AUTHORITY, "beer/*", BEER_ID);
        // TODO social sites
    }

    private SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        try {
            final CraftyDbHelper opener = new CraftyDbHelper(getContext());
            mDatabase = opener.getWritableDatabase();
            return true;
        } catch (SQLiteException e) {
            Log.v(TAG, "Error opening database");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (MATCHER.match(uri)) {
            case LOCATION: return BreweryLocationContract.CONTENT_TYPE;
            case LOCATION_ID: return BreweryLocationContract.CONTENT_ITEM_TYPE;
            case BREWERY: return BreweryDetailsContract.CONTENT_TYPE;
            case BREWERY_ID: return BreweryDetailsContract.CONTENT_ITEM_TYPE;
            case BREWERY_BEERS: return BeerDetailsContract.CONTENT_TYPE;
            case BEER: return BeerDetailsContract.CONTENT_TYPE;
            case BEER_ID: return BeerDetailsContract.CONTENT_ITEM_TYPE;
            // TODO social sites
            default: throw new UnknownUriException(uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String orderBy) {
        String sortOrder = null;
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (MATCHER.match(uri)) {
            case LOCATION_ID:
                builder.appendWhere(BreweryLocationContract.ID + "=" +
                    BreweryLocationContract.parseLocationId(uri));
            case LOCATION:
                builder.setTables(CraftyDbHelper.Tables.LOCATIONS);
                sortOrder = TextUtils.isEmpty(orderBy) ?
                    BreweryLocationContract.DEFAULT_SORT : orderBy;
                break;
            case BREWERY_ID:
                builder.appendWhere(BreweryDetailsContract.ID + "=" +
                    BreweryDetailsContract.parseBreweryDetailsUri(uri));
                builder.setTables(CraftyDbHelper.Tables.BREWERIES);
                break;
            case BREWERY_BEERS:
                builder.appendWhere(BeerDetailsContract.BREWERY_ID + "+" +
                    BreweryDetailsContract.parseBreweryBeersUri(uri));
                builder.setTables(CraftyDbHelper.Tables.BREWERIES);
                break;
            case BREWERY:
                builder.setTables(CraftyDbHelper.Tables.BREWERIES);
                break;
            case BEER_ID:
                builder.appendWhere(BeerDetailsContract.ID + "=" +
                BeerDetailsContract.parseBeerDetailsUri(uri));
            case BEER:
                builder.setTables(CraftyDbHelper.Tables.BEERS);
                sortOrder = TextUtils.isEmpty(orderBy) ?
                    BreweryDetailsContract.DEFAULT_BEER_SORT : orderBy;
                break;
            default:
                throw new UnknownUriException(uri);
        }

        final Cursor result = builder.query(mDatabase, projection,
            selection, selectionArgs, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (MATCHER.match(uri)) {
            case LOCATION:
                if (mDatabase.insert(CraftyDbHelper.Tables.LOCATIONS, "nullHack", values) >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return BreweryLocationContract.buildLocationUri(
                        values.getAsString(BreweryLocationContract.ID));
                }
                break;
            case BREWERY:
                if (mDatabase.insert(CraftyDbHelper.Tables.BREWERIES, "nullHack", values) >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return BreweryDetailsContract.buildBreweryDetailsUri(
                        values.getAsString(BreweryDetailsContract.ID));
                }
                break;
            case BEER:
                if (mDatabase.insert(CraftyDbHelper.Tables.BEERS, "nullHack", values) >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return BeerDetailsContract.buildBeerDetailsUri(
                        values.getAsString(BeerDetailsContract.ID));
                }
            case LOCATION_ID:
            case BREWERY_ID:
            case BEER_ID:
            case BREWERY_BEERS:
                throw new UnsupportedOperationException("insert", uri);
            default:
                throw new UnknownUriException(uri);
        }
        throw new UnsuccessfulOperationException("insert", uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName;
        switch (MATCHER.match(uri)) {
            case LOCATION_ID:
                selection = //
                    BreweryLocationContract.ID + "=\"" + BreweryLocationContract.parseLocationId(uri) + '"'
                    + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case LOCATION:
                tableName = CraftyDbHelper.Tables.LOCATIONS;
                break;
            case BREWERY_ID:
                selection = //
                    BreweryDetailsContract.ID + "=\"" + BreweryDetailsContract.parseBreweryDetailsUri(uri) + '"'
                    + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case BREWERY:
                tableName = CraftyDbHelper.Tables.BREWERIES;
                break;
            case BEER_ID:
                selection = //
                    BeerDetailsContract.ID + "=\"" + BeerDetailsContract.parseBeerDetailsUri(uri) + '"'
                    + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case BEER:
                tableName = CraftyDbHelper.Tables.BEERS;
                break;
            case BREWERY_BEERS:
                throw new UnsupportedOperationException("update", uri);
            default:
                throw new UnknownUriException(uri);
        }
        final int count = mDatabase.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName;
        switch (MATCHER.match(uri)) {
            case LOCATION_ID:
                selection = //
                    BreweryLocationContract.ID + "=\"" + BreweryLocationContract.parseLocationId(uri) + '"'
                    + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case LOCATION:
                tableName = CraftyDbHelper.Tables.LOCATIONS;
                break;
            case BREWERY_ID:
                selection = //
                    BreweryDetailsContract.ID + "=\"" + BreweryDetailsContract.parseBreweryDetailsUri(uri) + '"'
                        + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case BREWERY:
                tableName = CraftyDbHelper.Tables.BREWERIES;
                break;
            case BEER_ID:
                selection = //
                    BeerDetailsContract.ID + "=\"" + BeerDetailsContract.parseBeerDetailsUri(uri) + '"'
                        + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ')');
            case BEER:
                tableName = CraftyDbHelper.Tables.BEERS;
                break;
            case BREWERY_BEERS:
                throw new UnsupportedOperationException("delete", uri);
            default:
                throw new UnknownUriException(uri);
        }
        final int count = mDatabase.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
        throws OperationApplicationException {
        mDatabase.beginTransaction();
        final int numOperations = operations.size();
        final ContentProviderResult[] results = new ContentProviderResult[numOperations];
        for (int i = 0; i < numOperations; i++) {
            results[i] = operations.get(i).apply(this, results, i);
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        return results;
    }

    private class UnknownUriException extends IllegalArgumentException {
        public UnknownUriException(Uri uri) {
            super("Unknown uri: " + uri);
        }
    }

    private class UnsuccessfulOperationException extends IllegalStateException {
        public UnsuccessfulOperationException(String operation, Uri uri) {
            super("Operation " + operation + " failed for uri " + uri);
        }
    }

    private class UnsupportedOperationException extends java.lang.UnsupportedOperationException {
        public UnsupportedOperationException(String operation, Uri uri) {
            super("Operation " + operation +  " not supported for uri" + uri);
        }
    }
}
