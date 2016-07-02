package de.koandesign.scrohomapper;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * PathNodes
 */
public class PathNode {

    private static final int MAX_CONNECTIONS = 2;
    public PointF location;
    public PathNode parent;
    public List<PathNode> childNodes = new ArrayList<>();

    public PathNode(PointF point){
        location = point;
    }

    public void addChild(PathNode child){
        childNodes.add(child);
    }

    public void removeChild(PathNode child){
        childNodes.remove(child);
    }

    public void clearChildren() {
        childNodes.clear();
    }

    public boolean canTakeMoreConnections() {
        return childNodes.size() + 1 < MAX_CONNECTIONS;
    }
}
