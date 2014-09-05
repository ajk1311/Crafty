package com.akausejr.crafty.data.model.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.akausejr.crafty.data.NetworkException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Base loader class for loading models from the network. This class has the capability to retry
 * loads if network errors are encountered.
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public abstract class AbsModelLoader<TModel> extends AsyncTaskLoader<AbsModelLoader.Result<TModel>> {

    /**
     * Simple class that encapsulates the result of a network load. <br>
     * If the load succeeded, and error will be null. If this is the case,
     * then the payload will only be null if nothing was returned from the network.
     */
    public static class Result<TModel> {

        /** {@code null} if no error occurred during load */
        public final Exception error;

        /** The actual payload of the result as a model class */
        public final TModel payload;

        /* package */ Result(final Exception error, final TModel payload) {
            this.error = error;
            this.payload = payload;
        }
    }

    /** The max number of times to retry before giving up the load */
    private static final int DEFAULT_MAX_RETRY_COUNT = 8;

    /** Handle on the main thread for retrying loads */
    private static final Handler RETRY_HANDLER = new Handler();

    /** The currently loaded result */
    private Result<TModel> mResult;

    /** {@code true} if the load should be retried upon network error */
    private final boolean mRetry;

    /** How many times to retry until the load is abandoned */
    private final int mMaxRetryCount;

    /** How many times we have retried the load */
    private int mRetryCount = 1;

    protected AbsModelLoader(Context context) {
        this(context, DEFAULT_MAX_RETRY_COUNT);
    }

    protected AbsModelLoader(Context context, int maxRetryCount) {
        super(context);
        mRetry = maxRetryCount > 0;
        mMaxRetryCount = maxRetryCount;
    }

    /**
     * @return The exact URL of the model to be loaded
     */
    protected abstract String getModelUrl();

    /**
     * Given an InputStream connection, build a model object and return it
     *
     * @param inputStream The network stream to load from
     * @return A new model object
     * @throws IOException If a network error occurs
     */
    protected abstract TModel getModelFromStream(InputStream inputStream) throws IOException;

    @Override
    public Result<TModel> loadInBackground() {
        // Result params
        Exception error = null;
        TModel model = null;

        final ConnectivityManager connectivityManager = (ConnectivityManager) getContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            // No active network. If the user wants to auto retry, then schedule another load
            if (mRetry) {
                tryScheduleRetry();
            }
            return new Result<>(new NetworkException(NetworkException.CODE_NO_NETWORK), null);
        }

        // The resources we need to read the network response
        // Don't forget to clean these up after we are done
        HttpURLConnection connection = null;
        InputStream networkStream = null;

        try {
            // Connect to the server at the given url
            connection = (HttpURLConnection) new URL(getModelUrl()).openConnection();
            final int responseCode = connection.getResponseCode();

            // Only process the result if the code is a success
            if (responseCode == HTTP_OK) {
                networkStream = connection.getInputStream();
                model = getModelFromStream(networkStream);
            } else {
                error = new NetworkException(responseCode);
            }
        } catch (IOException ioe) {
            if (mRetry) {
                tryScheduleRetry();
            }
            error = ioe;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (networkStream != null) {
                try {
                    networkStream.close();
                } catch (IOException e) {
                    // Ignore since we are just closing the stream
                }
            }
        }

        return new Result<>(error, model);
    }

    /**
     * Try to schedule another load
     * @return {@code true} if the load was scheduled
     */
    private boolean tryScheduleRetry() {
        if (mRetryCount++ == mMaxRetryCount) {
            return false;
        }
        final long delay = (long) Math.pow(mRetryCount, 2) * 1000; // ms
        RETRY_HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceLoad();
            }
        }, delay);
        return true;
    }

    @Override
    public void deliverResult(Result<TModel> result) {
        mResult = result;
        if (isStarted()) {
            super.deliverResult(mResult);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mResult = null;
    }
}
