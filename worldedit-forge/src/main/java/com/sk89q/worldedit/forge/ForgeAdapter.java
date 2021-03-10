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
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ForgeAdapter {

    private ForgeAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new ForgeWorld(world);
    }

    public static Biome adapt(BiomeType biomeType) {
        return ServerLifecycleHooks.getCurrentServer()
            .func_244267_aX()
            .func_243612_b(Registry.field_239720_u_)
            .getOrDefault(new ResourceLocation(biomeType.getId()));
    }

    public static BiomeType adapt(Biome biome) {
        ResourceLocation id = ServerLifecycleHooks.getCurrentServer()
            .func_244267_aX()
            .func_243612_b(Registry.field_239720_u_)
            .getKey(biome);
        Objects.requireNonNull(id, "biome is not registered");
        return BiomeTypes.get(id.toString());
    }

    public static Vector3 adapt(Vector3d vector) {
        return Vector3.at(vector.x, vector.y, vector.z);
    }

    public static BlockVector3 adapt(BlockPos pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vector3d toVec3(BlockVector3 vector) {
        return new Vector3d(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static net.minecraft.util.Direction adapt(Direction face) {
        switch (face) {
            case NORTH: return net.minecraft.util.Direction.NORTH;
            case SOUTH: return net.minecraft.util.Direction.SOUTH;
            case WEST: return net.minecraft.util.Direction.WEST;
            case EAST: return net.minecraft.util.Direction.EAST;
            case DOWN: return net.minecraft.util.Direction.DOWN;
            case UP:
            default:
                return net.minecraft.util.Direction.UP;
        }
    }

    public static Direction adaptEnumFacing(@Nullable net.minecraft.util.Direction face) {
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
    public static Property<?> adaptProperty(net.minecraft.state.Property<?> property) {
        return ForgeTransmogrifier.transmogToWorldEditProperty(property);
    }

    /**
     * Adapts properties.
     * @deprecated without replacement, use the block adapter methods
     */
    @Deprecated
    public static Map<Property<?>, Object> adaptProperties(BlockType block, Map<net.minecraft.state.Property<?>, Comparable<?>> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<net.minecraft.state.Property<?>, Comparable<?>> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (prop.getKey() instanceof DirectionProperty) {
                value = adaptEnumFacing((net.minecraft.util.Direction) value);
            } else if (prop.getKey() instanceof net.minecraft.state.EnumProperty) {
                value = ((IStringSerializable) value).func_176610_l();
            }
            props.put(block.getProperty(prop.getKey().getName()), value);
        }
        return props;
    }

    public static net.minecraft.block.BlockState adapt(BlockState blockState) {
        int blockStateId = BlockStateIdAccess.getBlockStateId(blockState);
        if (!BlockStateIdAccess.isValidInternalId(blockStateId)) {
            return ForgeTransmogrifier.transmogToMinecraft(blockState);
        }
        return Block.getStateById(blockStateId);
    }

    public static BlockState adapt(net.minecraft.block.BlockState blockState) {
        int blockStateId = Block.getStateId(blockState);
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
        CompoundNBT forgeCompound = null;
        if (baseItemStack.getNbt() != null) {
            forgeCompound = NBTConverter.toNative(baseItemStack.getNbt());
        }
        final ItemStack itemStack = new ItemStack(adapt(baseItemStack.getType()), baseItemStack.getAmount());
        itemStack.setTag(forgeCompound);
        return itemStack;
    }

    public static BaseItemStack adapt(ItemStack itemStack) {
        CompoundNBT tag = itemStack.serializeNBT();
        if (tag.keySet().isEmpty()) {
            tag = null;
        } else {
            final INBT tagTag = tag.get("tag");
            if (tagTag instanceof CompoundNBT) {
                tag = ((CompoundNBT) tagTag);
            } else {
                tag = null;
            }
        }
        CompoundNBT finalTag = tag;
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
    public static ForgePlayer adaptPlayer(ServerPlayerEntity player) {
        checkNotNull(player);
        return new ForgePlayer(player);
    }
}
