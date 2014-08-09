package com.akausejr.crafty.app.explore;


import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.akausejr.crafty.R;
import com.akausejr.crafty.graphics.CircleLetterColorDrawable;
import com.akausejr.crafty.model.BreweryLocation;
import com.akausejr.crafty.model.LocationType;
import com.akausejr.crafty.provider.BreweryLocationContract;
import com.akausejr.crafty.util.CircleTransform;
import com.akausejr.crafty.util.CompatUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Displays brewery locations as an ordered list. The list can be filtered by type and ordered
 * by distance, name or type.
 *
 * @author AJ Kause
 * Created on 7/9/14.
 */
public class BreweryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Controller interface for handling list interaction */
    public static interface BreweryListController {
        /**
         * Tells the controller to react to a brewery selection
         * @param breweryId The id of the brewery location
         */
        public void onBrewerySelectedFromList(String breweryId);

        /**
         * Tells the controller that the user wants to filter content. All other views should
         * be updated accordingly
         * @param filterType The type of location to filter for
         */
        public void onLocationFilterTypeSelectedFromList(LocationType filterType);
    }

    /** Used for debugging and prefixing */
    private static final String TAG = BreweryListFragment.class.getSimpleName();

    // Named indices for the spinner
    private static final int SORT_DISTANCE = 0;
    private static final int SORT_NAME = 1;
    private static final int SORT_TYPE = 2;

    /** The columns to fetch */
    private static final String[] QUERY_PROJECTION = new String[] {
        BaseColumns._ID,
        BreweryLocationContract.ID,
        BreweryLocationContract.BREWERY_NAME,
        BreweryLocationContract.DISTANCE,
        BreweryLocationContract.LOCATION_TYPE,
        BreweryLocationContract.LOCATION_TYPE_DISPLAY,
        BreweryLocationContract.BREWERY_ICON_URL
    };

    /** Only display locations that are verified by brewerydb.com */
    private static final String QUERY_SELECTION = BreweryLocationContract.STATUS + "=\"verified\"";

    // Cursor indices
    private static final int ID_INDEX = 1;
    private static final int BREWERY_NAME_INDEX = 2;
    private static final int DISTANCE_INDEX = 3;
    private static final int LOCATION_TYPE_INDEX = 4;
    private static final int LOCATION_TYPE_DISPLAY_INDEX = 5;
    private static final int ICON_URL_INDEX = 6;

    /** Handle on this fragment's controller */
    private BreweryListController mController;

    /** The adapter for the list */
    private BreweryLocationAdapter mAdapter;

    /** The spinner that allows type filtering */
    private Spinner mTypeSpinner;

    /** The spinner that allows different sorting */
    private Spinner mSortSpinner;

    /** Displays the content */
    private ListView mListView;

    /** Message to show if no breweries around */
    private View mListEmptyView;

    /** Responds to list clicks and tells the controller that a brewery was selected */
    private final AdapterView.OnItemClickListener mBreweryClickListener =
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mController != null) {
                    final String breweryId = mAdapter.getItem(position);
                    mController.onBrewerySelectedFromList(breweryId);
                }
            }
        };

    /** Responds to user clicks on the type filter, thus filtering search results by type */
    private final AdapterView.OnItemSelectedListener mTypeSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final LocationType filterType = (LocationType) parent.getItemAtPosition(position);
                setCurrentLocationFilterType(filterType.type, true);
                if (mController != null) {
                    mController.onLocationFilterTypeSelectedFromList(filterType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

    /** Responds to user clicks on the sorting spinner and reloads content appropriately */
    private final AdapterView.OnItemSelectedListener mSortOrderSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getLoaderManager().restartLoader(0, null, BreweryListFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BreweryListController) {
            mController = (BreweryListController) activity;
        } else if (getTargetFragment() instanceof BreweryListController) {
            mController = (BreweryListController) getTargetFragment();
        } else {
            mController = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brewery_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Allocate the adapter
        mAdapter = new BreweryLocationAdapter(getActivity());

        // Initialize the list
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setOnItemClickListener(mBreweryClickListener);
        mListView.setAdapter(mAdapter);

        mListEmptyView = view.findViewById(R.id.empty_message);

        mTypeSpinner = (Spinner) view.findViewById(R.id.type_spinner);
        mTypeSpinner.setAdapter(new BreweryLocationTypeSpinnerAdapter(getActivity()));
        mTypeSpinner.setOnItemSelectedListener(mTypeSelectedListener);

        mSortSpinner = (Spinner) view.findViewById(R.id.sort_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
            R.array.sort_order, R.layout.type_spinner_item);
        adapter.setDropDownViewResource(R.layout.type_spinner_dropdown_item);
        mSortSpinner.setAdapter(adapter);
        mSortSpinner.setOnItemSelectedListener(mSortOrderSelectedListener);

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Filters the content based on the given type
     * @param filterType The type to filter by
     */
    public void setCurrentLocationFilterType(LocationType filterType) {
        setCurrentLocationFilterType(filterType.type, false);
    }

    /**
     * Filters the content based on the given type
     * @param filterType The type to filter by
     * @param fromSelf {@code true} if this instance called the method
     */
    private void setCurrentLocationFilterType(String filterType, boolean fromSelf) {
        if (!fromSelf) {
            mTypeSpinner.setSelection(getIndexForType(filterType));
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * @param filterType The location type to filter by
     * @return The index in the Spinner of the given type
     */
    private int getIndexForType(String filterType) {
        for (int i = 0, sz = mTypeSpinner.getAdapter().getCount(); i < sz; i++) {
            final LocationType type = (LocationType) mTypeSpinner.getAdapter().getItem(i);
            if (type.type.equals(filterType)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = QUERY_SELECTION;
        final LocationType filterType = (LocationType) mTypeSpinner.getSelectedItem();
        if (!TextUtils.isEmpty(filterType.type)) {
            selection += " AND " +
                BreweryLocationContract.LOCATION_TYPE + "=\"" + filterType.type + '"';
        }
        String sortBy = null;
        switch (mSortSpinner.getSelectedItemPosition()) {
            case SORT_DISTANCE:
                sortBy = BreweryLocationContract.DISTANCE_SORT;
                break;
            case SORT_NAME:
                sortBy = BreweryLocationContract.NAME_SORT;
                break;
            case SORT_TYPE:
                sortBy = BreweryLocationContract.TYPE_SORT;
                break;
        }
        return new CursorLoader(getActivity(),
            BreweryLocationContract.CONTENT_URI,
            QUERY_PROJECTION,
            selection,
            null,
            sortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (mListView.getEmptyView() == null) {
            mListView.setEmptyView(mListEmptyView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /** Adapter that provides views for each brewery location list row  */
    private static class BreweryLocationAdapter extends CursorAdapter {

        private static final String BULLET = " \u2022 ";

        private static final double FEET_IN_METER = 3.28084;

        private static final double FEET_IN_MILE = 5280;

        // Format strings for different distances
        private final String mFeetFormatString;
        private final String mMilesFormatString;

        /** Each brewery icon is transformed into a circle */
        private final Transformation mCircleTransform;

        private BreweryLocationAdapter(Context context) {
            super(context, null, 0);
            mFeetFormatString = context.getString(R.string.feet_away);
            mMilesFormatString = context.getString(R.string.miles_away);
            mCircleTransform = new CircleTransform(context.getResources()
                .getDimensionPixelSize(R.dimen.list_item_icon_bg_padding));
        }

        @Override
        public String getItem(int position) {
            final Cursor cursor = (Cursor) super.getItem(position);
            return cursor.getString(ID_INDEX);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_item_2_line, parent, false);
            itemView.setTag(new ViewHolder(itemView));
            return itemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            // Brewery name
            final String breweryName = cursor.getString(BREWERY_NAME_INDEX);
            holder.name.setText(breweryName);

            // Info
            final StringBuilder info = new StringBuilder();
            info.append(cursor.getString(LOCATION_TYPE_DISPLAY_INDEX)).append(BULLET)
                .append(distanceString(cursor.getDouble(DISTANCE_INDEX)));
            holder.info.setText(info);

            // Icon background
            CompatUtil.setViewBackground(holder.icon,
                new CircleLetterColorDrawable(
                    context.getResources(),
                    BreweryLocation.getColorResIdForType(cursor.getString(LOCATION_TYPE_INDEX)),
                    breweryName.charAt(0)));

            // Finally, load the icon image
            final String iconUrl = cursor.getString(ICON_URL_INDEX);
            Picasso.with(context).load(iconUrl).transform(mCircleTransform).into(holder.icon);

            holder.divider.setVisibility(cursor.isLast() ? View.INVISIBLE : View.VISIBLE);
        }

        /**
         * @param meters The distance in meters
         * @return The string to display given a brewery's distance from the search location
         */
        private String distanceString(double meters) {
            final double feet = meters * FEET_IN_METER;
            if (feet > 1000) {
                final double miles = feet / FEET_IN_MILE;
                return String.format(mMilesFormatString, miles);
            }
            return String.format(mFeetFormatString, feet);
        }

        static class ViewHolder {
            ImageView icon;
            TextView name;
            TextView info;
            View divider;
            private ViewHolder(View itemView) {
                icon = (ImageView) itemView.findViewById(R.id.brewery_icon);
                name = (TextView) itemView.findViewById(R.id.brewery_name);
                info = (TextView) itemView.findViewById(R.id.brewery_info);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }
}
