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

package com.sk89q.worldedit.fabric.internal;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.StringIdentifiable;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Raw, un-cached transformations.
 */
public class FabricTransmogrifier {

    public static Property<?> transmogToWorldEditProperty(net.minecraft.state.property.Property<?> property) {
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

    private static Map<Property<?>, Object> transmogToWorldEditProperties(BlockType block, Map<net.minecraft.state.property.Property<?>, Comparable<?>> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<net.minecraft.state.property.Property<?>, Comparable<?>> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (prop.getKey() instanceof DirectionProperty) {
                value = FabricAdapter.adaptEnumFacing((net.minecraft.util.math.Direction) value);
            } else if (prop.getKey() instanceof net.minecraft.state.property.EnumProperty) {
                value = ((StringIdentifiable) value).asString();
            }
            props.put(block.getProperty(prop.getKey().getName()), value);
        }
        return props;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static net.minecraft.block.BlockState transmogToMinecraftProperties(StateManager<Block, BlockState> stateContainer,
                                                                  net.minecraft.block.BlockState newState, Map<Property<?>, Object> states) {
        for (Map.Entry<Property<?>, Object> state : states.entrySet()) {
            net.minecraft.state.property.Property property = stateContainer.getProperty(state.getKey().getName());
            Comparable value = (Comparable) state.getValue();
            // we may need to adapt this value, depending on the source prop
            if (property instanceof DirectionProperty) {
                Direction dir = (Direction) value;
                value = FabricAdapter.adapt(dir);
            } else if (property instanceof net.minecraft.state.property.EnumProperty) {
                String enumName = (String) value;
                value = ((net.minecraft.state.property.EnumProperty<?>) property).parse((String) value).orElseGet(() -> {
                    throw new IllegalStateException("Enum property " + property.getName() + " does not contain " + enumName);
                });
            }

            newState = newState.with(property, value);
        }
        return newState;
    }

    public static net.minecraft.block.BlockState transmogToMinecraft(com.sk89q.worldedit.world.block.BlockState blockState) {
        Block mcBlock = FabricAdapter.adapt(blockState.getBlockType());
        net.minecraft.block.BlockState newState = mcBlock.getDefaultState();
        Map<Property<?>, Object> states = blockState.getStates();
        return transmogToMinecraftProperties(mcBlock.getStateManager(), newState, states);
    }

    public static com.sk89q.worldedit.world.block.BlockState transmogToWorldEdit(net.minecraft.block.BlockState blockState) {
        BlockType blockType = FabricAdapter.adapt(blockState.getBlock());
        return blockType.getState(transmogToWorldEditProperties(blockType, blockState.getEntries()));
    }

    private FabricTransmogrifier() {
    }
}
