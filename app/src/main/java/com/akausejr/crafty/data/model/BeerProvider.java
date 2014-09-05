package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;

/**
 * Decodes {@link com.akausejr.crafty.data.model.Beer} objects from Json and Cursors
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class BeerProvider extends ModelProvider<Beer> {

    /** List size estimate */
    private static final int DEFAULT_LIST_SIZE = 16;

    public BeerProvider(Context context) {
        super(context, DEFAULT_LIST_SIZE);
    }

    @Override
    public Beer fromJson(JsonReader reader) throws IOException {
        String id = null;
        String name = null;
        String description = null;
        double abv = Double.MAX_VALUE;
        boolean isOrganic = false;
        Beer.Style style = null;
        Beer.Labels labels = null;
        Beer.Availability available = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "id":
                    id = reader.nextString();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                case "abv":
                    abv = reader.nextDouble();
                    break;
                case "isOrganic":
                    isOrganic = "Y".equals(reader.nextString());
                    break;
                case "style":
                    style = readStyle(reader);
                    break;
                case "labels":
                    labels = readLabels(reader);
                    break;
                case "available":
                    available = readAvailability(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Beer(id, name, description, abv, isOrganic, style, labels, available);
    }

    private Beer.Style readStyle(JsonReader reader) throws IOException {
        String id = null;
        String name = null;
        String description = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "id":
                    id = reader.nextString();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Beer.Style(id, name, description);
    }

    private Beer.Labels readLabels(JsonReader reader) throws IOException {
        String icon = null;
        String medium = null;
        String large = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "icon":
                    icon = reader.nextString();
                    break;
                case "medium":
                    medium = reader.nextString();
                    break;
                case "large":
                    large = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Beer.Labels(icon, medium, large);
    }

    private Beer.Availability readAvailability(JsonReader reader) throws IOException {
        String id = null;
        String name = null;
        String description = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            switch (fieldName) {
                case "id":
                    id = reader.nextString();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Beer.Availability(id, name, description);
    }
}
