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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.spout.SpoutUtil;
import org.spout.api.geo.World;

import java.util.Collections;
import java.util.List;

public class Polygonal2DSelection extends RegionSelection {

    protected Polygonal2DRegion poly2d;

    public Polygonal2DSelection(World world, RegionSelector sel, Polygonal2DRegion region) {
        super(world, sel, region);
        this.poly2d = region;
    }

    public Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY) {
        super(world);
        LocalWorld lWorld = SpoutUtil.getLocalWorld(world);

        // Validate input
        minY = Math.min(Math.max(0, minY), world.getHeight());
        maxY = Math.min(Math.max(0, maxY), world.getHeight());

        // Create and set up new selector
        Polygonal2DRegionSelector sel = new Polygonal2DRegionSelector(lWorld, points, minY, maxY);

        // set up CuboidSelection
        poly2d = sel.getIncompleteRegion();

        // set up RegionSelection
        setRegionSelector(sel);
        setRegion(poly2d);
    }

    public List<BlockVector2D> getNativePoints() {
        return Collections.unmodifiableList(poly2d.getPoints());
    }
}
