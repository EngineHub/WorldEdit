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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Checks whether any face of the given offsets to a block match a given mask.
 */
public class OffsetsMask extends AbstractMask {

    private static final ImmutableList<BlockVector3> OFFSET_LIST = ImmutableList.copyOf(
        Direction.valuesOf(Direction.Flag.CARDINAL | Direction.Flag.UPRIGHT)
            .stream()
            .map(Direction::toBlockVector)
            .collect(Collectors.toList())
    );

    private final Mask mask;
    private final boolean excludeSelf;
    private final int minMatches;
    private final int maxMatches;
    private final ImmutableList<BlockVector3> offsets;

    /**
     * Create an OffsetsMask for a single offset.
     *
     * @param mask The mask to use
     * @param offset The offset
     * @return The offsets mask
     */
    public static OffsetsMask single(Mask mask, BlockVector3 offset) {
        return new OffsetsMask(mask, false, 1, 1, ImmutableList.of(offset));
    }

    /**
     * Create a new instance using all adjacent faces excluding diagonals.
     *
     * <p>
     *     Note: This passes as long as there is at least one match, and does not
     *     exclude cases where the block being checked matches the mask.
     * </p>
     *
     * @param mask the mask to test against
     */
    public OffsetsMask(Mask mask) {
        this(mask, false);
    }

    /**
     * Create a new instance using all adjacent faces excluding diagonals.
     *
     * <p>
     *     Note: This passes as long as there is at least one match.
     * </p>
     *
     * @param mask the mask to test against
     * @param excludeSelf excludes blocks where the mask matches itself
     */
    public OffsetsMask(Mask mask, boolean excludeSelf) {
        this(mask, excludeSelf, 1, Integer.MAX_VALUE);
    }

    /**
     * Create a new instance using all adjacent faces excluding diagonals.
     *
     * @param mask the mask to test against
     * @param excludeSelf excludes blocks where the mask matches itself
     * @param minMatches the minimum number of matches (inclusive)
     * @param maxMatches the maximum number of matches (inclusive)
     */
    public OffsetsMask(Mask mask, boolean excludeSelf, int minMatches, int maxMatches) {
        this(mask, excludeSelf, minMatches, maxMatches, OFFSET_LIST);
    }

    /**
     * Create a new instance.
     *
     * <p>
     *     Note: the minimum number of matches must be 0 or greater, and
     *     the maximum number of matches cannot be below the minimum.
     * </p>
     *
     * @param mask the mask to test against
     * @param excludeSelf excludes blocks where the mask matches itself
     * @param minMatches the minimum number of matches (inclusive)
     * @param maxMatches the maximum number of matches (inclusive)
     * @param offsets the block offsets to test with
     */
    public OffsetsMask(Mask mask, boolean excludeSelf, int minMatches, int maxMatches, List<BlockVector3> offsets) {
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
        this.offsets = ImmutableList.copyOf(offsets);
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
     * Get the offsets.
     *
     * @return the offsets
     */
    public ImmutableList<BlockVector3> getOffsets() {
        return this.offsets;
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
            return new OffsetsMask2D(
                childMask,
                excludeSelf,
                minMatches,
                maxMatches,
                getOffsets().stream().map(BlockVector3::toBlockVector2).collect(Collectors.toList())
            );
        } else {
            return null;
        }
    }
}
