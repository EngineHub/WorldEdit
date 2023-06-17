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
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getMaterial} method.
 */
public abstract class ArbitraryShape {
    /**
     * This Object instance is used for cache entries that are known to be outside the shape.
     */
    private static final Object OUTSIDE = new Object();

    protected final Region extent;

    private final int cacheOffsetX;
    private final int cacheOffsetY;
    private final int cacheOffsetZ;
    @SuppressWarnings("FieldCanBeLocal")
    private final int cacheSizeX;
    private final int cacheSizeY;
    private final int cacheSizeZ;

    /**
     * Cache for expression results.
     *
     * <p>Possible cache entries:
     * <ul>
     * <li>null = unknown</li>
     * <li>ArbitraryShape.OUTSIDE = outside</li>
     * <li>any BaseBlock = inside</li>
     * <li>anything else = (invalid, not used)</li>
     * </ul>
     */
    private Object[] cache;

    public ArbitraryShape(Region extent) {
        this.extent = extent;

        BlockVector3 min = extent.getMinimumPoint();
        BlockVector3 max = extent.getMaximumPoint();

        cacheOffsetX = min.getBlockX() - 1;
        cacheOffsetY = min.getBlockY() - 1;
        cacheOffsetZ = min.getBlockZ() - 1;

        cacheSizeX = max.getX() - cacheOffsetX + 2;
        cacheSizeY = max.getY() - cacheOffsetY + 2;
        cacheSizeZ = max.getZ() - cacheOffsetZ + 2;
    }

    protected Region getExtent() {
        return extent;
    }

    /**
     * Override this function to specify the shape to generate.
     *
     * @param x X coordinate to be queried
     * @param y Y coordinate to be queried
     * @param z Z coordinate to be queried
     * @param defaultMaterial The material returned by the pattern for the current block.
     * @return material to place or null to not place anything.
     */
    protected abstract BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial);

    /**
     * Generates the shape.
     *
     * @param editSession The EditSession to use.
     * @param pattern The pattern to generate default materials from.
     * @param hollow Specifies whether to generate a hollow shape.
     * @return number of affected blocks.
     * @throws MaxChangedBlocksException if the maximum blocks changed is exceeded
     */
    public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
        if (hollow && cache == null) {
            cache = new Object[cacheSizeX * cacheSizeY * cacheSizeZ];
        }

        int affected = 0;

        for (BlockVector3 position : getExtent()) {
            final BaseBlock material = getMaterial(position, pattern, hollow);

            if (material != null && editSession.setBlock(position, material)) {
                ++affected;
            }
        }

        return affected;
    }

    private BaseBlock getMaterial(BlockVector3 position, Pattern pattern, boolean hollow) {
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        if (!hollow) {
            return getMaterial(x, y, z, pattern.applyBlock(position));
        }

        final Object cacheEntry = getMaterialCached(x, y, z, pattern);
        if (cacheEntry == OUTSIDE) {
            return null;
        }

        final BaseBlock material = (BaseBlock) cacheEntry;

        if (isOutsideCached(x + 1, y, z, pattern)) {
            return material;
        }
        if (isOutsideCached(x - 1, y, z, pattern)) {
            return material;
        }
        if (isOutsideCached(x, y, z + 1, pattern)) {
            return material;
        }
        if (isOutsideCached(x, y, z - 1, pattern)) {
            return material;
        }
        if (isOutsideCached(x, y + 1, z, pattern)) {
            return material;
        }
        if (isOutsideCached(x, y - 1, z, pattern)) {
            return material;
        }

        return null;
    }

    private boolean isOutsideCached(int x, int y, int z, Pattern pattern) {
        return getMaterialCached(x, y, z, pattern) == OUTSIDE;
    }

    private Object getMaterialCached(int x, int y, int z, Pattern pattern) {
        final int index = (y - cacheOffsetY) + (z - cacheOffsetZ) * cacheSizeY + (x - cacheOffsetX) * cacheSizeY * cacheSizeZ;
        final Object cacheEntry = cache[index];
        if (cacheEntry == null) {
            final BaseBlock material = getMaterial(x, y, z, pattern.applyBlock(BlockVector3.at(x, y, z)));
            if (material == null) {
                return cache[index] = OUTSIDE;
            } else {
                return cache[index] = material;
            }
        }
        return cacheEntry;
    }
}
