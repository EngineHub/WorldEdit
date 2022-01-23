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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.forge.internal.ForgeTransmogrifier;
import com.sk89q.worldedit.forge.internal.NBTConverter;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ForgeAdapter {

    private ForgeAdapter() {
    }

    public static World adapt(ServerLevel world) {
        return new ForgeWorld(world);
    }

    /**
     * Create a Forge world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Forge world
     */
    public static ServerLevel adapt(World world) {
        checkNotNull(world);
        if (world instanceof ForgeWorld) {
            return ((ForgeWorld) world).getWorld();
        } else {
            // TODO introduce a better cross-platform world API to match more easily
            throw new UnsupportedOperationException("Cannot adapt from a " + world.getClass());
        }
    }

    public static Biome adapt(BiomeType biomeType) {
        return ServerLifecycleHooks.getCurrentServer()
            .registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY)
            .getOptional(new ResourceLocation(biomeType.getId()))
            .orElseThrow(() -> new IllegalStateException("No biome for " + biomeType.getId()));
    }

    public static BiomeType adapt(Biome biome) {
        ResourceLocation id = ServerLifecycleHooks.getCurrentServer()
            .registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY)
            .getKey(biome);
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
            case NORTH: return net.minecraft.core.Direction.NORTH;
            case SOUTH: return net.minecraft.core.Direction.SOUTH;
            case WEST: return net.minecraft.core.Direction.WEST;
            case EAST: return net.minecraft.core.Direction.EAST;
            case DOWN: return net.minecraft.core.Direction.DOWN;
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
    public static Property<?> adaptProperty(net.minecraft.world.level.block.state.properties.Property<?> property) {
        return ForgeTransmogrifier.transmogToWorldEditProperty(property);
    }

    /**
     * Adapts properties.
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
            return ForgeTransmogrifier.transmogToMinecraft(blockState);
        }
        return Block.stateById(blockStateId);
    }

    public static BlockState adapt(net.minecraft.world.level.block.state.BlockState blockState) {
        int blockStateId = Block.getId(blockState);
        BlockState worldEdit = BlockStateIdAccess.getBlockStateById(blockStateId);
        if (worldEdit == null) {
            return ForgeTransmogrifier.transmogToWorldEdit(blockState);
        }
        return worldEdit;
    }

    public static Block adapt(BlockType blockType) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockType.getId()));
    }

    public static BlockType adapt(Block block) {
        return BlockTypes.get(ForgeRegistries.BLOCKS.getKey(block).toString());
    }

    public static Item adapt(ItemType itemType) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemType.getId()));
    }

    public static ItemType adapt(Item item) {
        return ItemTypes.get(ForgeRegistries.ITEMS.getKey(item).toString());
    }

    public static ItemStack adapt(BaseItemStack baseItemStack) {
        net.minecraft.nbt.CompoundTag forgeCompound = null;
        if (baseItemStack.getNbt() != null) {
            forgeCompound = NBTConverter.toNative(baseItemStack.getNbt());
        }
        final ItemStack itemStack = new ItemStack(adapt(baseItemStack.getType()), baseItemStack.getAmount());
        itemStack.setTag(forgeCompound);
        return itemStack;
    }

    public static BaseItemStack adapt(ItemStack itemStack) {
        net.minecraft.nbt.CompoundTag tag = itemStack.serializeNBT();
        if (tag.getAllKeys().isEmpty()) {
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
            itemStack.getCount()
        );
    }

    /**
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public static ForgePlayer adaptPlayer(ServerPlayer player) {
        checkNotNull(player);
        return new ForgePlayer(player);
    }
}
