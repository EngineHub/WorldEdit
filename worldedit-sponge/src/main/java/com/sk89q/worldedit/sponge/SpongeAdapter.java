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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.BooleanStateProperty;
import org.spongepowered.api.state.EnumStateProperty;
import org.spongepowered.api.state.IntegerStateProperty;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Sponge and WorldEdit equivalent objects.
 */
public class SpongeAdapter {

    private SpongeAdapter() {
    }

    /**
     * Create a WorldEdit world from a Sponge world.
     *
     * @param world the Sponge world
     * @return a WorldEdit world
     */
    public static World adapt(ServerWorld world) {
        checkNotNull(world);

        return new SpongeWorld(world);
    }

    /**
     * Create a WorldEdit Player from a Sponge Player.
     *
     * @param player The Sponge player
     * @return The WorldEdit player
     */
    public static SpongePlayer adapt(ServerPlayer player) {
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
    public static ServerWorld adapt(World world) {
        checkNotNull(world);
        if (world instanceof SpongeWorld) {
            return ((SpongeWorld) world).getWorld();
        } else {
            ServerWorld match = Sponge.getServer().getWorldManager().world(ResourceKey.resolve(world.getName())).orElse(null);
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Sponge world for " + world);
            }
        }
    }

    public static ItemType adapt(org.spongepowered.api.item.ItemType itemType) {
        return ItemTypes.get(itemType.key(RegistryTypes.ITEM_TYPE).getFormatted());
    }

    public static org.spongepowered.api.item.ItemType adapt(ItemType itemType) {
        return RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(itemType.getId())).orElse(null);
    }

    public static BlockType adapt(org.spongepowered.api.block.BlockType blockType) {
        return BlockTypes.get(blockType.key(RegistryTypes.BLOCK_TYPE).getFormatted());
    }

    public static org.spongepowered.api.block.BlockType adapt(BlockType blockType) {
        return RegistryTypes.BLOCK_TYPE.get().findValue(ResourceKey.resolve(blockType.getId())).orElse(null);
    }

    public static WeatherType adapt(org.spongepowered.api.world.weather.WeatherType weatherType) {
        return WeatherTypes.get(weatherType.key(RegistryTypes.WEATHER_TYPE).getFormatted());
    }

    public static org.spongepowered.api.world.weather.WeatherType adapt(WeatherType weatherType) {
        return RegistryTypes.WEATHER_TYPE.get().findValue(ResourceKey.resolve(weatherType.getId())).orElse(null);
    }

    public static BiomeType adapt(Biome biomeType, RegistryHolder registryHolder) {
        return BiomeTypes.get(RegistryTypes.BIOME.keyFor(registryHolder, biomeType).getFormatted());
    }

    public static Biome adapt(BiomeType biomeType, RegistryHolder registryHolder) {
        return RegistryTypes.BIOME.referenced(ResourceKey.resolve(biomeType.getId())).get(registryHolder);
    }

    /**
     * Create a WorldEdit location from a Sponge location.
     *
     * @param location the Sponge location
     * @return a WorldEdit location
     */
    public static Location adapt(ServerLocation location, Vector3d rotation) {
        checkNotNull(location);
        Vector3 position = asVector(location);
        return new Location(
                adapt(location.getWorld()),
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
    public static ServerLocation adapt(Location location) {
        checkNotNull(location);
        Vector3 position = location.toVector();
        return ServerLocation.of(
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
    public static Vector3 asVector(ServerLocation location) {
        checkNotNull(location);
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit BlockVector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    public static BlockVector3 asBlockVector(ServerLocation location) {
        checkNotNull(location);
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit BlockVector3 from a Sponge Vector3i.
     *
     * @param vec The Sponge Vector3i
     * @return The WorldEdit BlockVector3
     */
    public static BlockVector3 adapt(Vector3i vec) {
        return BlockVector3.at(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Create a Sponge Vector3i from a WorldEdit BlockVector3.
     *
     * @param vec The WorldEdit BlockVector3
     * @return The Sponge Vector3i
     */
    public static Vector3i adapt(BlockVector3 vec) {
        return new Vector3i(vec.getX(), vec.getY(), vec.getZ());
    }

    public static com.sk89q.worldedit.util.Direction adapt(Direction direction) {
        if (direction == null) {
            return null;
        }
        switch (direction) {
            case NORTH: return com.sk89q.worldedit.util.Direction.NORTH;
            case SOUTH: return com.sk89q.worldedit.util.Direction.SOUTH;
            case WEST: return com.sk89q.worldedit.util.Direction.WEST;
            case EAST: return com.sk89q.worldedit.util.Direction.EAST;
            case DOWN: return com.sk89q.worldedit.util.Direction.DOWN;
            case UP:
            default:
                return com.sk89q.worldedit.util.Direction.UP;
        }
    }

    @SuppressWarnings("unchecked")
    public static Property<?> adaptProperty(StateProperty<?> property) {
        if (property instanceof BooleanStateProperty) {
            return new BooleanProperty(property.getName(), ImmutableList.copyOf(((BooleanStateProperty) property).getPossibleValues()));
        }
        if (property instanceof IntegerStateProperty) {
            return new IntegerProperty(property.getName(), ImmutableList.copyOf(((IntegerStateProperty) property).getPossibleValues()));
        }
        if (property instanceof EnumStateProperty) {
            if (property.getPossibleValues().stream().anyMatch(ent -> ent instanceof Direction)) {
                return new DirectionalProperty(property.getName(), ((EnumStateProperty<Direction>) property).getPossibleValues()
                    .stream()
                    .map(SpongeAdapter::adapt)
                    .collect(Collectors.toList()));
            } else {
                return new EnumProperty(property.getName(), ((EnumStateProperty<?>) property).getPossibleValues().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList()));
            }
        }
        return new SpongePropertyAdapter<>(property);
    }

    /**
     * Create a WorldEdit BlockState from a Sponge BlockState.
     *
     * @param blockState The Sponge BlockState
     * @return The WorldEdit BlockState
     */
    public static BlockState adapt(org.spongepowered.api.block.BlockState blockState) {
        checkNotNull(blockState);

        return BlockStateIdAccess.getBlockStateById(PaletteTypes.BLOCK_STATE_PALETTE.get().create(Sponge.getGame().registries(), RegistryTypes.BLOCK_TYPE).get(blockState).getAsInt());
    }

    private static final Int2ObjectMap<org.spongepowered.api.block.BlockState> spongeBlockStateCache = new Int2ObjectOpenHashMap<>();

    /**
     * Create a Sponge BlockState from a WorldEdit BlockStateHolder.
     *
     * @param block The WorldEdit BlockStateHolder
     * @return The Sponge BlockState
     */
    public static <B extends BlockStateHolder<B>> org.spongepowered.api.block.BlockState adapt(B block) {
        checkNotNull(block);
        // Should never not have an ID for this BlockState.
        int cacheKey = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        if (cacheKey == BlockStateIdAccess.invalidId()) {
            cacheKey = block.hashCode();
        }

        return spongeBlockStateCache.computeIfAbsent(cacheKey, input -> PaletteTypes.BLOCK_STATE_PALETTE.get().create(Sponge.getGame().registries(), RegistryTypes.BLOCK_TYPE).get(input, Sponge.getGame().registries()).orElse(null));
    }

}
