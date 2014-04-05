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

package com.sk89q.worldedit.regions.selector;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link RegionSelector} for {@link ConvexPolyhedralRegion}s.
 */
public class ConvexPolyhedralRegionSelector extends com.sk89q.worldedit.regions.ConvexPolyhedralRegionSelector implements RegionSelector, CUIRegion {

    private int maxVertices;
    private final ConvexPolyhedralRegion region;
    private BlockVector pos1;

    @Deprecated
    public ConvexPolyhedralRegionSelector(@Nullable LocalWorld world, int maxVertices) {
        this((World) world, maxVertices);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param maxVertices the maximum number of vertices, where a number below 0 means unbounded
     */
    public ConvexPolyhedralRegionSelector(@Nullable World world, int maxVertices) {
        this.maxVertices = maxVertices;
        region = new ConvexPolyhedralRegion(world);
    }

    /**
     * Create a new selector.
     *
     * @param oldSelector the old selector
     * @param maxVertices the maximum number of vertices, where a number below 0 means unbounded
     */
    public ConvexPolyhedralRegionSelector(RegionSelector oldSelector, int maxVertices) {
        checkNotNull(oldSelector);

        this.maxVertices = maxVertices;
        if (oldSelector instanceof ConvexPolyhedralRegionSelector) {
            final ConvexPolyhedralRegionSelector convexPolyhedralRegionSelector = (ConvexPolyhedralRegionSelector) oldSelector;

            pos1 = convexPolyhedralRegionSelector.pos1;
            region = new ConvexPolyhedralRegion(convexPolyhedralRegionSelector.region);
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                region = new ConvexPolyhedralRegion(oldSelector.getIncompleteRegion().getWorld());
                return;
            }

            final int minY = oldRegion.getMinimumPoint().getBlockY();
            final int maxY = oldRegion.getMaximumPoint().getBlockY();

            region = new ConvexPolyhedralRegion(oldRegion.getWorld());

            for (final BlockVector2D pt : new ArrayList<BlockVector2D>(oldRegion.polygonize(maxVertices < 0 ? maxVertices : maxVertices / 2))) {
                region.addVertex(pt.toVector(minY));
                region.addVertex(pt.toVector(maxY));
            }

            learnChanges();
        }
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        clear();
        pos1 = pos.toBlockVector();
        return region.addVertex(pos);
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        if (maxVertices >= 0 && region.getVertices().size() > maxVertices) {
            return false;
        }

        return region.addVertex(pos);
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        return pos1;
    }

    @Override
    public Region getRegion() throws IncompleteRegionException {
        if (!region.isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public Region getIncompleteRegion() {
        return region;
    }

    @Override
    public boolean isDefined() {
        return region.isDefined();
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    @Override
    public void learnChanges() {
        pos1 = region.getVertices().iterator().next().toBlockVector();
    }

    @Override
    public void clear() {
        region.clear();
    }

    @Override
    public String getTypeName() {
        return "Convex Polyhedron";
    }

    @Override
    public List<String> getInformationLines() {
        List<String> ret = new ArrayList<String>();

        ret.add("Vertices: "+region.getVertices().size());
        ret.add("Triangles: "+region.getTriangles().size());

        return ret;
    }


    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        session.describeCUI(player);

        player.print("Started new selection with vertex "+pos+".");
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        session.describeCUI(player);

        player.print("Added vertex "+pos+" to the selection.");
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        session.describeCUI(player);
    }


    @Override
    public int getProtocolVersion() {
        return 3;
    }

    @Override
    public String getTypeID() {
        return "polyhedron";
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        Collection<Vector> vertices = region.getVertices();
        Collection<Triangle> triangles = region.getTriangles();

        player.dispatchCUIEvent(new SelectionShapeEvent(getTypeID()));

        Map<Vector, Integer> vertexIds = new HashMap<Vector, Integer>(vertices.size());
        int lastVertexId = -1;
        for (Vector vertex : vertices) {
            vertexIds.put(vertex, ++lastVertexId);
            session.dispatchCUIEvent(player, new SelectionPointEvent(lastVertexId, vertex, getArea()));
        }

        for (Triangle triangle : triangles) {
            final int[] v = new int[3];
            for (int i = 0; i < 3; ++i) {
                v[i] = vertexIds.get(triangle.getVertex(i));
            }
            session.dispatchCUIEvent(player, new SelectionPolygonEvent(v));
        }
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        if (isDefined()) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
        } else {
            session.dispatchCUIEvent(player, new SelectionShapeEvent(getLegacyTypeID()));
        }
    }

    @Override
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector position) {
        explainPrimarySelection((Actor) player, session, position);
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector position) {
        explainSecondarySelection((Actor) player, session, position);
    }

    @Override
    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        explainRegionAdjust((Actor) player, session);
    }

}
