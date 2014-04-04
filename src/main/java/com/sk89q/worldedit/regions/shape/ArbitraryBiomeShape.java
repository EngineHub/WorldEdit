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

package com.sk89q.worldedit.regions.shape;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getBiome} method.
 *
 * @author TomyLobo
 */
public abstract class ArbitraryBiomeShape {
    private final FlatRegion extent;
    private int cacheOffsetX;
    private int cacheOffsetZ;
    @SuppressWarnings("FieldCanBeLocal")
    private int cacheSizeX;
    private int cacheSizeZ;

    public ArbitraryBiomeShape(Region extent) {
        if (extent instanceof FlatRegion) {
            this.extent = (FlatRegion) extent;
        }
        else {
            // TODO: polygonize
            this.extent = new CuboidRegion(extent.getWorld(), extent.getMinimumPoint(), extent.getMaximumPoint());
        }

        Vector2D min = extent.getMinimumPoint().toVector2D();
        Vector2D max = extent.getMaximumPoint().toVector2D();

        cacheOffsetX = min.getBlockX() - 1;
        cacheOffsetZ = min.getBlockZ() - 1;

        cacheSizeX = (int) (max.getX() - cacheOffsetX + 2);
        cacheSizeZ = (int) (max.getZ() - cacheOffsetZ + 2);

        cache = new BiomeType[cacheSizeX * cacheSizeZ];
    }

    protected Iterable<Vector2D> getExtent() {
        return extent.asFlatRegion();
    }


    /**
     * Cache entries:
     * null = unknown
     * OUTSIDE = outside
     * else = inside
     */
    private final BiomeType[] cache;

    /**
     * Override this function to specify the shape to generate.
     *
     * @param x X coordinate to be queried
     * @param z Z coordinate to be queried
     * @param defaultBiomeType The default biome for the current column.
     * @return material to place or null to not place anything.
     */
    protected abstract BiomeType getBiome(int x, int z, BiomeType defaultBiomeType);

    private BiomeType getBiomeCached(int x, int z, BiomeType biomeType) {
        final int index = (z - cacheOffsetZ) + (x - cacheOffsetX) * cacheSizeZ;

        final BiomeType cacheEntry = cache[index];
        if (cacheEntry == null) {// unknown, fetch material
            final BiomeType material = getBiome(x, z, biomeType);
            if (material == null) {
                // outside
                cache[index] = OUTSIDE;
                return null;
            }

            cache[index] = material;
            return material;
        }

        if (cacheEntry == OUTSIDE) {
            // outside
            return null;
        }

        return cacheEntry;
    }

    private boolean isInsideCached(int x, int z, BiomeType biomeType) {
        final int index = (z - cacheOffsetZ) + (x - cacheOffsetX) * cacheSizeZ;

        final BiomeType cacheEntry = cache[index];
        if (cacheEntry == null) {
            // unknown block, meaning they must be outside the extent at this stage, but might still be inside the shape
            return getBiomeCached(x, z, biomeType) != null;
        }

        return cacheEntry != OUTSIDE;
    }

    /**
     * Generates the shape.
     *
     * @param editSession The EditSession to use.
     * @param biomeType The default biome type.
     * @param hollow Specifies whether to generate a hollow shape.
     * @return number of affected blocks.
     */
    public int generate(EditSession editSession, BiomeType biomeType, boolean hollow) {
        int affected = 0;

        for (Vector2D position : getExtent()) {
            int x = position.getBlockX();
            int z = position.getBlockZ();

            if (!hollow) {
                final BiomeType material = getBiome(x, z, biomeType);
                if (material != OUTSIDE) {
                    editSession.getWorld().setBiome(position, material);
                    ++affected;
                }

                continue;
            }

            final BiomeType material = getBiomeCached(x, z, biomeType);
            if (material == null) {
                continue;
            }

            boolean draw = false;
            do {
                if (!isInsideCached(x + 1, z, biomeType)) {
                    draw = true;
                    break;
                }
                if (!isInsideCached(x - 1, z, biomeType)) {
                    draw = true;
                    break;
                }
                if (!isInsideCached(x, z + 1, biomeType)) {
                    draw = true;
                    break;
                }
                if (!isInsideCached(x, z - 1, biomeType)) {
                    draw = true;
                    break;
                }
            } while (false);

            if (!draw) {
                continue;
            }

            editSession.getWorld().setBiome(position, material);
            ++affected;
        }

        return affected;
    }

    private static final BiomeType OUTSIDE = new BiomeType() {
        public String getName() {
            throw new UnsupportedOperationException();
        }
    };
}
