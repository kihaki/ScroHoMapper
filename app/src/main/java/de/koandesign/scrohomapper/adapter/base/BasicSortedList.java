package de.koandesign.scrohomapper.adapter.base;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;

public abstract class BasicSortedList<T> extends SortedList.Callback<T> {

    protected abstract RecyclerView.Adapter getRecyclerAdapter();

    @Override
    public void onInserted(int position, int count) {
        getRecyclerAdapter().notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        getRecyclerAdapter().notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        getRecyclerAdapter().notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count) {
        getRecyclerAdapter().notifyItemRangeChanged(position, count);
    }
}
