package com.sk89q.worldedit.shape.kdtree;

import java.util.Collection;

import com.sk89q.worldedit.shape.Vertex;

class Leaf implements Node {
    private final Collection<Vertex> vertices;

    public Leaf(Collection<Vertex> vertices) {
        this.vertices = vertices;
    }
}
