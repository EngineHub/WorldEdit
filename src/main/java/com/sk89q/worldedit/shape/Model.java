package com.sk89q.worldedit.shape;

import com.sk89q.worldedit.Vector;

public interface Model {
    Iterable<Vertex> getVertices();
    Vector getMinimumPoint();
    Vector getMaximumPoint();
}
