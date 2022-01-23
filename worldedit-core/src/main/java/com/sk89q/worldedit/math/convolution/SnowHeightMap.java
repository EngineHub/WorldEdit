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

package com.sk89q.worldedit.math.convolution;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows applications of Kernels onto the region's height map for snow layers
 *
 * <p>Currently only used for snow layer smoothing (with a GaussianKernel)</p>.
 */
public class SnowHeightMap {
    private final float[] data;
    private final int width;
    private final int height;

    private final Region region;
    private final EditSession session;

    private final Property<Integer> layers;

    /**
     * Constructs the SnowHeightMap.
     *
     * @param session an edit session
     * @param region the region
     * @param mask optional mask for the height map
     */
    public SnowHeightMap(EditSession session, Region region, @Nullable Mask mask) {
        checkNotNull(session);
        checkNotNull(region);

        this.session = session;
        this.region = region;

        this.width = region.getWidth();
        this.height = region.getLength();

        this.layers = BlockTypes.SNOW.getProperty("layers");

        int minX = region.getMinimumPoint().getBlockX();
        int minY = region.getMinimumPoint().getBlockY();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxY = region.getMaximumPoint().getBlockY();

        // Store current heightmap data
        data = new float[width * height];
        for (int z = 0; z < height; ++z) {
            for (int x = 0; x < width; ++x) {
                int highestBlockY = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY, mask);
                BlockState upper = session.getBlock(BlockVector3.at(x + minX, highestBlockY + 1, z + minZ));
                if (upper.getBlockType() == BlockTypes.SNOW) {
                    Integer amountLayers = upper.getState(layers);
                    data[z * width + x] = (highestBlockY + 1 + (((float) amountLayers - 1) / 8));
                } else {
                    BlockState block = session.getBlock(BlockVector3.at(x + minX, highestBlockY, z + minZ));
                    if (block.getBlockType().getMaterial().isAir()) {
                        data[z * width + x] = highestBlockY;
                    } else {
                        data[z * width + x] = highestBlockY + 1;
                    }
                }
            }
        }
    }

    /**
     * Compute the new heightmap with the filter 'iterations' amount times.
     *
     * @param filter the filter
     * @param iterations the number of iterations
     * @return new generated heightmap of the terrain
     */
    public float[] applyFilter(HeightMapFilter filter, int iterations) {
        checkNotNull(filter);

        float[] newData = data.clone();

        for (int i = 0; i < iterations; ++i) {
            // add an offset from 0.0625F to the values (snowlayer half)
            newData = filter.filter(newData, width, height, 0.0625F);
        }
        return newData;
    }

    /**
     * Apply a raw heightmap to a region. Use snow layers.
     *
     * @param data the data
     * @param layerBlocks amount of blocks with type SNOW_BLOCK
     * @return number of blocks affected
     * @throws MaxChangedBlocksException if the maximum block change limit is exceeded
     */
    public int applyChanges(float[] data, int layerBlocks) throws MaxChangedBlocksException {
        checkNotNull(data);

        BlockVector3 minY = region.getMinimumPoint();
        int originX = minY.getBlockX();
        int originY = minY.getBlockY();
        int originZ = minY.getBlockZ();

        int maxY = region.getMaximumPoint().getBlockY();

        BlockState fillerAir = BlockTypes.AIR.getDefaultState();
        BlockState fillerSnow = BlockTypes.SNOW_BLOCK.getDefaultState();

        int blocksChanged = 0;

        // Apply heightmap
        for (int z = 0; z < height; ++z) {
            for (int x = 0; x < width; ++x) {
                int index = z * width + x;
                float curHeight = this.data[index];

                if (curHeight == originY) {
                    continue;
                }

                // Clamp newHeight within the selection area
                float newHeight = Math.min(maxY, data[index]);

                // Offset x,z to be 'real' coordinates
                int xr = x + originX;
                int zr = z + originZ;

                // We are keeping the topmost blocks so take that in account for the scale
                double scale = (double) (curHeight - originY) / (double) (newHeight - originY);

                // Depending on growing or shrinking we need to start at the bottom or top
                if (newHeight >= curHeight) {
                    // Set the top block of the column to be the same type (this might go wrong with rounding)
                    BlockState existing = session.getBlock(BlockVector3.at(xr, curHeight, zr));

                    // Skip water/lava
                    if (!existing.getBlockType().getMaterial().isLiquid()) {
                        setSnowLayer(xr, zr, newHeight);
                        ++blocksChanged;

                        // Grow -- start from 1 below top replacing airblocks
                        for (int y = (int) Math.floor(newHeight - 1 - originY); y >= 0; --y) {
                            if (y >= Math.floor(newHeight - 1 - originY - layerBlocks)) {
                                session.setBlock(BlockVector3.at(xr, originY + y, zr), fillerSnow);
                            } else {
                                int copyFrom = (int) Math.floor(y * scale);
                                BlockState block = session.getBlock(BlockVector3.at(xr, originY + copyFrom, zr));
                                session.setBlock(BlockVector3.at(xr, originY + y, zr), block);
                            }
                            ++blocksChanged;
                        }
                    }
                } else {
                    // Shrink -- start from bottom
                    for (int y = 0; y < (int) Math.floor(newHeight - originY); ++y) {
                        if (y >= (int) Math.floor(newHeight - originY - layerBlocks)) {
                            session.setBlock(BlockVector3.at(xr, originY + y, zr), fillerSnow);
                        } else {
                            int copyFrom = (int) Math.floor(y * scale);
                            BlockState block = session.getBlock(BlockVector3.at(xr, originY + copyFrom, zr));
                            session.setBlock(BlockVector3.at(xr, originY + y, zr), block);
                        }
                        ++blocksChanged;
                    }

                    setSnowLayer(xr, zr, newHeight);
                    ++blocksChanged;

                    // Fill rest with air
                    for (int y = (int) Math.floor(newHeight + 1); y <= Math.floor(curHeight); ++y) {
                        session.setBlock(BlockVector3.at(xr, y, zr), fillerAir);
                        ++blocksChanged;
                    }
                }
            }
        }

        // Drop trees to the floor -- TODO
        return blocksChanged;
    }

    private void setSnowLayer(int x, int z, float newHeight) throws MaxChangedBlocksException {
        int y = (int) Math.floor(newHeight);
        int numOfLayers = (int) ((newHeight - y) * 8) + 1;
        session.setBlock(BlockVector3.at(x, y, z), BlockTypes.SNOW.getState(ImmutableMap.of(this.layers, numOfLayers)));
    }
}
