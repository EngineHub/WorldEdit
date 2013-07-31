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

package com.sk89q.worldedit.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import com.sk89q.worldedit.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.polyhedron.Triangle;

public class ConvexPolyhedralRegionSelector implements RegionSelector, CUIRegion {
    private int maxVertices;
    private final ConvexPolyhedralRegion region;
    private BlockVector pos1;

    public ConvexPolyhedralRegionSelector(LocalWorld world, int maxVertices) {
        this.maxVertices = maxVertices;
        region = new ConvexPolyhedralRegion(world);
    }

    public ConvexPolyhedralRegionSelector(RegionSelector oldSelector, int maxVertices) {
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
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        session.describeCUI(player);

        player.print("Started new selection with vertex "+pos+".");
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        session.describeCUI(player);

        player.print("Added vertex "+pos+" to the selection.");
    }

    @Override
    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
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
    public void describeCUI(LocalSession session, LocalPlayer player) {
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
    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
        if (isDefined()) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
        } else {
            session.dispatchCUIEvent(player, new SelectionShapeEvent(getLegacyTypeID()));
        }
    }
}
