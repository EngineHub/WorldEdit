// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

/**
 * A mask that only matches blocks inside a given region.
 */
public class RegionMask implements Mask {

    private final Region region;

    /**
     * Create a new region mask with the given region.
     * 
     * @param region the region
     */
    public RegionMask(Region region) {
        this.region = region.clone();
    }

    /**
     * Get the region.
     * 
     * @return the region
     */
    public Region getRegion() {
        return region;
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return region.contains(pos);
    }

    @Override
    public String toString() {
        return String.format("RegionMask(region=%s)", region);
    }

}
