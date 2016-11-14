package com.peprally.jeremy.peprally.custom.ui;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.peprally.jeremy.peprally.R;

public class EmptyViewSwipeRefreshLayout extends SwipeRefreshLayout {

    public EmptyViewSwipeRefreshLayout(Context callingContext) {
        super(callingContext);
    }

    public EmptyViewSwipeRefreshLayout(Context callingContext, AttributeSet attrs) {
        super(callingContext, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        ViewGroup target = (ViewGroup) findViewById(R.id.id_container_swipe_refresh_view_group);

        // check if adapter view is visible
        View scrollableView = target.getChildAt(1);
        if (scrollableView.getVisibility() == GONE) {
            // use empty view layout instead
            scrollableView = target.getChildAt(0);
        }

        return ViewCompat.canScrollVertically(scrollableView, -1);
    }
}
