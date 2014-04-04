/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions.polyhedron;

import com.sk89q.worldedit.Vector;

public class Triangle {
    private final Vector[] vertices;
    private final Vector normal;
    private final double b;

    /**
     * Constructs a triangle with the given vertices (counter-clockwise)
     *
     * @param v0
     * @param v1
     * @param v2
     */
    public Triangle(Vector v0, Vector v1, Vector v2) {
        vertices = new Vector[] { v0, v1, v2 };

        this.normal = v1.subtract(v0).cross(v2.subtract(v0)).normalize();
        this.b = Math.max(Math.max(normal.dot(v0), normal.dot(v1)), normal.dot(v2));
    }

    /**
     * Returns the triangle's vertex with the given index, counter-clockwise.
     *
     * @param index Vertex index. Valid input: 0..2
     * @return a vertex
     */
    public Vector getVertex(int index) {
        return vertices[index];
    }

    /**
     * Returns the triangle's edge with the given index, counter-clockwise.
     *
     * @param index Edge index. Valid input: 0..2
     * @return an edge
     */
    public Edge getEdge(int index) {
        if (index == vertices.length - 1) {
            return new Edge(vertices[index], vertices[0]);
        }
        return new Edge(vertices[index], vertices[index + 1]);
    }

    /**
     * Returns whether the given point is above the plane the triangle is in.
     *
     * @param pt
     * @return
     */
    public boolean below(Vector pt) {
        return normal.dot(pt) < b;
    }

    /**
     * Returns whether the given point is above the plane the triangle is in.
     *
     * @param pt
     * @return
     */
    public boolean above(Vector pt) {
        return normal.dot(pt) > b;
    }

    @Override
    public String toString() {
        return tag+"(" + this.vertices[0] + "," + this.vertices[1] + "," + this.vertices[2] + ")";
    }
    String tag = "Triangle";
    public Triangle tag(String tag) {
        this.tag = tag;
        return this;
    }
}
