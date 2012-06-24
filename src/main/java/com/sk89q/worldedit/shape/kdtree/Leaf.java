package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

class Leaf implements Node {
    private final List<Vertex> vertices;

    public Leaf(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    @Override
    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint) {
        final List<Vertex> ret = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            if (vertex.getPosition().containedWithin(minimumPoint, maximumPoint))
                ret.add(vertex);
        }
        return ret;
    }

    @Override
    public List<Vertex> getVerticesFast(Vector minimumPoint, Vector maximumPoint) {
        return vertices;
    }

    @Override
    public SortedMap<Double, Vertex> getKNearestVertices(Vector center, int amount) {
        // TODO Auto-generated method stub
        return null;
    }
}
