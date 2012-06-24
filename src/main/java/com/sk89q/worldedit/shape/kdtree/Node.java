package com.sk89q.worldedit.shape.kdtree;

import java.util.List;
import java.util.SortedMap;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

interface Node {
    List<Vertex> getVertices();
    List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint);
    List<Vertex> getVerticesFast(Vector minimumPoint, Vector maximumPoint);
    SortedMap<Double, Vertex> getKNearestVertices(Vector center, int amount);
}
