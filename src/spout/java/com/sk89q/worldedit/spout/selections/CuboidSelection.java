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

package com.sk89q.worldedit.spout.selections;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.spout.SpoutUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;

public class CuboidSelection extends RegionSelection {

    protected CuboidRegion cuboid;

    public CuboidSelection(World world, Point pt1, Point pt2) {
        this(world, SpoutUtil.toVector(pt1), SpoutUtil.toVector(pt2));
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
        CuboidRegionSelector sel = new CuboidRegionSelector(SpoutUtil.getLocalWorld(world));

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
