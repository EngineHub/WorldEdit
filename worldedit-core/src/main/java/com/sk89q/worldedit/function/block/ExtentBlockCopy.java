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

package com.sk89q.worldedit.function.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Direction.Flag;
import com.sk89q.worldedit.world.block.BaseBlock;

/**
 * Copies blocks from one extent to another.
 */
public class ExtentBlockCopy implements RegionFunction {

    private final Extent source;
    private final Extent destination;
    private final BlockVector3 from;
    private final BlockVector3 to;
    private final Transform transform;

    /**
     * Make a new copy.
     *
     * @param source the source extent
     * @param from the source offset
     * @param destination the destination extent
     * @param to the destination offset
     * @param transform a transform to apply to positions (after source offset, before destination offset)
     */
    public ExtentBlockCopy(Extent source, BlockVector3 from, Extent destination, BlockVector3 to, Transform transform) {
        checkNotNull(source);
        checkNotNull(from);
        checkNotNull(destination);
        checkNotNull(to);
        checkNotNull(transform);
        this.source = source;
        this.from = from;
        this.destination = destination;
        this.to = to;
        this.transform = transform;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        BaseBlock block = source.getFullBlock(position);
        BlockVector3 orig = position.subtract(from);
        BlockVector3 transformed = transform.apply(orig.toVector3()).toBlockPoint();

        // Apply transformations to NBT data if necessary
        block = transformNbtData(block);

        return destination.setBlock(transformed.add(to), block);
    }

    /**
     * Transform NBT data in the given block state and return a new instance
     * if the NBT data needs to be transformed.
     *
     * @param state the existing state
     * @return a new state or the existing one
     */
    private BaseBlock transformNbtData(BaseBlock state) {
        CompoundTag tag = state.getNbtData();

        if (tag != null) {
            // Handle blocks which store their rotation in NBT
            if (tag.containsKey("Rot")) {
                int rot = tag.asInt("Rot");

                Direction direction = MCDirections.fromRotation(rot);

                if (direction != null) {
                    Vector3 vector = transform.apply(direction.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                    Direction newDirection = Direction.findClosest(vector, Flag.CARDINAL | Flag.ORDINAL | Flag.SECONDARY_ORDINAL);

                    if (newDirection != null) {
                        CompoundTagBuilder builder = tag.createBuilder();

                        builder.putByte("Rot", (byte) MCDirections.toRotation(newDirection));

                        return state.toBaseBlock(builder.build());
                    }
                }
            }
        }

        return state;
    }

}
