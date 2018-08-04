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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Creates a {@code Polygonal2DRegion} from a user's selections.
 */
public class Polygonal2DRegionSelector implements RegionSelector, CUIRegion {

    private transient BlockVector pos1;
    private transient Polygonal2DRegion region;

    /**
     * Create a new selector with a {@code null} world.
     */
    public Polygonal2DRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new selector with the given world.
     *
     * @param world the world
     */
    public Polygonal2DRegionSelector(@Nullable World world) {
        region = new Polygonal2DRegion(world);
    }

    /**
     * Create a new selector from another one.
     *
     * @param oldSelector the old selector
     */
    public Polygonal2DRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());

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

            List<BlockVector2D> points = oldRegion.polygonize(Integer.MAX_VALUE);

            pos1 = points.get(0).toVector(minY).toBlockVector();
            region = new Polygonal2DRegion(oldRegion.getWorld(), points, minY, maxY);
        }
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

    @Nullable
    @Override
    public World getWorld() {
        return region.getWorld();
    }

    @Override
    public void setWorld(@Nullable World world) {
        region.setWorld(world);
    }

    @Override
    public boolean selectPrimary(Vector position, SelectorLimits limits) {
        if (position.equals(pos1)) {
            return false;
        }

        pos1 = position.toBlockVector();
        region = new Polygonal2DRegion(region.getWorld());
        region.addPoint(position);
        region.expandY(position.getBlockY());

        return true;
    }

    @Override
    public boolean selectSecondary(Vector position, SelectorLimits limits) {
        if (region.size() > 0) {
            final List<BlockVector2D> points = region.getPoints();

            final BlockVector2D lastPoint = points.get(region.size() - 1);
            if (lastPoint.getBlockX() == position.getBlockX() && lastPoint.getBlockZ() == position.getBlockZ()) {
                return false;
            }

            Optional<Integer> vertexLimit = limits.getPolygonVertexLimit();

            if (vertexLimit.isPresent() && points.size() > vertexLimit.get()) {
                return false;
            }
        }

        region.addPoint(position);
        region.expandY(position.getBlockY());

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

}
