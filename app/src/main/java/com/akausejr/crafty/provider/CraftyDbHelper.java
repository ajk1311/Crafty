package com.akausejr.crafty.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class CraftyDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "crafty.db";

    private static final int DATABASE_VERSION = 1;

    private static final String LOCATION_TABLE_CREATE =
        "CREATE TABLE " + BreweryLocationContract.TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BreweryLocationContract.ID + " TEXT NOT NULL, " +
            BreweryLocationContract.NAME + " TEXT NOT NULL, " +
            BreweryLocationContract.STATUS + " TEXT, " +
            BreweryLocationContract.STATUS_DISPLAY + " TEXT, " +
            BreweryLocationContract.LOCATION_TYPE + " TEXT, " +
            BreweryLocationContract.LOCATION_TYPE_DISPLAY + " TEXT, " +
            BreweryLocationContract.LATITUDE + " REAL, " +
            BreweryLocationContract.LONGITUDE + " REAL, " +
            BreweryLocationContract.FULL_ADDRESS + " TEXT, " +
            BreweryLocationContract.PHONE + " TEXT, " +
            BreweryLocationContract.DISTANCE + " REAL, " +
            BreweryLocationContract.BREWERY_ID + " TEXT, " +
            BreweryLocationContract.BREWERY_NAME + " TEXT NOT NULL, " +
            BreweryLocationContract.BREWERY_STATUS + " TEXT, " +
            BreweryLocationContract.BREWERY_STATUS_DISPLAY + " TEXT, " +
            BreweryLocationContract.BREWERY_DESCRIPTION + " TEXT, "  +
            BreweryLocationContract.BREWERY_ESTABLISH_DATE + " TEXT, "  +
            BreweryLocationContract.BREWERY_ICON_IMAGE_URL + " TEXT, "  +
            BreweryLocationContract.BREWERY_MEDIUM_IMAGE_URL + " TEXT, "  +
            BreweryLocationContract.BREWERY_LARGE_IMAGE_URL + " TEXT, "  +
            BreweryLocationContract.BREWERY_IS_ORGANIC + " TEXT, "  +
            BreweryLocationContract.BREWERY_WEBSITE + " TEXT" +
        ");";

    public CraftyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BreweryLocationContract.TABLE_NAME);

        onCreate(db);
    }
}
