package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

class SubTree implements Node {
    private final Axis axis;
    private final double value;
    private final Node left;
    private final Node right;

    public SubTree(Axis axis, double value, Node left, Node right) {
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
        boolean minimumPointIsLeft = axis.dot(minimumPoint) < value;
        boolean maximumPointIsLeft = axis.dot(maximumPoint) < value;

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

    @Override
    public List<Vertex> getVerticesFast(Vector minimumPoint, Vector maximumPoint) {
        boolean minimumPointIsLeft = axis.dot(minimumPoint) < value;
        boolean maximumPointIsLeft = axis.dot(maximumPoint) < value;

        if (minimumPointIsLeft != maximumPointIsLeft) {
            final ArrayList<Vertex> vertices = new ArrayList<Vertex>(left.getVerticesFast(minimumPoint, maximumPoint));
            vertices.addAll(right.getVerticesFast(minimumPoint, maximumPoint));
            return vertices;
        }

        if (minimumPointIsLeft) {
            return left.getVerticesFast(minimumPoint, maximumPoint);
        } else {
            return right.getVerticesFast(minimumPoint, maximumPoint);
        }
    }

    @Override
    public SortedMap<Double, Vertex> getKNearestVertices(Vector center, int amount) {
        // TODO Auto-generated method stub
        return null;
    }
}
