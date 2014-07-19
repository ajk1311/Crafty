package com.akausejr.crafty.app;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.akausejr.crafty.R;
import com.akausejr.crafty.provider.SearchSuggestionProvider;
import com.akausejr.crafty.util.CompatUtil;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/18/14.
 */
public class CraftySearchSuggestionAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SUGGESTION = 1;

    public CraftySearchSuggestionAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        final Cursor cursor = (Cursor) getItem(position);
        return cursor.getInt(cursor.getColumnIndex(SearchSuggestionProvider.SUGGEST_COLUMN_HEADER));
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_SUGGESTION;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        switch (getItemViewType(cursor.getPosition())) {
            case VIEW_TYPE_HEADER:
                final TextView header = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.search_suggestion_item, parent, false);
                header.setTextColor(context.getResources()
                    .getColor(R.color.list_item_text_secondary));
                return header;
            case VIEW_TYPE_SUGGESTION:
                return LayoutInflater.from(context)
                    .inflate(R.layout.search_suggestion_item, parent, false);
        }
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String text = cursor.getString(cursor.getColumnIndex(
            SearchManager.SUGGEST_COLUMN_TEXT_1));
        ((TextView) view).setText(text);
        if (cursor.getInt(cursor.getColumnIndex(SearchSuggestionProvider.SUGGEST_COLUMN_DIVIDER)) == 1) {
            CompatUtil.setViewBackground(view,
                context.getResources().getDrawable(R.drawable.list_divider_bg));
        } else {
            CompatUtil.setViewBackground(view, null);
        }
    }
}
