package com.akausejr.crafty.data.model.loader;

import android.content.Context;
import android.net.Uri;
import android.util.JsonReader;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.data.model.ModelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Loads a list of models from the network and delivers the result via a LoaderManager
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public abstract class ModelListLoader<TModel> extends AbsModelLoader<List<TModel>> {

    private final Uri mUrl;

    /** Parses the network response */
    private final ModelProvider<TModel> mModelProvider;

    public ModelListLoader(Context context, Class<TModel> modelClass, String baseUrl) {
        super(context);
        mModelProvider = CraftyApp.getModelProviderFactory().getProvider(modelClass);
        mUrl = Uri.parse(baseUrl);
    }

    /**
     * Called when the network request is about to be made.
     * Subclasses should add the appropriate paths and query parameters to the url.
     *
     * @param urlBuilder The builder responsible for building the model url
     */
    protected abstract void onBuildUrl(Uri.Builder urlBuilder);

    @Override
    protected String getModelUrl() {
        final Uri.Builder urlBuilder = mUrl.buildUpon();
        onBuildUrl(urlBuilder);
        return urlBuilder.toString();
    }

    @Override
    protected List<TModel> getModelFromStream(InputStream inputStream) throws IOException {
        final JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        final List<TModel> modelList = mModelProvider.listFromJson(reader);
        onModelListLoaded(modelList);
        return modelList;
    }

    /** Called after the model was parsed */
    protected void onModelListLoaded(final List<TModel> model) {
        /* Stub for subclasses */
    }
}
