package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes {@link Brewery}
 * objects from an {@link java.io.InputStream}
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class BreweryProvider extends ModelProvider<Brewery> {

    private static final int DEFAULT_LIST_SIZE = 16;

    public BreweryProvider(Context context) {
        super(context, DEFAULT_LIST_SIZE);
    }

    @Override
    public Brewery fromJson(JsonReader reader) throws IOException {
        String id = null;
        String name = null;
        String status = null;
        String statusDisplay = null;
        String description = null;
        String established = null;
        Brewery.Images images = null;
        boolean isOrganic = false;
        String website = null;
        List<Brewery.SocialSite> socialAccounts = null;

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
                case "status":
                    status = reader.nextString();
                    break;
                case "statusDisplay":
                    statusDisplay = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                case "established":
                    established = reader.nextString();
                    break;
                case "images":
                    images = readImages(reader);
                    break;
                case "isOrganic":
                    isOrganic = "Y".equals(reader.nextString());
                    break;
                case "website":
                    website = reader.nextString();
                    break;
                case "socialAccounts":
                    socialAccounts = readSocialAccounts(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Brewery(id, name, status, statusDisplay, description, established,
            images, isOrganic, website, socialAccounts);
    }

    private Brewery.Images readImages(JsonReader reader) throws IOException {
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

        return new Brewery.Images(icon, medium, large);
    }

    private List<Brewery.SocialSite> readSocialAccounts(JsonReader reader) throws IOException {
        final List<Brewery.SocialSite> list = new ArrayList<>(8);

        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                String id = null;
                String name = null;
                String website = null;

                final String fieldName = reader.nextName();
                switch (fieldName) {
                    case "id":
                        id = reader.nextString();
                        break;
                    case "name":
                        name = reader.nextString();
                        break;
                    case "website":
                        website = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
                final Brewery.SocialSite socialSite = new Brewery.SocialSite(id, name, website);
                list.add(socialSite);
            }
            reader.endObject();
        }
        reader.endArray();

        return list;
    }
}
