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

package com.sk89q.worldedit.extent.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Transforms blocks themselves (but not their position) according to a
 * given transform.
 */
public class BlockTransformExtent extends AbstractDelegateExtent {

    private final Transform transform;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public BlockTransformExtent(Extent extent, Transform transform) {
        super(extent);
        checkNotNull(transform);
        this.transform = transform;
    }

    /**
     * Get the transform.
     *
     * @return the transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Transform a block without making a copy.
     *
     * @param block the block
     * @param reverse true to transform in the opposite direction
     * @return the same block
     */
    private <T extends BlockStateHolder<T>> T transformBlock(T block, boolean reverse) {
        return transform(block, reverse ? transform.inverse() : transform);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return transformBlock(super.getBlock(position), false);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return transformBlock(super.getFullBlock(position), false);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        return super.setBlock(location, transformBlock(block, true));
    }

    private static final Set<String> directionNames = Sets.newHashSet("north", "south", "east", "west");

    /**
     * Transform the given block using the given transform.
     *
     * <p>The provided block is <em>not</em> modified.</p>
     *
     * @param block the block
     * @param transform the transform
     * @return the same block
     */
    public static <B extends BlockStateHolder<B>> B transform(B block, Transform transform) {
        checkNotNull(block);
        checkNotNull(transform);

        B result = block;
        List<? extends Property<?>> properties = block.getBlockType().getProperties();

        for (Property<?> property : properties) {
            if (property instanceof DirectionalProperty) {
                DirectionalProperty dirProp = (DirectionalProperty) property;
                Direction value = (Direction) block.getState(property);
                if (value != null) {
                    Vector3 newValue = getNewStateValue(dirProp.getValues(), transform, value.toVector());
                    if (newValue != null) {
                        result = result.with(dirProp, Direction.findClosest(newValue, Direction.Flag.ALL));
                    }
                }
            } else if (property instanceof EnumProperty) {
                EnumProperty enumProp = (EnumProperty) property;
                if (property.getName().equals("axis")) {
                    // We have an axis - this is something we can do the rotations to :sunglasses:
                    Direction value = null;
                    switch ((String) block.getState(property)) {
                        case "x":
                            value = Direction.EAST;
                            break;
                        case "y":
                            value = Direction.UP;
                            break;
                        case "z":
                            value = Direction.NORTH;
                            break;
                    }
                    if (value != null) {
                        Vector3 newValue = getNewStateValue(Direction.valuesOf(Direction.Flag.UPRIGHT | Direction.Flag.CARDINAL), transform, value.toVector());
                        if (newValue != null) {
                            String axis = null;
                            Direction newDir = Direction.findClosest(newValue, Direction.Flag.UPRIGHT | Direction.Flag.CARDINAL);
                            if (newDir == Direction.NORTH || newDir == Direction.SOUTH) {
                                axis = "z";
                            } else if (newDir == Direction.EAST || newDir == Direction.WEST) {
                                axis = "x";
                            } else if (newDir == Direction.UP || newDir == Direction.DOWN) {
                                axis = "y";
                            }
                            if (axis != null) {
                                result = result.with(enumProp, axis);
                            }
                        }
                    }
                }
            } else if (property instanceof IntegerProperty) {
                IntegerProperty intProp = (IntegerProperty) property;
                if (property.getName().equals("rotation")) {
                    if (intProp.getValues().size() == 16) {
                        Optional<Direction> direction = Direction.fromRotationIndex(block.getState(intProp));
                        int horizontalFlags = Direction.Flag.CARDINAL | Direction.Flag.ORDINAL | Direction.Flag.SECONDARY_ORDINAL;
                        if (direction.isPresent()) {
                            Vector3 vec = getNewStateValue(Direction.valuesOf(horizontalFlags), transform, direction.get().toVector());
                            if (vec != null) {
                                OptionalInt newRotation = Direction.findClosest(vec, horizontalFlags).toRotationIndex();
                                if (newRotation.isPresent()) {
                                    result = result.with(intProp, newRotation.getAsInt());
                                }
                            }
                        }
                    }
                }
            }
        }

        List<String> directionalProperties = properties.stream()
                .filter(prop -> prop instanceof BooleanProperty)
                .filter(prop -> directionNames.contains(prop.getName()))
                .filter(property -> (Boolean) block.getState(property))
                .map(Property::getName)
                .map(String::toUpperCase)
                .map(Direction::valueOf)
                .map(dir -> Direction.findClosest(transform.apply(dir.toVector()), Direction.Flag.CARDINAL))
                .filter(Objects::nonNull)
                .map(Direction::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (directionalProperties.size() > 0) {
            for (String directionName : directionNames) {
                result = result.with(block.getBlockType().getProperty(directionName), directionalProperties.contains(directionName));
            }
        }

        return result;
    }

    /**
     * Get the new value with the transformed direction.
     *
     * @param allowedStates the allowed states
     * @param transform the transform
     * @param oldDirection the old direction to transform
     * @return a new state or null if none could be found
     */
    @Nullable
    private static Vector3 getNewStateValue(List<Direction> allowedStates, Transform transform, Vector3 oldDirection) {
        Vector3 newDirection = transform.apply(oldDirection).subtract(transform.apply(Vector3.ZERO)).normalize();
        Vector3 newValue = null;
        double closest = -2;
        boolean found = false;

        for (Direction v : allowedStates) {
            double dot = v.toVector().normalize().dot(newDirection);
            if (dot >= closest) {
                closest = dot;
                newValue = v.toVector();
                found = true;
            }
        }

        if (found) {
            return newValue;
        } else {
            return null;
        }
    }

}
