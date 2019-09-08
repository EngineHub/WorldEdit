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

package com.sk89q.worldedit.fabric;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class FabricAdapter {

    private FabricAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new FabricWorld(world);
    }

    public static Biome adapt(BiomeType biomeType) {
        return Registry.BIOME.get(new Identifier(biomeType.getId()));
    }

    public static BiomeType adapt(Biome biome) {
        return BiomeTypes.get(Registry.BIOME.getId(biome).toString());
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

    public static Direction adaptEnumFacing(net.minecraft.util.math.Direction face) {
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

    public static Property<?> adaptProperty(net.minecraft.state.property.Property<?> property) {
        if (property instanceof net.minecraft.state.property.BooleanProperty) {
            return new BooleanProperty(property.getName(), ImmutableList.copyOf(((net.minecraft.state.property.BooleanProperty) property).getValues()));
        }
        if (property instanceof net.minecraft.state.property.IntProperty) {
            return new IntegerProperty(property.getName(), ImmutableList.copyOf(((net.minecraft.state.property.IntProperty) property).getValues()));
        }
        if (property instanceof DirectionProperty) {
            return new DirectionalProperty(property.getName(), ((DirectionProperty) property).getValues().stream()
                    .map(FabricAdapter::adaptEnumFacing)
                    .collect(Collectors.toList()));
        }
        if (property instanceof net.minecraft.state.property.EnumProperty) {
            // Note: do not make x.asString a method reference.
            // It will cause runtime bootstrap exceptions.
            return new EnumProperty(property.getName(), ((net.minecraft.state.property.EnumProperty<?>) property).getValues().stream()
                    .map(x -> x.asString())
                    .collect(Collectors.toList()));
        }
        return new PropertyAdapter<>(property);
    }

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

    private static net.minecraft.block.BlockState applyProperties(StateFactory<Block, net.minecraft.block.BlockState> stateContainer,
            net.minecraft.block.BlockState newState, Map<Property<?>, Object> states) {
        for (Map.Entry<Property<?>, Object> state : states.entrySet()) {
            net.minecraft.state.property.Property property = stateContainer.getProperty(state.getKey().getName());
            Comparable value = (Comparable) state.getValue();
            // we may need to adapt this value, depending on the source prop
            if (property instanceof DirectionProperty) {
                Direction dir = (Direction) value;
                value = adapt(dir);
            } else if (property instanceof net.minecraft.state.property.EnumProperty) {
                String enumName = (String) value;
                value = ((net.minecraft.state.property.EnumProperty<?>) property).getValue((String) value).orElseGet(() -> {
                    throw new IllegalStateException("Enum property " + property.getName() + " does not contain " + enumName);
                });
            }

            newState = newState.with(property, value);
        }
        return newState;
    }

    public static net.minecraft.block.BlockState adapt(BlockState blockState) {
        Block mcBlock = adapt(blockState.getBlockType());
        net.minecraft.block.BlockState newState = mcBlock.getDefaultState();
        Map<Property<?>, Object> states = blockState.getStates();
        return applyProperties(mcBlock.getStateFactory(), newState, states);
    }

    public static BlockState adapt(net.minecraft.block.BlockState blockState) {
        BlockType blockType = adapt(blockState.getBlock());
        return blockType.getState(adaptProperties(blockType, blockState.getEntries()));
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
        if (baseItemStack.getNbtData() != null) {
            fabricCompound = NBTConverter.toNative(baseItemStack.getNbtData());
        }
        final ItemStack itemStack = new ItemStack(adapt(baseItemStack.getType()), baseItemStack.getAmount());
        itemStack.setTag(fabricCompound);
        return itemStack;
    }

    public static BaseItemStack adapt(ItemStack itemStack) {
        CompoundTag tag = NBTConverter.fromNative(itemStack.toTag(new net.minecraft.nbt.CompoundTag()));
        if (tag.getValue().isEmpty()) {
            tag = null;
        } else {
            final Tag tagTag = tag.getValue().get("tag");
            if (tagTag instanceof CompoundTag) {
                tag = ((CompoundTag) tagTag);
            } else {
                tag = null;
            }
        }
        return new BaseItemStack(adapt(itemStack.getItem()), tag, itemStack.getCount());
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
