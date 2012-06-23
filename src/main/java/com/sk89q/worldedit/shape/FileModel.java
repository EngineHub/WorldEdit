package com.sk89q.worldedit.shape;

import java.util.List;

public abstract class FileModel implements Model {
    private final List<Vertex> vertices;
    protected FileModel(String filename) {
        vertices = load();
    }

    protected abstract List<Vertex> load();

    @Override
    public List<Vertex> getVertices() {
        return vertices;
    }
}
