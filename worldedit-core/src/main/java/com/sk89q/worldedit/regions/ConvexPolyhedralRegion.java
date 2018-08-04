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

package com.sk89q.worldedit.regions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.polyhedron.Edge;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class ConvexPolyhedralRegion extends AbstractRegion {

    /**
     * Vertices that are contained in the convex hull.
     */
    private final Set<Vector> vertices = new LinkedHashSet<>();

    /**
     * Triangles that form the convex hull.
     */
    private final List<Triangle> triangles = new ArrayList<>();

    /**
     * Vertices that are coplanar to the first 3 vertices.
     */
    private final Set<Vector> vertexBacklog = new LinkedHashSet<>();

    /**
     * Minimum point of the axis-aligned bounding box.
     */
    private Vector minimumPoint;

    /**
     * Maximum point of the axis-aligned bounding box.
     */
    private Vector maximumPoint;

    /**
     * Accumulator for the barycenter of the polyhedron. Divide by vertices.size() to get the actual center.
     */
    private Vector centerAccum = Vector.ZERO;

    /**
     * The last triangle that caused a {@link #contains(Vector)} to classify a point as "outside". Used for optimization.
     */
    private Triangle lastTriangle;

    /**
     * Constructs an empty mesh, containing no vertices or triangles.
     *
     * @param world the world
     */
    public ConvexPolyhedralRegion(@Nullable World world) {
        super(world);
    }

    /**
     * Constructs an independent copy of the given region.
     *
     * @param region the region to copy
     */
    public ConvexPolyhedralRegion(ConvexPolyhedralRegion region) {
        this(region.world);
        vertices.addAll(region.vertices);
        triangles.addAll(region.triangles);
        vertexBacklog.addAll(region.vertexBacklog);

        minimumPoint = region.minimumPoint;
        maximumPoint = region.maximumPoint;
        centerAccum = region.centerAccum;
        lastTriangle = region.lastTriangle;
    }

    /**
     * Clears the region, removing all vertices and triangles.
     */
    public void clear() {
        vertices.clear();
        triangles.clear();
        vertexBacklog.clear();

        minimumPoint = null;
        maximumPoint = null;
        centerAccum = Vector.ZERO;
        lastTriangle = null;
    }

    /**
     * Add a vertex to the region.
     *
     * @param vertex the vertex
     * @return true, if something changed.
     */
    public boolean addVertex(Vector vertex) {
        checkNotNull(vertex);

        lastTriangle = null; // Probably not necessary

        if (vertices.contains(vertex)) {
            return false;
        }

        if (vertices.size() == 3) {
            if (vertexBacklog.contains(vertex)) {
                return false;
            }

            if (containsRaw(vertex)) {
                return vertexBacklog.add(vertex);
            }
        }

        vertices.add(vertex);

        centerAccum = centerAccum.add(vertex);

        if (minimumPoint == null) {
            minimumPoint = maximumPoint = vertex;
        } else {
            minimumPoint = Vector.getMinimum(minimumPoint, vertex);
            maximumPoint = Vector.getMaximum(maximumPoint, vertex);
        }


        switch (vertices.size()) {
        case 0:
        case 1:
        case 2:
            // Incomplete, can't make a mesh yet
            return true;

        case 3:
            // Generate minimal mesh to start from
            final Vector[] v = vertices.toArray(new Vector[vertices.size()]);

            triangles.add((new Triangle(v[0], v[1], v[2])));
            triangles.add((new Triangle(v[0], v[2], v[1])));
            return true;
        }

        // Look for triangles that face the vertex and remove them
        final Set<Edge> borderEdges = new LinkedHashSet<>();
        for (Iterator<Triangle> it = triangles.iterator(); it.hasNext(); ) {
            final Triangle triangle = it.next();

            // If the triangle can't be seen, it's not relevant
            if (!triangle.above(vertex)) {
                continue;
            }

            // Remove the triangle from the mesh
            it.remove();

            // ...and remember its edges
            for (int i = 0; i < 3; ++i) {
                final Edge edge = triangle.getEdge(i);
                if (borderEdges.remove(edge)) {
                    continue;
                }

                borderEdges.add(edge);
            }
        }

        // Add triangles between the remembered edges and the new vertex.
        for (Edge edge : borderEdges) {
            triangles.add(edge.createTriangle(vertex));
        }

        if (!vertexBacklog.isEmpty()) {
            // Remove the new vertex 
            vertices.remove(vertex);

            // Clone, clear and work through the backlog
            final List<Vector> vertexBacklog2 = new ArrayList<>(vertexBacklog);
            vertexBacklog.clear();
            for (Vector vertex2 : vertexBacklog2) {
                addVertex(vertex2);
            }

            // Re-add the new vertex after the backlog.
            vertices.add(vertex);
        }

        return true;
    }

    public boolean isDefined() {
        return !triangles.isEmpty();
    }

    @Override
    public Vector getMinimumPoint() {
        return minimumPoint;
    }

    @Override
    public Vector getMaximumPoint() {
        return maximumPoint;
    }
    
    @Override
    public Vector getCenter() {
        return centerAccum.divide(vertices.size());
    }

    @Override
    public void expand(Vector... changes) throws RegionOperationException {
    }

    @Override
    public void contract(Vector... changes) throws RegionOperationException {
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        shiftCollection(vertices, change);
        shiftCollection(vertexBacklog, change);

        for (int i = 0; i < triangles.size(); ++i) {
            final Triangle triangle = triangles.get(i);

            final Vector v0 = change.add(triangle.getVertex(0));
            final Vector v1 = change.add(triangle.getVertex(1));
            final Vector v2 = change.add(triangle.getVertex(2));

            triangles.set(i, new Triangle(v0, v1, v2));
        }

        minimumPoint = change.add(minimumPoint);
        maximumPoint = change.add(maximumPoint);
        centerAccum = change.multiply(vertices.size()).add(centerAccum);
        lastTriangle = null;
    }

    private static void shiftCollection(Collection<Vector> collection, Vector change) {
        final List<Vector> tmp = new ArrayList<>(collection);
        collection.clear();
        for (Vector vertex : tmp) {
            collection.add(change.add(vertex));
        }
    }

    @Override
    public boolean contains(Vector position) {
        if (!isDefined()) {
            return false;
        }

        final int x = position.getBlockX();
        final int y = position.getBlockY();
        final int z = position.getBlockZ();

        final Vector min = getMinimumPoint();
        final Vector max = getMaximumPoint();

        if (x < min.getBlockX()) return false;
        if (x > max.getBlockX()) return false;
        if (y < min.getBlockY()) return false;
        if (y > max.getBlockY()) return false;
        if (z < min.getBlockZ()) return false;
        if (z > max.getBlockZ()) return false;

        return containsRaw(position);
    }

    private boolean containsRaw(Vector pt) {
        if (lastTriangle != null && lastTriangle.above(pt)) {
            return false;
        }

        for (Triangle triangle : triangles) {
            if (lastTriangle == triangle) {
                continue;
            }

            if (triangle.above(pt)) {
                lastTriangle = triangle;
                return false;
            }
        }

        return true;
    }

    public Collection<Vector> getVertices() {
        if (vertexBacklog.isEmpty()) {
            return vertices;
        }

        final List<Vector> ret = new ArrayList<>(vertices);
        ret.addAll(vertexBacklog);

        return ret;
    }

    public Collection<Triangle> getTriangles() {
        return triangles;
    }

    @Override
    public AbstractRegion clone() {
        return new ConvexPolyhedralRegion(this);
    }
}
