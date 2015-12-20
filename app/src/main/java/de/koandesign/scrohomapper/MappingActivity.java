package de.koandesign.scrohomapper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.koandesign.scrohomapper.widget.MapDrawView;
import de.koandesign.scrohomapper.widget.MapDrawingViewSystem;

public class MappingActivity extends AppCompatActivity {

    private MapDrawingViewSystem mMapDrawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        mMapDrawView = (MapDrawingViewSystem) findViewById(R.id.map_draw_view);

        //mMapDrawView.setMap(getBitmapFromAsset("floor2.png"));

        mMapDrawView.setMapAsset("floor2half.png");
        mMapDrawView.setDownsamplingFactor(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mapping_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_straight_lines:
                mMapDrawView.toggleSnapToGrid();
                boolean isSnapToGrid = mMapDrawView.isSnapToGrid();
                item.setTitle(isSnapToGrid ? "Snap to Grid" : "No Snap to Grid");
                item.setTitleCondensed(isSnapToGrid ? "Snap to Grid" : "No Snap to Grid");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
