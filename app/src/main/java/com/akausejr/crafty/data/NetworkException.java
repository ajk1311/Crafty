package com.akausejr.crafty.data;

/**
 * Represents a network error from the server
 *
 * @author AJ Kause
 * Created on 9/4/14.
 */
public class NetworkException extends Exception {

    /** Error code used in place of an HTTP response code if there is no network */
    public static final int CODE_NO_NETWORK = -1;

    private final int mHttpResponseCode;

    public NetworkException(int httpResponseCode) {
        mHttpResponseCode = httpResponseCode;
    }

    public int getResponseCode() {
        return mHttpResponseCode;
    }
}
