package com.sk89q.worldedit.shape;

import com.sk89q.worldedit.Vector;

public class Vertex {
    private final Vector position;
    private final Vector normal;

    public Vertex(Vector position, Vector normal) {
        super();
        this.position = position;
        this.normal = normal.normalize();
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getNormal() {
        return normal;
    }

    @Override
    public String toString() {
        return "(" + getPosition() + ", " + getNormal() + ")";
    }
}
