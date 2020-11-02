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

package com.sk89q.worldedit.function.mask;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Direction.Flag;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Checks whether any face of the given offsets to a block match a given mask.
 */
public class OffsetsMask extends AbstractMask {

    private static final ImmutableSet<BlockVector3> OFFSET_LIST =
        Direction.valuesOf(Flag.CARDINAL | Flag.UPRIGHT)
            .stream()
            .map(Direction::toBlockVector)
            .collect(ImmutableSet.toImmutableSet());

    /**
     * Create an offsets mask for a single offset.
     *
     * @param mask the mask to use
     * @param offset the offset
     * @return the new offsets mask
     */
    public static OffsetsMask single(Mask mask, BlockVector3 offset) {
        return builder(mask).maxMatches(1).offsets(ImmutableList.of(offset)).build();
    }

    /**
     * Create a new builder, using the given mask.
     * @param mask the mask to use
     * @return the builder
     */
    public static Builder builder(Mask mask) {
        return new Builder().mask(mask);
    }

    /**
     * A builder for an {@link OffsetsMask}.
     */
    public static final class Builder {
        private Mask mask;
        private boolean excludeSelf;
        private int minMatches = 1;
        private int maxMatches = Integer.MAX_VALUE;
        private ImmutableSet<BlockVector3> offsets = OFFSET_LIST;

        private Builder() {
        }

        /**
         * Set the mask to test.
         * @param mask the mask to test
         * @return this builder, for chaining
         */
        public Builder mask(Mask mask) {
            this.mask = mask;
            return this;
        }

        /**
         * Set whether the mask should fail if the original position matches. Defaults to
         * {@code false}.
         *
         * @param excludeSelf {@code true} to exclude the original position if it matches
         * @return this builder, for chaining
         */
        public Builder excludeSelf(boolean excludeSelf) {
            this.excludeSelf = excludeSelf;
            return this;
        }

        /**
         * Set the minimum amount of matches required. Defaults to {@code 1}. Must be smaller than
         * or equal to the {@linkplain #maxMatches(int) max matches} and the {@link #offsets} size,
         * and greater than or equal to {@code 0}.
         *
         * @param minMatches the minimum amount of matches required
         * @return this builder, for chaining
         */
        public Builder minMatches(int minMatches) {
            this.minMatches = minMatches;
            return this;
        }

        /**
         * Set the maximum amount of matches allowed. Defaults to {@link Integer#MAX_VALUE}. Must
         * be greater than or equal to {@linkplain #minMatches(int)}.
         *
         * @param maxMatches the maximum amount of matches allowed
         * @return this builder, for chaining
         */
        public Builder maxMatches(int maxMatches) {
            this.maxMatches = maxMatches;
            return this;
        }

        /**
         * Set the offsets to test. Defaults to all {@linkplain Flag#CARDINAL cardinal}
         * and {@linkplain Flag#UPRIGHT upright} directions.
         *
         * @param offsets the offsets to test
         * @return this builder, for chaining
         */
        public Builder offsets(Iterable<BlockVector3> offsets) {
            this.offsets = ImmutableSet.copyOf(offsets);
            return this;
        }

        /**
         * Build an offsets mask.
         *
         * @return the new mask
         */
        public OffsetsMask build() {
            return new OffsetsMask(mask, excludeSelf, minMatches, maxMatches, offsets);
        }
    }

    private final Mask mask;
    private final boolean excludeSelf;
    private final int minMatches;
    private final int maxMatches;
    private final ImmutableSet<BlockVector3> offsets;

    private OffsetsMask(Mask mask, boolean excludeSelf, int minMatches, int maxMatches, ImmutableSet<BlockVector3> offsets) {
        checkNotNull(mask);
        checkNotNull(offsets);
        // Validate match args. No need to test maxMatches as it must be >=0 based on the conditions here.
        checkArgument(minMatches <= maxMatches, "minMatches must be less than or equal to maxMatches");
        checkArgument(minMatches >= 0, "minMatches must be greater than or equal to 0");
        checkArgument(minMatches <= offsets.size(), "minMatches must be less than or equal to the number of offsets");
        checkArgument(offsets.size() > 0, "offsets must have at least one element");

        this.mask = mask;
        this.excludeSelf = excludeSelf;
        this.minMatches = minMatches;
        this.maxMatches = maxMatches;
        this.offsets = offsets;
    }

    /**
     * Get the mask.
     *
     * @return the mask
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Get the flag determining if matching the current block should fail the mask.
     *
     * @return if it should exclude self-matches
     */
    public boolean getExcludeSelf() {
        return this.excludeSelf;
    }

    /**
     * Gets the minimum number of matches to pass.
     *
     * @return the minimum number of matches
     */
    public int getMinMatches() {
        return this.minMatches;
    }

    /**
     * Gets the maximum number of matches to pass.
     *
     * @return the maximum number of matches
     */
    public int getMaxMatches() {
        return this.maxMatches;
    }

    /**
     * Get the offsets.
     *
     * @return the offsets
     */
    public ImmutableSet<BlockVector3> getOffsets() {
        return this.offsets;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        if (excludeSelf && mask.test(vector)) {
            return false;
        }

        int matches = 0;

        for (BlockVector3 offset : offsets) {
            if (mask.test(vector.add(offset))) {
                matches++;
                if (matches > maxMatches) {
                    return false;
                }
            }
        }

        return minMatches <= matches && matches <= maxMatches;
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        Mask2D childMask = getMask().toMask2D();
        if (childMask != null) {
            return OffsetsMask2D.builder(childMask)
                .excludeSelf(excludeSelf)
                .minMatches(minMatches)
                .maxMatches(maxMatches)
                .offsets(Iterables.transform(offsets, BlockVector3::toBlockVector2))
                .build();
        } else {
            return null;
        }
    }
}
