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

package com.sk89q.worldedit.tools.brushes;

/**
 * @author Nichts
 */
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.UnsupportedRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylindricalRegion;
import com.sk89q.worldedit.regions.Region;

public class OverlayBrush implements Brush {
    private boolean replace;
    private Class<? extends Region> regionType;
    
    public OverlayBrush (Class<? extends Region> regionType, boolean replace)
        throws UnsupportedRegionException {
        this.replace = replace;
        this.regionType = regionType;
        if (regionType != CylindricalRegion.class &&
            regionType != CuboidRegion.class) {
            throw new UnsupportedRegionException(regionType.getSimpleName()  + " not supported");
        }
    }
    
    public void build(EditSession editSession, Vector pos, Pattern mat, int size)
            throws MaxChangedBlocksException {
        if(regionType == CylindricalRegion.class) {
            CylindricalRegion region = new CylindricalRegion(new Vector(pos.getX(), 0, pos.getZ()), size, 127);
            editSession.overlayBlocks(region, mat, replace);
        } else if(regionType == CuboidRegion.class) {
            CuboidRegion region = new CuboidRegion(new Vector(pos.getX() - size, 0, pos.getZ() - size),
                    new Vector(pos.getX() + size, 127, pos.getZ() + size));
            editSession.overlayCuboidBlocks(region, mat, replace);
        }
    }

}
