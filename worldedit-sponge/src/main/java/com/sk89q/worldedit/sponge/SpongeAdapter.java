/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Sponge and WorldEdit equivalent objects.
 */
public class SpongeAdapter {

    private SpongeAdapter() {
    }

    /**
     * Create a WorldEdit world from a Sponge extent.
     *
     * @param world the Sponge extent
     * @return a WorldEdit world
     */
    public static World checkWorld(org.spongepowered.api.world.extent.Extent world) {
        checkNotNull(world);
        if (world instanceof org.spongepowered.api.world.World) {
            return adapt((org.spongepowered.api.world.World) world);
        } else {
            throw new IllegalArgumentException("Extent type is not a world");
        }
    }

    /**
     * Create a WorldEdit world from a Sponge world.
     *
     * @param world the Sponge world
     * @return a WorldEdit world
     */
    public static World adapt(org.spongepowered.api.world.World world) {
        checkNotNull(world);
        return SpongeWorldEdit.inst().getWorld(world);
    }

    /**
     * Create a WorldEdit Player from a Sponge Player.
     *
     * @param player The Sponge player
     * @return The WorldEdit player
     */
    public static SpongePlayer adapt(Player player) {
        return SpongeWorldEdit.inst().wrapPlayer(player);
    }

    /**
     * Create a Bukkit Player from a WorldEdit Player.
     *
     * @param player The WorldEdit player
     * @return The Bukkit player
     */
    public static Player adapt(com.sk89q.worldedit.entity.Player player) {
        return ((SpongePlayer) player).getPlayer();
    }

    /**
     * Create a Sponge world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Sponge world
     */
    public static org.spongepowered.api.world.World adapt(World world) {
        checkNotNull(world);
        if (world instanceof SpongeWorld) {
            return ((SpongeWorld) world).getWorld();
        } else {
            org.spongepowered.api.world.World match = Sponge.getServer().getWorld(world.getName()).orElse(null);
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Sponge world for " + world);
            }
        }
    }

    public static BiomeType adapt(org.spongepowered.api.world.biome.BiomeType biomeType) {
        return BiomeTypes.get(biomeType.getId());
    }

    public static org.spongepowered.api.world.biome.BiomeType adapt(BiomeType biomeType) {
        return Sponge.getRegistry().getType(org.spongepowered.api.world.biome.BiomeType.class, biomeType.getId()).orElse(null);
    }

    /**
     * Create a WorldEdit location from a Sponge location.
     *
     * @param location the Sponge location
     * @return a WorldEdit location
     */
    public static Location adapt(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location, Vector3d rotation) {
        checkNotNull(location);
        Vector3 position = asVector(location);
        return new Location(
                adapt(location.getExtent()),
                position,
                (float) rotation.getX(),
                (float) rotation.getY());
    }

    /**
     * Create a Sponge location from a WorldEdit location.
     *
     * @param location the WorldEdit location
     * @return a Sponge location
     */
    public static org.spongepowered.api.world.Location<org.spongepowered.api.world.World> adapt(Location location) {
        checkNotNull(location);
        Vector3 position = location.toVector();
        return new org.spongepowered.api.world.Location<>(
                adapt((World) location.getExtent()),
                position.getX(), position.getY(), position.getZ());
    }

    /**
     * Create a Sponge rotation from a WorldEdit location.
     *
     * @param location the WorldEdit location
     * @return a Sponge rotation
     */
    public static Vector3d adaptRotation(Location location) {
        checkNotNull(location);
        return new Vector3d(location.getPitch(), location.getYaw(), 0);
    }

    /**
     * Create a WorldEdit Vector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    public static Vector3 asVector(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        checkNotNull(location);
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit BlockVector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    public static BlockVector3 asBlockVector(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        checkNotNull(location);
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }
}
