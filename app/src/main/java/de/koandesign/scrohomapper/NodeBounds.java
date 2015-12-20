package de.koandesign.scrohomapper;

public class NodeBounds {

    float mMinX;
    float mMaxX;
    float mMinY;
    float mMaxY;

    public NodeBounds(float minX, float maxX, float minY, float maxY){
        this.mMinX = minX;
        this.mMaxX = maxX;
        this.mMinY = minY;
        this.mMaxY = maxY;
    }

    public boolean contains(float x, float y){
        return x >= mMinX && x <= mMaxX && y >= mMinY && y <= mMaxY;
    }

    public float getMidX() {
        return getMinX() + (getMaxX() - getMinX()) / 2;
    }

    public float getMidY() {
        return getMinY() + (getMaxY() - getMinY()) / 2;
    }

    public float getMinX() {
        return mMinX;
    }

    public float getMinY() {
        return mMinY;
    }

    public float getMaxX() {
        return mMaxX;
    }

    public float getMaxY() {
        return mMaxY;
    }

    public boolean intersects(NodeBounds other) {
        boolean intersectsHorizontal = (other.getMaxX() <= getMaxX() && other.getMaxX() >= getMinX()) ||
                (other.getMinX() <= getMaxX() && other.getMinX() >= getMinX());
        boolean intersectsVertical = (other.getMaxY() <= getMaxY() && other.getMaxY() >= getMinY()) ||
                (other.getMinY() <= getMaxY() && other.getMinY() >= getMinY());
        return intersectsHorizontal && intersectsVertical;
    }

    public boolean contains(PathNode point) {
        return point.location.x >= getMinX() && point.location.x <= getMaxX() && point.location.y >= getMinY() && point.location.y <= getMaxY();
    }

    public boolean contains(NodeBounds bounds) {
        boolean containsHorizontal = bounds.getMinX() >= getMinX() && bounds.getMaxX() <= getMaxX();
        boolean containsVertical = bounds.getMinY() >= getMinY() && bounds.getMaxY() <= getMaxY();
        return containsHorizontal && containsVertical;
    }
}
