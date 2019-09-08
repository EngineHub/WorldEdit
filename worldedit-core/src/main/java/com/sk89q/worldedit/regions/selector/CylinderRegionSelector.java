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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionCylinderEvent;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Creates a {@code CylinderRegionSelector} from a user's selections.
 */
public class CylinderRegionSelector implements RegionSelector, CUIRegion {

    protected static transient final NumberFormat NUMBER_FORMAT;
    protected transient CylinderRegion region;

    static {
        NUMBER_FORMAT = (NumberFormat) NumberFormat.getInstance().clone();
        NUMBER_FORMAT.setMaximumFractionDigits(3);
    }

    /**
     * Create a new region selector with a {@code null} world.
     */
    public CylinderRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new region selector.
     *
     * @param world the world, which may be {@code null}
     */
    public CylinderRegionSelector(@Nullable World world) {
        region = new CylinderRegion(world);
    }

    /**
     * Create a new selector from the given one.
     *
     * @param oldSelector the old selector
     */
    public CylinderRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());

        if (oldSelector instanceof CylinderRegionSelector) {
            final CylinderRegionSelector cylSelector = (CylinderRegionSelector) oldSelector;

            region = new CylinderRegion(cylSelector.region);
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            BlockVector3 pos1 = oldRegion.getMinimumPoint();
            BlockVector3 pos2 = oldRegion.getMaximumPoint();

            BlockVector3 center = pos1.add(pos2).divide(2).floor();
            region.setCenter(center.toBlockVector2());
            region.setRadius(pos2.toBlockVector2().subtract(center.toBlockVector2()).toVector2());

            region.setMaximumY(Math.max(pos1.getBlockY(), pos2.getBlockY()));
            region.setMinimumY(Math.min(pos1.getBlockY(), pos2.getBlockY()));
        }
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param center the center
     * @param radius the radius
     * @param minY the minimum Y
     * @param maxY the maximum Y
     */
    public CylinderRegionSelector(@Nullable World world, BlockVector2 center, Vector2 radius, int minY, int maxY) {
        this(world);

        region.setCenter(center);
        region.setRadius(radius);

        region.setMinimumY(Math.min(minY, maxY));
        region.setMaximumY(Math.max(minY, maxY));
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
    public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
        if (!region.getCenter().equals(Vector3.ZERO) && position.equals(region.getCenter().toBlockPoint())) {
            return false;
        }

        region = new CylinderRegion(region.getWorld());
        region.setCenter(position.toBlockVector2());
        region.setY(position.getBlockY());

        return true;
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        Vector3 center = region.getCenter();
        if (center.equals(Vector3.ZERO)) {
            return true;
        }

        final Vector2 diff = position.toVector3().subtract(center).toVector2();
        final Vector2 minRadius = diff.getMaximum(diff.multiply(-1.0));
        region.extendRadius(minRadius);

        region.setY(position.getBlockY());

        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        player.print("Starting a new cylindrical selection at " + pos + ".");

        session.describeCUI(player);
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        Vector3 center = region.getCenter();

        if (!center.equals(Vector3.ZERO)) {
            player.print("Radius set to " + NUMBER_FORMAT.format(region.getRadius().getX()) + "/" + NUMBER_FORMAT.format(region.getRadius().getZ()) + " blocks. (" + region.getArea() + ").");
        } else {
            player.printError("You must select the center point before setting the radius.");
            return;
        }

        session.describeCUI(player);
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        session.describeCUI(player);
    }

    @Override
    public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region.getCenter().toBlockPoint();
    }

    @Override
    public CylinderRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public CylinderRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public boolean isDefined() {
        return !region.getRadius().equals(Vector2.ZERO);
    }

    @Override
    public void learnChanges() {
    }

    @Override
    public void clear() {
        region = new CylinderRegion(region.getWorld());
    }

    @Override
    public String getTypeName() {
        return "Cylinder";
    }

    @Override
    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<>();

        if (!region.getCenter().equals(Vector3.ZERO)) {
            lines.add("Center: " + region.getCenter());
        }
        if (!region.getRadius().equals(Vector2.ZERO)) {
            lines.add("Radius: " + region.getRadius());
        }

        return lines;
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        session.dispatchCUIEvent(player, new SelectionCylinderEvent(region.getCenter().toBlockPoint(), region.getRadius()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY()));
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        if (isDefined()) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
        }
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String getTypeID() {
        return "cylinder";
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

}
