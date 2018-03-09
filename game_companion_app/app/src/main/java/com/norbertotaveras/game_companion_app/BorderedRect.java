package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Norberto Taveras on 1/30/2018.
 */

/**
 * Draw a view with a border that has the given dash pattern, width, and color
 */
public class BorderedRect extends View {
    private String dashPattern = "10,10";
    private int lineColor = Color.RED;
    private int lineWidth = 1;

    private Paint paint;
    private DashPathEffect pathEffect;

    public BorderedRect(Context context) {
        super(context);
        init(null, 0);
    }

    public BorderedRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BorderedRect(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BorderedRect, defStyle, 0);

        dashPattern = a.getString(R.styleable.BorderedRect_dashPattern);
        lineColor = a.getColor(R.styleable.BorderedRect_lineColor, lineColor);
        lineWidth = a.getInteger(R.styleable.BorderedRect_lineWidth, lineWidth);

        a.recycle();

        // Update TextPaint and text measurements from attributes
        invalidateSettings();
    }

    private void invalidateSettings() {
        String[] parts = dashPattern.split(",");
        float[] floatParts = new float[parts.length];
        for (int i = 0; i < parts.length; ++i)
            floatParts[i] = Float.parseFloat(parts[i]);

        pathEffect = new DashPathEffect(floatParts, 0);
        paint.setPathEffect(pathEffect);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentWidth = getWidth();
        int contentHeight = getHeight();

        canvas.drawRect(0, 0, contentWidth, contentHeight, paint);
    }

    public String getDashPattern() {
        return dashPattern;
    }

    public void setDashPattern(String pattern) {
        dashPattern = pattern;
        invalidateSettings();
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int color) {
        lineColor = color;
        invalidateSettings();
    }

    public int getLineWidth() {
        return lineWidth;
    }
}
