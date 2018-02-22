import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.R;

/**
 * Created by Norberto on 2/21/2018.
 */

public class RoundTextView extends android.support.v7.widget.AppCompatTextView {
    Paint backgroundPaint;
    Paint textPaint;
    Rect textBounds;

    public RoundTextView(Context context) {
        super(context);
    }

    public RoundTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFF123456);
        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textBounds = new Rect();
        setTextColor(getCurrentTextColor());
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        textPaint.setColor(color);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
        CharSequence text = getText();
        int width = getWidth();
        int height = getHeight();

        //textPaint.getTextBounds(text.toString(), 0, text.length(), textBounds);


        canvas.drawCircle(width / 2, height / 2, Math.min(width, height), backgroundPaint);
        canvas.drawText(text, 0, text.length(), width / 2, height / 2, textPaint);
    }
}
