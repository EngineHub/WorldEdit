package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

class SubTree implements Node {
    private final Vector axis;
    private final double value;
    private final Node left;
    private final Node right;

    public SubTree(Vector axis, double value, Node left, Node right) {
        this.axis = axis;
        this.value = value;
        this.left = left;
        this.right = right;
    }

    @Override
    public List<Vertex> getVertices() {
        final ArrayList<Vertex> vertices = new ArrayList<Vertex>(left.getVertices());
        vertices.addAll(right.getVertices());
        return vertices;
    }

    @Override
    public List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint) {
        boolean minimumPointIsLeft = minimumPoint.dot(axis) < value;
        boolean maximumPointIsLeft = maximumPoint.dot(axis) < value;

        if (minimumPointIsLeft != maximumPointIsLeft) {
            final ArrayList<Vertex> vertices = new ArrayList<Vertex>(left.getVertices(minimumPoint, maximumPoint));
            vertices.addAll(right.getVertices(minimumPoint, maximumPoint));
            return vertices;
        }

        if (minimumPointIsLeft) {
            return left.getVertices(minimumPoint, maximumPoint);
        } else {
            return right.getVertices(minimumPoint, maximumPoint);
        }
    }
}
