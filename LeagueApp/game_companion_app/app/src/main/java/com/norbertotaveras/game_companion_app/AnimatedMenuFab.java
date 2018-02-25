package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.gordonwong.materialsheetfab.AnimatedFab;

/**
 * Created by Norberto on 2/2/2018.
 */

public class AnimatedMenuFab
        extends FloatingActionButton
        implements AnimatedFab
{
    public AnimatedMenuFab(Context context) {
        super(context);
    }

    public AnimatedMenuFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedMenuFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show(float translationX, float translationY) {

    }
}
