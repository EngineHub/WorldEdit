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
import org.spongepowered.api.state.BooleanStateProperty;
import org.spongepowered.api.state.EnumStateProperty;
import org.spongepowered.api.state.IntegerStateProperty;
import org.spongepowered.api.state.StateProperty;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Raw, un-cached transformations.
 */
public class SpongeTransmogrifier {

    private static final LoadingCache<StateProperty<?>, Property<?>> PROPERTY_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<StateProperty<?>, Property<?>>() {
        @Override
        public Property<?> load(StateProperty<?> property) throws Exception {
            if (property instanceof BooleanStateProperty) {
                return new BooleanProperty(property.name(), ImmutableList.copyOf(((BooleanStateProperty) property).possibleValues()));
            }
            if (property instanceof IntegerStateProperty) {
                return new IntegerProperty(property.name(), ImmutableList.copyOf(((IntegerStateProperty) property).possibleValues()));
            }
            if (isDirectionProperty(property)) {
                return new DirectionalProperty(property.name(),
                    ((EnumStateProperty<?>) property).possibleValues().stream()
                        .map(x -> adaptDirection((net.minecraft.core.Direction) x))
                        .collect(Collectors.toList())
                );
            }
            if (property instanceof EnumStateProperty) {
                return new EnumProperty(property.name(), ((EnumStateProperty<?>) property).possibleValues().stream()
                    .map(x -> ((StringRepresentable) x).getSerializedName())
                    .collect(Collectors.toList()));
            }
            throw new IllegalStateException("Unknown property type");
        }
    });

    public static Property<?> transmogToWorldEditProperty(StateProperty<?> property) {
        return PROPERTY_CACHE.getUnchecked(property);
    }

    private static Map<Property<?>, Object> transmogToWorldEditProperties(BlockType block, Map<StateProperty<?>, ?> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<StateProperty<?>, ?> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (isDirectionProperty(prop.getKey())) {
                value = adaptDirection((net.minecraft.core.Direction) value);
            } else if (prop.getKey() instanceof EnumStateProperty) {
                value = ((StringRepresentable) value).getSerializedName();
            }
            props.put(block.getProperty(prop.getKey().name()), value);
        }
        return props;
    }

    private static boolean isDirectionProperty(StateProperty<?> property) {
        return property instanceof EnumStateProperty
            && property.valueClass().isAssignableFrom(net.minecraft.core.Direction.class);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static org.spongepowered.api.block.BlockState transmogToMinecraftProperties(
        org.spongepowered.api.block.BlockState newState,
        Map<Property<?>, Object> states
    ) {
        org.spongepowered.api.block.BlockType type = newState.type();
        for (Map.Entry<Property<?>, Object> state : states.entrySet()) {
            StateProperty<?> property = type.findStateProperty(state.getKey().getName())
                .orElseThrow(() -> new IllegalStateException(
                    "Missing property in " + type + ": " + state.getKey().getName())
                );
            Comparable value = (Comparable) state.getValue();
            // we may need to adapt this value, depending on the source prop
            if (state.getKey() instanceof DirectionalProperty) {
                Direction dir = (Direction) value;
                value = adaptDirection(dir);
            } else if (state.getKey() instanceof EnumProperty) {
                String enumName = (String) value;
                value = property.parseValue(enumName).orElseThrow(() -> new IllegalStateException(
                    "Failed to parse '" + enumName + "' into " + state.getKey().getName()
                ));
            }

            Optional<org.spongepowered.api.block.BlockState> optional =
                newState.withStateProperty((StateProperty) property, value);
            newState = optional.orElseThrow(() -> new IllegalStateException(
                "Failed to change state property " + property.name()
            ));
        }
        return newState;
    }

    public static org.spongepowered.api.block.BlockState transmogToMinecraft(BlockState blockState) {
        org.spongepowered.api.block.BlockType mcBlock = Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
            .value(ResourceKey.resolve(blockState.getBlockType().getId()));
        org.spongepowered.api.block.BlockState newState = mcBlock.defaultState();
        Map<Property<?>, Object> states = blockState.getStates();
        return transmogToMinecraftProperties(newState, states);
    }

    public static BlockState transmogToWorldEdit(org.spongepowered.api.block.BlockState blockState) {
        BlockType blockType = BlockType.REGISTRY.get(
            blockState.type().key(RegistryTypes.BLOCK_TYPE).asString()
        );
        return blockType.getState(transmogToWorldEditProperties(blockType, blockState.statePropertyMap()));
    }

    private SpongeTransmogrifier() {
    }
}
