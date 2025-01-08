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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.StringRepresentable;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.BooleanStateProperty;
import org.spongepowered.api.state.EnumStateProperty;
import org.spongepowered.api.state.IntegerStateProperty;
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
            return switch (property) {
                case BooleanStateProperty stateProperty ->
                        new BooleanProperty(property.name(), ImmutableList.copyOf(stateProperty.possibleValues()));
                case IntegerStateProperty stateProperty -> new IntegerProperty(property.name(), ImmutableList.copyOf(stateProperty.possibleValues()));
                case EnumStateProperty<?> stateProperty when stateProperty.valueClass() == org.spongepowered.api.util.Direction.class ->
                        new DirectionalProperty(property.name(), stateProperty.possibleValues().stream()
                                .map(x -> adaptDirection((org.spongepowered.api.util.Direction) x))
                                .toList());
                case EnumStateProperty<?> stateProperty ->
                        new EnumProperty(property.name(), ((net.minecraft.world.level.block.state.properties.EnumProperty<?>) (Object) stateProperty).getPossibleValues().stream()
                                .map(net.minecraft.util.StringRepresentable::getSerializedName)
                                .toList());
                default -> throw new IllegalStateException("Unknown property type");
            };

        }
    });

    public static Property<?> transmogToWorldEditProperty(StateProperty<?> property) {
        return PROPERTY_CACHE.getUnchecked(property);
    }

    private static Map<Property<?>, Object> transmogToWorldEditProperties(
        BlockType block,
        org.spongepowered.api.block.BlockState blockState
    ) {
        Map<Property<?>, Object> properties = new TreeMap<>(Comparator.comparing(Property::getName));
        for (StateProperty<?> stateProperty: blockState.stateProperties()) {
            Object value = ((net.minecraft.world.level.block.state.BlockState) blockState)
                    .getValue((net.minecraft.world.level.block.state.properties.Property<?>) stateProperty);
            if (stateProperty.valueClass() == org.spongepowered.api.util.Direction.class) {
                org.spongepowered.api.util.Direction nativeDirectionValue = (org.spongepowered.api.util.Direction) value;
                value = adaptDirection(nativeDirectionValue);
            } else if (stateProperty instanceof EnumStateProperty<?>) {
                value = ((StringRepresentable) value).serializationString();
            }
            properties.put(block.getProperty(stateProperty.name()), value);
        }
        return properties;
    }

    private static Direction adaptDirection(org.spongepowered.api.util.Direction direction) {
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

    private static org.spongepowered.api.util.Direction adaptDirection(Direction direction) {
        switch (direction) {
            case UP:
                return org.spongepowered.api.util.Direction.UP;
            case DOWN:
                return org.spongepowered.api.util.Direction.DOWN;
            case EAST:
                return org.spongepowered.api.util.Direction.EAST;
            case WEST:
                return org.spongepowered.api.util.Direction.WEST;
            case NORTH:
                return org.spongepowered.api.util.Direction.NORTH;
            case SOUTH:
                return org.spongepowered.api.util.Direction.SOUTH;
            default:
                throw new AssertionError("New direction added: " + direction);
        }
    }

    private static StateProperty<?> findPropertyByName(org.spongepowered.api.block.BlockState blockState, String propertyName) {
        for (StateProperty<?> property: blockState.stateProperties()) {
            if (property.name().equals(propertyName)) {
                return property;
            }
        }

        throw new IllegalStateException("Missing property in " + blockState.asString() + ": " + propertyName);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static org.spongepowered.api.block.BlockState transmogToMinecraftProperties(
        org.spongepowered.api.block.BlockState blockState,
        Map<Property<?>, Object> states
    ) {
        org.spongepowered.api.block.BlockState nativeBlockState = blockState;

        for (Map.Entry<Property<?>, Object> stateEntry: states.entrySet()) {
            Property<?> property = stateEntry.getKey();
            Object value = stateEntry.getValue();
            StateProperty nativeProperty = findPropertyByName(blockState, property.getName());
            Comparable nativeValue;
            if (property instanceof DirectionalProperty) {
                Direction directionValue = (Direction) value;
                nativeValue = adaptDirection(directionValue);
            } else if (property instanceof EnumProperty) {
                String valueName = (String) value;
                Optional<? extends Comparable<?>> nativeValueOpt = nativeProperty.parseValue(valueName);
                if (nativeValueOpt.isEmpty()) {
                    throw new IllegalStateException("Failed to parse " + valueName + " into " + property.getName());
                }
                nativeValue = nativeValueOpt.get();
            } else {
                nativeValue = (Comparable<?>) value;
            }

            nativeBlockState = (org.spongepowered.api.block.BlockState) nativeBlockState.withStateProperty(nativeProperty, nativeValue).orElseThrow();
        }

        return nativeBlockState;
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
        return blockType.getState(transmogToWorldEditProperties(blockType, blockState));
    }

    private SpongeTransmogrifier() {
    }
}
