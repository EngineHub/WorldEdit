/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Art;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

import java.util.UUID;

/**
 * @author zml2008
 */
public class BukkitPainting extends BukkitEntity {
    private final Art art;
    private final BlockFace facingDirection;
    public BukkitPainting(Location loc, Art art, BlockFace facingDirection, UUID entityId) {
        super(loc, EntityType.PAINTING, entityId);
        this.art = art;
        this.facingDirection = facingDirection;
    }

    public boolean spawn(Location weLoc) {
        org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
        Painting paint = loc.getWorld().spawn(loc, Painting.class);
        if (paint != null) {
            paint.setFacingDirection(facingDirection);
            paint.setArt(art);
            return true;
        }
        return false;
    }
}
