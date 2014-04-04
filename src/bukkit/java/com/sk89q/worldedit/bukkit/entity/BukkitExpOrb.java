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

package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

import java.util.UUID;

/**
 * @author zml2008
 */
public class BukkitExpOrb extends BukkitEntity {
    private final int amount;

    public BukkitExpOrb(Location loc, UUID entityId, int amount) {
        super(loc, EntityType.EXPERIENCE_ORB, entityId);
        this.amount = amount;
    }

    @Override
    public boolean spawn(Location weLoc) {
        org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
        ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
        if (orb != null) {
            orb.setExperience(amount);
            return true;
        }
        return false;
    }
}
