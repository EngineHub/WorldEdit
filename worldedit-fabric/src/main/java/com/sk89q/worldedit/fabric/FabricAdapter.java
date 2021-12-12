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

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.fabric.internal.FabricTransmogrifier;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FabricAdapter {

    private FabricAdapter() {
    }

    public static World adapt(net.minecraft.world.level.Level world) {
        return new FabricWorld(world);
    }

    /**
     * Create a Fabric world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Fabric world
     */
    public static net.minecraft.world.level.Level adapt(World world) {
        checkNotNull(world);
        if (world instanceof FabricWorld) {
            return ((FabricWorld) world).getWorld();
        } else {
            // TODO introduce a better cross-platform world API to match more easily
            throw new UnsupportedOperationException("Cannot adapt from a " + world.getClass());
        }
    }

    public static Biome adapt(BiomeType biomeType) {
        return FabricWorldEdit.LIFECYCLED_SERVER.valueOrThrow()
            .registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY)
            .get(new ResourceLocation(biomeType.getId()));
    }

    public static BiomeType adapt(Biome biome) {
        ResourceLocation id = FabricWorldEdit.LIFECYCLED_SERVER.valueOrThrow().registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
        Objects.requireNonNull(id, "biome is not registered");
        return BiomeTypes.get(id.toString());
    }

    public static Vector3 adapt(Vec3 vector) {
        return Vector3.at(vector.x, vector.y, vector.z);
    }

    public static BlockVector3 adapt(BlockPos pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 toVec3(BlockVector3 vector) {
        return new Vec3(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static net.minecraft.core.Direction adapt(Direction face) {
        switch (face) {
            case NORTH:
                return net.minecraft.core.Direction.NORTH;
            case SOUTH:
                return net.minecraft.core.Direction.SOUTH;
            case WEST:
                return net.minecraft.core.Direction.WEST;
            case EAST:
                return net.minecraft.core.Direction.EAST;
            case DOWN:
                return net.minecraft.core.Direction.DOWN;
            case UP:
            default:
                return net.minecraft.core.Direction.UP;
        }
    }

    public static Direction adaptEnumFacing(@Nullable net.minecraft.core.Direction face) {
        if (face == null) {
            return null;
        }
        switch (face) {
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
            case DOWN:
                return Direction.DOWN;
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
     *
     * @deprecated without replacement, use the block adapter methods
     */
    @Deprecated
    public static Property<?> adaptProperty(net.minecraft.world.level.block.state.properties.Property<?> property) {
        return FabricTransmogrifier.transmogToWorldEditProperty(property);
    }

    /**
     * Adapts properties.
     *
     * @deprecated without replacement, use the block adapter methods
     */
    @Deprecated
    public static Map<Property<?>, Object> adaptProperties(BlockType block, Map<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (prop.getKey() instanceof DirectionProperty) {
                value = adaptEnumFacing((net.minecraft.core.Direction) value);
            } else if (prop.getKey() instanceof net.minecraft.world.level.block.state.properties.EnumProperty) {
                value = ((StringRepresentable) value).getSerializedName();
            }
            props.put(block.getProperty(prop.getKey().getName()), value);
        }
        return props;
    }

    public static net.minecraft.world.level.block.state.BlockState adapt(BlockState blockState) {
        int blockStateId = BlockStateIdAccess.getBlockStateId(blockState);
        if (!BlockStateIdAccess.isValidInternalId(blockStateId)) {
            return FabricTransmogrifier.transmogToMinecraft(blockState);
        }
        return Block.stateById(blockStateId);
    }

    public static BlockState adapt(net.minecraft.world.level.block.state.BlockState blockState) {
        int blockStateId = Block.getId(blockState);
        BlockState worldEdit = BlockStateIdAccess.getBlockStateById(blockStateId);
        if (worldEdit == null) {
            return FabricTransmogrifier.transmogToWorldEdit(blockState);
        }
        return worldEdit;
    }

    public static Block adapt(BlockType blockType) {
        return Registry.BLOCK.get(new ResourceLocation(blockType.getId()));
    }

    public static BlockType adapt(Block block) {
        return BlockTypes.get(Registry.BLOCK.getKey(block).toString());
    }

    public static Item adapt(ItemType itemType) {
        return Registry.ITEM.get(new ResourceLocation(itemType.getId()));
    }

    public static ItemType adapt(Item item) {
        return ItemTypes.get(Registry.ITEM.getKey(item).toString());
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
        net.minecraft.nbt.CompoundTag tag = itemStack.save(new net.minecraft.nbt.CompoundTag());
        if (tag.isEmpty()) {
            tag = null;
        } else {
            final net.minecraft.nbt.Tag tagTag = tag.get("tag");
            if (tagTag instanceof net.minecraft.nbt.CompoundTag) {
                tag = ((net.minecraft.nbt.CompoundTag) tagTag);
            } else {
                tag = null;
            }
        }
        net.minecraft.nbt.CompoundTag finalTag = tag;
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
    public static FabricPlayer adaptPlayer(ServerPlayer player) {
        checkNotNull(player);
        return new FabricPlayer(player);
    }
}
