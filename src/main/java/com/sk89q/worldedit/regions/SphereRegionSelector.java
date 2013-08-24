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

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

/**
 * Selector for spheres.
 *
 * @author TomyLobo
 */
public class SphereRegionSelector extends EllipsoidRegionSelector {
    public SphereRegionSelector(LocalWorld world) {
        super(world);
    }

    public SphereRegionSelector() {
        super();
    }

    public SphereRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);
        final Vector radius = region.getRadius();
        final double radiusScalar = Math.max(Math.max(radius.getX(), radius.getY()), radius.getZ());
        region.setRadius(new Vector(radiusScalar, radiusScalar, radiusScalar));
    }

    public SphereRegionSelector(LocalWorld world, Vector center, int radius) {
        super(world, center, new Vector(radius, radius, radius));
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        final double radiusScalar = Math.ceil(pos.distance(region.getCenter()));
        region.setRadius(new Vector(radiusScalar, radiusScalar, radiusScalar));

        return true;
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
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
