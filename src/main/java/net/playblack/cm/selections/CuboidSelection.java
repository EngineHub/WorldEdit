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

package net.playblack.cm.selections;

import net.canarymod.api.world.World;
import net.canarymod.api.world.position.Location;
import net.playblack.cm.CanaryUtil;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;

public class CuboidSelection extends RegionSelection {

    protected CuboidRegion cuboid;

    public CuboidSelection(World world, Location pt1, Location pt2) {
        this(world, CanaryUtil.toVector(pt1), CanaryUtil.toVector(pt2));
    }

    public CuboidSelection(World world, Vector pt1, Vector pt2) {
        super(world);

        // Validate input
        if (pt1 == null) {
            throw new IllegalArgumentException("Null point 1 not permitted");
        }

        if (pt2 == null) {
            throw new IllegalArgumentException("Null point 2 not permitted");
        }

        // Create new selector
        CuboidRegionSelector sel = new CuboidRegionSelector(CanaryUtil.getLocalWorld(world));

        // set up selector
        sel.selectPrimary(pt1);
        sel.selectSecondary(pt2);

        // set up CuboidSelection
        cuboid = sel.getIncompleteRegion();

        // set up RegionSelection
        setRegionSelector(sel);
        setRegion(cuboid);
    }

    public CuboidSelection(World world, RegionSelector sel, CuboidRegion region) {
        super(world, sel, region);
        this.cuboid = region;
    }
}
