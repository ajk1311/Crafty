package com.akausejr.crafty.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.akausejr.crafty.R;

/**
 * Modified {@link android.widget.TextView} that will shrink its text size if the text
 * set at the preferred text size does not fit in the bounds of the view
 *
 * @author AJ Kause
 * Created on 7/17/14.
 */
public class AutoTextView extends TextView {

    private int mMinTextSize;

    private int mTextSizeIncrement;

    private TextPaint mTextPaint;

    public AutoTextView(Context context) {
        this(context, null, 0);
    }

    public AutoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextPaint = new TextPaint();
        final TypedArray a = context.obtainStyledAttributes(attrs,
            R.styleable.AutoTextView, defStyleAttr, 0);
        mMinTextSize = a.getDimensionPixelSize(R.styleable.AutoTextView_minTextSize,
            context.getResources().getDimensionPixelSize(R.dimen.auto_text_view_min_text_size));
        mTextSizeIncrement = context.getResources()
            .getDimensionPixelSize(R.dimen.auto_text_view_text_size_increment);
        a.recycle();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        fitTextToView();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (width != oldWidth) {
            fitTextToView();
        }
    }

    /** Shrinks the current text to fit into the view */
    private void fitTextToView() {
        int currentTextSize = (int) getTextSize();
        if (currentTextSize == mMinTextSize) {
            return;
        }

        final float width = getAvailableWidth();
        final String currentText = getText().toString();

        if (width <= 0 || TextUtils.isEmpty(currentText)) {
            return;
        }

        mTextPaint.set(getPaint());
        mTextPaint.setTextSize(currentTextSize);

        while (mTextPaint.measureText(currentText) > width && currentTextSize > mMinTextSize) {
            currentTextSize -= mTextSizeIncrement;
            mTextPaint.setTextSize(currentTextSize);
        }

        currentTextSize = Math.max(currentTextSize, mMinTextSize);
        setEllipsize(currentTextSize == mMinTextSize ? TextUtils.TruncateAt.END : null);

        setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
    }

    /**
     * @return The available width for the view taking into account padding and compound drawables
     */
    private int getAvailableWidth() {
        int available = getWidth() - getPaddingLeft() - getPaddingRight();
        final Drawable left = getCompoundDrawables()[0];
        if (left != null) {
            available -= (left.getIntrinsicWidth() + getCompoundDrawablePadding());
        }
        final Drawable right = getCompoundDrawables()[2];
        if (right != null) {
            available -= (right.getIntrinsicWidth() + getCompoundDrawablePadding());
        }
        return available;
    }
}
