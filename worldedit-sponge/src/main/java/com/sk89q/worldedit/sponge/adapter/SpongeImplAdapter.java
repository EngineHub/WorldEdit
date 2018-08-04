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

package com.sk89q.worldedit.sponge.adapter;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.util.Location;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

/**
 * An interface for various things that can't be done through the Sponge API.
 */
public interface SpongeImplAdapter {

    /**
     * Resolves the numerical ID from this {@link BiomeType}
     *
     * @param type The biometype
     * @return The numerical ID
     */
    int resolve(BiomeType type);

    BiomeType resolveBiome(int intID);

    BaseEntity createBaseEntity(Entity entity);

    ItemStack makeSpongeStack(BaseItemStack itemStack);

    SpongeWorld getWorld(World world);

    default boolean isBest() {
        return true;
    }

    default Location adapt(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> loc, Vector3d rot) {
        Vector position = new Vector(loc.getX(), loc.getY(), loc.getZ());

        return new Location(getWorld(loc.getExtent()), position, (float) rot.getY(), (float) rot.getX());
    }
}
