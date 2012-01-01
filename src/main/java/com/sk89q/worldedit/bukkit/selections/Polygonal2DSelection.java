// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.bukkit.selections;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import org.bukkit.World;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.*;

public class Polygonal2DSelection extends RegionSelection {

    protected Polygonal2DRegion poly2d;

    public Polygonal2DSelection(World world, RegionSelector sel, Polygonal2DRegion region) {
        super(world, sel, region);
        this.poly2d = region;
    }

    public Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY) {
        super(world);

        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Incomplete or null points list is not permitted");
        }

        LocalWorld lWorld = BukkitUtil.getLocalWorld(world);
        minY = Math.min(Math.max(0, minY), world.getMaxHeight());
        maxY = Math.min(Math.max(0, maxY), world.getMaxHeight());

        Polygonal2DRegionSelector sel = new Polygonal2DRegionSelector(lWorld);
        poly2d = new Polygonal2DRegion(lWorld, points, minY, maxY);

        Iterator<BlockVector2D> it = points.iterator();
        BlockVector2D pt = it.next();
        sel.selectPrimary(new BlockVector(pt.getBlockX(), minY, pt.getBlockZ()));

        do {
            pt = it.next();
            sel.selectSecondary(new BlockVector(pt.getBlockX(), maxY, pt.getBlockZ()));
        } while (it.hasNext());

        setRegionSelector(sel);
        setRegion(poly2d);
    }

    public List<BlockVector2D> getNativePoints() {
        return Collections.unmodifiableList(poly2d.getPoints());
    }
}
