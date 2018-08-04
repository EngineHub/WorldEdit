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

package com.sk89q.worldedit.math.convolution;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

/**
 * Allows applications of Kernels onto the region's height map.
 *
 * <p>Currently only used for smoothing (with a GaussianKernel)</p>.
 */
public class HeightMap {

    private int[] data;
    private int width;
    private int height;

    private Region region;
    private EditSession session;

    /**
     * Constructs the HeightMap
     *
     * @param session an edit session
     * @param region the region
     */
    public HeightMap(EditSession session, Region region) {
        checkNotNull(session);
        checkNotNull(region);

        this.session = session;
        this.region = region;

        this.width = region.getWidth();
        this.height = region.getLength();

        int minX = region.getMinimumPoint().getBlockX();
        int minY = region.getMinimumPoint().getBlockY();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxY = region.getMaximumPoint().getBlockY();

        // Store current heightmap data
        data = new int[width * height];
        for (int z = 0; z < height; ++z) {
            for (int x = 0; x < width; ++x) {
                data[z * width + x] = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY);
            }
        }
    }

    /**
     * Apply the filter 'iterations' amount times.
     * 
     * @param filter the filter
     * @param iterations the number of iterations
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */

    public int applyFilter(HeightMapFilter filter, int iterations) throws MaxChangedBlocksException {
        checkNotNull(filter);

        int[] newData = new int[data.length];
        System.arraycopy(data, 0, newData, 0, data.length);

        for (int i = 0; i < iterations; ++i) {
            newData = filter.filter(newData, width, height);
        }

        return apply(newData);
    }

    /**
     * Apply a raw heightmap to the region
     * 
     * @param data the data
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */

    public int apply(int[] data) throws MaxChangedBlocksException {
        checkNotNull(data);

        Vector minY = region.getMinimumPoint();
        int originX = minY.getBlockX();
        int originY = minY.getBlockY();
        int originZ = minY.getBlockZ();

        int maxY = region.getMaximumPoint().getBlockY();
        BlockState fillerAir = BlockTypes.AIR.getDefaultState();

        int blocksChanged = 0;

        // Apply heightmap
        for (int z = 0; z < height; ++z) {
            for (int x = 0; x < width; ++x) {
                int index = z * width + x;
                int curHeight = this.data[index];

                // Clamp newHeight within the selection area
                int newHeight = Math.min(maxY, data[index]);

                // Offset x,z to be 'real' coordinates
                int xr = x + originX;
                int zr = z + originZ;

                // We are keeping the topmost blocks so take that in account for the scale
                double scale = (double) (curHeight - originY) / (double) (newHeight - originY);

                // Depending on growing or shrinking we need to start at the bottom or top
                if (newHeight > curHeight) {
                    // Set the top block of the column to be the same type (this might go wrong with rounding)
                    BlockState existing = session.getBlock(new Vector(xr, curHeight, zr));

                    // Skip water/lava
                    if (existing.getBlockType() != BlockTypes.WATER && existing.getBlockType() != BlockTypes.LAVA) {
                        session.setBlock(new Vector(xr, newHeight, zr), existing);
                        ++blocksChanged;

                        // Grow -- start from 1 below top replacing airblocks
                        for (int y = newHeight - 1 - originY; y >= 0; --y) {
                            int copyFrom = (int) (y * scale);
                            session.setBlock(new Vector(xr, originY + y, zr), session.getBlock(new Vector(xr, originY + copyFrom, zr)));
                            ++blocksChanged;
                        }
                    }
                } else if (curHeight > newHeight) {
                    // Shrink -- start from bottom
                    for (int y = 0; y < newHeight - originY; ++y) {
                        int copyFrom = (int) (y * scale);
                        session.setBlock(new Vector(xr, originY + y, zr), session.getBlock(new Vector(xr, originY + copyFrom, zr)));
                        ++blocksChanged;
                    }

                    // Set the top block of the column to be the same type
                    // (this could otherwise go wrong with rounding)
                    session.setBlock(new Vector(xr, newHeight, zr), session.getBlock(new Vector(xr, curHeight, zr)));
                    ++blocksChanged;

                    // Fill rest with air
                    for (int y = newHeight + 1; y <= curHeight; ++y) {
                        session.setBlock(new Vector(xr, y, zr), fillerAir);
                        ++blocksChanged;
                    }
                }
            }
        }

        // Drop trees to the floor -- TODO

        return blocksChanged;
    }

}
