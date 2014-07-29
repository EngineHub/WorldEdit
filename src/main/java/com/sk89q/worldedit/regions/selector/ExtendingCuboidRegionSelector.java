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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * Creates a {@code CuboidRegion} from a user's selections by expanding
 * the region on every right click.
 */
public class ExtendingCuboidRegionSelector extends CuboidRegionSelector {

    /**
     * Create a new selector with a {@code null} world.
     */
    public ExtendingCuboidRegionSelector() {
        super((World) null);
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public ExtendingCuboidRegionSelector(@Nullable World world) {
        super(world);
    }

    /**
     * Create a new selector from another one.
     *
     * @param oldSelector the other selector
     */
    public ExtendingCuboidRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);

        if (position1 == null || position2 == null) {
            return;
        }

        position1 = region.getMinimumPoint().toBlockVector();
        position2 = region.getMaximumPoint().toBlockVector();
        region.setPos1(position1);
        region.setPos2(position2);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param position1 the first position
     * @param position2 the second position
     */
    public ExtendingCuboidRegionSelector(@Nullable World world, Vector position1, Vector position2) {
        this(world);
        position1 = Vector.getMinimum(position1, position2);
        position2 = Vector.getMaximum(position1, position2);
        region.setPos1(position1);
        region.setPos2(position2);
    }

    @Override
    public boolean selectPrimary(Vector position, SelectorLimits limits) {
        if (position1 != null && position2 != null && position.compareTo(position1) == 0 && position.compareTo(position2) == 0) {
            return false;
        }

        position1 = position2 = position.toBlockVector();
        region.setPos1(position1);
        region.setPos2(position2);
        return true;
    }

    @Override
    public boolean selectSecondary(Vector position, SelectorLimits limits) {
        if (position1 == null || position2 == null) {
            return selectPrimary(position, limits);
        }

        if (region.contains(position)) {
            return false;
        }

        double x1 = Math.min(position.getX(), position1.getX());
        double y1 = Math.min(position.getY(), position1.getY());
        double z1 = Math.min(position.getZ(), position1.getZ());

        double x2 = Math.max(position.getX(), position2.getX());
        double y2 = Math.max(position.getY(), position2.getY());
        double z2 = Math.max(position.getZ(), position2.getZ());

        final BlockVector o1 = position1;
        final BlockVector o2 = position2;
        position1 = new BlockVector(x1, y1, z1);
        position2 = new BlockVector(x2, y2, z2);
        region.setPos1(position1);
        region.setPos2(position2);

        assert(region.contains(o1));
        assert(region.contains(o2));
        assert(region.contains(position));

        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        player.print("Started selection at " + pos + " (" + region.getArea() + ").");

        explainRegionAdjust(player, session);
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        player.print("Extended selection to encompass " + pos + " (" + region.getArea() + ").");

        explainRegionAdjust(player, session);
    }

}
