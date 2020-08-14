/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions.polyhedron;

import com.sk89q.worldedit.math.Vector3;

import static com.google.common.base.Preconditions.checkNotNull;

public class Edge {

    private final Vector3 start;
    private final Vector3 end;

    public Edge(Vector3 start, Vector3 end) {
        checkNotNull(start);
        checkNotNull(end);

        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Edge)) {
            return false;
        }

        Edge otherEdge = (Edge) other;
        if (this.start.equals(otherEdge.end) && this.end.equals(otherEdge.start)) {
            return true;
        }

        return this.end.equals(otherEdge.end) && this.start.equals(otherEdge.start);
    }

    @Override
    public int hashCode() {
        return start.hashCode() ^ end.hashCode();
    }


    @Override
    public String toString() {
        return "(" + this.start + "," + this.end + ")";
    }

    /**
     * Create a triangle from { this.start, this.end, vertex }
     *
     * @param vertex the 3rd vertex for the triangle
     * @return a triangle
     */
    public Triangle createTriangle(Vector3 vertex) {
        checkNotNull(vertex);
        return new Triangle(this.start, this.end, vertex);
    }

    /**
     * Create a triangle from { this.start, vertex, this.end }.
     *
     * @param vertex the second vertex
     * @return a new triangle
     */
    public Triangle createTriangle2(Vector3 vertex) {
        checkNotNull(vertex);
        return new Triangle(this.start, vertex, this.end);
    }

}
