package de.koandesign.scrohomapper;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * PathNodes
 */
public class PathNode {

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
}
