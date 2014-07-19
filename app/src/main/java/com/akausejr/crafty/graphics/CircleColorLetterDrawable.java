package com.akausejr.crafty.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.akausejr.crafty.R;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/17/14.
 */
public class CircleColorLetterDrawable extends Drawable {

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint mLetterPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Rect mLetterBounds = new Rect();
    private char[] mLetter = new char[1];

    private static Typeface sTypeface;

    public CircleColorLetterDrawable(Resources resources, int colorResId, char letter) {
        if (sTypeface == null) {
            sTypeface = Typeface.createFromAsset(resources.getAssets(), "Roboto-Light.tff");
        }

        mBackgroundPaint.setColor(resources.getColor(colorResId));

        mLetter[0] = letter;
        mLetterPaint.setTextAlign(Paint.Align.CENTER);
        mLetterPaint.setTypeface(sTypeface);
        mLetterPaint.setColor(resources.getColor(android.R.color.white));
        mLetterPaint.getTextBounds("a", 0, 1, mLetterBounds);
        mLetterPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.large_letter_size));
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        final float cx = bounds.width() / 2f;
        final float cy = bounds.height() / 2f;
        canvas.drawCircle(cx, cy, cx, mBackgroundPaint);

        final float x = bounds.width() / 2f;
        final float y = bounds.height() / 2f - mLetterBounds.top + mLetterPaint.descent();
        canvas.drawText(mLetter, 0, 1, x, y, mLetterPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mBackgroundPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBackgroundPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return mBackgroundPaint.getAlpha();
    }
}
