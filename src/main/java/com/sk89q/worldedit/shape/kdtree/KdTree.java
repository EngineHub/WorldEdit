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

        root = buildTree(vertices, minimumPoint, maximumPoint);
    }

    private Node buildTree(List<Vertex> vertices, Vector minimumPoint, Vector maximumPoint) {
        if (vertices.size() <= 1)
            return new Leaf(vertices);

        // find dominant axis
        final Vector boxSize = maximumPoint.subtract(minimumPoint);
        final Vector axis;
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

        // find median
        final double value = 0;
        Collections.sort(vertices, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex lhs, Vertex rhs) {
                return Double.compare(lhs.getPosition().dot(axis), rhs.getPosition().dot(axis));
            }
        });

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

        final Vector leftMaximumPoint = maximumPoint.add(axis.multiply(value - maximumPoint.dot(axis)));
        final Vector rightMinimumPoint = minimumPoint.add(axis.multiply(value - minimumPoint.dot(axis)));

        final Node leftNode = buildTree(leftVertices, minimumPoint, leftMaximumPoint);
        final Node rightNode = buildTree(rightVertices, rightMinimumPoint, maximumPoint);

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
}
