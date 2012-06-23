package com.sk89q.worldedit.shape;

import java.util.List;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.shape.kdtree.KdTree;

public abstract class FileModel implements Model {
    private final List<Vertex> vertices;
    private KdTree kdTree;

    protected FileModel(String filename) {
        vertices = load();

        kdTree = new KdTree(vertices);
    }

    protected abstract List<Vertex> load();

    @Override
    public Iterable<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Vector getMinimumPoint() {
        return kdTree.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return kdTree.getMaximumPoint();
    }
}
