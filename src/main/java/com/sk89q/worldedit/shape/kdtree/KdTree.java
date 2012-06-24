package com.sk89q.worldedit.shape.kdtree;

import java.util.ArrayList;
import java.util.Collection;
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
        if (depth > 20 || vertices.size() <= 3)
            return new Leaf(vertices);

        // TODO: maybe check for duplicates here instead of in the model loader?

        // cycle through axes
        final Axis axis = Axis.values()[depth % 3];

        // find median
        Collections.sort(vertices, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex lhs, Vertex rhs) {
                return Double.compare(axis.dot(lhs.getPosition()), axis.dot(rhs.getPosition()));
            }
        });

        final int mid = vertices.size() / 2;
        final double median;
        if ((vertices.size() & 1) == 0) {
            // example: 6 elements => average index 3 and 2 of  0 1 >2< >3< 4 5
            final double before = axis.dot(vertices.get(mid-1).getPosition());
            final double after = axis.dot(vertices.get(mid).getPosition());

            median = (before + after) * 0.5;
        }
        else {
            // example: 5 elements => index 2 of  0 1 >2< 3 4
            median = axis.dot(vertices.get(mid).getPosition());
        }

        // split up vertices at split plane
        final List<Vertex> leftVertices = new ArrayList<Vertex>();
        final List<Vertex> rightVertices = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            if (axis.dot(vertex.getPosition()) < median) {
                leftVertices.add(vertex);
            } else {
                rightVertices.add(vertex);
            }
        }

        if (leftVertices.isEmpty() || rightVertices.isEmpty()) { // OPTIMIZE: rightVertices can't be empty, replace by assert
            //System.out.println(median+"/"+axis.multiply(1)+"/"+leftVertices.isEmpty()+"/"+rightVertices.isEmpty());
            //System.out.println(vertices);
            return buildTree(vertices, depth+1, minimumPoint, maximumPoint); // FIXME: this is an infinite recursion for points that occur multiple times (with different normals/colors/etc)
        }

        final Vector leftMaximumPoint = maximumPoint.add(axis.multiply(median - axis.dot(maximumPoint)));
        final Vector rightMinimumPoint = minimumPoint.add(axis.multiply(median - axis.dot(minimumPoint)));

        final Node leftNode = buildTree(leftVertices, depth+1, minimumPoint, leftMaximumPoint);
        final Node rightNode = buildTree(rightVertices, depth+1, rightMinimumPoint, maximumPoint);

        final SubTree subTree = new SubTree(axis, median, leftNode, rightNode);
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

    /**
     * Returns all vertices in the tree.
     * 
     * @return
     */
    public List<Vertex> getVertices() {
        return root.getVertices();
    }

    /**
     * Returns vertices in a given bounding box.
     * 
     * @param minimumPoint minimum point of the bounding box
     * @param maximumPoint minimum point of the bounding box
     * @return a list that contains all vertices inside the bounding box and none outside.
     */
    public List<Vertex> getVertices(Vector minimumPoint, Vector maximumPoint) {
        return root.getVertices(minimumPoint, maximumPoint);
    }

    /**
     * Like {@link #getVertices(Vector, Vector)}, except without doing any bounds checking in the leaves.
     * 
     * This means that that the returned list might contain elements outside the bounding box.
     * 
     * @param minimumPoint minimum point of the bounding box
     * @param maximumPoint minimum point of the bounding box
     * @return a list that contains all vertices inside the bounding box and maybe some outside.
     */
    public List<Vertex> getVerticesFast(Vector minimumPoint, Vector maximumPoint) {
        return root.getVerticesFast(minimumPoint, maximumPoint);
    }

    public Collection<Vertex> getKNearestVertices(Vector center, int amount) {
        return root.getKNearestVertices(center, amount).values();
    }
}
