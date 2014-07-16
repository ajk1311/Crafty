package com.akausejr.crafty.util;

import android.os.Build;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;

/**
 * Common utilities for detecting device information regarding version compatibility
 *
 * @author AJ Kause
 * Created on 7/14/14.
 */
public class CompatUtil {
    private CompatUtil(){ /* No instances */ }

    public static boolean isGingerbread() {
        return isSdkVersionOrLater(Build.VERSION_CODES.GINGERBREAD);
    }

    public static boolean isGingerbreadMr1() {
        return isSdkVersionOrLater(Build.VERSION_CODES.GINGERBREAD_MR1);
    }

    public static boolean isHoneycomb() {
        return isSdkVersionOrLater(Build.VERSION_CODES.HONEYCOMB);
    }

    public static boolean isHoneycombMr1() {
        return isSdkVersionOrLater(Build.VERSION_CODES.HONEYCOMB_MR1);
    }

    public static boolean isHoneycombMr2() {
        return isSdkVersionOrLater(Build.VERSION_CODES.HONEYCOMB_MR2);
    }

    public static boolean isIceCreamSandwich() {
        return isSdkVersionOrLater(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    public static boolean isIceCreamSandwichMr1() {
        return isSdkVersionOrLater(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1);
    }

    public static boolean isJellyBean() {
        return isSdkVersionOrLater(Build.VERSION_CODES.JELLY_BEAN);
    }

    public static boolean isJellyBeanMr1() {
        return isSdkVersionOrLater(Build.VERSION_CODES.JELLY_BEAN_MR1);
    }

    public static boolean isJellyBeanMr2() {
        return isSdkVersionOrLater(Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

    public static boolean isKitKat() {
        return isSdkVersionOrLater(Build.VERSION_CODES.KITKAT);
    }

    public static boolean isKitKatWatch() {
        return isSdkVersionOrLater(Build.VERSION_CODES.KITKAT_WATCH);
    }

    public static boolean isLPreview() {
        return "L".equals(Build.VERSION.CODENAME);
    }

    private static boolean isSdkVersionOrLater(int versionInt) {
        return Build.VERSION.SDK_INT >= versionInt;
    }

    public static class AnimatorListenerAdapterCompat implements ViewPropertyAnimatorListener {
        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
        }

        @Override
        public void onAnimationCancel(View view) {
        }
    }
}
