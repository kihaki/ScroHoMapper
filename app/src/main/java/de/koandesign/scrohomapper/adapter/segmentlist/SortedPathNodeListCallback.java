package de.koandesign.scrohomapper.adapter.segmentlist;

import android.support.v7.widget.RecyclerView;

import de.koandesign.scrohomapper.PathNode;
import de.koandesign.scrohomapper.adapter.base.BasicSortedList;

public class SortedPathNodeListCallback extends BasicSortedList<PathNode> {

    private final RecyclerView.Adapter mAdapter;

    public SortedPathNodeListCallback(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public int compare(PathNode a, PathNode b) {
        return a.segmentNumber - b.segmentNumber;
    }

    @Override
    public boolean areContentsTheSame(PathNode oldItem, PathNode newItem) {
        return oldItem.location.equals(newItem.location);
    }

    @Override
    public boolean areItemsTheSame(PathNode a, PathNode b) {
        return a.location.equals(b.location);
    }
}