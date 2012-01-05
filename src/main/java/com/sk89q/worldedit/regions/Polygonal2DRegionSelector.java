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
import java.util.Collections;
import java.util.List;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;

/**
 * Selector for polygonal regions.
 *
 * @author sk89q
 */
public class Polygonal2DRegionSelector implements RegionSelector, CUIRegion {
    private BlockVector pos1;
    private Polygonal2DRegion region;

    public Polygonal2DRegionSelector(LocalWorld world) {
        region = new Polygonal2DRegion(world);
    }

    public Polygonal2DRegionSelector(RegionSelector oldSelector) {
        this(oldSelector.getIncompleteRegion().getWorld());
        if (oldSelector instanceof Polygonal2DRegionSelector) {
            final Polygonal2DRegionSelector polygonal2DRegionSelector = (Polygonal2DRegionSelector) oldSelector;

            pos1 = polygonal2DRegionSelector.pos1;
            region = new Polygonal2DRegion(polygonal2DRegionSelector.region);
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            BlockVector min = oldRegion.getMinimumPoint().toBlockVector();
            BlockVector max = oldRegion.getMaximumPoint().toBlockVector();

            int minY = min.getBlockY();
            int maxY = max.getBlockY();

            List<BlockVector2D> points = new ArrayList<BlockVector2D>(4);

            points.add(new BlockVector2D(min.getX(), min.getZ()));
            points.add(new BlockVector2D(min.getX(), max.getZ()));
            points.add(new BlockVector2D(max.getX(), max.getZ()));
            points.add(new BlockVector2D(max.getX(), min.getZ()));

            pos1 = min;
            region = new Polygonal2DRegion(oldRegion.getWorld(), points, minY, maxY);
        }
    }

    public Polygonal2DRegionSelector(LocalWorld world, List<BlockVector2D> points, int minY, int maxY) {
        final BlockVector2D pos2D = points.get(0);
        pos1 = new BlockVector(pos2D.getX(), minY, pos2D.getZ());
        region = new Polygonal2DRegion(world, points, minY, maxY);
    }

    public boolean selectPrimary(Vector pos) {
        if (pos.equals(pos1)) {
            return false;
        }

        pos1 = pos.toBlockVector();
        region = new Polygonal2DRegion(region.getWorld());
        region.addPoint(pos);
        region.expandY(pos.getBlockY());

        return true;
    }

    public boolean selectSecondary(Vector pos) {
        if (region.size() > 0) {
            final List<BlockVector2D> points = region.getPoints();

            final BlockVector2D lastPoint = points.get(region.size() - 1);
            if (lastPoint.getBlockX() == pos.getBlockX() && lastPoint.getBlockZ() == pos.getBlockZ()) {
                return false;
            }

            if (points.size() >= 20) {
                return false;
            }
        }

        region.addPoint(pos);
        region.expandY(pos.getBlockY());

        return true;
    }

    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Starting a new polygon at " + pos + ".");

        session.dispatchCUIEvent(player, new SelectionShapeEvent(getTypeID()));
        session.dispatchCUIEvent(player, new SelectionPoint2DEvent(0, pos, getArea()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMininumY(), region.getMaximumY()));
    }

    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Added point #" + region.size() + " at " + pos + ".");

        session.dispatchCUIEvent(player, new SelectionPoint2DEvent(region.size() - 1, pos, getArea()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMininumY(), region.getMaximumY()));
    }

    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMininumY(), region.getMaximumY()));
    }

    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (pos1 == null) {
            throw new IncompleteRegionException();
        }

        return pos1;
    }

    public Polygonal2DRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    public Polygonal2DRegion getIncompleteRegion() {
        return region;
    }

    public boolean isDefined() {
        return region.size() > 2;
    }

    public void learnChanges() {
        BlockVector2D pt = region.getPoints().get(0);
        pos1 = new BlockVector(pt.getBlockX(), region.getMinimumPoint().getBlockY(), pt.getBlockZ());
    }

    public void clear() {
        pos1 = null;
        region = new Polygonal2DRegion(region.getWorld());
    }

    public String getTypeName() {
        return "2Dx1D polygon";
    }

    public List<String> getInformationLines() {
        return Collections.singletonList("# points: " + region.size());
    }

    public int getArea() {
        return region.getArea();
    }

    public int getPointCount() {
        return region.getPoints().size();
    }

    public void describeCUI(LocalSession session, LocalPlayer player) {
        final List<BlockVector2D> points = region.getPoints();
        for (int id = 0; id < points.size(); id++) {
            session.dispatchCUIEvent(player, new SelectionPoint2DEvent(id, points.get(id), getArea()));
        }

        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMininumY(), region.getMaximumY()));
    }

    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
        describeCUI(session, player);
    }

    public int getProtocolVersion() {
        return 0;
    }

    public String getTypeID() {
        return "polygon2d";
    }

    public String getLegacyTypeID() {
        return "polygon2d";
    }
}
