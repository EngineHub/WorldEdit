package com.sk89q.worldedit.shape;

import java.util.List;

public abstract class AbstractModel implements Model {
    private final List<Vertex> vertices;
    protected AbstractModel(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    @Override
    public List<Vertex> getVertices() {
        return vertices;
    }
}
