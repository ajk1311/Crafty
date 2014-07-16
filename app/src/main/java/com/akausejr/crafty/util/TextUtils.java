package com.akausejr.crafty.util;

/**
 * Extension of {@link android.text.TextUtils} to include some app-specific methods
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class TextUtils {

    public static String join(CharSequence delimiter, CharSequence... tokens) {
        return android.text.TextUtils.join(delimiter, tokens);
    }
}
