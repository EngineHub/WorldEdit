package com.sk89q.worldedit.shape.kdtree;

import java.util.List;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

interface Node {
    List<Vertex> getVertices();
    List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint);
}
