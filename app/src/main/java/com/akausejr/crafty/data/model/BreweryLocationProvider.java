package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;

/**
 * Decodes {@link BreweryLocation}
 * objects from an {@link java.io.InputStream}
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class BreweryLocationProvider extends ModelProvider<BreweryLocation> {

    /** Start the results list at this size */
    private static final int DEFAULT_LIST_SIZE = 25;

    /** Builds {@link Brewery} objects */
    private final BreweryProvider mBreweryProvider;

    public BreweryLocationProvider(Context context, BreweryProvider breweryProvider) {
        super(context, DEFAULT_LIST_SIZE);
        mBreweryProvider = breweryProvider;
    }

    @Override
    public BreweryLocation fromJson(JsonReader reader) throws IOException {
        String id = null;
        String status = null;
        String locationType = null;
        String locationTypeDisplay = null;
        double latitude = Double.MAX_VALUE;
        double longitude = Double.MAX_VALUE;
        String streetAddress = null;
        String locality = null;
        String region = null;
        String postalCode = null;
        String phone = null;
        String breweryId = null;
        Brewery brewery = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "id":
                    id = reader.nextString();
                    break;
                case "status":
                    status = reader.nextString();
                    break;
                case "locationType":
                    locationType = reader.nextString();
                    break;
                case "locationTypeDisplay":
                    locationTypeDisplay = reader.nextString();
                    break;
                case "latitude":
                    latitude = reader.nextDouble();
                    break;
                case "longitude":
                    longitude = reader.nextDouble();
                    break;
                case "streetAddress":
                    streetAddress = reader.nextString();
                    break;
                case "locality":
                    locality = reader.nextString();
                    break;
                case "region":
                    region = reader.nextString();
                    break;
                case "postalCode":
                    postalCode = reader.nextString();
                    break;
                case "phone":
                    phone = reader.nextString();
                    break;
                case "breweryId":
                    breweryId = reader.nextString();
                    break;
                case "brewery":
                    brewery = mBreweryProvider.fromJson(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new BreweryLocation(id, status, locationType, locationTypeDisplay, latitude,
            longitude, streetAddress, locality, region, postalCode, phone, breweryId, brewery);
    }
}
