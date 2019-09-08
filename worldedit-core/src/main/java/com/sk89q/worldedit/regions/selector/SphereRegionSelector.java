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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * Creates a {@code SphereRegion} from a user's selections.
 */
public class SphereRegionSelector extends EllipsoidRegionSelector {

    /**
     * Create a new selector with a {@code null world}.
     */
    public SphereRegionSelector() {
        super();
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public SphereRegionSelector(@Nullable World world) {
        super(world);
    }

    /**
     * Create a new selector from another one
     *
     * @param oldSelector the old selector
     */
    public SphereRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);
        final Vector3 radius = region.getRadius();
        final double radiusScalar = Math.max(Math.max(radius.getX(), radius.getY()), radius.getZ());
        region.setRadius(Vector3.at(radiusScalar, radiusScalar, radiusScalar));
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param center the center position
     * @param radius the radius
     */
    public SphereRegionSelector(@Nullable World world, BlockVector3 center, int radius) {
        super(world, center, Vector3.at(radius, radius, radius));
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        if (!started) {
            return false;
        }

        final double radiusScalar = Math.ceil(position.toVector3().distance(region.getCenter()));
        region.setRadius(Vector3.at(radiusScalar, radiusScalar, radiusScalar));

        return true;
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        if (isDefined()) {
            player.print("Radius set to " + region.getRadius().getX() + " (" + region.getArea() + ").");
        } else {
            player.print("Radius set to " + region.getRadius().getX() + ".");
        }

        session.describeCUI(player);
    }

    @Override
    public String getTypeName() {
        return "sphere";
    }

}
