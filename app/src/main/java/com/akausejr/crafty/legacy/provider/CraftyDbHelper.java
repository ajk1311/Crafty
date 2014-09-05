package com.akausejr.crafty.legacy.provider;

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

    /* package */ static interface Tables {
        static final String LOCATIONS = "breweryLocations";
        static final String BREWERIES = "breweries";
        static final String BEERS = "beers";
        static final String SOCIAL_SITES = "socialSites";
    }

    /* package */ static interface References {
        static final String BREWERY_ID =
            "REFERENCES " + Tables.BREWERIES + '(' + LocationColumns.ID + ')';
    }

    /* package */ static interface LocationColumns extends BaseColumns {
        static final String ID = "id";
        static final String STATUS = "status";
        static final String LOCATION_TYPE = "locationType";
        static final String LOCATION_TYPE_DISPLAY = "locationTypeDisplay";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String ADDRESS = "address";
        static final String PHONE = "phone";
        static final String DISTANCE = "distance";
        static final String BREWERY_ID = "breweryId";
        static final String BREWERY_NAME = "breweryName";
        static final String BREWERY_ICON_URL = "breweryIconUrl";
    }

    private static final String LOCATION_TABLE_CREATE =
        "CREATE TABLE IF NOT EXISTS " + Tables.LOCATIONS + " (" +
            LocationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LocationColumns.ID + " TEXT NOT NULL, " +
            LocationColumns.STATUS + " TEXT, " +
            LocationColumns.LOCATION_TYPE + " TEXT, " +
            LocationColumns.LOCATION_TYPE_DISPLAY + " TEXT, " +
            LocationColumns.LATITUDE + " REAL, " +
            LocationColumns.LONGITUDE + " REAL, " +
            LocationColumns.ADDRESS + " TEXT, " +
            LocationColumns.PHONE + " TEXT, " +
            LocationColumns.DISTANCE + " REAL, " +
            LocationColumns.BREWERY_ID + " TEXT, " +
            LocationColumns.BREWERY_NAME + " TEXT NOT NULL, " +
            LocationColumns.BREWERY_ICON_URL + " TEXT, "  +
            "UNIQUE (" + LocationColumns.ID + ") ON CONFLICT REPLACE);";

    /* package */ static interface BreweryColumns extends BaseColumns {
        static final String ID = "id";
        static final String STATUS = "status";
        static final String NAME = "name";
        static final String DESCRIPTION = "description";
        static final String ICON_URL = "iconUrl";
        static final String IMAGE_URL_MEDIUM = "mediumImageUrl";
        static final String IMAGE_URL_large = "largeImageUrl";
        static final String ESTABLISHED = "established";
        static final String IS_ORGANIC = "isOrganic";
        static final String WEBSITE = "website";
    }

    private static final String BREWERY_TABLE_CREATE =
        "CREATE TABLE IF NOT EXISTS " + Tables.BREWERIES + " (" +
            BreweryColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BreweryColumns.ID + " TEXT NOT NULL, " +
            BreweryColumns.STATUS + " TEXT, " +
            BreweryColumns.NAME + " TEXT, " +
            BreweryColumns.DESCRIPTION + " TEXT, " +
            BreweryColumns.ICON_URL + " TEXT, " +
            BreweryColumns.IMAGE_URL_MEDIUM + " TEXT, " +
            BreweryColumns.IMAGE_URL_large + " TEXT, " +
            BreweryColumns.ESTABLISHED + " TEXT, " +
            BreweryColumns.IS_ORGANIC + " TEXT, " +
            BreweryColumns.WEBSITE + " TEXT, " +
            "UNIQUE (" + BreweryColumns.ID + ") ON CONFLICT REPLACE);";

    /* package */ static interface BeerColumns extends BaseColumns {
        static final String ID = "id";
        static final String STATUS = "status";
        static final String NAME = "name";
        static final String DESCRIPTION = "description";
        static final String AVAILABILITY_NAME = "availabilityName";
        static final String AVAILABILITY_DESCRIPTION = "availabilityDescription";
        static final String IS_ORGANIC = "isOrganic";
        static final String IBU = "ibu";
        static final String ORIGINAL_GRAVITY = "originalGravity";
        static final String YEAR = "year";
        static final String ABV = "abv";
        static final String STYLE_NAME = "styleName";
        static final String STYLE_DESCRIPTION = "styleDescription";
        static final String STYLE_CATEGORY_NAME = "styleCategoryName";
        static final String SERVING_TEMPERATURE = "servingTemperature";
        static final String SERVING_TEMPERATURE_DISPLAY = "servingTemperatureDisplay";
        static final String LABEL_URL_MEDIUM = "mediumLabelUrl";
        static final String LABEL_URL_LARGE = "largeLabelUrl";
        static final String LABEL_URL_ICON = "iconLabelUrl";
        static final String FOOD_PAIRINGS = "foodPairings";
        static final String BREWERY_ID = "breweryId";
    }

    private static final String BEER_TABLE_CREATE =
        "CREATE TABLE IF NOT EXISTS " + Tables.BEERS + " (" +
            BeerColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BeerColumns.ID + " TEXT NOT NULL, " +
            BeerColumns.STATUS + " TEXT, " +
            BeerColumns.NAME + " TEXT, " +
            BeerColumns.DESCRIPTION+ " TEXT, " +
            BeerColumns.AVAILABILITY_NAME+ " TEXT, " +
            BeerColumns.AVAILABILITY_DESCRIPTION+ " TEXT, " +
            BeerColumns.IS_ORGANIC+ " TEXT, " +
            BeerColumns.IBU+ " REAL, " +
            BeerColumns.ORIGINAL_GRAVITY+ " REAL, " +
            BeerColumns.YEAR+ " TEXT, " +
            BeerColumns.ABV+ " REAL, " +
            BeerColumns.STYLE_NAME+ " TEXT, " +
            BeerColumns.STYLE_DESCRIPTION+ " TEXT, " +
            BeerColumns.STYLE_CATEGORY_NAME+ " TEXT, " +
            BeerColumns.SERVING_TEMPERATURE+ " TEXT, " +
            BeerColumns.SERVING_TEMPERATURE_DISPLAY+ " TEXT, " +
            BeerColumns.LABEL_URL_MEDIUM+ " TEXT, " +
            BeerColumns.LABEL_URL_LARGE+ " TEXT, " +
            BeerColumns.LABEL_URL_ICON+ " TEXT, " +
            BeerColumns.FOOD_PAIRINGS+ " TEXT, " +
            BeerColumns.BREWERY_ID+ " TEXT NOT NULL " + References.BREWERY_ID + ", " +
            "UNIQUE (" + BeerColumns.ID + ") ON CONFLICT REPLACE);";

    /* package */ static interface SocialSiteColumns extends BaseColumns {
        // TODO fill in columns
    }

    private static final String SOCIAL_SITE_TABLE_CREATE = ""; // TODO fill out statement

    public CraftyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
        db.execSQL(BREWERY_TABLE_CREATE);
        db.execSQL(BEER_TABLE_CREATE);
        // TODO social sites
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.BREWERIES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.BEERS);
        // TODO social sites
        onCreate(db);
    }
}
