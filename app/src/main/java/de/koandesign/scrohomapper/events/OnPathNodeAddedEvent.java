package de.koandesign.scrohomapper.events;

import de.koandesign.scrohomapper.PathNode;

public class OnPathNodeAddedEvent {
    public PathNode pathNode;

    public OnPathNodeAddedEvent(PathNode node) {
        pathNode = node;
    }
}
