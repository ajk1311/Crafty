package com.akausejr.crafty.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.akausejr.crafty.R;
import com.akausejr.crafty.data.model.LocationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for a spinner
 *
 * @author AJ Kause
 * Created on 7/17/14.
 */
public class BreweryLocationTypeSpinnerAdapter extends ArrayAdapter<LocationType> {

    private List<Drawable> mColorDrawables = new ArrayList<>();

    public BreweryLocationTypeSpinnerAdapter(Context context) {
        this(context, LocationType.list(context));
    }

    public BreweryLocationTypeSpinnerAdapter(Context context, List<LocationType> list) {
        super(context, R.layout.type_spinner_item, list);
        for (LocationType type : list) {
            mColorDrawables.add(createColorDrawableForType(type));
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.type_spinner_dropdown_item, parent, false);
        }
        final LocationType type = getItem(position);
        final TextView item = (TextView) convertView;
        item.setText(type.display);
        item.setCompoundDrawablesWithIntrinsicBounds(
            null, null, mColorDrawables.get(position), null);
        return item;
    }

    private Drawable createColorDrawableForType(LocationType type) {
        final int size = getContext().getResources()
            .getDimensionPixelSize(R.dimen.type_indicator_size);
        return new CircleColorDrawable(type.color, size);
    }

    private static class CircleColorDrawable extends ColorDrawable {

        private int mSize;
        private Path mPath = new Path();

        public CircleColorDrawable(int color, int size) {
            super(color);
            mSize = size;
        }

        @Override
        public void draw(Canvas canvas) {
            mPath.reset();
            mPath.addCircle(mSize / 2, mSize / 2, mSize / 2, Path.Direction.CW);
            canvas.clipPath(mPath);
            super.draw(canvas);
        }

        @Override
        public int getIntrinsicHeight() {
            return mSize;
        }

        @Override
        public int getIntrinsicWidth() {
            return mSize;
        }
    }
}
