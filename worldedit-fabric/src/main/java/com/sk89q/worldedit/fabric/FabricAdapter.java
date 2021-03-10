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

package com.sk89q.worldedit.fabric;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.fabric.internal.FabricTransmogrifier;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FabricAdapter {

    private static @Nullable MinecraftServer server;

    private static MinecraftServer requireServer() {
        return Objects.requireNonNull(server, "No server injected");
    }

    static void setServer(@Nullable MinecraftServer server) {
        FabricAdapter.server = server;
    }

    private FabricAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new FabricWorld(world);
    }

    public static Biome adapt(BiomeType biomeType) {
        return requireServer()
            .getRegistryManager()
            .get(Registry.BIOME_KEY)
            .get(new Identifier(biomeType.getId()));
    }

    public static BiomeType adapt(Biome biome) {
        Identifier id = requireServer().getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
        Objects.requireNonNull(id, "biome is not registered");
        return BiomeTypes.get(id.toString());
    }

    public static Vector3 adapt(Vec3d vector) {
        return Vector3.at(vector.x, vector.y, vector.z);
    }

    public static BlockVector3 adapt(BlockPos pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3d toVec3(BlockVector3 vector) {
        return new Vec3d(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static net.minecraft.util.math.Direction adapt(Direction face) {
        switch (face) {
            case NORTH: return net.minecraft.util.math.Direction.NORTH;
            case SOUTH: return net.minecraft.util.math.Direction.SOUTH;
            case WEST: return net.minecraft.util.math.Direction.WEST;
            case EAST: return net.minecraft.util.math.Direction.EAST;
            case DOWN: return net.minecraft.util.math.Direction.DOWN;
            case UP:
            default:
                return net.minecraft.util.math.Direction.UP;
        }
    }

    public static Direction adaptEnumFacing(@Nullable net.minecraft.util.math.Direction face) {
        if (face == null) {
            return null;
        }
        switch (face) {
            case NORTH: return Direction.NORTH;
            case SOUTH: return Direction.SOUTH;
            case WEST: return Direction.WEST;
            case EAST: return Direction.EAST;
            case DOWN: return Direction.DOWN;
            case UP:
            default:
                return Direction.UP;
        }
    }

    public static BlockPos toBlockPos(BlockVector3 vector) {
        return new BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Adapts property.
     * @deprecated without replacement, use the block adapter methods
     */
    @Deprecated
    public static Property<?> adaptProperty(net.minecraft.state.property.Property<?> property) {
        return FabricTransmogrifier.transmogToWorldEditProperty(property);
    }

    /**
     * Adapts properties.
     * @deprecated without replacement, use the block adapter methods
     */
    @Deprecated
    public static Map<Property<?>, Object> adaptProperties(BlockType block, Map<net.minecraft.state.property.Property<?>, Comparable<?>> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<net.minecraft.state.property.Property<?>, Comparable<?>> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (prop.getKey() instanceof DirectionProperty) {
                value = adaptEnumFacing((net.minecraft.util.math.Direction) value);
            } else if (prop.getKey() instanceof net.minecraft.state.property.EnumProperty) {
                value = ((StringIdentifiable) value).asString();
            }
            props.put(block.getProperty(prop.getKey().getName()), value);
        }
        return props;
    }

    public static net.minecraft.block.BlockState adapt(BlockState blockState) {
        int blockStateId = BlockStateIdAccess.getBlockStateId(blockState);
        if (!BlockStateIdAccess.isValidInternalId(blockStateId)) {
            return FabricTransmogrifier.transmogToMinecraft(blockState);
        }
        return Block.getStateFromRawId(blockStateId);
    }

    public static BlockState adapt(net.minecraft.block.BlockState blockState) {
        int blockStateId = Block.getRawIdFromState(blockState);
        BlockState worldEdit = BlockStateIdAccess.getBlockStateById(blockStateId);
        if (worldEdit == null) {
            return FabricTransmogrifier.transmogToWorldEdit(blockState);
        }
        return worldEdit;
    }

    public static Block adapt(BlockType blockType) {
        return Registry.BLOCK.get(new Identifier(blockType.getId()));
    }

    public static BlockType adapt(Block block) {
        return BlockTypes.get(Registry.BLOCK.getId(block).toString());
    }

    public static Item adapt(ItemType itemType) {
        return Registry.ITEM.get(new Identifier(itemType.getId()));
    }

    public static ItemType adapt(Item item) {
        return ItemTypes.get(Registry.ITEM.getId(item).toString());
    }

    public static ItemStack adapt(BaseItemStack baseItemStack) {
        net.minecraft.nbt.CompoundTag fabricCompound = null;
        if (baseItemStack.getNbt() != null) {
            fabricCompound = NBTConverter.toNative(baseItemStack.getNbt());
        }
        final ItemStack itemStack = new ItemStack(adapt(baseItemStack.getType()), baseItemStack.getAmount());
        itemStack.setTag(fabricCompound);
        return itemStack;
    }

    public static BaseItemStack adapt(ItemStack itemStack) {
        CompoundTag tag = itemStack.toTag(new CompoundTag());
        if (tag.isEmpty()) {
            tag = null;
        } else {
            final Tag tagTag = tag.get("tag");
            if (tagTag instanceof CompoundTag) {
                tag = ((CompoundTag) tagTag);
            } else {
                tag = null;
            }
        }
        CompoundTag finalTag = tag;
        return new BaseItemStack(
            adapt(itemStack.getItem()),
            finalTag == null ? null : LazyReference.from(() -> NBTConverter.fromNative(finalTag)),
            itemStack.getCount());
    }

    /**
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public static FabricPlayer adaptPlayer(ServerPlayerEntity player) {
        checkNotNull(player);
        return new FabricPlayer(player);
    }
}
