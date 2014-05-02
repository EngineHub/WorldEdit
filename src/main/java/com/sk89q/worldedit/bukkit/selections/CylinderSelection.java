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

package com.sk89q.worldedit.bukkit.selections;

import com.sk89q.worldedit.regions.selector.CylinderRegionSelector;
import org.bukkit.World;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.RegionSelector;

/**
 * A selection representing a {@link CylinderRegion}
 */
public class CylinderSelection extends RegionSelection {

    private CylinderRegion cylRegion;

    public CylinderSelection(World world, RegionSelector selector, CylinderRegion region) {
        super(world, selector, region);
        this.cylRegion = region;
    }
    
    public CylinderSelection(World world, BlockVector2D center, BlockVector2D radius, int minY, int maxY) {
        super(world);
        LocalWorld lWorld = BukkitUtil.getLocalWorld(world);

        // Validate input
        minY = Math.min(Math.max(0, minY), world.getMaxHeight());
        maxY = Math.min(Math.max(0, maxY), world.getMaxHeight());

        // Create and set up new selector
        CylinderRegionSelector sel = new CylinderRegionSelector(lWorld, center, radius, minY, maxY);

        // set up selection
        cylRegion = sel.getIncompleteRegion();

        // set up RegionSelection
        setRegionSelector(sel);
        setRegion(cylRegion);
    }

    /**
     * Returns the center vector of the cylinder
     * 
     * @return the center
     */
    public BlockVector2D getCenter() {
        return cylRegion.getCenter().toVector2D().toBlockVector2D();
    }

    /**
     * Returns the radius vector of the cylinder
     * 
     * @return the radius
     */
    public BlockVector2D getRadius() {
        return cylRegion.getRadius().toBlockVector2D();
    }

}
