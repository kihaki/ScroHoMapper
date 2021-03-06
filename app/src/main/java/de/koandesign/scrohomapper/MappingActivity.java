package de.koandesign.scrohomapper;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.koandesign.scrohomapper.adapter.segmentlist.SegmentListAdapter;
import de.koandesign.scrohomapper.events.OnClearMapEvent;
import de.koandesign.scrohomapper.events.OnPathNodeAddedEvent;
import de.koandesign.scrohomapper.widget.MapDrawingViewSystem;
import de.koandesign.scrohomapper.widget.NonBlockingDrawerLayout;

@EActivity(R.layout.activity_mapping)
@OptionsMenu(R.menu.mapping_activity)
public class MappingActivity extends AppCompatActivity {

    private static final String FLOOR_PLAN = "floor2half.png";

    @StringRes String showBinaryTreeOff, showBinaryTreeOn;
    @StringRes String snapToGridOff, snapToGridOn;
    @StringRes String showSegments, hideSegments;

    @ViewById(R.id.map_draw_view) MapDrawingViewSystem mMapDrawView;
    @ViewById(R.id.drawer_layout) NonBlockingDrawerLayout mDrawerLayout;
    @ViewById(R.id.ll_right_drawer) ViewGroup mDrawerRight;
    @ViewById(R.id.rv_segments_list) RecyclerView mSegmentsRecycler;

    @Bean SegmentListAdapter mSegmentsAdapter;

    private EventBus mEventBus = EventBus.getDefault();

    @OptionsItem(R.id.action_toggle_binary_tree_drawing)
    void toggleBinaryTreeDrawing(MenuItem item) {
        mMapDrawView.toggleBinaryTreeDrawing();
        boolean isBinaryTreeDrawingEnabled = mMapDrawView.isBinaryTreeDrawingEnabled();
        item.setTitle(isBinaryTreeDrawingEnabled ? showBinaryTreeOn : showBinaryTreeOff);
        item.setTitleCondensed(isBinaryTreeDrawingEnabled ? showBinaryTreeOn : showBinaryTreeOff);
    }

    @OptionsItem(R.id.action_straight_lines)
    void toggleSnapToGrid(MenuItem item) {
        mMapDrawView.toggleSnapToGrid();
        boolean isSnapToGrid = mMapDrawView.isSnapToGrid();
        item.setTitle(isSnapToGrid ? snapToGridOn : snapToGridOff);
        item.setTitleCondensed(isSnapToGrid ? snapToGridOn : snapToGridOff);
    }

    @OptionsItem(R.id.action_show_segments)
    void showSegments(MenuItem item) {
        if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
            item.setTitle(showSegments);
            item.setTitleCondensed(showSegments);
        } else {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
            item.setTitle(hideSegments);
            item.setTitleCondensed(hideSegments);
        }
    }

    @Click(R.id.btn_clear_map)
    void clearMap(Button btn) {
        mMapDrawView.clearMap();
    }

    @AfterViews
    protected void setupViews() {
        // Drawer
        mDrawerLayout.setDrawerViewRightForNonBlocking(mDrawerRight);
        mDrawerLayout.setScrimColor(0x39000000);

        // Floorplan
        mMapDrawView.setMapAsset(FLOOR_PLAN);
        mMapDrawView.setDownsamplingFactor(1);

        // Segments Recycler
        mSegmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mSegmentsRecycler.setAdapter(mSegmentsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void onPathNodeAdded(OnPathNodeAddedEvent event) {
        mSegmentsAdapter.addSegment(event.pathNode);
    }

    @Subscribe
    public void onClearMapDone(OnClearMapEvent event) {
        mSegmentsAdapter.clear();
    }
}
