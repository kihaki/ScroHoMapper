package de.koandesign.scrohomapper;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
public class PointFQuadTree {

    /**
     * The bounds of this quad.
     */
    private final NodeBounds mBounds;

    /**
     * The depth of this quad in the tree.
     */
    private final int mDepth;

    /**
     * Maximum number of elements to store in a quad before splitting.
     */
    private final static int MAX_ELEMENTS = 2; // For testing we allow only very low count // 50;

    /**
     * The elements inside this quad, if any.
     */
    private List<PathNode> mItems;

    /**
     * Maximum depth.
     */
    private final static int MAX_DEPTH = 40;

    /**
     * Child quads.
     */
    private List<PointFQuadTree> mChildren = null;

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public PointFQuadTree(float minX, float maxX, float minY, float maxY) {
        this(new NodeBounds(minX, maxX, minY, maxY));
    }

    public PointFQuadTree(NodeBounds bounds) {
        this(bounds, 0);
    }

    private PointFQuadTree(float minX, float maxX, float minY, float maxY, int depth) {
        this(new NodeBounds(minX, maxX, minY, maxY), depth);
    }

    private PointFQuadTree(NodeBounds bounds, int depth) {
        mBounds = bounds;
        mDepth = depth;
    }

    /**
     * Get children (for debugging)
     */
    public List<PointFQuadTree> getChildren() {
        return mChildren;
    }

    /**
     * Get Bounds (for debugging)
     */
    public NodeBounds getBounds(){
        return mBounds;
    }

    /**
     * Insert an item.
     */
    public void add(PathNode node) {
        if (this.mBounds.contains(node.location.x, node.location.y)) {
            insert(node.location.x, node.location.y, node);
        }
    }

    private void insert(double x, double y, PathNode item) {
        if (this.mChildren != null) {
            if (y < mBounds.getMidY()) {
                if (x < mBounds.getMidX()) { // top left
                    mChildren.get(0).insert(x, y, item);
                } else { // top right
                    mChildren.get(1).insert(x, y, item);
                }
            } else {
                if (x < mBounds.getMidX()) { // bottom left
                    mChildren.get(2).insert(x, y, item);
                } else {
                    mChildren.get(3).insert(x, y, item);
                }
            }
            return;
        }
        if (mItems == null) {
            mItems = new ArrayList<PathNode>();
        }
        mItems.add(item);
        if (mItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split();
        }
    }

    /**
     * Split this quad.
     */
    private void split() {
        mChildren = new ArrayList<>(4);
        mChildren.add(new PointFQuadTree(mBounds.getMinX(), mBounds.getMidX(), mBounds.getMinY(), mBounds.getMidY(), mDepth + 1));
        mChildren.add(new PointFQuadTree(mBounds.getMidX(), mBounds.getMaxX(), mBounds.getMinY(), mBounds.getMidY(), mDepth + 1));
        mChildren.add(new PointFQuadTree(mBounds.getMinX(), mBounds.getMidX(), mBounds.getMidY(), mBounds.getMaxY(), mDepth + 1));
        mChildren.add(new PointFQuadTree(mBounds.getMidX(), mBounds.getMaxX(), mBounds.getMidY(), mBounds.getMaxY(), mDepth + 1));

        List<PathNode> items = mItems;
        mItems = null;

        for (PathNode node : items) {
            // re-insert items into child quads.
            insert(node.location.x, node.location.y, node);
        }

        Log.d("NodeSelect", "Splitting tree");
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    public boolean remove(PathNode node) {
        if (this.mBounds.contains(node.location.x, node.location.y)) {
            return remove(node.location.x, node.location.y, node);
        } else {
            return false;
        }
    }

    private boolean remove(double x, double y, PathNode node) {
        if (this.mChildren != null) {
            if (y < mBounds.getMidY()) {
                if (x < mBounds.getMidX()) { // top left
                    return mChildren.get(0).remove(x, y, node);
                } else { // top right
                    return mChildren.get(1).remove(x, y, node);
                }
            } else {
                if (x < mBounds.getMidX()) { // bottom left
                    return mChildren.get(2).remove(x, y, node);
                } else {
                    return mChildren.get(3).remove(x, y, node);
                }
            }
        }
        else {
            return mItems.remove(node);
        }
    }

    /**
     * Removes all points from the quadTree
     */
    public void clear() {
        mChildren = null;
        if (mItems != null) {
            mItems.clear();
        }
    }

    /**
     * Search for all items within a given bounds.
     */
    public Collection<PathNode> search(NodeBounds searchBounds) {
        final List<PathNode> results = new ArrayList<>();
        search(searchBounds, results);
        return results;
    }

    private void search(NodeBounds searchBounds, Collection<PathNode> results) {
        if (!mBounds.intersects(searchBounds)) {
            return;
        }

        if (this.mChildren != null) {
            for (PointFQuadTree quad : mChildren) {
                quad.search(searchBounds, results);
            }
        } else if (mItems != null) {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mItems);
            } else {
                for (PathNode item : mItems) {
                    if (searchBounds.contains(item)) {
                        results.add(item);
                    }
                }
            }
        }
    }

    public Collection<PathNode> searchSquareWithSize(float x, float y, float size){
        Log.v("NodeSelect", String.format("Looking for close nodes to %f, %f, area=%f", x, y, size));
        float sizeDelta = size / 2;
        NodeBounds searchBounds = new NodeBounds(x - sizeDelta, x + sizeDelta, y - sizeDelta, y + sizeDelta);
        return search(searchBounds);
    }
}