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

package com.sk89q.worldedit.function.biome;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.biome.BiomeType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copies the biome from one extent to another.
 */
public class ExtentBiomeCopy implements FlatRegionFunction, RegionFunction {

    private final Extent source;
    private final Extent destination;
    private final BlockVector3 from;
    private final BlockVector3 to;
    private final Transform transform;

    /**
     * Make a new biome copy.
     *
     * @param source the source extent
     * @param from the source offset
     * @param destination the destination extent
     * @param to the destination offset
     * @param transform a transform to apply to positions (after source offset, before destination offset)
     * @deprecated use {@link ExtentBiomeCopy#ExtentBiomeCopy(Extent, BlockVector3, Extent, BlockVector3, Transform)}
     */
    @Deprecated
    public ExtentBiomeCopy(Extent source, BlockVector2 from, Extent destination, BlockVector2 to, Transform transform) {
        checkNotNull(source);
        checkNotNull(from);
        checkNotNull(destination);
        checkNotNull(to);
        checkNotNull(transform);
        this.source = source;
        this.from = from.toBlockVector3();
        this.destination = destination;
        this.to = to.toBlockVector3();
        this.transform = transform;
    }

    /**
     * Make a new biome copy.
     *
     * @param source the source extent
     * @param from the source offset
     * @param destination the destination extent
     * @param to the destination offset
     * @param transform a transform to apply to positions (after source offset, before destination offset)
     */
    public ExtentBiomeCopy(Extent source, BlockVector3 from, Extent destination, BlockVector3 to, Transform transform) {
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
        BiomeType biome = source.getBiome(position);
        BlockVector3 orig = position.subtract(from);
        BlockVector3 transformed = transform.apply(orig.toVector3())
            .toBlockPoint()
            .add(to);

        return destination.setBiome(transformed, biome);
    }

    @Override
    @Deprecated
    public boolean apply(BlockVector2 position) throws WorldEditException {
        boolean success = false;
        for (int y = destination.getMinimumPoint().getY(); y <= destination.getMaximumPoint().getY(); y++) {
            success |= apply(position.toBlockVector3(y));
        }
        return success;
    }
}
