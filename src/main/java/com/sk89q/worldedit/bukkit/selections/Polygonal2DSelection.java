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
import java.util.List;
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

        minY = Math.min(Math.max(0, minY), world.getMaxHeight());
        maxY = Math.min(Math.max(0, maxY), world.getMaxHeight());

        Polygonal2DRegionSelector sel = new Polygonal2DRegionSelector(BukkitUtil.getLocalWorld(world));
        poly2d = sel.getIncompleteRegion();
        
        for (BlockVector2D pt : points) {
            if (pt == null) {
                throw new IllegalArgumentException("Null point not permitted");
            }
            
            poly2d.addPoint(pt);
        }
        
        poly2d.setMinimumY(minY);
        poly2d.setMaximumY(maxY);
        
        sel.learnChanges();

        setRegionSelector(sel);
        setRegion(poly2d);
    }
    
    public List<BlockVector2D> getNativePoints() {
        return Collections.unmodifiableList(poly2d.getPoints());
    }
}
