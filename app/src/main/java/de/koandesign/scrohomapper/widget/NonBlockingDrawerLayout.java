package de.koandesign.scrohomapper.widget;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NonBlockingDrawerLayout extends DrawerLayout {

    private View rightDrawerView;

    public NonBlockingDrawerLayout(Context context) {
        super(context);
    }

    public NonBlockingDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonBlockingDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDrawerViewRightForNonBlocking(View view) {
        this.rightDrawerView = view;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (rightDrawerView != null && isDrawerOpen(rightDrawerView)) {
            if (event.getX() < rightDrawerView.getX()) {
                return false;
            }
        }
        return super.onInterceptTouchEvent(event);
    }
}
