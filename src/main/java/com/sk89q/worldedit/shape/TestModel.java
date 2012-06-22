package com.sk89q.worldedit.shape;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.Vector;

public class TestModel implements Model {
    List<Vertex> vertices = new ArrayList<Vertex>();

    public TestModel() {
        for (double phi = 0; phi < 360; phi += 15) {
            for (double theta = -90; theta < 90; theta += 15) {
                double x = Math.cos(theta) * Math.cos(phi);
                double y = Math.cos(theta) * Math.sin(phi);
                double z = Math.sin(theta);

                Vector pos = new Vector(x, y, z);
                vertices.add(new Vertex(pos , pos));
            }
        }
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Vector getMinimumPoint() {
        return new Vector(-1,-1,-1);
    }

    @Override
    public Vector getMaximumPoint() {
        return new Vector(1,1,1);
    }
}
