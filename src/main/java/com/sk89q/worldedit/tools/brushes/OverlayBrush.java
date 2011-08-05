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
import java.util.Arrays;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.UnsupportedRegionTypeException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylindricalRegion;
import com.sk89q.worldedit.regions.RegionType;

public class OverlayBrush implements Brush {

    boolean replace = false;
    private static final RegionType[] supportedRegionTypes = {RegionType.CUBOID, RegionType.CYLINDER};
    private RegionType regionType = RegionType.CUBOID;
    private boolean flat = false;
    
    public OverlayBrush() {
    }
    
    public OverlayBrush(RegionType regionType, boolean replace, boolean flat) throws UnsupportedRegionTypeException {
        setRegionType(regionType);
        setReplace(replace);
        setFlat(flat);
    }
    
    public OverlayBrush(RegionType regionType, boolean replace) throws UnsupportedRegionTypeException {
        setRegionType(regionType);
        setReplace(replace);
    }

    public void build(EditSession editSession, Vector pos, Pattern mat, int size)
            throws MaxChangedBlocksException {
        int layer = flat ? pos.getBlockY() : -1;
        if(regionType == RegionType.CYLINDER) {
            CylindricalRegion region = new CylindricalRegion(new Vector(pos.getX(), 0, pos.getZ()), size, 127);
            editSession.overlayBlocks(region, mat, replace, layer);
        } else if(regionType == RegionType.CUBOID) {
            CuboidRegion region = new CuboidRegion(new Vector(pos.getX() - size, 0, pos.getZ() - size),
                    new Vector(pos.getX() + size, 127, pos.getZ() + size));
            editSession.overlayCuboidBlocks(region, mat, replace, layer);
        }        
    }
    public void build(EditSession editSession, Vector pos)
            throws MaxChangedBlocksException {
        
    }

    public RegionType[] supportedRegionTypes() {
        return supportedRegionTypes;
    }

    public boolean supportsRegionType(RegionType regionType) {
        return Arrays.asList(supportedRegionTypes).contains(regionType);
    }

    public RegionType getRegionType() {
        return regionType;
    }

    public void setRegionType(RegionType regionType)
            throws UnsupportedRegionTypeException {
        if (!Arrays.asList(supportedRegionTypes).contains(regionType)) {
            throw new UnsupportedRegionTypeException(regionType.toString()  + " not supported");
        }
        this.regionType = regionType;
    }
    
    public boolean getReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public boolean getFlat() {
        return this.flat;
    }
    
    public void setFlat(boolean flat) {
        this.flat = flat;
    }
}
