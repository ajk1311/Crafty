package com.akausejr.crafty.widget;

import android.content.Context;
import android.graphics.Outline;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.akausejr.crafty.util.CompatUtil;

/**
 * {@link android.widget.ImageButton} extension that maintains a circular background
 *
 * Created on 7/7/14.
 */
public class FabImageButton extends ImageButton implements View.OnLongClickListener {

    public FabImageButton(Context context) {
        super(context);
        setOnLongClickListener(this);
    }

    public FabImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (CompatUtil.isLPreview()) {
            setCircleOutline(w, h);
        }
    }

    @SuppressWarnings("NewApi")
    private void setCircleOutline(int w, int h) {
        final Outline circle = new Outline();
        circle.setOval(0, 0, w, h);
        setOutline(circle);
        setClipToOutline(true);
    }

    @Override
    public boolean onLongClick(View view) {
        final CharSequence description = getContentDescription();
        if (!TextUtils.isEmpty(description)) {
            Toast.makeText(view.getContext(), description, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
