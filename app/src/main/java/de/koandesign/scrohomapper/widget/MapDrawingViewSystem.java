package de.koandesign.scrohomapper.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.koandesign.scrohomapper.PathNode;
import de.koandesign.scrohomapper.R;

/**
 * Created by Kolossus on 02.12.15.
 */
public class MapDrawingViewSystem extends FrameLayout {
    private CardView mPointMenu;
    private LinearLayout mPointMenuLinearLayout;
    private MapDrawView mDrawingView;
    private boolean mPointMenuVisible;

    public MapDrawingViewSystem(Context context) {
        super(context);
        init();
    }

    public MapDrawingViewSystem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapDrawingViewSystem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MapDrawingViewSystem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if(mDrawingView == null){
            mDrawingView = new MapDrawView(getContext());
            mDrawingView.setContainer(this);
            addView(mDrawingView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mDrawingView.setFocusableInTouchMode(true);
        }
    }

    public void showPointMenu(PathNode selectedNode) {
        // x and y ignored for now
        if(mPointMenu == null){
            mPointMenu = (CardView) findViewById(R.id.point_menu_outer);
            mPointMenuLinearLayout = (LinearLayout) findViewById(R.id.point_menu_inner);
        }
        mPointMenuVisible = true;
        mPointMenu.setVisibility(View.VISIBLE);
        mPointMenu.animate().alpha(1);
        updatePointMenu(selectedNode);
    }

    public void updatePointMenu(PathNode selectedNode) {
        if(selectedNode != null) {
            TextView pointInfo = (TextView) mPointMenuLinearLayout.findViewById(R.id.point_menu_info);
            pointInfo.setText(String.format("Point selected at %d, %d", (int) selectedNode.location.x, (int) selectedNode.location.y));

            // Update connections info
            // Remove old connections info
            for (int i = mPointMenuLinearLayout.getChildCount() - 1; i > 1; i--) {
                mPointMenuLinearLayout.removeViewAt(i);
            }
            // Add new connectionsInfo
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (final PathNode childNode : selectedNode.childNodes) {
                addChildNodeEditorRow(inflater, mPointMenuLinearLayout, childNode);
            }
        }
    }

    private void addChildNodeEditorRow(LayoutInflater inflater, ViewGroup parent, final PathNode childNode) {
        final ViewGroup connectionRow = (ViewGroup) inflater.inflate(R.layout.row_connection, parent, false);
        TextView title = (TextView) connectionRow.findViewById(R.id.row_con_title);
        title.setText(String.format("Connection to point at %d, %d", (int) childNode.location.x, (int) childNode.location.y));

        ImageButton btn = (ImageButton) connectionRow.findViewById(R.id.row_con_remove);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.removeConnectionFromSelectedView(childNode);
                mPointMenuLinearLayout.removeView(connectionRow);
            }
        });
        mPointMenuLinearLayout.addView(connectionRow);
    }

    public void hidePointMenu() {
        mPointMenuVisible = false;
        mPointMenu.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mPointMenu.setVisibility(View.GONE);
            }
        });
    }

    public boolean isPointMenuVisible() {
        return mPointMenuVisible;
    }

    public void setMapAsset(String file) {
        mDrawingView.setMapAsset(file);
    }

    public void setDownsamplingFactor(int factor) {
        mDrawingView.setDownsamplingFactor(factor);
    }

    public void toggleSnapToGrid() {
        mDrawingView.toggleSnapToGrid();
    }

    public boolean isSnapToGrid() {
        return mDrawingView.isSnapToGrid();
    }

    public void toggleBinaryTreeDrawing() {
        mDrawingView.toggleBinaryTreeDrawing();
    }

    public boolean isBinaryTreeDrawingEnabled() {
        return mDrawingView.isBinaryTreeDrawingEnabled();
    }

    public void clearMap() {
        mDrawingView.clearMap();
    }
}
