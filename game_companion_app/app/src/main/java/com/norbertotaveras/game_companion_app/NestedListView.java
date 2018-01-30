package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Norberto on 1/27/2018.
 */

public class NestedListView extends ListView {
    private NestedScrollView scrollParent;

    public NestedListView(Context context) {
        super(context);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollParent(NestedScrollView parent) {
        this.scrollParent = parent;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (scrollParent == null)
            return super.canScrollList(direction);

        if (scrollParent.canScrollVertically(direction))
            return false;

        return super.canScrollVertically(direction);
    }
}
