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

package com.sk89q.worldedit.regions;

import java.util.ArrayList;
import java.util.List;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

/**
 * Selector for polygonal regions.
 *
 * @author sk89q
 */
public class Polygonal2DRegionSelector implements RegionSelector {
    protected BlockVector pos1;
    protected Polygonal2DRegion region;
    
    public Polygonal2DRegionSelector(LocalWorld world) {
        region = new Polygonal2DRegion(world);
    }
    
    public boolean selectPrimary(Vector pos) {
        if (pos1 != null && pos1.equals(pos)) {
            return false;
        }
        pos1 = pos.toBlockVector();
        region = new Polygonal2DRegion(region.getWorld());
        region.addPoint(pos);
        region.expandY(pos.getBlockY());
        return true;
    }
    
    public boolean selectSecondary(Vector pos) {
        if (region.size() > 0) {
            List<BlockVector2D> points = region.getPoints();
            BlockVector2D lastPoint = points.get(region.size() - 1);
            if (lastPoint.getBlockX() == pos.getBlockX()
                    && lastPoint.getBlockZ() == pos.getBlockZ()) {
                return false;
            }
            
            if (points.size() >= 20) {
                return false;
            }
        }
        
        region.addPoint(pos);
        region.expandY(pos.getBlockY());
        return true;
    }

    public void explainPrimarySelection(LocalPlayer player,
            LocalSession session, Vector pos) {
        player.print("Starting a new polygon at " + pos + ".");
    }

    public void explainSecondarySelection(LocalPlayer player,
            LocalSession session, Vector pos) {
        player.print("Added point #" + region.size() + " at " + pos + ".");
    }

    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
    }

    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (pos1 == null) {
            throw new IncompleteRegionException();
        }
        return pos1;
    }

    public Polygonal2DRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }
        
        return region;
    }

    public Polygonal2DRegion getIncompleteRegion() {
        return region;
    }

    public boolean isDefined() {
        return region.size() > 2;
    }

    public void learnChanges() {
        BlockVector2D pt = region.getPoints().get(0);
        pos1 = new BlockVector(pt.getBlockX(),
                region.getMinimumPoint().getBlockY(), pt.getBlockZ());
    }

    public void clear() {
        pos1 = null;
        region = new Polygonal2DRegion(region.getWorld());
    }

    public String getTypeName() {
        return "2Dx1D polygon";
    }

    public List<String> getInformationLines() {
        List<String> lines = new ArrayList<String>();
        lines.add("# points: " + region.size());
        return lines;
    }

    public String getTypeId() {
        return "polygon2d";
    }

    public int getArea() {
        return region.getArea();
    }
    
    public int getPointCount() {
        return region.getPoints().size();
    }

}
