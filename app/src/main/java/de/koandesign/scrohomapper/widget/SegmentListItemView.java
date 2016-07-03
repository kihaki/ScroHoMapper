package de.koandesign.scrohomapper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

import de.koandesign.scrohomapper.PathNode;
import de.koandesign.scrohomapper.R;

@EViewGroup(R.layout.item_segment_list)
public class SegmentListItemView extends RelativeLayout {

    @ViewById(R.id.text) TextView text;

    @DimensionPixelSizeRes(R.dimen.segment_list_item_height) int height;
    @DimensionPixelSizeRes(R.dimen.segment_list_item_padding) int padding;

    public SegmentListItemView(Context context) {
        super(context);
    }

    public SegmentListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SegmentListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SegmentListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        getLayoutParams().height = height;
        setPadding(padding, padding, padding, padding);
        super.onAttachedToWindow();
    }

    public void bind(PathNode pathNode) {
        text.setText(pathNode.segmentNumber + " : "+pathNode.location);
    }
}
