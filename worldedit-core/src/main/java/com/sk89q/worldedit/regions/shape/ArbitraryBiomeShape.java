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

package com.sk89q.worldedit.regions.shape;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;

import java.util.BitSet;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getBiome} method.
 */
public abstract class ArbitraryBiomeShape {

    private final Region extent;
    private final int cacheOffsetX;
    private final int cacheOffsetY;
    private final int cacheOffsetZ;
    @SuppressWarnings("FieldCanBeLocal")
    private final int cacheSizeX;
    private final int cacheSizeY;
    private final int cacheSizeZ;
    private final BiomeType[] cache;
    private final BitSet isCached;

    public ArbitraryBiomeShape(Region extent) {
        this.extent = extent;

        BlockVector3 min = extent.getMinimumPoint();
        BlockVector3 max = extent.getMaximumPoint();

        cacheOffsetX = min.getBlockX() - 1;
        cacheOffsetY = min.getBlockY() - 1;
        cacheOffsetZ = min.getBlockZ() - 1;

        cacheSizeX = max.getX() - cacheOffsetX + 2;
        cacheSizeY = max.getY() - cacheOffsetY + 2;
        cacheSizeZ = max.getZ() - cacheOffsetZ + 2;

        cache = new BiomeType[cacheSizeX * cacheSizeY * cacheSizeZ];
        isCached = new BitSet(cache.length);
    }

    protected Iterable<BlockVector3> getExtent() {
        return extent;
    }

    /**
     * Override this function to specify the shape to generate.
     *
     * @param x X coordinate to be queried
     * @param z Z coordinate to be queried
     * @param defaultBaseBiome The default biome for the current column.
     * @return material to place or null to not place anything.
     */
    protected abstract BiomeType getBiome(int x, int y, int z, BiomeType defaultBaseBiome);

    private BiomeType getBiomeCached(int x, int y, int z, BiomeType baseBiome) {
        final int index = (y - cacheOffsetY) + (z - cacheOffsetZ) * cacheSizeY + (x - cacheOffsetX) * cacheSizeY * cacheSizeZ;

        if (!isCached.get(index)) {
            final BiomeType material = getBiome(x, y, z, baseBiome);
            isCached.set(index);
            cache[index] = material;
            return material;
        }

        return cache[index];
    }

    private boolean isOutside(int x, int y, int z, BiomeType baseBiome) {
        return getBiomeCached(x, y, z, baseBiome) == null;
    }

    /**
     * Generates the shape.
     *
     * @param editSession The EditSession to use.
     * @param baseBiome The default biome type.
     * @param hollow Specifies whether to generate a hollow shape.
     * @return number of affected blocks.
     */
    public int generate(EditSession editSession, BiomeType baseBiome, boolean hollow) {
        int affected = 0;

        for (BlockVector3 position : getExtent()) {
            int x = position.getBlockX();
            int y = position.getBlockY();
            int z = position.getBlockZ();

            if (!hollow) {
                final BiomeType material = getBiome(x, y, z, baseBiome);
                if (material != null) {
                    editSession.getWorld().setBiome(position, material);
                    ++affected;
                }

                continue;
            }

            final BiomeType material = getBiomeCached(x, y, z, baseBiome);
            if (material == null) {
                continue;
            }

            if (!shouldDraw(x, y, z, material)) {
                continue;
            }

            editSession.getWorld().setBiome(position, material);
            ++affected;
        }

        return affected;
    }

    private boolean shouldDraw(int x, int y, int z, BiomeType material) {
        // we should draw this if the surrounding blocks fall outside the shape,
        // this position will form an edge of the hull
        if (isOutside(x + 1, y, z, material)) {
            return true;
        }
        if (isOutside(x - 1, y, z, material)) {
            return true;
        }
        if (isOutside(x, y, z + 1, material)) {
            return true;
        }
        if (isOutside(x, y, z - 1, material)) {
            return true;
        }
        if (isOutside(x, y + 1, z, material)) {
            return true;
        }
        return isOutside(x, y - 1, z, material);
    }

}
