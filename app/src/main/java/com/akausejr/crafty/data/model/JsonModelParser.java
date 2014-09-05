package com.akausejr.crafty.data.model;

import android.util.JsonReader;

import java.io.IOException;
import java.util.List;

/**
 * Interface allowing ModelProviders to parse Json into model objects
 *
 * @author AJ Kause
 * Created on 9/3/14.
 */
public interface JsonModelParser<TModel> {

    /**
     * Given a JsonReader, parse the Json stream into a model object
     *
     * @param reader A reader that supplies Json tokens
     * @return A new model object
     * @throws IOException If a network error occurs
     */
    TModel fromJson(JsonReader reader) throws IOException;

    /**
     * Given a JsonReader, parse the Json stream into a list of model objects
     *
     * @param reader A reader that supplies Json tokens
     * @return A new list of model objects
     * @throws IOException If a network error occurs
     */
    List<TModel> listFromJson(JsonReader reader) throws IOException;
}
