package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Norberto Taveras on 1/29/2018.
 */

public class NestedParentView extends NestedScrollView {
    public NestedParentView(@NonNull Context context) {
        super(context);
    }

    public NestedParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedParentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int pos = getScrollY();
        int childSize = getChildAt(0).getMeasuredHeight();
        int parentHeight = getMeasuredHeight();
        int maxScroll = Math.max(0, childSize - parentHeight);

        if (dy > 0) {
            consumed[1] = Math.min(maxScroll - pos, dy);
            scrollBy(0, dy);
        } else if (dy < 0) {
            // Example:
            //  if targetPos = 3 and dy = -5
            //  then consumed is 3 and scroll parent by -2
            ListView list = (ListView)target;
            int targetPos = -list.getChildAt(0).getTop();
            if (targetPos == 0) {
                consumed[1] = dy;
                scrollBy(0, dy);
            }
        }

        Log.v("Prescroll", String.format("dy=%d maxScroll=%d consumed=%d", dy, maxScroll,
                consumed[1]));
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.v("NestedParentView", "onStartNestedScroll");
        return true;
        //return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
//
//    @Override
//    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
//                               int dxUnconsumed, int dyUnconsumed) {
//        Log.v("NestedParentView", "onNestedScroll");
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//    }
//
//    @Override
//    public void onStopNestedScroll(View target) {
//        Log.v("NestedParentView", "onStopNestedScroll");
//        super.onStopNestedScroll(target);
//    }
}
