package de.koandesign.scrohomapper;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import de.koandesign.scrohomapper.widget.MapDrawingViewSystem;

@EActivity(R.layout.activity_mapping)
@OptionsMenu(R.menu.mapping_activity)
public class MappingActivity extends AppCompatActivity {

    private static final String FLOOR_PLAN = "floor2half.png";

    @StringRes String snapToGridOff, snapToGridOn;
    @ViewById(R.id.map_draw_view) MapDrawingViewSystem mMapDrawView;
    @ViewById(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    @OptionsItem(R.id.action_calculate_segments)
    void calculateSegments(MenuItem item) {
        if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    @OptionsItem(R.id.action_clear_map)
    void clearMap(MenuItem item) {
        mMapDrawView.clearMap();
    }

    @OptionsItem(R.id.action_straight_lines)
    void toggleSnapToGrid(MenuItem item) {
        mMapDrawView.toggleSnapToGrid();
        boolean isSnapToGrid = mMapDrawView.isSnapToGrid();
        item.setTitle(isSnapToGrid ? snapToGridOn : snapToGridOff);
        item.setTitleCondensed(isSnapToGrid ? snapToGridOn : snapToGridOff);
    }

    @AfterViews
    protected void setupViews() {
        mMapDrawView.setMapAsset(FLOOR_PLAN);
        mMapDrawView.setDownsamplingFactor(1);
    }
}
