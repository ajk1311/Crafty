package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;

/**
 * Decodes {@link com.akausejr.crafty.data.model.Place} objects
 * from an {@link java.io.InputStream}
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class PlacesProvider extends ModelProvider<Place> {

    private static final int DEFAULT_LIST_SIZE = 5;

    public PlacesProvider(Context context) {
        super(context, DEFAULT_LIST_SIZE);
    }

    @Override
    public Place fromJson(JsonReader reader) throws IOException {
        String id = null;
        String name = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "description":
                    final String description = reader.nextString();
                    final int lastComma = description.lastIndexOf(',');
                    name = description.substring(0, lastComma);
                    break;
                case "place_id":
                    id = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Place(id, name);
    }
}
