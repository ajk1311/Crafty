package com.akausejr.crafty.provider;

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
        "com.akausejr.crafty.provider.CraftyContentProvider";

    /* package */ static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* package */ static final String BASE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.crafty";
    /* package */ static final String BASE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.crafty";

    private static final int LOCATION = 0;
    private static final int LOCATION_ID = 1;

    private static final UriMatcher MATCHER;
    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(CONTENT_AUTHORITY, "location", LOCATION);
        MATCHER.addURI(CONTENT_AUTHORITY, "location/*", LOCATION_ID);
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
            default: throw new UnknownUriException(uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String orderBy) {
        String sortOrder;
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (MATCHER.match(uri)) {
            case LOCATION_ID:
                builder.appendWhere(BreweryLocationContract.ID + "=" +
                    BreweryLocationContract.parseLocationId(uri));
            case LOCATION:
                builder.setTables(BreweryLocationContract.TABLE_NAME);
                sortOrder = TextUtils.isEmpty(orderBy) ?
                    BreweryLocationContract.DEFAULT_SORT : orderBy;
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
                if (mDatabase.insert(BreweryLocationContract.TABLE_NAME, "nullHack", values) >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return BreweryLocationContract.buildLocationUri(
                        values.getAsString(BreweryLocationContract.ID));
                }
                break;
            case LOCATION_ID:
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
                tableName = BreweryLocationContract.TABLE_NAME;
                break;
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
                tableName = BreweryLocationContract.TABLE_NAME;
                break;
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
