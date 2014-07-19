package com.akausejr.crafty.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by julian on 13/6/21.
 * https://gist.github.com/julianshen/5829333
 *
 * Modified by AJ Kause on 7/16/2014
 */
public class CircleTransform implements Transformation {

    private int mPadding;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleTransform(int padding) {
        mPadding = padding;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final int size = Math.min(source.getWidth(), source.getHeight());

        final int x = (source.getWidth() - size) / 2;
        final int y = (source.getHeight() - size) / 2;

        final Bitmap circleBmp = Bitmap.createBitmap(source, x, y, size, size);
        if (circleBmp != source) {
            source.recycle();
        }

        final Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        final Canvas canvas = new Canvas(bitmap);
        final BitmapShader shader = new BitmapShader(circleBmp,
            BitmapShader.TileMode.CLAMP,
            BitmapShader.TileMode.CLAMP);
        mPaint.setShader(shader);

        final float c = size / 2f;
        final float r = c - mPadding;
        canvas.drawCircle(c, c, r, mPaint);

        circleBmp.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
