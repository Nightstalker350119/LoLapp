package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by Norberto Taveras on 1/29/2018.
 */

public class RoundImageView extends android.support.v7.widget.AppCompatImageView {
    Paint paint = new Paint();
    PorterDuffXfermode atopMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    Bitmap tempBitmap;

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable original = getDrawable();

        if (original == null || !(original instanceof  BitmapDrawable))
            return;

        BitmapDrawable drawable = (BitmapDrawable)original;

        int width = getWidth();
        int height = getHeight();
        int minDim = Math.min(width, height);
        float minDimD2 = minDim / 2.0f;

        if (tempBitmap == null)
            tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas tempCanvas = new Canvas(tempBitmap);

        paint.setXfermode(null);
        paint.setColor(0);
        tempCanvas.drawRect(0, 0, (float)width, (float)height, paint);

        Rect dstRect = new Rect(0, 0, minDim, minDim);
        paint.setColor(0xFFFFFFFF);
        tempCanvas.drawCircle(minDimD2, minDimD2, minDimD2, paint);
        paint.setXfermode(atopMode);
        tempCanvas.drawBitmap(drawable.getBitmap(), null, dstRect, paint);
        //paint.setXfermode(null);

        canvas.drawBitmap(tempBitmap, null, dstRect, null);
    }
}
