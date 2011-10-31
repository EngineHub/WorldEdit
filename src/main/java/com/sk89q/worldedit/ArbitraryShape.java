// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

public abstract class ArbitraryShape {
    private final Region extent;
    private int cacheSizeX;
    private int cacheSizeY;
    private int cacheSizeZ;
    private int cacheX;
    private int cacheY;
    private int cacheZ;

    public ArbitraryShape(Region extent) {
        this.extent = extent;

        Vector min = extent.getMinimumPoint();
        Vector max = extent.getMaximumPoint();

        cacheSizeX = (int)(max.getX() - min.getX() + 1 + 2);
        cacheSizeY = (int)(max.getY() - min.getY() + 1 + 2);
        cacheSizeZ = (int)(max.getZ() - min.getZ() + 1 + 2);

        cacheX = min.getBlockX() - 1;
        cacheY = min.getBlockY() - 1;
        cacheZ = min.getBlockZ() - 1;

        cache = new short[cacheSizeX * cacheSizeY * cacheSizeZ];
    }

    protected Region getExtent() {
        return extent;
    }


    /**
     * Cache entries:
     * 0 = unknown
     * -1 = outside
     * -2 = inside but type and data 0
     * > 0 = inside, value = (type | (data << 8)), not handling data < -1
     */
    private final short[] cache;

    protected abstract BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial);

    private BaseBlock getMaterialCached(int x, int y, int z, Pattern pattern) {
        final int index = (y - cacheY) + (z - cacheZ) * cacheSizeY + (x - cacheX) * cacheSizeY * cacheSizeZ;

        final short cacheEntry = cache[index];
        switch (cacheEntry) {
        case 0:
            // unknown, fetch material
            final BaseBlock material = getMaterial(x, y, z, pattern.next(new BlockVector(x, y, z)));
            if (material == null) {
                // outside
                cache[index] = -1;
                return null;
            }

            short newCacheEntry = (short) (material.getType() | ((material.getData()+1) << 8));
            if (newCacheEntry == 0) {
                // type and data 0
                newCacheEntry = -2;
            }

            cache[index] = newCacheEntry;
            return material;

        case -1:
            // outside
            return null;

        case -2:
            // type and data 0
            return new BaseBlock(0, 0);
        }

        return new BaseBlock(cacheEntry & 255, ((cacheEntry >> 8) - 1) & 15);
    }

    private boolean isInsideCached(int x, int y, int z, Pattern pattern) {
        final int index = (y - cacheY) + (z - cacheZ) * cacheSizeY + (x - cacheX) * cacheSizeY * cacheSizeZ;
        
        switch (cache[index]) {
        case 0:
            // unknown block, meaning they must be outside the extent at this stage, but might still be inside the shape
            return getMaterialCached(x, y, z, pattern) != null;

        case -1:
            // outside
            return false;

        default:
            // inside
            return true;
        }
    }

    public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
        int affected = 0;

        for (BlockVector position : getExtent()) {
            int x = position.getBlockX();
            int y = position.getBlockY();
            int z = position.getBlockZ();

            if (!hollow) {
                final BaseBlock material = getMaterial(x, y, z, pattern.next(position));
                if (material != null && editSession.setBlock(position, material)) {
                    ++affected;
                }

                continue;
            }

            final BaseBlock material = getMaterialCached(x, y, z, pattern);
            if (material == null) {
                continue;
            }

            if (hollow) {
                boolean draw = false;
                do {
                    if (!isInsideCached(x + 1, y, z, pattern)) {
                        draw = true;
                        break;
                    }
                    if (!isInsideCached(x - 1, y, z, pattern)) {
                        draw = true;
                        break;
                    }
                    if (!isInsideCached(x, y + 1, z, pattern)) {
                        draw = true;
                        break;
                    }
                    if (!isInsideCached(x, y - 1, z, pattern)) {
                        draw = true;
                        break;
                    }
                    if (!isInsideCached(x, y, z + 1, pattern)) {
                        draw = true;
                        break;
                    }
                    if (!isInsideCached(x, y, z - 1, pattern)) {
                        draw = true;
                        break;
                    }
                } while (false);

                if (!draw) {
                    continue;
                }
            }

            if (editSession.setBlock(position, material)) {
                ++affected;
            }
        }

        return affected;
    }
}
