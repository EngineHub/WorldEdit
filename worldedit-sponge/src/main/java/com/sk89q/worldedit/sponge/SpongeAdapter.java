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

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.sponge.internal.NbtAdapter;
import com.sk89q.worldedit.sponge.internal.SpongeTransmogrifier;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.minecraft.world.level.block.Block;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Sponge and WorldEdit equivalent objects.
 */
public class SpongeAdapter {

    public static org.spongepowered.api.block.BlockState adapt(BlockState blockState) {
        int blockStateId = BlockStateIdAccess.getBlockStateId(blockState);
        if (!BlockStateIdAccess.isValidInternalId(blockStateId)) {
            return SpongeTransmogrifier.transmogToMinecraft(blockState);
        }
        return (org.spongepowered.api.block.BlockState) Block.stateById(blockStateId);
    }

    public static BlockState adapt(org.spongepowered.api.block.BlockState blockState) {
        int blockStateId = Block.getId((net.minecraft.world.level.block.state.BlockState) blockState);
        BlockState worldEdit = BlockStateIdAccess.getBlockStateById(blockStateId);
        if (worldEdit == null) {
            return SpongeTransmogrifier.transmogToWorldEdit(blockState);
        }
        return worldEdit;
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
        Objects.requireNonNull(player);
        return new SpongePlayer(player);
    }

    /**
     * Create a Sponge Player from a WorldEdit Player.
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
            // Currently this is 99% certain to fail, we don't have consistent world name/id mapping
            ServerWorld match = Sponge.server().worldManager().world(
                ResourceKey.resolve(world.getName())
            ).orElse(null);
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Sponge world for " + world);
            }
        }
    }

    public static RegistryReference<Biome> adapt(BiomeType biomeType) {
        return RegistryKey.of(RegistryTypes.BIOME, ResourceKey.resolve(biomeType.id()))
            .asReference();
    }

    public static BiomeType adapt(Biome biomeType) {
        return BiomeType.REGISTRY.get(biomeType.toString());
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
            adapt(location.world()),
            position,
            (float) rotation.y(),
            (float) rotation.x()
        );
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
            position.x(), position.y(), position.z()
        );
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
     * Create a WorldEdit Vector from a Sponge location.
     *
     * @param location The Sponge location
     * @return a WorldEdit vector
     */
    public static Vector3 asVector(ServerLocation location) {
        checkNotNull(location);
        return Vector3.at(location.x(), location.y(), location.z());
    }

    /**
     * Create a WorldEdit BlockVector from a Sponge location.
     *
     * @param location The Sponge location
     * @return a WorldEdit vector
     */
    public static BlockVector3 asBlockVector(ServerLocation location) {
        checkNotNull(location);
        return BlockVector3.at(location.x(), location.y(), location.z());
    }

    public static BaseItemStack adapt(ItemStack itemStack) {
        DataView tag = itemStack.toContainer().getView(Constants.Sponge.UNSAFE_NBT)
            .orElse(null);
        return new BaseItemStack(
            ItemTypes.get(itemStack.type().key(RegistryTypes.ITEM_TYPE).asString()),
            tag == null ? null : LazyReference.from(() -> NbtAdapter.adaptToWorldEdit(tag)),
            itemStack.quantity()
        );
    }

    public static ItemStack adapt(BaseItemStack itemStack) {
        ItemStack stack = ItemStack.builder()
            .itemType(() -> Sponge.game().registry(RegistryTypes.ITEM_TYPE)
                .value(ResourceKey.resolve(itemStack.getType().id())))
            .quantity(itemStack.getAmount())
            .build();
        LinCompoundTag nbt = itemStack.getNbt();
        if (nbt != null) {
            stack.setRawData(
                DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED)
                    .set(Constants.Sponge.UNSAFE_NBT, NbtAdapter.adaptFromWorldEdit(nbt))
            );
        }
        return stack;
    }

    public static Direction adapt(org.spongepowered.api.util.Direction direction) {
        return Direction.valueOf(direction.name());
    }

    public static Vector3i adaptVector3i(BlockVector3 bv3) {
        return new Vector3i(bv3.x(), bv3.y(), bv3.z());
    }

    public static BlockVector3 adaptVector3i(Vector3i vec3i) {
        return BlockVector3.at(vec3i.x(), vec3i.y(), vec3i.z());
    }

    private SpongeAdapter() {
    }

}
