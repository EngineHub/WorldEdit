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
import com.sk89q.worldedit.internal.cui.SelectionEllipsoidPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link RegionSelector} for {@link EllipsoidRegion}s.
 */
public class EllipsoidRegionSelector extends com.sk89q.worldedit.regions.EllipsoidRegionSelector implements RegionSelector, CUIRegion {

    protected EllipsoidRegion region;

    /**
     * Create a new selector.
     */
    public EllipsoidRegionSelector() {
        this((World) null);
    }

    @Deprecated
    public EllipsoidRegionSelector(@Nullable LocalWorld world) {
        this((World) world);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     */
    public EllipsoidRegionSelector(@Nullable World world) {
        region = new EllipsoidRegion(world, new Vector(), new Vector());
    }

    /**
     * Create a new selector from the given selector.
     *
     * @param oldSelector the old selector
     */
    public EllipsoidRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());
        if (oldSelector instanceof EllipsoidRegionSelector) {
            final EllipsoidRegionSelector ellipsoidRegionSelector = (EllipsoidRegionSelector) oldSelector;

            region = new EllipsoidRegion(ellipsoidRegionSelector.getIncompleteRegion());
        } else {
            Region oldRegion = null;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            BlockVector pos1 = oldRegion.getMinimumPoint().toBlockVector();
            BlockVector pos2 = oldRegion.getMaximumPoint().toBlockVector();

            Vector center = pos1.add(pos2).divide(2).floor();
            region.setCenter(center);
            region.setRadius(pos2.subtract(center));
        }
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param center the center
     * @param radius the radius
     */
    public EllipsoidRegionSelector(@Nullable LocalWorld world, Vector center, Vector radius) {
        this(world);

        region.setCenter(center);
        region.setRadius(radius);
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        if (pos.equals(region.getCenter()) && region.getRadius().lengthSq() == 0) {
            return false;
        }

        region.setCenter(pos.toBlockVector());
        region.setRadius(new Vector());
        return true;
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        final Vector diff = pos.subtract(region.getCenter());
        final Vector minRadius = Vector.getMaximum(diff, diff.multiply(-1.0));
        region.extendRadius(minRadius);
        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        if (isDefined()) {
            player.print("Center position set to " + region.getCenter() + " (" + region.getArea() + ").");
        } else {
            player.print("Center position set to " + region.getCenter() + ".");
        }

        session.describeCUI(player);
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        if (isDefined()) {
            player.print("Radius set to " + region.getRadius() + " (" + region.getArea() + ").");
        } else {
            player.print("Radius set to " + region.getRadius() + ".");
        }

        session.describeCUI(player);
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        session.describeCUI(player);
    }

    @Override
    public boolean isDefined() {
        return region.getRadius().lengthSq() > 0;
    }

    @Override
    public EllipsoidRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public EllipsoidRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public void learnChanges() {
    }

    @Override
    public void clear() {
        region.setCenter(new Vector());
        region.setRadius(new Vector());
    }

    @Override
    public String getTypeName() {
        return "ellipsoid";
    }

    @Override
    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<String>();

        final Vector center = region.getCenter();
        if (center.lengthSq() > 0) {
            lines.add("Center: " + center);
        }

        final Vector radius = region.getRadius();
        if (radius.lengthSq() > 0) {
            lines.add("X/Y/Z radius: " + radius);
        }

        return lines;
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(0, region.getCenter()));
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(1, region.getRadius()));
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
        session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String getTypeID() {
        return "ellipsoid";
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        return region.getCenter().toBlockVector();
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
