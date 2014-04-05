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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link RegionSelector} for {@link Polygonal2DRegion}s.
 */
public class Polygonal2DRegionSelector extends com.sk89q.worldedit.regions.Polygonal2DRegionSelector implements RegionSelector, CUIRegion {

    private int maxPoints;
    private BlockVector pos1;
    private Polygonal2DRegion region;

    /**
     * @deprecated Use {@link #Polygonal2DRegionSelector(LocalWorld, int)}
     */
    @Deprecated
    public Polygonal2DRegionSelector(@Nullable LocalWorld world) {
        this(world, 50);
    }

    @Deprecated
    public Polygonal2DRegionSelector(@Nullable LocalWorld world, int maxPoints) {
        this((World) world, maxPoints);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param maxPoints the maximum number of points
     */
    public Polygonal2DRegionSelector(@Nullable World world, int maxPoints) {
        this.maxPoints = maxPoints;
        region = new Polygonal2DRegion(world);
    }

    /**
     * @deprecated Use {@link #Polygonal2DRegionSelector(RegionSelector, int)}
     */
    @Deprecated
    public Polygonal2DRegionSelector(RegionSelector oldSelector) {
        this(oldSelector, 50);
    }

    /**
     * Create a new selector from another one.
     *
     * @param oldSelector the old selector
     * @param maxPoints the maximum number of points
     */
    public Polygonal2DRegionSelector(RegionSelector oldSelector, int maxPoints) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld(), maxPoints);
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

            final int minY = oldRegion.getMinimumPoint().getBlockY();
            final int maxY = oldRegion.getMaximumPoint().getBlockY();

            List<BlockVector2D> points = oldRegion.polygonize(maxPoints);

            pos1 = points.get(0).toVector(minY).toBlockVector();
            region = new Polygonal2DRegion(oldRegion.getWorld(), points, minY, maxY);
        }
    }

    @Deprecated
    public Polygonal2DRegionSelector(@Nullable LocalWorld world, List<BlockVector2D> points, int minY, int maxY) {
        this((World) world, points, minY, maxY);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param points a list of points
     * @param minY the minimum Y
     * @param maxY the maximum Y
     */
    public Polygonal2DRegionSelector(@Nullable World world, List<BlockVector2D> points, int minY, int maxY) {
        checkNotNull(points);
        
        final BlockVector2D pos2D = points.get(0);
        pos1 = new BlockVector(pos2D.getX(), minY, pos2D.getZ());
        region = new Polygonal2DRegion(world, points, minY, maxY);
    }

    @Override
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

    @Override
    public boolean selectSecondary(Vector pos) {
        if (region.size() > 0) {
            final List<BlockVector2D> points = region.getPoints();

            final BlockVector2D lastPoint = points.get(region.size() - 1);
            if (lastPoint.getBlockX() == pos.getBlockX() && lastPoint.getBlockZ() == pos.getBlockZ()) {
                return false;
            }

            if (maxPoints >= 0 && points.size() > maxPoints) {
                return false;
            }
        }

        region.addPoint(pos);
        region.expandY(pos.getBlockY());

        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        player.print("Starting a new polygon at " + pos + ".");

        session.dispatchCUIEvent(player, new SelectionShapeEvent(getTypeID()));
        session.dispatchCUIEvent(player, new SelectionPoint2DEvent(0, pos, getArea()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY()));
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        player.print("Added point #" + region.size() + " at " + pos + ".");

        session.dispatchCUIEvent(player, new SelectionPoint2DEvent(region.size() - 1, pos, getArea()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY()));
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        session.dispatchCUIEvent(player, new SelectionShapeEvent(getTypeID()));
        describeCUI(session, player);
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (pos1 == null) {
            throw new IncompleteRegionException();
        }

        return pos1;
    }

    @Override
    public Polygonal2DRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public Polygonal2DRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public boolean isDefined() {
        return region.size() > 2;
    }

    @Override
    public void learnChanges() {
        BlockVector2D pt = region.getPoints().get(0);
        pos1 = new BlockVector(pt.getBlockX(), region.getMinimumPoint().getBlockY(), pt.getBlockZ());
    }

    @Override
    public void clear() {
        pos1 = null;
        region = new Polygonal2DRegion(region.getWorld());
    }

    @Override
    public String getTypeName() {
        return "2Dx1D polygon";
    }

    @Override
    public List<String> getInformationLines() {
        return Collections.singletonList("# points: " + region.size());
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    /**
     * Get the number of points.
     *
     * @return the number of points
     */
    @Override
    public int getPointCount() {
        return region.getPoints().size();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        final List<BlockVector2D> points = region.getPoints();
        for (int id = 0; id < points.size(); id++) {
            session.dispatchCUIEvent(player, new SelectionPoint2DEvent(id, points.get(id), getArea()));
        }

        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY()));
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        describeCUI(session, player);
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public String getTypeID() {
        return "polygon2d";
    }

    @Override
    public String getLegacyTypeID() {
        return "polygon2d";
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
