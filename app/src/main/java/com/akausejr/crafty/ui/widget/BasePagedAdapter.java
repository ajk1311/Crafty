package com.akausejr.crafty.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * {@link android.widget.BaseAdapter} implementation that supports paginated data
 *
 * @author AJ Kause
 * Created on 7/8/14.
 */
public abstract class BasePagedAdapter extends BaseAdapter {

    /** Default value for when the adapter should notify the end of the page has been reached */
    private static final int DEFAULT_LOADING_THRESHOLD = 3;

    /** When this many items are remaining in the list, notify subclasses that the end of the page was reached */
    private int mLoadingThreshold;

    /** The current number of loaded pages */
    private int mCurrentPageCount = 0;

    /** Keeps track of the last position we started loading on as not to duplicate loading calls */
    private int mLastNotifiedPosition = -1;

    /**
     * @return The total number of pages in the data set
     */
    public abstract int getPageCount();

    /**
     * Called when the loading threshold of the page has been reached. This method is no longer
     * called when the current page count of the adapter reaches the value returned from
     * {@link BasePagedAdapter#getPageCount()}
     */
    public abstract void onEndOfPage();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(position, convertView);

        if (position != mLastNotifiedPosition && position >= getPageCount() - mLoadingThreshold) {
            onEndOfPage();
            mLastNotifiedPosition = position;
        }

        return convertView;
    }

    public abstract View newView(ViewGroup parent);

    public abstract void bindView(int position, View view);

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        mCurrentPageCount = 0;
    }
}
