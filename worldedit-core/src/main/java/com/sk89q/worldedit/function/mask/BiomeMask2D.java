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

package com.sk89q.worldedit.function.mask;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.biome.BaseBiome;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests true if the biome at applied points is the same as the one given.
 */
public class BiomeMask2D extends AbstractMask2D {

    private final Extent extent;
    private final Set<BaseBiome> biomes = new HashSet<>();

    /**
     * Create a new biome mask.
     *
     * @param extent the extent
     * @param biomes a list of biomes to match
     */
    public BiomeMask2D(Extent extent, Collection<BaseBiome> biomes) {
        checkNotNull(extent);
        checkNotNull(biomes);
        this.extent = extent;
        this.biomes.addAll(biomes);
    }

    /**
     * Create a new biome mask.
     *
     * @param extent the extent
     * @param biome an array of biomes to match
     */
    public BiomeMask2D(Extent extent, BaseBiome... biome) {
        this(extent, Arrays.asList(checkNotNull(biome)));
    }

    /**
     * Add the given biomes to the list of criteria.
     *
     * @param biomes a list of biomes
     */
    public void add(Collection<BaseBiome> biomes) {
        checkNotNull(biomes);
        this.biomes.addAll(biomes);
    }

    /**
     * Add the given biomes to the list of criteria.
     *
     * @param biome an array of biomes
     */
    public void add(BaseBiome... biome) {
        add(Arrays.asList(checkNotNull(biome)));
    }

    /**
     * Get the list of biomes that are tested with.
     *
     * @return a list of biomes
     */
    public Collection<BaseBiome> getBiomes() {
        return biomes;
    }

    @Override
    public boolean test(Vector2D vector) {
        BaseBiome biome = extent.getBiome(vector);
        return biomes.contains(biome);
    }

}
