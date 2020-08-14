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

package com.sk89q.worldedit.regions;

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An intersection of several other regions. Any location that is contained in one
 * of the child regions is considered as contained by this region.
 *
 * <p>{@link #iterator()} returns a special iterator that will iterate through
 * the iterators of each region in an undefined sequence. Some positions may
 * be repeated if the position is contained in more than one region, but this cannot
 * be guaranteed to occur.</p>
 */
public class RegionIntersection extends AbstractRegion {

    private final List<Region> regions = new ArrayList<>();

    /**
     * Create a new instance with the included list of regions.
     *
     * @param regions a list of regions, which is copied
     */
    public RegionIntersection(List<Region> regions) {
        this(null, regions);
    }

    /**
     * Create a new instance with the included list of regions.
     *
     * @param regions a list of regions, which is copied
     */
    public RegionIntersection(Region... regions) {
        this(null, regions);
    }

    /**
     * Create a new instance with the included list of regions.
     *
     * @param world   the world
     * @param regions a list of regions, which is copied
     */
    public RegionIntersection(World world, List<Region> regions) {
        super(world);
        checkNotNull(regions);
        checkArgument(!regions.isEmpty(), "empty region list is not supported");
        this.regions.addAll(regions);
    }

    /**
     * Create a new instance with the included list of regions.
     *
     * @param world   the world
     * @param regions an array of regions, which is copied
     */
    public RegionIntersection(World world, Region... regions) {
        super(world);
        checkNotNull(regions);
        checkArgument(regions.length > 0, "empty region list is not supported");
        Collections.addAll(this.regions, regions);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        BlockVector3 minimum = regions.get(0).getMinimumPoint();
        for (int i = 1; i < regions.size(); i++) {
            minimum = regions.get(i).getMinimumPoint().getMinimum(minimum);
        }
        return minimum;
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        BlockVector3 maximum = regions.get(0).getMaximumPoint();
        for (int i = 1; i < regions.size(); i++) {
            maximum = regions.get(i).getMaximumPoint().getMaximum(maximum);
        }
        return maximum;
    }

    @Override
    public void expand(BlockVector3... changes) throws RegionOperationException {
        checkNotNull(changes);
        throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.intersection.error.cannot-expand"));
    }

    @Override
    public void contract(BlockVector3... changes) throws RegionOperationException {
        checkNotNull(changes);
        throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.intersection.error.cannot-contract"));
    }

    @Override
    public boolean contains(BlockVector3 position) {
        checkNotNull(position);

        for (Region region : regions) {
            if (region.contains(position)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        return Iterators.concat(Iterators.transform(regions.iterator(), r -> r.iterator()));
    }

}
