package com.sk89q.worldedit.shape;

import java.util.Arrays;

import com.sk89q.worldedit.Vector;

public class MultiVertex extends Vertex {
    private final Vector[] normals;
    public MultiVertex(Vector position, Vector... normals) {
        super(position, average(normals));

        this.normals = normals;
    }

    private static Vector average(Vector... normal) {
        return new Vector().add(normal).divide(normal.length);
    }

    public MultiVertex addNormal(Vector normal) {
        Vector[] newNormals = Arrays.copyOf(normals, normals.length+1);
        newNormals[normals.length] = normal;

        return new MultiVertex(getPosition(), newNormals);
    }
}
