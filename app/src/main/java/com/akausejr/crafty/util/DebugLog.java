package com.akausejr.crafty.util;

import android.util.Log;

import com.akausejr.crafty.BuildConfig;

/**
 * Wrapper around {@link android.util.Log} that only actually logs if the running app is a debug
 * build. Otherwise, each method is a NO-OP.
 *
 * @author AJ Kause
 * Created on 7/15/14.
 */
public class DebugLog {
    private DebugLog(){
    }

    /** @see android.util.Log#v(String, String) */
    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG) Log.v(tag, message);
    }

    /** @see android.util.Log#i(String, String) */
    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) Log.i(tag, message);
    }

    /** @see android.util.Log#d(String, String) */
    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    /** @see android.util.Log#w(String, String) */
    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) Log.w(tag, message);
    }

    /** @see android.util.Log#e(String, String) */
    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) Log.e(tag, message);
    }

    /** @see java.lang.Throwable#printStackTrace() */
    public static void printStackTrace(Throwable t) {
        if (BuildConfig.DEBUG) t.printStackTrace();
    }
}
