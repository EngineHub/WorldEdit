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

package com.sk89q.worldedit.bukkit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Adapts between Bukkit and WorldEdit equivalent objects.
 */
public class BukkitAdapter {

    private BukkitAdapter() {
    }

    private static final ParserContext TO_BLOCK_CONTEXT = new ParserContext();

    static {
        TO_BLOCK_CONTEXT.setRestricted(false);
    }

    /**
     * Checks equality between a WorldEdit BlockType and a Bukkit Material
     *
     * @param blockType The WorldEdit BlockType
     * @param type The Bukkit Material
     * @return If they are equal
     */
    public static boolean equals(BlockType blockType, Material type) {
        return Objects.equals(blockType.getId(), type.getKey().toString());
    }

    /**
     * Convert any WorldEdit world into an equivalent wrapped Bukkit world.
     *
     * <p>If a matching world cannot be found, a {@link RuntimeException}
     * will be thrown.</p>
     *
     * @param world the world
     * @return a wrapped Bukkit world
     */
    public static BukkitWorld asBukkitWorld(World world) {
        if (world instanceof BukkitWorld) {
            return (BukkitWorld) world;
        } else {
            BukkitWorld bukkitWorld = WorldEditPlugin.getInstance().getInternalPlatform().matchWorld(world);
            if (bukkitWorld == null) {
                throw new RuntimeException("World '" + world.getName() + "' has no matching version in Bukkit");
            }
            return bukkitWorld;
        }
    }

    /**
     * Create a WorldEdit world from a Bukkit world.
     *
     * @param world the Bukkit world
     * @return a WorldEdit world
     */
    public static World adapt(org.bukkit.World world) {
        checkNotNull(world);
        return new BukkitWorld(world);
    }

    /**
     * Create a WorldEdit Player from a Bukkit Player.
     *
     * @param player The Bukkit player
     * @return The WorldEdit player
     */
    public static BukkitPlayer adapt(Player player) {
        return WorldEditPlugin.getInstance().wrapPlayer(player);
    }

    /**
     * Create a Bukkit Player from a WorldEdit Player.
     *
     * @param player The WorldEdit player
     * @return The Bukkit player
     */
    public static Player adapt(com.sk89q.worldedit.entity.Player player) {
        return ((BukkitPlayer) player).getPlayer();
    }

    /**
     * Create a Bukkit world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Bukkit world
     */
    public static org.bukkit.World adapt(World world) {
        checkNotNull(world);
        if (world instanceof BukkitWorld) {
            return ((BukkitWorld) world).getWorld();
        } else {
            org.bukkit.World match = Bukkit.getServer().getWorld(world.getName());
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Bukkit world for " + world);
            }
        }
    }

    /**
     * Create a WorldEdit location from a Bukkit location.
     *
     * @param location the Bukkit location
     * @return a WorldEdit location
     */
    public static Location adapt(org.bukkit.Location location) {
        checkNotNull(location);
        Vector3 position = asVector(location);
        return new com.sk89q.worldedit.util.Location(
                adapt(location.getWorld()),
                position,
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a Bukkit location from a WorldEdit location.
     *
     * @param location the WorldEdit location
     * @return a Bukkit location
     */
    public static org.bukkit.Location adapt(Location location) {
        checkNotNull(location);
        Vector3 position = location.toVector();
        return new org.bukkit.Location(
                adapt((World) location.getExtent()),
                position.getX(), position.getY(), position.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a Bukkit location from a WorldEdit position with a Bukkit world.
     *
     * @param world the Bukkit world
     * @param position the WorldEdit position
     * @return a Bukkit location
     */
    public static org.bukkit.Location adapt(org.bukkit.World world, Vector3 position) {
        checkNotNull(world);
        checkNotNull(position);
        return new org.bukkit.Location(
                world,
                position.getX(), position.getY(), position.getZ());
    }

    /**
     * Create a Bukkit location from a WorldEdit position with a Bukkit world.
     *
     * @param world the Bukkit world
     * @param position the WorldEdit position
     * @return a Bukkit location
     */
    public static org.bukkit.Location adapt(org.bukkit.World world, BlockVector3 position) {
        checkNotNull(world);
        checkNotNull(position);
        return new org.bukkit.Location(
                world,
                position.getX(), position.getY(), position.getZ());
    }

    /**
     * Create a Bukkit location from a WorldEdit location with a Bukkit world.
     *
     * @param world the Bukkit world
     * @param location the WorldEdit location
     * @return a Bukkit location
     */
    public static org.bukkit.Location adapt(org.bukkit.World world, Location location) {
        checkNotNull(world);
        checkNotNull(location);
        return new org.bukkit.Location(
                world,
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a WorldEdit Vector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    public static Vector3 asVector(org.bukkit.Location location) {
        checkNotNull(location);
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit BlockVector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    public static BlockVector3 asBlockVector(org.bukkit.Location location) {
        checkNotNull(location);
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit entity from a Bukkit entity.
     *
     * @param entity the Bukkit entity
     * @return a WorldEdit entity
     */
    public static Entity adapt(org.bukkit.entity.Entity entity) {
        checkNotNull(entity);
        return new BukkitEntity(entity);
    }

    /**
     * Create a Bukkit Material form a WorldEdit ItemType
     *
     * @param itemType The WorldEdit ItemType
     * @return The Bukkit Material
     */
    public static Material adapt(ItemType itemType) {
        checkNotNull(itemType);
        if (!itemType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports Minecraft items");
        }
        return Material.getMaterial(itemType.getId().substring(10).toUpperCase());
    }

    /**
     * Create a Bukkit Material form a WorldEdit BlockType
     *
     * @param blockType The WorldEdit BlockType
     * @return The Bukkit Material
     */
    public static Material adapt(BlockType blockType) {
        checkNotNull(blockType);
        if (!blockType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports Minecraft blocks");
        }
        return Material.getMaterial(blockType.getId().substring(10).toUpperCase());
    }

    /**
     * Create a WorldEdit GameMode from a Bukkit one.
     *
     * @param gameMode Bukkit GameMode
     * @return WorldEdit GameMode
     */
    public static GameMode adapt(org.bukkit.GameMode gameMode) {
        checkNotNull(gameMode);
        return GameModes.get(gameMode.name().toLowerCase());
    }

    /**
     * Create a WorldEdit BiomeType from a Bukkit one.
     *
     * @param biome Bukkit Biome
     * @return WorldEdit BiomeType
     */
    public static BiomeType adapt(Biome biome) {
        return BiomeTypes.get(biome.name().toLowerCase());
    }

    public static Biome adapt(BiomeType biomeType) {
        if (!biomeType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports vanilla biomes");
        }
        try {
            return Biome.valueOf(biomeType.getId().substring(10).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Create a WorldEdit EntityType from a Bukkit one.
     *
     * @param entityType Bukkit EntityType
     * @return WorldEdit EntityType
     */
    public static EntityType adapt(org.bukkit.entity.EntityType entityType) {
        return EntityTypes.get(entityType.name().toLowerCase());
    }

    public static org.bukkit.entity.EntityType adapt(EntityType entityType) {
        if (!entityType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports vanilla entities");
        }
        return org.bukkit.entity.EntityType.fromName(entityType.getId().substring(10));
    }

    /**
     * Converts a Material to a BlockType
     *
     * @param material The material
     * @return The blocktype
     */
    public static BlockType asBlockType(Material material) {
        checkNotNull(material);
        if (!material.isBlock()) {
            throw new IllegalArgumentException(material.getKey().toString() + " is not a block!");
        }
        return BlockTypes.get(material.getKey().toString());
    }

    /**
     * Converts a Material to a ItemType
     *
     * @param material The material
     * @return The itemtype
     */
    public static ItemType asItemType(Material material) {
        checkNotNull(material);
        if (!material.isItem()) {
            throw new IllegalArgumentException(material.getKey().toString() + " is not an item!");
        }
        return ItemTypes.get(material.getKey().toString());
    }

    private static Map<String, BlockState> blockStateCache = new HashMap<>();

    /**
     * Create a WorldEdit BlockState from a Bukkit BlockData
     *
     * @param blockData The Bukkit BlockData
     * @return The WorldEdit BlockState
     */
    public static BlockState adapt(BlockData blockData) {
        checkNotNull(blockData);
        return blockStateCache.computeIfAbsent(blockData.getAsString(), new Function<String, BlockState>() {
            @Nullable
            @Override
            public BlockState apply(@Nullable String input) {
                try {
                    return WorldEdit.getInstance().getBlockFactory().parseFromInput(input, TO_BLOCK_CONTEXT).toImmutableState();
                } catch (InputParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    private static Map<String, BlockData> blockDataCache = new HashMap<>();

    /**
     * Create a Bukkit BlockData from a WorldEdit BlockStateHolder
     *
     * @param block The WorldEdit BlockStateHolder
     * @return The Bukkit BlockData
     */
    public static <B extends BlockStateHolder<B>> BlockData adapt(B block) {
        checkNotNull(block);
        return blockDataCache.computeIfAbsent(block.getAsString(), new Function<String, BlockData>() {
            @Nullable
            @Override
            public BlockData apply(@Nullable String input) {
                return Bukkit.createBlockData(block.getAsString());
            }
        }).clone();
    }

    /**
     * Create a WorldEdit BlockState from a Bukkit ItemStack
     *
     * @param itemStack The Bukkit ItemStack
     * @return The WorldEdit BlockState
     */
    public static BlockState asBlockState(ItemStack itemStack) throws WorldEditException {
        checkNotNull(itemStack);
        if (itemStack.getType().isBlock()) {
            return adapt(itemStack.getType().createBlockData());
        } else {
            throw new NotABlockException();
        }
    }

    /**
     * Create a WorldEdit BaseItemStack from a Bukkit ItemStack
     *
     * @param itemStack The Bukkit ItemStack
     * @return The WorldEdit BaseItemStack
     */
    public static BaseItemStack adapt(ItemStack itemStack) {
        checkNotNull(itemStack);
        return new BaseItemStack(ItemTypes.get(itemStack.getType().getKey().toString()), itemStack.getAmount());
    }

    /**
     * Create a Bukkit ItemStack from a WorldEdit BaseItemStack
     *
     * @param item The WorldEdit BaseItemStack
     * @return The Bukkit ItemStack
     */
    public static ItemStack adapt(BaseItemStack item) {
        checkNotNull(item);
        return new ItemStack(adapt(item.getType()), item.getAmount());
    }
}
