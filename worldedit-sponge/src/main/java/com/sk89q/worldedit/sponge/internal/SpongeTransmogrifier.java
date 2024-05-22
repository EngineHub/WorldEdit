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

package com.sk89q.worldedit.sponge.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Raw, un-cached transformations.
 */
public class SpongeTransmogrifier {

    private static final LoadingCache<StateProperty<?>, Property<?>> PROPERTY_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public Property<?> load(StateProperty<?> property) {
            net.minecraft.world.level.block.state.properties.Property<?> nativeProperty =
                    (net.minecraft.world.level.block.state.properties.Property<?>) property;
            String propertyName = nativeProperty.getName();
            if (nativeProperty instanceof net.minecraft.world.level.block.state.properties.BooleanProperty) {
                return new BooleanProperty(propertyName,
                    ImmutableList.copyOf(((net.minecraft.world.level.block.state.properties.BooleanProperty) nativeProperty).getPossibleValues()));
            }
            if (nativeProperty instanceof net.minecraft.world.level.block.state.properties.IntegerProperty) {
                return new IntegerProperty(propertyName,
                    ImmutableList.copyOf(((net.minecraft.world.level.block.state.properties.IntegerProperty) nativeProperty).getPossibleValues()));
            }
            if (isDirectionProperty(nativeProperty)) {
                return new DirectionalProperty(propertyName,
                    ((net.minecraft.world.level.block.state.properties.EnumProperty<?>) nativeProperty).getPossibleValues().stream()
                        .map(x -> adaptDirection((net.minecraft.core.Direction) x))
                        .toList()
                );
            }
            if (nativeProperty instanceof net.minecraft.world.level.block.state.properties.EnumProperty) {
                return new EnumProperty(propertyName,
                    ((net.minecraft.world.level.block.state.properties.EnumProperty<?>) nativeProperty).getPossibleValues().stream()
                        .map(StringRepresentable::getSerializedName)
                        .toList());
            }
            throw new IllegalStateException("Unknown property type");
        }
    });

    public static Property<?> transmogToWorldEditProperty(StateProperty<?> property) {
        return PROPERTY_CACHE.getUnchecked(property);
    }

    private static Map<Property<?>, Object> transmogToWorldEditProperties(
        BlockType block,
        net.minecraft.world.level.block.state.BlockState blockState
    ) {
        Map<Property<?>, Object> properties = new TreeMap<>(Comparator.comparing(Property::getName));
        for (net.minecraft.world.level.block.state.properties.Property<?> nativeProperty: blockState.getProperties()) {
            Object value = blockState.getValue(nativeProperty);
            if (isDirectionProperty(nativeProperty)) {
                net.minecraft.core.Direction nativeDirectionValue = (net.minecraft.core.Direction) value;
                value = adaptDirection(nativeDirectionValue);
            } else if (nativeProperty instanceof net.minecraft.world.level.block.state.properties.EnumProperty) {
                value = ((StringRepresentable) value).getSerializedName();
            }
            properties.put(block.getProperty(nativeProperty.getName()), value);
        }
        return properties;
    }

    private static boolean isDirectionProperty(net.minecraft.world.level.block.state.properties.Property<?> property) {
        return property instanceof net.minecraft.world.level.block.state.properties.EnumProperty
            && property.getValueClass().isAssignableFrom(net.minecraft.core.Direction.class);
    }

    private static Direction adaptDirection(net.minecraft.core.Direction direction) {
        switch (direction) {
            case UP:
                return Direction.UP;
            case DOWN:
                return Direction.DOWN;
            case EAST:
                return Direction.EAST;
            case WEST:
                return Direction.WEST;
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            default:
                throw new AssertionError("New direction added: " + direction);
        }
    }

    private static net.minecraft.core.Direction adaptDirection(Direction direction) {
        switch (direction) {
            case UP:
                return net.minecraft.core.Direction.UP;
            case DOWN:
                return net.minecraft.core.Direction.DOWN;
            case EAST:
                return net.minecraft.core.Direction.EAST;
            case WEST:
                return net.minecraft.core.Direction.WEST;
            case NORTH:
                return net.minecraft.core.Direction.NORTH;
            case SOUTH:
                return net.minecraft.core.Direction.SOUTH;
            default:
                throw new AssertionError("New direction added: " + direction);
        }
    }

    private static net.minecraft.world.level.block.state.properties.Property<?> findPropertyByName(
        net.minecraft.world.level.block.state.BlockState blockState,
        String propertyName
    ) {
        for (net.minecraft.world.level.block.state.properties.Property<?> property: blockState.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }

        throw new IllegalStateException("Missing property in " + blockState.getBlock() + ": " + propertyName);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static org.spongepowered.api.block.BlockState transmogToMinecraftProperties(
        org.spongepowered.api.block.BlockState blockState,
        Map<Property<?>, Object> states
    ) {
        net.minecraft.world.level.block.state.BlockState nativeBlockState =
            (net.minecraft.world.level.block.state.BlockState) blockState;
        for (Map.Entry<Property<?>, Object> stateEntry: states.entrySet()) {
            Property<?> property = stateEntry.getKey();
            Object value = stateEntry.getValue();
            net.minecraft.world.level.block.state.properties.Property<?> nativeProperty =
                findPropertyByName(nativeBlockState, property.getName());
            Comparable<?> nativeValue;
            if (property instanceof DirectionalProperty) {
                Direction directionValue = (Direction) value;
                nativeValue = adaptDirection(directionValue);
            } else if (property instanceof EnumProperty) {
                String valueName = (String) value;
                Optional<? extends Comparable<?>> nativeValueOpt = nativeProperty.getValue(valueName);
                if (nativeValueOpt.isEmpty()) {
                    throw new IllegalStateException("Failed to parse " + valueName + " into " + property.getName());
                }
                nativeValue = nativeValueOpt.get();
            } else {
                nativeValue = (Comparable<?>) value;
            }
            nativeBlockState = nativeBlockState.setValue(
                (net.minecraft.world.level.block.state.properties.Property) nativeProperty, (Comparable) nativeValue);
        }

        return (org.spongepowered.api.block.BlockState) nativeBlockState;
    }

    public static org.spongepowered.api.block.BlockState transmogToMinecraft(BlockState blockState) {
        org.spongepowered.api.block.BlockType mcBlock = Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
            .value(ResourceKey.resolve(blockState.getBlockType().id()));
        org.spongepowered.api.block.BlockState newState = mcBlock.defaultState();
        Map<Property<?>, Object> states = blockState.getStates();
        return transmogToMinecraftProperties(newState, states);
    }

    public static BlockState transmogToWorldEdit(org.spongepowered.api.block.BlockState blockState) {
        BlockType blockType = BlockType.REGISTRY.get(
            blockState.type().key(RegistryTypes.BLOCK_TYPE).asString()
        );
        return blockType.getState(transmogToWorldEditProperties(blockType,
            (net.minecraft.world.level.block.state.BlockState) blockState));
    }

    private SpongeTransmogrifier() {
    }
}
