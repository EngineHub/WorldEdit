package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.Vertex;

public class KdTree {
    private final int size;
    private final Node root;
    private final Vector minimumPoint;
    private final Vector maximumPoint;

    public KdTree(List<Vertex> vertices) {
        size = vertices.size();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Vertex vertex : vertices) {
            Vector position = vertex.getPosition();

            if (position.getX() < minX) minX = position.getX();
            if (position.getX() > maxX) maxX = position.getX();

            if (position.getY() < minY) minY = position.getY();
            if (position.getY() > maxY) maxY = position.getY();

            if (position.getZ() < minZ) minZ = position.getZ();
            if (position.getZ() > maxZ) maxZ = position.getZ();
        }

        minimumPoint = new Vector(minX, minY, minZ);
        maximumPoint = new Vector(maxX, maxY, maxZ);

        root = buildTree(vertices, 0, minimumPoint, maximumPoint);
    }

    private Node buildTree(List<Vertex> vertices, int depth, Vector minimumPoint, Vector maximumPoint) {
        if (vertices.size() <= 1)
            return new Leaf(vertices);

        // find dominant axis
        final Vector axis;
        if ("!".isEmpty()) {
            final Vector boxSize = maximumPoint.subtract(minimumPoint);
            if (boxSize.getX() > boxSize.getY()) {
                // x>y
                if (boxSize.getX() > boxSize.getZ()) {
                    // x>y && x>z
                    axis = new Vector(1,0,0);
                } else {
                    // z>=x && x>y
                    axis = new Vector(0,0,1);
                }
            } else {
                // y>=x
                if (boxSize.getY() > boxSize.getZ()) {
                    // y>=x && y>z
                    axis = new Vector(0,1,0);
                } else {
                    // z>=y && y>=x
                    axis = new Vector(0,0,1);
                }
            }
        } else {
            switch (depth%3) {
            case 0:
                axis = new Vector(1,0,0);
                break;

            case 1:
                axis = new Vector(0,1,0);
                break;

            default:
                axis = new Vector(0,0,1);
            }
        }

        // find median
        Collections.sort(vertices, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex lhs, Vertex rhs) {
                return Double.compare(lhs.getPosition().dot(axis), rhs.getPosition().dot(axis));
            }
        });

        final double value;
        if (vertices.size() % 2 == 0) {
            int i = vertices.size()/2;
            value = vertices.get(i-1).getPosition().add(vertices.get(i).getPosition()).dot(axis)*0.5;
        }
        else {
            value = vertices.get(vertices.size()/2).getPosition().dot(axis);
        }

        // split up vertices at split plane
        final List<Vertex> leftVertices = new ArrayList<Vertex>();
        final List<Vertex> rightVertices = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            if (vertex.getPosition().dot(axis) < value) {
                leftVertices.add(vertex);
            } else {
                rightVertices.add(vertex);
            }
        }

        if (leftVertices.isEmpty() || rightVertices.isEmpty()) { // OPTIMIZE: rightVertices can't be empty, replace by assert
            return buildTree(vertices, depth+1, minimumPoint, maximumPoint); // FIXME: this is an infinite recursion for points that occur multiple times (with different normals/colors/etc)
        }

        final Vector leftMaximumPoint = maximumPoint.add(axis.multiply(value - maximumPoint.dot(axis)));
        final Vector rightMinimumPoint = minimumPoint.add(axis.multiply(value - minimumPoint.dot(axis)));

        final Node leftNode = buildTree(leftVertices, depth+1, minimumPoint, leftMaximumPoint);
        final Node rightNode = buildTree(rightVertices, depth+1, rightMinimumPoint, maximumPoint);

        final SubTree subTree = new SubTree(axis, value, leftNode, rightNode);
        return subTree;
    }

    public int getSize() {
        return size;
    }

    public Vector getMinimumPoint() {
        return minimumPoint;
    }

    public Vector getMaximumPoint() {
        return maximumPoint;
    }

    public List<Vertex> getVertices() {
        return root.getVertices();
    }

    public List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint) {
        return root.getVertices(minimumPoint, maximumPoint);
    }
}
