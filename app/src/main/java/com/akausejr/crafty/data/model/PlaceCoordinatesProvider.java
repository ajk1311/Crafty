package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Decodes {@link com.google.android.gms.maps.model.LatLng} objects
 * from an {@link java.io.InputStream}
 *
 * @author AJ Kause
 * Created on 9/3/14.
 */
public class PlaceCoordinatesProvider extends ModelProvider<LatLng> {

    public PlaceCoordinatesProvider(Context context) {
        super(context, 0);
    }

    @Override
    public LatLng fromJson(JsonReader reader) throws IOException {
        double lat = Double.MAX_VALUE;
        double lng = Double.MAX_VALUE;

        // WARNING: Ugly, ugly code follows
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "result":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "geometry":
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    switch (reader.nextName()) {
                                        case "location":
                                            reader.beginObject();
                                            while (reader.hasNext()) {
                                                switch (reader.nextName()) {
                                                    case "lat":
                                                        lat = reader.nextDouble();
                                                        break;
                                                    case "lng":
                                                        lng = reader.nextDouble();
                                                        break;
                                                    default:
                                                        reader.skipValue();
                                                        break;
                                                }
                                            }
                                            reader.endObject();
                                        default:
                                            reader.skipValue();
                                            break;
                                    }
                                }
                                reader.endObject();
                            default:
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new LatLng(lat, lng);
    }
}
