package com.akausejr.crafty.data.model.loader;

import android.content.Context;
import android.net.Uri;
import android.util.JsonReader;

import com.akausejr.crafty.CraftyApp;
import com.akausejr.crafty.data.model.ModelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Loads a single model from the network and delivers the result via a LoaderManager
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public abstract class ModelLoader<TModel> extends AbsModelLoader<TModel> {

    private final Uri mUrl;

    /** Parses the network response */
    private final ModelProvider<TModel> mModelProvider;

    public ModelLoader(Context context, Class<TModel> modelClass, String baseUrl) {
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
    protected TModel getModelFromStream(InputStream inputStream) throws IOException {
        final JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        final TModel model = mModelProvider.fromJson(reader);
        onModelLoaded(model);
        return model;
    }

    /** Called once the model has been parsed */
    protected void onModelLoaded(final TModel model) {
        /* Stub for subclasses */
    }
}
