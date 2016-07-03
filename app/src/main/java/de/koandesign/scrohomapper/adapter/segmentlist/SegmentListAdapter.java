package de.koandesign.scrohomapper.adapter.segmentlist;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import de.koandesign.scrohomapper.PathNode;
import de.koandesign.scrohomapper.adapter.base.RecyclerViewAdapterBase;
import de.koandesign.scrohomapper.adapter.base.ViewWrapper;
import de.koandesign.scrohomapper.widget.SegmentListItemView;
import de.koandesign.scrohomapper.widget.SegmentListItemView_;

@EBean
public class SegmentListAdapter extends RecyclerViewAdapterBase<PathNode, View> {

    @RootContext Context context;
    protected SortedList<PathNode> segments = new SortedList<>(PathNode.class, new SortedPathNodeListCallback(SegmentListAdapter.this));

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return SegmentListItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<View> holder, int position) {
        final SegmentListItemView segmentView = (SegmentListItemView) holder.getView();
        segmentView.bind(segments.get(position));
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

    public void clear() {
        segments.clear();
    }

    public void addSegment(PathNode segment) {
        segments.add(segment);
    }

    public void addSegments(List<PathNode> segments) {
        this.segments.addAll(segments);
    }
}
