package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.List;

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
}
