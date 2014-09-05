package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;
import java.util.List;

/**
 * Decodes {@link com.akausejr.crafty.data.model.PlacesPrediction} objects
 * from an {@link java.io.InputStream}
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class PlacesPredictionProvider extends ModelProvider<PlacesPrediction> {

    private final PlacesProvider mPlacesProvider;

    public PlacesPredictionProvider(Context context, PlacesProvider placesProvider) {
        super(context, 0);
        mPlacesProvider = placesProvider;
    }

    @Override
    public PlacesPrediction fromJson(JsonReader reader) throws IOException {
        List<Place> predictions = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "predictions":
                    predictions = mPlacesProvider.listFromJson(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new PlacesPrediction(predictions);
    }
}
