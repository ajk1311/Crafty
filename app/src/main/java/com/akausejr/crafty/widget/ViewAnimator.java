package com.akausejr.crafty.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

/**
 * {@link android.widget.ViewAnimator} subclass that supports
 * displaying its children by id in addition to index
 *
 * @author AJ Kause
 * Created on 7/20/14.
 */
public class ViewAnimator extends android.widget.ViewAnimator {

    /** Used internally to denote that a view was not found */
    private static final int INVALID_ID = -1;

    /** Keep a mapping of child id to index so we avoid a lookup every time */
    private final SparseIntArray mIndexCache;

    public ViewAnimator(Context context) {
        super(context);
        mIndexCache = new SparseIntArray();
    }

    public ViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIndexCache = new SparseIntArray();
    }

    /**
     * Sets which child view will be displayed
     * @param childId the id of the child view to display
     */
    public void setDisplayedChildId(int childId) {
        int childIndex = mIndexCache.get(childId, INVALID_ID);
        if (childIndex == INVALID_ID) {
            childIndex = findIndexForId(childId);
            mIndexCache.put(childId, childIndex);
        }
        setDisplayedChild(childIndex);
    }

    private int findIndexForId(int id) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getId() == id) {
                return i;
            }
        }
        return INVALID_ID;
    }
}
