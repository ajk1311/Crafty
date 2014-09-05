package com.akausejr.crafty.data.model;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given an {@link java.io.InputStream}, provides one or more model objects corresponding
 * to the given parameter data class.
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public abstract class ModelProvider<TModel> implements JsonModelParser<TModel> {

    /** Approximation for the size of the lists returned by the list factory methods */
    private final int mDefaultListSize;

    /** The application's Context. Safe to use across Activity life-cycles */
    private final Context mApplicationContext;

    public ModelProvider(final Context context, final int defaultListSize) {
        mDefaultListSize = defaultListSize;
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public List<TModel> listFromJson(JsonReader reader) throws IOException {
        final List<TModel> list = createList();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                list.add(fromJson(reader));
            }
            reader.endArray();
        } finally {
            reader.close();
        }

        return list;
    }

    /**
     * @return A new list to populate with TModel data
     */
    private List<TModel> createList() {
        return new ArrayList<>(mDefaultListSize);
    }

    /**
     * @return An application Context that is safe to use outside the Activity lifecycle
     */
    protected Context getContext() {
        return mApplicationContext;
    }

    /**
     * Supplies ModelProvider instances
     */
    public static class Factory {

        /** Keeps all of the ModelProviders indexed by their respective model classes */
        private final Map<Class<?>, ModelProvider<?>> mProviders = new HashMap<>();

        public <TModel> void registerProvider(Class<TModel> modelClass,
                                              ModelProvider<TModel> provider) {
            if (mProviders.containsKey(modelClass)) {
                throw new IllegalArgumentException("Provider already registered for " + modelClass);
            }
            mProviders.put(modelClass, provider);
        }

        /**
         * @return A ModelProvider that builds TModel objects for the given class
         */
        @SuppressWarnings("unchecked")
        public <TModel> ModelProvider<TModel> getProvider(Class<TModel> modelClass) {
            final ModelProvider<TModel> provider = (ModelProvider<TModel>) mProviders.get(modelClass);
            if (provider == null) {
                throw new IllegalArgumentException("No provider registered for" + modelClass);
            }
            return provider;
        }
    }
}
