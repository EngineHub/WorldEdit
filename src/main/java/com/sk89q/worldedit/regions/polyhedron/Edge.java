// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.regions.polyhedron;

import com.sk89q.worldedit.Vector;

public class Edge {
    private final Vector start;
    private final Vector end;

    public Edge(Vector start, Vector end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Edge)) {
            return false;
        }

        Edge otherEdge = (Edge) other;
        if ((this.start == otherEdge.end) && (this.end == otherEdge.start)) {
            return true;
        }

        if ((this.end == otherEdge.end) && (this.start == otherEdge.start)) {
            return true;
        }

        return false;
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
     * Generates a triangle from { this.start, this.end, vertex }
     * 
     * @param vertex The 3rd vertex for the triangle
     * @return a triangle
     */
    public Triangle createTriangle(Vector vertex) {
        return new Triangle(this.start, this.end, vertex);
    }
    public Triangle createTriangle2(Vector vertex) {
        return new Triangle(this.start, vertex, this.end);
    }
}
